use astar_hub;

select p.name from product p 
join category c 
on p.category_id = c.id;