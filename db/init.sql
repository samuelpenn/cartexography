

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

insert into terrain values(0, 1, 'unknown', 'Unknown', false, '#000000', 0);
insert into terrain values(0, 1, 'oob', 'oob', false, '#000000', 0);
insert into terrain values(0, 1, 'ocean', 'Ocean', true, '#a7ccff', 0);
insert into terrain values(0, 1, 'sea', 'Sea', true, '#d2e5ff', 0);
insert into terrain values(0, 1, 'grass', 'Grassland', false, '#a1ea84', 0);
insert into terrain values(0, 1, 'hills', 'Hills', false, '#a1ea84', 0);
insert into terrain values(0, 1, 'broadleaf_woods', 'Woodland', false, '#6bd633', 0);
insert into terrain values(0, 1, 'broadleaf_forest', 'Broadleaf Forest', false, '#54b024', 0);
insert into terrain values(0, 1, 'cropland', 'Cropland', false, '#88FF88', 0);
insert into terrain values(0, 1, 'heath', 'Heath', false, '#a1ea84', 0);
insert into terrain values(0, 1, 'moors', 'Moors', false, '#a1ea84', 0);
insert into terrain values(0, 1, 'dense_forest', 'Dense Forest', false, '#227722', 0);
insert into terrain values(0, 1, 'foothills', 'Foothills', false, '#a0a548', 0);
insert into terrain values(0, 1, 'mountains', 'Mountains', false, '#a0a548', 0);
insert into terrain values(0, 1, 'montane_forest', 'Montane Forest', false, '#a0a548', 0);
insert into terrain values(0, 1, 'mountains_high', 'High Mountains', false, '#909538', 0);
insert into terrain values(0, 1, 'marsh', 'Mashes', false, '#a1ea84', 0);
insert into terrain values(0, 1, 'swamp', 'Swamp', false, '#a1ea84', 0);
insert into terrain values(0, 1, 'tundra', 'Tundra', false, '#77FF77', 0);
insert into terrain values(0, 1, 'snow', 'Snow', false, '#EEEEEE', 0);
insert into terrain values(0, 1, 'ice', 'Ice', true, '#F0F0F0', 0);
insert into terrain values(0, 1, 'dry', 'Dry Grassland', false, '#AAFF77', 0);
insert into terrain values(0, 1, 'desert_sand', 'Sandy Desert', false, '#FFFF77', 0);
insert into terrain values(0, 1, 'needleleaf_forest', 'Needleleaf Forest', false, '#338833', 0);
insert into terrain values(0, 1, 'desert_rock', 'Rocky Desert', false, '#FFFF77', 0);
insert into terrain values(0, 1, 'sandbanks', 'Sandbanks', true, '#7777FF', 0);
insert into terrain values(0, 1, 'volcano', 'Volcano', false, '#ff4444', 0);
insert into terrain values(0, 1, 'broadleaf_hills', 'Broadleaf Hills', false, '#338833', 0);
insert into terrain values(0, 1, 'boreal_forest', 'Boreal Forest', false, '#77FF77', 0);
insert into terrain values(0, 1, 'taiga', 'Taiga', false, '#77FF77', 0);
insert into terrain values(0, 1, 'mountains_ice', 'Ice Mountains', false, '#444444', 0);
insert into terrain values(0, 1, 'tropical_forest', 'Tropical Forest', false, '#77FF77', 0);
insert into terrain values(0, 1, 'tropical_woods', 'Tropical Woods', false, '#77FF77', 0);
insert into terrain values(0, 1, 'tropical_forest_hills', 'Tropical Forest Hills', false, '#77FF77', 0);
insert into terrain values(0, 1, 'tropical_woods_hills', 'Tropical Woods Hills', false, '#77FF77', 0);
insert into terrain values(0, 1, 'jungle', 'Jungle', false, '#77FF77', 0);

create table area (id int not null auto_increment,
        mapinfo_id int,
        name varchar(32),
	title varchar(256),
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
        primary key(id),
	unique key (mapinfo_id, x, y));

