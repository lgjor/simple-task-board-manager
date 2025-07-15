package org.desviante.test;

import jakarta.persistence.EntityManager;
import org.desviante.persistence.entity.*;
import org.desviante.util.JPAUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskRepositoryTest {

    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        this.entityManager = JPAUtil.getEntityManager();
    }

    @AfterEach
    void tearDown() {
        if (this.entityManager != null && this.entityManager.isOpen()) {
            this.entityManager.close();
        }
    }

    @Test
    @DisplayName("Deve salvar uma nova tarefa e encontrá-la pelo ID")
    void shouldPersistAndFindTaskById() {
        // --- 1. Cenário: Preparar a estrutura de dependências ---

        // Criar um Board
        BoardEntity board = new BoardEntity();
        board.setName("Quadro de Teste");

        // Criar uma Coluna e associar ao Board
        BoardColumnEntity column = new BoardColumnEntity();
        column.setName("A Fazer");
        column.setKind(BoardColumnKindEnum.INITIAL); // Certifique-se que este Enum existe
        column.setOrder_index(0);
        // O método auxiliar `addBoardColumn` já deve cuidar da relação bidirecional
        board.addBoardColumn(column);

        // Criar um Card e associar à Coluna e ao Board
        CardEntity card = new CardEntity();
        card.setTitle("Card de Teste");
        card.setBoard(board);
        // O método auxiliar `addCard` já deve cuidar da relação bidirecional
        column.addCard(card);

        // Finalmente, criar a Tarefa e associá-la ao Card
        TaskEntity newTask = new TaskEntity();
        newTask.setTitle("Testar o Hibernate");
        newTask.setNotes("Criar um teste de integração para a entidade Task.");
        newTask.setSent(false);
        newTask.setCard(card); // Associando a tarefa ao card

        // --- 2. Ação: Salvar a hierarquia de entidades ---
        try {
            entityManager.getTransaction().begin();
            // Como BoardEntity tem Cascade.ALL para suas colunas, e colunas para seus cards,
            // persistir o board deve persistir a hierarquia.
            entityManager.persist(board);
            // A tarefa precisa ser persistida separadamente, pois não há cascade a partir do card.
            entityManager.persist(newTask);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            fail("Falha ao salvar a hierarquia de entidades", e);
        }

        assertNotNull(newTask.getId(), "O ID da tarefa não deveria ser nulo após persistir.");
        assertNotNull(card.getId(), "O ID do card não deveria ser nulo após persistir.");

        // --- 3. Verificação: Buscar a tarefa salva e verificar os dados ---
        TaskEntity foundTask = entityManager.find(TaskEntity.class, newTask.getId());

        assertNotNull(foundTask, "A tarefa deveria ter sido encontrada no banco de dados.");
        assertEquals("Testar o Hibernate", foundTask.getTitle());
        assertNotNull(foundTask.getCard(), "A tarefa encontrada deveria estar associada a um card.");
        assertEquals(card.getId(), foundTask.getCard().getId(), "O ID do card associado não corresponde.");
        assertEquals(board.getName(), foundTask.getCard().getBoard().getName(), "O nome do board associado não corresponde.");
    }
}