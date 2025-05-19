package org.desviante.ui.components;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.desviante.persistence.entity.CardEntity;
import javafx.scene.layout.VBox;
import org.desviante.service.CardService;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.util.AlertUtils;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            Connection connection = getConnection();
            String sql = "SELECT creation_date, last_update_date, completion_date FROM CARDS WHERE id = ?";
            try (var preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, card.getId());
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        card.setCreationDate(resultSet.getTimestamp("creation_date").toLocalDateTime());
                        Timestamp lastUpdate = resultSet.getTimestamp("last_update_date");
                        if (lastUpdate != null) {
                            card.setLastUpdateDate(lastUpdate.toLocalDateTime());
                        }
                        Timestamp completionDate = resultSet.getTimestamp("completion_date");
                        if (completionDate != null) {
                            card.setCompletionDate(completionDate.toLocalDateTime());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar datas do card", e);
            System.err.println("Erro ao buscar datas do card: " + e.getMessage());
        }

        // Cria a caixa principal do card
        VBox cardBox = new VBox();
        cardBox.setId("card-" + card.getId());
        cardBox.setStyle("-fx-border-color: #DDDDDD; -fx-background-color: white; -fx-padding: 8; " +
                "-fx-spacing: 3; -fx-border-radius: 3; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        // Criação dos componentes do card
        Label titleLabel = new Label(card.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");
        cardBox.getChildren().add(titleLabel);

        Label descLabel = new Label(card.getDescription());
        descLabel.setWrapText(true);
        cardBox.getChildren().add(descLabel);

        Label dateLabel = new Label("Criado em: " +
                card.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
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

        // Eventos (ex.: duplo clique para edição)
        cardBox.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                event.consume();
                TextField titleField = new TextField(card.getTitle());
                titleField.setStyle("-fx-font-weight: bold;");
                cardBox.getChildren().set(cardBox.getChildren().indexOf(titleLabel), titleField);

                TextArea descArea = new TextArea(card.getDescription());
                descArea.setWrapText(true);
                descArea.setPrefRowCount(3);
                cardBox.getChildren().set(cardBox.getChildren().indexOf(descLabel), descArea);

                HBox buttons = new HBox(5);
                Button saveButton = new Button("Salvar");
                Button cancelButton = new Button("Cancelar");
                Button deleteButton = new Button("Excluir");
                buttons.getChildren().addAll(saveButton, cancelButton, deleteButton);
                cardBox.getChildren().add(buttons);

                Platform.runLater(titleField::requestFocus);

                // Lógica do botão de salvar
                saveButton.setOnAction(e -> {
                    String newTitle = titleField.getText().trim();
                    String newDescription = descArea.getText().trim();
                    if (newTitle.isEmpty() || newDescription.isEmpty()) {
                        AlertUtils.showAlert(Alert.AlertType.ERROR, "Campos inválidos", "Título e descrição não podem estar vazios.");
                        return;
                    }
                    Connection connection = null;
                    try {
                        connection = getConnection();
                        boolean originalAutoCommit = connection.getAutoCommit();
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
                                lastUpdateLabel.setText("Ultima atualizacao: " + now.format(formatter));
                                int titleIndex = cardBox.getChildren().indexOf(titleField);
                                int descIndex = cardBox.getChildren().indexOf(descArea);
                                int buttonsIndex = cardBox.getChildren().indexOf(buttons);
                                if (titleIndex >= 0) cardBox.getChildren().set(titleIndex, titleLabel);
                                if (descIndex >= 0) cardBox.getChildren().set(descIndex, descLabel);
                                if (buttonsIndex >= 0) cardBox.getChildren().remove(buttonsIndex);
                                BoardTableComponent.refreshBoardView(
                                        ((CardEntity) tableView.getSelectionModel().getSelectedItem()).getId(),
                                        (TableView<BoardEntity>) tableView,
                                        columnDisplay
                                );
                            } else {
                                connection.rollback();
                                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao atualizar", "Nenhum registro foi atualizado no banco de dados.");
                                restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);
                            }
                            connection.setAutoCommit(originalAutoCommit);
                        }
                    } catch (SQLException ex) {
                        logger.error("Erro ao atualizar o card", ex);
                        if (connection != null) {
                            try {
                                connection.rollback();
                            } catch (SQLException rollbackEx) {
                                logger.error("Erro ao realizar rollback da transacao", rollbackEx);
                            }
                        }
                        AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao atualizar", "Nenhum registro foi atualizado no banco de dados " + ex.getMessage());
                        restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);
                    }
                });

                // Lógica do botão de cancelar
                cancelButton.setOnAction(e -> {
                    restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);
                });

                // Lógica do botão de excluir
                deleteButton.setOnAction(e -> {
                    try {
                        Connection connection = getConnection();
                        CardService cardService = new CardService(connection);
                        cardService.delete(card.getId());
                        ((VBox) cardBox.getParent()).getChildren().remove(cardBox);
                        Platform.runLater(() -> loadBoardsConsumer.accept(boardTableView));
                    } catch (SQLException ex) {
                        logger.error("Erro ao excluir o card", ex);
                        AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao excluir o card: "+ ex.getMessage());
                    }
                });
            }
        });

        return cardBox;
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
