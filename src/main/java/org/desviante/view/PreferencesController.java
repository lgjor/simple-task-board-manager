package org.desviante.view;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.desviante.config.AppMetadataConfig;
import org.desviante.model.CardType;
import org.desviante.model.enums.ProgressType;
import org.desviante.service.CardTypeService;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Controller para a tela de preferências da aplicação.
 * 
 * <p>Permite ao usuário configurar preferências globais como tipos padrão
 * de card e progresso que serão aplicados a novos cards criados.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Component
@Slf4j
public class PreferencesController {

    @FXML
    private ComboBox<CardType> defaultCardTypeComboBox;
    
    @FXML
    private ComboBox<ProgressType> defaultProgressTypeComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private CardTypeService cardTypeService;
    private AppMetadataConfig appMetadataConfig;
    
    /**
     * Inicializa o controller após o FXML ser carregado.
     */
    @FXML
    public void initialize() {
        setupComboBoxes();
        // As preferências serão carregadas quando os serviços forem configurados
    }
    
    /**
     * Configura os ComboBoxes com os dados necessários.
     */
    private void setupComboBoxes() {
        // Configurar ComboBox de tipos de card
        defaultCardTypeComboBox.setCellFactory(param -> new CardTypeListCell());
        defaultCardTypeComboBox.setButtonCell(new CardTypeListCell());
        
        // Configurar ComboBox de tipos de progresso
        defaultProgressTypeComboBox.setCellFactory(param -> new ProgressTypeListCell());
        defaultProgressTypeComboBox.setButtonCell(new ProgressTypeListCell());
        
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
    }
    
    /**
     * Atualiza o estado do botão de salvar baseado nas mudanças.
     */
    private void updateSaveButtonState() {
        if (defaultCardTypeComboBox.getValue() != null && 
            defaultProgressTypeComboBox.getValue() != null) {
            saveButton.setDisable(false);
        } else {
            saveButton.setDisable(true);
        }
    }
    
    /**
     * Carrega as preferências atuais da aplicação.
     */
    private void loadCurrentPreferences() {
        if (cardTypeService != null && appMetadataConfig != null) {
            // Carregar tipos de card disponíveis
            List<CardType> availableCardTypes = cardTypeService.getAllCardTypes();
            defaultCardTypeComboBox.getItems().addAll(availableCardTypes);
            
            // Selecionar o tipo padrão atual ou o primeiro disponível
            Optional<Long> currentDefaultCardTypeId = appMetadataConfig.getDefaultCardTypeId();
            if (currentDefaultCardTypeId.isPresent()) {
                availableCardTypes.stream()
                    .filter(type -> type.getId().equals(currentDefaultCardTypeId.get()))
                    .findFirst()
                    .ifPresent(defaultCardTypeComboBox::setValue);
            } else if (!availableCardTypes.isEmpty()) {
                // Se não há configuração, usar o primeiro tipo disponível
                defaultCardTypeComboBox.setValue(availableCardTypes.get(0));
            }
            
            // Carregar tipos de progresso disponíveis
            defaultProgressTypeComboBox.getItems().addAll(ProgressType.values());
            
            // Selecionar o tipo de progresso padrão atual ou NONE como padrão
            Optional<ProgressType> currentDefaultProgressType = appMetadataConfig.getDefaultProgressType();
            if (currentDefaultProgressType.isPresent()) {
                defaultProgressTypeComboBox.setValue(currentDefaultProgressType.get());
            } else {
                // Se não há configuração, usar NONE como padrão
                defaultProgressTypeComboBox.setValue(ProgressType.NONE);
            }
            
            // Atualizar estado do botão de salvar
            updateSaveButtonState();
        }
    }
    
    /**
     * Salva as preferências selecionadas.
     */
    @FXML
    private void handleSave() {
        try {
            // Validar se as seleções foram feitas
            if (defaultCardTypeComboBox.getValue() == null) {
                showAlert("Aviso", "Selecione um tipo de card padrão.");
                return;
            }
            
            if (defaultProgressTypeComboBox.getValue() == null) {
                showAlert("Aviso", "Selecione um tipo de progresso padrão.");
                return;
            }
            
            // Verificar se houve mudanças reais
            boolean hasChanges = false;
            Optional<Long> currentCardTypeId = appMetadataConfig.getDefaultCardTypeId();
            Optional<ProgressType> currentProgressType = appMetadataConfig.getDefaultProgressType();
            
            if (!currentCardTypeId.equals(Optional.of(defaultCardTypeComboBox.getValue().getId()))) {
                hasChanges = true;
            }
            
            if (!currentProgressType.equals(Optional.of(defaultProgressTypeComboBox.getValue()))) {
                hasChanges = true;
            }
            
            if (!hasChanges) {
                showAlert("Informação", "Nenhuma alteração foi feita nas preferências.");
                return;
            }
            
            // Atualizar as preferências
            appMetadataConfig.updateMetadata(metadata -> {
                metadata.setDefaultCardTypeId(defaultCardTypeComboBox.getValue().getId());
                metadata.setDefaultProgressType(defaultProgressTypeComboBox.getValue());
            });
            
            // Fechar a janela - o AppMetadataConfig irá mostrar o alerta de reinicialização
            closeWindow();
            
        } catch (Exception e) {
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
     */
    public void setCardTypeService(CardTypeService cardTypeService) {
        this.cardTypeService = cardTypeService;
        // Recarregar preferências se já estiver inicializado
        if (defaultCardTypeComboBox != null) {
            loadCurrentPreferences();
        }
    }
    
    /**
     * Define a configuração de metadados da aplicação.
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
}
