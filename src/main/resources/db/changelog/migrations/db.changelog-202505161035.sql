CREATE TABLE IF NOT EXISTS boards (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS boards_columns (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    "order" INTEGER NOT NULL,
    kind TEXT NOT NULL,
    board_id INTEGER NOT NULL,
    CONSTRAINT fk_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT unique_board_id_order UNIQUE (board_id, "order")
);

CREATE TABLE IF NOT EXISTS cards (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    board_column_id INTEGER NOT NULL,
    blocked INTEGER DEFAULT 0,
    block_reason TEXT,
    unblock_reason TEXT,
    creation_date TEXT DEFAULT (datetime('now')),
    last_update_date TEXT,
    completion_date TEXT,
    CONSTRAINT fk_board_column FOREIGN KEY (board_column_id) REFERENCES boards_columns(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS boards_column (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    "order" INTEGER NOT NULL,
    kind TEXT NOT NULL,
    board_id INTEGER NOT NULL,
    CONSTRAINT fk_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT unique_board_id_order UNIQUE (board_id, "order")
);
CREATE TABLE BLOCKS(
    id BIGINT PRIMARY KEY,
    blocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    block_reason VARCHAR(255) NOT NULL,
    unblocked_at TIMESTAMP NULL,
    unblock_reason VARCHAR(255) NOT NULL,
    card_id BIGINT NOT NULL,
    CONSTRAINT cards__blocks_fk FOREIGN KEY (card_id) REFERENCES CARDS(id) ON DELETE CASCADE
) ENGINE=InnoDB;