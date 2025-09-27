package org.desviante.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.tasks.Tasks;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.desviante.config.GoogleApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.desviante.exception.GoogleApiServiceException; // Importa a exceção customizada
import org.desviante.service.dto.CreateTaskRequest; // Importa o DTO
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Gerencia a comunicação com a API do Google Tasks.
 * 
 * <p>Responsável por implementar todas as operações de comunicação com a
 * API externa do Google Tasks, incluindo criação de tarefas e gerenciamento
 * de listas. Esta camada de serviço abstrai a complexidade da API do Google
 * e fornece uma interface simplificada para o sistema local.</p>
 * 
 * <p>Implementa funcionalidades específicas como construção inteligente de notas
 * (incluindo informações de horário), formatação adequada de datas para RFC3339,
 * e gerenciamento automático de listas de tarefas (criação se não existir).</p>
 * 
 * <p>Utiliza logging para rastreamento de operações e tratamento robusto de
 * exceções para garantir que falhas na API externa sejam adequadamente
 * propagadas para o sistema local.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CreateTaskRequest
 * @see GoogleApiServiceException
 * @see Tasks
 */
@Service
@Slf4j
@Profile("!test")
public class GoogleTasksApiService {

    private Tasks tasksService;
    private final GoogleApiConfig googleApiConfig;
    private final NetHttpTransport httpTransport;

    /**
     * Construtor que injeta o serviço do Google Tasks de forma opcional.
     * Se o serviço não puder ser criado (por falta de credenciais ou falha na autorização),
     * a integração ficará desativada, mas a aplicação continuará funcionando.
     * @param tasksService O serviço do Google Tasks, que pode ser nulo.
     * @param googleApiConfig A configuração da API do Google para reautenticação.
     * @param httpTransport O transporte HTTP para as requisições.
     */
    @Autowired
    public GoogleTasksApiService(
            @Autowired(required = false) Tasks tasksService,
            @Autowired(required = false) GoogleApiConfig googleApiConfig,
            @Autowired(required = false) NetHttpTransport httpTransport
    ) {
        log.info("GoogleTasksApiService sendo inicializado!");
        log.info("tasksService: {}", tasksService != null ? "disponível" : "null");
        log.info("googleApiConfig: {}", googleApiConfig != null ? "disponível" : "null");
        log.info("httpTransport: {}", httpTransport != null ? "disponível" : "null");
        
        this.tasksService = tasksService;
        this.googleApiConfig = googleApiConfig;
        this.httpTransport = httpTransport;
    }

    /**
     * Cria uma tarefa no Google Tasks com informações completas.
     * 
     * <p>Implementa lógica inteligente para construção de notas, incluindo
     * informações de horário quando fornecidas pelo usuário. A API do Google
     * Tasks tem limitações para horários específicos, então esta informação
     * é anexada nas notas como solução alternativa.</p>
     * 
     * <p>Gerenciamento automático de listas: se a lista especificada não
     * existir, ela é criada automaticamente. Formata adequadamente as datas
     * para o padrão RFC3339 exigido pela API do Google.</p>
     * 
     * @param request dados da tarefa a ser criada (título, notas, data de vencimento)
     * @return tarefa criada no Google Tasks com ID gerado
     * @throws GoogleApiServiceException se houver falha na comunicação com a API
     */
    public Task createTaskInList(CreateTaskRequest request) {
        log.info("🔧 GOOGLE TASKS API SERVICE - Iniciando criação de task: {}", request.title());
        
        // Verifica se o serviço do Google está disponível, e tenta inicializar se não estiver.
        if (tasksService == null) {
            log.warn("⚠️ GOOGLE TASKS API SERVICE - Serviço do Google Tasks não inicializado. Tentando inicialização sob demanda...");
            log.warn("⚠️ GOOGLE TASKS API SERVICE - googleApiConfig disponível: {}", googleApiConfig != null ? "sim" : "não");
            log.warn("⚠️ GOOGLE TASKS API SERVICE - httpTransport disponível: {}", httpTransport != null ? "sim" : "não");
            
            try {
                // Tenta criar o serviço, o que vai disparar a autenticação via navegador se necessário.
                log.info("🔧 GOOGLE TASKS API SERVICE - Chamando createAndSetTasksService()...");
                createAndSetTasksService();
                log.info("✅ GOOGLE TASKS API SERVICE - Serviço do Google Tasks inicializado com sucesso sob demanda.");
            } catch (Exception e) {
                log.error("❌ GOOGLE TASKS API SERVICE - Falha ao inicializar o serviço do Google Tasks sob demanda.", e);
                // Monta a mensagem de erro informativa que o usuário está vendo.
                String userMessage = "A integração com Google Tasks não está configurada.\n" +
                                     "Para habilitar:\n" +
                                     "1. Configure as credenciais do Google em src/main/resources/auth/credentials.json\n" +
                                     "2. Execute a autenticação inicial\n" +
                                     "3. Reinicie a aplicação\n\n" +
                                     "A tarefa foi salva localmente, mas não foi sincronizada com o Google Tasks.";
                throw new GoogleApiServiceException(userMessage, e);
            }
        } else {
            log.info("✅ GOOGLE TASKS API SERVICE - Serviço do Google Tasks já está disponível");
        }

        try {
            // 1. Encontra ou cria a lista de tarefas no Google.
            TaskList targetList = findOrCreateTaskList(request.listTitle());

            // --- MUDANÇA PRINCIPAL: Lógica para construir as notas ---
            StringBuilder notesBuilder = new StringBuilder();
            if (request.notes() != null && !request.notes().isBlank()) {
                notesBuilder.append(request.notes());
            }

            // Anexa a informação de horário, se uma foi fornecida pelo usuário.
            if (request.due() != null && !request.due().toLocalTime().equals(LocalTime.MIDNIGHT)) {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String timeInfo = "Horário: " + request.due().toLocalTime().format(timeFormatter);

                // Adiciona espaçamento se já existiam notas.
                if (notesBuilder.length() > 0) {
                    notesBuilder.append("\n\n");
                }
                notesBuilder.append(timeInfo);
            }
            // --- FIM DA MUDANÇA PRINCIPAL ---

            // 2. Cria o payload da tarefa usando as notas construídas.
            Task taskPayload = new Task()
                    .setTitle(request.title())
                    .setNotes(notesBuilder.toString()); // Usa a string final das notas

            // 3. Adiciona a data de vencimento (a API usará apenas a parte da data).
            if (request.due() != null) {
                String rfc3339FormattedDateTime = request.due()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                taskPayload.setDue(rfc3339FormattedDateTime);
            }

            // 4. Loga e insere a nova tarefa.
            log.info("Criando tarefa '" + request.title() + "' na lista '" + request.listTitle() + "' com vencimento em " + taskPayload.getDue());
            return tasksService.tasks().insert(targetList.getId(), taskPayload).execute();

        } catch (TokenResponseException e) {
            // Erro específico de token inválido (ex: revogado pelo usuário)
            if (e.getDetails() != null && "invalid_grant".equals(e.getDetails().getError())) {
                log.warn("🔑 GOOGLE TASKS API SERVICE - Token inválido detectado (invalid_grant). Tentando reautenticar automaticamente...");
                log.info("🌐 GOOGLE TASKS API SERVICE - O navegador será aberto para você fazer login novamente no Google.");
                try {
                    return reAuthenticateAndRetry(request);
                } catch (Exception ex) {
                    log.error("❌ GOOGLE TASKS API SERVICE - Falha crítica durante o processo de reautenticação. A tarefa não pôde ser criada no Google.", ex);
                    throw new GoogleApiServiceException("Falha ao tentar reautenticar com o Google. Por favor, reinicie a aplicação.", ex);
                }
            }
            // Outros erros de token
            String userFriendlyMessage = String.format("Erro de autenticação com Google: %s", e.getDetails() != null ? e.getDetails().getErrorDescription() : e.getMessage());
            log.error(userFriendlyMessage, e);
            throw new GoogleApiServiceException(userFriendlyMessage, e);

        } catch (GoogleJsonResponseException e) {
            // Erro específico da API do Google com detalhes em JSON
            String details = e.getDetails() != null ? e.getDetails().getMessage() : "Nenhum detalhe adicional da API.";
            String userFriendlyMessage = String.format("Erro da API do Google: %s (Código: %d). Detalhes: %s",
                    e.getStatusMessage(), e.getStatusCode(), details);
            log.error("Falha na API do Google ao criar tarefa: {}", userFriendlyMessage, e);
            throw new GoogleApiServiceException(userFriendlyMessage, e);

        } catch (IOException e) {
            // Erro genérico de comunicação (rede, etc.)
            String errorMessage = "Falha na comunicação com a API do Google Tasks. Verifique sua conexão com a internet ou se as permissões da aplicação foram revogadas.";
            log.error(errorMessage, e);
            throw new GoogleApiServiceException(errorMessage, e);
        }
    }

    /**
     * Encontra uma lista de tarefas pelo título ou cria uma nova se não existir.
     * 
     * <p>Implementa busca case-insensitive para evitar duplicação de listas.
     * Se a lista não for encontrada, cria automaticamente uma nova lista
     * com o título especificado. Utiliza streams para busca eficiente
     * e tratamento elegante de criação condicional.</p>
     * 
     * <p>O método é robusto e evita a criação de listas duplicadas através
     * de busca precisa antes da criação. Logs informativos para rastreamento
     * de operações de criação de listas.</p>
     * 
     * @param listTitle título da lista a ser encontrada ou criada
     * @return lista de tarefas existente ou recém-criada
     * @throws IOException se houver falha na comunicação com a API
     * @throws GoogleApiServiceException se houver falha ao criar nova lista
     */
    private TaskList findOrCreateTaskList(String listTitle) throws IOException {
        // Verifica se o serviço do Google está disponível
        if (tasksService == null) {
            throw new GoogleApiServiceException("Google Tasks API não está configurada.", null);
        }

        List<TaskList> lists = tasksService.tasklists().list().execute().getItems();

        // O 'orElseGet' é uma forma elegante de executar uma ação (criar a lista)
        // apenas se o 'findFirst' não encontrar nenhum resultado.
        return lists.stream()
                .filter(list -> list.getTitle().equalsIgnoreCase(listTitle))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        log.info("Lista de tarefas '{}' não encontrada. Criando...", listTitle);
                        TaskList newList = new TaskList().setTitle(listTitle);
                        return tasksService.tasklists().insert(newList).execute();
                    } catch (GoogleJsonResponseException e) {
                        String details = e.getDetails() != null ? e.getDetails().getMessage() : "Nenhum detalhe adicional.";
                        String userFriendlyMessage = String.format("Falha ao criar a lista de tarefas '%s'. Erro da API: %s (Código: %d). Detalhes: %s",
                                listTitle, e.getStatusMessage(), e.getStatusCode(), details);
                        log.error("Falha na API do Google ao criar lista de tarefas: {}", userFriendlyMessage, e);
                        // Re-lança como uma exceção de runtime para ser capturada pelo bloco try-catch principal.
                        throw new GoogleApiServiceException(userFriendlyMessage, e);
                    } catch (IOException e) {
                        String errorMessage = "Falha ao criar a lista de tarefas '" + listTitle + "'. Verifique sua conexão com a internet.";
                        log.error(errorMessage, e);
                        // Re-lança como uma exceção de runtime para ser capturada pelo bloco try-catch principal.
                        // Isso simplifica o tratamento de exceções no chamador.
                        throw new GoogleApiServiceException(errorMessage, e);
                    }
                });
    }

    /**
     * Verifica se o Google Tasks API está disponível.
     * 
     * @return true se o serviço estiver configurado, false caso contrário
     */
    public boolean isGoogleTasksAvailable() {
        return tasksService != null;
    }

    /**
     * Lida com o erro 'invalid_grant', que geralmente significa que o token de atualização
     * foi revogado. A solução é limpar as credenciais armazenadas para forçar uma
     * nova autenticação na próxima inicialização.
     */
    private void handleInvalidGrant() {
        log.error("O token de atualização do Google foi revogado ou expirou (invalid_grant). Removendo credenciais antigas para forçar nova autenticação.");
        try {
            File tokenDirectory = new File(GoogleApiConfig.TOKENS_DIRECTORY_PATH);
            if (tokenDirectory.exists() && tokenDirectory.isDirectory()) {
                for (File file : tokenDirectory.listFiles()) {
                    if (!file.delete()) {
                        log.warn("Não foi possível deletar o arquivo de credencial: {}", file.getAbsolutePath());
                    }
                }
                log.info("Arquivos de credenciais do Google removidos com sucesso.");
            }
        } catch (Exception ex) {
            log.error("Falha ao tentar remover o diretório de credenciais antigas.", ex);
        }
    }

    /**
     * Executa o fluxo de reautenticação completo.
     * <p>
     * Este método é chamado quando um erro 'invalid_grant' é detectado. Ele limpa
     * as credenciais antigas, dispara o fluxo de autorização OAuth2 (que abrirá o
     * navegador para o usuário) e recria o serviço do Google Tasks com as novas
     * credenciais válidas.
     * </p>
     * @throws IOException se houver um erro de I/O durante a autorização.
     * @throws GeneralSecurityException se houver um erro de segurança.
     */
    private Task reAuthenticateAndRetry(CreateTaskRequest request) throws IOException, GeneralSecurityException {
        handleInvalidGrant(); // Limpa as credenciais antigas
        createAndSetTasksService(); // Recria o serviço, disparando a autenticação
        log.info("Serviço do Google Tasks foi recriado com novas credenciais.");
        return createTaskInList(request);
    }

    /**
     * Cria (ou recria) a instância do serviço do Google Tasks.
     * <p>
     * Este método centraliza a lógica de autorização e criação do serviço. Ele dispara
     * o fluxo de autorização OAuth2, que abrirá o navegador para o usuário se
     * necessário, e então constrói e atribui a instância do serviço Tasks.
     * </p>
     * @throws IOException se houver um erro de I/O durante a autorização.
     * @throws GeneralSecurityException se houver um erro de segurança ou de configuração.
     */
    private void createAndSetTasksService() throws IOException, GeneralSecurityException {
        log.info("🔧 GOOGLE TASKS API SERVICE - Iniciando createAndSetTasksService()...");
        
        if (googleApiConfig == null || httpTransport == null) {
            log.error("❌ GOOGLE TASKS API SERVICE - Componentes de configuração do Google não estão disponíveis");
            log.error("❌ GOOGLE TASKS API SERVICE - googleApiConfig: {}", googleApiConfig != null ? "disponível" : "null");
            log.error("❌ GOOGLE TASKS API SERVICE - httpTransport: {}", httpTransport != null ? "disponível" : "null");
            throw new GeneralSecurityException("Componentes de configuração do Google não estão disponíveis. Verifique se 'google.api.enabled=true' está ativo.");
        }

        log.info("✅ GOOGLE TASKS API SERVICE - Componentes de configuração disponíveis, iniciando autorização...");
        log.info("🌐 GOOGLE TASKS API SERVICE - O navegador será aberto para você fazer login no Google Tasks.");
        
        // O método authorize já abre o navegador e lida com o fluxo OAuth2.
        log.info("🔧 GOOGLE TASKS API SERVICE - Chamando googleApiConfig.authorize()...");
        Credential credential = googleApiConfig.authorize(httpTransport);
        
        log.info("✅ GOOGLE TASKS API SERVICE - Credencial obtida, criando serviço Tasks...");
        this.tasksService = new Tasks.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(GoogleApiConfig.APPLICATION_NAME)
                .build();
        
        log.info("🎉 GOOGLE TASKS API SERVICE - Serviço Tasks criado com sucesso!");
    }
    
    /**
     * Remove uma tarefa do Google Tasks.
     * 
     * @param taskId ID da tarefa no Google Tasks
     * @throws IOException se houver falha na comunicação com a API
     * @throws GoogleApiServiceException se houver falha na operação
     */
    public void deleteTask(String taskId) throws IOException {
        log.info("🔧 GOOGLE TASKS API SERVICE - Removendo task do Google Tasks: {}", taskId);
        
        if (tasksService == null) {
            log.warn("⚠️ GOOGLE TASKS API SERVICE - Serviço do Google Tasks não inicializado. Tentando inicialização sob demanda...");
            try {
                createAndSetTasksService();
                log.info("✅ GOOGLE TASKS API SERVICE - Serviço do Google Tasks inicializado com sucesso sob demanda.");
            } catch (Exception e) {
                log.error("❌ GOOGLE TASKS API SERVICE - Falha ao inicializar o serviço do Google Tasks sob demanda.", e);
                throw new GoogleApiServiceException("A integração com Google Tasks não está configurada.", e);
            }
        }
        
        try {
            // Buscar a tarefa em todas as listas para encontrar onde ela está
            List<TaskList> lists = tasksService.tasklists().list().execute().getItems();
            com.google.api.services.tasks.model.Task taskToDelete = null;
            String listId = null;
            
            // Procurar a tarefa em todas as listas
            for (TaskList list : lists) {
                try {
                    com.google.api.services.tasks.model.Task task = tasksService.tasks().get(list.getId(), taskId).execute();
                    if (task != null) {
                        taskToDelete = task;
                        listId = list.getId();
                        log.info("🔍 GOOGLE TASKS API SERVICE - Task encontrada na lista: {} (ID: {})", list.getTitle(), listId);
                        break;
                    }
                } catch (GoogleJsonResponseException e) {
                    if (e.getStatusCode() != 404) {
                        // Se não for 404, re-lançar o erro
                        throw e;
                    }
                    // Se for 404, continuar procurando na próxima lista
                }
            }
            
            if (taskToDelete == null || listId == null) {
                log.warn("⚠️ GOOGLE TASKS API SERVICE - Task não encontrada em nenhuma lista do Google Tasks: {}", taskId);
                // Não é um erro crítico, a task já foi removida
                return;
            }
            
            // Remover a tarefa da lista onde foi encontrada
            tasksService.tasks().delete(listId, taskId).execute();
            log.info("✅ GOOGLE TASKS API SERVICE - Task removida com sucesso do Google Tasks: {} da lista: {}", taskId, listId);
            
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                log.warn("⚠️ GOOGLE TASKS API SERVICE - Task não encontrada no Google Tasks: {}", taskId);
                // Não é um erro crítico, a task já foi removida
                return;
            }
            String details = e.getDetails() != null ? e.getDetails().getMessage() : "Nenhum detalhe adicional da API.";
            String userFriendlyMessage = String.format("Erro da API do Google ao remover task: %s (Código: %d). Detalhes: %s",
                    e.getStatusMessage(), e.getStatusCode(), details);
            log.error(userFriendlyMessage, e);
            throw new GoogleApiServiceException(userFriendlyMessage, e);
            
        } catch (IOException e) {
            String errorMessage = "Falha ao remover task do Google Tasks. Verifique sua conexão com a internet.";
            log.error(errorMessage, e);
            throw new GoogleApiServiceException(errorMessage, e);
        }
    }

}
