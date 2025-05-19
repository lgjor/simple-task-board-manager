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
import org.desviante.service.BoardStatusService;

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

        TableColumn<BoardEntity, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());

        TableColumn<BoardEntity, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        // Coluna de status
        TableColumn<BoardEntity, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> {
            BoardEntity board = data.getValue();
            return new ReadOnlyStringWrapper(BoardStatusService.determineBoardStatus(board));
        });

        tableView.getColumns().setAll(List.of(idColumn, nameColumn, statusColumn));
        tableView.setItems(boardList);

        return tableView;
    }

    public static void configureTableViewListener(TableView<BoardEntity> tableView, VBox columnDisplay) {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try (var connection = getConnection()) {
                    var queryService = new BoardQueryService(connection);
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
                (tv) -> BoardTableComponent.loadBoards(tv, tableView.getItems(), columnDisplay)
        );


        // Cria o gerenciador de drag and drop com um novo listener
        CardDragAndDrop dragAndDrop = new CardDragAndDrop(listener);

        // Configura a área de cards e a coluna como alvos de drop
        dragAndDrop.setupDropTarget(cardsArea, column.getId());
        dragAndDrop.setupDropTarget(columnBox, column.getId());

        // Adiciona os cards à área de cards
        for (CardEntity card : column.getCards()) {
            VBox cardBox = CardTableComponent.createCardBox(card, tableView, columnDisplay);
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

    public static void loadBoards(TableView<BoardEntity> tableView, ObservableList<BoardEntity> boardList, VBox columnDisplay) {
        // Salva o ID do board selecionado antes de limpar a lista
        BoardEntity selectedBefore = tableView.getSelectionModel().getSelectedItem();
        Long selectedId = selectedBefore != null ? selectedBefore.getId() : null;
        // Limpa a lista de boards
        boardList.clear();

        try {
            var connection = getConnection();
            var queryService = new BoardQueryService(connection);
            var boards = queryService.findAll();

            // Para cada board, carregamos as colunas e os cards
            for (BoardEntity board : boards) {
                // Carrega as colunas do board com seus cards
                var optionalBoard = queryService.findById(board.getId());
                if (optionalBoard.isPresent()) {
                    BoardEntity fullBoard = optionalBoard.get();
                    board.setBoardColumns(fullBoard.getBoardColumns());

                    // Verifica se as colunas foram carregadas corretamente
                    System.out.println("Board carregado: " + board.getName() +
                            ", Colunas: " + (board.getBoardColumns() != null ?
                            board.getBoardColumns().size() : "null"));
                } else {
                    System.err.println("Não foi possível carregar o board completo: " + board.getId());
                }
            }

            boardList.addAll(boards);
            tableView.refresh();

            // Atualiza a visualização das colunas/cards do board selecionado
            if (selectedId != null) {
                for (BoardEntity board : boardList) {
                    if (board.getId().equals(selectedId)) {
                        tableView.getSelectionModel().select(board);
                        refreshBoardView(board.getId(), tableView, columnDisplay);;
                        return;
                    }
                }
            }

            // Se não houver seleção anterior, seleciona o primeiro (caso exista)
            if (!boardList.isEmpty()) {
                tableView.getSelectionModel().selectFirst();
                refreshBoardView(boardList.get(0).getId(), tableView, columnDisplay);
            }
        } catch (SQLException ex) {
            logger.error("Erro ao carregar boards", ex);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao carregar boards", "Ocorreu um erro ao carregar os boards: " + ex.getMessage());
        }
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

        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);  // Garante consistência da leitura

            // Busca o board atualizado
            BoardQueryService queryService = new BoardQueryService(connection);
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
                            columnDisplay.getChildren().add(boardColumns);

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

                connection.commit();
            } else {
                System.err.println("Board não encontrado: " + boardId);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar a visualização do board: " + e.getMessage());
            logger.error("Erro ao atualizar a visualização do board", e);
        }
    }
}
