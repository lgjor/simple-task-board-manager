package org.desviante.persistence.dao;

import org.desviante.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@AllArgsConstructor
public class BoardDAO {

    private Connection connection;

    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        var sql = "SELECT id, name FROM BOARDS WHERE id = ?;";
        try(var statement = connection.prepareStatement(sql)){
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
        var sql = "SELECT id, name FROM BOARDS;";
        var boards = new ArrayList<BoardEntity>();
        try (var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                var entity = new BoardEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                boards.add(entity);
            }
        }
        return boards;
    }

    public BoardEntity insert(BoardEntity entity) throws SQLException {
        var sql = "INSERT INTO BOARDS (name) VALUES (?);";
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

    public void update(BoardEntity entity) throws SQLException {
        var sql = "UPDATE BOARDS SET name = ? WHERE id = ?;";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getName());
            statement.setLong(2, entity.getId());
            statement.executeUpdate();
        }
    }

    public void delete(final Long id) throws SQLException {
        var sql = "DELETE FROM BOARDS WHERE id = ?;";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public boolean exists(final Long id) throws SQLException {
        var sql = "SELECT 1 FROM BOARDS WHERE id = ?;";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}