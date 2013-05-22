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

				VIEW.xMargins = 320;
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
			right: 8px;
			bottom: 8px;
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
		div.floating {
			position: absolute;
			z-index: 1
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
		
	</style>
	
	<body>
	
		<div id="panel">
			<div style="padding: 3px">
				<img src="${resource(dir: 'images/icons', file: 'up.png')}" alt="Up" onclick="moveMap(0, -1)"/>
				<img src="${resource(dir: 'images/icons', file: 'down.png')}" alt="Down" onclick="moveMap(0, +1)"/>
				<span style="width: 16px; display: inline-block"> </span>
				<img id="zoomBtn0" src="${resource(dir: 'images/icons', file: 'zoom_0.png')}" alt="Large" onclick="setZoom(0)"/>
				<img id="zoomBtn1" src="${resource(dir: 'images/icons', file: 'zoom_1.png')}" alt="Medium" onclick="setZoom(1)"/>
				<img id="zoomBtn2" src="${resource(dir: 'images/icons', file: 'zoom_2.png')}" alt="Small" onclick="setZoom(2)"/>
				<img id="zoomBtn3" src="${resource(dir: 'images/icons', file: 'zoom_3.png')}" alt="Tiny" onclick="setZoom(3)"/>
				<br/>
				<img src="${resource(dir: 'images/icons', file: 'left.png')}" alt="Left" onclick="moveMap(-1, 0)"/>
				<img src="${resource(dir: 'images/icons', file: 'right.png')}" alt="Right" onclick="moveMap(+1, 0)"/>
				<span style="width: 16px; display: inline-block"> </span>
				<img id="brushBtn1" src="${resource(dir: 'images/icons', file: 'brush_1.png')}" title="Small Brush (1)" alt="Small (1)" onclick="setBrush(1)"/>
				<img id="brushBtn3" src="${resource(dir: 'images/icons', file: 'brush_3.png')}" title="Medium Brush (3)" alt="Medium (3)" onclick="setBrush(3)"/>
				<img id="brushBtn5" src="${resource(dir: 'images/icons', file: 'brush_5.png')}" title="Large Brush (5)" alt="Large (5)" onclick="setBrush(5)"/>
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
		<canvas id="map" width="100%"></canvas>
		<!--  <canvas id="map" width="1600px" height="1200px"></canvas> -->
		
	
	</body>
</html>