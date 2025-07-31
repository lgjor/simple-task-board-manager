package org.desviante.repository;

import org.desviante.model.BoardGroup;
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

@Repository
public class BoardGroupRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public BoardGroupRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("board_groups")
                .usingColumns("name", "description", "color", "icon", "creation_date")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<BoardGroup> boardGroupRowMapper = (ResultSet rs, int rowNum) -> {
        BoardGroup boardGroup = new BoardGroup();
        boardGroup.setId(rs.getLong("id"));
        boardGroup.setName(rs.getString("name"));
        boardGroup.setDescription(rs.getString("description"));
        boardGroup.setColor(rs.getString("color"));
        boardGroup.setIcon(rs.getString("icon"));
        
        Timestamp creationDate = rs.getTimestamp("creation_date");
        if (creationDate != null) {
            boardGroup.setCreationDate(creationDate.toLocalDateTime());
        }
        
        // Removido isDefault - não precisamos mais de grupo padrão
        return boardGroup;
    };

    public List<BoardGroup> findAll() {
        String sql = "SELECT * FROM board_groups ORDER BY name";
        return jdbcTemplate.query(sql, boardGroupRowMapper);
    }

    public Optional<BoardGroup> findById(Long id) {
        String sql = "SELECT * FROM board_groups WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, boardGroupRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public BoardGroup save(BoardGroup boardGroup) {
        var params = new MapSqlParameterSource()
                .addValue("name", boardGroup.getName())
                .addValue("description", boardGroup.getDescription())
                .addValue("color", boardGroup.getColor())
                .addValue("icon", boardGroup.getIcon())
                .addValue("creation_date", boardGroup.getCreationDate());
                // Removido is_default - não precisamos mais de grupo padrão

        if (boardGroup.getId() == null) {
            Number newId = jdbcInsert.executeAndReturnKey(params);
            boardGroup.setId(newId.longValue());
        } else {
            params.addValue("id", boardGroup.getId());
            String sql = "UPDATE board_groups SET name = :name, description = :description, " +
                        "color = :color, icon = :icon WHERE id = :id";
            jdbcTemplate.update(sql, params);
        }
        return boardGroup;
    }

    @Transactional
    public void deleteById(Long id) {
        String sql = "DELETE FROM board_groups WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }

    // Removido findDefaultGroup() - não precisamos mais de grupo padrão

    public Optional<BoardGroup> findByName(String name) {
        String sql = "SELECT * FROM board_groups WHERE UPPER(name) = UPPER(:name)";
        var params = new MapSqlParameterSource("name", name);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, boardGroupRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<BoardGroup> findByNameExcludingId(String name, Long excludeId) {
        String sql = "SELECT * FROM board_groups WHERE UPPER(name) = UPPER(:name) AND id != :excludeId";
        var params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("excludeId", excludeId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, boardGroupRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
} 