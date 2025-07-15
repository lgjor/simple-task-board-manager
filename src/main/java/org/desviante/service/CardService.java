package org.desviante.service;

import jakarta.persistence.EntityManager;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.util.JPAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

public class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    public void moveCard(Long cardId, Long targetColumnId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            CardEntity card = em.find(CardEntity.class, cardId);
            BoardColumnEntity targetColumn = em.find(BoardColumnEntity.class, targetColumnId);

            if (card == null || targetColumn == null) {
                throw new IllegalArgumentException("Card ou Coluna de destino não encontrados.");
            }

            // A FORMA CORRETA DE MOVER O CARD:
            // Apenas atualize o lado "dono" da relação (o lado ManyToOne).
            // Não manipule as coleções 'sourceColumn.getCards()' ou 'targetColumn.getCards()' diretamente.
            // O Hibernate cuidará de sincronizar as coleções com base nesta mudança.
            card.setBoardColumn(targetColumn);
            card.setLastUpdateDate(LocalDateTime.now());

            em.getTransaction().commit();
            logger.info("Card {} movido para a coluna {}", cardId, targetColumnId);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erro ao mover o card: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Cria um novo card, associando-o a um board e colocando-o na coluna inicial do board.
     * @param boardId O ID do board ao qual o card pertencerá.
     * @param title O título do card.
     * @param description A descrição do card.
     * @return A entidade CardEntity recém-criada e persistida.
     */
    public CardEntity createCard(Long boardId, String title, String description) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            BoardEntity board = em.find(BoardEntity.class, boardId);
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

            em.persist(card);
            em.getTransaction().commit();

            return card;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erro ao criar o card: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public Optional<CardEntity> findById(Long cardId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(CardEntity.class, cardId));
        }
    }

    public void updateCard(Long cardId, String newTitle, String newDescription) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CardEntity card = em.find(CardEntity.class, cardId);
            if (card != null) {
                card.setTitle(newTitle);
                card.setDescription(newDescription);
                card.setLastUpdateDate(LocalDateTime.now());
            } else {
                throw new IllegalArgumentException("Card com ID " + cardId + " não encontrado para atualização.");
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erro ao atualizar o card: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void delete(Long cardId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CardEntity card = em.find(CardEntity.class, cardId);
            if (card != null) {
                em.remove(card);
            } else {
                throw new IllegalArgumentException("Card com ID " + cardId + " não encontrado para exclusão.");
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erro ao deletar o card: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}