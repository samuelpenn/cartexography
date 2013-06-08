<html>
	<head>
		<title>New Map</title>
		<g:javascript library="jquery"/>
		<g:javascript src="hexweb.js"/>
		
		<g:javascript>
			function selectTemplate(id) {
				$("#t"+id).addClass("selected");
			}
			
			
		</g:javascript>
		
		<r:layoutResources/>
		<r:layoutResources disposition="defer"/>
	</head>
	
	<style>
		div#header {
			background-image: url('/hexweb/images/img/hexweb.png');
			background-repeat: no-repeat;
			height: 72px;
		}
		#header h1 {
			padding-top: 12px;
			margin-left: 80px;
			border-bottom: 3px solid #777777;
		}
		
		div.inline {
			display: inline-block;
			margin-right: 1em;
			margin-bottom: 1em;
			border: 3px solid #777777;
			border-radius: 5px;
			width: 160px;
			height: 180px;
			text-align: center;
			background-color: white;
		}
		
		div.inline h4 {
			margin: 0px;
		}
		div.inline img {
		}
		div#info h5 {
			font-family: sans;
			width: 6em;
			display: inline-block;
			margin-bottom: 0px;
		}
		
		div.selected {
			border: 5px solid red;
		}
		
	</style>
	
	<body onload="selectTemplate(1)">
		<div id="header">
			<h1>HexWeb - New Map</h1>
		</div>	
		
		<div>
			<div class="inline">
				<h4 style="color: #888888">back</h4>
				<a href="../"><img src="/hexweb/images/icons/back.png"/></a>
				<br/>
				&nbsp;
			</div>
			<g:each in="${maps}">
				<div class="inline" id="t${it.id}">
					<h4>${it.title}</h4>
					<img src="/hexweb/api/map/${it.id}/thumb?w=128" onclick="selectTemplate(1);"/>
					<br/>
					&nbsp;
				</div>
			</g:each>
		</div>
		
		<div id="info">
			<g:form  name="createMap" url="[action: 'createMap']">
				<p>
					<h5>Name</h5> <input id="mapName" name="name"></input>
					<br/>
					<h5>Title</h5> <input id="mapTitle" name="title"></input>
					<br/>
					<h5>Width</h5> <input id="mapWidth" name="width"></input>
					<br/>
					<h5>Height</h5> <input id="mapHeight" name="height"></input>
					<br/>
					<h5>Scale (m)</h5> <input id="mapScale" name="scale"></input>
				</p>
				<button type="submit">Create</button>
			</g:form>
			
		</div>
	
	</body>
</html>