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
 *   <li>Inicializador automático do banco de dados</li>
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
     * Configura o inicializador automático do banco de dados.
     *
     * <p>Executa o script schema.sql apenas na primeira execução da aplicação,
     * verificando se o arquivo físico do banco já existe. Isso evita a execução
     * desnecessária de DROP TABLE em execuções subsequentes.</p>
     *
     * @param dataSource fonte de dados configurada
     * @return DataSourceInitializer configurado para inicialização condicional
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

        // 1. Verifica se o arquivo físico do banco de dados já existe.
        //    O H2 cria um arquivo com a extensão .mv.db.
        File dbFile = new File(DB_FILE_PATH + ".mv.db");

        // 2. Habilita o inicializador (que roda o script) APENAS se o arquivo NÃO existir.
        //    Isso garante que o `DROP TABLE` só seja executado na primeira vez.
        initializer.setEnabled(!dbFile.exists());

        return initializer;
    }
}