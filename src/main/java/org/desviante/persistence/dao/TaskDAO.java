package org.desviante.persistence.dao;

import org.desviante.persistence.entity.TaskEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.desviante.persistence.config.ConnectionConfig.getConnection;

public class TaskDAO {

    /**
     * Insere uma nova tarefa no banco de dados.
     *
     * @param task A entidade TaskEntity com os dados da tarefa a ser salva.
     * @return TaskEntity com o ID gerado atualizado.
     * @throws SQLException em caso de erro na operação com o banco.
     */
    public TaskEntity insert(TaskEntity task) throws SQLException {
        String sql = "INSERT INTO tasks (date_time, message, sent, card_id, listTitle, title) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(task.getDue().toLocalDateTime()));
            ps.setString(2, task.getNotes());
            ps.setInt(3, task.isSent() ? 1 : 0);
            ps.setLong(4, task.getCard().getId());
            ps.setString(5, task.getListTitle());
            ps.setString(6, task.getTitle());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir a tarefa, nenhuma linha afetada.");
            }

            try (var generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getLong(1));
                }
            }

            // Confirmar transação se necessário
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
        return task;
    }

    // TODO: métodos úteis, como update, delete, findById, etc.
}