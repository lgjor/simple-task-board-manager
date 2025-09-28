--liquibase formatted sql
--changeset junior:202408191938
--comment: boards_columns table create
-- Compatible with H2 Database

CREATE TABLE BOARDS_COLUMNS(
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    `order` int NOT NULL,
    kind VARCHAR(7) NOT NULL,
    board_id BIGINT NOT NULL,
    FOREIGN KEY (board_id) REFERENCES BOARDS(id) ON DELETE CASCADE,
    UNIQUE (board_id, `order`)
);

--rollback DROP TABLE BOARDS_COLUMNS
