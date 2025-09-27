package org.desviante.integration.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ExponentialBackoffRetryStrategy.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class ExponentialBackoffRetryStrategyTest {
    
    private RetryConfig config;
    private ExponentialBackoffRetryStrategy strategy;
    
    @BeforeEach
    void setUp() {
        config = RetryConfig.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofSeconds(1))
                .maxDelay(Duration.ofMinutes(5))
                .backoffMultiplier(2.0)
                .enableJitter(false)
                .build();
        
        strategy = new ExponentialBackoffRetryStrategy(config);
    }
    
    @Test
    void shouldCalculateExponentialDelay() {
        // Arrange
        RetryContext context1 = createContext(1);
        RetryContext context2 = createContext(2);
        RetryContext context3 = createContext(3);
        
        // Act & Assert
        assertEquals(Duration.ofSeconds(1), strategy.calculateDelay(context1));
        assertEquals(Duration.ofSeconds(2), strategy.calculateDelay(context2));
        assertEquals(Duration.ofSeconds(4), strategy.calculateDelay(context3));
    }
    
    @Test
    void shouldRespectMaxDelay() {
        // Arrange
        RetryConfig limitedConfig = RetryConfig.builder()
                .maxAttempts(10)
                .initialDelay(Duration.ofSeconds(1))
                .maxDelay(Duration.ofSeconds(5))
                .backoffMultiplier(3.0)
                .enableJitter(false)
                .build();
        
        ExponentialBackoffRetryStrategy limitedStrategy = new ExponentialBackoffRetryStrategy(limitedConfig);
        RetryContext context = createContext(5); // Tentativa que excederia o limite
        
        // Act
        Duration delay = limitedStrategy.calculateDelay(context);
        
        // Assert
        assertEquals(Duration.ofSeconds(5), delay);
    }
    
    @Test
    void shouldAllowRetryWithinLimits() {
        // Arrange
        RetryContext context = createContext(2);
        context.setMaxAttempts(5);
        
        // Act & Assert
        assertTrue(strategy.shouldRetry(context));
    }
    
    @Test
    void shouldNotAllowRetryWhenMaxAttemptsReached() {
        // Arrange
        RetryContext context = createContext(5);
        context.setMaxAttempts(5);
        
        // Act & Assert
        assertFalse(strategy.shouldRetry(context));
    }
    
    @Test
    void shouldNotAllowRetryWhenTimeLimitExceeded() {
        // Arrange
        RetryConfig timeLimitedConfig = RetryConfig.builder()
                .maxAttempts(10)
                .maxRetryDuration(Duration.ofMinutes(1))
                .build();
        
        ExponentialBackoffRetryStrategy timeLimitedStrategy = new ExponentialBackoffRetryStrategy(timeLimitedConfig);
        
        RetryContext context = createContext(2);
        context.setStartTime(java.time.LocalDateTime.now().minusMinutes(2)); // 2 minutos atrás
        
        // Act & Assert
        assertFalse(timeLimitedStrategy.shouldRetry(context));
    }
    
    @Test
    void shouldIdentifyRetryableExceptions() {
        // Act & Assert
        assertTrue(strategy.isRetryable(new java.net.ConnectException("Connection failed")));
        assertTrue(strategy.isRetryable(new java.net.SocketTimeoutException("Timeout")));
        assertTrue(strategy.isRetryable(new java.io.IOException("IO error")));
        assertTrue(strategy.isRetryable(new java.util.concurrent.TimeoutException("Operation timeout")));
        assertTrue(strategy.isRetryable(new java.util.concurrent.CompletionException("Completion failed", new RuntimeException())));
    }
    
    @Test
    void shouldIdentifyNonRetryableExceptions() {
        // Act & Assert
        assertFalse(strategy.isRetryable(new IllegalArgumentException("Invalid argument")));
        assertFalse(strategy.isRetryable(new SecurityException("Security violation")));
        assertFalse(strategy.isRetryable(new UnsupportedOperationException("Not supported")));
    }
    
    @Test
    void shouldUseCustomRetryableExceptions() {
        // Arrange
        RetryConfig customConfig = RetryConfig.builder()
                .retryableExceptions(Arrays.asList(
                    java.lang.RuntimeException.class,
                    java.lang.NullPointerException.class
                ))
                .nonRetryableExceptions(Arrays.asList(
                    java.lang.IllegalArgumentException.class
                ))
                .build();
        
        ExponentialBackoffRetryStrategy customStrategy = new ExponentialBackoffRetryStrategy(customConfig);
        
        // Act & Assert
        assertTrue(customStrategy.isRetryable(new RuntimeException("Runtime error")));
        assertTrue(customStrategy.isRetryable(new NullPointerException("Null pointer")));
        assertFalse(customStrategy.isRetryable(new IllegalArgumentException("Invalid argument")));
        assertFalse(customStrategy.isRetryable(new java.net.ConnectException("Connection failed"))); // Não está na lista customizada
    }
    
    @Test
    void shouldReturnCorrectMaxAttempts() {
        // Act & Assert
        assertEquals(5, strategy.getMaxAttempts());
    }
    
    @Test
    void shouldReturnCorrectStrategyName() {
        // Act & Assert
        assertEquals("ExponentialBackoff", strategy.getStrategyName());
    }
    
    @Test
    void shouldNotRetryWhenLastExceptionIsNotRetryable() {
        // Arrange
        RetryContext context = createContext(2);
        
        RetryAttempt failedAttempt = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(java.time.LocalDateTime.now())
                .build();
        failedAttempt.markAsFailed(new IllegalArgumentException("Non-retryable"), java.time.LocalDateTime.now());
        
        context.addAttempt(failedAttempt);
        
        // Act & Assert
        assertFalse(strategy.shouldRetry(context));
    }
    
    @Test
    void shouldRetryWhenLastExceptionIsRetryable() {
        // Arrange
        RetryContext context = createContext(2);
        
        RetryAttempt failedAttempt = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(java.time.LocalDateTime.now())
                .build();
        failedAttempt.markAsFailed(new java.net.ConnectException("Connection failed"), java.time.LocalDateTime.now());
        
        context.addAttempt(failedAttempt);
        
        // Act & Assert
        assertTrue(strategy.shouldRetry(context));
    }
    
    @Test
    void shouldHandleNullException() {
        // Act & Assert
        assertFalse(strategy.isRetryable(null));
    }
    
    @Test
    void shouldHandleContextWithoutAttempts() {
        // Arrange
        RetryContext context = createContext(1);
        
        // Act & Assert
        assertTrue(strategy.shouldRetry(context));
    }
    
    /**
     * Helper method para criar contexto de teste.
     */
    private RetryContext createContext(int currentAttempt) {
        return RetryContext.builder()
                .retryId("test-" + currentAttempt)
                .currentAttempt(currentAttempt)
                .maxAttempts(5)
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(config)
                .build();
    }
}
