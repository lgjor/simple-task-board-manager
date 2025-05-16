module org.desviante {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires java.desktop; // Adiciona o módulo java.desktop para usar javax.swing
    requires liquibase.core;
    requires org.slf4j;
    exports org.desviante.persistence.entity;
    exports org.desviante.persistence.dao;
    exports org.desviante.ui;
    exports org.desviante.service;
    exports org.desviante.persistence.config;
}