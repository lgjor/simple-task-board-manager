package org.desviante.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * Configuração para executar migrações manuais do banco de dados.
 * 
 * <p>Esta classe executa migrações que não podem ser feitas via Liquibase,
 * como a adição do campo order_index na tabela cards.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Component
@Slf4j
public class DatabaseMigrationConfig implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseMigrationConfig(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Verificando migrações necessárias...");
        
        // Verifica se a coluna order_index existe
        if (!columnExists("CARDS", "ORDER_INDEX")) {
            log.info("Executando migração: adicionando campo order_index na tabela cards");
            executeMigration();
        } else {
            log.info("Migração order_index já foi executada anteriormente");
        }
        
        // Verifica se a coluna progress_type existe
        if (!columnExists("CARDS", "PROGRESS_TYPE")) {
            log.info("Executando migração: adicionando campo progress_type na tabela cards");
            executeProgressTypeMigration();
        } else {
            log.info("Migração progress_type já foi executada anteriormente");
        }
        
        // Verifica se a tabela checklist_items existe
        if (!tableExists("CHECKLIST_ITEMS")) {
            log.info("Executando migração: criando tabela checklist_items");
            executeChecklistItemsMigration();
        } else {
            log.info("Migração checklist_items já foi executada anteriormente");
        }
    }

    /**
     * Verifica se uma coluna existe em uma tabela.
     * 
     * @param tableName nome da tabela
     * @param columnName nome da coluna
     * @return true se a coluna existe, false caso contrário
     */
    private boolean columnExists(String tableName, String columnName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(null, null, tableName, columnName);
            return rs.next();
        } catch (Exception e) {
            log.error("Erro ao verificar existência da coluna: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica se uma tabela existe.
     * 
     * @param tableName nome da tabela
     * @return true se a tabela existe, false caso contrário
     */
    private boolean tableExists(String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, tableName, null);
            return rs.next();
        } catch (Exception e) {
            log.error("Erro ao verificar existência da tabela: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Executa a migração para adicionar o campo order_index.
     */
    private void executeMigration() {
        try {
            // 1. Adiciona a coluna order_index
            jdbcTemplate.execute("ALTER TABLE cards ADD COLUMN order_index INTEGER DEFAULT 0");
            log.info("✓ Coluna order_index adicionada");

            // 2. Cria índice para otimizar consultas
            jdbcTemplate.execute("CREATE INDEX idx_cards_column_order ON cards(board_column_id, order_index)");
            log.info("✓ Índice criado");

            // 3. Popula o campo order_index baseado na data de criação
            jdbcTemplate.execute("""
                UPDATE cards SET order_index = (
                    SELECT ROW_NUMBER() OVER (
                        PARTITION BY board_column_id 
                        ORDER BY creation_date ASC, id ASC
                    )
                    FROM cards c2 
                    WHERE c2.id = cards.id
                )
                """);
            log.info("✓ Dados populados");

            // 4. Verifica o resultado
            Integer totalCards = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cards", Integer.class);
            log.info("✓ Migração concluída. Total de cards: {}", totalCards);

        } catch (Exception e) {
            log.error("Erro ao executar migração: {}", e.getMessage(), e);
        }
    }

    /**
     * Executa a migração para adicionar o campo progress_type.
     */
    private void executeProgressTypeMigration() {
        try {
            // 1. Adiciona a coluna progress_type
            jdbcTemplate.execute("ALTER TABLE cards ADD COLUMN progress_type VARCHAR(50) DEFAULT 'PERCENTAGE'");
            log.info("✓ Coluna progress_type adicionada");

            // 2. Atualiza cards existentes para ter o valor padrão PERCENTAGE
            jdbcTemplate.execute("UPDATE cards SET progress_type = 'PERCENTAGE' WHERE progress_type IS NULL");
            log.info("✓ Cards existentes atualizados com PERCENTAGE");

            // 3. Verifica o resultado
            Integer totalCards = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cards", Integer.class);
            log.info("✓ Migração progress_type concluída. Total de cards: {}", totalCards);

        } catch (Exception e) {
            log.error("Erro ao executar migração progress_type: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Executa a migração para criar a tabela checklist_items.
     */
    private void executeChecklistItemsMigration() {
        try (Connection conn = dataSource.getConnection()) {
            String product = conn.getMetaData().getDatabaseProductName();
            boolean isH2 = product != null && product.toLowerCase().contains("h2");
            String createTableSql;
            if (isH2) {
                // DDL compatível com H2
                createTableSql = """
                    CREATE TABLE IF NOT EXISTS checklist_items (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        card_id BIGINT NOT NULL,
                        text VARCHAR(1000) NOT NULL,
                        completed BOOLEAN NOT NULL DEFAULT FALSE,
                        order_index INT NOT NULL DEFAULT 0,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        completed_at TIMESTAMP,
                        CONSTRAINT fk_checklist_items_cards FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
                    )
                    """;
            } else {
                // Padrão: SQLite
                createTableSql = """
                    CREATE TABLE IF NOT EXISTS checklist_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        card_id INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        completed BOOLEAN NOT NULL DEFAULT FALSE,
                        order_index INTEGER NOT NULL DEFAULT 0,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        completed_at TIMESTAMP NULL,
                        FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
                    )
                    """;
            }

            // 1. Cria a tabela checklist_items
            jdbcTemplate.execute(createTableSql);
            log.info("✓ Tabela checklist_items criada ({})", product);

            // 2. Cria índices para melhor performance
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_checklist_items_card_id ON checklist_items(card_id)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_checklist_items_order_index ON checklist_items(order_index)");
            log.info("✓ Índices criados");

            // 3. Verifica o resultado
            Integer totalItems = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM checklist_items", Integer.class);
            log.info("✓ Migração checklist_items concluída. Total de itens: {}", totalItems);

        } catch (Exception e) {
            log.error("Erro ao executar migração checklist_items: {}", e.getMessage(), e);
        }
    }
}
