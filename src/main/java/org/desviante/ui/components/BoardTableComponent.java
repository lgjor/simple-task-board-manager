package org.desviante.ui.components;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.service.BoardQueryService;
import org.desviante.service.BoardService;
import org.desviante.service.BoardStatusService;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.desviante.util.AlertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.desviante.persistence.config.ConnectionConfig.getConnection;

public class BoardTableComponent {
    private static final Logger logger = LoggerFactory.getLogger(BoardTableComponent.class);

    public static TableView<BoardEntity> createBoardTable(ObservableList<BoardEntity> boardList) {
        TableView<BoardEntity> tableView = new TableView<>();
        tableView.setPlaceholder(new Label("Nenhum board disponível"));

        createBoardColumns(tableView, boardList);

        return tableView;
    }

    /**
    *
    * Cria as colunas fixas e dinâmicas do board
    * @param tableView A TableView onde as colunas serão adicionadas
    * @param boardList A lista de boards que será exibida na TableView
    */
    public static void createBoardColumns(TableView<BoardEntity> tableView, ObservableList<BoardEntity> boardList){
        tableView.getColumns().clear();
        // Criação das colunas fixas do board
        // Coluna ID
        //TableColumn<BoardEntity, Long> idColumn = new TableColumn<>("ID");
        //idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());

        // Coluna nome
        TableColumn<BoardEntity, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        // Coluna status
        TableColumn<BoardEntity, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> {
            BoardEntity board = data.getValue();
            return new ReadOnlyStringWrapper(BoardStatusService.determineBoardStatus(board));
        });

        // Adiciona as colunas fixas (removida a coluna idColumn)
        tableView.getColumns().addAll(nameColumn, statusColumn);

        // Criação das colunas dinâmicas do board
        // Busca o primeiro board que tenha colunas
        BoardEntity boardWithColumns = boardList.stream()
                .filter(b -> b.getBoardColumns() != null && !b.getBoardColumns().isEmpty())
                .findFirst()
                .orElse(null);

        if (boardWithColumns != null) {
            for (BoardColumnEntity col : boardWithColumns.getBoardColumns()) {
                TableColumn<BoardEntity, String> dynamicCol = new TableColumn<>(col.getName());
                dynamicCol.setCellValueFactory(cellData -> {
                    double percent = cellData.getValue().getPercentage(col.getName());
                    return new SimpleStringProperty(String.format("%.1f%%", percent));
                });
                tableView.getColumns().add(dynamicCol);
            }
        }

        tableView.setItems(boardList);
    }

    public static void configureTableViewListener(TableView<BoardEntity> tableView, VBox columnDisplay) {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try (var connection = getConnection()) {
                    var queryService = new BoardQueryService();
                    // Busca as informações atualizadas do board no banco
                    var refreshedBoardOptional = queryService.findById(newSelection.getId());
                    if (refreshedBoardOptional.isPresent()) {
                        var refreshedBoard = refreshedBoardOptional.get();

                        // Atualiza a visualização vertical dos cards com drag and drop
                        columnDisplay.getChildren().clear();

                        // Cria as colunas com suporte a drag and drop
                        HBox boardColumns = new HBox(10);
                        boardColumns.setPrefWidth(Double.MAX_VALUE);

                        // Fazer o HBox crescer para ocupar o espaço disponível
                        VBox.setVgrow(boardColumns, Priority.ALWAYS);

                        for (BoardColumnEntity column : refreshedBoard.getBoardColumns()) {
                            VBox columnBox = createColumnBoxWithDragDrop(column, refreshedBoard, tableView, columnDisplay);
                            boardColumns.getChildren().add(columnBox);
                        }

                        columnDisplay.getChildren().add(boardColumns);
                    }
                } catch (Exception e) {
                    logger.error("Erro ao carregar o board", e);
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao carregar", "Não foi possível carregar board " + e.getMessage());
                }
            }
        });
    }

    /**
     * Cria uma caixa de cards com suporte a drag and drop
     */
    public static VBox createColumnBoxWithDragDrop(BoardColumnEntity column, BoardEntity board, TableView<BoardEntity> tableView, VBox columnDisplay) {
        VBox columnBox = new VBox();
        columnBox.setId("column-" + column.getId());

        // Configurações de estilo e layout...
        columnBox.setStyle("-fx-border-color: #CCCCCC; -fx-background-color: #F5F5F5; -fx-padding: 10; -fx-spacing: 5; -fx-border-radius: 5;");
        HBox.setHgrow(columnBox, Priority.ALWAYS);

        // Título da coluna
        Label columnTitle = new Label(column.getName());
        columnTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        columnBox.getChildren().add(columnTitle);

        // Separador
        Separator separator = new Separator();
        columnBox.getChildren().add(separator);

        // Área para cards
        VBox cardsArea = new VBox();
        cardsArea.setSpacing(5);
        cardsArea.setMinHeight(300);
        cardsArea.setStyle("-fx-padding: 5;");
        VBox.setVgrow(cardsArea, Priority.ALWAYS);

        // Envolve o cardsArea em um ScrollPane para exibir barra de rolagem quando necessário
        ScrollPane scrollPane = new ScrollPane(cardsArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setPrefViewportHeight(300); // Altura fixa da viewport
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        columnBox.getChildren().add(scrollPane);

        CardDragAndDropListener listener = new CardDragAndDropListener(
                tableView,
                (tv) -> BoardTableComponent.loadBoards(tv, tableView.getItems(), columnDisplay),
                columnDisplay
        );


        // Cria o gerenciador de drag and drop com um novo listener
        CardDragAndDrop dragAndDrop = new CardDragAndDrop(listener);

        // Configura a área de cards e a coluna como alvos de drop
        dragAndDrop.setupDropTarget(cardsArea, column.getId());
        dragAndDrop.setupDropTarget(columnBox, column.getId());

        // Adiciona os cards à área de cards
        for (CardEntity card : column.getCards()) {
            VBox cardBox = CardTableComponent.createCardBox(
                    card,
                    tableView,
                    columnDisplay,
                    (tv) -> BoardTableComponent.loadBoards(tv, tableView.getItems(), columnDisplay),
                    tableView
            );
            dragAndDrop.setupDragSource(cardBox, card.getId());
            cardsArea.getChildren().add(cardBox);
        }

        // Se não houver cards, não exibe o scrollPane
        if (cardsArea.getChildren().isEmpty()) {
            scrollPane.setVisible(false);
        } else {
            scrollPane.setVisible(true);
        }

        return columnBox;
    }

    public static void loadBoards(TableView<BoardEntity> tableView,
                                  ObservableList<BoardEntity> boardList,
                                  VBox columnDisplay) {
        BoardEntity selectedBefore = tableView.getSelectionModel().getSelectedItem();
        Long selectedId = selectedBefore != null ? selectedBefore.getId() : null;
        boardList.clear();

        try {
            List<BoardEntity> boards = BoardService.loadBoardsFromDatabase();
            boardList.addAll(boards);

            createBoardColumns(tableView, boardList);
            tableView.refresh();

            BoardEntity toSelect = null;
            if (selectedId != null) {
                for (BoardEntity board : boardList) {
                    if (board.getId().equals(selectedId)) {
                        toSelect = board;
                        break;
                    }
                }
            }
            if (toSelect == null && !boardList.isEmpty()) {
                toSelect = boardList.get(0);
            }
            if (toSelect != null) {
                tableView.getSelectionModel().select(toSelect);
                loadBoardColumnsAndCards(toSelect, columnDisplay, tableView);
            } else {
                columnDisplay.getChildren().clear();
            }

        } catch (SQLException ex) {
            logger.error("Erro ao carregar boards", ex);
            if (Platform.isFxApplicationThread()) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao carregar boards",
                        "Ocorreu um erro ao carregar os boards: " + ex.getMessage());
            }
        }
    }

//    public static void loadBoards(TableView<BoardEntity> tableView, ObservableList<BoardEntity> boardList, VBox columnDisplay) {
//        BoardEntity selectedBefore = tableView.getSelectionModel().getSelectedItem();
//        Long selectedId = selectedBefore != null ? selectedBefore.getId() : null;
//        boardList.clear();
//
//        try (Connection connection = getConnection()) {
//            BoardQueryService queryService = new BoardQueryService();
//            List<BoardEntity> boards = queryService.findAll();
//
//            // Carrega as colunas e cards para cada board
//            for (BoardEntity board : boards) {
//                var optionalBoard = queryService.findById(board.getId());
//                optionalBoard.ifPresent(fullBoard -> board.setBoardColumns(fullBoard.getBoardColumns()));
//            }
//
//            boardList.addAll(boards);
//            //tableView.setItems(boardList);
//            createBoardColumns(tableView, boardList);
//            tableView.refresh();
//
//            // Seleciona o board anterior ou o primeiro
//            BoardEntity toSelect = null;
//            if (selectedId != null) {
//                for (BoardEntity board : boardList) {
//                    if (board.getId().equals(selectedId)) {
//                        toSelect = board;
//                        break;
//                    }
//                }
//            }
//            if (toSelect == null && !boardList.isEmpty()) {
//                toSelect = boardList.get(0);
//            }
//            if (toSelect != null) {
//                tableView.getSelectionModel().select(toSelect);
//                BoardTableComponent.loadBoardColumnsAndCards(toSelect, columnDisplay, tableView);
//            } else {
//                columnDisplay.getChildren().clear();
//            }
//        } catch (SQLException ex) {
//            logger.error("Erro ao carregar boards", ex);
//            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao carregar boards", "Ocorreu um erro ao carregar os boards: " + ex.getMessage());
//        }
//    }

    public static void loadBoardColumnsAndCards(BoardEntity board, VBox columnDisplay, TableView<BoardEntity> tableView) {
        columnDisplay.getChildren().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
        if (board == null) return;

        BoardEntity fullBoard = null;
        try {
            fullBoard = new BoardQueryService().findById(board.getId()).orElse(null);
        } catch (SQLException e) {
            logger.error("Erro ao buscar board completo", e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao carregar colunas", "Não foi possível carregar as colunas do board: " + e.getMessage());
            return;
        }
        if (fullBoard == null || fullBoard.getBoardColumns() == null) return;

        HBox boardColumns = new HBox(16);
        boardColumns.setPrefWidth(Double.MAX_VALUE);

        try (Connection connection = getConnection()) {
            for (var column : fullBoard.getBoardColumns()) {
                VBox columnBox = createColumnBoxWithDragDrop(
                        column, fullBoard, tableView, columnDisplay
                );

                // Ajuste visual do columnBox
                columnBox.setStyle(
                        "-fx-border-color: #cccccc; " +
                                "-fx-background-color: #f5f5f5; " +
                                "-fx-padding: 8; " +
                                "-fx-border-radius: 8;"
                );
                columnBox.setMinWidth(260);
                columnBox.setPrefWidth(320);
                columnBox.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(columnBox, javafx.scene.layout.Priority.ALWAYS);

                // Ajuste visual do título da coluna
                if (!columnBox.getChildren().isEmpty() && columnBox.getChildren().get(0) instanceof Label) {
                    Label columnTitle = (Label) columnBox.getChildren().get(0);
                    columnTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 0 2 0;");
                }

                // Ajuste visual dos cards
                if (columnBox.getChildren().size() > 2 && columnBox.getChildren().get(2) instanceof ScrollPane) {
                    ScrollPane scrollPane = (ScrollPane) columnBox.getChildren().get(2);
                    if (scrollPane.getContent() instanceof VBox) {
                        VBox cardsArea = (VBox) scrollPane.getContent();
                        for (var node : cardsArea.getChildren()) {
                            if (node instanceof VBox cardBox && !cardBox.getChildren().isEmpty()) {
                                // Ajuste visual do título do card
                                if (cardBox.getChildren().get(0) instanceof Label titleLabel) {
                                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 0 0 2 0;");
                                }
                                cardBox.setStyle(
                                        "-fx-background-color: #fff; " +
                                                "-fx-padding: 4; " +
                                                "-fx-border-radius: 6; " +
                                                "-fx-border-color: #e0e0e0; " +
                                                "-fx-effect: dropshadow(gaussian, #ddd, 2, 0, 0, 1);"
                                );
                                cardBox.setMinWidth(180);
                                cardBox.setPrefWidth(220);
                                cardBox.setMaxWidth(Double.MAX_VALUE);
                            }
                        }
                    }
                }

                boardColumns.getChildren().add(columnBox);
            }
        } catch (SQLException e) {
            logger.error("Erro ao criar colunas com drag and drop", e);
        }

        columnDisplay.getChildren().add(boardColumns);
    }

    /**
     * Atualiza a visualização do board com o ID especificado
     */
    public static void refreshBoardView(Long boardId, TableView<BoardEntity> tableView, VBox columnDisplay) {
        // Pequeno delay para garantir que a transação foi finalizada
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);  // Garante consistência da leitura

            // Busca o board atualizado
            BoardQueryService queryService = new BoardQueryService();
            var boardOptional = queryService.findById(boardId);

            if (boardOptional.isPresent()) {
                BoardEntity refreshedBoard = boardOptional.get();

                // Garante que a atualização da UI ocorra na thread do JavaFX
                Platform.runLater(() -> {
                    // Obtém a área de exibição das colunas
                    if (columnDisplay != null) {
                        columnDisplay.getChildren().clear();

                        // Cria as colunas com suporte a drag and drop
                        HBox boardColumns = new HBox(10);
                        boardColumns.setPrefWidth(Double.MAX_VALUE);
                        VBox.setVgrow(boardColumns, Priority.ALWAYS);

                        try {
                            // Nova conexão para operações dentro do Platform.runLater
                            getConnection();
                            for (BoardColumnEntity column : refreshedBoard.getBoardColumns()) {
                                VBox columnBox = createColumnBoxWithDragDrop(column, refreshedBoard, tableView, columnDisplay);
                                boardColumns.getChildren().add(columnBox);
                            }

                            // Força atualização da TableView
                            tableView.refresh();

                            System.out.println("Visualização do board atualizada com sucesso");
                        } catch (SQLException e) {
                            System.err.println("Erro ao criar colunas: " + e.getMessage());
                            logger.error("Erro ao criar colunas", e);
                        }
                    } else {
                        System.err.println("Área de exibição das colunas não encontrada");
                    }
                });
                // teste
                //connection.commit();
            } else {
                System.err.println("Board não encontrado: " + boardId);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar a visualização do board: " + e.getMessage());
            logger.error("Erro ao atualizar a visualização do board", e);
        }
    }

}
