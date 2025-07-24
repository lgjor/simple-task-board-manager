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

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CardRepository cardRepository;
    private final GoogleTasksApiService googleApiService;

    /**
     * Cria uma nova tarefa no Google Tasks e salva a entidade localmente.
     * A assinatura foi atualizada para incluir a data e hora de vencimento.
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