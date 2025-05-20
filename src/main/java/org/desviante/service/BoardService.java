package org.desviante.service;

import org.desviante.persistence.dao.BoardDAO;
import org.desviante.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;
import java.util.List;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class BoardService {
    private final BoardDAO boardDAO;

    public List<BoardEntity> findAll() throws SQLException {
        return boardDAO.findAll();
    }

    public void insert(BoardEntity board) throws SQLException {
        var sql = "INSERT INTO boards (name) VALUES (?)";
        try (Connection connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, board.getName());
            preparedStatement.executeUpdate();

            try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    board.setId(generatedKeys.getLong(1));
                }
            }
            connection.commit();
        }
    }

    public boolean delete(final Long id) throws SQLException {
        try (Connection connection = getConnection()) {
            var dao = new BoardDAO(connection);
            if (!dao.exists(id)) {
                return false;
            }
            dao.delete(id);
            connection.commit();
            return true;
        }
    }

    private Connection getConnection() throws SQLException {
        return org.desviante.persistence.config.ConnectionConfig.getConnection();
    }

}
