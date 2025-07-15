package org.desviante.ui;

import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.desviante.Main;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
import org.desviante.util.TestDatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.testfx.api.FxAssert.verifyThat;
import java.util.List;

/**
 * Teste de integração para a funcionalidade de criação de Boards.
 * Simula a interação do usuário e verifica a persistência no banco de dados.
 */
@ExtendWith(ApplicationExtension.class)
public class BoardCreationTest {

    // Instanciar o serviço como um campo evita recriá-lo a cada método.
    private final BoardService boardService = new BoardService();

    /**
     * O método @Start é executado pelo TestFX para iniciar a aplicação JavaFX.
     * Isso garante que a UI esteja pronta para ser testada.
     */
    @Start
    public void start(Stage stage) {
        Main mainApp = new Main();
        mainApp.start(stage);
    }

    /**
     * O método @BeforeEach é executado antes de cada teste.
     * Usamos isso para limpar o banco de dados e garantir que cada teste
     * comece com um ambiente limpo e previsível.
     */
    @BeforeEach
    void setup() {
        // Usa o novo utilitário para uma limpeza de banco de dados mais robusta e eficiente.
        TestDatabaseUtil.cleanDatabase();
    }

    /**
     * Testa o fluxo completo de criação de um board.
     * 1. Clica no botão "Criar Board".
     * 2. Digita o nome do board no diálogo.
     * 3. Confirma a criação.
     * 4. Verifica se o novo board aparece na tabela da UI.
     * 5. Verifica se o novo board foi salvo no banco de dados via Hibernate.
     *
     * @param robot O robô do TestFX que executa as interações do usuário.
     */
    @Test
    void should_create_board_and_display_it_in_the_table(FxRobot robot) {
        // --- Simulação do Usuário ---

        // 1. Clica no botão "Criar Board" usando o ID que definimos.
        robot.clickOn("#createBoardButton");

        // 2. TestFX encontra o diálogo que apareceu.
        //    Escreve o nome do novo board no campo de texto do diálogo.
        robot.write("Meu Novo Board de Teste");

        // 3. Clica no botão "OK" do diálogo.
        robot.clickOn("OK");

        // --- Verificações (Asserts) ---

        // 4. Verifica se a UI foi atualizada.
        //    Procuramos na TableView (com o ID que definimos) se existe uma linha
        //    contendo o nome do board que acabamos de criar.
        //    A verificação agora é mais específica e clara.
        verifyThat("#boardTableView", (TableView<BoardEntity> tv) -> {
            if (tv.getItems().size() != 1) return false;
            return "Meu Novo Board de Teste".equals(tv.getItems().get(0).getName());
        });

        // 5. Verifica se o board foi realmente persistido no banco de dados.
        //    Esta é a parte crucial do teste de integração com o Hibernate.
        //    Usamos nosso BoardService para consultar o banco.
        List<BoardEntity> boards = this.boardService.findAllWithColumns();

        // As asserções agora são mais explícitas, facilitando o debug se algo falhar.
        assertFalse(boards.isEmpty(), "A lista de boards não deveria estar vazia após a criação.");
        assertEquals(1, boards.size(), "Deveria haver exatamente um board no banco de dados.");
        assertEquals("Meu Novo Board de Teste", boards.get(0).getName(), "O nome do board salvo no banco está incorreto.");
    }
}