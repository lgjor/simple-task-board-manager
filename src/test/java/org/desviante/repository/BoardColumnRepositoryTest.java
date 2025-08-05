package org.desviante.repository;

import org.desviante.config.DataConfig;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql; // Import necessário
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = DataConfig.class)
@Sql(scripts = "/test-schema.sql") // CORREÇÃO: Garante que o schema seja criado antes dos testes.
@Transactional // Garante que cada teste rode em uma transação isolada e seja revertido
public class BoardColumnRepositoryTest {

    @Autowired
    private BoardColumnRepository columnRepository;

    @Autowired
    private BoardRepository boardRepository;

    private Board testBoard;

    @BeforeEach
    void setup() {
        Board boardToSave = new Board(null, "Board de Teste Base", LocalDateTime.now(), null, null);
        testBoard = boardRepository.save(boardToSave);
    }

    @AfterEach
    void cleanup() {
        // Limpar dados de teste na ordem correta (devido às foreign keys)
        if (testBoard != null) {
            boardRepository.deleteById(testBoard.getId());
        }
    }

    @Test
    @DisplayName("Deve salvar uma nova coluna associada a um board")
    void save_shouldInsertNewColumn() {
        // ARRANGE
        BoardColumn newColumn = new BoardColumn(null, "A Fazer", 0, BoardColumnKindEnum.INITIAL, testBoard.getId());

        // ACT
        BoardColumn savedColumn = columnRepository.save(newColumn);

        // ASSERT
        assertNotNull(savedColumn);
        assertNotNull(savedColumn.getId());
        assertEquals("A Fazer", savedColumn.getName());
        assertEquals(testBoard.getId(), savedColumn.getBoardId());
    }

    @Test
    @DisplayName("Deve encontrar colunas pelo ID do board, ordenadas pelo index")
    void findByBoardId_shouldReturnColumns_inOrder() {
        // ARRANGE
        BoardColumn col1 = new BoardColumn(null, "Em Andamento", 1, BoardColumnKindEnum.PENDING, testBoard.getId());
        BoardColumn col0 = new BoardColumn(null, "A Fazer", 0, BoardColumnKindEnum.INITIAL, testBoard.getId());
        columnRepository.save(col1);
        columnRepository.save(col0);

        // ACT
        List<BoardColumn> columns = columnRepository.findByBoardId(testBoard.getId());

        // ASSERT
        assertNotNull(columns);
        assertEquals(2, columns.size());
        assertEquals("A Fazer", columns.get(0).getName());
        assertEquals("Em Andamento", columns.get(1).getName());
    }

    @Test
    @DisplayName("Deve atualizar uma coluna existente")
    void save_shouldUpdateExistingColumn() {
        // ARRANGE
        BoardColumn originalColumn = new BoardColumn(null, "Nome Original", 0, BoardColumnKindEnum.INITIAL, testBoard.getId());
        BoardColumn savedColumn = columnRepository.save(originalColumn);

        // ACT
        savedColumn.setName("Nome Atualizado");
        savedColumn.setOrderIndex(99);
        columnRepository.save(savedColumn);

        // ASSERT
        Optional<BoardColumn> updatedColumnOpt = columnRepository.findById(savedColumn.getId());
        assertTrue(updatedColumnOpt.isPresent());
        BoardColumn updatedColumn = updatedColumnOpt.get();
        assertEquals("Nome Atualizado", updatedColumn.getName());
        assertEquals(99, updatedColumn.getOrderIndex());
    }

    @Test
    @DisplayName("Deve deletar uma coluna pelo seu ID")
    void deleteById_shouldRemoveColumn() {
        // ARRANGE
        BoardColumn columnToDelete = new BoardColumn(null, "Coluna Temporária", 0, BoardColumnKindEnum.INITIAL, testBoard.getId());
        BoardColumn savedColumn = columnRepository.save(columnToDelete);
        Long id = savedColumn.getId();

        // ACT
        columnRepository.deleteById(id);

        // ASSERT
        Optional<BoardColumn> deletedColumn = columnRepository.findById(id);
        assertTrue(deletedColumn.isEmpty());
    }
}
