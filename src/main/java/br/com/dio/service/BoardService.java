package br.com.dio.service;

import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class BoardService {

    private final Connection connection;

    public void insert(BoardEntity board) throws SQLException {
        var sql = "INSERT INTO boards (name) VALUES (?)";
        try (var preparedStatement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, board.getName());
            preparedStatement.executeUpdate();

            try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    board.setId(generatedKeys.getLong(1)); // Define o ID gerado no objeto BoardEntity
                }
            }
            connection.commit(); // Confirma a transação
        } catch (SQLException e) {
            connection.rollback(); // Reverte a transação em caso de erro
            throw e;
        }
    }

    public boolean delete(final Long id) throws SQLException {
        var dao = new BoardDAO(connection);
        try{
            if (!dao.exists(id)) {
                return false;
            }
            dao.delete(id);
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

}
