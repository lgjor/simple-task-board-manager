package org.desviante.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuração principal da aplicação Spring Boot.
 *
 * <p>Esta classe centraliza a configuração da aplicação, definindo os componentes
 * que devem ser escaneados e importando outras configurações específicas. Segue
 * o princípio de responsabilidade única, focando apenas na composição de configurações.</p>
 *
 * <p>A classe utiliza anotações Spring para:</p>
 * <ul>
 *   <li><code>@Configuration</code>: Marca esta classe como uma fonte de definições de beans</li>
 *   <li><code>@ComponentScan</code>: Define os pacotes que devem ser escaneados para componentes</li>
 *   <li><code>@Import</code>: Importa outras configurações específicas</li>
 * </ul>
 *
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see DataConfig
 * @see GoogleApiConfig
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ComponentScan
 * @see org.springframework.context.annotation.Import
 */
@Configuration
@ComponentScan(basePackages = {"org.desviante.service", "org.desviante.view"}) // Scan
@Import({DataConfig.class, GoogleApiConfig.class}) // Import the other configs
public class AppConfig {
    // This class is now much cleaner and composes the other configurations.
}