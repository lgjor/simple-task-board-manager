package org.desviante.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Configuração de DataSource específica para testes de integração.
 * Cria um banco de dados H2 em memória, garantindo testes rápidos e isolados.
 */
@Configuration
public class TestDataSourceConfig {

    @Bean
    public DataSource dataSource() {
        // A URL "jdbc:h2:mem:..." cria um banco de dados em memória.
        // "testdb" é o nome do banco, útil para depuração.
        // "DB_CLOSE_DELAY=-1" impede que o H2 apague o banco entre conexões dentro do mesmo teste.
        // "MODE=LEGACY" é uma boa prática para compatibilidade.
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=LEGACY");
        dataSource.setUsername("sa"); // Usuário padrão para H2 em memória
        dataSource.setPassword("");   // Senha padrão
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    // IMPORTANTE: Não precisamos de um DataSourceInitializer aqui.
    // Testes com anotação @Sql(scripts = "/test-schema.sql")
    // para criar o schema, o que dá controle total por teste.
}