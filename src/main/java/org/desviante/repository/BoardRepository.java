package org.desviante.repository;

import org.desviante.model.Board;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
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
                .usingColumns("name", "creation_date")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Board> boardRowMapper = (ResultSet rs, int rowNum) -> {
        Board board = new Board();
        board.setId(rs.getLong("id"));
        board.setName(rs.getString("name"));
        board.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
        return board;
    };

    public List<Board> findAll() {
        String sql = "SELECT * FROM boards ORDER BY name";
        return jdbcTemplate.query(sql, boardRowMapper);
    }

    public Optional<Board> findById(Long id) {
        String sql = "SELECT * FROM boards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, boardRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Board save(Board board) {
        // Usamos MapSqlParameterSource para mapear explicitamente as propriedades para as colunas.
        var params = new MapSqlParameterSource()
                .addValue("name", board.getName())
                .addValue("creation_date", board.getCreationDate());

        if (board.getId() == null) {
            Number newId = jdbcInsert.executeAndReturnKey(params);
            board.setId(newId.longValue());
        } else {
            params.addValue("id", board.getId());
            String sql = "UPDATE boards SET name = :name WHERE id = :id";
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