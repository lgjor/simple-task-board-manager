package org.desviante.controller;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.IBoardService;
import org.desviante.service.ProductionBoardService;

import java.util.List;
import java.util.Optional;

public class BoardController {

    private final IBoardService boardService;

    public BoardController() {
        // Em vez de instanciar a classe de lógica pura (BoardService),
        // Instanciamos o Decorator de produção (ProductionBoardService).
        // Ele gerencia o ciclo de vida do EntityManager e das transações.
        this.boardService = new ProductionBoardService();
    }

    public List<BoardEntity> findAllBoards() {
        // Delega diretamente para o serviço, sem gerenciar conexões ou exceções SQL.
        return boardService.findAllWithColumns();
    }

    public BoardEntity createBoard(String boardName) {
        return boardService.createBoardWithDefaultColumns(boardName);
    }

    public void updateBoard(BoardEntity board) {
        boardService.saveOrUpdate(board);
    }

    public void deleteBoard(BoardEntity board) {
        boardService.delete(board.getId());
    }

    public Optional<BoardEntity> getBoardById(Long id) {
        return boardService.findById(id);
    }
}