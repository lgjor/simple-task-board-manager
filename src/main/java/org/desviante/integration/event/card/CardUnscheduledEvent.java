package org.desviante.integration.event.card;

import lombok.Builder;
import lombok.Data;
import org.desviante.integration.event.DomainEvent;
import org.desviante.model.Card;

import java.time.LocalDateTime;

/**
 * Evento disparado quando um card é desagendado (data de agendamento removida).
 * 
 * <p>Este evento é publicado sempre que um card tem sua data de agendamento
 * removida ou definida como null, indicando que as integrações externas
 * (Google Tasks e Calendário) devem ser atualizadas adequadamente.</p>
 * 
 * <p><strong>Contexto de Uso:</strong></p>
 * <ul>
 *   <li>Data de agendamento removida de um card</li>
 *   <li>Card movido para coluna que não requer agendamento</li>
 *   <li>Card excluído (se tinha data de agendamento)</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see DomainEvent
 * @see Card
 */
@Data
@Builder
public class CardUnscheduledEvent implements DomainEvent {
    
    /**
     * Card que foi desagendado.
     */
    private final Card card;
    
    /**
     * Data de agendamento anterior que foi removida.
     */
    private final LocalDateTime previousScheduledDate;
    
    /**
     * Momento exato em que o evento ocorreu.
     */
    @Builder.Default
    private final LocalDateTime occurredOn = LocalDateTime.now();
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    @Override
    public String getEventType() {
        return "CardUnscheduled";
    }
    
    @Override
    public Long getEntityId() {
        return card != null ? card.getId() : null;
    }
    
    @Override
    public String getEntityType() {
        return "Card";
    }
    
    /**
     * Obtém o ID do card desagendado.
     * 
     * @return ID do card, ou null se o card não estiver disponível
     */
    public Long getCardId() {
        return getEntityId();
    }
    
    /**
     * Verifica se o card tinha uma data de agendamento anterior.
     * 
     * @return true se havia uma data de agendamento anterior
     */
    public boolean hadPreviousScheduling() {
        return previousScheduledDate != null;
    }
}
