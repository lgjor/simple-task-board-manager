package org.desviante.integration.sync;

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
 * Repository para operações de persistência de status de sincronização.
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * ao rastreamento de sincronização entre cards locais e sistemas externos,
 * incluindo CRUD básico e consultas específicas para retry e auditoria.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pelas operações de persistência</li>
 *   <li><strong>OCP:</strong> Extensível através de novos métodos de consulta</li>
 *   <li><strong>LSP:</strong> Implementa padrões consistentes de repository</li>
 *   <li><strong>ISP:</strong> Interface específica para sincronização</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (DataSource, JDBC)</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see IntegrationSyncStatus
 * @see IntegrationType
 * @see SyncStatus
 */
@Repository
public class IntegrationSyncRepository {
    
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    
    /**
     * Construtor que inicializa os templates JDBC necessários.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     */
    public IntegrationSyncRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("integration_sync_status")
                .usingColumns("card_id", "integration_type", "external_id", "sync_status", 
                             "last_sync_date", "error_message", "retry_count", "max_retries")
                .usingGeneratedKeyColumns("id");
    }
    
    /**
     * Mapeia os resultados do banco para objetos IntegrationSyncStatus.
     */
    private final RowMapper<IntegrationSyncStatus> syncStatusRowMapper = (ResultSet rs, int rowNum) -> {
        IntegrationSyncStatus status = new IntegrationSyncStatus();
        status.setId(rs.getLong("id"));
        status.setCardId(rs.getLong("card_id"));
        
        String integrationTypeStr = rs.getString("integration_type");
        status.setIntegrationType(IntegrationType.valueOf(integrationTypeStr));
        
        status.setExternalId(rs.getString("external_id"));
        
        String syncStatusStr = rs.getString("sync_status");
        status.setSyncStatus(SyncStatus.valueOf(syncStatusStr));
        
        // Tratar campos que podem ser nulos
        Timestamp lastSyncTimestamp = rs.getTimestamp("last_sync_date");
        if (lastSyncTimestamp != null) {
            status.setLastSyncDate(lastSyncTimestamp.toLocalDateTime());
        }
        
        status.setErrorMessage(rs.getString("error_message"));
        status.setRetryCount(rs.getInt("retry_count"));
        status.setMaxRetries(rs.getInt("max_retries"));
        
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        if (createdAtTimestamp != null) {
            status.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        }
        
        Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");
        if (updatedAtTimestamp != null) {
            status.setUpdatedAt(updatedAtTimestamp.toLocalDateTime());
        }
        
        return status;
    };
    
    /**
     * Salva ou atualiza um status de sincronização.
     * 
     * @param status status a ser salvo ou atualizado
     * @return status com ID definido (se for novo)
     */
    public IntegrationSyncStatus save(IntegrationSyncStatus status) {
        var params = new MapSqlParameterSource()
                .addValue("card_id", status.getCardId())
                .addValue("integration_type", status.getIntegrationType().name())
                .addValue("external_id", status.getExternalId())
                .addValue("sync_status", status.getSyncStatus().name())
                .addValue("last_sync_date", status.getLastSyncDate())
                .addValue("error_message", status.getErrorMessage())
                .addValue("retry_count", status.getRetryCount())
                .addValue("max_retries", status.getMaxRetries());
        
        if (status.getId() == null) {
            Number newId = jdbcInsert.executeAndReturnKey(params);
            status.setId(newId.longValue());
        } else {
            params.addValue("id", status.getId());
            String sql = """
                    UPDATE integration_sync_status SET
                        card_id = :card_id,
                        integration_type = :integration_type,
                        external_id = :external_id,
                        sync_status = :sync_status,
                        last_sync_date = :last_sync_date,
                        error_message = :error_message,
                        retry_count = :retry_count,
                        max_retries = :max_retries,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = :id
                    """;
            jdbcTemplate.update(sql, params);
        }
        return status;
    }
    
    /**
     * Busca um status de sincronização por ID.
     * 
     * @param id ID do status
     * @return Optional contendo o status se encontrado
     */
    public Optional<IntegrationSyncStatus> findById(Long id) {
        String sql = "SELECT * FROM integration_sync_status WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, syncStatusRowMapper));
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Busca status de sincronização por card e tipo de integração.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @return Optional contendo o status se encontrado
     */
    public Optional<IntegrationSyncStatus> findByCardIdAndType(Long cardId, IntegrationType integrationType) {
        String sql = "SELECT * FROM integration_sync_status WHERE card_id = :card_id AND integration_type = :integration_type";
        var params = new MapSqlParameterSource()
                .addValue("card_id", cardId)
                .addValue("integration_type", integrationType.name());
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, syncStatusRowMapper));
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Busca todos os status de sincronização para um card.
     * 
     * @param cardId ID do card
     * @return lista de status de sincronização
     */
    public List<IntegrationSyncStatus> findByCardId(Long cardId) {
        String sql = "SELECT * FROM integration_sync_status WHERE card_id = :card_id ORDER BY integration_type";
        var params = new MapSqlParameterSource("card_id", cardId);
        return jdbcTemplate.query(sql, params, syncStatusRowMapper);
    }
    
    /**
     * Busca todos os status de sincronização por tipo.
     * 
     * @param integrationType tipo de integração
     * @return lista de status de sincronização
     */
    public List<IntegrationSyncStatus> findByIntegrationType(IntegrationType integrationType) {
        String sql = "SELECT * FROM integration_sync_status WHERE integration_type = :integration_type ORDER BY created_at DESC";
        var params = new MapSqlParameterSource("integration_type", integrationType.name());
        return jdbcTemplate.query(sql, params, syncStatusRowMapper);
    }
    
    /**
     * Busca todos os status de sincronização por status.
     * 
     * @param syncStatus status de sincronização
     * @return lista de status de sincronização
     */
    public List<IntegrationSyncStatus> findBySyncStatus(SyncStatus syncStatus) {
        String sql = "SELECT * FROM integration_sync_status WHERE sync_status = :sync_status ORDER BY updated_at DESC";
        var params = new MapSqlParameterSource("sync_status", syncStatus.name());
        return jdbcTemplate.query(sql, params, syncStatusRowMapper);
    }
    
    /**
     * Busca status que precisam de retry.
     * 
     * @return lista de status que podem ser tentados novamente
     */
    public List<IntegrationSyncStatus> findRetryableStatuses() {
        String sql = """
                SELECT * FROM integration_sync_status 
                WHERE (sync_status = 'PENDING' OR sync_status = 'RETRY') 
                AND retry_count < max_retries 
                ORDER BY updated_at ASC
                """;
        return jdbcTemplate.query(sql, syncStatusRowMapper);
    }
    
    /**
     * Busca status com erro que podem ser tentados novamente.
     * 
     * @return lista de status com erro que ainda podem ser retry
     */
    public List<IntegrationSyncStatus> findErrorStatusesForRetry() {
        String sql = """
                SELECT * FROM integration_sync_status 
                WHERE sync_status = 'ERROR' 
                AND retry_count < max_retries 
                ORDER BY updated_at ASC
                """;
        return jdbcTemplate.query(sql, syncStatusRowMapper);
    }
    
    /**
     * Remove um status de sincronização por ID.
     * 
     * @param id ID do status a ser removido
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM integration_sync_status WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }
    
    /**
     * Remove todos os status de sincronização para um card.
     * 
     * @param cardId ID do card
     */
    public void deleteByCardId(Long cardId) {
        String sql = "DELETE FROM integration_sync_status WHERE card_id = :card_id";
        var params = new MapSqlParameterSource("card_id", cardId);
        jdbcTemplate.update(sql, params);
    }
    
    /**
     * Conta o número de status por tipo de integração.
     * 
     * @param integrationType tipo de integração
     * @return número de registros
     */
    public long countByIntegrationType(IntegrationType integrationType) {
        String sql = "SELECT COUNT(*) FROM integration_sync_status WHERE integration_type = :integration_type";
        var params = new MapSqlParameterSource("integration_type", integrationType.name());
        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }
    
    /**
     * Conta o número de status por status de sincronização.
     * 
     * @param syncStatus status de sincronização
     * @return número de registros
     */
    public long countBySyncStatus(SyncStatus syncStatus) {
        String sql = "SELECT COUNT(*) FROM integration_sync_status WHERE sync_status = :sync_status";
        var params = new MapSqlParameterSource("sync_status", syncStatus.name());
        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }
    
    /**
     * Busca status criados em um período específico.
     * 
     * @param startDate data de início
     * @param endDate data de fim
     * @return lista de status criados no período
     */
    public List<IntegrationSyncStatus> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM integration_sync_status WHERE created_at BETWEEN :start_date AND :end_date ORDER BY created_at DESC";
        var params = new MapSqlParameterSource()
                .addValue("start_date", startDate)
                .addValue("end_date", endDate);
        return jdbcTemplate.query(sql, params, syncStatusRowMapper);
    }
}
