package org.desviante.repository;

import org.desviante.model.BoardGroup;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de persistência para grupos de quadros.
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * aos grupos de quadros, incluindo CRUD básico e consultas específicas
 * como busca por nome, validação de unicidade, etc.</p>
 * 
 * <p>Os grupos permitem organizar quadros relacionados, facilitando
 * a navegação e manutenção do sistema. Cada grupo possui propriedades
 * visuais como cor e ícone para identificação rápida.</p>
 * 
 * <p>Utiliza JDBC direto com NamedParameterJdbcTemplate para operações
 * de banco, mantendo controle total sobre as consultas SQL.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardGroup
 */
@Repository
public class BoardGroupRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Construtor que inicializa os templates JDBC necessários.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     */
    public BoardGroupRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("board_groups")
                .usingColumns("name", "description", "color", "icon", "creation_date")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * Mapeia os resultados do banco para objetos BoardGroup.
     * 
     * <p>Trata adequadamente campos que podem ser nulos, como
     * creation_date, garantindo a integridade dos dados.</p>
     */
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
        
        return boardGroup;
    };

    /**
     * Busca todos os grupos ordenados por nome.
     * 
     * @return lista de todos os grupos ordenados alfabeticamente
     */
    public List<BoardGroup> findAll() {
        String sql = "SELECT * FROM board_groups ORDER BY name";
        return jdbcTemplate.query(sql, boardGroupRowMapper);
    }

    /**
     * Busca um grupo específico pelo ID.
     * 
     * @param id identificador único do grupo
     * @return Optional contendo o grupo se encontrado, vazio caso contrário
     */
    public Optional<BoardGroup> findById(Long id) {
        String sql = "SELECT * FROM board_groups WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, boardGroupRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Salva ou atualiza um grupo no banco de dados.
     * 
     * <p>Se o grupo não possui ID, executa INSERT e retorna o ID gerado.
     * Se possui ID, executa UPDATE dos campos modificáveis.
     * A data de criação não é atualizada em operações de UPDATE.</p>
     * 
     * @param boardGroup grupo a ser salvo
     * @return grupo com ID atualizado (em caso de inserção)
     */
    public BoardGroup save(BoardGroup boardGroup) {
        var params = new MapSqlParameterSource()
                .addValue("name", boardGroup.getName())
                .addValue("description", boardGroup.getDescription())
                .addValue("color", boardGroup.getColor())
                .addValue("icon", boardGroup.getIcon())
                .addValue("creation_date", boardGroup.getCreationDate());

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

    /**
     * Remove um grupo do banco de dados pelo ID.
     * 
     * @param id identificador do grupo a ser removido
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM board_groups WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }

    /**
     * Busca um grupo pelo nome (case-insensitive).
     * 
     * <p>Utilizada para validação de unicidade de nomes de grupos.
     * A busca é case-insensitive para evitar duplicatas com diferenças
     * apenas de maiúsculas/minúsculas.</p>
     * 
     * @param name nome do grupo a ser buscado
     * @return Optional contendo o grupo se encontrado, vazio caso contrário
     */
    public Optional<BoardGroup> findByName(String name) {
        String sql = "SELECT * FROM board_groups WHERE UPPER(name) = UPPER(:name)";
        var params = new MapSqlParameterSource("name", name);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, boardGroupRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca um grupo pelo nome excluindo um ID específico.
     * 
     * <p>Utilizada para validação de unicidade durante atualizações.
     * Permite verificar se existe outro grupo com o mesmo nome,
     * excluindo o grupo que está sendo atualizado.</p>
     * 
     * @param name nome do grupo a ser buscado
     * @param excludeId ID do grupo a ser excluído da busca
     * @return Optional contendo o grupo se encontrado, vazio caso contrário
     */
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

    /**
     * Verifica se existe um grupo com o nome especificado.
     * 
     * <p>Utilizada para validação de unicidade de nomes de grupos.
     * A verificação é case-insensitive para evitar duplicatas com diferenças
     * apenas de maiúsculas/minúsculas.</p>
     * 
     * @param name nome do grupo a ser verificado
     * @return true se o grupo existe, false caso contrário
     */
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM board_groups WHERE UPPER(name) = UPPER(:name)";
        var params = new MapSqlParameterSource("name", name);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
} 