

create table mapinfo (id int not null auto_increment, 
        name varchar(16) not null, 
        title varchar(256) not null, 
        width int, 
        height int, 
        world smallint,
	scale int,
	template int,
        version int,
        primary key (id));

insert into mapinfo values(0, 'test', 'Test Map', 320, 400, false, 5000, 0, 0);

create table terrain(id int not null auto_increment, 
	mapinfo_id int,
        name varchar(32), 
        title varchar(64), 
        water smallint, 
	colour varchar(8),
        version int,
        primary key(id));

insert into terrain values( 1, 1, 'ocean', 'Ocean', true, '#0000FF', 0);
insert into terrain values( 2, 1, 'sea', 'Sea', true, '#7777FF', 0);
insert into terrain values( 3, 1, 'grass', 'Grassland', false, '#77FF77', 0);
insert into terrain values( 4, 1, 'hills', 'Hills', false, '#55EE55', 0);
insert into terrain values( 5, 1, 'woods', 'Woodland', false, '#44AA44', 0);
insert into terrain values( 6, 1, 'mixed_forest', 'Mixed Forest', false, '#338833', 0);
insert into terrain values( 7, 1, 'cropland', 'Cropland', false, '#88FF88', 0);
insert into terrain values( 8, 1, 'heath', 'Heath', false, '#77EE77', 0);
insert into terrain values( 9, 1, 'moors', 'Moors', false, '#55EE55', 0);
insert into terrain values(10, 1, 'dense_forest', 'Dense Forest', false, '#227722', 0);
insert into terrain values(11, 1, 'foothills', 'Foothills', false, '#44AA44', 0);
insert into terrain values(12, 1, 'mountains', 'Mountains', false, '#444444', 0);
insert into terrain values(13, 1, 'montane_forest', 'Montane Forest', false, '#447744', 0);
insert into terrain values(14, 1, 'alpine_forest', 'Alpine Forest', false, '#447744', 0);
insert into terrain values(15, 1, 'marsh', 'Mashes', false, '#77FF77', 0);
insert into terrain values(16, 1, 'swamp', 'Swamp', false, '#77FF77', 0);
insert into terrain values(17, 1, 'tundra', 'Tundra', false, '#77FF77', 0);
insert into terrain values(18, 1, 'snow', 'Snow', false, '#EEEEEE', 0);
insert into terrain values(19, 1, 'ice', 'Ice', true, '#F0F0F0', 0);
insert into terrain values(20, 1, 'dry', 'Dry Grassland', false, '#AAFF77', 0);
insert into terrain values(21, 1, 'desert', 'Desert', false, '#FFFF77', 0);

create table thing(id int not null auto_increment,
	mapinfo_id int,
	name varchar(32),
	title varchar(64),
	importance int,
	version int,
	primary key(id));

insert into thing values(1, 1, 'town', "Town", 2, 0);

create table place(id int not null auto_increment,
	mapinfo_id int,
	thing_id int,
	importance int,
	tile_x int,
	tile_y int,
	sub_x int,
	sub_y int,
	name varchar(32),
	title varchar(64),
	version int,
	primary key(id));


create table map (id bigint not null auto_increment,
        mapinfo_id int,
        x int,
        y int,
        terrain_id int,
        primary key(id));

