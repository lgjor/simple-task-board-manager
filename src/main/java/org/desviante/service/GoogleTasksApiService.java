package org.desviante.service;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.desviante.exception.GoogleApiServiceException; // Importa a exceção customizada
import org.desviante.service.dto.CreateTaskRequest; // Importa o DTO
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

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
@RequiredArgsConstructor
@Log
@Profile("!test")
public class GoogleTasksApiService {

    private final Tasks tasksService;

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

        } catch (IOException e) {
            throw new GoogleApiServiceException("Falha na comunicação com a API do Google Tasks.", e);
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
        List<TaskList> lists = tasksService.tasklists().list().execute().getItems();

        // O 'orElseGet' é uma forma elegante de executar uma ação (criar a lista)
        // apenas se o 'findFirst' não encontrar nenhum resultado.
        return lists.stream()
                .filter(list -> list.getTitle().equalsIgnoreCase(listTitle))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        log.info("Lista de tarefas "+ listTitle + " não encontrada. Criando...");
                        TaskList newList = new TaskList().setTitle(listTitle);
                        return tasksService.tasklists().insert(newList).execute();
                    } catch (IOException e) {
                        // Re-lança como uma exceção de runtime para ser capturada pelo bloco try-catch principal.
                        // Isso simplifica o tratamento de exceções no chamador.
                        throw new GoogleApiServiceException("Falha ao criar a lista de tarefas '" + listTitle + "'.", e);
                    }
                });
    }
}
