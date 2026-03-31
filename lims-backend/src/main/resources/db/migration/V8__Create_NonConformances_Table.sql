-- V8: Create the non_conformances table.
-- Records quality deviations, process failures, QC issues and equipment problems.

CREATE TABLE non_conformances
(
    id                 BIGSERIAL PRIMARY KEY,
    title              VARCHAR(255)             NOT NULL,
    description        TEXT                     NOT NULL,
    type               VARCHAR(50)              NOT NULL,
    severity           VARCHAR(50)              NOT NULL,
    status             VARCHAR(50)              NOT NULL DEFAULT 'OPEN',
    detected_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    resolved_at        TIMESTAMP WITH TIME ZONE,
    root_cause         TEXT,
    corrective_action  TEXT,
    preventive_action  TEXT,
    detected_by        BIGINT                   NOT NULL,
    assigned_to        BIGINT,
    sample_id          BIGINT,
    test_result_id     BIGINT,
    CONSTRAINT fk_nc_detected_by   FOREIGN KEY (detected_by)    REFERENCES users (id),
    CONSTRAINT fk_nc_assigned_to   FOREIGN KEY (assigned_to)    REFERENCES users (id),
    CONSTRAINT fk_nc_sample        FOREIGN KEY (sample_id)      REFERENCES samples (id),
    CONSTRAINT fk_nc_test_result   FOREIGN KEY (test_result_id) REFERENCES tests_results (id)
);

CREATE INDEX idx_nc_status   ON non_conformances (status);
CREATE INDEX idx_nc_severity ON non_conformances (severity);
CREATE INDEX idx_nc_sample   ON non_conformances (sample_id);

-- Hibernate Auditing
CREATE TABLE non_conformances_aud
(
    id                BIGINT   NOT NULL,
    rev               INTEGER  NOT NULL,
    revtype           SMALLINT,
    title             VARCHAR(255),
    description       TEXT,
    type              VARCHAR(50),
    severity          VARCHAR(50),
    status            VARCHAR(50),
    detected_at       TIMESTAMP WITH TIME ZONE,
    resolved_at       TIMESTAMP WITH TIME ZONE,
    root_cause        TEXT,
    corrective_action TEXT,
    preventive_action TEXT,
    detected_by       BIGINT,
    assigned_to       BIGINT,
    sample_id         BIGINT,
    test_result_id    BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_nc_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);
