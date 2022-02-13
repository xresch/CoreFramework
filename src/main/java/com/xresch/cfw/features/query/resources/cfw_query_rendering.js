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
			
		return value;
	
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_customizerCreateCustom(formatterArray){
		
	return function (record, value, rendererName, fieldname){
	
		var resultSpan = $('<span>');
		resultSpan.text(value);
		
		for(var i in formatterArray){
			var current = formatterArray[i];
			
			var formatterName = current[0].toLowerCase();
			
			switch(formatterName){
				
				case 'align': 		cfw_query_formatAlign(resultSpan, value, current[1]); break;
				case 'boolean': 	cfw_query_formatBoolean(resultSpan, value, current[1], current[2], current[3], current[4]); break;
				case 'css':		 	cfw_query_formatCSS(resultSpan, value, current[1], current[2]); break;
				case 'date': 		cfw_query_formatTimestamp(resultSpan, value, current[1]); break;
				case 'eastereggs': 	cfw_query_formatEasterEggs(resultSpan, value, current[1]); break;
				case 'link': 		cfw_query_formatLink(resultSpan, value, current[1], current[2], current[3], current[4]); break;
				case 'none': 		return $('<span class="">').text(value); break;
				case 'shownulls':	cfw_query_formatShowNulls(resultSpan, value, current[1]); break;
				case 'prefix': 		cfw_query_formatPrefix(resultSpan, value, current[1]); break;
				case 'postfix': 	cfw_query_formatPostfix(resultSpan, value, current[1]); break;
				case 'threshold': 	cfw_query_formatThreshold(resultSpan, value, current[1], current[2], current[3], current[4], current[5], current[6]); break;
				case 'timestamp': 	cfw_query_formatTimestamp(resultSpan, value, current[1]); break;
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
	span.addClass('format-base text-'+lower);
	
	if(lower == "center"){
		span.removeClass('text-left text-right');
	}else if(lower == "left"){
		span.removeClass('text-center text-right');
	}else {
		span.removeClass('text-left text-center');
	}
	
	return span;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatBoolean(span, value, trueBGColor, falseBGColor, trueTextColor, falseTextColor){
	
	span.addClass('format-base text-center');
	
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
	
	return span;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatCSS(span, value, propertyName, propertyValue){
	
	span.css(propertyName, propertyValue);

	return span;
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
	
	
	
	return span;
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
	
	return span;
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
	
	return span;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatPrefix(span, value, prefix){
	
	value = span.text();
	span.text(prefix+value);
	
	return span;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatPostfix(span, value, postfix){
	
	value = span.text();
	span.text(value+postfix);
	
	return span;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatThreshold(span, value, excellent, good, warning, emergency, danger, type){
	
	span.addClass('format-base text-right font-weight-bold');
	
	var style = CFW.colors.getThresholdStyle(value, excellent, good, warning, emergency, danger, false);
	
	if(type == 'bg'){
		CFW.colors.colorizeElement(span, "white", "text");
	}
	
	CFW.colors.colorizeElement(span, style, type, "2px");

	return span;
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatDate(span, value, format){
	
	span.addClass('format-base');
	
	if(value != null){
		span.text(new  moment(value).format(format));
	}

	return span;
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_formatTimestamp(span, value, format){
	
	span.addClass('format-base');
	
	if(value != null){
		span.text(new  moment(value).format(format));
	}

	return span;
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
