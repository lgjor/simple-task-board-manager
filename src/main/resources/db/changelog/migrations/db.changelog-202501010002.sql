--liquibase formatted sql
--changeset desviante:202501010002
--comment: Remove manual_progress field as it's no longer needed
ALTER TABLE cards DROP COLUMN manual_progress;
--rollback ALTER TABLE cards ADD COLUMN manual_progress INT; 