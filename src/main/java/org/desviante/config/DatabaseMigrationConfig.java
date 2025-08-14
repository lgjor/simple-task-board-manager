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
 * Configuração para executar migrações manuais do banco de dados durante a inicialização da aplicação.
 * 
 * <p>Esta classe implementa {@link CommandLineRunner} para executar automaticamente
 * migrações de banco de dados que não podem ser realizadas via Liquibase. As migrações
 * incluem alterações estruturais como adição de colunas e criação de tabelas.</p>
 * 
 * <p><strong>Funcionalidades Principais:</strong></p>
 * <ul>
 *   <li><strong>Migração order_index:</strong> Adiciona coluna para ordenação de cards</li>
 *   <li><strong>Migração progress_type:</strong> Adiciona coluna para tipo de progresso</li>
 *   <li><strong>Migração checklist_items:</strong> Cria tabela para itens de checklist</li>
 *   <li><strong>Verificação automática:</strong> Detecta se migrações já foram executadas</li>
 *   <li><strong>Compatibilidade multi-banco:</strong> Suporta H2 e SQLite</li>
 * </ul>
 * 
 * <p><strong>Segurança:</strong> Todas as migrações são idempotentes e podem ser
 * executadas múltiplas vezes sem causar erros ou duplicação de dados.</p>
 * 
 * <p><strong>Tratamento de Erros:</strong> Falhas durante migrações são registradas
 * no log mas não interrompem a inicialização da aplicação.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CommandLineRunner
 * @see DataSource
 * @see JdbcTemplate
 */
@Component
@Slf4j
public class DatabaseMigrationConfig implements CommandLineRunner {

    /**
     * Fonte de dados para conexão com o banco de dados.
     * Utilizada para obter metadados e verificar estrutura das tabelas.
     */
    private final DataSource dataSource;

    /**
     * Template JDBC para execução de comandos SQL.
     * Utilizado para executar as migrações e alterações no banco.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Construtor que injeta as dependências necessárias para execução das migrações.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     * @param jdbcTemplate template JDBC para execução de comandos SQL
     */
    @Autowired
    public DatabaseMigrationConfig(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executa automaticamente todas as migrações necessárias durante a inicialização da aplicação.
     * 
     * <p>Este método é chamado pelo Spring Boot após a inicialização completa do contexto.
     * Executa as seguintes verificações e migrações em sequência:</p>
     * 
     * <ol>
     *   <li><strong>order_index:</strong> Verifica e adiciona coluna para ordenação de cards</li>
     *   <li><strong>progress_type:</strong> Verifica e adiciona coluna para tipo de progresso</li>
     *   <li><strong>checklist_items:</strong> Verifica e cria tabela para itens de checklist</li>
     * </ol>
     * 
     * <p>Cada migração é executada apenas se necessário, baseado na verificação
     * da estrutura atual do banco de dados.</p>
     * 
     * @param args argumentos da linha de comando (não utilizados neste contexto)
     * @throws Exception pode lançar exceções durante migrações, mas são capturadas
     *         e tratadas internamente sem propagar para o Spring Boot
     * 
     * @see #columnExists(String, String)
     * @see #tableExists(String)
     * @see #executeMigration()
     * @see #executeProgressTypeMigration()
     * @see #executeChecklistItemsMigration()
     */
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
     * Verifica se uma coluna específica existe em uma tabela do banco de dados.
     * 
     * <p>Utiliza {@link DatabaseMetaData} para consultar informações sobre colunas
     * existentes, permitindo determinar se uma migração específica já foi executada.</p>
     * 
     * <p><strong>Tratamento de Erros:</strong> Em caso de falha na consulta, retorna
     * {@code false} e registra o erro no log, garantindo que a aplicação continue
     * funcionando mesmo com problemas de conexão.</p>
     * 
     * @param tableName nome da tabela (case-insensitive)
     * @param columnName nome da coluna (case-insensitive)
     * @return {@code true} se a coluna existe, {@code false} caso contrário
     * 
     * @see DatabaseMetaData#getColumns(String, String, String, String)
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
     * Verifica se uma tabela específica existe no banco de dados.
     * 
     * <p>Utiliza {@link DatabaseMetaData} para consultar informações sobre tabelas
     * existentes, permitindo determinar se uma migração de criação de tabela
     * já foi executada.</p>
     * 
     * <p><strong>Tratamento de Erros:</strong> Em caso de falha na consulta, retorna
     * {@code false} e registra o erro no log, garantindo que a aplicação continue
     * funcionando mesmo com problemas de conexão.</p>
     * 
     * @param tableName nome da tabela (case-insensitive)
     * @return {@code true} se a tabela existe, {@code false} caso contrário
     * 
     * @see DatabaseMetaData#getTables(String, String, String, String[])
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
     * Executa a migração para adicionar o campo order_index na tabela cards.
     * 
     * <p>Esta migração realiza as seguintes operações em sequência:</p>
     * <ol>
     *   <li>Adiciona a coluna {@code order_index INTEGER DEFAULT 0}</li>
     *   <li>Cria índice composto para otimizar consultas por coluna e ordem</li>
     *   <li>Popula o campo com valores baseados na data de criação dos cards</li>
     *   <li>Verifica e registra o resultado da migração</li>
     * </ol>
     * 
     * <p><strong>Índice Criado:</strong> {@code idx_cards_column_order(board_column_id, order_index)}
     * para otimizar consultas que ordenam cards por coluna.</p>
     * 
     * <p><strong>Tratamento de Erros:</strong> Falhas são registradas no log mas
     * não interrompem a execução da aplicação.</p>
     * 
     * @see JdbcTemplate#execute(String)
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
     * Executa a migração para adicionar o campo progress_type na tabela cards.
     * 
     * <p>Esta migração adiciona suporte para diferentes tipos de progresso nos cards,
     * permitindo que o sistema utilize estratégias de progresso variadas como
     * percentual, checklist ou sem progresso.</p>
     * 
     * <p><strong>Operações Realizadas:</strong></p>
     * <ol>
     *   <li>Adiciona coluna {@code progress_type VARCHAR(50) DEFAULT 'PERCENTAGE'}</li>
     *   <li>Atualiza cards existentes com valor padrão 'PERCENTAGE'</li>
     *   <li>Verifica e registra o resultado da migração</li>
     * </ol>
     * 
     * <p><strong>Valor Padrão:</strong> Todos os cards existentes são configurados
     * com tipo de progresso 'PERCENTAGE' para manter compatibilidade.</p>
     * 
     * <p><strong>Tratamento de Erros:</strong> Falhas são registradas no log mas
     * não interrompem a execução da aplicação.</p>
     * 
     * @see JdbcTemplate#execute(String)
     * @see JdbcTemplate#queryForObject(String, Class)
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
     * Executa a migração para criar a tabela checklist_items com suporte multi-banco.
     * 
     * <p>Esta migração cria uma tabela para armazenar itens de checklist associados
     * aos cards, com suporte para diferentes tipos de banco de dados (H2 e SQLite).</p>
     * 
     * <p><strong>Estrutura da Tabela:</strong></p>
     * <ul>
     *   <li><strong>id:</strong> Identificador único (BIGINT/INTEGER)</li>
     *   <li><strong>card_id:</strong> Referência ao card pai (chave estrangeira)</li>
     *   <li><strong>text:</strong> Texto do item do checklist</li>
     *   <li><strong>completed:</strong> Status de conclusão (BOOLEAN)</li>
     *   <li><strong>order_index:</strong> Ordem de exibição (INT)</li>
     *   <li><strong>created_at:</strong> Data de criação (TIMESTAMP)</li>
     *   <li><strong>completed_at:</strong> Data de conclusão (TIMESTAMP, opcional)</li>
     * </ul>
     * 
     * <p><strong>Compatibilidade Multi-Banco:</strong></p>
     * <ul>
     *   <li><strong>H2:</strong> Utiliza sintaxe específica com AUTO_INCREMENT</li>
     *   <li><strong>SQLite:</strong> Utiliza sintaxe padrão com AUTOINCREMENT</li>
     * </ul>
     * 
     * <p><strong>Índices Criados:</strong></p>
     * <ul>
     *   <li><strong>idx_checklist_items_card_id:</strong> Para consultas por card</li>
     *   <li><strong>idx_checklist_items_order_index:</strong> Para ordenação</li>
     * </ul>
     * 
     * <p><strong>Tratamento de Erros:</strong> Falhas são registradas no log mas
     * não interrompem a execução da aplicação.</p>
     * 
     * @see DataSource#getConnection()
     * @see DatabaseMetaData#getDatabaseProductName()
     * @see JdbcTemplate#execute(String)
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
