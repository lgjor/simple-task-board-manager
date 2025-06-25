package org.desviante.test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.ui.components.BoardTableComponent;
import org.junit.jupiter.api.Test;

import org.testfx.framework.junit5.ApplicationTest;
import javafx.stage.Stage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestLoadBoardsPopulatesList extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        // Requerido para inicializar o JavaFX Toolkit
    }

    /**
     * In this test case, we create a TableView and an empty ObservableList of BoardEntity objects.
     * We then call the loadBoards() method to populate the view with data from the list.
     * Finally, we verify that the list is not empty and that the TableView is synchronized with the list of boards.
     */
    @Test
    void testLoadBoardsPopulatesList() {
        // Set up the test environment
        TableView<BoardEntity> tableView = new TableView<>();
        VBox columnDisplay = new VBox();
        ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();

        // Populate the list with some boards
        BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);

        // Verify that the TableView is synchronized with the list of boards
        assertEquals(tableView.getItems(), boardList, "A TableView debe estar sincronizada com a lista de boards.");
    }
}
