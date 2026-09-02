ALTER TABLE orders
    ADD COLUMN source VARCHAR(20);

UPDATE orders
SET source = 'TELEGRAM';

ALTER TABLE orders
    ALTER COLUMN source SET NOT NULL;

ALTER TABLE orders
    ALTER COLUMN telegram_user_id DROP NOT NULL;