package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.integration.coordinator.IntegrationCoordinator;
import org.desviante.integration.event.EventPublisher;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;
import org.desviante.integration.sync.IntegrationSyncService;
import org.desviante.integration.sync.IntegrationType;
import org.desviante.model.Card;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Serviço aprimorado de cards com integração ao sistema de eventos.
 * 
 * <p>Esta classe estende o CardService original adicionando integração
 * com o sistema de eventos para sincronização automática com sistemas
 * externos (Google Tasks, Calendário).</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável pela coordenação de operações de card com eventos</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos de eventos</li>
 *   <li><strong>LSP:</strong> Mantém compatibilidade com CardService original</li>
 *   <li><strong>ISP:</strong> Interface específica para operações com eventos</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (EventPublisher, IntegrationCoordinator)</li>
 * </ul>
 * 
 * <p><strong>Funcionalidades:</strong></p>
 * <ul>
 *   <li>Delegação para CardService original</li>
 *   <li>Publicação de eventos de domínio</li>
 *   <li>Coordenação de integrações</li>
 *   <li>Rastreamento de sincronização</li>
 *   <li>Tratamento de erros robusto</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CardService
 * @see EventPublisher
 * @see IntegrationCoordinator
 * @see IntegrationSyncService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedCardService {
    
    private final CardService cardService;
    private final EventPublisher eventPublisher;
    private final IntegrationCoordinator integrationCoordinator;
    private final IntegrationSyncService integrationSyncService;
    
    /**
     * Cria um novo card com integração de eventos.
     * 
     * @param title título do card
     * @param description descrição do card
     * @param parentColumnId ID da coluna pai
     * @param cardTypeId ID do tipo de card
     * @return card criado
     */
    @Transactional
    public Card createCard(String title, String description, Long parentColumnId, Long cardTypeId) {
        log.debug("Criando card: {}", title);
        
        Card card = cardService.createCard(title, description, parentColumnId, cardTypeId);
        
        // Publicar evento de atualização (card criado)
        publishCardUpdatedEvent(card, null);
        
        log.info("Card criado com sucesso: {} (ID: {})", title, card.getId());
        return card;
    }
    
    /**
     * Move um card para uma nova coluna com integração de eventos.
     * 
     * @param cardId ID do card
     * @param newColumnId ID da nova coluna
     * @return card movido
     */
    @Transactional
    public Card moveCardToColumn(Long cardId, Long newColumnId) {
        log.debug("Movendo card {} para coluna {}", cardId, newColumnId);
        
        // Obter card atual antes da movimentação
        Optional<Card> currentCardOpt = cardService.getCardById(cardId);
        if (currentCardOpt.isEmpty()) {
            throw new IllegalArgumentException("Card não encontrado: " + cardId);
        }
        
        Card currentCard = currentCardOpt.get();
        Long previousColumnId = currentCard.getBoardColumnId();
        
        // Mover o card
        Card movedCard = cardService.moveCardToColumn(cardId, newColumnId);
        
        // Coordenar integrações para movimentação
        try {
            integrationCoordinator.onCardMoved(movedCard, previousColumnId, newColumnId);
        } catch (Exception e) {
            log.error("Erro ao coordenar integrações para movimentação do card {}: {}", cardId, e.getMessage(), e);
            // Não falhar a operação principal por erro de integração
        }
        
        log.info("Card {} movido de coluna {} para {}", cardId, previousColumnId, newColumnId);
        return movedCard;
    }
    
    /**
     * Atualiza os detalhes de um card com integração de eventos.
     * 
     * @param cardId ID do card
     * @param newTitle novo título
     * @param newDescription nova descrição
     * @return card atualizado
     */
    @Transactional
    public Card updateCardDetails(Long cardId, String newTitle, String newDescription) {
        log.debug("Atualizando detalhes do card {}", cardId);
        
        // Obter card atual antes da atualização
        Optional<Card> currentCardOpt = cardService.getCardById(cardId);
        if (currentCardOpt.isEmpty()) {
            throw new IllegalArgumentException("Card não encontrado: " + cardId);
        }
        
        Card currentCard = currentCardOpt.get();
        
        // Atualizar o card
        Card updatedCard = cardService.updateCardDetails(cardId, newTitle, newDescription);
        
        // Publicar evento de atualização
        publishCardUpdatedEvent(updatedCard, currentCard);
        
        log.info("Detalhes do card {} atualizados", cardId);
        return updatedCard;
    }
    
    /**
     * Define a data de agendamento de um card com integração de eventos.
     * 
     * @param cardId ID do card
     * @param scheduledDate nova data de agendamento
     * @return card atualizado
     */
    @Transactional
    public Card setScheduledDate(Long cardId, LocalDateTime scheduledDate) {
        log.debug("Definindo data de agendamento para card {}: {}", cardId, scheduledDate);
        
        // Obter card atual antes da atualização
        Optional<Card> currentCardOpt = cardService.getCardById(cardId);
        if (currentCardOpt.isEmpty()) {
            throw new IllegalArgumentException("Card não encontrado: " + cardId);
        }
        
        Card currentCard = currentCardOpt.get();
        LocalDateTime previousScheduledDate = currentCard.getScheduledDate();
        
        // Atualizar o card
        Card updatedCard = cardService.setScheduledDate(cardId, scheduledDate);
        
        // Processar eventos de agendamento/desagendamento
        if (scheduledDate != null && previousScheduledDate == null) {
            // Card foi agendado pela primeira vez
            processCardScheduled(updatedCard);
        } else if (scheduledDate != null && !scheduledDate.equals(previousScheduledDate)) {
            // Data de agendamento foi alterada
            processCardRescheduled(updatedCard, previousScheduledDate);
        } else if (scheduledDate == null && previousScheduledDate != null) {
            // Card foi desagendado
            processCardUnscheduled(updatedCard, previousScheduledDate);
        } else {
            // Apenas atualização normal
            publishCardUpdatedEvent(updatedCard, currentCard);
        }
        
        log.info("Data de agendamento do card {} definida como: {}", cardId, scheduledDate);
        return updatedCard;
    }
    
    /**
     * Remove a data de agendamento de um card com integração de eventos.
     * 
     * @param cardId ID do card
     * @return card atualizado
     */
    @Transactional
    public Card removeScheduledDate(Long cardId) {
        return setScheduledDate(cardId, null);
    }
    
    /**
     * Define a data de vencimento de um card com integração de eventos.
     * 
     * @param cardId ID do card
     * @param dueDate nova data de vencimento
     * @return card atualizado
     */
    @Transactional
    public Card setDueDate(Long cardId, LocalDateTime dueDate) {
        log.debug("Definindo data de vencimento para card {}: {}", cardId, dueDate);
        
        // Obter card atual antes da atualização
        Optional<Card> currentCardOpt = cardService.getCardById(cardId);
        if (currentCardOpt.isEmpty()) {
            throw new IllegalArgumentException("Card não encontrado: " + cardId);
        }
        
        Card currentCard = currentCardOpt.get();
        
        // Atualizar o card
        Card updatedCard = cardService.setDueDate(cardId, dueDate);
        
        // Publicar evento de atualização
        publishCardUpdatedEvent(updatedCard, currentCard);
        
        log.info("Data de vencimento do card {} definida como: {}", cardId, dueDate);
        return updatedCard;
    }
    
    /**
     * Define ambas as datas (agendamento e vencimento) de um card com integração de eventos.
     * 
     * @param cardId ID do card
     * @param scheduledDate nova data de agendamento
     * @param dueDate nova data de vencimento
     * @return card atualizado
     */
    @Transactional
    public Card setSchedulingDates(Long cardId, LocalDateTime scheduledDate, LocalDateTime dueDate) {
        log.debug("Definindo datas de agendamento e vencimento para card {}: {} / {}", cardId, scheduledDate, dueDate);
        
        // Obter card atual antes da atualização
        Optional<Card> currentCardOpt = cardService.getCardById(cardId);
        if (currentCardOpt.isEmpty()) {
            throw new IllegalArgumentException("Card não encontrado: " + cardId);
        }
        
        Card currentCard = currentCardOpt.get();
        LocalDateTime previousScheduledDate = currentCard.getScheduledDate();
        
        // Atualizar o card PRIMEIRO - esta é a operação principal que deve sempre funcionar
        Card updatedCard = cardService.setSchedulingDates(cardId, scheduledDate, dueDate);
        
        // Processar eventos de agendamento em transação separada para evitar rollback
        processSchedulingEventsInSeparateTransaction(updatedCard, currentCard, scheduledDate, previousScheduledDate);
        
        log.debug("Datas do card {} definidas - Agendamento: {}, Vencimento: {}", cardId, scheduledDate, dueDate);
        return updatedCard;
    }
    
    /**
     * Exclui um card com integração de eventos.
     * 
     * @param cardId ID do card
     */
    @Transactional
    public void deleteCard(Long cardId) {
        log.debug("Excluindo card {}", cardId);
        
        // Obter card antes da exclusão para coordenar integrações
        Optional<Card> cardOpt = cardService.getCardById(cardId);
        
        // Excluir o card
        cardService.deleteCard(cardId);
        
        // Coordenar integrações para exclusão
        if (cardOpt.isPresent()) {
            try {
                integrationCoordinator.onCardDeleted(cardId);
            } catch (Exception e) {
                log.error("Erro ao coordenar integrações para exclusão do card {}: {}", cardId, e.getMessage(), e);
                // Não falhar a operação principal por erro de integração
            }
        }
        
        log.info("Card {} excluído", cardId);
    }
    
    /**
     * Obtém um card por ID (delegação direta).
     * 
     * @param id ID do card
     * @return Optional contendo o card se encontrado
     */
    public Optional<Card> getCardById(Long id) {
        return cardService.getCardById(id);
    }
    
    /**
     * Processa eventos de agendamento de forma segura em transação separada.
     * 
     * <p>Este método executa a integração em uma transação independente para evitar
     * que falhas de integração causem rollback na operação principal de salvamento
     * do card no banco de dados.</p>
     * 
     * @param updatedCard card atualizado
     * @param currentCard card anterior
     * @param scheduledDate nova data de agendamento
     * @param previousScheduledDate data anterior de agendamento
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSchedulingEventsInSeparateTransaction(Card updatedCard, Card currentCard, 
                                                           LocalDateTime scheduledDate, LocalDateTime previousScheduledDate) {
        try {
            // Processar eventos de agendamento baseado no estado
            if (scheduledDate != null && previousScheduledDate == null) {
                // Card foi agendado pela primeira vez
                processCardScheduled(updatedCard);
            } else if (scheduledDate != null && !scheduledDate.equals(previousScheduledDate)) {
                // Data de agendamento foi alterada
                processCardRescheduled(updatedCard, previousScheduledDate);
            } else if (scheduledDate == null && previousScheduledDate != null) {
                // Card foi desagendado
                processCardUnscheduled(updatedCard, previousScheduledDate);
            } else {
                // Apenas atualização normal
                publishCardUpdatedEvent(updatedCard, currentCard);
            }
            
            log.debug("Eventos de agendamento processados com sucesso para card {}", updatedCard.getId());
            
        } catch (Exception e) {
            log.error("Erro ao processar eventos de agendamento para card {}: {}", 
                     updatedCard.getId(), e.getMessage(), e);
            
            // Não re-lançar a exceção para não afetar a transação principal
            // A integração falhou, mas o card foi salvo com sucesso
            log.warn("Card {} salvo com sucesso, mas integração falhou", updatedCard.getId());
        }
    }

    /**
     * Processa o agendamento de um card.
     * 
     * @param card card agendado
     */
    private void processCardScheduled(Card card) {
        try {
            log.debug("Processando agendamento do card {}", card.getId());
            
            // Criar status de sincronização
            integrationSyncService.createSyncStatus(card.getId(), IntegrationType.GOOGLE_TASKS);
            integrationSyncService.createSyncStatus(card.getId(), IntegrationType.CALENDAR);
            
            // Publicar evento de agendamento
            CardScheduledEvent event = CardScheduledEvent.builder()
                    .card(card)
                    .scheduledDate(card.getScheduledDate())
                    .previousScheduledDate(null)
                    .build();
            
            eventPublisher.publish(event);
            
            log.debug("Agendamento do card {} processado com sucesso", card.getId());
            
        } catch (Exception e) {
            log.error("Erro ao processar agendamento do card {}: {}", card.getId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao processar agendamento do card " + card.getId(), e);
        }
    }
    
    /**
     * Processa o reagendamento de um card.
     * 
     * @param card card reagendado
     * @param previousScheduledDate data anterior de agendamento
     */
    private void processCardRescheduled(Card card, LocalDateTime previousScheduledDate) {
        try {
            // Publicar evento de agendamento (com data anterior)
            CardScheduledEvent event = CardScheduledEvent.builder()
                    .card(card)
                    .scheduledDate(card.getScheduledDate())
                    .previousScheduledDate(previousScheduledDate)
                    .build();
            
            eventPublisher.publish(event);
            
            log.debug("Card {} processado como reagendado", card.getId());
            
        } catch (Exception e) {
            log.error("Erro ao processar reagendamento do card {}: {}", card.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Processa o desagendamento de um card.
     * 
     * @param card card desagendado
     * @param previousScheduledDate data anterior de agendamento
     */
    private void processCardUnscheduled(Card card, LocalDateTime previousScheduledDate) {
        try {
            // Publicar evento de desagendamento
            CardUnscheduledEvent event = CardUnscheduledEvent.builder()
                    .card(card)
                    .previousScheduledDate(previousScheduledDate)
                    .build();
            
            eventPublisher.publish(event);
            
            log.debug("Card {} processado como desagendado", card.getId());
            
        } catch (Exception e) {
            log.error("Erro ao processar desagendamento do card {}: {}", card.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Publica evento de atualização de card.
     * 
     * @param updatedCard card atualizado
     * @param previousCard card anterior
     */
    private void publishCardUpdatedEvent(Card updatedCard, Card previousCard) {
        try {
            // Determinar campos alterados
            Set<String> changedFields = determineChangedFields(updatedCard, previousCard);
            
            // Publicar evento de atualização
            CardUpdatedEvent event = CardUpdatedEvent.builder()
                    .card(updatedCard)
                    .previousCard(previousCard)
                    .changedFields(changedFields)
                    .build();
            
            eventPublisher.publish(event);
            
            // Coordenar integrações se necessário
            if (event.requiresExternalSync()) {
                integrationCoordinator.onCardUpdated(updatedCard, previousCard);
            }
            
            log.debug("Evento de atualização publicado para card {}", updatedCard.getId());
            
        } catch (Exception e) {
            log.error("Erro ao publicar evento de atualização para card {}: {}", updatedCard.getId(), e.getMessage(), e);
        }
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
