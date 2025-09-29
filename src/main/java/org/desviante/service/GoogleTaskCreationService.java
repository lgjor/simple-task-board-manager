package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Card;
import org.desviante.service.dto.CreateTaskRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Serviço dedicado para criação de tarefas no Google Tasks baseadas em cards.
 * 
 * <p>Esta classe é responsável exclusivamente por criar tarefas no Google Tasks
 * baseadas nas informações dos cards. A operação é isolada e não afeta
 * o estado do card no sistema local.</p>
 * 
 * <p>Esta separação garante que falhas na integração com Google Tasks não
 * causem rollback no salvamento das datas do card.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Card
 * @see CardSchedulingService
 * @see GoogleTasksApiService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTaskCreationService {

    private final CardSchedulingService cardSchedulingService;
    private final GoogleTasksApiService googleTasksApiService;

    /**
     * Cria uma tarefa no Google Tasks baseada nas informações de um card.
     * 
     * <p>Esta operação é independente e não afeta o estado do card.
     * Falhas na criação da tarefa não causam rollback nas datas do card.</p>
     * 
     * @param cardId identificador do card
     * @return true se a tarefa foi criada com sucesso, false caso contrário
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    public boolean createGoogleTask(Long cardId) {
        log.info("Criando tarefa no Google Tasks para card {}", cardId);
        
        try {
            // Buscar o card
            Optional<Card> cardOpt = cardSchedulingService.getCardById(cardId);
            if (cardOpt.isEmpty()) {
                throw new ResourceNotFoundException("Card com ID " + cardId + " não encontrado.");
            }
            
            Card card = cardOpt.get();
            
            // Verificar se o card tem data de vencimento
            if (card.getDueDate() == null) {
                log.warn("Card {} não possui data de vencimento para criar tarefa no Google Tasks", cardId);
                return false;
            }
            
            // Criar request para o Google Tasks
            CreateTaskRequest request = createTaskRequest(card);
            
            // Criar a tarefa no Google Tasks
            com.google.api.services.tasks.model.Task googleTask = googleTasksApiService.createTaskInList(request);
            
            log.info("Tarefa no Google Tasks criada com sucesso para card {} - ID: {}, Título: {}", 
                    cardId, googleTask.getId(), card.getTitle());
            
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao criar tarefa no Google Tasks para card {}: {}", cardId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica se um card pode ter uma tarefa criada no Google Tasks.
     * 
     * @param cardId identificador do card
     * @return true se o card pode ter tarefa criada, false caso contrário
     */
    public boolean canCreateGoogleTask(Long cardId) {
        try {
            Optional<Card> cardOpt = cardSchedulingService.getCardById(cardId);
            if (cardOpt.isEmpty()) {
                return false;
            }
            
            Card card = cardOpt.get();
            
            // Verificar se o card tem data de vencimento
            return card.getDueDate() != null;
            
        } catch (Exception e) {
            log.error("Erro ao verificar se card {} pode ter tarefa criada: {}", cardId, e.getMessage());
            return false;
        }
    }

    /**
     * Obtém informações do card para criação de tarefa no Google Tasks.
     * 
     * @param cardId identificador do card
     * @return informações do card ou null se não encontrado
     */
    public GoogleTaskInfo getGoogleTaskInfo(Long cardId) {
        try {
            Optional<Card> cardOpt = cardSchedulingService.getCardById(cardId);
            if (cardOpt.isEmpty()) {
                return null;
            }
            
            Card card = cardOpt.get();
            
            return new GoogleTaskInfo(
                card.getId(),
                card.getTitle(),
                card.getDescription(),
                card.getScheduledDate(),
                card.getDueDate()
            );
            
        } catch (Exception e) {
            log.error("Erro ao obter informações do card {} para tarefa no Google Tasks: {}", cardId, e.getMessage());
            return null;
        }
    }

    /**
     * Cria um CreateTaskRequest baseado nas informações do card.
     * 
     * @param card card a ser convertido
     * @return CreateTaskRequest para criação no Google Tasks
     */
    private CreateTaskRequest createTaskRequest(Card card) {
        String title = card.getTitle();
        
        // Construir notas com informações do card
        StringBuilder notes = new StringBuilder();
        if (card.getDescription() != null && !card.getDescription().trim().isEmpty()) {
            notes.append(card.getDescription());
        }
        
        // Adicionar informações de agendamento se disponível
        if (card.getScheduledDate() != null) {
            if (notes.length() > 0) {
                notes.append("\n\n");
            }
            notes.append("Agendado para: ").append(formatDateTime(card.getScheduledDate()));
        }
        
        // Converter data de vencimento para OffsetDateTime
        java.time.OffsetDateTime dueDate = null;
        if (card.getDueDate() != null) {
            dueDate = card.getDueDate().atOffset(ZoneOffset.UTC);
        }
        
        return new CreateTaskRequest("MyBoards", title, notes.toString(), 
                dueDate != null ? dueDate.toLocalDateTime() : null);
    }

    /**
     * Formata LocalDateTime para exibição.
     * 
     * @param dateTime data/hora a ser formatada
     * @return string formatada
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Classe para transportar informações do card para criação de tarefas no Google Tasks.
     */
    public static class GoogleTaskInfo {
        private final Long cardId;
        private final String title;
        private final String description;
        private final LocalDateTime scheduledDate;
        private final LocalDateTime dueDate;

        public GoogleTaskInfo(Long cardId, String title, String description, 
                            LocalDateTime scheduledDate, LocalDateTime dueDate) {
            this.cardId = cardId;
            this.title = title;
            this.description = description;
            this.scheduledDate = scheduledDate;
            this.dueDate = dueDate;
        }

        public Long getCardId() { return cardId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDateTime getScheduledDate() { return scheduledDate; }
        public LocalDateTime getDueDate() { return dueDate; }
    }
}
