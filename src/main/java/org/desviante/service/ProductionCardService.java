package org.desviante.service;

import jakarta.persistence.EntityManager;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.util.JPAUtil;

import java.util.Optional;
import java.util.function.Function;

/**
 * Implementação de ICardService para o ambiente de produção.
 * Esta classe atua como um Decorator, envolvendo a lógica pura de CardService
 * com o gerenciamento de EntityManager e transações.
 */
public class ProductionCardService implements ICardService {

    private <T> T executeWriteInTransaction(Function<CardService, T> operation) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            try {
                em.getTransaction().begin();
                T result = operation.apply(new CardService(em));
                em.getTransaction().commit();
                return result;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Falha na operação de escrita do serviço de card: " + e.getMessage(), e);
            }
        }
    }

    // O método sobrecarregado que aceitava um Consumer foi removido para eliminar a ambiguidade.
    // Todas as operações de escrita agora usarão a versão que aceita uma Function.
    private <T> T executeRead(Function<CardService, T> operation) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return operation.apply(new CardService(em));
        }
    }

    @Override
    public CardEntity createCard(long boardId, String title, String description) {
        return executeWriteInTransaction(service -> service.createCard(boardId, title, description));
    }

    @Override
    public void moveCard(Long cardId, Long targetColumnId) {
        executeWriteInTransaction(service -> {
            service.moveCard(cardId, targetColumnId);
            return null; // Retornar null para satisfazer a assinatura da Function
        });
    }

    @Override
    public Optional<CardEntity> findById(Long cardId) {
        return executeRead(service -> service.findById(cardId));
    }

    @Override
    public void updateCard(Long cardId, String newTitle, String newDescription) {
        executeWriteInTransaction(service -> {
            service.updateCard(cardId, newTitle, newDescription);
            return null; // Retornar null para satisfazer a assinatura da Function
        });
    }

    @Override
    public void delete(Long cardId) {
        executeWriteInTransaction(service -> {
            service.delete(cardId);
            return null; // Retornar null para satisfazer a assinatura da Function
        });
    }
}