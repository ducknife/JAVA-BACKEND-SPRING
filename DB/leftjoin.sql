use astar_hub;

insert into category (name) values 
('Comic');

select * from category c 
left join product p 
on c.id = p.category_id;