-- V17: Create environmental_legislations and legislation_parameters tables.
-- VMP (Maximum Permitted Value) belongs to legislation, not to the analysis parameter.

CREATE TABLE environmental_legislations
(
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50) UNIQUE NOT NULL,
    name        VARCHAR(255)       NOT NULL,
    region      VARCHAR(100),
    description TEXT,
    active      BOOLEAN            NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_leg_code   ON environmental_legislations (code);
CREATE INDEX idx_leg_active ON environmental_legislations (active);

-- Junction table: one legislation → many parameters, each with its own VMP
CREATE TABLE legislation_parameters
(
    id              BIGSERIAL PRIMARY KEY,
    legislation_id  BIGINT         NOT NULL REFERENCES environmental_legislations (id) ON DELETE CASCADE,
    analysis_type_id BIGINT        NOT NULL REFERENCES analysis_types (id) ON DELETE CASCADE,
    vmp             DECIMAL(14, 6) NOT NULL,  -- Valor Máximo Permitido
    unit            VARCHAR(30),               -- May differ from analysis_type default_unit
    notes           TEXT,
    CONSTRAINT uq_leg_param UNIQUE (legislation_id, analysis_type_id)
);

CREATE INDEX idx_legparam_leg  ON legislation_parameters (legislation_id);
CREATE INDEX idx_legparam_type ON legislation_parameters (analysis_type_id);

-- Audit tables for Envers
CREATE TABLE environmental_legislations_aud
(
    id          BIGINT   NOT NULL,
    rev         INTEGER  NOT NULL,
    revtype     SMALLINT,
    code        VARCHAR(50),
    name        VARCHAR(255),
    region      VARCHAR(100),
    description TEXT,
    active      BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_leg_aud_rev FOREIGN KEY (rev) REFERENCES revisions (id)
);

CREATE TABLE legislation_parameters_aud
(
    id               BIGINT         NOT NULL,
    rev              INTEGER        NOT NULL,
    revtype          SMALLINT,
    legislation_id   BIGINT,
    analysis_type_id BIGINT,
    vmp              DECIMAL(14, 6),
    unit             VARCHAR(30),
    notes            TEXT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_legparam_aud_rev FOREIGN KEY (rev) REFERENCES revisions (id)
);
