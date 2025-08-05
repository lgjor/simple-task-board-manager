package org.desviante;

import javafx.application.Application;
import lombok.Getter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Esta é a ÚNICA classe de inicialização da aplicação.
 * Ela assume o controle total, iniciando o Spring e, em seguida,
 * lançando a interface gráfica do JavaFX.
 */
@SpringBootApplication
public class SimpleTaskBoardManagerApplication {

    /**
     * -- GETTER --
     *  Um método helper estático para que a classe MainApp possa obter o contexto
     *  do Spring que foi inicializado aqui.
     *
     * @return O contexto da aplicação Spring.
     */
    @Getter
    private static ConfigurableApplicationContext springContext;

    /**
     * O único e verdadeiro método main() da nossa aplicação.
     * @param args Argumentos da linha de comando.
     */
    public static void main(String[] args) {
        // A mágica acontece aqui:
        // 1. O SpringApplication.run() é chamado de forma NÃO-BLOQUEANTE.
        // 2. Ele retorna o contexto da aplicação totalmente inicializado.
        // 3. Nós o armazenamos para que a classe JavaFX possa usá-lo.
        //
        // NOTA: Usamos um inicializador (builder) para garantir que a aplicação
        // não seja "headless" (sem interface gráfica), o que é importante para o JavaFX.
        springContext = new org.springframework.boot.builder.SpringApplicationBuilder(SimpleTaskBoardManagerApplication.class)
                .headless(false)
                .run(args);

        // Agora que o Spring está pronto, lançamos a aplicação JavaFX.
        Application.launch(MainApp.class, args);
    }

}