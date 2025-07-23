package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.model.Board;
import org.desviante.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Lombok cria o construtor para injeção de dependência.
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional(readOnly = true)
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Board> getBoardById(Long id) {
        return boardRepository.findById(id);
    }

    @Transactional
    public Board createBoard(String name) {
        Board newBoard = new Board();
        newBoard.setName(name);
        newBoard.setCreationDate(LocalDateTime.now());
        return boardRepository.save(newBoard);
    }

    @Transactional
    public Optional<Board> updateBoardName(Long id, String newName) {
        return boardRepository.findById(id)
                .map(board -> {
                    board.setName(newName);
                    return boardRepository.save(board);
                });
    }

    @Transactional
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }
}