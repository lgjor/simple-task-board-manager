package org.desviante.calendar.controller;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.calendar.CalendarEventType;
import org.desviante.calendar.CalendarService;
import org.desviante.calendar.adapter.CalendarFXAdapter;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller para a visualização do calendário.
 * 
 * <p>Este controller gerencia a interface do calendário, integrando
 * o CalendarFX com o sistema de eventos existente.</p>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Gerenciar a visualização do calendário</li>
 *   <li>Integrar eventos do sistema com CalendarFX</li>
 *   <li>Controlar navegação entre períodos</li>
 *   <li>Gerenciar interações do usuário</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarService
 * @see CalendarView
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarViewController implements Initializable {

    private final CalendarService calendarService;
    private final CalendarFXAdapter calendarFXAdapter;
    
    @FXML
    private BorderPane calendarContainer;
    
    @FXML
    private Label statusLabel;
    
    private CalendarView calendarView;
    private CalendarSource calendarSource;
    private LocalDate currentDate = LocalDate.now();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Inicializando CalendarViewController");
        initializeCalendarView();
        loadEventsForCurrentPeriod();
        updateStatusLabel();
    }

    /**
     * Inicializa a visualização do calendário.
     */
    private void initializeCalendarView() {
        calendarView = new CalendarView();
        
        // Configurar o calendário para mostrar a data atual
        calendarView.setToday(LocalDate.now());
        calendarView.setDate(LocalDate.now());
        
        // Criar e configurar o CalendarSource
        calendarSource = calendarFXAdapter.createCalendarSource();
        calendarView.getCalendarSources().add(calendarSource);
        
        // Adicionar o calendário ao container
        calendarContainer.setCenter(calendarView);
        
        log.info("CalendarView inicializado com sucesso");
    }

    /**
     * Carrega eventos para o período atual.
     */
    private void loadEventsForCurrentPeriod() {
        try {
            LocalDate startDate = currentDate.withDayOfMonth(1);
            LocalDate endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            
            List<CalendarEventDTO> events = calendarService.getEventsForDateRange(startDate, endDate);
            
            // Limpar eventos existentes
            clearAllCalendars();
            
            // Agrupar eventos por tipo
            Map<CalendarEventType, List<CalendarEventDTO>> eventsByType = events.stream()
                    .collect(Collectors.groupingBy(CalendarEventDTO::getType));
            
            // Adicionar eventos aos calendários correspondentes
            for (Map.Entry<CalendarEventType, List<CalendarEventDTO>> entry : eventsByType.entrySet()) {
                Calendar<CalendarEventDTO> calendar = getCalendarForType(entry.getKey());
                if (calendar != null) {
                    calendarFXAdapter.addEventsToCalendar(calendar, entry.getValue());
                }
            }
            
            log.info("Carregados {} eventos para o período {} a {}", 
                    events.size(), startDate, endDate);
            
            // Atualizar status
            updateStatusLabel();
            
        } catch (Exception e) {
            log.error("Erro ao carregar eventos para o período atual", e);
            statusLabel.setText("Erro ao carregar eventos: " + e.getMessage());
        }
    }

    /**
     * Atualiza o label de status.
     */
    private void updateStatusLabel() {
        try {
            int eventCount = calendarService.getEventCountOnDate(currentDate);
            statusLabel.setText(String.format("Data atual: %s | Eventos hoje: %d", 
                    currentDate, eventCount));
        } catch (Exception e) {
            log.error("Erro ao atualizar status", e);
            statusLabel.setText("Erro ao atualizar status");
        }
    }

    /**
     * Navega para o mês anterior.
     */
    @FXML
    public void previousMonth() {
        currentDate = currentDate.minusMonths(1);
        calendarView.setDate(currentDate);
        loadEventsForCurrentPeriod();
        log.info("Navegando para mês anterior: {}", currentDate);
    }

    /**
     * Navega para o mês seguinte.
     */
    @FXML
    public void nextMonth() {
        currentDate = currentDate.plusMonths(1);
        calendarView.setDate(currentDate);
        loadEventsForCurrentPeriod();
        log.info("Navegando para mês seguinte: {}", currentDate);
    }

    /**
     * Vai para a data atual.
     */
    @FXML
    public void goToToday() {
        currentDate = LocalDate.now();
        calendarView.setDate(currentDate);
        calendarView.setToday(currentDate);
        loadEventsForCurrentPeriod();
        log.info("Navegando para hoje: {}", currentDate);
    }

    /**
     * Atualiza a visualização do calendário.
     */
    public void refreshCalendar() {
        loadEventsForCurrentPeriod();
        log.info("Calendário atualizado");
    }

    /**
     * Obtém a data atual selecionada.
     */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /**
     * Define a data atual selecionada.
     */
    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
        calendarView.setDate(date);
        loadEventsForCurrentPeriod();
    }

    /**
     * Limpa todos os calendários.
     */
    private void clearAllCalendars() {
        if (calendarSource != null) {
            for (Calendar<CalendarEventDTO> calendar : calendarSource.getCalendars()) {
                calendar.clear();
            }
        }
    }

    /**
     * Obtém o calendário para um tipo específico de evento.
     * 
     * @param type tipo do evento
     * @return calendário correspondente
     */
    private Calendar<CalendarEventDTO> getCalendarForType(CalendarEventType type) {
        if (calendarSource == null || type == null) {
            return null;
        }
        
        String calendarName = getCalendarNameForType(type);
        return calendarSource.getCalendars().stream()
                .filter(calendar -> calendar.getName().equals(calendarName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtém o nome do calendário para o tipo de evento.
     * 
     * @param type tipo do evento
     * @return nome do calendário
     */
    private String getCalendarNameForType(CalendarEventType type) {
        return switch (type) {
            case CARD -> "Cards";
            case TASK -> "Tasks";
            case CUSTOM -> "Eventos Personalizados";
            case MEETING -> "Reuniões";
            case REMINDER -> "Lembretes";
            default -> "Outros";
        };
    }
}
