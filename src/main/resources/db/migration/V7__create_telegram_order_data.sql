CREATE TABLE telegram_order_data
(
    order_id         BIGINT PRIMARY KEY,
    telegram_user_id BIGINT NOT NULL,

    CONSTRAINT fk_telegram_order_data_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_telegram_order_data_user
        FOREIGN KEY (telegram_user_id)
            REFERENCES telegram_users (telegram_user_id)
);

INSERT INTO telegram_order_data (order_id,
                                 telegram_user_id)
SELECT o.id,
       o.telegram_user_id
FROM orders o
         JOIN telegram_users tu
              ON tu.telegram_user_id = o.telegram_user_id
WHERE o.telegram_user_id IS NOT NULL;