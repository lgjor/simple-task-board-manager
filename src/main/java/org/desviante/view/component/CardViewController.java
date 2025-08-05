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
    
    // Seção de progresso genérica (substitui as seções específicas)
    @FXML private VBox progressSection;
    
    // Campos genéricos de progresso (substitui os campos específicos)
    @FXML private Label totalLabel;
    @FXML private Spinner<Integer> totalSpinner;
    @FXML private Label currentLabel;
    @FXML private Spinner<Integer> currentSpinner;
    
    // Campo de progresso geral
    @FXML private Label progressLabel;
    @FXML private Label progressValueLabel;
    
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
        // Configurar spinners com valores mínimos apropriados
        totalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        currentSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
        
        // Adicionar listeners para validação em tempo real
        setupSpinnerValidation();
        
        // Adicionar listeners para atualizar o progresso em tempo real
        totalSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
        currentSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateProgressDisplay());
    }
    
    /**
     * Configura validações em tempo real para os spinners de progresso.
     */
    private void setupSpinnerValidation() {
        // Validação: current não pode ser maior que total
        currentSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            Integer total = totalSpinner.getValue();
            if (total != null && newVal != null && newVal > total) {
                currentSpinner.getValueFactory().setValue(total);
                showValidationWarning("Valor atual ajustado para o total.");
            }
        });
        
        // Validação: se total for 0 ou null, current deve ser 0
        totalSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            Integer current = currentSpinner.getValue();
            if ((newVal == null || newVal <= 0) && current != null && current > 0) {
                currentSpinner.getValueFactory().setValue(0);
                showValidationWarning("Valor atual ajustado para 0 pois o total é inválido.");
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
        
        // Atualizar status do card baseado na coluna
        updateCardStatus(card.columnKind());
    }

    /**
     * Atualiza os valores dos spinners baseado nos dados do card
     */
    private void updateSpinnerValues(CardDetailDTO card) {
        // Usar os valores de progresso do DTO
        Integer totalUnits = card.totalUnits();
        Integer currentUnits = card.currentUnits();
        
        // Garantir que total seja sempre válido (mínimo 1)
        int total = (totalUnits != null && totalUnits > 0) ? totalUnits : 1;
        int current = (currentUnits != null && currentUnits >= 0) ? currentUnits : 0;
        
        // Garantir que current não seja maior que total
        if (current > total) {
            current = total;
        }
        
        // Atualizar todos os spinners com os valores apropriados
        totalSpinner.getValueFactory().setValue(total);
        currentSpinner.getValueFactory().setValue(current);
        
        // Desabilitar spinners em modo de exibição (somente leitura)
        setSpinnersEditable(false);
    }

    /**
     * Define se os spinners estão habilitados ou somente leitura
     */
    private void setSpinnersEditable(boolean editable) {
        totalSpinner.setDisable(!editable);
        currentSpinner.setDisable(!editable);
    }

    /**
     * Mostra/esconde campos de progresso baseado no tipo do card
     */
    private void showProgressFieldsForType(CardType cardType) {
        boolean showProgress = cardType != null;
        progressContainer.setVisible(showProgress);
        progressContainer.setManaged(showProgress);
        
        if (showProgress) {
            configureProgressFieldsForType(cardType);
        } else {
            hideAllProgressFields();
        }
    }

    private void configureProgressFieldsForType(CardType cardType) {
        // Usar labels padrão para todos os tipos
        totalLabel.setText("Total:");
        currentLabel.setText("Atual:");
        
        // Mostrar seção de progresso genérica
        progressSection.setVisible(true);
        progressSection.setManaged(true);
    }

    private void showBookFields() {
        configureProgressFieldsForType(CardType.BOOK);
    }

    private void showVideoFields() {
        configureProgressFieldsForType(CardType.VIDEO);
    }

    private void showCourseFields() {
        configureProgressFieldsForType(CardType.COURSE);
    }

    private void showCardFields() {
        configureProgressFieldsForType(CardType.CARD);
    }
    
    private void hideAllProgressFields() {
        progressSection.setVisible(false);
        progressSection.setManaged(false);
    }

    /**
     * Atualiza o display de progresso baseado nos valores dos spinners
     */
    private void updateProgressDisplay() {
        if (cardData == null || cardData.type() == null) {
            progressValueLabel.setText("0%");
            return;
        }
        
        double progress = 0.0;
        CardType cardType = cardData.type();
        
        // Usar spinners genéricos para todos os tipos
        int total = totalSpinner.getValue();
        int current = currentSpinner.getValue();
        
        if (total > 0) {
            progress = (double) current / total * 100;
        }
        
        // Limitar progresso a 100%
        progress = Math.min(100.0, progress);
        
        // Atualizar label de progresso
        progressValueLabel.setText(String.format("%.1f%%", progress));
        
        // NÃO atualizar status aqui - isso é feito separadamente baseado na coluna
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
        
        // Coletar valores de progresso usando spinners genéricos
        Integer totalUnits = totalSpinner.getValue();
        Integer currentUnits = currentSpinner.getValue();
        Integer manualProgress = null; // Não usado mais, mantido para compatibilidade
        
        // Validações genéricas para todos os tipos
        if (totalUnits == null || totalUnits <= 0) {
            showAlert("Erro", "O total deve ser maior que zero.", Alert.AlertType.ERROR);
            return;
        }
        if (currentUnits == null || currentUnits < 0) {
            showAlert("Erro", "O valor atual não pode ser negativo.", Alert.AlertType.ERROR);
            return;
        }
        if (currentUnits > totalUnits) {
            showAlert("Erro", "O valor atual não pode ser maior que o total.", Alert.AlertType.ERROR);
            return;
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