package org.desviante.integration.event;

import java.time.LocalDateTime;

/**
 * Interface base para eventos de domínio no sistema.
 * 
 * <p>Esta interface define o contrato básico que todos os eventos de domínio
 * devem implementar, garantindo consistência e rastreabilidade no sistema
 * de eventos.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela definição de eventos de domínio</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos de eventos</li>
 *   <li><strong>LSP:</strong> Implementado por todos os eventos de domínio</li>
 *   <li><strong>ISP:</strong> Interface específica para eventos</li>
 *   <li><strong>DIP:</strong> Abstração para o sistema de eventos</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see java.time.LocalDateTime
 */
public interface DomainEvent {
    
    /**
     * Obtém o momento exato em que o evento ocorreu.
     * 
     * <p>Este timestamp é fundamental para ordenação cronológica
     * dos eventos e para auditoria do sistema.</p>
     * 
     * @return data e hora em que o evento ocorreu
     */
    LocalDateTime getOccurredOn();
    
    /**
     * Obtém o tipo do evento para identificação e roteamento.
     * 
     * <p>O tipo do evento é usado pelo sistema de eventos para
     * determinar quais observadores devem ser notificados.</p>
     * 
     * @return tipo do evento (ex: "CardScheduled", "CardUpdated")
     */
    String getEventType();
    
    /**
     * Obtém o ID da entidade relacionada ao evento.
     * 
     * <p>Permite rastrear qual entidade específica foi afetada
     * pelo evento, facilitando debugging e auditoria.</p>
     * 
     * @return ID da entidade relacionada, ou null se não aplicável
     */
    default Long getEntityId() {
        return null;
    }
    
    /**
     * Obtém o tipo da entidade relacionada ao evento.
     * 
     * <p>Especifica se o evento está relacionado a um card, task,
     * ou outra entidade do sistema.</p>
     * 
     * @return tipo da entidade (ex: "Card", "Task"), ou null se não aplicável
     */
    default String getEntityType() {
        return null;
    }
}
