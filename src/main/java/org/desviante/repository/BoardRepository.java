package org.desviante.repository;

import org.desviante.model.Board;
import org.desviante.model.BoardGroup;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de persistência para quadros (boards).
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * aos quadros, incluindo CRUD básico e consultas específicas como
 * busca por grupo, quadros sem grupo, etc.</p>
 * 
 * <p>Utiliza JDBC direto com NamedParameterJdbcTemplate para operações
 * de banco, mantendo controle total sobre as consultas SQL e performance.
 * As consultas incluem JOIN com board_groups para carregar informações
 * completas dos grupos associados.</p>
 * 
 * <p>Implementa transações para operações de escrita (save, delete)
 * garantindo consistência dos dados.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Board
 * @see BoardGroup
 */
@Repository
public class BoardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Construtor que inicializa os templates JDBC necessários.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     */
    public BoardRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("boards")
                // CORREÇÃO: Especificamos explicitamente as colunas para o INSERT.
                .usingColumns("name", "creation_date", "group_id")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * Mapeia os resultados do banco para objetos Board.
     * 
     * <p>Carrega informações completas do quadro incluindo dados do grupo
     * associado através de JOIN com a tabela board_groups.</p>
     */
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

    /**
     * Busca todos os quadros ordenados por nome.
     * 
     * <p>Executa uma consulta que carrega todos os quadros com suas
     * informações de grupo através de LEFT JOIN.</p>
     * 
     * @return lista de todos os quadros ordenados por nome
     */
    public List<Board> findAll() {
        String sql = "SELECT b.*, bg.name as group_name, bg.description as group_description, " +
                    "bg.color as group_color, bg.icon as group_icon, bg.creation_date as group_creation_date " +
                    "FROM boards b " +
                    "LEFT JOIN board_groups bg ON b.group_id = bg.id " +
                    "ORDER BY b.name";
        return jdbcTemplate.query(sql, boardRowMapper);
    }

    /**
     * Busca um quadro específico pelo ID.
     * 
     * @param id identificador único do quadro
     * @return Optional contendo o quadro se encontrado, vazio caso contrário
     */
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

    /**
     * Busca todos os quadros de um grupo específico.
     * 
     * @param groupId identificador do grupo
     * @return lista de quadros do grupo ordenados por nome
     */
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

    /**
     * Busca todos os quadros que não possuem grupo associado.
     * 
     * <p>Útil para exibir quadros "órfãos" que não foram organizados
     * em grupos específicos.</p>
     * 
     * @return lista de quadros sem grupo ordenados por nome
     */
    public List<Board> findBoardsWithoutGroup() {
        String sql = "SELECT b.*, bg.name as group_name, bg.description as group_description, " +
                    "bg.color as group_color, bg.icon as group_icon, bg.creation_date as group_creation_date " +
                    "FROM boards b " +
                    "LEFT JOIN board_groups bg ON b.group_id = bg.id " +
                    "WHERE b.group_id IS NULL " +
                    "ORDER BY b.name";
        return jdbcTemplate.query(sql, boardRowMapper);
    }

    /**
     * Salva ou atualiza um quadro no banco de dados.
     * 
     * <p>Se o quadro não possui ID, executa INSERT e retorna o ID gerado.
     * Se possui ID, executa UPDATE dos campos modificáveis.</p>
     * 
     * @param board quadro a ser salvo
     * @return quadro com ID atualizado (em caso de inserção)
     */
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

    /**
     * Remove um quadro do banco de dados pelo ID.
     * 
     * @param id identificador do quadro a ser removido
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM boards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
}