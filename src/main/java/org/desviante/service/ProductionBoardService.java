package org.desviante.service;

import jakarta.persistence.EntityManager;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.util.JPAUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementação de IBoardService para o ambiente de produção.
 * Esta classe atua como um Decorator, envolvendo a lógica pura de BoardService
 * com o gerenciamento de EntityManager e transações.
 */
public class ProductionBoardService implements IBoardService {

    /**
     * Executa uma operação que realiza escrita (create, update, delete) dentro de um contexto transacional.
     * Gerencia o ciclo de vida completo do EntityManager e da transação.
     *
     * @param operation A função a ser executada, que recebe um BoardService e retorna um resultado.
     * @return O resultado da operação.
     */
    private <T> T executeWriteInTransaction(Function<BoardService, T> operation) {
        // O try-with-resources garante que o EntityManager seja fechado, mesmo se ocorrer um erro.
        try (EntityManager em = JPAUtil.getEntityManager()) {
            try {
                em.getTransaction().begin();
                T result = operation.apply(new BoardService(em));
                em.getTransaction().commit();
                return result;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                // Re-lança a exceção para que a camada superior (UI) possa ser notificada.
                throw new RuntimeException("Falha na operação de escrita do serviço: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Executa uma operação de leitura, que não requer uma transação explícita.
     * Gerencia apenas o ciclo de vida do EntityManager.
     *
     * @param operation A função a ser executada.
     * @return O resultado da operação.
     */
    private <T> T executeRead(Function<BoardService, T> operation) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return operation.apply(new BoardService(em));
        }
    }

    @Override
    public List<BoardEntity> findAllWithColumns() {
        return executeRead(BoardService::findAllWithColumns);
    }

    @Override
    public Optional<BoardEntity> findById(Long id) {
        return executeRead(service -> service.findById(id));
    }

    @Override
    public BoardEntity saveOrUpdate(BoardEntity board) {
        return executeWriteInTransaction(service -> service.saveOrUpdate(board));
    }

    @Override
    public void delete(Long id) {
        executeWriteInTransaction(service -> { service.delete(id); return null; });
    }

    @Override
    public BoardEntity createBoardWithDefaultColumns(String boardName) {
        return executeWriteInTransaction(service -> service.createBoardWithDefaultColumns(boardName));
    }
}