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

public class CardViewController {

    @FXML private VBox cardPane;
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
        titleLabel.setText(card.title());
        descriptionLabel.setText(card.description());
        updateFooter(card);
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
        footerPane.setVisible(true);
        footerPane.setManaged(true);
    }

    /**
     * Método auxiliar para gerenciar uma linha de data no rodapé.
     * Define o texto da data e controla a visibilidade do container HBox.
     * @return true se a linha estiver visível, false caso contrário.
     */
    private boolean setDateRow(HBox container, Label dateLabel, String dateValue) {
        boolean isVisible = dateValue != null && !dateValue.isBlank();
        if (isVisible) {
            dateLabel.setText(dateValue);
        }
        container.setVisible(isVisible);
        container.setManaged(isVisible);
        return isVisible;
    }

    public void updateSourceColumn(Long newSourceColumnId) {
        this.sourceColumnId = newSourceColumnId;
    }

    private void setupDragAndDrop() {
        cardPane.setOnDragDetected(event -> {
            if (cardData == null) return;
            Dragboard db = cardPane.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(cardData.id() + ":" + sourceColumnId);
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
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                handleSave();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                switchToDisplayMode();
            }
        });
    }

    private void switchToEditMode() {
        titleField.setText(cardData.title());
        descriptionArea.setText(cardData.description());

        // Esconde os elementos de visualização
        titleLabel.setVisible(false);
        descriptionLabel.setVisible(false);
        footerPane.setVisible(false); // Esconde todo o rodapé na edição
        footerSeparator.setVisible(false);

        // Mostra os elementos de edição
        titleField.setVisible(true);
        titleField.setManaged(true);
        descriptionArea.setVisible(true);
        descriptionArea.setManaged(true);
        editControlsBox.setVisible(true);
        editControlsBox.setManaged(true);

        titleField.requestFocus();
    }

    private void switchToDisplayMode() {
        updateDisplayData(this.cardData);

        // Mostra os elementos de visualização
        titleLabel.setVisible(true);
        descriptionLabel.setVisible(true);
        footerPane.setVisible(true); // Mostra o rodapé novamente

        // Esconde os elementos de edição
        titleField.setVisible(false);
        titleField.setManaged(false);
        descriptionArea.setVisible(false);
        descriptionArea.setManaged(false);
        editControlsBox.setVisible(false);
        editControlsBox.setManaged(false);
    }

    @FXML
    private void handleSave() {
        String newTitle = titleField.getText();
        if (newTitle == null || newTitle.isBlank()) {
            switchToDisplayMode(); // Apenas cancela se o título for inválido
            return;
        }

        UpdateCardDetailsDTO updatedDetails = new UpdateCardDetailsDTO(
                newTitle,
                descriptionArea.getText()
        );

        if (onSaveCallback != null) {
            onSaveCallback.accept(cardData.id(), updatedDetails);
        }

        this.cardData = new CardDetailDTO(
                cardData.id(),
                updatedDetails.title(),
                updatedDetails.description(),
                cardData.creationDate(),
                "agora",
                cardData.completionDate()
        );

        switchToDisplayMode();
    }

    @FXML
    private void handleDelete() {
        if (cardData == null) {
            return;
        }

        // Confirmação antes de deletar
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar Exclusão");
        confirmDialog.setHeaderText("Excluir Card");
        confirmDialog.setContentText("Tem certeza que deseja excluir o card '" + cardData.title() + "'?\n\nEsta ação não pode ser desfeita.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Chama o facade para deletar o card
                    facade.deleteCard(cardData.id());
                    
                    // Remove o card da interface
                    if (cardPane.getParent() instanceof VBox) {
                        VBox parent = (VBox) cardPane.getParent();
                        parent.getChildren().remove(cardPane);
                    }
                    
                    // Notifica mudança de dados (se houver callback)
                    if (onSaveCallback != null) {
                        // Usa o callback de save para notificar a mudança
                        // Passa null como UpdateCardDetailsDTO para indicar que foi deletado
                        onSaveCallback.accept(cardData.id(), null);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorDialog = new Alert(Alert.AlertType.ERROR);
                    errorDialog.setTitle("Erro ao Excluir");
                    errorDialog.setHeaderText("Falha na Exclusão");
                    errorDialog.setContentText("Não foi possível excluir o card: " + e.getMessage());
                    errorDialog.showAndWait();
                }
            }
        });
    }

    /**
     * Abre um diálogo para criar uma Task do Google, agora com DatePicker e TimeField.
     */
    @FXML
    private void handleCreateTask() {
        Dialog<CreateTaskRequestDTO> dialog = new Dialog<>();
        dialog.setTitle("Criar Tarefa Google");
        dialog.setHeaderText("Criar uma nova tarefa no Google Tasks a partir deste card.");

        ButtonType createButtonType = new ButtonType("Criar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField listTitleField = new TextField();
        TextField titleField = new TextField();
        TextArea notesArea = new TextArea();
        DatePicker dueDatePicker = new DatePicker();
        TextField dueTimeField = new TextField(); // <-- NOVO CAMPO PARA A HORA

        // Pré-popula os campos
        listTitleField.setText(this.boardName);
        titleField.setText(this.cardData.title());
        notesArea.setText(this.cardData.description());
        dueDatePicker.setPromptText("Data");
        dueTimeField.setPromptText("HH:mm"); // <-- SUGESTÃO DE FORMATO
        dueTimeField.setPrefWidth(80); // Define um tamanho menor para o campo de hora

        grid.add(new Label("Lista de Tarefas:"), 0, 0);
        grid.add(listTitleField, 1, 0);
        grid.add(new Label("Título da Tarefa:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Notas:"), 0, 2);
        grid.add(notesArea, 1, 2);
        grid.add(new Label("Vencimento:"), 0, 3);

        // Agrupa o DatePicker e o TimeField em um HBox para melhor alinhamento
        HBox dateTimeBox = new HBox(10, dueDatePicker, dueTimeField);
        grid.add(dateTimeBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(titleField::requestFocus);

        // Atualiza o conversor de resultado para combinar data e hora
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                LocalDate date = dueDatePicker.getValue();
                String timeText = dueTimeField.getText();
                LocalDateTime dueDateTime = null;

                if (date != null) {
                    try {
                        // Tenta parsear a hora. Se for inválida ou vazia, usa o início do dia.
                        LocalTime time = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
                        dueDateTime = LocalDateTime.of(date, time);
                    } catch (DateTimeParseException e) {
                        // Se a hora for inválida, usa a data com a hora zerada.
                        dueDateTime = date.atStartOfDay();
                    }
                }

                return new CreateTaskRequestDTO(
                        listTitleField.getText(),
                        titleField.getText(),
                        notesArea.getText(),
                        dueDateTime, // <-- Passa o LocalDateTime combinado
                        this.cardData.id()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(request -> {
            try {
                facade.createTaskForCard(request);
                new Alert(Alert.AlertType.INFORMATION, "Tarefa criada com sucesso!").showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Falha ao criar a tarefa: " + e.getMessage()).showAndWait();
            }
        });
    }
}