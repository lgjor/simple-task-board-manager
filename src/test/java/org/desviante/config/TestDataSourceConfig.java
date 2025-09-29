package org.desviante.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Configuração de fonte de dados para testes de integração.
 * 
 * <p>Fornece uma fonte de dados H2 configurada com HikariCP para testes
 * que requerem configuração mais robusta de conexão. Esta configuração
 * é usada em testes de integração que simulam o ambiente real da aplicação.</p>
 * 
 * <p>Características:</p>
 * <ul>
 *   <li>Pool de conexões HikariCP para performance</li>
 *   <li>Banco de dados H2 em memória para isolamento</li>
 *   <li>Gerenciamento de transações habilitado</li>
 *   <li>Configuração otimizada para testes</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see TestConfiguration
 * @see DataSource
 * @see HikariDataSource
 */
@TestConfiguration
@EnableTransactionManagement
public class TestDataSourceConfig {

    /**
     * Fornece uma fonte de dados H2 configurada com HikariCP para testes.
     * 
     * <p>Esta configuração fornece um pool de conexões otimizado para testes,
     * simulando o comportamento real da aplicação em produção. O banco é
     * criado em memória e destruído automaticamente após os testes.</p>
     * 
     * @return DataSource configurado com HikariCP para testes
     */
    @Bean
    @Primary
    public DataSource dataSource() {
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
