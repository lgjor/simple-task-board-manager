package org.desviante.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuração específica para testes do Google API.
 * Exclui o DatabaseMigrationService e DatabaseIntegrityChecker para evitar problemas durante os testes.
 */
@Configuration
@ComponentScan(basePackages = "org.desviante.config",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                value = {org.desviante.service.DatabaseMigrationService.class,
                        org.desviante.config.DatabaseIntegrityChecker.class}))
public class TestGoogleApiConfig {

    /**
     * Mock do JdbcTemplate para testes que precisam de um bean JdbcTemplate.
     */
    @Bean
    public JdbcTemplate jdbcTemplate() {
        return org.mockito.Mockito.mock(org.springframework.jdbc.core.JdbcTemplate.class);
    }
} 