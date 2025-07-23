package org.desviante.service;

import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Card;
import org.desviante.model.Task;
import org.desviante.repository.CardRepository;
import org.desviante.repository.TaskRepository;
import org.desviante.service.dto.CreateTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Modern way to initialize Mockito
class TaskServiceTest {

    @Mock // Mockito will create a mock implementation of this repository
    private TaskRepository taskRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private GoogleTasksApiService googleApiService;

    @InjectMocks // Mockito will inject the mocks above into this instance
    private TaskService taskService;

    private Card parentCard;
    private com.google.api.services.tasks.model.Task googleTask;

    @BeforeEach
    void setUp() {
        // Common setup for tests
        parentCard = new Card();
        parentCard.setId(1L);
        parentCard.setTitle("My Test List");

        googleTask = new com.google.api.services.tasks.model.Task();
        googleTask.setId("google-task-123");
    }

    @Test
    @DisplayName("Deve criar uma task com sucesso, salvá-la e sincronizá-la com a API do Google")
    void shouldCreateTaskSuccessfully() {
        // Arrange
        long cardId = 1L;
        String title = "Nova Tarefa";
        String notes = "Detalhes da tarefa";

        // --- Mocking Behavior ---
        // 1. When the card repository is asked for cardId, return our parentCard.
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(parentCard));

        // 2. CORREÇÃO: When the googleApiService is called with ANY CreateTaskRequest object, return our fake googleTask.
        when(googleApiService.createTaskInList(any(CreateTaskRequest.class))).thenReturn(googleTask);

        // 3. When the task repository saves ANY task, just return that same task.
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        // CORREÇÃO: Call the correct method name 'createTask'.
        Task result = taskService.createTask(cardId, title, notes);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals("google-task-123", result.getGoogleTaskId(), "O ID da task do Google deveria ter sido definido.");
        assertTrue(result.isSent(), "A task deveria ser marcada como enviada.");

        // --- Verification ---
        // We can use an ArgumentCaptor to capture the DTO and verify its contents.
        ArgumentCaptor<CreateTaskRequest> requestCaptor = ArgumentCaptor.forClass(CreateTaskRequest.class);

        // CORREÇÃO: Verify the googleApiService was called once with the captured request.
        verify(googleApiService).createTaskInList(requestCaptor.capture());
        assertEquals("My Test List", requestCaptor.getValue().listTitle());
        assertEquals(title, requestCaptor.getValue().taskTitle());

        // Verify that the repository's save method was called once.
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se o Card pai não for encontrado")
    void shouldThrowException_whenCardNotFound() {
        // Arrange: Mock the repository to find nothing.
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        // CORREÇÃO: O teste agora espera a exceção correta e mais específica que o serviço lança.
        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(99L, "Título", ""));

        // Verify that the Google API service was NEVER called.
        verify(googleApiService, never()).createTaskInList(any(CreateTaskRequest.class));
    }
}