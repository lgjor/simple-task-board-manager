import java.sql.*;

public class ForceMigration {
    public static void main(String[] args) {
        try {
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:file:C:\\Users\\Lucas\\myboards\\board_h2_db";
            Connection conn = DriverManager.getConnection(url);
            
            Statement stmt = conn.createStatement();
            
            System.out.println("🔄 Aplicando migração das colunas de agendamento...");
            
            // Verificar se as colunas já existem
            try {
                stmt.executeQuery("SELECT scheduled_date FROM cards LIMIT 1");
                System.out.println("✅ Coluna scheduled_date já existe");
            } catch (SQLException e) {
                System.out.println("➕ Adicionando coluna scheduled_date...");
                stmt.execute("ALTER TABLE cards ADD COLUMN scheduled_date TIMESTAMP NULL");
                System.out.println("✅ Coluna scheduled_date adicionada");
            }
            
            try {
                stmt.executeQuery("SELECT due_date FROM cards LIMIT 1");
                System.out.println("✅ Coluna due_date já existe");
            } catch (SQLException e) {
                System.out.println("➕ Adicionando coluna due_date...");
                stmt.execute("ALTER TABLE cards ADD COLUMN due_date TIMESTAMP NULL");
                System.out.println("✅ Coluna due_date adicionada");
            }
            
            // Criar índices
            try {
                stmt.execute("CREATE INDEX idx_cards_scheduled_date ON cards(scheduled_date)");
                System.out.println("✅ Índice idx_cards_scheduled_date criado");
            } catch (SQLException e) {
                System.out.println("ℹ️ Índice idx_cards_scheduled_date já existe");
            }
            
            try {
                stmt.execute("CREATE INDEX idx_cards_due_date ON cards(due_date)");
                System.out.println("✅ Índice idx_cards_due_date criado");
            } catch (SQLException e) {
                System.out.println("ℹ️ Índice idx_cards_due_date já existe");
            }
            
            try {
                stmt.execute("CREATE INDEX idx_cards_urgency ON cards(completion_date, due_date)");
                System.out.println("✅ Índice idx_cards_urgency criado");
            } catch (SQLException e) {
                System.out.println("ℹ️ Índice idx_cards_urgency já existe");
            }
            
            // Verificar se funcionou
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cards");
            if (rs.next()) {
                System.out.println("✅ Total de cards: " + rs.getInt("total"));
            }
            
            conn.close();
            System.out.println("🎉 Migração concluída com sucesso!");
            
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
