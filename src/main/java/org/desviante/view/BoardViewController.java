package org.desviante.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.desviante.util.WindowManager;

/**
 * Controlador principal para a visualização e gerenciamento de quadros.
 * 
 * <p>Responsável por gerenciar a interface principal da aplicação, incluindo
 * a visualização de quadros, filtros, criação de cards e navegação entre
 * diferentes funcionalidades do sistema.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Component
public class BoardViewController {

    // Declarado como 'final' para garantir a imutabilidade após a construção.
    private final TaskManagerFacade facade;
    
    // Injetar o AppMetadataConfig para acessar as configurações
    @Autowired
    private org.desviante.config.AppMetadataConfig appMetadataConfig;
    
    // Injetar o WindowManager para gerenciar janelas secundárias
    @Autowired
    private WindowManager windowManager;

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
    private TableColumn<BoardSummaryDTO, Integer> statusInitialColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, Integer> statusPendingColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, Integer> statusFinalColumn;

    // --- Filtro de Grupos ---
    @FXML
    private ComboBox<Object> groupFilterComboBox;

    // --- Filtro de Status ---
    @FXML
    private ComboBox<String> statusFilterComboBox;

    // --- Container para o Kanban ---
    @FXML
    private HBox kanbanContainer;
    
    // --- Separador redimensionável ---
    @FXML
    private Pane resizableSeparator;

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
    @FXML
    private Button cardTypesButton;
    @FXML
    private Button customTypesButton;
    @FXML
    private Button googleTaskButton;
    @FXML
    private Button preferencesButton;
    @FXML
    private Button aboutButton;
    
    @FXML
    private Button calendarButton;

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

    /**
     * Construtor que inicializa o controlador com as dependências necessárias.
     * 
     * @param facade fachada principal para gerenciamento de tarefas
     */
    public BoardViewController(TaskManagerFacade facade) {
        this.facade = facade;
    }

    /**
     * Inicializa o controlador e configura a interface.
     * 
     * <p>Este método é chamado automaticamente pelo JavaFX após a
     * construção do controlador.</p>
     */
    @FXML
    public void initialize() {
        System.out.println("BoardViewController inicializado.");
        setupBoardsTable();
        setupGroupFilter();
        setupStatusFilter();
        setupResizableSeparator();
        setupLayoutConstraints();

        editBoardButton.disableProperty().bind(
                boardsTableView.getSelectionModel().selectedItemProperty().isNull()
        );
        deleteBoardButton.disableProperty().bind(
                boardsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        loadBoards();
    }

        private void setupResizableSeparator() {
        // Configurar tooltip para o separador redimensionável
        Tooltip tooltip = new Tooltip("Clique e arraste para redimensionar a altura da tabela de boards");
        tooltip.setShowDelay(javafx.util.Duration.millis(200));
        tooltip.setShowDuration(javafx.util.Duration.seconds(3));
        Tooltip.install(resizableSeparator, tooltip);
        
        // Garantir que o separador sempre seja clicável
        resizableSeparator.setMouseTransparent(false);
        resizableSeparator.setPickOnBounds(true);
        
        // Configurar o separador redimensionável
        resizableSeparator.setOnMousePressed(event -> {
            resizableSeparator.setUserData(event.getY());
            event.consume(); // Consumir o evento para evitar propagação
        });

        resizableSeparator.setOnMouseDragged(event -> {
            Double startY = (Double) resizableSeparator.getUserData();
            if (startY != null) {
                double deltaY = event.getY() - startY;
                double currentHeight = boardsTableView.getPrefHeight();
                // Garantir altura mínima de 120px e máxima de 60% da altura da janela
                double maxHeight = Math.max(400, boardsTableView.getScene().getHeight() * 0.6);
                double newHeight = Math.max(120, Math.min(maxHeight, currentHeight + deltaY));
                boardsTableView.setPrefHeight(newHeight);
                resizableSeparator.setUserData(event.getY());
                event.consume(); // Consumir o evento para evitar propagação
            }
        });

        resizableSeparator.setOnMouseReleased(event -> {
            resizableSeparator.setUserData(null);
            event.consume(); // Consumir o evento para evitar propagação
        });
    }

    private void setupLayoutConstraints() {
        // Garantir que a tabela de boards sempre tenha uma altura mínima visível
        boardsTableView.setMinHeight(120);
        
        // Listener para garantir que a altura mínima seja respeitada quando a janela for redimensionada
        boardsTableView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                    if (boardsTableView.getPrefHeight() < 120) {
                        boardsTableView.setPrefHeight(120);
                    }
                });
            }
        });
        
        // Garantir que pelo menos uma linha da tabela seja sempre visível
        boardsTableView.setFixedCellSize(30); // Altura fixa para cada linha
        boardsTableView.setPlaceholder(new Label("Nenhum board encontrado"));
        
        // Garantir que o separador sempre seja visível e clicável
        resizableSeparator.setMinHeight(12);
        resizableSeparator.setPrefHeight(16);
        resizableSeparator.setMaxHeight(24);
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
                    setAlignment(javafx.geometry.Pos.CENTER);
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
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // Configurar coluna do nome do board
        boardNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        boardNameColumn.setCellFactory(column -> new TableCell<BoardSummaryDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
        
        boardStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().status()));
        boardStatusColumn.setCellFactory(column -> new TableCell<BoardSummaryDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
        
        statusInitialColumn.setCellValueFactory(cellData -> 
                new ReadOnlyObjectWrapper<>(cellData.getValue().percentInitial()));
        statusInitialColumn.setCellFactory(column -> new TableCell<BoardSummaryDTO, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " %");
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
        
        statusPendingColumn.setCellValueFactory(cellData -> 
                new ReadOnlyObjectWrapper<>(cellData.getValue().percentPending()));
        statusPendingColumn.setCellFactory(column -> new TableCell<BoardSummaryDTO, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " %");
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
        
        statusFinalColumn.setCellValueFactory(cellData -> 
                new ReadOnlyObjectWrapper<>(cellData.getValue().percentFinal()));
        statusFinalColumn.setCellFactory(column -> new TableCell<BoardSummaryDTO, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " %");
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

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

    private void setupStatusFilter() {
        // Configurar o ComboBox para mostrar o status
        statusFilterComboBox.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todos os Status");
                } else {
                    setText(item);
                }
            }
        });

        // Configurar o botão do ComboBox para mostrar o status
        statusFilterComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todos os Status");
                } else {
                    setText(item);
                }
            }
        });

        // Carregar opções de status
        loadStatusOptions();
    }

    private void loadStatusOptions() {
        try {
            statusFilterComboBox.getItems().clear();
            statusFilterComboBox.getItems().add(null); // Opção "Todos os Status"
            statusFilterComboBox.getItems().add("Vazio");
            statusFilterComboBox.getItems().add("Não iniciado");
            statusFilterComboBox.getItems().add("Em andamento");
            statusFilterComboBox.getItems().add("Concluído");
            statusFilterComboBox.setValue(null); // Selecionar "Todos os Status" por padrão
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Carregar Status", "Não foi possível carregar as opções de status: " + e.getMessage());
        }
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
        Object selectedGroup = groupFilterComboBox.getValue();
        String selectedStatus = statusFilterComboBox.getValue();
        loadBoardsWithFilters(selectedGroup, selectedStatus);
    }

    @FXML
    private void handleStatusFilterChange() {
        Object selectedGroup = groupFilterComboBox.getValue();
        String selectedStatus = statusFilterComboBox.getValue();
        loadBoardsWithFilters(selectedGroup, selectedStatus);
    }

    private void loadBoards() {
        try {
            // Aplicar filtros combinados
            Object selectedGroup = groupFilterComboBox.getValue();
            String selectedStatus = statusFilterComboBox.getValue();
            loadBoardsWithFilters(selectedGroup, selectedStatus);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao Carregar Boards", "Não foi possível carregar os boards: " + e.getMessage());
        }
    }

    private void loadBoardsWithFilters(Object selectedGroup, String selectedStatus) {
        try {
            List<BoardSummaryDTO> boards;
            
            // Primeiro, filtrar por grupo
            if (selectedGroup == null) {
                // Carregar todos os boards
                boards = facade.getAllBoardSummaries();
            } else if (selectedGroup instanceof NoGroupOption) {
                // Carregar boards sem grupo
                boards = facade.getBoardsWithoutGroup();
            } else if (selectedGroup instanceof BoardGroup) {
                // Carregar boards do grupo selecionado
                boards = facade.getBoardsByGroup(((BoardGroup) selectedGroup).getId());
            } else {
                // Fallback: carregar todos os boards
                boards = facade.getAllBoardSummaries();
            }
            
            // Depois, filtrar por status
            if (selectedStatus != null) {
                boards = boards.stream()
                    .filter(board -> selectedStatus.equals(board.status()))
                    .toList();
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
        
        // Se updatedDetails for null, significa que o card foi deletado ou apenas movido
        if (updatedDetails == null) {
            System.out.println("Card ID " + cardId + " foi deletado ou movido - recarregando interface");
            // Recarregar a interface para refletir mudanças de posição ou deleção
            BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                loadKanbanViewForBoard(selectedBoard.id());
            }
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
            
            // Definir valor padrão baseado nas configurações do sistema
            BoardGroup suggestedGroup = facade.suggestDefaultBoardGroup();
            System.out.println("Grupo sugerido pelo sistema: " + (suggestedGroup != null ? suggestedGroup.getName() + " (ID: " + suggestedGroup.getId() + ")" : "null"));
            
            if (suggestedGroup != null) {
                groupComboBox.setValue(suggestedGroup);
                System.out.println("Grupo padrão definido: " + suggestedGroup.getName());
            } else {
                // Nenhum grupo sugerido - selecionar "Sem Grupo" (null)
                groupComboBox.setValue(null);
                System.out.println("Usando 'Sem Grupo' como padrão (nenhum grupo sugerido pelo sistema)");
            }
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
                    if (selectedGroup != null) {
                        facade.createNewBoardWithGroup(boardName, selectedGroup.getId());
                    } else {
                        facade.createNewBoard(boardName);
                    }
                    
                    showInfo("Board Criado", "Board '" + boardName + "' criado com sucesso!");
                    return new CreateBoardResult();
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
            // Recarregar boards, grupos e status
            loadBoardGroups();
            loadStatusOptions();
            loadBoards();
        }
    }

    // Classe auxiliar para o resultado do dialog
    private static class CreateBoardResult {
        public CreateBoardResult() {
            // Marker class - no data needed
        }
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
                loadStatusOptions();
                loadBoards();
            }
        }
    }

    // Classe auxiliar para o resultado do dialog de edição
    private static class EditBoardResult {
        private final String boardName;

        public EditBoardResult(String boardName, BoardGroup group) {
            this.boardName = boardName;
        }

        public String getBoardName() { return boardName; }
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
            // Recarregar grupos e status primeiro
            loadBoardGroups();
            loadStatusOptions();
            // Depois recarregar boards com o filtro atual
            loadBoards();
            
            // Se há um board selecionado, recarregar o Kanban
            BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                loadKanbanViewForBoard(selectedBoard.id());
            }
            
            System.out.println("Boards, grupos e status recarregados com sucesso.");
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
    private void handleCardTypes() {
        try {
            // Carregar a tela de gerenciamento de tipos de card
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/card-type-management.fxml"));
            Parent root = loader.load();
            
            // Obter o controlador
            CardTypeManagementController controller = loader.getController();
            
            // Configurar o serviço
            controller.setCardTypeService(facade.getCardTypeService());
            
            // Criar uma nova janela
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Gerenciamento de Tipos de Card");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.setResizable(true);
            
            // Centralizar a janela
            stage.centerOnScreen();
            
            // Registrar a janela no WindowManager para fechamento automático
            windowManager.registerWindow(stage, "Gerenciamento de Tipos de Card");
            
            // Mostrar a janela
            stage.show();
            
        } catch (IOException e) {
            showError("Erro", "Não foi possível abrir a tela de gerenciamento de tipos de card: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateGroup() {
        try {
            // Carregar a tela de gerenciamento de grupos de board
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/board-group-management.fxml"));
            Parent root = loader.load();
            
            // Obter o controlador
            BoardGroupManagementController controller = loader.getController();
            
            // Configurar o serviço
            controller.setBoardGroupService(facade.getBoardGroupService());
            
            // Criar uma nova janela
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Gerenciamento de Grupos de Board");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(500);
            stage.setResizable(true);
            
            // Centralizar a janela
            stage.centerOnScreen();
            
            // Registrar a janela no WindowManager para fechamento automático
            windowManager.registerWindow(stage, "Gerenciamento de Grupos de Board");
            
            // Mostrar a janela
            stage.show();
            
        } catch (IOException e) {
            showError("Erro", "Não foi possível abrir a tela de gerenciamento de grupos de board: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditGroup() {
        try {
            // Carregar a tela de gerenciamento de grupos de board
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/board-group-management.fxml"));
            Parent root = loader.load();
            
            // Obter o controlador
            BoardGroupManagementController controller = loader.getController();
            
            // Configurar o serviço
            controller.setBoardGroupService(facade.getBoardGroupService());
            
            // Criar uma nova janela
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Gerenciamento de Grupos de Board");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(500);
            stage.setResizable(true);
            
            // Centralizar a janela
            stage.centerOnScreen();
            
            // Registrar a janela no WindowManager para fechamento automático
            windowManager.registerWindow(stage, "Gerenciamento de Grupos de Board");
            
            // Mostrar a janela
            stage.show();
            
        } catch (IOException e) {
            showError("Erro", "Não foi possível abrir a tela de gerenciamento de grupos de board: " + e.getMessage());
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
            imageView.getStyleClass().add("icon-image-view");
            return imageView;
        }
        return null;
    }

    /**
     * Abre a janela de preferências para configurar tipos padrão.
     */
    @FXML
    private void handlePreferences() {
        try {
            // Carregar a tela de preferências
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/preferences.fxml"));
            Parent root = loader.load();
            
            // Obter o controlador
            PreferencesController controller = loader.getController();
            
            // Configurar os serviços necessários
            controller.setCardTypeService(facade.getCardTypeService());
            controller.setBoardGroupService(facade.getBoardGroupService());
            controller.setAppMetadataConfig(appMetadataConfig);
            
            // Criar uma nova janela
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Preferências");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.setResizable(true);
            
            // Centralizar a janela
            stage.centerOnScreen();
            
            // Registrar a janela no WindowManager para fechamento automático
            windowManager.registerWindow(stage, "Preferências");
            
            // Mostrar a janela
            stage.show();
            
        } catch (IOException e) {
            showError("Erro", "Não foi possível abrir a tela de preferências: " + e.getMessage());
        }
    }

    /**
     * Abre a janela do calendário.
     */
    @FXML
    private void handleOpenCalendar() {
        try {
            // Carregar a tela do calendário
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/calendar-view.fxml"));
            
            // Configurar o controller usando o Spring
            org.desviante.calendar.controller.CalendarViewController controller = 
                org.desviante.SimpleTaskBoardManagerApplication.getSpringContext()
                    .getBean(org.desviante.calendar.controller.CalendarViewController.class);
            loader.setController(controller);
            
            Parent root = loader.load();
            
            // Criar uma nova janela
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Calendário - Simple Task Board Manager");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setResizable(true);
            
            // Centralizar a janela
            stage.centerOnScreen();
            
            // Mostrar a janela
            stage.show();
            
            // Registrar a janela no WindowManager
            if (windowManager != null) {
                windowManager.registerWindow(stage);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            // Mostrar erro para o usuário
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao abrir o calendário");
            alert.setContentText("Não foi possível carregar a tela do calendário: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Abre a janela "Sobre" com informações do aplicativo.
     */
    @FXML
    private void handleAbout() {
        try {
            // Carregar a tela About definitiva
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/about.fxml"));
            Parent root = loader.load();
            
            // Criar uma nova janela
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Sobre - Simple Task Board Manager");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMinWidth(600);
            stage.setMinHeight(700);
            stage.setResizable(false);
            
            // Configurar handler para o botão fechar
            Button closeButton = (Button) root.lookup("#closeButton");
            if (closeButton != null) {
                closeButton.setOnAction(e -> stage.close());
            }
            
            // Aplicar CSS diretamente
            try {
                String cssPath = getClass().getResource("/css/about.css").toExternalForm();
                stage.getScene().getStylesheets().add(cssPath);
            } catch (Exception cssException) {
                System.err.println("Erro ao carregar CSS: " + cssException.getMessage());
                // Continuar sem CSS se houver erro
            }
            
            // Centralizar a janela
            stage.centerOnScreen();
            
            // Registrar a janela no WindowManager para fechamento automático
            windowManager.registerWindow(stage, "Sobre");
            
            // Mostrar a janela
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace(); // Adicionar stack trace para debug
            showError("Erro", "Não foi possível abrir a tela Sobre: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Adicionar stack trace para debug
            showError("Erro Inesperado", "Erro inesperado ao abrir a tela Sobre: " + e.getMessage());
        }
    }
}