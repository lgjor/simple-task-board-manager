package org.desviante.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.desviante.integration.google.GoogleCalendarIntegration;

public class GoogleAuthController {
    @FXML
    private Button authButton;

    public static void setupGoogleAuthButton(Button button) {
        button.setOnAction(event -> {
            try {
                GoogleCalendarIntegration.authenticateUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void initialize() {
        authButton.setOnAction(event -> {
            System.out.println("Botão Google Auth clicado!");
            try {
                GoogleCalendarIntegration.authenticateUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}