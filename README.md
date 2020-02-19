# Cartexography

**Author:** Samuel Penn  
**License:** GPL v3

## What's New

Moving the code base from _Grails_ to _SparkJava_ and _React_. The code
will be non-functional whilst this move is in progress,

## About

Cartexography is a web based hex mapper, designed for drawing large scale 
fantasy (or other) maps, for regions the size of countries, continents or 
worlds. Backed by a MySQL database, the idea is to allow very large, sparse 
maps, so a single map can be used to include both detail of a country as 
well as an outline of the entire world.

Hex tiles are bitmap based, but support for a vector layer to show roads,
rivers and coastlines is planned. Places such as towns and cities can also
be placed on their own layer, and are not tied to the hexagonal grid.

Editing of maps is done via the browser using Javascript/AJAX - there is
no requirement for plugins such as Java or Flash. The server can also
generate static images for inclusion in a web page or for output for
printing. Being able to consume maps in both print and online formats is
a desired goal of Cartexography.

This is effectively version 2 of my Mapcraft application.
See http://mapcraft.glendale.org.uk/ for the details of the original.
It was originally called 'Hexweb'.

The current status is "Get It Working", so it's neither pretty nor usable
for an end user. There is no documentation other than code comments, and
no installation or setup instructions.

See https://github.com/samuelpenn/cartexography/wiki/Screenshots for an
idea of what it looks like at the moment.


Samuel Penn  
sam@glendale.org.uk
