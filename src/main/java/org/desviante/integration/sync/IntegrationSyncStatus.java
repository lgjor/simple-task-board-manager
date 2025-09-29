package org.desviante.integration.sync;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa o status de sincronização entre um card local e um sistema externo.
 * 
 * <p>Esta entidade rastreia o estado da sincronização entre cards do sistema
 * local e sistemas externos (Google Tasks, Calendário), permitindo controle
 * de consistência, retry automático e auditoria de operações.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pelo rastreamento de sincronização</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos de integração</li>
 *   <li><strong>LSP:</strong> Implementa contratos consistentes</li>
 *   <li><strong>ISP:</strong> Interface específica para sincronização</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (LocalDateTime, enums)</li>
 * </ul>
 * 
 * <p><strong>Estados de Sincronização:</strong></p>
 * <ul>
 *   <li><strong>SYNCED:</strong> Sincronização bem-sucedida</li>
 *   <li><strong>PENDING:</strong> Aguardando sincronização</li>
 *   <li><strong>ERROR:</strong> Falha na sincronização</li>
 *   <li><strong>RETRY:</strong> Tentativa de retry em andamento</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see IntegrationType
 * @see SyncStatus
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationSyncStatus {
    
    /**
     * Identificador único do registro de sincronização.
     */
    private Long id;
    
    /**
     * ID do card local sendo sincronizado.
     */
    private Long cardId;
    
    /**
     * Tipo de integração (GOOGLE_TASKS, CALENDAR).
     */
    private IntegrationType integrationType;
    
    /**
     * ID da entidade no sistema externo.
     * 
     * <p>Pode ser o Google Task ID, Calendar Event ID, etc.
     * Null se ainda não foi sincronizado com sucesso.</p>
     */
    private String externalId;
    
    /**
     * Status atual da sincronização.
     */
    private SyncStatus syncStatus;
    
    /**
     * Data e hora da última sincronização.
     * 
     * <p>Null se nunca foi sincronizado.</p>
     */
    private LocalDateTime lastSyncDate;
    
    /**
     * Mensagem de erro da última falha.
     * 
     * <p>Null se não houve erro ou se a última operação foi bem-sucedida.</p>
     */
    private String errorMessage;
    
    /**
     * Número de tentativas de retry realizadas.
     * 
     * <p>Incrementado a cada tentativa de retry após falha.</p>
     */
    @Builder.Default
    private Integer retryCount = 0;
    
    /**
     * Número máximo de tentativas de retry permitidas.
     * 
     * <p>Quando retryCount atinge este valor, a sincronização é marcada
     * como ERROR permanente.</p>
     */
    @Builder.Default
    private Integer maxRetries = 3;
    
    /**
     * Data e hora de criação do registro.
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Data e hora da última atualização do registro.
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * Verifica se a sincronização está bem-sucedida.
     * 
     * @return true se o status é SYNCED
     */
    public boolean isSynced() {
        return SyncStatus.SYNCED.equals(syncStatus);
    }
    
    /**
     * Verifica se a sincronização está pendente.
     * 
     * @return true se o status é PENDING
     */
    public boolean isPending() {
        return SyncStatus.PENDING.equals(syncStatus);
    }
    
    /**
     * Verifica se a sincronização falhou.
     * 
     * @return true se o status é ERROR
     */
    public boolean hasError() {
        return SyncStatus.ERROR.equals(syncStatus);
    }
    
    /**
     * Verifica se está em processo de retry.
     * 
     * @return true se o status é RETRY
     */
    public boolean isRetrying() {
        return SyncStatus.RETRY.equals(syncStatus);
    }
    
    /**
     * Verifica se ainda pode tentar retry.
     * 
     * @return true se retryCount < maxRetries
     */
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    /**
     * Verifica se atingiu o limite máximo de tentativas.
     * 
     * @return true se retryCount >= maxRetries
     */
    public boolean hasReachedMaxRetries() {
        return retryCount >= maxRetries;
    }
    
    /**
     * Incrementa o contador de tentativas.
     * 
     * @return o novo valor do contador
     */
    public int incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
        return this.retryCount;
    }
    
    /**
     * Marca a sincronização como bem-sucedida.
     * 
     * @param externalId ID da entidade no sistema externo
     */
    public void markAsSynced(String externalId) {
        this.syncStatus = SyncStatus.SYNCED;
        this.externalId = externalId;
        this.lastSyncDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.errorMessage = null; // Limpar erro anterior
    }
    
    /**
     * Marca a sincronização como pendente.
     */
    public void markAsPending() {
        this.syncStatus = SyncStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca a sincronização como erro.
     * 
     * @param errorMessage mensagem de erro
     */
    public void markAsError(String errorMessage) {
        this.syncStatus = SyncStatus.ERROR;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca a sincronização como retry.
     */
    public void markAsRetry() {
        this.syncStatus = SyncStatus.RETRY;
        this.updatedAt = LocalDateTime.now();
        incrementRetryCount();
    }
    
    /**
     * Reseta o contador de tentativas.
     */
    public void resetRetryCount() {
        this.retryCount = 0;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica se o registro está ativo (não é ERROR permanente).
     * 
     * @return true se não é ERROR ou se ainda pode tentar retry
     */
    public boolean isActive() {
        return !hasError() || canRetry();
    }
    
    /**
     * Obtém uma descrição resumida do status.
     * 
     * @return string descritiva do status atual
     */
    public String getStatusDescription() {
        if (isSynced()) {
            return "Sincronizado com " + integrationType + " (ID: " + externalId + ")";
        } else if (isPending()) {
            return "Aguardando sincronização com " + integrationType;
        } else if (isRetrying()) {
            return "Tentativa " + retryCount + "/" + maxRetries + " com " + integrationType;
        } else if (hasError()) {
            return "Erro na sincronização com " + integrationType + ": " + errorMessage;
        }
        return "Status desconhecido: " + syncStatus;
    }
}
