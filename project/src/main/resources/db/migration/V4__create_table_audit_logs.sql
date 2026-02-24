create table audit_logs (
	id bigint primary key auto_increment not null,
	log_type varchar(100) not null,
    log_message text,
	create_at timestamp not null default current_timestamp
);