package org.desviante.integration.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RetryConfig.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class RetryConfigTest {
    
    @Test
    void shouldCreateDefaultConfig() {
        // Act
        RetryConfig config = RetryConfig.defaultConfig();
        
        // Assert
        assertNotNull(config);
        assertEquals(3, config.getMaxAttempts());
        assertEquals(Duration.ofSeconds(1), config.getInitialDelay());
        assertEquals(Duration.ofMinutes(5), config.getMaxDelay());
        assertEquals(2.0, config.getBackoffMultiplier());
        assertTrue(config.isEnableJitter());
        assertFalse(config.getRetryableExceptions().isEmpty());
    }
    
    @Test
    void shouldCreateCriticalConfig() {
        // Act
        RetryConfig config = RetryConfig.criticalConfig();
        
        // Assert
        assertNotNull(config);
        assertEquals(5, config.getMaxAttempts());
        assertEquals(Duration.ofSeconds(2), config.getInitialDelay());
        assertEquals(Duration.ofMinutes(10), config.getMaxDelay());
        assertEquals(1.5, config.getBackoffMultiplier());
        assertEquals(Duration.ofHours(1), config.getMaxRetryDuration());
    }
    
    @Test
    void shouldCreateFastConfig() {
        // Act
        RetryConfig config = RetryConfig.fastConfig();
        
        // Assert
        assertNotNull(config);
        assertEquals(2, config.getMaxAttempts());
        assertEquals(Duration.ofMillis(500), config.getInitialDelay());
        assertEquals(Duration.ofSeconds(5), config.getMaxDelay());
        assertEquals(2.0, config.getBackoffMultiplier());
        assertEquals(Duration.ofMinutes(5), config.getMaxRetryDuration());
    }
    
    @Test
    void shouldCalculateDelayCorrectly() {
        // Arrange
        RetryConfig config = RetryConfig.builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofSeconds(1))
                .backoffMultiplier(2.0)
                .enableJitter(false) // Desabilitar jitter para teste determinístico
                .build();
        
        // Act & Assert
        assertEquals(Duration.ofSeconds(1), config.calculateDelay(1));
        assertEquals(Duration.ofSeconds(2), config.calculateDelay(2));
        assertEquals(Duration.ofSeconds(4), config.calculateDelay(3));
    }
    
    @Test
    void shouldIdentifyRetryableExceptions() {
        // Arrange
        RetryConfig config = RetryConfig.defaultConfig();
        
        // Act & Assert
        assertTrue(config.isRetryable(new java.net.ConnectException("Connection failed")));
        assertTrue(config.isRetryable(new java.io.IOException("IO error")));
        assertFalse(config.isRetryable(new IllegalArgumentException("Invalid argument")));
        assertFalse(config.isRetryable(new SecurityException("Security error")));
    }
}
