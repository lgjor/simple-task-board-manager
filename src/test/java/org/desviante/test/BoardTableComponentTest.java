import javafx.scene.control.TableView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.ui.components.BoardTableComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTableComponentTest {

    @BeforeAll
    static void initJFX() {
        // Inicializa o JavaFX Toolkit para evitar erros em ambiente de teste
        new JFXPanel();
    }

    @Test
    void testLoadBoardsPopulatesList() {
        TableView<BoardEntity> tableView = new TableView<>();
        VBox columnDisplay = new VBox();
        ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();

        BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);

        assertFalse(boardList.isEmpty(), "A lista de boards deve ser populada após o carregamento.");
        assertEquals(tableView.getItems(), boardList, "A TableView deve estar sincronizada com a lista de boards.");
    }
}