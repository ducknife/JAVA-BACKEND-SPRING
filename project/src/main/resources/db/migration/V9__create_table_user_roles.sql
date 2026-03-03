create table
    user_roles (
        user_id bigint not null,
        role_id bigint not null,
        constraint fk_user_roles_user foreign key (user_id) references users(user_id),
        constraint fk_user_roles_role foreign key (role_id) references roles(id)
    );

insert into user_roles values 
(1, 1),
(1, 2),
(2, 3)