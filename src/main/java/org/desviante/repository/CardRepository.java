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

/**
 * Gerencia as operações de persistência para cards (tarefas).
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * aos cards, incluindo CRUD básico e consultas específicas como
 * busca por coluna, ordenação por data de criação, etc.</p>
 * 
 * <p>Os cards são as unidades fundamentais de trabalho no sistema,
 * representando tarefas que podem ser movidas entre colunas de um quadro.
 * Cada card possui datas de criação, atualização e conclusão para
 * rastreamento completo do ciclo de vida.</p>
 * 
 * <p>Utiliza JDBC direto com NamedParameterJdbcTemplate para operações
 * de banco, mantendo controle total sobre as consultas SQL.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Card
 */
@Repository
public class CardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Construtor que inicializa os templates JDBC necessários.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     */
    public CardRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("cards")
                .usingColumns("title", "description", "type", "total_units", "current_units", "creation_date", "last_update_date", "completion_date", "board_column_id")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * Mapeia os resultados do banco para objetos Card.
     * 
     * <p>Trata adequadamente campos que podem ser nulos, como
     * completion_date, garantindo a integridade dos dados.
     * Converte timestamps para LocalDateTime para uso no sistema.</p>
     */
    private final RowMapper<Card> cardRowMapper = (ResultSet rs, int rowNum) -> {
        Card card = new Card();
        card.setId(rs.getLong("id"));
        card.setTitle(rs.getString("title"));
        card.setDescription(rs.getString("description"));
        
        // Mapear o tipo do card
        String typeStr = rs.getString("type");
        if (typeStr != null) {
            card.setType(org.desviante.model.enums.CardType.valueOf(typeStr));
        }
        
        // Mapear campos de progresso
        card.setTotalUnits(rs.getObject("total_units", Integer.class));
        card.setCurrentUnits(rs.getObject("current_units", Integer.class));
        
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

    /**
     * Busca um card específico pelo ID.
     * 
     * @param id identificador único do card
     * @return Optional contendo o card se encontrado, vazio caso contrário
     */
    public Optional<Card> findById(Long id) {
        String sql = "SELECT * FROM cards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, cardRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca cards de múltiplas colunas de uma vez.
     * 
     * <p>Método otimizado para carregar cards de várias colunas
     * em uma única consulta, reduzindo o número de acessos ao banco.
     * Os cards são retornados ordenados por data de criação (ASC)
     * para manter a sequência cronológica.</p>
     * 
     * @param columnIds lista de identificadores das colunas
     * @return lista de cards ordenados por data de criação
     */
    public List<Card> findByBoardColumnIdIn(List<Long> columnIds) {
        if (columnIds == null || columnIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM cards WHERE board_column_id IN (:columnIds) ORDER BY creation_date ASC";
        var params = new MapSqlParameterSource("columnIds", columnIds);
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Salva ou atualiza um card no banco de dados.
     * 
     * <p>Se o card não possui ID, executa INSERT e retorna o ID gerado.
     * Se possui ID, executa UPDATE dos campos modificáveis.
     * A data de criação não é atualizada em operações de UPDATE,
     * apenas last_update_date é atualizada automaticamente.</p>
     * 
     * @param card card a ser salvo
     * @return card com ID atualizado (em caso de inserção)
     */
    @Transactional
    public Card save(Card card) {
        var params = new MapSqlParameterSource()
                .addValue("title", card.getTitle())
                .addValue("description", card.getDescription())
                .addValue("type", card.getType() != null ? card.getType().name() : "CARD")
                .addValue("total_units", card.getTotalUnits())
                .addValue("current_units", card.getCurrentUnits())
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
                        type = :type,
                        total_units = :total_units,
                        current_units = :current_units,
                        last_update_date = :last_update_date,
                        completion_date = :completion_date,
                        board_column_id = :board_column_id
                    WHERE id = :id
                    """;
            jdbcTemplate.update(sql, params);
        }
        return card;
    }

    /**
     * Remove um card do banco de dados pelo ID.
     * 
     * @param id identificador do card a ser removido
     */
    @Transactional
    public void deleteById(Long id) {
        String sql = "DELETE FROM cards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
}