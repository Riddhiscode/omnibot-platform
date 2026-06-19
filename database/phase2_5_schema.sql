-- ============================================================
-- OmniBot Phase 2.5 — Conversational Booking Flow Schema
-- Run this after phase2_schema.sql
-- ============================================================
USE omnibot_db;

-- Tracks in-progress multi-step conversations (slot filling)
CREATE TABLE IF NOT EXISTS conversation_states (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    session_id    VARCHAR(64) NOT NULL,
    user_id       BIGINT      NOT NULL,
    flow_type     VARCHAR(20) NOT NULL,
    current_step  VARCHAR(20) NOT NULL,
    data          TEXT,
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cs_session (session_id),
    CONSTRAINT fk_cs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Saved routes for quick future bookings
CREATE TABLE IF NOT EXISTS saved_routes (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    source         VARCHAR(255) NOT NULL,
    destination    VARCHAR(255) NOT NULL,
    preferred_app  VARCHAR(30),
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_sr_user (user_id),
    CONSTRAINT fk_sr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
