package org.desviante.view.component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.desviante.model.enums.CardType;
import org.desviante.service.TaskManagerFacade;
import org.desviante.service.dto.CardDetailDTO;
import org.desviante.service.dto.CreateTaskRequestDTO;
import org.desviante.service.dto.UpdateCardDetailsDTO;

// Add these imports at the top of CardViewController.java
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.function.BiConsumer;
import org.desviante.model.enums.BoardColumnKindEnum;

public class CardViewController {

    @FXML private VBox cardPane;
    @FXML private Label cardTypeLabel;
    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private Label descriptionLabel;
    @FXML private TextArea descriptionArea;

    // --- COMPONENTES DO RODAPÉ ATUALIZADOS ---
    @FXML private Separator footerSeparator;
    @FXML private VBox footerPane;
    @FXML private HBox creationDateBox;
    @FXML private Label creationDateLabel;
    @FXML private HBox lastUpdateDateBox;
    @FXML private Label lastUpdateDateLabel;
    @FXML private HBox completionDateBox;
    @FXML private Label completionDateLabel;

    // --- BOTÃO PARA CRIAÇÃO DE TAREFAS ---
    @FXML private Button createTaskButton;

    // --- COMPONENTES DE CONTROLE DE EDIÇÃO ---
    @FXML private HBox editControlsBox;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    // --- CAMPOS DE PROGRESSO ---
    @FXML private VBox progressContainer;
    
    // Seções de progresso por tipo
    @FXML private VBox bookProgressSection;
    @FXML private VBox videoProgressSection;
    @FXML private VBox courseProgressSection;
    
    // Campos para BOOK
    @FXML private Label totalPagesLabel;
    @FXML private Spinner<Integer> totalPagesSpinner;
    @FXML private Label currentPageLabel;
    @FXML private Spinner<Integer> currentPageSpinner;
    
    // Campos para VIDEO
    @FXML private Label totalMinutesLabel;
    @FXML private Spinner<Integer> totalMinutesSpinner;
    @FXML private Label currentMinutesLabel;
    @FXML private Spinner<Integer> currentMinutesSpinner;
    
    // Campos para COURSE
    @FXML private Label totalModulesLabel;
    @FXML private Spinner<Integer> totalModulesSpinner;
    @FXML private Label currentModuleLabel;
    @FXML private Spinner<Integer> currentModuleSpinner;
    
    // Campo de progresso geral
    @FXML private Label progressLabel;
    @FXML private Label progressValueLabel;
    
    // Campo de progresso manual para CARD
    @FXML private Label manualProgressLabel;
    @FXML private Spinner<Integer> manualProgressSpinner;

    // Campo de status do card
    @FXML private Label statusValueLabel;

    // --- CAMPOS DE DADOS ---
    private TaskManagerFacade facade;
    private String boardName;
    private CardDetailDTO cardData;
    private Long sourceColumnId;
    private BiConsumer<Long, UpdateCardDetailsDTO> onSaveCallback;

    @FXML
    public void initialize() {
        setupDragAndDrop();
        setupEditMode();
        setupProgressSpinners();
    }

    /**
     * Configura os spinners de progresso com valores padrão e listeners
     */
    private void setupProgressSpinners() {
        // Configurar spinners para BOOK
        totalPagesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        currentPageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
        
        // Configurar spinners para VIDEO
        totalMinutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        currentMinutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
        
        // Configurar spinners para COURSE
        totalModulesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        currentModuleSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
        
        // Configurar spinner para CARD (progresso manual)
        manualProgressSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        
        // Adicionar listeners para validação em tempo real
        setupSpinnerValidation();
        
        // Adicionar listeners para atualizar o progresso em tempo real
        totalPagesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
        currentPageSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
        totalMinutesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
        currentMinutesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
        totalModulesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
        currentModuleSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
        manualProgressSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
    }
    
    /**
     * Configura validações em tempo real para os spinners de progresso.
     */
    private void setupSpinnerValidation() {
        // Validação para BOOK: currentPage não pode ser maior que totalPages
        currentPageSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            Integer totalPages = totalPagesSpinner.getValue();
            if (totalPages != null && newVal != null && newVal > totalPages) {
                currentPageSpinner.getValueFactory().setValue(totalPages);
                showValidationWarning("Página atual ajustada para o total de páginas.");
            }
        });
        
        // Validação para VIDEO: currentMinutes não pode ser maior que totalMinutes
        currentMinutesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            Integer totalMinutes = totalMinutesSpinner.getValue();
            if (totalMinutes != null && newVal != null && newVal > totalMinutes) {
                currentMinutesSpinner.getValueFactory().setValue(totalMinutes);
                showValidationWarning("Tempo atual ajustado para o tempo total.");
            }
        });
        
        // Validação para COURSE: currentModule não pode ser maior que totalModules
        currentModuleSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            Integer totalModules = totalModulesSpinner.getValue();
            if (totalModules != null && newVal != null && newVal > totalModules) {
                currentModuleSpinner.getValueFactory().setValue(totalModules);
                showValidationWarning("Módulo atual ajustado para o total de módulos.");
            }
        });
        
        // Validação para CARD: progresso manual deve estar entre 0-100
        manualProgressSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && (newVal < 0 || newVal > 100)) {
                int clampedValue = Math.max(0, Math.min(100, newVal));
                manualProgressSpinner.getValueFactory().setValue(clampedValue);
                showValidationWarning("Progresso ajustado para " + clampedValue + "%.");
            }
        });
    }
    
    /**
     * Mostra um aviso de validação temporário.
     */
    private void showValidationWarning(String message) {
        // Criar um tooltip temporário
        Tooltip tooltip = new Tooltip(message);
        tooltip.setShowDelay(javafx.util.Duration.millis(100));
        tooltip.setShowDuration(javafx.util.Duration.seconds(3));
        
        // Mostrar o tooltip no card atual
        if (cardPane != null) {
            Tooltip.install(cardPane, tooltip);
            
            // Remover o tooltip após 3 segundos
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> Tooltip.uninstall(cardPane, tooltip));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    public void setData(
            TaskManagerFacade facade,
            String boardName,
            CardDetailDTO card,
            Long sourceColumnId,
            BiConsumer<Long, UpdateCardDetailsDTO> onSaveCallback
    ) {
        this.facade = facade;
        this.boardName = boardName;
        this.cardData = card;
        this.sourceColumnId = sourceColumnId;
        this.onSaveCallback = onSaveCallback;
        updateDisplayData(card);
    }

    public void updateDisplayData(CardDetailDTO card) {
        this.cardData = card;
        updateCardTypeLabel(card);
        titleLabel.setText(card.title());
        descriptionLabel.setText(card.description());
        updateProgressFields(card);
        updateFooter(card);
    }

    /**
     * Atualiza a label da categoria do card
     */
    private void updateCardTypeLabel(CardDetailDTO card) {
        CardType cardType = card.type();
        String typeText = "";
        
        switch (cardType) {
            case BOOK:
                typeText = "Livro";
                break;
            case VIDEO:
                typeText = "Vídeo";
                break;
            case COURSE:
                typeText = "Curso";
                break;
            case CARD:
            default:
                typeText = "Card";
                break;
        }
        
        cardTypeLabel.setText(typeText);
        cardTypeLabel.setVisible(true);
        cardTypeLabel.setManaged(true);
    }

    /**
     * Atualiza os campos de progresso baseado no tipo do card
     */
    private void updateProgressFields(CardDetailDTO card) {
        // Usar o tipo do card do DTO
        CardType cardType = card.type();
        
        // Mostrar/esconder campos baseado no tipo
        showProgressFieldsForType(cardType);
        
        // Atualizar valores dos spinners
        updateSpinnerValues(card);
        
        // Atualizar display de progresso
        updateProgressDisplay();
    }

    /**
     * Atualiza os valores dos spinners baseado nos dados do card
     */
    private void updateSpinnerValues(CardDetailDTO card) {
        // Usar os valores de progresso do DTO
        Integer totalUnits = card.totalUnits();
        Integer currentUnits = card.currentUnits();
        
        // Converter para valores padrão se null
        int total = totalUnits != null ? totalUnits : 0;
        int current = currentUnits != null ? currentUnits : 0;
        
        // Atualizar todos os spinners com os valores apropriados
        totalPagesSpinner.getValueFactory().setValue(total);
        currentPageSpinner.getValueFactory().setValue(current);
        totalMinutesSpinner.getValueFactory().setValue(total);
        currentMinutesSpinner.getValueFactory().setValue(current);
        totalModulesSpinner.getValueFactory().setValue(total);
        currentModuleSpinner.getValueFactory().setValue(current);
        
        // Desabilitar spinners em modo de exibição (somente leitura)
        setSpinnersEditable(false);
    }

    /**
     * Define se os spinners estão habilitados ou somente leitura
     */
    private void setSpinnersEditable(boolean editable) {
        totalPagesSpinner.setDisable(!editable);
        currentPageSpinner.setDisable(!editable);
        totalMinutesSpinner.setDisable(!editable);
        currentMinutesSpinner.setDisable(!editable);
        totalModulesSpinner.setDisable(!editable);
        currentModuleSpinner.setDisable(!editable);
    }

    /**
     * Mostra/esconde campos de progresso baseado no tipo do card
     */
    private void showProgressFieldsForType(CardType cardType) {
        boolean showProgress = cardType != null;
        progressContainer.setVisible(showProgress);
        progressContainer.setManaged(showProgress);
        
        if (showProgress) {
            switch (cardType) {
                case BOOK:
                    showBookFields();
                    break;
                case VIDEO:
                    showVideoFields();
                    break;
                case COURSE:
                    showCourseFields();
                    break;
                case CARD:
                    showCardFields(); // CARD tem campo de progresso manual
                    break;
                default:
                    hideAllProgressFields();
                    break;
            }
        } else {
            hideAllProgressFields();
        }
    }

    private void showBookFields() {
        // Restaurar labels originais
        restoreOriginalLabels();
        
        // Mostrar seção de BOOK
        bookProgressSection.setVisible(true);
        bookProgressSection.setManaged(true);
        
        // Esconder seções de outros tipos
        videoProgressSection.setVisible(false);
        videoProgressSection.setManaged(false);
        courseProgressSection.setVisible(false);
        courseProgressSection.setManaged(false);
    }

    private void showVideoFields() {
        // Restaurar labels originais
        restoreOriginalLabels();
        
        // Mostrar seção de VIDEO
        videoProgressSection.setVisible(true);
        videoProgressSection.setManaged(true);
        
        // Esconder seções de outros tipos
        bookProgressSection.setVisible(false);
        bookProgressSection.setManaged(false);
        courseProgressSection.setVisible(false);
        courseProgressSection.setManaged(false);
    }

    private void showCourseFields() {
        // Restaurar labels originais
        restoreOriginalLabels();
        
        // Mostrar seção de COURSE
        courseProgressSection.setVisible(true);
        courseProgressSection.setManaged(true);
        
        // Esconder seções de outros tipos
        bookProgressSection.setVisible(false);
        bookProgressSection.setManaged(false);
        videoProgressSection.setVisible(false);
        videoProgressSection.setManaged(false);
    }

    private void showCardFields() {
        // Para cards do tipo CARD, usar campos genéricos de unidades
        // Configurar labels para CARD
        totalPagesLabel.setText("Total:");
        currentPageLabel.setText("Atual:");
        
        // Mostrar seção de progresso para CARD
        bookProgressSection.setVisible(true);
        bookProgressSection.setManaged(true);
        
        // Esconder seções de outros tipos
        videoProgressSection.setVisible(false);
        videoProgressSection.setManaged(false);
        courseProgressSection.setVisible(false);
        courseProgressSection.setManaged(false);
    }
    
    private void restoreOriginalLabels() {
        // Restaurar labels originais
        totalPagesLabel.setText("Total de páginas:");
        currentPageLabel.setText("Página atual:");
        totalMinutesLabel.setText("Tempo total (min):");
        currentMinutesLabel.setText("Tempo atual (min):");
        totalModulesLabel.setText("Total de módulos:");
        currentModuleLabel.setText("Módulo atual:");
    }

    private void hideAllProgressFields() {
        bookProgressSection.setVisible(false);
        bookProgressSection.setManaged(false);
        videoProgressSection.setVisible(false);
        videoProgressSection.setManaged(false);
        courseProgressSection.setVisible(false);
        courseProgressSection.setManaged(false);
    }

    /**
     * Atualiza o display de progresso baseado nos valores dos spinners
     */
    private void updateProgressDisplay() {
        // Usar o tipo do card atual
        CardType cardType = cardData != null ? cardData.type() : CardType.CARD;
        
        double progress = 0.0;
        
        if (cardType == CardType.BOOK) {
            int total = totalPagesSpinner.getValue();
            int current = currentPageSpinner.getValue();
            if (total > 0) {
                progress = (double) current / total * 100;
            }
        } else if (cardType == CardType.VIDEO) {
            int total = totalMinutesSpinner.getValue();
            int current = currentMinutesSpinner.getValue();
            if (total > 0) {
                progress = (double) current / total * 100;
            }
        } else if (cardType == CardType.COURSE) {
            int total = totalModulesSpinner.getValue();
            int current = currentModuleSpinner.getValue();
            if (total > 0) {
                progress = (double) current / total * 100;
            }
        } else if (cardType == CardType.CARD) {
            // Para cards do tipo CARD, usar os mesmos spinners que BOOK
            int total = totalPagesSpinner.getValue();
            int current = currentPageSpinner.getValue();
            if (total > 0) {
                progress = (double) current / total * 100;
            }
        }
        
        // Limitar o progresso a 100%
        progress = Math.min(100.0, Math.max(0.0, progress));
        
        // Atualizar o label de progresso
        progressValueLabel.setText(String.format("%.1f%%", progress));
        
        // Atualizar o status do card baseado na coluna atual
        updateCardStatus(cardData.columnKind());
    }
    
    /**
     * Atualiza o status do card baseado na coluna atual.
     * 
     * @param columnKind tipo da coluna atual
     */
    private void updateCardStatus(BoardColumnKindEnum columnKind) {
        String status;
        String cssClass;
        
        switch (columnKind) {
            case INITIAL:
                status = "Não iniciado";
                cssClass = "status-not-started";
                break;
            case PENDING:
                status = "Em andamento";
                cssClass = "status-in-progress";
                break;
            case FINAL:
                status = "Concluído";
                cssClass = "status-completed";
                break;
            default:
                status = "Desconhecido";
                cssClass = "status-unknown";
                break;
        }
        
        // Limpar classes CSS anteriores
        statusValueLabel.getStyleClass().removeAll(
            "status-not-started", "status-in-progress", "status-completed", "status-unknown"
        );
        
        // Aplicar nova classe CSS
        statusValueLabel.getStyleClass().add(cssClass);
        statusValueLabel.setText(status);
    }
    
    /**
     * Atualiza o status do card baseado no progresso (DEPRECATED - agora usa coluna).
     * 
     * @param progress progresso em porcentagem (0-100)
     */
    private void updateCardStatus(double progress) {
        String status;
        if (progress == 0) {
            status = "Não iniciado";
        } else if (progress >= 100) {
            status = "Concluído";
        } else {
            status = "Em andamento";
        }
        
        statusValueLabel.setText(status);
    }

    /**
     * Lógica atualizada para gerenciar a visibilidade de cada linha de data.
     */
    private void updateFooter(CardDetailDTO card) {
        boolean creationVisible = setDateRow(creationDateBox, creationDateLabel, card.creationDate());
        boolean updateVisible = setDateRow(lastUpdateDateBox, lastUpdateDateLabel, card.lastUpdateDate());
        boolean completionVisible = setDateRow(completionDateBox, completionDateLabel, card.completionDate());

        // Torna o rodapé e o separador visíveis apenas se houver alguma data para mostrar
        boolean hasAnyDate = creationVisible || updateVisible || completionVisible;
        footerSeparator.setVisible(hasAnyDate);
        footerSeparator.setManaged(hasAnyDate);
        footerPane.setVisible(hasAnyDate);
        footerPane.setManaged(hasAnyDate);
    }

    private boolean setDateRow(HBox container, Label dateLabel, String dateValue) {
        if (dateValue != null && !dateValue.trim().isEmpty()) {
            dateLabel.setText(dateValue);
            container.setVisible(true);
            container.setManaged(true);
            return true;
        } else {
            container.setVisible(false);
            container.setManaged(false);
            return false;
        }
    }

    public void updateSourceColumn(Long newSourceColumnId) {
        this.sourceColumnId = newSourceColumnId;
    }

    private void setupDragAndDrop() {
        cardPane.setOnDragDetected(event -> {
            Dragboard db = cardPane.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(cardData.id().toString());
            db.setContent(content);
            event.consume();
        });
    }

    private void setupEditMode() {
        cardPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                switchToEditMode();
            }
        });

        titleField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSave();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                switchToDisplayMode();
            }
        });

        descriptionArea.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                handleSave();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                switchToDisplayMode();
            }
        });
    }

    private void switchToEditMode() {
        titleField.setText(titleLabel.getText());
        descriptionArea.setText(descriptionLabel.getText());
        
        titleLabel.setVisible(false);
        titleField.setVisible(true);
        titleField.setManaged(true);
        
        descriptionLabel.setVisible(false);
        descriptionArea.setVisible(true);
        descriptionArea.setManaged(true);
        
        editControlsBox.setVisible(true);
        editControlsBox.setManaged(true);
        
        // Mostrar campos de progresso em modo de edição e torná-los editáveis
        if (progressContainer.isVisible()) {
            progressContainer.setVisible(true);
            progressContainer.setManaged(true);
            setSpinnersEditable(true); // Permitir edição dos spinners
        }
        
        Platform.runLater(() -> titleField.requestFocus());
    }

    private void switchToDisplayMode() {
        titleField.setVisible(false);
        titleField.setManaged(false);
        titleLabel.setVisible(true);
        
        descriptionArea.setVisible(false);
        descriptionArea.setManaged(false);
        descriptionLabel.setVisible(true);
        
        editControlsBox.setVisible(false);
        editControlsBox.setManaged(false);
        
        // Manter campos de progresso visíveis mas somente leitura
        if (progressContainer.isVisible()) {
            progressContainer.setVisible(true);
            progressContainer.setManaged(true);
            setSpinnersEditable(false); // Somente leitura
        }
    }

    @FXML
    private void handleSave() {
        String newTitle = titleField.getText().trim();
        String newDescription = descriptionArea.getText().trim();
        
        if (newTitle.isEmpty()) {
            showAlert("Erro", "O título não pode estar vazio.", Alert.AlertType.ERROR);
            return;
        }
        
        // Coletar valores de progresso se o card suporta progresso
        Integer totalUnits = null;
        Integer currentUnits = null;
        Integer manualProgress = null;
        
        if (cardData != null) {
            switch (cardData.type()) {
                case BOOK:
                    totalUnits = totalPagesSpinner.getValue();
                    currentUnits = currentPageSpinner.getValue();
                    
                    // Validações para BOOK
                    if (totalUnits != null && currentUnits != null) {
                        if (totalUnits <= 0) {
                            showAlert("Erro", "O total de páginas deve ser maior que zero.", Alert.AlertType.ERROR);
                            return;
                        }
                        if (currentUnits < 0) {
                            showAlert("Erro", "A página atual não pode ser negativa.", Alert.AlertType.ERROR);
                            return;
                        }
                        if (currentUnits > totalUnits) {
                            showAlert("Erro", "A página atual não pode ser maior que o total de páginas.", Alert.AlertType.ERROR);
                            return;
                        }
                    }
                    break;
                    
                case VIDEO:
                    totalUnits = totalMinutesSpinner.getValue();
                    currentUnits = currentMinutesSpinner.getValue();
                    
                    // Validações para VIDEO
                    if (totalUnits != null && currentUnits != null) {
                        if (totalUnits <= 0) {
                            showAlert("Erro", "O tempo total deve ser maior que zero.", Alert.AlertType.ERROR);
                            return;
                        }
                        if (currentUnits < 0) {
                            showAlert("Erro", "O tempo atual não pode ser negativo.", Alert.AlertType.ERROR);
                            return;
                        }
                        if (currentUnits > totalUnits) {
                            showAlert("Erro", "O tempo atual não pode ser maior que o tempo total.", Alert.AlertType.ERROR);
                            return;
                        }
                    }
                    break;
                    
                case COURSE:
                    totalUnits = totalModulesSpinner.getValue();
                    currentUnits = currentModuleSpinner.getValue();
                    
                    // Validações para COURSE
                    if (totalUnits != null && currentUnits != null) {
                        if (totalUnits <= 0) {
                            showAlert("Erro", "O total de módulos deve ser maior que zero.", Alert.AlertType.ERROR);
                            return;
                        }
                        if (currentUnits < 0) {
                            showAlert("Erro", "O módulo atual não pode ser negativo.", Alert.AlertType.ERROR);
                            return;
                        }
                        if (currentUnits > totalUnits) {
                            showAlert("Erro", "O módulo atual não pode ser maior que o total de módulos.", Alert.AlertType.ERROR);
                            return;
                        }
                    }
                    break;
                    
                case CARD:
                    totalUnits = totalPagesSpinner.getValue();
                    currentUnits = currentPageSpinner.getValue();
                    
                    // Validações para CARD
                    if (totalUnits != null && currentUnits != null) {
                        if (totalUnits <= 0) {
                            showAlert("Erro", "O total deve ser maior que zero.", Alert.AlertType.ERROR);
                            return;
                        }
                        if (currentUnits < 0) {
                            showAlert("Erro", "O valor atual não pode ser negativo.", Alert.AlertType.ERROR);
                            return;
                        }
                        if (currentUnits > totalUnits) {
                            showAlert("Erro", "O valor atual não pode ser maior que o total.", Alert.AlertType.ERROR);
                            return;
                        }
                    }
                    break;
            }
        }
        
        // Criar DTO de atualização com progresso
        UpdateCardDetailsDTO updateData = new UpdateCardDetailsDTO(newTitle, newDescription, totalUnits, currentUnits, manualProgress);
        
        // Verificar se houve mudança significativa no progresso para mostrar feedback
        String progressChangeMessage = getProgressChangeMessage(cardData, updateData);
        
        if (onSaveCallback != null) {
            onSaveCallback.accept(cardData.id(), updateData);
        }
        
        // Mostrar feedback sobre mudança de coluna se aplicável
        if (progressChangeMessage != null && !progressChangeMessage.isEmpty()) {
            showAlert("Sincronização Automática", progressChangeMessage, Alert.AlertType.INFORMATION);
        }
        
        switchToDisplayMode();
    }
    
    /**
     * Gera mensagem de feedback sobre mudanças de progresso e coluna.
     * 
     * @param originalCard dados originais do card
     * @param updateData dados de atualização
     * @return mensagem de feedback ou null se não houver mudança significativa
     */
    private String getProgressChangeMessage(CardDetailDTO originalCard, UpdateCardDetailsDTO updateData) {
        // Progresso e status estão desacoplados, não mostrar mensagens de sincronização
        return null;
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir Card");
        alert.setContentText("Tem certeza que deseja excluir o card '" + cardData.title() + "'?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    facade.deleteCard(cardData.id());
                    
                    // Notificar o controlador pai para remover o card da interface
                    if (onSaveCallback != null) {
                        onSaveCallback.accept(cardData.id(), null);
                    }
                    
                    showAlert("Sucesso", "Card excluído com sucesso!", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Erro", "Erro ao excluir o card: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleCreateTask() {
        try {
            CreateTaskRequestDTO taskRequest = new CreateTaskRequestDTO(
                    boardName, // listTitle
                    cardData.title(), // title
                    cardData.description(), // notes
                    null, // due (null por enquanto)
                    cardData.id() // cardId
            );
            
            facade.createTaskForCard(taskRequest);
            showAlert("Sucesso", "Tarefa criada com sucesso no Google Tasks!", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            showAlert("Erro", "Erro ao criar tarefa: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}