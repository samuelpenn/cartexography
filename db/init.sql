

create table mapinfo (id int not null auto_increment, 
        name varchar(16) not null, 
        title varchar(256) not null, 
        width int, 
        height int, 
        world smallint,
        version int,
        primary key (id));

insert into mapinfo values(0, 'test', 'Test Map', 320, 400, false, 0);

create table terrain(id int not null auto_increment, 
	mapinfo_id int,
        name varchar(32), 
        title varchar(64), 
        water smallint, 
	colour varchar(8),
	letter varchar(2),
        version int,
        primary key(id));

insert into terrain values(1, 1, 'ocean', 'Ocean', true, '#0000FF', '  ', 0);
insert into terrain values(2, 1, 'sea', 'Sea', true, '#7777FF', '  ', 0);
insert into terrain values(3, 1, 'grass', 'Grassland', false, '#77FF77', '..', 0);
insert into terrain values(4, 1, 'hills', 'Hills', false, '#55EE55', '~~', 0);
insert into terrain values(5, 1, 'woods', 'Woodland', false, '#44AA44', 'ww', 0);
insert into terrain values(6, 1, 'mixed_forest', 'Mixed Forest', false, '#338833', 'WW', 0);
insert into terrain values(7, 1, 'cropland', 'Cropland', false, '#88FF88', '..', 0);


create table map (id bigint not null auto_increment,
        mapinfo_id int,
        x int,
        y int,
        terrain_id int,
        primary key(id));

