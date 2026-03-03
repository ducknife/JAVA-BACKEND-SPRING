create table
    invoices (
        id bigint primary key auto_increment not null,
        order_id bigint not null,
        total_price decimal(10, 2) not null default 0, 
        constraint fk_invoices_orders foreign key (order_id) references sale_orders (order_id) on delete cascade
    );

