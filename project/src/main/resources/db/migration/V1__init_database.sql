create table
    categories (
        id bigint auto_increment primary key not null,
        name varchar(100) not null,
        constraint uk_category_name unique (name)
    );

insert into
    categories (name)
values
    ("Machine Learning"),
    ("Deep Learning")   