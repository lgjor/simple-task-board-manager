package org.desviante.calendar.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.desviante.calendar.dto.CalendarEventDTO;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Componente personalizado para exibir detalhes de eventos com melhor contraste.
 * 
 * <p>Esta classe cria uma interface personalizada para mostrar os detalhes
 * de um evento do calendário, organizados na seguinte ordem:
 * 1. Título do evento
 * 2. Descrição (se disponível)
 * 3. Datas de início e fim</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Organização lógica: título → descrição → datas</li>
 *   <li>Texto com bom contraste para legibilidade</li>
 *   <li>Seleção de texto habilitada</li>
 *   <li>Edição desabilitada (somente leitura)</li>
 *   <li>Layout organizado e profissional</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventDTO
 */
public class EventDetailsView extends VBox {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    
    private final CalendarEventDTO eventDTO;
    
    /**
     * Construtor que cria a visualização de detalhes do evento.
     * 
     * @param eventDTO dados do evento a ser exibido
     */
    public EventDetailsView(CalendarEventDTO eventDTO) {
        this.eventDTO = eventDTO;
        
        initializeView();
        setupStyles();
    }
    
    /**
     * Inicializa os componentes da visualização.
     */
    private void initializeView() {
        setSpacing(10);
        setPadding(new Insets(15));
        setAlignment(Pos.TOP_LEFT);
        
        // 1. TÍTULO DO EVENTO
        Label titleLabel = new Label("Título:");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.BLACK);
        
        TextArea titleArea = new TextArea(eventDTO.getTitle());
        titleArea.setEditable(false);
        titleArea.setPrefRowCount(1);
        titleArea.setWrapText(true);
        titleArea.setStyle("-fx-text-fill: #333333; " +
                          "-fx-background-color: #f8f8f8; " +
                          "-fx-border-color: #cccccc; " +
                          "-fx-border-width: 1px; " +
                          "-fx-font-size: 14px; " +
                          "-fx-font-weight: bold;");
        
        // 2. DESCRIÇÃO (se disponível)
        if (eventDTO.getDescription() != null && !eventDTO.getDescription().trim().isEmpty()) {
            Label descriptionLabel = new Label("Descrição:");
            descriptionLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            descriptionLabel.setTextFill(Color.BLACK);
            
            TextArea descriptionArea = new TextArea(eventDTO.getDescription());
            descriptionArea.setEditable(false);
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setWrapText(true);
            descriptionArea.setStyle("-fx-text-fill: #333333; " +
                                    "-fx-background-color: #f8f8f8; " +
                                    "-fx-border-color: #cccccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-font-size: 13px;");
            
            // Adicionar título e descrição primeiro
            getChildren().addAll(titleLabel, titleArea, descriptionLabel, descriptionArea);
        } else {
            // Adicionar apenas título se não há descrição
            getChildren().addAll(titleLabel, titleArea);
        }
        
        // 3. DATAS
        Label startDateLabel = new Label("Data de Início:");
        startDateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        startDateLabel.setTextFill(Color.BLACK);
        
        TextArea startDateArea = new TextArea(formatDateTime(eventDTO.getStartDateTime()));
        startDateArea.setEditable(false);
        startDateArea.setPrefRowCount(1);
        startDateArea.setStyle("-fx-text-fill: #333333; " +
                              "-fx-background-color: #f8f8f8; " +
                              "-fx-border-color: #cccccc; " +
                              "-fx-border-width: 1px; " +
                              "-fx-font-size: 13px;");
        
        Label endDateLabel = new Label("Data de Fim:");
        endDateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        endDateLabel.setTextFill(Color.BLACK);
        
        TextArea endDateArea = new TextArea(formatDateTime(eventDTO.getEndDateTime()));
        endDateArea.setEditable(false);
        endDateArea.setPrefRowCount(1);
        endDateArea.setStyle("-fx-text-fill: #333333; " +
                            "-fx-background-color: #f8f8f8; " +
                            "-fx-border-color: #cccccc; " +
                            "-fx-border-width: 1px; " +
                            "-fx-font-size: 13px;");
        
        // Adicionar as datas por último
        getChildren().addAll(startDateLabel, startDateArea, endDateLabel, endDateArea);
    }
    
    /**
     * Configura os estilos da visualização.
     */
    private void setupStyles() {
        setStyle("-fx-background-color: #ffffff; " +
                "-fx-border-color: #dddddd; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
    }
    
    /**
     * Formata data e hora para exibição.
     * 
     * @param dateTime data e hora a ser formatada
     * @return string formatada
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Não especificado";
        }
        
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
}
