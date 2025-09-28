package org.desviante.repository;

import org.desviante.model.Task;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZoneOffset;     // Import necessário
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de persistência para tarefas sincronizadas com Google Tasks.
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * às tarefas que são sincronizadas com a API do Google Tasks, incluindo
 * CRUD básico e controle de estado de sincronização.</p>
 * 
 * <p>As tarefas mantêm informações específicas para integração com Google Tasks,
 * como google_task_id, sent (status de envio), list_title (nome da lista no Google),
 * due (data de vencimento com fuso horário) e notes (descrições das tarefas).</p>
 * 
 * <p>Utiliza JDBC direto com NamedParameterJdbcTemplate para operações
 * de banco, mantendo controle total sobre as consultas SQL.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Task
 */
@Repository
public class TaskRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Construtor que inicializa os templates JDBC necessários.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     */
    public TaskRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("tasks")
                .usingColumns("list_title", "title", "due", "notes", "google_task_id", "sent", "card_id", "creation_date", "last_update_date")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * Mapeia os resultados do banco para objetos Task.
     * 
     * <p>Trata adequadamente campos que podem ser nulos, como due, creation_date
     * e last_update_date. Converte timestamps para OffsetDateTime (para due)
     * e LocalDateTime (para outras datas) para uso no sistema.</p>
     * 
     * <p>A conversão de due usa ZoneOffset.UTC para manter consistência
     * com a API do Google Tasks.</p>
     */
    private final RowMapper<Task> taskRowMapper = (ResultSet rs, int rowNum) -> {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setListTitle(rs.getString("list_title"));
        task.setTitle(rs.getString("title"));
        // Trata campos que podem ser nulos
        Timestamp dueTimestamp = rs.getTimestamp("due");
        if (dueTimestamp != null) {
            // Converte o Instant para um OffsetDateTime usando o fuso UTC.
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

    /**
     * Busca todas as tarefas sincronizadas.
     * 
     * @return lista de todas as tarefas no sistema
     */
    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks";
        return jdbcTemplate.query(sql, taskRowMapper);
    }

    /**
     * Busca uma tarefa pelo ID.
     * 
     * @param id ID da tarefa a ser buscada
     * @return Optional contendo a tarefa se encontrada, ou vazio caso contrário
     */
    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, taskRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Salva ou atualiza uma tarefa no banco de dados.
     * 
     * <p>Se a tarefa não tiver ID, ela será inserida como nova.
     * Se já tiver ID, será atualizada.</p>
     * 
     * @param task tarefa a ser salva ou atualizada
     * @return tarefa com ID definido (se for nova)
     */
    public Task save(Task task) {

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

    /**
     * Busca uma tarefa pelo ID do card.
     * 
     * @param cardId ID do card
     * @return Optional contendo a tarefa se encontrada, ou vazio caso contrário
     */
    public Optional<Task> findByCardId(Long cardId) {
        String sql = "SELECT * FROM tasks WHERE card_id = :cardId";
        var params = new MapSqlParameterSource("cardId", cardId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, taskRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Remove uma tarefa pelo ID.
     * 
     * @param id ID da tarefa a ser removida
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM tasks WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
    
    /**
     * Remove uma tarefa pelo ID do card.
     * 
     * @param cardId ID do card
     */
    public void deleteByCardId(Long cardId) {
        String sql = "DELETE FROM tasks WHERE card_id = :cardId";
        var params = new MapSqlParameterSource("cardId", cardId);
        jdbcTemplate.update(sql, params);
    }
}