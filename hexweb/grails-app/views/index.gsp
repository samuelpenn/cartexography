<html>
	<head>
		<title>HexWeb</title>
		<g:javascript library="jquery"/>
		<g:javascript src="hexweb.js"/>
		
		<g:javascript>
			var		BASE_PATH = "/hexweb/images/style/standard/";
			
			LIST = ${maps};
			
			
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
		
	</style>
	
	<body>
		<div id="header">
			<h1>HexWeb</h1>
		</div>	
		
		<div>
			<div class="inline">
				<h4 style="color: #888888">new map</h4>
				<a href="admin/create"><img src="/hexweb/images/icons/newmap.png"/></a>
				<br/>
				&nbsp;
			</div>
			<g:each in="${maps}">
				<div class="inline">
					<h4>${it.title}</h4>
					<a href="map/${it.name}"><img src="/hexweb/api/map/${it.id}/thumb?w=128"/></a>
					<br/>
					(${it.width}x${it.height})
				</div>
			</g:each>
		</div>
	
	</body>
</html>