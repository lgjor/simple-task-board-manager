import java.sql.*;

public class CheckColumns {
    public static void main(String[] args) {
        try {
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:file:C:\\Users\\Lucas\\myboards\\board_h2_db";
            Connection conn = DriverManager.getConnection(url);
            
            Statement stmt = conn.createStatement();
            
            // Verificar se as colunas existem
            try {
                ResultSet rs = stmt.executeQuery("SELECT scheduled_date FROM cards LIMIT 1");
                System.out.println("✅ Coluna scheduled_date existe");
            } catch (SQLException e) {
                System.out.println("❌ Coluna scheduled_date NÃO existe: " + e.getMessage());
            }
            
            try {
                ResultSet rs = stmt.executeQuery("SELECT due_date FROM cards LIMIT 1");
                System.out.println("✅ Coluna due_date existe");
            } catch (SQLException e) {
                System.out.println("❌ Coluna due_date NÃO existe: " + e.getMessage());
            }
            
            // Verificar estrutura da tabela
            ResultSet rs = stmt.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'CARDS' ORDER BY ORDINAL_POSITION");
            System.out.println("\n📋 Colunas da tabela CARDS:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString("COLUMN_NAME"));
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }
}
