-- V21: Add legislation_id to commercial proposals so we can define VMP references for the entire project.

ALTER TABLE commercial_proposals
    ADD COLUMN legislation_id BIGINT REFERENCES environmental_legislations (id) ON DELETE SET NULL;

ALTER TABLE commercial_proposals_aud
    ADD COLUMN legislation_id BIGINT;
