/*******************************************************************************
 * This file contains the methods needed to render the Query results.
 * 
 ******************************************************************************/

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_getRendererIndex(queryResult){
	
	rendererIndex = 0;		
	if(queryResult.displaySettings.as != null){
		switch(queryResult.displaySettings.as.trim().toLowerCase()){
			case 'table':			rendererIndex = 0; break;		
			case 'biggertable':		rendererIndex = 1; break;	
			case 'panels':			rendererIndex = 2; break;	
			case 'cards':			rendererIndex = 3; break;	
			case 'tiles':			rendererIndex = 4; break;	
			case 'csv':				rendererIndex = 5; break;	
			case 'xml':				rendererIndex = 6; break;	
			case 'json':			rendererIndex = 7; break;	
		}
	}
	
	return rendererIndex;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_createLables(queryResult){
	
	var fields = queryResult.detectedFields;
	var labels = {};
	
	for(var i in fields){
		
		labels[fields[i]] = fields[i];
	}
	
	return labels;
}
	
/*******************************************************************************
 * Renders the result of a single query and appends it to the target Element.
 * 
 * @param resultTarget the DOM or JQuery Element to which the results are
 *                     appended.
 * @param queryResult the json object holding all data related to a single query
 ******************************************************************************/
function cfw_query_renderQueryResult(resultTarget, queryResult){
	
	targetDiv = $(resultTarget);
	targetDiv.html(""); 
	
	//-----------------------------------
	// Create Title				
	var execSeconds = '';
	if(queryResult.execTimeMillis != -1){
		execSeconds = " ("+(queryResult.execTimeMillis / 1000).toFixed(3)+"s)";
	}
	
	var title = $('<h2>');
	if(queryResult.metadata.name == null){
		title.text('Query Results '+ execSeconds);
	}else{
		title.text(queryResult.metadata.name + execSeconds);
	}
	targetDiv.append(title);
						
	//-----------------------------------
	// Handle Description
	if(queryResult.metadata.description != null){
		targetDiv.append('<span>'+queryResult.metadata.description+'</span>');
	}
	
	
	//-----------------------------------
	// Check is result empty
	if(queryResult.results.length == 0 ){
		targetDiv.append('<p>The result is empty.</p>')
		return;
	}
	
	//-----------------------------------
	// Get Renderer Settings
	rendererIndex = cfw_query_getRendererIndex(queryResult);		
	labels = cfw_query_createLables(queryResult);	
	
	visibleFields = ((queryResult.displaySettings.visible != null)) ? queryResult.displaySettings.visible : queryResult.detectedFields;
	
	//-----------------------------------
	// Render Results
	var rendererSettings = {
			data: queryResult.results,
		 	//idfield: 'PK_ID',
		 	bgstylefield: null,
		 	textstylefield: null,
		 	titlefields: null,
		 	titleformat: '{0}',
		 	visiblefields: visibleFields,
		 	labels: labels,
		 	customizers: {},

			rendererSettings: {
				dataviewer: {
					//storeid: 'cfw-query',
					rendererIndex: rendererIndex,
					sortable: false,
					renderers: [
						{	label: 'Table',
							name: 'table',
							renderdef: {
								rendererSettings: {
									table: {filterable: false, narrow: true},
								},
							}
						},
						{	label: 'Bigger Table',
							name: 'table',
							renderdef: {
								rendererSettings: {
									table: {filterable: false},
								},
							}
						},
						
						{	label: 'Panels',
							name: 'panels',
							renderdef: {}
						},
						{	label: 'Cards',
							name: 'cards',
							renderdef: {}
						},
						{	label: 'Tiles',
							name: 'tiles',
							renderdef: {
								visiblefields: ['PK_ID', 'LOCATION', "EMAIL", "LIKES_TIRAMISU"],
								rendererSettings: {
									tiles: {
										popover: false,
										border: '2px solid black'
									},
								},
								
							}
						},
						{	label: 'CSV',
							name: 'csv',
							renderdef: {}
						},
						{	label: 'XML',
							name: 'xml',
							renderdef: {}
						},
						{	label: 'JSON',
							name: 'json',
							renderdef: {}
						}
					],
				},
			},
		};

	var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
	
	targetDiv.append(renderResult);
}
