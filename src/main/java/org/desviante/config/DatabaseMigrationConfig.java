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
            log.info("Migração já foi executada anteriormente");
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
}
