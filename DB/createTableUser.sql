use astar_hub;

create table user (
	user_id bigint primary key auto_increment not null,
    full_name varchar(100) not null,
    user_name varchar(100) not null,
    password varchar(100) not null,
    constraint uk_user_name unique(user_name)
);

insert into user values
(1, 'Alan Karmat', 'ducknife', 'hash1@'),
(2, 'Judy Hernandez', 'judymaam', 'hash2@');

select * from user;