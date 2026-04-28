create table
    users (
        user_id bigint primary key auto_increment not null,
        fullname varchar(100) not null,
        username varchar(100) not null,
        password varchar(100) not null,
        version bigint not null default 1,
        constraint uk_user_name unique (username)
    );