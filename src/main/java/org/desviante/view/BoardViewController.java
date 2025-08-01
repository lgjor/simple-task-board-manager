package org.desviante.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.desviante.model.BoardGroup;
import org.desviante.service.TaskManagerFacade;
import org.desviante.service.dto.BoardColumnDetailDTO;
import org.desviante.service.dto.BoardDetailDTO;
import org.desviante.service.dto.BoardSummaryDTO;
import org.desviante.service.dto.CardDetailDTO;
import org.desviante.service.dto.UpdateCardDetailsDTO;
import org.desviante.view.component.CardViewController;
import org.desviante.view.component.ColumnViewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@Component
public class BoardViewController {

    // Declarado como 'final' para garantir a imutabilidade após a construção.
    private final TaskManagerFacade facade;

    // --- Componentes da Tabela de Boards ---
    @FXML
    private TableView<BoardSummaryDTO> boardsTableView;

    // --- Componentes da Tabela de Boards ---
    @FXML
    private TableColumn<BoardSummaryDTO, String> boardNameColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> boardGroupColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> boardGroupIconColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> boardStatusColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> statusInitialColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> statusPendingColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> statusFinalColumn;

    // --- Filtro de Grupos ---
    @FXML
    private ComboBox<Object> groupFilterComboBox;

    // --- Container para o Kanban ---
    @FXML
    private HBox kanbanContainer;

    // --- Botões ---
    @FXML
    private Button createBoardButton;
    @FXML
    private Button editBoardButton;
    @FXML
    private Button deleteBoardButton;
    @FXML
    private Button createGroupButton;
    @FXML
    private Button editGroupButton;
    @FXML
    private Button refreshButton;
    // O botão linkGoogleButton foi removido
    @FXML
    private Button googleTaskButton;

    // Mapa para rastrear o nó visual de cada card pelo seu ID.
    private final Map<Long, Node> cardNodeMap = new HashMap<>();

    // Classe especial para representar a opção "Sem Grupo"
    private static class NoGroupOption {
        public static final NoGroupOption INSTANCE = new NoGroupOption();
        
        @Override
        public String toString() {
            return "Sem Grupo";
        }
    }

    public BoardViewController(TaskManagerFacade facade) {
        this.facade = facade;
    }

    @FXML
    public void initialize() {
        System.out.println("BoardViewController inicializado.");
        setupBoardsTable();
        setupGroupFilter();

        editBoardButton.disableProperty().bind(
                boardsTableView.getSelectionModel().selectedItemProperty().isNull()
        );
        deleteBoardButton.disableProperty().bind(
                boardsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        loadBoards();
    }

    private void setupBoardsTable() {
        // Configurar coluna do nome do grupo
        boardGroupColumn.setCellValueFactory(cellData -> {
            BoardGroup group = cellData.getValue().group();
            return new SimpleStringProperty(group != null ? group.getName() : "Sem Grupo");
        });
        
        // Aplicar estilo especial para boards sem grupo
        boardGroupColumn.setCellFactory(column -> new TableCell<BoardSummaryDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Sem Grupo".equals(item)) {
                        setStyle("-fx-text-fill: #adb5bd; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: #adb5bd; -fx-font-weight: normal;");
                    }
                }
            }
        });

        // Configurar coluna do ícone do grupo
        boardGroupIconColumn.setCellValueFactory(cellData -> {
            BoardGroup group = cellData.getValue().group();
            return new SimpleStringProperty(group != null ? group.getIcon() : null);
        });
        
        // Configurar a célula para mostrar o ícone
        boardGroupIconColumn.setCellFactory(column -> new TableCell<BoardSummaryDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    ImageView imageView = createEmojiImageView(item);
                    setGraphic(imageView);
                }
            }
        });

        // Configurar coluna do nome do board
        boardNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        
        boardStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().status()));
        statusInitialColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().percentInitial() + " %"));
        statusPendingColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().percentPending() + " %"));
        statusFinalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().percentFinal() + " %"));

        boardsTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadKanbanViewForBoard(newValue.id());
                    } else {
                        kanbanContainer.getChildren().clear();
                        cardNodeMap.clear();
                    }
                }
        );

        // Habilita a edição com duplo clique na tabela de Boards
        boardsTableView.setRowFactory(tv -> {
            TableRow<BoardSummaryDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleEditBoard();
                }
            });
            return row;
        });
    }

    private void setupGroupFilter() {
        // Configurar o ComboBox para mostrar o nome do grupo
        groupFilterComboBox.setCellFactory(param -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todos os Grupos");
                } else if (item instanceof BoardGroup) {
                    setText(((BoardGroup) item).getName());
                } else if (item instanceof NoGroupOption) {
                    setText("Sem Grupo");
                } else {
                    setText("Todos os Grupos");
                }
            }
        });

        // Configurar o botão do ComboBox para mostrar o nome do grupo
        groupFilterComboBox.setButtonCell(new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todos os Grupos");
                } else if (item instanceof BoardGroup) {
                    setText(((BoardGroup) item).getName());
                } else if (item instanceof NoGroupOption) {
                    setText("Sem Grupo");
                } else {
                    setText("Todos os Grupos");
                }
            }
        });

        // Carregar grupos
        loadBoardGroups();
    }

    private void loadBoardGroups() {
        try {
            List<BoardGroup> groups = facade.getAllBoardGroups();
            groupFilterComboBox.getItems().clear();
            groupFilterComboBox.getItems().add(null); // Opção "Todos os Grupos"
            groupFilterComboBox.getItems().add(NoGroupOption.INSTANCE); // Opção "Sem Grupo"
            groupFilterComboBox.getItems().addAll(groups);
            groupFilterComboBox.setValue(null); // Selecionar "Todos os Grupos" por padrão
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Carregar Grupos", "Não foi possível carregar os grupos de boards: " + e.getMessage());
        }
    }

    @FXML
    private void handleGroupFilterChange() {
        Object selectedItem = groupFilterComboBox.getValue();
        loadBoardsByGroup(selectedItem);
    }

    private void loadBoardsByGroup(Object selectedItem) {
        try {
            List<BoardSummaryDTO> boards;
            if (selectedItem == null) {
                // Carregar todos os boards
                boards = facade.getAllBoardSummaries();
            } else if (selectedItem instanceof NoGroupOption) {
                // Carregar boards sem grupo
                boards = facade.getBoardsWithoutGroup();
            } else if (selectedItem instanceof BoardGroup) {
                // Carregar boards do grupo selecionado
                boards = facade.getBoardsByGroup(((BoardGroup) selectedItem).getId());
            } else {
                // Fallback: carregar todos os boards
                boards = facade.getAllBoardSummaries();
            }
            
            boardsTableView.getItems().clear();
            boardsTableView.getItems().addAll(boards);
            
            // Limpar o Kanban se não houver board selecionado
            if (boards.isEmpty()) {
                kanbanContainer.getChildren().clear();
                cardNodeMap.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Filtrar Boards", "Não foi possível filtrar os boards: " + e.getMessage());
        }
    }

    private void loadBoards() {
        try {
            // Usar o filtro de grupo atual
            Object selectedItem = groupFilterComboBox.getValue();
            loadBoardsByGroup(selectedItem);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Carregar Boards", "Não foi possível carregar os boards: " + e.getMessage());
        }
    }

    private void loadKanbanViewForBoard(Long boardId) {
        kanbanContainer.getChildren().clear();
        cardNodeMap.clear();
        System.out.println("Carregando visão Kanban para o Board ID: " + boardId);

        try {
            BoardDetailDTO boardDetails = facade.getBoardDetails(boardId);

            for (BoardColumnDetailDTO columnData : boardDetails.columns()) {
                FXMLLoader columnLoader = new FXMLLoader(getClass().getResource("/view/column-view.fxml"));
                Parent columnNode = columnLoader.load();
                ColumnViewController columnController = columnLoader.getController();
                columnNode.setUserData(columnController);

                columnController.setData(
                        this.facade,
                        boardDetails.name(),
                        columnData,
                        this::handleCardDrop,
                        this::updateSelectedBoardSummary,
                        this::handleCardUpdate
                );

                for (CardDetailDTO cardData : columnData.cards()) {
                    FXMLLoader cardLoader = new FXMLLoader(getClass().getResource("/view/card-view.fxml"));
                    Parent cardNode = cardLoader.load();
                    CardViewController cardController = cardLoader.getController();
                    cardNode.setUserData(cardController);

                    cardController.setData(
                            this.facade,
                            boardDetails.name(),
                            cardData,
                            columnData.id(),
                            this::handleCardUpdate
                    );

                    cardNodeMap.put(cardData.id(), cardNode);
                    columnController.addCard(cardNode);
                }
                kanbanContainer.getChildren().add(columnNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erro de UI", "Falha ao carregar a visão Kanban: " + e.getMessage());
        }
    }

    private void handleCardUpdate(Long cardId, UpdateCardDetailsDTO updatedDetails) {
        System.out.println("Atualizando detalhes para o card ID: " + cardId);
        
        // Se updatedDetails for null, significa que o card foi deletado
        if (updatedDetails == null) {
            System.out.println("Card ID " + cardId + " foi deletado");
            // Remove o card do mapa de nós
            cardNodeMap.remove(cardId);
            // Atualiza o resumo do board
            updateSelectedBoardSummary();
            return;
        }
        
        try {
            facade.updateCardDetails(cardId, updatedDetails);
            BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                loadKanbanViewForBoard(selectedBoard.id());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Atualizar", "Não foi possível salvar as alterações do card: " + e.getMessage());
        }
    }

    private void handleCardDrop(Long cardId, Long targetColumnId) {
        System.out.println("Tentando mover card " + cardId + " para a coluna " + targetColumnId);
        try {
            CardDetailDTO updatedCardDTO = facade.moveCard(cardId, targetColumnId);

            Node cardNode = cardNodeMap.get(cardId);
            if (cardNode != null) {
                for (Node columnNode : kanbanContainer.getChildren()) {
                    ColumnViewController controller = (ColumnViewController) columnNode.getUserData();
                    if (controller != null && controller.getColumnId().equals(targetColumnId)) {
                        controller.addCard(cardNode);
                        break;
                    }
                }

                CardViewController cardController = (CardViewController) cardNode.getUserData();
                if (cardController != null) {
                    cardController.updateDisplayData(updatedCardDTO);
                    cardController.updateSourceColumn(targetColumnId);
                }
            }
            updateSelectedBoardSummary();
            System.out.println("Movido com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Mover", "Falha ao mover o card: " + e.getMessage());
            BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                loadKanbanViewForBoard(selectedBoard.id());
            }
        }
    }

    private void updateSelectedBoardSummary() {
        BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
        if (selectedBoard != null) {
            BoardSummaryDTO updatedSummary = facade.getBoardSummary(selectedBoard.id());
            int index = boardsTableView.getItems().indexOf(selectedBoard);
            if (index != -1) {
                boardsTableView.getItems().set(index, updatedSummary);
                boardsTableView.getSelectionModel().select(index);
            }
        }
    }

    @FXML
    private void handleCreateBoard() {
        // Criar dialog para criar board com grupo
        Dialog<CreateBoardResult> dialog = new Dialog<>();
        dialog.setTitle("Criar Novo Board");
        dialog.setHeaderText("Preencha as informações do novo board");

        // Configurar botões
        ButtonType createButtonType = new ButtonType("Criar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Criar campos do formulário
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Nome do board");
        
        ComboBox<BoardGroup> groupComboBox = new ComboBox<>();
        groupComboBox.setPromptText("Selecione um grupo");
        
        // Configurar o ComboBox para mostrar o nome do grupo
        groupComboBox.setCellFactory(param -> new ListCell<BoardGroup>() {
            @Override
            protected void updateItem(BoardGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sem Grupo");
                } else {
                    setText(item.getName());
                }
            }
        });

        groupComboBox.setButtonCell(new ListCell<BoardGroup>() {
            @Override
            protected void updateItem(BoardGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sem Grupo");
                } else {
                    setText(item.getName());
                }
            }
        });

        // Carregar grupos existentes
        try {
            List<BoardGroup> groups = facade.getAllBoardGroups();
            groupComboBox.getItems().add(null); // Opção "Sem Grupo"
            groupComboBox.getItems().addAll(groups);
            groupComboBox.setValue(null); // Selecionar "Sem Grupo" por padrão
        } catch (Exception e) {
            e.printStackTrace();
        }

        grid.add(new Label("Nome do Board:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Grupo:"), 0, 1);
        grid.add(groupComboBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Focar no campo nome
        Platform.runLater(nameField::requestFocus);

        // Converter resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String boardName = nameField.getText().trim();
                BoardGroup selectedGroup = groupComboBox.getValue();

                if (boardName.isEmpty()) {
                    showError("Erro de Validação", "O nome do board é obrigatório.");
                    return null;
                }

                try {
                    BoardSummaryDTO newBoard;
                    if (selectedGroup != null) {
                        newBoard = facade.createNewBoardWithGroup(boardName, selectedGroup.getId());
                    } else {
                        newBoard = facade.createNewBoard(boardName);
                    }
                    
                    showInfo("Board Criado", "Board '" + boardName + "' criado com sucesso!");
                    return new CreateBoardResult(boardName, selectedGroup);
                } catch (Exception e) {
                    showError("Erro ao Criar Board", "Não foi possível criar o board: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Mostrar dialog e processar resultado
        Optional<CreateBoardResult> result = dialog.showAndWait();
        if (result.isPresent()) {
            // Recarregar boards e grupos
            loadBoardGroups();
            loadBoards();
        }
    }

    // Classe auxiliar para o resultado do dialog
    private static class CreateBoardResult {
        private final String boardName;
        private final BoardGroup group;

        public CreateBoardResult(String boardName, BoardGroup group) {
            this.boardName = boardName;
            this.group = group;
        }

        public String getBoardName() { return boardName; }
        public BoardGroup getGroup() { return group; }
    }

    @FXML
    private void handleEditBoard() {
        BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null) {
            showError("Nenhum Board Selecionado", "Por favor, selecione o board que você deseja editar.");
            return;
        }

        // Criar dialog para editar board com grupo
        Dialog<EditBoardResult> dialog = new Dialog<>();
        dialog.setTitle("Editar Board");
        dialog.setHeaderText("Editando o board: " + selectedBoard.name());

        // Configurar botões
        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Excluir", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        // Criar campos do formulário
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedBoard.name());
        nameField.setPromptText("Nome do board");
        
        ComboBox<BoardGroup> groupComboBox = new ComboBox<>();
        groupComboBox.setPromptText("Selecione um grupo");
        
        // Configurar o ComboBox para mostrar o nome do grupo
        groupComboBox.setCellFactory(param -> new ListCell<BoardGroup>() {
            @Override
            protected void updateItem(BoardGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sem Grupo");
                } else {
                    setText(item.getName());
                }
            }
        });

        groupComboBox.setButtonCell(new ListCell<BoardGroup>() {
            @Override
            protected void updateItem(BoardGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sem Grupo");
                } else {
                    setText(item.getName());
                }
            }
        });

        // Carregar grupos existentes e selecionar o atual
        try {
            List<BoardGroup> groups = facade.getAllBoardGroups();
            groupComboBox.getItems().add(null); // Opção "Sem Grupo"
            groupComboBox.getItems().addAll(groups);
            
            // Selecionar o grupo atual do board
            BoardGroup currentGroup = selectedBoard.group();
            groupComboBox.setValue(currentGroup);
        } catch (Exception e) {
            e.printStackTrace();
        }

        grid.add(new Label("Nome do Board:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Grupo:"), 0, 1);
        grid.add(groupComboBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Focar no campo nome
        Platform.runLater(nameField::requestFocus);

        // Converter resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newName = nameField.getText().trim();
                BoardGroup selectedGroup = groupComboBox.getValue();

                if (newName.isEmpty()) {
                    showError("Erro de Validação", "O nome do board é obrigatório.");
                    return null;
                }

                try {
                    boolean hasChanges = false;
                    
                    // Verificar se o nome mudou
                    if (!newName.equals(selectedBoard.name())) {
                        facade.updateBoardName(selectedBoard.id(), newName);
                        hasChanges = true;
                    }
                    
                    // Verificar se o grupo mudou
                    BoardGroup currentGroup = selectedBoard.group();
                    Long newGroupId = selectedGroup != null ? selectedGroup.getId() : null;
                    Long currentGroupId = currentGroup != null ? currentGroup.getId() : null;
                    
                    if (!Objects.equals(newGroupId, currentGroupId)) {
                        facade.updateBoardGroup(selectedBoard.id(), newGroupId);
                        hasChanges = true;
                    }
                    
                    if (hasChanges) {
                        showInfo("Board Atualizado", "Board '" + newName + "' atualizado com sucesso!");
                        return new EditBoardResult(newName, selectedGroup);
                    } else {
                        showInfo("Nenhuma Alteração", "Nenhuma alteração foi feita.");
                        return null;
                    }
                } catch (Exception e) {
                    showError("Erro ao Atualizar Board", "Não foi possível atualizar o board: " + e.getMessage());
                    return null;
                }
            } else if (dialogButton == deleteButtonType) {
                // Confirmar exclusão
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmar Exclusão");
                confirmationDialog.setHeaderText("Excluir o board '" + selectedBoard.name() + "'?");
                confirmationDialog.setContentText("Esta ação é irreversível e também excluirá todas as colunas e cards associados a este board.");

                Optional<ButtonType> confirmationResult = confirmationDialog.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    try {
                        facade.deleteBoard(selectedBoard.id());
                        showInfo("Board Excluído", "Board '" + selectedBoard.name() + "' foi excluído com sucesso!");
                        return new EditBoardResult(null, null); // Indica que foi excluído
                    } catch (Exception e) {
                        showError("Erro ao Excluir Board", "Não foi possível excluir o board: " + e.getMessage());
                        return null;
                    }
                }
                return null; // Usuário cancelou a exclusão
            }
            return null;
        });

        // Mostrar dialog e processar resultado
        Optional<EditBoardResult> result = dialog.showAndWait();
        if (result.isPresent()) {
            EditBoardResult editResult = result.get();
            if (editResult.getBoardName() == null) {
                // Board foi excluído
                loadBoards();
            } else {
                // Board foi atualizado
                loadBoardGroups();
                loadBoards();
            }
        }
    }

    // Classe auxiliar para o resultado do dialog de edição
    private static class EditBoardResult {
        private final String boardName;
        private final BoardGroup group;

        public EditBoardResult(String boardName, BoardGroup group) {
            this.boardName = boardName;
            this.group = group;
        }

        public String getBoardName() { return boardName; }
        public BoardGroup getGroup() { return group; }
    }

    @FXML
    private void handleDeleteBoard() {
        BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null) {
            showError("Nenhum Board Selecionado", "Por favor, selecione o board que você deseja excluir.");
            return;
        }

        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Confirmar Exclusão");
        confirmationDialog.setHeaderText("Excluir o board '" + selectedBoard.name() + "'?");
        confirmationDialog.setContentText("Esta ação é irreversível e também excluirá todas as colunas e cards associados a este board.");

        confirmationDialog.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    facade.deleteBoard(selectedBoard.id());
                    loadBoards();
                });
    }

    @FXML
    private void handleRefresh() {
        try {
            // Recarregar grupos primeiro
            loadBoardGroups();
            // Depois recarregar boards com o filtro atual
            loadBoards();
            
            // Se há um board selecionado, recarregar o Kanban
            BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                loadKanbanViewForBoard(selectedBoard.id());
            }
            
            System.out.println("Boards e grupos recarregados com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Atualizar", "Não foi possível atualizar os dados: " + e.getMessage());
        }
    }

    // O método handleLinkGoogle() foi removido.

    @FXML
    private void handleGoogleTask() {
        try {
            Desktop.getDesktop().browse(new URI("https://tasks.google.com"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            showError("Erro ao Abrir Google Tasks", "Não foi possível abrir o Google Tasks: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateGroup() {
        // Criar dialog para criar grupo
        Dialog<BoardGroup> dialog = new Dialog<>();
        dialog.setTitle("Criar Novo Grupo");
        dialog.setHeaderText("Preencha as informações do novo grupo");

        // Configurar botões
        ButtonType createButtonType = new ButtonType("Criar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Criar campos do formulário
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Nome do grupo");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Descrição (opcional)");
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        ColorPicker colorPicker = new ColorPicker(Color.BLUE);
        colorPicker.setPromptText("Cor do grupo");

        // Lista de códigos de emojis PNG disponíveis (50 ícones verificados e existentes)
        String[] availableIcons = {
            // 📁 Pastas e Arquivos
            "1f4c1", "1f4c2", "1f4c3", "1f4c4", "1f4c5", "1f4c6", "1f4c7", "1f4c8", "1f4c9",
            
            // 💰 Dinheiro e Finanças
            "1f4b0", "1f4b1", "1f4b2", "1f4b3", "1f4b4", "1f4b5", "1f4b6", "1f4b7", "1f4b8", "1f4b9",
            
            // 💻 Tecnologia e Computação
            "1f4bb", "1f4bd", "1f4be", "1f4bf", "1f4c0",
            
            // 🏠 Casa e Vida Doméstica
            "1f3e0", "1f3e1", "1f3e2", "1f3e3", "1f3e4", "1f3e5", "1f3e6", "1f3e7", "1f3e8", "1f3e9",
            
            // 🏢 Organizações e Lugares
            "1f3ea", "1f3eb", "1f3ec", "1f3ed", "1f3ee", "1f3ef", "1f3f0", "1f3f3",
            
            // 🚀 Transporte e Mobilidade
            "1f680", "1f681", "1f682", "1f683", "1f684", "1f685", "1f686", "1f687", "1f688", "1f689",
            
            // 🔥 Elementos e Natureza
            "1f525", "1f526", "1f527", "1f528", "1f529", "1f52a", "1f52b", "1f52c", "1f52d", "1f52e",
            
            // 💡 Ideias e Inovação
            "1f4a1", "1f4a2", "1f4a3", "1f4a4", "1f4a5", "1f4a6", "1f4a7", "1f4a8", "1f4a9", "1f4aa",
            
            // ⭐ Símbolos e Indicadores
            "2b50", "2b55",
            
            // 📚 Conhecimento e Estudo
            "1f4da", "1f4db", "1f4dc", "1f4dd", "1f4de", "1f4df", "1f4e0", "1f4e1", "1f4e2", "1f4e3",
            
            // 🎓 Educação e Aprendizado
            "1f393", "1f396", "1f397", "1f399",
            
            // 📱 Dispositivos
            "1f4f1", "1f4f2", "1f4f3", "1f4f4", "1f4f5", "1f4f6", "1f4f7", "1f4f8", "1f4f9", "1f4fa",
            
            // 🎨 Criatividade e Arte
            "1f3a8", "1f3a9", "1f3aa", "1f3ab", "1f3ac", "1f3ad", "1f3ae", "1f3af", "1f3b0", "1f3b1",
            
            // 🏆 Conquistas e Prêmios
            "1f3c6", "1f3c7", "1f3c8", "1f3c9", "1f3ca", "1f3cb", "1f3cc", "1f3cd", "1f3ce", "1f3cf",
            
            // 🎯 Objetivos e Metas
            "1f3af", "1f3b2", "1f3b3", "1f3b4", "1f3b5", "1f3b6", "1f3b7", "1f3b8", "1f3b9", "1f3ba"
        };

        ComboBox<String> iconComboBox = new ComboBox<>();
        iconComboBox.getItems().addAll(availableIcons);
        iconComboBox.setValue("1f4c1"); // Ícone padrão (pasta)
        iconComboBox.setPromptText("Selecione um ícone");
        iconComboBox.getStyleClass().add("icon-combo-box");
        
        // Configurar o ComboBox para mostrar imagens PNG
        iconComboBox.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null); // Não mostrar texto, apenas imagem
                    ImageView imageView = createEmojiImageView(item);
                    setGraphic(imageView);
                    getStyleClass().setAll("icon-list-cell");
                }
            }
        });
        
        iconComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("📁");
                    setGraphic(null);
                } else {
                    setText(null); // Não mostrar texto, apenas imagem
                    ImageView imageView = createEmojiImageView(item);
                    setGraphic(imageView);
                }
                getStyleClass().setAll("icon-combo-button");
            }
        });

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Cor:"), 0, 2);
        grid.add(colorPicker, 1, 2);
        grid.add(new Label("Ícone:"), 0, 3);
        grid.add(iconComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Focar no campo nome
        Platform.runLater(nameField::requestFocus);

        // Converter resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                String color = String.format("#%02X%02X%02X",
                        (int) (colorPicker.getValue().getRed() * 255),
                        (int) (colorPicker.getValue().getGreen() * 255),
                        (int) (colorPicker.getValue().getBlue() * 255));
                String icon = iconComboBox.getValue();

                if (name.isEmpty()) {
                    showError("Erro de Validação", "O nome do grupo é obrigatório.");
                    return null;
                }

                try {
                    BoardGroup newGroup = facade.createBoardGroup(name, description, color, icon);
                    showInfo("Grupo Criado", "Grupo '" + name + "' criado com sucesso!");
                    return newGroup;
                } catch (Exception e) {
                    showError("Erro ao Criar Grupo", "Não foi possível criar o grupo: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Mostrar dialog e processar resultado
        Optional<BoardGroup> result = dialog.showAndWait();
        if (result.isPresent()) {
            // Recarregar grupos e atualizar filtro
            loadBoardGroups();
            // Manter o filtro atual
            Object currentFilter = groupFilterComboBox.getValue();
            if (currentFilter != null) {
                // Se estava filtrando por um grupo específico, manter
                groupFilterComboBox.setValue(currentFilter);
            }
        }
    }

    @FXML
    private void handleEditGroup() {
        // Primeiro, mostrar dialog para selecionar o grupo a editar
        Dialog<BoardGroup> selectDialog = new Dialog<>();
        selectDialog.setTitle("Selecionar Grupo para Editar");
        selectDialog.setHeaderText("Escolha o grupo que você deseja editar");

        // Configurar botões
        ButtonType selectButtonType = new ButtonType("Editar", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Excluir", ButtonBar.ButtonData.OTHER);
        selectDialog.getDialogPane().getButtonTypes().addAll(selectButtonType, deleteButtonType, ButtonType.CANCEL);

        // Criar ComboBox para selecionar grupo
        ComboBox<BoardGroup> groupComboBox = new ComboBox<>();
        groupComboBox.setPromptText("Selecione um grupo");
        
        // Configurar o ComboBox para mostrar o nome do grupo
        groupComboBox.setCellFactory(param -> new ListCell<BoardGroup>() {
            @Override
            protected void updateItem(BoardGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Selecione um grupo");
                } else {
                    setText(item.getName());
                }
            }
        });

        groupComboBox.setButtonCell(new ListCell<BoardGroup>() {
            @Override
            protected void updateItem(BoardGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Selecione um grupo");
                } else {
                    setText(item.getName());
                }
            }
        });

        // Carregar grupos existentes
        try {
            List<BoardGroup> groups = facade.getAllBoardGroups();
            if (groups.isEmpty()) {
                showError("Nenhum Grupo", "Não há grupos para editar. Crie um grupo primeiro.");
                return;
            }
            groupComboBox.getItems().addAll(groups);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Carregar Grupos", "Não foi possível carregar os grupos: " + e.getMessage());
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Grupo:"), 0, 0);
        grid.add(groupComboBox, 1, 0);

        selectDialog.getDialogPane().setContent(grid);

        // Focar no ComboBox
        Platform.runLater(groupComboBox::requestFocus);

        // Converter resultado da seleção
        selectDialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return groupComboBox.getValue();
            } else if (dialogButton == deleteButtonType) {
                BoardGroup selectedGroup = groupComboBox.getValue();
                if (selectedGroup == null) {
                    showError("Nenhum Grupo Selecionado", "Por favor, selecione um grupo para excluir.");
                    return null;
                }
                
                // Confirmar exclusão
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmar Exclusão");
                confirmationDialog.setHeaderText("Excluir o grupo '" + selectedGroup.getName() + "'?");
                confirmationDialog.setContentText("Esta ação é irreversível. Todos os boards deste grupo ficarão sem grupo (Sem Grupo).");

                Optional<ButtonType> confirmationResult = confirmationDialog.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    try {
                        facade.deleteBoardGroup(selectedGroup.getId());
                        showInfo("Grupo Excluído", "Grupo '" + selectedGroup.getName() + "' foi excluído com sucesso!");
                        return new BoardGroup(); // Retorna um objeto vazio para indicar que foi excluído
                    } catch (Exception e) {
                        showError("Erro ao Excluir Grupo", "Não foi possível excluir o grupo: " + e.getMessage());
                        return null;
                    }
                }
                return null; // Usuário cancelou a exclusão
            }
            return null;
        });

        // Mostrar dialog de seleção
        Optional<BoardGroup> selectedGroup = selectDialog.showAndWait();
        if (selectedGroup.isPresent()) {
            BoardGroup groupResult = selectedGroup.get();
            if (groupResult.getId() == null) {
                // Grupo foi excluído
                loadBoardGroups();
                loadBoards();
            } else {
                // Agora mostrar dialog para editar o grupo selecionado
                showEditGroupDialog(groupResult);
            }
        }
    }

    private void showEditGroupDialog(BoardGroup groupToEdit) {
        // Criar dialog para editar grupo
        Dialog<BoardGroup> dialog = new Dialog<>();
        dialog.setTitle("Editar Grupo");
        dialog.setHeaderText("Editando o grupo: " + groupToEdit.getName());

        // Configurar botões
        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Excluir", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        // Criar campos do formulário
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(groupToEdit.getName());
        nameField.setPromptText("Nome do grupo");
        
        TextArea descriptionField = new TextArea(groupToEdit.getDescription() != null ? groupToEdit.getDescription() : "");
        descriptionField.setPromptText("Descrição (opcional)");
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        
        ColorPicker colorPicker = new ColorPicker();
        if (groupToEdit.getColor() != null && groupToEdit.getColor().startsWith("#")) {
            try {
                String colorStr = groupToEdit.getColor();
                int r = Integer.parseInt(colorStr.substring(1, 3), 16);
                int g = Integer.parseInt(colorStr.substring(3, 5), 16);
                int b = Integer.parseInt(colorStr.substring(5, 7), 16);
                colorPicker.setValue(Color.rgb(r, g, b));
            } catch (Exception e) {
                colorPicker.setValue(Color.BLUE);
            }
        } else {
            colorPicker.setValue(Color.BLUE);
        }
        colorPicker.setPromptText("Cor do grupo");

        // Lista de códigos de emojis PNG disponíveis (50 ícones verificados e existentes)
        String[] availableIcons = {
            // 📁 Pastas e Arquivos
            "1f4c1", "1f4c2", "1f4c3", "1f4c4", "1f4c5", "1f4c6", "1f4c7", "1f4c8", "1f4c9",
            
            // 💰 Dinheiro e Finanças
            "1f4b0", "1f4b1", "1f4b2", "1f4b3", "1f4b4", "1f4b5", "1f4b6", "1f4b7", "1f4b8", "1f4b9",
            
            // 💻 Tecnologia e Computação
            "1f4bb", "1f4bd", "1f4be", "1f4bf", "1f4c0",
            
            // 🏠 Casa e Vida Doméstica
            "1f3e0", "1f3e1", "1f3e2", "1f3e3", "1f3e4", "1f3e5", "1f3e6", "1f3e7", "1f3e8", "1f3e9",
            
            // 🏢 Organizações e Lugares
            "1f3ea", "1f3eb", "1f3ec", "1f3ed", "1f3ee", "1f3ef", "1f3f0", "1f3f3",
            
            // 🚀 Transporte e Mobilidade
            "1f680", "1f681", "1f682", "1f683", "1f684", "1f685", "1f686", "1f687", "1f688", "1f689",
            
            // 🔥 Elementos e Natureza
            "1f525", "1f526", "1f527", "1f528", "1f529", "1f52a", "1f52b", "1f52c", "1f52d", "1f52e",
            
            // 💡 Ideias e Inovação
            "1f4a1", "1f4a2", "1f4a3", "1f4a4", "1f4a5", "1f4a6", "1f4a7", "1f4a8", "1f4a9", "1f4aa",
            
            // ⭐ Símbolos e Indicadores
            "2b50", "2b55",
            
            // 📚 Conhecimento e Estudo
            "1f4da", "1f4db", "1f4dc", "1f4dd", "1f4de", "1f4df", "1f4e0", "1f4e1", "1f4e2", "1f4e3",
            
            // 🎓 Educação e Aprendizado
            "1f393", "1f396", "1f397", "1f399",
            
            // 📱 Dispositivos
            "1f4f1", "1f4f2", "1f4f3", "1f4f4", "1f4f5", "1f4f6", "1f4f7", "1f4f8", "1f4f9", "1f4fa",
            
            // 🎨 Criatividade e Arte
            "1f3a8", "1f3a9", "1f3aa", "1f3ab", "1f3ac", "1f3ad", "1f3ae", "1f3af", "1f3b0", "1f3b1",
            
            // 🏆 Conquistas e Prêmios
            "1f3c6", "1f3c7", "1f3c8", "1f3c9", "1f3ca", "1f3cb", "1f3cc", "1f3cd", "1f3ce", "1f3cf",
            
            // 🎯 Objetivos e Metas
            "1f3af", "1f3b2", "1f3b3", "1f3b4", "1f3b5", "1f3b6", "1f3b7", "1f3b8", "1f3b9", "1f3ba"
        };

        ComboBox<String> iconComboBox = new ComboBox<>();
        iconComboBox.getItems().addAll(availableIcons);
        iconComboBox.setValue(groupToEdit.getIcon() != null ? groupToEdit.getIcon() : "1f4c1");
        iconComboBox.setPromptText("Selecione um ícone");
        iconComboBox.getStyleClass().add("icon-combo-box");
        
        // Configurar o ComboBox para mostrar imagens PNG
        iconComboBox.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null); // Não mostrar texto, apenas imagem
                    ImageView imageView = createEmojiImageView(item);
                    setGraphic(imageView);
                    getStyleClass().setAll("icon-list-cell");
                }
            }
        });
        
        iconComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("📁");
                    setGraphic(null);
                } else {
                    setText(null); // Não mostrar texto, apenas imagem
                    ImageView imageView = createEmojiImageView(item);
                    setGraphic(imageView);
                }
                getStyleClass().setAll("icon-combo-button");
            }
        });

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Cor:"), 0, 2);
        grid.add(colorPicker, 1, 2);
        grid.add(new Label("Ícone:"), 0, 3);
        grid.add(iconComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Focar no campo nome
        Platform.runLater(nameField::requestFocus);

        // Converter resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                String color = String.format("#%02X%02X%02X",
                        (int) (colorPicker.getValue().getRed() * 255),
                        (int) (colorPicker.getValue().getGreen() * 255),
                        (int) (colorPicker.getValue().getBlue() * 255));
                String icon = iconComboBox.getValue();

                if (name.isEmpty()) {
                    showError("Erro de Validação", "O nome do grupo é obrigatório.");
                    return null;
                }

                try {
                    BoardGroup updatedGroup = facade.updateBoardGroup(groupToEdit.getId(), name, description, color, icon);
                    showInfo("Grupo Atualizado", "Grupo '" + name + "' atualizado com sucesso!");
                    return updatedGroup;
                } catch (Exception e) {
                    showError("Erro ao Atualizar Grupo", "Não foi possível atualizar o grupo: " + e.getMessage());
                    return null;
                }
            } else if (dialogButton == deleteButtonType) {
                // Confirmar exclusão
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmar Exclusão");
                confirmationDialog.setHeaderText("Excluir o grupo '" + groupToEdit.getName() + "'?");
                confirmationDialog.setContentText("Esta ação é irreversível. Todos os boards deste grupo ficarão sem grupo (Sem Grupo).");

                Optional<ButtonType> confirmationResult = confirmationDialog.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    try {
                        facade.deleteBoardGroup(groupToEdit.getId());
                        showInfo("Grupo Excluído", "Grupo '" + groupToEdit.getName() + "' foi excluído com sucesso!");
                        return new BoardGroup(); // Retorna um objeto vazio para indicar que foi excluído
                    } catch (Exception e) {
                        showError("Erro ao Excluir Grupo", "Não foi possível excluir o grupo: " + e.getMessage());
                        return null;
                    }
                }
                return null; // Usuário cancelou a exclusão
            }
            return null;
        });

        // Mostrar dialog e processar resultado
        Optional<BoardGroup> result = dialog.showAndWait();
        if (result.isPresent()) {
            BoardGroup groupResult = result.get();
            if (groupResult.getId() == null) {
                // Grupo foi excluído
                loadBoardGroups();
                loadBoards();
            } else {
                // Grupo foi atualizado
                loadBoardGroups();
                loadBoards();
                // Manter o filtro atual
                Object currentFilter = groupFilterComboBox.getValue();
                if (currentFilter != null) {
                    // Se estava filtrando por um grupo específico, manter
                    groupFilterComboBox.setValue(currentFilter);
                }
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Carrega uma imagem PNG do diretório de recursos
     */
    private Image loadEmojiImage(String emojiCode) {
        try {
            String imagePath = "/icons/emoji/" + emojiCode + ".png";
            return new Image(getClass().getResourceAsStream(imagePath));
        } catch (Exception e) {
            // Se não conseguir carregar a imagem, retorna null
            return null;
        }
    }

    /**
     * Cria um ImageView com tamanho 16x16 para o ComboBox
     */
    private ImageView createEmojiImageView(String emojiCode) {
        Image image = loadEmojiImage(emojiCode);
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(16);
            imageView.setFitHeight(16);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return imageView;
        }
        return null;
    }
}