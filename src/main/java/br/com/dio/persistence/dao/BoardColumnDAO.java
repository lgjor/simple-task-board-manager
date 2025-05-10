package br.com.dio.persistence.dao;

import br.com.dio.dto.BoardColumnDTO;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.CardEntity;
import com.mysql.cj.jdbc.StatementImpl;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.dio.persistence.entity.BoardColumnKindEnum.findByName;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class BoardColumnDAO {

    private final Connection connection;
    private static final int DEFAULT_BOARD_ID = 1;

    public BoardColumnEntity insert(final BoardColumnEntity entity) throws SQLException {
        var sql = "INSERT INTO BOARDS_COLUMNS (name, `order`, kind, board_id) VALUES (?, ?, ?, ?);";
        try(var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setString(i ++, entity.getName());
            statement.setInt(i ++, entity.getOrder());
            statement.setString(i ++, entity.getKind().name());
            statement.setLong(i, entity.getBoard().getId());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
            return entity;
        }
    }

    public void insertDefaultColumns(Long boardId) throws SQLException {
        // Verifica se já foi inserida alguma coluna para este board
        if (!findByBoardId(boardId).isEmpty()) {
            System.out.println("Colunas default já existem para o boardId: " + boardId);
            return;
        }

        String sql = "INSERT INTO BOARDS_COLUMNS (board_id, name, kind, `order`) VALUES (?, ?, ?, ?)";
        try (var preparedStatement = connection.prepareStatement(sql)) {
            // Coluna Inicial
            preparedStatement.setLong(1, boardId);
            preparedStatement.setString(2, "Inicial");
            preparedStatement.setString(3, "INITIAL");
            preparedStatement.setInt(4, 1);
            preparedStatement.addBatch();

            // Coluna Em andamento
            preparedStatement.setLong(1, boardId);
            preparedStatement.setString(2, "Em andamento");
            preparedStatement.setString(3, "IN_PROGRESS");
            preparedStatement.setInt(4, 2);
            preparedStatement.addBatch();

            // Coluna Concluído
            preparedStatement.setLong(1, boardId);
            preparedStatement.setString(2, "Concluído");
            preparedStatement.setString(3, "FINAL");
            preparedStatement.setInt(4, 3);
            preparedStatement.addBatch();

            int[] results = preparedStatement.executeBatch();
            connection.commit();
            System.out.println("Colunas padrão inseridas com sucesso para o boardId: " + boardId);
            System.out.println("Total de colunas inseridas: " + results.length);
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("Erro ao inserir colunas padrão: " + e.getMessage());
            throw e;
        }
    }

    public List<BoardColumnEntity> findByBoardId(Long boardId) throws SQLException {
        System.out.println("Buscando colunas para o boardId: " + boardId);
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = """
        SELECT bc.id AS column_id, 
               bc.name AS column_name, 
               bc.`order` AS column_order, 
               bc.kind AS column_kind, 
               c.id AS card_id, 
               c.title AS card_title, 
               c.description AS card_description
          FROM BOARDS_COLUMNS bc
     LEFT JOIN CARDS c 
            ON c.board_column_id = bc.id
         WHERE bc.board_id = ?
      ORDER BY bc.`order`, c.id;
    """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            BoardColumnEntity currentColumn = null;
            while (resultSet.next()) {
                Long columnId = resultSet.getLong("column_id");
                if (currentColumn == null || !currentColumn.getId().equals(columnId)) {
                    currentColumn = new BoardColumnEntity();
                    currentColumn.setId(columnId);
                    currentColumn.setName(resultSet.getString("column_name"));
                    currentColumn.setOrder(resultSet.getInt("column_order"));
                    currentColumn.setKind(findByName(resultSet.getString("column_kind")));
                    entities.add(currentColumn);
                }
                if (resultSet.getLong("card_id") != 0) {
                    var card = new CardEntity();
                    card.setId(resultSet.getLong("card_id"));
                    card.setTitle(resultSet.getString("card_title"));
                    card.setDescription(resultSet.getString("card_description"));
                    currentColumn.addCard(card);
                }
            }
        }
        return entities;
    }

    public List<BoardColumnDTO> findByBoardIdWithDetails(final Long boardId) throws SQLException {
        List<BoardColumnDTO> dtos = new ArrayList<>();
        var sql =
                """
                SELECT bc.id,
                       bc.name,
                       bc.kind,
                       (SELECT COUNT(c.id)
                               FROM CARDS c
                              WHERE c.board_column_id = bc.id) cards_amount
                  FROM BOARDS_COLUMNS bc
                 WHERE board_id = ?
                 ORDER BY `order`;
                """;
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()){
                var dto = new BoardColumnDTO(
                        resultSet.getLong("bc.id"),
                        resultSet.getString("bc.name"),
                        findByName(resultSet.getString("bc.kind")),
                        resultSet.getInt("cards_amount")
                );
                dtos.add(dto);
            }
            return dtos;
        }
    }

    public List<BoardColumnEntity> findDefaultColumns() throws SQLException {
        List<BoardColumnEntity> columns = new ArrayList<>();
        String sql = "SELECT id, name, kind, `order` FROM BOARDS_COLUMNS WHERE board_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, DEFAULT_BOARD_ID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BoardColumnEntity column = new BoardColumnEntity();
                    column.setId(rs.getLong("id"));
                    column.setName(rs.getString("name"));
                    column.setKind(BoardColumnKindEnum.valueOf(rs.getString("kind")));
                    column.setOrder(rs.getInt("order"));
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    public Optional<BoardColumnEntity> findById(final Long boardId) throws SQLException{
        var sql =
        """
        SELECT bc.name,
               bc.kind,
               c.id,
               c.title,
               c.description
          FROM BOARDS_COLUMNS bc
          LEFT JOIN CARDS c
            ON c.board_column_id = bc.id
         WHERE bc.id = ?;
        """;
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next()){
                var entity = new BoardColumnEntity();
                entity.setName(resultSet.getString("bc.name"));
                entity.setKind(findByName(resultSet.getString("bc.kind")));
                do {
                    var card = new CardEntity();
                    if (isNull(resultSet.getString("c.title"))){
                        break;
                    }
                    card.setId(resultSet.getLong("c.id"));
                    card.setTitle(resultSet.getString("c.title"));
                    card.setDescription(resultSet.getString("c.description"));
                    entity.getCards().add(card);
                }while (resultSet.next());
                return Optional.of(entity);
            }
            return Optional.empty();
        }
    }

    // Método para atualizar a coluna de um cartão
    public void updateCardColumn(Long cardId, Long columnId) throws SQLException {
        String sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?";

        System.out.println("Executando SQL: " + sql);
        System.out.println("Parâmetros: columnId=" + columnId + ", cardId=" + cardId);

        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            // Garante que o autocommit está ativado
            connection.setAutoCommit(true);

            try (var statement = connection.prepareStatement(sql)) {
                statement.setLong(1, columnId);
                statement.setLong(2, cardId);

                int rowsAffected = statement.executeUpdate();

                System.out.println("Linhas afetadas: " + rowsAffected);

                if (rowsAffected == 0) {
                    throw new SQLException("Falha ao atualizar o card. Nenhuma linha afetada.");
                }

                System.out.println("Card " + cardId + " movido para a coluna " + columnId + " com sucesso!");
            }
        } finally {
            // Restaura a configuração original de autocommit
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public Long getBoardIdByColumnId(Long columnId) throws SQLException {
        String sql = "SELECT board_id FROM BOARD_COLUMNS WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, columnId);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("board_id");
            }
            return null;
        }
    }

}
