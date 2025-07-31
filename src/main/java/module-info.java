module org.desviante {
    // JavaFX modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    
    // Spring Boot modules (usando apenas os básicos)
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.jdbc;

    // Database
    requires java.sql;
    requires com.zaxxer.hikari;

    // Logging
    requires java.logging;
    requires org.slf4j;
    
    // Validation
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires org.jboss.logging;
    requires com.fasterxml.classmate;
    
    // Jackson for JSON processing
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    
    // Google API Client (usando apenas os básicos)
    requires google.api.client;
    requires com.google.auth.oauth2;
    
    // Micrometer for metrics
    requires micrometer.observation;
    
    // Lombok
    requires static lombok;
    
    // Reflection and other Java modules
    requires java.desktop;
    requires java.net.http;
    requires java.naming;

    // Javax
    requires java.xml;
    
    // Opens for reflection (necessary for Spring Boot)
    opens org.desviante to spring.core;
    opens org.desviante.config to spring.core;
    opens org.desviante.service to spring.core;
    opens org.desviante.repository to spring.core;
    opens org.desviante.model to spring.core;
    opens org.desviante.exception to spring.core;
    opens org.desviante.view to javafx.fxml;

    // Exports the main package
    exports org.desviante;
    exports org.desviante.config;
    exports org.desviante.service;
    exports org.desviante.repository;
    exports org.desviante.model;
    exports org.desviante.exception;
    exports org.desviante.view;
}