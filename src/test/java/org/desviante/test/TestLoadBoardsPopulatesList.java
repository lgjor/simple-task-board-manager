package org.desviante.test;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.ui.components.BoardTableComponent;
import org.desviante.service.BoardService;
import org.desviante.service.IBoardService;
import org.desviante.util.TestJPAUtil;
import org.desviante.util.TestDatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.testfx.framework.junit5.ApplicationTest;
import javafx.stage.Stage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private IBoardService boardService;
    private EntityManager entityManager;

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

    @BeforeEach
    void setUp() {
        // A SOLUÇÃO: Usamos o EntityManager de teste (H2) e o injetamos no serviço puro.
        // Isso garante que tanto a preparação dos dados quanto o método testado
        // operem no mesmo banco de dados em memória.
        this.entityManager = TestJPAUtil.createEntityManager();
        this.boardService = new BoardService(this.entityManager);

        // Garante que o banco de dados esteja limpo antes de cada teste, usando o mesmo
        // utilitário do BoardCreationTest para consistência.
        //TestDatabaseUtil.cleanDatabase(); // REMOVIDO: Esta linha causava o erro por tentar inicializar o JPAUtil de produção (SQLite).

        // CENÁRIO: Adiciona dados ao banco de dados para que o teste tenha o que carregar.
        // A transação é gerenciada manualmente no teste para um controle preciso.
        try {
            entityManager.getTransaction().begin();
            TestDatabaseUtil.cleanDatabase(this.entityManager);
            boardService.createBoardWithDefaultColumns("Board de Teste 1");
            boardService.createBoardWithDefaultColumns("Board de Teste 2");
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            fail("Falha ao preparar os dados no banco de teste H2.", e);
        }
    }

    /**
     * Testa o método {@code loadBoards} para verificar se a {@link TableView}
     * é populada corretamente com a lista de boards.
     *
     * <p>Este teste cria uma {@code TableView}, uma {@code VBox} e uma lista observável de {@code BoardEntity}.
     * Após chamar o método {@code loadBoards}, o teste valida se:
     * <ol>
     *     <li>A lista de boards foi populada com os dados do banco de dados.</li>
     *     <li>A TableView está sincronizada com os dados da lista.</li>
     * </ol>
     */
    @Test
    void testLoadBoardsPopulatesList() {
        // 1. ARRANGE: Prepara os componentes da UI
        TableView<BoardEntity> tableView = new TableView<>();
        VBox columnDisplay = new VBox();
        ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();

        // 2. ACT: Executa o método a ser testado.
        // Este método deve ler os boards criados no setUp() do banco de dados e popular a boardList.
        BoardTableComponent.loadBoards(tableView, boardList, columnDisplay, this.boardService);

        // 3. ASSERT: Verifica se o estado final está correto.
        assertFalse(boardList.isEmpty(), "A lista de boards não deveria estar vazia após a chamada do método.");
        assertEquals(2, boardList.size(), "A lista deveria conter os 2 boards criados no banco de dados.");
        assertEquals(tableView.getItems(), boardList, "A TableView deve estar sincronizada com a lista de boards.");
        assertTrue(boardList.stream().anyMatch(b -> "Board de Teste 1".equals(b.getName())), "O 'Board de Teste 1' deveria estar na lista.");
        assertTrue(boardList.stream().anyMatch(b -> "Board de Teste 2".equals(b.getName())), "O 'Board de Teste 2' deveria estar na lista.");
    }
}
