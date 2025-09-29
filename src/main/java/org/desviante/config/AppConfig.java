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
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see DataConfig
 * @see GoogleApiConfig
 * @see RetryConfig
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ComponentScan
 * @see org.springframework.context.annotation.Import
 */
@Configuration
@ComponentScan(basePackages = {
    "org.desviante.service", 
    "org.desviante.view", 
    "org.desviante.config",
    "org.desviante.integration",
    "org.desviante.calendar",
    "org.desviante.util"
})
@Import({DataConfig.class, GoogleApiConfig.class, AppMetadataConfig.class, RetryConfig.class})
public class AppConfig {
    /**
     * Construtor padrão da classe de configuração.
     * 
     * <p>Esta classe não requer inicialização especial, pois utiliza apenas
     * anotações Spring para configurar o contexto da aplicação.</p>
     */
    public AppConfig() {
        // Configuração automática via anotações Spring
    }
}