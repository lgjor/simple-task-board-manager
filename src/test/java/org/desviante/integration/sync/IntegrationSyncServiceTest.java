package org.desviante.integration.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para IntegrationSyncService.
 * 
 * <p>Estes testes verificam o funcionamento do serviço de sincronização,
 * incluindo criação, atualização, retry e estatísticas.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class IntegrationSyncServiceTest {
    
    @Mock
    private IntegrationSyncRepository repository;
    
    private IntegrationSyncService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new IntegrationSyncService(repository);
    }
    
    @Test
    void shouldCreateSyncStatus() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.empty());
        
        IntegrationSyncStatus expectedStatus = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.PENDING)
                .build();
        
        when(repository.save(any(IntegrationSyncStatus.class)))
                .thenReturn(expectedStatus);
        
        // Act
        IntegrationSyncStatus result = service.createSyncStatus(cardId, integrationType);
        
        // Assert
        assertNotNull(result);
        assertEquals(cardId, result.getCardId());
        assertEquals(integrationType, result.getIntegrationType());
        assertEquals(SyncStatus.PENDING, result.getSyncStatus());
        
        verify(repository, times(1)).findByCardIdAndType(cardId, integrationType);
        verify(repository, times(1)).save(any(IntegrationSyncStatus.class));
    }
    
    @Test
    void shouldNotCreateDuplicateSyncStatus() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        
        IntegrationSyncStatus existingStatus = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.SYNCED)
                .build();
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.of(existingStatus));
        
        // Act
        IntegrationSyncStatus result = service.createSyncStatus(cardId, integrationType);
        
        // Assert
        assertEquals(existingStatus, result);
        verify(repository, never()).save(any(IntegrationSyncStatus.class));
    }
    
    @Test
    void shouldMarkAsSynced() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        String externalId = "external-123";
        
        IntegrationSyncStatus status = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.PENDING)
                .build();
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.of(status));
        when(repository.save(any(IntegrationSyncStatus.class)))
                .thenReturn(status);
        
        // Act
        service.markAsSynced(cardId, integrationType, externalId);
        
        // Assert
        assertEquals(SyncStatus.SYNCED, status.getSyncStatus());
        assertEquals(externalId, status.getExternalId());
        assertNotNull(status.getLastSyncDate());
        
        verify(repository, times(1)).save(status);
    }
    
    @Test
    void shouldMarkAsError() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        String errorMessage = "API error";
        
        IntegrationSyncStatus status = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.PENDING)
                .build();
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.of(status));
        when(repository.save(any(IntegrationSyncStatus.class)))
                .thenReturn(status);
        
        // Act
        service.markAsError(cardId, integrationType, errorMessage);
        
        // Assert
        assertEquals(SyncStatus.ERROR, status.getSyncStatus());
        assertEquals(errorMessage, status.getErrorMessage());
        
        verify(repository, times(1)).save(status);
    }
    
    @Test
    void shouldMarkForRetry() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        
        IntegrationSyncStatus status = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.of(status));
        when(repository.save(any(IntegrationSyncStatus.class)))
                .thenReturn(status);
        
        // Act
        boolean result = service.markForRetry(cardId, integrationType);
        
        // Assert
        assertTrue(result);
        assertEquals(SyncStatus.RETRY, status.getSyncStatus());
        assertEquals(1, status.getRetryCount());
        
        verify(repository, times(1)).save(status);
    }
    
    @Test
    void shouldNotAllowRetryWhenMaxRetriesReached() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        
        IntegrationSyncStatus status = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.PENDING)
                .retryCount(3)
                .maxRetries(3)
                .build();
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.of(status));
        when(repository.save(any(IntegrationSyncStatus.class)))
                .thenReturn(status);
        
        // Act
        boolean result = service.markForRetry(cardId, integrationType);
        
        // Assert
        assertFalse(result);
        assertEquals(SyncStatus.ERROR, status.getSyncStatus());
        assertEquals("Limite de tentativas atingido", status.getErrorMessage());
        
        verify(repository, times(1)).save(status);
    }
    
    @Test
    void shouldGetSyncStatus() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        
        IntegrationSyncStatus expectedStatus = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.SYNCED)
                .build();
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.of(expectedStatus));
        
        // Act
        Optional<IntegrationSyncStatus> result = service.getSyncStatus(cardId, integrationType);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedStatus, result.get());
    }
    
    @Test
    void shouldGetSyncStatusesForCard() {
        // Arrange
        Long cardId = 1L;
        
        List<IntegrationSyncStatus> expectedStatuses = Arrays.asList(
                IntegrationSyncStatus.builder()
                        .id(1L)
                        .cardId(cardId)
                        .integrationType(IntegrationType.GOOGLE_TASKS)
                        .syncStatus(SyncStatus.SYNCED)
                        .build(),
                IntegrationSyncStatus.builder()
                        .id(2L)
                        .cardId(cardId)
                        .integrationType(IntegrationType.CALENDAR)
                        .syncStatus(SyncStatus.PENDING)
                        .build()
        );
        
        when(repository.findByCardId(cardId))
                .thenReturn(expectedStatuses);
        
        // Act
        List<IntegrationSyncStatus> result = service.getSyncStatusesForCard(cardId);
        
        // Assert
        assertEquals(expectedStatuses.size(), result.size());
        assertEquals(expectedStatuses, result);
    }
    
    @Test
    void shouldGetRetryableStatuses() {
        // Arrange
        List<IntegrationSyncStatus> expectedStatuses = Arrays.asList(
                IntegrationSyncStatus.builder()
                        .id(1L)
                        .cardId(1L)
                        .integrationType(IntegrationType.GOOGLE_TASKS)
                        .syncStatus(SyncStatus.PENDING)
                        .build()
        );
        
        when(repository.findRetryableStatuses())
                .thenReturn(expectedStatuses);
        
        // Act
        List<IntegrationSyncStatus> result = service.getRetryableStatuses();
        
        // Assert
        assertEquals(expectedStatuses, result);
    }
    
    @Test
    void shouldRemoveSyncStatusesForCard() {
        // Arrange
        Long cardId = 1L;
        
        // Act
        service.removeSyncStatusesForCard(cardId);
        
        // Assert
        verify(repository, times(1)).deleteByCardId(cardId);
    }
    
    @Test
    void shouldCheckIfCardIsSynced() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        
        IntegrationSyncStatus syncedStatus = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.SYNCED)
                .build();
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.of(syncedStatus));
        
        // Act
        boolean result = service.isCardSynced(cardId, integrationType);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void shouldCheckIfCardHasSyncErrors() {
        // Arrange
        Long cardId = 1L;
        
        List<IntegrationSyncStatus> statuses = Arrays.asList(
                IntegrationSyncStatus.builder()
                        .id(1L)
                        .cardId(cardId)
                        .integrationType(IntegrationType.GOOGLE_TASKS)
                        .syncStatus(SyncStatus.ERROR)
                        .build(),
                IntegrationSyncStatus.builder()
                        .id(2L)
                        .cardId(cardId)
                        .integrationType(IntegrationType.CALENDAR)
                        .syncStatus(SyncStatus.SYNCED)
                        .build()
        );
        
        when(repository.findByCardId(cardId))
                .thenReturn(statuses);
        
        // Act
        boolean result = service.hasSyncErrors(cardId);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void shouldGetExternalId() {
        // Arrange
        Long cardId = 1L;
        IntegrationType integrationType = IntegrationType.GOOGLE_TASKS;
        String externalId = "external-123";
        
        IntegrationSyncStatus status = IntegrationSyncStatus.builder()
                .id(1L)
                .cardId(cardId)
                .integrationType(integrationType)
                .externalId(externalId)
                .syncStatus(SyncStatus.SYNCED)
                .build();
        
        when(repository.findByCardIdAndType(cardId, integrationType))
                .thenReturn(Optional.of(status));
        
        // Act
        Optional<String> result = service.getExternalId(cardId, integrationType);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(externalId, result.get());
    }
    
    @Test
    void shouldGetStatistics() {
        // Arrange
        when(repository.countBySyncStatus(SyncStatus.SYNCED)).thenReturn(10L);
        when(repository.countBySyncStatus(SyncStatus.PENDING)).thenReturn(5L);
        when(repository.countBySyncStatus(SyncStatus.ERROR)).thenReturn(2L);
        when(repository.countBySyncStatus(SyncStatus.RETRY)).thenReturn(1L);
        when(repository.countByIntegrationType(IntegrationType.GOOGLE_TASKS)).thenReturn(12L);
        when(repository.countByIntegrationType(IntegrationType.CALENDAR)).thenReturn(6L);
        
        // Act
        SyncStatistics stats = service.getStatistics();
        
        // Assert
        assertNotNull(stats);
        assertEquals(18L, stats.getTotalSyncs());
        assertEquals(10L, stats.getSyncedCount());
        assertEquals(5L, stats.getPendingCount());
        assertEquals(2L, stats.getErrorCount());
        assertEquals(1L, stats.getRetryCount());
        assertEquals(12L, stats.getGoogleTasksCount());
        assertEquals(6L, stats.getCalendarCount());
        
        assertEquals(55.56, stats.getSuccessRate(), 0.01);
        assertEquals(11.11, stats.getErrorRate(), 0.01);
        assertTrue(stats.hasActiveSyncs());
        assertTrue(stats.hasErrors());
    }
}
