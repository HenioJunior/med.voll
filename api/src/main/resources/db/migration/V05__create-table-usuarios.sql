create table usuarios(
    id bigint not null auto_increment,
    login varchar(100) not null unique,
    senha varchar(255) not null,
    primary key(id)
);

insert into usuarios values (1, 'ana.souza@voll.med', '$2a$12$ax9fnVdag6vGBQERusrKmO/gAlTd.AhJUm9lcWQvi.TjS1INJFUi.');