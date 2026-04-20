-- V22: Add legislation_id to samples_aud

ALTER TABLE samples_aud
    ADD COLUMN legislation_id BIGINT;
