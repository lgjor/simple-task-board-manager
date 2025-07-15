package org.desviante.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.persistence.entity.TaskEntity;
import org.desviante.util.JPAUtil;

import java.time.OffsetDateTime;

public class TaskService {

    /**
     * Cria uma nova tarefa e a associa a um card existente.
     * A operação é transacional, garantindo a integridade dos dados.
     *
     * @param cardId    O ID do card ao qual a tarefa será associada.
     * @param listTitle O título da lista de tarefas (ex: nome do board).
     * @param title     O título da tarefa.
     * @param notes     A descrição/notas da tarefa.
     * @param due       A data e hora de vencimento da tarefa.
     */
    public void createTask(Long cardId, String listTitle, String title, String notes, OffsetDateTime due) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            CardEntity card = em.find(CardEntity.class, cardId);
            if (card == null) {
                throw new EntityNotFoundException("Card com ID " + cardId + " não encontrado.");
            }

            TaskEntity task = new TaskEntity();
            task.setListTitle(listTitle);
            task.setTitle(title);
            task.setDue(due);
            task.setNotes(notes);
            task.setSent(false); // Valor padrão para novas tarefas
            task.setCard(card);  // Associa a tarefa ao card

            em.persist(task); // Salva a nova tarefa no banco

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Falha ao criar a tarefa: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
