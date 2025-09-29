package org.desviante.service;

import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Card;
import org.desviante.model.Task;
import org.desviante.repository.CardRepository;
import org.desviante.repository.TaskRepository;
import org.desviante.service.dto.CreateTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o TaskService.
 * 
 * <p>Testa as operações de negócio relacionadas às tarefas, incluindo
 * criação de tarefas associadas a cards e integração com a API do Google Tasks.</p>
 * 
 * <p>Foca na validação de regras de negócio, tratamento de exceções e
 * verificação da integração correta com serviços externos.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see TaskService
 * @see Task
 * @see Card
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private GoogleTasksApiService googleApiService;

    @InjectMocks
    private TaskService taskService;

    private Card mockCard;
    private com.google.api.services.tasks.model.Task mockGoogleTask;

    @BeforeEach
    void setUp() {
        mockCard = new Card();
        mockCard.setId(1L);
        mockCard.setTitle("Mock Card");

        mockGoogleTask = new com.google.api.services.tasks.model.Task();
        mockGoogleTask.setId("google-task-123");
        mockGoogleTask.setTitle("Google Task Title");
    }

    @Test
    void createTask_shouldSucceed_whenCardExists() {
        // Arrange
        String listTitle = "My Board";
        String title = "Test Task";
        String notes = "Test Notes";
        LocalDateTime dueDate = LocalDateTime.now();
        Long cardId = 1L;

        // Mocks
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(googleApiService.createTaskInList(any(CreateTaskRequest.class))).thenReturn(mockGoogleTask);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        // CORREÇÃO: Chamada do método atualizada para incluir todos os parâmetros.
        Task result = taskService.createTask(listTitle, title, notes, dueDate, cardId);

        // Assert
        assertNotNull(result);
        assertEquals(cardId, result.getCardId());
        assertEquals(title, result.getTitle());
        assertEquals(notes, result.getNotes());
        assertEquals("google-task-123", result.getGoogleTaskId());
        assertTrue(result.isSent());

        // Verifica se o DTO correto foi passado para o serviço da API do Google
        ArgumentCaptor<CreateTaskRequest> requestCaptor = ArgumentCaptor.forClass(CreateTaskRequest.class);
        verify(googleApiService).createTaskInList(requestCaptor.capture());

        CreateTaskRequest capturedRequest = requestCaptor.getValue();
        // CORREÇÃO: Acessores do record corrigidos de .taskTitle() para .title()
        assertEquals(listTitle, capturedRequest.listTitle());
        assertEquals(title, capturedRequest.title());
        assertEquals(notes, capturedRequest.notes());
        assertEquals(dueDate, capturedRequest.due());
    }

    @Test
    void createTask_shouldThrowException_whenCardNotFound() {
        // Arrange
        long nonExistentCardId = 99L;
        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        // Act & Assert
        // CORREÇÃO: Chamada do método atualizada para corresponder à nova assinatura.
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask("Any List", "Any Title", "Any notes", null, nonExistentCardId);
        });

        // Garante que, se o card não existe, a API do Google nunca é chamada.
        verify(googleApiService, never()).createTaskInList(any());
        verify(taskRepository, never()).save(any());
    }
}
