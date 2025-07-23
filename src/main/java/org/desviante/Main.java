package org.desviante;

import javafx.application.Application;

/**
 * Esta é a classe de ponto de entrada principal para a aplicação.
 * Sua única responsabilidade é lançar a aplicação JavaFX (MainApp).
 */
public class Main {
    public static void main(String[] args) {
        // Este método inicia o ciclo de vida do JavaFX,
        // que por sua vez chamará os métodos init(), start() e stop() da nossa MainApp.
        Application.launch(MainApp.class, args);
    }
}