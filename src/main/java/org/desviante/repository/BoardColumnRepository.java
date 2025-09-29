package org.desviante.repository;

import org.desviante.model.BoardColumn;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de persistência para colunas de quadros.
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * às colunas dos quadros, incluindo CRUD básico e consultas específicas
 * como busca por quadro, ordenação por índice, etc.</p>
 * 
 * <p>As colunas são organizadas por order_index para manter a sequência
 * visual no quadro (esquerda para direita). Cada coluna possui um tipo
 * (kind) que define seu comportamento no fluxo de trabalho.</p>
 * 
 * <p>Utiliza JDBC direto com NamedParameterJdbcTemplate para operações
 * de banco, mantendo controle total sobre as consultas SQL.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardColumn
 * @see BoardColumnKindEnum
 */
@Repository
public class BoardColumnRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Construtor que inicializa os templates JDBC necessários.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     */
    public BoardColumnRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("board_columns")
                .usingColumns("name", "order_index", "kind", "board_id")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * Mapeia os resultados do banco para objetos BoardColumn.
     * 
     * <p>Converte o enum kind de String para BoardColumnKindEnum
     * para manter a tipagem correta no sistema.</p>
     */
    private final RowMapper<BoardColumn> columnRowMapper = (ResultSet rs, int rowNum) -> {
        BoardColumn column = new BoardColumn();
        column.setId(rs.getLong("id"));
        column.setName(rs.getString("name"));
        column.setOrderIndex(rs.getInt("order_index"));
        column.setKind(BoardColumnKindEnum.valueOf(rs.getString("kind")));
        column.setBoardId(rs.getLong("board_id"));
        return column;
    };

    /**
     * Busca uma coluna específica pelo ID.
     * 
     * @param id identificador único da coluna
     * @return Optional contendo a coluna se encontrada, vazio caso contrário
     */
    public Optional<BoardColumn> findById(Long id) {
        String sql = "SELECT * FROM board_columns WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, columnRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca todas as colunas de um quadro específico.
     * 
     * <p>As colunas são retornadas ordenadas por order_index (ASC)
     * para manter a sequência visual no quadro.</p>
     * 
     * @param boardId identificador do quadro
     * @return lista de colunas do quadro ordenadas por posição
     */
    public List<BoardColumn> findByBoardId(Long boardId) {
        String sql = "SELECT * FROM board_columns WHERE board_id = :boardId ORDER BY order_index ASC";
        var params = new MapSqlParameterSource("boardId", boardId);
        return jdbcTemplate.query(sql, params, columnRowMapper);
    }

    /**
     * Busca colunas de múltiplos quadros de uma vez.
     * 
     * <p>Método otimizado para carregar colunas de vários quadros
     * em uma única consulta, reduzindo o número de acessos ao banco.</p>
     * 
     * @param boardIds lista de identificadores dos quadros
     * @return lista de colunas ordenadas por quadro e posição
     */
    public List<BoardColumn> findByBoardIdIn(List<Long> boardIds) {
        if (boardIds == null || boardIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM board_columns WHERE board_id IN (:boardIds) ORDER BY board_id, order_index ASC";
        var params = new MapSqlParameterSource("boardIds", boardIds);
        return jdbcTemplate.query(sql, params, columnRowMapper);
    }

    /**
     * Busca uma coluna específica de um quadro pelo tipo.
     * 
     * <p>Útil para encontrar colunas de tipos específicos (INITIAL, PENDING, FINAL)
     * em um quadro, especialmente para sincronização automática de cards.</p>
     * 
     * @param boardId identificador do quadro
     * @param kind tipo da coluna (INITIAL, PENDING, FINAL)
     * @return Optional contendo a coluna se encontrada, vazio caso contrário
     */
    public Optional<BoardColumn> findByBoardIdAndKind(Long boardId, BoardColumnKindEnum kind) {
        String sql = "SELECT * FROM board_columns WHERE board_id = :boardId AND kind = :kind LIMIT 1";
        var params = new MapSqlParameterSource()
                .addValue("boardId", boardId)
                .addValue("kind", kind.name());
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, columnRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Salva ou atualiza uma coluna no banco de dados.
     * 
     * <p>Se a coluna não possui ID, executa INSERT e retorna o ID gerado.
     * Se possui ID, executa UPDATE dos campos modificáveis.
     * O enum kind é convertido para String usando .name().</p>
     * 
     * @param column coluna a ser salva
     * @return coluna com ID atualizado (em caso de inserção)
     */
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

    /**
     * Remove uma coluna do banco de dados pelo ID.
     * 
     * @param id identificador da coluna a ser removida
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM board_columns WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
}