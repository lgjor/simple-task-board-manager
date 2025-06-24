module org.desviante.board {
    requires javafx.controls;
    requires javafx.fxml;
    requires liquibase.core;
    requires org.slf4j;
    requires org.xerial.sqlitejdbc;
    requires java.smartcardio;
    requires java.sql;
    requires java.desktop;

    // google
    requires com.google.api.services.tasks;
    requires java.base;
    requires com.google.api.client.json.jackson2;
    requires com.google.api.client.auth;
    requires com.google.api.client;
    requires static lombok;

    exports org.desviante;
    exports org.desviante.persistence.entity;
    exports org.desviante.persistence.dao;
    exports org.desviante.service;
    exports org.desviante.persistence.config;
    exports org.desviante.ui.components;
    exports org.desviante.ui;
    exports org.desviante.controller;
}