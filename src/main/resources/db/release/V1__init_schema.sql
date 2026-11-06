CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    image_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    track_stock BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE product_images
(
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT  NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    image_url  TEXT    NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    main_image BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,

    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_comment TEXT,

    status VARCHAR(50) NOT NULL DEFAULT 'NEW',

    total_amount NUMERIC(10, 2) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    source VARCHAR(20) NOT NULL,
    idempotency_key UUID,
    payment_expires_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_orders_status
        CHECK (status IN ('AWAITING_PAYMENT', 'NEW', 'IN_WORK', 'DONE', 'CANCELLED')),

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

CREATE INDEX idx_orders_status ON orders(status);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

CREATE INDEX idx_order_items_product_id ON order_items(product_id);

CREATE TABLE telegram_users (
    telegram_user_id BIGINT PRIMARY KEY,
    telegram_chat_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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

CREATE UNIQUE INDEX ux_orders_idempotency_key
    ON orders (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

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

    provider_invoice_id VARCHAR(255),

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

CREATE UNIQUE INDEX ux_payments_one_pending_per_order
    ON payments(order_id)
    WHERE status = 'PENDING';

CREATE UNIQUE INDEX ux_payments_provider_invoice_id
    ON payments(provider_invoice_id)
    WHERE provider_invoice_id IS NOT NULL;
