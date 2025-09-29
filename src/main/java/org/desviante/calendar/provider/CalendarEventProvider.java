package org.desviante.calendar.provider;

import org.desviante.calendar.dto.CalendarEventDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface para provedores de eventos do calendário.
 * 
 * <p>Esta interface define o contrato que todos os provedores de eventos
 * devem implementar para fornecer dados ao sistema de calendário. Segue
 * o princípio de inversão de dependência (DIP) permitindo que diferentes
 * tipos de provedores sejam injetados no sistema.</p>
 * 
 * <p><strong>Implementações Típicas:</strong></p>
 * <ul>
 *   <li><strong>CardCalendarEventProvider:</strong> Eventos originados de cards</li>
 *   <li><strong>TaskCalendarEventProvider:</strong> Eventos originados de tasks do Google</li>
 *   <li><strong>CustomEventProvider:</strong> Eventos personalizados do usuário</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventDTO
 * @see LocalDate
 */
public interface CalendarEventProvider {
    
    /**
     * Obtém eventos para um período específico.
     * 
     * @param start data de início do período
     * @param end data de fim do período
     * @return lista de eventos no período especificado
     */
    List<CalendarEventDTO> getEventsForDateRange(LocalDate start, LocalDate end);
    
    /**
     * Cria um novo evento.
     * 
     * @param event dados do evento a ser criado
     * @return evento criado com ID gerado
     */
    CalendarEventDTO createEvent(CalendarEventDTO event);
    
    /**
     * Atualiza um evento existente.
     * 
     * @param event dados atualizados do evento
     */
    void updateEvent(CalendarEventDTO event);
    
    /**
     * Remove um evento do sistema.
     * 
     * @param eventId ID do evento a ser removido
     */
    void deleteEvent(Long eventId);
}