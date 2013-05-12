<html>
	<head>
		<title>Map</title>
		<g:javascript library="jquery"/>
		<g:javascript src="hexweb.js"/>
		<g:javascript src="editor.js"/>
		
		<g:javascript>
			var		BASE_PATH = "/hexweb/images/style/standard/";
			
			MAP.info = { id: ${mapInfo.id}, 
						 name: "${mapInfo.name}",
						 title: "${mapInfo.title}",
						 width: ${mapInfo.width},
						 height: ${mapInfo.height} }; 
			
	
			var		imagesToLoad = 0;


			window.onload = function() {
				VIEW.context = document.getElementById("map").getContext("2d");
				
				$.getJSON("/hexweb/api/map/"+MAP.info.id+"/info", function(data) {
					MAP.info = data.info;
					MAP.images = {}; // Hex images
					MAP.things = {}; // Thing data
					
					document.title = MAP.info.title;
					
					imagesToLoad = 0;
					for (var i=0; i < data.terrain.length; i++) {
						imagesToLoad++;
						MAP.images[data.terrain[i].id] = data.terrain[i];

						var d = data.terrain[i];
						d.image = new Image();
						d.image.src = BASE_PATH + "terrain/" + d.name + ".png";
						d.image.onload = function() {
							imagesToLoad--;
						}
						if (d.name == "ocean") {
							VIEW.terrainBrush = d.id;
						}
						
						var h = "<li id='t"+d.id+"' onclick='selectTerrain("+d.id+")'>";
						h += "<img src='"+BASE_PATH + "terrain/" + d.name +".png'/>";
						h += d.title;
						h +="</li>";
						$("#terrainPalette").append(h);
					}
					selectTerrain(VIEW.terrainBrush);
					
					for (var i=0; i < data.things.length; i++) {
						imagesToLoad++;
						var t = data.things[i];
						MAP.things[t.id] = t;
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
				document.getElementById("map").addEventListener("dblclick", dblclickMap, false);
			};
			
			
			function moveMap(mx, my) {
				VIEW.x += mx;
				VIEW.y += my;
				
				if (VIEW.x < 0) VIEW.x = 0;
				if (VIEW.y < 0) VIEW.y = 0;
				
				if (VIEW.x > MAP.info.width - VIEW.width) {
					VIEW.x = MAP.info.width - VIEW.width;
				}
				if (VIEW.y > MAP.info.height - VIEW.height) {
					VIEW.y = MAP.info.height - VIEW.height;
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
				<span style="width: 32px; display: inline-block"> </span>
				<img src="${resource(dir: 'images/icons', file: 'paint.png')}" alt="Paint" onclick="mode_paint()"/>
				<img src="${resource(dir: 'images/icons', file: 'select.png')}" alt="Paint" onclick="mode_select()"/>
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
			<div id="debug" style="width: 200px"/>
		</div>
		<canvas id="map" width="1600px" height="1200px"></canvas>
		
	
	</body>
</html>