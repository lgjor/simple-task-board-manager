package org.desviante.integration.retry;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contexto de informações para operações de retry.
 * 
 * <p>Esta classe encapsula todas as informações necessárias para
 * controlar e monitorar operações de retry, incluindo histórico
 * de tentativas, configurações e metadados.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RetryStrategy
 * @see RetryResult
 */
@Data
@Builder
public class RetryContext {
    
    /**
     * Identificador único do contexto de retry.
     */
    private String retryId;
    
    /**
     * Número da tentativa atual (começando em 1).
     */
    @Builder.Default
    private int currentAttempt = 1;
    
    /**
     * Número máximo de tentativas permitidas.
     */
    @Builder.Default
    private int maxAttempts = 3;
    
    /**
     * Data e hora de início do retry.
     */
    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();
    
    /**
     * Data e hora da última tentativa.
     */
    private LocalDateTime lastAttemptTime;
    
    /**
     * Histórico de tentativas realizadas.
     */
    @Builder.Default
    private List<RetryAttempt> attempts = new ArrayList<>();
    
    /**
     * Tipo de operação sendo executada.
     */
    private String operationType;
    
    /**
     * ID da entidade relacionada (ex: card ID).
     */
    private Long entityId;
    
    /**
     * Tipo de integração (ex: GOOGLE_TASKS, CALENDAR).
     */
    private String integrationType;
    
    /**
     * Configurações específicas do retry.
     */
    @Builder.Default
    private RetryConfig config = RetryConfig.builder().build();
    
    /**
     * Metadados adicionais para o contexto.
     */
    @Builder.Default
    private java.util.Map<String, Object> metadata = new java.util.HashMap<>();
    
    /**
     * Verifica se ainda pode tentar novamente.
     * 
     * @return true se currentAttempt < maxAttempts
     */
    public boolean canRetry() {
        return currentAttempt < maxAttempts;
    }
    
    /**
     * Incrementa o número da tentativa atual.
     */
    public void incrementAttempt() {
        this.currentAttempt++;
        this.lastAttemptTime = LocalDateTime.now();
    }
    
    /**
     * Adiciona uma tentativa ao histórico.
     * 
     * @param attempt tentativa a ser adicionada
     */
    public void addAttempt(RetryAttempt attempt) {
        this.attempts.add(attempt);
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
     * Verifica se há tentativas no histórico.
     * 
     * @return true se há tentativas
     */
    public boolean hasAttempts() {
        return !attempts.isEmpty();
    }
    
    /**
     * Obtém o número total de tentativas realizadas.
     * 
     * @return número de tentativas
     */
    public int getTotalAttempts() {
        return attempts.size();
    }
    
    /**
     * Calcula o tempo decorrido desde o início.
     * 
     * @return duração em milissegundos
     */
    public long getElapsedTimeMillis() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
    
    /**
     * Verifica se excedeu o tempo limite máximo.
     * 
     * @return true se excedeu o tempo limite
     */
    public boolean hasExceededTimeLimit() {
        if (config.getMaxRetryDuration() == null) {
            return false;
        }
        return getElapsedTimeMillis() > config.getMaxRetryDuration().toMillis();
    }
    
    /**
     * Adiciona metadado ao contexto.
     * 
     * @param key chave do metadado
     * @param value valor do metadado
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    /**
     * Obtém metadado do contexto.
     * 
     * @param key chave do metadado
     * @return valor do metadado ou null se não encontrado
     */
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    /**
     * Obtém metadado tipado do contexto.
     * 
     * @param key chave do metadado
     * @param type tipo esperado
     * @return valor do metadado tipado ou null se não encontrado
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = this.metadata.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
}
