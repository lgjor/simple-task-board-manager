package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException; // Importe a nova exceção
import org.desviante.model.Card;
import org.desviante.model.Task;
import org.desviante.repository.CardRepository;
import org.desviante.repository.TaskRepository;
import org.desviante.service.dto.CreateTaskRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CardRepository cardRepository;
    private final GoogleTasksApiService googleApiService;

    @Transactional
    public Task createTask(Long cardId, String title, String notes) {
        Card parentCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));

        // 1. Chama a API externa PRIMEIRO. Se isso falhar, uma GoogleApiServiceException (RuntimeException)
        // será lançada, e a anotação @Transactional irá reverter a transação. Nada será salvo.
        var createTaskRequest = new CreateTaskRequest(parentCard.getTitle(), title, notes);
        com.google.api.services.tasks.model.Task googleTask = googleApiService.createTaskInList(createTaskRequest);

        // 2. SOMENTE se a chamada externa for bem-sucedida, criamos e salvamos a entidade local.
        Task localTask = new Task();
        localTask.setCardId(cardId);
        localTask.setTitle(title);
        localTask.setNotes(notes);
        localTask.setGoogleTaskId(googleTask.getId());
        localTask.setSent(true);

        // Esta operação de salvamento agora faz parte da mesma transação que será confirmada (commit)
        // ao final do método.
        return taskRepository.save(localTask);
    }
}