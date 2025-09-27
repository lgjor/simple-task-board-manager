package org.desviante.integration.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Executor de operações com retry automático.
 * 
 * <p>Esta classe implementa a lógica de execução de operações com
 * retry automático baseado em estratégias configuráveis, fornecendo
 * controle completo sobre tentativas, delays e tratamento de erros.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela execução de retry</li>
 *   <li><strong>OCP:</strong> Extensível através de novas estratégias</li>
 *   <li><strong>LSP:</strong> Trabalha com qualquer implementação de RetryStrategy</li>
 *   <li><strong>ISP:</strong> Interface específica para execução de retry</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (RetryStrategy, Supplier)</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RetryStrategy
 * @see RetryContext
 * @see RetryResult
 */
@RequiredArgsConstructor
@Slf4j
public class RetryExecutor {
    
    private final RetryStrategy strategy;
    
    /**
     * Executa uma operação com retry automático.
     * 
     * @param operation operação a ser executada
     * @param context contexto de retry
     * @return resultado da operação
     */
    public RetryResult execute(Supplier<Object> operation, RetryContext context) {
        log.debug("Iniciando execução com retry para operação: {} (ID: {})", 
                 context.getOperationType(), context.getRetryId());
        
        RetryResult result = RetryResult.builder()
                .startTime(context.getStartTime())
                .config(context.getConfig())
                .build();
        
        while (context.canRetry() && !result.isSuccessful()) {
            try {
                // Executar a operação
                Object operationResult = executeOperation(operation, context, result);
                
                // Se chegou aqui, a operação foi bem-sucedida
                result.markAsSuccessful(operationResult, LocalDateTime.now());
                log.info("Operação {} executada com sucesso na tentativa {} (ID: {})", 
                        context.getOperationType(), context.getCurrentAttempt(), context.getRetryId());
                break;
                
            } catch (Exception e) {
                // Registrar tentativa falhada
                RetryAttempt attempt = recordFailedAttempt(e, context);
                result.addAttempt(attempt);
                
                log.warn("Tentativa {} falhou para operação {} (ID: {}): {}", 
                        context.getCurrentAttempt(), context.getOperationType(), 
                        context.getRetryId(), e.getMessage());
                
                // Verificar se deve continuar tentando
                if (!strategy.shouldRetry(context)) {
                    log.error("Operação {} falhou definitivamente após {} tentativas (ID: {}): {}", 
                            context.getOperationType(), context.getCurrentAttempt(), 
                            context.getRetryId(), e.getMessage());
                    result.markAsFailed(e, LocalDateTime.now());
                    break;
                }
                
                // Calcular delay para próxima tentativa
                Duration delay = strategy.calculateDelay(context);
                log.debug("Aguardando {} antes da próxima tentativa para operação {} (ID: {})", 
                         delay, context.getOperationType(), context.getRetryId());
                
                // Aguardar delay
                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrompido para operação {} (ID: {})", 
                            context.getOperationType(), context.getRetryId());
                    result.markAsFailed(new RuntimeException("Retry interrompido", ie), LocalDateTime.now());
                    break;
                }
                
                // Incrementar tentativa
                context.incrementAttempt();
            }
        }
        
        // Se esgotaram as tentativas sem sucesso
        if (!result.isSuccessful() && !context.canRetry()) {
            RetryAttempt lastAttempt = result.getLastAttempt();
            Exception finalException = lastAttempt != null ? lastAttempt.getException() : 
                    new RuntimeException("Todas as tentativas falharam");
            result.markAsFailed(finalException, LocalDateTime.now());
        }
        
        log.info("Execução com retry finalizada para operação {} (ID: {}): {}", 
                context.getOperationType(), context.getRetryId(), result.getSummary());
        
        return result;
    }
    
    /**
     * Executa uma operação simples com retry automático.
     * 
     * @param operation operação a ser executada
     * @param operationType tipo da operação
     * @param entityId ID da entidade
     * @param integrationType tipo de integração
     * @return resultado da operação
     */
    public RetryResult execute(Supplier<Object> operation, String operationType, 
                              Long entityId, String integrationType) {
        RetryContext context = RetryContext.builder()
                .retryId(generateRetryId())
                .operationType(operationType)
                .entityId(entityId)
                .integrationType(integrationType)
                .config(strategy.getMaxAttempts() > 0 ? 
                        RetryConfig.defaultConfig() : 
                        RetryConfig.builder().maxAttempts(1).build())
                .build();
        
        return execute(operation, context);
    }
    
    /**
     * Executa uma operação com configuração personalizada.
     * 
     * @param operation operação a ser executada
     * @param operationType tipo da operação
     * @param entityId ID da entidade
     * @param integrationType tipo de integração
     * @param config configuração de retry
     * @return resultado da operação
     */
    public RetryResult execute(Supplier<Object> operation, String operationType, 
                              Long entityId, String integrationType, RetryConfig config) {
        RetryContext context = RetryContext.builder()
                .retryId(generateRetryId())
                .operationType(operationType)
                .entityId(entityId)
                .integrationType(integrationType)
                .config(config)
                .build();
        
        return execute(operation, context);
    }
    
    /**
     * Executa a operação individual e registra a tentativa.
     * 
     * @param operation operação a ser executada
     * @param context contexto de retry
     * @param result resultado acumulado
     * @return resultado da operação
     * @throws Exception se a operação falhar
     */
    private Object executeOperation(Supplier<Object> operation, RetryContext context, RetryResult result) throws Exception {
        RetryAttempt attempt = RetryAttempt.builder()
                .attemptNumber(context.getCurrentAttempt())
                .startTime(LocalDateTime.now())
                .build();
        
        try {
            Object operationResult = operation.get();
            attempt.markAsSuccessful(LocalDateTime.now());
            context.addAttempt(attempt);
            return operationResult;
            
        } catch (Exception e) {
            attempt.markAsFailed(e, LocalDateTime.now());
            context.addAttempt(attempt);
            throw e;
        }
    }
    
    /**
     * Registra uma tentativa falhada.
     * 
     * @param exception exceção que ocorreu
     * @param context contexto de retry
     * @return tentativa registrada
     */
    private RetryAttempt recordFailedAttempt(Exception exception, RetryContext context) {
        RetryAttempt attempt = RetryAttempt.builder()
                .attemptNumber(context.getCurrentAttempt())
                .startTime(context.getLastAttemptTime() != null ? context.getLastAttemptTime() : LocalDateTime.now())
                .build();
        
        attempt.markAsFailed(exception, LocalDateTime.now());
        return attempt;
    }
    
    /**
     * Gera um ID único para o retry.
     * 
     * @return ID único
     */
    private String generateRetryId() {
        return "retry-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
