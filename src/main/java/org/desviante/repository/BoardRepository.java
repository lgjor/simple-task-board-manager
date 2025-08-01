package org.desviante.repository;

import org.desviante.model.Board;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BoardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public BoardRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("boards")
                // CORREÇÃO: Especificamos explicitamente as colunas para o INSERT.
                .usingColumns("name", "creation_date", "group_id")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Board> boardRowMapper = (ResultSet rs, int rowNum) -> {
        Board board = new Board();
        board.setId(rs.getLong("id"));
        board.setName(rs.getString("name"));
        board.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
        
        // Mapeamento do group_id
        Long groupId = rs.getLong("group_id");
        if (!rs.wasNull()) {
            board.setGroupId(groupId);
            
            // Carregar o objeto BoardGroup completo
            BoardGroup group = new BoardGroup();
            group.setId(groupId);
            group.setName(rs.getString("group_name"));
            group.setDescription(rs.getString("group_description"));
            group.setColor(rs.getString("group_color"));
            group.setIcon(rs.getString("group_icon"));
            group.setCreationDate(rs.getTimestamp("group_creation_date") != null ? 
                rs.getTimestamp("group_creation_date").toLocalDateTime() : null);
            // Removido isDefault - não precisamos mais de grupo padrão
            
            board.setGroup(group);
        }
        
        return board;
    };

    public List<Board> findAll() {
        String sql = "SELECT b.*, bg.name as group_name, bg.description as group_description, " +
                    "bg.color as group_color, bg.icon as group_icon, bg.creation_date as group_creation_date " +
                    "FROM boards b " +
                    "LEFT JOIN board_groups bg ON b.group_id = bg.id " +
                    "ORDER BY b.name";
        return jdbcTemplate.query(sql, boardRowMapper);
    }

    public Optional<Board> findById(Long id) {
        String sql = "SELECT b.*, bg.name as group_name, bg.description as group_description, " +
                    "bg.color as group_color, bg.icon as group_icon, bg.creation_date as group_creation_date " +
                    "FROM boards b " +
                    "LEFT JOIN board_groups bg ON b.group_id = bg.id " +
                    "WHERE b.id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, boardRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Board> findByGroupId(Long groupId) {
        String sql = "SELECT b.*, bg.name as group_name, bg.description as group_description, " +
                    "bg.color as group_color, bg.icon as group_icon, bg.creation_date as group_creation_date " +
                    "FROM boards b " +
                    "LEFT JOIN board_groups bg ON b.group_id = bg.id " +
                    "WHERE b.group_id = :groupId " +
                    "ORDER BY b.name";
        var params = new MapSqlParameterSource("groupId", groupId);
        return jdbcTemplate.query(sql, params, boardRowMapper);
    }

    public List<Board> findBoardsWithoutGroup() {
        String sql = "SELECT b.*, bg.name as group_name, bg.description as group_description, " +
                    "bg.color as group_color, bg.icon as group_icon, bg.creation_date as group_creation_date " +
                    "FROM boards b " +
                    "LEFT JOIN board_groups bg ON b.group_id = bg.id " +
                    "WHERE b.group_id IS NULL " +
                    "ORDER BY b.name";
        return jdbcTemplate.query(sql, boardRowMapper);
    }

    @Transactional
    public Board save(Board board) {
        // Usamos MapSqlParameterSource para mapear explicitamente as propriedades para as colunas.
        var params = new MapSqlParameterSource()
                .addValue("name", board.getName())
                .addValue("creation_date", board.getCreationDate())
                .addValue("group_id", board.getGroupId());

        if (board.getId() == null) {
            Number newId = jdbcInsert.executeAndReturnKey(params);
            board.setId(newId.longValue());
        } else {
            params.addValue("id", board.getId());
            String sql = "UPDATE boards SET name = :name, group_id = :group_id WHERE id = :id";
            jdbcTemplate.update(sql, params);
        }
        return board;
    }

    @Transactional
    public void deleteById(Long id) {
        String sql = "DELETE FROM boards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
}