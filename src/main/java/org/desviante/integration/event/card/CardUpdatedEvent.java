package org.desviante.integration.event.card;

import lombok.Builder;
import lombok.Data;
import org.desviante.integration.event.DomainEvent;
import org.desviante.model.Card;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Evento disparado quando um card é atualizado com mudanças relevantes.
 * 
 * <p>Este evento é publicado sempre que um card é modificado e as mudanças
 * podem afetar as integrações externas (Google Tasks e Calendário).</p>
 * 
 * <p><strong>Mudanças Relevantes:</strong></p>
 * <ul>
 *   <li>Título ou descrição alterados</li>
 *   <li>Data de vencimento modificada</li>
 *   <li>Status do card alterado</li>
 *   <li>Progresso atualizado</li>
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
public class CardUpdatedEvent implements DomainEvent {
    
    /**
     * Card que foi atualizado.
     */
    private final Card card;
    
    /**
     * Versão anterior do card (pode ser null se não disponível).
     */
    private final Card previousCard;
    
    /**
     * Campos que foram alterados no card.
     */
    private final Set<String> changedFields;
    
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
        return "CardUpdated";
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
     * Obtém o ID do card atualizado.
     * 
     * @return ID do card, ou null se o card não estiver disponível
     */
    public Long getCardId() {
        return getEntityId();
    }
    
    /**
     * Verifica se um campo específico foi alterado.
     * 
     * @param fieldName nome do campo a ser verificado
     * @return true se o campo foi alterado
     */
    public boolean isFieldChanged(String fieldName) {
        return changedFields != null && changedFields.contains(fieldName);
    }
    
    /**
     * Verifica se a data de agendamento foi alterada.
     * 
     * @return true se a data de agendamento foi modificada
     */
    public boolean isScheduledDateChanged() {
        return isFieldChanged("scheduledDate");
    }
    
    /**
     * Verifica se a data de vencimento foi alterada.
     * 
     * @return true se a data de vencimento foi modificada
     */
    public boolean isDueDateChanged() {
        return isFieldChanged("dueDate");
    }
    
    /**
     * Verifica se o título foi alterado.
     * 
     * @return true se o título foi modificado
     */
    public boolean isTitleChanged() {
        return isFieldChanged("title");
    }
    
    /**
     * Verifica se a descrição foi alterada.
     * 
     * @return true se a descrição foi modificada
     */
    public boolean isDescriptionChanged() {
        return isFieldChanged("description");
    }
    
    /**
     * Verifica se há mudanças que afetam as integrações externas.
     * 
     * @return true se as mudanças requerem sincronização externa
     */
    public boolean requiresExternalSync() {
        return isScheduledDateChanged() || isDueDateChanged() || 
               isTitleChanged() || isDescriptionChanged();
    }
}
