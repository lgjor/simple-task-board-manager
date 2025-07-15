package org.desviante.ui.components;

import org.checkerframework.checker.units.qual.A;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
import org.desviante.service.CardService;
import org.desviante.util.AlertUtils;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BoardUI {

    private final BoardService boardService;
    private final CardService cardService;

    public BoardUI() {
        this.boardService = new BoardService();
        this.cardService = new CardService();
    }

    public VBox displayBoardColumns(Long boardId) {
        VBox columnContainer = new VBox();
        try {
            Optional<BoardEntity> boardOptional = boardService.findById(boardId);
            if (boardOptional.isEmpty()){
                columnContainer.getChildren().add(new Text("Board com ID " + boardId + " não encontrado."));
                return columnContainer;
            }
            BoardEntity board = boardOptional.get();
            List<BoardColumnEntity> columns = board.getBoardColumns();

            // Adiciona as colunas ao container
            for (BoardColumnEntity column : columns) {
                VBox columnBox = new VBox();
                columnBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-spacing: 5;");
                columnBox.getChildren().add(new Text(column.getName()));

                // Adiciona os cards da coluna
                for (CardEntity card : column.getCards()) {
                    VBox cardBox = new VBox();
                    cardBox.setStyle("-fx-border-color: gray; -fx-padding: 5; -fx-spacing: 3;");
                    cardBox.getChildren().add(new Text("Título: " + card.getTitle()));
                    cardBox.getChildren().add(new Text("Descrição: " + card.getDescription()));

                    // Botão para mover o card para a próxima coluna
                    Button moveButton = new Button("Mover");
                    moveButton.setOnAction(e -> moveCardToNextColumn(card, column, board));
                    cardBox.getChildren().add(moveButton);

                    columnBox.getChildren().add(cardBox);
                }

                columnContainer.getChildren().add(columnBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
            columnContainer.getChildren().add(new Text("Erro ao carregar o board: " + e.getMessage()));
        }
        return columnContainer;
    }

    private void moveCardToNextColumn(CardEntity card, BoardColumnEntity currentColumn, BoardEntity board) {
        try {
            // Ordena as colunas para encontrar a próxima de forma confiável
            List<BoardColumnEntity> sortedColumns = board.getBoardColumns().stream()
                    .sorted(Comparator.comparingInt(BoardColumnEntity::getOrder_index))
                    .collect(Collectors.toList());
            BoardColumnEntity nextColumn = null;
            for (int i=0; i<sortedColumns.size() -1; i++){
                if (sortedColumns.get(i).getId().equals(currentColumn.getId())) {
                    nextColumn = sortedColumns.get(i + 1);
                    break;
                }
            }
            if (nextColumn == null) {
                throw new IllegalStateException("Não há próxima coluna disponível.");
            }

            // Delega a operação de mover para o service
            cardService.moveCard(card.getId(), nextColumn.getId());

            // Idealmente, a UI principal seria notificada para se atualizar aqui.
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Card movido com sucesso. Atualize a visualização!");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao Mover", e.getMessage());
        }
    }
}