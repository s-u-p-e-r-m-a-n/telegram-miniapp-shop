CREATE TABLE product_images
(
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT  NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    image_url  TEXT    NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    main_image BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);

INSERT INTO product_images (product_id,
                            image_url,
                            sort_order,
                            main_image)
SELECT id,
       image_url,
       sort_order,
       true
FROM products
WHERE image_url IS NOT NULL;

ALTER TABLE products
    DROP COLUMN image_url;