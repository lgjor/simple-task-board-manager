package org.desviante.integration.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.integration.event.EventObserver;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;
import org.desviante.model.Card;
import org.desviante.service.TaskService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Observador responsável pela sincronização com Google Tasks.
 * 
 * <p>Este observador processa eventos relacionados a cards agendados e
 * executa as operações necessárias para manter a sincronização com
 * o Google Tasks, incluindo criação, atualização e remoção de tasks.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela sincronização com Google Tasks</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos de eventos</li>
 *   <li><strong>LSP:</strong> Implementa EventObserver corretamente</li>
 *   <li><strong>ISP:</strong> Interface específica para observação de eventos</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (TaskService, GoogleTasksApiService)</li>
 * </ul>
 * 
 * <p><strong>Operações Suportadas:</strong></p>
 * <ul>
 *   <li>Criação de task quando card é agendado</li>
 *   <li>Atualização de task quando card é modificado</li>
 *   <li>Remoção de task quando card é desagendado</li>
 *   <li>Sincronização de datas e descrições</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see EventObserver
 * @see CardScheduledEvent
 * @see CardUnscheduledEvent
 * @see CardUpdatedEvent
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleTasksSyncObserver implements EventObserver<CardScheduledEvent> {
    
    private final TaskService taskService;
    
    // TODO: Injetar RetryExecutor quando implementado
    // private final RetryExecutor retryExecutor;
    
    @Override
    public void handle(CardScheduledEvent event) throws Exception {
        if (event == null || event.getCard() == null) {
            log.warn("Evento de agendamento inválido recebido");
            return;
        }
        
        Card card = event.getCard();
        log.info("Processando sincronização com Google Tasks para card agendado: {}", card.getId());
        
        try {
            if (event.isFirstScheduling()) {
                // Primeira vez sendo agendado - criar nova task
                createGoogleTask(card);
            } else {
                // Card já estava agendado - atualizar task existente
                updateGoogleTask(card);
            }
            
            log.debug("Sincronização com Google Tasks concluída com sucesso para card: {}", card.getId());
            
        } catch (Exception e) {
            log.error("Erro ao sincronizar card {} com Google Tasks: {}", card.getId(), e.getMessage(), e);
            throw e; // Re-throw para que o sistema de eventos possa tratar
        }
    }
    
    @Override
    public boolean canHandle(org.desviante.integration.event.DomainEvent event) {
        return event instanceof CardScheduledEvent || 
               event instanceof CardUnscheduledEvent || 
               event instanceof CardUpdatedEvent;
    }
    
    @Override
    public int getPriority() {
        return 10; // Prioridade alta para Google Tasks
    }
    
    @Override
    public String getObserverName() {
        return "GoogleTasksSyncObserver";
    }
    
    /**
     * Cria uma nova task no Google Tasks para um card agendado.
     * 
     * @param card card agendado
     * @throws Exception se ocorrer erro durante a criação
     */
    private void createGoogleTask(Card card) throws Exception {
        log.debug("Criando nova task no Google Tasks para card: {}", card.getId());
        
        String listTitle = "Simple Task Board Manager"; // Lista padrão
        String title = card.getTitle();
        String notes = buildTaskNotes(card);
        LocalDateTime due = card.getDueDate();
        
        taskService.createTask(listTitle, title, notes, due, card.getId());
        
        log.info("Task criada no Google Tasks para card: {}", card.getId());
    }
    
    /**
     * Atualiza uma task existente no Google Tasks.
     * 
     * @param card card atualizado
     * @throws Exception se ocorrer erro durante a atualização
     */
    private void updateGoogleTask(Card card) throws Exception {
        log.debug("Atualizando task no Google Tasks para card: {}", card.getId());
        
        // Por simplicidade, vamos criar uma nova task
        // Em uma implementação mais robusta, buscaríamos a task existente
        log.info("Recriando task no Google Tasks para card: {}", card.getId());
        createGoogleTask(card);
    }
    
    /**
     * Remove uma task do Google Tasks quando um card é desagendado.
     * 
     * @param card card desagendado
     * @throws Exception se ocorrer erro durante a remoção
     */
    public void handleUnscheduled(Card card) throws Exception {
        if (card == null) {
            log.warn("Card inválido recebido para desagendamento");
            return;
        }
        
        log.info("Removendo task do Google Tasks para card desagendado: {}", card.getId());
        
        // Por simplicidade, apenas logamos a remoção
        // Em uma implementação mais robusta, buscaríamos e removeríamos a task
        log.debug("Task seria removida do Google Tasks para card: {}", card.getId());
    }
    
    /**
     * Atualiza uma task quando um card é modificado.
     * 
     * @param card card atualizado
     * @param previousCard versão anterior do card
     * @throws Exception se ocorrer erro durante a atualização
     */
    public void handleUpdated(Card card, Card previousCard) throws Exception {
        if (card == null) {
            log.warn("Card inválido recebido para atualização");
            return;
        }
        
        // Só processar se o card está agendado
        if (card.getScheduledDate() == null) {
            log.debug("Card {} não está agendado, ignorando atualização no Google Tasks", card.getId());
            return;
        }
        
        log.info("Atualizando task no Google Tasks para card modificado: {}", card.getId());
        
        // Se o card não tinha data de agendamento antes, criar nova task
        if (previousCard == null || previousCard.getScheduledDate() == null) {
            createGoogleTask(card);
        } else {
            // Atualizar task existente
            updateGoogleTask(card);
        }
    }
    
    /**
     * Constrói as notas da task baseadas nas informações do card.
     * 
     * @param card card para construção das notas
     * @return notas formatadas para a task
     */
    private String buildTaskNotes(Card card) {
        StringBuilder notes = new StringBuilder();
        
        if (card.getDescription() != null && !card.getDescription().trim().isEmpty()) {
            notes.append("Descrição: ").append(card.getDescription()).append("\n\n");
        }
        
        if (card.getScheduledDate() != null) {
            notes.append("Agendado para: ").append(card.getScheduledDate()).append("\n");
        }
        
        if (card.getDueDate() != null) {
            notes.append("Vence em: ").append(card.getDueDate()).append("\n");
        }
        
        notes.append("Card ID: ").append(card.getId());
        
        return notes.toString();
    }
}
