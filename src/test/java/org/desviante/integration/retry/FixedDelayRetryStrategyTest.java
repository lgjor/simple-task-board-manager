package org.desviante.integration.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para FixedDelayRetryStrategy.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class FixedDelayRetryStrategyTest {
    
    private RetryConfig config;
    private FixedDelayRetryStrategy strategy;
    
    @BeforeEach
    void setUp() {
        config = RetryConfig.builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofSeconds(2))
                .maxDelay(Duration.ofMinutes(5))
                .backoffMultiplier(2.0)
                .enableJitter(false)
                .build();
        
        strategy = new FixedDelayRetryStrategy(config);
    }
    
    @Test
    void shouldAlwaysReturnFixedDelay() {
        // Arrange
        RetryContext context1 = createContext(1);
        RetryContext context2 = createContext(2);
        RetryContext context3 = createContext(3);
        RetryContext context10 = createContext(10);
        
        // Act & Assert
        assertEquals(Duration.ofSeconds(2), strategy.calculateDelay(context1));
        assertEquals(Duration.ofSeconds(2), strategy.calculateDelay(context2));
        assertEquals(Duration.ofSeconds(2), strategy.calculateDelay(context3));
        assertEquals(Duration.ofSeconds(2), strategy.calculateDelay(context10));
    }
    
    @Test
    void shouldAllowRetryWithinLimits() {
        // Arrange
        RetryContext context = createContext(2);
        context.setMaxAttempts(3);
        
        // Act & Assert
        assertTrue(strategy.shouldRetry(context));
    }
    
    @Test
    void shouldNotAllowRetryWhenMaxAttemptsReached() {
        // Arrange
        RetryContext context = createContext(3);
        context.setMaxAttempts(3);
        
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
        
        FixedDelayRetryStrategy timeLimitedStrategy = new FixedDelayRetryStrategy(timeLimitedConfig);
        
        RetryContext context = RetryContext.builder()
                .retryId("test-time-limit")
                .currentAttempt(2)
                .maxAttempts(10)
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(timeLimitedConfig)
                .startTime(java.time.LocalDateTime.now().minusMinutes(2)) // 2 minutos atrás
                .build();
        
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
        
        FixedDelayRetryStrategy customStrategy = new FixedDelayRetryStrategy(customConfig);
        
        // Act & Assert
        assertTrue(customStrategy.isRetryable(new RuntimeException("Runtime error")));
        assertTrue(customStrategy.isRetryable(new NullPointerException("Null pointer")));
        assertFalse(customStrategy.isRetryable(new IllegalArgumentException("Invalid argument")));
        assertFalse(customStrategy.isRetryable(new java.net.ConnectException("Connection failed"))); // Não está na lista customizada
    }
    
    @Test
    void shouldReturnCorrectMaxAttempts() {
        // Act & Assert
        assertEquals(3, strategy.getMaxAttempts());
    }
    
    @Test
    void shouldReturnCorrectStrategyName() {
        // Act & Assert
        assertEquals("FixedDelay", strategy.getStrategyName());
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
    
    @Test
    void shouldIgnoreBackoffMultiplier() {
        // Arrange
        RetryConfig configWithBackoff = RetryConfig.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofSeconds(3))
                .backoffMultiplier(10.0) // Multiplicador alto que seria usado em exponential
                .build();
        
        FixedDelayRetryStrategy strategyWithBackoff = new FixedDelayRetryStrategy(configWithBackoff);
        
        RetryContext context = createContext(5);
        
        // Act
        Duration delay = strategyWithBackoff.calculateDelay(context);
        
        // Assert - Deve sempre retornar o delay inicial, ignorando o backoff
        assertEquals(Duration.ofSeconds(3), delay);
    }
    
    /**
     * Helper method para criar contexto de teste.
     */
    private RetryContext createContext(int currentAttempt) {
        return RetryContext.builder()
                .retryId("test-" + currentAttempt)
                .currentAttempt(currentAttempt)
                .maxAttempts(3)
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(config)
                .build();
    }
}
