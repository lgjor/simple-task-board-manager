package org.desviante;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * A classe da UI do JavaFX, agora tratada como um simples Componente Spring.
 * Ela não controla mais o ciclo de vida do Spring.
 */
@Component // Opcional, mas boa prática para indicar que é um bean Spring.
public class MainApp extends Application {

    private ConfigurableApplicationContext springContext;

    /**
     * O método init() agora simplesmente obtém o contexto Spring
     * que já foi criado pela classe principal.
     */
    @Override
    public void init() {
        this.springContext = SimpleTaskBoardManagerApplication.getSpringContext();
    }

    /**
     * O método start() permanece o mesmo. Sua lógica para carregar o FXML
     * e usar a ControllerFactory do Spring já estava perfeita.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/board-view.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Simple Task Board Manager");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    /**
     * O método stop() também permanece o mesmo, garantindo que o contexto
     * do Spring seja fechado corretamente quando a UI fechar.
     */
    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}