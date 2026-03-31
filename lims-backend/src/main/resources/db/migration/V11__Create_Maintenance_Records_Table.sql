-- V11: Create the maintenance_records table.
-- Stores preventive and corrective maintenance history for each piece of equipment.

CREATE TABLE maintenance_records
(
    id                BIGSERIAL PRIMARY KEY,
    type              VARCHAR(20)              NOT NULL,   -- PREVENTIVE | CORRECTIVE
    performed_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    description       TEXT                     NOT NULL,
    resolution        TEXT,
    cost              DECIMAL(12, 2),
    performed_by      BIGINT,
    external_provider VARCHAR(255),
    equipment_id      BIGINT                   NOT NULL,
    CONSTRAINT fk_maint_performed_by FOREIGN KEY (performed_by) REFERENCES users (id),
    CONSTRAINT fk_maint_equipment    FOREIGN KEY (equipment_id) REFERENCES equipment (id)
);

CREATE INDEX idx_maintenance_equipment ON maintenance_records (equipment_id);
CREATE INDEX idx_maintenance_type      ON maintenance_records (type);

-- Hibernate Auditing
CREATE TABLE maintenance_records_aud
(
    id                BIGINT   NOT NULL,
    rev               INTEGER  NOT NULL,
    revtype           SMALLINT,
    type              VARCHAR(20),
    performed_at      TIMESTAMP WITH TIME ZONE,
    description       TEXT,
    resolution        TEXT,
    cost              DECIMAL(12, 2),
    performed_by      BIGINT,
    external_provider VARCHAR(255),
    equipment_id      BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_maintenance_records_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);
