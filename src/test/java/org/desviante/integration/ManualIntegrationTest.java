package org.desviante.integration;

import org.desviante.integration.coordinator.DefaultIntegrationCoordinator;
import org.desviante.integration.event.SimpleEventPublisher;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.observer.GoogleTasksSyncObserver;
import org.desviante.integration.observer.CalendarSyncObserver;
import org.desviante.model.Card;
import org.desviante.service.TaskService;
import org.desviante.calendar.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Teste de integração manual do sistema.
 * 
 * <p>Este teste valida o funcionamento básico do sistema de integração
 * sem depender de configuração Spring, focando na comunicação entre componentes
 * e na publicação/processamento de eventos.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class ManualIntegrationTest {
    
    @Mock
    private TaskService mockTaskService;
    
    @Mock
    private CalendarService mockCalendarService;
    
    private SimpleEventPublisher eventPublisher;
    private DefaultIntegrationCoordinator integrationCoordinator;
    private GoogleTasksSyncObserver googleTasksSyncObserver;
    private CalendarSyncObserver calendarSyncObserver;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configurar mocks
        doNothing().when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        doNothing().when(mockCalendarService).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
        
        // Criar componentes
        eventPublisher = new SimpleEventPublisher();
        integrationCoordinator = new DefaultIntegrationCoordinator(eventPublisher);
        googleTasksSyncObserver = new GoogleTasksSyncObserver(mockTaskService);
        calendarSyncObserver = new CalendarSyncObserver(mockCalendarService);
        
        // Inscrever observadores
        eventPublisher.subscribe(googleTasksSyncObserver);
        eventPublisher.subscribe(calendarSyncObserver);
    }
    
    @Test
    void shouldPublishAndProcessCardScheduledEvent() throws InterruptedException {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(1L)
                .title("Manual Integration Test Card")
                .description("Testing manual event publishing and processing")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(scheduledDate)
                .build();
        
        CountDownLatch eventLatch = new CountDownLatch(2); // Esperamos 2 observadores
        
        // Configurar latch para aguardar processamento
        doAnswer(invocation -> {
            eventLatch.countDown();
            return null;
        }).when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        
        doAnswer(invocation -> {
            eventLatch.countDown();
            return null;
        }).when(mockCalendarService).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
        
        // Act - Publicar evento
        eventPublisher.publish(event);
        
        // Assert - Aguardar processamento
        assertTrue(eventLatch.await(5, TimeUnit.SECONDS), "Event should be processed within 5 seconds");
        
        // Verificar que o Google Tasks foi chamado
        verify(mockTaskService, times(1)).createTask(
                eq("Simple Task Board Manager"),
                eq("Manual Integration Test Card"),
                contains("Manual Integration Test Card"),
                eq(scheduledDate.plusDays(1)),
                eq(1L)
        );
        
        // Verificar que o Calendar foi chamado
        verify(mockCalendarService, times(1)).createEvent(argThat(eventDTO ->
                eventDTO.getTitle().equals("Manual Integration Test Card") &&
                eventDTO.getRelatedEntityId().equals(1L) &&
                eventDTO.getRelatedEntityType().equals("Card")
        ));
    }
    
    @Test
    void shouldCoordinateIntegrationThroughCoordinator() throws InterruptedException {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(2L)
                .title("Manual Coordinator Test Card")
                .description("Testing manual integration coordination")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        CountDownLatch coordinatorLatch = new CountDownLatch(2); // Esperamos 2 integrações
        
        // Configurar latch para aguardar coordenação
        doAnswer(invocation -> {
            coordinatorLatch.countDown();
            return null;
        }).when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        
        doAnswer(invocation -> {
            coordinatorLatch.countDown();
            return null;
        }).when(mockCalendarService).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
        
        // Act - Coordenar integração
        integrationCoordinator.onCardScheduled(card);
        
        // Assert - Aguardar processamento
        assertTrue(coordinatorLatch.await(5, TimeUnit.SECONDS), "Integration should be coordinated within 5 seconds");
        
        // Verificar que o Google Tasks foi chamado
        verify(mockTaskService, times(1)).createTask(
                eq("Simple Task Board Manager"),
                eq("Manual Coordinator Test Card"),
                contains("Manual Coordinator Test Card"),
                eq(scheduledDate.plusDays(1)),
                eq(2L)
        );
        
        // Verificar que o Calendar foi chamado
        verify(mockCalendarService, times(1)).createEvent(argThat(eventDTO ->
                eventDTO.getTitle().equals("Manual Coordinator Test Card") &&
                eventDTO.getRelatedEntityId().equals(2L)
        ));
    }
    
    @Test
    void shouldHandleMultipleEventsConcurrently() throws InterruptedException {
        // Arrange
        int numberOfEvents = 3;
        CountDownLatch allEventsLatch = new CountDownLatch(numberOfEvents * 2); // 2 observadores por evento
        
        // Configurar latch para aguardar todos os eventos
        doAnswer(invocation -> {
            allEventsLatch.countDown();
            return null;
        }).when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        
        doAnswer(invocation -> {
            allEventsLatch.countDown();
            return null;
        }).when(mockCalendarService).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
        
        // Act - Publicar múltiplos eventos
        for (int i = 1; i <= numberOfEvents; i++) {
            Card card = Card.builder()
                    .id((long) i)
                    .title("Manual Concurrent Test Card " + i)
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
        verify(mockCalendarService, times(numberOfEvents)).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
    }
    
    @Test
    void shouldMaintainIntegrationStats() throws InterruptedException {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(3L)
                .title("Manual Stats Test Card")
                .description("Testing manual integration statistics")
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
    void shouldVerifyObserverCompatibility() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(4L)
                .title("Manual Compatibility Test Card")
                .description("Testing manual observer compatibility")
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
                .id(5L)
                .title("Manual Null Date Test Card")
                .description("Testing manual event with null scheduled date")
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
    
    @Test
    void shouldHandleAsyncEventProcessing() throws InterruptedException {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = Card.builder()
                .id(6L)
                .title("Manual Async Test Card")
                .description("Testing manual async event processing")
                .scheduledDate(scheduledDate)
                .dueDate(scheduledDate.plusDays(1))
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(scheduledDate)
                .build();
        
        CountDownLatch asyncLatch = new CountDownLatch(2); // 2 observadores
        
        // Configurar latch para aguardar processamento assíncrono
        doAnswer(invocation -> {
            asyncLatch.countDown();
            return null;
        }).when(mockTaskService).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        
        doAnswer(invocation -> {
            asyncLatch.countDown();
            return null;
        }).when(mockCalendarService).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
        
        // Act - Publicar evento de forma assíncrona
        eventPublisher.publishAsync(event);
        
        // Assert - Aguardar processamento assíncrono
        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS), "Async event should be processed within 5 seconds");
        
        // Verificar que os serviços foram chamados
        verify(mockTaskService, times(1)).createTask(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyLong());
        verify(mockCalendarService, times(1)).createEvent(any(org.desviante.calendar.dto.CalendarEventDTO.class));
    }
}
