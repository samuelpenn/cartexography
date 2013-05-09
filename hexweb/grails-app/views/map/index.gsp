<html>
	<head>
		<title>Map</title>
		<g:javascript library="jquery"/>
		
		<g:javascript>
			var		BASE_PATH = "/hexweb/images/style/standard/terrain/";
			var		MAP_ID = 1;
			var		X = 0;
			var		Y = 0;
			var		WIDTH = 32;
			var		HEIGHT = 20;
			
			var		TERRAIN = 0;
			
			var		imagesToLoad = 0;
			var		images = {};
			var 	context = null;
			var		mapData = null;

			function refreshMap() {
				$.getJSON("/hexweb/api/map/"+MAP_ID+"/map?x="+X+"&y="+Y+"&w="+WIDTH+"&h="+HEIGHT, function(data) {
					var mapData = data;

					for (var y=0; y < 20; y++) {
						for (var x=0; x < 32; x++) {
							var t = mapData[y][x];
							context.drawImage(images[t].image, x * 48 + 8, y*56 + (x%2 * 28) + 8, 65, 56);
						}
					}
					
					$("#x-orig-view").html(X)
					$("#y-orig-view").html(Y)
				
				});
			};
			
			function selectTerrain(id) {
				$("#t"+TERRAIN).removeClass("sterrain");
				TERRAIN = id;
				$("#t"+TERRAIN).addClass("sterrain");
			}
			
			function clickMap(event) {
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
				
				context.drawImage(images[TERRAIN].image, x * 48 + 8, y*56 + (x%2 * 28) + 8, 65, 56);
			}

			window.onload = function() {
				context = document.getElementById("map").getContext("2d");

				$.getJSON("/hexweb/api/map/"+MAP_ID+"/terrain", function(data) {
					imagesToLoad = 0;
					for (var i=0; i < data.length; i++) {
						imagesToLoad++;
						images[data[i].id] = data[i];
						
						
						var d = data[i];
						d.image = new Image();
						d.image.src = BASE_PATH + d.name + ".png";
						d.image.onload = function() {
							imagesToLoad--;
						}
						if (d.name == "ocean") {
							TERRAIN = d.id;
						}
						
						var h = "<li id='t"+d.id+"' onclick='selectTerrain("+d.id+")'>";
						h += "<img src='"+BASE_PATH + d.name +".png'/>";
						h += d.title;
						h +="</li>";
						$("#terrainPalette").append(h);
					}
					selectTerrain(TERRAIN);
				});

				while (imagesToLoad > 0) {
				}
				refreshMap();
				document.getElementById("map").addEventListener("mousedown", clickMap, false);
			};
			
			
			function moveMap(mx, my) {
				X += mx;
				Y += my;
				
				if (X < 0) X = 0;
				if (Y < 0) Y = 0;
				
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
		li.sterrain {
			background-color: #ddddff;
			font-weight: bold;
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
			<div>
				<ul id="terrainPalette">
				</ul>
			</div>
		</div>
		<canvas id="map" width="1600px" height="1200px"></canvas>
		
		<div id="debug" style="clear: both"/>
	
	</body>
</html>