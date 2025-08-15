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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;

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
     * Construtor que inicializa a configuração de metadados.
     */
    public AppMetadataConfig() {
        // ⭐ NOVO: Logs para verificar diretório de trabalho da aplicação reiniciada
        log.info("🔄 CONSTRUTOR AppMetadataConfig() CHAMADO!");
        log.info("🔄 Diretório de trabalho atual: {}", System.getProperty("user.dir"));
        log.info("🔄 Diretório home do usuário: {}", System.getProperty("user.home"));
        log.info("🔄 Diretório temporário: {}", System.getProperty("java.io.tmpdir"));
        
        // Inicializar ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        log.info("✅ Construtor AppMetadataConfig() concluído com sucesso");
    }
    
    /**
     * Inicializa a configuração de metadados.
     */
    @PostConstruct
    public void init() {
        try {
            log.info("🔄 MÉTODO init() CHAMADO!");
            log.info("🔄 metadataDirectoryPath: {}", metadataDirectoryPath);
            
            // ⭐ CORREÇÃO: Verificar se metadataDirectoryPath foi injetado
            if (metadataDirectoryPath == null || metadataDirectoryPath.isEmpty()) {
                log.warn("⚠️ metadataDirectoryPath não foi injetado, usando valor padrão");
                metadataDirectoryPath = System.getProperty("user.home") + "/myboards/config";
            }
            
            log.info("🔄 Diretório de configuração final: {}", metadataDirectoryPath);
            
            initializeMetadataFiles();
            loadMetadata();
            startFileMonitoring();
            log.info("✅ Configuração de metadados inicializada com sucesso");
        } catch (Exception e) {
            log.error("❌ Erro ao inicializar configuração de metadados", e);
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
                .defaultCardTypeId(1L) // Tipo "Card" como padrão
                .defaultProgressType(org.desviante.model.enums.ProgressType.PERCENTAGE) // Progresso percentual como padrão
                .defaultBoardGroupId(null) // Sem grupo padrão - usuário deve configurar explicitamente
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
     * Carrega os metadados da aplicação do arquivo JSON.
     */
    private void loadMetadata() {
        log.info("🔄 Iniciando carregamento de metadados...");
        log.info("📁 Caminho do arquivo: {}", metadataFilePath);
        log.info("📁 Arquivo existe: {}", Files.exists(metadataFilePath));
        
        if (!Files.exists(metadataFilePath)) {
            log.warn("❌ Arquivo de metadados não encontrado, usando configurações padrão");
            useDefaultMetadata();
            return;
        }
        
        try {
            long fileSize = Files.size(metadataFilePath);
            log.info("📏 Tamanho do arquivo: {} bytes", fileSize);
            
            if (fileSize == 0) {
                log.warn("❌ Arquivo de metadados está vazio, usando configurações padrão");
                useDefaultMetadata();
                return;
            }
            
            log.info("📖 Tentando ler arquivo de metadados...");
            String content = Files.readString(metadataFilePath);
            log.info("📄 Conteúdo do arquivo lido: {} caracteres", content.length());
            log.info("📄 Primeiros 200 caracteres: {}", content.substring(0, Math.min(200, content.length())));
            
            AppMetadata loadedMetadata = objectMapper.readValue(content, AppMetadata.class);
            log.info("✅ Metadados carregados com sucesso de: {}", metadataFilePath);
            
            if (loadedMetadata != null) {
                this.currentMetadata = loadedMetadata;
                log.info("📊 Dados carregados:");
                log.info("   - Versão: {}", this.currentMetadata.getMetadataVersion());
                log.info("   - defaultCardTypeId: {}", this.currentMetadata.getDefaultCardTypeId());
                log.info("   - defaultProgressType: {}", this.currentMetadata.getDefaultProgressType());
                log.info("   - defaultBoardGroupId: {}", this.currentMetadata.getDefaultBoardGroupId());
                
                // ⭐ NOVO: Validação adicional dos dados carregados
                if (this.currentMetadata.getDefaultBoardGroupId() == null) {
                    log.warn("⚠️ ATENÇÃO: defaultBoardGroupId é null após carregamento!");
                    log.warn("⚠️ Isso pode indicar um problema no arquivo ou na deserialização");
                } else {
                    log.info("✅ defaultBoardGroupId carregado corretamente: {}", this.currentMetadata.getDefaultBoardGroupId());
                }
            } else {
                log.warn("⚠️ Metadados carregados são null, usando configurações padrão");
                useDefaultMetadata();
                return;
            }
            
            // Validação dos metadados carregados
            if (this.currentMetadata.getMetadataVersion() == null || this.currentMetadata.getMetadataVersion().isEmpty()) {
                log.warn("❌ Versão dos metadados inválida, usando configurações padrão");
                useDefaultMetadata();
                return;
            }
            
            log.info("✅ Validação de metadados concluída com sucesso");
            
        } catch (IOException e) {
            log.error("❌ Erro ao ler arquivo de metadados: {}", e.getMessage());
            log.error("❌ Stack trace completo:", e);
            
            // Tentar restaurar do backup
            Path backupPath = metadataFilePath.resolveSibling(metadataFilePath.getFileName() + ".backup");
            if (Files.exists(backupPath)) {
                log.info("🔄 Tentando restaurar do backup: {}", backupPath);
                try {
                    log.info("📖 Tentando restaurar do backup...");
                    String backupContent = Files.readString(backupPath);
                    AppMetadata backupMetadata = objectMapper.readValue(backupContent, AppMetadata.class);
                    this.currentMetadata = backupMetadata;
                    log.info("✅ Backup restaurado com sucesso");
                    return;
                } catch (Exception backupException) {
                    log.error("❌ Erro ao restaurar backup: {}", backupException.getMessage());
                }
            } else {
                log.warn("⚠️ Arquivo de backup não encontrado: {}", backupPath);
            }
            
            log.warn("🔄 Usando metadados padrão devido a falha no carregamento");
            useDefaultMetadata();
            
        } catch (Exception e) {
            log.error("❌ Erro inesperado ao carregar metadados: {}", e.getMessage());
            log.error("❌ Stack trace completo:", e);
            log.warn("🔄 Usando metadados padrão devido a erro inesperado");
            useDefaultMetadata();
        }
    }
    
    /**
     * Cria e aplica metadados padrão quando não é possível carregar do arquivo.
     */
    private void useDefaultMetadata() {
        log.warn("🔄 MÉTODO useDefaultMetadata() CHAMADO!");
        log.warn("🔄 Stack trace da chamada:");
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().contains("org.desviante")) {
                log.warn("   - {}:{}({})", element.getClassName(), element.getMethodName(), element.getLineNumber());
            }
        }
        
        // ⭐ NOVO: Log adicional para identificar o contexto
        log.warn("🔄 Contexto da chamada:");
        log.warn("   - Thread: {}", Thread.currentThread().getName());
        log.warn("   - Stack trace completo:");
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            log.warn("     {}:{}({})", element.getClassName(), element.getMethodName(), element.getLineNumber());
        }
        
        this.currentMetadata = createDefaultMetadata();
        log.info("✅ Metadados padrão criados e aplicados");
        log.info("📊 Dados padrão aplicados:");
        log.info("   - defaultCardTypeId: {}", this.currentMetadata.getDefaultCardTypeId());
        log.info("   - defaultProgressType: {}", this.currentMetadata.getDefaultProgressType());
        log.info("   - defaultBoardGroupId: {}", this.currentMetadata.getDefaultBoardGroupId());
        
        // ⭐ NOVO: Log adicional para confirmar que defaultBoardGroupId é null
        if (this.currentMetadata.getDefaultBoardGroupId() == null) {
            log.warn("⚠️ CONFIRMADO: defaultBoardGroupId definido como null nos metadados padrão");
            log.warn("⚠️ Este é o motivo pelo qual o sistema sugere 'Sem Grupo'");
        }
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
        
        // Aguardar um pouco para evitar conflitos com operações de salvamento
        try {
            Thread.sleep(100); // Aguardar 100ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        // Verificar se o arquivo ainda existe e não está vazio
        try {
            if (!Files.exists(metadataFilePath) || Files.size(metadataFilePath) == 0) {
                log.warn("Arquivo de metadados não existe ou está vazio após alteração, aguardando...");
                return;
            }
        } catch (IOException e) {
            log.warn("Erro ao verificar tamanho do arquivo de metadados: {}, aguardando...", e.getMessage());
            return;
        }
        
        log.info("🔄 ALTERAÇÃO DETECTADA! Recarregando configurações em tempo real...");
        
        // ⭐ NOVA ESTRATÉGIA: Recarregar configurações sem reiniciar
        try {
            // Recarregar metadados do arquivo
            loadMetadata();
            
            log.info("✅ Configurações atualizadas com sucesso em tempo real!");
            log.info("📊 Novos valores carregados:");
            log.info("   - defaultCardTypeId: {}", this.currentMetadata.getDefaultCardTypeId());
            log.info("   - defaultProgressType: {}", this.currentMetadata.getDefaultProgressType());
            log.info("   - defaultBoardGroupId: {}", this.currentMetadata.getDefaultBoardGroupId());
            
            // Mostrar notificação de sucesso para o usuário
            showSuccessNotification();
            
        } catch (Exception e) {
            log.error("❌ Erro ao recarregar configurações: {}", e.getMessage());
            
            // Mostrar notificação de erro para o usuário
            showErrorNotification();
        }
    }
    
    /**
     * Mostra notificação de sucesso para o usuário.
     */
    private void showSuccessNotification() {
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("✅ Configurações Atualizadas");
                alert.setHeaderText("Configurações Atualizadas com Sucesso!");
                alert.setContentText(
                    "As preferências foram atualizadas em tempo real!\n\n" +
                    "• Novos cards e boards usarão as novas configurações padrão\n" +
                    "• Não é necessário reiniciar a aplicação\n" +
                    "• Todas as mudanças estão ativas agora"
                );
                
                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(okButton);
                
                alert.showAndWait();
                
            } catch (Exception e) {
                log.error("❌ Erro ao mostrar notificação de sucesso: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Mostra notificação de erro para o usuário.
     */
    private void showErrorNotification() {
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("❌ Erro na Atualização");
                alert.setHeaderText("Erro ao Atualizar Configurações");
                alert.setContentText(
                    "Ocorreu um erro ao atualizar as configurações.\n\n" +
                    "• As configurações antigas continuam ativas\n" +
                    "• Tente salvar novamente ou reiniciar a aplicação\n" +
                    "• Verifique os logs para mais detalhes"
                );
                
                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(okButton);
                
                alert.showAndWait();
                
            } catch (Exception e) {
                log.error("❌ Erro ao mostrar notificação de erro: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Notifica o usuário sobre a necessidade de reiniciar.
     */
    private void notifyUserAboutRestart() {
        // ⭐ NOVA ESTRATÉGIA: Não é mais necessário reiniciar!
        // As configurações são atualizadas em tempo real
        log.info("✅ Configurações atualizadas com sucesso");
        
        // Executa na thread da UI do JavaFX
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("✅ Configurações Atualizadas");
                alert.setHeaderText("Configurações Atualizadas com Sucesso!");
                alert.setContentText(
                    "As preferências foram salvas e aplicadas com sucesso!\n\n" +
                    "✅ NÃO é necessário reiniciar a aplicação!\n\n" +
                    "• Novos cards e boards usarão as novas configurações padrão\n" +
                    "• Todas as mudanças estão ativas agora\n" +
                    "• Continue usando a aplicação normalmente"
                );
                
                // Botão simples de OK
                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(okButton);
                
                // Mostrar o alerta
                alert.showAndWait();
                
            } catch (Exception e) {
                // Fallback para log se houver erro na UI
                log.error("Erro ao mostrar notificação de sucesso", e);
                log.info("✅ CONFIGURAÇÕES ATUALIZADAS EM TEMPO REAL - NÃO É NECESSÁRIO REINICIAR!");
            }
        });
    }
    
    /**
     * Reinicia a aplicação de forma robusta e cross-platform.
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
                            
                            // Aguardar um pouco mais e tentar reiniciar
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                    log.info("Tentando reiniciar a aplicação...");
                                    
                                    // Tentar reiniciar usando o mecanismo do sistema operacional
                                    if (restartUsingSystemCommand()) {
                                        log.info("Comando de reinicialização executado com sucesso");
                                        
                                        // Verificar se a reinicialização foi bem-sucedida
                                        if (verifyRestartSuccess()) {
                                            log.info("Reinicialização confirmada com sucesso");
                                        } else {
                                            log.warn("Reinicialização pode ter falhado - verificando novamente...");
                                            // Aguardar mais um pouco e verificar novamente
                                            Thread.sleep(3000);
                                            if (!verifyRestartSuccess()) {
                                                log.error("Falha na reinicialização - aplicação não foi iniciada");
                                            }
                                        }
                                    } else {
                                        log.warn("Falha ao executar comando de reinicialização, saindo da aplicação");
                                        Platform.exit();
                                        System.exit(0);
                                    }
                                    
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    log.warn("Thread de reinicialização interrompida");
                                    Platform.exit();
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
    
    /**
     * Tenta reiniciar a aplicação usando comandos do sistema operacional.
     * 
     * @return true se o comando foi executado com sucesso, false caso contrário
     */
    private boolean restartUsingSystemCommand() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String javaHome = System.getProperty("java.home");
            String classpath = System.getProperty("java.class.path");
            String mainClass = "org.desviante.SimpleTaskBoardManagerApplication";
            
            // Detectar se estamos rodando como uma aplicação instalada
            String appPath = detectInstalledApplicationPath();
            
            if (appPath != null) {
                // Se encontramos o caminho da aplicação instalada, usar ela
                log.info("Aplicação instalada detectada em: {}", appPath);
                return restartUsingInstalledApplication(osName, appPath);
            } else {
                // Caso contrário, tentar reiniciar usando Java diretamente
                log.info("Aplicação instalada não detectada, tentando reiniciar via Java");
                return restartUsingJavaCommand(osName, javaHome, classpath, mainClass);
            }
            
        } catch (Exception e) {
            log.error("Erro ao executar comando de reinicialização: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Detecta o caminho da aplicação instalada no sistema.
     * 
     * @return caminho da aplicação instalada ou null se não encontrada
     */
    private String detectInstalledApplicationPath() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String appName = "SimpleTaskBoardManager";
            
            if (osName.contains("win")) {
                // Windows: verificar locais padrão de instalação
                String[] possiblePaths = {
                    System.getenv("PROGRAMFILES") + "\\" + appName + "\\" + appName + ".exe",
                    System.getenv("PROGRAMFILES(X86)") + "\\" + appName + "\\" + appName + ".exe",
                    System.getProperty("user.home") + "\\AppData\\Local\\" + appName + "\\" + appName + ".exe"
                };
                
                for (String path : possiblePaths) {
                    if (path != null && new File(path).exists()) {
                        return path;
                    }
                }
                
            } else if (osName.contains("linux")) {
                // Linux: verificar locais padrão
                String[] possiblePaths = {
                    "/usr/bin/" + appName,
                    "/usr/local/bin/" + appName,
                    System.getProperty("user.home") + "/.local/bin/" + appName,
                    "/opt/" + appName + "/bin/" + appName
                };
                
                for (String path : possiblePaths) {
                    if (new File(path).exists()) {
                        return path;
                    }
                }
                
            } else if (osName.contains("mac")) {
                // macOS: verificar locais padrão
                String[] possiblePaths = {
                    "/Applications/" + appName + ".app/Contents/MacOS/" + appName,
                    System.getProperty("user.home") + "/Applications/" + appName + ".app/Contents/MacOS/" + appName
                };
                
                for (String path : possiblePaths) {
                    if (new File(path).exists()) {
                        return path;
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("Erro ao detectar aplicação instalada: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Reinicia a aplicação usando o executável instalado.
     * 
     * @param osName nome do sistema operacional
     * @param appPath caminho da aplicação instalada
     * @return true se o comando foi executado com sucesso
     */
    private boolean restartUsingInstalledApplication(String osName, String appPath) {
        try {
            List<String> command = new ArrayList<>();
            
            if (osName.contains("win")) {
                // ⭐ CORREÇÃO: Comando mais robusto para Windows
                // Windows: usar start com diretório de trabalho correto e aguardar
                command.add("cmd");
                command.add("/c");
                command.add("cd");
                command.add("/d");
                command.add(System.getProperty("user.dir")); // ⭐ Usar diretório atual
                command.add("&&");
                command.add("start");
                command.add("/wait"); // ⭐ Aguardar processo iniciar
                command.add("\"SimpleTaskBoardManager\"");
                command.add("\"" + appPath + "\"");
                
            } else if (osName.contains("linux") || osName.contains("mac")) {
                // Linux/Mac: executar diretamente com diretório correto
                command.add(appPath);
                
            } else {
                log.warn("Sistema operacional não suportado: {}", osName);
                return false;
            }
            
            log.info("Comando de reinicialização via aplicação instalada: {}", String.join(" ", command));
            
            // ⭐ NOVO: Log adicional para verificar diretório de trabalho
            log.info("🔄 Diretório de trabalho atual: {}", System.getProperty("user.dir"));
            log.info("🔄 Diretório de trabalho da aplicação instalada: {}", new File(appPath).getParent());
            log.info("🔄 Caminho do arquivo de configuração: {}", metadataFilePath);
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            
            // ⭐ CORREÇÃO: Sempre usar o diretório atual da aplicação
            processBuilder.directory(new File(System.getProperty("user.dir")));
            
            // ⭐ CORREÇÃO: Configurar variáveis de ambiente para garantir compatibilidade
            Map<String, String> env = processBuilder.environment();
            env.put("JAVA_HOME", System.getProperty("java.home"));
            env.put("PATH", System.getenv("PATH"));
            
            // ⭐ NOVO: Configurar variáveis de ambiente específicas para a aplicação reiniciada
            env.put("APP_CONFIG_DIR", System.getProperty("user.home") + "/myboards/config");
            env.put("APP_WORKING_DIR", System.getProperty("user.dir"));
            
            log.info("🔄 Variáveis de ambiente configuradas:");
            log.info("   - JAVA_HOME: {}", env.get("JAVA_HOME"));
            log.info("   - APP_CONFIG_DIR: {}", env.get("APP_CONFIG_DIR"));
            log.info("   - APP_WORKING_DIR: {}", env.get("APP_WORKING_DIR"));
            
            Process process = processBuilder.start();
            
            // ⭐ CORREÇÃO: Aguardar mais tempo para verificar se o processo foi iniciado
            Thread.sleep(3000); // Aguardar 3 segundos
            
            if (process.isAlive()) {
                log.info("Processo de reinicialização iniciado com PID: {}", process.pid());
                return true;
            } else {
                int exitCode = process.exitValue();
                log.warn("Processo de reinicialização falhou com código de saída: {}", exitCode);
                
                // ⭐ CORREÇÃO: Tentar método alternativo se o primeiro falhar
                log.info("Tentando método alternativo de reinicialização...");
                return restartUsingAlternativeMethod(osName, appPath);
            }
            
        } catch (Exception e) {
            log.error("Erro ao reiniciar via aplicação instalada: {}", e.getMessage());
            
            // ⭐ CORREÇÃO: Tentar método alternativo em caso de erro
            log.info("Tentando método alternativo de reinicialização...");
            return restartUsingAlternativeMethod(osName, appPath);
        }
    }
    
    /**
     * Método alternativo de reinicialização usando Java diretamente.
     * 
     * @param osName nome do sistema operacional
     * @param appPath caminho da aplicação instalada
     * @return true se o comando foi executado com sucesso
     */
    private boolean restartUsingAlternativeMethod(String osName, String appPath) {
        try {
            log.info("Usando método alternativo de reinicialização via Java...");
            
            String javaHome = System.getProperty("java.home");
            String classpath = System.getProperty("java.class.path");
            String mainClass = "org.desviante.SimpleTaskBoardManagerApplication";
            
            return restartUsingJavaCommand(osName, javaHome, classpath, mainClass);
            
        } catch (Exception e) {
            log.error("Erro no método alternativo de reinicialização: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica se a reinicialização foi bem-sucedida aguardando um tempo
     * e verificando se novos processos foram criados.
     * 
     * @return true se a reinicialização parece ter sido bem-sucedida
     */
    private boolean verifyRestartSuccess() {
        try {
            // Aguardar um pouco mais para que a nova instância seja iniciada
            Thread.sleep(2000);
            
            // Verificar se há processos Java rodando com nossa classe principal
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("win")) {
                return checkWindowsProcesses();
            } else {
                return checkUnixProcesses();
            }
            
        } catch (Exception e) {
            log.warn("Erro ao verificar sucesso da reinicialização: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica processos no Windows para confirmar reinicialização.
     */
    private boolean checkWindowsProcesses() {
        try {
            ProcessBuilder pb = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq java.exe");
            Process process = pb.start();
            
            // Aguardar o comando terminar
            process.waitFor();
            
            // Ler a saída
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                int javaProcesses = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("java.exe")) {
                        javaProcesses++;
                    }
                }
                
                // Se há pelo menos um processo Java rodando, consideramos sucesso
                return javaProcesses > 0;
            }
            
        } catch (Exception e) {
            log.warn("Erro ao verificar processos Windows: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica processos no Unix/Linux para confirmar reinicialização.
     */
    private boolean checkUnixProcesses() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ps", "aux");
            Process process = pb.start();
            
            // Aguardar o comando terminar
            process.waitFor();
            
            // Ler a saída
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                int javaProcesses = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("java") && 
                        line.toLowerCase().contains("simpletaskboardmanager")) {
                        javaProcesses++;
                    }
                }
                
                // Se há pelo menos um processo Java rodando, consideramos sucesso
                return javaProcesses > 0;
            }
            
        } catch (Exception e) {
            log.warn("Erro ao verificar processos Unix: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Reinicia a aplicação usando o comando Java diretamente.
     * 
     * @param osName nome do sistema operacional
     * @param javaHome caminho do Java
     * @param classpath classpath da aplicação
     * @param mainClass classe principal
     * @return true se o comando foi executado com sucesso
     */
    private boolean restartUsingJavaCommand(String osName, String javaHome, String classpath, String mainClass) {
        try {
            List<String> command = new ArrayList<>();
            
            if (osName.contains("win")) {
                // ⭐ CORREÇÃO: Garantir que use o diretório de configuração correto
                // Windows: usar cmd /c com diretório de trabalho correto
                command.add("cmd");
                command.add("/c");
                command.add("cd");
                command.add("/d");
                command.add(System.getProperty("user.dir")); // ⭐ Usar diretório atual
                command.add("&&");
                command.add("start");
                command.add("\"SimpleTaskBoardManager\"");
                command.add("\"" + javaHome + "\\bin\\java.exe\"");
                command.add("-cp");
                command.add(classpath);
                command.add(mainClass);
                
            } else if (osName.contains("linux") || osName.contains("mac")) {
                // Linux/Mac: usar bash com diretório correto
                command.add("bash");
                command.add("-c");
                
                StringBuilder bashCommand = new StringBuilder();
                bashCommand.append("\"cd ");
                bashCommand.append(System.getProperty("user.dir")); // ⭐ Usar diretório atual
                bashCommand.append(" && ");
                bashCommand.append(javaHome).append("/bin/java");
                bashCommand.append(" -cp ").append(classpath);
                bashCommand.append(" ").append(mainClass);
                bashCommand.append(" &\"");
                
                command.add(bashCommand.toString());
                
            } else {
                log.warn("Sistema operacional não suportado para reinicialização: {}", osName);
                return false;
            }
            
            log.info("Comando Java de reinicialização: {}", String.join(" ", command));
            
            // Executar o comando
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            
            // Configurar diretório de trabalho
            String workingDir = System.getProperty("user.dir");
            if (workingDir != null) {
                processBuilder.directory(new File(workingDir));
            }
            
            // Configurar variáveis de ambiente
            Map<String, String> env = processBuilder.environment();
            env.put("JAVA_HOME", javaHome);
            
            // Executar o processo
            Process process = processBuilder.start();
            
            // Aguardar um pouco para verificar se o processo foi iniciado
            Thread.sleep(1000);
            
            if (process.isAlive()) {
                log.info("Processo de reinicialização iniciado com PID: {}", process.pid());
                return true;
            } else {
                int exitCode = process.exitValue();
                log.warn("Processo de reinicialização falhou com código de saída: {}", exitCode);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Erro ao executar comando Java de reinicialização: {}", e.getMessage());
            return false;
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
     * Obtém o ID do grupo de quadro padrão.
     * 
     * @return ID do grupo de quadro padrão ou null se não definido
     */
    public Optional<Long> getDefaultBoardGroupId() {
        return Optional.ofNullable(currentMetadata.getDefaultBoardGroupId());
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
        // Criar backup do arquivo atual se existir
        if (Files.exists(metadataFilePath)) {
            Path backupPath = metadataFilePath.resolveSibling(metadataFilePath.getFileName() + ".backup");
            try {
                Files.copy(metadataFilePath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.debug("Backup criado: {}", backupPath);
            } catch (IOException e) {
                log.warn("Não foi possível criar backup: {}", e.getMessage());
            }
        }
        
        // Salvar em arquivo temporário primeiro
        Path tempFile = metadataFilePath.resolveSibling(metadataFilePath.getFileName() + ".tmp");
        try {
            // Salvar no arquivo temporário
            objectMapper.writeValue(tempFile.toFile(), currentMetadata);
            log.debug("Metadados salvos em arquivo temporário: {}", tempFile);
            
            // Mover arquivo temporário para localização final
            Files.move(tempFile, metadataFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("Metadados salvos com sucesso em: {}", metadataFilePath);
            
            // Verificar se o arquivo foi salvo corretamente
            try {
                if (!Files.exists(metadataFilePath) || Files.size(metadataFilePath) == 0) {
                    throw new IOException("Arquivo salvo está vazio ou não existe");
                }
            } catch (IOException e) {
                throw new IOException("Erro ao verificar arquivo salvo: " + e.getMessage(), e);
            }
            
            // Verificar se o arquivo pode ser lido novamente
            try {
                AppMetadata testRead = objectMapper.readValue(metadataFilePath.toFile(), AppMetadata.class);
                log.debug("Verificação de leitura bem-sucedida - arquivo válido");
            } catch (Exception e) {
                throw new IOException("Arquivo salvo não pode ser lido: " + e.getMessage());
            }
            
        } catch (Exception e) {
            // Se algo deu errado, tentar restaurar do backup
            if (Files.exists(metadataFilePath.resolveSibling(metadataFilePath.getFileName() + ".backup"))) {
                try {
                    Path backupPath = metadataFilePath.resolveSibling(metadataFilePath.getFileName() + ".backup");
                    Files.copy(backupPath, metadataFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    log.warn("Arquivo restaurado do backup devido a erro no salvamento");
                } catch (IOException restoreError) {
                    log.error("Falha ao restaurar arquivo do backup: {}", restoreError.getMessage());
                }
            }
            
            // Limpar arquivo temporário se existir
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException deleteError) {
                log.warn("Não foi possível deletar arquivo temporário: {}", deleteError.getMessage());
            }
            
            throw new IOException("Erro ao salvar metadados: " + e.getMessage(), e);
        }
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
