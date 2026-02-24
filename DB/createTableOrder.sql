use astar_hub;

create table
    sale_orders (
        order_id bigint primary key auto_increment not null,
        user_id bigint not null,
        constraint fk_order_user foreign key (user_id) references user (user_id)
    );

insert into
    sale_orders
values
    (1, 2),
    (2, 1);

select
    *
from
    sale_orders;