package org.desviante.integration.event;

/**
 * Exceção lançada quando ocorre erro durante a publicação de eventos.
 * 
 * <p>Esta exceção encapsula erros que podem ocorrer durante o processo
 * de publicação de eventos, incluindo falhas na notificação de observadores
 * ou problemas de configuração do sistema de eventos.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class EventPublishingException extends RuntimeException {
    
    private final DomainEvent failedEvent;
    
    /**
     * Construtor com mensagem de erro.
     * 
     * @param message mensagem descritiva do erro
     */
    public EventPublishingException(String message) {
        super(message);
        this.failedEvent = null;
    }
    
    /**
     * Construtor com mensagem de erro e causa.
     * 
     * @param message mensagem descritiva do erro
     * @param cause exceção que causou este erro
     */
    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
        this.failedEvent = null;
    }
    
    /**
     * Construtor com mensagem de erro e evento que falhou.
     * 
     * @param message mensagem descritiva do erro
     * @param failedEvent evento que falhou ao ser publicado
     */
    public EventPublishingException(String message, DomainEvent failedEvent) {
        super(message);
        this.failedEvent = failedEvent;
    }
    
    /**
     * Construtor com mensagem de erro, causa e evento que falhou.
     * 
     * @param message mensagem descritiva do erro
     * @param cause exceção que causou este erro
     * @param failedEvent evento que falhou ao ser publicado
     */
    public EventPublishingException(String message, Throwable cause, DomainEvent failedEvent) {
        super(message, cause);
        this.failedEvent = failedEvent;
    }
    
    /**
     * Obtém o evento que falhou ao ser publicado.
     * 
     * @return evento que falhou, ou null se não disponível
     */
    public DomainEvent getFailedEvent() {
        return failedEvent;
    }
}
