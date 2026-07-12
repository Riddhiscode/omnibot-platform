-- ============================================================
-- OmniBot Platform — Full Database Schema
-- Run this ONCE on a fresh database to set up every table.
--
-- Usage:
--   mysql -u root -p < database/schema.sql
-- Or paste into phpMyAdmin's SQL tab.
-- ============================================================

CREATE DATABASE IF NOT EXISTS omnibot_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE omnibot_db;

-- ------------------------------------------------------------
-- users — core account table, one identity across all services
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    email           VARCHAR(180)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,
    phone           VARCHAR(15)     DEFAULT NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    is_verified     TINYINT(1)      NOT NULL DEFAULT 0,
    role            ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login      DATETIME        DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- chat_messages — every user/bot turn, tagged with detected intent
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- conversation_states — in-progress multi-step booking flows
-- (e.g. cab booking: source -> destination -> time -> preference)
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- saved_routes — favourite source/destination pairs for quick booking
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- saved_addresses — favourite delivery addresses for food/grocery orders
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS saved_addresses (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    address        VARCHAR(255) NOT NULL,
    preferred_app  VARCHAR(30),
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_sa_user (user_id),
    CONSTRAINT fk_sa_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- connected_services — links to third-party accounts (future real APIs)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS connected_services (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    service_name    VARCHAR(50)     NOT NULL,
    service_user_id VARCHAR(100)    DEFAULT NULL,
    access_token    TEXT            DEFAULT NULL,
    refresh_token   TEXT            DEFAULT NULL,
    token_expiry    DATETIME        DEFAULT NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cs2_user_service (user_id, service_name),
    CONSTRAINT fk_cs2_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
