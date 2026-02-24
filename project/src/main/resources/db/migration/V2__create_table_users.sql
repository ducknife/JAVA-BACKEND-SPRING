create table
    users (
        user_id bigint primary key auto_increment not null,
        full_name varchar(100) not null,
        user_name varchar(100) not null,
        password varchar(100) not null,
        version bigint not null default 1,
        constraint uk_user_name unique (user_name)
    );

insert into
    users (full_name, user_name, password)
values
    ("Alan Karmat", "alankarmat", "karmat126"),
    ("Judy Hernandez", "judyhernandez", "judy217")