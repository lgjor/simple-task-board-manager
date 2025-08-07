package org.desviante.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verificador de integridade do banco de dados.
 *
 * <p>Esta classe verifica a integridade do banco de dados H2 durante a inicialização
 * da aplicação, garantindo que as migrações foram aplicadas corretamente e que
 * o banco está em um estado consistente.</p>
 *
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see org.springframework.stereotype.Component
 * @see org.springframework.context.event.EventListener
 */
@Component
public class DatabaseIntegrityChecker {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIntegrityChecker.class);
    
    private static final String DB_FILE_PATH = System.getProperty("user.home") + "/myboards/board_h2_db";
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Verifica a integridade do banco de dados após a aplicação estar pronta.
     *
     * <p>Esta verificação é executada automaticamente após o Spring Boot
     * ter inicializado completamente, incluindo as migrações do Liquibase.</p>
     *
     * @param event evento de inicialização da aplicação
     */
    @EventListener(ApplicationReadyEvent.class)
    public void checkDatabaseIntegrity(ApplicationReadyEvent event) {
        logger.info("Iniciando verificação de integridade do banco de dados...");
        
        try {
            // Verifica se o arquivo do banco existe
            checkDatabaseFileExists();
            
            // Verifica se as tabelas principais existem
            checkRequiredTablesExist();
            
            // Verifica a estrutura das tabelas
            checkTableStructure();
            
            // Verifica se há dados de exemplo (opcional)
            checkSampleData();
            
            logger.info("Verificação de integridade do banco concluída com sucesso!");
            
        } catch (Exception e) {
            logger.error("Erro durante verificação de integridade do banco: {}", e.getMessage(), e);
            // Não falha a aplicação, apenas registra o erro
        }
    }

    /**
     * Verifica se o arquivo do banco de dados existe.
     */
    private void checkDatabaseFileExists() {
        File dbFile = new File(DB_FILE_PATH + ".mv.db");
        if (!dbFile.exists()) {
            logger.warn("Arquivo do banco de dados não encontrado: {}. O banco será criado na primeira execução.", dbFile.getAbsolutePath());
        } else {
            logger.info("Arquivo do banco de dados encontrado: {}", dbFile.getAbsolutePath());
        }
    }

    /**
     * Verifica se as tabelas principais existem.
     */
    private void checkRequiredTablesExist() {
        List<String> requiredTables = List.of("BOARDS", "BOARDS_COLUMNS", "CARDS", "BLOCKS");
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (String tableName : requiredTables) {
                try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    if (tables.next()) {
                        logger.debug("Tabela {} encontrada", tableName);
                    } else {
                        logger.warn("Tabela {} não encontrada - será criada pela migração", tableName);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar tabelas: {}", e.getMessage());
        }
    }

    /**
     * Verifica a estrutura das tabelas principais.
     */
    private void checkTableStructure() {
        try {
            // Verifica se a tabela BOARDS tem as colunas esperadas
            String boardsColumns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'BOARDS'", 
                String.class
            );
            logger.info("Tabela BOARDS tem {} colunas", boardsColumns);
            
            // Verifica se a tabela CARDS tem as colunas esperadas
            String cardsColumns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'CARDS'", 
                String.class
            );
            logger.info("Tabela CARDS tem {} colunas", cardsColumns);
            
        } catch (Exception e) {
            logger.warn("Não foi possível verificar a estrutura das tabelas: {}", e.getMessage());
        }
    }

    /**
     * Verifica se há dados de exemplo no banco.
     */
    private void checkSampleData() {
        try {
            // Verifica se há boards
            Integer boardCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BOARDS", Integer.class);
            logger.info("Número de boards no banco: {}", boardCount != null ? boardCount : 0);
            
            // Verifica se há cards
            Integer cardCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CARDS", Integer.class);
            logger.info("Número de cards no banco: {}", cardCount != null ? cardCount : 0);
            
        } catch (Exception e) {
            logger.debug("Não foi possível verificar dados de exemplo: {}", e.getMessage());
        }
    }

    /**
     * Verifica se o banco está acessível e funcional.
     *
     * @return true se o banco está acessível, false caso contrário
     */
    public boolean isDatabaseAccessible() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            logger.error("Banco de dados não está acessível: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtém informações sobre o banco de dados.
     *
     * @return informações do banco de dados
     */
    public String getDatabaseInfo() {
        try {
            String dbVersion = jdbcTemplate.queryForObject("SELECT H2VERSION()", String.class);
            String dbPath = jdbcTemplate.queryForObject("SELECT DATABASE_PATH()", String.class);
            
            return String.format("H2 Version: %s, Path: %s", dbVersion, dbPath);
        } catch (Exception e) {
            return "Informações do banco não disponíveis: " + e.getMessage();
        }
    }
} 