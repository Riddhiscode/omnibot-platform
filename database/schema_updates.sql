-- ------------------------------------------------------------
-- vendor_mappings — platform mappings for aggregators
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vendor_mappings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    vendor_id       VARCHAR(50)     NOT NULL,
    vendor_name     VARCHAR(100)    NOT NULL,
    category        VARCHAR(50)     NOT NULL,
    api_endpoint    VARCHAR(255),
    api_key_alias   VARCHAR(100),
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_vendor_id (vendor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- orders — unified order storage
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    user_id           BIGINT          NOT NULL,
    vendor_id         VARCHAR(50)     NOT NULL,
    category          VARCHAR(50)     NOT NULL,
    status            VARCHAR(50)     NOT NULL,
    total_amount      DECIMAL(10, 2)  NOT NULL,
    currency          VARCHAR(3)      NOT NULL DEFAULT 'USD',
    external_order_id VARCHAR(100),
    tracking_url      VARCHAR(500),
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_orders_user (user_id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_vendor FOREIGN KEY (vendor_id) REFERENCES vendor_mappings(vendor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- payment_transactions — atomic checkout background transactions
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_transactions (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    order_id          BIGINT          NOT NULL,
    user_id           BIGINT          NOT NULL,
    amount            DECIMAL(10, 2)  NOT NULL,
    currency          VARCHAR(3)      NOT NULL DEFAULT 'USD',
    status            VARCHAR(50)     NOT NULL,
    payment_method    VARCHAR(50),
    transaction_ref   VARCHAR(100),
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_payments_order (order_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- intents_extracted — NLP logs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS intents_extracted (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    message_id        BIGINT          NOT NULL,
    raw_text          TEXT            NOT NULL,
    parsed_intent     VARCHAR(50),
    confidence_score  DECIMAL(5, 4),
    entities_json     TEXT,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ie_message FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
