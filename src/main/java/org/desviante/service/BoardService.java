package org.desviante.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardColumnKindEnum;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Service responsável por orquestrar todas as operações e lógicas de negócio
 * relacionadas a Boards. Utiliza exclusivamente JPA para persistência de dados.
 */
public class BoardService implements IBoardService {

    private final EntityManager entityManager;

    /**
     * Construtor que implementa a Injeção de Dependência.
     * O serviço não cria mais seu próprio EntityManager, ele o recebe.
     * Isso o desacopla da infraestrutura (JPAUtil) e o torna testável.
     *
     * @param entityManager O EntityManager a ser usado por todas as operações do serviço.
     */
    public BoardService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Busca todos os boards, já carregando suas colunas para evitar
     * problemas de LazyInitializationException na camada de visualização.
     *
     * @return Uma lista de todos os boards com suas colunas.
     */
    public List<BoardEntity> findAllWithColumns() {
        try {
            // Passo 1: Busca os boards e suas colunas.
            String jpqlBoards = "SELECT DISTINCT b FROM BoardEntity b " +
                    "LEFT JOIN FETCH b.boardColumns";
            List<BoardEntity> boards = this.entityManager.createQuery(jpqlBoards, BoardEntity.class).getResultList();

            // Passo 2: Em uma segunda consulta, busca as colunas novamente para carregar seus cards.
            if (!boards.isEmpty()) {
                String jpqlCards = "SELECT DISTINCT bc FROM BoardColumnEntity bc LEFT JOIN FETCH bc.cards WHERE bc.board IN :boards";
                this.entityManager.createQuery(jpqlCards, BoardColumnEntity.class)
                        .setParameter("boards", boards)
                        .getResultList(); // A consulta é executada para popular os objetos já em memória.
            }

            return boards;
        } catch (Exception e) {
            // Logar o erro é uma boa prática
            System.err.println("Erro ao buscar todos os boards: " + e.getMessage());
            return Collections.emptyList(); // Retorna uma lista vazia em caso de erro.
        }
    }

    /**
     * Busca um único board pelo seu ID.
     *
     * @param id O ID do board a ser encontrado.
     * @return Um Optional contendo o BoardEntity se encontrado, ou um Optional vazio.
     */
    public Optional<BoardEntity> findById(Long id) {
        try {
            // Passo 1: Busca o board e suas colunas
            String jpqlBoard = "SELECT b FROM BoardEntity b " +
                    "LEFT JOIN FETCH b.boardColumns " +
                    "WHERE b.id = :id";
            List<BoardEntity> result = this.entityManager.createQuery(jpqlBoard, BoardEntity.class)
                    .setParameter("id", id)
                    .getResultList();

            Optional<BoardEntity> boardOptional = result.stream().findFirst();

            // Passo 2: Se o board foi encontrado e tem colunas, busca os cards para essas colunas.
            if (boardOptional.isPresent() && !boardOptional.get().getBoardColumns().isEmpty()) {
                String jpqlCards = "SELECT DISTINCT bc FROM BoardColumnEntity bc LEFT JOIN FETCH bc.cards WHERE bc.board = :board";
                this.entityManager.createQuery(jpqlCards, BoardColumnEntity.class)
                        .setParameter("board", boardOptional.get())
                        .getResultList(); // Executa para popular a coleção 'cards' dos objetos 'BoardColumnEntity'.
            }
            // Retorna um Optional da primeira entidade encontrada, ou um Optional vazio
            return boardOptional;
        } catch (Exception e) {
            // Logar o erro é uma boa prática
            System.err.println("Erro ao buscar board por ID: " + id + e.getMessage());
            return Optional.empty(); // Retorna um Optional vazio em caso de erro.

        }
    }

    /**
     * Salva um novo board ou atualiza um existente.
     * O método 'merge' do JPA lida com as duas situações de forma transparente.
     *
     * @param board O BoardEntity a ser salvo ou atualizado.
     * @return A entidade gerenciada pelo JPA após a operação.
     */
    public BoardEntity saveOrUpdate(BoardEntity board) {
        // A responsabilidade da transação agora é do chamador.
        // O 'merge' retorna a instância da entidade que está no contexto de persistência.
        return this.entityManager.merge(board);
    }

    /**
     * Deleta um board pelo seu ID.
     * Graças à configuração CascadeType.ALL na BoardEntity, ao remover um board,
     * o Hibernate automaticamente removerá suas colunas e cards associados.
     *
     * @param id O ID do board a ser deletado.
     */
    public void delete(Long id) {
        // A responsabilidade da transação é do chamador.
        // É uma boa prática buscar a entidade antes de removê-la.
        BoardEntity board = this.entityManager.find(BoardEntity.class, id);
        if (board != null) {
            this.entityManager.remove(board);
        }
    }

    /**
     * Cria um novo board com um conjunto de colunas padrão.
     * Toda a operação é executada em uma única transação.
     *
     * @param boardName O nome do novo board.
     * @return A entidade BoardEntity criada e gerenciada pelo JPA.
     */
    public BoardEntity createBoardWithDefaultColumns(String boardName) {
        // 1. Cria a entidade principal do Board
        BoardEntity board = new BoardEntity();
        board.setName(boardName);

        // 2. Cria as colunas padrão
        BoardColumnEntity colToDo = new BoardColumnEntity();
        colToDo.setName("Não iniciado");
        colToDo.setOrder_index(0);
        colToDo.setKind(BoardColumnKindEnum.INITIAL);

        BoardColumnEntity colDoing = new BoardColumnEntity();
        colDoing.setName("Em Andamento");
        colDoing.setOrder_index(1);
        colDoing.setKind(BoardColumnKindEnum.PENDING);

        BoardColumnEntity colDone = new BoardColumnEntity();
        colDone.setName("Concluído");
        colDone.setOrder_index(2);
        colDone.setKind(BoardColumnKindEnum.FINAL);

        // 3. Associa as colunas ao board usando os métodos auxiliares
        board.addBoardColumn(colToDo);
        board.addBoardColumn(colDoing);
        board.addBoardColumn(colDone);

        // 4. Persiste o board. O chamador é responsável pela transação.
        this.entityManager.persist(board);
        return board;
    }
}