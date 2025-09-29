package org.desviante.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.desviante.calendar.CalendarEventPriority;
import org.desviante.calendar.CalendarEventType;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para eventos do calendário.
 * 
 * <p>Esta classe representa os dados de um evento do calendário para transferência
 * entre diferentes camadas da aplicação. É uma versão simplificada da entidade
 * CalendarEvent, contendo apenas os campos essenciais para exibição e manipulação.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Campos essenciais para exibição no calendário</li>
 *   <li>Serialização otimizada para transferência de dados</li>
 *   <li>Validações básicas de integridade</li>
 *   <li>Compatibilidade com diferentes formatos de API</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEvent
 * @see CalendarEventType
 * @see CalendarEventPriority
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {

    /**
     * Identificador único do evento.
     */
    private Long id;

    /**
     * Título do evento.
     */
    private String title;

    /**
     * Descrição do evento.
     */
    private String description;

    /**
     * Data e hora de início do evento.
     */
    private LocalDateTime startDateTime;

    /**
     * Data e hora de fim do evento.
     */
    private LocalDateTime endDateTime;

    /**
     * Indica se o evento é de dia inteiro.
     */
    @Builder.Default
    private boolean allDay = false;

    /**
     * Tipo do evento.
     */
    private CalendarEventType type;

    /**
     * Prioridade do evento.
     */
    @Builder.Default
    private CalendarEventPriority priority = CalendarEventPriority.STANDARD;

    /**
     * Cor personalizada do evento.
     */
    private String color;

    /**
     * ID da entidade relacionada.
     */
    private Long relatedEntityId;

    /**
     * Tipo da entidade relacionada.
     */
    private String relatedEntityType;

    /**
     * Indica se o evento é recorrente.
     */
    @Builder.Default
    private boolean recurring = false;

    /**
     * Regra de recorrência do evento (formato RRULE).
     * 
     * <p>Define como o evento se repete seguindo o padrão RRULE do RFC 5545.
     * Exemplos: "FREQ=DAILY", "FREQ=WEEKLY;BYDAY=MO,WE,FR", "FREQ=MONTHLY"</p>
     */
    private String recurrenceRule;

    /**
     * Indica se o evento está ativo.
     */
    @Builder.Default
    private boolean active = true;
}
