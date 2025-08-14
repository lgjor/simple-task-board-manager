package org.desviante.repository;

import org.desviante.config.TestDataConfig;
import org.desviante.model.Board;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql; // Import necessário
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para o BoardRepository.
 * 
 * <p>Estes testes verificam as operações CRUD básicas do BoardRepository,
 * incluindo inserção, busca, atualização e exclusão de boards. Os testes
 * utilizam um banco de dados em memória configurado especificamente para
 * testes, garantindo isolamento e limpeza automática dos dados.</p>
 * 
 * <p>Características dos testes:</p>
 * <ul>
 *   <li>Utilizam transações que são revertidas automaticamente</li>
 *   <li>Configuram dados de teste antes de cada teste</li>
 *   <li>Limpam dados de teste após cada teste</li>
 *   <li>Verificam tanto casos de sucesso quanto casos de erro</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardRepository
 * @see Board
 */
@SpringJUnitConfig(classes = TestDataConfig.class)
@Sql(scripts = "/test-schema.sql") // Garante que o schema seja criado antes dos testes.
@Transactional // Garante que cada teste rode em uma transação isolada e seja revertido
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Test
    @DisplayName("Deve salvar um novo board, atribuir um ID e registrar a data de criação")
    void deveSalvarUmNovoBoardEAtribuirUmId() {
        // ARRANGE
        LocalDateTime creationTime = LocalDateTime.now();
        Board newBoard = new Board(null, "New Test Board", creationTime, null, null);

        // ACT
        Board savedBoard = boardRepository.save(newBoard);

        // ASSERT
        assertNotNull(savedBoard);
        assertNotNull(savedBoard.getId());
        assertEquals("New Test Board", savedBoard.getName());
        assertNotNull(savedBoard.getCreationDate());

        // ASERÇÃO ROBUSTA: Comparamos as datas truncadas para o segundo mais próximo.
        assertEquals(
                creationTime.truncatedTo(ChronoUnit.SECONDS),
                savedBoard.getCreationDate().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("Deve encontrar um board pelo ID")
    void deveEncontrarUmBoardPeloId() {
        // ARRANGE
        Board savedBoard = boardRepository.save(new Board(null, "Board para Busca", LocalDateTime.now(), null, null));

        // ACT
        Optional<Board> foundBoard = boardRepository.findById(savedBoard.getId());

        // ASSERT
        assertTrue(foundBoard.isPresent());
        assertEquals(savedBoard.getId(), foundBoard.get().getId());
        assertEquals("Board para Busca", foundBoard.get().getName());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para ID inexistente")
    void deveRetornarOptionalVazioParaIdInexistente() {
        // ACT
        Optional<Board> foundBoard = boardRepository.findById(999L);

        // ASSERT
        assertTrue(foundBoard.isEmpty());
    }

    @Test
    @DisplayName("Deve atualizar um board existente")
    void deveAtualizarUmBoardExistente() {
        // ARRANGE
        Board savedBoard = boardRepository.save(new Board(null, "Nome Original", LocalDateTime.now(), null, null));
        String newName = "Nome Atualizado";

        // ACT
        savedBoard.setName(newName);
        Board updatedBoard = boardRepository.save(savedBoard);

        // ASSERT
        assertEquals(newName, updatedBoard.getName());
        assertEquals(savedBoard.getId(), updatedBoard.getId());
    }

    @Test
    @DisplayName("Deve deletar um board com sucesso")
    void deveDeletarUmBoardComSucesso() {
        // ARRANGE
        Board savedBoard = boardRepository.save(new Board(null, "Board para Deletar", LocalDateTime.now(), null, null));

        // ACT
        boardRepository.deleteById(savedBoard.getId());

        // ASSERT
        Optional<Board> foundBoard = boardRepository.findById(savedBoard.getId());
        assertFalse(foundBoard.isPresent());
    }
}
