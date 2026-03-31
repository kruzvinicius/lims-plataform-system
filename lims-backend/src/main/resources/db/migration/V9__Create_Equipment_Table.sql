-- V9: Create the equipment table.
-- Tracks all laboratory equipment including status and calibration/maintenance due dates.

CREATE TABLE equipment
(
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    model                 VARCHAR(100),
    serial_number         VARCHAR(100) UNIQUE,
    manufacturer          VARCHAR(100),
    type                  VARCHAR(50)  NOT NULL,
    location              VARCHAR(255),
    status                VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    purchased_at          DATE,
    next_calibration_due  DATE,
    next_maintenance_due  DATE,
    registered_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_equipment_status ON equipment (status);
CREATE INDEX idx_equipment_type   ON equipment (type);

-- Hibernate Auditing
CREATE TABLE equipment_aud
(
    id                   BIGINT   NOT NULL,
    rev                  INTEGER  NOT NULL,
    revtype              SMALLINT,
    name                 VARCHAR(255),
    model                VARCHAR(100),
    serial_number        VARCHAR(100),
    manufacturer         VARCHAR(100),
    type                 VARCHAR(50),
    location             VARCHAR(255),
    status               VARCHAR(50),
    purchased_at         DATE,
    next_calibration_due DATE,
    next_maintenance_due DATE,
    registered_at        TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_equipment_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);
