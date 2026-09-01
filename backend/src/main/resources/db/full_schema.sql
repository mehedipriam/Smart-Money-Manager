-- =====================================================================
-- Smart Money Manager — Full Database Schema (Phase 2)
-- =====================================================================
-- Reference DDL only. This file is NOT executed automatically by Spring
-- Boot (it is not named schema.sql/data.sql and Flyway/Liquibase are not
-- on the classpath). The live schema is generated from the JPA entities
-- under com.smartmoneymanager.backend.entity via
-- spring.jpa.hibernate.ddl-auto=update (dev profile).
--
-- Keep this file in sync with the entities — it exists so the schema can
-- be reviewed, diffed, or applied manually (e.g. in MySQL Workbench)
-- without booting the application.
-- =====================================================================

CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS users (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name           VARCHAR(150)  NOT NULL,
    email               VARCHAR(191)  NOT NULL UNIQUE,
    password            VARCHAR(255)  NOT NULL,
    phone               VARCHAR(30),
    profile_image_url   VARCHAR(500),
    default_currency    VARCHAR(10)   NOT NULL DEFAULT 'BDT',
    preferred_language  VARCHAR(5)    NOT NULL DEFAULT 'EN',
    email_verified      BOOLEAN       NOT NULL DEFAULT FALSE,
    enabled             BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          DATETIME      NOT NULL,
    updated_at          DATETIME      NOT NULL
) ENGINE = InnoDB;

-- Many-to-many join table between users and roles.
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS accounts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    account_name    VARCHAR(100)  NOT NULL,
    account_type    VARCHAR(30)   NOT NULL,
    initial_balance DECIMAL(19,4) NOT NULL,
    current_balance DECIMAL(19,4) NOT NULL,
    currency        VARCHAR(10)   NOT NULL,
    created_at      DATETIME      NOT NULL,
    updated_at      DATETIME      NOT NULL,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;
CREATE INDEX idx_accounts_user ON accounts (user_id);

CREATE TABLE IF NOT EXISTS categories (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT,                      -- NULL = default/global category
    name       VARCHAR(100) NOT NULL,
    type       VARCHAR(10)  NOT NULL,       -- INCOME | EXPENSE
    icon       VARCHAR(50),
    color      VARCHAR(20),
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;
CREATE INDEX idx_categories_user ON categories (user_id);
CREATE INDEX idx_categories_type ON categories (type);

CREATE TABLE IF NOT EXISTS transactions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT        NOT NULL,
    account_id        BIGINT        NOT NULL,
    category_id       BIGINT        NOT NULL,
    type              VARCHAR(10)   NOT NULL, -- INCOME | EXPENSE
    amount            DECIMAL(19,4) NOT NULL,
    transaction_date  DATE          NOT NULL,
    description       VARCHAR(255),
    note              VARCHAR(1000),
    created_at        DATETIME      NOT NULL,
    updated_at        DATETIME      NOT NULL,
    CONSTRAINT fk_transactions_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_transactions_account  FOREIGN KEY (account_id)  REFERENCES accounts (id),
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE = InnoDB;
CREATE INDEX idx_transactions_user_date ON transactions (user_id, transaction_date);
CREATE INDEX idx_transactions_account   ON transactions (account_id);
CREATE INDEX idx_transactions_category  ON transactions (category_id);

CREATE TABLE IF NOT EXISTS recurring_transactions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT        NOT NULL,
    account_id    BIGINT        NOT NULL,
    category_id   BIGINT        NOT NULL,
    type          VARCHAR(10)   NOT NULL, -- INCOME | EXPENSE
    amount        DECIMAL(19,4) NOT NULL,
    description   VARCHAR(255),
    note          VARCHAR(1000),
    frequency     VARCHAR(10)   NOT NULL, -- DAILY | WEEKLY | MONTHLY | YEARLY
    start_date    DATE          NOT NULL,
    next_run_date DATE          NOT NULL,
    end_date      DATE,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME      NOT NULL,
    updated_at    DATETIME      NOT NULL,
    CONSTRAINT fk_recurring_tx_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_recurring_tx_account  FOREIGN KEY (account_id)  REFERENCES accounts (id),
    CONSTRAINT fk_recurring_tx_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE = InnoDB;
CREATE INDEX idx_recurring_tx_user     ON recurring_transactions (user_id);
CREATE INDEX idx_recurring_tx_next_run ON recurring_transactions (next_run_date, active);

CREATE TABLE IF NOT EXISTS budgets (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT        NOT NULL,
    category_id   BIGINT        NOT NULL,
    budget_amount DECIMAL(19,4) NOT NULL,
    month         INT           NOT NULL, -- 1-12
    year          INT           NOT NULL,
    created_at    DATETIME      NOT NULL,
    updated_at    DATETIME      NOT NULL,
    CONSTRAINT fk_budgets_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT uq_budget_user_category_period UNIQUE (user_id, category_id, month, year)
) ENGINE = InnoDB;
CREATE INDEX idx_budgets_user_period ON budgets (user_id, year, month);

CREATE TABLE IF NOT EXISTS goals (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT        NOT NULL,
    goal_name            VARCHAR(150)  NOT NULL,
    target_amount        DECIMAL(19,4) NOT NULL,
    current_saved_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    target_date          DATE,
    description          VARCHAR(1000),
    status               VARCHAR(15)   NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | COMPLETED | CANCELLED
    created_at           DATETIME      NOT NULL,
    updated_at           DATETIME      NOT NULL,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;
CREATE INDEX idx_goals_user   ON goals (user_id);
CREATE INDEX idx_goals_status ON goals (status);

CREATE TABLE IF NOT EXISTS goal_contributions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id           BIGINT        NOT NULL,
    amount            DECIMAL(19,4) NOT NULL,
    contribution_date DATE          NOT NULL,
    note              VARCHAR(500),
    created_at        DATETIME      NOT NULL,
    CONSTRAINT fk_goal_contributions_goal FOREIGN KEY (goal_id) REFERENCES goals (id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE INDEX idx_goal_contributions_goal ON goal_contributions (goal_id);

CREATE TABLE IF NOT EXISTS bills (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    bill_name      VARCHAR(150)  NOT NULL,
    amount         DECIMAL(19,4) NOT NULL,
    due_date       DATE          NOT NULL,
    category_id    BIGINT,
    recurring_type VARCHAR(10),             -- NULL | DAILY | WEEKLY | MONTHLY | YEARLY
    payment_status VARCHAR(10)   NOT NULL DEFAULT 'PENDING', -- PENDING | PAID | OVERDUE
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NOT NULL,
    CONSTRAINT fk_bills_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_bills_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE = InnoDB;
CREATE INDEX idx_bills_user_due_date ON bills (user_id, due_date);
CREATE INDEX idx_bills_status        ON bills (payment_status);

CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    type       VARCHAR(40)  NOT NULL, -- BUDGET_WARNING | BUDGET_EXCEEDED | BILL_DUE_REMINDER | GOAL_COMPLETED | RECURRING_TRANSACTION_ADDED
    title      VARCHAR(150) NOT NULL,
    message    VARCHAR(500) NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME     NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE INDEX idx_password_reset_tokens_user ON password_reset_tokens (user_id);

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME     NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL,
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE INDEX idx_email_verification_tokens_user ON email_verification_tokens (user_id);
