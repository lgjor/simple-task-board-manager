package org.desviante.service;

import org.desviante.persistence.entity.BoardEntity;

import java.util.List;
import java.util.Optional;

/**
 * Interface que define o contrato para serviços relacionados a Boards.
 * Abstrai a implementação, permitindo diferentes estratégias (ex: produção vs. teste).
 */
public interface IBoardService {

    /**
     * @see BoardService#findAllWithColumns()
     */
    List<BoardEntity> findAllWithColumns();

    /**
     * @see BoardService#findById(Long)
     */
    Optional<BoardEntity> findById(Long id);

    /**
     * @see BoardService#saveOrUpdate(BoardEntity)
     */
    BoardEntity saveOrUpdate(BoardEntity board);

    /**
     * @see BoardService#delete(Long)
     */
    void delete(Long id);

    /**
     * @see BoardService#createBoardWithDefaultColumns(String)
     */
    BoardEntity createBoardWithDefaultColumns(String boardName);
}