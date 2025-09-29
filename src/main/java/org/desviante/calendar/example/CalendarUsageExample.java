package org.desviante.calendar.example;

import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DayView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.calendar.adapter.CalendarFXAdapter;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.desviante.calendar.CalendarEventType;
import org.desviante.calendar.CalendarEventPriority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Exemplo de uso do CalendarFXAdapter com configuração de cores e opacidade.
 * 
 * <p>Esta classe demonstra como configurar e usar o CalendarFXAdapter
 * com as novas funcionalidades de cores e opacidade personalizadas.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarFXAdapter
 * @see CalendarEventDTO
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarUsageExample {

    private final CalendarFXAdapter calendarFXAdapter;

    /**
     * Exemplo de configuração completa do calendário com cores personalizadas.
     * 
     * @param calendarView CalendarView a ser configurado
     */
    public void configureCalendarWithCustomColors(CalendarView calendarView) {
        log.info("Configurando calendário com cores personalizadas");
        
        // 1. Criar CalendarSource com todos os tipos de calendário
        var calendarSource = calendarFXAdapter.createCalendarSource();
        calendarView.getCalendarSources().add(calendarSource);
        
        // 2. Configurar fábrica de visualização personalizada para o DayView
        DayView dayView = calendarView.getDayPage().getDetailedDayView().getDayView();
        calendarFXAdapter.configureCustomEntryViewFactory(dayView);
        
        // 3. Adicionar alguns eventos de exemplo
        addExampleEvents(calendarSource);
        
        log.info("Calendário configurado com sucesso");
    }

    /**
     * Adiciona eventos de exemplo para demonstrar as diferentes cores e opacidades.
     * 
     * @param calendarSource CalendarSource onde adicionar os eventos
     */
    private void addExampleEvents(com.calendarfx.model.CalendarSource calendarSource) {
        // Evento de Card (azul, opacidade padrão)
        CalendarEventDTO cardEvent = CalendarEventDTO.builder()
                .title("Reunião de Planejamento")
                .description("Discussão sobre próximos passos do projeto")
                .type(CalendarEventType.CARD)
                .priority(CalendarEventPriority.HIGH)
                .startDateTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0))
                .endDateTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .allDay(false)
                .recurring(false)
                .active(true)
                .build();

        // Evento de Task (verde, opacidade baixa)
        CalendarEventDTO taskEvent = CalendarEventDTO.builder()
                .title("Implementar nova funcionalidade")
                .description("Desenvolver feature de notificações")
                .type(CalendarEventType.TASK)
                .priority(CalendarEventPriority.LOW)
                .startDateTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0))
                .endDateTime(LocalDateTime.now().plusDays(2).withHour(16).withMinute(0))
                .allDay(false)
                .recurring(false)
                .active(true)
                .build();

        // Evento de Meeting (vermelho, opacidade padrão)
        CalendarEventDTO meetingEvent = CalendarEventDTO.builder()
                .title("Reunião com Cliente")
                .description("Apresentação do progresso do projeto")
                .type(CalendarEventType.MEETING)
                .priority(CalendarEventPriority.URGENT)
                .startDateTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0))
                .endDateTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(30))
                .allDay(false)
                .recurring(false)
                .active(true)
                .build();

        // Evento de dia inteiro (amarelo, opacidade padrão)
        CalendarEventDTO allDayEvent = CalendarEventDTO.builder()
                .title("Feriado Nacional")
                .description("Dia de independência")
                .type(CalendarEventType.CUSTOM)
                .priority(CalendarEventPriority.STANDARD)
                .startDateTime(LocalDateTime.now().plusDays(7).withHour(0).withMinute(0))
                .endDateTime(LocalDateTime.now().plusDays(7).withHour(23).withMinute(59))
                .allDay(true)
                .recurring(false)
                .active(true)
                .build();

        // Evento recorrente (roxo, opacidade padrão)
        CalendarEventDTO recurringEvent = CalendarEventDTO.builder()
                .title("Stand-up Diário")
                .description("Reunião diária da equipe")
                .type(CalendarEventType.REMINDER)
                .priority(CalendarEventPriority.STANDARD)
                .startDateTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(30))
                .endDateTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0))
                .allDay(false)
                .recurring(true)
                .recurrenceRule("FREQ=DAILY;COUNT=5")
                .active(true)
                .build();

        // Adicionar eventos aos calendários correspondentes
        for (var calendar : calendarSource.getCalendars()) {
            @SuppressWarnings("unchecked")
            var typedCalendar = (com.calendarfx.model.Calendar<CalendarEventDTO>) calendar;
            
            if (calendar.getName().equals("Cards")) {
                calendarFXAdapter.addEventsToCalendar(typedCalendar, List.of(cardEvent));
            } else if (calendar.getName().equals("Tasks")) {
                calendarFXAdapter.addEventsToCalendar(typedCalendar, List.of(taskEvent));
            } else if (calendar.getName().equals("Reuniões")) {
                calendarFXAdapter.addEventsToCalendar(typedCalendar, List.of(meetingEvent));
            } else if (calendar.getName().equals("Eventos Personalizados")) {
                calendarFXAdapter.addEventsToCalendar(typedCalendar, List.of(allDayEvent));
            } else if (calendar.getName().equals("Lembretes")) {
                calendarFXAdapter.addEventsToCalendar(typedCalendar, List.of(recurringEvent));
            }
        }

        log.info("Eventos de exemplo adicionados ao calendário");
    }
}
