--liquibase formatted sql
--changeset desviante:202501010003
--comment: Add card_types table to support user-defined card types
CREATE TABLE card_types (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    unit_label      VARCHAR(100) NOT NULL,
    creation_date   TIMESTAMP NOT NULL,
    last_update_date TIMESTAMP NOT NULL
);
--rollback DROP TABLE card_types; 