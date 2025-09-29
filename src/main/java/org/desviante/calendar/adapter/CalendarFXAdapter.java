package org.desviante.calendar.adapter;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DayView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.calendar.CalendarEventPriority;
import org.desviante.calendar.CalendarEventType;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.desviante.calendar.view.DayEntryViewPersonalizada;
import org.springframework.stereotype.Component;

import javafx.scene.control.Tooltip;
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
 *   <li>Configurar regras de recorrência para eventos recorrentes</li>
 *   <li>Gerenciar calendários por tipo de evento</li>
 *   <li>Manter sincronização entre os formatos</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
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
     * <p>Este método configura todas as propriedades visuais e comportamentais
     * do Entry do CalendarFX, incluindo:</p>
     * <ul>
     *   <li>Configuração de dia inteiro</li>
     *   <li>Regras de recorrência (formato RRULE)</li>
     *   <li>Localização do evento</li>
     *   <li>Estado ativo/inativo</li>
     *   <li>Cores e opacidade baseadas no tipo e prioridade</li>
     * </ul>
     * 
     * @param entry entry a ser configurado
     * @param eventDTO evento de origem
     */
    private void configureEntryStyle(Entry<CalendarEventDTO> entry, CalendarEventDTO eventDTO) {
        // Configurar propriedades específicas do Entry
        
        // Configurar se é evento de dia inteiro
        entry.setFullDay(eventDTO.isAllDay());
        
        // Configurar se é recorrente
        if (eventDTO.isRecurring() && eventDTO.getRecurrenceRule() != null && !eventDTO.getRecurrenceRule().trim().isEmpty()) {
            try {
                // Configurar regra de recorrência no CalendarFX
                entry.setRecurrenceRule(eventDTO.getRecurrenceRule());
                log.debug("Regra de recorrência configurada para evento {}: {}", eventDTO.getTitle(), eventDTO.getRecurrenceRule());
            } catch (Exception e) {
                log.warn("Erro ao configurar regra de recorrência para evento {}: {}", eventDTO.getTitle(), e.getMessage());
                // Continua sem recorrência se houver erro na configuração
            }
        }
        
        // Configurar localização se disponível
        if (eventDTO.getDescription() != null && !eventDTO.getDescription().isEmpty()) {
            entry.setLocation(eventDTO.getDescription());
        }
        
        // Configurar se está oculto
        entry.setHidden(!eventDTO.isActive());
        
        // Configurar cor e opacidade através de CSS personalizado
        String eventColor = getColorForEventType(eventDTO.getType());
        double opacity = getOpacityForPriority(eventDTO.getPriority());
        
        // Aplicar estilo CSS personalizado
        applyCustomEntryStyle(entry, eventColor, opacity, eventDTO.isRecurring(), eventDTO.isAllDay());
        
        log.debug("Evento {} - Cor: {}, Opacidade: {}, Recorrente: {}, Dia inteiro: {}", 
                 eventDTO.getTitle(), eventColor, opacity, eventDTO.isRecurring(), eventDTO.isAllDay());
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
        
        // Configurar tooltip usando JavaFX Tooltip
        try {
            Tooltip tooltipControl = new Tooltip(tooltip.toString());
            tooltipControl.setWrapText(true);
            tooltipControl.setMaxWidth(300);
            
            // O tooltip será aplicado através da fábrica de visualização personalizada
            // configurada no DayView. Aqui apenas logamos as configurações.
            log.debug("Tooltip configurado para evento {}: {}", eventDTO.getTitle(), tooltip.toString());
        } catch (Exception e) {
            log.warn("Erro ao configurar tooltip para evento {}: {}", eventDTO.getTitle(), e.getMessage());
        }
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
     * Aplica estilo CSS personalizado ao entry.
     * 
     * <p>Nota: O CalendarFX não suporta setStyle diretamente no Entry.
     * O estilo é aplicado através da fábrica de visualização personalizada
     * configurada no DayView.</p>
     * 
     * @param entry entry a ser estilizado
     * @param colorHex cor em formato hexadecimal
     * @param opacity opacidade (0.0 a 1.0)
     * @param isRecurring se o evento é recorrente
     * @param isAllDay se o evento é de dia inteiro
     */
    private void applyCustomEntryStyle(Entry<CalendarEventDTO> entry, String colorHex, double opacity, 
                                     boolean isRecurring, boolean isAllDay) {
        if (entry == null) {
            return;
        }
        
        // O estilo será aplicado através da fábrica de visualização personalizada
        // configurada no DayView. Aqui apenas logamos as configurações.
        log.debug("Configurações de estilo para entrada {}: Cor={}, Opacidade={}, Recorrente={}, Dia inteiro={}", 
                 entry.getTitle(), colorHex, opacity, isRecurring, isAllDay);
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
     * Configura a fábrica de visualização personalizada para um DayView.
     * 
     * <p>Este método configura o DayView para usar a DayEntryViewPersonalizada,
     * permitindo a aplicação de cores e opacidade personalizadas aos eventos.</p>
     * 
     * @param dayView DayView a ser configurado
     */
    public void configureCustomEntryViewFactory(DayView dayView) {
        if (dayView == null) {
            log.warn("Tentativa de configurar fábrica de visualização com DayView null");
            return;
        }
        
        dayView.setEntryViewFactory(param -> {
            DayEntryViewPersonalizada customView = new DayEntryViewPersonalizada(param);
            
            // Obter dados do evento se disponível
            if (param.getUserObject() instanceof CalendarEventDTO eventDTO) {
                String eventColor = getColorForEventType(eventDTO.getType());
                double opacity = getOpacityForPriority(eventDTO.getPriority());
                
                // Aplicar estilo personalizado
                customView.applyCustomStyle(eventColor, opacity, eventDTO.isRecurring(), eventDTO.isAllDay());
                
                // Aplicar tooltip ao evento
                applyTooltipToEntry(customView, eventDTO);
                
                log.debug("Fábrica de visualização configurada para evento: {} - Cor: {}, Opacidade: {}", 
                         eventDTO.getTitle(), eventColor, opacity);
            }
            
            return customView;
        });
        
        log.debug("Fábrica de visualização personalizada configurada para DayView");
    }

    /**
     * Aplica tooltip a uma visualização de entrada personalizada.
     * 
     * @param customView visualização personalizada do evento
     * @param eventDTO dados do evento
     */
    private void applyTooltipToEntry(DayEntryViewPersonalizada customView, CalendarEventDTO eventDTO) {
        if (customView == null || eventDTO == null) {
            return;
        }
        
        try {
            // Criar conteúdo do tooltip
            StringBuilder tooltipContent = new StringBuilder();
            tooltipContent.append("Título: ").append(eventDTO.getTitle()).append("\n");
            
            if (eventDTO.getDescription() != null && !eventDTO.getDescription().isEmpty()) {
                tooltipContent.append("Descrição: ").append(eventDTO.getDescription()).append("\n");
            }
            
            tooltipContent.append("Tipo: ").append(eventDTO.getType().getDisplayName()).append("\n");
            tooltipContent.append("Prioridade: ").append(eventDTO.getPriority().getDisplayName()).append("\n");
            
            if (eventDTO.isAllDay()) {
                tooltipContent.append("Duração: Dia inteiro");
            } else {
                tooltipContent.append("Início: ").append(eventDTO.getStartDateTime().toLocalTime());
                if (eventDTO.getEndDateTime() != null) {
                    tooltipContent.append("\nFim: ").append(eventDTO.getEndDateTime().toLocalTime());
                }
            }
            
            if (eventDTO.isRecurring()) {
                tooltipContent.append("\nRecorrente: Sim");
            }
            
            // Criar e configurar tooltip
            Tooltip tooltip = new Tooltip(tooltipContent.toString());
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(300);
            
            // Aplicar tooltip à visualização personalizada
            Tooltip.install(customView, tooltip);
            
            log.debug("Tooltip aplicado ao evento: {}", eventDTO.getTitle());
            
        } catch (Exception e) {
            log.warn("Erro ao aplicar tooltip ao evento {}: {}", eventDTO.getTitle(), e.getMessage());
        }
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
