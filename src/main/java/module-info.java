/**
 * Define o módulo principal da aplicação "Simple Task Board Manager".
 */
module org.desviante {
    // --- Módulos do JavaFX ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    // Adicionado para resolver o erro de "package is not visible"
    requires org.checkerframework.checker.qual;

    // Adicionado para que o compilador reconheça as anotações do Lombok em tempo de compilação
    requires static lombok;

    // --- Módulos do Hibernate e Jakarta Persistence ---
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires jakarta.transaction;
    // Adicionados para satisfazer as dependências opcionais do Hibernate,
    // tornando a arquitetura do módulo explícita e robusta.
    requires jakarta.xml.bind;
    requires jakarta.annotation;
    requires jakarta.cdi;

    // --- Módulos de Logging ---
    requires org.slf4j;

    // --- Módulos de Conectividade e Banco de Dados ---
    requires java.sql; // Fornece a API JDBC

    // --- Módulos da API do Google ---
    requires com.google.api.client;
    requires com.google.api.services.tasks;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    
    // --- Abre pacotes para reflexão e FXML ---
    // Necessário para que frameworks como JavaFX, Hibernate e outros possam acessar
    // classes e membros privados via reflexão. Abrir o pacote sem um 'to' (abertura
    // incondicional) o torna acessível a qualquer módulo no classpath, o que é uma
    // solução robusta para erros de acesso ilegal (`IllegalAccessError`) em tempo de execução.
    opens org.desviante.controller;
    opens org.desviante.persistence.entity;
    exports org.desviante;
}