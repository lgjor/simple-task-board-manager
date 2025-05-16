import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.desviante.persistence.config.ConnectionConfig.getConnection;

class SQLiteConnectionTest {

    @Test
    void testSQLiteConnection() {
        try (Connection connection = getConnection()) {
            assertNotNull(connection, "A conexão não deve ser nula");
            assertFalse(connection.isClosed(), "A conexão deve estar aberta");
        } catch (SQLException e) {
            fail("Exceção ao conectar ao SQLite: " + e.getMessage());
        }
    }
}