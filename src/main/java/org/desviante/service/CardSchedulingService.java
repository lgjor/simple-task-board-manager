package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.calendar.CalendarService;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Card;
import org.desviante.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Serviço dedicado para gerenciar apenas as datas de agendamento e vencimento dos cards.
 * 
 * <p>Esta classe é responsável exclusivamente por salvar e atualizar as datas
 * de agendamento (scheduled_date) e vencimento (due_date) dos cards, sem
 * realizar integrações com sistemas externos como calendário ou Google Tasks.</p>
 * 
 * <p>Esta separação de responsabilidades garante que o salvamento das datas
 * seja sempre bem-sucedido, independentemente de falhas em integrações externas,
 * evitando problemas de rollback transacional.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Card
 * @see CardRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardSchedulingService {

    private final CardRepository cardRepository;
    private final CalendarService calendarService;

    /**
     * Define as datas de agendamento e vencimento de um card.
     * 
     * <p>Esta operação é transacional e isolada, garantindo que as datas
     * sejam salvas com sucesso independentemente de outras operações.</p>
     * 
     * <p>Validações realizadas:</p>
     * <ul>
     *   <li>Card deve existir</li>
     *   <li>Data de vencimento não pode ser anterior à data de agendamento</li>
     * </ul>
     * 
     * @param cardId identificador do card
     * @param scheduledDate data de agendamento (pode ser null)
     * @param dueDate data de vencimento (pode ser null)
     * @return card atualizado
     * @throws ResourceNotFoundException se o card não for encontrado
     * @throws IllegalArgumentException se as datas forem inválidas
     */
    @Transactional
    public Card setSchedulingDates(Long cardId, LocalDateTime scheduledDate, LocalDateTime dueDate) {
        log.debug("Definindo datas de agendamento para card {}: agendamento={}, vencimento={}", 
                 cardId, scheduledDate, dueDate);
        
        // Buscar o card
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));

        // Validar datas se ambas estiverem preenchidas
        if (scheduledDate != null && dueDate != null && dueDate.isBefore(scheduledDate)) {
            throw new IllegalArgumentException("Data de vencimento não pode ser anterior à data de agendamento");
        }

        // Atualizar as datas
        card.setScheduledDate(scheduledDate);
        card.setDueDate(dueDate);
        
        // Salvar no banco
        Card updatedCard = cardRepository.save(card);
        
        log.debug("Datas de agendamento salvas com sucesso para card {}: agendamento={}, vencimento={}", 
                 cardId, scheduledDate, dueDate);
        
        return updatedCard;
    }

    /**
     * Obtém um card pelo ID.
     * 
     * @param cardId identificador do card
     * @return Optional contendo o card se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Card> getCardById(Long cardId) {
        return cardRepository.findById(cardId);
    }

    /**
     * Remove apenas a data de agendamento de um card.
     * 
     * @param cardId identificador do card
     * @return card atualizado
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    @Transactional
    public Card clearScheduledDate(Long cardId) {
        log.debug("Removendo data de agendamento do card {}", cardId);
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));
        
        card.setScheduledDate(null);
        card.setLastUpdateDate(LocalDateTime.now());
        Card updatedCard = cardRepository.save(card);
        
        log.debug("Data de agendamento removida do card {}", cardId);
        return updatedCard;
    }

    /**
     * Remove a data de agendamento de um card e também remove o evento relacionado do calendário.
     * 
     * <p>Esta versão remove tanto a data quanto o evento, para casos onde a exclusão
     * é feita diretamente no card (não através do calendário).</p>
     * 
     * @param cardId identificador do card
     * @return card atualizado
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    @Transactional
    public Card clearScheduledDateWithEvent(Long cardId) {
        log.debug("Removendo data de agendamento do card {} e evento relacionado", cardId);
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));
        
        boolean hadScheduledDate = card.getScheduledDate() != null;
        card.setScheduledDate(null);
        card.setLastUpdateDate(LocalDateTime.now());
        Card updatedCard = cardRepository.save(card);
        
        // Se o card tinha data de agendamento, remover evento relacionado do calendário
        if (hadScheduledDate) {
            try {
                calendarService.deleteEvent(cardId);
                log.debug("Evento do calendário removido para card {} (data de agendamento limpa)", cardId);
            } catch (Exception e) {
                log.warn("Erro ao remover evento do calendário para card {}: {}", cardId, e.getMessage());
                // Não falha a operação se não conseguir remover o evento
            }
        }
        
        log.debug("Data de agendamento removida do card {} com evento", cardId);
        return updatedCard;
    }

    /**
     * Remove apenas a data de vencimento de um card.
     * 
     * @param cardId identificador do card
     * @return card atualizado
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    @Transactional
    public Card clearDueDate(Long cardId) {
        log.debug("Removendo data de vencimento do card {}", cardId);
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));
        
        card.setDueDate(null);
        Card updatedCard = cardRepository.save(card);
        
        log.debug("Data de vencimento removida do card {}", cardId);
        return updatedCard;
    }
}
