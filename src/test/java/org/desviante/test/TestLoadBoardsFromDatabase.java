package org.desviante.test;

import jakarta.persistence.EntityManager;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
import org.desviante.util.TestJPAUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração para o BoardService.
 * Verifica se a criação e a busca de boards estão funcionando corretamente com o banco de dados.
 */
class TestLoadBoardsFromDatabase {

    private BoardService boardService;
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 1. Crie um EntityManager de teste.
        this.entityManager = TestJPAUtil.createEntityManager();
        // 2. Injete-o no serviço.
        this.boardService = new BoardService(this.entityManager);
    }

    @AfterEach
    void tearDown() {
        if (this.entityManager != null && this.entityManager.isOpen()) {
            this.entityManager.close();
        }
    }

    @Test
    @DisplayName("Deve criar um novo board e encontrá-lo na lista de todos os boards")
    void shouldCreateAndFindBoardInList() {
        // --- 1. CENÁRIO: Preparar os dados ---
        String boardName = "Board de Teste - " + System.currentTimeMillis();
        BoardEntity createdBoard;

        // Os testes agora devem gerenciar suas próprias transações, pois o serviço foi desacoplado.
        try {
            entityManager.getTransaction().begin();
            createdBoard = boardService.createBoardWithDefaultColumns(boardName);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            fail("Falha ao criar o board no teste.", e);
            return; // Encerra o teste se a preparação falhar.
        }

        // Verificação primária: o board foi criado com sucesso?
        assertNotNull(createdBoard, "O board não deveria ser nulo após a criação.");
        assertNotNull(createdBoard.getId(), "O board criado deveria ter um ID gerado pelo banco.");
        assertEquals(boardName, createdBoard.getName(), "O nome do board criado não corresponde.");

        // --- 2. AÇÃO: Buscar todos os boards ---
        List<BoardEntity> boards = boardService.findAllWithColumns();

        // --- 3. VERIFICAÇÃO ---
        assertNotNull(boards, "A lista de boards retornada não pode ser nula.");
        assertFalse(boards.isEmpty(), "A lista de boards não deveria estar vazia após a criação de um.");

        boolean foundInList = boards.stream()
                .anyMatch(b -> b.getId().equals(createdBoard.getId()));

        assertTrue(foundInList, "O board recém-criado não foi encontrado na lista de boards.");

        // --- 4. LIMPEZA ---
        entityManager.getTransaction().begin();
        boardService.delete(createdBoard.getId()); // Deleta o board de teste
        entityManager.getTransaction().commit();
    }
}