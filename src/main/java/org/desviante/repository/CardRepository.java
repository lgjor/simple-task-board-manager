package org.desviante.repository;

import org.desviante.model.Card;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de persistência para cards (tarefas).
 * 
 * <p>Responsável por todas as operações de banco de dados relacionadas
 * aos cards, incluindo CRUD básico e consultas específicas como
 * busca por coluna, ordenação por data de criação, etc.</p>
 * 
 * <p>Os cards são as unidades fundamentais de trabalho no sistema,
 * representando tarefas que podem ser movidas entre colunas de um quadro.
 * Cada card possui datas de criação, atualização e conclusão para
 * rastreamento completo do ciclo de vida.</p>
 * 
 * <p>Utiliza JDBC direto com NamedParameterJdbcTemplate para operações
 * de banco, mantendo controle total sobre as consultas SQL.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Card
 */
@Repository
public class CardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final JdbcTemplate simpleJdbcTemplate;
    private static final String LOG_FILE = System.getProperty("user.home") + "/myboards/card_repository_debug.log";

    /**
     * Construtor que inicializa os templates JDBC necessários.
     * 
     * @param dataSource fonte de dados para conexão com o banco
     */
    public CardRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("cards")
                .usingColumns("title", "description", "card_type_id", "total_units", "current_units", "progress_type", "creation_date", "last_update_date", "completion_date", "scheduled_date", "due_date", "board_column_id", "order_index")
                .usingGeneratedKeyColumns("id");
        
        // Garantir que as colunas de agendamento existam
        ensureSchedulingColumnsExist();
    }

    /**
     * Mapeia os resultados do banco para objetos Card.
     * 
     * <p>Trata adequadamente campos que podem ser nulos, como
     * completion_date, garantindo a integridade dos dados.
     * Converte timestamps para LocalDateTime para uso no sistema.</p>
     */
    private final RowMapper<Card> cardRowMapper = (ResultSet rs, int rowNum) -> {
        Card card = new Card();
        card.setId(rs.getLong("id"));
        card.setTitle(rs.getString("title"));
        card.setDescription(rs.getString("description"));
        
        // Mapear o tipo de card
        Long cardTypeId = rs.getObject("card_type_id", Long.class);
        if (cardTypeId != null) {
            card.setCardTypeId(cardTypeId);
            // Intencional: não carregamos o objeto CardType aqui para evitar acoplamento no repositório.
            // A camada de serviço (ex.: CardService) é responsável por carregar o CardType quando necessário.
        }
        
        // Mapear campos de progresso
        card.setTotalUnits(rs.getObject("total_units", Integer.class));
        card.setCurrentUnits(rs.getObject("current_units", Integer.class));
        
        // Mapear o tipo de progresso
        String progressTypeStr = rs.getString("progress_type");
        if (progressTypeStr != null) {
            try {
                card.setProgressType(org.desviante.model.enums.ProgressType.valueOf(progressTypeStr));
            } catch (IllegalArgumentException e) {
                // Se o valor não for válido, usar NONE como padrão
                card.setProgressType(org.desviante.model.enums.ProgressType.NONE);
            }
        } else {
            // Se for null, usar NONE como padrão para compatibilidade
            card.setProgressType(org.desviante.model.enums.ProgressType.NONE);
        }
        
        card.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
        card.setLastUpdateDate(rs.getTimestamp("last_update_date").toLocalDateTime());
        // Trata a data de conclusão, que pode ser nula
        Timestamp completionTimestamp = rs.getTimestamp("completion_date");
        if (completionTimestamp != null) {
            card.setCompletionDate(completionTimestamp.toLocalDateTime());
        }
        // Trata a data de agendamento, que pode ser nula
        Timestamp scheduledTimestamp = rs.getTimestamp("scheduled_date");
        if (scheduledTimestamp != null) {
            card.setScheduledDate(scheduledTimestamp.toLocalDateTime());
        }
        // Trata a data de vencimento, que pode ser nula
        Timestamp dueTimestamp = rs.getTimestamp("due_date");
        if (dueTimestamp != null) {
            card.setDueDate(dueTimestamp.toLocalDateTime());
        }
        card.setBoardColumnId(rs.getLong("board_column_id"));
        
        // Mapear o campo order_index
        Integer orderIndex = rs.getObject("order_index", Integer.class);
        card.setOrderIndex(orderIndex != null ? orderIndex : 0);
        
        return card;
    };

    /**
     * Busca um card específico pelo ID.
     * 
     * @param id identificador único do card
     * @return Optional contendo o card se encontrado, vazio caso contrário
     */
    public Optional<Card> findById(Long id) {
        String sql = "SELECT * FROM cards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, cardRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca cards de múltiplas colunas de uma vez.
     * 
     * <p>Método otimizado para carregar cards de várias colunas
     * em uma única consulta, reduzindo o número de acessos ao banco.
     * Os cards são retornados ordenados por order_index (ASC)
     * para manter a sequência manual definida pelo usuário.</p>
     * 
     * @param columnIds lista de identificadores das colunas
     * @return lista de cards ordenados por order_index
     */
    public List<Card> findByBoardColumnIdIn(List<Long> columnIds) {
        if (columnIds == null || columnIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM cards WHERE board_column_id IN (:columnIds) ORDER BY order_index ASC, creation_date ASC";
        var params = new MapSqlParameterSource("columnIds", columnIds);
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Salva ou atualiza um card no banco de dados.
     * 
     * <p>Se o card não possui ID, executa INSERT e retorna o ID gerado.
     * Se possui ID, executa UPDATE dos campos modificáveis.
     * A data de criação não é atualizada em operações de UPDATE,
     * apenas last_update_date é atualizada automaticamente.</p>
     * 
     * @param card card a ser salvo
     * @return card com ID atualizado (em caso de inserção)
     */
    @Transactional
    public Card save(Card card) {
        var params = new MapSqlParameterSource()
                .addValue("title", card.getTitle())
                .addValue("description", card.getDescription())
                .addValue("card_type_id", card.getCardTypeId())
                .addValue("total_units", card.getTotalUnits())
                .addValue("current_units", card.getCurrentUnits())
                .addValue("progress_type", card.getProgressTypeOrDefault().name())
                .addValue("creation_date", card.getCreationDate())
                .addValue("last_update_date", card.getLastUpdateDate())
                .addValue("completion_date", card.getCompletionDate())
                .addValue("scheduled_date", card.getScheduledDate())
                .addValue("due_date", card.getDueDate())
                .addValue("board_column_id", card.getBoardColumnId())
                .addValue("order_index", card.getOrderIndex());

        if (card.getId() == null) {
            Number newId = jdbcInsert.executeAndReturnKey(params);
            card.setId(newId.longValue());
        } else {
            params.addValue("id", card.getId());
            
            // Verificar se as colunas de agendamento existem e construir SQL dinamicamente
            boolean hasSchedulingColumns = checkSchedulingColumnsExist();
            
            String sql;
            if (hasSchedulingColumns) {
                sql = """
                        UPDATE cards SET
                            title = :title,
                            description = :description,
                            card_type_id = :card_type_id,
                            total_units = :total_units,
                            current_units = :current_units,
                            progress_type = :progress_type,
                            last_update_date = :last_update_date,
                            completion_date = :completion_date,
                            scheduled_date = :scheduled_date,
                            due_date = :due_date,
                            board_column_id = :board_column_id,
                            order_index = :order_index
                        WHERE id = :id
                        """;
            } else {
                sql = """
                        UPDATE cards SET
                            title = :title,
                            description = :description,
                            card_type_id = :card_type_id,
                            total_units = :total_units,
                            current_units = :current_units,
                            progress_type = :progress_type,
                            last_update_date = :last_update_date,
                            completion_date = :completion_date,
                            board_column_id = :board_column_id,
                            order_index = :order_index
                        WHERE id = :id
                        """;
            }
            
            try {
                String logMsg1 = "🔧 CARD REPOSITORY - Atualizando card ID: " + card.getId();
                String logMsg2 = "🔧 CARD REPOSITORY - Scheduled Date: " + card.getScheduledDate();
                String logMsg3 = "🔧 CARD REPOSITORY - Due Date: " + card.getDueDate();
                String logMsg4 = "🔧 CARD REPOSITORY - Colunas de agendamento existem: " + hasSchedulingColumns;
                
                System.out.println(logMsg1);
                System.out.println(logMsg2);
                System.out.println(logMsg3);
                System.out.println(logMsg4);
                
                logToFile(logMsg1);
                logToFile(logMsg2);
                logToFile(logMsg3);
                logToFile(logMsg4);
                
                logToFile("🔧 CARD REPOSITORY - Executando SQL: " + sql);
                logToFile("🔧 CARD REPOSITORY - Parâmetros: " + params.getValues());
                
                int rowsAffected = jdbcTemplate.update(sql, params);
                
                String successMsg = "✅ CARD REPOSITORY - Card atualizado com sucesso. Linhas afetadas: " + rowsAffected;
                System.out.println(successMsg);
                logToFile(successMsg);
                
            } catch (Exception e) {
                String errorMsg1 = "❌ CARD REPOSITORY - Erro ao atualizar card ID " + card.getId() + ": " + e.getMessage();
                String errorMsg2 = "❌ CARD REPOSITORY - SQL: " + sql;
                String errorMsg3 = "❌ CARD REPOSITORY - Parâmetros: " + params.getValues();
                
                System.err.println(errorMsg1);
                System.err.println(errorMsg2);
                System.err.println(errorMsg3);
                
                logToFile(errorMsg1);
                logToFile(errorMsg2);
                logToFile(errorMsg3);
                
                // Tentar fallback: atualizar sem as colunas de agendamento
                if (hasSchedulingColumns) {
                    String fallbackMsg = "🔄 CARD REPOSITORY - Tentando fallback sem colunas de agendamento...";
                    System.out.println(fallbackMsg);
                    logToFile(fallbackMsg);
                    try {
                        String fallbackSql = """
                                UPDATE cards SET
                                    title = :title,
                                    description = :description,
                                    card_type_id = :card_type_id,
                                    total_units = :total_units,
                                    current_units = :current_units,
                                    progress_type = :progress_type,
                                    last_update_date = :last_update_date,
                                    completion_date = :completion_date,
                                    board_column_id = :board_column_id,
                                    order_index = :order_index
                                WHERE id = :id
                                """;
                        
                        logToFile("🔧 CARD REPOSITORY - Executando fallback SQL: " + fallbackSql);
                        
                        int rowsAffected = jdbcTemplate.update(fallbackSql, params);
                        
                        String fallbackSuccessMsg = "✅ CARD REPOSITORY - Fallback executado com sucesso. Linhas afetadas: " + rowsAffected;
                        System.out.println(fallbackSuccessMsg);
                        logToFile(fallbackSuccessMsg);
                        
                        // Tentar atualizar as colunas de agendamento separadamente
                        updateSchedulingColumns(card);
                        
                    } catch (Exception fallbackException) {
                        System.err.println("❌ CARD REPOSITORY - Fallback também falhou: " + fallbackException.getMessage());
                        e.printStackTrace();
                        throw e; // Re-lançar a exceção original
                    }
                } else {
                    e.printStackTrace();
                    throw e; // Re-lançar a exceção para que o erro seja propagado
                }
            }
        }
        return card;
    }

    /**
     * Atualiza apenas as colunas de agendamento de um card.
     * 
     * @param card card com as datas de agendamento a serem atualizadas
     */
    private void updateSchedulingColumns(Card card) {
        try {
            String logMsg = "🔧 CARD REPOSITORY - Atualizando colunas de agendamento para card ID: " + card.getId();
            System.out.println(logMsg);
            logToFile(logMsg);
            
            String sql = "UPDATE cards SET scheduled_date = ?, due_date = ? WHERE id = ?";
            logToFile("🔧 CARD REPOSITORY - SQL agendamento: " + sql);
            logToFile("🔧 CARD REPOSITORY - Scheduled: " + card.getScheduledDate() + ", Due: " + card.getDueDate());
            
            int rowsAffected = simpleJdbcTemplate.update(sql, 
                card.getScheduledDate() != null ? Timestamp.valueOf(card.getScheduledDate()) : null,
                card.getDueDate() != null ? Timestamp.valueOf(card.getDueDate()) : null,
                card.getId());
            
            String successMsg = "✅ CARD REPOSITORY - Colunas de agendamento atualizadas. Linhas afetadas: " + rowsAffected;
            System.out.println(successMsg);
            logToFile(successMsg);
            
        } catch (Exception e) {
            String errorMsg = "❌ CARD REPOSITORY - Erro ao atualizar colunas de agendamento: " + e.getMessage();
            System.err.println(errorMsg);
            logToFile(errorMsg);
            e.printStackTrace();
        }
    }

    /**
     * Escreve uma mensagem de log no arquivo de debug.
     * 
     * @param message mensagem a ser logada
     */
    private void logToFile(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println("[" + LocalDateTime.now() + "] " + message);
            writer.flush();
        } catch (IOException e) {
            // Se não conseguir escrever no arquivo, pelo menos imprimir no console
            System.err.println("Erro ao escrever log: " + e.getMessage());
        }
    }

    /**
     * Verifica se as colunas de agendamento existem na tabela cards.
     * 
     * @return true se as colunas scheduled_date e due_date existem
     */
    private boolean checkSchedulingColumnsExist() {
        try {
            // Tentar executar uma consulta que usa as colunas de agendamento
            simpleJdbcTemplate.query("SELECT scheduled_date, due_date FROM cards LIMIT 1", (rs, rowNum) -> null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Garante que as colunas de agendamento existam na tabela cards.
     * 
     * <p>Este método é executado no construtor para garantir que as colunas
     * scheduled_date e due_date existam antes de qualquer operação com cards.</p>
     */
    private void ensureSchedulingColumnsExist() {
        try {
            String logMsg = "🔧 CARD REPOSITORY - Verificando colunas de agendamento...";
            System.out.println(logMsg);
            logToFile(logMsg);
            
            // Verificar se a coluna scheduled_date existe
            try {
                simpleJdbcTemplate.queryForObject("SELECT scheduled_date FROM cards LIMIT 1", String.class);
                System.out.println("✅ CARD REPOSITORY - Coluna scheduled_date já existe");
            } catch (Exception e) {
                System.out.println("➕ CARD REPOSITORY - Adicionando coluna scheduled_date...");
                simpleJdbcTemplate.execute("ALTER TABLE cards ADD COLUMN scheduled_date TIMESTAMP NULL");
                System.out.println("✅ CARD REPOSITORY - Coluna scheduled_date adicionada");
            }
            
            // Verificar se a coluna due_date existe
            try {
                simpleJdbcTemplate.queryForObject("SELECT due_date FROM cards LIMIT 1", String.class);
                System.out.println("✅ CARD REPOSITORY - Coluna due_date já existe");
            } catch (Exception e) {
                System.out.println("➕ CARD REPOSITORY - Adicionando coluna due_date...");
                simpleJdbcTemplate.execute("ALTER TABLE cards ADD COLUMN due_date TIMESTAMP NULL");
                System.out.println("✅ CARD REPOSITORY - Coluna due_date adicionada");
            }
            
            // Criar índices se não existirem
            try {
                simpleJdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_cards_scheduled_date ON cards(scheduled_date)");
                System.out.println("✅ CARD REPOSITORY - Índice scheduled_date criado/verificado");
            } catch (Exception e) {
                System.out.println("ℹ️ CARD REPOSITORY - Índice scheduled_date já existe ou erro: " + e.getMessage());
            }
            
            try {
                simpleJdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_cards_due_date ON cards(due_date)");
                System.out.println("✅ CARD REPOSITORY - Índice due_date criado/verificado");
            } catch (Exception e) {
                System.out.println("ℹ️ CARD REPOSITORY - Índice due_date já existe ou erro: " + e.getMessage());
            }
            
            try {
                simpleJdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_cards_urgency ON cards(completion_date, due_date)");
                System.out.println("✅ CARD REPOSITORY - Índice urgency criado/verificado");
            } catch (Exception e) {
                System.out.println("ℹ️ CARD REPOSITORY - Índice urgency já existe ou erro: " + e.getMessage());
            }
            
            System.out.println("🎉 CARD REPOSITORY - Verificação de colunas concluída!");
            
        } catch (Exception e) {
            System.err.println("❌ CARD REPOSITORY - Erro ao garantir colunas de agendamento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Busca cards de uma coluna específica ordenados por order_index.
     * 
     * @param columnId identificador da coluna
     * @return lista de cards ordenados por order_index
     */
    public List<Card> findByBoardColumnId(Long columnId) {
        String sql = "SELECT * FROM cards WHERE board_column_id = :columnId ORDER BY order_index ASC, creation_date ASC";
        var params = new MapSqlParameterSource("columnId", columnId);
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Busca o maior order_index de uma coluna específica.
     * 
     * @param columnId identificador da coluna
     * @return o maior order_index encontrado, ou null se a coluna estiver vazia
     */
    public Integer findMaxOrderIndexByColumnId(Long columnId) {
        String sql = "SELECT MAX(order_index) FROM cards WHERE board_column_id = :columnId";
        var params = new MapSqlParameterSource("columnId", columnId);
        try {
            Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return result;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Busca o próximo card na mesma coluna.
     * 
     * @param columnId identificador da coluna
     * @param currentOrderIndex order_index do card atual
     * @return Optional contendo o próximo card se existir
     */
    public Optional<Card> findNextCard(Long columnId, Integer currentOrderIndex) {
        String sql = "SELECT * FROM cards WHERE board_column_id = :columnId AND order_index > :currentOrderIndex ORDER BY order_index ASC LIMIT 1";
        var params = new MapSqlParameterSource()
                .addValue("columnId", columnId)
                .addValue("currentOrderIndex", currentOrderIndex);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, cardRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca o card anterior na mesma coluna.
     * 
     * @param columnId identificador da coluna
     * @param currentOrderIndex order_index do card atual
     * @return Optional contendo o card anterior se existir
     */
    public Optional<Card> findPreviousCard(Long columnId, Integer currentOrderIndex) {
        String sql = "SELECT * FROM cards WHERE board_column_id = :columnId AND order_index < :currentOrderIndex ORDER BY order_index DESC LIMIT 1";
        var params = new MapSqlParameterSource()
                .addValue("columnId", columnId)
                .addValue("currentOrderIndex", currentOrderIndex);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, cardRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Troca a posição de dois cards na mesma coluna.
     * 
     * @param card1Id ID do primeiro card
     * @param card1NewOrder nova ordem do primeiro card
     * @param card2Id ID do segundo card
     * @param card2NewOrder nova ordem do segundo card
     */
    @Transactional
    public void swapCardPositions(Long card1Id, Integer card1NewOrder, Long card2Id, Integer card2NewOrder) {
        // Atualiza o primeiro card
        String sql = "UPDATE cards SET order_index = :newOrder WHERE id = :cardId";
        var params1 = new MapSqlParameterSource()
                .addValue("newOrder", card1NewOrder)
                .addValue("cardId", card1Id);
        jdbcTemplate.update(sql, params1);

        // Atualiza o segundo card
        var params2 = new MapSqlParameterSource()
                .addValue("newOrder", card2NewOrder)
                .addValue("cardId", card2Id);
        jdbcTemplate.update(sql, params2);
    }

    /**
     * Remove um card do banco de dados pelo ID.
     * 
     * @param id identificador do card a ser removido
     */
    @Transactional
    public void deleteById(Long id) {
        String sql = "DELETE FROM cards WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }

    /**
     * Verifica se existem cards usando um tipo específico de card.
     * 
     * <p>Esta consulta é utilizada para validar se um tipo de card pode ser
     * alterado com segurança, verificando se há cards que dependem dele.</p>
     * 
     * @param cardTypeId identificador do tipo de card para verificação
     * @return {@code true} se existem cards usando o tipo, {@code false} caso contrário
     * @throws IllegalArgumentException se o cardTypeId for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     */
    public boolean existsByCardTypeId(Long cardTypeId) {
        if (cardTypeId == null) {
            throw new IllegalArgumentException("ID do tipo de card não pode ser null");
        }
        
        String sql = "SELECT COUNT(*) FROM cards WHERE card_type_id = :cardTypeId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cardTypeId", cardTypeId);
        
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Conta quantos cards estão usando um tipo específico de card.
     * 
     * <p>Esta consulta fornece informações detalhadas sobre o uso de um tipo
     * de card, permitindo que a interface do usuário mostre quantos cards
     * seriam afetados pela remoção do tipo.</p>
     * 
     * @param cardTypeId identificador do tipo de card para contagem
     * @return número de cards usando o tipo especificado (pode ser zero)
     * @throws IllegalArgumentException se o cardTypeId for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     */
    public int countByCardTypeId(Long cardTypeId) {
        if (cardTypeId == null) {
            throw new IllegalArgumentException("ID do tipo de card não pode ser null");
        }
        
        String sql = "SELECT COUNT(*) FROM cards WHERE card_type_id = :cardTypeId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cardTypeId", cardTypeId);
        
        return jdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    /**
     * Busca todos os cards que usam um tipo específico de card.
     * 
     * <p>Esta consulta retorna uma lista de cards que dependem do tipo
     * especificado, permitindo que a interface do usuário mostre quais
     * cards seriam afetados pela remoção do tipo.</p>
     * 
     * @param cardTypeId identificador do tipo de card para busca
     * @return lista de cards usando o tipo especificado (pode estar vazia)
     * @throws IllegalArgumentException se o cardTypeId for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see #cardRowMapper
     */
    public List<Card> findByCardTypeId(Long cardTypeId) {
        if (cardTypeId == null) {
            throw new IllegalArgumentException("ID do tipo de card não pode ser null");
        }
        
        String sql = "SELECT * FROM cards WHERE card_type_id = :cardTypeId ORDER BY creation_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cardTypeId", cardTypeId);
        
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }
    
    /**
     * Verifica se existem cards usando um tipo específico de progresso.
     * 
     * <p>Esta consulta é utilizada para validar se um tipo de progresso pode ser
     * alterado com segurança, verificando se há cards que dependem dele.</p>
     * 
     * @param progressType tipo de progresso a ser verificado
     * @return {@code true} se existem cards usando o tipo, {@code false} caso contrário
     * @throws IllegalArgumentException se o progressType for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     */
    public boolean existsByProgressType(org.desviante.model.enums.ProgressType progressType) {
        if (progressType == null) {
            throw new IllegalArgumentException("Tipo de progresso não pode ser null");
        }
        
        String sql = "SELECT COUNT(*) FROM cards WHERE progress_type = :progressType";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("progressType", progressType.name());
        
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
    
    /**
     * Conta quantos cards estão usando um tipo específico de progresso.
     * 
     * <p>Esta consulta fornece informações detalhadas sobre o uso de um tipo
     * de progresso, permitindo que a interface do usuário mostre quantos cards
     * seriam afetados pela alteração do tipo.</p>
     * 
     * @param progressType tipo de progresso para contagem
     * @return número de cards usando o tipo especificado (pode ser zero)
     * @throws IllegalArgumentException se o progressType for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     */
    public int countByProgressType(org.desviante.model.enums.ProgressType progressType) {
        if (progressType == null) {
            throw new IllegalArgumentException("Tipo de progresso não pode ser null");
        }
        
        String sql = "SELECT COUNT(*) FROM cards WHERE progress_type = :progressType";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("progressType", progressType.name());
        
        return jdbcTemplate.queryForObject(sql, params, Integer.class);
    }
    
    /**
     * Busca todos os cards que usam um tipo específico de progresso.
     * 
     * <p>Esta consulta retorna uma lista de cards que dependem do tipo
     * de progresso especificado, permitindo que a interface do usuário mostre quais
     * cards seriam afetados pela alteração do tipo.</p>
     * 
     * @param progressType tipo de progresso para busca
     * @return lista de cards usando o tipo especificado (pode estar vazia)
     * @throws IllegalArgumentException se o progressType for {@code null}
     * @throws RuntimeException se houver erro na operação de banco
     * 
     * @see #cardRowMapper
     */
    public List<Card> findByProgressType(org.desviante.model.enums.ProgressType progressType) {
        if (progressType == null) {
            throw new IllegalArgumentException("Tipo de progresso não pode ser null");
        }
        
        String sql = "SELECT * FROM cards WHERE progress_type = :progressType ORDER BY creation_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("progressType", progressType.name());
        
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Busca cards agendados para uma data específica.
     * 
     * @param date data para busca de cards agendados
     * @return lista de cards agendados para a data especificada
     */
    public List<Card> findByScheduledDate(java.time.LocalDate date) {
        // Usar sintaxe compatível com H2: CAST para DATE ou comparação direta com TIMESTAMP
        String sql = "SELECT * FROM cards WHERE CAST(scheduled_date AS DATE) = :date ORDER BY scheduled_date ASC";
        var params = new MapSqlParameterSource("date", date);
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Busca cards próximos do vencimento.
     * 
     * @param daysThreshold número de dias para considerar "próximo do vencimento"
     * @return lista de cards próximos do vencimento
     */
    public List<Card> findNearDue(int daysThreshold) {
        String sql = """
                SELECT * FROM cards 
                WHERE due_date IS NOT NULL 
                AND completion_date IS NULL 
                AND due_date <= :threshold 
                AND due_date >= :now
                ORDER BY due_date ASC
                """;
        var params = new MapSqlParameterSource()
                .addValue("threshold", java.time.LocalDateTime.now().plusDays(daysThreshold))
                .addValue("now", java.time.LocalDateTime.now());
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Busca cards vencidos (não concluídos e com data de vencimento passada).
     * 
     * @return lista de cards vencidos
     */
    public List<Card> findOverdue() {
        String sql = """
                SELECT * FROM cards 
                WHERE due_date IS NOT NULL 
                AND completion_date IS NULL 
                AND due_date < :now
                ORDER BY due_date ASC
                """;
        var params = new MapSqlParameterSource("now", java.time.LocalDateTime.now());
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Busca cards por nível de urgência baseado na data de vencimento.
     * 
     * @param urgencyLevel nível de urgência (0-4)
     * @return lista de cards com o nível de urgência especificado
     */
    public List<Card> findByUrgencyLevel(int urgencyLevel) {
        String sql = """
                SELECT * FROM cards 
                WHERE due_date IS NOT NULL 
                AND completion_date IS NULL
                AND (
                    CASE 
                        WHEN due_date < :now THEN 4
                        WHEN CAST(due_date AS DATE) = CAST(:now AS DATE) THEN 3
                        WHEN due_date <= :now + INTERVAL 1 DAY THEN 2
                        WHEN due_date <= :now + INTERVAL 3 DAY THEN 1
                        ELSE 0
                    END
                ) = :urgencyLevel
                ORDER BY due_date ASC
                """;
        var params = new MapSqlParameterSource()
                .addValue("now", java.time.LocalDateTime.now())
                .addValue("urgencyLevel", urgencyLevel);
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Busca cards agendados para um período específico.
     * 
     * @param startDate data de início do período
     * @param endDate data de fim do período
     * @return lista de cards agendados no período
     */
    public List<Card> findByScheduledDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        // Usar sintaxe compatível com H2: CAST para DATE
        String sql = """
                SELECT * FROM cards 
                WHERE scheduled_date IS NOT NULL 
                AND CAST(scheduled_date AS DATE) BETWEEN :startDate AND :endDate
                ORDER BY scheduled_date ASC
                """;
        var params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Busca cards com vencimento em um período específico.
     * 
     * @param startDate data de início do período
     * @param endDate data de fim do período
     * @return lista de cards com vencimento no período
     */
    public List<Card> findByDueDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        // Usar sintaxe compatível com H2: CAST para DATE
        String sql = """
                SELECT * FROM cards 
                WHERE due_date IS NOT NULL 
                AND CAST(due_date AS DATE) BETWEEN :startDate AND :endDate
                ORDER BY due_date ASC
                """;
        var params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        return jdbcTemplate.query(sql, params, cardRowMapper);
    }

    /**
     * Busca todos os cards que possuem data de agendamento.
     * 
     * @return lista de cards com data de agendamento
     */
    public List<Card> findByScheduledDateNotNull() {
        String sql = "SELECT * FROM cards WHERE scheduled_date IS NOT NULL ORDER BY scheduled_date ASC";
        return jdbcTemplate.query(sql, cardRowMapper);
    }
}