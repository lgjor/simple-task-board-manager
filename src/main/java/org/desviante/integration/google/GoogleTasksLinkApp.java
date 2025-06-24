package org.desviante.integration.google;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URI;

public class GoogleTasksLinkApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button googleTaskLink = new Button("Google Task");

        googleTaskLink.setOnAction(event -> abrirGoogleTasks());

        StackPane root = new StackPane(googleTaskLink);
        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("Abrir Google Tasks");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void abrirGoogleTasks() {
        String url = "https://tasks.google.com/tasks/";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.err.println("Abertura de link não suportada no sistema.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}