-- V4: Add new fields to samples table to support full laboratory workflow.
-- Adds collection metadata, notes, rejection reason and expands valid status values.

ALTER TABLE samples
    ADD COLUMN IF NOT EXISTS collection_location VARCHAR(255),
    ADD COLUMN IF NOT EXISTS collection_date     DATE,
    ADD COLUMN IF NOT EXISTS notes               TEXT,
    ADD COLUMN IF NOT EXISTS rejection_reason    TEXT;

-- Extend the audit table with the same new columns.
ALTER TABLE samples_aud
    ADD COLUMN IF NOT EXISTS collection_location VARCHAR(255),
    ADD COLUMN IF NOT EXISTS collection_date     DATE,
    ADD COLUMN IF NOT EXISTS notes               TEXT,
    ADD COLUMN IF NOT EXISTS rejection_reason    TEXT;

-- Valid status values (stored as VARCHAR):
-- PENDING_RECEIPT, RECEIVED, IN_ANALYSIS, PENDING_APPROVAL,
-- APPROVED, RELEASED, REJECTED, REANALYSIS_REQUESTED, IN_REANALYSIS
COMMENT ON COLUMN samples.status IS
    'Workflow status: PENDING_RECEIPT | RECEIVED | IN_ANALYSIS | PENDING_APPROVAL | APPROVED | RELEASED | REJECTED | REANALYSIS_REQUESTED | IN_REANALYSIS';
