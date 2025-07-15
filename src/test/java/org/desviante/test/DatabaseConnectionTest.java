package org.desviante.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de diagnóstico para verificar a conectividade fundamental com o banco de dados H2.
 * <p>
 * Este teste ignora completamente o Hibernate e o JPA, usando uma conexão JDBC pura
 * para isolar a causa raiz de quaisquer problemas de inicialização.
 * Se este teste passar, a conectividade básica está funcionando e o problema
 * reside na configuração do Hibernate. Se falhar, o problema está no classpath
 * ou na visibilidade do driver do H2.
 */
class DatabaseConnectionTest {

    @Test
    @DisplayName("Deve conectar-se diretamente ao banco de dados H2 em memória via JDBC")
    void shouldConnectToH2Database() {
        Connection connection = null;
        try {
            // Tenta carregar o driver H2 e estabelecer uma conexão direta.
            // A URL é a mesma definida no persistence.xml de teste.
            connection = DriverManager.getConnection("jdbc:h2:mem:board;DB_CLOSE_DELAY=-1", "sa", "");

            // A verificação principal: a conexão foi estabelecida com sucesso?
            assertNotNull(connection, "A conexão JDBC não deveria ser nula.");
            assertFalse(connection.isClosed(), "A conexão JDBC deveria estar aberta.");
        } catch (SQLException e) {
            fail("Falha ao conectar ao banco de dados H2 via JDBC. Verifique o driver e o classpath.", e);
        }
    }
}