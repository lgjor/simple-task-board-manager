package org.desviante.integration.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RetryContext.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class RetryContextTest {
    
    private RetryContext context;
    private RetryConfig config;
    
    @BeforeEach
    void setUp() {
        config = RetryConfig.defaultConfig();
        
        context = RetryContext.builder()
                .retryId("test-retry-1")
                .currentAttempt(1)
                .maxAttempts(3)
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(config)
                .build();
    }
    
    @Test
    void shouldCreateContextWithDefaultValues() {
        // Act
        RetryContext defaultContext = RetryContext.builder().build();
        
        // Assert
        assertEquals(1, defaultContext.getCurrentAttempt());
        assertEquals(3, defaultContext.getMaxAttempts());
        assertNotNull(defaultContext.getStartTime());
        assertNotNull(defaultContext.getAttempts());
        assertNotNull(defaultContext.getMetadata());
    }
    
    @Test
    void shouldAllowRetryWhenUnderMaxAttempts() {
        // Arrange
        context.setCurrentAttempt(2);
        context.setMaxAttempts(5);
        
        // Act & Assert
        assertTrue(context.canRetry());
    }
    
    @Test
    void shouldNotAllowRetryWhenAtMaxAttempts() {
        // Arrange
        context.setCurrentAttempt(3);
        context.setMaxAttempts(3);
        
        // Act & Assert
        assertFalse(context.canRetry());
    }
    
    @Test
    void shouldNotAllowRetryWhenOverMaxAttempts() {
        // Arrange
        context.setCurrentAttempt(5);
        context.setMaxAttempts(3);
        
        // Act & Assert
        assertFalse(context.canRetry());
    }
    
    @Test
    void shouldIncrementAttempt() {
        // Arrange
        LocalDateTime beforeIncrement = LocalDateTime.now();
        context.setCurrentAttempt(1);
        
        // Act
        context.incrementAttempt();
        
        // Assert
        assertEquals(2, context.getCurrentAttempt());
        assertNotNull(context.getLastAttemptTime());
        assertTrue(context.getLastAttemptTime().isAfter(beforeIncrement) || 
                  context.getLastAttemptTime().isEqual(beforeIncrement));
    }
    
    @Test
    void shouldAddAttemptToHistory() {
        // Arrange
        RetryAttempt attempt = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(LocalDateTime.now())
                .build();
        attempt.markAsSuccessful(LocalDateTime.now());
        
        // Act
        context.addAttempt(attempt);
        
        // Assert
        assertTrue(context.hasAttempts());
        assertEquals(1, context.getTotalAttempts());
        assertEquals(attempt, context.getLastAttempt());
    }
    
    @Test
    void shouldReturnNullForLastAttemptWhenEmpty() {
        // Act & Assert
        assertFalse(context.hasAttempts());
        assertNull(context.getLastAttempt());
        assertEquals(0, context.getTotalAttempts());
    }
    
    @Test
    void shouldCalculateElapsedTime() throws InterruptedException {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now().minusNanos(100_000_000); // -100ms
        context.setStartTime(startTime);
        
        // Act
        Thread.sleep(50); // Aguardar um pouco
        long elapsed = context.getElapsedTimeMillis();
        
        // Assert
        assertTrue(elapsed >= 100);
        assertTrue(elapsed < 200); // Margem de erro
    }
    
    @Test
    void shouldDetectTimeLimitExceeded() {
        // Arrange
        config.setMaxRetryDuration(Duration.ofMinutes(1));
        context.setConfig(config);
        context.setStartTime(LocalDateTime.now().minusMinutes(2));
        
        // Act & Assert
        assertTrue(context.hasExceededTimeLimit());
    }
    
    @Test
    void shouldNotDetectTimeLimitExceededWhenWithinLimit() {
        // Arrange
        config.setMaxRetryDuration(Duration.ofMinutes(5));
        context.setConfig(config);
        context.setStartTime(LocalDateTime.now().minusMinutes(1));
        
        // Act & Assert
        assertFalse(context.hasExceededTimeLimit());
    }
    
    @Test
    void shouldNotDetectTimeLimitExceededWhenNoLimit() {
        // Arrange
        config.setMaxRetryDuration(null);
        context.setConfig(config);
        context.setStartTime(LocalDateTime.now().minusMinutes(10));
        
        // Act & Assert
        assertFalse(context.hasExceededTimeLimit());
    }
    
    @Test
    void shouldAddAndRetrieveMetadata() {
        // Act
        context.addMetadata("key1", "value1");
        context.addMetadata("key2", 42);
        context.addMetadata("key3", LocalDateTime.now());
        
        // Assert
        assertEquals("value1", context.getMetadata("key1"));
        assertEquals(42, context.getMetadata("key2"));
        assertNotNull(context.getMetadata("key3"));
        assertNull(context.getMetadata("nonexistent"));
    }
    
    @Test
    void shouldRetrieveTypedMetadata() {
        // Arrange
        LocalDateTime testTime = LocalDateTime.now();
        context.addMetadata("stringValue", "test");
        context.addMetadata("intValue", 123);
        context.addMetadata("dateValue", testTime);
        context.addMetadata("nullValue", null);
        
        // Act & Assert
        assertEquals("test", context.getMetadata("stringValue", String.class));
        assertEquals(Integer.valueOf(123), context.getMetadata("intValue", Integer.class));
        assertEquals(testTime, context.getMetadata("dateValue", LocalDateTime.class));
        assertNull(context.getMetadata("nullValue", String.class));
        assertNull(context.getMetadata("nonexistent", String.class));
        assertNull(context.getMetadata("stringValue", Integer.class)); // Tipo incorreto
    }
    
    @Test
    void shouldHandleMultipleAttempts() {
        // Arrange
        RetryAttempt attempt1 = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(LocalDateTime.now())
                .build();
        attempt1.markAsSuccessful(LocalDateTime.now());
        
        RetryAttempt attempt2 = RetryAttempt.builder()
                .attemptNumber(2)
                .startTime(LocalDateTime.now())
                .build();
        attempt2.markAsFailed(new RuntimeException("Test error"), LocalDateTime.now());
        
        // Act
        context.addAttempt(attempt1);
        context.addAttempt(attempt2);
        
        // Assert
        assertTrue(context.hasAttempts());
        assertEquals(2, context.getTotalAttempts());
        assertEquals(attempt2, context.getLastAttempt());
    }
    
    @Test
    void shouldPreserveAllFields() {
        // Arrange
        String retryId = "custom-retry-123";
        int currentAttempt = 2;
        int maxAttempts = 5;
        String operationType = "CUSTOM_OPERATION";
        Long entityId = 999L;
        String integrationType = "CALENDAR";
        
        // Act
        RetryContext customContext = RetryContext.builder()
                .retryId(retryId)
                .currentAttempt(currentAttempt)
                .maxAttempts(maxAttempts)
                .operationType(operationType)
                .entityId(entityId)
                .integrationType(integrationType)
                .config(config)
                .build();
        
        // Assert
        assertEquals(retryId, customContext.getRetryId());
        assertEquals(currentAttempt, customContext.getCurrentAttempt());
        assertEquals(maxAttempts, customContext.getMaxAttempts());
        assertEquals(operationType, customContext.getOperationType());
        assertEquals(entityId, customContext.getEntityId());
        assertEquals(integrationType, customContext.getIntegrationType());
        assertEquals(config, customContext.getConfig());
    }
}
