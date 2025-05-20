module org.desviante {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires java.desktop; // Adiciona o módulo java.desktop para usar javax.swing
    requires liquibase.core;
    requires org.slf4j;
    requires org.xerial.sqlitejdbc;
    requires java.smartcardio;
    exports org.desviante;
    exports org.desviante.persistence.entity;
    exports org.desviante.persistence.dao;
    exports org.desviante.service;
    exports org.desviante.persistence.config;
    exports org.desviante.ui.components;
    exports org.desviante.ui;
    exports org.desviante.controller;
}