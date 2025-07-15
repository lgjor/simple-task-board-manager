module org.desviante.board {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires org.xerial.sqlitejdbc;
    requires java.smartcardio;
    requires java.sql;
    requires java.desktop;
    requires jakarta.persistence;
    requires static lombok;

    // google
    requires com.google.api.services.tasks;
    requires com.google.api.client.json.jackson2;
    requires com.google.api.client.auth;
    requires com.google.api.client;

    // Esta dependência é para uma ferramenta de verificação estática, pode ser mantida.
    requires org.checkerframework.checker.qual;

    exports org.desviante;
    exports org.desviante.persistence.entity;
    exports org.desviante.service;
    exports org.desviante.ui.components;
    exports org.desviante.ui;
    exports org.desviante.controller;
    // Exportando o pacote de utilitários, que contém JPAUtil, etc.
    exports org.desviante.util;
}