-- V7: Create the reanalysis_requests table.
-- Records formal requests for reanalysis after a sample has been rejected.

CREATE TABLE reanalysis_requests
(
    id               BIGSERIAL PRIMARY KEY,
    reason           TEXT                     NOT NULL,
    requested_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    resolved_at      TIMESTAMP WITH TIME ZONE,
    resolution_notes TEXT,
    requested_by     BIGINT                   NOT NULL,
    sample_id        BIGINT                   NOT NULL,
    CONSTRAINT fk_reanalysis_user   FOREIGN KEY (requested_by) REFERENCES users (id),
    CONSTRAINT fk_reanalysis_sample FOREIGN KEY (sample_id)    REFERENCES samples (id)
);

CREATE INDEX idx_reanalysis_requests_sample ON reanalysis_requests (sample_id);

-- Hibernate Auditing
CREATE TABLE reanalysis_requests_aud
(
    id               BIGINT   NOT NULL,
    rev              INTEGER  NOT NULL,
    revtype          SMALLINT,
    reason           TEXT,
    requested_at     TIMESTAMP WITH TIME ZONE,
    resolved_at      TIMESTAMP WITH TIME ZONE,
    resolution_notes TEXT,
    requested_by     BIGINT,
    sample_id        BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_reanalysis_requests_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);
