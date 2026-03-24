-- ==========================================================
-- 1. DOMAIN TABLES (CORE BUSINESS ENTITIES)
-- ==========================================================

CREATE TABLE customers
(
    id               BIGSERIAL PRIMARY KEY,
    corporate_reason VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    tax_id           VARCHAR(50)  NOT NULL UNIQUE,
    phone            VARCHAR(20),
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE samples
(
    id            BIGSERIAL PRIMARY KEY,
    description   VARCHAR(255)        NOT NULL,
    barcode       VARCHAR(100) UNIQUE NOT NULL,
    material_type VARCHAR(100),
    status        VARCHAR(50)         NOT NULL DEFAULT 'RECEIVED',
    customer_id   BIGINT              NOT NULL,
    received_at   TIMESTAMP WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sample_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
            ON DELETE RESTRICT
);

CREATE TABLE tests_results
(
    id             BIGSERIAL PRIMARY KEY,
    parameter_name VARCHAR(100) NOT NULL,
    result_value   VARCHAR(255),
    display_value  VARCHAR(100),
    numeric_value  DECIMAL(12, 4),
    unit           VARCHAR(20),
    sample_id      BIGINT       NOT NULL,
    performed_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_test_sample
        FOREIGN KEY (sample_id) REFERENCES samples (id)
            ON DELETE CASCADE
);

-- ==========================================================
-- 2. AUDIT TABLES (HIBERNATE ENVERS INFRASTRUCTURE)
-- ==========================================================

CREATE TABLE revisions
(
    id          BIGSERIAL PRIMARY KEY,
    timestamp   TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_by VARCHAR(255)
);

CREATE TABLE customers_aud
(
    id               BIGINT NOT NULL,
    rev              BIGINT NOT NULL,
    revtype          SMALLINT,
    corporate_reason VARCHAR(255),
    email            VARCHAR(255),
    tax_id           VARCHAR(50),
    phone            VARCHAR(20),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_customers_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);

CREATE TABLE samples_aud
(
    id            BIGINT NOT NULL,
    rev           BIGINT NOT NULL,
    revtype       SMALLINT,
    description   VARCHAR(255),
    barcode       VARCHAR(100),
    material_type VARCHAR(100),
    status        VARCHAR(50),
    customer_id   BIGINT,
    received_at   TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_samples_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);

CREATE TABLE tests_results_aud
(
    id             BIGINT NOT NULL,
    rev            BIGINT NOT NULL,
    revtype        SMALLINT,
    parameter_name VARCHAR(100),
    result_value   VARCHAR(255),
    unit           VARCHAR(20),
    sample_id      BIGINT,
    performed_at   TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_tests_results_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);