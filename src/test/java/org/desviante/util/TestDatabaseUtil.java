package org.desviante.util;

import jakarta.persistence.EntityManager;

public class TestDatabaseUtil {

    /**
     * Limpa todas as tabelas relevantes do banco de dados em uma única transação.
     * Garante um estado completamente limpo para o início de cada teste.
     */
   public static void cleanDatabase(EntityManager em) {
       // O gerenciamento da transação (begin/commit/rollback) foi movido para os
       // métodos chamadores (@BeforeEach nos testes). Isso evita o erro IllegalStateException
       // por tentar iniciar uma transação dentro de outra já ativa.
       // A responsabilidade deste método é apenas executar as queries de limpeza.
       em.createQuery("DELETE FROM TaskEntity").executeUpdate();
       em.createQuery("DELETE FROM CardEntity").executeUpdate();
       em.createQuery("DELETE FROM BoardColumnEntity").executeUpdate();
       em.createQuery("DELETE FROM BoardEntity").executeUpdate();
   }
}