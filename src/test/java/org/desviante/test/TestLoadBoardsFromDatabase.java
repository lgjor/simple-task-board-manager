package org.desviante.test;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Classe de teste para o método {@link BoardService#loadBoardsFromDatabase()}.
 *
 * <p>Este teste verifica se a lógica de carregamento dos boards do banco de dados
 * está funcionando corretamente, garantindo que a lista retornada:
 * <ul>
 *     <li>não seja nula;</li>
 *     <li>não esteja vazia;</li>
 *     <li>contenha boards com colunas devidamente carregadas.</li>
 * </ul>
 *
 * <p>Este teste pressupõe que o banco de dados esteja acessível e populado com
 * dados válidos.
 */
public class TestLoadBoardsFromDatabase {

    /**
     * Testa o método {@code loadBoardsFromDatabase} garantindo que ele retorne
     * uma lista válida de boards com colunas associadas.
     *
     * @throws SQLException se ocorrer algum erro ao acessar o banco de dados
     */
    @Test
    void testLoadBoardsFromDatabase() throws SQLException {
        List<BoardEntity> boards = BoardService.loadBoardsFromDatabase();

        assertNotNull(boards);
        assertFalse(boards.isEmpty());
        assertNotNull(boards.get(0).getBoardColumns());
    }

}
