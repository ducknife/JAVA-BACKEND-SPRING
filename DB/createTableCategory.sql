use astar_hub;

create table category (
	id int auto_increment primary key not null,
    name varchar(100) not null,
    constraint uk_category_name unique(name)
);

insert into category (name) values 
('Machine Learning'),
('Deep Learning');

select * from category;
describe category;

-- alter table category
-- add constraint uk_category_name unique(name);