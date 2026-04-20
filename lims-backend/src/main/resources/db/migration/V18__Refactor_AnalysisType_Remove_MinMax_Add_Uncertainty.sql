-- V18: Refactor analysis_types table.
-- Remove min_value / max_value (these belong to legislation as VMP).
-- Add uncertainty_value (intrinsic measurement uncertainty of the analytical method).

ALTER TABLE analysis_types DROP COLUMN IF EXISTS min_value;
ALTER TABLE analysis_types DROP COLUMN IF EXISTS max_value;
ALTER TABLE analysis_types ADD COLUMN uncertainty_value DECIMAL(12, 6);

-- Audit table adjustments
ALTER TABLE analysis_types_aud DROP COLUMN IF EXISTS min_value;
ALTER TABLE analysis_types_aud DROP COLUMN IF EXISTS max_value;
ALTER TABLE analysis_types_aud ADD COLUMN uncertainty_value DECIMAL(12, 6);
