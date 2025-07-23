package org.desviante.service;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.exception.GoogleApiServiceException; // Importe a exceção customizada
import org.desviante.service.dto.CreateTaskRequest; // Importe o DTO
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class GoogleTasksApiService {

    private final Tasks tasksService;

    /**
     * VERSÃO REATORADA: Usa um DTO para a requisição e lança uma exceção de runtime específica.
     */
    public Task createTaskInList(CreateTaskRequest request) {
        try {
            // 1. Encontra ou cria a lista de tarefas
            TaskList targetList = findOrCreateTaskList(request.listTitle());

            // 2. Cria e insere a nova tarefa
            log.info("Criando tarefa '{}' na lista '{}'", request.taskTitle(), request.listTitle());
            Task googleTask = new Task().setTitle(request.taskTitle()).setNotes(request.taskNotes());
            return tasksService.tasks().insert(targetList.getId(), googleTask).execute();

        } catch (IOException e) {
            // Encapsula a IOException em nossa exceção de runtime específica.
            throw new GoogleApiServiceException("Falha na comunicação com a API do Google Tasks.", e);
        }
    }

    private TaskList findOrCreateTaskList(String listTitle) throws IOException {
        List<TaskList> lists = tasksService.tasklists().list().execute().getItems();
        return lists.stream()
                .filter(list -> list.getTitle().equalsIgnoreCase(listTitle))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        log.info("Lista de tarefas '{}' não encontrada. Criando...", listTitle);
                        return tasksService.tasklists().insert(new TaskList().setTitle(listTitle)).execute();
                    } catch (IOException e) {
                        // Re-lança como uma exceção de runtime para ser capturada pelo bloco principal.
                        throw new RuntimeException(e);
                    }
                });
    }
}