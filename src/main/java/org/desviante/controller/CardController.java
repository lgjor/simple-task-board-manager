package org.desviante.controller;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.CardService;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class CardController {

    public CardController() {
    }

    public void createCard(BoardEntity board, String title, String description) throws SQLException {
        try (Connection connection = getConnection()) {
            CardService cardService = new CardService(connection);
            cardService.createAndInsertCard(board, title, description);
        }
    }
}