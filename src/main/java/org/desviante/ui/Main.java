package org.desviante.ui;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
import org.desviante.ui.components.BoardAccordion;
import org.desviante.ui.components.BoardTableComponent;
import org.desviante.controller.BoardController;
import org.desviante.controller.CardController;
import org.desviante.util.AlertUtils;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.desviante.persistence.dao.BoardColumnDAO;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Objects;
import javafx.scene.layout.Priority;

import static org.desviante.persistence.config.ConnectionConfig.getConnection;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();
    private BorderPane root;
    private VBox columnDisplay;
    private BoardController boardController;
    private CardController cardController;
    /**
     * Cria a tabela de boards
     */
    private TableView<BoardEntity> tableView = BoardTableComponent.createBoardTable(boardList);

    @Override
    public void start(Stage primaryStage) {
        try (Connection connection = getConnection()){
            BoardColumnDAO boardColumnDAO = new BoardColumnDAO(connection);
            boardColumnDAO.setRefreshBoardCallback((cardId, tableView, columnDisplay) ->
                    BoardTableComponent.refreshBoardView(cardId, tableView, columnDisplay)
            );
            boardController = new BoardController();
            cardController = new CardController();

            primaryStage.setTitle("Gerenciador de Boards");

            root = new BorderPane();
            root.setPadding(new Insets(10));

            VBox actionButtons = createActionButtons(tableView);

            columnDisplay = new VBox();
            root.setBottom(columnDisplay);
            columnDisplay.setId("column-display");
            columnDisplay.setSpacing(10);

            BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);
            BorderPane.setMargin(columnDisplay, new Insets(10, 0, 0, 0));
            BoardTableComponent.configureTableViewListener(tableView, columnDisplay);

            root.setCenter(tableView);
            root.setRight(actionButtons);
            root.setBottom(columnDisplay);

            Scene scene = new Scene(root, 1024, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
            primaryStage.setScene(scene);

            BoardTableComponent.configureTableViewListener(tableView, columnDisplay);
            BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);

            primaryStage.show();
        } catch (Exception e) {
            logger.error("Erro ao carregar o board", e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro de Conexão", "Não foi possível conectar ao banco de dados: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    /**
     * Cria os botões de ação
     */
    private VBox createActionButtons(TableView<BoardEntity> tableView) {
        Button createBoardButton = new Button("Criar Board");
        createBoardButton.setOnAction(e -> createBoard(tableView));

        Button deleteBoardButton = new Button("Excluir Board");
        deleteBoardButton.setOnAction(e -> deleteSelectedBoard(tableView));

        Button refreshButton = new Button("Atualizar");
        refreshButton.setOnAction(e -> BoardTableComponent.loadBoards(tableView, boardList, columnDisplay));

        Button createCardButton = getCreateCardButton(tableView);

        VBox actionButtons = new VBox(10, createBoardButton, deleteBoardButton, refreshButton, createCardButton);
        actionButtons.setPadding(new Insets(10));

        return actionButtons;
    }

    private Button getCreateCardButton(TableView<BoardEntity> tableView) {
        Button createCardButton = new Button("Criar Card");
        createCardButton.setOnAction(e -> {
            BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                new BoardAccordion().create(selectedBoard);
                new TableView<>();
                new VBox();// Certifique-se de inicializar o VBox
                createCard(selectedBoard);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Nenhum Board Selecionado");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, selecione um board para criar um card.");
                alert.showAndWait();
            }
        });
        return createCardButton;
    }

    private static BoardEntity getBoardEntity(String boardName, Connection connection) throws SQLException {
        var boardService = new BoardService();
        var boardColumnDAO = new BoardColumnDAO(connection);
        var newBoard = new BoardEntity();
        newBoard.setName(boardName);
        boardService.insert(newBoard);

        // Insere as colunas padrão para o novo board
        boardColumnDAO.insertDefaultColumns(newBoard.getId());
        // Associa as colunas recém inseridas ao board
        newBoard.setBoardColumns(boardColumnDAO.findByBoardId(newBoard.getId()));
        return newBoard;
    }

    /**
     * Cria um card no board selecionado
     */
    private void createCard(BoardEntity board) {
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Criar Card");
        titleDialog.setHeaderText(null);
        titleDialog.setContentText("Digite o título do novo card:");

        titleDialog.showAndWait().ifPresent(cardTitle -> {
            if (cardTitle.trim().isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Título Inválido", "O título do card não pode estar vazio.");
                return;
            }
            TextInputDialog descriptionDialog = new TextInputDialog();
            descriptionDialog.setTitle("Criar Card");
            descriptionDialog.setHeaderText(null);
            descriptionDialog.setContentText("Digite a descrição do novo card:");

            descriptionDialog.showAndWait().ifPresent(cardDescription -> {
                if (cardDescription.trim().isEmpty()) {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Descrição Inválida", "A descrição do card não pode estar vazia.");
                    return;
                }
                try {
                    cardController.createCard(board, cardTitle, cardDescription);
                    var updatedBoard = boardController.getBoardById(board.getId()).orElseThrow();
                    int index = boardList.indexOf(board);
                    if (index >= 0) {
                        boardList.set(index, updatedBoard);
                        tableView.refresh();
                    }
                    BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Card criado com sucesso!");
                } catch (Exception ex) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao criar o card: " + ex.getMessage());
                }
            });
        });
    }

    private void createBoard(TableView<BoardEntity> tableView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Criar Board");
        dialog.setHeaderText(null);
        dialog.setContentText("Digite o nome do novo board:");

        dialog.showAndWait().ifPresent(boardName -> {
            if (boardName.trim().isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Nome Inválido", "O nome do board não pode estar vazio.");
                return;
            }
            try {
                var newBoard = boardController.createBoard(boardName);
                boardList.add(newBoard);
                tableView.refresh();
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Board criado com sucesso!");
            } catch (SQLException ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao criar o board: " + ex.getMessage());
            }
        });
    }

    private void deleteSelectedBoard(TableView<BoardEntity> tableView) {
        BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
        if (selectedBoard != null) {
            try {
                boolean deleted = boardController.deleteBoard(selectedBoard);
                if (deleted) {
                    boardList.remove(selectedBoard);
                    tableView.refresh();
                    if (boardList.isEmpty()) columnDisplay.getChildren().clear();
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Board excluído com sucesso!");
                } else {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Erro", "O board não foi encontrado para exclusão.");
                }
            } catch (SQLException ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao excluir o board: " + ex.getMessage());
            }
        } else {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Nenhum Board Selecionado", "Por favor, selecione um board para excluir.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}