--liquibase formatted sql
--changeset junior:202408191938
--comment: boards table create

CREATE TABle BOARDS(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

--rollback DROP TABLE BOARDS