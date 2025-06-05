package org.desviante.controller;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.dao.BoardDAO;
import org.desviante.persistence.dao.BoardColumnDAO;
import org.desviante.service.BoardService;
import org.desviante.service.BoardQueryService;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public class BoardController {

    public BoardController() {
    }

    public List<BoardEntity> findAllBoards() throws SQLException {
        try (Connection connection = getConnection()) {
            BoardDAO boardDAO = new BoardDAO(connection);
            BoardService boardService = new BoardService(boardDAO);
            return boardService.findAll();
        }
    }

    public BoardEntity createBoard(String boardName) throws SQLException {
        try (Connection connection = getConnection()) {
            BoardDAO boardDAO = new BoardDAO(connection);
            BoardService boardService = new BoardService(boardDAO);
            BoardColumnDAO boardColumnDAO = new BoardColumnDAO(connection);
            BoardEntity newBoard = new BoardEntity();
            newBoard.setName(boardName);
            boardService.insert(newBoard);
            boardColumnDAO.insertDefaultColumns(newBoard.getId());
            newBoard.setBoardColumns(boardColumnDAO.findByBoardId(newBoard.getId()));
            return newBoard;
        }
    }

    public void updateBoard(BoardEntity board) throws SQLException {
        try (Connection connection = getConnection()) {
            BoardDAO boardDAO = new BoardDAO(connection);
            BoardService boardService = new BoardService(boardDAO);
            boardService.update(board);
        }
    }

    public boolean deleteBoard(BoardEntity board) throws SQLException {
        try (Connection connection = getConnection()) {
            BoardDAO boardDAO = new BoardDAO(connection);
            BoardService boardService = new BoardService(boardDAO);
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