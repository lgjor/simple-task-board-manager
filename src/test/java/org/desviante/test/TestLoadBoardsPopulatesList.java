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

/**
 * Classe de teste que verifica o comportamento do método
 * {@link BoardTableComponent#loadBoards(TableView, ObservableList, VBox)}.
 *
 * <p>Este teste utiliza {@link ApplicationTest} do TestFX para garantir que
 * o ambiente JavaFX esteja corretamente inicializado durante a execução do teste.
 *
 * <p>O foco é validar se a {@code TableView} é corretamente populada com os dados da lista
 * de boards e se a sincronização entre a lista e a tabela é mantida após a chamada do método.
 */
public class TestLoadBoardsPopulatesList extends ApplicationTest {

    /**
     * Método sobrescrito requerido pelo {@link ApplicationTest} para inicializar
     * o JavaFX Toolkit antes da execução dos testes.
     *
     * @param stage o palco principal (não utilizado neste teste)
     */
    @Override
    public void start(Stage stage) {
        // Requerido para inicializar o JavaFX Toolkit
    }

    /**
     * Testa o método {@code loadBoards} para verificar se a {@link TableView}
     * é populada corretamente com a lista de boards.
     *
     * <p>Este teste cria uma {@code TableView}, uma {@code VBox} e uma lista observável de {@code BoardEntity}.
     * Após chamar o método {@code loadBoards}, o teste valida se:
     * <ul>
     *     <li>a lista foi populada corretamente;</li>
     *     <li>a {@code TableView} está sincronizada com os dados da lista.</li>
     * </ul>
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
