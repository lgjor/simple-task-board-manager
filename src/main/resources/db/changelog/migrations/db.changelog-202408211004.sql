--liquibase formatted sql
--changeset junior:202408191938
--comment: cards table create

CREATE TABLE CARDS(
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    board_column_id BIGINT NOT NULL,
    CONSTRAINT boards_columns__cards_fk FOREIGN KEY (board_column_id) REFERENCES BOARDS_COLUMNS(id) ON DELETE CASCADE
);

--rollback DROP TABLE CARDS
