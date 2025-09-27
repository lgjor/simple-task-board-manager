package org.desviante.calendar.controller;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
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
import org.desviante.calendar.view.EventDetailsView;
import org.desviante.service.TaskManagerFacade;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller para visualização do calendário (somente leitura).
 * 
 * <p>Este controller gerencia a interface do calendário como uma tela de visualização,
 * integrando o CalendarFX com o sistema de eventos existente. Não permite edição
 * de eventos diretamente no calendário.</p>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Exibir eventos do sistema no calendário</li>
 *   <li>Integrar eventos do sistema com CalendarFX</li>
 *   <li>Controlar navegação entre períodos</li>
 *   <li>Fornecer interface somente leitura</li>
 * </ul>
 * 
 * <p><strong>Nota:</strong> Edições de eventos devem ser feitas através da interface
 * de cards, não diretamente no calendário.</p>
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
    private final TaskManagerFacade taskManagerFacade;
    
    @FXML
    private BorderPane calendarContainer;
    
    @FXML
    private Label statusLabel;
    
    private CalendarView calendarView;
    private CalendarSource calendarSource;
    private LocalDate currentDate = LocalDate.now();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Inicializando CalendarViewController como tela de visualização");
        initializeCalendarView();
        loadEventsForCurrentPeriod();
        updateStatusLabel();
    }

    /**
     * Inicializa a visualização do calendário como tela somente leitura.
     */
    private void initializeCalendarView() {
        calendarView = new CalendarView();
        
        // Configurar o calendário para mostrar a data atual
        calendarView.setToday(LocalDate.now());
        calendarView.setDate(LocalDate.now());
        
        // Criar e configurar o CalendarSource
        calendarSource = calendarFXAdapter.createCalendarSource();
        
        // Configurar todos os calendários como somente leitura
        for (Calendar<?> calendar : calendarSource.getCalendars()) {
            calendar.setReadOnly(true);
        }
        
        calendarView.getCalendarSources().add(calendarSource);
        
        // Garantir que não há menu de contexto (tela somente leitura)
        calendarView.setContextMenuCallback(null);
        calendarView.setEntryContextMenuCallback(null);
        
        // Configurar PopOver personalizado para detalhes de eventos
        calendarView.setEntryDetailsPopOverContentCallback(param -> {
            if (param.getEntry() != null && param.getEntry().getUserObject() instanceof CalendarEventDTO) {
                CalendarEventDTO eventDTO = (CalendarEventDTO) param.getEntry().getUserObject();
                return new EventDetailsView(eventDTO);
            }
            return null;
        });
        
        // Adicionar o calendário ao container
        calendarContainer.setCenter(calendarView);
        
        log.info("CalendarView inicializado como tela de visualização (somente leitura)");
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
                    
                    // Nota: Listeners para propriedades serão configurados quando necessário
                    // CalendarFX não expõe diretamente os entries para configuração de listeners
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
    @SuppressWarnings("unchecked")
    private void clearAllCalendars() {
        if (calendarSource != null) {
            for (Calendar<?> calendar : calendarSource.getCalendars()) {
                ((Calendar<CalendarEventDTO>) calendar).clear();
            }
        }
    }

    /**
     * Obtém o calendário para um tipo específico de evento.
     * 
     * @param type tipo do evento
     * @return calendário correspondente
     */
    @SuppressWarnings("unchecked")
    private Calendar<CalendarEventDTO> getCalendarForType(CalendarEventType type) {
        if (calendarSource == null || type == null) {
            return null;
        }
        
        String calendarName = getCalendarNameForType(type);
        return (Calendar<CalendarEventDTO>) calendarSource.getCalendars().stream()
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
