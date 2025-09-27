package org.desviante.integration.event.card;

import lombok.Builder;
import lombok.Data;
import org.desviante.integration.event.DomainEvent;
import org.desviante.model.Card;

import java.time.LocalDateTime;

/**
 * Evento disparado quando um card é agendado (recebe uma data de agendamento).
 * 
 * <p>Este evento é publicado sempre que um card recebe uma data de agendamento
 * (scheduledDate), indicando que ele deve ser sincronizado com sistemas externos
 * como Google Tasks e Calendário.</p>
 * 
 * <p><strong>Contexto de Uso:</strong></p>
 * <ul>
 *   <li>Card criado com data de agendamento</li>
 *   <li>Data de agendamento adicionada a um card existente</li>
 *   <li>Data de agendamento de um card é modificada</li>
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
public class CardScheduledEvent implements DomainEvent {
    
    /**
     * Card que foi agendado.
     */
    private final Card card;
    
    /**
     * Data e hora de agendamento do card.
     */
    private final LocalDateTime scheduledDate;
    
    /**
     * Data anterior de agendamento (null se é a primeira vez sendo agendado).
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
        return "CardScheduled";
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
     * Verifica se este é o primeiro agendamento do card.
     * 
     * @return true se é a primeira vez que o card está sendo agendado
     */
    public boolean isFirstScheduling() {
        return previousScheduledDate == null;
    }
    
    /**
     * Verifica se a data de agendamento foi alterada.
     * 
     * @return true se a data foi modificada (não é o primeiro agendamento)
     */
    public boolean isSchedulingChanged() {
        return !isFirstScheduling();
    }
    
    /**
     * Obtém o ID do card agendado.
     * 
     * @return ID do card, ou null se o card não estiver disponível
     */
    public Long getCardId() {
        return getEntityId();
    }
}
