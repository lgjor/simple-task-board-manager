package org.desviante.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.service.DataMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Inicializador de dados padrão do sistema.
 * 
 * <p>Executa na inicialização da aplicação para garantir que dados
 * essenciais estejam disponíveis e que dados existentes sejam migrados
 * para compatibilidade com novas versões.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "default", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final DataMigrationService dataMigrationService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando inicialização de dados padrão...");
        
        try {
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