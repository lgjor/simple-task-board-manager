package org.desviante.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.desviante.model.BoardGroup;
import org.desviante.service.BoardGroupService;
import org.desviante.service.dto.BoardGroupDTO;
import org.desviante.service.dto.CreateBoardGroupRequestDTO;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para gerenciamento de grupos de quadros.
 * 
 * <p>Responsável por gerenciar a interface de usuário para criação, edição,
 * visualização e remoção de grupos de quadros. Implementa funcionalidades
 * como validação de dados, navegação entre telas e sincronização com
 * o serviço de negócio.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class BoardGroupManagementController implements Initializable {

    @FXML private VBox rootContainer;
    @FXML private TableView<BoardGroupDTO> groupsTable;
    @FXML private TableColumn<BoardGroupDTO, String> nameColumn;
    @FXML private TableColumn<BoardGroupDTO, String> descriptionColumn;
    @FXML private TableColumn<BoardGroupDTO, String> iconColumn;
    @FXML private TableColumn<BoardGroupDTO, String> creationDateColumn;
    
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;

    private BoardGroupService boardGroupService;
    private ObservableList<BoardGroupDTO> groupsList;

    /**
     * Construtor padrão do controlador.
     * 
     * <p>Este construtor é chamado automaticamente pelo JavaFX
     * durante a inicialização da interface.</p>
     */
    public BoardGroupManagementController() {
        // Inicialização automática via JavaFX
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupButtons();
        loadBoardGroups();
    }

    /**
     * Configura a tabela de grupos de board
     */
    private void setupTable() {
        groupsList = FXCollections.observableArrayList();
        groupsTable.setItems(groupsList);

        // Configurar colunas
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().name()));
        
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().description()));
        
        iconColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().icon()));
        
        // Configurar célula personalizada para ícones
        iconColumn.setCellFactory(column -> new TableCell<BoardGroupDTO, String>() {
            private final HBox container = new HBox(5);
            private final ImageView iconView = new ImageView();
            private final Label codeLabel = new Label();
            
            {
                iconView.setFitWidth(20);
                iconView.setFitHeight(20);
                container.setAlignment(javafx.geometry.Pos.CENTER);
                container.getChildren().addAll(iconView, codeLabel);
                setGraphic(container);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
            
            @Override
            protected void updateItem(String iconCode, boolean empty) {
                super.updateItem(iconCode, empty);
                
                if (empty || iconCode == null || iconCode.trim().isEmpty()) {
                    setGraphic(null);
                    return;
                }
                
                try {
                    // Tentar carregar o ícone
                    String iconPath = "/icons/emoji/" + iconCode + ".png";
                    URL iconUrl = getClass().getResource(iconPath);
                    
                    if (iconUrl != null) {
                        Image iconImage = new Image(iconUrl.toExternalForm());
                        iconView.setImage(iconImage);
                        codeLabel.setText(""); // Não mostrar o código se o ícone foi carregado
                    } else {
                        // Se não encontrar o ícone, mostrar apenas o código
                        iconView.setImage(null);
                        codeLabel.setText(iconCode);
                    }
                    
                    setGraphic(container);
                } catch (Exception e) {
                    // Em caso de erro, mostrar apenas o código
                    iconView.setImage(null);
                    codeLabel.setText(iconCode);
                    setGraphic(container);
                }
            }
        });
        
        creationDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().creationDate()));

        // Permitir seleção única
        groupsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Atualizar estado dos botões baseado na seleção
        groupsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateButtonStates());
    }

    /**
     * Configura os botões e suas ações
     */
    private void setupButtons() {
        addButton.setOnAction(e -> showCreateDialog());
        editButton.setOnAction(e -> showEditDialog());
        deleteButton.setOnAction(e -> deleteSelectedGroup());
        closeButton.setOnAction(e -> closeWindow());

        // Estado inicial dos botões
        updateButtonStates();
    }

    /**
     * Atualiza o estado dos botões baseado na seleção da tabela
     */
    private void updateButtonStates() {
        boolean hasSelection = groupsTable.getSelectionModel().getSelectedItem() != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * Carrega os grupos de board do banco de dados
     */
    private void loadBoardGroups() {
        try {
            if (boardGroupService != null) {
                List<BoardGroup> groups = boardGroupService.getAllBoardGroups();
                List<BoardGroupDTO> dtos = groups.stream()
                    .map(this::convertToDTO)
                    .toList();
                
                groupsList.clear();
                groupsList.addAll(dtos);
            }
        } catch (Exception e) {
            showError("Erro ao carregar grupos de board", e.getMessage());
        }
    }

    /**
     * Converte BoardGroup para BoardGroupDTO
     */
    private BoardGroupDTO convertToDTO(BoardGroup group) {
        return new BoardGroupDTO(
            group.getId(),
            group.getName(),
            group.getDescription(),
            group.getColor(),
            group.getIcon(),
            group.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }

    /**
     * Mostra o diálogo para criar um novo grupo
     */
    private void showCreateDialog() {
        Dialog<CreateBoardGroupRequestDTO> dialog = createGroupDialog("Criar Novo Grupo", null);
        
        dialog.showAndWait().ifPresent(request -> {
            try {
                BoardGroup newGroup = boardGroupService.createBoardGroup(
                    request.getNormalizedName(), 
                    request.getNormalizedDescription(), 
                    request.getNormalizedIcon());
                
                BoardGroupDTO newDTO = convertToDTO(newGroup);
                groupsList.add(newDTO);
                
                showSuccess("Grupo criado com sucesso", 
                    "O grupo '" + newGroup.getName() + "' foi criado com sucesso.");
            } catch (Exception e) {
                showError("Erro ao criar grupo", e.getMessage());
            }
        });
    }

    /**
     * Mostra o diálogo para editar um grupo
     */
    private void showEditDialog() {
        BoardGroupDTO selectedGroup = groupsTable.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) return;

        Dialog<CreateBoardGroupRequestDTO> dialog = createGroupDialog("Editar Grupo", selectedGroup);
        
        dialog.showAndWait().ifPresent(request -> {
            try {
                BoardGroup updatedGroup = boardGroupService.updateBoardGroup(
                    selectedGroup.id(),
                    request.getNormalizedName(), 
                    request.getNormalizedDescription(), 
                    request.getNormalizedIcon());
                
                BoardGroupDTO updatedDTO = convertToDTO(updatedGroup);
                
                // Atualizar o item na lista
                int index = groupsList.indexOf(selectedGroup);
                if (index >= 0) {
                    groupsList.set(index, updatedDTO);
                }
                
                showSuccess("Grupo atualizado com sucesso", 
                    "O grupo '" + updatedGroup.getName() + "' foi atualizado com sucesso.");
            } catch (Exception e) {
                showError("Erro ao atualizar grupo", e.getMessage());
            }
        });
    }

    /**
     * Cria um diálogo para criar/editar grupos de board
     */
    private Dialog<CreateBoardGroupRequestDTO> createGroupDialog(String title, BoardGroupDTO existingGroup) {
        Dialog<CreateBoardGroupRequestDTO> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Digite os detalhes do grupo de board");

        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Campos do formulário
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Nome do grupo (ex: Projetos Pessoais, Trabalho)");
        
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Descrição do grupo (opcional)");
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        
        // Campo de ícone com preview visual e botão para seleção
        HBox iconBox = new HBox(5);
        iconBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Preview do ícone atual
        ImageView iconPreview = new ImageView();
        iconPreview.setFitWidth(24);
        iconPreview.setFitHeight(24);
        iconPreview.setPreserveRatio(true);
        iconPreview.setSmooth(true);
        
        // Campo de texto (oculto, usado apenas internamente)
        TextField iconField = new TextField();
        iconField.setVisible(false);
        iconField.setManaged(false);
        
        // Label para mostrar o ícone selecionado
        Label iconLabel = new Label("Nenhum ícone selecionado");
        iconLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        
        Button selectIconButton = new Button("Escolher Ícone");
        selectIconButton.setOnAction(e -> {
            String currentIcon = iconField.getText().trim();
            IconSelectionDialog.showIconSelection(currentIcon).ifPresent(selectedIcon -> {
                iconField.setText(selectedIcon);
                updateIconPreview(iconPreview, iconLabel, selectedIcon);
            });
        });
        
        iconBox.getChildren().addAll(iconPreview, iconLabel, selectIconButton);

        // Preencher campos se estiver editando
        if (existingGroup != null) {
            nameField.setText(existingGroup.name());
            descriptionField.setText(existingGroup.description());
            iconField.setText(existingGroup.icon());
            updateIconPreview(iconPreview, iconLabel, existingGroup.icon());
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Ícone:"), 0, 2);
        grid.add(iconBox, 1, 2);

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
        descriptionField.textProperty().addListener(validationListener);
        iconField.textProperty().addListener(validationListener);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                String icon = iconField.getText().trim();
                
                return new CreateBoardGroupRequestDTO(name, description, icon);
            }
            return null;
        });

        return dialog;
    }
    
    /**
     * Atualiza o preview do ícone e o texto da label
     */
    private void updateIconPreview(ImageView iconPreview, Label iconLabel, String iconCode) {
        if (iconCode == null || iconCode.trim().isEmpty()) {
            iconPreview.setImage(null);
            iconLabel.setText("Nenhum ícone selecionado");
            iconLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
            return;
        }
        
        try {
            // Tentar carregar o ícone
            String iconPath = "/icons/emoji/" + iconCode + ".png";
            URL iconUrl = getClass().getResource(iconPath);
            
            if (iconUrl != null) {
                Image iconImage = new Image(iconUrl.toExternalForm());
                iconPreview.setImage(iconImage);
                iconLabel.setText(""); // Sem texto quando o ícone é carregado com sucesso
                iconLabel.setStyle("-fx-font-size: 12px;");
            } else {
                // Se não encontrar o ícone, mostrar apenas o código
                iconPreview.setImage(null);
                iconLabel.setText("Código: " + iconCode);
                iconLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: orange;");
            }
        } catch (Exception e) {
            // Em caso de erro, mostrar apenas o código
            iconPreview.setImage(null);
            iconLabel.setText("Código: " + iconCode);
            iconLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: orange;");
        }
    }

    /**
     * Exclui o grupo selecionado
     */
    private void deleteSelectedGroup() {
        BoardGroupDTO selectedGroup = groupsTable.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar Exclusão");
        confirmDialog.setHeaderText("Excluir Grupo de Board");
        confirmDialog.setContentText("Tem certeza que deseja excluir o grupo '" + selectedGroup.name() + "'?\n\n" +
            "Esta ação não pode ser desfeita.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boardGroupService.deleteBoardGroup(selectedGroup.id());
                    groupsList.remove(selectedGroup);
                    showSuccess("Grupo excluído", 
                        "O grupo '" + selectedGroup.name() + "' foi excluído com sucesso.");
                } catch (Exception e) {
                    showError("Erro ao excluir grupo", e.getMessage());
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
     * Define o serviço de grupos de board.
     * 
     * @param service serviço de grupos de board a ser utilizado
     */
    public void setBoardGroupService(BoardGroupService service) {
        this.boardGroupService = service;
        loadBoardGroups();
    }
}
