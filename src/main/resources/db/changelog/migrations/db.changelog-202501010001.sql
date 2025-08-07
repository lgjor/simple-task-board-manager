--liquibase formatted sql
--changeset desviante:202501010001
--comment: Add manual progress field for CARD type cards
ALTER TABLE cards ADD COLUMN manual_progress INT;
--rollback ALTER TABLE cards DROP COLUMN manual_progress; 