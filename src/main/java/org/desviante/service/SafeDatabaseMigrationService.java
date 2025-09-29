package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para migrações seguras do banco de dados.
 * 
 * <p>Este serviço garante que migrações sejam executadas de forma segura,
 * preservando dados existentes e verificando a estrutura atual antes
 * de fazer alterações.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SafeDatabaseMigrationService {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseBackupService backupService;

    /**
     * Verifica se uma coluna existe em uma tabela.
     * 
     * @param tableName nome da tabela
     * @param columnName nome da coluna
     * @return true se a coluna existe, false caso contrário
     */
    public boolean columnExists(String tableName, String columnName) {
        try {
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase());
            boolean exists = columns.next();
            columns.close();
            return exists;
        } catch (Exception e) {
            log.warn("Erro ao verificar se coluna {} existe na tabela {}: {}", columnName, tableName, e.getMessage());
            return false;
        }
    }

    /**
     * Adiciona uma coluna de forma segura se ela não existir.
     * 
     * @param tableName nome da tabela
     * @param columnName nome da coluna
     * @param columnDefinition definição da coluna (ex: "INT NOT NULL DEFAULT 0")
     * @return true se a coluna foi adicionada, false se já existia
     */
    @Transactional
    public boolean addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        if (columnExists(tableName, columnName)) {
            log.info("Coluna {} já existe na tabela {}", columnName, tableName);
            return false;
        }

        try {
            String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDefinition);
            jdbcTemplate.execute(sql);
            log.info("Coluna {} adicionada à tabela {} com sucesso", columnName, tableName);
            return true;
        } catch (Exception e) {
            log.error("Erro ao adicionar coluna {} à tabela {}: {}", columnName, tableName, e.getMessage());
            throw new RuntimeException("Falha ao adicionar coluna " + columnName, e);
        }
    }

    /**
     * Cria um índice de forma segura se ele não existir.
     * 
     * @param indexName nome do índice
     * @param tableName nome da tabela
     * @param columns colunas do índice (separadas por vírgula)
     * @return true se o índice foi criado, false se já existia
     */
    @Transactional
    public boolean createIndexIfNotExists(String indexName, String tableName, String columns) {
        try {
            // Verificar se o índice já existe
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES WHERE INDEX_NAME = ? AND TABLE_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, indexName.toUpperCase(), tableName.toUpperCase());
            
            if (count != null && count > 0) {
                log.info("Índice {} já existe na tabela {}", indexName, tableName);
                return false;
            }

            String sql = String.format("CREATE INDEX %s ON %s (%s)", indexName, tableName, columns);
            jdbcTemplate.execute(sql);
            log.info("Índice {} criado na tabela {} com sucesso", indexName, tableName);
            return true;
        } catch (Exception e) {
            log.error("Erro ao criar índice {} na tabela {}: {}", indexName, tableName, e.getMessage());
            throw new RuntimeException("Falha ao criar índice " + indexName, e);
        }
    }

    /**
     * Popula o campo order_index baseado na data de criação dos cards existentes.
     * 
     * @return número de cards atualizados
     */
    @Transactional
    public int populateOrderIndexForExistingCards() {
        try {
            // Verificar se há cards sem order_index definido
            String checkSql = "SELECT COUNT(*) FROM cards WHERE order_index IS NULL OR order_index = 0";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                log.info("Todos os cards já possuem order_index definido");
                return 0;
            }

            // Atualizar order_index baseado na data de criação
            String updateSql = """
                UPDATE cards SET order_index = (
                    SELECT ROW_NUMBER() OVER (
                        PARTITION BY board_column_id 
                        ORDER BY creation_date ASC, id ASC
                    )
                    FROM cards c2 
                    WHERE c2.id = cards.id
                )
                WHERE order_index IS NULL OR order_index = 0
                """;
            
            int updated = jdbcTemplate.update(updateSql);
            log.info("Order_index populado para {} cards existentes", updated);
            return updated;
        } catch (Exception e) {
            log.error("Erro ao popular order_index dos cards existentes: {}", e.getMessage());
            throw new RuntimeException("Falha ao popular order_index", e);
        }
    }

    /**
     * Executa migração segura para adicionar suporte à ordenação de cards.
     * 
     * @return true se a migração foi executada, false se já estava aplicada
     */
    @Transactional
    public boolean migrateCardOrderingSupport() {
        log.info("Iniciando migração segura para suporte à ordenação de cards...");
        
        // Criar backup antes da migração
        String backupPath = backupService.createPreMigrationBackup();
        log.info("Backup criado antes da migração: {}", backupPath);
        
        boolean changesMade = false;
        
        // 1. Adicionar coluna order_index se não existir
        if (addColumnIfNotExists("cards", "order_index", "INT NOT NULL DEFAULT 0")) {
            changesMade = true;
        }
        
        // 2. Criar índice para otimizar consultas se não existir
        if (createIndexIfNotExists("idx_cards_column_order", "cards", "board_column_id, order_index")) {
            changesMade = true;
        }
        
        // 3. Popular order_index para cards existentes
        int updatedCards = populateOrderIndexForExistingCards();
        if (updatedCards > 0) {
            changesMade = true;
        }
        
        if (changesMade) {
            log.info("Migração de ordenação de cards concluída com sucesso");
        } else {
            log.info("Migração de ordenação de cards já estava aplicada");
        }
        
        return changesMade;
    }

    /**
     * Verifica se a migração de ordenação de cards está aplicada.
     * 
     * @return true se a migração está aplicada, false caso contrário
     */
    public boolean isCardOrderingMigrationApplied() {
        return columnExists("cards", "order_index");
    }

    /**
     * Obtém estatísticas do banco de dados.
     * 
     * @return lista de estatísticas
     */
    public List<String> getDatabaseStats() {
        List<String> stats = new ArrayList<>();
        
        try {
            // Contar boards
            Integer boardCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM boards", Integer.class);
            stats.add("Boards: " + (boardCount != null ? boardCount : 0));
            
            // Contar cards
            Integer cardCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cards", Integer.class);
            stats.add("Cards: " + (cardCount != null ? cardCount : 0));
            
            // Contar cards com order_index válido
            Integer validOrderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cards WHERE order_index > 0", Integer.class);
            stats.add("Cards com order_index válido: " + (validOrderCount != null ? validOrderCount : 0));
            
            // Verificar se migração está aplicada
            stats.add("Migração de ordenação aplicada: " + isCardOrderingMigrationApplied());
            
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas do banco: {}", e.getMessage());
            stats.add("Erro ao obter estatísticas: " + e.getMessage());
        }
        
        return stats;
    }
}
