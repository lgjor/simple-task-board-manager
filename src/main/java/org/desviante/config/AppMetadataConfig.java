package org.desviante.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

// Importações JavaFX para notificações
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;

/**
 * Configuração principal dos metadados da aplicação.
 * 
 * <p>Esta classe gerencia o carregamento, salvamento e monitoramento dos
 * metadados de configuração da aplicação. Os metadados são armazenados
 * em um arquivo JSON que pode ser modificado pelo usuário sem necessidade
 * de recompilação.</p>
 * 
 * <p>Principais funcionalidades:</p>
 * <ul>
 *   <li>Carregamento automático de metadados ao inicializar</li>
 *   <li>Monitoramento de alterações no arquivo de configuração</li>
 *   <li>Notificação quando reinicialização é necessária</li>
 *   <li>Validação de configurações carregadas</li>
 *   <li>Fallback para configurações padrão</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class AppMetadataConfig {
    
    private static final String METADATA_FILENAME = "app-metadata.json";
    private static final String DEFAULT_METADATA_FILENAME = "default-app-metadata.json";
    
    @Value("${app.metadata.directory:${user.home}/myboards/config}")
    private String metadataDirectoryPath;
    
    @Autowired
    private FileWatcherService fileWatcherService;
    
    private AppMetadata currentMetadata;
    private Path metadataFilePath;
    private Path defaultMetadataFilePath;
    private final ObjectMapper objectMapper;
    
    /**
     * Construtor que inicializa o ObjectMapper para JSON.
     */
    public AppMetadataConfig() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Inicializa a configuração de metadados.
     */
    @PostConstruct
    public void init() {
        try {
            initializeMetadataFiles();
            loadMetadata();
            startFileMonitoring();
            log.info("Configuração de metadados inicializada com sucesso");
        } catch (Exception e) {
            log.error("Erro ao inicializar configuração de metadados", e);
            // Usar configurações padrão em caso de erro
            useDefaultMetadata();
        }
    }
    
    /**
     * Inicializa os arquivos de metadados.
     */
    private void initializeMetadataFiles() throws IOException {
        // Cria o diretório de configuração se não existir
        Path configDir = Paths.get(metadataDirectoryPath);
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
            log.info("Diretório de configuração criado: {}", configDir);
        }
        
        this.metadataFilePath = configDir.resolve(METADATA_FILENAME);
        this.defaultMetadataFilePath = configDir.resolve(DEFAULT_METADATA_FILENAME);
        
        // Cria arquivo de metadados padrão se não existir
        if (!Files.exists(metadataFilePath)) {
            createDefaultMetadataFile();
        }
        
        // Cria arquivo de backup dos metadados padrão se não existir
        if (!Files.exists(defaultMetadataFilePath)) {
            createBackupDefaultMetadata();
        }
    }
    
    /**
     * Cria o arquivo de metadados padrão.
     */
    private void createDefaultMetadataFile() throws IOException {
        AppMetadata defaultMetadata = createDefaultMetadata();
        objectMapper.writeValue(metadataFilePath.toFile(), defaultMetadata);
        log.info("Arquivo de metadados padrão criado: {}", metadataFilePath);
    }
    
    /**
     * Cria backup dos metadados padrão.
     */
    private void createBackupDefaultMetadata() throws IOException {
        AppMetadata defaultMetadata = createDefaultMetadata();
        objectMapper.writeValue(defaultMetadataFilePath.toFile(), defaultMetadata);
        log.info("Backup de metadados padrão criado: {}", defaultMetadataFilePath);
    }
    
    /**
     * Cria metadados padrão com valores sensatos.
     */
    private AppMetadata createDefaultMetadata() {
        return AppMetadata.builder()
                .metadataVersion("1.0")
                .defaultCardTypeId(null) // Será definido pelo sistema
                .defaultProgressType(null) // Será definido pelo sistema
                .installationDirectory(System.getProperty("user.dir"))
                .userDataDirectory(System.getProperty("user.home") + "/myboards")
                .logDirectory(System.getProperty("user.home") + "/myboards/logs")
                .defaultLogLevel("INFO")
                .maxLogFileSizeMB(10)
                .maxLogFiles(5)
                .updateCheckIntervalHours(24)
                .autoCheckUpdates(true)
                .showSystemNotifications(true)
                .databaseTimeoutSeconds(30)
                .autoBackupDatabase(true)
                .autoBackupIntervalHours(24)
                .autoBackupDirectory(System.getProperty("user.home") + "/myboards/backups")
                .uiConfig(AppMetadata.UIConfig.builder()
                        .theme("system")
                        .language("pt-BR")
                        .fontSize(12)
                        .showTooltips(true)
                        .confirmDestructiveActions(true)
                        .showProgressBars(true)
                        .build())
                .performanceConfig(AppMetadata.PerformanceConfig.builder()
                        .maxCardsPerPage(100)
                        .enableCaching(true)
                        .maxCacheSizeMB(50)
                        .cacheTimeToLiveMinutes(30)
                        .build())
                .securityConfig(AppMetadata.SecurityConfig.builder()
                        .validateInput(true)
                        .logSensitiveOperations(false)
                        .maxSessionTimeMinutes(480)
                        .build())
                .build();
    }
    
    /**
     * Carrega os metadados do arquivo.
     */
    private void loadMetadata() {
        try {
            if (Files.exists(metadataFilePath)) {
                this.currentMetadata = objectMapper.readValue(metadataFilePath.toFile(), AppMetadata.class);
                log.info("Metadados carregados de: {}", metadataFilePath);
            } else {
                log.warn("Arquivo de metadados não encontrado, usando configurações padrão");
                useDefaultMetadata();
            }
        } catch (IOException e) {
            log.error("Erro ao carregar metadados, usando configurações padrão", e);
            useDefaultMetadata();
        }
    }
    
    /**
     * Usa metadados padrão em caso de erro.
     */
    private void useDefaultMetadata() {
        this.currentMetadata = createDefaultMetadata();
        log.info("Usando metadados padrão devido a erro no carregamento");
    }
    
    /**
     * Inicia o monitoramento do arquivo de metadados.
     */
    private void startFileMonitoring() {
        Path configDir = Paths.get(metadataDirectoryPath);
        fileWatcherService.startWatching(configDir, METADATA_FILENAME, this::handleMetadataFileChange);
        log.info("Monitoramento de metadados iniciado para: {}", metadataFilePath);
    }
    
    /**
     * Manipula alterações no arquivo de metadados.
     */
    private void handleMetadataFileChange(Path changedFile) {
        log.warn("Arquivo de metadados alterado: {}", changedFile);
        log.warn("⚠️  ALTERAÇÃO DETECTADA! A aplicação deve ser reiniciada para aplicar as mudanças.");
        
        // Verificar se já existe um alerta aberto para evitar múltiplas janelas
        if (isRestartAlertAlreadyOpen()) {
            log.info("Alerta de reinicialização já está aberto, ignorando nova notificação");
            return;
        }
        
        // Verificar se houve uma notificação recente para evitar spam
        if (isRecentNotification()) {
            log.info("Notificação recente detectada, ignorando para evitar spam");
            return;
        }
        
        // Recarrega os metadados
        try {
            loadMetadata();
            log.info("Metadados recarregados com sucesso");
        } catch (Exception e) {
            log.error("Erro ao recarregar metadados", e);
        }
        
        // Notificar o usuário sobre a necessidade de reiniciar
        notifyUserAboutRestart();
    }
    
    /**
     * Notifica o usuário sobre a necessidade de reiniciar.
     */
    private void notifyUserAboutRestart() {
        // Executa na thread da UI do JavaFX
        Platform.runLater(() -> {
            try {
                // Verificar se já existe um alerta aberto para evitar múltiplas janelas
                if (isRestartAlertAlreadyOpen()) {
                    log.info("Alerta de reinicialização já está aberto, ignorando nova notificação");
                    return;
                }
                
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Configuração Alterada");
                alert.setHeaderText("✅ Configurações Atualizadas com Sucesso!");
                alert.setContentText(
                    "As preferências foram salvas com sucesso!\n\n" +
                    "IMPORTANTE: Para visualizar as mudanças, reinicie a aplicação.\n\n" +
                    "• Novos cards criados usarão as novas configurações padrão\n" +
                    "• Cards existentes não serão afetados\n" +
                    "• Recomendamos reiniciar a aplicação agora"
                );
                
                // Botões personalizados
                ButtonType restartButton = new ButtonType("Reiniciar Agora", ButtonBar.ButtonData.OK_DONE);
                ButtonType laterButton = new ButtonType("Mais Tarde", ButtonBar.ButtonData.CANCEL_CLOSE);
                
                alert.getButtonTypes().setAll(restartButton, laterButton);
                
                // Marcar que o alerta está aberto
                setRestartAlertOpen(true);
                
                // Configurar ação do botão de reiniciar
                alert.setResultConverter(dialogButton -> {
                    if (dialogButton == restartButton) {
                        // Reiniciar a aplicação
                        restartApplication();
                    }
                    // Marcar que o alerta foi fechado
                    setRestartAlertOpen(false);
                    return null;
                });
                
                // Configurar ação quando o alerta for fechado
                alert.setOnCloseRequest(event -> {
                    setRestartAlertOpen(false);
                });
                
                // Mostrar o alerta
                alert.showAndWait();
                
            } catch (Exception e) {
                // Fallback para log se houver erro na UI
                log.error("Erro ao mostrar notificação de reinicialização", e);
                log.warn("NOTIFICAÇÃO: A aplicação deve ser reiniciada para aplicar as mudanças de configuração!");
                setRestartAlertOpen(false);
            }
        });
    }
    
    /**
     * Reinicia a aplicação de forma robusta.
     */
    private void restartApplication() {
        try {
            log.info("Iniciando processo de reinicialização da aplicação...");
            
            // Aguardar um pouco para garantir que a UI seja atualizada
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Aguardar 1 segundo
                    
                    // Executar na thread do JavaFX
                    Platform.runLater(() -> {
                        try {
                            log.info("Fechando aplicação para reinicialização...");
                            
                            // Tentar fechar todas as janelas abertas de forma ordenada e segura
                            try {
                                // Obter uma cópia da lista de janelas para evitar problemas de concorrência
                                javafx.stage.Window[] windows = javafx.stage.Window.getWindows().toArray(new javafx.stage.Window[0]);
                                
                                for (javafx.stage.Window window : windows) {
                                    if (window instanceof javafx.stage.Stage) {
                                        try {
                                            javafx.stage.Stage stage = (javafx.stage.Stage) window;
                                            if (stage.isShowing()) {
                                                log.info("Fechando janela: {}", stage.getTitle());
                                                stage.close();
                                            }
                                        } catch (Exception e) {
                                            log.warn("Erro ao fechar janela: {}", e.getMessage());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("Erro ao fechar janelas: {}", e.getMessage());
                            }
                            
                            // Aguardar um pouco mais e sair
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                    log.info("Saindo da aplicação...");
                                    Platform.exit();
                                    System.exit(0);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    log.warn("Thread de reinicialização interrompida");
                                    System.exit(1);
                                }
                            }).start();
                            
                        } catch (Exception e) {
                            log.error("Erro durante reinicialização: {}", e.getMessage());
                            // Fallback: sair diretamente
                            Platform.exit();
                            System.exit(0);
                        }
                    });
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Thread de reinicialização interrompida");
                    // Fallback: sair diretamente
                    Platform.runLater(() -> {
                        Platform.exit();
                        System.exit(0);
                    });
                }
            }).start();
            
        } catch (Exception e) {
            log.error("Erro crítico durante reinicialização: {}", e.getMessage());
            // Fallback final: sair diretamente
            Platform.exit();
            System.exit(0);
        }
    }
    
    // Flag para controlar se já existe um alerta de reinicialização aberto
    private static volatile boolean restartAlertOpen = false;
    
    // Controle de tempo para evitar notificações em sequência
    private static volatile long lastNotificationTime = 0;
    private static final long NOTIFICATION_COOLDOWN_MS = 5000; // 5 segundos
    
    /**
     * Verifica se já existe um alerta de reinicialização aberto.
     */
    private boolean isRestartAlertAlreadyOpen() {
        return restartAlertOpen;
    }
    
    /**
     * Define se o alerta de reinicialização está aberto.
     */
    private void setRestartAlertOpen(boolean open) {
        restartAlertOpen = open;
    }
    
    /**
     * Verifica se houve uma notificação recente para evitar spam.
     */
    private boolean isRecentNotification() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNotificationTime < NOTIFICATION_COOLDOWN_MS) {
            return true;
        }
        lastNotificationTime = currentTime;
        return false;
    }
    
    /**
     * Obtém os metadados atuais.
     * 
     * @return metadados atuais da aplicação
     */
    public AppMetadata getCurrentMetadata() {
        return currentMetadata;
    }
    
    /**
     * Obtém o ID do tipo de card padrão.
     * 
     * @return ID do tipo de card padrão ou null se não definido
     */
    public Optional<Long> getDefaultCardTypeId() {
        return Optional.ofNullable(currentMetadata.getDefaultCardTypeId());
    }
    
    /**
     * Obtém o tipo de progresso padrão.
     * 
     * @return tipo de progresso padrão ou null se não definido
     */
    public Optional<org.desviante.model.enums.ProgressType> getDefaultProgressType() {
        return Optional.ofNullable(currentMetadata.getDefaultProgressType());
    }
    
    /**
     * Obtém o diretório de instalação.
     * 
     * @return diretório de instalação
     */
    public String getInstallationDirectory() {
        return currentMetadata.getInstallationDirectory();
    }
    
    /**
     * Obtém o diretório de dados do usuário.
     * 
     * @return diretório de dados do usuário
     */
    public String getUserDataDirectory() {
        return currentMetadata.getUserDataDirectory();
    }
    
    /**
     * Obtém o diretório de logs.
     * 
     * @return diretório de logs
     */
    public String getLogDirectory() {
        return currentMetadata.getLogDirectory();
    }
    
    /**
     * Obtém o nível de logging padrão.
     * 
     * @return nível de logging padrão
     */
    public String getDefaultLogLevel() {
        return currentMetadata.getDefaultLogLevel();
    }
    
    /**
     * Obtém as configurações de interface.
     * 
     * @return configurações de interface
     */
    public AppMetadata.UIConfig getUIConfig() {
        return currentMetadata.getUiConfig();
    }
    
    /**
     * Obtém as configurações de performance.
     * 
     * @return configurações de performance
     */
    public AppMetadata.PerformanceConfig getPerformanceConfig() {
        return currentMetadata.getPerformanceConfig();
    }
    
    /**
     * Obtém as configurações de segurança.
     * 
     * @return configurações de segurança
     */
    public AppMetadata.SecurityConfig getSecurityConfig() {
        return currentMetadata.getSecurityConfig();
    }
    
    /**
     * Salva os metadados atuais no arquivo.
     * 
     * @throws IOException se houver erro ao salvar
     */
    public void saveMetadata() throws IOException {
        objectMapper.writeValue(metadataFilePath.toFile(), currentMetadata);
        log.info("Metadados salvos em: {}", metadataFilePath);
    }
    
    /**
     * Atualiza um valor específico nos metadados.
     * 
     * @param updater função para atualizar os metadados
     * @throws IOException se houver erro ao salvar
     */
    public void updateMetadata(MetadataUpdater updater) throws IOException {
        updater.update(currentMetadata);
        saveMetadata();
        log.info("Metadados atualizados e salvos");
    }
    
    /**
     * Interface funcional para atualizar metadados.
     */
    @FunctionalInterface
    public interface MetadataUpdater {
        void update(AppMetadata metadata);
    }
    
    /**
     * Obtém o caminho do arquivo de metadados.
     * 
     * @return caminho do arquivo de metadados
     */
    public Path getMetadataFilePath() {
        return metadataFilePath;
    }
    
    /**
     * Verifica se o arquivo de metadados existe.
     * 
     * @return true se existir, false caso contrário
     */
    public boolean metadataFileExists() {
        return Files.exists(metadataFilePath);
    }
}
