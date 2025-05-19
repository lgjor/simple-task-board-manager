package org.desviante.controller;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.dao.BoardColumnDAO;
import org.desviante.service.BoardService;
import org.desviante.service.BoardQueryService;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class BoardController {

    public BoardController() {}

    public BoardEntity createBoard(String boardName) throws SQLException {
        try (Connection connection = getConnection()) {
            var boardService = new BoardService();
            var boardColumnDAO = new BoardColumnDAO(connection);
            var newBoard = new BoardEntity();
            newBoard.setName(boardName);
            boardService.insert(newBoard);
            boardColumnDAO.insertDefaultColumns(newBoard.getId());
            newBoard.setBoardColumns(boardColumnDAO.findByBoardId(newBoard.getId()));
            return newBoard;
        }
    }

    public boolean deleteBoard(BoardEntity board) throws SQLException {
        try (Connection connection = getConnection()) {
            var boardService = new BoardService();
            return boardService.delete(board.getId());
        }
    }

    public Optional<BoardEntity> getBoardById(Long id) throws SQLException {
        try (Connection connection = getConnection()) {
            var queryService = new BoardQueryService();
            return queryService.findById(id);
        }
    }
}