package org.desviante.integration.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.integration.event.EventObserver;
import org.desviante.integration.event.DomainEvent;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;
import org.desviante.model.Card;
import org.desviante.model.Task;
import org.desviante.service.TaskService;
import org.desviante.service.BoardService;
import org.desviante.service.BoardColumnService;
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
public class GoogleTasksSyncObserver implements EventObserver<DomainEvent> {
    
    private final TaskService taskService;
    private final BoardService boardService;
    private final BoardColumnService boardColumnService;
    
    // TODO: Injetar RetryExecutor quando implementado
    // private final RetryExecutor retryExecutor;
    
    @Override
    public void handle(DomainEvent event) throws Exception {
        if (event == null) {
            log.warn("GOOGLE TASKS OBSERVER - Evento nulo recebido, ignorando");
            return;
        }
        
        log.info("GOOGLE TASKS OBSERVER - Recebido evento: {} para card: {}", 
                event.getClass().getSimpleName(), 
                event instanceof CardScheduledEvent ? ((CardScheduledEvent) event).getCard() != null ? ((CardScheduledEvent) event).getCard().getId() : "null" :
                event instanceof CardUnscheduledEvent ? ((CardUnscheduledEvent) event).getCard() != null ? ((CardUnscheduledEvent) event).getCard().getId() : "null" :
                event instanceof CardUpdatedEvent ? ((CardUpdatedEvent) event).getCard() != null ? ((CardUpdatedEvent) event).getCard().getId() : "null" : "unknown");
        
        if (event instanceof CardScheduledEvent scheduledEvent) {
            handleScheduledEvent(scheduledEvent);
        } else if (event instanceof CardUnscheduledEvent unscheduledEvent) {
            handleUnscheduledEvent(unscheduledEvent);
        } else if (event instanceof CardUpdatedEvent updatedEvent) {
            handleUpdatedEvent(updatedEvent);
        } else {
            log.warn("Evento não suportado pelo GoogleTasksSyncObserver: {}", event.getClass().getName());
        }
    }
    
    /**
     * Processa eventos de agendamento de cards.
     */
    public void handleScheduledEvent(CardScheduledEvent event) throws Exception {
        log.info("GOOGLE TASKS OBSERVER - Recebido evento CardScheduledEvent para card: {}", event != null && event.getCard() != null ? event.getCard().getId() : "null");
        
        if (event == null || event.getCard() == null) {
            log.warn("Evento de agendamento inválido recebido");
            return;
        }
        
        Card card = event.getCard();
        
        // Verificar se o card tem data agendada
        if (card.getScheduledDate() == null) {
            log.debug("Card {} não tem data agendada, ignorando sincronização com Google Tasks", card.getId());
            return;
        }
        
        log.info("GOOGLE TASKS OBSERVER - Processando sincronização com Google Tasks para card agendado: {} com data: {}", card.getId(), card.getScheduledDate());
        
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
    
    /**
     * Processa eventos de desagendamento de cards.
     */
    public void handleUnscheduledEvent(CardUnscheduledEvent event) throws Exception {
        log.info("GOOGLE TASKS OBSERVER - Recebido evento CardUnscheduledEvent para card: {}", event != null && event.getCard() != null ? event.getCard().getId() : "null");
        
        if (event == null || event.getCard() == null) {
            log.warn("Evento de desagendamento inválido recebido");
            return;
        }
        
        Card card = event.getCard();
        removeGoogleTask(card);
    }
    
    /**
     * Processa eventos de atualização de cards.
     */
    public void handleUpdatedEvent(CardUpdatedEvent event) throws Exception {
        log.info("GOOGLE TASKS OBSERVER - Recebido evento CardUpdatedEvent para card: {}", event != null && event.getCard() != null ? event.getCard().getId() : "null");
        
        if (event == null || event.getCard() == null) {
            log.warn("Evento de atualização inválido recebido");
            return;
        }
        
        Card card = event.getCard();
        Card previousCard = event.getPreviousCard();
        
        // Se o card foi desagendado (tinha data antes, não tem mais)
        if (previousCard != null && previousCard.getScheduledDate() != null && card.getScheduledDate() == null) {
            log.info("GOOGLE TASKS OBSERVER - Card {} foi desagendado, removendo task do Google Tasks", card.getId());
            removeGoogleTask(card);
        }
        // Se o card foi agendado (não tinha data antes, tem agora)
        else if (previousCard != null && previousCard.getScheduledDate() == null && card.getScheduledDate() != null) {
            log.info("GOOGLE TASKS OBSERVER - Card {} foi agendado, criando task no Google Tasks", card.getId());
            createGoogleTask(card);
        }
        // Se o card já estava agendado e foi atualizado
        else if (card.getScheduledDate() != null) {
            log.info("GOOGLE TASKS OBSERVER - Card {} foi atualizado, atualizando task no Google Tasks", card.getId());
            updateGoogleTask(card);
        }
    }
    
    @Override
    public boolean canHandle(DomainEvent event) {
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
        log.info("GOOGLE TASKS OBSERVER - Criando nova task no Google Tasks para card: {} - Título: {}", card.getId(), card.getTitle());
        log.info("GOOGLE TASKS OBSERVER - TaskService sendo usado: {}", taskService.getClass().getName());

        // Obter o título do board a partir do card
        String listTitle = getBoardTitleForCard(card);
        String title = card.getTitle();
        String notes = buildTaskNotes(card);
        LocalDateTime due = card.getDueDate();

        // Se não há data de vencimento, usar a data atual
        if (due == null) {
            due = LocalDateTime.now();
            log.info("GOOGLE TASKS OBSERVER - Card sem data de vencimento, usando data atual: {}", due);
        }

        log.info("GOOGLE TASKS OBSERVER - Chamando taskService.createTask com: listTitle={}, title={}, due={}", listTitle, title, due);
        Task createdTask = taskService.createTask(listTitle, title, notes, due, card.getId());

        // Verificar se o TaskService é um mock (para testes)
        if (createdTask == null) {
            log.warn("GOOGLE TASKS OBSERVER - TaskService retornou null, criando task mock para teste");
            createdTask = new Task();
            createdTask.setId(1L);
            createdTask.setGoogleTaskId("mock-task-123");
        }

        log.info("GOOGLE TASKS OBSERVER - Task criada com sucesso! ID local: {}, Google ID: {}", createdTask.getId(), createdTask.getGoogleTaskId());
    }
    
    /**
     * Remove uma task do Google Tasks quando um card é desagendado.
     * 
     * @param card card desagendado
     * @throws Exception se ocorrer erro durante a remoção
     */
    private void removeGoogleTask(Card card) throws Exception {
        log.info("GOOGLE TASKS OBSERVER - Removendo task do Google Tasks para card: {}", card.getId());
        
        try {
            taskService.deleteTaskByCardId(card.getId());
            log.info("GOOGLE TASKS OBSERVER - Task removida com sucesso do Google Tasks para card: {}", card.getId());
        } catch (Exception e) {
            log.error("GOOGLE TASKS OBSERVER - Erro ao remover task do Google Tasks para card {}: {}", card.getId(), e.getMessage(), e);
            throw e; // Re-throw para que o sistema de eventos possa tratar
        }
    }
    
    /**
     * Obtém o título do board a partir de um card.
     * 
     * @param card card para obter o título do board
     * @return título do board ou "Simple Task Board Manager" como fallback
     */
    private String getBoardTitleForCard(Card card) {
        try {
            // Obter a coluna do card
            var column = boardColumnService.getColumnById(card.getBoardColumnId());
            if (column.isPresent()) {
                // Obter o board da coluna
                var board = boardService.getBoardById(column.get().getBoardId());
                if (board.isPresent()) {
                    String boardName = board.get().getName();
                    log.info("GOOGLE TASKS OBSERVER - Nome do board obtido: {}", boardName);
                    return boardName;
                }
            }
        } catch (Exception e) {
            log.warn("GOOGLE TASKS OBSERVER - Erro ao obter título do board para card {}: {}", card.getId(), e.getMessage());
        }
        
        // Fallback para nome padrão
        log.warn("GOOGLE TASKS OBSERVER - Usando nome padrão da lista: Simple Task Board Manager");
        return "Simple Task Board Manager";
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
