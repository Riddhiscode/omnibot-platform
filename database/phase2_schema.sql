-- ============================================================
-- OmniBot Phase 2 — Chat Schema
-- Run this in phpMyAdmin or mysql CLI after Phase 1 schema
-- ============================================================
USE omnibot_db;

CREATE TABLE IF NOT EXISTS chat_messages (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    session_id  VARCHAR(64) NOT NULL,
    role        ENUM('USER','BOT') NOT NULL,
    content     TEXT        NOT NULL,
    intent      VARCHAR(20) DEFAULT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_cm_session (session_id),
    INDEX idx_cm_user (user_id),
    CONSTRAINT fk_cm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
