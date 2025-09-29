package org.desviante.calendar.provider;

import lombok.RequiredArgsConstructor;
import org.desviante.calendar.CalendarEvent;
import org.desviante.calendar.CalendarEventManager;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.desviante.calendar.CalendarEventType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provedor de eventos personalizados do calendário.
 * 
 * <p>Esta implementação gerencia eventos criados diretamente pelo usuário
 * no calendário, independentemente de cards ou tasks do sistema.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventProvider
 */
@Component
@RequiredArgsConstructor
public class CustomEventProvider implements CalendarEventProvider {

    private final CalendarEventManager eventManager;

    @Override
    public List<CalendarEventDTO> getEventsForDateRange(LocalDate start, LocalDate end) {
        return eventManager.findAll().stream()
                .filter(event -> isCustomEvent(event))
                .filter(event -> event.isActiveInPeriod(start, end))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CalendarEventDTO createEvent(CalendarEventDTO eventDTO) {
        CalendarEvent event = convertToEntity(eventDTO);
        event.setType(CalendarEventType.CUSTOM);
        CalendarEvent savedEvent = eventManager.save(event);
        return convertToDTO(savedEvent);
    }

    @Override
    public void updateEvent(CalendarEventDTO eventDTO) {
        CalendarEvent existingEvent = eventManager.findById(eventDTO.getId());
        if (existingEvent != null && isCustomEvent(existingEvent)) {
            CalendarEvent updatedEvent = convertToEntity(eventDTO);
            updatedEvent.setId(eventDTO.getId());
            eventManager.save(updatedEvent);
        }
    }

    @Override
    public void deleteEvent(Long eventId) {
        CalendarEvent event = eventManager.findById(eventId);
        if (event != null && isCustomEvent(event)) {
            eventManager.deleteById(eventId);
        }
    }

    private boolean isCustomEvent(CalendarEvent event) {
        return event.getType() == CalendarEventType.CUSTOM;
    }

    private CalendarEventDTO convertToDTO(CalendarEvent event) {
        return CalendarEventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .allDay(event.isAllDay())
                .type(event.getType())
                .priority(event.getPriority())
                .color(event.getColor())
                .relatedEntityId(event.getRelatedEntityId())
                .relatedEntityType(event.getRelatedEntityType())
                .recurring(event.isRecurring())
                .active(event.isActive())
                .build();
    }

    private CalendarEvent convertToEntity(CalendarEventDTO dto) {
        return CalendarEvent.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .allDay(dto.isAllDay())
                .type(dto.getType())
                .priority(dto.getPriority())
                .color(dto.getColor())
                .relatedEntityId(dto.getRelatedEntityId())
                .relatedEntityType(dto.getRelatedEntityType())
                .recurring(dto.isRecurring())
                .active(dto.isActive())
                .build();
    }
}
