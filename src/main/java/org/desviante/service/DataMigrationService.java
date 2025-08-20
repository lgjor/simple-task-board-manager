package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Serviço responsável por migrar dados existentes para compatibilidade
 * com a nova lógica de order_index e progressType.
 * 
 * <p>Este serviço corrige automaticamente dados existentes que podem
 * estar inconsistentes após atualizações do sistema.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "default", matchIfMissing = true)
public class DataMigrationService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Executa a migração de dados existentes para corrigir inconsistências.
     * 
     * <p>Corrige:
     * - order_index de todos os cards (sequencial por coluna)
     * - progressType baseado no tipo do card
     * - campos de progresso (total_units, current_units)</p>
     * 
     * @return true se a migração foi executada com sucesso
     */
    @Transactional
    public boolean migrateExistingData() {
        try {
            log.info("Iniciando migração de dados existentes...");
            
            // 1. Corrigir order_index
            fixOrderIndex();
            
            // 2. Corrigir ProgressType
            fixProgressType();
            
            // 3. Limpar campos de progresso
            cleanupProgressFields();
            
            // 4. Verificar integridade
            verifyDataIntegrity();
            
            log.info("Migração de dados existentes concluída com sucesso!");
            return true;
            
        } catch (Exception e) {
            log.error("Erro durante a migração de dados existentes: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Corrige o order_index de todos os cards existentes.
     */
    private void fixOrderIndex() {
        log.info("Corrigindo order_index dos cards existentes...");
        
        // Criar tabela temporária com nova ordem
        String createTempTable = """
            CREATE TEMPORARY TABLE temp_card_order AS
            SELECT 
                c.id,
                c.board_column_id,
                ROW_NUMBER() OVER (PARTITION BY c.board_column_id ORDER BY c.creation_date ASC) as new_order_index
            FROM cards c
            """;
        
        jdbcTemplate.execute(createTempTable);
        
        // Atualizar order_index
        String updateOrderIndex = """
            UPDATE cards 
            SET order_index = (
                SELECT new_order_index 
                FROM temp_card_order 
                WHERE temp_card_order.id = cards.id
            )
            """;
        
        int updatedRows = jdbcTemplate.update(updateOrderIndex);
        log.info("Order_index corrigido para {} cards", updatedRows);
        
        // Limpar tabela temporária
        jdbcTemplate.execute("DROP TABLE temp_card_order");
    }

    /**
     * Corrige o ProgressType baseado no tipo do card.
     */
    private void fixProgressType() {
        log.info("Corrigindo ProgressType dos cards existentes...");
        
        // Cards do tipo CARD (ID 1) devem ter ProgressType.NONE apenas se for NULL (não sobrescrever configurações do usuário)
        String fixCardType = """
            UPDATE cards 
            SET progress_type = 'NONE' 
            WHERE card_type_id = 1 AND progress_type IS NULL
            """;
        
        int cardTypeFixed = jdbcTemplate.update(fixCardType);
        log.info("ProgressType definido como NONE para {} cards do tipo CARD que estavam NULL", cardTypeFixed);
        
        // Cards dos tipos BOOK, VIDEO, COURSE (IDs 2, 3, 4) devem ter ProgressType.PERCENTAGE
        String fixProgressTypes = """
            UPDATE cards 
            SET progress_type = 'PERCENTAGE' 
            WHERE card_type_id IN (2, 3, 4) AND (progress_type = 'NONE' OR progress_type IS NULL)
            """;
        
        int progressTypesFixed = jdbcTemplate.update(fixProgressTypes);
        log.info("ProgressType corrigido para {} cards dos tipos BOOK/VIDEO/COURSE", progressTypesFixed);
    }

    /**
     * Limpa campos de progresso para cards que não devem tê-los.
     */
    private void cleanupProgressFields() {
        log.info("Limpando campos de progresso...");
        
        // Cards com ProgressType.NONE não devem ter total_units ou current_units
        String cleanupNoProgress = """
            UPDATE cards 
            SET total_units = NULL, current_units = NULL 
            WHERE progress_type = 'NONE' AND (total_units IS NOT NULL OR current_units IS NOT NULL)
            """;
        
        int noProgressCleaned = jdbcTemplate.update(cleanupNoProgress);
        log.info("Campos de progresso limpos para {} cards sem progresso", noProgressCleaned);
        
        // Garantir que cards com ProgressType.PERCENTAGE tenham valores válidos
        String fixProgressValues = """
            UPDATE cards 
            SET total_units = COALESCE(total_units, 1), current_units = COALESCE(current_units, 0)
            WHERE progress_type = 'PERCENTAGE' AND (total_units IS NULL OR current_units IS NULL)
            """;
        
        int progressValuesFixed = jdbcTemplate.update(fixProgressValues);
        log.info("Valores de progresso corrigidos para {} cards com progresso", progressValuesFixed);
    }

    /**
     * Verifica a integridade dos dados após a migração.
     */
    private void verifyDataIntegrity() {
        log.info("Verificando integridade dos dados...");
        
        String integrityQuery = """
            SELECT 
                COUNT(*) as total_cards,
                COUNT(CASE WHEN progress_type = 'NONE' THEN 1 END) as cards_sem_progresso,
                COUNT(CASE WHEN progress_type = 'PERCENTAGE' THEN 1 END) as cards_com_progresso,
                COUNT(CASE WHEN order_index > 0 THEN 1 END) as cards_com_order_index_valido
            FROM cards
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(integrityQuery);
        
        if (!results.isEmpty()) {
            Map<String, Object> stats = results.get(0);
            log.info("Estatísticas da migração:");
            log.info("- Total de cards: {}", stats.get("total_cards"));
            log.info("- Cards sem progresso: {}", stats.get("cards_sem_progresso"));
            log.info("- Cards com progresso: {}", stats.get("cards_com_progresso"));
            log.info("- Cards com order_index válido: {}", stats.get("cards_com_order_index_valido"));
        }
    }

    /**
     * Verifica se a migração é necessária.
     * 
     * @return true se há dados que precisam ser migrados
     */
    public boolean isMigrationNeeded() {
        try {
            // Verificar se há cards com order_index = 0 ou ProgressType incorreto
            String checkQuery = """
                SELECT COUNT(*) FROM cards 
                WHERE order_index = 0 
                   OR (card_type_id IN (2, 3, 4) AND progress_type = 'NONE')
                   OR (progress_type = 'NONE' AND (total_units IS NOT NULL OR current_units IS NOT NULL))
                   OR progress_type IS NULL
                """;
            
            int count = jdbcTemplate.queryForObject(checkQuery, Integer.class);
            return count > 0;
            
        } catch (Exception e) {
            log.warn("Erro ao verificar se migração é necessária: {}", e.getMessage());
            return false;
        }
    }
}
