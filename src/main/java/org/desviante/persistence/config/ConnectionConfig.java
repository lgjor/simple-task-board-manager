package org.desviante.persistence.config;

import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ConnectionConfig {

    private static Connection connection;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                String userHome = System.getProperty("user.home");
                String dbDir = userHome + File.separator + "MyBoards";

                // Cria o diretório "MyBoards" se ele não existir
                File directory = new File(dbDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String dbPath = dbDir + File.separator + "myboard.db";
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                connection.setAutoCommit(false); // Mantendo o autoCommit como false

                // Cria a tabela boards na primeira conexão
                createTablesIfNotExist(connection);

            } catch (SQLException e) {
                // Log de erro mais informativo
                System.err.println("Erro ao conectar ou criar o banco de dados: " + e.getMessage());
                throw e; // Relança a exceção para indicar falha na obtenção da conexão
            }
        }
        return connection;
    }

    public static synchronized void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage());
                throw e; // Relança a exceção se houver um problema ao fechar
            } finally {
                connection = null; // Reseta a conexão para a próxima vez
            }
        }
    }

    private static void createTablesIfNotExist(Connection connection) throws SQLException {
        String sqlBoards = "CREATE TABLE IF NOT EXISTS boards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL" +
                ");";
        String sqlBoardsColumns = "CREATE TABLE IF NOT EXISTS boards_columns (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "board_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "kind TEXT NOT NULL," +
                "order_index INTEGER NOT NULL," +
                "FOREIGN KEY(board_id) REFERENCES boards(id)" +
                ");";
        String sqlCards = "CREATE TABLE IF NOT EXISTS cards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "board_column_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "creation_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "last_update_date DATETIME," +
                "completion_date DATETIME," +
                "blocked BOOLEAN DEFAULT 0," +
                "block_reason TEXT," +
                "unblock_reason TEXT," +
                "FOREIGN KEY(board_column_id) REFERENCES boards_columns(id)" +
                ");";
        try (var stmt = connection.createStatement()) {
            stmt.execute(sqlBoards);
            stmt.execute(sqlBoardsColumns);
            stmt.execute(sqlCards);
        }
    }
}