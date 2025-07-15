package org.desviante.service;

import org.desviante.persistence.entity.CardEntity;

import java.util.Optional;

/**
 * Interface que define o contrato para serviços relacionados a Cards.
 * Abstrai a implementação, permitindo diferentes estratégias (ex: produção vs. teste).
 */
public interface ICardService {

    /**
     * Cria um novo card no sistema.
     */
    CardEntity createCard(long boardId, String title, String description);

    /**
     * Move um card para uma nova coluna.
     * @param cardId O ID do card a ser movido.
     * @param targetColumnId O ID da coluna de destino.
     */
    void moveCard(Long cardId, Long targetColumnId);

    Optional<CardEntity> findById(Long cardId);
    void updateCard(Long cardId, String newTitle, String newDescription);
    void delete(Long cardId);
}