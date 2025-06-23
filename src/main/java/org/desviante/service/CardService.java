package org.desviante.service;

import org.desviante.dto.BoardColumnInfoDTO;
import org.desviante.exception.CardBlockedException;
import org.desviante.exception.CardFinishedException;
import org.desviante.exception.EntityNotFoundException;
import org.desviante.persistence.dao.BlockDAO;
import org.desviante.persistence.dao.CardDAO;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardColumnKindEnum;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.persistence.entity.BoardEntity;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.desviante.persistence.config.ConnectionConfig.getConnection;
import static org.desviante.persistence.entity.BoardColumnKindEnum.CANCEL;
import static org.desviante.persistence.entity.BoardColumnKindEnum.FINAL;

public class CardService {
    private Connection connection;

    // Construtor para inicializar a conexão
    public CardService(Connection connection) {
        this.connection = connection;
    }

    // Método para inserir o card no BD e associar a coluna inicial
    public void isertCard(CardEntity card) throws SQLException {
        String sql = "INSERT INTO cards (title, description, board_column_id, creation_date) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (card.getBoardColumn() == null || card.getBoardColumn().getId() == null) {
                throw new IllegalStateException("A coluna inicial do card não foi definida corretamente.");
            }

            System.out.println("Inserindo card: " + card.getTitle() + ", Coluna ID: " + card.getBoardColumn().getId());

            statement.setString(1, card.getTitle());
            statement.setString(2, card.getDescription());
            statement.setLong(3, card.getBoardColumn().getId());
            statement.setTimestamp(4, Timestamp.valueOf(card.getCreationDate()));
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    card.setId(generatedKeys.getLong(1));
                    System.out.println("Card inserido com ID: " + card.getId());
                } else {
                    throw new SQLException("Falha ao obter o ID gerado para o card.");
                }
            }

            connection.commit();
        } catch (SQLException ex) {
            // Se a conexão foi aberta, tente rollback
            throw ex;
        }
    }

    public void createAndInsertCard(BoardEntity board, String title, String description) throws SQLException {
        // Obtém a coluna inicial do board (por exemplo, a primeira coluna)
        BoardColumnEntity initialColumn = board.getBoardColumns().stream()
                .findFirst()
                .orElseThrow(() -> new SQLException("Coluna inicial não encontrada para o board"));

        CardEntity card = new CardEntity();
        card.setTitle(title);
        card.setDescription(description);
        card.setBoardColumn(initialColumn);
        card.setCreationDate(LocalDateTime.now());

        isertCard(card);
    }

    public void update(CardEntity card) throws SQLException {
        String sql = "UPDATE cards SET title = ?, description = ?, last_update_date = ? WHERE id = ?";
        try (Connection connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {

            System.out.println("Atualizando card: " + card.getId() +
                    ", LastUpdateDate: " + card.getLastUpdateDate());

            card.setLastUpdateDate(LocalDateTime.now());
            preparedStatement.setString(1, card.getTitle());
            preparedStatement.setString(2, card.getDescription());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(card.getLastUpdateDate()));
            preparedStatement.setLong(4, card.getId());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw e;
        }
    }

    public void delete(Long cardId) throws SQLException {
        String sql = "DELETE FROM cards WHERE id = ?";
        try (Connection connection = getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Falha ao excluir o card. Card não encontrado.");
            }
            connection.commit();
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try (Connection connection = getConnection()) {
            var dao = new CardDAO();
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.isBlocked()) {
                throw new CardBlockedException("O card está bloqueado e não pode ser movido.");
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.getBoardColumn().getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }
            var nextColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.order_index() == currentColumn.order_index() + 1)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Não há próxima coluna disponível"));

            String sql = "UPDATE cards SET board_column_id = ?, last_update_date = ? WHERE id = ?";
            try (var statement = connection.prepareStatement(sql)) {
                statement.setLong(1, nextColumn.id());
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                statement.setLong(3, cardId);
                statement.executeUpdate();
            }

            connection.commit();
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public void cancel(final Long cardId, final Long cancelColumnId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try (Connection connection = getConnection()) {
            var dao = new CardDAO();
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.isBlocked()) {
                throw new CardBlockedException("O card está bloqueado e não pode ser movido.");
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.getBoardColumn().getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }

            String sql = "UPDATE cards SET board_column_id = ?, last_update_date = ? WHERE id = ?";
            try (var statement = connection.prepareStatement(sql)) {
                statement.setLong(1, cancelColumnId);
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                statement.setLong(3, cardId);
                statement.executeUpdate();
            }

            connection.commit();
        }
    }

    public void block(final Long id, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try (Connection connection = getConnection()) {
            var dao = new CardDAO();
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );
            if (dto.isBlocked()) {
                throw new CardBlockedException("O card já está bloqueado.");
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.getBoardColumn().getId()))
                    .findFirst()
                    .orElseThrow();
            if (currentColumn.kind().equals(FINAL) || currentColumn.kind().equals(CANCEL)) {
                throw new IllegalStateException("O card está em uma coluna que não permite bloqueio.");
            }
            var blockDAO = new BlockDAO();
            blockDAO.block(reason, id);
            connection.commit();
        }
    }

    public void unblock(final Long id, final String reason) throws SQLException {
        try (Connection connection = getConnection()) {
            var dao = new CardDAO();
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );
            if (!dto.isBlocked()) {
                throw new CardBlockedException("O card não está bloqueado.");
            }
            var blockDAO = new BlockDAO();
            blockDAO.unblock(reason, id);
            connection.commit();
        }
    }

    // Verifica se o CardService está implementado corretamente
    public CardEntity findById(Long id) throws SQLException {
        String sql = """
        SELECT c.id, c.title, c.description, c.board_column_id, 
               bc.name AS column_name, bc.kind AS column_kind,
               b.id AS board_id, b.name AS board_name
        FROM cards c
        JOIN boards_columns bc ON c.board_column_id = bc.id
        JOIN boards b ON bc.board_id = b.id
        WHERE c.id = ?
        """;

        try (Connection connection = getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    CardEntity card = new CardEntity();
                    card.setId(resultSet.getLong("id"));
                    card.setTitle(resultSet.getString("title"));
                    card.setDescription(resultSet.getString("description"));

                    BoardEntity board = new BoardEntity();
                    board.setId(resultSet.getLong("board_id"));
                    board.setName(resultSet.getString("board_name"));
                    System.out.println("METODO ATUALIZADO: board_name: " + resultSet.getString("board_name"));

                    BoardColumnEntity column = new BoardColumnEntity();
                    column.setId(resultSet.getLong("board_column_id"));
                    column.setName(resultSet.getString("column_name"));
                    String kindStr = resultSet.getString("column_kind");
                    BoardColumnKindEnum kind = BoardColumnKindEnum.valueOf(kindStr.trim().toUpperCase());
                    column.setKind(kind);
                    column.setBoard(board);
                    card.setBoardColumn(column);
                    card.setBoard(board);
                    return card;
                }
            }
        }

        return null;
    }

    public void moveToColumn(Long cardId, Long targetColumnId) throws SQLException {
        String sql = """
        UPDATE cards
        SET board_column_id = ?,
            last_update_date = CURRENT_TIMESTAMP
        WHERE id = ?""";

        try (Connection connection = getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, targetColumnId);
            statement.setLong(2, cardId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Falha ao mover o card. Card não encontrado.");
            }
            connection.commit();
        }
    }
}