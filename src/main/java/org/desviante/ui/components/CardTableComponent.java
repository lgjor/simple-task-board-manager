package org.desviante.ui.components;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.desviante.persistence.entity.CardEntity;
import javafx.scene.layout.VBox;
import org.desviante.service.CardService;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.util.AlertUtils;
import org.desviante.persistence.entity.TaskEntity;

import java.util.function.Consumer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.control.Alert;

import static org.desviante.persistence.config.ConnectionConfig.getConnection;

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
        carregarDatasDoCard(card);

        VBox cardBox = criarCardVisual(card);
        configurarEventoEdicao(cardBox, card, tableView, columnDisplay, loadBoardsConsumer, boardTableView);

        return cardBox;
    }

    private static void carregarDatasDoCard(CardEntity card) {
        try (Connection connection = getConnection()) {
            String sql = "SELECT creation_date, last_update_date, completion_date FROM cards WHERE id = ?";
            try (var preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, card.getId());
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        card.setCreationDate(resultSet.getTimestamp("creation_date").toLocalDateTime());
                        Timestamp lastUpdate = resultSet.getTimestamp("last_update_date");
                        if (lastUpdate != null) card.setLastUpdateDate(lastUpdate.toLocalDateTime());
                        Timestamp completionDate = resultSet.getTimestamp("completion_date");
                        if (completionDate != null) card.setCompletionDate(completionDate.toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            // log e tratamento
        }
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

        // Campos de entrada
        DatePicker datePicker = new DatePicker();
        TextField timeField = new TextField();
        timeField.setPromptText("HH:mm");
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Descrição da tarefa");

        VBox content = new VBox(8, new Label("Data:"), datePicker, new Label("Hora:"), timeField, new Label("Mensagem:"), messageArea);
        dialog.getDialogPane().setContent(content);

        ButtonType okButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    var date = datePicker.getValue();
                    var time = java.time.LocalTime.parse(timeField.getText());
                    var dateTime = java.time.LocalDateTime.of(date, time);
                    String message = messageArea.getText().trim();
                    if (date == null || message.isEmpty()) return null;
                    TaskEntity task = new TaskEntity();
                    task.setDue(dateTime);
                    task.setNotes(message);
                    task.setSent(false);
                    task.setCard(card);
                    return task;
                } catch (Exception ex) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Data ou hora inválida.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            // Aqui você pode salvar a tarefa no banco de dados
            try (Connection connection = getConnection()) {
                System.out.println("Banco em uso: " + connection.getMetaData().getURL());
                String sql = "INSERT INTO tasks (date_time, message, sent, card_id) VALUES (?, ?, ?, ?)";
                try (var ps = connection.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(task.getDue()));
                    ps.setString(2, task.getNotes());
                    ps.setInt(3, task.isSent() ? 1 : 0); // Para SQLite
                    ps.setLong(4, card.getId());
                    ps.executeUpdate();
                }
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Tarefa", "Tarefa salva com sucesso!");
            } catch (SQLException ex) {
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
        try (Connection connection = getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                LocalDateTime now = LocalDateTime.now();
                String sql = "UPDATE CARDS SET title = ?, description = ?, last_update_date = ? WHERE id = ?";
                try (var preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, newTitle);
                    preparedStatement.setString(2, newDescription);
                    preparedStatement.setTimestamp(3, Timestamp.valueOf(now));
                    preparedStatement.setLong(4, card.getId());
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        connection.commit();
                        card.setTitle(newTitle);
                        card.setDescription(newDescription);
                        card.setLastUpdateDate(now);
                        titleLabel.setText(newTitle);
                        descLabel.setText(newDescription);
                        // Atualize o label de última atualização se necessário
                        int titleIndex = cardBox.getChildren().indexOf(titleField);
                        int descIndex = cardBox.getChildren().indexOf(descArea);
                        int buttonsIndex = cardBox.getChildren().indexOf(buttons);
                        if (titleIndex >= 0) cardBox.getChildren().set(titleIndex, titleLabel);
                        if (descIndex >= 0) cardBox.getChildren().set(descIndex, descLabel);
                        if (buttonsIndex >= 0) cardBox.getChildren().remove(buttonsIndex);

                        Platform.runLater(() -> BoardTableComponent.loadBoards(
                                (TableView<BoardEntity>) tableView,
                                ((TableView<BoardEntity>) tableView).getItems(),
                                columnDisplay
                        ));
                    }
                }
                if (!connection.isClosed()) {
                    connection.setAutoCommit(originalAutoCommit);
                }
            } catch (SQLException ex) {
                try {
                    if (!connection.isClosed()) {
                        connection.rollback();
                    }
                } catch (SQLException rollbackEx) {
                    logger.error("Erro ao realizar rollback da transacao", rollbackEx);
                }
                logger.error("Erro ao atualizar o card", ex);
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao atualizar", "Erro ao atualizar o card: " + ex.getMessage());
                restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);
            }
        } catch (SQLException ex) {
            logger.error("Erro ao abrir conexão", ex);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro de conexão", "Não foi possível conectar ao banco de dados: " + ex.getMessage());
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
            Connection connection = getConnection();
            CardService cardService = new CardService(connection);
            cardService.delete(card.getId());
            ((VBox) cardBox.getParent()).getChildren().remove(cardBox);
            Platform.runLater(() -> loadBoardsConsumer.accept(boardTableView));
        } catch (SQLException ex) {
            logger.error("Erro ao excluir o card", ex);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao excluir o card: " + ex.getMessage());
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
