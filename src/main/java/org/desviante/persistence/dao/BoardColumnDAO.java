package org.desviante.persistence.dao;

import org.desviante.dto.BoardColumnDTO;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardColumnKindEnum;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.util.AlertUtils;
import lombok.RequiredArgsConstructor;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.desviante.persistence.entity.BoardColumnKindEnum.findByName;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class BoardColumnDAO {

    private static final Logger logger = LoggerFactory.getLogger(BoardColumnDAO.class);
    private final Connection connection;
    private static final int DEFAULT_BOARD_ID = 1;
    @Setter
    private RefreshBoardCallback refreshBoardCallback;

    public BoardColumnEntity insert(final BoardColumnEntity entity) throws SQLException {
        var sql = "INSERT INTO BOARDS_COLUMNS (name, order_index, kind, board_id) VALUES (?, ?, ?, ?);";
        try (var statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            var i = 1;
            statement.setString(i++, entity.getName());
            statement.setInt(i++, entity.getOrder_index());
            statement.setString(i++, entity.getKind().name());
            statement.setLong(i, entity.getBoard().getId());
            statement.executeUpdate();
            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setId(rs.getLong(1));
                }
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

        String sql = "INSERT INTO BOARDS_COLUMNS (board_id, name, kind, order_index) VALUES (?, ?, ?, ?)";
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
               bc.order_index AS column_order, 
               bc.kind AS column_kind, 
               c.id AS card_id, 
               c.title AS card_title, 
               c.description AS card_description
          FROM BOARDS_COLUMNS bc
     LEFT JOIN CARDS c 
            ON c.board_column_id = bc.id
         WHERE bc.board_id = ?
      ORDER BY bc.order_index, c.id;
    """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            var resultSet = statement.executeQuery();
            BoardColumnEntity currentColumn = null;
            while (resultSet.next()) {
                Long columnId = resultSet.getLong("column_id");
                if (currentColumn == null || !currentColumn.getId().equals(columnId)) {
                    currentColumn = new BoardColumnEntity();
                    currentColumn.setId(columnId);
                    currentColumn.setName(resultSet.getString("column_name"));
                    currentColumn.setOrder_index(resultSet.getInt("column_order"));
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
                 ORDER BY order_index;
                """;
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, boardId);
            var resultSet = statement.executeQuery();
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
        String sql = "SELECT id, name, kind, order_index FROM BOARDS_COLUMNS WHERE board_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, DEFAULT_BOARD_ID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BoardColumnEntity column = new BoardColumnEntity();
                    column.setId(rs.getLong("id"));
                    column.setName(rs.getString("name"));
                    column.setKind(BoardColumnKindEnum.valueOf(rs.getString("kind")));
                    column.setOrder_index(rs.getInt("order_index"));
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
            var resultSet = statement.executeQuery();
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
    public void updateCardColumn(Long cardId, Long columnId) {
        logger.info("Atualizando coluna do card. cardId={}, columnId={}", cardId, columnId);
        try {
            connection.setAutoCommit(false);

            // 1. Verifica o card atual (lock)
            String sql = "SELECT bc.kind FROM CARDS c JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id WHERE c.id = ?";
            try (var stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, cardId);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Card não encontrado");
                    }
                }
            }

            // 2. (Opcional) Verifica o tipo da coluna destino
            sql = "SELECT kind FROM BOARDS_COLUMNS WHERE id = ?";
            try (var stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, columnId);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Coluna destino não encontrada");
                    }
                }
            }

            // 3. Atualiza o card
            sql = "UPDATE CARDS SET board_column_id = ?, last_update_date = CURRENT_TIMESTAMP, completion_date = ? WHERE id = ?";
            try (var stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, columnId);
                stmt.setTimestamp(2, "FINAL".equals(getTargetColumnType(columnId)) ?
                        new java.sql.Timestamp(System.currentTimeMillis()) : null);
                stmt.setLong(3, cardId);
                int updated = stmt.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("Falha ao atualizar o card");
                }
            }

            // 4. Verifica se o card foi atualizado corretamente
            sql = "SELECT board_column_id FROM CARDS WHERE id = ? AND board_column_id = ?";
            try (var stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, cardId);
                stmt.setLong(2, columnId);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Verificação falhou: card não encontrado na coluna destino");
                    }
                }
            }

            connection.commit();
            System.out.println("Card " + cardId + " movido com sucesso para coluna " + columnId);
        } catch (SQLException e) {
            System.err.println("Erro ao mover card: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro no rollback: " + ex.getMessage());
            }
        }
    }

    private void doUpdateCardColumn(Connection conn, Long cardId, Long columnId, String targetType) throws SQLException {
        // 1. Atualiza o card
        String sql = """
        UPDATE CARDS 
        SET board_column_id = ?,
            last_update_date = CURRENT_TIMESTAMP,
            completion_date = ?
        WHERE id = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, columnId);
            stmt.setTimestamp(2, "FINAL".equals(targetType) ?
                    new java.sql.Timestamp(System.currentTimeMillis()) : null);
            stmt.setLong(3, cardId);

            int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Falha ao atualizar o card");
            }
        }

        // 2. Verifica se a atualização foi bem sucedida
        sql = "SELECT board_column_id FROM CARDS WHERE id = ? AND board_column_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            stmt.setLong(2, columnId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Verificação falhou: card não encontrado na coluna destino");
            }
        }

        // 3. Obtém o boardId
        Long boardId = getBoardIdFromColumn(conn, columnId);
        if (boardId == null) {
            throw new SQLException("Board não encontrado");
        }

        // 4. Commit
        conn.commit();
        System.out.println("Card " + cardId + " movido para coluna " + columnId);

        // 5. Atualiza UI
        updateBoardView(boardId);
    }

    private void showConfirmationForFinalMove(Long cardId, Long columnId, String targetColumnType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Movimentação");
            alert.setHeaderText("Remover Data de Conclusão");
            alert.setContentText("Este card está marcado como concluído. Ao movê-lo para outra coluna, " +
                    "a data de conclusão será removida. Deseja continuar?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                alert.close(); // Fecha o diálogo explicitamente
                CompletableFuture.runAsync(() -> {
                    try (Connection conn = getConnection()) {
                        conn.setAutoCommit(false);
                        executeColumnUpdateWithConnection(conn, cardId, columnId, targetColumnType);
                        verifyUpdateWithConnection(conn, cardId, columnId);
                        conn.commit();

                        Long boardId = getBoardId(conn, columnId);
                        if (boardId != null) {
                            updateUI(boardId);
                        }
                    } catch (Exception e) {
                        Platform.runLater(() ->
                                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao mover o card: " + e.getMessage())
                        );
                    }
                });
            } else {
                alert.close(); // Fecha o diálogo mesmo se cancelar
            }
        });
    }

    private void updateBoardView(Long boardId) {
        if (refreshBoardCallback != null) {
            Platform.runLater(() -> {
                refreshBoardCallback.refresh(boardId, null, null);
                System.out.println("Atualização da UI solicitada para board " + boardId);
            });
        }
    }

    // Método auxiliar para verificar se a conexão está fechada
    private boolean isClosed(Connection connection) {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    private Long getBoardIdFromColumn(Connection conn, Long columnId) throws SQLException {
        String sql = "SELECT board_id FROM BOARDS_COLUMNS WHERE id = ?";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, columnId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("board_id");
            }
        }
        return null;
    }

    // Método auxiliar para obter o boardId
    private Long getBoardId(Connection conn, Long columnId) throws SQLException {
        String sql = "SELECT board_id FROM BOARDS_COLUMNS WHERE id = ?";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, columnId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("board_id");
            }
        }
        return null;
    }

    private void updateUI(Long boardId) {
        try (Connection conn = getConnection()) {
            // ... (busca dos dados atualizados)
            List<BoardColumnEntity> updatedColumns = new ArrayList<>();
            // ... (preenche updatedColumns)

            Platform.runLater(() -> {
                if (refreshBoardCallback != null) {
                    refreshBoardCallback.refresh(boardId, null, null);
                    System.out.println("UI atualizada para board " + boardId);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erro");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Erro ao atualizar a interface: " + e.getMessage());
                errorAlert.show();
            });
        }
    }

    private void executeColumnUpdateWithConnection(Connection conn, Long cardId, Long columnId, String targetColumnType)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
        UPDATE CARDS
        SET board_column_id = ?,
            last_update_date = CURRENT_TIMESTAMP,
            completion_date =
    """);

        if ("FINAL".equals(targetColumnType)) {
            sql.append("CURRENT_TIMESTAMP");
        } else {
            sql.append("NULL");
        }
        sql.append(" WHERE id = ?");

        try (var statement = conn.prepareStatement(sql.toString())) {
            statement.setLong(1, columnId);
            statement.setLong(2, cardId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Falha ao atualizar o card. Nenhuma linha afetada.");
            }
        }
    }

    private void verifyUpdateWithConnection(Connection conn, Long cardId, Long expectedColumnId) throws SQLException {
        String sql = "SELECT board_column_id FROM CARDS WHERE id = ?";
        try (var statement = conn.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            var rs = statement.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Card não encontrado após atualização");
            }

            Long actualColumnId = rs.getLong("board_column_id");
            if (!actualColumnId.equals(expectedColumnId)) {
                throw new SQLException("Falha na atualização: o card não foi movido para a coluna correta");
            }
        }
    }

    private String getCurrentColumnType(Long cardId) throws SQLException {
        String sql = """
        SELECT bc.kind
        FROM CARDS c
        JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id
        WHERE c.id = ?
    """;
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            var rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Card não encontrado");
            }
            return rs.getString("kind");
        }
    }

    private String getTargetColumnType(Long columnId) throws SQLException {
        String sql = "SELECT kind FROM BOARDS_COLUMNS WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, columnId);
            var rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Coluna de destino não encontrada");
            }
            return rs.getString("kind");
        }
    }
}
