package org.desviante;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.desviante.util.WindowManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Classe principal da interface gráfica JavaFX para o Simple Task Board Manager.
 * 
 * <p>Esta classe estende {@link Application} do JavaFX e é responsável por inicializar
 * e gerenciar a interface gráfica principal da aplicação. Ela trabalha em conjunto
 * com o Spring Framework para gerenciar dependências e beans da aplicação.</p>
 * 
 * <p><strong>Responsabilidades Principais:</strong></p>
 * <ul>
 *   <li>Inicialização da interface gráfica JavaFX</li>
 *   <li>Carregamento da tela principal (board-view.fxml)</li>
 *   <li>Integração com o contexto Spring para injeção de dependências</li>
 *   <li>Gerenciamento do ciclo de vida da aplicação</li>
 *   <li>Coordenação do fechamento de janelas secundárias</li>
 * </ul>
 * 
 * <p><strong>Integração com Spring:</strong></p>
 * <p>A classe obtém o contexto Spring através de {@link SimpleTaskBoardManagerApplication}
 * e utiliza o {@link WindowManager} para gerenciar janelas secundárias da aplicação.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Application
 * @see SimpleTaskBoardManagerApplication
 * @see WindowManager
 * @see ConfigurableApplicationContext
 */
@Component
public class MainApp extends Application {

    private ConfigurableApplicationContext springContext;
    private WindowManager windowManager;
    
    /**
     * Construtor padrão da classe MainApp.
     * 
     * <p>Inicializa a aplicação JavaFX principal.
     * O contexto Spring será configurado no método init().</p>
     */
    public MainApp() {
        // Construtor padrão - contexto Spring será configurado no método init()
    }

    /**
     * Inicializa a aplicação JavaFX obtendo o contexto Spring.
     * 
     * <p>Este método é chamado automaticamente pelo JavaFX antes do método
     * {@link #start(Stage)}. Ele configura as dependências necessárias
     * para a interface gráfica funcionar corretamente.</p>
     * 
     * <p><strong>Comportamento:</strong></p>
     * <ul>
     *   <li>Obtém o contexto Spring da aplicação principal</li>
     *   <li>Configura o WindowManager para gerenciamento de janelas</li>
     * </ul>
     */
    @Override
    public void init() {
        this.springContext = SimpleTaskBoardManagerApplication.getSpringContext();
        this.windowManager = springContext.getBean(WindowManager.class);
    }

    /**
     * Inicializa e exibe a interface gráfica principal da aplicação.
     * 
     * <p>Este método é responsável por carregar o arquivo FXML principal,
     * configurar a janela principal e exibir a interface para o usuário.
     * A aplicação é iniciada maximizada para melhor aproveitamento da tela.</p>
     * 
     * <p><strong>Configurações da Janela:</strong></p>
     * <ul>
     *   <li>Título: "Simple Task Board Manager"</li>
     *   <li>Tamanho inicial: 1200x800 pixels</li>
     *   <li>Estado: Maximizada</li>
     *   <li>Listener de fechamento configurado</li>
     * </ul>
     * 
     * @param primaryStage palco principal da aplicação JavaFX
     * @throws Exception se houver erro ao carregar o FXML ou configurar a interface
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/board-view.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Simple Task Board Manager");
        primaryStage.setScene(new Scene(root, 1200, 800));
        
        // Iniciar a aplicação maximizada
        primaryStage.setMaximized(true);
        
        // Configurar listener para fechar todas as janelas secundárias quando a principal for fechada
        primaryStage.setOnCloseRequest(event -> {
            // Fechar todas as janelas secundárias antes de sair
            if (windowManager != null) {
                windowManager.closeAllSecondaryWindows();
            }
        });
        
        primaryStage.show();
    }

    /**
     * Finaliza a aplicação e libera recursos do Spring.
     * 
     * <p>Este método é chamado automaticamente quando a aplicação JavaFX
     * é fechada. Ele garante que todas as janelas secundárias sejam
     * fechadas adequadamente e que o contexto Spring seja encerrado.</p>
     * 
     * <p><strong>Comportamento:</strong></p>
 * <ul>
 *   <li>Fecha todas as janelas secundárias através do WindowManager</li>
 *   <li>Encerra o contexto Spring para liberar recursos</li>
 * </ul>
     */
    @Override
    public void stop() {
        // Fechar todas as janelas secundárias antes de encerrar
        if (windowManager != null) {
            windowManager.closeAllSecondaryWindows();
        }
        
        springContext.close();
    }
}