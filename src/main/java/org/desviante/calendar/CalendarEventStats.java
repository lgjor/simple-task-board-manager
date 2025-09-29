package org.desviante.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estatísticas de eventos do calendário.
 * 
 * <p>Esta classe encapsula estatísticas agregadas sobre eventos em um período
 * específico, fornecendo informações úteis para relatórios e análises.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventStats {

    /**
     * Número total de eventos.
     */
    private int totalEvents;

    /**
     * Número de eventos de dia inteiro.
     */
    private int allDayEvents;

    /**
     * Número de eventos com horário específico.
     */
    private int timedEvents;

    /**
     * Número de eventos urgentes.
     */
    private int urgentEvents;

    /**
     * Número de eventos de alta prioridade.
     */
    private int highPriorityEvents;
}
