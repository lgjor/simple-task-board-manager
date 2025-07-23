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

@Configuration
@ComponentScan(basePackages = "org.desviante.repository") // Scan ONLY for repositories
@EnableTransactionManagement
public class DataConfig {

    private static final String DB_FILE_PATH = System.getProperty("user.home") + "/myboards/board_h2_db";

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

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

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