package org.desviante.persistence.dao;

import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;
import java.sql.Connection;

import java.sql.SQLException;
import java.util.Optional;
import java.sql.Statement;

@AllArgsConstructor
public class CardDAO {

    public CardEntity insert(final CardEntity entity) throws SQLException {
        var sql = "INSERT INTO CARDS (title, description, board_column_id) VALUES (?, ?, ?);";
        try (var connection = getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            statement.setString(i++, entity.getTitle());
            statement.setString(i++, entity.getDescription());

            // Validação de boardColumn presente
            if (entity.getBoardColumn() == null || entity.getBoardColumn().getId() == null) {
                throw new SQLException("Erro: BoardColumn ou ID da coluna é nulo no insert do Card.");
            }
            statement.setLong(i, entity.getBoardColumn().getId());

            statement.executeUpdate();

            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setId(rs.getLong(1)); // ID gerado atribuído ao objeto CardEntity
                } else {
                    throw new SQLException("Erro ao recuperar o ID gerado do Card.");
                }
            }
        }
        return entity;
    }

    public void moveToColumn(final Long columnId, final Long cardId) throws SQLException {
        var sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?;";
        try (var connection = getConnection();
             var statement = connection.prepareStatement(sql)) {

            if (columnId == null || cardId == null) {
                throw new IllegalArgumentException("columnId e cardId não podem ser nulos.");
            }

            int i = 1;
            statement.setLong(i++, columnId);
            statement.setLong(i, cardId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Nenhum registro foi atualizado. Verifique se o cardId existe.");
            }
        }
    }

    public Optional<CardEntity> findById(final Long id) throws SQLException {
        var sql = """
        SELECT c.id AS card_id,
               c.title,
               c.description,
               b.blocked_at,
               b.block_reason,
               c.board_column_id,
               bc.name AS column_name,
               bc.id AS board_column_id,
               bo.id AS board_id,
               bo.name AS board_name,
               (SELECT COUNT(sub_b.id)
                  FROM BLOCKS sub_b
                 WHERE sub_b.card_id = c.id) AS blocks_amount
          FROM cards c
          LEFT JOIN BLOCKS b ON c.id = b.card_id AND b.unblocked_at IS NULL
         INNER JOIN BOARDS_COLUMNS bc ON bc.id = c.board_column_id
         INNER JOIN BOARDS bo ON bc.board_id = bo.id
         WHERE c.id = ?;
        """;

        try (Connection connection = getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BoardEntity board = new BoardEntity();
                    board.setId(resultSet.getLong("board_id"));
                    board.setName(resultSet.getString("board_name"));
                    System.out.println("DEBUG X - board_name do banco: " + resultSet.getString("board_name"));

                    BoardColumnEntity column = new BoardColumnEntity();
                    column.setId(resultSet.getLong("board_column_id"));
                    column.setName(resultSet.getString("column_name"));
                    column.setBoard(board);

                    CardEntity card = new CardEntity();
                    card.setId(resultSet.getLong("card_id")); // alias "card_id" melhora clareza
                    card.setTitle(resultSet.getString("title"));
                    card.setDescription(resultSet.getString("description"));
                    card.setBoardColumn(column);

                    return Optional.of(card);
                }
            }
        }
        return Optional.empty();
    }
}
