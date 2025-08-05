package org.desviante.test;

import org.desviante.config.AppConfig;
import org.desviante.config.DataConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração para verificar a configuração do DataSource e a conexão com o banco de dados.
 */
@SpringJUnitConfig(classes = DataConfig.class) // Carrega o contexto do Spring a partir da nossa classe de configuração.
class DataSourceConnectionTest {

    @Autowired // Pede ao Spring para injetar o bean DataSource que configuramos.
    private DataSource dataSource;

    @Test
    @DisplayName("Deve injetar o DataSource e obter uma conexão válida")
    void shouldConnectToDatabase() throws SQLException {
        assertNotNull(dataSource, "O DataSource não deveria ser nulo. Verifique a configuração do Spring.");
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1), "A conexão com o banco de dados deveria ser válida.");
        }
    }
}
