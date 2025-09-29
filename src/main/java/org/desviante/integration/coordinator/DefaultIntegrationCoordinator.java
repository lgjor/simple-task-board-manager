package org.desviante.integration.coordinator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.integration.event.EventPublisher;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;
import org.desviante.model.Card;
import org.desviante.service.DatabaseMigrationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementação padrão do IntegrationCoordinator.
 * 
 * <p>Esta implementação coordena as integrações através do sistema de eventos,
 * seguindo o padrão Observer para desacoplar a coordenação dos detalhes
 * específicos de cada integração.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Coordenação baseada em eventos</li>
 *   <li>Estatísticas em tempo real</li>
 *   <li>Thread-safe</li>
 *   <li>Logging detalhado</li>
 *   <li>Tratamento de erros robusto</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see IntegrationCoordinator
 * @see EventPublisher
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultIntegrationCoordinator implements IntegrationCoordinator {
    
    private final EventPublisher eventPublisher;
    private final DatabaseMigrationService migrationService;
    
    /**
     * Garante que a tabela de sincronização existe antes de executar operações.
     */
    private void ensureTableExists() {
        try {
            migrationService.ensureIntegrationSyncStatusTable();
        } catch (Exception e) {
            log.error("Erro ao garantir existência da tabela de sincronização: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao preparar banco de dados para sincronização", e);
        }
    }
    
    // Contadores thread-safe para estatísticas
    private final AtomicLong successfulIntegrations = new AtomicLong(0);
    private final AtomicLong failedIntegrations = new AtomicLong(0);
    private final AtomicLong scheduledIntegrations = new AtomicLong(0);
    private final AtomicLong unscheduledIntegrations = new AtomicLong(0);
    private final AtomicLong updateIntegrations = new AtomicLong(0);
    private final AtomicLong moveIntegrations = new AtomicLong(0);
    private final AtomicLong deleteIntegrations = new AtomicLong(0);
    
    private volatile LocalDateTime lastIntegrationTime;
    private final LocalDateTime startTime = LocalDateTime.now();
    
    @Override
    public void onCardScheduled(Card card) {
        if (card == null) {
            log.warn("Tentativa de agendamento com card null");
            return;
        }
        
        if (card.getScheduledDate() == null) {
            log.warn("Card {} não possui data de agendamento", card.getId());
            return;
        }
        
        log.info("Coordenando integrações para card agendado: {} em {}", 
                card.getId(), card.getScheduledDate());
        
        // Garantir que a tabela existe antes da operação
        ensureTableExists();
        
        try {
            // Publicar evento de agendamento
            CardScheduledEvent event = CardScheduledEvent.builder()
                    .card(card)
                    .scheduledDate(card.getScheduledDate())
                    .build();
            
            eventPublisher.publish(event);
            
            // Atualizar estatísticas
            scheduledIntegrations.incrementAndGet();
            successfulIntegrations.incrementAndGet();
            lastIntegrationTime = LocalDateTime.now();
            
            log.debug("Integrações de agendamento coordenadas com sucesso para card: {}", card.getId());
            
        } catch (Exception e) {
            failedIntegrations.incrementAndGet();
            log.error("Erro ao coordenar integrações de agendamento para card {}: {}", 
                     card.getId(), e.getMessage(), e);
            throw new RuntimeException(
                "Falha na coordenação de integrações de agendamento para card " + card.getId(),
                e);
        }
    }
    
    @Override
    public void onCardUnscheduled(Card card) {
        if (card == null) {
            log.warn("Tentativa de desagendamento com card null");
            return;
        }
        
        log.info("Coordenando integrações para card desagendado: {}", card.getId());
        
        // Garantir que a tabela existe antes da operação
        ensureTableExists();
        
        try {
            // Publicar evento de desagendamento
            CardUnscheduledEvent event = CardUnscheduledEvent.builder()
                    .card(card)
                    .previousScheduledDate(card.getScheduledDate()) // Pode ser null
                    .build();
            
            eventPublisher.publish(event);
            
            // Atualizar estatísticas
            unscheduledIntegrations.incrementAndGet();
            successfulIntegrations.incrementAndGet();
            lastIntegrationTime = LocalDateTime.now();
            
            log.debug("Integrações de desagendamento coordenadas com sucesso para card: {}", card.getId());
            
        } catch (Exception e) {
            failedIntegrations.incrementAndGet();
            log.error("Erro ao coordenar integrações de desagendamento para card {}: {}", 
                     card.getId(), e.getMessage(), e);
            throw new RuntimeException(
                "Falha na coordenação de integrações de desagendamento para card " + card.getId(),
                e);
        }
    }
    
    @Override
    public void onCardUpdated(Card card, Card previousCard) {
        if (card == null) {
            log.warn("Tentativa de atualização com card null");
            return;
        }
        
        log.info("Coordenando integrações para card atualizado: {}", card.getId());
        
        // Garantir que a tabela existe antes da operação
        ensureTableExists();
        
        try {
            // Determinar campos alterados
            Set<String> changedFields = determineChangedFields(card, previousCard);
            
            // Publicar evento de atualização
            CardUpdatedEvent event = CardUpdatedEvent.builder()
                    .card(card)
                    .previousCard(previousCard)
                    .changedFields(changedFields)
                    .build();
            
            eventPublisher.publish(event);
            
            // Atualizar estatísticas
            updateIntegrations.incrementAndGet();
            successfulIntegrations.incrementAndGet();
            lastIntegrationTime = LocalDateTime.now();
            
            log.debug("Integrações de atualização coordenadas com sucesso para card: {} (campos: {})", 
                     card.getId(), changedFields);
            
        } catch (Exception e) {
            failedIntegrations.incrementAndGet();
            log.error("Erro ao coordenar integrações de atualização para card {}: {}", 
                     card.getId(), e.getMessage(), e);
            throw new RuntimeException(
                "Falha na coordenação de integrações de atualização para card " + card.getId(),
                e);
        }
    }
    
    @Override
    public void onCardMoved(Card card, Long previousColumnId, Long newColumnId) {
        if (card == null) {
            log.warn("Tentativa de movimentação com card null");
            return;
        }
        
        log.info("Coordenando integrações para card movido: {} de coluna {} para {}", 
                card.getId(), previousColumnId, newColumnId);
        
        try {
            // Para movimentação, tratamos como uma atualização especial
            // onCardUpdated já garante que a tabela existe
            onCardUpdated(card, null);
            
            // Atualizar estatísticas específicas de movimentação
            moveIntegrations.incrementAndGet();
            
            log.debug("Integrações de movimentação coordenadas com sucesso para card: {}", card.getId());
            
        } catch (Exception e) {
            failedIntegrations.incrementAndGet();
            log.error("Erro ao coordenar integrações de movimentação para card {}: {}", 
                     card.getId(), e.getMessage(), e);
            throw new RuntimeException(
                "Falha na coordenação de integrações de movimentação para card " + card.getId(),
                e);
        }
    }
    
    @Override
    public void onCardDeleted(Long cardId) {
        if (cardId == null) {
            log.warn("Tentativa de exclusão com card ID null");
            return;
        }
        
        log.info("Coordenando integrações para card excluído: {}", cardId);
        
        try {
            // Para exclusão, criamos um card temporário apenas com o ID
            Card deletedCard = Card.builder()
                    .id(cardId)
                    .build();
            
            // Tratamos como desagendamento para limpar integrações
            // onCardUnscheduled já garante que a tabela existe
            onCardUnscheduled(deletedCard);
            
            // Atualizar estatísticas específicas de exclusão
            deleteIntegrations.incrementAndGet();
            
            log.debug("Integrações de exclusão coordenadas com sucesso para card: {}", cardId);
            
        } catch (Exception e) {
            failedIntegrations.incrementAndGet();
            log.error("Erro ao coordenar integrações de exclusão para card {}: {}", 
                     cardId, e.getMessage(), e);
            throw new RuntimeException(
                "Falha na coordenação de integrações de exclusão para card " + cardId,
                e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        return eventPublisher != null;
    }
    
    @Override
    public IntegrationStats getStats() {
        return IntegrationStats.builder()
                .successfulIntegrations(successfulIntegrations.get())
                .failedIntegrations(failedIntegrations.get())
                .scheduledIntegrations(scheduledIntegrations.get())
                .unscheduledIntegrations(unscheduledIntegrations.get())
                .updateIntegrations(updateIntegrations.get())
                .moveIntegrations(moveIntegrations.get())
                .deleteIntegrations(deleteIntegrations.get())
                .lastIntegrationTime(lastIntegrationTime)
                .startTime(startTime)
                .build();
    }
    
    /**
     * Determina quais campos foram alterados entre duas versões de um card.
     * 
     * @param currentCard card atual
     * @param previousCard card anterior
     * @return conjunto de nomes dos campos alterados
     */
    private Set<String> determineChangedFields(Card currentCard, Card previousCard) {
        java.util.HashSet<String> changedFields = new java.util.HashSet<>();
        
        if (previousCard == null) {
            // Se não há versão anterior, todos os campos são considerados alterados
            changedFields.addAll(java.util.Arrays.asList(
                "title", "description", "scheduledDate", "dueDate", 
                "boardColumnId", "cardTypeId", "progressType"
            ));
            return changedFields;
        }
        
        // Comparar campos relevantes
        if (!java.util.Objects.equals(currentCard.getTitle(), previousCard.getTitle())) {
            changedFields.add("title");
        }
        
        if (!java.util.Objects.equals(currentCard.getDescription(), previousCard.getDescription())) {
            changedFields.add("description");
        }
        
        if (!java.util.Objects.equals(currentCard.getScheduledDate(), previousCard.getScheduledDate())) {
            changedFields.add("scheduledDate");
        }
        
        if (!java.util.Objects.equals(currentCard.getDueDate(), previousCard.getDueDate())) {
            changedFields.add("dueDate");
        }
        
        if (!java.util.Objects.equals(currentCard.getBoardColumnId(), previousCard.getBoardColumnId())) {
            changedFields.add("boardColumnId");
        }
        
        if (!java.util.Objects.equals(currentCard.getCardTypeId(), previousCard.getCardTypeId())) {
            changedFields.add("cardTypeId");
        }
        
        if (!java.util.Objects.equals(currentCard.getProgressType(), previousCard.getProgressType())) {
            changedFields.add("progressType");
        }
        
        return changedFields;
    }
}
