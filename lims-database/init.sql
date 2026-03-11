--- Table for customers/companies ---

CREATE TABLE customers(
    id SERIAL PRIMARY KEY,
    corporate_reason VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(100),
    tax_id VARCHAR(100)
);

--- Table for Samples ---

CREATE TABLE samples(
    id SERIAL PRIMARY KEY,
    barcode VARCHAR(100) UNIQUE NOT NULL,
    material_type VARCHAR(100),
    status VARCHAR(20) DEFAULT 'Received',
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    customer_id INT REFERENCES customers(id)
);

--- Table for laboratory tests ---

CREATE TABLE tests_results(
    id SERIAL PRIMARY KEY,
    parameter_name VARCHAR(100),
    display_value VARCHAR(100), -- Qualitative significant value
    numeric_value DECIMAL(12,4), -- Quantitative significant value
    uom VARCHAR(10),
    sample_id INT REFERENCES samples(id)
);
