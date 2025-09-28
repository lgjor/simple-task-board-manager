package org.desviante.repository;

import org.desviante.calendar.CalendarEvent;
import org.desviante.calendar.CalendarEventPriority;
import org.desviante.calendar.CalendarEventType;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência de eventos do calendário.
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * aos eventos do calendário, incluindo CRUD básico e consultas específicas
 * como busca por período, entidade relacionada, etc.</p>
 * 
 * <p>Os eventos do calendário são persistidos permanentemente no banco de dados
 * para garantir que sejam mantidos entre sessões da aplicação.</p>
 * 
 * <p>Utiliza JDBC direto com NamedParameterJdbcTemplate para operações
 * de banco, mantendo controle total sobre as consultas SQL.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEvent
 */
@Repository
public class CalendarEventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Construtor que inicializa os templates JDBC necessários.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     */
    public CalendarEventRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("calendar_events")
                .usingColumns("title", "description", "start_date_time", "end_date_time", 
                             "all_day", "event_type", "priority", "color", "related_entity_id", 
                             "related_entity_type", "active", "created_at", "updated_at")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * Mapeia os resultados do banco para objetos CalendarEvent.
     * 
     * <p>Trata adequadamente campos que podem ser nulos, como description,
     * color, related_entity_id. Converte timestamps para LocalDateTime
     * e strings para enums apropriados.</p>
     */
    private final RowMapper<CalendarEvent> calendarEventRowMapper = (ResultSet rs, int rowNum) -> {
        CalendarEvent event = new CalendarEvent();
        event.setId(rs.getLong("id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        
        // Converter timestamps para LocalDateTime
        Timestamp startTimestamp = rs.getTimestamp("start_date_time");
        if (startTimestamp != null) {
            event.setStartDateTime(startTimestamp.toLocalDateTime());
        }
        
        Timestamp endTimestamp = rs.getTimestamp("end_date_time");
        if (endTimestamp != null) {
            event.setEndDateTime(endTimestamp.toLocalDateTime());
        }
        
        event.setAllDay(rs.getBoolean("all_day"));
        
        // Converter string para enum
        String eventTypeStr = rs.getString("event_type");
        if (eventTypeStr != null) {
            try {
                event.setType(CalendarEventType.valueOf(eventTypeStr));
            } catch (IllegalArgumentException e) {
                event.setType(CalendarEventType.CARD); // Valor padrão
            }
        }
        
        String priorityStr = rs.getString("priority");
        if (priorityStr != null) {
            try {
                event.setPriority(CalendarEventPriority.valueOf(priorityStr));
            } catch (IllegalArgumentException e) {
                event.setPriority(CalendarEventPriority.LOW); // Valor padrão
            }
        }
        
        event.setColor(rs.getString("color"));
        event.setRelatedEntityId(rs.getObject("related_entity_id", Long.class));
        event.setRelatedEntityType(rs.getString("related_entity_type"));
        event.setActive(rs.getBoolean("active"));
        
        // Converter timestamps de auditoria
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        if (createdAtTimestamp != null) {
            event.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        }
        
        Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");
        if (updatedAtTimestamp != null) {
            event.setUpdatedAt(updatedAtTimestamp.toLocalDateTime());
        }
        
        return event;
    };

    /**
     * Salva um evento no banco de dados.
     * 
     * <p>Se o evento não tiver ID, ele será inserido como novo.
     * Se já tiver ID, será atualizado.</p>
     * 
     * @param event evento a ser salvo
     * @return evento com ID definido (se for novo)
     */
    @Transactional
    public CalendarEvent save(CalendarEvent event) {
        if (event.getId() == null) {
            // Inserir novo evento
            var params = new MapSqlParameterSource()
                    .addValue("title", event.getTitle())
                    .addValue("description", event.getDescription())
                    .addValue("start_date_time", event.getStartDateTime())
                    .addValue("end_date_time", event.getEndDateTime())
                    .addValue("all_day", event.isAllDay())
                    .addValue("event_type", event.getType() != null ? event.getType().name() : "CARD")
                    .addValue("priority", event.getPriority() != null ? event.getPriority().name() : "LOW")
                    .addValue("color", event.getColor())
                    .addValue("related_entity_id", event.getRelatedEntityId())
                    .addValue("related_entity_type", event.getRelatedEntityType())
                    .addValue("active", event.isActive())
                    .addValue("created_at", event.getCreatedAt() != null ? event.getCreatedAt() : LocalDateTime.now())
                    .addValue("updated_at", event.getUpdatedAt() != null ? event.getUpdatedAt() : LocalDateTime.now());
            
            Long id = jdbcInsert.executeAndReturnKey(params).longValue();
            event.setId(id);
        } else {
            // Atualizar evento existente
            String sql = """
                UPDATE calendar_events 
                SET title = :title, description = :description, start_date_time = :start_date_time, 
                    end_date_time = :end_date_time, all_day = :all_day, event_type = :event_type, 
                    priority = :priority, color = :color, related_entity_id = :related_entity_id, 
                    related_entity_type = :related_entity_type, active = :active, updated_at = :updated_at
                WHERE id = :id
                """;
            
            var params = new MapSqlParameterSource()
                    .addValue("id", event.getId())
                    .addValue("title", event.getTitle())
                    .addValue("description", event.getDescription())
                    .addValue("start_date_time", event.getStartDateTime())
                    .addValue("end_date_time", event.getEndDateTime())
                    .addValue("all_day", event.isAllDay())
                    .addValue("event_type", event.getType() != null ? event.getType().name() : "CARD")
                    .addValue("priority", event.getPriority() != null ? event.getPriority().name() : "LOW")
                    .addValue("color", event.getColor())
                    .addValue("related_entity_id", event.getRelatedEntityId())
                    .addValue("related_entity_type", event.getRelatedEntityType())
                    .addValue("active", event.isActive())
                    .addValue("updated_at", LocalDateTime.now());
            
            jdbcTemplate.update(sql, params);
        }
        
        return event;
    }

    /**
     * Busca um evento por ID.
     * 
     * @param id ID do evento
     * @return Optional contendo o evento se encontrado, ou vazio caso contrário
     */
    public Optional<CalendarEvent> findById(Long id) {
        String sql = "SELECT * FROM calendar_events WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, calendarEventRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Lista todos os eventos ativos.
     * 
     * @return lista de todos os eventos ativos
     */
    public List<CalendarEvent> findAll() {
        String sql = "SELECT * FROM calendar_events WHERE active = true ORDER BY start_date_time";
        return jdbcTemplate.query(sql, calendarEventRowMapper);
    }

    /**
     * Busca eventos por período de datas.
     * 
     * @param startDate data de início
     * @param endDate data de fim
     * @return lista de eventos no período
     */
    public List<CalendarEvent> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT * FROM calendar_events 
            WHERE active = true 
            AND start_date_time >= :start_date 
            AND start_date_time <= :end_date 
            ORDER BY start_date_time
            """;
        var params = new MapSqlParameterSource()
                .addValue("start_date", startDate)
                .addValue("end_date", endDate);
        return jdbcTemplate.query(sql, params, calendarEventRowMapper);
    }

    /**
     * Busca eventos por entidade relacionada.
     * 
     * @param relatedEntityId ID da entidade relacionada
     * @param relatedEntityType tipo da entidade relacionada
     * @return lista de eventos relacionados à entidade
     */
    public List<CalendarEvent> findByRelatedEntity(Long relatedEntityId, String relatedEntityType) {
        System.out.println("🔍 CALENDAR EVENT REPOSITORY - Buscando eventos para relatedEntityId: " + relatedEntityId + ", relatedEntityType: " + relatedEntityType);
        
        String sql = """
            SELECT * FROM calendar_events 
            WHERE active = true 
            AND related_entity_id = :related_entity_id 
            AND related_entity_type = :related_entity_type 
            ORDER BY start_date_time
            """;
        var params = new MapSqlParameterSource()
                .addValue("related_entity_id", relatedEntityId)
                .addValue("related_entity_type", relatedEntityType);
        
        List<CalendarEvent> result = jdbcTemplate.query(sql, params, calendarEventRowMapper);
        System.out.println("🔍 CALENDAR EVENT REPOSITORY - Eventos encontrados: " + result.size());
        
        return result;
    }

    /**
     * Remove um evento por ID.
     * 
     * @param id ID do evento a ser removido
     * @return true se o evento foi removido
     */
    @Transactional
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM calendar_events WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        int rowsAffected = jdbcTemplate.update(sql, params);
        return rowsAffected > 0;
    }

    /**
     * Remove eventos por entidade relacionada.
     * 
     * @param relatedEntityId ID da entidade relacionada
     * @param relatedEntityType tipo da entidade relacionada
     * @return número de eventos removidos
     */
    @Transactional
    public int deleteByRelatedEntity(Long relatedEntityId, String relatedEntityType) {
        System.out.println("🗑️ CALENDAR EVENT REPOSITORY - Removendo eventos para relatedEntityId: " + relatedEntityId + ", relatedEntityType: " + relatedEntityType);
        
        String sql = "DELETE FROM calendar_events WHERE related_entity_id = :related_entity_id AND related_entity_type = :related_entity_type";
        var params = new MapSqlParameterSource()
                .addValue("related_entity_id", relatedEntityId)
                .addValue("related_entity_type", relatedEntityType);
        
        int deleted = jdbcTemplate.update(sql, params);
        System.out.println("🗑️ CALENDAR EVENT REPOSITORY - Eventos removidos: " + deleted);
        
        return deleted;
    }

    /**
     * Verifica se existe um evento com o ID especificado.
     * 
     * @param id ID do evento
     * @return true se o evento existe
     */
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM calendar_events WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Conta o número total de eventos ativos.
     * 
     * @return número total de eventos ativos
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM calendar_events WHERE active = true";
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
        return count != null ? count : 0;
    }
}
