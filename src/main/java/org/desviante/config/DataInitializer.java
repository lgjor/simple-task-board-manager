package org.desviante.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.service.DataMigrationService;
import org.desviante.service.SafeDatabaseMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Inicializador de dados padrão do sistema que executa durante a inicialização da aplicação.
 * 
 * <p>Esta classe implementa {@link CommandLineRunner} para executar automaticamente
 * na inicialização da aplicação Spring Boot. Sua principal responsabilidade é:</p>
 * 
 * <ul>
 *   <li>Verificar se há necessidade de migração de dados existentes</li>
 *   <li>Executar migrações automáticas quando necessário</li>
 *   <li>Garantir compatibilidade entre versões do sistema</li>
 *   <li>Registrar logs detalhados do processo de inicialização</li>
 * </ul>
 * 
 * <p>A classe é condicionalmente ativada apenas no perfil padrão da aplicação
 * através da anotação {@link ConditionalOnProperty}, garantindo que migrações
 * automáticas não sejam executadas em ambientes de teste ou desenvolvimento.</p>
 * 
 * <p><strong>Importante:</strong> Erros durante a migração não interrompem
 * a inicialização da aplicação, permitindo que o sistema continue funcionando
 * mesmo com falhas na migração de dados.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CommandLineRunner
 * @see DataMigrationService
 * @see ConditionalOnProperty
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "default", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    /**
     * Serviço responsável pela migração de dados existentes.
     * Injetado automaticamente pelo Spring através do construtor.
     */
    private final DataMigrationService dataMigrationService;
    
    /**
     * Serviço responsável por migrações seguras do banco de dados.
     * Injetado automaticamente pelo Spring através do construtor.
     */
    private final SafeDatabaseMigrationService safeMigrationService;

    /**
     * Executa a inicialização de dados padrão durante a inicialização da aplicação.
     * 
     * <p>Este método é chamado automaticamente pelo Spring Boot após a inicialização
     * completa do contexto da aplicação. O processo inclui:</p>
     * 
     * <ol>
     *   <li>Verificação se migração de dados é necessária</li>
     *   <li>Execução da migração quando aplicável</li>
     *   <li>Registro de logs detalhados do processo</li>
     *   <li>Tratamento de erros sem interromper a aplicação</li>
     * </ol>
     * 
     * <p><strong>Tratamento de Erros:</strong> Qualquer exceção durante o processo
     * é capturada e registrada no log, mas não interrompe a inicialização da aplicação.
     * Isso garante que o sistema continue funcionando mesmo com falhas na migração.</p>
     * 
     * @param args argumentos da linha de comando passados para a aplicação
     * @throws Exception pode lançar exceções durante a migração, mas são capturadas
     *         e tratadas internamente sem propagar para o Spring Boot
     * 
     * @see DataMigrationService#isMigrationNeeded()
     * @see DataMigrationService#migrateExistingData()
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando inicialização de dados padrão...");
        
        try {
            // Executar migração segura para suporte à ordenação de cards
            log.info("Executando migração segura para suporte à ordenação de cards...");
            boolean cardOrderingMigrationApplied = safeMigrationService.migrateCardOrderingSupport();
            
            if (cardOrderingMigrationApplied) {
                log.info("Migração de ordenação de cards aplicada com sucesso!");
            } else {
                log.info("Migração de ordenação de cards já estava aplicada.");
            }
            
            // Exibir estatísticas do banco
            log.info("Estatísticas do banco de dados:");
            safeMigrationService.getDatabaseStats().forEach(log::info);
            
            // Verificar se migração de dados existentes é necessária
            if (dataMigrationService.isMigrationNeeded()) {
                log.info("Migração de dados existentes necessária. Executando...");
                boolean migrationSuccess = dataMigrationService.migrateExistingData();
                
                if (migrationSuccess) {
                    log.info("Migração de dados existentes concluída com sucesso!");
                } else {
                    log.warn("Migração de dados existentes falhou. Verifique os logs para mais detalhes.");
                }
            } else {
                log.info("Migração de dados existentes não é necessária.");
            }
            
            log.info("Inicialização de dados padrão concluída com sucesso.");
            
        } catch (Exception e) {
            log.error("Erro durante a inicialização de dados: {}", e.getMessage(), e);
            // Não interromper a inicialização da aplicação por causa de erro na migração
        }
    }
} 