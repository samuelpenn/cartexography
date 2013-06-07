<html>
	<head>
		<title>Map</title>
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
		}
		
		div.inline {
			display: inline-block;
			margin-right: 1em;
			border: 1px solid #777777;
			border-radius: 3px;
			width: 160px;
			height: 180px;
			text-align: center;
			background-color: #ffffee;
		}
		
		div.inline h4 {
			margin: 0px;
		}
		
	</style>
	
	<body>
		<div id="header">
			<h1>HexWeb</h1>
		</div>	
		
		<div style="width: 800px">
			<g:each in="${maps}">
				<div class="inline">
					<h4><a href="${it.name}">${it.title}</a></h4>
					<img src="/hexweb/api/map/${it.id}/thumb?w=48"/><br/>
					(${it.width}x${it.height})
				</div>
			</g:each>
		</div>
	
	</body>
</html>