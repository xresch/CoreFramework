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
			if(trimmed.startsWith('http')){	return '<a href="'+value+'" target="blank">'+value+'</a>'; }
			
			return value;
			
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
			return '<span class="format-base text-white text-center '+booleanClass+' m-1">'+value+'</span>';
		}
	
		//----------------------------------------------
		// Nulls
		if(value === null || value === undefined){
			return '<span class="badge badge-primary m-1">NULL</span>';
		}
		
		//----------------------------------------------
		// Arrays
		if(Array.isArray(value)){
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
				
				case 'align': 		cfw_query_formatAlign(resultSpan, value, current[1]); break;
				case 'boolean': 	cfw_query_formatBoolean(resultSpan, value, current[1], current[2], current[3], current[4]); break;
				case 'case':		cfw_query_formatCase(resultSpan, record, value, rendererName, fieldname, current); break;
				case 'css':		 	cfw_query_formatCSS(resultSpan, value, current[1], current[2]); break;
				case 'date': 		cfw_query_formatTimestamp(resultSpan, value, current[1]); break;
				case 'decimals': 	cfw_query_formatDecimals(resultSpan, value, current[1]); break;
				case 'duration': 	cfw_query_formatDuration(resultSpan, value, current[1]); break;
				case 'eastereggs': 	cfw_query_formatEasterEggs(resultSpan, value, current[1]); break;
				case 'link': 		cfw_query_formatLink(resultSpan, value, current[1], current[2], current[3], current[4]); break;
				case 'lowercase': 	cfw_query_formatLowercase(resultSpan); break;
				case 'none': 		return $('<span class="">').text(value); break;
				case 'prefix': 		cfw_query_formatPrefix(resultSpan, value, current[1]); break;
				case 'postfix': 	cfw_query_formatPostfix(resultSpan, value, current[1]); break;
				case 'separators':	cfw_query_formatSeparators(resultSpan, value, current[1], current[2]); break;
				case 'shownulls':	cfw_query_formatShowNulls(resultSpan, value, current[1]); break;
				case 'thousands': 	cfw_query_formatThousands(resultSpan, value, current[1], current[2], current[3]); break;
				case 'threshold': 	cfw_query_formatThreshold(resultSpan, value, current[1], current[2], current[3], current[4], current[5], current[6]); break;
				case 'timestamp': 	cfw_query_formatTimestamp(resultSpan, value, current[1]); break;
				case 'uppercase': 	cfw_query_formatUppercase(resultSpan); break;
			}	
		}

		return resultSpan;
	
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatAlign(span, value, alignment){
	
	lower = alignment.toLowerCase();
	span.addClass('text-'+lower);
	
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
			let color = lower == "true" ? trueColor : falseColor;
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
		console.log("conditionArray.length:"+conditionArray.length);
		
		if(conditionArray.length == 0){
			console.log("TRUE");
			hasMatched = true;
		}
		
		for(let index in conditionArray){
			let condition = conditionArray[index];
			console.log("condition:"+condition);
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
			if(condition.startsWith("=="))			{ conditionResult = (value == condition.substring(2)); }
			else if(condition.startsWith("!="))		{ conditionResult = (value != condition.substring(2)); }
			else if(condition.startsWith("<="))		{ conditionResult = (value <= condition.substring(2)); }
			else if(condition.startsWith(">="))		{ conditionResult = (value >= condition.substring(2)); }
			else if(condition.startsWith("<"))		{ conditionResult = (value < condition.substring(1)); }
			else if(condition.startsWith(">"))		{ conditionResult = (value > condition.substring(1)); }
			else 									{ conditionResult = (value == condition); }
			
			//-------------------------------
			// Combine results
			if(hasMatched == null){	
				hasMatched = conditionResult;
				continue;
			}
			if(andOperation){
				console.log("andOperation:"+andOperation);
				hasMatched &= conditionResult;
			}else{
				console.log("andOperation:"+andOperation);
				hasMatched |= conditionResult;
			}
			
		}
		
		//---------------------------
		// Apply format
		if(hasMatched){
			let formatting = caseParams[i+1];
			console.log("condition:"+caseParams[i]);
			console.log("value:"+value);
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
	
	span.css(propertyName, propertyValue);
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatDate(span, value, format){
		
	if(value != null){
		span.text(new  moment(value).format(format));
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatDecimals(span, value, precision){
		
	var valueToProcess = value;
	
	var stringValue = span.text();
	if(stringValue != null && !isNaN(stringValue)){
		valueToProcess = parseFloat(stringValue);
	}
	

	if(valueToProcess != null){
		span.text(valueToProcess.toFixed(precision));
	}

}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatDuration(span, value, durationUnit){
	
	var millisValue = value;
	
	if(durationUnit == 's'){
		millisValue = value * 1000;
	}else if(durationUnit == 'm'){
		millisValue = value * 1000 * 60;
	}else if(durationUnit == 'h'){
		millisValue = value * 1000 * 60 * 60;
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
function cfw_query_formatEasterEggs(span, value, prefix){
	
	value = span.text();
	
	value = value.replaceAll("o", '<i class="fas fa-egg"></i>')
				.replaceAll("O", '<i class="fas fa-egg"></i>')
				.replaceAll("0", '<i class="fas fa-egg"></i>')
				;
				
	span.html(value).find("i").each(function(){
		$(this).css('color', CFW.colors.randomHSL(65,100,55,70));
	})
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatLink(span, value, linkText, displayAs, icon, target){
	
	var linkElement = $('<a>'+linkText+'</a>');
	linkElement.attr('href', value)
			   .attr('target', value);
	
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
	
	span.addClass('text-right');
	
	if(value != null){
		span.html(CFW.format.numbersInThousands(value, decimals, addBlank, isBytes));
	}

}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatThreshold(span, value, excellent, good, warning, emergency, danger, type){
	
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
	
	visibleFields = ((queryResult.displaySettings.visiblefields != null)) ? queryResult.displaySettings.visiblefields : queryResult.detectedFields;
	titleFields = ((queryResult.displaySettings.titlefields != null)) ? queryResult.displaySettings.titlefields : null;
	titleFormat = ((queryResult.displaySettings.titleformat != null)) ? queryResult.displaySettings.titleformat : null;
	
	customizers = cfw_query_createCustomizers(queryResult, visibleFields);
	
	//-----------------------------------
	// Render Results
	var rendererSettings = {
			data: queryResult.results,
		 	//idfield: 'PK_ID',
		 	bgstylefield: null,
		 	textstylefield: null,
		 	titlefields: titleFields,
		 	titleformat: titleFormat,
		 	visiblefields: visibleFields,
		 	labels: labels,
		 	customizers: customizers,

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
