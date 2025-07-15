package org.desviante.ui.components;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.service.BoardService; // A única fonte de dados de Board
import org.desviante.service.BoardStatusService;
import org.desviante.util.AlertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;


public class BoardTableComponent {
    private static final Logger logger = LoggerFactory.getLogger(BoardTableComponent.class);

    // O método createBoardTable não precisa de alterações.
    public static TableView<BoardEntity> createBoardTable(ObservableList<BoardEntity> boardList) {
        TableView<BoardEntity> tableView = new TableView<>();
        tableView.setPlaceholder(new Label("Nenhum board disponível"));
        createBoardColumns(tableView, boardList);
        return tableView;
    }

    // O método createBoardColumns não precisa de alterações.
    public static void createBoardColumns(TableView<BoardEntity> tableView, ObservableList<BoardEntity> boardList){
        tableView.getColumns().clear();
        TableColumn<BoardEntity, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        TableColumn<BoardEntity, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(BoardStatusService.determineBoardStatus(data.getValue())));
        tableView.getColumns().addAll(nameColumn, statusColumn);
        BoardEntity boardWithColumns = boardList.stream()
                .filter(b -> b.getBoardColumns() != null && !b.getBoardColumns().isEmpty())
                .findFirst()
                .orElse(null);
        if (boardWithColumns != null) {
            // Ordena as colunas antes de exibi-las
            List<BoardColumnEntity> sortedColumns = boardWithColumns.getBoardColumns().stream()
                    .sorted((c1, c2) -> Integer.compare(c1.getOrder_index(), c2.getOrder_index()))
                    .collect(Collectors.toList());
            for (BoardColumnEntity col : sortedColumns) {
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

    // --- MÉTODO REFATORADO ---
    public static void configureTableViewListener(TableView<BoardEntity> tableView, VBox columnDisplay) {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    // Usa o BoardService para buscar a versão mais recente e completa do board.
                    BoardService boardService = new BoardService();
                    var refreshedBoardOptional = boardService.findById(newSelection.getId());

                    if (refreshedBoardOptional.isPresent()) {
                        BoardEntity refreshedBoard = refreshedBoardOptional.get();
                        // O método abaixo agora também usará o BoardService.
                        loadBoardColumnsAndCards(refreshedBoard, columnDisplay, tableView);
                    }
                } catch (Exception e) { // Captura Exception genérica, pois o serviço pode lançar RuntimeException.
                    logger.error("Erro ao carregar o board selecionado", e);
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao carregar", "Não foi possível carregar o board: " + e.getMessage());
                }
            }
        });
    }

    // --- createColumnBoxWithDragDrop permanece o mesmo, pois já delega para o listener ---
    public static VBox createColumnBoxWithDragDrop(BoardColumnEntity column, BoardEntity board, TableView<BoardEntity> tableView, VBox columnDisplay) {
        VBox columnBox = new VBox();
        columnBox.setId("column-" + column.getId());
        columnBox.setStyle("-fx-border-color: #CCCCCC; -fx-background-color: #F5F5F5; -fx-padding: 10; -fx-spacing: 5; -fx-border-radius: 5;");
        HBox.setHgrow(columnBox, Priority.ALWAYS);
        Label columnTitle = new Label(column.getName());
        columnTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        columnBox.getChildren().add(columnTitle);
        columnBox.getChildren().add(new Separator());
        VBox cardsArea = new VBox(5);
        cardsArea.setMinHeight(300);
        cardsArea.setStyle("-fx-padding: 5;");
        VBox.setVgrow(cardsArea, Priority.ALWAYS);
        ScrollPane scrollPane = new ScrollPane(cardsArea);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        columnBox.getChildren().add(scrollPane);
        CardDragAndDropListener listener = new CardDragAndDropListener(
                tableView,
                (tv) -> BoardTableComponent.loadBoards(tv, tableView.getItems(), columnDisplay),
                columnDisplay
        );
        CardDragAndDrop dragAndDrop = new CardDragAndDrop(listener);
        dragAndDrop.setupDropTarget(cardsArea, column.getId());
        dragAndDrop.setupDropTarget(columnBox, column.getId());
        for (CardEntity card : column.getCards()) {
            // TODO: O método createCardBox ainda precisa ser refatorado.
            VBox cardBox = CardTableComponent.createCardBox(card, tableView, columnDisplay, (tv) -> BoardTableComponent.loadBoards(tv, tableView.getItems(), columnDisplay), tableView);
            dragAndDrop.setupDragSource(cardBox, card.getId());
            cardsArea.getChildren().add(cardBox);
        }
        scrollPane.setVisible(!cardsArea.getChildren().isEmpty());
        return columnBox;
    }

    // --- MÉTODO REFATORADO ---
    // Este método já estava correto na sua versão anterior, apenas garantindo que ele permaneça.
    public static void loadBoards(TableView<BoardEntity> tableView,
                                  ObservableList<BoardEntity> boardList,
                                  VBox columnDisplay) {
        BoardEntity selectedBefore = tableView.getSelectionModel().getSelectedItem();
        Long selectedId = selectedBefore != null ? selectedBefore.getId() : null;
        boardList.clear();
        try {
            // Unificado para usar apenas o BoardService.
            BoardService boardService = new BoardService();
            List<BoardEntity> boards = boardService.findAllWithColumns();
            boardList.addAll(boards);
            createBoardColumns(tableView, boardList);
            tableView.refresh();
            BoardEntity toSelect = findBoardToSelect(boardList, selectedId);
            if (toSelect != null) {
                tableView.getSelectionModel().select(toSelect);
                // O listener da seleção (configureTableViewListener) cuidará de chamar o loadBoardColumnsAndCards.
            } else {
                columnDisplay.getChildren().clear();
            }
        } catch (Exception ex) {
            logger.error("Erro ao carregar a lista de boards", ex);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro de Carregamento", "Ocorreu um erro ao buscar os boards: " + ex.getMessage());
        }
    }

    // --- MÉTODO REFATORADO ---
    public static void loadBoardColumnsAndCards(BoardEntity board, VBox columnDisplay, TableView<BoardEntity> tableView) {
        columnDisplay.getChildren().clear();
        if (board == null) return;
        try {
            // Busca a versão mais recente e completa do board usando o serviço.
            BoardService boardService = new BoardService();
            BoardEntity fullBoard = boardService.findById(board.getId()).orElse(null);
            if (fullBoard == null || fullBoard.getBoardColumns() == null) return;

            HBox boardColumnsLayout = new HBox(16);
            boardColumnsLayout.setPrefWidth(Double.MAX_VALUE);

            // Ordena as colunas pela ordem definida antes de criar a UI
            List<BoardColumnEntity> sortedColumns = fullBoard.getBoardColumns().stream()
                    .sorted((c1, c2) -> Integer.compare(c1.getOrder_index(), c2.getOrder_index()))
                    .collect(Collectors.toList());

            for (var column : sortedColumns) {
                VBox columnBox = createColumnBoxWithDragDrop(column, fullBoard, tableView, columnDisplay);
                // Estilização do columnBox... (o código de estilo foi omitido para brevidade, mas deve ser mantido)
                boardColumnsLayout.getChildren().add(columnBox);
            }
            columnDisplay.getChildren().add(boardColumnsLayout);
        } catch (Exception e) {
            logger.error("Erro ao buscar detalhes do board completo", e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro ao Carregar Colunas", "Não foi possível carregar as colunas do board: " + e.getMessage());
        }
    }

    /**
     * Helper method to determine which board should be selected after a reload.
     * Tries to find the previously selected board by ID, otherwise defaults to the first in the list.
     */
    private static BoardEntity findBoardToSelect(List<BoardEntity> boardList, Long selectedId) {
        if (selectedId == null) {
            return boardList.isEmpty() ? null : boardList.get(0);
        }
        return boardList.stream()
                .filter(b -> selectedId.equals(b.getId()))
                .findFirst()
                .orElse(boardList.isEmpty() ? null : boardList.get(0));
    }

}
