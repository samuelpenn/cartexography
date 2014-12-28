

create table mapinfo (id int not null auto_increment,
        name varchar(16) not null,
        title varchar(256) not null,
        style varchar(16) not null,
        width int,
        height int,
        world smallint,
        scale int,
        background int,
        oob int,
        template int,
        version int,
        primary key (id));

insert into mapinfo values(1, 'base', 'Base', 'simple', 32, 40, false, 1000, 3, 2, 0, 0);
insert into mapinfo values(2, 'simple', 'Simple Style', 'simple', 320, 400, false, 5000, 3, 2, 1, 0);
insert into mapinfo values(3, 'standard', 'Standard Style', 'standard', 320, 400, false, 5000, 3, 2, 1, 0);

create table terrain(id int not null auto_increment,
        mapinfo_id int,
        name varchar(32),
        variants int default 0,
        title varchar(64),
        water smallint,
        colour varchar(8),
        ordering int,
        version int,
        primary key(id));

-- Base tile types
insert into terrain values(0, 1, 'unknown', 0, 'Unknown', false, '#000000', 0, 0);
insert into terrain values(0, 1, 'oob', 0, 'oob', false, '#000000', 0, 0);
insert into terrain values(0, 1, 'sea', 0, 'Sea', true, '#b7f9ff', 100, 0);

-- Simple tiles
insert into terrain values(0, 2, 'grass', 0, 'Grassland', false, '#aeea82', 200, 0);
insert into terrain values(0, 2, 'hills', 0, 'Hills', false, '#39af02', 210, 0);
insert into terrain values(0, 2, 'mountains', 0, 'Mountains', false, '#ae9334', 215, 0);
insert into terrain values(0, 2, 'shrubland', 0, 'Heath', false, '#79ef22', 300, 0);
insert into terrain values(0, 2, 'broadleaf_woods', 0, 'Woods', false, '#59cf02', 305, 0);
insert into terrain values(0, 2, 'broadleaf_forest', 0, 'Forest', false, '#39af02', 310, 0);
insert into terrain values(0, 2, 'broadleaf_woods_hills', 0, 'Wooded Hills', false, '#59cf02', 315, 0);
insert into terrain values(0, 2, 'broadleaf_forest_hills', 0, 'Forested Hills', false, '#39af02', 320, 0);
insert into terrain values(0, 2, 'marsh', 0, 'Marsh', false, '#39af02', 400, 0);
insert into terrain values(0, 2, 'swamp', 0, 'Swamp', false, '#39af02', 405, 0);
insert into terrain values(0, 2, 'tundra', 0, 'Tundra', false, '#ae9334', 500, 0);
insert into terrain values(0, 2, 'boreal_woods', 0, 'Boreal Woods', false, '#59cf02', 510, 0);
insert into terrain values(0, 2, 'snow', 0, 'Snow', false, '#f0f0f0', 550, 0);
insert into terrain values(0, 2, 'grass_dry', 0, 'Dry Grassland', false, '#f7fe8e', 600, 0);
insert into terrain values(0, 2, 'desert_sand', 0, 'Sandy Desert', false, '#f7fe8e', 620, 0);
insert into terrain values(0, 2, 'islands_grass', 1, 'Islands', true, '#b7f9ff', 120, 0);

-- Standard tiles
insert into terrain values(0, 3, 'ocean', 0, 'Ocean', true, '#8eebf3', 105, 0);
insert into terrain values(0, 3, 'ice', 0, 'Sea Ice', false, '#f0f0f0', 110, 0);

insert into terrain values(0, 3, 'grass', 0, 'Grassland', false, '#aeea82', 200, 0);
insert into terrain values(0, 3, 'cropland', 0, 'Cropland', false, '#88FF88', 205, 0);
insert into terrain values(0, 3, 'hills', 0, 'Hills', false, '#39af02', 210, 0);
insert into terrain values(0, 3, 'broken', 0, 'Broken', false, '#39af02', 212, 0);
insert into terrain values(0, 3, 'foothills', 0, 'Foothills', false, '#49af02', 213, 0);
insert into terrain values(0, 3, 'mountains', 0, 'Mountains', false, '#ae9334', 215, 0);
insert into terrain values(0, 3, 'mountains_high', 0, 'High Mountains', false, '#ae9334', 220, 0);
insert into terrain values(0, 3, 'montane_woods', 0, 'Montane Woods', false, '#ae9334', 230, 0);
insert into terrain values(0, 3, 'montane_forest', 0, 'Montane Forest', false, '#ae9334', 235, 0);

insert into terrain values(0, 3, 'shrubland', 0, 'Heath', false, '#79ef22', 300, 0);
insert into terrain values(0, 3, 'shrubland_hills', 0, 'Moors', false, '#79ef22', 301, 0);
insert into terrain values(0, 3, 'shrubland_broken', 0, 'Broken Shrub', false, '#79ef22', 302, 0);
insert into terrain values(0, 3, 'shrubland_foothills', 0, 'Shrub Foothills', false, '#79ef22', 303, 0);
insert into terrain values(0, 3, 'broadleaf_woods', 0, 'Broadleaf Woods', false, '#59cf02', 305, 0);
insert into terrain values(0, 3, 'broadleaf_forest', 0, 'Broadleaf Forest', false, '#39af02', 310, 0);
insert into terrain values(0, 3, 'broadleaf_woods_hills', 0, 'Broadleaf Wooded Hills', false, '#59cf02', 315, 0);
insert into terrain values(0, 3, 'broadleaf_forest_hills', 0, 'Broadleaf Forested Hills', false, '#39af02', 320, 0);
insert into terrain values(0, 3, 'broadleaf_woods_foothills', 0, 'Broadleaf Wooded Foothills', false, '#59cf02', 321, 0);
insert into terrain values(0, 3, 'broadleaf_forest_foothills', 0, 'Broadleaf Forested Foothills', false, '#39af02', 322, 0);

insert into terrain values(0, 3, 'needleleaf_woods', 0, 'Needleleaf Woods', false, '#59cf02', 325, 0);
insert into terrain values(0, 3, 'needleleaf_forest', 0, 'Needleleaf Forest', false, '#39af02', 330, 0);
insert into terrain values(0, 3, 'needleleaf_woods_hills', 0, 'Needleleaf Wooded Hills', false, '#59cf02', 335, 0);
insert into terrain values(0, 3, 'needleleaf_forest_hills', 0, 'Needleleaf Forested Hills', false, '#39af02', 340, 0);
insert into terrain values(0, 3, 'needleleaf_woods_foothills', 0, 'Needleleaf Wooded Foothills', false, '#59cf02', 345, 0);
insert into terrain values(0, 3, 'needleleaf_forest_foothills', 0, 'Needleleaf Forested Foothills', false, '#39af02', 350, 0);

insert into terrain values(0, 3, 'tropical_woods', 0, 'Tropical Woods', false, '#59cf02', 360, 0);
insert into terrain values(0, 3, 'tropical_forest', 0, 'Tropical Forest', false, '#39af02', 365, 0);
insert into terrain values(0, 3, 'tropical_woods_hills', 0, 'Tropical Wooded Hills', false, '#59cf02', 370, 0);
insert into terrain values(0, 3, 'tropical_forest_hills', 0, 'Tropical Forested Hills', false, '#39af02', 375, 0);
insert into terrain values(0, 3, 'tropical_woods_foothills', 0, 'Tropical Wooded Foothills', false, '#59cf02', 380, 0);
insert into terrain values(0, 3, 'tropical_forest_foothills', 0, 'Tropical Forested Foothills', false, '#39af02', 385, 0);

insert into terrain values(0, 3, 'marsh', 0, 'Marsh', true, '#39af02', 400, 0);
insert into terrain values(0, 3, 'swamp', 0, 'Swamp', true, '#39af02', 405, 0);

insert into terrain values(0, 3, 'tundra', 0, 'Tundra', false, '#d2e6aa', 500, 0);
insert into terrain values(0, 3, 'tundra_hills', 0, 'Tundra Hills', false, '#c1d59b', 505, 0);
insert into terrain values(0, 3, 'tundra_foothills', 0, 'Tundra Foothills', false, '#c1d59b', 507, 0);
insert into terrain values(0, 3, 'boreal_woods', 0, 'Boreal Woods', false, '#59cf02', 510, 0);
insert into terrain values(0, 3, 'boreal_forest', 0, 'Boreal Forest', false, '#39af02', 515, 0);
insert into terrain values(0, 3, 'boreal_woods_hills', 0, 'Boreal Wooded Hills', false, '#59cf02', 520, 0);
insert into terrain values(0, 3, 'boreal_forest_hills', 0, 'Boreal Forested Hills', false, '#39af02', 530, 0);

insert into terrain values(0, 3, 'snow', 0, 'Snow', false, '#e0e0e0', 550, 0);
insert into terrain values(0, 3, 'snow_hills', 0, 'Snow Hills', false, '#d0d0d0', 555, 0);
insert into terrain values(0, 3, 'snow_foothills', 0, 'Snow foothills', false, '#c0c0c0', 557, 0);
insert into terrain values(0, 3, 'mountains_ice', 0, 'Ice Mountains', false, '#a0a0a0', 560, 0);
insert into terrain values(0, 3, 'glacier', 0, 'Glacier', false, '#f0f0f0', 570, 0);

insert into terrain values(0, 3, 'grass_dry', 0, 'Dry Grassland', false, '#dcff9e', 600, 0);
insert into terrain values(0, 3, 'hills_dry', 0, 'Dry Hills', false, '#ccec94', 605, 0);
insert into terrain values(0, 3, 'desert_sand', 0, 'Sandy Desert', false, '#f9ff9e', 620, 0);
insert into terrain values(0, 3, 'desert_rock', 0, 'Rocky Desert', false, '#f9ff9e', 625, 0);


insert into terrain values(0, 1, 'foothills', 0, 'Foothills', false, '#a0a548', 0);
insert into terrain values(0, 1, 'mountains', 0, 'Mountains', false, '#a0a548', 0);
insert into terrain values(0, 1, 'montane_forest', 0, 'Montane Forest', false, '#a0a548', 0);
insert into terrain values(0, 1, 'mountains_high', 0, 'High Mountains', false, '#909538', 0);
insert into terrain values(0, 1, 'tundra', 0, 'Tundra', false, '#77FF77', 0);
insert into terrain values(0, 1, 'snow', 0, 'Snow', false, '#EEEEEE', 0);
insert into terrain values(0, 1, 'ice', 0, 'Ice', true, '#F0F0F0', 0);
insert into terrain values(0, 1, 'dry', 0, 'Dry Grassland', false, '#AAFF77', 0);
insert into terrain values(0, 1, 'desert_sand', 0, 'Sandy Desert', false, '#FFFF77', 0);
insert into terrain values(0, 1, 'needleleaf_forest', 0, 'Needleleaf Forest', false, '#338833', 0);
insert into terrain values(0, 1, 'desert_rock', 0, 'Rocky Desert', false, '#FFFF77', 0);
insert into terrain values(0, 1, 'sandbanks', 0, 'Sandbanks', true, '#7777FF', 0);
insert into terrain values(0, 1, 'volcano', 0, 'Volcano', false, '#ff4444', 0);
insert into terrain values(0, 1, 'broadleaf_hills', 0, 'Broadleaf Hills', false, '#338833', 0);
insert into terrain values(0, 1, 'boreal_forest', 0, 'Boreal Forest', false, '#77FF77', 0);
insert into terrain values(0, 1, 'taiga', 0, 'Taiga', false, '#77FF77', 0);
insert into terrain values(0, 1, 'mountains_ice', 0, 'Ice Mountains', false, '#444444', 0);
insert into terrain values(0, 1, 'tropical_forest', 0, 'Tropical Forest', false, '#77FF77', 0);
insert into terrain values(0, 1, 'tropical_woods', 0, 'Tropical Woods', false, '#77FF77', 0);
insert into terrain values(0, 1, 'tropical_forest_hills', 0, 'Tropical Forest Hills', false, '#77FF77', 0);
insert into terrain values(0, 1, 'tropical_woods_hills', 0, 'Tropical Woods Hills', false, '#77FF77', 0);
insert into terrain values(0, 1, 'jungle', 0, 'Jungle', false, '#77FF77', 0);


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
        variant int default 0,
        area_id int default 0,
        primary key(id),
        unique key (mapinfo_id, x, y));


create table path(
        id bigint not null auto_increment,
        mapinfo_id int,
        name varchar(64),
        style varchar(16),
        thickness1 int,
        thickness2 int,
        version int,
        primary key(id),
        unique key (mapinfo_id, name));

create table vertex(
        id bigint not null auto_increment,
        path_id bigint,
        vertex int not null,
        x int,
        y int,
        sub_x int,
        sub_y int,
        version int,
        primary key (id),
        unique key(path_id, vertex));

create table label(
        id bigint not null auto_increment,
        mapinfo_id int not null,
        tile_x int,
        tile_y int,
        sub_x int,
        sub_y int,
        name varchar(32),
        title varchar(64),
        font_size int,
        rotation int,
        style varchar(16),
        version int,
        primary key(id));
        