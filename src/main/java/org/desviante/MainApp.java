package org.desviante;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.desviante.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class MainApp extends Application {

    private ConfigurableApplicationContext springContext;

    /**
     * O método init() é chamado antes do start().
     * É o lugar perfeito para inicializar nosso contexto Spring.
     */
    @Override
    public void init() {
        // CORREÇÃO: Use AnnotationConfigApplicationContext para inicializar o Spring.
        springContext = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    /**
     * O método start() é o ponto de entrada principal para a UI.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carrega o arquivo FXML que define nossa UI.
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/board-view.fxml"));

        // AQUI ESTÁ A MÁGICA:
        // Dizemos ao FXMLLoader para usar o contexto do Spring para obter instâncias de controller.
        // Quando o FXML pedir o MainViewController, o Spring o fornecerá com a facade já injetada.
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Simple Task Board Manager");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    /**
     * O método stop() é chamado quando a aplicação fecha.
     * É crucial fechar o contexto do Spring para liberar recursos.
     */
    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}