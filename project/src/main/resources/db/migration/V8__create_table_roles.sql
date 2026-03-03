create table
    roles (
        id bigint primary key auto_increment,
        name varchar(50) not null,
        constraint uk_name unique (name)
    );

insert into roles (name) values 
("ROLE_ADMIN"),
("ROLE_USER"),
("ROLE_COLLABORATOR")