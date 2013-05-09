

create table hexweb (name varchar(16) not null, title varchar(256) not null, width int, height int, world smallint);

create table terrain(id int not null auto_increment, name varchar(32), title varchar(64), water smallint, primary key(id));

insert into terrain values(1, 'ocean', 'Ocean', true);
insert into terrain values(2, 'sea', 'Sea', true);
insert into terrain values(3, 'grass', 'Grassland', false);

