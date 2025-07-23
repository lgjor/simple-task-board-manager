package org.desviante.repository;

import org.desviante.model.Card;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class CardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public CardRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("cards")
                .usingColumns("title", "description", "creation_date", "last_update_date", "completion_date", "board_column_id")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Card> cardRowMapper = (ResultSet rs, int rowNum) -> {
        Card card = new Card();
        card.setId(rs.getLong("id"));
        card.setTitle(rs.getString("title"));
        card.setDescription(rs.getString("description"));
        card.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
        card.setLastUpdateDate(rs.getTimestamp("last_update_date").toLocalDateTime());
        // Trata a data de conclusão, que pode ser nula
        Timestamp completionTimestamp = rs.getTimestamp("completion_date");
        if (completionTimestamp != null) {
            card.setCompletionDate(completionTimestamp.toLocalDateTime());
        }
        card.setBoardColumnId(rs.getLong("board_column_id"));
        return card;
    };

    public Optional<Card> findById(Long id) {
        String sql = "SELECT * FROM cards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, cardRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Card> findByBoardColumnIdIn(List<Long> columnIds) {
        if (columnIds == null || columnIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM cards WHERE board_column_id IN (:columnIds) ORDER BY creation_date ASC";
        var params = new MapSqlParameterSource("columnIds", columnIds);
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    @Transactional
    public Card save(Card card) {
        var params = new MapSqlParameterSource()
                .addValue("title", card.getTitle())
                .addValue("description", card.getDescription())
                .addValue("creation_date", card.getCreationDate())
                .addValue("last_update_date", card.getLastUpdateDate())
                .addValue("completion_date", card.getCompletionDate())
                .addValue("board_column_id", card.getBoardColumnId());

        if (card.getId() == null) {
            Number newId = jdbcInsert.executeAndReturnKey(params);
            card.setId(newId.longValue());
        } else {
            params.addValue("id", card.getId());
            String sql = """
                    UPDATE cards SET
                        title = :title,
                        description = :description,
                        last_update_date = :last_update_date,
                        completion_date = :completion_date,
                        board_column_id = :board_column_id
                    WHERE id = :id
                    """;
            jdbcTemplate.update(sql, params);
        }
        return card;
    }

    @Transactional
    public void deleteById(Long id) {
        String sql = "DELETE FROM cards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
}