-- V10: Create the calibration_records table.
-- Stores the full calibration history for each piece of equipment.

CREATE TABLE calibration_records
(
    id                    BIGSERIAL PRIMARY KEY,
    performed_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    next_due_at           DATE,
    result                VARCHAR(10)  NOT NULL,   -- PASS | FAIL
    performed_by          BIGINT,
    external_provider     VARCHAR(255),
    certificate_reference VARCHAR(100),
    observations          TEXT,
    equipment_id          BIGINT       NOT NULL,
    CONSTRAINT fk_cal_performed_by FOREIGN KEY (performed_by) REFERENCES users (id),
    CONSTRAINT fk_cal_equipment    FOREIGN KEY (equipment_id) REFERENCES equipment (id)
);

CREATE INDEX idx_calibration_equipment ON calibration_records (equipment_id);
CREATE INDEX idx_calibration_result    ON calibration_records (result);

-- Hibernate Auditing
CREATE TABLE calibration_records_aud
(
    id                    BIGINT   NOT NULL,
    rev                   INTEGER  NOT NULL,
    revtype               SMALLINT,
    performed_at          TIMESTAMP WITH TIME ZONE,
    next_due_at           DATE,
    result                VARCHAR(10),
    performed_by          BIGINT,
    external_provider     VARCHAR(255),
    certificate_reference VARCHAR(100),
    observations          TEXT,
    equipment_id          BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_calibration_records_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);
