package org.desviante.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Configuração de dados e persistência da aplicação.
 *
 * <p>Esta classe configura a infraestrutura de dados da aplicação, incluindo
 * a fonte de dados H2, gerenciamento de transações e inicialização do banco.
 * Utiliza HikariCP para pool de conexões e H2 como banco de dados embutido.</p>
 *
 * <p>A configuração inclui:</p>
 * <ul>
 *   <li>DataSource H2 com pool de conexões HikariCP</li>
 *   <li>Gerenciador de transações para operações JDBC</li>
 *   <li>Inicializador automático do banco de dados com preservação de dados</li>
 *   <li>Escaneamento de repositórios para injeção de dependência</li>
 * </ul>
 *
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ComponentScan
 * @see org.springframework.transaction.annotation.EnableTransactionManagement
 * @see com.zaxxer.hikari.HikariDataSource
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 */
@Configuration
@ComponentScan(basePackages = "org.desviante.repository") // Scan ONLY for repositories
@EnableTransactionManagement
public class DataConfig {

    private static final String DB_FILE_PATH = System.getProperty("user.home") + "/myboards/board_h2_db";
    private static final Logger logger = Logger.getLogger(DataConfig.class.getName());

    /**
     * Configura e retorna a fonte de dados H2 com pool de conexões HikariCP.
     *
     * <p>Configura um banco de dados H2 persistente no diretório do usuário,
     * com otimizações de performance para prepared statements e configurações
     * de segurança básicas.</p>
     *
     * @return DataSource configurado com HikariCP
     * @see com.zaxxer.hikari.HikariDataSource
     * @see javax.sql.DataSource
     */
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:" + DB_FILE_PATH + ";AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1");
        config.setUsername("myboarduser");
        config.setPassword("myboardpassword"); // Considere usar uma senha mais forte ou externa no futuro
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    /**
     * Configura o gerenciador de transações para operações JDBC.
     *
     * <p>Utiliza DataSourceTransactionManager para gerenciar transações
     * em operações de banco de dados, garantindo consistência e isolamento.</p>
     *
     * @param dataSource fonte de dados configurada
     * @return PlatformTransactionManager para controle de transações
     * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
     * @see org.springframework.transaction.PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * Configura o inicializador automático do banco de dados com preservação de dados.
     *
     * <p>Executa o script schema.sql apenas quando necessário, preservando dados existentes.
     * Verifica se o banco existe e se as tabelas necessárias estão presentes antes
     * de executar qualquer script de inicialização.</p>
     *
     * @param dataSource fonte de dados configurada
     * @return DataSourceInitializer configurado para inicialização segura
     * @see org.springframework.jdbc.datasource.init.DataSourceInitializer
     * @see org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
     */
    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);

        // Verifica se o banco existe e se precisa de inicialização
        File dbFile = new File(DB_FILE_PATH + ".mv.db");
        boolean shouldInitialize = !dbFile.exists() || !isDatabaseValid(dataSource);
        
        logger.info("Banco de dados existe: " + dbFile.exists());
        logger.info("Banco de dados válido: " + isDatabaseValid(dataSource));
        logger.info("Inicialização necessária: " + shouldInitialize);
        
        initializer.setEnabled(shouldInitialize);

        return initializer;
    }

    /**
     * Verifica se o banco de dados é válido e contém as tabelas necessárias.
     *
     * @param dataSource fonte de dados para verificação
     * @return true se o banco é válido, false caso contrário
     */
    private boolean isDatabaseValid(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Lista de tabelas obrigatórias
            String[] requiredTables = {
                "BOARDS", "BOARD_COLUMNS", "CARDS", "TASKS", 
                "BOARD_GROUPS", "CARD_TYPES"
            };
            
            // Verifica se todas as tabelas obrigatórias existem
            for (String tableName : requiredTables) {
                try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    if (!tables.next()) {
                        logger.warning("Tabela obrigatória não encontrada: " + tableName);
                        return false;
                    }
                }
            }
            
            logger.info("Banco de dados válido - todas as tabelas obrigatórias encontradas");
            return true;
            
        } catch (SQLException e) {
            logger.severe("Erro ao verificar integridade do banco: " + e.getMessage());
            return false;
        }
    }
}