package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.model.Board;
import org.desviante.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de negócio relacionadas aos quadros (boards).
 * 
 * <p>Responsável por implementar a lógica de negócio para criação, atualização,
 * consulta e remoção de quadros. Esta camada de serviço atua como intermediária
 * entre os controllers e o repositório, garantindo que as regras de negócio
 * sejam aplicadas antes das operações de persistência.</p>
 * 
 * <p>Utiliza transações para garantir consistência dos dados, com operações
 * de leitura marcadas como readOnly para otimização de performance.
 * A criação de quadros inclui automaticamente a data de criação.</p>
 * 
 * <p>Utiliza Lombok para injeção de dependência automática do repositório,
 * reduzindo código boilerplate e mantendo a legibilidade.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Board
 * @see BoardRepository
 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * Busca todos os quadros disponíveis no sistema.
     * 
     * @return lista de todos os quadros
     */
    @Transactional(readOnly = true)
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    /**
     * Busca quadros que não estão associados a nenhum grupo.
     * 
     * @return lista de quadros sem grupo
     */
    @Transactional(readOnly = true)
    public List<Board> getBoardsWithoutGroup() {
        return boardRepository.findBoardsWithoutGroup();
    }

    /**
     * Busca um quadro específico pelo ID.
     * 
     * @param id identificador único do quadro
     * @return Optional contendo o quadro se encontrado, vazio caso contrário
     */
    @Transactional(readOnly = true)
    public Optional<Board> getBoardById(Long id) {
        return boardRepository.findById(id);
    }

    /**
     * Cria um novo quadro com nome especificado.
     * 
     * <p>Define automaticamente a data de criação como o momento atual.
     * O quadro é criado sem associação a grupo (groupId = null).</p>
     * 
     * @param name nome do novo quadro
     * @return quadro criado com ID gerado
     */
    @Transactional
    public Board createBoard(String name) {
        Board newBoard = new Board();
        newBoard.setName(name);
        newBoard.setCreationDate(LocalDateTime.now());
        return boardRepository.save(newBoard);
    }

    /**
     * Atualiza o nome de um quadro existente.
     * 
     * @param id identificador do quadro a ser atualizado
     * @param newName novo nome para o quadro
     * @return Optional contendo o quadro atualizado se encontrado, vazio caso contrário
     */
    @Transactional
    public Optional<Board> updateBoardName(Long id, String newName) {
        return boardRepository.findById(id)
                .map(board -> {
                    board.setName(newName);
                    return boardRepository.save(board);
                });
    }

    /**
     * Atualiza um quadro completo.
     * 
     * @param board quadro com dados atualizados
     * @return quadro salvo no banco
     */
    @Transactional
    public Board updateBoard(Board board) {
        return boardRepository.save(board);
    }

    /**
     * Remove um quadro do sistema pelo ID.
     * 
     * @param id identificador do quadro a ser removido
     */
    @Transactional
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }
}