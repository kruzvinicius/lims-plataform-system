-- V12: Add laboratory_unit to users table for branch/unit filtering.
ALTER TABLE users ADD COLUMN IF NOT EXISTS laboratory_unit VARCHAR(100);
ALTER TABLE users_aud ADD COLUMN IF NOT EXISTS laboratory_unit VARCHAR(100);
COMMENT ON COLUMN users.laboratory_unit IS 'Lab unit/branch this user belongs to (e.g. LAB-SP, LAB-RJ)';
