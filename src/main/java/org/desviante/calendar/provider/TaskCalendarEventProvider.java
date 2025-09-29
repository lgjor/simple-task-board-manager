package org.desviante.calendar.provider;

import lombok.RequiredArgsConstructor;
import org.desviante.calendar.CalendarEventPriority;
import org.desviante.calendar.CalendarEventType;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.desviante.model.Card;
import org.desviante.service.CardService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provedor de eventos do calendário para tarefas (Provider para tasks).
 * 
 * <p>Este provedor integra as tarefas do sistema com o calendário,
 * criando eventos baseados na data de agendamento (scheduledDate)
 * dos cards que representam tarefas. Os eventos são gerados automaticamente
 * e sincronizados com as alterações nos cards.</p>
 * 
 * <p><strong>Características Principais:</strong></p>
 * <ul>
 *   <li>Eventos baseados na data de agendamento dos cards de tarefas</li>
 *   <li>Prioridade calculada baseada na data de vencimento</li>
 *   <li>Sincronização automática com alterações nos cards</li>
 *   <li>Suporte a eventos de dia inteiro e com horário específico</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventProvider
 * @see Card
 * @see CalendarEventDTO
 */
@Component
@RequiredArgsConstructor
public class TaskCalendarEventProvider implements CalendarEventProvider {

    private final CardService cardService;

    @Override
    public List<CalendarEventDTO> getEventsForDateRange(LocalDate start, LocalDate end) {
        // Buscar cards agendados no período que representam tarefas
        List<Card> cards = cardService.getCardsScheduledBetween(start, end);
        
        return cards.stream()
                .filter(card -> card.getScheduledDate() != null)
                .filter(this::isTaskCard) // Filtrar apenas cards que são tarefas
                .map(this::convertCardToEventDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CalendarEventDTO createEvent(CalendarEventDTO event) {
        // Para tasks, não criamos eventos diretamente - eles são criados
        // quando um card de tarefa é agendado através do CardService
        throw new UnsupportedOperationException("Eventos de task são criados automaticamente quando um card de tarefa é agendado");
    }

    @Override
    public void updateEvent(CalendarEventDTO event) {
        // Para tasks, atualizamos o card correspondente
        if (event.getRelatedEntityType() != null && event.getRelatedEntityType().equals("TASK")) {
            Long cardId = event.getRelatedEntityId();
            if (cardId != null && event.getStartDateTime() != null) {
                cardService.setScheduledDate(cardId, event.getStartDateTime());
            }
        }
    }

    @Override
    public void deleteEvent(Long eventId) {
        // Para tasks, removemos a data de agendamento do card correspondente
        throw new UnsupportedOperationException("Remoção de eventos de task deve ser feita através do CardService");
    }

    /**
     * Verifica se um card representa uma tarefa.
     * 
     * @param card card a ser verificado
     * @return true se o card é uma tarefa
     */
    private boolean isTaskCard(Card card) {
        // Aqui você pode implementar lógica específica para identificar
        // se um card é uma tarefa. Por exemplo, baseado no tipo do card
        // ou em algum campo específico.
        return card.getCardType() != null && 
               (card.getCardType().getName().equals("TASK") || 
                card.getCardType().getName().equals("TODO"));
    }

    /**
     * Converte um card em um DTO de evento do calendário.
     * 
     * @param card card a ser convertido
     * @return DTO do evento do calendário
     */
    private CalendarEventDTO convertCardToEventDTO(Card card) {
        LocalDateTime scheduledDate = card.getScheduledDate();
        LocalDateTime dueDate = card.getDueDate();
        
        // Determinar se é evento de dia inteiro ou com horário específico
        boolean allDay = isAllDayEvent(scheduledDate);
        
        // Calcular prioridade baseada na data de vencimento
        CalendarEventPriority priority = calculatePriority(card);
        
        // Determinar cor baseada na urgência
        String color = calculateColor(card);
        
        return CalendarEventDTO.builder()
                .id(card.getId())
                .title(card.getTitle())
                .description(card.getDescription())
                .startDateTime(scheduledDate)
                .endDateTime(allDay ? scheduledDate.plusHours(1) : dueDate)
                .allDay(allDay)
                .type(CalendarEventType.TASK)
                .priority(priority)
                .color(color)
                .relatedEntityId(card.getId())
                .relatedEntityType("TASK")
                .active(true)
                .build();
    }

    /**
     * Determina se o evento deve ser de dia inteiro.
     * 
     * @param scheduledDate data de agendamento
     * @return true se for evento de dia inteiro
     */
    private boolean isAllDayEvent(LocalDateTime scheduledDate) {
        // Se não há horário específico (meia-noite), é evento de dia inteiro
        return scheduledDate.getHour() == 0 && scheduledDate.getMinute() == 0;
    }

    /**
     * Calcula a prioridade do evento baseada na urgência do card.
     * 
     * @param card card para cálculo da prioridade
     * @return prioridade do evento
     */
    private CalendarEventPriority calculatePriority(Card card) {
        int urgencyLevel = card.getUrgencyLevel();
        
        return switch (urgencyLevel) {
            case 4 -> CalendarEventPriority.URGENT;      // Vencido
            case 3 -> CalendarEventPriority.HIGH;        // Vence hoje
            case 2 -> CalendarEventPriority.HIGH;        // Vence em 1 dia
            case 1 -> CalendarEventPriority.STANDARD;    // Vence em 2-3 dias
            default -> CalendarEventPriority.LOW;        // Sem urgência
        };
    }

    /**
     * Calcula a cor do evento baseada na urgência do card.
     * 
     * @param card card para cálculo da cor
     * @return cor em formato hexadecimal
     */
    private String calculateColor(Card card) {
        int urgencyLevel = card.getUrgencyLevel();
        
        return switch (urgencyLevel) {
            case 4 -> "#FF0000";  // Vermelho - vencido
            case 3 -> "#FF6600";  // Laranja - vence hoje
            case 2 -> "#FFAA00";  // Amarelo - vence em 1 dia
            case 1 -> "#00AAFF";  // Azul - vence em 2-3 dias
            default -> "#00AA00"; // Verde - sem urgência
        };
    }
}
