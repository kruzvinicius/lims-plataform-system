-- V5: Create the custody_events table for chain-of-custody tracking.
-- Every time a sample's physical possession changes, a record is inserted here.

CREATE TABLE custody_events
(
    id            BIGSERIAL PRIMARY KEY,
    event_type    VARCHAR(50)              NOT NULL,
    location      VARCHAR(255)             NOT NULL,
    notes         TEXT,
    occurred_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    transferred_by BIGINT                  NOT NULL,
    sample_id     BIGINT                   NOT NULL,
    CONSTRAINT fk_custody_user   FOREIGN KEY (transferred_by) REFERENCES users (id),
    CONSTRAINT fk_custody_sample FOREIGN KEY (sample_id)      REFERENCES samples (id)
);

CREATE INDEX idx_custody_events_sample ON custody_events (sample_id);

-- Hibernate Auditing
CREATE TABLE custody_events_aud
(
    id             BIGINT       NOT NULL,
    rev            INTEGER      NOT NULL,
    revtype        SMALLINT,
    event_type     VARCHAR(50),
    location       VARCHAR(255),
    notes          TEXT,
    occurred_at    TIMESTAMP WITH TIME ZONE,
    transferred_by BIGINT,
    sample_id      BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_custody_events_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);
