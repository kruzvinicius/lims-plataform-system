-- V20: Add vmp_min and rename vmp → vmp_max in legislation_parameters.
-- Parameters like pH require both a minimum AND maximum permitted value (e.g. 6.0 – 9.0).
-- vmp_min is nullable: parameters with only a maximum (e.g. turbidity) leave it NULL.

ALTER TABLE legislation_parameters
    RENAME COLUMN vmp TO vmp_max;

ALTER TABLE legislation_parameters
    ADD COLUMN vmp_min DECIMAL(14, 6);

-- Audit table: same changes
ALTER TABLE legislation_parameters_aud
    RENAME COLUMN vmp TO vmp_max;

ALTER TABLE legislation_parameters_aud
    ADD COLUMN vmp_min DECIMAL(14, 6);
