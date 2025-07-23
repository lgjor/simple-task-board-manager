package org.desviante.repository;

import org.desviante.model.BoardColumn;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class BoardColumnRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public BoardColumnRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("board_columns")
                .usingColumns("name", "order_index", "kind", "board_id")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<BoardColumn> columnRowMapper = (ResultSet rs, int rowNum) -> {
        BoardColumn column = new BoardColumn();
        column.setId(rs.getLong("id"));
        column.setName(rs.getString("name"));
        column.setOrderIndex(rs.getInt("order_index"));
        column.setKind(BoardColumnKindEnum.valueOf(rs.getString("kind")));
        column.setBoardId(rs.getLong("board_id"));
        return column;
    };

    public Optional<BoardColumn> findById(Long id) {
        String sql = "SELECT * FROM board_columns WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, columnRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<BoardColumn> findByBoardId(Long boardId) {
        String sql = "SELECT * FROM board_columns WHERE board_id = :boardId ORDER BY order_index ASC";
        var params = new MapSqlParameterSource("boardId", boardId);
        return jdbcTemplate.query(sql, params, columnRowMapper);
    }

    /**
     * CORREÇÃO: Método adicionado para buscar colunas de múltiplos boards de uma vez.
     * Isso resolve o erro de compilação no BoardColumnService.
     */
    public List<BoardColumn> findByBoardIdIn(List<Long> boardIds) {
        if (boardIds == null || boardIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM board_columns WHERE board_id IN (:boardIds) ORDER BY board_id, order_index ASC";
        var params = new MapSqlParameterSource("boardIds", boardIds);
        return jdbcTemplate.query(sql, params, columnRowMapper);
    }

    @Transactional
    public BoardColumn save(BoardColumn column) {
        var params = new MapSqlParameterSource()
                .addValue("name", column.getName())
                .addValue("order_index", column.getOrderIndex())
                .addValue("kind", column.getKind().name()) // Para enums, usamos .name()
                .addValue("board_id", column.getBoardId());

        if (column.getId() == null) {
            Number newId = jdbcInsert.executeAndReturnKey(params);
            column.setId(newId.longValue());
        } else {
            params.addValue("id", column.getId());
            String sql = "UPDATE board_columns SET name = :name, order_index = :order_index, kind = :kind WHERE id = :id";
            jdbcTemplate.update(sql, params);
        }
        return column;
    }

    @Transactional
    public void deleteById(Long id) {
        String sql = "DELETE FROM board_columns WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
}