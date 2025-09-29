package org.desviante.service;

import lombok.extern.slf4j.Slf4j;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Task;
import org.desviante.repository.CardRepository;
import org.desviante.repository.TaskRepository;
import org.desviante.service.dto.CreateTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Gerencia as operações de negócio relacionadas à sincronização com Google Tasks.
 * 
 * <p>Responsável por implementar a lógica de negócio para criação e sincronização
 * de tarefas com a API do Google Tasks. Esta camada de serviço garante a
 * integridade dos dados através de validações antes das operações de persistência
 * e coordena a comunicação entre o sistema local e a API externa.</p>
 * 
 * <p>Implementa estratégia de sincronização onde a API externa é chamada primeiro,
 * e somente após sucesso é criada a entidade local. Isso garante consistência
 * entre os sistemas e previne dados órfãos no sistema local.</p>
 * 
 * <p>Utiliza transações para garantir consistência dos dados e conversões
 * adequadas de tipos de data para compatibilidade com a API do Google Tasks.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Task
 * @see CreateTaskRequest
 * @see GoogleTasksApiService
 * @see TaskRepository
 */
@Service
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final CardRepository cardRepository;
    private final GoogleTasksApiService googleApiService;

    /**
     * Construtor que inicializa o serviço com as dependências necessárias.
     * 
     * @param taskRepository repositório para operações de tarefas
     * @param cardRepository repositório para operações de cards
     * @param googleApiService serviço da API do Google Tasks (opcional)
     */
    public TaskService(TaskRepository taskRepository, CardRepository cardRepository, 
                      @Autowired(required = false) GoogleTasksApiService googleApiService) {
        this.taskRepository = taskRepository;
        this.cardRepository = cardRepository;
        this.googleApiService = googleApiService;
    }

    /**
     * Cria uma nova tarefa no Google Tasks e salva a entidade localmente.
     * 
     * <p>Implementa estratégia de sincronização onde a API externa é chamada
     * primeiro para garantir que a tarefa seja criada no Google Tasks antes
     * de persistir localmente. Isso previne inconsistências entre os sistemas.</p>
     * 
     * <p>Valida a existência do card pai antes de prosseguir, garantindo
     * integridade referencial. Converte adequadamente tipos de data para
     * compatibilidade com a API do Google Tasks (LocalDateTime para OffsetDateTime).</p>
     * 
     * <p>Após sucesso na API externa, cria a entidade local com flag sent=true
     * para indicar que a sincronização foi bem-sucedida.</p>
     * 
     * @param listTitle título da lista no Google Tasks
     * @param title título da tarefa
     * @param notes notas ou descrição da tarefa
     * @param due data e hora de vencimento da tarefa (pode ser null)
     * @param cardId identificador do card associado à tarefa
     * @return tarefa criada com ID do Google Tasks e dados locais
     * @throws ResourceNotFoundException se o card pai não for encontrado
     * @throws RuntimeException se a API do Google Tasks falhar
     */
    @Transactional
    public Task createTask(String listTitle, String title, String notes, LocalDateTime due, Long cardId) { // <-- Changed to LocalDateTime
        // Valida se o card pai existe.
        cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));

        // Verifica se o Google API está disponível
        if (googleApiService == null) {
            log.warn("GoogleTasksApiService não está disponível! Criando apenas task local.");
            // Cria apenas a entidade local sem sincronização com Google Tasks
            Task localTask = new Task();
            localTask.setCardId(cardId);
            localTask.setListTitle(listTitle);
            localTask.setTitle(title);
            localTask.setNotes(notes);
            localTask.setGoogleTaskId(null); // Sem ID do Google
            localTask.setSent(false); // Não foi enviado

            // Converte o LocalDateTime para OffsetDateTime para salvar no banco de dados local.
            if (due != null) {
                localTask.setDue(due.atOffset(ZoneOffset.UTC));
            }

            log.info("Task local criada com sucesso (ID: {})", localTask.getId());
            return taskRepository.save(localTask);
        }

        log.info("Criando task no Google Tasks: {} - {}", title, due);
        // 1. Chama a API externa PRIMEIRO.
        var createTaskRequest = new CreateTaskRequest(listTitle, title, notes, due); // Passa o LocalDateTime
        com.google.api.services.tasks.model.Task googleTask = googleApiService.createTaskInList(createTaskRequest);
        log.info("Task criada no Google Tasks com ID: {}", googleTask.getId());

        // 2. SOMENTE se a chamada externa for bem-sucedida, criamos e salvamos a entidade local.
        Task localTask = new Task();
        localTask.setCardId(cardId);
        localTask.setListTitle(listTitle);
        localTask.setTitle(title);
        localTask.setNotes(notes);
        localTask.setGoogleTaskId(googleTask.getId());
        localTask.setSent(true);

        // Converte o LocalDateTime para OffsetDateTime para salvar no banco de dados local.
        if (due != null) {
            // Não precisa mais de atStartOfDay()
            localTask.setDue(due.atOffset(ZoneOffset.UTC));
        }

        return taskRepository.save(localTask);
    }
    
    /**
     * Busca uma tarefa pelo ID do card.
     * 
     * @param cardId ID do card
     * @return Optional contendo a tarefa se encontrada, ou vazio caso contrário
     */
    public Optional<Task> findByCardId(Long cardId) {
        return taskRepository.findByCardId(cardId);
    }
    
    /**
     * Remove uma tarefa do Google Tasks e do banco local.
     * 
     * @param cardId ID do card
     */
    @Transactional
    public void deleteTaskByCardId(Long cardId) {
        log.info("Removendo task para card: {}", cardId);
        
        // Buscar a tarefa local
        Optional<Task> taskOpt = taskRepository.findByCardId(cardId);
        if (taskOpt.isEmpty()) {
            log.warn("Nenhuma task encontrada para card: {}", cardId);
            return;
        }
        
        Task task = taskOpt.get();
        
        // Se tem Google Task ID, remover do Google Tasks
        if (task.getGoogleTaskId() != null && !task.getGoogleTaskId().isEmpty()) {
            try {
                if (googleApiService != null) {
                    log.info("Removendo task do Google Tasks: {}", task.getGoogleTaskId());
                    googleApiService.deleteTask(task.getGoogleTaskId());
                    log.info("Task removida do Google Tasks com sucesso");
                } else {
                    log.warn("GoogleTasksApiService não está disponível! Removendo apenas do banco local.");
                }
            } catch (Exception e) {
                log.error("Erro ao remover task do Google Tasks: {}", e.getMessage(), e);
                // Continua removendo do banco local mesmo se falhar no Google
            }
        }
        
        // Remover do banco local
        taskRepository.deleteByCardId(cardId);
        log.info("Task removida do banco local para card: {}", cardId);
    }
}