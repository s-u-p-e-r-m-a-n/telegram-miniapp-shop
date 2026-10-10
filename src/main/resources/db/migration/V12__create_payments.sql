CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,

                          order_id BIGINT NOT NULL
                              REFERENCES orders(id),

                          provider_payment_id VARCHAR(255),

                          idempotency_key UUID NOT NULL,

                          amount NUMERIC(10, 2) NOT NULL,

                          status VARCHAR(32) NOT NULL DEFAULT 'PENDING',

                          confirmation_url TEXT,

                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          paid_at TIMESTAMP WITH TIME ZONE,

                          CONSTRAINT chk_payments_amount
                              CHECK (amount > 0),

                          CONSTRAINT chk_payments_status
                              CHECK (status IN ('PENDING', 'PAID', 'CANCELLED'))
);

CREATE UNIQUE INDEX ux_payments_idempotency_key
    ON payments(idempotency_key);

CREATE UNIQUE INDEX ux_payments_provider_payment_id
    ON payments(provider_payment_id)
    WHERE provider_payment_id IS NOT NULL;

CREATE INDEX idx_payments_order_id
    ON payments(order_id);