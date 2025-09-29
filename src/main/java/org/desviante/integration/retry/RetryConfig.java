package org.desviante.integration.retry;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Configuração para operações de retry.
 * 
 * <p>Esta classe define as configurações específicas para operações
 * de retry, incluindo delays, limites e estratégias personalizadas.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
public class RetryConfig {
    
    /**
     * Número máximo de tentativas.
     */
    @Builder.Default
    private int maxAttempts = 3;
    
    /**
     * Delay inicial entre tentativas.
     */
    @Builder.Default
    private Duration initialDelay = Duration.ofSeconds(1);
    
    /**
     * Delay máximo entre tentativas.
     */
    @Builder.Default
    private Duration maxDelay = Duration.ofMinutes(5);
    
    /**
     * Multiplicador para delay exponencial.
     */
    @Builder.Default
    private double backoffMultiplier = 2.0;
    
    /**
     * Jitter (variação aleatória) no delay.
     */
    @Builder.Default
    private boolean enableJitter = true;
    
    /**
     * Duração máxima total para retry.
     */
    private Duration maxRetryDuration;
    
    /**
     * Tipos de exceção que permitem retry.
     */
    @Builder.Default
    private List<Class<? extends Exception>> retryableExceptions = Arrays.asList(
        java.net.ConnectException.class,
        java.net.SocketTimeoutException.class,
        java.io.IOException.class,
        java.util.concurrent.TimeoutException.class,
        java.util.concurrent.CompletionException.class
    );
    
    /**
     * Tipos de exceção que NÃO permitem retry.
     */
    @Builder.Default
    private List<Class<? extends Exception>> nonRetryableExceptions = Arrays.asList(
        IllegalArgumentException.class,
        SecurityException.class,
        UnsupportedOperationException.class
    );
    
    /**
     * Verifica se uma exceção é retryable baseada na configuração.
     * 
     * @param exception exceção a ser verificada
     * @return true se a exceção permite retry
     */
    public boolean isRetryable(Exception exception) {
        if (exception == null) {
            return false;
        }
        
        Class<?> exceptionClass = exception.getClass();
        
        // Verificar se está na lista de não-retryable
        for (Class<? extends Exception> nonRetryable : nonRetryableExceptions) {
            if (nonRetryable.isAssignableFrom(exceptionClass)) {
                return false;
            }
        }
        
        // Verificar se está na lista de retryable
        for (Class<? extends Exception> retryable : retryableExceptions) {
            if (retryable.isAssignableFrom(exceptionClass)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calcula o delay para uma tentativa específica.
     * 
     * @param attemptNumber número da tentativa (1-based)
     * @return delay calculado
     */
    public Duration calculateDelay(int attemptNumber) {
        if (attemptNumber <= 1) {
            return initialDelay;
        }
        
        // Calcular delay exponencial
        double delaySeconds = initialDelay.toMillis() * Math.pow(backoffMultiplier, attemptNumber - 1) / 1000.0;
        Duration delay = Duration.ofMillis((long) delaySeconds * 1000);
        
        // Aplicar limite máximo
        if (delay.compareTo(maxDelay) > 0) {
            delay = maxDelay;
        }
        
        // Aplicar jitter se habilitado
        if (enableJitter) {
            double jitterFactor = 0.5 + (Math.random() * 0.5); // 0.5 a 1.0
            delay = Duration.ofMillis((long) (delay.toMillis() * jitterFactor));
        }
        
        return delay;
    }
    
    /**
     * Cria configuração padrão para operações rápidas.
     * 
     * @return configuração padrão
     */
    public static RetryConfig defaultConfig() {
        return RetryConfig.builder().build();
    }
    
    /**
     * Cria configuração para operações críticas com mais tentativas.
     * 
     * @return configuração para operações críticas
     */
    public static RetryConfig criticalConfig() {
        return RetryConfig.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofSeconds(2))
                .maxDelay(Duration.ofMinutes(10))
                .backoffMultiplier(1.5)
                .maxRetryDuration(Duration.ofHours(1))
                .build();
    }
    
    /**
     * Cria configuração para operações rápidas com menos tentativas.
     * 
     * @return configuração para operações rápidas
     */
    public static RetryConfig fastConfig() {
        return RetryConfig.builder()
                .maxAttempts(2)
                .initialDelay(Duration.ofMillis(500))
                .maxDelay(Duration.ofSeconds(5))
                .backoffMultiplier(2.0)
                .maxRetryDuration(Duration.ofMinutes(5))
                .build();
    }
}
