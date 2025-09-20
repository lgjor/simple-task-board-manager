package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Serviço para backup automático do banco de dados.
 * 
 * <p>Este serviço cria backups automáticos antes de executar migrações,
 * garantindo que os dados do usuário sejam preservados em caso de falha.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupService {

    private final JdbcTemplate jdbcTemplate;
    private static final String BACKUP_DIR = System.getProperty("user.home") + "/myboards/backups";

    /**
     * Cria um backup automático do banco de dados.
     * 
     * @param reason motivo do backup (ex: "before_migration", "manual")
     * @return caminho do arquivo de backup criado
     */
    public String createBackup(String reason) {
        try {
            // Criar diretório de backup se não existir
            Path backupDir = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
                log.info("Diretório de backup criado: {}", backupDir);
            }

            // Gerar nome do arquivo com timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("backup_%s_%s.sql", reason, timestamp);
            String backupPath = BACKUP_DIR + "/" + fileName;

            // Criar arquivo de backup
            try (FileWriter writer = new FileWriter(backupPath)) {
                writer.write("-- Backup automático criado em: " + LocalDateTime.now() + "\n");
                writer.write("-- Motivo: " + reason + "\n\n");

                // Backup da tabela boards
                backupTable(writer, "boards", "id, name, creation_date, group_id");

                // Backup da tabela board_columns
                backupTable(writer, "board_columns", "id, name, order_index, kind, board_id");

                // Backup da tabela cards
                backupTable(writer, "cards", 
                    "id, title, description, board_column_id, card_type_id, progress_type, " +
                    "total_units, current_units, creation_date, last_update_date, completion_date, order_index");

                // Backup da tabela card_types
                backupTable(writer, "card_types", "id, name, unit_label, creation_date, last_update_date");

                // Backup da tabela board_groups
                backupTable(writer, "board_groups", "id, name, description, color, icon, creation_date");

                writer.write("\n-- Backup concluído com sucesso\n");
            }

            log.info("Backup criado com sucesso: {}", backupPath);
            return backupPath;

        } catch (Exception e) {
            log.error("Erro ao criar backup: {}", e.getMessage());
            throw new RuntimeException("Falha ao criar backup", e);
        }
    }

    /**
     * Faz backup de uma tabela específica.
     * 
     * @param writer FileWriter para escrever o backup
     * @param tableName nome da tabela
     * @param columns colunas para backup (separadas por vírgula)
     */
    private void backupTable(FileWriter writer, String tableName, String columns) throws IOException {
        try {
            writer.write("-- Backup da tabela " + tableName + "\n");
            
            // Verificar se a tabela existe
            String checkTableSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
            Integer tableExists = jdbcTemplate.queryForObject(checkTableSql, Integer.class, tableName.toUpperCase());
            
            if (tableExists == null || tableExists == 0) {
                writer.write("-- Tabela " + tableName + " não existe\n\n");
                return;
            }

            // Obter dados da tabela
            String selectSql = "SELECT " + columns + " FROM " + tableName;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql);

            if (rows.isEmpty()) {
                writer.write("-- Tabela " + tableName + " está vazia\n\n");
                return;
            }

            // Gerar INSERT statements
            for (Map<String, Object> row : rows) {
                StringBuilder insertSql = new StringBuilder("INSERT INTO " + tableName + " (");
                StringBuilder values = new StringBuilder(" VALUES (");
                
                String[] columnArray = columns.split(", ");
                for (int i = 0; i < columnArray.length; i++) {
                    if (i > 0) {
                        insertSql.append(", ");
                        values.append(", ");
                    }
                    insertSql.append(columnArray[i]);
                    
                    Object value = row.get(columnArray[i].toUpperCase());
                    if (value == null) {
                        values.append("NULL");
                    } else if (value instanceof String) {
                        values.append("'").append(value.toString().replace("'", "''")).append("'");
                    } else if (value instanceof java.sql.Timestamp) {
                        values.append("'").append(value.toString()).append("'");
                    } else {
                        values.append(value.toString());
                    }
                }
                
                insertSql.append(")");
                values.append(");");
                
                writer.write(insertSql.toString() + values.toString() + "\n");
            }
            
            writer.write("\n");

        } catch (Exception e) {
            writer.write("-- Erro ao fazer backup da tabela " + tableName + ": " + e.getMessage() + "\n\n");
            log.warn("Erro ao fazer backup da tabela {}: {}", tableName, e.getMessage());
        }
    }

    /**
     * Lista backups disponíveis.
     * 
     * @return lista de arquivos de backup
     */
    public List<String> listBackups() {
        try {
            Path backupDir = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupDir)) {
                return List.of();
            }
            
            return Files.list(backupDir)
                .filter(path -> path.toString().endsWith(".sql"))
                .map(path -> path.getFileName().toString())
                .sorted()
                .toList();
                
        } catch (Exception e) {
            log.error("Erro ao listar backups: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Remove backups antigos, mantendo apenas os mais recentes.
     * 
     * @param keepCount número de backups para manter
     */
    public void cleanupOldBackups(int keepCount) {
        try {
            List<String> backups = listBackups();
            if (backups.size() <= keepCount) {
                return;
            }
            
            int toDelete = backups.size() - keepCount;
            for (int i = 0; i < toDelete; i++) {
                String backupFile = BACKUP_DIR + "/" + backups.get(i);
                Files.deleteIfExists(Paths.get(backupFile));
                log.info("Backup antigo removido: {}", backupFile);
            }
            
        } catch (Exception e) {
            log.error("Erro ao limpar backups antigos: {}", e.getMessage());
        }
    }

    /**
     * Cria backup antes de uma migração.
     * 
     * @return caminho do arquivo de backup
     */
    public String createPreMigrationBackup() {
        log.info("Criando backup antes da migração...");
        String backupPath = createBackup("before_migration");
        
        // Limpar backups antigos, mantendo apenas os 5 mais recentes
        cleanupOldBackups(5);
        
        return backupPath;
    }
}
