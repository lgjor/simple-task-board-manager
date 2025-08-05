package org.desviante.service;

import org.desviante.config.DataConfig;
import org.desviante.config.TestDataSourceConfig;
import org.desviante.model.Board;
import org.desviante.repository.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração para a BoardService.
 * Carrega um contexto Spring real com a configuração de dados (DataConfig),
 * usando um banco de dados H2 em memória para os testes.
 */
@SpringJUnitConfig(classes = BoardServiceIntegrationTest.TestConfig.class)
@Sql(scripts = "/test-schema.sql")
@Transactional
class BoardServiceIntegrationTest {

    /**
     * Configuração de contexto específica para este teste de integração.
     * Ela importa a configuração de dados real e APENAS o serviço que está sendo testado.
     * Isso evita carregar outros serviços (como TaskService) que têm dependências
     * complexas (como a API do Google) que não são necessárias para este teste.
     */
    @Configuration
    @Import({TestDataSourceConfig.class, BoardService.class, BoardRepository.class})
    static class TestConfig {
        // Classe de configuração vazia, usada apenas para as anotações.
    }

    @Autowired
    private BoardService boardService;

    // Os métodos de teste (@Test) permanecem exatamente os mesmos.
    @Test
    @DisplayName("Deve criar e encontrar um board com sucesso")
    void shouldCreateAndFindBoard() {
        // Act
        Board createdBoard = boardService.createBoard("Meu Board de Integração");

        // Assert
        assertNotNull(createdBoard);
        assertNotNull(createdBoard.getId());
        assertEquals("Meu Board de Integração", createdBoard.getName());

        // Act 2
        Optional<Board> foundBoard = boardService.getBoardById(createdBoard.getId());

        // Assert 2
        assertTrue(foundBoard.isPresent());
        assertEquals(createdBoard.getId(), foundBoard.get().getId());
    }

    @Test
    @DisplayName("Deve deletar um board e não encontrá-lo depois")
    void shouldDeleteBoard() {
        // Arrange
        Board createdBoard = boardService.createBoard("Board para Deletar");
        Long boardId = createdBoard.getId();
        assertTrue(boardService.getBoardById(boardId).isPresent(), "Board deveria existir antes de deletar.");

        // Act
        boardService.deleteBoard(boardId);

        // Assert
        assertTrue(boardService.getBoardById(boardId).isEmpty(), "Board não deveria ser encontrado após deletar.");
    }

    @Test
    @DisplayName("Deve atualizar o nome de um board com sucesso")
    void shouldUpdateBoardName() {
        // Arrange: Cria um board inicial.
        Board createdBoard = boardService.createBoard("Nome Antigo");
        Long boardId = createdBoard.getId();

        // Act: Chama o serviço para atualizar o nome.
        Optional<Board> updatedBoardOpt = boardService.updateBoardName(boardId, "Nome Novo");

        // Assert: Verifica se o Optional não está vazio e se o nome foi atualizado.
        assertTrue(updatedBoardOpt.isPresent(), "O board atualizado não deveria ser nulo.");
        assertEquals("Nome Novo", updatedBoardOpt.get().getName());

        // Assert (Verificação extra): Busca novamente no banco para garantir a persistência.
        Optional<Board> foundAfterUpdate = boardService.getBoardById(boardId);
        assertTrue(foundAfterUpdate.isPresent());
        assertEquals("Nome Novo", foundAfterUpdate.get().getName());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao tentar atualizar um board inexistente")
    void updateBoardName_shouldReturnEmpty_whenBoardNotFound() {
        // Act: Tenta atualizar um board com um ID que não existe.
        Optional<Board> result = boardService.updateBoardName(999L, "Qualquer Nome");

        // Assert: O resultado deve ser um Optional vazio.
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar uma lista com todos os boards")
    void shouldGetAllBoards() {
        // Act: Chama o serviço para buscar todos os boards.
        List<Board> boards = boardService.getAllBoards();

        // Assert: Verifica se a lista não é nula e tem o tamanho esperado.
        // A implementação do repositório ordena por nome, então podemos verificar a ordem.
        // Temos apenas o board de exemplo
        assertNotNull(boards);
        assertEquals(1, boards.size());
        assertEquals("Board de Exemplo", boards.get(0).getName());
    }
}