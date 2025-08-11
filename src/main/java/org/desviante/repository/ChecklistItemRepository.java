package org.desviante.repository;

import org.desviante.model.ChecklistItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar itens do checklist no banco de dados.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Repository
public class ChecklistItemRepository {
    
    private final JdbcTemplate jdbcTemplate;
    public ChecklistItemRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        new SimpleJdbcInsert(dataSource)
                .withTableName("checklist_items")
                .usingGeneratedKeyColumns("id")
                // Deixa created_at usar DEFAULT CURRENT_TIMESTAMP no banco
                // e completed_at iniciar como NULL
                .usingColumns("card_id", "text", "completed", "order_index");
    }
    
    /**
     * RowMapper para mapear resultados do banco para objetos ChecklistItem.
     */
    private static final RowMapper<ChecklistItem> checklistItemRowMapper = (ResultSet rs, int rowNum) -> {
        ChecklistItem item = new ChecklistItem();
        item.setId(rs.getLong("ID"));
        item.setCardId(rs.getLong("CARD_ID"));
        // Em alguns bancos (H2), TEXT é palavra-chave; a coluna é criada como TEXT e fica em maiúsculas
        item.setText(rs.getString("TEXT"));
        item.setCompleted(rs.getBoolean("COMPLETED"));
        item.setOrderIndex(rs.getInt("ORDER_INDEX"));
        
        // Mapear timestamps
        if (rs.getTimestamp("CREATED_AT") != null) {
            item.setCreatedAt(rs.getTimestamp("CREATED_AT").toLocalDateTime());
        }
        if (rs.getTimestamp("COMPLETED_AT") != null) {
            item.setCompletedAt(rs.getTimestamp("COMPLETED_AT").toLocalDateTime());
        }
        
        return item;
    };
    
    /**
     * Salva um novo item do checklist.
     * 
     * @param item item a ser salvo
     * @return item salvo com ID gerado
     */
    public ChecklistItem save(ChecklistItem item) {
        // Usar INSERT explícito com identificadores entre aspas para compatibilidade com H2 (coluna TEXT)
        final String sql = "INSERT INTO checklist_items (card_id, \"TEXT\", completed, order_index) VALUES (?, ?, ?, ?)";
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setLong(1, item.getCardId());
            ps.setString(2, item.getText());
            ps.setBoolean(3, item.isCompleted());
            ps.setInt(4, item.getOrderIndex());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            item.setId(key.longValue());
        }
        return item;
    }
    
    /**
     * Atualiza um item existente do checklist.
     * 
     * @param item item a ser atualizado
     * @return true se atualizado com sucesso
     */
    public boolean update(ChecklistItem item) {
        String sql = """
            UPDATE checklist_items 
            SET \"TEXT\" = ?, completed = ?, order_index = ?, completed_at = ?
            WHERE id = ?
            """;
        
        int rowsAffected = jdbcTemplate.update(sql,
            item.getText(),
            item.isCompleted(),
            item.getOrderIndex(),
            item.getCompletedAt(),
            item.getId()
        );
        
        return rowsAffected > 0;
    }
    
    /**
     * Remove um item do checklist.
     * 
     * @param id identificador do item
     * @return true se removido com sucesso
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM checklist_items WHERE ID = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }
    
    /**
     * Remove todos os itens de um card.
     * 
     * @param cardId identificador do card
     * @return número de itens removidos
     */
    public int deleteByCardId(Long cardId) {
        String sql = "DELETE FROM checklist_items WHERE CARD_ID = ?";
        return jdbcTemplate.update(sql, cardId);
    }
    
    /**
     * Busca um item por ID.
     * 
     * @param id identificador do item
     * @return item encontrado ou empty
     */
    public Optional<ChecklistItem> findById(Long id) {
        String sql = "SELECT * FROM checklist_items WHERE ID = ?";
        List<ChecklistItem> items = jdbcTemplate.query(sql, checklistItemRowMapper, id);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }
    
    /**
     * Busca todos os itens de um card, ordenados por posição.
     * 
     * @param cardId identificador do card
     * @return lista de itens ordenados
     */
    public List<ChecklistItem> findByCardIdOrderByOrderIndex(Long cardId) {
        String sql = "SELECT * FROM checklist_items WHERE CARD_ID = ? ORDER BY ORDER_INDEX";
        return jdbcTemplate.query(sql, checklistItemRowMapper, cardId);
    }
    
    /**
     * Conta quantos itens um card tem.
     * 
     * @param cardId identificador do card
     * @return número de itens
     */
    public int countByCardId(Long cardId) {
        String sql = "SELECT COUNT(*) FROM checklist_items WHERE CARD_ID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, cardId);
    }
    
    /**
     * Conta quantos itens concluídos um card tem.
     * 
     * @param cardId identificador do card
     * @return número de itens concluídos
     */
    public int countCompletedByCardId(Long cardId) {
        String sql = "SELECT COUNT(*) FROM checklist_items WHERE CARD_ID = ? AND COMPLETED = TRUE";
        return jdbcTemplate.queryForObject(sql, Integer.class, cardId);
    }
    
    /**
     * Atualiza a posição de um item.
     * 
     * @param id identificador do item
     * @param newOrderIndex nova posição
     * @return true se atualizado com sucesso
     */
    public boolean updateOrderIndex(Long id, int newOrderIndex) {
        String sql = "UPDATE checklist_items SET ORDER_INDEX = ? WHERE ID = ?";
        int rowsAffected = jdbcTemplate.update(sql, newOrderIndex, id);
        return rowsAffected > 0;
    }
    
    /**
     * Marca um item como concluído ou não concluído.
     * 
     * @param id identificador do item
     * @param completed estado de conclusão
     * @return true se atualizado com sucesso
     */
    public boolean updateCompleted(Long id, boolean completed) {
        String sql = "UPDATE checklist_items SET COMPLETED = ?, COMPLETED_AT = ? WHERE ID = ?";
        LocalDateTime completedAt = completed ? LocalDateTime.now() : null;
        int rowsAffected = jdbcTemplate.update(sql, completed, completedAt, id);
        return rowsAffected > 0;
    }
}
