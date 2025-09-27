package org.desviante.integration.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RetryAttempt.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class RetryAttemptTest {
    
    private RetryAttempt attempt;
    private LocalDateTime startTime;
    
    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now();
        attempt = RetryAttempt.builder()
                .attemptNumber(1)
                .startTime(startTime)
                .build();
    }
    
    @Test
    void shouldCreateAttemptWithDefaultValues() {
        // Act
        RetryAttempt defaultAttempt = RetryAttempt.builder().build();
        
        // Assert
        assertEquals(1, defaultAttempt.getAttemptNumber());
        assertNotNull(defaultAttempt.getStartTime());
        assertFalse(defaultAttempt.isSuccessful());
        assertNull(defaultAttempt.getEndTime());
        assertNull(defaultAttempt.getDuration());
        assertNull(defaultAttempt.getException());
        assertNull(defaultAttempt.getErrorMessage());
        assertNull(defaultAttempt.getErrorCode());
        assertNull(defaultAttempt.getDetails());
    }
    
    @Test
    void shouldMarkAsSuccessful() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(1);
        
        // Act
        attempt.markAsSuccessful(endTime);
        
        // Assert
        assertTrue(attempt.isSuccessful());
        assertFalse(attempt.isFailed());
        assertEquals(endTime, attempt.getEndTime());
        assertEquals(Duration.ofSeconds(1), attempt.getDuration());
        assertEquals(1000, attempt.getDurationMillis());
        assertNull(attempt.getException());
        assertNull(attempt.getErrorMessage());
        assertNull(attempt.getErrorCode());
    }
    
    @Test
    void shouldMarkAsFailed() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(1);
        RuntimeException exception = new RuntimeException("Test error");
        
        // Act
        attempt.markAsFailed(exception, endTime);
        
        // Assert
        assertFalse(attempt.isSuccessful());
        assertTrue(attempt.isFailed());
        assertEquals(endTime, attempt.getEndTime());
        assertEquals(Duration.ofSeconds(1), attempt.getDuration());
        assertEquals(1000, attempt.getDurationMillis());
        assertEquals(exception, attempt.getException());
        assertEquals("Test error", attempt.getErrorMessage());
        assertEquals("RuntimeException", attempt.getErrorCode());
    }
    
    @Test
    void shouldMarkAsFailedWithNullException() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(1);
        
        // Act
        attempt.markAsFailed(null, endTime);
        
        // Assert
        assertFalse(attempt.isSuccessful());
        assertTrue(attempt.isFailed());
        assertEquals(endTime, attempt.getEndTime());
        assertNull(attempt.getException());
        assertEquals("Erro desconhecido", attempt.getErrorMessage());
        assertEquals("UNKNOWN", attempt.getErrorCode());
    }
    
    @Test
    void shouldDetectInProgressAttempt() {
        // Act & Assert - Attempt recém criado
        assertTrue(attempt.isInProgress());
        
        // Act - Marcar como sucesso
        attempt.markAsSuccessful(LocalDateTime.now());
        
        // Assert
        assertFalse(attempt.isInProgress());
    }
    
    @Test
    void shouldGenerateCorrectSummaryForInProgress() {
        // Act
        String summary = attempt.getSummary();
        
        // Assert
        assertTrue(summary.contains("Tentativa 1 em andamento"));
        assertTrue(summary.contains("desde"));
    }
    
    @Test
    void shouldGenerateCorrectSummaryForSuccessful() {
        // Arrange
        LocalDateTime endTime = startTime.plusNanos(500_000_000); // 500ms
        
        // Act
        attempt.markAsSuccessful(endTime);
        String summary = attempt.getSummary();
        
        // Assert
        assertTrue(summary.contains("Tentativa 1 bem-sucedida"));
        assertTrue(summary.contains("500ms"));
    }
    
    @Test
    void shouldGenerateCorrectSummaryForFailed() {
        // Arrange
        LocalDateTime endTime = startTime.plusNanos(750_000_000); // 750ms
        RuntimeException exception = new RuntimeException("Operation failed");
        
        // Act
        attempt.markAsFailed(exception, endTime);
        String summary = attempt.getSummary();
        
        // Assert
        assertTrue(summary.contains("Tentativa 1 falhou"));
        assertTrue(summary.contains("Operation failed"));
    }
    
    @Test
    void shouldCalculateDurationCorrectly() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(2).plusNanos(500_000_000); // +500ms
        
        // Act
        attempt.markAsSuccessful(endTime);
        
        // Assert
        assertEquals(2500, attempt.getDurationMillis());
    }
    
    @Test
    void shouldPreserveAllFields() {
        // Arrange
        int attemptNumber = 3;
        LocalDateTime customStartTime = LocalDateTime.now().minusMinutes(1);
        String details = "Custom attempt details";
        
        // Act
        RetryAttempt customAttempt = RetryAttempt.builder()
                .attemptNumber(attemptNumber)
                .startTime(customStartTime)
                .details(details)
                .build();
        
        // Assert
        assertEquals(attemptNumber, customAttempt.getAttemptNumber());
        assertEquals(customStartTime, customAttempt.getStartTime());
        assertEquals(details, customAttempt.getDetails());
    }
    
    @Test
    void shouldHandleDifferentExceptionTypes() {
        // Arrange
        LocalDateTime endTime = startTime.plusSeconds(1);
        
        // Test IllegalArgumentException
        IllegalArgumentException illegalArg = new IllegalArgumentException("Invalid argument");
        attempt.markAsFailed(illegalArg, endTime);
        
        // Assert
        assertEquals("IllegalArgumentException", attempt.getErrorCode());
        assertEquals("Invalid argument", attempt.getErrorMessage());
        
        // Test IOException
        java.io.IOException ioException = new java.io.IOException("IO error");
        attempt.markAsFailed(ioException, endTime);
        
        // Assert
        assertEquals("IOException", attempt.getErrorCode());
        assertEquals("IO error", attempt.getErrorMessage());
    }
    
    @Test
    void shouldHandleZeroDuration() {
        // Arrange
        LocalDateTime sameTime = startTime;
        
        // Act
        attempt.markAsSuccessful(sameTime);
        
        // Assert
        assertEquals(0, attempt.getDurationMillis());
        assertEquals(Duration.ZERO, attempt.getDuration());
    }
    
    @Test
    void shouldHandleLongDuration() {
        // Arrange
        LocalDateTime endTime = startTime.plusMinutes(5).plusSeconds(30);
        
        // Act
        attempt.markAsSuccessful(endTime);
        
        // Assert
        assertEquals(330000, attempt.getDurationMillis()); // 5:30 = 330 segundos = 330000 ms
    }
}
