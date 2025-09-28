package org.desviante.service;

import org.desviante.calendar.CalendarService;
import org.desviante.calendar.CalendarEventType;
import org.desviante.calendar.CalendarEventPriority;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.desviante.model.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CalendarEventService.
 * 
 * <p>Testa as funcionalidades de criação e exclusão de eventos,
 * validando a arquitetura de desacoplamento entre eventos e cards.</p>
 */
@ExtendWith(MockitoExtension.class)
class CalendarEventServiceTest {

    @Mock
    private CardSchedulingService cardSchedulingService;

    @Mock
    private CalendarService calendarService;

    @InjectMocks
    private CalendarEventService calendarEventService;

    private Card testCard;

    @BeforeEach
    void setUp() {
        // Configurar card de teste
        testCard = new Card();
        testCard.setId(1L);
        testCard.setTitle("Teste Card");
        testCard.setDescription("Card para teste");
        testCard.setScheduledDate(LocalDateTime.now().plusDays(1));
        testCard.setDueDate(LocalDateTime.now().plusDays(2));
    }

    @Test
    void createCalendarEvent_Success() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.of(testCard));
        
        // Mock do CalendarService para retornar um evento criado
        CalendarEventDTO mockEvent = CalendarEventDTO.builder()
                .id(1L)
                .title("Teste Card")
                .description("Card para teste")
                .startDateTime(testCard.getScheduledDate())
                .endDateTime(testCard.getDueDate())
                .allDay(false)
                .type(CalendarEventType.CARD)
                .priority(CalendarEventPriority.LOW)
                .color("#00AA00")
                .relatedEntityId(1L)
                .relatedEntityType("CARD")
                .active(true)
                .build();
        
        when(calendarService.createEvent(any(CalendarEventDTO.class))).thenReturn(mockEvent);

        // Act
        boolean result = calendarEventService.createCalendarEvent(1L);

        // Assert
        assertTrue(result, "Criação de evento deve retornar true quando bem-sucedida");
        verify(cardSchedulingService).getCardById(1L);
        verify(calendarService).createEvent(any(CalendarEventDTO.class));
    }

    @Test
    void createCalendarEvent_CardNotFound() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = calendarEventService.createCalendarEvent(1L);

        // Assert
        assertFalse(result, "Deve retornar false quando card não é encontrado");
        verify(cardSchedulingService).getCardById(1L);
    }

    @Test
    void createCalendarEvent_NoScheduledDate() {
        // Arrange
        testCard.setScheduledDate(null);
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.of(testCard));

        // Act
        boolean result = calendarEventService.createCalendarEvent(1L);

        // Assert
        assertFalse(result, "Criação de evento deve retornar false quando card não tem data de agendamento");
    }

    @Test
    void deleteCalendarEvent_Success() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.of(testCard));
        when(cardSchedulingService.clearScheduledDateWithEvent(1L)).thenReturn(testCard);

        // Act
        boolean result = calendarEventService.deleteCalendarEvent("1");

        // Assert
        assertTrue(result, "Exclusão de evento deve retornar true quando bem-sucedida");
        verify(cardSchedulingService).getCardById(1L);
        verify(cardSchedulingService).clearScheduledDateWithEvent(1L); // Deve chamar clearScheduledDateWithEvent
        verify(calendarService, never()).deleteEvent(anyLong()); // NÃO chama diretamente
    }

    @Test
    void deleteCalendarEvent_InvalidId() {
        // Act
        boolean result = calendarEventService.deleteCalendarEvent("invalid");

        // Assert
        assertFalse(result, "Exclusão de evento deve retornar false para ID inválido");
        verify(calendarService, never()).deleteEvent(anyLong());
    }

    @Test
    void deleteCalendarEvent_ServiceThrowsException() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.of(testCard));
        doThrow(new RuntimeException("Erro simulado")).when(cardSchedulingService).clearScheduledDateWithEvent(anyLong());

        // Act
        boolean result = calendarEventService.deleteCalendarEvent("1");

        // Assert
        assertFalse(result, "Exclusão de evento deve retornar false quando serviço lança exceção");
        verify(cardSchedulingService).getCardById(1L);
        verify(cardSchedulingService).clearScheduledDateWithEvent(1L); // Deve chamar clearScheduledDateWithEvent
        verify(calendarService, never()).deleteEvent(anyLong()); // NÃO chama diretamente
    }

    @Test
    void deleteCalendarEvent_CardNotFound() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = calendarEventService.deleteCalendarEvent("1");

        // Assert
        assertFalse(result, "Exclusão de evento deve retornar false quando card não é encontrado");
        verify(cardSchedulingService).getCardById(1L);
        verify(calendarService, never()).deleteEvent(anyLong());
    }

    @Test
    void canCreateCalendarEvent_WithScheduledDate() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.of(testCard));

        // Act
        boolean result = calendarEventService.canCreateCalendarEvent(1L);

        // Assert
        assertTrue(result, "Deve poder criar evento quando card tem data de agendamento");
    }

    @Test
    void canCreateCalendarEvent_WithoutScheduledDate() {
        // Arrange
        testCard.setScheduledDate(null);
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.of(testCard));

        // Act
        boolean result = calendarEventService.canCreateCalendarEvent(1L);

        // Assert
        assertFalse(result, "Não deve poder criar evento quando card não tem data de agendamento");
    }

    @Test
    void canCreateCalendarEvent_CardNotFound() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = calendarEventService.canCreateCalendarEvent(1L);

        // Assert
        assertFalse(result, "Não deve poder criar evento quando card não é encontrado");
    }

    @Test
    void canDeleteCalendarEvent_ValidId() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.of(testCard));

        // Act
        boolean result = calendarEventService.canDeleteCalendarEvent("1");

        // Assert
        assertTrue(result, "Deve poder deletar evento com ID válido");
        verify(cardSchedulingService).getCardById(1L);
    }

    @Test
    void canDeleteCalendarEvent_EmptyId() {
        // Act
        boolean result = calendarEventService.canDeleteCalendarEvent("");

        // Assert
        assertFalse(result, "Não deve poder deletar evento com ID vazio");
    }

    @Test
    void canDeleteCalendarEvent_NullId() {
        // Act
        boolean result = calendarEventService.canDeleteCalendarEvent(null);

        // Assert
        assertFalse(result, "Não deve poder deletar evento com ID null");
    }

    @Test
    void getCardEventInfo_Success() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.of(testCard));

        // Act
        CalendarEventService.CardEventInfo result = calendarEventService.getCardEventInfo(1L);

        // Assert
        assertNotNull(result, "Informações do evento não devem ser null");
        assertEquals(testCard.getId(), result.getCardId());
        assertEquals(testCard.getTitle(), result.getTitle());
        assertEquals(testCard.getDescription(), result.getDescription());
        assertEquals(testCard.getScheduledDate(), result.getScheduledDate());
        assertEquals(testCard.getDueDate(), result.getDueDate());
    }

    @Test
    void getCardEventInfo_CardNotFound() {
        // Arrange
        when(cardSchedulingService.getCardById(1L)).thenReturn(Optional.empty());

        // Act
        CalendarEventService.CardEventInfo result = calendarEventService.getCardEventInfo(1L);

        // Assert
        assertNull(result, "Deve retornar null quando card não é encontrado");
        verify(cardSchedulingService).getCardById(1L);
    }
}
