package org.desviante.calendar.adapter;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.calendar.CalendarEventPriority;
import org.desviante.calendar.CalendarEventType;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador para converter CalendarEventDTO para CalendarFX Entry.
 * 
 * <p>Esta classe é responsável por converter os eventos do sistema
 * para o formato esperado pelo CalendarFX, mantendo a compatibilidade
 * entre as duas bibliotecas.</p>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Converter CalendarEventDTO para CalendarFX Entry</li>
 *   <li>Configurar cores e estilos baseados no tipo e prioridade</li>
 *   <li>Gerenciar calendários por tipo de evento</li>
 *   <li>Manter sincronização entre os formatos</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventDTO
 * @see Entry
 * @see Calendar
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarFXAdapter {

    private static final String CARD_CALENDAR_NAME = "Cards";
    private static final String TASK_CALENDAR_NAME = "Tasks";
    private static final String CUSTOM_CALENDAR_NAME = "Eventos Personalizados";
    private static final String MEETING_CALENDAR_NAME = "Reuniões";
    private static final String REMINDER_CALENDAR_NAME = "Lembretes";

    /**
     * Converte um CalendarEventDTO para um CalendarFX Entry.
     * 
     * @param eventDTO evento a ser convertido
     * @return Entry do CalendarFX
     */
    public Entry<CalendarEventDTO> convertToEntry(CalendarEventDTO eventDTO) {
        if (eventDTO == null) {
            log.warn("Tentativa de converter evento null para Entry");
            return null;
        }

        try {
            Entry<CalendarEventDTO> entry = new Entry<>(eventDTO.getTitle());
            entry.setUserObject(eventDTO);
            
            // Configurar datas
            LocalDateTime startDateTime = eventDTO.getStartDateTime();
            LocalDateTime endDateTime = eventDTO.getEndDateTime();
            
            if (eventDTO.isAllDay()) {
                // Evento de dia inteiro
                entry.setFullDay(true);
                entry.setInterval(LocalDate.from(startDateTime), LocalDate.from(startDateTime));
            } else {
                // Evento com horário específico
                entry.setFullDay(false);
                if (endDateTime != null) {
                    entry.setInterval(startDateTime, endDateTime);
                } else {
                    // Se não há data de fim, assume 1 hora de duração
                    entry.setInterval(startDateTime, startDateTime.plusHours(1));
                }
            }
            
            // Configurar cor e estilo baseado no tipo e prioridade
            configureEntryStyle(entry, eventDTO);
            
            // Configurar tooltip
            configureEntryTooltip(entry, eventDTO);
            
            log.debug("Evento convertido com sucesso: {} -> {}", eventDTO.getTitle(), entry.getTitle());
            return entry;
            
        } catch (Exception e) {
            log.error("Erro ao converter evento para Entry: {}", eventDTO.getTitle(), e);
            return null;
        }
    }

    /**
     * Configura o estilo do entry baseado no tipo e prioridade do evento.
     * 
     * @param entry entry a ser configurado
     * @param eventDTO evento de origem
     */
    private void configureEntryStyle(Entry<CalendarEventDTO> entry, CalendarEventDTO eventDTO) {
        // No CalendarFX, o estilo é aplicado através do CSS e das propriedades do Entry
        // Configurar propriedades específicas do Entry
        
        // Configurar se é evento de dia inteiro
        entry.setFullDay(eventDTO.isAllDay());
        
        // Configurar se é recorrente
        if (eventDTO.isRecurring()) {
            // TODO: Implementar regra de recorrência quando necessário
            // entry.setRecurrenceRule(eventDTO.getRecurrenceRule());
        }
        
        // Configurar localização se disponível
        if (eventDTO.getDescription() != null && !eventDTO.getDescription().isEmpty()) {
            entry.setLocation(eventDTO.getDescription());
        }
        
        // Configurar se está oculto
        entry.setHidden(!eventDTO.isActive());
    }

    /**
     * Configura o tooltip do entry.
     * 
     * @param entry entry a ser configurado
     * @param eventDTO evento de origem
     */
    private void configureEntryTooltip(Entry<CalendarEventDTO> entry, CalendarEventDTO eventDTO) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append("Título: ").append(eventDTO.getTitle()).append("\n");
        
        if (eventDTO.getDescription() != null && !eventDTO.getDescription().isEmpty()) {
            tooltip.append("Descrição: ").append(eventDTO.getDescription()).append("\n");
        }
        
        tooltip.append("Tipo: ").append(eventDTO.getType().getDisplayName()).append("\n");
        tooltip.append("Prioridade: ").append(eventDTO.getPriority().getDisplayName()).append("\n");
        
        if (eventDTO.isAllDay()) {
            tooltip.append("Duração: Dia inteiro");
        } else {
            tooltip.append("Início: ").append(eventDTO.getStartDateTime().toLocalTime());
            if (eventDTO.getEndDateTime() != null) {
                tooltip.append("\nFim: ").append(eventDTO.getEndDateTime().toLocalTime());
            }
        }
        
        if (eventDTO.isRecurring()) {
            tooltip.append("\nRecorrente: Sim");
        }
        
        entry.setTitle(tooltip.toString());
    }

    /**
     * Obtém o estilo para o tipo de evento.
     * 
     * @param type tipo do evento
     * @return estilo do calendário
     */
    private Calendar.Style getStyleForEventType(CalendarEventType type) {
        if (type == null) {
            return Calendar.Style.STYLE1;
        }
        
        return switch (type) {
            case CARD -> Calendar.Style.STYLE1; // Azul
            case TASK -> Calendar.Style.STYLE2; // Verde
            case CUSTOM -> Calendar.Style.STYLE3; // Amarelo
            case MEETING -> Calendar.Style.STYLE4; // Vermelho
            case REMINDER -> Calendar.Style.STYLE5; // Roxo
            default -> Calendar.Style.STYLE1; // Padrão
        };
    }

    /**
     * Obtém a cor para o tipo de evento.
     * 
     * @param type tipo do evento
     * @return cor em formato hexadecimal
     */
    private String getColorForEventType(CalendarEventType type) {
        if (type == null) {
            return "#6c757d"; // Cinza padrão
        }
        
        return switch (type) {
            case CARD -> "#007bff"; // Azul
            case TASK -> "#28a745"; // Verde
            case CUSTOM -> "#ffc107"; // Amarelo
            case MEETING -> "#dc3545"; // Vermelho
            case REMINDER -> "#6f42c1"; // Roxo
            default -> "#6c757d"; // Cinza padrão
        };
    }

    /**
     * Obtém a opacidade para a prioridade do evento.
     * 
     * @param priority prioridade do evento
     * @return opacidade (0.0 a 1.0)
     */
    private double getOpacityForPriority(CalendarEventPriority priority) {
        if (priority == null) {
            return 1.0;
        }
        
        return switch (priority) {
            case LOW -> 0.7;
            case STANDARD -> 1.0;
            case HIGH -> 1.0;
            case URGENT -> 1.0;
        };
    }

    /**
     * Cria um calendário para um tipo específico de evento.
     * 
     * @param type tipo do evento
     * @return calendário configurado
     */
    public Calendar<CalendarEventDTO> createCalendarForType(CalendarEventType type) {
        if (type == null) {
            log.warn("Tentativa de criar calendário para tipo null");
            return null;
        }
        
        String calendarName = getCalendarNameForType(type);
        
        Calendar<CalendarEventDTO> calendar = new Calendar<>(calendarName);
        calendar.setReadOnly(false);
        
        // Configurar estilo baseado no tipo
        Calendar.Style style = getStyleForEventType(type);
        calendar.setStyle(style);
        
        log.debug("Calendário criado para tipo {}: {}", type, calendarName);
        return calendar;
    }

    /**
     * Obtém o nome do calendário para o tipo de evento.
     * 
     * @param type tipo do evento
     * @return nome do calendário
     */
    private String getCalendarNameForType(CalendarEventType type) {
        return switch (type) {
            case CARD -> CARD_CALENDAR_NAME;
            case TASK -> TASK_CALENDAR_NAME;
            case CUSTOM -> CUSTOM_CALENDAR_NAME;
            case MEETING -> MEETING_CALENDAR_NAME;
            case REMINDER -> REMINDER_CALENDAR_NAME;
            default -> "Outros";
        };
    }

    /**
     * Cria um CalendarSource com todos os tipos de calendário.
     * 
     * @return CalendarSource configurado
     */
    public CalendarSource createCalendarSource() {
        CalendarSource calendarSource = new CalendarSource("Sistema de Calendário");
        
        // Criar calendários para cada tipo de evento
        for (CalendarEventType type : CalendarEventType.values()) {
            Calendar<CalendarEventDTO> calendar = createCalendarForType(type);
            if (calendar != null) {
                calendarSource.getCalendars().add(calendar);
            }
        }
        
        log.info("CalendarSource criado com {} calendários", calendarSource.getCalendars().size());
        return calendarSource;
    }

    /**
     * Adiciona eventos a um calendário.
     * 
     * @param calendar calendário de destino
     * @param events lista de eventos
     */
    public void addEventsToCalendar(Calendar<CalendarEventDTO> calendar, List<CalendarEventDTO> events) {
        if (calendar == null || events == null) {
            log.warn("Tentativa de adicionar eventos com calendário ou lista null");
            return;
        }
        
        for (CalendarEventDTO event : events) {
            Entry<CalendarEventDTO> entry = convertToEntry(event);
            if (entry != null) {
                calendar.addEntry(entry);
            }
        }
        
        log.debug("Adicionados {} eventos ao calendário {}", events.size(), calendar.getName());
    }
}
