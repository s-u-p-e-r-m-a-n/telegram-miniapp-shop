ALTER TABLE payments
    ADD COLUMN provider_invoice_id VARCHAR(255);

CREATE UNIQUE INDEX ux_payments_provider_invoice_id
    ON payments(provider_invoice_id)
    WHERE provider_invoice_id IS NOT NULL;