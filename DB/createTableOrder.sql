use astar_hub;

create table sale_order(
	order_id bigint primary key auto_increment not null,
    user_id bigint not null,
    foreign key (user_id) references user(user_id) on delete cascade
);

insert into sale_order values
(1, 2),
(2, 1);

select * from sale_order;