package org.desviante.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
import java.util.HashMap;
import java.util.Map;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class BoardViewController {

    // Declarado como 'final' para garantir a imutabilidade após a construção.
    private final TaskManagerFacade facade;

    // --- Componentes da Tabela de Boards ---
    @FXML
    private TableView<BoardSummaryDTO> boardsTableView;

    // --- Componentes da Tabela de Boards ---
    @FXML
    private TableColumn<BoardSummaryDTO, String> boardIdColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> boardNameColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> boardStatusColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> statusInitialColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> statusPendingColumn;
    @FXML
    private TableColumn<BoardSummaryDTO, String> statusFinalColumn;

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
    private Button refreshButton;
    // O botão linkGoogleButton foi removido
    @FXML
    private Button googleTaskButton;

    // Mapa para rastrear o nó visual de cada card pelo seu ID.
    private final Map<Long, Node> cardNodeMap = new HashMap<>();

    public BoardViewController(TaskManagerFacade facade) {
        this.facade = facade;
    }

    @FXML
    public void initialize() {
        System.out.println("BoardViewController inicializado.");
        setupBoardsTable();

        editBoardButton.disableProperty().bind(
                boardsTableView.getSelectionModel().selectedItemProperty().isNull()
        );
        deleteBoardButton.disableProperty().bind(
                boardsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        loadBoards();
    }

    private void setupBoardsTable() {
        boardIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().id().toString()));
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

    private void loadBoards() {
        var boards = facade.getAllBoardSummaries();
        boardsTableView.getItems().clear();
        boardsTableView.getItems().addAll(boards);
        System.out.println(boards.size() + " boards carregados na tabela.");
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
        TextInputDialog dialog = new TextInputDialog("Novo Board");
        dialog.setTitle("Criar Novo Board");
        dialog.setHeaderText("Digite o nome para o seu novo board.");
        dialog.setContentText("Nome:");

        dialog.showAndWait().ifPresent(boardName -> {
            if (!boardName.trim().isEmpty()) {
                facade.createNewBoard(boardName);
                loadBoards();
            }
        });
    }

    @FXML
    private void handleEditBoard() {
        BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null) {
            showError("Nenhum Board Selecionado", "Por favor, selecione o board que você deseja editar.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedBoard.name());
        dialog.setTitle("Editar Board");
        dialog.setHeaderText("Editando o board: " + selectedBoard.name());
        dialog.setContentText("Novo nome:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(selectedBoard.name())) {
                facade.updateBoardName(selectedBoard.id(), newName);
                loadBoards();
            }
        });
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
        System.out.println("Botão 'Atualizar' clicado.");
        BoardSummaryDTO selectedBoard = boardsTableView.getSelectionModel().getSelectedItem();
        loadBoards();
        if (selectedBoard != null) {
            boardsTableView.getItems().stream()
                    .filter(b -> b.id().equals(selectedBoard.id()))
                    .findFirst()
                    .ifPresent(b -> boardsTableView.getSelectionModel().select(b));
        }
    }

    // O método handleLinkGoogle() foi removido.

    @FXML
    private void handleGoogleTask() {
        System.out.println("Botão 'Google Task' clicado. Abrindo o navegador...");
        final String url = "https://tasks.google.com/tasks/";

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.err.println("Ação de abrir navegador não é suportada nesta plataforma.");
                showError("Ação não suportada", "Não foi possível abrir o navegador automaticamente.");
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            showError("Erro ao abrir o navegador", "Ocorreu um erro ao tentar abrir a página do Google Tasks.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}