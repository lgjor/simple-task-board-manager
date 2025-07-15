package org.desviante.service;

import org.desviante.persistence.entity.TaskEntity;

import java.time.OffsetDateTime;

/**
 * Interface que define o contrato para serviços relacionados a Tasks.
 * Abstrai a implementação, permitindo diferentes estratégias (ex: produção vs. teste).
 */
public interface ITaskService {
    /**
     * Cria uma nova tarefa associada a um card.
     * @return A entidade TaskEntity criada e persistida.
     */
    TaskEntity createTask(Long cardId, String listTitle, String title, String notes, OffsetDateTime due);
}