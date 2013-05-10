<html>
	<head>
		<title>Map</title>
		<g:javascript library="jquery"/>
		
		<g:javascript>
			var		BASE_PATH = "/hexweb/images/style/standard/";
			var		MAP_ID = ${mapInfo.id};
			var		X = 0;
			var		Y = 0;
			var		WIDTH = 32;
			var		HEIGHT = 20;
			
			var 	MAP_TITLE = "${mapInfo.title}";
			var		MAP_WIDTH = ${mapInfo.width};
			var		MAP_HEIGHT = ${mapInfo.height};
			
			var		TERRAIN = 0;
			var		THING = 0;
			
			var 	PAINT_MODE_TERRAIN = "TERRAIN";
			var		PAINT_MODE_THING_NEW = "THING_NEW"
			
			var		PAINT_MODE = PAINT_MODE_TERRAIN;
			
			var		imagesToLoad = 0;
			var		images = {};
			var		things = {};
			var 	context = null;
			var		mapData = null;
			
			var		MAP = new Object();

			function refreshMap() {
				$.getJSON("/hexweb/api/map/"+MAP_ID+"/map?x="+X+"&y="+Y+"&w="+WIDTH+"&h="+HEIGHT, function(data) {
					var mapData = data.map;
					var	placeData = data.places;
					
					MAP.map = mapData;
					MAP.places = placeData;

					for (var y=0; y < 20; y++) {
						for (var x=0; x < 32; x++) {
							var t = mapData[y][x];
							context.drawImage(images[t].image, x * 48 + 8, y*56 + (x%2 * 28) + 8, 65, 56);
						}
					}
					
					$("#x-orig-view").html(X + " / " + MAP_WIDTH)
					$("#y-orig-view").html(Y + " / " + MAP_HEIGHT)
					
					for (var i=0; i < placeData.length; i++) {
						drawPlace(placeData[i]);
					}
				
				});
			};
			
			function drawPlace(p) {
				var x = p.x * 48 - 24 + (p.sx * 65)/100;
				var y = p.y * 56 + (p.x %2 * 28) - 20 + (p.sy * 56)/100;
				context.drawImage(things[p.thing_id].image, x, y, 65, 56);
			}
			
			function selectTerrain(id) {
				PAINT_MODE = PAINT_MODE_TERRAIN;

				$("#t"+TERRAIN).removeClass("selected");
				TERRAIN = id;
				$("#t"+TERRAIN).addClass("selected");
			}
			
			function selectThing(id) {
				PAINT_MODE = PAINT_MODE_THING_NEW;

				$("#th"+THING).removeClass("selected");
				THING = id;
				$("#th"+THING).addClass("selected");
			}
			
			var MOUSE_DOWN = 0;
			function unclickMap(event) {
				MOUSE_DOWN = 0;
			}
			
			function clickMap(event) {
				MOUSE_DOWN = 1;
				drawMap(event);
			}
			
			function drawMap(event) {
				if (MOUSE_DOWN == 0) {
					return;
				}
				
				if (PAINT_MODE == PAINT_MODE_TERRAIN) {
					var	px, py;
					var canoffset = $("#map").offset();
					px = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - Math.floor(canoffset.left);
					py = event.clientY + document.body.scrollTop + document.documentElement.scrollTop - Math.floor(canoffset.top) + 1;
					
					px -= 8;
					py -= 8;
					var x = Math.floor(px / 48);
					if (x %2 == 1) {
						py -= 28;
					} 
					var y = Math.floor(py / 56);
					
					if (y < 0 || x < 0 || y >= HEIGHT || x >= WIDTH) {
						return;
					}
					
					context.drawImage(images[TERRAIN].image, x * 48 + 8, y*56 + (x%2 * 28) + 8, 65, 56);
					
					$.getJSON("/hexweb/api/map/"+MAP_ID+"/update?x="+(X+x)+"&y="+(Y+y)+"&terrain="+TERRAIN);
				} else if (PAINT_MODE == PAINT_MODE_THING_NEW) {
					MOUSE_DOWN = 0; // Only paint one thing per click
					var	px, py;
					var canoffset = $("#map").offset();
					px = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - Math.floor(canoffset.left);
					py = event.clientY + document.body.scrollTop + document.documentElement.scrollTop - Math.floor(canoffset.top) + 1;
					
					px -= 8;
					py -= 8;
					var x = Math.floor(px / 48);
					var sx = Math.floor(((px - x*48.0) * 100.0) / 48.0);
					if (x %2 == 1) {
						py -= 28;
					} 
					var y = Math.floor(py / 56);
					var sy = Math.floor(((py - y*56.0) * 100.0) / 56.0);

					if (y < 0 || x < 0 || y >= HEIGHT || x >= WIDTH) {
						return;
					}

					//context.drawImage(images[TERRAIN].image, x * 48 + 8, y*56 + (x%2 * 28) + 8, 65, 56);
					$.getJSON("/hexweb/api/map/"+MAP_ID+"/place?x="+(X+x)+"&y="+(Y+y)+"&sx="+sx+"&sy="+sy+"&thingId="+THING, function (data) {
						MAP.places.push(data);
						drawPlace(data);
					});
					
				}
			}

			window.onload = function() {
				context = document.getElementById("map").getContext("2d");
				
				$.getJSON("/hexweb/api/map/"+MAP_ID+"/info", function(data) {
					MAP_WIDTH = data.info.width;
					MAP_HEIGHT = data.info.height;
					MAP_TITLE = data.info.title;
					
					document.title = MAP_TITLE;
					
					imagesToLoad = 0;
					for (var i=0; i < data.terrain.length; i++) {
						imagesToLoad++;
						images[data.terrain[i].id] = data.terrain[i];
						
						
						var d = data.terrain[i];
						d.image = new Image();
						d.image.src = BASE_PATH + "terrain/" + d.name + ".png";
						d.image.onload = function() {
							imagesToLoad--;
						}
						if (d.name == "ocean") {
							TERRAIN = d.id;
						}
						
						var h = "<li id='t"+d.id+"' onclick='selectTerrain("+d.id+")'>";
						h += "<img src='"+BASE_PATH + "terrain/" + d.name +".png'/>";
						h += d.title;
						h +="</li>";
						$("#terrainPalette").append(h);
					}
					selectTerrain(TERRAIN);
					
					for (var i=0; i < data.things.length; i++) {
						imagesToLoad++;
						var t = data.things[i];
						things[t.id] = t;
						t.image = new Image();
						t.image.src = BASE_PATH + "things/" + t.name + ".png";
						t.image.onload = function() {
							imagesToLoad--;
						}
						
						var h = "<li id='th"+t.id+"' onclick='selectThing("+t.id+")'>";
						h += "<img src='"+BASE_PATH + "things/" + t.name +".png'/>";
						h += t.title;
						h +="</li>";
						$("#thingPalette").append(h);
						
					}
					
				});

				while (imagesToLoad > 0) {
				}
				refreshMap();
				document.getElementById("map").addEventListener("mousedown", clickMap, false);
				document.getElementById("map").addEventListener("mouseup", unclickMap, false);
				document.getElementById("map").addEventListener("mouseout", unclickMap, false);
				document.getElementById("map").addEventListener("mousemove", drawMap, false);
			};
			
			
			function moveMap(mx, my) {
				X += mx;
				Y += my;
				
				if (X < 0) X = 0;
				if (Y < 0) Y = 0;
				
				if (X > MAP_WIDTH - WIDTH) {
					X = MAP_WIDTH - WIDTH;
				}
				if (Y > MAP_HEIGHT - HEIGHT) {
					Y = MAP_HEIGHT - HEIGHT;
				} 
				
				refreshMap();
			};
			
		</g:javascript>
		
		<r:layoutResources/>
		<r:layoutResources disposition="defer"/>
	</head>
	
	<style>
		canvas {
			border: 1px solid #a0a0a0;
			border-radius: 5px;
			position: absolute;
			top: 8px;
			left: 320px;
		}
		#panel {
			background-color: #f0f0f0;
			border: 1px solid #a0a0a0;
			border-radius: 5px;
			height: 1200px;
			width: 300px;
			position: absolute;
			left: 8px;
			top: 8px;
		}
		#terrainPanel {
			height: 400px;
			overflow: scroll;
		}
		#thingPanel {
			height: 300px;
			overflow: scroll;
		}
		li.selected {
			background-color: #ddddff;
			font-weight: bold;
		}
		.palette {
			margin-left: 0px;
			padding-left: 0px;
		}
		.palette li {
			list-style: none;
			margin-left: 0px;
			padding-left: 0px;
		}
		
	</style>
	
	<body>
	
		<div id="panel">
			<div>
				<img src="${resource(dir: 'images/icons', file: 'up.png')}" alt="Up" onclick="moveMap(0, -4)"/>
				<img src="${resource(dir: 'images/icons', file: 'down.png')}" alt="Down" onclick="moveMap(0, +4)"/>
				<img src="${resource(dir: 'images/icons', file: 'left.png')}" alt="Left" onclick="moveMap(-4, 0)"/>
				<img src="${resource(dir: 'images/icons', file: 'right.png')}" alt="Right" onclick="moveMap(+4, 0)"/>
			</div>
			<div>
				<p><b>X: </b> <span id="x-orig-view">?</span></p>
				<p><b>Y: </b> <span id="y-orig-view">?</span></p>
			</div>
			<div id="terrainPanel">
				<ul id="terrainPalette" class="palette">
				</ul>
			</div>
			<div id="thingPanel">
				<ul id="thingPalette" class="palette">
				</ul>
			</div>
		</div>
		<canvas id="map" width="1600px" height="1200px"></canvas>
		
		<div id="debug" style="clear: both"/>
	
	</body>
</html>