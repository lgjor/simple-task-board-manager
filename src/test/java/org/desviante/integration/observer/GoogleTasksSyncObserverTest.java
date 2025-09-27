package org.desviante.integration.observer;

import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;
import org.desviante.integration.retry.RetryExecutor;
import org.desviante.integration.retry.RetryResult;
import org.desviante.model.Card;
import org.desviante.service.TaskService;
import org.desviante.service.BoardService;
import org.desviante.service.BoardColumnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para GoogleTasksSyncObserver.
 * 
 * <p>Estes testes verificam o funcionamento do observador de sincronização
 * com Google Tasks, incluindo criação e atualização de tasks.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class GoogleTasksSyncObserverTest {
    
    @Mock
    private TaskService taskService;
    
    private GoogleTasksSyncObserver observer;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock dos serviços necessários
        BoardService mockBoardService = mock(BoardService.class);
        BoardColumnService mockBoardColumnService = mock(BoardColumnService.class);
        
        // Configurar mocks para retornar valores válidos
        org.desviante.model.Board mockBoard = new org.desviante.model.Board();
        mockBoard.setId(1L);
        mockBoard.setName("Test Board");
        
        org.desviante.model.BoardColumn mockColumn = new org.desviante.model.BoardColumn();
        mockColumn.setId(1L);
        mockColumn.setBoardId(1L);
        
        when(mockBoardService.getBoardById(anyLong())).thenReturn(java.util.Optional.of(mockBoard));
        when(mockBoardColumnService.getColumnById(anyLong())).thenReturn(java.util.Optional.of(mockColumn));
        
        // Criar um RetryExecutor mock para o teste que executa a operação diretamente
        RetryExecutor mockRetryExecutor = mock(RetryExecutor.class);
        RetryResult mockResult = RetryResult.builder()
                .successful(true)
                .totalAttempts(1)
                .build();
        
        // Configurar o mock para executar a operação passada como parâmetro
        when(mockRetryExecutor.execute(any(), any(), any(), any())).thenAnswer(invocation -> {
            java.util.function.Supplier<?> operation = invocation.getArgument(0);
            try {
                operation.get();
            } catch (Exception e) {
                // Se a operação falhar, criar um resultado de falha
                return RetryResult.builder()
                        .successful(false)
                        .totalAttempts(1)
                        .errorMessage(e.getMessage())
                        .finalException(e)
                        .build();
            }
            return mockResult;
        });
        
        observer = new GoogleTasksSyncObserver(taskService, mockBoardService, mockBoardColumnService, mockRetryExecutor);
        
        // Configurar mock para retornar Task válido
        org.desviante.model.Task mockTask = new org.desviante.model.Task();
        mockTask.setId(1L);
        mockTask.setGoogleTaskId("google-task-123");
        
        when(taskService.createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong()))
                .thenReturn(mockTask);
    }
    
    @Test
    void shouldCreateTaskOnFirstScheduling() throws Exception {
        // Arrange
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .description("Test Description")
                .scheduledDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(card.getScheduledDate())
                .previousScheduledDate(null) // Primeira vez sendo agendado
                .build();
        
        // Act
        observer.handle(event);
        
        // Assert
        verify(taskService, times(1)).createTask(
            eq("Simple Task Board Manager"),
            eq("Test Card"),
            anyString(),
            any(LocalDateTime.class),
            eq(1L)
        );
    }
    
    @Test
    void shouldUpdateTaskOnRescheduling() throws Exception {
        // Arrange
        Card card = Card.builder()
                .id(1L)
                .title("Updated Card")
                .description("Updated Description")
                .scheduledDate(LocalDateTime.now().plusDays(1))
                .dueDate(LocalDateTime.now().plusDays(2))
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(card.getScheduledDate())
                .previousScheduledDate(LocalDateTime.now()) // Já estava agendado
                .build();
        
        // Act
        observer.handle(event);
        
        // Assert
        verify(taskService, times(1)).createTask(
            eq("Simple Task Board Manager"),
            eq("Updated Card"),
            anyString(),
            any(LocalDateTime.class),
            eq(1L)
        );
    }
    
    @Test
    void shouldHandleNullEvent() throws Exception {
        // Act & Assert
        observer.handle(null);
        
        // Verificar que nenhuma task foi criada
        verify(taskService, never()).createTask(anyString(), anyString(), anyString(), any(), anyLong());
    }
    
    @Test
    void shouldHandleEventWithNullCard() throws Exception {
        // Arrange
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(null)
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act & Assert
        observer.handle(event);
        
        // Verificar que nenhuma task foi criada
        verify(taskService, never()).createTask(anyString(), anyString(), anyString(), any(), anyLong());
    }
    
    @Test
    void shouldHandleTaskServiceException() throws Exception {
        // Arrange
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(card.getScheduledDate())
                .build();
        
        doThrow(new RuntimeException("Task service error")).when(taskService)
                .createTask(anyString(), anyString(), anyString(), any(), anyLong());
        
        // Act & Assert
        try {
            observer.handle(event);
        } catch (Exception e) {
            // Esperado que a exceção seja propagada
            verify(taskService, times(1)).createTask(anyString(), anyString(), anyString(), any(), anyLong());
        }
    }
    
    @Test
    void shouldReturnCorrectObserverInfo() {
        // Assert
        assertEquals("GoogleTasksSyncObserver", observer.getObserverName());
        assertEquals(10, observer.getPriority());
    }
    
    @Test
    void shouldHandleCardScheduledEvent() {
        // Arrange
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(Card.builder().id(1L).build())
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act & Assert
        assertTrue(observer.canHandle(event));
    }
    
    @Test
    void shouldHandleCardUnscheduledEvent() {
        // Arrange
        var event = CardUnscheduledEvent.builder()
                .card(Card.builder().id(1L).build())
                .previousScheduledDate(LocalDateTime.now())
                .build();
        
        // Act & Assert
        assertTrue(observer.canHandle(event));
    }
    
    @Test
    void shouldHandleCardUpdatedEvent() {
        // Arrange
        var event = CardUpdatedEvent.builder()
                .card(Card.builder().id(1L).build())
                .build();
        
        // Act & Assert
        assertTrue(observer.canHandle(event));
    }
}
