package org.desviante.integration.retry;

import java.time.Duration;

/**
 * Interface para estratégias de retry em operações de integração.
 * 
 * <p>Esta interface define o contrato para implementação de diferentes
 * estratégias de retry, permitindo flexibilidade na configuração
 * de tentativas de recuperação de falhas temporárias.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela definição de estratégias de retry</li>
 *   <li><strong>OCP:</strong> Extensível através de novas estratégias</li>
 *   <li><strong>LSP:</strong> Implementado por diferentes estratégias</li>
 *   <li><strong>ISP:</strong> Interface específica para retry</li>
 *   <li><strong>DIP:</strong> Abstração para o sistema de retry</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RetryContext
 * @see RetryResult
 */
public interface RetryStrategy {
    
    /**
     * Calcula o próximo delay para retry baseado na estratégia.
     * 
     * @param context contexto do retry
     * @return duração do delay até a próxima tentativa
     */
    Duration calculateDelay(RetryContext context);
    
    /**
     * Verifica se deve continuar tentando baseado na estratégia.
     * 
     * @param context contexto do retry
     * @return true se deve continuar tentando
     */
    boolean shouldRetry(RetryContext context);
    
    /**
     * Obtém o número máximo de tentativas para esta estratégia.
     * 
     * @return número máximo de tentativas
     */
    int getMaxAttempts();
    
    /**
     * Verifica se a exceção é retryable para esta estratégia.
     * 
     * @param exception exceção a ser verificada
     * @return true se a exceção permite retry
     */
    boolean isRetryable(Exception exception);
    
    /**
     * Obtém o nome da estratégia.
     * 
     * @return nome descritivo da estratégia
     */
    String getStrategyName();
}
