

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
insert into terrain values( 6, 1, 'broadleaf_forest', 'Broadleaf Forest', false, '#338833', 0);
insert into terrain values( 7, 1, 'cropland', 'Cropland', false, '#88FF88', 0);
insert into terrain values( 8, 1, 'heath', 'Heath', false, '#77EE77', 0);
insert into terrain values( 9, 1, 'moors', 'Moors', false, '#55EE55', 0);
insert into terrain values(10, 1, 'dense_forest', 'Dense Forest', false, '#227722', 0);
insert into terrain values(11, 1, 'foothills', 'Foothills', false, '#44AA44', 0);
insert into terrain values(12, 1, 'mountains', 'Mountains', false, '#444444', 0);
insert into terrain values(13, 1, 'montane_forest', 'Montane Forest', false, '#447744', 0);
insert into terrain values(14, 1, 'mountains_high', 'High Mountains', false, '#444444', 0);
insert into terrain values(15, 1, 'marsh', 'Mashes', false, '#77FF77', 0);
insert into terrain values(16, 1, 'swamp', 'Swamp', false, '#77FF77', 0);
insert into terrain values(17, 1, 'tundra', 'Tundra', false, '#77FF77', 0);
insert into terrain values(18, 1, 'snow', 'Snow', false, '#EEEEEE', 0);
insert into terrain values(19, 1, 'ice', 'Ice', true, '#F0F0F0', 0);
insert into terrain values(20, 1, 'dry', 'Dry Grassland', false, '#AAFF77', 0);
insert into terrain values(21, 1, 'desert_sand', 'Sandy Desert', false, '#FFFF77', 0);
insert into terrain values(22, 1, 'needleleaf_forest', 'Needleleaf Forest', false, '#338833', 0);
insert into terrain values(23, 1, 'desert_rock', 'Rocky Desert', false, '#FFFF77', 0);
insert into terrain values(24, 1, 'sandbanks', 'Sandbanks', true, '#7777FF', 0);
insert into terrain values(25, 1, 'volcano', 'Volcano', false, '#ff4444', 0);
insert into terrain values(26, 1, 'broadleaf_hills', 'Broadleaf Hills', false, '#338833', 0);
insert into terrain values(27, 1, 'boreal_forest', 'Boreal Forest', false, '#77FF77', 0);
insert into terrain values(28, 1, 'taiga', 'Taiga', false, '#77FF77', 0);
insert into terrain values(29, 1, 'mountains_ice', 'Ice Mountains', false, '#444444', 0);
insert into terrain values(30, 1, 'rainforest_tropical', 'Tropical Rainforest', false, '#77FF77', 0);
insert into terrain values(31, 1, 'jungle', 'Jungle', false, '#77FF77', 0);

create table area (id int not null auto_increment,
        mapinfo_id int,
        name varchar(64),
        parent_id int,
        primary key(id));

create table thing(id int not null auto_increment,
	mapinfo_id int,
	name varchar(32),
	title varchar(64),
	importance int,
	version int,
	primary key(id));

insert into thing values(1, 1, 'village', 'Village', 1, 0);
insert into thing values(2, 1, 'town', 'Town', 2, 0);
insert into thing values(3, 1, 'city', 'City', 3, 0);
insert into thing values(4, 1, 'castle', 'Castle', 2, 0);
insert into thing values(5, 1, 'keep', 'Keep', 1, 0);
insert into thing values(6, 1, 'largecity', 'Large city', 3, 0);
insert into thing values(7, 1, 'ruins', 'Ruins', 2, 0);
insert into thing values(8, 1, 'mine', 'Mine', 1, 0);
insert into thing values(9, 1, 'peak', 'Peak', 3, 0);
insert into thing values(10, 1, 'monument', 'Monument', 2, 0);
insert into thing values(11, 1, 'label', 'Label', 3, 0);

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

