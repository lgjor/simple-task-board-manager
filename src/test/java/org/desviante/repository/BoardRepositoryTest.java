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

@SpringJUnitConfig(classes = TestDataConfig.class)
@Sql(scripts = "/test-schema.sql") // CORREÇÃO: Garante que o schema seja criado antes dos testes.
@Transactional // Garante que cada teste rode em uma transação isolada e seja revertido
public class BoardRepositoryTest {

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
    @DisplayName("Deve encontrar um board pelo seu ID e retornar todos os dados")
    void findById_shouldReturnBoard_whenBoardExists() {
        // ARRANGE
        LocalDateTime creationTime = LocalDateTime.now();
        Board boardToSave = new Board(null, "Board de Teste", creationTime, null, null);
        Board savedBoard = boardRepository.save(boardToSave);

        // ACT
        Optional<Board> foundBoardOpt = boardRepository.findById(savedBoard.getId());

        // ASSERT
        assertTrue(foundBoardOpt.isPresent());
        Board foundBoard = foundBoardOpt.get();
        assertEquals(savedBoard.getId(), foundBoard.getId());
        assertEquals("Board de Teste", foundBoard.getName());

        // ASERÇÃO ROBUSTA
        assertEquals(
                creationTime.truncatedTo(ChronoUnit.SECONDS),
                foundBoard.getCreationDate().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o board não existe")
    void findById_shouldReturnEmpty_whenBoardDoesNotExist() {
        // ACT
        Optional<Board> foundBoard = boardRepository.findById(999L);
        // ASSERT
        assertTrue(foundBoard.isEmpty());
    }

    @Test
    @DisplayName("Deve atualizar o nome de um board existente sem alterar a data de criação")
    void save_shouldUpdateName_whenBoardExists() {
        // ARRANGE
        LocalDateTime creationTime = LocalDateTime.now();
        Board boardToSave = new Board(null, "Nome Antigo", creationTime, null, null);
        Board savedBoard = boardRepository.save(boardToSave);

        // ACT
        savedBoard.setName("Nome Novo");
        boardRepository.save(savedBoard);

        // ASSERT
        Optional<Board> updatedBoardOpt = boardRepository.findById(savedBoard.getId());

        assertTrue(updatedBoardOpt.isPresent());
        Board updatedBoard = updatedBoardOpt.get();
        assertEquals("Nome Novo", updatedBoard.getName());

        // ASERÇÃO ROBUSTA
        assertEquals(
                creationTime.truncatedTo(ChronoUnit.SECONDS),
                updatedBoard.getCreationDate().truncatedTo(ChronoUnit.SECONDS),
                "A data de criação não deve ser alterada na atualização."
        );
    }

    @Test
    @DisplayName("Deve deletar um board pelo seu ID")
    void deleteById_shouldRemoveBoard() {
        // ARRANGE
        Board boardToSave = new Board(null, "Board a Deletar", LocalDateTime.now(), null, null);
        Board savedBoard = boardRepository.save(boardToSave);
        Long id = savedBoard.getId();

        // ACT
        boardRepository.deleteById(id);

        // ASSERT
        Optional<Board> deletedBoard = boardRepository.findById(id);
        assertTrue(deletedBoard.isEmpty());
    }
}
