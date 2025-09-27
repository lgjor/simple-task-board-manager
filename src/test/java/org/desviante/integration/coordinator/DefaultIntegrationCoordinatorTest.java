package org.desviante.integration.coordinator;

import org.desviante.integration.event.EventPublisher;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;
import org.desviante.model.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para DefaultIntegrationCoordinator.
 * 
 * <p>Estes testes verificam o funcionamento do coordenador de integrações,
 * incluindo coordenação de eventos, estatísticas e tratamento de erros.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class DefaultIntegrationCoordinatorTest {
    
    @Mock
    private EventPublisher eventPublisher;
    
    private DefaultIntegrationCoordinator coordinator;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coordinator = new DefaultIntegrationCoordinator(eventPublisher);
    }
    
    @Test
    void shouldCoordinateCardScheduling() {
        // Arrange
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .description("Test Description")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act
        coordinator.onCardScheduled(card);
        
        // Assert
        verify(eventPublisher, times(1)).publish(any(CardScheduledEvent.class));
        
        IntegrationStats stats = coordinator.getStats();
        assertEquals(1, stats.getScheduledIntegrations());
        assertEquals(1, stats.getSuccessfulIntegrations());
        assertEquals(0, stats.getFailedIntegrations());
        assertNotNull(stats.getLastIntegrationTime());
    }
    
    @Test
    void shouldCoordinateCardUnscheduling() {
        // Arrange
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .description("Test Description")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act
        coordinator.onCardUnscheduled(card);
        
        // Assert
        verify(eventPublisher, times(1)).publish(any(CardUnscheduledEvent.class));
        
        IntegrationStats stats = coordinator.getStats();
        assertEquals(1, stats.getUnscheduledIntegrations());
        assertEquals(1, stats.getSuccessfulIntegrations());
    }
    
    @Test
    void shouldCoordinateCardUpdate() {
        // Arrange
        Card currentCard = Card.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        Card previousCard = Card.builder()
                .id(1L)
                .title("Original Title")
                .description("Original Description")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act
        coordinator.onCardUpdated(currentCard, previousCard);
        
        // Assert
        ArgumentCaptor<CardUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(CardUpdatedEvent.class);
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        
        CardUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(currentCard, capturedEvent.getCard());
        assertEquals(previousCard, capturedEvent.getPreviousCard());
        assertTrue(capturedEvent.isTitleChanged());
        assertTrue(capturedEvent.isDescriptionChanged());
        
        IntegrationStats stats = coordinator.getStats();
        assertEquals(1, stats.getUpdateIntegrations());
    }
    
    @Test
    void shouldCoordinateCardMovement() {
        // Arrange
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .boardColumnId(2L)
                .build();
        
        // Act
        coordinator.onCardMoved(card, 1L, 2L);
        
        // Assert
        verify(eventPublisher, times(1)).publish(any(CardUpdatedEvent.class));
        
        IntegrationStats stats = coordinator.getStats();
        assertEquals(1, stats.getMoveIntegrations());
        assertEquals(1, stats.getUpdateIntegrations()); // Movimentação também conta como atualização
    }
    
    @Test
    void shouldCoordinateCardDeletion() {
        // Arrange
        Long cardId = 1L;
        
        // Act
        coordinator.onCardDeleted(cardId);
        
        // Assert
        verify(eventPublisher, times(1)).publish(any(CardUnscheduledEvent.class));
        
        IntegrationStats stats = coordinator.getStats();
        assertEquals(1, stats.getDeleteIntegrations());
        assertEquals(1, stats.getUnscheduledIntegrations()); // Exclusão também conta como desagendamento
    }
    
    @Test
    void shouldHandleNullCardGracefully() {
        // Act & Assert
        assertDoesNotThrow(() -> coordinator.onCardScheduled(null));
        assertDoesNotThrow(() -> coordinator.onCardUnscheduled(null));
        assertDoesNotThrow(() -> coordinator.onCardUpdated(null, null));
        assertDoesNotThrow(() -> coordinator.onCardMoved(null, 1L, 2L));
        assertDoesNotThrow(() -> coordinator.onCardDeleted(null));
        
        // Verificar que nenhum evento foi publicado
        verify(eventPublisher, never()).publish(any());
    }
    
    @Test
    void shouldHandleCardWithoutScheduledDate() {
        // Arrange
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .scheduledDate(null) // Sem data de agendamento
                .build();
        
        // Act
        coordinator.onCardScheduled(card);
        
        // Assert
        verify(eventPublisher, never()).publish(any());
        
        IntegrationStats stats = coordinator.getStats();
        assertEquals(0, stats.getScheduledIntegrations());
    }
    
    @Test
    void shouldHandleEventPublisherErrors() {
        // Arrange
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        doThrow(new RuntimeException("Publisher error")).when(eventPublisher).publish(any());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> coordinator.onCardScheduled(card));
        
        IntegrationStats stats = coordinator.getStats();
        assertEquals(1, stats.getFailedIntegrations());
        assertEquals(0, stats.getSuccessfulIntegrations());
    }
    
    @Test
    void shouldTrackStatisticsCorrectly() {
        // Arrange
        Card card1 = Card.builder().id(1L).title("Card 1").scheduledDate(LocalDateTime.now()).build();
        Card card2 = Card.builder().id(2L).title("Card 2").scheduledDate(LocalDateTime.now()).build();
        
        // Act
        coordinator.onCardScheduled(card1);
        coordinator.onCardScheduled(card2);
        coordinator.onCardUnscheduled(card1);
        coordinator.onCardUpdated(card2, card1);
        
        // Assert
        IntegrationStats stats = coordinator.getStats();
        assertEquals(4, stats.getTotalIntegrations());
        assertEquals(4, stats.getSuccessfulIntegrations());
        assertEquals(0, stats.getFailedIntegrations());
        assertEquals(2, stats.getScheduledIntegrations());
        assertEquals(1, stats.getUnscheduledIntegrations());
        assertEquals(1, stats.getUpdateIntegrations());
        assertEquals(100.0, stats.getSuccessRate());
        assertEquals(0.0, stats.getFailureRate());
        assertTrue(stats.hasIntegrations());
    }
    
    @Test
    void shouldBeAvailable() {
        // Assert
        assertTrue(coordinator.isAvailable());
    }
    
    @Test
    void shouldDetermineChangedFieldsCorrectly() {
        // Arrange
        Card previousCard = Card.builder()
                .id(1L)
                .title("Original Title")
                .description("Original Description")
                .scheduledDate(LocalDateTime.of(2024, 1, 1, 10, 0))
                .dueDate(LocalDateTime.of(2024, 1, 2, 18, 0))
                .boardColumnId(1L)
                .build();
        
        Card currentCard = Card.builder()
                .id(1L)
                .title("Updated Title") // Mudou
                .description("Original Description") // Não mudou
                .scheduledDate(LocalDateTime.of(2024, 1, 1, 11, 0)) // Mudou
                .dueDate(LocalDateTime.of(2024, 1, 2, 18, 0)) // Não mudou
                .boardColumnId(2L) // Mudou
                .build();
        
        // Act
        coordinator.onCardUpdated(currentCard, previousCard);
        
        // Assert
        ArgumentCaptor<CardUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(CardUpdatedEvent.class);
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        
        CardUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertTrue(capturedEvent.isTitleChanged());
        assertTrue(capturedEvent.isScheduledDateChanged());
        assertTrue(capturedEvent.isFieldChanged("boardColumnId"));
        assertFalse(capturedEvent.isDescriptionChanged());
        assertFalse(capturedEvent.isDueDateChanged());
    }
}
