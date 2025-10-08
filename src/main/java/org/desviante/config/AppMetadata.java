package org.desviante.config;

import lombok.*;
import org.desviante.model.enums.ProgressType;

/**
 * Metadados de configuração da aplicação.
 * 
 * <p>Esta classe encapsula todas as configurações dinâmicas da aplicação,
 * permitindo ajustes sem necessidade de recompilação. Os metadados são
 * carregados de um arquivo JSON e podem ser modificados pelo usuário.</p>
 * 
 * <p>As configurações incluem preferências de interface, tipos padrão para
 * criação de cards, diretórios de instalação e outras opções personalizáveis.</p>
 * 
 * <p>Esta classe utiliza o padrão Builder para facilitar a criação de instâncias
 * com diferentes configurações. Todos os métodos do Builder herdam a documentação
 * dos campos correspondentes.</p>
 * 
 * <p><strong>Uso do Builder:</strong></p>
 * <pre>{@code
 * AppMetadata config = AppMetadata.builder()
 *     .metadataVersion("1.1")
 *     .defaultLogLevel("DEBUG")
 *     .uiConfig(UIConfig.builder().theme("dark").build())
 *     .build();
 * }</pre>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@lombok.Generated
public class AppMetadata {
    
    /**
     * Versão dos metadados para controle de compatibilidade.
     * 
     * @return versão atual dos metadados
     * @param metadataVersion nova versão dos metadados
     */
    @Builder.Default
    private String metadataVersion = "1.0";
    
    /**
     * ID do tipo de card padrão sugerido ao criar novos cards.
     * Se null, o sistema usará o primeiro tipo disponível.
     * 
     * @return ID do tipo de card padrão ou null se não configurado
     * @param defaultCardTypeId novo ID do tipo de card padrão
     */
    private Long defaultCardTypeId;
    
    /**
     * Tipo de progresso padrão para novos cards.
     * Se null, o sistema usará ProgressType.NONE.
     * 
     * @return tipo de progresso padrão ou null se não configurado
     * @param defaultProgressType novo tipo de progresso padrão
     */
    private ProgressType defaultProgressType;
    
    /**
     * ID do grupo de board padrão sugerido ao criar novos boards.
     * Se null, o sistema criará boards sem grupo específico.
     * 
     * @return ID do grupo padrão ou null se não configurado
     * @param defaultBoardGroupId novo ID do grupo padrão
     */
    private Long defaultBoardGroupId;
    
    /**
     * Filtro de status padrão ao abrir o sistema.
     * Valores possíveis: null (todos), "Vazio", "Não iniciado", "Em andamento", "Concluído", "Não concluídos"
     * Se null, mostra todos os boards.
     * 
     * @return filtro de status padrão ou null se não configurado
     * @param defaultStatusFilter novo filtro de status padrão
     */
    private String defaultStatusFilter;
    
    /**
     * Diretório de instalação da aplicação.
     * Usado para localizar recursos e arquivos de configuração.
     * 
     * @return caminho do diretório de instalação
     * @param installationDirectory novo caminho do diretório de instalação
     */
    private String installationDirectory;
    
    /**
     * Diretório de dados do usuário.
     * Local onde são armazenados bancos de dados e arquivos de usuário.
     * 
     * @return caminho do diretório de dados do usuário
     * @param userDataDirectory novo caminho do diretório de dados do usuário
     */
    private String userDataDirectory;
    
    /**
     * Diretório de logs da aplicação.
     * 
     * @return caminho do diretório de logs
     * @param logDirectory novo caminho do diretório de logs
     */
    private String logDirectory;
    
    /**
     * Nível de logging padrão da aplicação.
     * Valores possíveis: DEBUG, INFO, WARN, ERROR
     * 
     * @return nível de logging atual
     * @param defaultLogLevel novo nível de logging
     */
    @Builder.Default
    private String defaultLogLevel = "INFO";
    
    /**
     * Tamanho máximo do arquivo de log em MB.
     * 
     * @return tamanho máximo em MB
     * @param maxLogFileSizeMB novo tamanho máximo em MB
     */
    @Builder.Default
    private Integer maxLogFileSizeMB = 10;
    
    /**
     * Número máximo de arquivos de log a manter.
     * 
     * @return número máximo de arquivos
     * @param maxLogFiles novo número máximo de arquivos
     */
    @Builder.Default
    private Integer maxLogFiles = 5;
    
    /**
     * Intervalo de verificação de atualizações em horas.
     * 0 = desabilitado
     * 
     * @return intervalo em horas ou 0 se desabilitado
     * @param updateCheckIntervalHours novo intervalo em horas
     */
    @Builder.Default
    private Integer updateCheckIntervalHours = 24;
    
    /**
     * Se deve verificar atualizações automaticamente.
     * 
     * @return true se verificação automática estiver habilitada
     * @param autoCheckUpdates novo valor para verificação automática
     */
    @Builder.Default
    private Boolean autoCheckUpdates = true;
    
    /**
     * Se deve mostrar notificações de sistema.
     * 
     * @return true se notificações estiverem habilitadas
     * @param showSystemNotifications novo valor para notificações de sistema
     */
    @Builder.Default
    private Boolean showSystemNotifications = true;
    
    /**
     * Tempo de timeout para operações de banco de dados em segundos.
     * 
     * @return timeout em segundos
     * @param databaseTimeoutSeconds novo timeout em segundos
     */
    @Builder.Default
    private Integer databaseTimeoutSeconds = 30;
    
    /**
     * Se deve fazer backup automático do banco de dados.
     * 
     * @return true se backup automático estiver habilitado
     * @param autoBackupDatabase novo valor para backup automático
     */
    @Builder.Default
    private Boolean autoBackupDatabase = true;
    
    /**
     * Intervalo de backup automático em horas.
     * 
     * @return intervalo em horas
     * @param autoBackupIntervalHours novo intervalo em horas
     */
    @Builder.Default
    private Integer autoBackupIntervalHours = 24;
    
    /**
     * Diretório para backups automáticos.
     * 
     * @return caminho do diretório de backup
     * @param autoBackupDirectory novo caminho do diretório de backup
     */
    private String autoBackupDirectory;
    
    /**
     * Configurações de interface do usuário.
     * 
     * @return configurações de interface do usuário
     * @param uiConfig novas configurações de interface do usuário
     */
    @Builder.Default
    private UIConfig uiConfig = new UIConfig();
    
    /**
     * Configurações de performance.
     * 
     * @return configurações de performance
     * @param performanceConfig novas configurações de performance
     */
    @Builder.Default
    private PerformanceConfig performanceConfig = new PerformanceConfig();
    
    /**
     * Configurações de segurança.
     * 
     * @return configurações de segurança
     * @param securityConfig novas configurações de segurança
     */
    @Builder.Default
    private SecurityConfig securityConfig = new SecurityConfig();
    
    /**
     * Configurações de interface do usuário.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UIConfig {
        /**
         * Tema da interface (light, dark, system).
         * 
         * @return tema atual da interface
         * @param theme novo tema da interface
         */
        @Builder.Default
        private String theme = "system";
        
        /**
         * Idioma da interface.
         * 
         * @return idioma atual da interface
         * @param language novo idioma da interface
         */
        @Builder.Default
        private String language = "pt-BR";
        
        /**
         * Tamanho da fonte padrão.
         * 
         * @return tamanho da fonte em pixels
         * @param fontSize novo tamanho da fonte em pixels
         */
        @Builder.Default
        private Integer fontSize = 12;
        
        /**
         * Se deve mostrar dicas de uso.
         * 
         * @return true se dicas estiverem habilitadas
         * @param showTooltips novo valor para exibição de dicas
         */
        @Builder.Default
        private Boolean showTooltips = true;
        
        /**
         * Se deve confirmar ações destrutivas.
         * 
         * @return true se confirmação estiver habilitada
         * @param confirmDestructiveActions novo valor para confirmação de ações destrutivas
         */
        @Builder.Default
        private Boolean confirmDestructiveActions = true;
        
        /**
         * Se deve mostrar barra de progresso para operações longas.
         * 
         * @return true se barras de progresso estiverem habilitadas
         * @param showProgressBars novo valor para exibição de barras de progresso
         */
        @Builder.Default
        private Boolean showProgressBars = true;
        
        /**
         * Builder para configuração de interface do usuário.
         * 
         * <p>Permite construção fluente de objetos UIConfig
         * com valores personalizados.</p>
         */
        public static class UIConfigBuilder {
            private String theme = "system";
            private String language = "pt-BR";
            private Integer fontSize = 12;
            private Boolean showTooltips = true;
            private Boolean confirmDestructiveActions = true;
            private Boolean showProgressBars = true;
            
            /**
             * Define o tema da interface.
             * 
             * @param theme novo tema da interface
             * @return builder para encadeamento
             */
            public UIConfigBuilder theme(String theme) {
                this.theme = theme;
                return this;
            }
            
            /**
             * Define o idioma da interface.
             * 
             * @param language novo idioma da interface
             * @return builder para encadeamento
             */
            public UIConfigBuilder language(String language) {
                this.language = language;
                return this;
            }
            
            /**
             * Define o tamanho da fonte.
             * 
             * @param fontSize novo tamanho da fonte
             * @return builder para encadeamento
             */
            public UIConfigBuilder fontSize(Integer fontSize) {
                this.fontSize = fontSize;
                return this;
            }
            
            /**
             * Define se deve mostrar dicas.
             * 
             * @param showTooltips novo valor para exibição de dicas
             * @return builder para encadeamento
             */
            public UIConfigBuilder showTooltips(Boolean showTooltips) {
                this.showTooltips = showTooltips;
                return this;
            }
            
            /**
             * Define se deve confirmar ações destrutivas.
             * 
             * @param confirmDestructiveActions novo valor para confirmação
             * @return builder para encadeamento
             */
            public UIConfigBuilder confirmDestructiveActions(Boolean confirmDestructiveActions) {
                this.confirmDestructiveActions = confirmDestructiveActions;
                return this;
            }
            
            /**
             * Define se deve mostrar barras de progresso.
             * 
             * @param showProgressBars novo valor para exibição de barras de progresso
             * @return builder para encadeamento
             */
            public UIConfigBuilder showProgressBars(Boolean showProgressBars) {
                this.showProgressBars = showProgressBars;
                return this;
            }
            
            /**
             * Constrói a configuração de interface do usuário.
             * 
             * @return nova instância de UIConfig
             */
            public UIConfig build() {
                return new UIConfig(theme, language, fontSize, showTooltips, confirmDestructiveActions, showProgressBars);
            }
        }
        
        /**
         * Cria um novo builder para configuração de interface.
         * 
         * @return builder para configuração de interface
         */
        public static UIConfigBuilder builder() {
            return new UIConfigBuilder();
        }
    }
    
    /**
     * Configurações de performance.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceConfig {
        /**
         * Número máximo de cards carregados por vez.
         * 
         * @return número máximo de cards por página
         * @param maxCardsPerPage novo número máximo de cards por página
         */
        @Builder.Default
        private Integer maxCardsPerPage = 100;
        
        /**
         * Se deve usar cache para operações frequentes.
         * 
         * @return true se cache estiver habilitado
         * @param enableCaching novo valor para habilitação do cache
         */
        @Builder.Default
        private Boolean enableCaching = true;
        
        /**
         * Tamanho máximo do cache em MB.
         * 
         * @return tamanho máximo do cache em MB
         * @param maxCacheSizeMB novo tamanho máximo do cache em MB
         */
        @Builder.Default
        private Integer maxCacheSizeMB = 50;
        
        /**
         * Tempo de vida do cache em minutos.
         * 
         * @return tempo de vida do cache em minutos
         * @param cacheTimeToLiveMinutes novo tempo de vida do cache em minutos
         */
        @Builder.Default
        private Integer cacheTimeToLiveMinutes = 60;
        
        /**
         * Builder para configuração de performance.
         * 
         * <p>Permite construção fluente de objetos PerformanceConfig
         * com valores personalizados.</p>
         */
        public static class PerformanceConfigBuilder {
            private Integer maxCardsPerPage = 100;
            private Boolean enableCaching = true;
            private Integer maxCacheSizeMB = 50;
            private Integer cacheTimeToLiveMinutes = 60;
            
            /**
             * Define o número máximo de cards por página.
             * 
             * @param maxCardsPerPage novo número máximo de cards por página
             * @return builder para encadeamento
             */
            public PerformanceConfigBuilder maxCardsPerPage(Integer maxCardsPerPage) {
                this.maxCardsPerPage = maxCardsPerPage;
                return this;
            }
            
            /**
             * Define se deve usar cache.
             * 
             * @param enableCaching novo valor para habilitação do cache
             * @return builder para encadeamento
             */
            public PerformanceConfigBuilder enableCaching(Boolean enableCaching) {
                this.enableCaching = enableCaching;
                return this;
            }
            
            /**
             * Define o tamanho máximo do cache.
             * 
             * @param maxCacheSizeMB novo tamanho máximo do cache em MB
             * @return builder para encadeamento
             */
            public PerformanceConfigBuilder maxCacheSizeMB(Integer maxCacheSizeMB) {
                this.maxCacheSizeMB = maxCacheSizeMB;
                return this;
            }
            
            /**
             * Define o tempo de vida do cache.
             * 
             * @param cacheTimeToLiveMinutes novo tempo de vida do cache em minutos
             * @return builder para encadeamento
             */
            public PerformanceConfigBuilder cacheTimeToLiveMinutes(Integer cacheTimeToLiveMinutes) {
                this.cacheTimeToLiveMinutes = cacheTimeToLiveMinutes;
                return this;
            }
            
            /**
             * Constrói a configuração de performance.
             * 
             * @return nova instância de PerformanceConfig
             */
            public PerformanceConfig build() {
                return new PerformanceConfig(maxCardsPerPage, enableCaching, maxCacheSizeMB, cacheTimeToLiveMinutes);
            }
        }
        
        /**
         * Cria um novo builder para configuração de performance.
         * 
         * @return builder para configuração de performance
         */
        public static PerformanceConfigBuilder builder() {
            return new PerformanceConfigBuilder();
        }
    }
    
    /**
     * Configurações de segurança.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SecurityConfig {
        /**
         * Se deve validar entrada de dados.
         * 
         * @return true se validação de entrada estiver habilitada
         * @param validateInput novo valor para validação de entrada
         */
        @Builder.Default
        private Boolean validateInput = true;
        
        /**
         * Se deve registrar operações sensíveis no log.
         * 
         * @return true se registro de operações sensíveis estiver habilitado
         * @param logSensitiveOperations novo valor para registro de operações sensíveis
         */
        @Builder.Default
        private Boolean logSensitiveOperations = true;
        
        /**
         * Tempo máximo de sessão em minutos.
         * 
         * @return tempo máximo de sessão em minutos
         * @param maxSessionTimeMinutes novo tempo máximo de sessão em minutos
         */
        @Builder.Default
        private Integer maxSessionTimeMinutes = 480;
        
        /**
         * Builder para configuração de segurança.
         * 
         * <p>Permite construção fluente de objetos SecurityConfig
         * com valores personalizados.</p>
         */
        public static class SecurityConfigBuilder {
            private Boolean validateInput = true;
            private Boolean logSensitiveOperations = true;
            private Integer maxSessionTimeMinutes = 480;
            
            /**
             * Define se deve validar entrada de dados.
             * 
             * @param validateInput novo valor para validação de entrada
             * @return builder para encadeamento
             */
            public SecurityConfigBuilder validateInput(Boolean validateInput) {
                this.validateInput = validateInput;
                return this;
            }
            
            /**
             * Define se deve registrar operações sensíveis.
             * 
             * @param logSensitiveOperations novo valor para registro de operações sensíveis
             * @return builder para encadeamento
             */
            public SecurityConfigBuilder logSensitiveOperations(Boolean logSensitiveOperations) {
                this.logSensitiveOperations = logSensitiveOperations;
                return this;
            }
            
            /**
             * Define o tempo máximo de sessão.
             * 
             * @param maxSessionTimeMinutes novo tempo máximo de sessão em minutos
             * @return builder para encadeamento
             */
            public SecurityConfigBuilder maxSessionTimeMinutes(Integer maxSessionTimeMinutes) {
                this.maxSessionTimeMinutes = maxSessionTimeMinutes;
                return this;
            }
            
            /**
             * Constrói a configuração de segurança.
             * 
             * @return nova instância de SecurityConfig
             */
            public SecurityConfig build() {
                return new SecurityConfig(validateInput, logSensitiveOperations, maxSessionTimeMinutes);
            }
        }
        
        /**
         * Cria um novo builder para configuração de segurança.
         * 
         * @return builder para configuração de segurança
         */
        public static SecurityConfigBuilder builder() {
            return new SecurityConfigBuilder();
        }
    }
    
    /**
     * Builder para configuração principal da aplicação.
     * 
     * <p>Permite construção fluente de objetos AppMetadata
     * com valores personalizados para todas as configurações.</p>
     */
    public static class AppMetadataBuilder {
        private String metadataVersion = "1.0";
        private Long defaultCardTypeId;
        private ProgressType defaultProgressType = ProgressType.NONE;
        private Long defaultBoardGroupId;
        private String defaultStatusFilter;
        private String installationDirectory;
        private String userDataDirectory;
        private String logDirectory;
        private String defaultLogLevel = "INFO";
        private Integer maxLogFileSizeMB = 10;
        private Integer maxLogFiles = 5;
        private Integer updateCheckIntervalHours = 24;
        private Boolean autoCheckUpdates = true;
        private Boolean showSystemNotifications = true;
        private Integer databaseTimeoutSeconds = 30;
        private Boolean autoBackupDatabase = true;
        private Integer autoBackupIntervalHours = 24;
        private String autoBackupDirectory;
        private UIConfig uiConfig = UIConfig.builder().build();
        private PerformanceConfig performanceConfig = PerformanceConfig.builder().build();
        private SecurityConfig securityConfig = SecurityConfig.builder().build();
        
        /**
         * Define a versão dos metadados.
         * 
         * @param metadataVersion nova versão dos metadados
         * @return builder para encadeamento
         */
        public AppMetadataBuilder metadataVersion(String metadataVersion) {
            this.metadataVersion = metadataVersion;
            return this;
        }
        
        /**
         * Define o ID do tipo de card padrão.
         * 
         * @param defaultCardTypeId novo ID do tipo de card padrão
         * @return builder para encadeamento
         */
        public AppMetadataBuilder defaultCardTypeId(Long defaultCardTypeId) {
            this.defaultCardTypeId = defaultCardTypeId;
            return this;
        }
        
        /**
         * Define o tipo de progresso padrão.
         * 
         * @param defaultProgressType novo tipo de progresso padrão
         * @return builder para encadeamento
         */
        public AppMetadataBuilder defaultProgressType(ProgressType defaultProgressType) {
            this.defaultProgressType = defaultProgressType;
            return this;
        }
        
        /**
         * Define o ID do grupo de board padrão.
         * 
         * @param defaultBoardGroupId novo ID do grupo de board padrão
         * @return builder para encadeamento
         */
        public AppMetadataBuilder defaultBoardGroupId(Long defaultBoardGroupId) {
            this.defaultBoardGroupId = defaultBoardGroupId;
            return this;
        }
        
        /**
         * Define o filtro de status padrão.
         * 
         * @param defaultStatusFilter novo filtro de status padrão
         * @return builder para encadeamento
         */
        public AppMetadataBuilder defaultStatusFilter(String defaultStatusFilter) {
            this.defaultStatusFilter = defaultStatusFilter;
            return this;
        }
        
        /**
         * Define o diretório de instalação.
         * 
         * @param installationDirectory novo diretório de instalação
         * @return builder para encadeamento
         */
        public AppMetadataBuilder installationDirectory(String installationDirectory) {
            this.installationDirectory = installationDirectory;
            return this;
        }
        
        /**
         * Define o diretório de dados do usuário.
         * 
         * @param userDataDirectory novo diretório de dados do usuário
         * @return builder para encadeamento
         */
        public AppMetadataBuilder userDataDirectory(String userDataDirectory) {
            this.userDataDirectory = userDataDirectory;
            return this;
        }
        
        /**
         * Define o diretório de logs.
         * 
         * @param logDirectory novo diretório de logs
         * @return builder para encadeamento
         */
        public AppMetadataBuilder logDirectory(String logDirectory) {
            this.logDirectory = logDirectory;
            return this;
        }
        
        /**
         * Define o nível de log padrão.
         * 
         * @param defaultLogLevel novo nível de log padrão
         * @return builder para encadeamento
         */
        public AppMetadataBuilder defaultLogLevel(String defaultLogLevel) {
            this.defaultLogLevel = defaultLogLevel;
            return this;
        }
        
        /**
         * Define o tamanho máximo do arquivo de log.
         * 
         * @param maxLogFileSizeMB novo tamanho máximo do arquivo de log em MB
         * @return builder para encadeamento
         */
        public AppMetadataBuilder maxLogFileSizeMB(Integer maxLogFileSizeMB) {
            this.maxLogFileSizeMB = maxLogFileSizeMB;
            return this;
        }
        
        /**
         * Define o número máximo de arquivos de log.
         * 
         * @param maxLogFiles novo número máximo de arquivos de log
         * @return builder para encadeamento
         */
        public AppMetadataBuilder maxLogFiles(Integer maxLogFiles) {
            this.maxLogFiles = maxLogFiles;
            return this;
        }
        
        /**
         * Define o intervalo de verificação de atualizações.
         * 
         * @param updateCheckIntervalHours novo intervalo em horas
         * @return builder para encadeamento
         */
        public AppMetadataBuilder updateCheckIntervalHours(Integer updateCheckIntervalHours) {
            this.updateCheckIntervalHours = updateCheckIntervalHours;
            return this;
        }
        
        /**
         * Define se deve verificar atualizações automaticamente.
         * 
         * @param autoCheckUpdates novo valor para verificação automática
         * @return builder para encadeamento
         */
        public AppMetadataBuilder autoCheckUpdates(Boolean autoCheckUpdates) {
            this.autoCheckUpdates = autoCheckUpdates;
            return this;
        }
        
        /**
         * Define se deve mostrar notificações do sistema.
         * 
         * @param showSystemNotifications novo valor para notificações do sistema
         * @return builder para encadeamento
         */
        public AppMetadataBuilder showSystemNotifications(Boolean showSystemNotifications) {
            this.showSystemNotifications = showSystemNotifications;
            return this;
        }
        
        /**
         * Define o timeout do banco de dados.
         * 
         * @param databaseTimeoutSeconds novo timeout em segundos
         * @return builder para encadeamento
         */
        public AppMetadataBuilder databaseTimeoutSeconds(Integer databaseTimeoutSeconds) {
            this.databaseTimeoutSeconds = databaseTimeoutSeconds;
            return this;
        }
        
        /**
         * Define se deve fazer backup automático do banco.
         * 
         * @param autoBackupDatabase novo valor para backup automático
         * @return builder para encadeamento
         */
        public AppMetadataBuilder autoBackupDatabase(Boolean autoBackupDatabase) {
            this.autoBackupDatabase = autoBackupDatabase;
            return this;
        }
        
        /**
         * Define o intervalo de backup automático.
         * 
         * @param autoBackupIntervalHours novo intervalo em horas
         * @return builder para encadeamento
         */
        public AppMetadataBuilder autoBackupIntervalHours(Integer autoBackupIntervalHours) {
            this.autoBackupIntervalHours = autoBackupIntervalHours;
            return this;
        }
        
        /**
         * Define o diretório de backup automático.
         * 
         * @param autoBackupDirectory novo diretório de backup
         * @return builder para encadeamento
         */
        public AppMetadataBuilder autoBackupDirectory(String autoBackupDirectory) {
            this.autoBackupDirectory = autoBackupDirectory;
            return this;
        }
        
        /**
         * Define a configuração de interface do usuário.
         * 
         * @param uiConfig nova configuração de interface
         * @return builder para encadeamento
         */
        public AppMetadataBuilder uiConfig(UIConfig uiConfig) {
            this.uiConfig = uiConfig;
            return this;
        }
        
        /**
         * Define a configuração de performance.
         * 
         * @param performanceConfig nova configuração de performance
         * @return builder para encadeamento
         */
        public AppMetadataBuilder performanceConfig(PerformanceConfig performanceConfig) {
            this.performanceConfig = performanceConfig;
            return this;
        }
        
        /**
         * Define a configuração de segurança.
         * 
         * @param securityConfig nova configuração de segurança
         * @return builder para encadeamento
         */
        public AppMetadataBuilder securityConfig(SecurityConfig securityConfig) {
            this.securityConfig = securityConfig;
            return this;
        }
        
        /**
         * Constrói a configuração principal da aplicação.
         * 
         * @return nova instância de AppMetadata
         */
        public AppMetadata build() {
            return new AppMetadata(metadataVersion, defaultCardTypeId, defaultProgressType, 
                                 defaultBoardGroupId, defaultStatusFilter, installationDirectory, 
                                 userDataDirectory, logDirectory, defaultLogLevel, maxLogFileSizeMB, 
                                 maxLogFiles, updateCheckIntervalHours, autoCheckUpdates, 
                                 showSystemNotifications, databaseTimeoutSeconds, autoBackupDatabase, 
                                 autoBackupIntervalHours, autoBackupDirectory, uiConfig, 
                                 performanceConfig, securityConfig);
        }
    }
    
    /**
     * Cria um novo builder para configuração principal da aplicação.
     * 
     * @return builder para configuração principal
     */
    public static AppMetadataBuilder builder() {
        return new AppMetadataBuilder();
    }
}
