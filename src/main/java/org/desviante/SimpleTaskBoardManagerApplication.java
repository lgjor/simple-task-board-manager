package org.desviante;

import javafx.application.Application;
import lombok.Getter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Classe principal de inicialização da aplicação Simple Task Board Manager.
 * 
 * <p>Esta é a classe responsável por inicializar todo o sistema, incluindo
 * o contexto Spring e a interface gráfica JavaFX. Ela implementa uma estratégia
 * de inicialização não-bloqueante que permite que o Spring seja inicializado
 * antes do JavaFX.</p>
 * 
 * <p><strong>Responsabilidades Principais:</strong></p>
 * <ul>
 *   <li>Inicialização do contexto Spring Boot</li>
 *   <li>Configuração da aplicação como não-headless para suporte ao JavaFX</li>
 *   <li>Lançamento da interface gráfica JavaFX após inicialização do Spring</li>
 *   <li>Fornecimento de acesso ao contexto Spring para outras classes</li>
 * </ul>
 * 
 * <p><strong>Estratégia de Inicialização:</strong></p>
 * <p>A aplicação utiliza o SpringApplicationBuilder para inicializar
 * o Spring de forma não-bloqueante, garantindo que todos os beans e configurações
 * estejam prontos antes de lançar a interface gráfica. Isso evita problemas
 * de timing e garante que a injeção de dependências funcione corretamente.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see SpringBootApplication
 * @see Application
 * @see MainApp
 * @see ConfigurableApplicationContext
 */
@SpringBootApplication
public class SimpleTaskBoardManagerApplication {

    /**
     * Contexto da aplicação Spring compartilhado entre as classes.
     * 
     * <p>Este campo estático armazena o contexto Spring após a inicialização,
     * permitindo que outras classes (como {@link MainApp}) acessem o contexto
     * para obter beans e configurações.</p>
     * 
     * <p><strong>Uso:</strong></p>
     * <ul>
     *   <li>Inicializado no método {@link #main(String[])}</li>
     *   <li>Acessado através do método getter gerado pelo Lombok</li>
     *   <li>Utilizado por {@link MainApp} para injeção de dependências</li>
     * </ul>
     * 
     * @see ConfigurableApplicationContext
     * @see MainApp
     */
    @Getter
    private static ConfigurableApplicationContext springContext;
    
    /**
     * Construtor padrão da classe SimpleTaskBoardManagerApplication.
     * 
     * <p>Esta classe não requer inicialização especial.</p>
     */
    public SimpleTaskBoardManagerApplication() {
        // Construtor padrão - inicialização via método main()
    }

    /**
     * Método principal de entrada da aplicação.
     * 
     * <p>Este método implementa a sequência de inicialização da aplicação:</p>
     * <ol>
     *   <li>Inicializa o contexto Spring Boot de forma não-bloqueante</li>
     *   <li>Configura a aplicação para suportar interface gráfica (não-headless)</li>
     *   <li>Armazena o contexto Spring para uso posterior</li>
     *   <li>Lança a aplicação JavaFX através de {@link MainApp}</li>
     * </ol>
     * 
     * <p><strong>Configurações Especiais:</strong></p>
     * <ul>
     *   <li><code>headless(false)</code>: Garante que a aplicação suporte interface gráfica</li>
     *   <li>Inicialização não-bloqueante: Spring é inicializado antes do JavaFX</li>
     *   <li>Compartilhamento de contexto: MainApp pode acessar beans Spring</li>
     * </ul>
     * 
     * @param args Argumentos da linha de comando passados para a aplicação
     * @see MainApp
     * @see ConfigurableApplicationContext
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