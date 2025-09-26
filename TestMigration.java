import java.sql.*;

public class TestMigration {
    public static void main(String[] args) {
        try {
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:file:C:\\Users\\Lucas\\myboards\\board_h2_db;USER=MYBOARDUSER;PASSWORD=MYBOARDPASS";
            Connection conn = DriverManager.getConnection(url);
            
            Statement stmt = conn.createStatement();
            
            // Tentar inserir um card com as novas colunas
            try {
                String sql = "INSERT INTO cards (title, description, type, creation_date, last_update_date, board_column_id, order_index, scheduled_date, due_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, "Teste Migração");
                pstmt.setString(2, "Teste das novas colunas");
                pstmt.setString(3, "TASK");
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                pstmt.setLong(6, 1L);
                pstmt.setInt(7, 0);
                pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
                pstmt.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
                
                int result = pstmt.executeUpdate();
                System.out.println("✅ Migração aplicada com sucesso! Colunas scheduled_date e due_date funcionando.");
                System.out.println("✅ Card de teste inserido com ID: " + result);
                
                // Remover o card de teste
                stmt.executeUpdate("DELETE FROM cards WHERE title = 'Teste Migração'");
                System.out.println("✅ Card de teste removido.");
                
            } catch (SQLException e) {
                if (e.getMessage().contains("scheduled_date") || e.getMessage().contains("due_date")) {
                    System.out.println("❌ Migração NÃO foi aplicada. Colunas não existem: " + e.getMessage());
                } else {
                    System.out.println("⚠️ Erro inesperado: " + e.getMessage());
                }
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("❌ Erro de conexão: " + e.getMessage());
        }
    }
}
