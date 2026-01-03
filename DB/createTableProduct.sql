create database astar_hub;
use astar_hub;

create table product (
	id int primary key not null auto_increment,
    name varchar(200),
    price decimal(10, 2)
)

