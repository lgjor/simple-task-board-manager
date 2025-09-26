import java.sql.*;

public class QuickCheck {
    public static void main(String[] args) {
        try {
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:file:C:\\Users\\Lucas\\myboards\\board_h2_db;USER=MYBOARDUSER;PASSWORD=MYBOARDPASS";
            Connection conn = DriverManager.getConnection(url);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM boards");
            if (rs.next()) {
                System.out.println("✅ Boards: " + rs.getInt("total"));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cards");
            if (rs.next()) {
                System.out.println("✅ Cards: " + rs.getInt("total"));
            }
            
            conn.close();
            System.out.println("✅ Banco restaurado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }
}
