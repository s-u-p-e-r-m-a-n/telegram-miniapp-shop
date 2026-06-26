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
                          image_url TEXT,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          featured BOOLEAN NOT NULL DEFAULT FALSE,
                          sort_order INTEGER NOT NULL DEFAULT 0,
                          stock_quantity INTEGER NOT NULL DEFAULT 0,
                          track_stock BOOLEAN NOT NULL DEFAULT TRUE
);