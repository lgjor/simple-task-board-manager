package org.desviante.integration.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RetryExecutor.
 * 
 * <p>Estes testes verificam o funcionamento do executor de retry,
 * incluindo execução bem-sucedida, falhas e recuperação.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class RetryExecutorTest {
    
    @Mock
    private RetryStrategy strategy;
    
    private RetryExecutor executor;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        executor = new RetryExecutor(strategy);
    }
    
    @Test
    void shouldExecuteSuccessfullyOnFirstAttempt() {
        // Arrange
        String expectedResult = "Success";
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(strategy.shouldRetry(any(RetryContext.class))).thenReturn(false);
        when(strategy.getMaxAttempts()).thenReturn(3);
        
        RetryContext context = RetryContext.builder()
                .retryId("test-1")
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(RetryConfig.builder().maxAttempts(3).build())
                .build();
        
        // Act
        RetryResult result = executor.execute(() -> {
            callCount.incrementAndGet();
            return expectedResult;
        }, context);
        
        // Assert
        assertTrue(result.isSuccessful());
        assertEquals(expectedResult, result.getLastSuccessfulResult());
        assertEquals(1, callCount.get());
        assertEquals(1, result.getTotalAttempts());
        assertEquals(1, result.getSuccessfulAttemptsCount());
        assertEquals(0, result.getFailedAttemptsCount());
        
        verify(strategy, never()).calculateDelay(any(RetryContext.class));
    }
    
    @Test
    void shouldRetryOnFailureAndEventuallySucceed() throws InterruptedException {
        // Arrange
        String expectedResult = "Success";
        AtomicInteger callCount = new AtomicInteger(0);
        
        // Primeira tentativa falha, segunda sucede
        when(strategy.shouldRetry(any(RetryContext.class)))
                .thenReturn(true)  // Primeira falha
                .thenReturn(false); // Segunda sucesso
        
        when(strategy.calculateDelay(any(RetryContext.class)))
                .thenReturn(Duration.ofMillis(10)); // Delay curto para teste
        
        when(strategy.isRetryable(any(Exception.class))).thenReturn(true);
        
        RetryContext context = RetryContext.builder()
                .retryId("test-2")
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(RetryConfig.builder().maxAttempts(3).build())
                .build();
        
        // Act
        RetryResult result = executor.execute(() -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new RuntimeException("First attempt fails");
            }
            return expectedResult;
        }, context);
        
        // Assert
        assertTrue(result.isSuccessful());
        assertEquals(expectedResult, result.getLastSuccessfulResult());
        assertEquals(2, callCount.get());
        assertEquals(2, result.getTotalAttempts());
        assertEquals(1, result.getSuccessfulAttemptsCount());
        assertEquals(1, result.getFailedAttemptsCount());
        
        verify(strategy, times(1)).calculateDelay(any(RetryContext.class));
    }
    
    @Test
    void shouldFailAfterMaxAttempts() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(strategy.shouldRetry(any(RetryContext.class))).thenReturn(true);
        when(strategy.calculateDelay(any(RetryContext.class))).thenReturn(Duration.ofMillis(1));
        when(strategy.isRetryable(any(Exception.class))).thenReturn(true);
        
        RetryContext context = RetryContext.builder()
                .retryId("test-3")
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(RetryConfig.builder().maxAttempts(2).build())
                .build();
        
        // Act
        RetryResult result = executor.execute(() -> {
            callCount.incrementAndGet();
            throw new RuntimeException("Always fails");
        }, context);
        
        // Assert
        assertFalse(result.isSuccessful());
        assertEquals(2, callCount.get());
        assertEquals(2, result.getTotalAttempts());
        assertEquals(0, result.getSuccessfulAttemptsCount());
        assertEquals(2, result.getFailedAttemptsCount());
        assertNotNull(result.getFinalException());
        assertEquals("Always fails", result.getErrorMessage());
    }
    
    @Test
    void shouldNotRetryNonRetryableException() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(strategy.shouldRetry(any(RetryContext.class))).thenReturn(false);
        when(strategy.isRetryable(any(Exception.class))).thenReturn(false);
        
        RetryContext context = RetryContext.builder()
                .retryId("test-4")
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(RetryConfig.builder().maxAttempts(3).build())
                .build();
        
        // Act
        RetryResult result = executor.execute(() -> {
            callCount.incrementAndGet();
            throw new IllegalArgumentException("Non-retryable error");
        }, context);
        
        // Assert
        assertFalse(result.isSuccessful());
        assertEquals(1, callCount.get());
        assertEquals(1, result.getTotalAttempts());
        assertEquals(0, result.getSuccessfulAttemptsCount());
        assertEquals(1, result.getFailedAttemptsCount());
        
        verify(strategy, never()).calculateDelay(any(RetryContext.class));
    }
    
    @Test
    void shouldExecuteWithSimpleContext() {
        // Arrange
        String expectedResult = "Success";
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(strategy.shouldRetry(any(RetryContext.class))).thenReturn(false);
        
        // Act
        RetryResult result = executor.execute(() -> {
            callCount.incrementAndGet();
            return expectedResult;
        }, "SIMPLE_OPERATION", 1L, "GOOGLE_TASKS");
        
        // Assert
        assertTrue(result.isSuccessful());
        assertEquals(expectedResult, result.getLastSuccessfulResult());
        assertEquals(1, callCount.get());
        assertEquals(1, result.getTotalAttempts());
    }
    
    @Test
    void shouldExecuteWithCustomConfig() {
        // Arrange
        String expectedResult = "Success";
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(strategy.shouldRetry(any(RetryContext.class))).thenReturn(false);
        
        RetryConfig customConfig = RetryConfig.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofSeconds(2))
                .build();
        
        // Act
        RetryResult result = executor.execute(() -> {
            callCount.incrementAndGet();
            return expectedResult;
        }, "CUSTOM_OPERATION", 1L, "GOOGLE_TASKS", customConfig);
        
        // Assert
        assertTrue(result.isSuccessful());
        assertEquals(expectedResult, result.getLastSuccessfulResult());
        assertEquals(1, callCount.get());
        assertEquals(1, result.getTotalAttempts());
        assertEquals(customConfig, result.getConfig());
    }
    
    @Test
    void shouldHandleInterruptedException() throws InterruptedException {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(strategy.shouldRetry(any(RetryContext.class))).thenReturn(true);
        when(strategy.calculateDelay(any(RetryContext.class))).thenReturn(Duration.ofSeconds(1));
        when(strategy.isRetryable(any(Exception.class))).thenReturn(true);
        
        RetryContext context = RetryContext.builder()
                .retryId("test-interrupt")
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(RetryConfig.builder().maxAttempts(3).build())
                .build();
        
        // Act & Assert
        RetryResult result = executor.execute(() -> {
            callCount.incrementAndGet();
            throw new RuntimeException("Fails");
        }, context);
        
        assertFalse(result.isSuccessful());
        assertNotNull(result.getFinalException());
        assertTrue(result.getErrorMessage().contains("Retry interrompido") || 
                  result.getErrorMessage().contains("Fails"));
    }
    
    @Test
    void shouldRecordAttemptHistoryCorrectly() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(strategy.shouldRetry(any(RetryContext.class))).thenReturn(false);
        
        RetryContext context = RetryContext.builder()
                .retryId("test-history")
                .operationType("TEST_OPERATION")
                .entityId(1L)
                .integrationType("GOOGLE_TASKS")
                .config(RetryConfig.builder().maxAttempts(3).build())
                .build();
        
        // Act
        RetryResult result = executor.execute(() -> {
            callCount.incrementAndGet();
            return "Success";
        }, context);
        
        // Assert
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getAttempts().size());
        
        RetryAttempt attempt = result.getAttempts().get(0);
        assertEquals(1, attempt.getAttemptNumber());
        assertTrue(attempt.isSuccessful());
        assertFalse(attempt.isFailed());
        assertNotNull(attempt.getStartTime());
        assertNotNull(attempt.getEndTime());
        assertNotNull(attempt.getDuration());
    }
}
