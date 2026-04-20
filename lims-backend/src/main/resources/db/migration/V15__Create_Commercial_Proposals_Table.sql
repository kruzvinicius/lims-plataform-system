-- V15: Commercial CRM Tables

CREATE TABLE commercial_proposals
(
    id               BIGSERIAL PRIMARY KEY,
    proposal_number  VARCHAR(255)             NOT NULL UNIQUE,
    title            VARCHAR(255)             NOT NULL,
    status           VARCHAR(50)              NOT NULL,
    total_amount     DECIMAL(12, 2),
    discount         DECIMAL(12, 2),
    final_amount     DECIMAL(12, 2),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_until      DATE,
    customer_id      BIGINT                   NOT NULL,
    created_by       BIGINT                   NOT NULL,
    service_order_id BIGINT UNIQUE,
    CONSTRAINT fk_comm_prop_cust FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_comm_prop_user FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_comm_prop_so FOREIGN KEY (service_order_id) REFERENCES service_orders (id)
);

CREATE TABLE proposal_items
(
    id               BIGSERIAL PRIMARY KEY,
    proposal_id      BIGINT         NOT NULL,
    description      VARCHAR(255)   NOT NULL,
    quantity         INTEGER        NOT NULL,
    unit_price       DECIMAL(12, 2) NOT NULL,
    total_price      DECIMAL(12, 2) NOT NULL,
    analysis_type_id BIGINT,
    CONSTRAINT fk_prop_items_prop FOREIGN KEY (proposal_id) REFERENCES commercial_proposals (id),
    CONSTRAINT fk_prop_items_analysis FOREIGN KEY (analysis_type_id) REFERENCES analysis_types (id)
);

CREATE INDEX idx_comm_prop_number ON commercial_proposals (proposal_number);
CREATE INDEX idx_comm_prop_cust ON commercial_proposals (customer_id);

CREATE TABLE commercial_proposals_aud
(
    id               BIGINT  NOT NULL,
    rev              INTEGER NOT NULL,
    revtype          SMALLINT,
    proposal_number  VARCHAR(255),
    title            VARCHAR(255),
    status           VARCHAR(50),
    total_amount     DECIMAL(12, 2),
    discount         DECIMAL(12, 2),
    final_amount     DECIMAL(12, 2),
    created_at       TIMESTAMP WITH TIME ZONE,
    valid_until      DATE,
    customer_id      BIGINT,
    created_by       BIGINT,
    service_order_id BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_comm_prop_aud_rev FOREIGN KEY (rev) REFERENCES revisions (id)
);

CREATE TABLE proposal_items_aud
(
    id               BIGINT  NOT NULL,
    rev              INTEGER NOT NULL,
    revtype          SMALLINT,
    proposal_id      BIGINT,
    description      VARCHAR(255),
    quantity         INTEGER,
    unit_price       DECIMAL(12, 2),
    total_price      DECIMAL(12, 2),
    analysis_type_id BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_prop_items_aud_rev FOREIGN KEY (rev) REFERENCES revisions (id)
);
