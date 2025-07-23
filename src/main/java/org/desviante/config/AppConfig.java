package org.desviante.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {"org.desviante.service", "org.desviante.view"}) // Scan
@Import({DataConfig.class, GoogleApiConfig.class}) // Import the other configs
public class AppConfig {
    // This class is now much cleaner and composes the other configurations.
}