// Arquivo: src/main/java/br/com/dio/ui/components/CardDragAndDropListener.java
package org.desviante.ui.components;

import org.desviante.persistence.dao.BoardColumnDAO;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.service.CardService;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;
import org.desviante.persistence.entity.BoardColumnKindEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;

public class CardDragAndDropListener implements DragDropListenerInterface {
    private static final Logger logger = LoggerFactory.getLogger(CardDragAndDropListener.class);
    private final TableView boardTableView;
    private final Consumer<TableView> loadBoardsConsumer;

    // Construtor ajustado conforme atributos usados
    public CardDragAndDropListener(TableView boardTableView, Consumer<TableView> loadBoardsConsumer) {
        this.boardTableView = boardTableView;
        this.loadBoardsConsumer = loadBoardsConsumer;
    }

    public Long getOriginalColumnId(Long cardId) {
        if (cardId == null) {
            logger.warn("ID do card é nulo");
            return null;
        }

        try (Connection connection = getConnection()) {
            String sql = "SELECT board_column_id FROM CARDS WHERE id = ?";
            try (var preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, cardId);

                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Long columnId = resultSet.getLong("board_column_id");
                        logger.info("Card {} está originalmente na coluna {}", cardId, columnId);
                        return columnId;
                    } else {
                        logger.warn("Card com ID {} não encontrado no banco de dados", cardId);
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao recuperar a coluna original do card {}: {}", cardId, e.getMessage());
            return null;
        }
    }

    @Override
    public void onCardMoved(Long cardId, Long targetColumnId) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                connection.setAutoCommit(false);
                BoardColumnDAO boardColumnDAO = new BoardColumnDAO(connection);
                boardColumnDAO.updateCardColumn(cardId, targetColumnId);

                CardService cardService = new CardService(connection);
                CardEntity updatedCard = cardService.findById(cardId);

                if (updatedCard != null && updatedCard.getBoardColumn() != null &&
                        updatedCard.getBoardColumn().getId().equals(targetColumnId)) {
                    connection.commit();
                    Platform.runLater(() -> loadBoardsConsumer.accept(boardTableView));
                    System.out.println(String.format("Card %d atualizado para coluna %d", cardId, targetColumnId));
                } else {
                    connection.rollback();
                    Platform.runLater(() -> showErrorAlert("Erro ao atualizar o card no banco de dados."));
                    System.out.println(String.format("Card não encontrado: %d", cardId));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert(e.getMessage()));
            }
        });
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro ao mover o card");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshView() {
        Platform.runLater(() -> loadBoardsConsumer.accept(boardTableView));
    }

    @Override
    public boolean isCardInFinalColumn(Long cardId) {
        try (Connection conn = getConnection()) {
            CardService cardService = new CardService(conn);
            CardEntity card = cardService.findById(cardId);
            if (card == null || card.getBoardColumn() == null) {
                System.out.println(String.format("Card nao encontrado: %d", cardId));
                return false;
            }
            return card.getBoardColumn().getKind() == BoardColumnKindEnum.FINAL;
        } catch (Exception e) {
            logger.error("Erro ao buscar o card", e);
            return false;
        }
    }

    @Override
    public boolean isTargetColumnFinal(Long cardId) {
        return isCardInFinalColumn(cardId);
    }
}