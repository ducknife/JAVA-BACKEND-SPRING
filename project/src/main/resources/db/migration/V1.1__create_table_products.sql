create table
    products (
        id bigint auto_increment primary key not null,
        name varchar(200) not null,
        price decimal(10, 2),
        category_id bigint not null,
        constraint fk_product_category foreign key (category_id) references categories (id),
        constraint uk_product_name unique (name)
    );

insert into
    products (name, price, category_id)
values
    (
        "Machine Learning Design Pattern",
        "120000.00",
        "1"
    ),
    ("Dive Into Deep Learning", "150000.00", "2")