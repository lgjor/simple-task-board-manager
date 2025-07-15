package org.desviante.util;

import jakarta.persistence.EntityManager;

public class TestDatabaseUtil {

    /**
     * Limpa todas as tabelas relevantes do banco de dados em uma única transação.
     * Garante um estado completamente limpo para o início de cada teste.
     */
    public static void cleanDatabase() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // A ordem de deleção é importante para respeitar as chaves estrangeiras.
            // Deleta-se das tabelas "filhas" para as "mães".
            em.createQuery("DELETE FROM TaskEntity").executeUpdate();
            em.createQuery("DELETE FROM CardEntity").executeUpdate();
            em.createQuery("DELETE FROM BoardColumnEntity").executeUpdate();
            em.createQuery("DELETE FROM BoardEntity").executeUpdate();

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Lança uma exceção para que o teste falhe claramente se a limpeza não funcionar.
            throw new RuntimeException("Falha ao limpar o banco de dados para o teste", e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}