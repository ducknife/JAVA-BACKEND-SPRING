create database astar_hub;
use astar_hub;

create table product (
	id int auto_increment primary key not null,
    name varchar(200),
    price decimal(10, 2),
    category_id int not null,
    foreign key (category_id) references category(id)
);

describe product;

alter table product 
add constraint uk_product_name unique(name);

alter table product
modify name varchar(200) not null;