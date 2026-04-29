create table
    role_permissions (
        role_id bigint not null,
        permission_id bigint not null,
        constraint fk_role_permissions_role foreign key (role_id) references roles (id),
        constraint fk_role_permissions_permission foreign key (permission_id) references permissions (id)
    );

insert into
    role_permissions
values
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (3, 1), 
    (3, 4),
    (2, 1)