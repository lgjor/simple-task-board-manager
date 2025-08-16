package org.desviante.view;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.desviante.config.AppMetadataConfig;
import org.desviante.model.CardType;
import org.desviante.model.BoardGroup;
import org.desviante.model.enums.ProgressType;
import org.desviante.service.CardTypeService;
import org.desviante.service.BoardGroupService;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Controller para a tela de preferências da aplicação.
 * 
 * <p>Esta classe gerencia a interface de usuário para configuração de preferências
 * globais do sistema. Permite ao usuário definir valores padrão que serão aplicados
 * automaticamente a novos elementos criados na aplicação.</p>
 * 
 * <p><strong>Responsabilidades Principais:</strong></p>
 * <ul>
 *   <li>Gerenciamento da interface de preferências (FXML)</li>
 *   <li>Configuração de tipos padrão de cards</li>
 *   <li>Configuração de tipos padrão de progresso</li>
 *   <li>Configuração de grupos padrão para quadros</li>
 *   <li>Persistência das configurações através do AppMetadataConfig</li>
 * </ul>
 * 
 * <p><strong>Funcionalidades:</strong></p>
 * <ul>
 *   <li>Carregamento automático de opções disponíveis nos ComboBoxes</li>
 *   <li>Validação de mudanças antes de habilitar o botão de salvar</li>
 *   <li>Persistência das configurações no banco de dados</li>
 *   <li>Feedback visual para o usuário sobre o estado das configurações</li>
 * </ul>
 * 
 * <p><strong>Integração:</strong></p>
 * <p>Utiliza serviços especializados ({@link CardTypeService}, {@link BoardGroupService})
 * para obter dados e o {@link AppMetadataConfig} para persistir as configurações
 * escolhidas pelo usuário.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CardTypeService
 * @see BoardGroupService
 * @see AppMetadataConfig
 * @see CardType
 * @see BoardGroup
 * @see ProgressType
 */
@Component
@Slf4j
public class PreferencesController {

    @FXML
    private ComboBox<CardType> defaultCardTypeComboBox;
    
    @FXML
    private ComboBox<ProgressType> defaultProgressTypeComboBox;
    
    @FXML
    private ComboBox<BoardGroup> defaultBoardGroupComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private CardTypeService cardTypeService;
    private BoardGroupService boardGroupService;
    private AppMetadataConfig appMetadataConfig;
    
    /**
     * Construtor padrão da classe PreferencesController.
     * 
     * <p>Inicializa o controller de preferências da aplicação.
     * Os serviços serão injetados via setter methods.</p>
     */
    public PreferencesController() {
        // Construtor padrão - serviços serão injetados via setter methods
    }
    
    /**
     * Inicializa o controller após o FXML ser carregado.
     * 
     * <p>Este método é chamado automaticamente pelo JavaFX após o carregamento
     * do arquivo FXML. Ele configura os componentes da interface e prepara
     * os listeners para detectar mudanças nas preferências.</p>
     * 
     * <p><strong>Comportamento:</strong></p>
     * <ul>
     *   <li>Configura os ComboBoxes com células personalizadas</li>
     *   <li>Adiciona listeners para detectar mudanças de valores</li>
     *   <li>Prepara a interface para carregamento das preferências</li>
     * </ul>
     */
    @FXML
    public void initialize() {
        setupComboBoxes();
        // As preferências serão carregadas quando os serviços forem configurados
    }
    
    /**
     * Configura os ComboBoxes com os dados necessários.
     * 
     * <p>Este método privado configura cada ComboBox com células personalizadas
     * para exibição adequada dos dados e adiciona listeners para detectar
     * mudanças nos valores selecionados.</p>
     * 
     * <p><strong>Configurações:</strong></p>
     * <ul>
     *   <li>Tipos de card com células personalizadas</li>
     *   <li>Tipos de progresso com células personalizadas</li>
     *   <li>Grupos de quadros com células personalizadas</li>
     *   <li>Listeners para atualização do estado do botão salvar</li>
     * </ul>
     */
    private void setupComboBoxes() {
        // Configurar ComboBox de tipos de card
        defaultCardTypeComboBox.setCellFactory(param -> new CardTypeListCell());
        defaultCardTypeComboBox.setButtonCell(new CardTypeListCell());
        
        // Configurar ComboBox de tipos de progresso
        defaultProgressTypeComboBox.setCellFactory(param -> new ProgressTypeListCell());
        defaultProgressTypeComboBox.setButtonCell(new ProgressTypeListCell());
        
        // Configurar ComboBox de grupos de tabuleiro
        defaultBoardGroupComboBox.setCellFactory(param -> new BoardGroupListCell());
        defaultBoardGroupComboBox.setButtonCell(new BoardGroupListCell());
        
        // Adicionar listeners para detectar mudanças
        defaultCardTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSaveButtonState();
            }
        });
        
        defaultProgressTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSaveButtonState();
            }
        });

        defaultBoardGroupComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSaveButtonState();
            }
        });
    }
    
    /**
     * Atualiza o estado do botão de salvar baseado nas mudanças.
     * 
     * <p>Verifica se houve alterações nas preferências selecionadas e
     * habilita ou desabilita o botão de salvar conforme necessário.</p>
     */
    private void updateSaveButtonState() {
        // Tipo de card e progresso são obrigatórios
        boolean cardTypeValid = defaultCardTypeComboBox.getValue() != null;
        boolean progressTypeValid = defaultProgressTypeComboBox.getValue() != null;
        
        // Grupo pode ser null (Sem Grupo) ou um grupo válido
        boolean boardGroupValid = defaultBoardGroupComboBox.getValue() != null;
        
        // Habilitar botão apenas se todos os campos obrigatórios estiverem preenchidos
        saveButton.setDisable(!(cardTypeValid && progressTypeValid && boardGroupValid));
    }
    
    /**
     * Carrega as preferências atuais da aplicação.
     */
    private void loadCurrentPreferences() {
        if (cardTypeService != null && boardGroupService != null && appMetadataConfig != null) {
            log.debug("Carregando preferências atuais...");
            
            // Carregar tipos de card disponíveis
            List<CardType> availableCardTypes = cardTypeService.getAllCardTypes();
            defaultCardTypeComboBox.getItems().addAll(availableCardTypes);
            log.debug("Carregados {} tipos de card", availableCardTypes.size());
            
            // Selecionar o tipo padrão atual ou o primeiro disponível
            Optional<Long> currentDefaultCardTypeId = appMetadataConfig.getDefaultCardTypeId();
            if (currentDefaultCardTypeId.isPresent() && currentDefaultCardTypeId.get() != null) {
                availableCardTypes.stream()
                    .filter(type -> type.getId().equals(currentDefaultCardTypeId.get()))
                    .findFirst()
                    .ifPresent(type -> {
                        defaultCardTypeComboBox.setValue(type);
                        log.debug("Tipo de card padrão selecionado: {} (ID: {})", type.getName(), type.getId());
                    });
            } else if (!availableCardTypes.isEmpty()) {
                // Se não há configuração, usar o primeiro tipo disponível
                CardType firstType = availableCardTypes.get(0);
                defaultCardTypeComboBox.setValue(firstType);
                log.debug("Usando primeiro tipo disponível como padrão: {} (ID: {})", firstType.getName(), firstType.getId());
            }
            
            // Carregar tipos de progresso disponíveis
            defaultProgressTypeComboBox.getItems().addAll(ProgressType.values());
            
            // Selecionar o tipo de progresso padrão atual ou NONE como padrão
            Optional<ProgressType> currentDefaultProgressType = appMetadataConfig.getDefaultProgressType();
            if (currentDefaultProgressType.isPresent() && currentDefaultProgressType.get() != null) {
                defaultProgressTypeComboBox.setValue(currentDefaultProgressType.get());
                log.debug("Tipo de progresso padrão selecionado: {}", currentDefaultProgressType.get());
            } else {
                // Se não há configuração, usar NONE como padrão
                defaultProgressTypeComboBox.setValue(ProgressType.NONE);
                log.debug("Usando ProgressType.NONE como padrão");
            }

            // Carregar grupos de tabuleiro disponíveis
            List<BoardGroup> availableBoardGroups = boardGroupService.getAllBoardGroups();
            log.info("DEBUG: Grupos disponíveis carregados: {} grupos", availableBoardGroups.size());
            availableBoardGroups.forEach(group -> log.debug("DEBUG: - {} (ID: {})", group.getName(), group.getId()));
            
            // Limpar e popular o ComboBox
            defaultBoardGroupComboBox.getItems().clear();
            
            // Adicionar opção "Sem Grupo" no início da lista
            BoardGroup noGroupOption = createNoGroupOption();
            defaultBoardGroupComboBox.getItems().add(noGroupOption);
            log.debug("DEBUG: Opção 'Sem Grupo' adicionada ao ComboBox");
            
            // Adicionar grupos disponíveis
            defaultBoardGroupComboBox.getItems().addAll(availableBoardGroups);
            log.debug("DEBUG: ComboBox populado com {} itens", defaultBoardGroupComboBox.getItems().size());
            
            // Selecionar o grupo padrão atual ou "Sem Grupo" como padrão
            Optional<Long> currentDefaultBoardGroupId = appMetadataConfig.getDefaultBoardGroupId();
            log.info("DEBUG: currentDefaultBoardGroupId = {}", currentDefaultBoardGroupId);
            
            if (currentDefaultBoardGroupId.isPresent() && currentDefaultBoardGroupId.get() != null) {
                log.info("DEBUG: Grupo padrão configurado encontrado: ID = {}", currentDefaultBoardGroupId.get());
                
                // Buscar o grupo configurado na lista
                defaultBoardGroupComboBox.getItems().stream()
                    .filter(group -> group.getId() != null && group.getId().equals(currentDefaultBoardGroupId.get()))
                    .findFirst()
                    .ifPresent(group -> {
                        defaultBoardGroupComboBox.setValue(group);
                        log.info("DEBUG: Grupo padrão selecionado no ComboBox: {} (ID: {})", group.getName(), group.getId());
                    });
                
                // Verificar se o grupo foi encontrado
                if (defaultBoardGroupComboBox.getValue() == null || 
                    !defaultBoardGroupComboBox.getValue().getId().equals(currentDefaultBoardGroupId.get())) {
                    log.warn("DEBUG: Grupo configurado não foi encontrado na lista! ID esperado: {}", currentDefaultBoardGroupId.get());
                    log.warn("DEBUG: Itens disponíveis no ComboBox:");
                    defaultBoardGroupComboBox.getItems().forEach(item -> 
                        log.warn("DEBUG: - {} (ID: {})", item.getName(), item.getId()));
                }
            } else {
                log.info("DEBUG: Nenhum grupo padrão configurado ou é null");
                // Se não há configuração ou é null, usar "Sem Grupo" como padrão
                defaultBoardGroupComboBox.setValue(noGroupOption); // "Sem Grupo" está na posição 0
                log.debug("Usando 'Sem Grupo' como padrão (nenhuma configuração anterior)");
            }
            
            // Atualizar estado do botão de salvar
            updateSaveButtonState();
            log.debug("Preferências carregadas com sucesso");
        }
    }
    
    /**
     * Salva as preferências selecionadas.
     */
    @FXML
    private void handleSave() {
        try {
            log.info("Iniciando salvamento das preferências...");
            
            // Validar se as seleções foram feitas
            if (defaultCardTypeComboBox.getValue() == null) {
                showAlert("Aviso", "Selecione um tipo de card padrão.");
                return;
            }
            
            if (defaultProgressTypeComboBox.getValue() == null) {
                showAlert("Aviso", "Selecione um tipo de progresso padrão.");
                return;
            }

            if (defaultBoardGroupComboBox.getValue() == null) {
                showAlert("Aviso", "Selecione um grupo de tabuleiro padrão.");
                return;
            }
            
            // Log das seleções para debug
            BoardGroup selectedGroup = defaultBoardGroupComboBox.getValue();
            log.info("Preferências selecionadas - Tipo de Card: {}, Progresso: {}, Grupo: {} (ID: {})", 
                    defaultCardTypeComboBox.getValue().getName(),
                    defaultProgressTypeComboBox.getValue(),
                    selectedGroup.getName(),
                    selectedGroup.getId());
            
            // Verificar se houve mudanças reais
            boolean hasChanges = false;
            Optional<Long> currentCardTypeId = appMetadataConfig.getDefaultCardTypeId();
            Optional<ProgressType> currentProgressType = appMetadataConfig.getDefaultProgressType();
            Optional<Long> currentBoardGroupId = appMetadataConfig.getDefaultBoardGroupId();
            
            // Verificar mudanças no tipo de card
            if (!currentCardTypeId.equals(Optional.of(defaultCardTypeComboBox.getValue().getId()))) {
                hasChanges = true;
                log.debug("Mudança detectada no tipo de card padrão");
            }
            
            // Verificar mudanças no tipo de progresso
            if (!currentProgressType.equals(Optional.of(defaultProgressTypeComboBox.getValue()))) {
                hasChanges = true;
                log.debug("Mudança detectada no tipo de progresso padrão");
            }

            // Verificar mudanças no grupo padrão
            Long newGroupId = null;
            
            // Se "Sem Grupo" for selecionado, definir como null
            if (selectedGroup.getId() == null) {
                // "Sem Grupo" selecionado - manter como null
                newGroupId = null;
                log.debug("'Sem Grupo' selecionado - configuração será null");
            } else {
                // Grupo válido selecionado - usar o ID do grupo
                newGroupId = selectedGroup.getId();
                log.debug("Grupo válido selecionado: {} (ID: {})", selectedGroup.getName(), newGroupId);
            }
            
            if (!currentBoardGroupId.equals(Optional.ofNullable(newGroupId))) {
                hasChanges = true;
                log.debug("Mudança detectada no grupo padrão: {} -> {}", 
                        currentBoardGroupId.orElse(null), newGroupId);
            }
            
            if (!hasChanges) {
                showAlert("Informação", "Nenhuma alteração foi feita nas preferências.");
                return;
            }
            
            log.info("Salvando alterações nas preferências...");
            
            // Capturar valores finais para uso no lambda
            final Long finalNewGroupId = newGroupId;
            final BoardGroup finalSelectedGroup = selectedGroup;
            
            // Atualizar as preferências
            appMetadataConfig.updateMetadata(metadata -> {
                metadata.setDefaultCardTypeId(defaultCardTypeComboBox.getValue().getId());
                metadata.setDefaultProgressType(defaultProgressTypeComboBox.getValue());
                
                // Aplicar o grupo padrão baseado na seleção
                if (finalNewGroupId == null) {
                    // "Sem Grupo" ou nenhum grupo selecionado - definir como null
                    metadata.setDefaultBoardGroupId(null);
                    log.debug("Definindo grupo padrão como null (Sem Grupo)");
                } else {
                    // Grupo válido selecionado - usar o ID do grupo
                    metadata.setDefaultBoardGroupId(finalNewGroupId);
                    log.debug("Definindo grupo padrão como: {} (ID: {})", 
                             finalSelectedGroup.getName(), finalNewGroupId);
                }
            });
            
            log.info("Preferências salvas com sucesso");
            
            // Fechar a janela - o AppMetadataConfig irá mostrar o alerta de reinicialização
            closeWindow();
            
        } catch (Exception e) {
            log.error("Erro ao salvar preferências", e);
            showAlert("Erro", "Erro ao salvar preferências: " + e.getMessage());
        }
    }
    
    /**
     * Cancela as alterações e fecha a janela.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    /**
     * Fecha a janela de preferências.
     */
    private void closeWindow() {
        Stage stage = (Stage) defaultCardTypeComboBox.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Mostra um alerta para o usuário.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Define o serviço de tipos de card.
     * 
     * @param cardTypeService serviço de tipos de card
     */
    public void setCardTypeService(CardTypeService cardTypeService) {
        this.cardTypeService = cardTypeService;
        // Recarregar preferências se já estiver inicializado
        if (defaultCardTypeComboBox != null) {
            loadCurrentPreferences();
        }
    }

    /**
     * Define o serviço de grupos de tabuleiro.
     * 
     * @param boardGroupService serviço de grupos de tabuleiro
     */
    public void setBoardGroupService(BoardGroupService boardGroupService) {
        this.boardGroupService = boardGroupService;
        // Recarregar preferências se já estiver inicializado
        if (defaultBoardGroupComboBox != null) {
            loadCurrentPreferences();
        }
    }
    
    /**
     * Define a configuração de metadados da aplicação.
     * 
     * @param appMetadataConfig configuração de metadados da aplicação
     */
    public void setAppMetadataConfig(AppMetadataConfig appMetadataConfig) {
        this.appMetadataConfig = appMetadataConfig;
        // Recarregar preferências se já estiver inicializado
        if (defaultCardTypeComboBox != null) {
            loadCurrentPreferences();
        }
    }
    
    /**
     * Célula personalizada para exibir tipos de card no ComboBox.
     */
    private static class CardTypeListCell extends javafx.scene.control.ListCell<CardType> {
        @Override
        protected void updateItem(CardType item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getName());
            }
        }
    }
    
    /**
     * Célula personalizada para exibir tipos de progresso no ComboBox.
     */
    private static class ProgressTypeListCell extends javafx.scene.control.ListCell<ProgressType> {
        @Override
        protected void updateItem(ProgressType item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getDisplayName());
            }
        }
    }

    /**
     * Célula personalizada para exibir grupos de tabuleiro no ComboBox.
     */
    private static class BoardGroupListCell extends javafx.scene.control.ListCell<BoardGroup> {
        @Override
        protected void updateItem(BoardGroup item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                // Se o ID for null, é a opção "Sem Grupo"
                if (item.getId() == null) {
                    setText("Sem Grupo");
                    setStyle("-fx-text-fill: #adb5bd; -fx-font-style: italic;");
                } else {
                    setText(item.getName());
                    setStyle(""); // Resetar estilo
                }
            }
        }
    }

    /**
     * Cria a opção "Sem Grupo" para o ComboBox de grupos de tabuleiro.
     * Esta opção permite que novos boards sejam criados sem grupo específico.
     */
    private BoardGroup createNoGroupOption() {
        BoardGroup noGroup = new BoardGroup();
        noGroup.setId(null); // Indica que é a opção "Sem Grupo"
        noGroup.setName("Sem Grupo");
        noGroup.setDescription("Novos boards serão criados sem grupo específico");
        return noGroup;
    }
}
