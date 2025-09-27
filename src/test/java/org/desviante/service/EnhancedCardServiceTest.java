package org.desviante.service;

import org.desviante.integration.coordinator.IntegrationCoordinator;
import org.desviante.integration.event.EventPublisher;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;
import org.desviante.integration.sync.IntegrationSyncService;
import org.desviante.integration.sync.IntegrationType;
import org.desviante.integration.sync.IntegrationSyncStatus;
import org.desviante.integration.sync.SyncStatus;
import org.desviante.model.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para EnhancedCardService.
 * 
 * <p>Estes testes verificam a integração do CardService com o sistema
 * de eventos, incluindo publicação de eventos e coordenação de integrações.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class EnhancedCardServiceTest {
    
    @Mock
    private CardService cardService;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @Mock
    private IntegrationCoordinator integrationCoordinator;
    
    @Mock
    private IntegrationSyncService integrationSyncService;
    
    private EnhancedCardService enhancedCardService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        enhancedCardService = new EnhancedCardService(
            cardService, eventPublisher, integrationCoordinator, integrationSyncService);
    }
    
    @Test
    void shouldCreateCardWithEventPublication() {
        // Arrange
        String title = "Test Card";
        String description = "Test Description";
        Long parentColumnId = 1L;
        Long cardTypeId = 1L;
        
        Card createdCard = Card.builder()
                .id(1L)
                .title(title)
                .description(description)
                .boardColumnId(parentColumnId)
                .cardTypeId(cardTypeId)
                .build();
        
        when(cardService.createCard(title, description, parentColumnId, cardTypeId))
                .thenReturn(createdCard);
        
        // Act
        Card result = enhancedCardService.createCard(title, description, parentColumnId, cardTypeId);
        
        // Assert
        assertNotNull(result);
        assertEquals(createdCard, result);
        
        verify(cardService, times(1)).createCard(title, description, parentColumnId, cardTypeId);
        verify(eventPublisher, times(1)).publish(any(CardUpdatedEvent.class));
        // onCardUpdated pode ser chamado se requiresExternalSync() retornar true
    }
    
    @Test
    void shouldSetScheduledDateWithIntegration() {
        // Arrange
        Long cardId = 1L;
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card currentCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .scheduledDate(null) // Não agendado
                .build();
        
        Card updatedCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .scheduledDate(scheduledDate)
                .build();
        
        when(cardService.getCardById(cardId))
                .thenReturn(Optional.of(currentCard));
        when(cardService.setScheduledDate(cardId, scheduledDate))
                .thenReturn(updatedCard);
        
        // Act
        Card result = enhancedCardService.setScheduledDate(cardId, scheduledDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(scheduledDate, result.getScheduledDate());
        
        verify(cardService, times(1)).setScheduledDate(cardId, scheduledDate);
        verify(eventPublisher, times(1)).publish(any(CardScheduledEvent.class));
        verify(integrationSyncService, times(1)).createSyncStatus(cardId, IntegrationType.GOOGLE_TASKS);
        verify(integrationSyncService, times(1)).createSyncStatus(cardId, IntegrationType.CALENDAR);
        verify(integrationCoordinator, times(1)).onCardScheduled(updatedCard);
    }
    
    @Test
    void shouldRemoveScheduledDateWithIntegration() {
        // Arrange
        Long cardId = 1L;
        LocalDateTime previousScheduledDate = LocalDateTime.now().plusDays(1);
        
        Card currentCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .scheduledDate(previousScheduledDate) // Já agendado
                .build();
        
        Card updatedCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .scheduledDate(null) // Desagendado
                .build();
        
        when(cardService.getCardById(cardId))
                .thenReturn(Optional.of(currentCard));
        when(cardService.setScheduledDate(cardId, null))
                .thenReturn(updatedCard);
        
        // Act
        Card result = enhancedCardService.removeScheduledDate(cardId);
        
        // Assert
        assertNotNull(result);
        assertNull(result.getScheduledDate());
        
        verify(cardService, times(1)).setScheduledDate(cardId, null);
        verify(eventPublisher, times(1)).publish(any(CardUnscheduledEvent.class));
        verify(integrationCoordinator, times(1)).onCardUnscheduled(updatedCard);
    }
    
    @Test
    void shouldUpdateCardDetailsWithEvent() {
        // Arrange
        Long cardId = 1L;
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        
        Card currentCard = Card.builder()
                .id(cardId)
                .title("Original Title")
                .description("Original Description")
                .build();
        
        Card updatedCard = Card.builder()
                .id(cardId)
                .title(newTitle)
                .description(newDescription)
                .build();
        
        when(cardService.getCardById(cardId))
                .thenReturn(Optional.of(currentCard));
        when(cardService.updateCardDetails(cardId, newTitle, newDescription))
                .thenReturn(updatedCard);
        
        // Act
        Card result = enhancedCardService.updateCardDetails(cardId, newTitle, newDescription);
        
        // Assert
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        
        verify(cardService, times(1)).updateCardDetails(cardId, newTitle, newDescription);
        
        // Verificar se o evento foi publicado com os campos corretos
        ArgumentCaptor<CardUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(CardUpdatedEvent.class);
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        
        CardUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(updatedCard, capturedEvent.getCard());
        assertEquals(currentCard, capturedEvent.getPreviousCard());
        assertTrue(capturedEvent.isTitleChanged());
        assertTrue(capturedEvent.isDescriptionChanged());
    }
    
    @Test
    void shouldMoveCardWithIntegration() {
        // Arrange
        Long cardId = 1L;
        Long previousColumnId = 1L;
        Long newColumnId = 2L;
        
        Card currentCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .boardColumnId(previousColumnId)
                .build();
        
        Card movedCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .boardColumnId(newColumnId)
                .build();
        
        when(cardService.getCardById(cardId))
                .thenReturn(Optional.of(currentCard));
        when(cardService.moveCardToColumn(cardId, newColumnId))
                .thenReturn(movedCard);
        
        // Act
        Card result = enhancedCardService.moveCardToColumn(cardId, newColumnId);
        
        // Assert
        assertNotNull(result);
        assertEquals(newColumnId, result.getBoardColumnId());
        
        verify(cardService, times(1)).moveCardToColumn(cardId, newColumnId);
        verify(integrationCoordinator, times(1)).onCardMoved(movedCard, previousColumnId, newColumnId);
    }
    
    @Test
    void shouldDeleteCardWithIntegration() {
        // Arrange
        Long cardId = 1L;
        
        Card cardToDelete = Card.builder()
                .id(cardId)
                .title("Test Card")
                .build();
        
        when(cardService.getCardById(cardId))
                .thenReturn(Optional.of(cardToDelete));
        
        // Act
        enhancedCardService.deleteCard(cardId);
        
        // Assert
        verify(cardService, times(1)).deleteCard(cardId);
        verify(integrationCoordinator, times(1)).onCardDeleted(cardId);
    }
    
    @Test
    void shouldHandleIntegrationErrorsGracefully() {
        // Arrange
        Long cardId = 1L;
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        
        Card currentCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .scheduledDate(null)
                .build();
        
        Card updatedCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .scheduledDate(scheduledDate)
                .build();
        
        when(cardService.getCardById(cardId))
                .thenReturn(Optional.of(currentCard));
        when(cardService.setScheduledDate(cardId, scheduledDate))
                .thenReturn(updatedCard);
        
        // Simular erro na integração
        doThrow(new RuntimeException("Integration error"))
                .when(integrationCoordinator).onCardScheduled(updatedCard);
        
        // Act & Assert - não deve lançar exceção
        assertDoesNotThrow(() -> enhancedCardService.setScheduledDate(cardId, scheduledDate));
        
        // Verificar que a operação principal foi executada
        verify(cardService, times(1)).setScheduledDate(cardId, scheduledDate);
        verify(eventPublisher, times(1)).publish(any(CardScheduledEvent.class));
    }
    
    @Test
    void shouldDelegateGetCardById() {
        // Arrange
        Long cardId = 1L;
        Card expectedCard = Card.builder()
                .id(cardId)
                .title("Test Card")
                .build();
        
        when(cardService.getCardById(cardId))
                .thenReturn(Optional.of(expectedCard));
        
        // Act
        Optional<Card> result = enhancedCardService.getCardById(cardId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedCard, result.get());
        
        verify(cardService, times(1)).getCardById(cardId);
        verify(eventPublisher, never()).publish(any());
        verify(integrationCoordinator, never()).onCardScheduled(any());
    }
    
    @Test
    void shouldThrowExceptionForNonExistentCard() {
        // Arrange
        Long cardId = 999L;
        
        when(cardService.getCardById(cardId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            enhancedCardService.setScheduledDate(cardId, LocalDateTime.now()));
        
        verify(cardService, never()).setScheduledDate(any(), any());
        verify(eventPublisher, never()).publish(any());
    }
}
