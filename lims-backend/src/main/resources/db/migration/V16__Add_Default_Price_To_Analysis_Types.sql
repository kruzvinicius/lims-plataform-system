-- V16: Add Default Price to Analysis Types

ALTER TABLE analysis_types ADD COLUMN default_price DECIMAL(12, 2);
ALTER TABLE analysis_types_aud ADD COLUMN default_price DECIMAL(12, 2);
