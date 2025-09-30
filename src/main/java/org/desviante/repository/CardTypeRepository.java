package org.desviante.repository;

import org.desviante.model.CardType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de persistência para tipos de card.
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * aos tipos de card, incluindo CRUD básico e consultas específicas.</p>
 * 
 * <p>Utiliza JDBC direto com NamedParameterJdbcTemplate para operações
 * de banco, mantendo controle total sobre as consultas SQL.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CardType
 */
@Repository
public class CardTypeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Construtor que inicializa os templates JDBC necessários.
     *
     * @param dataSource fonte de dados para conexão com o banco
     */
    public CardTypeRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("card_types")
                .usingColumns("name", "unit_label", "creation_date", "last_update_date")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * Mapeador de linha para converter resultados do banco em objetos CardType.
     */
    private final RowMapper<CardType> rowMapper = (ResultSet rs, int rowNum) -> {
        CardType cardType = new CardType();
        cardType.setId(rs.getLong("id"));
        cardType.setName(rs.getString("name"));
        cardType.setUnitLabel(rs.getString("unit_label"));
        
        Timestamp creationDate = rs.getTimestamp("creation_date");
        if (creationDate != null) {
            cardType.setCreationDate(creationDate.toLocalDateTime());
        }
        
        Timestamp lastUpdateDate = rs.getTimestamp("last_update_date");
        if (lastUpdateDate != null) {
            cardType.setLastUpdateDate(lastUpdateDate.toLocalDateTime());
        }
        
        return cardType;
    };

    /**
     * Busca um tipo de card pelo ID.
     *
     * @param id identificador do tipo de card
     * @return Optional contendo o tipo de card se encontrado
     */
    public Optional<CardType> findById(Long id) {
        String sql = "SELECT id, name, unit_label, creation_date, last_update_date " +
                    "FROM card_types WHERE id = :id";
        
        try {
            CardType cardType = jdbcTemplate.queryForObject(sql, 
                new MapSqlParameterSource("id", id), rowMapper);
            return Optional.ofNullable(cardType);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca um tipo de card pelo nome.
     *
     * @param name nome do tipo de card
     * @return Optional contendo o tipo de card se encontrado
     */
    public Optional<CardType> findByName(String name) {
        String sql = "SELECT id, name, unit_label, creation_date, last_update_date " +
                    "FROM card_types WHERE name = :name";
        
        try {
            CardType cardType = jdbcTemplate.queryForObject(sql, 
                new MapSqlParameterSource("name", name), rowMapper);
            return Optional.ofNullable(cardType);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Lista todos os tipos de card ordenados por nome.
     *
     * @return lista de todos os tipos de card
     */
    public List<CardType> findAll() {
        String sql = "SELECT id, name, unit_label, creation_date, last_update_date " +
                    "FROM card_types ORDER BY name";
        
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Salva um novo tipo de card.
     *
     * @param cardType tipo de card a ser salvo
     * @return tipo de card salvo com ID gerado
     * @throws RuntimeException se já existe um tipo com o mesmo nome
     */
    public CardType save(CardType cardType) {
        // Verificar se já existe um tipo com o mesmo nome
        if (existsByName(cardType.getName())) {
            throw new RuntimeException("Já existe um tipo de card com o nome: " + cardType.getName());
        }
        
        LocalDateTime now = LocalDateTime.now();
        cardType.setCreationDate(now);
        cardType.setLastUpdateDate(now);
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", cardType.getName());
        params.addValue("unit_label", cardType.getUnitLabel());
        params.addValue("creation_date", Timestamp.valueOf(cardType.getCreationDate()));
        params.addValue("last_update_date", Timestamp.valueOf(cardType.getLastUpdateDate()));
        
        Number generatedId = jdbcInsert.executeAndReturnKey(params);
        cardType.setId(generatedId.longValue());
        
        return cardType;
    }

    /**
     * Atualiza um tipo de card existente.
     *
     * @param cardType tipo de card a ser atualizado
     * @return tipo de card atualizado
     */
    public CardType update(CardType cardType) {
        cardType.setLastUpdateDate(LocalDateTime.now());
        
        String sql = "UPDATE card_types SET name = :name, unit_label = :unit_label, " +
                    "last_update_date = :last_update_date WHERE id = :id";
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", cardType.getId());
        params.addValue("name", cardType.getName());
        params.addValue("unit_label", cardType.getUnitLabel());
        params.addValue("last_update_date", Timestamp.valueOf(cardType.getLastUpdateDate()));
        
        int updatedRows = jdbcTemplate.update(sql, params);
        if (updatedRows == 0) {
            throw new RuntimeException("Tipo de card com ID " + cardType.getId() + " não encontrado");
        }
        
        return cardType;
    }

    /**
     * Remove um tipo de card pelo ID.
     *
     * @param id identificador do tipo de card a ser removido
     * @return true se o tipo foi removido, false se não existia
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM card_types WHERE id = :id";
        
        int deletedRows = jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
        return deletedRows > 0;
    }

    /**
     * Verifica se existe um tipo de card com o nome especificado.
     *
     * @param name nome a ser verificado
     * @return true se existe um tipo com este nome, false caso contrário
     */
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM card_types WHERE name = :name";
        
        Integer count = jdbcTemplate.queryForObject(sql, 
            new MapSqlParameterSource("name", name), Integer.class);
        
        return count != null && count > 0;
    }

    /**
     * Salva um novo tipo de card ou retorna o existente se já houver um com o mesmo nome.
     * 
     * <p>Este método evita conflitos de chave primária verificando se já existe
     * um tipo com o mesmo nome antes de tentar inserir. Se existir, retorna
     * o tipo existente em vez de tentar criar um novo.</p>
     *
     * @param cardType tipo de card a ser salvo
     * @return tipo de card salvo (novo ou existente)
     */
    public CardType saveOrGetExisting(CardType cardType) {
        // Primeiro, verificar se já existe um tipo com o mesmo nome
        Optional<CardType> existingType = findByName(cardType.getName());
        if (existingType.isPresent()) {
            return existingType.get();
        }
        
        // Se não existe, criar um novo
        return save(cardType);
    }
} 