package org.desviante.controller;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardColumnKindEnum;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.service.BoardQueryService;
import org.desviante.service.CardService;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class CardController {

    public CardController() {
    }

    public CardEntity createCard(BoardEntity board, String title, String description) throws SQLException {
        try (Connection connection = getConnection()) {
            var cardService = new CardService(connection);
            var queryService = new BoardQueryService();

            var fullBoard = queryService.findById(board.getId()).orElseThrow();

            BoardColumnEntity initialColumn = fullBoard.getBoardColumns()
                    .stream()
                    .filter(column -> column.getKind().equals(BoardColumnKindEnum.INITIAL))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Coluna INITIAL não encontrada."));

            var newCard = new CardEntity();
            newCard.setTitle(title);
            newCard.setDescription(description);
            newCard.setBoardColumn(initialColumn);

            cardService.create(newCard);
            return newCard;
        }
    }
}