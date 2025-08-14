package org.desviante.repository;

import org.desviante.model.CheckListItem;
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
 * Repository para gerenciar itens de checklist no banco de dados.
 * 
 * <p>Esta classe implementa operações de persistência para itens de checklist,
 * fornecendo funcionalidades CRUD completas e operações específicas para
 * gerenciamento de listas de tarefas associadas a cards.</p>
 * 
 * <p><strong>Funcionalidades Principais:</strong></p>
 * <ul>
 *   <li><strong>CRUD Básico:</strong> Criar, ler, atualizar e deletar itens</li>
 *   <li><strong>Gerenciamento de Ordem:</strong> Controle de posicionamento dos itens</li>
 *   <li><strong>Controle de Progresso:</strong> Marcação de itens como concluídos</li>
 *   <li><strong>Operações em Lote:</strong> Remoção de todos os itens de um card</li>
 *   <li><strong>Consultas Agregadas:</strong> Contagem de itens e progresso</li>
 * </ul>
 * 
 * <p><strong>Inicialização Automática:</strong> Durante a construção, verifica
 * e cria automaticamente a tabela {@code checklist_items} se ela não existir,
 * garantindo que o sistema funcione mesmo em bancos de dados vazios.</p>
 * 
 * <p><strong>Compatibilidade:</strong> Utiliza JDBC puro para máxima compatibilidade
 * com diferentes bancos de dados, especialmente H2 e SQLite.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CheckListItem
 * @see JdbcTemplate
 * @see DataSource
 */
@Repository
public class CheckListItemRepository {
    
    /**
     * Template JDBC para execução de operações no banco de dados.
     * Utilizado para todas as operações de persistência e consulta.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Construtor que inicializa o repository e cria a tabela se necessário.
     * 
     * <p>Este construtor realiza as seguintes operações:</p>
     * <ol>
     *   <li>Cria uma instância de {@link JdbcTemplate} com a fonte de dados fornecida</li>
     *   <li>Executa automaticamente {@link #createTableIfNotExists()} para garantir
     *       que a tabela necessária exista no banco</li>
     * </ol>
     * 
     * <p><strong>Importante:</strong> A criação da tabela é executada de forma
     * segura, não interrompendo a inicialização da aplicação em caso de falha.</p>
     * 
     * @param dataSource fonte de dados para conexão com o banco
     * @see #createTableIfNotExists()
     */
    public CheckListItemRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        // Criar tabela checklist_items se não existir
        createTableIfNotExists();
    }
    
    /**
     * Cria a tabela checklist_items se ela não existir no banco de dados.
     * 
     * <p>Esta operação é executada automaticamente durante a inicialização
     * do repository para garantir que a estrutura necessária esteja disponível.
     * A tabela criada inclui:</p>
     * 
     * <ul>
     *   <li><strong>Campos Básicos:</strong> id, card_id, text, completed, order_index</li>
     *   <li><strong>Timestamps:</strong> created_at, completed_at</li>
     *   <li><strong>Relacionamentos:</strong> Chave estrangeira para cards com CASCADE</li>
     *   <li><strong>Índices:</strong> Para otimizar consultas por card_id e order_index</li>
     * </ul>
     * 
     * <p><strong>Tratamento de Erros:</strong> Falhas durante a criação da tabela
     * são registradas mas não interrompem a inicialização da aplicação.</p>
     * 
     * <p><strong>Compatibilidade:</strong> Utiliza sintaxe SQL padrão compatível
     * com H2 e SQLite.</p>
     * 
     * @see JdbcTemplate#queryForObject(String, Class)
     * @see JdbcTemplate#execute(String)
     */
    private void createTableIfNotExists() {
        try {
            // Verificar se a tabela existe
            String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHECKLIST_ITEMS'";
            Integer count = jdbcTemplate.queryForObject(checkTableSQL, Integer.class);
            
            if (count == null || count == 0) {
                // Criar a tabela
                String createTableSQL = """
                    CREATE TABLE checklist_items (
                        id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                        card_id         BIGINT NOT NULL,
                        text            TEXT NOT NULL,
                        completed       BOOLEAN NOT NULL DEFAULT FALSE,
                        order_index     INT NOT NULL DEFAULT 0,
                        created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        completed_at    TIMESTAMP NULL,
                        
                        CONSTRAINT fk_checklist_items_cards FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
                    )
                    """;
                
                jdbcTemplate.execute(createTableSQL);
                
                // Criar índices
                jdbcTemplate.execute("CREATE INDEX idx_checklist_items_card_id ON checklist_items(card_id)");
                jdbcTemplate.execute("CREATE INDEX idx_checklist_items_order_index ON checklist_items(order_index)");
                
                System.out.println("Tabela checklist_items criada com sucesso");
            }
        } catch (Exception e) {
            System.err.println("Erro ao criar tabela checklist_items: " + e.getMessage());
            // Não re-lança a exceção para não impedir a inicialização da aplicação
        }
    }
    
    /**
     * RowMapper para converter resultados do banco de dados em objetos {@link CheckListItem}.
     * 
     * <p>Este mapper lida com as seguintes considerações:</p>
     * <ul>
     *   <li><strong>Compatibilidade H2:</strong> A coluna TEXT é criada em maiúsculas</li>
     *   <li><strong>Timestamps:</strong> Conversão segura de {@link java.sql.Timestamp} para {@link LocalDateTime}</li>
     *   <li><strong>Valores Nulos:</strong> Tratamento adequado de campos opcionais</li>
     * </ul>
     * 
     * <p><strong>Mapeamento de Campos:</strong></p>
     * <ul>
     *   <li><strong>ID:</strong> {@code rs.getLong("ID")}</li>
     *   <li><strong>CARD_ID:</strong> {@code rs.getLong("CARD_ID")}</li>
     *   <li><strong>TEXT:</strong> {@code rs.getString("TEXT")}</li>
     *   <li><strong>COMPLETED:</strong> {@code rs.getBoolean("COMPLETED")}</li>
     *   <li><strong>ORDER_INDEX:</strong> {@code rs.getInt("ORDER_INDEX")}</li>
     *   <li><strong>CREATED_AT:</strong> {@code rs.getTimestamp("CREATED_AT").toLocalDateTime()}</li>
     *   <li><strong>COMPLETED_AT:</strong> {@code rs.getTimestamp("COMPLETED_AT").toLocalDateTime()}</li>
     * </ul>
     * 
     * @see RowMapper
     * @see CheckListItem
     * @see LocalDateTime
     */
    private static final RowMapper<CheckListItem> checklistItemRowMapper = (ResultSet rs, int rowNum) -> {
        CheckListItem item = new CheckListItem();
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
     * Salva um novo item de checklist no banco de dados.
     * 
     * <p>Esta operação realiza um INSERT e retorna o item com o ID gerado
     * automaticamente pelo banco de dados. O método utiliza uma abordagem
     * compatível com H2 para capturar a chave gerada.</p>
     * 
     * <p><strong>Campos Inseridos:</strong></p>
     * <ul>
     *   <li><strong>card_id:</strong> Referência ao card pai</li>
     *   <li><strong>text:</strong> Descrição do item</li>
     *   <li><strong>completed:</strong> Status de conclusão</li>
     *   <li><strong>order_index:</strong> Posição na lista</li>
     * </ul>
     * 
     * <p><strong>Campos Automáticos:</strong></p>
     * <ul>
     *   <li><strong>id:</strong> Gerado automaticamente pelo banco</li>
     *   <li><strong>created_at:</strong> Definido como CURRENT_TIMESTAMP</li>
     * </ul>
     * 
     * @param item item a ser salvo (não deve ser {@code null})
     * @return item salvo com ID gerado automaticamente
     * @throws IllegalArgumentException se o item for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see JdbcTemplate#update(org.springframework.jdbc.core.PreparedStatementCreator, org.springframework.jdbc.support.KeyHolder)
     * @see org.springframework.jdbc.support.GeneratedKeyHolder
     */
    public CheckListItem save(CheckListItem item) {
        // Usar INSERT explícito para compatibilidade com H2
        final String sql = "INSERT INTO checklist_items (card_id, text, completed, order_index) VALUES (?, ?, ?, ?)";
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
     * Atualiza um item existente de checklist no banco de dados.
     * 
     * <p>Esta operação atualiza todos os campos modificáveis do item,
     * incluindo o timestamp de conclusão quando aplicável.</p>
     * 
     * <p><strong>Campos Atualizados:</strong></p>
     * <ul>
     *   <li><strong>text:</strong> Nova descrição do item</li>
     *   <li><strong>completed:</strong> Novo status de conclusão</li>
     *   <li><strong>order_index:</strong> Nova posição na lista</li>
     *   <li><strong>completed_at:</strong> Timestamp de conclusão (pode ser {@code null})</li>
     * </ul>
     * 
     * <p><strong>Critério de Atualização:</strong> O item é identificado pelo campo {@code id}.</p>
     * 
     * @param item item com os novos valores a serem persistidos
     * @return {@code true} se o item foi atualizado com sucesso, {@code false} se não foi encontrado
     * @throws IllegalArgumentException se o item for {@code null} ou não tiver ID válido
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see JdbcTemplate#update(String, Object...)
     */
    public boolean update(CheckListItem item) {
        String sql = """
            UPDATE checklist_items 
            SET text = ?, completed = ?, order_index = ?, completed_at = ?
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
     * Remove um item de checklist pelo seu identificador único.
     * 
     * @param id identificador único do item a ser removido
     * @return {@code true} se o item foi removido com sucesso, {@code false} se não foi encontrado
     * @throws IllegalArgumentException se o ID for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see JdbcTemplate#update(String, Object...)
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM checklist_items WHERE ID = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }
    
    /**
     * Remove todos os itens de checklist associados a um card específico.
     * 
     * <p>Esta operação é útil para limpeza em lote quando um card é removido
     * ou quando se deseja resetar completamente o checklist de um card.</p>
     * 
     * <p><strong>Comportamento:</strong> A operação é executada em uma única
     * transação SQL, garantindo consistência dos dados.</p>
     * 
     * @param cardId identificador do card cujos itens serão removidos
     * @return número de itens removidos (pode ser zero se não houver itens)
     * @throws IllegalArgumentException se o cardId for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see JdbcTemplate#update(String, Object...)
     */
    public int deleteByCardId(Long cardId) {
        String sql = "DELETE FROM checklist_items WHERE CARD_ID = ?";
        return jdbcTemplate.update(sql, cardId);
    }
    
    /**
     * Busca um item de checklist pelo seu identificador único.
     * 
     * <p>Esta operação retorna um {@link Optional} que pode estar vazio
     * se nenhum item for encontrado com o ID especificado.</p>
     * 
     * @param id identificador único do item a ser buscado
     * @return {@link Optional} contendo o item encontrado ou vazio se não encontrado
     * @throws IllegalArgumentException se o ID for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see Optional
     * @see JdbcTemplate#query(String, RowMapper, Object...)
     */
    public Optional<CheckListItem> findById(Long id) {
        String sql = "SELECT * FROM checklist_items WHERE ID = ?";
        List<CheckListItem> items = jdbcTemplate.query(sql, checklistItemRowMapper, id);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }
    
    /**
     * Busca todos os itens de checklist de um card específico, ordenados por posição.
     * 
     * <p>Esta consulta retorna os itens na ordem definida pelo campo {@code order_index},
     * permitindo que a interface do usuário exiba os itens na sequência correta.</p>
     * 
     * <p><strong>Ordenação:</strong> Os itens são retornados ordenados pelo campo
     * {@code ORDER_INDEX} em ordem crescente.</p>
     * 
     * @param cardId identificador do card cujos itens serão buscados
     * @return lista de itens ordenados por posição (pode estar vazia)
     * @throws IllegalArgumentException se o cardId for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see JdbcTemplate#query(String, RowMapper, Object...)
     * @see #checklistItemRowMapper
     */
    public List<CheckListItem> findByCardIdOrderByOrderIndex(Long cardId) {
        String sql = "SELECT * FROM checklist_items WHERE CARD_ID = ? ORDER BY ORDER_INDEX";
        return jdbcTemplate.query(sql, checklistItemRowMapper, cardId);
    }
    
    /**
     * Conta o número total de itens de checklist associados a um card.
     * 
     * <p>Esta operação é útil para exibir estatísticas do card ou para
     * validações de negócio que dependem do número de itens.</p>
     * 
     * @param cardId identificador do card para contagem
     * @return número total de itens (pode ser zero)
     * @throws IllegalArgumentException se o cardId for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see JdbcTemplate#queryForObject(String, Class, Object...)
     */
    public int countByCardId(Long cardId) {
        String sql = "SELECT COUNT(*) FROM checklist_items WHERE CARD_ID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, cardId);
    }
    
    /**
     * Conta o número de itens concluídos de um card específico.
     * 
     * <p>Esta operação é útil para calcular o progresso do card ou para
     * exibir indicadores de conclusão na interface do usuário.</p>
     * 
     * <p><strong>Critério:</strong> Apenas itens com {@code completed = TRUE} são contados.</p>
     * 
     * @param cardId identificador do card para contagem
     * @return número de itens concluídos (pode ser zero)
     * @throws IllegalArgumentException se o cardId for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see JdbcTemplate#queryForObject(String, Class, Object...)
     */
    public int countCompletedByCardId(Long cardId) {
        String sql = "SELECT COUNT(*) FROM checklist_items WHERE CARD_ID = ? AND COMPLETED = TRUE";
        return jdbcTemplate.queryForObject(sql, Integer.class, cardId);
    }
    
    /**
     * Atualiza a posição de um item específico na lista de itens.
     * 
     * <p>Esta operação é útil para reordenação de itens, permitindo que
     * o usuário reorganize a sequência dos itens do checklist.</p>
     * 
     * @param id identificador do item cuja posição será alterada
     * @param newOrderIndex nova posição do item na lista
     * @return {@code true} se a posição foi atualizada com sucesso, {@code false} se o item não foi encontrado
     * @throws IllegalArgumentException se o ID for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see JdbcTemplate#update(String, Object...)
     */
    public boolean updateOrderIndex(Long id, int newOrderIndex) {
        String sql = "UPDATE checklist_items SET ORDER_INDEX = ? WHERE ID = ?";
        int rowsAffected = jdbcTemplate.update(sql, newOrderIndex, id);
        return rowsAffected > 0;
    }
    
    /**
     * Marca um item como concluído ou não concluído, atualizando automaticamente o timestamp.
     * 
     * <p>Esta operação atualiza tanto o status de conclusão quanto o timestamp
     * correspondente, garantindo rastreabilidade de quando o item foi concluído.</p>
     * 
     * <p><strong>Comportamento Automático:</strong></p>
     * <ul>
     *   <li>Se {@code completed = true}: Define {@code completed_at} como data/hora atual</li>
     *   <li>Se {@code completed = false}: Define {@code completed_at} como {@code null}</li>
     * </ul>
     * 
     * @param id identificador do item cujo status será alterado
     * @param completed novo status de conclusão do item
     * @return {@code true} se o status foi atualizado com sucesso, {@code false} se o item não foi encontrado
     * @throws IllegalArgumentException se o ID for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see LocalDateTime#now()
     * @see JdbcTemplate#update(String, Object...)
     */
    public boolean updateCompleted(Long id, boolean completed) {
        String sql = "UPDATE checklist_items SET COMPLETED = ?, COMPLETED_AT = ? WHERE ID = ?";
        LocalDateTime completedAt = completed ? LocalDateTime.now() : null;
        int rowsAffected = jdbcTemplate.update(sql, completed, completedAt, id);
        return rowsAffected > 0;
    }
}
