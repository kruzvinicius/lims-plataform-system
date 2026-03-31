-- V14: Create analysis_types table for test type definitions with acceptance ranges.
CREATE TABLE analysis_types
(
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(50) UNIQUE NOT NULL,
    name         VARCHAR(255)       NOT NULL,
    description  TEXT,
    default_unit VARCHAR(30),
    min_value    DECIMAL(12, 4),
    max_value    DECIMAL(12, 4),
    active       BOOLEAN            NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_at_code   ON analysis_types (code);
CREATE INDEX idx_at_active ON analysis_types (active);

CREATE TABLE analysis_types_aud
(
    id           BIGINT   NOT NULL,
    rev          INTEGER  NOT NULL,
    revtype      SMALLINT,
    code         VARCHAR(50),
    name         VARCHAR(255),
    description  TEXT,
    default_unit VARCHAR(30),
    min_value    DECIMAL(12, 4),
    max_value    DECIMAL(12, 4),
    active       BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_at_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);
