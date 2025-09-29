package org.desviante.integration.retry;

import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * Estratégia de retry com delay fixo.
 * 
 * <p>Esta estratégia implementa retry com delay constante entre tentativas,
 * útil para operações que requerem intervalos regulares ou quando
 * o sistema de destino tem limitações específicas de taxa.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Delay fixo configurável</li>
 *   <li>Número máximo de tentativas</li>
 *   <li>Limite de tempo total</li>
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
public class FixedDelayRetryStrategy implements RetryStrategy {
    
    private final RetryConfig config;
    
    @Override
    public Duration calculateDelay(RetryContext context) {
        // Para estratégia de delay fixo, sempre usar o delay inicial
        return config.getInitialDelay();
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
        return "FixedDelay";
    }
}
