package org.desviante;

import org.desviante.persistence.entity.BoardEntity;

import org.desviante.ui.components.BoardTableComponent;
import org.desviante.controller.BoardController;
import org.desviante.controller.CardController;
import org.desviante.ui.BoardUIController;
import org.desviante.service.ProductionBoardService;
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

import org.desviante.util.JacksonFactoryKeepAlive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.stage.Stage;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();
    private VBox columnDisplay;
    private TableView<BoardEntity> tableView;

    /**
     * Cria a tabela de boards
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // --- 1. SETUP CONTROLLERS ---
            final BoardController boardController = new BoardController();
            final CardController cardController = new CardController();

            // --- 2. SETUP UI COMPONENTS ---
            columnDisplay = new VBox();
            columnDisplay.setId("column-display");
            columnDisplay.setSpacing(6);
            columnDisplay.setPadding(new Insets(6, 0, 0, 0));

            tableView = BoardTableComponent.createBoardTable(boardList);

            // --- 3. CONFIGURE LISTENERS ---
            // Delega a configuração do listener para o componente.
            // Isso garante que a lógica de carregar colunas ao selecionar um board
            // esteja encapsulada e seja robusta, pois re-busca os dados do banco.
            BoardTableComponent.configureTableViewListener(tableView, columnDisplay);

            // --- 4. SETUP MAIN LAYOUT ---
            BoardUIController boardUIController = new BoardUIController(
                    boardController, cardController, boardList, tableView, columnDisplay
            );

            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));
            root.setCenter(tableView);
            root.setRight(boardUIController.createActionButtons(tableView));
            root.setBottom(columnDisplay);

            // --- 5. INITIAL DATA LOAD ---
            // Carrega os boards do banco de dados na inicialização, usando a implementação de produção.
            // Isso irá popular a tabela e disparar o listener para selecionar o primeiro item.
            BoardTableComponent.loadBoards(tableView, boardList, columnDisplay, new ProductionBoardService());

            // --- 6. SETUP SCENE AND STAGE ---
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
            logger.error("Erro fatal ao iniciar a aplicação", e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro Crítico", "Ocorreu um erro ao iniciar a aplicação " + e.getMessage());
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1.2");
        JacksonFactoryKeepAlive.ensureUsed(); // força jlink a incluir jackson-core
        launch(args);
    }
}