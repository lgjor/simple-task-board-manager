package org.desviante.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.desviante.config.AppMetadataConfig;
import org.desviante.config.AppMetadata.UIConfig;
import org.desviante.config.AppMetadata.PerformanceConfig;
import org.desviante.config.AppMetadata.SecurityConfig;
import org.desviante.model.CardType;
import org.desviante.model.enums.ProgressType;
import org.desviante.service.CardTypeService;
import org.desviante.service.DefaultConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para gerenciamento de configurações da aplicação.
 * 
 * <p>Este controlador permite ao usuário visualizar e modificar
 * as configurações da aplicação através de uma interface gráfica,
 * incluindo tipos padrão de cards e progresso.</p>
 * 
 * <p>Principais funcionalidades:</p>
 * <ul>
 *   <li>Visualização das configurações atuais</li>
 *   <li>Modificação de tipos padrão</li>
 *   <li>Configuração de diretórios</li>
 *   <li>Validação de configurações</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class ConfigurationManagementController {
    
    @FXML private VBox configurationContainer;
    
    // Configurações de Card
    @FXML private ComboBox<CardType> defaultCardTypeComboBox;
    @FXML private ComboBox<ProgressType> defaultProgressTypeComboBox;
    
    // Configurações de Diretórios
    @FXML private TextField installationDirectoryField;
    @FXML private TextField userDataDirectoryField;
    @FXML private TextField logDirectoryField;
    @FXML private TextField backupDirectoryField;
    
    // Configurações de Interface
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Spinner<Integer> fontSizeSpinner;
    @FXML private CheckBox showTooltipsCheckBox;
    @FXML private CheckBox confirmDestructiveActionsCheckBox;
    @FXML private CheckBox showProgressBarsCheckBox;
    
    // Configurações de Performance
    @FXML private Spinner<Integer> maxCardsPerPageSpinner;
    @FXML private CheckBox enableCachingCheckBox;
    @FXML private Spinner<Integer> maxCacheSizeSpinner;
    @FXML private Spinner<Integer> cacheTimeToLiveSpinner;
    
    // Configurações de Segurança
    @FXML private CheckBox validateInputCheckBox;
    @FXML private CheckBox logSensitiveOperationsCheckBox;
    @FXML private Spinner<Integer> maxSessionTimeSpinner;
    
    // Botões
    @FXML private Button saveButton;
    @FXML private Button resetButton;
    @FXML private Button openMetadataFileButton;
    @FXML private Button validateButton;
    
    // Labels de status
    @FXML private Label statusLabel;
    @FXML private Label metadataFilePathLabel;
    
    @Autowired
    private AppMetadataConfig metadataConfig;
    
    @Autowired
    private DefaultConfigurationService defaultConfigService;
    
    @Autowired
    private CardTypeService cardTypeService;
    
    /**
     * Construtor padrão da classe ConfigurationManagementController.
     * 
     * <p>Inicializa o controlador de gerenciamento de configurações.
     * As dependências serão injetadas via Spring.</p>
     */
    public ConfigurationManagementController() {
        // Construtor padrão - dependências serão injetadas via Spring
    }
    
    /**
     * Inicializa o controlador.
     */
    @FXML
    public void initialize() {
        setupUI();
        loadCurrentConfigurations();
        setupEventHandlers();
        updateStatus("Configurações carregadas com sucesso");
    }
    
    /**
     * Configura a interface do usuário.
     */
    private void setupUI() {
        // Configurar ComboBoxes
        setupCardTypeComboBox();
        setupProgressTypeComboBox();
        setupThemeComboBox();
        setupLanguageComboBox();
        
        // Configurar Spinners
        setupSpinners();
        
        // Configurar campos de diretório como somente leitura
        installationDirectoryField.setEditable(false);
        userDataDirectoryField.setEditable(false);
        logDirectoryField.setEditable(false);
        backupDirectoryField.setEditable(false);
        
        // Mostrar caminho do arquivo de metadados
        Path metadataPath = metadataConfig.getMetadataFilePath();
        if (metadataPath != null) {
            metadataFilePathLabel.setText("Arquivo: " + metadataPath.toString());
        }
    }
    
    /**
     * Configura o ComboBox de tipos de card.
     */
    private void setupCardTypeComboBox() {
        try {
            List<CardType> cardTypes = cardTypeService.getAllCardTypes();
            defaultCardTypeComboBox.getItems().addAll(cardTypes);
            defaultCardTypeComboBox.setCellFactory(param -> new ListCell<CardType>() {
                @Override
                protected void updateItem(CardType item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName() + " (" + item.getUnitLabel() + ")");
                    }
                }
            });
            defaultCardTypeComboBox.setButtonCell(defaultCardTypeComboBox.getCellFactory().call(null));
        } catch (Exception e) {
            log.error("Erro ao configurar ComboBox de tipos de card", e);
        }
    }
    
    /**
     * Configura o ComboBox de tipos de progresso.
     */
    private void setupProgressTypeComboBox() {
        defaultProgressTypeComboBox.getItems().addAll(ProgressType.values());
        defaultProgressTypeComboBox.setCellFactory(param -> new ListCell<ProgressType>() {
            @Override
            protected void updateItem(ProgressType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name());
                }
            }
        });
        defaultProgressTypeComboBox.setButtonCell(defaultProgressTypeComboBox.getCellFactory().call(null));
    }
    
    /**
     * Configura o ComboBox de temas.
     */
    private void setupThemeComboBox() {
        themeComboBox.getItems().addAll("system", "light", "dark");
    }
    
    /**
     * Configura o ComboBox de idiomas.
     */
    private void setupLanguageComboBox() {
        languageComboBox.getItems().addAll("pt-BR", "en-US", "es-ES");
    }
    
    /**
     * Configura os spinners numéricos.
     */
    private void setupSpinners() {
        // Font size: 8-24
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 24, 12));
        
        // Max cards per page: 10-1000
        maxCardsPerPageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 100));
        
        // Max cache size: 10-500 MB
        maxCacheSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 500, 50));
        
        // Cache TTL: 5-120 minutes
        cacheTimeToLiveSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 120, 30));
        
        // Max session time: 30-1440 minutes
        maxSessionTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 1440, 480));
    }
    
    /**
     * Carrega as configurações atuais na interface.
     */
    private void loadCurrentConfigurations() {
        try {
            // Configurações de Card
            CardType defaultCardType = defaultConfigService.getDefaultCardType();
            if (defaultCardType != null) {
                defaultCardTypeComboBox.setValue(defaultCardType);
            }
            
            ProgressType defaultProgressType = defaultConfigService.getDefaultProgressType();
            if (defaultProgressType != null) {
                defaultProgressTypeComboBox.setValue(defaultProgressType);
            }
            
            // Configurações de Diretórios
            installationDirectoryField.setText(metadataConfig.getInstallationDirectory());
            userDataDirectoryField.setText(metadataConfig.getUserDataDirectory());
            logDirectoryField.setText(metadataConfig.getLogDirectory());
            
            // Configurações de Interface
            UIConfig uiConfig = metadataConfig.getUIConfig();
            if (uiConfig != null) {
                themeComboBox.setValue(uiConfig.getTheme());
                languageComboBox.setValue(uiConfig.getLanguage());
                fontSizeSpinner.getValueFactory().setValue(uiConfig.getFontSize());
                showTooltipsCheckBox.setSelected(uiConfig.getShowTooltips());
                confirmDestructiveActionsCheckBox.setSelected(uiConfig.getConfirmDestructiveActions());
                showProgressBarsCheckBox.setSelected(uiConfig.getShowProgressBars());
            }
            
            // Configurações de Performance
            PerformanceConfig perfConfig = metadataConfig.getPerformanceConfig();
            if (perfConfig != null) {
                maxCardsPerPageSpinner.getValueFactory().setValue(perfConfig.getMaxCardsPerPage());
                enableCachingCheckBox.setSelected(perfConfig.getEnableCaching());
                maxCacheSizeSpinner.getValueFactory().setValue(perfConfig.getMaxCacheSizeMB());
                cacheTimeToLiveSpinner.getValueFactory().setValue(perfConfig.getCacheTimeToLiveMinutes());
            }
            
            // Configurações de Segurança
            SecurityConfig secConfig = metadataConfig.getSecurityConfig();
            if (secConfig != null) {
                validateInputCheckBox.setSelected(secConfig.getValidateInput());
                logSensitiveOperationsCheckBox.setSelected(secConfig.getLogSensitiveOperations());
                maxSessionTimeSpinner.getValueFactory().setValue(secConfig.getMaxSessionTimeMinutes());
            }
            
        } catch (Exception e) {
            log.error("Erro ao carregar configurações", e);
            updateStatus("Erro ao carregar configurações: " + e.getMessage());
        }
    }
    
    /**
     * Configura os manipuladores de eventos.
     */
    private void setupEventHandlers() {
        // Botão Salvar
        saveButton.setOnAction(event -> handleSave());
        
        // Botão Reset
        resetButton.setOnAction(event -> handleReset());
        
        // Botão Abrir Arquivo
        openMetadataFileButton.setOnAction(event -> handleOpenMetadataFile());
        
        // Botão Validar
        validateButton.setOnAction(event -> handleValidate());
    }
    
    /**
     * Manipula o evento de salvar configurações.
     */
    private void handleSave() {
        try {
            // Atualiza configurações de Card
            CardType selectedCardType = defaultCardTypeComboBox.getValue();
            if (selectedCardType != null) {
                defaultConfigService.setDefaultCardType(selectedCardType.getId());
            }
            
            ProgressType selectedProgressType = defaultProgressTypeComboBox.getValue();
            if (selectedProgressType != null) {
                defaultConfigService.setDefaultProgressType(selectedProgressType);
            }
            
            // Atualiza outras configurações
            updateMetadataFromUI();
            
            updateStatus("Configurações salvas com sucesso!");
            
        } catch (Exception e) {
            log.error("Erro ao salvar configurações", e);
            updateStatus("Erro ao salvar: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza os metadados a partir da interface.
     */
    private void updateMetadataFromUI() throws Exception {
        metadataConfig.updateMetadata(metadata -> {
            // Configurações de Interface
            if (metadata.getUiConfig() != null) {
                metadata.getUiConfig().setTheme(themeComboBox.getValue());
                metadata.getUiConfig().setLanguage(languageComboBox.getValue());
                metadata.getUiConfig().setFontSize(fontSizeSpinner.getValue());
                metadata.getUiConfig().setShowTooltips(showTooltipsCheckBox.isSelected());
                metadata.getUiConfig().setConfirmDestructiveActions(confirmDestructiveActionsCheckBox.isSelected());
                metadata.getUiConfig().setShowProgressBars(showProgressBarsCheckBox.isSelected());
            }
            
            // Configurações de Performance
            if (metadata.getPerformanceConfig() != null) {
                metadata.getPerformanceConfig().setMaxCardsPerPage(maxCardsPerPageSpinner.getValue());
                metadata.getPerformanceConfig().setEnableCaching(enableCachingCheckBox.isSelected());
                metadata.getPerformanceConfig().setMaxCacheSizeMB(maxCacheSizeSpinner.getValue());
                metadata.getPerformanceConfig().setCacheTimeToLiveMinutes(cacheTimeToLiveSpinner.getValue());
            }
            
            // Configurações de Segurança
            if (metadata.getSecurityConfig() != null) {
                metadata.getSecurityConfig().setValidateInput(validateInputCheckBox.isSelected());
                metadata.getSecurityConfig().setLogSensitiveOperations(logSensitiveOperationsCheckBox.isSelected());
                metadata.getSecurityConfig().setMaxSessionTimeMinutes(maxSessionTimeSpinner.getValue());
            }
        });
    }
    
    /**
     * Manipula o evento de resetar configurações.
     */
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Reset");
        alert.setHeaderText("Resetar Configurações");
        alert.setContentText("Tem certeza que deseja resetar todas as configurações para os valores padrão?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Recarrega as configurações
                loadCurrentConfigurations();
                updateStatus("Configurações resetadas para valores padrão");
            } catch (Exception e) {
                log.error("Erro ao resetar configurações", e);
                updateStatus("Erro ao resetar: " + e.getMessage());
            }
        }
    }
    
    /**
     * Manipula o evento de abrir arquivo de metadados.
     */
    private void handleOpenMetadataFile() {
        try {
            Path metadataPath = metadataConfig.getMetadataFilePath();
            if (metadataPath != null && metadataPath.toFile().exists()) {
                // Abre o arquivo no editor padrão do sistema
                java.awt.Desktop.getDesktop().open(metadataPath.toFile());
                updateStatus("Arquivo de metadados aberto no editor padrão");
            } else {
                updateStatus("Arquivo de metadados não encontrado");
            }
        } catch (Exception e) {
            log.error("Erro ao abrir arquivo de metadados", e);
            updateStatus("Erro ao abrir arquivo: " + e.getMessage());
        }
    }
    
    /**
     * Manipula o evento de validar configurações.
     */
    private void handleValidate() {
        try {
            boolean isValid = defaultConfigService.validateDefaultConfigurations();
            if (isValid) {
                updateStatus("Configurações validadas com sucesso!");
            } else {
                updateStatus("Configurações inválidas detectadas!");
            }
        } catch (Exception e) {
            log.error("Erro ao validar configurações", e);
            updateStatus("Erro na validação: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza o status na interface.
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
        log.info("Status da configuração: {}", message);
    }
}
