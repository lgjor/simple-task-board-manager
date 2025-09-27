package org.desviante.integration;

import org.desviante.integration.coordinator.IntegrationCoordinator;
import org.desviante.integration.event.SimpleEventPublisher;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.observer.GoogleTasksSyncObserver;
import org.desviante.integration.observer.CalendarSyncObserver;
import org.desviante.model.Card;
import org.desviante.service.TaskService;
import org.desviante.calendar.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Teste de integração simples do sistema.
 * 
 * <p>Este teste valida o funcionamento básico do sistema de integração
 * sem depender de APIs complexas, focando na comunicação entre componentes
 * e na publicação/processamento de eventos.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest(classes = IntegrationTestConfig.class)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "google.api.enabled=false"
})
class SimpleIntegrationTest {
    
    @Autowired
    private IntegrationCoordinator integrationCoordinator;
    
    @Autowired
    private SimpleEventPublisher eventPublisher;
    
    @Autowired
    private GoogleTasksSyncObserver googleTasksSyncObserver;
    
    @Autowired
    private CalendarSyncObserver calendarSyncObserver;
    
    @MockBean
    private TaskService mockTaskService;
    
    @MockBean
    private CalendarService mockCalendarService;
    
    @BeforeEach
    void setUp() {
        // Configurar mock para retornar Task válido
        org.desviante.model.Task mockTask = new org.desviante.model.Task();
        mockTask.setId(1L);
        mockTask.setGoogleTaskId("google-task-123");

        when(mockTaskService.createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong()))
                .thenReturn(mockTask);
        
        when(mockCalendarService.createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class)))
                .thenReturn(new org.desviante.calendar.dto.CalendarEventDTO());
    }
    
    @Test
    void shouldPublishAndProcessCardScheduledEvent() throws InterruptedException {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(1L)
                .title("Integration Test Card")
                .description("Testing event publishing and processing")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(scheduledDate)
                .build();
        
        CountDownLatch eventLatch = new CountDownLatch(1);
        
        // Configurar latch para aguardar processamento
        doAnswer(invocation -> {
            eventLatch.countDown();
            return null;
        }).when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        
        // Act - Publicar evento
        eventPublisher.publish(event);
        
        // Assert - Aguardar processamento
        assertTrue(eventLatch.await(5, TimeUnit.SECONDS), "Event should be processed within 5 seconds");
        
        // Verificar que o Google Tasks foi chamado
        verify(mockTaskService, times(1)).createTask(
                eq("Simple Task Board Manager"),
                eq("Integration Test Card"),
                anyString(),
                eq(scheduledDate.plusDays(1)),
                eq(1L)
        );
    }
    
    @Test
    void shouldCoordinateIntegrationThroughCoordinator() throws InterruptedException {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(2L)
                .title("Coordinator Test Card")
                .description("Testing integration coordination")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        CountDownLatch coordinatorLatch = new CountDownLatch(1);
        
        // Configurar latch para aguardar coordenação
        doAnswer(invocation -> {
            coordinatorLatch.countDown();
            return null;
        }).when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        
        // Act - Coordenar integração
        integrationCoordinator.onCardScheduled(card);
        
        // Assert - Aguardar processamento
        assertTrue(coordinatorLatch.await(5, TimeUnit.SECONDS), "Integration should be coordinated within 5 seconds");
        
        // Verificar que o Google Tasks foi chamado
        verify(mockTaskService, times(1)).createTask(
                eq("Simple Task Board Manager"),
                eq("Coordinator Test Card"),
                anyString(),
                eq(scheduledDate.plusDays(1)),
                eq(2L)
        );
    }
    
    @Test
    void shouldHandleMultipleEventsConcurrently() throws InterruptedException {
        // Arrange
        int numberOfEvents = 5;
        CountDownLatch allEventsLatch = new CountDownLatch(numberOfEvents);
        
        // Configurar latch para aguardar todos os eventos
        doAnswer(invocation -> {
            allEventsLatch.countDown();
            return null;
        }).when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        
        // Act - Publicar múltiplos eventos
        for (int i = 1; i <= numberOfEvents; i++) {
            Card card = Card.builder()
                    .id((long) i)
                    .title("Concurrent Test Card " + i)
                    .description("Testing concurrent event processing")
                    .scheduledDate(LocalDateTime.now().plusDays(i))
                    .dueDate(LocalDateTime.now().plusDays(i + 1))
                    .build();
            
            CardScheduledEvent event = CardScheduledEvent.builder()
                    .card(card)
                    .scheduledDate(card.getScheduledDate())
                    .build();
            
            eventPublisher.publish(event);
        }
        
        // Assert - Aguardar processamento de todos os eventos
        assertTrue(allEventsLatch.await(10, TimeUnit.SECONDS), "All events should be processed within 10 seconds");
        
        // Verificar que o Google Tasks foi chamado para todos os eventos
        verify(mockTaskService, times(numberOfEvents)).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
    }
    
    @Test
    void shouldMaintainIntegrationStats() throws InterruptedException {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(3L)
                .title("Stats Test Card")
                .description("Testing integration statistics")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        // Act - Coordenar integração
        integrationCoordinator.onCardScheduled(card);
        
        // Aguardar um pouco para processamento
        Thread.sleep(1000);
        
        // Assert - Verificar estatísticas
        var stats = integrationCoordinator.getStats();
        assertNotNull(stats);
        assertTrue(stats.getSuccessfulIntegrations() > 0);
        assertTrue(stats.getTotalIntegrations() > 0);
        
        // Verificar que o coordenador está disponível
        assertTrue(integrationCoordinator.isAvailable());
    }
    
    @Test
    void shouldHandleEventPublisherSubscription() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(4L)
                .title("Subscription Test Card")
                .description("Testing event publisher subscription")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(scheduledDate)
                .build();
        
        // Act - Publicar evento
        eventPublisher.publish(event);
        
        // Aguardar um pouco para processamento
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Assert - Verificar que os observadores foram notificados
        verify(mockTaskService, times(1)).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        verify(mockCalendarService, times(1)).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
    }
    
    @Test
    void shouldHandleAsyncEventProcessing() throws InterruptedException {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(5L)
                .title("Async Test Card")
                .description("Testing async event processing")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(scheduledDate)
                .build();
        
        CountDownLatch asyncLatch = new CountDownLatch(1);
        
        // Configurar latch para aguardar processamento assíncrono
        doAnswer(invocation -> {
            asyncLatch.countDown();
            return null;
        }).when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        
        // Act - Publicar evento de forma assíncrona
        eventPublisher.publishAsync(event);
        
        // Assert - Aguardar processamento assíncrono
        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS), "Async event should be processed within 5 seconds");
        
        // Verificar que o Google Tasks foi chamado
        verify(mockTaskService, times(1)).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
    }
    
    @Test
    void shouldVerifyObserverCompatibility() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(6L)
                .title("Compatibility Test Card")
                .description("Testing observer compatibility")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(scheduledDate)
                .build();
        
        // Act & Assert - Verificar compatibilidade dos observadores
        assertTrue(googleTasksSyncObserver.canHandle(event), "GoogleTasksSyncObserver should handle CardScheduledEvent");
        assertTrue(calendarSyncObserver.canHandle(event), "CalendarSyncObserver should handle CardScheduledEvent");
        
        // Verificar nomes e prioridades
        assertEquals("GoogleTasksSyncObserver", googleTasksSyncObserver.getObserverName());
        assertEquals("CalendarSyncObserver", calendarSyncObserver.getObserverName());
        assertEquals(10, googleTasksSyncObserver.getPriority());
        assertEquals(20, calendarSyncObserver.getPriority());
    }
    
    @Test
    void shouldHandleEventWithNullCard() {
        // Arrange
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(null)
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act - Publicar evento com card nulo
        assertDoesNotThrow(() -> {
            eventPublisher.publish(event);
        });
        
        // Assert - Verificar que nenhum serviço foi chamado
        verify(mockTaskService, never()).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        verify(mockCalendarService, never()).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
    }
    
    @Test
    void shouldHandleEventWithNullScheduledDate() {
        // Arrange
        Card card = Card.builder()
                .id(7L)
                .title("Null Date Test Card")
                .description("Testing event with null scheduled date")
                .scheduledDate(null)
                .dueDate(null)
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(null)
                .build();
        
        // Act - Publicar evento com data nula
        eventPublisher.publish(event);
        
        // Aguardar um pouco para processamento
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Assert - Verificar que nenhum serviço foi chamado (card não está agendado)
        verify(mockTaskService, never()).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        verify(mockCalendarService, never()).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
    }
}
