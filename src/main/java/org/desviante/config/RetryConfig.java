package org.desviante.config;

import org.desviante.integration.retry.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para o sistema de retry da aplicação.
 * 
 * <p>Esta classe configura os beans necessários para o sistema de retry,
 * incluindo estratégias e o executor principal. O sistema de retry é
 * usado para operações de integração que podem falhar temporariamente.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela configuração de retry</li>
 *   <li><strong>OCP:</strong> Extensível através de novas estratégias</li>
 *   <li><strong>LSP:</strong> Trabalha com qualquer implementação de RetryStrategy</li>
 *   <li><strong>ISP:</strong> Interface específica para configuração</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (RetryStrategy)</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RetryExecutor
 * @see FixedDelayRetryStrategy
 * @see ExponentialBackoffRetryStrategy
 * @see RetryConfig
 */
@Configuration
public class RetryConfig {

    /**
     * Cria uma estratégia de retry com delay fixo para operações de integração.
     * 
     * <p>Esta estratégia é adequada para operações que requerem intervalos
     * regulares entre tentativas, como APIs com limitações de taxa.</p>
     * 
     * @return estratégia de retry com delay fixo
     */
    @Bean
    public FixedDelayRetryStrategy fixedDelayRetryStrategy() {
        org.desviante.integration.retry.RetryConfig retryConfig = org.desviante.integration.retry.RetryConfig.builder()
                .maxAttempts(3)
                .initialDelay(java.time.Duration.ofSeconds(2))
                .maxDelay(java.time.Duration.ofSeconds(10))
                .build();
        
        return new FixedDelayRetryStrategy(retryConfig);
    }
    
    /**
     * Cria uma estratégia de retry com backoff exponencial para operações críticas.
     * 
     * <p>Esta estratégia é adequada para operações que podem sobrecarregar
     * sistemas de destino, aumentando progressivamente o delay entre tentativas.</p>
     * 
     * @return estratégia de retry com backoff exponencial
     */
    @Bean
    public ExponentialBackoffRetryStrategy exponentialBackoffRetryStrategy() {
        org.desviante.integration.retry.RetryConfig retryConfig = org.desviante.integration.retry.RetryConfig.builder()
                .maxAttempts(5)
                .initialDelay(java.time.Duration.ofSeconds(1))
                .maxDelay(java.time.Duration.ofMinutes(5))
                .backoffMultiplier(2.0)
                .enableJitter(true)
                .build();
        
        return new ExponentialBackoffRetryStrategy(retryConfig);
    }
    
    /**
     * Cria o executor principal de retry usando a estratégia de delay fixo.
     * 
     * <p>O executor é configurado com a estratégia de delay fixo como padrão,
     * que é adequada para a maioria das operações de integração com Google Tasks.</p>
     * 
     * @param strategy estratégia de retry a ser usada
     * @return executor de retry configurado
     */
    @Bean
    public RetryExecutor retryExecutor(FixedDelayRetryStrategy strategy) {
        return new RetryExecutor(strategy);
    }
}
