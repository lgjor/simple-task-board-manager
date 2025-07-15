package org.desviante.service;

import jakarta.persistence.EntityManager;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.CardEntity; // Mantido
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.Optional;

public class CardService implements ICardService {

    private final EntityManager entityManager;

    public CardService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    public void moveCard(Long cardId, Long targetColumnId) {
        // A transação é gerenciada pelo ProductionCardService
        CardEntity card = entityManager.find(CardEntity.class, cardId);
        if (card == null) {
            throw new IllegalArgumentException("Card com ID " + cardId + " não encontrado.");
        }

        BoardColumnEntity targetColumn = entityManager.find(BoardColumnEntity.class, targetColumnId);
        if (targetColumn == null) {
            throw new IllegalArgumentException("Coluna de destino com ID " + targetColumnId + " não encontrada.");
        }

        // Remove o card da coluna antiga e adiciona na nova usando os helpers da entidade
        if (card.getBoardColumn() != null) {
            card.getBoardColumn().removeCard(card);
        }
        targetColumn.addCard(card);

        // O entityManager.merge(card) não é estritamente necessário se a entidade
        // já estiver gerenciada, mas é uma prática segura para garantir a sincronização.
        entityManager.merge(card);
    }

    public CardEntity createCard(long boardId, String title, String description) {
        BoardEntity board = this.entityManager.find(BoardEntity.class, boardId);
        if (board == null) {
            throw new IllegalArgumentException("Board com ID " + boardId + " não encontrado.");
        }

        // Encontra a coluna inicial do board para colocar o novo card.
        BoardColumnEntity initialColumn = board.getInitialColumn();

        CardEntity card = new CardEntity();
        card.setTitle(title);
        card.setDescription(description);
        card.setBoard(board);
        card.setBoardColumn(initialColumn); // Associa o card à coluna inicial
        card.setCreationDate(LocalDateTime.now());
        card.setLastUpdateDate(LocalDateTime.now());

        this.entityManager.persist(card);
        return card;
    }

    public Optional<CardEntity> findById(Long cardId) {
        return Optional.ofNullable(this.entityManager.find(CardEntity.class, cardId));
    }

    public void updateCard(Long cardId, String newTitle, String newDescription) {
        CardEntity card = this.entityManager.find(CardEntity.class, cardId);
        if (card != null) {
            card.setTitle(newTitle);
            card.setDescription(newDescription);
            card.setLastUpdateDate(LocalDateTime.now());
        } else {
            throw new IllegalArgumentException("Card com ID " + cardId + " não encontrado para atualização.");
        }
    }

    public void delete(Long cardId) {
        CardEntity card = this.entityManager.find(CardEntity.class, cardId);
        if (card != null) {
            this.entityManager.remove(card);
        } else {
            // Não lançar exceção se o card já não existir é uma prática mais tolerante.
            logger.warn("Tentativa de deletar um card não existente com ID: {}", cardId);
        }
    }
}