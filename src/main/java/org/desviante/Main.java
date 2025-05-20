package org.desviante;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
import org.desviante.ui.components.BoardAccordion;
import org.desviante.ui.components.BoardTableComponent;
import org.desviante.controller.BoardController;
import org.desviante.controller.CardController;
import org.desviante.ui.BoardUIController;
import org.desviante.ui.CardUIController;
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

import static org.desviante.persistence.config.ConnectionConfig.getConnection;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();
    private BorderPane root;
    private VBox columnDisplay;
    private BoardController boardController;
    private CardController cardController;
    private TableView<BoardEntity> tableView;

    /**
     * Cria a tabela de boards
     */
    @Override
    public void start(Stage primaryStage) {
        try (Connection connection = getConnection()) {
            boardController = new BoardController();
            cardController = new CardController();

            // Inicialize columnDisplay antes de usar
            columnDisplay = new VBox();
            columnDisplay.setId("column-display");
            columnDisplay.setSpacing(6);
            columnDisplay.setPadding(new Insets(6, 0, 0, 0));

            tableView = BoardTableComponent.createBoardTable(boardList);
            BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);

            BoardEntity selectedBoard = null;
            if (!boardList.isEmpty()) {
                selectedBoard = boardList.get(0);
            }

            BoardTableComponent.loadBoardColumnsAndCards(selectedBoard, columnDisplay, tableView);

            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldBoard, newBoard) -> {
                if (newBoard != null) {
                    BoardTableComponent.loadBoardColumnsAndCards(newBoard, columnDisplay, tableView);
                } else {
                    columnDisplay.getChildren().clear();
                }
            });

            BoardUIController boardUIController = new BoardUIController(
                    boardController, cardController, boardList, tableView, columnDisplay
            );

            root = new BorderPane();
            root.setPadding(new Insets(10));
            root.setCenter(tableView);
            root.setRight(boardUIController.createActionButtons(tableView));
            root.setBottom(columnDisplay);

            Scene scene = new Scene(root, 1024, 800);

            var cssResource = getClass().getResource("/styles.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                logger.warn("Arquivo styles.css não encontrado.");
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Gerenciador de Boards");
            primaryStage.show();
        } catch (Exception e) {
            logger.error("Erro ao carregar o board", e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro de Conexão", "Não foi possível conectar ao banco de dados: " + e.getMessage());
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}