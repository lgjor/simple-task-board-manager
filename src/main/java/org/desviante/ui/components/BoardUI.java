package org.desviante.ui.components;

import org.desviante.persistence.dao.BoardColumnDAO;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.CardEntity;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.util.List;

public class BoardUI {

    private final BoardColumnDAO boardColumnDAO;

    public BoardUI(Connection connection) {
        this.boardColumnDAO = new BoardColumnDAO(connection);
    }

    public VBox displayBoardColumns(Long boardId) {
        VBox columnContainer = new VBox();
        try {
            // Busca todas as colunas do board
            List<BoardColumnEntity> columns = boardColumnDAO.findByBoardId(boardId);

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
                    moveButton.setOnAction(e -> moveCardToNextColumn(card, column));
                    cardBox.getChildren().add(moveButton);

                    columnBox.getChildren().add(cardBox);
                }

                columnContainer.getChildren().add(columnBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return columnContainer;
    }

    private void moveCardToNextColumn(CardEntity card, BoardColumnEntity currentColumn) {
        try {
            BoardColumnEntity nextColumn = currentColumn.getBoard().getBoardColumns().stream()
                    .filter(c -> c.getOrder_index() == currentColumn.getOrder_index() + 1)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Não há próxima coluna disponível."));

            currentColumn.removeCard(card);
            nextColumn.addCard(card);
            card.setBoardColumn(nextColumn);

            // Atualiza o banco de dados
            boardColumnDAO.updateCardColumn(card.getId(), nextColumn.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}