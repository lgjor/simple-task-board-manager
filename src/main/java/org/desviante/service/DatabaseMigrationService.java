package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por executar migrações do banco de dados de forma segura.
 * 
 * <p>Este serviço garante que as migrações sejam executadas sem perder dados existentes,
 * verificando se as tabelas necessárias existem antes de tentar utilizá-las.</p>
 * 
 * @author Aú Desviante - Lucas Godoy
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationService {

    private final DataSource dataSource;
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * Verifica se uma tabela existe no banco de dados.
     * 
     * @param tableName nome da tabela a ser verificada
     * @return true se a tabela existe, false caso contrário
     */
    public boolean tableExists(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"});
            boolean exists = tables.next();
            tables.close();
            return exists;
        } catch (Exception e) {
            log.warn("Erro ao verificar existência da tabela {}: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * Executa a criação da tabela integration_sync_status se ela não existir.
     * Se existir com estrutura incorreta, recria a tabela.
     * 
     * <p>Esta migração é executada de forma segura, garantindo que a tabela
     * tenha a estrutura correta com a coluna last_sync_date.</p>
     */
    public void ensureIntegrationSyncStatusTable() {
        if (tableExists("INTEGRATION_SYNC_STATUS")) {
            if (hasCorrectColumnStructure()) {
                log.info("✅ Tabela INTEGRATION_SYNC_STATUS já existe com estrutura correta");
                return;
            } else {
                log.warn("⚠️ Tabela INTEGRATION_SYNC_STATUS existe mas com estrutura incorreta. Recriando...");
                dropAndRecreateTable();
                return;
            }
        }

        log.info("🔧 Criando tabela INTEGRATION_SYNC_STATUS...");
        createTableWithCorrectStructure();
    }

    /**
     * Verifica se a tabela integration_sync_status tem a estrutura correta.
     * 
     * @return true se a tabela tem a coluna last_sync_date, false caso contrário
     */
    private boolean hasCorrectColumnStructure() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "INTEGRATION_SYNC_STATUS", "LAST_SYNC_DATE");
            boolean hasCorrectColumn = columns.next();
            columns.close();
            return hasCorrectColumn;
        } catch (Exception e) {
            log.warn("Erro ao verificar estrutura da tabela: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Remove e recria a tabela integration_sync_status com a estrutura correta.
     */
    private void dropAndRecreateTable() {
        try {
            log.info("🔧 Removendo tabela INTEGRATION_SYNC_STATUS existente...");
            
            // Tentar remover a tabela (pode falhar se não existir, mas isso é OK)
            try {
                String dropTableSql = "DROP TABLE integration_sync_status";
                if (jdbcTemplate != null) {
                    jdbcTemplate.execute(dropTableSql);
                } else {
                    try (Connection connection = dataSource.getConnection()) {
                        connection.createStatement().execute(dropTableSql);
                    }
                }
                log.info("✅ Tabela removida com sucesso");
            } catch (Exception dropException) {
                // Se a tabela não existir, isso é normal
                log.debug("ℹ️ Tabela não existia ou não pôde ser removida: {}", dropException.getMessage());
            }
            
            log.info("✅ Tabela removida. Criando nova tabela com estrutura correta...");
            createTableWithCorrectStructure();
            
        } catch (Exception e) {
            log.error("❌ Erro ao recriar tabela INTEGRATION_SYNC_STATUS: {}", e.getMessage(), e);
            // Não lançar exceção para não causar rollback da transação principal
            log.warn("Tabela INTEGRATION_SYNC_STATUS não pôde ser recriada, mas a operação principal continuará");
        }
    }

    /**
     * Cria a tabela integration_sync_status com a estrutura correta.
     */
    private void createTableWithCorrectStructure() {
        try {
            String createTableSql = """
                CREATE TABLE integration_sync_status (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    card_id BIGINT NOT NULL,
                    integration_type VARCHAR(50) NOT NULL,
                    external_id VARCHAR(255),
                    sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    last_sync_date TIMESTAMP,
                    error_message TEXT,
                    retry_count INTEGER DEFAULT 0,
                    max_retries INTEGER DEFAULT 3,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    
                    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                    UNIQUE (card_id, integration_type)
                )
                """;

            if (jdbcTemplate != null) {
                jdbcTemplate.execute(createTableSql);
            } else {
                try (Connection connection = dataSource.getConnection()) {
                    connection.createStatement().execute(createTableSql);
                }
            }

            // Criar índices
            createIndexes();
            log.info("✅ Tabela INTEGRATION_SYNC_STATUS criada com estrutura correta");
            
        } catch (Exception e) {
            log.error("❌ Erro ao criar tabela INTEGRATION_SYNC_STATUS: {}", e.getMessage(), e);
            // Não lançar exceção para não causar rollback da transação principal
            log.warn("Tabela INTEGRATION_SYNC_STATUS não pôde ser criada, mas a operação principal continuará");
        }
    }

    /**
     * Cria os índices necessários para a tabela integration_sync_status.
     */
    private void createIndexes() {
        List<String> indexQueries = List.of(
            "CREATE INDEX idx_integration_sync_card_id ON integration_sync_status(card_id)",
            "CREATE INDEX idx_integration_sync_type ON integration_sync_status(integration_type)",
            "CREATE INDEX idx_integration_sync_status ON integration_sync_status(sync_status)",
            "CREATE INDEX idx_integration_sync_last_sync ON integration_sync_status(last_sync_date)"
        );

        for (String indexQuery : indexQueries) {
            try {
                if (jdbcTemplate != null) {
                    jdbcTemplate.execute(indexQuery);
                } else {
                    try (Connection connection = dataSource.getConnection()) {
                        connection.createStatement().execute(indexQuery);
                    }
                }
                log.debug("✅ Índice criado: {}", indexQuery);
            } catch (Exception e) {
                // Verificar se o erro é porque o índice já existe
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("already exists")) {
                    log.debug("ℹ️ Índice já existe: {}", indexQuery);
                } else {
                    log.warn("⚠️ Erro ao criar índice: {} - {}", indexQuery, e.getMessage());
                }
            }
        }
    }

    /**
     * Executa todas as migrações necessárias de forma segura.
     * 
     * <p>Este método deve ser chamado durante a inicialização da aplicação
     * para garantir que todas as tabelas necessárias existam.</p>
     */
    public void runSafeMigrations() {
        log.info("🔧 Iniciando migrações seguras do banco de dados...");
        
        try {
            ensureIntegrationSyncStatusTable();
            log.info("✅ Todas as migrações foram executadas com sucesso");
        } catch (Exception e) {
            log.error("❌ Erro durante as migrações: {}", e.getMessage(), e);
            // Não re-lançar exceção para não causar rollback da transação principal
            log.warn("Algumas migrações falharam, mas a operação principal continuará");
        }
    }

    /**
     * Lista todas as tabelas existentes no banco de dados.
     * 
     * @return lista de nomes das tabelas
     */
    public List<String> listExistingTables() {
        List<String> tables = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME"));
            }
            resultSet.close();
        } catch (Exception e) {
            log.warn("Erro ao listar tabelas: {}", e.getMessage());
        }
        return tables;
    }
}