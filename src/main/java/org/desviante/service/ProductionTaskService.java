package org.desviante.service;

import jakarta.persistence.EntityManager;
import org.desviante.persistence.entity.TaskEntity;
import org.desviante.util.JPAUtil;

import java.time.OffsetDateTime;
import java.util.function.Function;

/**
 * Implementação de ITaskService para o ambiente de produção.
 * Esta classe atua como um Decorator, envolvendo a lógica pura de TaskService
 * com o gerenciamento de EntityManager e transações.
 */
public class ProductionTaskService implements ITaskService {

    private <T> T executeWriteInTransaction(Function<TaskService, T> operation) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            try {
                em.getTransaction().begin();
                // Assume que TaskService foi refatorado para aceitar um EntityManager.
                T result = operation.apply(new TaskService(em));
                em.getTransaction().commit();
                return result;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Falha na operação de escrita do serviço de task: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public TaskEntity createTask(Long cardId, String listTitle, String title, String notes, OffsetDateTime due) {
        return executeWriteInTransaction(service -> service.createTask(cardId, listTitle, title, notes, due));
    }
}