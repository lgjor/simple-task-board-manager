package org.desviante.ui;

import org.desviante.persistence.entity.BoardColumnKindEnum;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardQueryService;
import org.desviante.service.BoardService;
import org.desviante.ui.components.BoardAccordion;
import org.desviante.ui.components.BoardTableComponent;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.service.CardService;
import org.desviante.persistence.dao.BoardColumnDAO;
import org.desviante.persistence.entity.BoardColumnEntity;
import java.sql.Connection;
import org.desviante.ui.components.CardDragAndDropListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.desviante.ui.components.CardDragAndDrop;

import java.sql.SQLException;
import java.util.Objects;
import javafx.scene.layout.Priority;
import org.desviante.service.BoardStatusService;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.sql.Timestamp;

import static org.desviante.persistence.config.ConnectionConfig.getConnection;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();
    private BorderPane root;
    /**
     * Cria a tabela de boards
     */
    private TableView<BoardEntity> tableView = BoardTableComponent.createBoardTable(boardList);

    @Override
    public void start(Stage primaryStage) {
        try {
            Connection connection = getConnection();
            BoardColumnDAO boardColumnDAO = new BoardColumnDAO(connection);
            boardColumnDAO.setRefreshBoardCallback((cardId, tableView, columnDisplay) ->
                    BoardTableComponent.refreshBoardView(cardId, tableView, columnDisplay)
            );

            primaryStage.setTitle("Gerenciador de Boards");

            // Layout principal
            root = new BorderPane();
            root.setPadding(new Insets(10));

            // Botões de ação
            VBox actionButtons = createActionButtons(tableView);

            // Exibe as colunas do board selecionado
            VBox columnDisplay = new VBox();
            columnDisplay.setId("column-display");
            columnDisplay.setSpacing(10);

            // Removido para não criar múltiplas instâncias da tabela de boards tableView = BoardTableComponent.createBoardTable(boardList);
            // Carrega os boards na tabela
            BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);

            // Configurar o columnDisplay
            BorderPane.setMargin(columnDisplay, new Insets(10, 0, 0, 0));

            // Configurar o listener da tabela...
            BoardTableComponent.configureTableViewListener(tableView, columnDisplay);

            // Adiciona componentes ao layout principal
            root.setCenter(tableView);
            root.setRight(actionButtons);
            root.setBottom(columnDisplay);

            // Cena e exibição
            Scene scene = new Scene(root, 1024, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
            primaryStage.setScene(scene);

            // Método para carregar os boards
            BoardTableComponent.configureTableViewListener(tableView, columnDisplay);

            /**
             * Carrega os boards na tabela
             */
            BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);

            primaryStage.show();
        } catch (SQLException e) {
            logger.error("Erro ao carregar o board", e);
            showErrorAlert("Erro de Conexão", "Não foi possível conectar ao banco de dados: " + e.getMessage());
            Platform.exit();
        }
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
        refreshButton.setOnAction(e -> BoardTableComponent.loadBoards(tableView, boardList, (VBox) root.getBottom()));

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





    /**
    * Método para criar um novo board
    */
    private void createBoard(TableView<BoardEntity> tableView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Criar Board");
        dialog.setHeaderText(null);
        dialog.setContentText("Digite o nome do novo board:");

        dialog.showAndWait().ifPresent(boardName -> {
            if (boardName.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Nome Inválido");
                alert.setHeaderText(null);
                alert.setContentText("O nome do board não pode estar vazio.");
                alert.showAndWait();
                return;
            }

            try {
                Connection connection = getConnection();
                var newBoard = getBoardEntity(boardName, connection);

                boardList.add(newBoard);
                tableView.refresh();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText(null);
                alert.setContentText("Board criado com sucesso!");
                alert.showAndWait();
            } catch (SQLException ex) {
                logger.error("Erro ao criar o card", ex);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Erro ao criar o board: " + ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    private static BoardEntity getBoardEntity(String boardName, Connection connection) throws SQLException {
        var boardService = new BoardService(connection);
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
     * Exclui o board selecionado
     */
    private void deleteSelectedBoard(TableView<BoardEntity> tableView) {
        BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
        if (selectedBoard != null) {
            try {
                var connection = getConnection();
                var boardService = new BoardService(connection);
                boolean deleted = boardService.delete(selectedBoard.getId());
                if (deleted) {
                    boardList.remove(selectedBoard); // Remove o board da lista
                    tableView.refresh(); // Atualiza a tabela
                    // Limpa o painel de colunas se não houver mais boards
                    if (boardList.isEmpty()) {
                        VBox columnDisplay = (VBox) root.getBottom();
                        columnDisplay.getChildren().clear();
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("Board excluído com sucesso!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("O board não foi encontrado para exclusão.");
                    alert.showAndWait();
                }
            } catch (SQLException ex) {
                logger.error("Erro ao excluir o board", ex);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Erro ao excluir o board: " + ex.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Nenhum Board Selecionado");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecione um board para excluir.");
            alert.showAndWait();
        }
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
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Título Inválido");
                alert.setHeaderText(null);
                alert.setContentText("O título do card não pode estar vazio.");
                alert.showAndWait();
                return;
            }

            TextInputDialog descriptionDialog = new TextInputDialog();
            descriptionDialog.setTitle("Criar Card");
            descriptionDialog.setHeaderText(null);
            descriptionDialog.setContentText("Digite a descrição do novo card:");

            descriptionDialog.showAndWait().ifPresent(cardDescription -> {
                if (cardDescription.trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Descrição Inválida");
                    alert.setHeaderText(null);
                    alert.setContentText("A descrição do card não pode estar vazia.");
                    alert.showAndWait();
                    return;
                }

                try {
                    Connection connection = getConnection();
                    var cardService = new CardService(connection);
                    var queryService = new BoardQueryService(connection);

                    // Obtém o board atualizado com suas colunas já existentes
                    var optionalBoard = queryService.findById(board.getId());
                    if (optionalBoard.isEmpty() || optionalBoard.get().getBoardColumns().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText(null);
                        alert.setContentText("O board não possui colunas configuradas.");
                        alert.showAndWait();
                        return;
                    }

                    var fullBoard = optionalBoard.get();
                    // Busca a coluna do tipo INITIAL dinamicamente
                    BoardColumnEntity initialColumn = fullBoard.getBoardColumns()
                            .stream()
                            .filter(column -> column.getKind().equals(BoardColumnKindEnum.INITIAL))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Coluna INITIAL não encontrada."));

                    // Cria o novo card associando a coluna default
                    var newCard = new CardEntity();
                    newCard.setTitle(cardTitle);
                    newCard.setDescription(cardDescription);
                    newCard.setBoardColumn(initialColumn);

                    cardService.create(newCard);

                    // Atualiza a interface com o board atualizado
                    var updatedBoard = queryService.findById(board.getId()).orElseThrow();

                    // Atualiza a visualização
                    VBox updatedColumnDisplay = (VBox) root.getBottom();
                    updatedColumnDisplay.getChildren().clear();

                    // Cria as colunas com suporte a drag and drop
                    HBox boardColumns = new HBox(10); // Espaçamento de 10 entre as colunas
                    boardColumns.setPrefWidth(Double.MAX_VALUE); // Ocupa toda a largura disponível

                    // Fazer o HBox crescer para ocupar o espaço disponível
                    VBox.setVgrow(boardColumns, Priority.ALWAYS);

                    for (BoardColumnEntity column : updatedBoard.getBoardColumns()) {
                        VBox columnBox = BoardTableComponent.createColumnBoxWithDragDrop(column, updatedBoard, tableView, (VBox) root.getBottom());
                        boardColumns.getChildren().add(columnBox);
                    }

                    updatedColumnDisplay.getChildren().add(boardColumns);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("Card criado com sucesso!");
                    alert.showAndWait();
                } catch (SQLException ex) {
                    logger.error("Erro ao criar o card", ex);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("Erro ao criar o card: " + ex.getMessage());
                    alert.showAndWait();
                }
            });
        });
    }


    /**
     * Exibe um alerta de erro com um título e uma mensagem.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}