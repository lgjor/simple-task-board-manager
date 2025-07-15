package org.desviante.ui;

import jakarta.persistence.EntityManager;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.desviante.Main;
import org.desviante.persistence.entity.BoardEntity; // Mantido
import org.desviante.service.IBoardService;
import org.desviante.service.ProductionBoardService;
import org.desviante.util.TestDatabaseUtil;
import org.desviante.util.JPAUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

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

    // A SOLUÇÃO: Em vez de instanciar a classe de lógica pura, o teste agora
    // usa a interface IBoardService e a implementação de produção.
    // No contexto de teste, a ProductionBoardService se conectará automaticamente
    // ao banco de dados H2 em memória, graças à configuração em 'src/test/resources'.
    private final IBoardService boardService = new ProductionBoardService();

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
        // A SOLUÇÃO: O teste deve operar sobre o mesmo banco de dados que a aplicação utiliza.
        // Como este é um teste de UI ponta a ponta que inicia a aplicação Main,
        // a aplicação usará o JPAUtil de produção (que cria um banco SQLite).
        // Portanto, a limpeza do banco de dados no teste também deve usar o JPAUtil.
        // Isso garante que a preparação e a verificação do teste ocorram no mesmo banco de dados.
        try (EntityManager em = JPAUtil.getEntityManager()) {
            em.getTransaction().begin();
            TestDatabaseUtil.cleanDatabase(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            // Se a limpeza falhar, o teste deve falhar de forma explícita.
            throw new RuntimeException("Falha ao limpar o banco de dados no setup do teste BoardCreationTest", e);
        }
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
    void should_create_board_and_display_it_in_the_table(FxRobot robot) throws TimeoutException {
        // --- Simulação do Usuário ---

        // 1. Clica no botão "Criar Board" usando o ID que definimos.
        robot.clickOn("#createBoardButton");

        // 2. TestFX encontra o diálogo que apareceu.
        //    Escreve o nome do novo board no campo de texto do diálogo.
        robot.write("Meu Novo Board de Teste");
 
        // 3. Clica no botão "OK" do diálogo.
        robot.clickOn("OK");
 
        // 4. O aplicativo exibe um alerta de sucesso que é modal e bloqueia o teste.
        //    É necessário clicar no botão "OK" deste alerta para que o teste prossiga.
        robot.clickOn("OK");
 
        // --- Verificações (Asserts) ---   
        
        // 5. Usa uma abordagem mais robusta, esperando a UI se estabilizar antes de verificar.
        //    O WaitForAsyncUtils.waitForFxEvents() garante que todas as operações pendentes na
        //    thread da UI (como redesenhar a tabela) sejam concluídas.
        WaitForAsyncUtils.waitForFxEvents();

        // Adiciona uma espera explícita e robusta. O teste aguardará até 5 segundos
        // para que o nó #boardTableView esteja presente na cena, resolvendo a condição de corrida.
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> robot.lookup("#boardTableView").tryQuery().isPresent());


        // 6. Em vez de 'verifyThat', buscamos o nó e fazemos as asserções separadamente.
        //    Isso torna o teste mais legível e fornece mensagens de erro muito melhores.
        TableView<BoardEntity> tv = robot.lookup("#boardTableView").query();

        assertFalse(tv.getItems().isEmpty(), "A tabela não deveria estar vazia após a criação.");
        assertEquals(1, tv.getItems().size(), "A tabela deveria ter exatamente uma linha.");
        assertEquals("Meu Novo Board de Teste", tv.getItems().get(0).getName(), "O nome do board na tabela está incorreto.");

        // 7. Verifica se o board foi realmente persistido no banco de dados.
        //    Esta é a parte crucial do teste de integração com o Hibernate.
        //    Usamos nosso BoardService para consultar o banco.
        List<BoardEntity> boards = this.boardService.findAllWithColumns();

        // As asserções agora são mais explícitas, facilitando o debug se algo falhar.
        assertFalse(boards.isEmpty(), "A lista de boards não deveria estar vazia após a criação.");
        assertEquals(1, boards.size(), "Deveria haver exatamente um board no banco de dados.");
        assertEquals("Meu Novo Board de Teste", boards.get(0).getName(), "O nome do board salvo no banco está incorreto.");
    }
}