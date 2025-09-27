package org.desviante.integration.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciamento de status de sincronização.
 * 
 * <p>Esta classe implementa a lógica de negócio para gerenciamento
 * de status de sincronização entre cards locais e sistemas externos,
 * incluindo criação, atualização, retry automático e auditoria.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela lógica de sincronização</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos de integração</li>
 *   <li><strong>LSP:</strong> Trabalha com qualquer implementação de repository</li>
 *   <li><strong>ISP:</strong> Interface específica para sincronização</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (IntegrationSyncRepository)</li>
 * </ul>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Gerenciamento de status de sincronização</li>
 *   <li>Implementação de lógicas de retry</li>
 *   <li>Auditoria de operações</li>
 *   <li>Validação de consistência</li>
 *   <li>Relatórios de sincronização</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see IntegrationSyncStatus
 * @see IntegrationSyncRepository
 * @see IntegrationType
 * @see SyncStatus
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationSyncService {
    
    private final IntegrationSyncRepository repository;
    
    /**
     * Cria um novo status de sincronização para um card e tipo de integração.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @param maxRetries número máximo de tentativas (opcional)
     * @return status criado
     */
    @Transactional
    public IntegrationSyncStatus createSyncStatus(Long cardId, IntegrationType integrationType, Integer maxRetries) {
        log.debug("Criando status de sincronização para card {} e tipo {}", cardId, integrationType);
        
        // Verificar se já existe um status para este card e tipo
        Optional<IntegrationSyncStatus> existing = repository.findByCardIdAndType(cardId, integrationType);
        if (existing.isPresent()) {
            log.warn("Status de sincronização já existe para card {} e tipo {}", cardId, integrationType);
            return existing.get();
        }
        
        IntegrationSyncStatus status = IntegrationSyncStatus.builder()
                .cardId(cardId)
                .integrationType(integrationType)
                .syncStatus(SyncStatus.PENDING)
                .retryCount(0)
                .maxRetries(maxRetries != null ? maxRetries : 3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        IntegrationSyncStatus saved = repository.save(status);
        log.info("Status de sincronização criado: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * Cria um novo status de sincronização com configurações padrão.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @return status criado
     */
    public IntegrationSyncStatus createSyncStatus(Long cardId, IntegrationType integrationType) {
        return createSyncStatus(cardId, integrationType, null);
    }
    
    /**
     * Marca uma sincronização como bem-sucedida.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @param externalId ID da entidade no sistema externo
     */
    @Transactional
    public void markAsSynced(Long cardId, IntegrationType integrationType, String externalId) {
        log.debug("Marcando sincronização como bem-sucedida para card {} e tipo {}", cardId, integrationType);
        
        Optional<IntegrationSyncStatus> statusOpt = repository.findByCardIdAndType(cardId, integrationType);
        if (statusOpt.isEmpty()) {
            log.warn("Status de sincronização não encontrado para card {} e tipo {}", cardId, integrationType);
            return;
        }
        
        IntegrationSyncStatus status = statusOpt.get();
        status.markAsSynced(externalId);
        repository.save(status);
        
        log.info("Sincronização marcada como bem-sucedida para card {} e tipo {} (external ID: {})", 
                cardId, integrationType, externalId);
    }
    
    /**
     * Marca uma sincronização como erro.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @param errorMessage mensagem de erro
     */
    @Transactional
    public void markAsError(Long cardId, IntegrationType integrationType, String errorMessage) {
        log.debug("Marcando sincronização como erro para card {} e tipo {}", cardId, integrationType);
        
        Optional<IntegrationSyncStatus> statusOpt = repository.findByCardIdAndType(cardId, integrationType);
        if (statusOpt.isEmpty()) {
            log.warn("Status de sincronização não encontrado para card {} e tipo {}", cardId, integrationType);
            return;
        }
        
        IntegrationSyncStatus status = statusOpt.get();
        status.markAsError(errorMessage);
        repository.save(status);
        
        log.info("Sincronização marcada como erro para card {} e tipo {}: {}", 
                cardId, integrationType, errorMessage);
    }
    
    /**
     * Marca uma sincronização para retry.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @return true se pode fazer retry, false se atingiu limite
     */
    @Transactional
    public boolean markForRetry(Long cardId, IntegrationType integrationType) {
        log.debug("Marcando sincronização para retry para card {} e tipo {}", cardId, integrationType);
        
        Optional<IntegrationSyncStatus> statusOpt = repository.findByCardIdAndType(cardId, integrationType);
        if (statusOpt.isEmpty()) {
            log.warn("Status de sincronização não encontrado para card {} e tipo {}", cardId, integrationType);
            return false;
        }
        
        IntegrationSyncStatus status = statusOpt.get();
        
        if (!status.canRetry()) {
            log.warn("Limite de retry atingido para card {} e tipo {}", cardId, integrationType);
            status.markAsError("Limite de tentativas atingido");
            repository.save(status);
            return false;
        }
        
        status.markAsRetry();
        repository.save(status);
        
        log.info("Sincronização marcada para retry para card {} e tipo {} (tentativa {}/{})", 
                cardId, integrationType, status.getRetryCount(), status.getMaxRetries());
        
        return true;
    }
    
    /**
     * Obtém o status de sincronização para um card e tipo específico.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @return Optional contendo o status se encontrado
     */
    public Optional<IntegrationSyncStatus> getSyncStatus(Long cardId, IntegrationType integrationType) {
        return repository.findByCardIdAndType(cardId, integrationType);
    }
    
    /**
     * Obtém todos os status de sincronização para um card.
     * 
     * @param cardId ID do card
     * @return lista de status de sincronização
     */
    public List<IntegrationSyncStatus> getSyncStatusesForCard(Long cardId) {
        return repository.findByCardId(cardId);
    }
    
    /**
     * Obtém todos os status que precisam de retry.
     * 
     * @return lista de status que podem ser tentados novamente
     */
    public List<IntegrationSyncStatus> getRetryableStatuses() {
        return repository.findRetryableStatuses();
    }
    
    /**
     * Obtém todos os status com erro que podem ser tentados novamente.
     * 
     * @return lista de status com erro que ainda podem ser retry
     */
    public List<IntegrationSyncStatus> getErrorStatusesForRetry() {
        return repository.findErrorStatusesForRetry();
    }
    
    /**
     * Remove todos os status de sincronização para um card.
     * 
     * @param cardId ID do card
     */
    @Transactional
    public void removeSyncStatusesForCard(Long cardId) {
        log.info("Removendo todos os status de sincronização para card {}", cardId);
        repository.deleteByCardId(cardId);
    }
    
    /**
     * Obtém estatísticas de sincronização.
     * 
     * @return estatísticas de sincronização
     */
    public SyncStatistics getStatistics() {
        long totalSyncs = repository.countBySyncStatus(SyncStatus.SYNCED) +
                         repository.countBySyncStatus(SyncStatus.PENDING) +
                         repository.countBySyncStatus(SyncStatus.ERROR) +
                         repository.countBySyncStatus(SyncStatus.RETRY);
        
        long syncedCount = repository.countBySyncStatus(SyncStatus.SYNCED);
        long pendingCount = repository.countBySyncStatus(SyncStatus.PENDING);
        long errorCount = repository.countBySyncStatus(SyncStatus.ERROR);
        long retryCount = repository.countBySyncStatus(SyncStatus.RETRY);
        
        long googleTasksCount = repository.countByIntegrationType(IntegrationType.GOOGLE_TASKS);
        long calendarCount = repository.countByIntegrationType(IntegrationType.CALENDAR);
        
        return SyncStatistics.builder()
                .totalSyncs(totalSyncs)
                .syncedCount(syncedCount)
                .pendingCount(pendingCount)
                .errorCount(errorCount)
                .retryCount(retryCount)
                .googleTasksCount(googleTasksCount)
                .calendarCount(calendarCount)
                .generatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Verifica se um card está sincronizado com um tipo específico.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @return true se está sincronizado
     */
    public boolean isCardSynced(Long cardId, IntegrationType integrationType) {
        Optional<IntegrationSyncStatus> status = getSyncStatus(cardId, integrationType);
        return status.isPresent() && status.get().isSynced();
    }
    
    /**
     * Verifica se um card tem erros de sincronização.
     * 
     * @param cardId ID do card
     * @return true se tem erros
     */
    public boolean hasSyncErrors(Long cardId) {
        List<IntegrationSyncStatus> statuses = getSyncStatusesForCard(cardId);
        return statuses.stream().anyMatch(IntegrationSyncStatus::hasError);
    }
    
    /**
     * Obtém o ID externo de um card sincronizado.
     * 
     * @param cardId ID do card
     * @param integrationType tipo de integração
     * @return Optional contendo o ID externo se encontrado
     */
    public Optional<String> getExternalId(Long cardId, IntegrationType integrationType) {
        Optional<IntegrationSyncStatus> status = getSyncStatus(cardId, integrationType);
        return status.map(IntegrationSyncStatus::getExternalId);
    }
}
