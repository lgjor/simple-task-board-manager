package org.desviante.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Configuração para aplicar migração das colunas de agendamento automaticamente.
 * 
 * <p>Esta classe garante que as colunas scheduled_date e due_date sejam criadas
 * no banco de dados, mesmo que o Liquibase não execute a migração.</p>
 * 
 * @author Aú Desviante - Lucas Godoy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulingMigrationConfig implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("🔄 Verificando migração das colunas de agendamento...");
        
        try {
            // Verificar se a coluna scheduled_date existe
            try {
                jdbcTemplate.queryForObject("SELECT scheduled_date FROM cards LIMIT 1", String.class);
                log.info("✅ Coluna scheduled_date já existe");
            } catch (Exception e) {
                log.info("➕ Adicionando coluna scheduled_date...");
                jdbcTemplate.execute("ALTER TABLE cards ADD COLUMN scheduled_date TIMESTAMP NULL");
                log.info("✅ Coluna scheduled_date adicionada com sucesso");
            }
            
            // Verificar se a coluna due_date existe
            try {
                jdbcTemplate.queryForObject("SELECT due_date FROM cards LIMIT 1", String.class);
                log.info("✅ Coluna due_date já existe");
            } catch (Exception e) {
                log.info("➕ Adicionando coluna due_date...");
                jdbcTemplate.execute("ALTER TABLE cards ADD COLUMN due_date TIMESTAMP NULL");
                log.info("✅ Coluna due_date adicionada com sucesso");
            }
            
            // Criar índices se não existirem
            try {
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_cards_scheduled_date ON cards(scheduled_date)");
                log.info("✅ Índice idx_cards_scheduled_date criado/verificado");
            } catch (Exception e) {
                log.debug("ℹ️ Índice idx_cards_scheduled_date já existe ou erro: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_cards_due_date ON cards(due_date)");
                log.info("✅ Índice idx_cards_due_date criado/verificado");
            } catch (Exception e) {
                log.debug("ℹ️ Índice idx_cards_due_date já existe ou erro: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_cards_urgency ON cards(completion_date, due_date)");
                log.info("✅ Índice idx_cards_urgency criado/verificado");
            } catch (Exception e) {
                log.debug("ℹ️ Índice idx_cards_urgency já existe ou erro: {}", e.getMessage());
            }
            
            log.info("🎉 Migração das colunas de agendamento concluída com sucesso!");
            
        } catch (Exception e) {
            log.error("❌ Erro ao aplicar migração das colunas de agendamento: {}", e.getMessage());
            // Não relançar a exceção para não impedir o startup da aplicação
        }
    }
}
