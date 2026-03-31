-- V13: Create service_orders table and add FK to samples.
CREATE TABLE service_orders
(
    id                  BIGSERIAL PRIMARY KEY,
    order_number        VARCHAR(50) UNIQUE       NOT NULL,
    description         VARCHAR(500)             NOT NULL,
    status              VARCHAR(30)              NOT NULL DEFAULT 'CREATED',
    priority            VARCHAR(20)              NOT NULL DEFAULT 'NORMAL',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    due_date            DATE,
    completed_at        TIMESTAMP WITH TIME ZONE,
    cancelled_at        TIMESTAMP WITH TIME ZONE,
    cancellation_reason TEXT,
    customer_id         BIGINT                   NOT NULL,
    created_by          BIGINT                   NOT NULL,
    assigned_to         BIGINT,
    CONSTRAINT fk_so_customer   FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_so_created_by FOREIGN KEY (created_by)  REFERENCES users (id),
    CONSTRAINT fk_so_assigned   FOREIGN KEY (assigned_to) REFERENCES users (id)
);

CREATE INDEX idx_so_status   ON service_orders (status);
CREATE INDEX idx_so_priority ON service_orders (priority);
CREATE INDEX idx_so_assigned ON service_orders (assigned_to);
CREATE INDEX idx_so_due_date ON service_orders (due_date);

-- Add service_order FK to samples
ALTER TABLE samples ADD COLUMN IF NOT EXISTS service_order_id BIGINT;
ALTER TABLE samples ADD CONSTRAINT fk_sample_service_order FOREIGN KEY (service_order_id) REFERENCES service_orders (id);

-- Audit tables
ALTER TABLE samples_aud ADD COLUMN IF NOT EXISTS service_order_id BIGINT;

CREATE TABLE service_orders_aud
(
    id                  BIGINT   NOT NULL,
    rev                 INTEGER  NOT NULL,
    revtype             SMALLINT,
    order_number        VARCHAR(50),
    description         VARCHAR(500),
    status              VARCHAR(30),
    priority            VARCHAR(20),
    created_at          TIMESTAMP WITH TIME ZONE,
    due_date            DATE,
    completed_at        TIMESTAMP WITH TIME ZONE,
    cancelled_at        TIMESTAMP WITH TIME ZONE,
    cancellation_reason TEXT,
    customer_id         BIGINT,
    created_by          BIGINT,
    assigned_to         BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_so_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);
