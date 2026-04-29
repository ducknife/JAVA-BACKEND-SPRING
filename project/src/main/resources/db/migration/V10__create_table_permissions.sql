create table
    permissions (
        id bigint primary key auto_increment,
        name varchar(50) not null,
        constraint uk_permission_name unique (name)
    );

insert into
    permissions (name)
values
    ("user:read"),
    ("user:write"),
    ("user:delete"),
    ("post:write")

/* format resource:action */