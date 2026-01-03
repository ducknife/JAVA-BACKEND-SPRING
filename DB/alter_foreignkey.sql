use astar_hub;

alter table product
add category_id int not null;

insert into product (category_id) values 
(1),
(2);

alter table product 
add constraint fk_product_category
foreign key (category_id) references category(id);

select * from product;	
