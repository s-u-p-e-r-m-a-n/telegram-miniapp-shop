insert into categories(name, image_url, active, sort_order)
values ('Цветы', 'https://example.com/flowers.jpg', true, 1),
       ('Подарки', 'https://example.com/gifts.jpg', true, 2),
       ('Открытки', 'https://example.com/cards.jpg', true, 3);

insert into products(category_id,
                     name,
                     description,
                     price,
                     image_url,
                     active,
                     featured,
                     sort_order,
                     stock_quantity,
                     track_stock)
    values
    (
        (SELECT id FROM categories WHERE name = 'Цветы'),
            'Букет роз',
            'Красные розы, 15 штук',
            3500.00,
            'https://example.com/rose.jpg',
            true,
            true,
            1,
            10,
            true
    ),
(
    (SELECT id FROM categories WHERE name = 'Цветы'),
        'Букет тюльпанов',
        'Свежие тюльпаны, 21 штука',
        2800.00,
        'https://example.com/tulips.jpg',
        true,
        true,
        2,
        7,
        true
),
(
    (SELECT id FROM categories WHERE name = 'Подарки'),
        'Подарочная коробка',
        'Набор сладостей и открытка',
        1500.00,
        'https://example.com/gift-box.jpg',
        true,
        false,
        1,
        5,
        true
);