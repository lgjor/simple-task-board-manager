package org.desviante.integration.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RetryResult.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class RetryResultTest {
    
    private RetryResult result;
    private RetryConfig config;
    private LocalDateTime startTime;
    
    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now();
        config = RetryConfig.defaultConfig();
        
        result = RetryResult.builder()
                .startTime(startTime)
                .config(config)
                .build();
    }
    
    @Test
    void shouldCreateResultWithDefaultValues() {
        // Act
        RetryResult defaultResult = RetryResult.builder().build();
        
        // Assert
        assertFalse(defaultResult.isSuccessful());
        assertEquals(0, defaultResult.getTotalAttempts());
        assertNotNull(defaultResult.getStartTime());
        assertNull(defaultResult.getEndTime());
        assertNull(defaultResult.getTotalDuration());
        assertNull(defaultResult.getFinalException());
        assertNull(defaultResult.getErrorMessage());
        assertNotNull(defaultResult.getAttempts());
        assertNotNull(defaultResult.getMetadata());
    }
    
    @Test
    void shouldMarkAsSuccessful() {
        // Arrange
        String expectedResult = "Operation completed";
        LocalDateTime endTime = startTime.plusSeconds(2);
        
        // Act
        result.markAsSuccessful(expectedResult, endTime);
        
        // Assert
        assertTrue(result.isSuccessful());
        assertFalse(result.isInProgress());
        assertEquals(expectedResult, result.getLastSuccessfulResult());
        assertEquals(endTime, result.getEndTime());
        assertEquals(Duration.ofSeconds(2), result.getTotalDuration());
        assertEquals(2000, result.getTotalDurationMillis());
        assertNull(result.getFinalException());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void shouldMarkAsFailed() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(3);
        RuntimeException exception = new RuntimeException("Operation failed");
        
        // Act
        result.markAsFailed(exception, endTime);
        
        // Assert
        assertFalse(result.isSuccessful());
        assertFalse(result.isInProgress());
        assertNull(result.getLastSuccessfulResult());
        assertEquals(endTime, result.getEndTime());
        assertEquals(Duration.ofSeconds(3), result.getTotalDuration());
        assertEquals(3000, result.getTotalDurationMillis());
        assertEquals(exception, result.getFinalException());
        assertEquals("Operation failed", result.getErrorMessage());
    }
    
    @Test
    void shouldMarkAsFailedWithNullException() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(1);
        
        // Act
        result.markAsFailed(null, endTime);
        
        // Assert
        assertFalse(result.isSuccessful());
        assertNull(result.getLastSuccessfulResult());
        assertNull(result.getFinalException());
        assertEquals("Erro desconhecido", result.getErrorMessage());
    }
    
    @Test
    void shouldAddAttemptsToHistory() {
        // Arrange
        RetryAttempt attempt1 = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(startTime)
                .build();
        attempt1.markAsFailed(new RuntimeException("First attempt failed"), startTime.plusSeconds(1));
        
        RetryAttempt attempt2 = RetryAttempt.builder()
                .attemptNumber(2)
                .startTime(startTime.plusSeconds(2))
                .build();
        attempt2.markAsSuccessful(startTime.plusSeconds(3));
        
        // Act
        result.addAttempt(attempt1);
        result.addAttempt(attempt2);
        
        // Assert
        assertEquals(2, result.getTotalAttempts());
        assertEquals(2, result.getAttempts().size());
        assertEquals(attempt2, result.getLastAttempt());
        assertEquals(1, result.getSuccessfulAttemptsCount());
        assertEquals(1, result.getFailedAttemptsCount());
        assertEquals(attempt2, result.getFirstSuccessfulAttempt());
    }
    
    @Test
    void shouldCalculateAverageAttemptDuration() {
        // Arrange
        RetryAttempt attempt1 = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(startTime)
                .build();
        attempt1.markAsSuccessful(startTime.plusNanos(500_000_000)); // +500ms
        
        RetryAttempt attempt2 = RetryAttempt.builder()
                .attemptNumber(2)
                .startTime(startTime.plusSeconds(1))
                .build();
        attempt2.markAsSuccessful(startTime.plusNanos(1_500_000_000)); // +1500ms
        
        // Act
        result.addAttempt(attempt1);
        result.addAttempt(attempt2);
        
        // Assert
        assertEquals(1000.0, result.getAverageAttemptDurationMillis()); // (500 + 1500) / 2 = 1000
    }
    
    @Test
    void shouldReturnZeroAverageForEmptyAttempts() {
        // Act & Assert
        assertEquals(0.0, result.getAverageAttemptDurationMillis());
    }
    
    @Test
    void shouldHandleMetadataOperations() {
        // Act
        result.addMetadata("operationId", "op-123");
        result.addMetadata("retryCount", 5);
        result.addMetadata("timestamp", LocalDateTime.now());
        
        // Assert
        assertEquals("op-123", result.getMetadata("operationId"));
        assertEquals(5, result.getMetadata("retryCount"));
        assertNotNull(result.getMetadata("timestamp"));
        assertNull(result.getMetadata("nonexistent"));
    }
    
    @Test
    void shouldGenerateCorrectSummaryForInProgress() {
        // Act
        String summary = result.getSummary();
        
        // Assert
        assertTrue(summary.contains("Retry em andamento"));
        assertTrue(summary.contains("0 tentativas realizadas"));
    }
    
    @Test
    void shouldGenerateCorrectSummaryForSuccessful() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(2);
        
        RetryAttempt attempt = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(startTime)
                .build();
        attempt.markAsSuccessful(endTime);
        
        // Act
        result.addAttempt(attempt);
        result.markAsSuccessful("Success", endTime);
        String summary = result.getSummary();
        
        // Assert
        assertTrue(summary.contains("Retry bem-sucedido"));
        assertTrue(summary.contains("1 tentativas"));
        assertTrue(summary.contains("2000ms"));
    }
    
    @Test
    void shouldGenerateCorrectSummaryForFailed() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(3);
        RuntimeException exception = new RuntimeException("Multiple attempts failed");
        
        RetryAttempt attempt = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(startTime)
                .build();
        attempt.markAsFailed(exception, endTime);
        
        // Act
        result.addAttempt(attempt);
        result.markAsFailed(exception, endTime);
        String summary = result.getSummary();
        
        // Assert
        assertTrue(summary.contains("Retry falhou"));
        assertTrue(summary.contains("1 tentativas"));
        assertTrue(summary.contains("3000ms"));
        assertTrue(summary.contains("Multiple attempts failed"));
    }
    
    @Test
    void shouldPreserveAllFields() {
        // Arrange
        LocalDateTime customStartTime = LocalDateTime.now().minusMinutes(1);
        RetryConfig customConfig = RetryConfig.criticalConfig();
        String operationResult = "Custom result";
        
        // Act
        RetryResult customResult = RetryResult.builder()
                .startTime(customStartTime)
                .config(customConfig)
                .build();
        
        customResult.markAsSuccessful(operationResult, LocalDateTime.now());
        
        // Assert
        assertEquals(customStartTime, customResult.getStartTime());
        assertEquals(customConfig, customResult.getConfig());
        assertEquals(operationResult, customResult.getLastSuccessfulResult());
    }
    
    @Test
    void shouldHandleMultipleFailedAttempts() {
        // Arrange
        RuntimeException exception1 = new RuntimeException("First failure");
        RuntimeException exception2 = new RuntimeException("Second failure");
        
        RetryAttempt attempt1 = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(startTime)
                .build();
        attempt1.markAsFailed(exception1, startTime.plusSeconds(1));
        
        RetryAttempt attempt2 = RetryAttempt.builder()
                .attemptNumber(2)
                .startTime(startTime.plusSeconds(2))
                .build();
        attempt2.markAsFailed(exception2, startTime.plusSeconds(3));
        
        // Act
        result.addAttempt(attempt1);
        result.addAttempt(attempt2);
        result.markAsFailed(exception2, startTime.plusSeconds(3));
        
        // Assert
        assertEquals(2, result.getTotalAttempts());
        assertEquals(0, result.getSuccessfulAttemptsCount());
        assertEquals(2, result.getFailedAttemptsCount());
        assertNull(result.getFirstSuccessfulAttempt());
        assertEquals(exception2, result.getFinalException());
    }
    
    @Test
    void shouldHandleMixedSuccessfulAndFailedAttempts() {
        // Arrange
        RuntimeException exception = new RuntimeException("Failure");
        String successResult = "Success";
        
        RetryAttempt failedAttempt = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(startTime)
                .build();
        failedAttempt.markAsFailed(exception, startTime.plusSeconds(1));
        
        RetryAttempt successfulAttempt = RetryAttempt.builder()
                .attemptNumber(2)
                .startTime(startTime.plusSeconds(2))
                .build();
        successfulAttempt.markAsSuccessful(startTime.plusSeconds(3));
        
        // Act
        result.addAttempt(failedAttempt);
        result.addAttempt(successfulAttempt);
        result.markAsSuccessful(successResult, startTime.plusSeconds(3));
        
        // Assert
        assertEquals(2, result.getTotalAttempts());
        assertEquals(1, result.getSuccessfulAttemptsCount());
        assertEquals(1, result.getFailedAttemptsCount());
        assertEquals(successfulAttempt, result.getFirstSuccessfulAttempt());
        assertEquals(successfulAttempt, result.getLastAttempt());
        assertEquals(successResult, result.getLastSuccessfulResult());
    }
}
