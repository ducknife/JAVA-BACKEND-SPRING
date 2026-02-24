use astar_hub;

create table audit_log (
	id bigint primary key auto_increment not null,
	log_type varchar(100) not null,
    log_message text,
	create_at timestamp not null default current_timestamp
);

select * from audit_log;
/* 
alter table audit_log
add column create_at timestamp not null default current_timestamp; */