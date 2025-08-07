package org.desviante.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Serviço responsável por migrações automáticas do banco de dados.
 * 
 * <p>Este serviço verifica se o banco de dados precisa de atualizações
 * e executa scripts de migração automaticamente para preservar dados
 * existentes durante atualizações do sistema.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Service
public class DatabaseMigrationService {

    private static final Logger logger = Logger.getLogger(DatabaseMigrationService.class.getName());
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Executa migrações automáticas após a inicialização do serviço.
     * Verifica se há mudanças necessárias no banco e as aplica automaticamente.
     */
    @PostConstruct
    public void executeMigrations() {
        try {
            logger.info("Iniciando verificação de migrações do banco de dados...");
            
            // Temporariamente desabilitado para usar apenas schema.sql
            logger.info("Migrações desabilitadas temporariamente - usando apenas schema.sql");
            
        } catch (Exception e) {
            logger.severe("Erro durante a migração do banco de dados: " + e.getMessage());
            // Não re-lança a exceção para não impedir a inicialização da aplicação
        }
    }

    /**
     * Verifica se o banco de dados precisa de migração.
     * 
     * @return true se a migração é necessária, false caso contrário
     */
    private boolean needsMigration() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Verifica se todas as tabelas obrigatórias existem
            String[] requiredTables = {
                "BOARDS", "BOARD_COLUMNS", "CARDS", "TASKS", 
                "BOARD_GROUPS", "CARD_TYPES"
            };
            
            for (String tableName : requiredTables) {
                try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    if (!tables.next()) {
                        logger.info("Tabela obrigatória não encontrada: " + tableName);
                        return true;
                    }
                }
            }
            
            // Verifica se as colunas obrigatórias existem
            if (!columnExists("BOARDS", "GROUP_ID")) {
                logger.info("Coluna GROUP_ID não encontrada na tabela BOARDS");
                return true;
            }
            
            if (!columnExists("CARD_TYPES", "LAST_UPDATE_DATE")) {
                logger.info("Coluna LAST_UPDATE_DATE não encontrada na tabela CARD_TYPES");
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.warning("Erro ao verificar necessidade de migração: " + e.getMessage());
            return true; // Em caso de erro, assume que precisa de migração
        }
    }

    /**
     * Verifica se uma coluna específica existe em uma tabela.
     * 
     * @param tableName nome da tabela
     * @param columnName nome da coluna
     * @return true se a coluna existe, false caso contrário
     */
    private boolean columnExists(String tableName, String columnName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
                return columns.next();
            }
        } catch (SQLException e) {
            logger.warning("Erro ao verificar coluna " + columnName + " na tabela " + tableName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Executa o script de migração.
     */
    private void executeMigrationScript() {
        try (Connection connection = dataSource.getConnection()) {
            // Desabilita auto-commit para controlar a transação
            connection.setAutoCommit(false);
            
            try {
                // Executa o script de migração
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("migration.sql"));
                
                // Commit da transação
                connection.commit();
                logger.info("Script de migração executado com sucesso");
                
            } catch (Exception e) {
                // Rollback em caso de erro
                connection.rollback();
                logger.severe("Erro durante execução do script de migração: " + e.getMessage());
                throw e;
            }
            
        } catch (Exception e) {
            logger.severe("Erro ao executar migração: " + e.getMessage());
            throw new RuntimeException("Falha na migração do banco de dados", e);
        }
    }

    /**
     * Executa uma consulta SQL personalizada para verificação.
     * 
     * @param sql consulta SQL a ser executada
     * @return resultado da consulta
     */
    public Object executeQuery(String sql) {
        return jdbcTemplate.queryForObject(sql, Object.class);
    }

    /**
     * Verifica a integridade do banco de dados após migração.
     * 
     * @return true se o banco está íntegro, false caso contrário
     */
    public boolean verifyDatabaseIntegrity() {
        try {
            // Verifica se todas as tabelas existem
            String[] requiredTables = {
                "BOARDS", "BOARD_COLUMNS", "CARDS", "TASKS", 
                "BOARD_GROUPS", "CARD_TYPES"
            };
            
            for (String tableName : requiredTables) {
                String sql = "SELECT COUNT(*) FROM " + tableName;
                jdbcTemplate.queryForObject(sql, Integer.class);
            }
            
            logger.info("Verificação de integridade do banco concluída com sucesso");
            return true;
            
        } catch (Exception e) {
            logger.severe("Erro na verificação de integridade: " + e.getMessage());
            return false;
        }
    }
} 