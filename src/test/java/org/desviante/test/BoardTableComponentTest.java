package org.desviante.test;

import javafx.scene.control.TableView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.ui.components.BoardTableComponent;
import org.desviante.persistence.entity.BoardColumnEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTableComponentTest {

    @BeforeAll
    static void initJFX() {
        // Inicializa o JavaFX Toolkit para evitar erros em ambiente de teste
        new JFXPanel();
    }

    /**
     * Testa a criação de colunas fixas na TableView.
     * Verifica se as colunas "Nome" e "Status" são criadas corretamente.
     */
    @Test
    void testLoadBoardsPopulatesList() {
        TableView<BoardEntity> tableView = new TableView<>();
        VBox columnDisplay = new VBox();
        ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();

        BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);

        assertFalse(boardList.isEmpty(), "A lista de boards deve ser populada após o carregamento.");
        assertEquals(tableView.getItems(), boardList, "A TableView deve estar sincronizada com a lista de boards.");
    }

    /**
     * Testa a criação de colunas fixas na TableView.
     * Verifica se as colunas "Nome" e "Status" são criadas corretamente.
     */
    @Test
    void testCreateBoardColumnsAddsColumns() {
        TableView<BoardEntity> tableView = new TableView<>();
        ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();

        // Cria um board com colunas dinâmicas
        BoardEntity board = new BoardEntity();
        board.setName("Board Teste");
        // Supondo que BoardColumnEntity tenha setName e setId
        BoardColumnEntity col = new BoardColumnEntity();
        col.setName("Coluna Dinâmica");
        col.setId(1L);
        board.setBoardColumns(List.of(col));
        boardList.add(board);

        BoardTableComponent.createBoardColumns(tableView, boardList);

        // Deve conter as colunas fixas + dinâmicas
        assertEquals(3, tableView.getColumns().size());
        assertEquals("Nome", tableView.getColumns().get(0).getText());
        assertEquals("Status", tableView.getColumns().get(1).getText());
        assertEquals("Coluna Dinâmica", tableView.getColumns().get(2).getText());
    }
}