package org.desviante.ui.components;

import com.google.api.services.tasks.model.Task;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.desviante.service.TaskService;
import org.desviante.integration.google.GoogleTaskIntegration;
import org.desviante.persistence.entity.CardEntity;
import javafx.scene.layout.VBox;
import org.desviante.service.CardService;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.util.AlertUtils;
import org.desviante.persistence.entity.TaskEntity;

import org.desviante.util.DateTimeConversion;

import java.util.Optional;
import java.util.function.Consumer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardTableComponent {
    private static final Logger logger = LoggerFactory.getLogger(CardTableComponent.class);

    /**
     * Cria uma caixa de card
     * Adiciona eventos de clique e drag and drop
     */

    public static VBox createCardBox(
            CardEntity card,
            TableView<?> tableView,
            VBox columnDisplay,
            Consumer<TableView> loadBoardsConsumer,
            TableView boardTableView
    ) {
        // O método carregarDatasDoCard() foi removido. Os dados agora vêm completos do BoardService.
        VBox cardBox = criarCardVisual(card);
        configurarEventoEdicao(cardBox, card, tableView, columnDisplay, loadBoardsConsumer, boardTableView);

        return cardBox;
    }

    private static VBox criarCardVisual(CardEntity card) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        VBox cardBox = new VBox();
        cardBox.setId("card-" + card.getId());
        cardBox.setStyle("-fx-border-color: #DDDDDD; -fx-background-color: white; -fx-padding: 8; " +
                "-fx-spacing: 3; -fx-border-radius: 3; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        Label titleLabel = new Label(card.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");
        cardBox.getChildren().add(titleLabel);

        Label descLabel = new Label(card.getDescription());
        descLabel.setWrapText(true);
        cardBox.getChildren().add(descLabel);

        Label dateLabel = new Label("Criado em: " +
                card.getCreationDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        cardBox.getChildren().add(dateLabel);

        Label lastUpdateLabel = new Label("Última atualização: " +
                (card.getLastUpdateDate() != null ? card.getLastUpdateDate().format(formatter) : "Não atualizado"));
        lastUpdateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        cardBox.getChildren().add(lastUpdateLabel);

        Label completionLabel = new Label("Concluido em: " +
                (card.getCompletionDate() != null ? card.getCompletionDate().format(formatter) : "Não concluído"));
        completionLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        cardBox.getChildren().add(completionLabel);

        return cardBox;
    }

    private static void configurarEventoEdicao(
            VBox cardBox,
            CardEntity card,
            TableView<?> tableView,
            VBox columnDisplay,
            Consumer<TableView> loadBoardsConsumer,
            TableView boardTableView
    ) {
        Label titleLabel = (Label) cardBox.getChildren().get(0);
        Label descLabel = (Label) cardBox.getChildren().get(1);

        cardBox.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                event.consume();
                TextField titleField = new TextField(card.getTitle());
                titleField.setStyle("-fx-font-weight: bold;");
                cardBox.getChildren().set(0, titleField);

                TextArea descArea = new TextArea(card.getDescription());
                descArea.setWrapText(true);
                descArea.setPrefRowCount(3);
                cardBox.getChildren().set(1, descArea);

                HBox buttons = criarBotoesEdicao(card, cardBox, titleLabel, descLabel, titleField, descArea, tableView, columnDisplay, loadBoardsConsumer, boardTableView);
                cardBox.getChildren().add(buttons);

                Platform.runLater(titleField::requestFocus);
            }
        });
    }

    private static HBox criarBotoesEdicao(
            CardEntity card,
            VBox cardBox,
            Label titleLabel,
            Label descLabel,
            TextField titleField,
            TextArea descArea,
            TableView<?> tableView,
            VBox columnDisplay,
            Consumer<TableView> loadBoardsConsumer,
            TableView boardTableView
    ) {
        HBox buttons = new HBox(5);
        Button saveButton = new Button("Salvar");
        Button cancelButton = new Button("Cancelar");
        Button deleteButton = new Button("Excluir");
        Button taskButton = new Button("Tarefa");
        buttons.getChildren().addAll(saveButton, cancelButton, deleteButton, taskButton);

        saveButton.setOnAction(e -> saveEdition(card, titleField, descArea, cardBox, titleLabel, descLabel, buttons, tableView, columnDisplay, loadBoardsConsumer));
        cancelButton.setOnAction(e -> restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons));
        deleteButton.setOnAction(e -> deleteCard(card, cardBox, boardTableView, loadBoardsConsumer, columnDisplay));
        taskButton.setOnAction(e -> setTask(card, cardBox, titleLabel, descLabel, titleField, descArea, buttons, tableView, columnDisplay, loadBoardsConsumer));

        return buttons;
    }

    // Metodo para definir tarefa
    private static void setTask(
            CardEntity card,
            VBox cardBox,
            Label titleLabel,
            Label descLabel,
            TextField titleField,
            TextArea descArea,
            HBox buttons,
            TableView<?> tableView,
            VBox columnDisplay,
            Consumer<TableView> loadBoardsConsumer
    ) {
        Dialog<TaskEntity> dialog = new Dialog<>();
        dialog.setTitle("Definir Tarefa");
        TextField listTitle = new TextField();
        listTitle.setPromptText("Lista da tarefa");
        // Sugere o título do board, se houver
        String boardTitle = "";
        if (card.getBoardColumn() != null && card.getBoardColumn().getBoard() != null) {
            boardTitle = card.getBoardColumn().getBoard().getName();
        }
        listTitle.setText(boardTitle);

        TextField titleArea = new TextField();
        titleArea.setPromptText("Título da tarefa");
        if (card.getTitle() != null && !card.getTitle().isBlank()) {
            titleArea.setText(card.getTitle());
        }
        DatePicker datePicker = new DatePicker();
        TextField timeField = new TextField();
        timeField.setPromptText("HH:mm");
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Descrição da tarefa");
        if (card.getDescription() != null && !card.getDescription().isBlank()) {
            messageArea.setText(card.getDescription());
        }

        VBox content = new VBox(8,
                new Label("Lista da tarefa"), listTitle,
                new Label("Título da tarefa:"), titleArea,
                new Label("Data:"), datePicker,
                new Label("Hora:"), timeField,
                new Label("Descrição:"), messageArea
        );
        dialog.getDialogPane().setContent(content);

        ButtonType okButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    var date = datePicker.getValue();
                    if (date == null) {
                        AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Por favor, selecione uma data.");
                        return null;
                    }

                    var timeText = timeField.getText().trim();
                    if (timeText.isEmpty()) {
                        AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Por favor, informe o horário no formato HH:mm.");
                        return null;
                    }

                    java.time.LocalTime time;
                    try {
                        time = java.time.LocalTime.parse(timeText);
                    } catch (Exception ex) {
                        AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Formato de hora inválido. Use o formato HH:mm, por exemplo 14:30.");
                        return null;
                    }

                    var due = DateTimeConversion.toOffsetDateTime(date, time);
                    String message = messageArea.getText().trim();
                    if (message.isEmpty()) {
                        AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "A descrição da tarefa não pode estar vazia.");
                        return null;
                    }

                    TaskEntity task = new TaskEntity();
                    task.setListTitle(listTitle.getText().trim());
                    task.setTitle(titleArea.getText().trim());
                    task.setDue(due);
                    task.setNotes(message);
                    task.setSent(false);
                    task.setCard(card);

                    GoogleTaskIntegration.createTaskFromCard(task.getListTitle(), task);

                    return task;

                } catch (Exception ex) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro inesperado", ex.getMessage());
                    ex.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            try {
                // O objeto 'task' já está configurado no resultConverter do diálogo.
                // Apenas delegamos a persistência para o novo TaskService.
                TaskService taskService = new TaskService();
                taskService.createTask(
                        task.getCard().getId(),
                        task.getListTitle(),
                        task.getTitle(),
                        task.getNotes(),
                        task.getDue()
                );
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Tarefa", "Tarefa salva com sucesso!");
            } catch (Exception ex) {
                logger.error("Erro ao salvar tarefa", ex);
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao salvar tarefa: " + ex.getMessage());
            }
        });
    }

    private static void saveEdition(
            CardEntity card,
            TextField titleField,
            TextArea descArea,
            VBox cardBox,
            Label titleLabel,
            Label descLabel,
            HBox buttons,
            TableView<?> tableView,
            VBox columnDisplay,
            Consumer<TableView> loadBoardsConsumer
    ) {
        String newTitle = titleField.getText().trim();
        String newDescription = descArea.getText().trim();
        if (newTitle.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Campos inválidos", "Título e descrição não podem estar vazios.");
            return;
        }
        try {
            // Delega a atualização para o CardService
            CardService cardService = new CardService();
            cardService.updateCard(card.getId(), newTitle, newDescription);

            // Atualiza a UI localmente para feedback imediato
            card.setTitle(newTitle);
            card.setDescription(newDescription);
            card.setLastUpdateDate(LocalDateTime.now()); // Simula a atualização da data
            titleLabel.setText(newTitle);
            descLabel.setText(newDescription);

            // Restaura a visualização normal
            restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);

            // Recarrega o board para garantir que a UI reflita 100% o estado do banco.
            // Isso é opcional, mas garante consistência.
            Platform.runLater(() -> loadBoardsConsumer.accept((TableView<BoardEntity>) tableView));

        } catch (Exception ex) {
            logger.error("Erro ao atualizar o card", ex);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao atualizar", "Erro ao atualizar o card: " + ex.getMessage());
            restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);
        }
    }

    private static void deleteCard(
            CardEntity card,
            VBox cardBox,
            TableView boardTableView,
            Consumer<TableView> loadBoardsConsumer,
            VBox columnDisplay
    ) {
        try {
            CardService cardService = new CardService();
            cardService.delete(card.getId());
            ((VBox) cardBox.getParent()).getChildren().remove(cardBox);
            Platform.runLater(() -> loadBoardsConsumer.accept(boardTableView));
        } catch (Exception e) {
            logger.error("Erro ao excluir o card", e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao excluir o card: " + e.getMessage());
        }
    }

    private static void restoreOriginalView(VBox cardBox, Label titleLabel, Label descLabel,
                                     TextField titleField, TextArea descArea, HBox buttons) {
        int titleIndex = cardBox.getChildren().indexOf(titleField);
        int descIndex = cardBox.getChildren().indexOf(descArea);
        if (titleIndex >= 0) cardBox.getChildren().set(titleIndex, titleLabel);
        if (descIndex >= 0) cardBox.getChildren().set(descIndex, descLabel);
        cardBox.getChildren().remove(buttons);
    }
}
