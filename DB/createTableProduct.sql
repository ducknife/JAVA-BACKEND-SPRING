create database astar_hub;
use astar_hub;

-- drop database astar_hub;
-- create database astar_hub;

create table product (
	id int auto_increment primary key not null,
    name varchar(200) not null,
    price decimal(10, 2),
    category_id int not null,
    constraint fk_product_category foreign key (category_id) references category(id),
	constraint uk_product_name unique(name)
);

describe product;

-- alter table product 
-- add constraint uk_product_name unique(name);

-- alter table product
-- modify name varchar(200) not null;

select * from product;