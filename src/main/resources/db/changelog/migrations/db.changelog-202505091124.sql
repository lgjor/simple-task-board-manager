CREATE TABLE boards_columns (
    id BIGINT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    kind VARCHAR(50) NOT NULL,
    `order` INT NOT NULL,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
);