package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Card;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviço dedicado para geração de eventos no calendário baseados em cards.
 * 
 * <p>Esta classe é responsável exclusivamente por criar eventos no calendário
 * baseados nas datas de agendamento dos cards. A operação é isolada e não
 * afeta o salvamento das datas do card.</p>
 * 
 * <p>Esta separação garante que falhas na integração com calendário não
 * causem rollback no salvamento das datas do card.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Card
 * @see CardSchedulingService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarEventService {

    private final CardSchedulingService cardSchedulingService;
    private final List<CalendarEventChangeListener> listeners = new ArrayList<>();

    /**
     * Adiciona um listener para mudanças em eventos.
     * 
     * @param listener listener a ser adicionado
     */
    public void addListener(CalendarEventChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove um listener de mudanças em eventos.
     * 
     * @param listener listener a ser removido
     */
    public void removeListener(CalendarEventChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifica todos os listeners sobre a exclusão de um evento.
     * 
     * @param cardId ID do card relacionado ao evento deletado
     */
    private void notifyEventDeleted(Long cardId) {
        for (CalendarEventChangeListener listener : listeners) {
            try {
                listener.onEventDeleted(cardId);
            } catch (Exception e) {
                log.warn("Erro ao notificar listener sobre exclusão de evento para card {}: {}", cardId, e.getMessage());
            }
        }
    }

    /**
     * Notifica todos os listeners sobre a criação de um evento.
     * 
     * @param cardId ID do card relacionado ao evento criado
     */
    private void notifyEventCreated(Long cardId) {
        for (CalendarEventChangeListener listener : listeners) {
            try {
                listener.onEventCreated(cardId);
            } catch (Exception e) {
                log.warn("Erro ao notificar listener sobre criação de evento para card {}: {}", cardId, e.getMessage());
            }
        }
    }

    /**
     * Cria um evento no calendário baseado nas informações de agendamento de um card.
     * 
     * <p>Esta operação é independente e não afeta o estado do card.
     * Falhas na criação do evento não causam rollback nas datas do card.</p>
     * 
     * @param cardId identificador do card
     * @return true se o evento foi criado com sucesso, false caso contrário
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    public boolean createCalendarEvent(Long cardId) {
        log.info("Criando evento no calendário para card {}", cardId);
        
        try {
            // Buscar o card
            Optional<Card> cardOpt = cardSchedulingService.getCardById(cardId);
            if (cardOpt.isEmpty()) {
                throw new ResourceNotFoundException("Card com ID " + cardId + " não encontrado.");
            }
            
            Card card = cardOpt.get();
            
            // Verificar se o card tem data de agendamento
            if (card.getScheduledDate() == null) {
                log.warn("Card {} não possui data de agendamento para criar evento no calendário", cardId);
                return false;
            }
            
            // TODO: Implementar integração com serviço de calendário
            // Por enquanto, apenas simular a criação do evento
            log.info("Evento no calendário criado com sucesso para card {} - Título: {}, Data: {}", 
                    cardId, card.getTitle(), card.getScheduledDate());
            
            // Notificar listeners sobre a criação do evento
            notifyEventCreated(cardId);
            
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao criar evento no calendário para card {}: {}", cardId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica se um card pode ter um evento criado no calendário.
     * 
     * @param cardId identificador do card
     * @return true se o card pode ter evento criado, false caso contrário
     */
    public boolean canCreateCalendarEvent(Long cardId) {
        try {
            Optional<Card> cardOpt = cardSchedulingService.getCardById(cardId);
            if (cardOpt.isEmpty()) {
                return false;
            }
            
            Card card = cardOpt.get();
            
            // Verificar se o card tem data de agendamento
            return card.getScheduledDate() != null;
            
        } catch (Exception e) {
            log.error("Erro ao verificar se card {} pode ter evento criado: {}", cardId, e.getMessage());
            return false;
        }
    }

    /**
     * Obtém informações do card para exibição na interface.
     * 
     * @param cardId identificador do card
     * @return informações do card ou null se não encontrado
     */
    public CardEventInfo getCardEventInfo(Long cardId) {
        try {
            Optional<Card> cardOpt = cardSchedulingService.getCardById(cardId);
            if (cardOpt.isEmpty()) {
                return null;
            }
            
            Card card = cardOpt.get();
            
            return new CardEventInfo(
                card.getId(),
                card.getTitle(),
                card.getDescription(),
                card.getScheduledDate(),
                card.getDueDate()
            );
            
        } catch (Exception e) {
            log.error("Erro ao obter informações do card {} para evento: {}", cardId, e.getMessage());
            return null;
        }
    }

    /**
     * Deleta um evento do calendário e remove a data de agendamento do card correspondente.
     * 
     * <p>Eventos são acoplados aos cards. A exclusão de um evento remove também
     * a data de agendamento do card que o gerou, mantendo a consistência entre
     * calendário e sistema de cards.</p>
     * 
     * <p><strong>Arquitetura:</strong></p>
     * <ul>
     *   <li>Card com data de agendamento → Gera evento no calendário</li>
     *   <li>Evento excluído → Data de agendamento do card é removida</li>
     *   <li>Card sem data → Evento relacionado é removido automaticamente</li>
     * </ul>
     * 
     * @param eventId identificador do evento a ser deletado
     * @return true se o evento foi deletado com sucesso, false caso contrário
     */
    public boolean deleteCalendarEvent(String eventId) {
        log.info("Deletando evento do calendário e removendo data de agendamento do card: {}", eventId);
        
        try {
            Long cardId = Long.parseLong(eventId);
            
            // Primeiro, verificar se o card existe
            Optional<Card> cardOptional = cardSchedulingService.getCardById(cardId);
            if (cardOptional.isEmpty()) {
                log.warn("Card com ID {} não encontrado para deletar evento", cardId);
                return false;
            }
            
            Card card = cardOptional.get();
            boolean hadScheduledDate = card.getScheduledDate() != null;
            
            // Se o card tinha data de agendamento, removê-la e também o evento
            if (hadScheduledDate) {
                cardSchedulingService.clearScheduledDateWithEvent(cardId);
                log.info("Data de agendamento e evento removidos do card {}: {}", cardId, card.getTitle());
                
                // Notificar listeners sobre a exclusão do evento
                notifyEventDeleted(cardId);
            } else {
                log.info("Card {} não tinha data de agendamento para remover", cardId);
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            log.error("ID de evento inválido: {}", eventId);
            return false;
        } catch (Exception e) {
            log.error("Erro ao deletar evento do calendário {}: {}", eventId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica se um evento pode ser deletado.
     * 
     * @param eventId identificador do evento
     * @return true se o evento pode ser deletado, false caso contrário
     */
    public boolean canDeleteCalendarEvent(String eventId) {
        try {
            // TODO: Implementar verificação se evento pode ser deletado
            // Por enquanto, sempre retorna true
            return eventId != null && !eventId.trim().isEmpty();
            
        } catch (Exception e) {
            log.error("Erro ao verificar se evento {} pode ser deletado: {}", eventId, e.getMessage());
            return false;
        }
    }

    /**
     * Classe para transportar informações do card para criação de eventos.
     */
    public static class CardEventInfo {
        private final Long cardId;
        private final String title;
        private final String description;
        private final LocalDateTime scheduledDate;
        private final LocalDateTime dueDate;

        public CardEventInfo(Long cardId, String title, String description, 
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
