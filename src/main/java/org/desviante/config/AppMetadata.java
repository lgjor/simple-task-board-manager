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
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMetadata {
    
    /**
     * Versão dos metadados para controle de compatibilidade.
     */
    @Builder.Default
    private String metadataVersion = "1.0";
    
    /**
     * ID do tipo de card padrão sugerido ao criar novos cards.
     * Se null, o sistema usará o primeiro tipo disponível.
     */
    private Long defaultCardTypeId;
    
    /**
     * Tipo de progresso padrão para novos cards.
     * Se null, o sistema usará ProgressType.NONE.
     */
    private ProgressType defaultProgressType;
    
    /**
     * ID do grupo de board padrão sugerido ao criar novos boards.
     * Se null, o sistema criará boards sem grupo específico.
     */
    private Long defaultBoardGroupId;
    
    /**
     * Diretório de instalação da aplicação.
     * Usado para localizar recursos e arquivos de configuração.
     */
    private String installationDirectory;
    
    /**
     * Diretório de dados do usuário.
     * Local onde são armazenados bancos de dados e arquivos de usuário.
     */
    private String userDataDirectory;
    
    /**
     * Diretório de logs da aplicação.
     */
    private String logDirectory;
    
    /**
     * Nível de logging padrão da aplicação.
     * Valores possíveis: DEBUG, INFO, WARN, ERROR
     */
    @Builder.Default
    private String defaultLogLevel = "INFO";
    
    /**
     * Tamanho máximo do arquivo de log em MB.
     */
    @Builder.Default
    private Integer maxLogFileSizeMB = 10;
    
    /**
     * Número máximo de arquivos de log a manter.
     */
    @Builder.Default
    private Integer maxLogFiles = 5;
    
    /**
     * Intervalo de verificação de atualizações em horas.
     * 0 = desabilitado
     */
    @Builder.Default
    private Integer updateCheckIntervalHours = 24;
    
    /**
     * Se deve verificar atualizações automaticamente.
     */
    @Builder.Default
    private Boolean autoCheckUpdates = true;
    
    /**
     * Se deve mostrar notificações de sistema.
     */
    @Builder.Default
    private Boolean showSystemNotifications = true;
    
    /**
     * Tempo de timeout para operações de banco de dados em segundos.
     */
    @Builder.Default
    private Integer databaseTimeoutSeconds = 30;
    
    /**
     * Se deve fazer backup automático do banco de dados.
     */
    @Builder.Default
    private Boolean autoBackupDatabase = true;
    
    /**
     * Intervalo de backup automático em horas.
     */
    @Builder.Default
    private Integer autoBackupIntervalHours = 24;
    
    /**
     * Diretório para backups automáticos.
     */
    private String autoBackupDirectory;
    
    /**
     * Configurações de interface do usuário.
     */
    @Builder.Default
    private UIConfig uiConfig = new UIConfig();
    
    /**
     * Configurações de performance.
     */
    @Builder.Default
    private PerformanceConfig performanceConfig = new PerformanceConfig();
    
    /**
     * Configurações de segurança.
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
         */
        @Builder.Default
        private String theme = "system";
        
        /**
         * Idioma da interface.
         */
        @Builder.Default
        private String language = "pt-BR";
        
        /**
         * Tamanho da fonte padrão.
         */
        @Builder.Default
        private Integer fontSize = 12;
        
        /**
         * Se deve mostrar dicas de uso.
         */
        @Builder.Default
        private Boolean showTooltips = true;
        
        /**
         * Se deve confirmar ações destrutivas.
         */
        @Builder.Default
        private Boolean confirmDestructiveActions = true;
        
        /**
         * Se deve mostrar barra de progresso para operações longas.
         */
        @Builder.Default
        private Boolean showProgressBars = true;
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
         */
        @Builder.Default
        private Integer maxCardsPerPage = 100;
        
        /**
         * Se deve usar cache para operações frequentes.
         */
        @Builder.Default
        private Boolean enableCaching = true;
        
        /**
         * Tamanho máximo do cache em MB.
         */
        @Builder.Default
        private Integer maxCacheSizeMB = 50;
        
        /**
         * Tempo de vida do cache em minutos.
         */
        @Builder.Default
        private Integer cacheTimeToLiveMinutes = 30;
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
         */
        @Builder.Default
        private Boolean validateInput = true;
        
        /**
         * Se deve logar operações sensíveis.
         */
        @Builder.Default
        private Boolean logSensitiveOperations = false;
        
        /**
         * Tempo máximo de sessão em minutos.
         */
        @Builder.Default
        private Integer maxSessionTimeMinutes = 480; // 8 horas
    }
}
