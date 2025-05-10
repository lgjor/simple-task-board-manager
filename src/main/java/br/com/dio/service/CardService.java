package br.com.dio.service;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.exception.CardBlockedException;
import br.com.dio.exception.CardFinishedException;
import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.dao.BlockDAO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.CardEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static br.com.dio.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.FINAL;

public class CardService {

    private final Connection connection;

    // Construtor para inicializar a conexão
    public CardService(Connection connection) {
        this.connection = connection;
    }

    // Método para inserir o card no BD e associar a coluna inicial
    public void create(CardEntity card) throws SQLException {
        String sql = "INSERT INTO CARDS (title, description, board_column_id) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (card.getBoardColumn() == null || card.getBoardColumn().getId() == null) {
                throw new IllegalStateException("A coluna inicial do card não foi definida corretamente.");
            }

            // Log para depuração
            System.out.println("Inserindo card: " + card.getTitle() + ", Coluna ID: " + card.getBoardColumn().getId());

            statement.setString(1, card.getTitle());
            statement.setString(2, card.getDescription());
            statement.setLong(3, card.getBoardColumn().getId());
            statement.executeUpdate();

            // Recupera o ID gerado
            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    card.setId(generatedKeys.getLong(1));
                    System.out.println("Card inserido com ID: " + card.getId());
                } else {
                    throw new SQLException("Falha ao obter o ID gerado para o card.");
                }
            }

            connection.commit(); // Confirma a transação
        } catch (SQLException ex) {
            connection.rollback(); // Reverte a transação em caso de erro
            System.err.println("Erro ao inserir card: " + ex.getMessage());
            throw ex;
        }
    }

    public void update(CardEntity card) throws SQLException {
        String sql = "UPDATE CARDS SET title = ?, description = ? WHERE id = ?";
        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, card.getTitle());
            preparedStatement.setString(2, card.getDescription());
            preparedStatement.setLong(3, card.getId());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.blocked()) {
                throw new CardBlockedException("O card está bloqueado e não pode ser movido.");
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }
            var nextColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Não há próxima coluna disponível"));
            dao.moveToColumn(nextColumn.id(), cardId);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void cancel(final Long cardId, final Long cancelColumnId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.blocked()) {
                throw new CardBlockedException("O card está bloqueado e não pode ser movido.");
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }
            dao.moveToColumn(cancelColumnId, cardId);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void block(final Long id, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );
            if (dto.blocked()) {
                throw new CardBlockedException("O card já está bloqueado.");
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow();
            if (currentColumn.kind().equals(FINAL) || currentColumn.kind().equals(CANCEL)) {
                throw new IllegalStateException("O card está em uma coluna que não permite bloqueio.");
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.block(reason, id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void unblock(final Long id, final String reason) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );
            if (!dto.blocked()) {
                throw new CardBlockedException("O card não está bloqueado.");
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.unblock(reason, id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    // Verifica se o CardService está implementado corretamente
    public CardEntity findById(Long id) throws SQLException {
        String sql = "SELECT c.id, c.title, c.description, c.board_column_id, " +
                "bc.name as column_name, bc.kind as column_kind " +
                "FROM CARDS c " +
                "JOIN BOARD_COLUMNS bc ON c.board_column_id = bc.id " +
                "WHERE c.id = ?";

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    CardEntity card = new CardEntity();
                    card.setId(resultSet.getLong("id"));
                    card.setTitle(resultSet.getString("title"));
                    card.setDescription(resultSet.getString("description"));

                    BoardColumnEntity column = new BoardColumnEntity();
                    column.setId(resultSet.getLong("board_column_id"));
                    column.setName(resultSet.getString("column_name"));
                    column.setKind(BoardColumnKindEnum.valueOf(resultSet.getString("column_kind")));

                    card.setBoardColumn(column);

                    return card;
                }
            }
        }

        return null;
    }
}