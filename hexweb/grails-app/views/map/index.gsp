<html>
	<head>
		<title>Map</title>
		<g:javascript library="jquery"/>
		<g:javascript src="hexweb.js"/>
		<g:javascript src="editor.js"/>
		
		<g:javascript>
			
			MAP.info = { id: ${mapInfo.id}, 
						 name: "${mapInfo.name}",
						 title: "${mapInfo.title}",
						 width: ${mapInfo.width},
						 height: ${mapInfo.height},
						 oob: ${mapInfo.oob },
						 background: ${mapInfo.background },
						 style: "${mapInfo.style }",
						 world: ${mapInfo.world } }; 
			
	
			var		imagesToLoad = 0;
			var		BASE_PATH = "/hexweb/images/style/"+MAP.info.style+"/";
			VIEW.imageBase = BASE_PATH;


			window.onload = function() {
				VIEW.context = document.getElementById("map").getContext("2d");
				document.getElementById("map").onselectstart = function() { return false; };
				
				$.getJSON("/hexweb/api/map/"+MAP.info.id+"/info", function(data) {
					MAP.info = data.info;
					MAP.images = {}; // Hex images
					MAP.things = {}; // Thing data
					MAP.paths = {}; // Path data
					
					document.title = MAP.info.title;
					
					imagesToLoad = 0;
					// Build the terrain palette.
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
					}
					selectTerrain(VIEW.terrainBrush);

					// Build the thing palette.
					console.log("Number of things: " + data.things.length);
					for (var i=0; i < data.things.length; i++) {
						console.log(i);
						imagesToLoad++;
						var t = data.things[i];
						if (t == null) {
							continue;
						}
						console.log("things["+i+":"+t.id+"] ["+t.name+"]");
						MAP.things[t.id] = t;
						t.image = new Image();
						t.image.src = BASE_PATH + "things/" + t.name + ".png";
						console.log(t.image.src);
						t.image.onload = function() {
							imagesToLoad--;
						}						
					}
					var url = VIEW.imageBase + "/things/" + MAP.things[1].name + ".png";
					$("#thingMenu").attr("src", url);
					
				});

				VIEW.xMargins = 96;
				VIEW.yMargins = 16;
				document.getElementById("map").addEventListener("mousedown", clickMap, false);
				document.getElementById("map").addEventListener("mouseup", unclickMap, false);
				document.getElementById("map").addEventListener("mouseout", unclickMap, false);
				document.getElementById("map").addEventListener("mousemove", drawMap, false);
				document.getElementById("map").addEventListener("dblclick", dblclickMap, false);
				
				setBrush(1);
				setTimeout(function () {
						setZoom(0);
					}, 500);
			};
			
			
			function moveMap(mx, my) {
				VIEW.x += mx * VIEW.currentScale.step;
				VIEW.y += my * VIEW.currentScale.step;
				
				if (VIEW.x %2 == 1) {
					VIEW.x --;
				}

				if (VIEW.x < 0) VIEW.x = 0;
				if (VIEW.y < 0) VIEW.y = 0;
				
				if (VIEW.x > MAP.info.width - VIEW.width) {
					VIEW.x = MAP.info.width - VIEW.width;
					if (VIEW.x %2 == 1) {
						VIEW.x --;						
					}
				}
				if (VIEW.y > MAP.info.height - VIEW.height) {
					VIEW.y = MAP.info.height - VIEW.height;
				}
				
				refreshMap();
			};			

			function moveMapTo(x, y) {
				VIEW.x = x;
				VIEW.y = y;
				
				if (VIEW.x %2 == 1) {
					VIEW.x --;
				}

				if (VIEW.x < 0) VIEW.x = 0;
				if (VIEW.y < 0) VIEW.y = 0;
				
				if (VIEW.x > MAP.info.width - VIEW.width) {
					VIEW.x = MAP.info.width - VIEW.width;
					if (VIEW.x %2 == 1) {
						VIEW.x --;						
					}
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
			left: 96px;
			right: 8px;
			bottom: 8px;
		}
		#panel {
			background-color: #f0f0f0;
			border: 1px solid #a0a0a0;
			border-radius: 5px;
			height: 98%;
			width: 80px;
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
		div.floating {
			position: absolute;
			top: 200px;
			left: 500px;
			width: 300px;
			height: 200px;
			border: 1px solid black;
			background-color: #ffffff;
			opacity: 0.75;
		}
		.selectedButton {
			border: 1px solid black;
		}
		.tilebox {
			display: inline-block;
			height: 72px;
			width: 160px;
			border-radius: 5px;
			background-color: #eeeeee;
			margin: 3px;
		}
		.tilebox img {
			float: left;
		}
		.tilebox .text {
			float: right;
		}
		.menu {
			background-image: url(${resource(dir: 'images/icons', file: 'popout.png')});
			width: 58px;
			padding-right: 6px;
		}
		#pathDialog h4 {
			margin: 0px;
			padding-left: 5px;
			padding-top: 5px;
			color: #444444;
		}
		#pathLength {
			float: right;
			padding-right: 5px;
		}
		.button {
			background-color: #eeeeee;
			text: black;
			border: 1px solid black;
			border-radius: 3px;
			padding-left: 5px;
			padding-right: 5px;
			padding-top: 2px;
			padding-bottom: 2px;
		}
		#infobar {
			position: absolute;
			bottom: 0px;
			left: 110%;
			width: 300px;
			opacity: 0.75;
			background-color: white;
			z-index: 1;
			height: 32px;
			border: 1px solid #777777;
			border-radius: 3px;
			padding: 3px;
		}
	</style>
	
	<body>
	
		<div id="panel">
			<div style="padding: 3px">
				<img src="${resource(dir: 'images/icons', file: 'up.png')}" alt="Up" onclick="moveMap(0, -1)"/>
				<img src="${resource(dir: 'images/icons', file: 'down.png')}" alt="Down" onclick="moveMap(0, +1)"/>
				<br/>
				<img src="${resource(dir: 'images/icons', file: 'left.png')}" alt="Left" onclick="moveMap(-1, 0)"/>
				<img src="${resource(dir: 'images/icons', file: 'right.png')}" alt="Right" onclick="moveMap(+1, 0)"/>
				<br />
				<b>X: </b> <span id="x-orig-view">?</span><br/>
				<b>Y: </b> <span id="y-orig-view">?</span><br/>
				<img id="zoomBtn0" src="${resource(dir: 'images/icons', file: 'zoom_0.png')}" title="Large" alt="Large" onclick="setZoom(0)"/>
				<img id="zoomBtn1" src="${resource(dir: 'images/icons', file: 'zoom_1.png')}" title="Medium" alt="Medium" onclick="setZoom(1)"/>
				<img id="zoomBtn2" src="${resource(dir: 'images/icons', file: 'zoom_2.png')}" title="Small" alt="Small" onclick="setZoom(2)"/>
				<img id="zoomBtn3" src="${resource(dir: 'images/icons', file: 'zoom_3.png')}" title="Tiny" alt="Tiny" onclick="setZoom(3)"/>
				<img id="zoomBtn4" src="${resource(dir: 'images/icons', file: 'zoom_4.png')}" title="Tiny+" alt="Tiny+" onclick="setZoom(4)"/>
				<img id="showGrid" src="${resource(dir: 'images/icons', file: 'grid.png')}" title="Grid toggle" alt="Grid toggle" onclick="toggleGrid()"/>
				<img class="menu" id="mapMenu" src="${resource(dir: 'images/icons', file: 'map.png')}" title="Map thumbnail" alt="Map thumbnail" onclick="openMap()"/>
				<div id="brushIcons">
					<br/>
					<img id="brushBtn1" src="${resource(dir: 'images/icons', file: 'brush_1.png')}" title="Small Brush (1)" alt="Small (1)" onclick="setBrush(1)"/>
					<img id="brushBtn3" src="${resource(dir: 'images/icons', file: 'brush_3.png')}" title="Medium Brush (3)" alt="Medium (3)" onclick="setBrush(3)"/>
					<img id="brushBtn5" src="${resource(dir: 'images/icons', file: 'brush_5.png')}" title="Large Brush (5)" alt="Large (5)" onclick="setBrush(5)"/>
				</div>
				<div id="pathIcons">
					<br/>
					<img id="pathBtnSel" src="${resource(dir: 'images/icons', file: 'path_sel.png')}" title="Select Path" alt="Small (1)" onclick="setPathEdit('SELECT')"/>
					<img id="pathBtnNew" src="${resource(dir: 'images/icons', file: 'path_new.png')}" title="Add Path" alt="Medium (3)" onclick="setPathEdit('NEW')"/>
				</div>
				<br/>
				<br/>
				<img class="menu" id="terrainMenu" onclick="openTerrainMenu()"/>
				<br/>
				<img class="menu" id="thingMenu" onclick="openThingMenu()"/>
				<br/>
				<img id="pathStyleROAD" src="${resource(dir: 'images/icons', file: 'path_road.png')}" onclick="setPathStyle(BRUSH_STYLE.ROAD)"/>
				<img id="pathStyleRIVER" src="${resource(dir: 'images/icons', file: 'path_river.png')}" onclick="setPathStyle(BRUSH_STYLE.RIVER)"/>
				<img id="pathStyleCOAST" src="${resource(dir: 'images/icons', file: 'path_coast.png')}" onclick="setPathStyle(BRUSH_STYLE.COAST)"/>
			</div>
			<div id="debug" style="width: 200px"/>
		</div>
		<canvas id="map" width="100%"></canvas>
		<!--  <canvas id="map" width="1600px" height="1200px"></canvas> -->
		
		<div id="infobar">${mapInfo.title}</div>
		
	
	</body>
</html>