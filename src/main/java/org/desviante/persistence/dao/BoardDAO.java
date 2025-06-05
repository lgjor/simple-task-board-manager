package org.desviante.persistence.dao;

import org.desviante.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@AllArgsConstructor
public class BoardDAO {
    private final Connection connection;

    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        var sql = "SELECT id, name FROM boards WHERE id = ?;";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    var entity = new BoardEntity();
                    entity.setId(resultSet.getLong("id"));
                    entity.setName(resultSet.getString("name"));
                    return Optional.of(entity);
                }
            }
        }
        return Optional.empty();
    }

    public List<BoardEntity> findAll() throws SQLException {
        List<BoardEntity> boards = new ArrayList<>();
        String sql = "SELECT id, name FROM boards";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BoardEntity board = new BoardEntity();
                board.setId(rs.getLong("id"));
                board.setName(rs.getString("name"));
                boards.add(board);
            }
        }
        return boards;
    }

    public BoardEntity insert(BoardEntity entity) throws SQLException {
        var sql = "INSERT INTO boards (name) VALUES (?);";
        try (var statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());
            statement.executeUpdate();
            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setId(rs.getLong(1));
                }
            }
        }
        return entity;
    }

    public void update(BoardEntity board) throws SQLException {
        String sql = "UPDATE boards SET name = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, board.getName());
            stmt.setLong(2, board.getId());
            int rows = stmt.executeUpdate();
            if (connection.getAutoCommit() == false) {
                connection.commit();
            }
            if (rows == 0) {
                throw new SQLException("Nenhum board atualizado. Verifique o id.");
            }
        }
    }

    public void delete(final Long id) throws SQLException {
        var sql = "DELETE FROM boards WHERE id = ?;";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public boolean exists(final Long id) throws SQLException {
        var sql = "SELECT 1 FROM boards WHERE id = ?;";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}