package org.desviante.integration.retry;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Representa uma tentativa individual de retry.
 * 
 * <p>Esta classe encapsula informações sobre uma tentativa específica
 * de retry, incluindo resultado, tempo de execução e detalhes do erro.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
public class RetryAttempt {
    
    /**
     * Número da tentativa (1-based).
     */
    @Builder.Default
    private int attemptNumber = 1;
    
    /**
     * Data e hora em que a tentativa foi iniciada.
     */
    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();
    
    /**
     * Data e hora em que a tentativa foi finalizada.
     */
    private LocalDateTime endTime;
    
    /**
     * Duração da tentativa.
     */
    private Duration duration;
    
    /**
     * Indica se a tentativa foi bem-sucedida.
     */
    @Builder.Default
    private boolean successful = false;
    
    /**
     * Exceção que ocorreu durante a tentativa (se houver).
     */
    private Exception exception;
    
    /**
     * Mensagem de erro da tentativa.
     */
    private String errorMessage;
    
    /**
     * Código de erro específico (se aplicável).
     */
    private String errorCode;
    
    /**
     * Detalhes adicionais sobre a tentativa.
     */
    private String details;
    
    /**
     * Marca a tentativa como bem-sucedida.
     * 
     * @param endTime tempo de finalização
     */
    public void markAsSuccessful(LocalDateTime endTime) {
        this.successful = true;
        this.endTime = endTime;
        this.duration = Duration.between(startTime, endTime);
        this.exception = null;
        this.errorMessage = null;
        this.errorCode = null;
    }
    
    /**
     * Marca a tentativa como falha.
     * 
     * @param exception exceção que ocorreu
     * @param endTime tempo de finalização
     */
    public void markAsFailed(Exception exception, LocalDateTime endTime) {
        this.successful = false;
        this.endTime = endTime;
        this.duration = Duration.between(startTime, endTime);
        this.exception = exception;
        this.errorMessage = exception != null ? exception.getMessage() : "Erro desconhecido";
        this.errorCode = exception != null ? exception.getClass().getSimpleName() : "UNKNOWN";
    }
    
    /**
     * Verifica se a tentativa foi bem-sucedida.
     * 
     * @return true se foi bem-sucedida
     */
    public boolean isSuccessful() {
        return successful;
    }
    
    /**
     * Verifica se a tentativa falhou.
     * 
     * @return true se falhou
     */
    public boolean isFailed() {
        return !successful;
    }
    
    /**
     * Obtém a duração da tentativa em milissegundos.
     * 
     * @return duração em milissegundos
     */
    public long getDurationMillis() {
        return duration != null ? duration.toMillis() : 0;
    }
    
    /**
     * Verifica se a tentativa ainda está em andamento.
     * 
     * @return true se ainda está em andamento
     */
    public boolean isInProgress() {
        return endTime == null;
    }
    
    /**
     * Obtém uma descrição resumida da tentativa.
     * 
     * @return descrição da tentativa
     */
    public String getSummary() {
        if (isInProgress()) {
            return String.format("Tentativa %d em andamento desde %s", attemptNumber, startTime);
        } else if (successful) {
            return String.format("Tentativa %d bem-sucedida em %dms", attemptNumber, getDurationMillis());
        } else {
            return String.format("Tentativa %d falhou: %s", attemptNumber, errorMessage);
        }
    }
}
