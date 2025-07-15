// Arquivo: src/main/java/br/com/dio/ui/components/CardDragAndDropListener.java
package org.desviante.ui.components;

import org.desviante.persistence.entity.CardEntity;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.ICardService;
import org.desviante.service.ProductionCardService;
import org.desviante.util.AlertUtils;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import java.util.Optional;
import java.util.function.Consumer;
import org.desviante.persistence.entity.BoardColumnKindEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;

public class CardDragAndDropListener implements DragDropListenerInterface {
    private static final Logger logger = LoggerFactory.getLogger(CardDragAndDropListener.class);
    private final TableView<BoardEntity> boardTableView;
    private final Consumer<TableView<BoardEntity>> loadBoardsConsumer;
    private final VBox columnDisplay;
    private final ICardService cardService;

    public CardDragAndDropListener(TableView<BoardEntity> boardTableView, Consumer<TableView<BoardEntity>> loadBoardsConsumer, VBox columnDisplay) {
        this.boardTableView = boardTableView;
        this.loadBoardsConsumer = loadBoardsConsumer;
        this.columnDisplay = columnDisplay;
        this.cardService = new ProductionCardService();
    }

    @Override
    public void onCardMoved(Long cardId, Long targetColumnId) {
        // A operação de backend é executada em uma thread separada para não bloquear a UI.
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Usa a instância de cardService do campo da classe.
                this.cardService.moveCard(cardId, targetColumnId);

                // 2. Após a operação ter sucesso, invoca o método de atualização da UI.
                this.refreshView();

            } catch (Exception e) {
                // 3. Captura qualquer erro do serviço e exibe na UI de forma segura.
                logger.error("Falha ao mover card {} para a coluna {}", cardId, targetColumnId, e);
                Platform.runLater(() -> AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao Mover", "Não foi possível mover o card: " + e.getMessage()));
            }
        });
    }

    /**
     * Dispara a atualização da UI na thread do JavaFX usando o Consumer
     * que foi fornecido durante a construção do listener.
     */
    public void refreshView() {
        Platform.runLater(() -> loadBoardsConsumer.accept(boardTableView));
    }

    @Override
    public boolean isCardInFinalColumn(Long cardId) {
        try {
            // A lógica está correta, usando o CardService.
            return cardService.findById(cardId)
                    .map(card -> card.getBoardColumn().getKind() == BoardColumnKindEnum.FINAL)
                    .orElse(false);
        } catch (Exception e) {
            logger.error("Erro ao buscar o card com ID {}", cardId, e);
            return false;
        }
    }

    @Override
    public boolean isTargetColumnFinal(Long columnId) {
        // Correção da assinatura e implementação para verificar a coluna de destino.
        BoardEntity selectedBoard = boardTableView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null || selectedBoard.getBoardColumns() == null) {
            return false;
        }

        return selectedBoard.getBoardColumns().stream()
                .filter(col -> col.getId().equals(columnId))
                .findFirst()
                .map(col -> col.getKind() == BoardColumnKindEnum.FINAL)
                .orElse(false);
    }
}