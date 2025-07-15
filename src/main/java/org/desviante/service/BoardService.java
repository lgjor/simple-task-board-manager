package org.desviante.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardColumnKindEnum;
import org.desviante.util.JPAUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Service responsável por orquestrar todas as operações e lógicas de negócio
 * relacionadas a Boards. Utiliza exclusivamente JPA para persistência de dados.
 */
public class BoardService {

    /**
     * Busca todos os boards, já carregando suas colunas para evitar
     * problemas de LazyInitializationException na camada de visualização.
     *
     * @return Uma lista de todos os boards com suas colunas.
     */
    public List<BoardEntity> findAllWithColumns() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            // Passo 1: Busca os boards e suas colunas.
            String jpqlBoards = "SELECT DISTINCT b FROM BoardEntity b " +
                    "LEFT JOIN FETCH b.boardColumns";
            List<BoardEntity> boards = em.createQuery(jpqlBoards, BoardEntity.class).getResultList();

            // Passo 2: Em uma segunda consulta, busca as colunas novamente para carregar seus cards.
            if (!boards.isEmpty()) {
                String jpqlCards = "SELECT DISTINCT bc FROM BoardColumnEntity bc LEFT JOIN FETCH bc.cards WHERE bc.board IN :boards";
                em.createQuery(jpqlCards, BoardColumnEntity.class)
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
        try (EntityManager em = JPAUtil.getEntityManager()) {
            // Passo 1: Busca o board e suas colunas
            String jpqlBoard = "SELECT b FROM BoardEntity b " +
                    "LEFT JOIN FETCH b.boardColumns " +
                    "WHERE b.id = :id";
            List<BoardEntity> result = em.createQuery(jpqlBoard, BoardEntity.class)
                    .setParameter("id", id)
                    .getResultList();

            Optional<BoardEntity> boardOptional = result.stream().findFirst();

            // Passo 2: Se o board foi encontrado e tem colunas, busca os cards para essas colunas.
            if (boardOptional.isPresent() && !boardOptional.get().getBoardColumns().isEmpty()) {
                String jpqlCards = "SELECT DISTINCT bc FROM BoardColumnEntity bc LEFT JOIN FETCH bc.cards WHERE bc.board = :board";
                em.createQuery(jpqlCards, BoardColumnEntity.class)
                        .setParameter("board", boardOptional.get())
                        .getResultList(); // Executa para popular a coleção 'cards' dos objetos 'BoardColumnEntity'.
            }
            // Retorna um Optional da primeira entidade encontrada, ou um Optional vazio
            return boardOptional;
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
        // Abrir e fechar um EntityManager para cada transação é o padrão.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // O 'merge' retorna a instância da entidade que está no contexto de persistência.
            BoardEntity managedBoard = em.merge(board);
            em.getTransaction().commit();
            return managedBoard;
        } catch (Exception e) {
            // Rollback é crucial para manter a integridade do banco em caso de erro.
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Falha ao salvar o board: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Deleta um board pelo seu ID.
     * Graças à configuração CascadeType.ALL na BoardEntity, ao remover um board,
     * o Hibernate automaticamente removerá suas colunas e cards associados.
     *
     * @param id O ID do board a ser deletado.
     */
    public void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // É uma boa prática buscar a entidade antes de removê-la.
            BoardEntity board = em.find(BoardEntity.class, id);
            if (board != null) {
                em.remove(board);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Falha ao deletar o board com ID " + id + ": " + e.getMessage(), e);
        } finally {
            em.close();
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
        EntityManager em = JPAUtil.getEntityManager();
        long newBoardId;
        try {
            em.getTransaction().begin();

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

            // 4. Persiste o board. O CascadeType.ALL cuidará de salvar as colunas.
            em.persist(board);
            em.flush();
            newBoardId = board.getId(); // Captura o ID após o flush
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Lança uma exceção para que a camada de UI possa capturá-la e mostrar uma mensagem
            throw new RuntimeException("Falha ao criar board com colunas padrão", e);
        } finally {
            em.close(); // A sessão é fechada, desanexando o objeto 'board' original
        }
        // 3. Após a transação, buscamos novamente para retornar um objeto completo e gerenciado.
        return findById(newBoardId).orElseThrow(() -> new IllegalStateException("Falha ao re-buscar o board recém-criado com ID: " + newBoardId));
    }
}