package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException; // Importe a nova exceção
import org.desviante.model.Task;
import org.desviante.repository.CardRepository;
import org.desviante.repository.TaskRepository;
import org.desviante.service.dto.CreateTaskRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Task
 * @see CreateTaskRequest
 * @see GoogleTasksApiService
 * @see TaskRepository
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CardRepository cardRepository;
    private final GoogleTasksApiService googleApiService;

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

        // 1. Chama a API externa PRIMEIRO.
        var createTaskRequest = new CreateTaskRequest(listTitle, title, notes, due); // Passa o LocalDateTime
        com.google.api.services.tasks.model.Task googleTask = googleApiService.createTaskInList(createTaskRequest);

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
}