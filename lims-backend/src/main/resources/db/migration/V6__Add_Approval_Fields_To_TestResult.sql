-- V6: Add approval workflow fields to tests_results.
-- Enables result-level approval by a supervisor before sample release.

ALTER TABLE tests_results
    ADD COLUMN IF NOT EXISTS status           VARCHAR(50)              NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS approved_by      BIGINT,
    ADD COLUMN IF NOT EXISTS approved_at      TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT,
    ADD CONSTRAINT fk_result_approved_by FOREIGN KEY (approved_by) REFERENCES users (id);

-- Extend the audit table with the same new columns.
ALTER TABLE tests_results_aud
    ADD COLUMN IF NOT EXISTS status           VARCHAR(50),
    ADD COLUMN IF NOT EXISTS approved_by      BIGINT,
    ADD COLUMN IF NOT EXISTS approved_at      TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

COMMENT ON COLUMN tests_results.status IS
    'Approval status: PENDING | APPROVED | REJECTED';
