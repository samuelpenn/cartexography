<html>
	<head>
		<title>Import Map</title>
		<g:javascript library="jquery"/>
		<g:javascript src="hexweb.js"/>
		
		<g:javascript>
			window.onload = function() {
				

									
			};			
		</g:javascript>
		
		<r:require modules="uploadr"/>
		
		<r:layoutResources/>
	</head>
	
	<style>
	</style>
	
	<body>	
		<h4>Import Map</h4>
		
		<div id="import">
			<uploadr:add name="mapUploadr" allowedExtensions="map" path="/tmp"
			             viewable="false" noSound="true"
			             controller="import"
			             action="upload"/>
			<r:layoutResources/>
		</div>
	</body>
</html>