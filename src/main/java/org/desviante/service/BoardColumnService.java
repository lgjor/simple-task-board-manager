package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.BoardColumn;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.repository.BoardColumnRepository;
import org.desviante.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardColumnService {

    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;

    /**
     * Cria uma nova coluna, mas primeiro valida se o board pai existe.
     * Isso previne a criação de colunas "órfãs" e melhora a integridade dos dados.
     */
    @Transactional
    public BoardColumn createColumn(String name, int orderIndex, BoardColumnKindEnum kind, Long boardId) {
        // Validação: Garante que o board pai existe antes de prosseguir.
        boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board com ID " + boardId + " não encontrado. Impossível criar coluna."));

        BoardColumn newColumn = new BoardColumn(null, name, orderIndex, kind, boardId);
        return columnRepository.save(newColumn);
    }

    /**
     * Busca todas as colunas de um board específico, ordenadas pelo seu índice.
     */
    @Transactional(readOnly = true)
    public List<BoardColumn> getColumnsForBoard(Long boardId) {
        return columnRepository.findByBoardId(boardId);
    }

    /**
     * Busca todas as colunas para uma lista de IDs de boards.
     * Este método é otimizado para evitar múltiplas chamadas ao banco de dados (problema N+1).
     *
     * @param boardIds A lista de IDs dos boards.
     * @return Uma lista de todas as colunas pertencentes aos boards especificados.
     */
    @Transactional(readOnly = true)
    public List<BoardColumn> getColumnsForBoards(List<Long> boardIds) {
        if (boardIds == null || boardIds.isEmpty()) {
            return Collections.emptyList(); // Retorna uma lista vazia se não houver IDs.
        }
        // Delega a chamada para o método do repositório que busca por uma lista de IDs.
        return columnRepository.findByBoardIdIn(boardIds);
    }

    /**
     * Deleta uma coluna. A deleção em cascata para os cards associados
     * é gerenciada pelo banco de dados (definido no schema.sql).
     */
    @Transactional
    public void deleteColumn(Long columnId) {
        // A verificação de existência é implícita na operação de deleção do repositório,
        // mas poderíamos adicionar uma verificação explícita se quiséssemos uma exceção customizada.
        columnRepository.deleteById(columnId);
    }
}