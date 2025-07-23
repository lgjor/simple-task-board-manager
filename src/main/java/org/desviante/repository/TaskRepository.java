package org.desviante.repository;

import org.desviante.model.Task;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime; // Import necessário
import java.time.ZoneOffset;     // Import necessário
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public TaskRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("tasks")
                .usingColumns("list_title", "title", "due", "notes", "google_task_id", "sent", "card_id", "creation_date", "last_update_date")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Task> taskRowMapper = (ResultSet rs, int rowNum) -> {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setListTitle(rs.getString("list_title"));
        task.setTitle(rs.getString("title"));
        // Trata campos que podem ser nulos
        Timestamp dueTimestamp = rs.getTimestamp("due");
        if (dueTimestamp != null) {
            // CORREÇÃO: Converte o Instant para um OffsetDateTime usando o fuso UTC.
            task.setDue(dueTimestamp.toInstant().atOffset(ZoneOffset.UTC));
        }
        task.setNotes(rs.getString("notes"));
        task.setGoogleTaskId(rs.getString("google_task_id"));
        task.setSent(rs.getBoolean("sent"));
        task.setCardId(rs.getObject("card_id", Long.class));
        Timestamp creationTimestamp = rs.getTimestamp("creation_date");
        if (creationTimestamp != null) {
            task.setCreationDate(creationTimestamp.toLocalDateTime());
        }
        Timestamp lastUpdateTimestamp = rs.getTimestamp("last_update_date");
        if (lastUpdateTimestamp != null) {
            task.setLastUpdateDate(lastUpdateTimestamp.toLocalDateTime());
        }
        return task;
    };

    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks";
        return jdbcTemplate.query(sql, taskRowMapper);
    }

    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, taskRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Task save(Task task) {
        // A lógica de definir datas foi removida do repositório.
        // Isso deve ser responsabilidade de um serviço.
        var params = new MapSqlParameterSource()
                .addValue("list_title", task.getListTitle())
                .addValue("title", task.getTitle())
                .addValue("due", task.getDue())
                .addValue("notes", task.getNotes())
                .addValue("google_task_id", task.getGoogleTaskId())
                .addValue("sent", task.isSent())
                .addValue("card_id", task.getCardId())
                .addValue("creation_date", task.getCreationDate())
                .addValue("last_update_date", task.getLastUpdateDate());

        if (task.getId() == null) {
            Number newId = jdbcInsert.executeAndReturnKey(params);
            task.setId(newId.longValue());
        } else {
            params.addValue("id", task.getId());
            String sql = """
                    UPDATE tasks SET
                        list_title = :list_title,
                        title = :title,
                        due = :due,
                        notes = :notes,
                        google_task_id = :google_task_id,
                        sent = :sent,
                        card_id = :card_id,
                        last_update_date = :last_update_date
                    WHERE id = :id
                    """;
            jdbcTemplate.update(sql, params);
        }
        return task;
    }

    @Transactional
    public void deleteById(Long id) {
        String sql = "DELETE FROM tasks WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
}