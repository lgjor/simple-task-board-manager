package org.desviante.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Configuração de dados específica para testes.
 * 
 * <p>Exclui o DatabaseMigrationService para evitar problemas durante os testes.
 * Fornece uma fonte de dados H2 configurada com HikariCP para testes que
 * requerem configuração mais robusta de conexão.</p>
 * 
 * <p>Características:</p>
 * <ul>
 *   <li>Pool de conexões HikariCP para performance</li>
 *   <li>Banco de dados H2 em memória para isolamento</li>
 *   <li>Gerenciamento de transações habilitado</li>
 *   <li>Configuração otimizada para testes</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Configuration
 * @see DataSource
 * @see HikariDataSource
 */
@Configuration
@ComponentScan(basePackages = "org.desviante.repository",
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                    value = org.desviante.service.DatabaseMigrationService.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                    value = org.desviante.config.AppMetadataConfig.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                    value = org.desviante.config.FileWatcherService.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                    value = org.desviante.config.AppConfig.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                    value = org.desviante.config.GoogleApiConfig.class)
        })
@EnableTransactionManagement
public class TestDataConfig {

    /**
     * Fornece uma fonte de dados H2 configurada com HikariCP para testes.
     * 
     * <p>Esta configuração fornece um pool de conexões otimizado para testes,
     * simulando o comportamento real da aplicação em produção. O banco é
     * criado em memória e destruído automaticamente após os testes.</p>
     * 
     * @return DataSource configurado com HikariCP para testes
     */
    @Bean(destroyMethod = "close")
    public DataSource testDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=LEGACY");
        config.setUsername("sa");
        config.setPassword("");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    /**
     * Fornece um gerenciador de transações para testes.
     * 
     * <p>Este bean permite que os testes utilizem anotações de transação
     * como @Transactional, garantindo que as operações de banco sejam
     * executadas dentro de transações controladas.</p>
     * 
     * @param dataSource fonte de dados para gerenciar transações
     * @return gerenciador de transações configurado
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
} 