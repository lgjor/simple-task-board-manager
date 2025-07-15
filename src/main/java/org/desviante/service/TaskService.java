package org.desviante.service;

import jakarta.persistence.EntityManager;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

/**
 * Classe de lógica de negócio "pura" para Tasks.
 * Depende de um EntityManager injetado e não gerencia transações.
 */
public class TaskService implements ITaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final EntityManager entityManager;

    /**
     * Construtor que implementa a Injeção de Dependência.
     * @param entityManager O EntityManager a ser usado por todas as operações do serviço.
     */
    public TaskService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public TaskEntity createTask(Long cardId, String listTitle, String title, String notes, OffsetDateTime due) {
        CardEntity card = entityManager.find(CardEntity.class, cardId);
        if (card == null) {
            throw new IllegalArgumentException("Card com ID " + cardId + " não encontrado para criar a tarefa.");
        }

        TaskEntity task = new TaskEntity();
        task.setCard(card);
        task.setListTitle(listTitle);
        task.setTitle(title);
        task.setNotes(notes);
        task.setDue(due);
        task.setSent(false); // Define o status inicial como não enviado

        entityManager.persist(task);
        logger.info("Tarefa persistida para o card ID: {}", cardId);
        return task;
    }
}
