ALTER TABLE cards
    RENAME TO cards_old;

CREATE TABLE cards (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    board_column_id INTEGER NOT NULL,
    blocked INTEGER DEFAULT 0,
    block_reason TEXT,
    unblock_reason TEXT,
    creation_date DATETIME DEFAULT (datetime('now')),
    last_update_date DATETIME,
    completion_date DATETIME,
    CONSTRAINT fk_board_column FOREIGN KEY (board_column_id) REFERENCES boards_columns(id) ON DELETE CASCADE
);

INSERT INTO cards (id, title, description, board_column_id, blocked, block_reason, unblock_reason, creation_date, last_update_date, completion_date)
SELECT id, title, description, board_column_id, blocked, block_reason, unblock_reason, creation_date, last_update_date, completion_date
FROM cards_old;

DROP TABLE cards_old;