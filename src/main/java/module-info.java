module br.com.dio {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires mysql.connector.j;
    requires java.desktop; // Adiciona o módulo java.desktop para usar javax.swing

    exports br.com.dio.persistence.entity;
    exports br.com.dio.persistence.dao;
    exports br.com.dio.ui;
    exports br.com.dio.service;
    exports br.com.dio.persistence.config;
}