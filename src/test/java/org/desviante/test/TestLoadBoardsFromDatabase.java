package org.desviante.test;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
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

    @BeforeEach
    void setUp() {
        // Uma nova instância do serviço é criada antes de cada teste
        // para garantir o isolamento.
        this.boardService = new BoardService();
    }

    @Test
    @DisplayName("Deve criar um novo board e encontrá-lo na lista de todos os boards")
    void shouldCreateAndFindBoardInList() {
        // --- 1. CENÁRIO: Preparar os dados ---
        // Primeiro, criamos um board para garantir que o banco de dados não esteja vazio.
        // Usamos um nome único para evitar conflitos com outros testes.
        String boardName = "Board de Teste - " + System.currentTimeMillis();
        BoardEntity createdBoard = boardService.createBoardWithDefaultColumns(boardName);

        // Verificação primária: o board foi criado com sucesso?
        assertNotNull(createdBoard, "O board não deveria ser nulo após a criação.");
        assertNotNull(createdBoard.getId(), "O board criado deveria ter um ID gerado pelo banco.");
        assertEquals(boardName, createdBoard.getName(), "O nome do board criado não corresponde.");

        // --- 2. AÇÃO: Executar o método que estamos testando ---
        // Chamamos o método refatorado para buscar todos os boards.
        List<BoardEntity> boards = boardService.findAllWithColumns();

        // --- 3. VERIFICAÇÃO: O resultado é o esperado? ---
        assertNotNull(boards, "A lista de boards retornada não pode ser nula.");
        assertFalse(boards.isEmpty(), "A lista de boards não deveria estar vazia após a criação de um.");

        // Verificamos se o board que acabamos de criar está presente na lista retornada.
        boolean foundInList = boards.stream()
                .anyMatch(b -> b.getId().equals(createdBoard.getId()));

        assertTrue(foundInList, "O board recém-criado não foi encontrado na lista de boards.");

        // --- 4. LIMPEZA (Opcional, mas recomendado) ---
        // Para manter o banco de dados limpo para execuções futuras,
        // deletamos o board que criamos para este teste.
        boardService.delete(createdBoard.getId());
    }
}