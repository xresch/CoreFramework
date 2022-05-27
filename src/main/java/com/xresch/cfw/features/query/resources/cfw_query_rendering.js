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
			case 'panels':			rendererIndex = 1; break;	
			case 'cards':				rendererIndex = 2; break;	
			case 'tiles':				rendererIndex = 3; break;
			case 'statuslist':			rendererIndex = 4; break;
			case 'statusbar':			rendererIndex = 5; break;
			case 'statusbarreverse':	rendererIndex = 6; break;
			
			case 'statusmap':			rendererIndex = 7; break;
			case 'statusmap_2to1':		rendererIndex = 8; break;
			case 'statusmap_4to1':		rendererIndex = 9; break;
			case 'statusmap_8to1':		rendererIndex = 10; break;
			case 'statusmap_1to2':		rendererIndex = 11; break;
			case 'statusmap_1to4':		rendererIndex = 12; break;
			case 'statusmap_1to8':		rendererIndex = 14; break;
			
			case 'csv':				rendererIndex = 14; break;	
			case 'json':			rendererIndex = 15; break;	
			case 'xml':				rendererIndex = 16; break;	
			
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
 * 
 ******************************************************************************/
function cfw_query_createCustomizers(queryResult, fields){
	
	var fieldFormats = queryResult.displaySettings.fieldFormats;
	var customizers = {};
	
	var defaultCustomizer = cfw_query_customizerCreateDefault();
	for(var i in fields){
		var fieldname = fields[i];
		
		if(fieldFormats == null || fieldFormats[fieldname] == null){
			customizers[fieldname] = defaultCustomizer;
		}else{
			formatterArray = fieldFormats[fieldname];
			customizers[fieldname] = cfw_query_customizerCreateCustom(formatterArray);
			
		}
		
	}

	return customizers;
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_customizerCreateDefault(){
		
	return function (record, value, rendererName, fieldname){
	
		
		//----------------------------------------------
		// Strings and Numbers
		if (_.isString(value)){
			
			let trimmed = value.trim();
			if(trimmed == ""){	return "&nbsp;"; }
			if(trimmed.startsWith('http')){	return '<a href="'+value+'" target="blank" style="color: unset;">'+value+'</a>'; }
			
			return $('<span>').text(value);
			
		}else if(_.isNumber(value)){
			
			let lower = fieldname.trim().toLowerCase();
			
			if(lower == "time" 
			|| lower == "timestamp"
			|| lower == "_epoch"){
				return CFW.format.epochToTimestamp(value);
			}
			
			return value;
		}
		
		//----------------------------------------------
		// Booleans
			if(typeof value === "boolean"){
			let booleanClass = value ? 'bg-success' : 'bg-danger';
			return '<span class="format-base text-white text-center '+booleanClass+' m-0">'+value+'</span>';
		}
	
		//----------------------------------------------
		// Nulls
		if(value === null || value === undefined){
			return '<span class="format-base text-white text-center badge-primary m-0">NULL</span>';
		}
		
		//----------------------------------------------
		// Arrays and Objects
		if(typeof value === 'object'){
			return JSON.stringify(value).replaceAll(',',', ');
		}

		return value;
	
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_customizerCreateCustom(formatterArray, span){
		
	return function (record, value, rendererName, fieldname){
	
		var resultSpan = span;
		if(resultSpan == null){
			resultSpan = $('<span class="format-base">');
		}
		resultSpan.text(value);
		
		for(var i in formatterArray){
			var current = formatterArray[i];
			
			var formatterName = current[0].toLowerCase();
			
			switch(formatterName){
				
				case 'align': 				cfw_query_formatAlign(resultSpan, value, current[1]); break;
				case 'boolean': 			cfw_query_formatBoolean(resultSpan, value, current[1], current[2], current[3], current[4]); break;
				case 'case':				cfw_query_formatCase(resultSpan, record, value, rendererName, fieldname, current); break;
				case 'css':		 			cfw_query_formatCSS(resultSpan, value, current[1], current[2]); break;
				case 'date': 				cfw_query_formatTimestamp(resultSpan, value, current[1]); break;
				case 'decimals': 			cfw_query_formatDecimals(resultSpan, value, current[1]); break;
				case 'duration': 			cfw_query_formatDuration(resultSpan, value, current[1]); break;
				case 'ea'+'stere'+'ggs': 	cfw_query_formatEa_sterE_ggs(resultSpan, value); break;
				case 'link': 				cfw_query_formatLink(resultSpan, value, current[1], current[2], current[3], current[4]); break;
				case 'lowercase': 			cfw_query_formatLowercase(resultSpan); break;
				case 'none': 				return $('<span class="">').text(value); break;
				case 'prefix': 				cfw_query_formatPrefix(resultSpan, value, current[1]); break;
				case 'postfix': 			cfw_query_formatPostfix(resultSpan, value, current[1]); break;
				case 'separators':			cfw_query_formatSeparators(resultSpan, value, current[1], current[2]); break;
				case 'shownulls':			cfw_query_formatShowNulls(resultSpan, value, current[1]); break;
				case 'thousands': 			cfw_query_formatThousands(resultSpan, value, current[1], current[2], current[3]); break;
				case 'threshold': 			cfw_query_formatThreshold(resultSpan, value, current[1], current[2], current[3], current[4], current[5], current[6]); break;
				case 'timestamp': 			cfw_query_formatTimestamp(resultSpan, value, current[1]); break;
				case 'uppercase': 			cfw_query_formatUppercase(resultSpan); break;
			}	
		}

		return resultSpan;
	
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatAlign(span, value, alignment){
	
	// set defaults
	if(alignment == null ){
		alignment = 'center';
	}
	
	lower = alignment.toLowerCase();
	span.addClass('w-100 text-'+lower);
	
	if(lower == "center"){
		span.removeClass('text-left text-right');
	}else if(lower == "left"){
		span.removeClass('text-center text-right');
	}else {
		span.removeClass('text-left text-center');
	}

}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatBoolean(span, value, trueBGColor, falseBGColor, trueTextColor, falseTextColor){
	
	// set defaults
	if(trueBGColor == null )	{ trueBGColor = 'cfw-excellent'; }
	if(falseBGColor == null )	{ falseBGColor = 'cfw-danger'; }
	if(trueTextColor == null )	{ trueTextColor = 'white'; }
	if(falseTextColor == null )	{ falseTextColor = 'white'; }

	span.addClass('text-center');
	
	if(typeof value === "boolean"){
		let color = value ? trueBGColor : falseBGColor;
		let textColor = value ? trueTextColor : falseTextColor;
		CFW.colors.colorizeElement(span, color, "bg");
		CFW.colors.colorizeElement(span, textColor, "text");
		
		return span;

	}else if(_.isString(value)){
		var lower = value.trim().toLowerCase();
		
		if(lower == "true" || lower == "false"){
			let color = lower == "true" ? trueBGColor : falseBGColor;
			let textColor = lower == "true" ? trueTextColor : falseTextColor;
			CFW.colors.colorizeElement(span, color, "bg");
			CFW.colors.colorizeElement(span, textColor, "text");
			return span;
		}
		
	}
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatCase(span, record, value, rendererName, fieldname, caseParams){
		
	/**
		example input
		['case'
			, "<10"
				, "green"
			, [">=30", "<40"]	
				, ['css', 'border', '1px solid black']	
			, ["<=60", "OR", ">90"]
				, [
					['css', 'border', '3px solid white']	
					,['css', 'background-color', 'red']	
				]
		]
	
	 */

	if(caseParams == null || caseParams.size == 2){
		return;
	}

	//---------------------------------
	// Loop all Conditions and Formatters
	for(let i = 1; i <= caseParams.length-2; i+=2 ){
		
		let tempCondition = caseParams[i];

		//---------------------------
		// Prepare Condition Array
		let conditionArray = tempCondition;
		if(typeof conditionArray == 'string'){
			conditionArray = [tempCondition];
		}
		
		//---------------------------
		// Iterate Conditions
		var hasMatched = null; 
		var andOperation = true;
		
		//default case is an empty array

		if(conditionArray.length == 0){
			hasMatched = true;
		}
		
		for(let index in conditionArray){
			let condition = conditionArray[index];

			// default to equals
			if(condition == null){
				return hasMatched &= (value == null);
			}
			
			if(condition.toLowerCase() == "or"){
				andOperation = false;
				continue;
			}else if(condition.toLowerCase() == "and"){
				andOperation = true;
				continue;
			}

			let conditionResult;
			if(condition.startsWith("=="))				{ conditionResult = (value == condition.substring(2)); }
			else if(condition.startsWith("!="))			{ conditionResult = (value != condition.substring(2)); }
			else if(condition.startsWith("<="))			{ conditionResult = (value <= condition.substring(2)); }
			else if(condition.startsWith(">="))			{ conditionResult = (value >= condition.substring(2)); }
			else if(condition.startsWith("<"))			{ conditionResult = (value < condition.substring(1)); }
			else if(condition.startsWith(">"))			{ conditionResult = (value > condition.substring(1)); }
			else if(condition.startsWith("startsWith:")){ conditionResult = ( (""+value).startsWith(condition.substring("startsWith:".length)) ); }
			else if(condition.startsWith("endsWith:"))	{ conditionResult = ( (""+value).endsWith(condition.substring("endsWith:".length)) ); }
			else if(condition.startsWith("contains:"))	{ conditionResult = ( (""+value).includes(condition.substring("contains:".length)) ); }
			else if(condition.startsWith("~="))	{ 
				var regexExpression = condition.substring("~=".length);
				var regex = new RegExp(regexExpression, "g")
				
				conditionResult = ( (""+value).match(regex) ); 
			}else { 
				// Default to equals
				conditionResult = (value == condition); 
			}
			
			//-------------------------------
			// Combine results
			if(hasMatched == null){	
				hasMatched = conditionResult;
				continue;
			}
			if(andOperation){
				hasMatched &= conditionResult;
			}else{
				hasMatched |= conditionResult;
			}
			
		}
		
		//---------------------------
		// Apply format
		if(hasMatched){
			let formatting = caseParams[i+1];
			// treat string as color
			if(typeof formatting == 'string'){
				CFW.colors.colorizeElement(span, formatting, "bg");
				CFW.colors.colorizeElement(span, "white", "text");
				
			}else if(Array.isArray(formatting)){
				
				if(formatting.length > 0){
					
					// convert single formatter into array of formatter
					if( !Array.isArray(formatting[0]) ){
						formatting = [formatting]
					}
					
					//-----------------------------------
					// Create Formatting Function and Execute
					let formatFunction = cfw_query_customizerCreateCustom(formatting, span);
					formatFunction(record, value, rendererName, fieldname);
				 }
				
			}else{
				// unsupported, ignore
			}
			
			break;
		}
	}

}
	

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatCSS(span, value, propertyName, propertyValue){
	
	// set defaults
	if(propertyName == null ){
		propertyName = 'font-weight';
		if(propertyValue == null ){
			propertyValue = 'bold';
		}
	}
	
	span.css(propertyName, propertyValue);
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatDate(span, value, format){
		
	// set defaults
	if(format == null ){
		format = "YYYY-MM-DD";
	}
	
		
	if(value != null){
		span.text(new  moment(value).format(format));
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatDecimals(span, value, precision){
		
	// set defaults
	if(precision == null ){
		precision = 2;
	}
	
	var valueToProcess = value;
	
	var stringValue = span.text();
	if(stringValue != null && !isNaN(stringValue)){
		valueToProcess = parseFloat(stringValue);
	}
	

	if(valueToProcess != null && !isNaN(valueToProcess)){
		span.text(valueToProcess.toFixed(precision));
	}

}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatDuration(span, value, durationUnit){
		// set defaults
	if(durationUnit == null ){
		durationUnit = "ms";
	}
	var millisValue = value;
	
	switch(durationUnit){
		case 'ns':	millisValue = value / 1000000; break
		case 'us':	millisValue = value / 1000; break
		case 's':	millisValue = value * 1000;
		case 'm':	millisValue = value * 1000 * 60;
		case 'h':	millisValue = value * 1000 * 60 * 60;
	}
	
	
	
	span.addClass('text-right');
	
	if(value != null){
		if(!isNaN(value)){
			span.text(CFW.format.millisToDuration(millisValue));
		}else{
			span.text(value);
		}
	}

}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatEa_sterE_ggs(span, value){
		
	value = span.text();
	
	value = value.replaceAll("o", '<i class="fas fa-e'+'gg"></i>')
				.replaceAll("O", '<i class="fas fa-e'+'gg"></i>')
				.replaceAll("0", '<i class="fas fa-e'+'gg"></i>')
				;
				
	span.html(value).find("i").each(function(){
		$(this).css('color', CFW.colors.randomHSL(65,100,55,70));
	})
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatLink(span, value, linkText, displayAs, icon, target){
		
	if(linkText == null )	{linkText = "Open Link";}
	if(displayAs == null )	{displayAs = "button";}
	if(icon == null )		{icon = "fa-external-link-square-alt";}
	if(target == null )		{target = "blank";}
	
	var linkElement = $('<a>'+linkText+'</a>');
	linkElement.attr('href', value)
			   .attr('target', value)
			   .css('color', 'unset');
	
	if(displayAs == 'button'){
		linkElement.attr('role', 'button')
				   .addClass('btn btn-sm btn-primary');
	}	
	
	if(icon != null){
		linkElement.prepend('<i class="fas '+icon+'"></i>&nbsp;');
	}
	
	span.html('');
	span.append(linkElement);
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatLowercase(span){
	
	span.addClass('text-lowercase');
	span.removeClass('text-uppercase');

}



/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatPrefix(span, value, prefix){
	
	value = span.text();
	span.text(prefix+value);
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatPostfix(span, value, postfix){
	
	value = span.text();
	span.text(value+postfix);
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatSeparators(span, value, separator, eachDigit ){
	
	// set defaults
	if(separator == null )	{ separator = "'"; }
	if(eachDigit == null )	{ eachDigit = 3; }
	
	span.addClass('text-right');
	
		var valueToProcess = value;
	
	var stringValue = span.text();
	if(stringValue != null && !isNaN(stringValue)){
		valueToProcess = parseFloat(stringValue);
	}
	

	if(valueToProcess != null){
		span.text(CFW.format.numberSeparators(valueToProcess, separator, eachDigit));
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatShowNulls(span, value, isVisible){
	
	// set defaults
	if(isVisible == null )	{ isVisible = true; }
	
	if(value === null || value === undefined){
		if(isVisible){
			span.text("NULL");
			span.addClass('format-base bg-primary text-white');
		}else{
			span.html("&nbsp;");
		}
	}
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatThousands(span, value, isBytes, decimals, addBlank ){
	
	// set defaults
	if(isBytes == null )	{ isBytes = false; }
	if(decimals == null )	{ decimals = 1; }
	if(addBlank == null )	{ addBlank = true; }
	
	span.addClass('text-right');
	
	if(value != null){
		span.html(CFW.format.numbersInThousands(value, decimals, addBlank, isBytes));
	}

}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatThreshold(span, value, excellent, good, warning, emergency, danger, type){
	
	// set defaults
	if(excellent 	=== undefined )		{ excellent = 0; }
	if(good 		=== undefined )		{ good = 20; }
	if(warning 		=== undefined )		{ warning = 40; }
	if(emergency 	=== undefined )		{ emergency = 60; }
	if(danger 		=== undefined )		{ danger = 80; }
	if(type 		=== undefined )		{ type = 'bg'; }
	
	span.addClass('text-right font-weight-bold');
	
	var style = CFW.colors.getThresholdStyle(value, excellent, good, warning, emergency, danger, false);
	
	if(type == 'bg'){
		CFW.colors.colorizeElement(span, "white", "text");
	}
	
	CFW.colors.colorizeElement(span, style, type, "2px");

}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatTimestamp(span, value, format){
	
	if(format == null )	{ format = "YYYY-MM-DD HH:mm:ss"; }
		
	if(value != null){
		span.text(new  moment(value).format(format));
	}

}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatUppercase(span){
	
	span.addClass('text-uppercase');
	span.removeClass('text-lowercase');

}


/*******************************************************************************
 * Renders the result of a single query and appends it to the target Element.
 * 
 * @param resultTarget the DOM or JQuery Element to which the results are
 *                     appended.
 * @param queryResultPayload the payload received from the server holding one or
 *        multipe query results.
 ******************************************************************************/
function cfw_query_renderAllQueryResults(resultTarget, queryResultsPayload){
	resultTarget.html("");
	//-----------------------------------
	// Handle Emptiness 
	if(queryResultsPayload == null || queryResultsPayload.length == 0){
		resultTarget.append("The result is empty.");
	}
	
	//-----------------------------------
	// Handle MultiDisplay
	var multidisplayColumns=1;
	
	if(queryResultsPayload[0].globals.multidisplay != null){
		multidisplayColumns = queryResultsPayload[0].globals.multidisplay;
	}
	
	if(multidisplayColumns < 1){ multidisplayColumns = 1; }
	if(multidisplayColumns > 6){ multidisplayColumns = 6; }
	
	//get percent column
	var colClass = "col-"+Math.floor(100/multidisplayColumns)+"pc";
	
	//-----------------------------------
	// Iterate all Query results
	
	var currentRow = $('<div class="row m-0 flex-grow-1">');
	resultTarget.append(currentRow);
	
	for(var i = 0; i < queryResultsPayload.length; i++){
		var currentColumn = $('<div class="col-percent '+colClass+'">');
		
		var currentResults = queryResultsPayload[i];
			
		cfw_query_renderQueryResult(currentColumn, currentResults);
		currentRow.append(currentColumn);
		
		if((i+1) % multidisplayColumns == 0 
		&& i < queryResultsPayload.length-1 ){

			currentRow = $('<div class="row m-0 flex-grow-1">');
			resultTarget.append(currentRow);
		}
	}
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
	if(queryResult.metadata.name != null){
		var execSeconds = '';
		if(queryResult.execTimeMillis != -1){
			execSeconds = " ("+(queryResult.execTimeMillis / 1000).toFixed(3)+"s)";
		}
		var title = $('<p class="query-title">');
		
		title.text(queryResult.metadata.name + execSeconds);
		targetDiv.append(title);
	}
	
						
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
	// Get StyleFields
	var bgstylefield = null;
	if(queryResult.displaySettings.bgstylefield != undefined){
		bgstylefield = queryResult.displaySettings.bgstylefield;
	}
	
	var textstylefield = null;
	if(queryResult.displaySettings.textstylefield != undefined){
		textstylefield = queryResult.displaySettings.textstylefield;
	}
	//-----------------------------------
	// Zoom
	if(queryResult.displaySettings.zoom != null){
		var zoomString = ""+queryResult.displaySettings.zoom;
		if(!zoomString.endsWith("%")){ zoomString += "%";}
		targetDiv.css('zoom', zoomString);
	}
	
	
	//-----------------------------------
	// Do chart if it is Chart
	if(queryResult.displaySettings.as == 'chart'){
		cfw_query_renderAsChart(targetDiv, queryResult);
		return;
	}
	
	//-----------------------------------
	// Get Renderer Settings
	var rendererName = "dataviewer";
	if(queryResult.displaySettings.menu == false){
		if(queryResult.displaySettings.as != null){
			rendererName = queryResult.displaySettings.as.trim().toLowerCase();
		}else{
			// default to table
			rendererName = "table";
		}
	}
	
	rendererIndex = cfw_query_getRendererIndex(queryResult);		
	labels = cfw_query_createLables(queryResult);	
	
	visibleFields = ((queryResult.displaySettings.visiblefields != null)) ? queryResult.displaySettings.visiblefields : queryResult.detectedFields;
	titleFields = ((queryResult.displaySettings.titlefields != null)) ? queryResult.displaySettings.titlefields : null;
	titleFormat = ((queryResult.displaySettings.titleformat != null)) ? queryResult.displaySettings.titleformat : null;
	
	customizers = cfw_query_createCustomizers(queryResult, queryResult.detectedFields);
	
	//-----------------------------------
	// Render Results
	var rendererSettings = {
			data: queryResult.results,
		 	//idfield: 'PK_ID',
		 	bgstylefield: bgstylefield,
		 	textstylefield: textstylefield,
		 	titlefields: titleFields,
		 	titleformat: titleFormat,
		 	visiblefields: visibleFields,
		 	labels: labels,
		 	customizers: customizers,

			rendererSettings: {
				table: { filterable: false, narrow: true},
				panels: { narrow: true},
				cards: { narrow: true},
				tiles: { 
					popover: false,
					border: '2px solid black'
				},
				csv: {},
				json: {},
				xml: {},
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
						
						{	label: 'Panels',
							name: 'panels',
							renderdef: {
								rendererSettings: {
									panels: {narrow: true},
								},
							}
						},
						{	label: 'Cards',
							name: 'cards',
							renderdef: {
								rendererSettings: {
									cards: {narrow: true},
								},
							}
						},
						{	label: 'Tiles',
							name: 'tiles',
							renderdef: {
								rendererSettings: {
									tiles: {
										popover: false,
										border: '2px solid black'
									},
								},
								
							}
						},
						{	label: 'Status List',
							name: 'statuslist',
							renderdef: {}
						},
						{	label: 'Status Bar',
							name: 'statusbar',
							renderdef: {}
						},
						{	label: 'Status Bar Reverse',
							name: 'statusbarreverse',
							renderdef: {}
						},
						{	label: 'Status Map(1:1)',
							name: 'statusmap',
							renderdef: {}
						},
						{	label: 'Status Map(2:1)',
							name: 'statusmap_2to1',
							renderdef: {}
						},
						{	label: 'Status Map(4:1)',
							name: 'statusmap_4to1',
							renderdef: {}
						},
						{	label: 'Status Map(8:1)',
							name: 'statusmap_8to1',
							renderdef: {}
						},
						{	label: 'Status Map(1:2)',
							name: 'statusmap_1to2',
							renderdef: {}
						},
						{	label: 'Status Map(1:4)',
							name: 'statusmap_1to4',
							renderdef: {}
						},
						{	label: 'Status Map(1:8)',
							name: 'statusmap_1to8',
							renderdef: {}
						},
						{	label: 'CSV',
							name: 'csv',
							renderdef: {}
						},
						{	label: 'JSON',
							name: 'json',
							renderdef: {}
						},
						{	label: 'XML',
							name: 'xml',
							renderdef: {}
						}
					],
				},
			},
		};

	var renderResult = CFW.render.getRenderer(rendererName).render(rendererSettings);	
	
	targetDiv.append(renderResult);
}

/*******************************************************************************
 * Renders the result of a single query as a chart.
 * 
 * @param resultTarget the JQuery Element to which the results are
 *                     appended.
 * @param queryResultPayload the payload received from the server holding one or
 *        multiple query results.
 ******************************************************************************/
function cfw_query_renderAsChart(resultTarget, queryResult){

	var settings = queryResult.displaySettings;
	
	//---------------------------------
	// Prepare TitleFields
	var seriesColumns = settings.groupby;
	var titlefields = seriesColumns;
	if(seriesColumns == null || seriesColumns.length == 0){
		// use second as default
		let keys = Object.keys(queryResult.results[0]);
		titlefields = [keys[1]];
	}
	
	//---------------------------------
	// Use first Column if not specified
	var xColumn;
	if(CFW.utils.isNullOrEmpty(settings.x)){
		let keys = Object.keys(queryResult.results[0]);
		xColumn = keys[0];
	}else{
		var xColumn = settings.x.trim();
	}
	
	
	//---------------------------------
	// Use last Column if not specified
	var yColumn;
	if(CFW.utils.isNullOrEmpty(settings.y)){
		let keys = Object.keys(queryResult.results[0]);
		yColumn = keys[keys.length-1];
	}else{
		var yColumn = settings.y.trim();
	}
	
	//---------------------------------
	// Fallback to Defaults if undefined
	var defaultSettings = {
		type: "line",
		xtype: "time",
		ytype: "linear",
		stacked: false,
		legend: false,
		axes: true,
		ymin: 0,
		ymax: null,
		pointradius: 1
	};
	
	var settings = Object.assign({}, defaultSettings, settings);
	
	//---------------------------
	// Render Settings
	var dataToRender = {
		data: queryResult.results,
		titlefields: titlefields, 
		titleformat: null, 
		labels: {},
		customizers: {},
		rendererSettings:{
			chart: {
				charttype: settings.type,
				// How should the input data be handled groupbytitle|arrays 
				datamode: 'groupbytitle',
				xfield: xColumn,
				yfield: yColumn,
				//the type of the x axis: linear|logarithmic|category|time
				xtype: 			settings.xtype,
				//the type of the y axis: linear|logarithmic|category|time
				ytype: 			settings.ytype,
				stacked: 		settings.stacked,
				showlegend: 	settings.legend,
				showaxes: 		settings.axes,
				ymin: 			settings.ymin,
				ymax: 			settings.ymax,
				pointradius: 	settings.pointradius,
				padding: 2
			}
		}
	};
									
	//--------------------------
	// Render Widget
	var renderer = CFW.render.getRenderer('chart');
	
	var renderResult = CFW.render.getRenderer('chart').render(dataToRender);	
	
	targetDiv.append(renderResult);
}

