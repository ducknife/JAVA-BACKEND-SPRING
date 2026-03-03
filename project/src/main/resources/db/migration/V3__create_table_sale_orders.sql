create table
    sale_orders (
        order_id bigint primary key auto_increment not null,
        user_id bigint not null,
        constraint fk_order_user foreign key (user_id) references users (user_id) on delete cascade
    );