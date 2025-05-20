// Arquivo: src/main/java/br/com/dio/ui/components/CardDragAndDropListener.java
package org.desviante.ui.components;

import org.desviante.persistence.dao.BoardColumnDAO;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.CardService;
import org.desviante.util.AlertUtils;
import javafx.scene.layout.VBox;
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
    private final VBox columnDisplay;

    public CardDragAndDropListener(TableView boardTableView, Consumer<TableView> loadBoardsConsumer, VBox columnDisplay) {
        this.boardTableView = boardTableView;
        this.loadBoardsConsumer = loadBoardsConsumer;
        this.columnDisplay = columnDisplay;
    }

    public Long getOriginalColumnId(Long cardId) {
        if (cardId == null) {
            logger.warn("ID do card é nulo");
            return null;
        }

        try (Connection connection = getConnection()) {
            String sql = "SELECT board_column_id FROM cards WHERE id = ?";
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
                    Platform.runLater(() -> {
                        BoardEntity selectedBoard = (BoardEntity) boardTableView.getSelectionModel().getSelectedItem();
                        if (selectedBoard != null) {
                            // Atualiza colunas e cards
                            BoardTableComponent.loadBoardColumnsAndCards(selectedBoard, columnDisplay, boardTableView);
                            // Atualiza a lista de boards na TableView
                            BoardTableComponent.loadBoards(boardTableView, boardTableView.getItems(), columnDisplay);
                        }
                    });
                } else {
                    connection.rollback();
                    Platform.runLater(() -> AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao atualizar o card no banco de dados."));
                    System.out.println(String.format("Card não encontrado: %d", cardId));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage()));
            }
        });
    }

    public void refreshView() {
        Platform.runLater(() -> loadBoardsConsumer.accept(boardTableView));
    }

    @Override
    public boolean isCardInFinalColumn(Long cardId) {
        try (Connection connection = getConnection()) {
            CardService cardService = new CardService(connection);
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