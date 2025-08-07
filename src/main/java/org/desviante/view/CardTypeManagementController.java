package org.desviante.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.desviante.model.CardType;
import org.desviante.service.CardTypeService;
import org.desviante.service.dto.CardTypeDTO;
import org.desviante.service.dto.CreateCardTypeRequestDTO;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para a tela de gerenciamento de tipos de card.
 * 
 * <p>Permite que os usuários criem, editem e excluam tipos de card personalizados
 * com seus próprios labels de unidade (ex: "páginas", "minutos", "aulas").</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class CardTypeManagementController implements Initializable {

    @FXML private VBox rootContainer;
    @FXML private TableView<CardTypeDTO> typesTable;
    @FXML private TableColumn<CardTypeDTO, String> nameColumn;
    @FXML private TableColumn<CardTypeDTO, String> unitLabelColumn;
    @FXML private TableColumn<CardTypeDTO, String> creationDateColumn;
    
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;

    private CardTypeService cardTypeService;
    private ObservableList<CardTypeDTO> typesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupButtons();
        loadCardTypes();
    }

    /**
     * Configura a tabela de tipos de card
     */
    private void setupTable() {
        typesList = FXCollections.observableArrayList();
        typesTable.setItems(typesList);

        // Configurar colunas
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().name()));
        
        unitLabelColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().unitLabel()));
        
        creationDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().creationDate()));

        // Permitir seleção única
        typesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Atualizar estado dos botões baseado na seleção
        typesTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateButtonStates());
    }

    /**
     * Configura os botões e suas ações
     */
    private void setupButtons() {
        addButton.setOnAction(e -> showCreateDialog());
        editButton.setOnAction(e -> showEditDialog());
        deleteButton.setOnAction(e -> deleteSelectedType());
        closeButton.setOnAction(e -> closeWindow());

        // Estado inicial dos botões
        updateButtonStates();
    }

    /**
     * Atualiza o estado dos botões baseado na seleção da tabela
     */
    private void updateButtonStates() {
        boolean hasSelection = typesTable.getSelectionModel().getSelectedItem() != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * Carrega os tipos de card do banco de dados
     */
    private void loadCardTypes() {
        try {
            if (cardTypeService != null) {
                List<CardType> types = cardTypeService.getAllCardTypes();
                List<CardTypeDTO> dtos = types.stream()
                    .map(this::convertToDTO)
                    .toList();
                
                typesList.clear();
                typesList.addAll(dtos);
            }
        } catch (Exception e) {
            showError("Erro ao carregar tipos de card", e.getMessage());
        }
    }

    /**
     * Converte CardType para CardTypeDTO
     */
    private CardTypeDTO convertToDTO(CardType type) {
        return new CardTypeDTO(
            type.getId(),
            type.getName(),
            type.getUnitLabel(),
            type.getCreationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            type.getLastUpdateDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }

    /**
     * Mostra o diálogo para criar um novo tipo de card
     */
    private void showCreateDialog() {
        Dialog<CreateCardTypeRequestDTO> dialog = createTypeDialog("Criar Novo Tipo", null);
        
        dialog.showAndWait().ifPresent(request -> {
            try {
                CardType newType = cardTypeService.createCardType(request.name(), request.getNormalizedUnitLabel());
                CardTypeDTO newDTO = convertToDTO(newType);
                typesList.add(newDTO);
                
                showSuccess("Tipo criado com sucesso", 
                    "O tipo '" + newType.getName() + "' foi criado com sucesso.");
            } catch (Exception e) {
                showError("Erro ao criar tipo", e.getMessage());
            }
        });
    }

    /**
     * Mostra o diálogo para editar um tipo de card
     */
    private void showEditDialog() {
        CardTypeDTO selectedType = typesTable.getSelectionModel().getSelectedItem();
        if (selectedType == null) return;

        Dialog<CreateCardTypeRequestDTO> dialog = createTypeDialog("Editar Tipo", selectedType);
        
        dialog.showAndWait().ifPresent(request -> {
            try {
                CardType updatedType = cardTypeService.updateCardType(
                    selectedType.id(), request.name(), request.getNormalizedUnitLabel());
                
                CardTypeDTO updatedDTO = convertToDTO(updatedType);
                
                // Atualizar o item na lista
                int index = typesList.indexOf(selectedType);
                if (index >= 0) {
                    typesList.set(index, updatedDTO);
                }
                
                showSuccess("Tipo atualizado com sucesso", 
                    "O tipo '" + updatedType.getName() + "' foi atualizado com sucesso.");
            } catch (Exception e) {
                showError("Erro ao atualizar tipo", e.getMessage());
            }
        });
    }

    /**
     * Cria um diálogo para criar/editar tipos de card
     */
    private Dialog<CreateCardTypeRequestDTO> createTypeDialog(String title, CardTypeDTO existingType) {
        Dialog<CreateCardTypeRequestDTO> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Digite os detalhes do tipo de card");

        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Campos do formulário
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Nome do tipo (ex: Livro, Vídeo, Curso)");
        
        TextField unitLabelField = new TextField();
        unitLabelField.setPromptText("Label da unidade (opcional, ex: páginas, minutos, aulas)");

        // Preencher campos se estiver editando
        if (existingType != null) {
            nameField.setText(existingType.name());
            unitLabelField.setText(existingType.unitLabel());
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Unidade:"), 0, 1);
        grid.add(unitLabelField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        // Validação
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validar em tempo real
        javafx.beans.value.ChangeListener<String> validationListener = (observable, oldValue, newValue) -> {
            boolean isValid = !nameField.getText().trim().isEmpty();
            saveButton.setDisable(!isValid);
        };

        nameField.textProperty().addListener(validationListener);
        unitLabelField.textProperty().addListener(validationListener);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String unitLabel = unitLabelField.getText().trim();
                
                // Se a unidade estiver em branco, usar "unidade" como padrão
                if (unitLabel.isEmpty()) {
                    unitLabel = "unidade";
                }
                
                return new CreateCardTypeRequestDTO(name, unitLabel);
            }
            return null;
        });

        return dialog;
    }

    /**
     * Exclui o tipo de card selecionado
     */
    private void deleteSelectedType() {
        CardTypeDTO selectedType = typesTable.getSelectionModel().getSelectedItem();
        if (selectedType == null) return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar Exclusão");
        confirmDialog.setHeaderText("Excluir Tipo de Card");
        confirmDialog.setContentText("Tem certeza que deseja excluir o tipo '" + selectedType.name() + "'?\n\n" +
            "Esta ação não pode ser desfeita.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = cardTypeService.deleteCardType(selectedType.id());
                    if (deleted) {
                        typesList.remove(selectedType);
                        showSuccess("Tipo excluído", 
                            "O tipo '" + selectedType.name() + "' foi excluído com sucesso.");
                    }
                } catch (Exception e) {
                    showError("Erro ao excluir tipo", e.getMessage());
                }
            }
        });
    }

    /**
     * Fecha a janela
     */
    private void closeWindow() {
        Stage stage = (Stage) rootContainer.getScene().getWindow();
        stage.close();
    }

    /**
     * Mostra uma mensagem de sucesso
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra uma mensagem de erro
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Define o serviço de tipos de card
     */
    public void setCardTypeService(CardTypeService service) {
        this.cardTypeService = service;
        loadCardTypes();
    }
} 