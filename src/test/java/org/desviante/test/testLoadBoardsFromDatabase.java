package org.desviante.test;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class testLoadBoardsFromDatabase {

    @Test
    void testLoadBoardsFromDatabase() throws SQLException {
        List<BoardEntity> boards = BoardService.loadBoardsFromDatabase();

        assertNotNull(boards);
        assertFalse(boards.isEmpty());
        assertNotNull(boards.get(0).getBoardColumns());
    }

}
