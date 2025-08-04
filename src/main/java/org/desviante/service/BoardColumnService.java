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

/**
 * Gerencia as operações de negócio relacionadas às colunas dos quadros.
 * 
 * <p>Responsável por implementar a lógica de negócio para criação, consulta
 * e remoção de colunas dos quadros. Esta camada de serviço garante a
 * integridade dos dados através de validações antes das operações de
 * persistência.</p>
 * 
 * <p>Implementa validações importantes como verificação da existência
 * do quadro pai antes de criar colunas, prevenindo colunas "órfãs".
 * Também otimiza consultas para evitar problemas N+1 através de
 * busca em lote de colunas para múltiplos quadros.</p>
 * 
 * <p>Utiliza transações para garantir consistência dos dados, com operações
 * de leitura marcadas como readOnly para otimização de performance.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardColumn
 * @see BoardColumnKindEnum
 * @see BoardColumnRepository
 */
@Service
@RequiredArgsConstructor
public class BoardColumnService {

    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;

    /**
     * Cria uma nova coluna para um quadro específico.
     * 
     * <p>Valida a existência do quadro pai antes de criar a coluna,
     * prevenindo a criação de colunas "órfãs" e garantindo integridade
     * dos dados. A coluna é criada com nome, ordem, tipo e associação
     * ao quadro especificado.</p>
     * 
     * @param name nome da nova coluna
     * @param orderIndex posição de ordem da coluna no quadro
     * @param kind tipo da coluna (INITIAL, PENDING, FINAL)
     * @param boardId identificador do quadro pai
     * @return coluna criada com ID gerado
     * @throws ResourceNotFoundException se o quadro pai não for encontrado
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
     * Busca todas as colunas de um quadro específico.
     * 
     * <p>As colunas são retornadas ordenadas pelo orderIndex para
     * manter a sequência visual correta no quadro (esquerda para direita).</p>
     * 
     * @param boardId identificador do quadro
     * @return lista de colunas ordenadas por ordem
     */
    @Transactional(readOnly = true)
    public List<BoardColumn> getColumnsForBoard(Long boardId) {
        return columnRepository.findByBoardId(boardId);
    }

    /**
     * Busca colunas para múltiplos quadros em uma única operação.
     * 
     * <p>Este método é otimizado para evitar múltiplas chamadas ao banco
     * de dados (problema N+1). Retorna uma lista vazia se nenhum ID
     * for fornecido, garantindo comportamento previsível.</p>
     * 
     * @param boardIds lista de identificadores dos quadros
     * @return lista de todas as colunas pertencentes aos quadros especificados
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
     * Remove uma coluna do sistema.
     * 
     * <p>A deleção em cascata para os cards associados é gerenciada
     * pelo banco de dados através de constraints definidas no schema.
     * Isso garante que todos os cards da coluna sejam removidos
     * automaticamente quando a coluna for deletada.</p>
     * 
     * @param columnId identificador da coluna a ser removida
     */
    @Transactional
    public void deleteColumn(Long columnId) {
        // A verificação de existência é implícita na operação de deleção do repositório,
        // mas poderíamos adicionar uma verificação explícita se quiséssemos uma exceção customizada.
        columnRepository.deleteById(columnId);
    }
}