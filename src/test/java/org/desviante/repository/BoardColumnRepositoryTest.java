package org.desviante.repository;

import org.desviante.config.TestDataConfig;
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

/**
 * Testes de integração para o BoardColumnRepository.
 * 
 * <p>Estes testes verificam as operações CRUD básicas do BoardColumnRepository,
 * incluindo inserção, busca, atualização e exclusão de colunas. Os testes
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
 * @see BoardColumnRepository
 * @see BoardColumn
 * @see Board
 */
@SpringJUnitConfig(classes = TestDataConfig.class)
@Sql(scripts = "/test-schema.sql") // CORREÇÃO: Garante que o schema seja criado antes dos testes.
@Transactional // Garante que cada teste rode em uma transação isolada e seja revertido
class BoardColumnRepositoryTest {

    @Autowired
    private BoardColumnRepository columnRepository;

    @Autowired
    private BoardRepository boardRepository;

    private Board testBoard;
    private BoardColumn testColumn;

    @BeforeEach
    void setUp() {
        // Criar um board de teste para as colunas
        testBoard = boardRepository.save(new Board(null, "Board de Teste para Colunas", LocalDateTime.now(), null, null));
        
        // Criar uma coluna de teste
        testColumn = new BoardColumn(null, "Coluna de Teste", 0, BoardColumnKindEnum.INITIAL, testBoard.getId());
        testColumn = columnRepository.save(testColumn);
    }

    @AfterEach
    void tearDown() {
        // Limpeza automática pelo @Transactional
    }

    @Test
    @DisplayName("Deve salvar uma nova coluna com sucesso")
    void deveSalvarUmaNovaColunaComSucesso() {
        // ARRANGE
        BoardColumn newColumn = new BoardColumn(null, "Nova Coluna", 1, BoardColumnKindEnum.PENDING, testBoard.getId());

        // ACT
        BoardColumn savedColumn = columnRepository.save(newColumn);

        // ASSERT
        assertNotNull(savedColumn);
        assertNotNull(savedColumn.getId());
        assertEquals("Nova Coluna", savedColumn.getName());
        assertEquals(1, savedColumn.getOrderIndex());
        assertEquals(BoardColumnKindEnum.PENDING, savedColumn.getKind());
        assertEquals(testBoard.getId(), savedColumn.getBoardId());
    }

    @Test
    @DisplayName("Deve encontrar uma coluna pelo ID")
    void deveEncontrarUmaColunaPeloId() {
        // ACT
        Optional<BoardColumn> foundColumn = columnRepository.findById(testColumn.getId());

        // ASSERT
        assertTrue(foundColumn.isPresent());
        assertEquals(testColumn.getId(), foundColumn.get().getId());
        assertEquals("Coluna de Teste", foundColumn.get().getName());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para ID inexistente")
    void deveRetornarOptionalVazioParaIdInexistente() {
        // ACT
        Optional<BoardColumn> foundColumn = columnRepository.findById(999L);

        // ASSERT
        assertTrue(foundColumn.isEmpty());
    }

    @Test
    @DisplayName("Deve encontrar colunas por board ID")
    void deveEncontrarColunasPorBoardId() {
        // ARRANGE - Criar mais uma coluna no mesmo board
        BoardColumn secondColumn = new BoardColumn(null, "Segunda Coluna", 1, BoardColumnKindEnum.FINAL, testBoard.getId());
        columnRepository.save(secondColumn);

        // ACT
        List<BoardColumn> columns = columnRepository.findByBoardId(testBoard.getId());

        // ASSERT
        assertNotNull(columns);
        assertTrue(columns.size() >= 2);
        assertTrue(columns.stream().anyMatch(col -> "Coluna de Teste".equals(col.getName())));
        assertTrue(columns.stream().anyMatch(col -> "Segunda Coluna".equals(col.getName())));
    }

    @Test
    @DisplayName("Deve atualizar uma coluna existente")
    void deveAtualizarUmaColunaExistente() {
        // ARRANGE
        String newName = "Coluna Atualizada";
        int newOrderIndex = 5;

        // ACT
        testColumn.setName(newName);
        testColumn.setOrderIndex(newOrderIndex);
        BoardColumn updatedColumn = columnRepository.save(testColumn);

        // ASSERT
        assertEquals(newName, updatedColumn.getName());
        assertEquals(newOrderIndex, updatedColumn.getOrderIndex());
        assertEquals(testColumn.getId(), updatedColumn.getId());
    }

    @Test
    @DisplayName("Deve deletar uma coluna com sucesso")
    void deveDeletarUmaColunaComSucesso() {
        // ACT
        columnRepository.deleteById(testColumn.getId());

        // ASSERT
        Optional<BoardColumn> foundColumn = columnRepository.findById(testColumn.getId());
        assertFalse(foundColumn.isPresent());
    }
}
