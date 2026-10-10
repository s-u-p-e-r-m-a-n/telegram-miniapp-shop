ALTER TABLE orders
    DROP CONSTRAINT chk_orders_status;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status ---->ограничение только эти статусы, проверяется статус
        CHECK (
            status IN (
                       'AWAITING_PAYMENT',
                       'NEW',
                       'IN_WORK',
                       'DONE',
                       'CANCELLED'
                )
            );