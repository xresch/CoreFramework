/*******************************************************************************
 * This file contains the methods needed to render the Query results.
 * 
 ******************************************************************************/

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_getRendererIndex(queryResult){
	
	let rendererIndex = 0;	
	

	if(queryResult.displaySettings.as != null){
		switch(queryResult.displaySettings.as.trim().toLowerCase()){
			case 'table':				rendererIndex = 0; break;		
			case 'panels':				rendererIndex = 1; break;	
			case 'properties':			rendererIndex = 2; break;	
			case 'cards':				rendererIndex = 3; break;	
			case 'tiles':				rendererIndex = 4; break;
			case 'tileandbar':			rendererIndex = 5; break;
			case 'statustiles':			rendererIndex = 6; break;
			case 'statuslist':			rendererIndex = 7; break;
			case 'statusbar':			rendererIndex = 8; break;
			case 'statusbarreverse':	rendererIndex = 9; break;
			
			case 'statusmap':			rendererIndex = 10; break;
			
			case 'title':			rendererIndex = 11; break;	
			case 'csv':				rendererIndex = 12; break;	
			case 'json':			rendererIndex = 13; break;	
			case 'xml':				rendererIndex = 14; break;	
			
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
	

	//--------------------------------
	// Customizer for every FieldFormat
	let fieldFormats = queryResult.displaySettings.fieldFormats;
	let customizers = {};
	
	for(let fieldname in fieldFormats){
		let formatterArray = fieldFormats[fieldname];

		customizers[fieldname] = cfw_query_customizerCreateCustom(formatterArray);

	}
	
	//--------------------------------
	// Default Customizer
	let defaultCustomizer = cfw_query_customizerCreateDefault();
	
	let knownFields = fields;
	if(queryResult.records.length > 0){
		knownFields = Object.keys(queryResult.records[0]).concat(knownFields);
	}

	for(let i in knownFields){
		
		let fieldname = knownFields[i];
		
		if(customizers[fieldname] == null){
			customizers[fieldname] = defaultCustomizer;
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
			
			return $('<span class="format-base word-wrap-preline">').text(value);
			
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
		if(value === null){
			return '<span class="format-base text-white text-center badge-primary m-0">NULL</span>';
		}
		
		//----------------------------------------------
		// Undefined
		if(value === undefined){
			return '<span class="format-base text-white text-center badge-primary m-0">undefined</span>';
		}
		
		//----------------------------------------------
		// Arrays and Objects
		if(typeof value === 'object'){
			let span = $('<span class="format-base word-wrap-preline">');
			return span.text( JSON.stringify(value).replaceAll(',',', ') );
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
				case 'indicator': 			cfw_query_formatIndicator(resultSpan, value, current[1], current[2], current[3], current[4], current[5], current[6], current[7]); break;
				case 'ea'+'stere'+'ggs': 	cfw_query_formatEa_sterE_ggs(resultSpan, value); break;
				case 'link': 				cfw_query_formatLink(resultSpan, value, current[1], current[2], current[3], current[4]); break;
				case 'list': 				cfw_query_formatList(resultSpan, value, current[1], current[2], current[3]); break;
				case 'lowercase': 			cfw_query_formatLowercase(resultSpan); break;
				case 'none': 				cfw_query_formatNone(resultSpan, value); break;
				case 'percent': 			cfw_query_formatPercent(resultSpan, value, current[1], current[2], current[3], current[4]); break;
				case 'prefix': 				cfw_query_formatPrefix(resultSpan, value, current[1]); break;
				case 'postfix': 			cfw_query_formatPostfix(resultSpan, value, current[1]); break;
				case 'separators':			cfw_query_formatSeparators(resultSpan, value, current[1], current[2]); break;
				case 'shownulls':			cfw_query_formatShowNulls(resultSpan, value, current[1]); break;
				case 'special':				cfw_query_formatSpecial(resultSpan, value); break;
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
	if(trueBGColor == null )	{ trueBGColor = 'cfw-green'; }
	if(falseBGColor == null )	{ falseBGColor = 'cfw-red'; }
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
		format = "yyyy-MM-dd";
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
	
	if(valueToProcess != null){
		if(typeof valueToProcess === "number"){
			span.text(valueToProcess.toFixed(precision));
		}else if(typeof valueToProcess === "string" && !Number.isNaN(valueToProcess)){
			span.text(parseFloat(valueToProcess).toFixed(precision));
		}
	}

}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatDuration(span, value, durationUnit){
	
	span.addClass('w-100 text-right');
	
	if(value != null){
		if(!isNaN(value)){
			span.text(CFW.format.timeToDuration(value, durationUnit));
		}else{
			span.text(value);
		}
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatIndicator(span, value, excellent, good, warning, emergency, danger, position, colortext){
	
	// set defaults
	if(excellent 	=== undefined )		{ excellent = 0; }
	if(good 		=== undefined )		{ good = 20; }
	if(warning 		=== undefined )		{ warning = 40; }
	if(emergency 	=== undefined )		{ emergency = 60; }
	if(danger 		=== undefined )		{ danger = 80; }
	if(position 	=== undefined )		{ position = 'right'; }
	if(colortext 	=== undefined )		{ colortext = false; }
	
	span.addClass('font-weight-bold');
	
	let style = CFW.colors.getThresholdStyle(
			  value
			, excellent
			, good
			, warning
			, emergency
			, danger
			, false
		);
	
	var isHighToLow = CFW.colors.getThresholdDirection(excellent, good, warning, emergency, danger);
	var indicator = CFW.colors.getThresholdIndicatorForStyle(style, isHighToLow);
	
	//-------------------------------
	// Add Indicator
	if(position == 'right'){
		span.addClass("w-100 text-right"); 
		span.append("&nbsp;")
			.append(indicator)
			;
	}else{
		span.prepend("&nbsp;")
			.prepend(indicator)
			;
	}
	
	//-------------------------------
	// Color Text
	if(colortext == true  || colortext == "true"){
			CFW.colors.colorizeElement(span, style, "text");
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
	
	var nbsp = (!CFW.utils.isNullOrEmpty(linkText)) ? "&nbsp;" : "";
	var linkElement = $('<a>'+nbsp+linkText+'</a>');
	linkElement.attr('href', value)
			   .attr('target', value)
			   .css('color', 'unset');
	
	if(displayAs == 'button'){
		linkElement.attr('role', 'button')
			 .addClass('btn btn-sm btn-primary text-white');
	}	
	
	if(icon != null){
		linkElement.prepend('<i class="fas '+icon+'"></i>');
	}
	
	span.html('');
	span.append(linkElement);
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatList(span, value, bulletType, paddingLeft, doLabelize){
	
	span.html('');
	if(typeof value == "object"){
		span.append(CFW.format.objectToHTMLList(value, bulletType, paddingLeft, doLabelize));
	}else{
		span.addClass('word-wrap-preline').text(value);
	}
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
function cfw_query_formatNone(span, value){
	
	//-----------------------
	// Reset All Styles
	span.html('');
	span.removeAttr('style');
	span.removeClass();
	span.addClass('format-base');

	//-----------------------
	// Set Value
	if(typeof value === 'object'){
		span.text(JSON.stringify(value).replaceAll(',',', ') );
	}else{
		span.text(value);
	}

}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatPercent(span, value, greenThreshold, redThreshold, type, neutralColor){
	
	//------------------------------
	// Handle non number values
	if(value == null){
		cfw_query_formatShowNulls(span, value, true);
		return;
	}else if(typeof value === "boolean"){
		cfw_query_formatBoolean(span, value);
		return;
	}else if(typeof value !== "number"){
		return;
	}
	
	//------------------------------
	// Set Defaults
	if(redThreshold === undefined )		{ redThreshold = 0; }
	if(redThreshold === undefined )		{ greenThreshold = 0; }
	if(type 		=== undefined )		{ type = 'bg'; }
	if(neutralColor === undefined )		{ neutralColor = null; }
	
	//------------------------------
	// Add Styles
	span.addClass('w-100 text-right font-weight-bold');
		
	var style = CFW.colors.getSplitThresholdStyle(value, greenThreshold, redThreshold, false);
	style = (style == CFW.style.notevaluated && neutralColor != null) ? neutralColor : style;
	
	if(type == 'bg'){
		CFW.colors.colorizeElement(span, "white", "text");
	}
	
	CFW.colors.colorizeElement(span, style, type, "2px");
	
	//------------------------------
	// Call Other Formatters
	cfw_query_formatDecimals(span, value, 1);
	cfw_query_formatPostfix(span, value, " %");
	if(value > 0){
		cfw_query_formatPrefix(span, value, "+");
	}

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
function cfw_query_formatSpecial(span, value){
	
	if( typeof value == "string"){
		value = JSON.parse(value);
	}
	
	if( typeof value == "object"){
		
		switch(value.format){
			case "boxplot":		cfw_query_formatSpecial_Boxplot(span, value);   break;
			case "css":			cfw_query_formatSpecial_CSS(span, value);   break;
			case "display":		cfw_query_formatSpecial_Display(span, value);   break;
			case "link":		cfw_query_formatSpecial_Link(span, value);   break;
			default:			break;
		}
	}

	
}
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatSpecial_Boxplot(span, object){
	
	let values = object.values;
	let color =  object.color;
	let width =  object.width;
	let height =  object.height;

	span.css("height", height);
	span.css("width", width);

	//---------------------------------
	// Data is null
	if(values == null){
		cfw_query_formatShowNulls(span, value);
		return;
	}
	
	//---------------------------------
	// Data is object
	if( typeof values === "object" ){
		span.html("");
		span.append( 
			CFW.format.boxplot(values, color) 
		);
		
		return;

	}
	
	//---------------------------------
	// Data is Object or Array
	if( Array.isArray(value) || typeof value === "object" ){

		span.text( JSON.stringify(records) );
		return;

	}

	//---------------------------------
	// Any other data type
	span.text(value);
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatSpecial_CSS(span, object){
	
	let value = object.value;
	let style =  object.style;

	span.attr("style", style);

	
	//---------------------------------
	// Data is null
	if(value == null){
		cfw_query_formatShowNulls(span, value);
		return;
	}
	
	//---------------------------------
	// Data is Object or Array
	if( Array.isArray(value) || typeof value === "object" ){

		span.text( JSON.stringify(records) );
		return;

	}

	//---------------------------------
	// Any other data type
	span.text(value);
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatSpecial_Display(span, object){
	
	let records = object.queryResults.records;
	let height =  object.height;
	let width =  object.width;
	span.css("height", height);
	span.css("width", width);
	span.css("overflow", "auto");
	
	
	//---------------------------------
	// Data is null
	if(records == null){
		cfw_query_formatShowNulls(span, records);
		return;
	}

	//---------------------------------
	// Data is Array
	if( Array.isArray(records) ){

		cfw_query_renderQueryResult(span, object.queryResults);
		//span.text("test");
		return;

	}
	
	//---------------------------------
	// Data is object
	if( typeof records === "object" ){
		let array = [records];
		cfw_query_renderQueryResult(span, array);
		return;

	}
	
	
	//---------------------------------
	// Any other data type
	span.text(records);
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatSpecial_Link(span, object){
	
	var target = ' target="_blank" ';
	if(!object.newtab){
		target = " ";
	}
	
	var style = ' ';
	if( !CFW.utils.isNullOrEmpty(object.style) ){
		style = ' style="'+object.style+'" ';;
	}
	
	span.html('<a href="'+object.url+'" '+ target + style +'>'+object.label+'</a>');
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
	
	if(format == null )	{ format = "yyyy-MM-dd HH:mm:ss"; }
		
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
	var maxheightClass = (queryResultsPayload.length == 1) ? "h-100 mh-100" : "" ; // needed to prevent overflow (e.g. charts)
	var currentRow = $('<div class="row m-0 flex-grow-1 '+maxheightClass+'">');
	resultTarget.append(currentRow);
	
	for(var i = 0; i < queryResultsPayload.length; i++){
		var currentColumn = $('<div class="col-percent mh-100 '+colClass+'">');
		
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
	
	let targetDiv = $(resultTarget);
	targetDiv.html(""); 
	
	let options = {};
	//-----------------------------------
	// Create Title				
	if(queryResult.metadata.title == true){
		let execSeconds = '';
		if( (
			   queryResult.metadata.titleduration == null 
			|| queryResult.metadata.titleduration == true
			)
		 &&  queryResult.execTimeMillis != -1
		 ){
			execSeconds = " ("+(queryResult.execTimeMillis / 1000).toFixed(3)+"s)";
		}
		let title = $('<p class="query-title">');
		
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
	if(queryResult.records.length == 0 ){
		targetDiv.append('<p>The result is empty.</p>')
		return;
	}
	
	//-----------------------------------
	// Get StyleFields
	options.bgstylefield = null;
	if(queryResult.displaySettings.bgstylefield != undefined){
		options.bgstylefield = queryResult.displaySettings.bgstylefield;
	}
	
	options.textstylefield = null;
	if(queryResult.displaySettings.textstylefield != undefined){
		options.textstylefield = queryResult.displaySettings.textstylefield;
	}
	//-----------------------------------
	// Zoom
	if(queryResult.displaySettings.zoom != null){
		let zoomString = ""+queryResult.displaySettings.zoom;
		if(!zoomString.endsWith("%")){ zoomString += "%";}
		targetDiv.css('zoom', zoomString);
	}
	
	//-----------------------------------
	// Various Options
	options.visibleFields = ((queryResult.displaySettings.visiblefields != null)) ? queryResult.displaySettings.visiblefields : queryResult.detectedFields;
	options.visibleFields = _.without(options.visibleFields, options.bgstylefield, options.textstylefield);
	
	let customizerFields = options.visibleFields.concat(queryResult.detectedFields);
	options.customizers = cfw_query_createCustomizers(queryResult, customizerFields);	
	
	rendererIndex = cfw_query_getRendererIndex(queryResult);		
	options.labels = cfw_query_createLables(queryResult);	
	
	options.titleFields = ((queryResult.displaySettings.titlefields != null)) ? queryResult.displaySettings.titlefields : null;
	options.titleFormat = ((queryResult.displaySettings.titleformat != null)) ? queryResult.displaySettings.titleformat : null;
	
	//-----------------------------------
	// Do chart if it is Chart
	if(queryResult.displaySettings.as == 'chart'){
		cfw_query_renderAsChart(targetDiv, queryResult, options);
		return;
	}
	
	
	//-----------------------------------
	// Render Definition 
	let renderDefinition = {
			data: queryResult.records,
		 	//idfield: 'PK_ID',
		 	bgstylefield: options.bgstylefield,
		 	textstylefield: options.textstylefield,
		 	titlefields: options.titleFields,
		 	titleformat: options.titleFormat,
		 	visiblefields: options.visibleFields,
		 	labels: options.labels,
		 	customizers: options.customizers,

			rendererSettings: {
				table: { filterable: false, narrow: true},
				panels: { narrow: true},
				properties: { narrow: true},
				cards: { narrow: true},
				tiles: { 
					popover: false,
					//border: '2px solid black'
				},
				statustiles: { 
					popover: false,
					//border: '2px solid black'
				},
				statuslist: {},
				statusbar: {},
				statusbarreverse: {},
				tileandbar: {},
				tileandbarreverse: {},
				statusmap: {},
				title: {},
				csv: {},
				json: {},
				xml: {},
				dataviewer: {
					//storeid: 'cfw-query',
					rendererIndex: rendererIndex,
					menu: queryResult.displaySettings.menu,
					pagination: queryResult.displaySettings.pagination,
					sizes: queryResult.displaySettings.sizes,
					defaultsize: queryResult.displaySettings.defaultsize,
					download: queryResult.displaySettings.download,
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
						{	label: 'Properties',
							name: 'properties',
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
										//border: '2px solid black'
									},
								},
								
							}
						},
						{	label: 'Tile and Bar',
							name: 'tileandbar',
							renderdef: { }
						},
						{	label: 'Status Tiles',
							name: 'statustiles',
							renderdef: {}
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
						{	label: 'Status Map',
							name: 'statusmap',
							renderdef: {}
						},
						{	label: 'Title',
							name: 'title',
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
	
	//-----------------------------------
	// Merge with Custom Settings
	let rendererName = "table";
	if(queryResult.displaySettings.as != null){
		rendererName = queryResult.displaySettings.as.trim().toLowerCase();
	}
	// Merge custom settings into default settings for selected renderer
	let customSettings = queryResult.displaySettings.settings;
	if(customSettings != null){
		var currentSettings = renderDefinition.rendererSettings[rendererName];
		Object.assign(currentSettings, customSettings);
	}

	//-----------------------------------
	// Render!!!
	let renderResult = CFW.render.getRenderer("dataviewer").render(renderDefinition);	
	
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
function cfw_query_renderAsChart(resultTarget, queryResult, options){
	
	let targetDiv = $(resultTarget);
	
	// settings contains params of chart-command
	var settings = queryResult.displaySettings;

	//---------------------------------
	// Prepare TitleFields
	var seriesColumns = (settings.by != null) ? settings.by : settings.groupby; // groupby is deprecated
	var titlefields = seriesColumns;
	if(titlefields == null ){
		titlefields=[]; // no grouping by default
	}
	
	//---------------------------------
	// Use first Column if not specified
	var xColumn = settings.x;
	if(CFW.utils.isNullOrEmpty(settings.x)){
		let keys = Object.keys(queryResult.records[0]);
		xColumn = keys[0];
	}
	
	//---------------------------------
	// Use last Column if not specified
	var yColumns = settings.y;
	if(CFW.utils.isNullOrEmpty(settings.y)){
		let keys = Object.keys(queryResult.records[0]);
		yColumns = keys[keys.length-1];
	}
	
	//---------------------------------
	// Fallback to Defaults if undefined
	var defaultSettings = {
		charttype: "line",
		// How should the input data be handled groupbytitle|arrays 
		datamode: 'groupbytitle',
		xfield: xColumn,
		yfield: yColumns,
		type: "line",
		xtype: "time",
		ytype: "linear",
		stacked: false,
		legend: false,
		axes: true,
		ymin: 0,
		ymax: null,
		pointradius: 1,
		spangaps: false,
		padding: '2px'
	};
	
	//---------------------------
	// Merge Parameters
	var finalSettings = Object.assign({}, defaultSettings, settings);
	if(settings.type != null){
		finalSettings.charttype = settings.type;
	}
	
	//Final override by param "settings" of chart command
	Object.assign(finalSettings, settings.settings)

	//---------------------------
	// Render Settings
	var dataToRender = {
		data: queryResult.records,
		titlefields: titlefields,
		bgstylefield: options.bgstylefield,
	 	textstylefield: options.textstylefield,
	 	titleformat: options.titleFormat,
	 	visiblefields: options.visibleFields,
	 	labels: options.labels,
	 	customizers: options.customizers,
		titleformat: options.titleFormat, 
		rendererSettings:{
			chart: finalSettings
		}
	};
									
	//--------------------------
	// Render Widget
	var renderer = CFW.render.getRenderer('chart');
	
	var renderResult = CFW.render.getRenderer('chart').render(dataToRender);	
	
	targetDiv.append(renderResult);
}

