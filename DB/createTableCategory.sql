use astar_hub;

create table categories (
	id int auto_increment primary key not null,
    name varchar(100) not null,
    constraint uk_category_name unique(name)
);

insert into categories (name) values 
('Machine Learning'),
('Deep Learning');

select * from categories;
describe categories;

-- alter table category
-- add constraint uk_category_name unique(name);