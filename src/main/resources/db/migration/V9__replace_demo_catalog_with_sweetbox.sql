-- =========================================================
-- 1. Скрываем старый демонстрационный каталог
-- =========================================================

UPDATE products
SET active = false;

UPDATE categories
SET active = false;


-- =========================================================
-- 2. Новые категории SweetBox
-- =========================================================

INSERT INTO categories (
    name,
    image_url,
    active,
    sort_order
)
VALUES
    ('Торты',    '/images/categories/cakes.jpg',      true, 1),
    ('Десерты',  '/images/categories/desserts.jpg',   true, 2),
    ('Наборы',   '/images/categories/gift-boxes.jpg', true, 3),
    ('Напитки',  '/images/categories/drinks.jpg',     true, 4);


-- =========================================================
-- 3. Торты
-- =========================================================

INSERT INTO products (
    category_id,
    name,
    description,
    price,
    active,
    featured,
    sort_order,
    stock_quantity,
    track_stock
)
VALUES
    (
        (SELECT id FROM categories WHERE name = 'Торты' AND active = true),
        'Клубничный торт',
        'Нежный бисквит, сливочный крем и свежая клубника.',
        1490.00,
        true,
        true,
        1,
        10,
        true
    ),
    (
        (SELECT id FROM categories WHERE name = 'Торты' AND active = true),
        'Шоколадный торт',
        'Шоколадный бисквит с насыщенным кремом и шоколадным декором.',
        1290.00,
        true,
        true,
        2,
        10,
        true
    );


-- =========================================================
-- 4. Десерты
-- =========================================================

INSERT INTO products (
    category_id,
    name,
    description,
    price,
    active,
    featured,
    sort_order,
    stock_quantity,
    track_stock
)
VALUES
    (
        (SELECT id FROM categories WHERE name = 'Десерты' AND active = true),
        'Капкейки, 6 шт.',
        'Набор воздушных капкейков с нежным сливочным кремом.',
        690.00,
        true,
        false,
        3,
        20,
        true
    ),
    (
        (SELECT id FROM categories WHERE name = 'Десерты' AND active = true),
        'Макаруны, 8 шт.',
        'Ассорти французских макарунов с разными начинками.',
        590.00,
        true,
        true,
        4,
        20,
        true
    );


-- =========================================================
-- 5. Подарочные наборы
-- =========================================================

INSERT INTO products (
    category_id,
    name,
    description,
    price,
    active,
    featured,
    sort_order,
    stock_quantity,
    track_stock
)
VALUES
    (
        (SELECT id FROM categories WHERE name = 'Наборы' AND active = true),
        'Подарочный SweetBox',
        'Большой подарочный набор десертов в фирменной упаковке.',
        1590.00,
        true,
        true,
        5,
        10,
        true
    ),
    (
        (SELECT id FROM categories WHERE name = 'Наборы' AND active = true),
        'Набор «Для двоих»',
        'Компактный набор десертов для уютного вечера вдвоём.',
        990.00,
        true,
        false,
        6,
        15,
        true
    );


-- =========================================================
-- 6. Напитки
-- =========================================================

INSERT INTO products (
    category_id,
    name,
    description,
    price,
    active,
    featured,
    sort_order,
    stock_quantity,
    track_stock
)
VALUES
    (
        (SELECT id FROM categories WHERE name = 'Напитки' AND active = true),
        'Какао',
        'Горячее какао на молоке с насыщенным шоколадным вкусом.',
        290.00,
        true,
        false,
        7,
        30,
        true
    ),
    (
        (SELECT id FROM categories WHERE name = 'Напитки' AND active = true),
        'Ягодный лимонад',
        'Освежающий лимонад с ягодами и лёгкой цитрусовой кислинкой.',
        250.00,
        true,
        false,
        8,
        30,
        true
    );


-- =========================================================
-- 7. Изображения товаров
-- =========================================================

INSERT INTO product_images (
    product_id,
    image_url,
    sort_order,
    main_image
)
VALUES
    (
        (SELECT id FROM products WHERE name = 'Клубничный торт' AND active = true),
        '/images/products/strawberry-cake.jpg',
        1,
        true
    ),
    (
        (SELECT id FROM products WHERE name = 'Шоколадный торт' AND active = true),
        '/images/products/chocolate-cake.jpg',
        1,
        true
    ),
    (
        (SELECT id FROM products WHERE name = 'Капкейки, 6 шт.' AND active = true),
        '/images/products/cupcakes.jpg',
        1,
        true
    ),
    (
        (SELECT id FROM products WHERE name = 'Макаруны, 8 шт.' AND active = true),
        '/images/products/macarons.jpg',
        1,
        true
    ),
    (
        (SELECT id FROM products WHERE name = 'Подарочный SweetBox' AND active = true),
        '/images/products/sweetbox-gift.jpg',
        1,
        true
    ),
    (
        (SELECT id FROM products WHERE name = 'Набор «Для двоих»' AND active = true),
        '/images/products/sweetbox-for-two.jpg',
        1,
        true
    ),
    (
        (SELECT id FROM products WHERE name = 'Какао' AND active = true),
        '/images/products/cocoa.jpg',
        1,
        true
    ),
    (
        (SELECT id FROM products WHERE name = 'Ягодный лимонад' AND active = true),
        '/images/products/berry-lemonade.jpg',
        1,
        true
    );