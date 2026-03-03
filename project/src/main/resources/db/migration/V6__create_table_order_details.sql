create table
    order_details (
        id bigint primary key auto_increment not null,
        price decimal(10, 2) not null,
        quantity bigint not null,
        order_id bigint not null,
        product_id bigint not null,
        constraint fk_order_details_sale_orders foreign key (order_id) references sale_orders (order_id) on delete cascade,
        constraint fk_order_details_products foreign key (product_id) references products (id)
    );
