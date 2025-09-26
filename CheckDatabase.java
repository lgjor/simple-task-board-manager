import java.sql.*;

public class CheckDatabase {
    public static void main(String[] args) {
        try {
            // Carregar driver H2
            Class.forName("org.h2.Driver");
            
            // Conectar ao banco
            String url = "jdbc:h2:file:C:\\Users\\Lucas\\myboards\\board_h2_db";
            Connection conn = DriverManager.getConnection(url);
            
            // Verificar boards
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM boards");
            if (rs.next()) {
                System.out.println("Total de boards: " + rs.getInt("total"));
            }
            
            // Verificar cards
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cards");
            if (rs.next()) {
                System.out.println("Total de cards: " + rs.getInt("total"));
            }
            
            // Verificar se tem as novas colunas
            try {
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cards WHERE scheduled_date IS NOT NULL");
                if (rs.next()) {
                    System.out.println("Cards com scheduled_date: " + rs.getInt("total"));
                }
            } catch (SQLException e) {
                System.out.println("Coluna scheduled_date não existe ainda");
            }
            
            try {
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cards WHERE due_date IS NOT NULL");
                if (rs.next()) {
                    System.out.println("Cards com due_date: " + rs.getInt("total"));
                }
            } catch (SQLException e) {
                System.out.println("Coluna due_date não existe ainda");
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
