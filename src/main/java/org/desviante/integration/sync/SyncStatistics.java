package org.desviante.integration.sync;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Estatísticas de sincronização do sistema.
 * 
 * <p>Esta classe encapsula métricas e estatísticas sobre as operações
 * de sincronização executadas pelo sistema, permitindo monitoramento
 * e análise de performance das integrações.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
public class SyncStatistics {
    
    /**
     * Total de sincronizações registradas.
     */
    @Builder.Default
    private long totalSyncs = 0;
    
    /**
     * Número de sincronizações bem-sucedidas.
     */
    @Builder.Default
    private long syncedCount = 0;
    
    /**
     * Número de sincronizações pendentes.
     */
    @Builder.Default
    private long pendingCount = 0;
    
    /**
     * Número de sincronizações com erro.
     */
    @Builder.Default
    private long errorCount = 0;
    
    /**
     * Número de sincronizações em retry.
     */
    @Builder.Default
    private long retryCount = 0;
    
    /**
     * Número de sincronizações com Google Tasks.
     */
    @Builder.Default
    private long googleTasksCount = 0;
    
    /**
     * Número de sincronizações com calendário.
     */
    @Builder.Default
    private long calendarCount = 0;
    
    /**
     * Data e hora em que as estatísticas foram geradas.
     */
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();
    
    /**
     * Calcula a taxa de sucesso das sincronizações.
     * 
     * @return taxa de sucesso em percentual (0.0 a 100.0)
     */
    public double getSuccessRate() {
        if (totalSyncs == 0) {
            return 0.0;
        }
        return (double) syncedCount / totalSyncs * 100.0;
    }
    
    /**
     * Calcula a taxa de erro das sincronizações.
     * 
     * @return taxa de erro em percentual (0.0 a 100.0)
     */
    public double getErrorRate() {
        if (totalSyncs == 0) {
            return 0.0;
        }
        return (double) errorCount / totalSyncs * 100.0;
    }
    
    /**
     * Calcula a taxa de sincronizações pendentes.
     * 
     * @return taxa de pendentes em percentual (0.0 a 100.0)
     */
    public double getPendingRate() {
        if (totalSyncs == 0) {
            return 0.0;
        }
        return (double) pendingCount / totalSyncs * 100.0;
    }
    
    /**
     * Verifica se há sincronizações ativas (pendentes ou em retry).
     * 
     * @return true se há sincronizações ativas
     */
    public boolean hasActiveSyncs() {
        return pendingCount > 0 || retryCount > 0;
    }
    
    /**
     * Verifica se há erros de sincronização.
     * 
     * @return true se há erros
     */
    public boolean hasErrors() {
        return errorCount > 0;
    }
    
    /**
     * Obtém o número total de sincronizações ativas.
     * 
     * @return número de sincronizações ativas
     */
    public long getActiveSyncsCount() {
        return pendingCount + retryCount;
    }
    
    /**
     * Obtém o número total de sincronizações finalizadas.
     * 
     * @return número de sincronizações finalizadas
     */
    public long getFinalizedSyncsCount() {
        return syncedCount + errorCount;
    }
    
    /**
     * Cria estatísticas vazias para inicialização.
     * 
     * @return estatísticas zeradas
     */
    public static SyncStatistics empty() {
        return SyncStatistics.builder().build();
    }
}
