-- Garante que as tabelas sejam recriadas do zero a cada inicialização,
-- evitando erros de "tabela já existe" e garantindo um ambiente limpo para testes.
DROP TABLE IF EXISTS integration_sync_status CASCADE;
DROP TABLE IF EXISTS calendar_events CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS checklist_items CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS board_columns CASCADE;
DROP TABLE IF EXISTS boards CASCADE;
DROP TABLE IF EXISTS board_groups CASCADE;
DROP TABLE IF EXISTS card_types CASCADE;

-- Definição da tabela 'board_groups' (deve vir primeiro por causa das foreign keys)
CREATE TABLE board_groups (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    color           VARCHAR(7), -- Código hex da cor (ex: #FF5733)
    icon            VARCHAR(50), -- Ícone do grupo (ex: "work", "personal", "study")
    creation_date   TIMESTAMP NOT NULL
);

-- Definição da tabela 'boards'
CREATE TABLE boards (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    creation_date  TIMESTAMP NOT NULL,
    group_id       BIGINT,
    CONSTRAINT fk_boards_to_board_groups FOREIGN KEY (group_id) REFERENCES board_groups(id) ON DELETE SET NULL
);

-- Definição da tabela 'board_columns'
CREATE TABLE board_columns (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    order_index   INT NOT NULL,
    kind          VARCHAR(50) NOT NULL,
    board_id      BIGINT NOT NULL,

    CONSTRAINT fk_board_columns_to_boards FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
);

-- Definição da tabela 'card_types' (para tipos de card)
CREATE TABLE card_types (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    unit_label      VARCHAR(100) NOT NULL,
    creation_date   TIMESTAMP NOT NULL,
    last_update_date TIMESTAMP NOT NULL
);

-- Definição da tabela 'cards'
CREATE TABLE cards (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    card_type_id      BIGINT,
    total_units       INT,
    current_units     INT,
    progress_type     VARCHAR(50) DEFAULT 'PERCENTAGE',
    creation_date     TIMESTAMP NOT NULL,
    last_update_date  TIMESTAMP NOT NULL,
    completion_date   TIMESTAMP,
    scheduled_date    TIMESTAMP,
    due_date          TIMESTAMP,
    board_column_id   BIGINT NOT NULL,
    order_index       INTEGER DEFAULT 0,

    CONSTRAINT fk_cards_to_board_columns FOREIGN KEY (board_column_id) REFERENCES board_columns(id) ON DELETE CASCADE,
    CONSTRAINT fk_cards_to_card_types FOREIGN KEY (card_type_id) REFERENCES card_types(id) ON DELETE SET NULL
);

-- Definição da tabela 'checklist_items' (compatível com H2)
CREATE TABLE checklist_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id         BIGINT NOT NULL,
    text            TEXT NOT NULL,
    completed       BOOLEAN NOT NULL DEFAULT FALSE,
    order_index     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP,
    CONSTRAINT fk_checklist_items_cards FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

CREATE INDEX idx_checklist_items_card_id ON checklist_items(card_id);
CREATE INDEX idx_checklist_items_order_index ON checklist_items(order_index);

-- Definição da tabela 'tasks' (para integração com Google Tasks)
CREATE TABLE tasks (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    list_title         VARCHAR(255),
    title              VARCHAR(255) NOT NULL,
    due                TIMESTAMP WITH TIME ZONE,
    notes              TEXT,
    google_task_id     VARCHAR(255),
    sent               BOOLEAN NOT NULL DEFAULT FALSE,
    card_id            BIGINT,
    creation_date      TIMESTAMP,
    last_update_date   TIMESTAMP,

    CONSTRAINT fk_tasks_to_cards FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Definição da tabela 'calendar_events' (para persistir eventos do calendário)
CREATE TABLE calendar_events (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    start_date_time     TIMESTAMP NOT NULL,
    end_date_time       TIMESTAMP NOT NULL,
    all_day             BOOLEAN NOT NULL DEFAULT FALSE,
    event_type          VARCHAR(50) NOT NULL DEFAULT 'CARD',
    priority            VARCHAR(20) NOT NULL DEFAULT 'LOW',
    color               VARCHAR(7), -- Código hex da cor (ex: #FF5733)
    related_entity_id   BIGINT,
    related_entity_type VARCHAR(50), -- Tipo da entidade relacionada (CARD, TASK, etc.)
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_calendar_events_to_cards FOREIGN KEY (related_entity_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Definição da tabela 'integration_sync_status' (para rastrear sincronização com sistemas externos)
CREATE TABLE integration_sync_status (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id             BIGINT NOT NULL,
    integration_type    VARCHAR(50) NOT NULL,
    external_id         VARCHAR(255),
    sync_status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_sync_date      TIMESTAMP,
    error_message       TEXT,
    retry_count         INTEGER DEFAULT 0,
    max_retries         INTEGER DEFAULT 3,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    UNIQUE (card_id, integration_type)
);

-- Cria índices para as chaves estrangeiras, melhorando a performance de joins e buscas.
CREATE INDEX idx_board_columns_board_id ON board_columns(board_id);
CREATE INDEX idx_cards_board_column_id ON cards(board_column_id);
CREATE INDEX idx_cards_column_order ON cards(board_column_id, order_index);
CREATE INDEX idx_cards_scheduled_date ON cards(scheduled_date);
CREATE INDEX idx_cards_due_date ON cards(due_date);
CREATE INDEX idx_cards_urgency ON cards(completion_date, due_date);
CREATE INDEX idx_tasks_card_id ON tasks(card_id);
CREATE INDEX idx_boards_group_id ON boards(group_id);
CREATE INDEX idx_calendar_events_related_entity ON calendar_events(related_entity_id, related_entity_type);
CREATE INDEX idx_calendar_events_start_date ON calendar_events(start_date_time);
CREATE INDEX idx_calendar_events_active ON calendar_events(active);
CREATE INDEX idx_integration_sync_card_id ON integration_sync_status(card_id);
CREATE INDEX idx_integration_sync_type ON integration_sync_status(integration_type);
CREATE INDEX idx_integration_sync_status ON integration_sync_status(sync_status);
CREATE INDEX idx_integration_sync_last_sync ON integration_sync_status(last_sync_date);

-- Dados de exemplo para testes
-- Inserir um board de exemplo
INSERT INTO boards (name, creation_date) VALUES 
('Board de Exemplo', CURRENT_TIMESTAMP);

-- Inserir as 3 colunas padrão para o board de exemplo (ID 1)
INSERT INTO board_columns (name, order_index, kind, board_id) VALUES 
('Inicial', 1, 'INITIAL', 1),
('Em Andamento', 2, 'PENDING', 1),
('Finalizado', 3, 'FINAL', 1);

-- Inserir tipos padrão de card
INSERT INTO card_types (name, unit_label, creation_date, last_update_date) VALUES 
('CARD', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('BOOK', 'páginas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VIDEO', 'minutos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('COURSE', 'aulas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inserir grupos padrão na tabela board_groups
INSERT INTO board_groups (name, description, color, icon, creation_date) VALUES 
('Projetos pessoais', 'Projetos pessoais e hobbies', '#FFEAA7', '1f4bb', CURRENT_TIMESTAMP),
('Livros', 'Leitura e estudo de livros', '#4ECDC4', '1f4da', CURRENT_TIMESTAMP),
('Trabalho', 'Tarefas profissionais e trabalho', '#45B7D1', '1f528', CURRENT_TIMESTAMP);

-- Inserir um card de exemplo na coluna inicial (ID 1)
INSERT INTO cards (title, description, card_type_id, board_column_id, order_index, progress_type, creation_date, last_update_date) VALUES 
('Card de Exemplo', 'Este é um card de exemplo para demonstrar o sistema', 1, 1, 1, 'PERCENTAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);