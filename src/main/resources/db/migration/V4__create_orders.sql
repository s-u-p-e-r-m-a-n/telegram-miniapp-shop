CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,

                        telegram_user_id BIGINT NOT NULL,
                        telegram_chat_id BIGINT,

                        customer_name VARCHAR(255) NOT NULL,
                        customer_phone VARCHAR(50) NOT NULL,
                        customer_comment TEXT,

                        status VARCHAR(50) NOT NULL DEFAULT 'NEW',

                        total_amount NUMERIC(10, 2) NOT NULL,

                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                        CONSTRAINT chk_orders_status
                            CHECK (status IN ('NEW', 'IN_WORK', 'DONE', 'CANCELLED')),

                        CONSTRAINT chk_orders_total_amount
                            CHECK (total_amount >= 0)
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,

                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id BIGINT NOT NULL REFERENCES products(id),

                             product_name VARCHAR(255) NOT NULL,
                             product_price NUMERIC(10, 2) NOT NULL,

                             quantity INTEGER NOT NULL,
                             total_price NUMERIC(10, 2) NOT NULL,

                             CONSTRAINT chk_order_items_quantity
                                 CHECK (quantity > 0),

                             CONSTRAINT chk_order_items_product_price
                                 CHECK (product_price >= 0),

                             CONSTRAINT chk_order_items_total_price
                                 CHECK (total_price >= 0)
);

CREATE INDEX idx_orders_telegram_user_id ON orders(telegram_user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);