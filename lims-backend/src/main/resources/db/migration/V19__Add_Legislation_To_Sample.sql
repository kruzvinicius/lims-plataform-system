-- V19: Add applicable legislation to samples.
-- Each sample collection point may fall under a different environmental class.

ALTER TABLE samples
    ADD COLUMN legislation_id BIGINT REFERENCES environmental_legislations (id) ON DELETE SET NULL;

CREATE INDEX idx_sample_legislation ON samples (legislation_id);
