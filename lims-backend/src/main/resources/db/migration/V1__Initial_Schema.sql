-- 1. Customers
CREATE TABLE customers
(
    id               BIGSERIAL PRIMARY KEY,
    corporate_reason VARCHAR(100) NOT NULL,
    email            VARCHAR(100),
    phone            VARCHAR(100),
    tax_id           VARCHAR(100)
);

-- 2. Samples
CREATE TABLE samples
(
    id            BIGSERIAL PRIMARY KEY,
    barcode       VARCHAR(100) UNIQUE NOT NULL,
    material_type VARCHAR(100),
    status        VARCHAR(20) DEFAULT 'Received',
    received_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    customer_id   BIGINT REFERENCES customers (id)
);

-- 3. Test Results
CREATE TABLE tests_results
(
    id             BIGSERIAL PRIMARY KEY,
    parameter_name VARCHAR(100),
    display_value  VARCHAR(100),
    numeric_value  DECIMAL(12, 4),
    uom            VARCHAR(10),
    sample_id      BIGINT REFERENCES samples (id)
);

-- 4. Audit Tables
CREATE TABLE revinfo
(
    rev_id      SERIAL PRIMARY KEY,
    revtstmp    BIGINT,
    modified_by VARCHAR(255)
);

CREATE TABLE samples_aud
(
    id            BIGINT  NOT NULL,
    rev_id        INTEGER NOT NULL,
    revtype       SMALLINT,
    barcode       VARCHAR(100),
    material_type VARCHAR(100),
    status        VARCHAR(20),
    PRIMARY KEY (id, rev_id),
    CONSTRAINT fk_samples_aud_revinfo FOREIGN KEY (rev_id) REFERENCES revinfo (rev_id)
);