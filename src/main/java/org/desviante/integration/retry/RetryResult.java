package org.desviante.integration.retry;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resultado de uma operação de retry.
 * 
 * <p>Esta classe encapsula o resultado final de uma operação de retry,
 * incluindo sucesso/falha, histórico de tentativas e estatísticas.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
public class RetryResult {
    
    /**
     * Indica se a operação foi bem-sucedida.
     */
    @Builder.Default
    private boolean successful = false;
    
    /**
     * Número total de tentativas realizadas.
     */
    @Builder.Default
    private int totalAttempts = 0;
    
    /**
     * Data e hora de início da operação.
     */
    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();
    
    /**
     * Data e hora de finalização da operação.
     */
    private LocalDateTime endTime;
    
    /**
     * Duração total da operação.
     */
    private Duration totalDuration;
    
    /**
     * Exceção final que causou a falha (se aplicável).
     */
    private Exception finalException;
    
    /**
     * Mensagem de erro final.
     */
    private String errorMessage;
    
    /**
     * Histórico de todas as tentativas.
     */
    @Builder.Default
    private List<RetryAttempt> attempts = new java.util.ArrayList<>();
    
    /**
     * Configuração utilizada para o retry.
     */
    private RetryConfig config;
    
    /**
     * Resultado da última tentativa bem-sucedida.
     */
    private Object lastSuccessfulResult;
    
    /**
     * Metadados adicionais sobre o resultado.
     */
    @Builder.Default
    private java.util.Map<String, Object> metadata = new java.util.HashMap<>();
    
    /**
     * Marca o resultado como bem-sucedido.
     * 
     * @param result resultado da operação
     * @param endTime tempo de finalização
     */
    public void markAsSuccessful(Object result, LocalDateTime endTime) {
        this.successful = true;
        this.endTime = endTime;
        this.totalDuration = Duration.between(startTime, endTime);
        this.lastSuccessfulResult = result;
        this.finalException = null;
        this.errorMessage = null;
    }
    
    /**
     * Marca o resultado como falha.
     * 
     * @param exception exceção final
     * @param endTime tempo de finalização
     */
    public void markAsFailed(Exception exception, LocalDateTime endTime) {
        this.successful = false;
        this.endTime = endTime;
        this.totalDuration = Duration.between(startTime, endTime);
        this.finalException = exception;
        this.errorMessage = exception != null ? exception.getMessage() : "Erro desconhecido";
        this.lastSuccessfulResult = null;
    }
    
    /**
     * Adiciona uma tentativa ao histórico.
     * 
     * @param attempt tentativa a ser adicionada
     */
    public void addAttempt(RetryAttempt attempt) {
        this.attempts.add(attempt);
        this.totalAttempts = attempts.size();
    }
    
    /**
     * Obtém a última tentativa do histórico.
     * 
     * @return última tentativa ou null se não houver
     */
    public RetryAttempt getLastAttempt() {
        return attempts.isEmpty() ? null : attempts.get(attempts.size() - 1);
    }
    
    /**
     * Obtém a primeira tentativa bem-sucedida.
     * 
     * @return primeira tentativa bem-sucedida ou null se não houver
     */
    public RetryAttempt getFirstSuccessfulAttempt() {
        return attempts.stream()
                .filter(RetryAttempt::isSuccessful)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Obtém o número de tentativas bem-sucedidas.
     * 
     * @return número de tentativas bem-sucedidas
     */
    public long getSuccessfulAttemptsCount() {
        return attempts.stream()
                .filter(RetryAttempt::isSuccessful)
                .count();
    }
    
    /**
     * Obtém o número de tentativas que falharam.
     * 
     * @return número de tentativas que falharam
     */
    public long getFailedAttemptsCount() {
        return attempts.stream()
                .filter(RetryAttempt::isFailed)
                .count();
    }
    
    /**
     * Calcula a duração total em milissegundos.
     * 
     * @return duração em milissegundos
     */
    public long getTotalDurationMillis() {
        return totalDuration != null ? totalDuration.toMillis() : 0;
    }
    
    /**
     * Calcula a duração média das tentativas em milissegundos.
     * 
     * @return duração média em milissegundos
     */
    public double getAverageAttemptDurationMillis() {
        if (attempts.isEmpty()) {
            return 0.0;
        }
        
        long totalMillis = attempts.stream()
                .mapToLong(RetryAttempt::getDurationMillis)
                .sum();
        
        return (double) totalMillis / attempts.size();
    }
    
    /**
     * Verifica se o resultado ainda está em andamento.
     * 
     * @return true se ainda está em andamento
     */
    public boolean isInProgress() {
        return endTime == null;
    }
    
    /**
     * Adiciona metadado ao resultado.
     * 
     * @param key chave do metadado
     * @param value valor do metadado
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    /**
     * Obtém metadado do resultado.
     * 
     * @param key chave do metadado
     * @return valor do metadado ou null se não encontrado
     */
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    /**
     * Obtém uma descrição resumida do resultado.
     * 
     * @return descrição do resultado
     */
    public String getSummary() {
        if (isInProgress()) {
            return String.format("Retry em andamento: %d tentativas realizadas", totalAttempts);
        } else if (successful) {
            return String.format("Retry bem-sucedido: %d tentativas em %dms", 
                    totalAttempts, getTotalDurationMillis());
        } else {
            return String.format("Retry falhou após %d tentativas em %dms: %s", 
                    totalAttempts, getTotalDurationMillis(), errorMessage);
        }
    }
}
