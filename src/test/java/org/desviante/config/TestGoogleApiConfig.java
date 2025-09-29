package org.desviante.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuração de teste para a API do Google.
 * 
 * <p>Fornece beans mockados para testes que não dependem
 * de credenciais reais da API do Google. Esta configuração
 * é usada especificamente em testes de integração onde
 * é necessário simular a presença dos serviços do Google.</p>
 * 
 * <p>Características:</p>
 * <ul>
 *   <li>Configuração específica para testes</li>
 *   <li>Beans mockados para evitar chamadas reais de API</li>
 *   <li>Anotação @Primary para sobrescrever configurações reais</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see TestConfiguration
 * @see GoogleApiConfig
 */
@TestConfiguration
public class TestGoogleApiConfig {

    /**
     * Fornece um bean mockado para o serviço Tasks do Google.
     * 
     * <p>Este bean é usado em testes para evitar chamadas reais
     * para a API do Google, permitindo que os testes sejam executados
     * sem dependências externas.</p>
     * 
     * @return bean mockado para testes
     */
    @Bean
    @Primary
    public com.google.api.services.tasks.Tasks googleTasksService() {
        return null; // Bean mockado para testes
    }
} 