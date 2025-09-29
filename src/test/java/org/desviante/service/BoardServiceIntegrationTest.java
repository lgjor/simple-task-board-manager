package org.desviante.service;

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
 * 
 * <p>Carrega um contexto Spring real com a configuração de dados (DataConfig),
 * usando um banco de dados H2 em memória para os testes.</p>
 * 
 * <p>Testa o comportamento real do sistema ao gerenciar boards, incluindo
 * criação, busca e remoção com persistência real no banco de dados.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardService
 * @see Board
 * @see BoardRepository
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
    void shouldDeleteBoardAndNotFindItAfterwards() {
        // Arrange
        Board createdBoard = boardService.createBoard("Board para Deletar");

        // Act
        boardService.deleteBoard(createdBoard.getId());

        // Assert
        Optional<Board> foundBoard = boardService.getBoardById(createdBoard.getId());
        assertFalse(foundBoard.isPresent());
    }

    @Test
    @DisplayName("Deve listar todos os boards criados")
    void shouldListAllCreatedBoards() {
        // Arrange
        Board board1 = boardService.createBoard("Board 1");
        Board board2 = boardService.createBoard("Board 2");

        // Act
        List<Board> allBoards = boardService.getAllBoards();

        // Assert
        assertTrue(allBoards.size() >= 2);
        assertTrue(allBoards.stream().anyMatch(b -> "Board 1".equals(b.getName())));
        assertTrue(allBoards.stream().anyMatch(b -> "Board 2".equals(b.getName())));
        
        // Verificar se os boards criados estão na lista usando seus IDs
        assertTrue(allBoards.stream().anyMatch(b -> b.getId().equals(board1.getId())));
        assertTrue(allBoards.stream().anyMatch(b -> b.getId().equals(board2.getId())));
    }
}
