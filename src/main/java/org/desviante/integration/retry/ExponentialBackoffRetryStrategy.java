package org.desviante.integration.retry;

import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * Estratégia de retry com backoff exponencial.
 * 
 * <p>Esta estratégia implementa retry com delay exponencial crescente,
 * onde cada tentativa subsequente aguarda um tempo maior que a anterior.
 * Isso ajuda a evitar sobrecarga em sistemas que estão temporariamente
 * indisponíveis.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Delay inicial configurável</li>
 *   <li>Multiplicador exponencial</li>
 *   <li>Limite máximo de delay</li>
 *   <li>Jitter opcional para evitar thundering herd</li>
 *   <li>Lista de exceções retryable</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RetryStrategy
 * @see RetryConfig
 */
@RequiredArgsConstructor
public class ExponentialBackoffRetryStrategy implements RetryStrategy {
    
    private final RetryConfig config;
    
    
    @Override
    public Duration calculateDelay(RetryContext context) {
        return config.calculateDelay(context.getCurrentAttempt());
    }
    
    @Override
    public boolean shouldRetry(RetryContext context) {
        // Verificar limite de tentativas
        if (!context.canRetry()) {
            return false;
        }
        
        // Verificar limite de tempo total
        if (context.hasExceededTimeLimit()) {
            return false;
        }
        
        // Verificar se a última exceção é retryable
        RetryAttempt lastAttempt = context.getLastAttempt();
        if (lastAttempt != null && lastAttempt.getException() != null) {
            return isRetryable(lastAttempt.getException());
        }
        
        return true;
    }
    
    @Override
    public int getMaxAttempts() {
        return config.getMaxAttempts();
    }
    
    @Override
    public boolean isRetryable(Exception exception) {
        if (exception == null) {
            return false;
        }
        
        // Usar configuração do RetryConfig (que já tem lógica para verificar ambas as listas)
        return config.isRetryable(exception);
    }
    
    @Override
    public String getStrategyName() {
        return "ExponentialBackoff";
    }
}
