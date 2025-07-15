package org.desviante.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Um utilitário JPA exclusivo para o ambiente de teste.
 * Esta classe inicializa o EntityManagerFactory usando o persistence unit 'board-pu',
 * que, no contexto de teste, carrega a configuração do H2 a partir de
 * 'src/test/resources/META-INF/persistence.xml'.
 */
public class TestJPAUtil {
    // Usa a unidade de persistência H2-específica, evitando conflitos com o teste de ponta-a-ponta.
    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("board-pu-h2");

    public static EntityManager createEntityManager() {
        return FACTORY.createEntityManager();
    }

    /**
     * Fecha o EntityManagerFactory global para liberar todos os recursos do banco de dados.
     * Ideal para ser chamado em um método @AfterAll do JUnit.
     */
    public static void close() {
        if (FACTORY != null && FACTORY.isOpen())
            FACTORY.close();
    }
}