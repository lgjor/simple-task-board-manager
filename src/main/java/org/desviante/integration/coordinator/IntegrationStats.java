package org.desviante.integration.coordinator;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Estatísticas de integrações processadas pelo IntegrationCoordinator.
 * 
 * <p>Esta classe encapsula métricas e estatísticas sobre as operações
 * de integração executadas pelo coordenador, permitindo monitoramento
 * e análise de performance do sistema.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
public class IntegrationStats {
    
    /**
     * Total de integrações bem-sucedidas.
     */
    @Builder.Default
    private long successfulIntegrations = 0;
    
    /**
     * Total de integrações que falharam.
     */
    @Builder.Default
    private long failedIntegrations = 0;
    
    /**
     * Total de integrações de agendamento processadas.
     */
    @Builder.Default
    private long scheduledIntegrations = 0;
    
    /**
     * Total de integrações de desagendamento processadas.
     */
    @Builder.Default
    private long unscheduledIntegrations = 0;
    
    /**
     * Total de integrações de atualização processadas.
     */
    @Builder.Default
    private long updateIntegrations = 0;
    
    /**
     * Total de integrações de movimentação processadas.
     */
    @Builder.Default
    private long moveIntegrations = 0;
    
    /**
     * Total de integrações de exclusão processadas.
     */
    @Builder.Default
    private long deleteIntegrations = 0;
    
    /**
     * Data e hora da última integração processada.
     */
    private LocalDateTime lastIntegrationTime;
    
    /**
     * Data e hora de início das estatísticas.
     */
    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();
    
    /**
     * Calcula o total de integrações processadas.
     * 
     * @return total de integrações (sucessos + falhas)
     */
    public long getTotalIntegrations() {
        return successfulIntegrations + failedIntegrations;
    }
    
    /**
     * Calcula a taxa de sucesso das integrações.
     * 
     * @return taxa de sucesso em percentual (0.0 a 100.0)
     */
    public double getSuccessRate() {
        long total = getTotalIntegrations();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulIntegrations / total * 100.0;
    }
    
    /**
     * Calcula a taxa de falha das integrações.
     * 
     * @return taxa de falha em percentual (0.0 a 100.0)
     */
    public double getFailureRate() {
        return 100.0 - getSuccessRate();
    }
    
    /**
     * Verifica se há integrações processadas.
     * 
     * @return true se pelo menos uma integração foi processada
     */
    public boolean hasIntegrations() {
        return getTotalIntegrations() > 0;
    }
    
    /**
     * Obtém o tempo decorrido desde o início das estatísticas.
     * 
     * @return tempo decorrido em milissegundos
     */
    public long getElapsedTimeMillis() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
    
    /**
     * Calcula a média de integrações por minuto.
     * 
     * @return média de integrações por minuto
     */
    public double getIntegrationsPerMinute() {
        long elapsedMillis = getElapsedTimeMillis();
        if (elapsedMillis == 0) {
            return 0.0;
        }
        return (double) getTotalIntegrations() / (elapsedMillis / 60000.0);
    }
    
    /**
     * Cria uma cópia das estatísticas atuais.
     * 
     * @return nova instância com os mesmos valores
     */
    public IntegrationStats copy() {
        return IntegrationStats.builder()
                .successfulIntegrations(this.successfulIntegrations)
                .failedIntegrations(this.failedIntegrations)
                .scheduledIntegrations(this.scheduledIntegrations)
                .unscheduledIntegrations(this.unscheduledIntegrations)
                .updateIntegrations(this.updateIntegrations)
                .moveIntegrations(this.moveIntegrations)
                .deleteIntegrations(this.deleteIntegrations)
                .lastIntegrationTime(this.lastIntegrationTime)
                .startTime(this.startTime)
                .build();
    }
    
    /**
     * Cria estatísticas vazias para inicialização.
     * 
     * @return estatísticas zeradas
     */
    public static IntegrationStats empty() {
        return IntegrationStats.builder().build();
    }
}
