package org.desviante.service;

import org.desviante.calendar.CalendarService;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Card;
import org.desviante.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CardSchedulingService.
 * 
 * <p>Testa as funcionalidades de agendamento de cards e a sincronização
 * com o sistema de calendário quando datas são alteradas.</p>
 */
@ExtendWith(MockitoExtension.class)
class CardSchedulingServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CalendarService calendarService;

    @InjectMocks
    private CardSchedulingService cardSchedulingService;

    private Card testCard;

    @BeforeEach
    void setUp() {
        testCard = new Card();
        testCard.setId(1L);
        testCard.setTitle("Teste Card");
        testCard.setDescription("Card para teste");
        testCard.setScheduledDate(LocalDateTime.now().plusDays(1));
        testCard.setDueDate(LocalDateTime.now().plusDays(2));
        testCard.setLastUpdateDate(LocalDateTime.now());
    }

    @Test
    void setSchedulingDates_Success() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardSchedulingService.setSchedulingDates(1L, scheduledDate, dueDate);

        // Assert
        assertNotNull(result, "Card retornado não deve ser null");
        assertEquals(scheduledDate, testCard.getScheduledDate());
        assertEquals(dueDate, testCard.getDueDate());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    void setSchedulingDates_CardNotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cardSchedulingService.setSchedulingDates(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        }, "Deve lançar exceção quando card não é encontrado");
    }

    @Test
    void setSchedulingDates_InvalidDates() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(2);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1); // Antes da data de agendamento
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cardSchedulingService.setSchedulingDates(1L, scheduledDate, dueDate);
        }, "Deve lançar exceção quando data de vencimento é anterior à data de agendamento");
    }

    @Test
    void clearScheduledDate_Success_WithScheduledDate() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardSchedulingService.clearScheduledDate(1L);

        // Assert
        assertNotNull(result, "Card retornado não deve ser null");
        assertNull(testCard.getScheduledDate(), "Data de agendamento deve ser null");
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        verify(calendarService, never()).deleteEvent(anyLong()); // NÃO deve remover evento do calendário
    }

    @Test
    void clearScheduledDate_Success_WithoutScheduledDate() {
        // Arrange
        testCard.setScheduledDate(null);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardSchedulingService.clearScheduledDate(1L);

        // Assert
        assertNotNull(result, "Card retornado não deve ser null");
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        verify(calendarService, never()).deleteEvent(anyLong()); // Não deve tentar remover evento
    }

    @Test
    void clearScheduledDate_CalendarServiceThrowsException() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardSchedulingService.clearScheduledDate(1L);

        // Assert
        assertNotNull(result, "Card retornado não deve ser null");
        assertNull(testCard.getScheduledDate(), "Data de agendamento deve ser null");
        verify(cardRepository).save(testCard);
        verify(calendarService, never()).deleteEvent(anyLong()); // NÃO deve chamar deleteEvent
    }

    @Test
    void clearDueDate_Success() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardSchedulingService.clearDueDate(1L);

        // Assert
        assertNotNull(result, "Card retornado não deve ser null");
        assertNull(testCard.getDueDate(), "Data de vencimento deve ser null");
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    void getCardById_Success() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        Optional<Card> result = cardSchedulingService.getCardById(1L);

        // Assert
        assertTrue(result.isPresent(), "Card deve ser encontrado");
        assertEquals(testCard, result.get());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_NotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Card> result = cardSchedulingService.getCardById(1L);

        // Assert
        assertFalse(result.isPresent(), "Card não deve ser encontrado");
        verify(cardRepository).findById(1L);
    }

    @Test
    void clearScheduledDateWithEvent_Success_WithScheduledDate() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        doNothing().when(calendarService).deleteEvent(1L);

        // Act
        Card result = cardSchedulingService.clearScheduledDateWithEvent(1L);

        // Assert
        assertNotNull(result, "Card retornado não deve ser null");
        assertNull(testCard.getScheduledDate(), "Data de agendamento deve ser null");
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        verify(calendarService).deleteEvent(1L); // Deve remover evento do calendário
    }

    @Test
    void clearScheduledDateWithEvent_Success_WithoutScheduledDate() {
        // Arrange
        testCard.setScheduledDate(null);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardSchedulingService.clearScheduledDateWithEvent(1L);

        // Assert
        assertNotNull(result, "Card retornado não deve ser null");
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        verify(calendarService, never()).deleteEvent(anyLong()); // Não deve tentar remover evento
    }

    @Test
    void clearScheduledDateWithEvent_CalendarServiceThrowsException() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        doThrow(new RuntimeException("Erro simulado")).when(calendarService).deleteEvent(1L);

        // Act
        Card result = cardSchedulingService.clearScheduledDateWithEvent(1L);

        // Assert
        assertNotNull(result, "Card deve ser salvo mesmo se calendário falhar");
        assertNull(testCard.getScheduledDate(), "Data de agendamento deve ser null");
        verify(cardRepository).save(testCard);
        verify(calendarService).deleteEvent(1L); // Deve tentar remover evento mesmo falhando
    }

    @Test
    void clearScheduledDateWithEvent_CardNotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cardSchedulingService.clearScheduledDateWithEvent(1L);
        }, "Deve lançar exceção quando card não é encontrado");
        
        verify(calendarService, never()).deleteEvent(anyLong());
    }
}