<html>
	<head>
		<title>Map</title>
		<g:javascript library="jquery"/>
		<g:javascript src="hexweb.js"/>
		
		<g:javascript>
			var		BASE_PATH = "/cartexography/images/style/standard/";
			var 	CONTEXT_PATH = "${application.contextPath}";
			var		API_PATH = CONTEXT_PATH+"/api";
			var		ICONS_PATH = CONTEXT_PATH + "/images/icons";
			
			MAP.info = { id: ${mapInfo.id}, 
						 name: "${mapInfo.name}",
						 title: "${mapInfo.title}",
						 width: ${mapInfo.width},
						 height: ${mapInfo.height} }; 
			
	
			var		imagesToLoad = 0;


			window.onload = function() {
				VIEW.context = document.getElementById("map").getContext("2d");
				
				$.getJSON("/cartexography/api/map/"+MAP.info.id+"/info", function(data) {
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
					}
					
					for (var i=0; i < data.things.length; i++) {
						imagesToLoad++;
						var t = data.things[i];
						MAP.things[t.id] = t;
						t.image = new Image();
						t.image.src = BASE_PATH + "things/" + t.name + ".png";
						t.image.onload = function() {
							imagesToLoad--;
						}
					}
					
				});
				
				setTimeout(function () {
						VIEW.xMargins = 16;
						VIEW.yMargins = 16;
						VIEW.x = ${x};
						VIEW.y = ${y};
						setZoom(${zoom});
					}, 500);
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
			left: 8px;
			right: 8px;
			bottom: 8px;
		}		
	</style>
	
	<body>	
		<canvas id="map" width="100%"></canvas>
	</body>
</html>