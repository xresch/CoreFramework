
/**************************************************************************************************************
 * CFW.js
 * ======
 * Main library for the core framwork.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/

/******************************************************************
 * Class to render HTML from JSON data.
 * 
 ******************************************************************/
class CFWRenderer{
	
	 constructor(renderFunction){
		 
		 this.renderFunction = renderFunction;
		 
		 this.defaultRenderDefinition = {
			// (Optional) name of the field that is used as the identifier of the data
		 	idfield: null,
		 	// (Optional) names of the fields that are used for a titles. Takes the first field from the first object if null
		 	titlefields: null,
		 	// (Optional) the delimiter used for multiple titles
		 	titledelimiter: ' ',
		 	// (Optional) Names of the fields that should be rendered and in the current order. If null or undefined, will display all fields
		 	visiblefields: null,
		 	// (Optional) Custom labels for fields, add them as "{fieldname}: {label}". If a label is not defined for a field, uses the capitalized field name
		 	labels: {},
		 	// field containing the bootstrap style (primary, info, danger ...) that should be used as the background
		 	bgstylefield: null,
		    // field containing the bootstrap style (primary, info, danger ...) that should be used as for texts
		 	textstylefield: null,
		 	// functions that return a customized htmlString to display a customized format, add as "<fieldname>: function(record, value)".  Cannot return a JQuery object.
		 	customizers: {},
		 	// array of functions that return html for buttons, add as "<fieldname>: function(record, id)". Cannot return a JQuery object.
		 	actions: [ ],
			// list of functions that should be working with multiple items. fieldname will be used as the button label
		 	bulkActions: null,
		 	// position of the multi actions, either top|bottom|both|none
		 	bulkActionsPos: "top",
			// the data that should be rendered as an array
		 	data: null,
		 	// settings specific for the renderer, add as "rendererSettings.{rendererName}.{setting}"
		 	rendererSettings: {}
		 };
		  
	 }
	 
	 /********************************************
	  * Returns a String in the format YYYY-MM-DD
	  ********************************************/
	 prepareDefinition(definition){
		 var data = definition.data;
		 var firstObject = null;
		 
		 //---------------------------
		 // get first object
		 if(Array.isArray(data)){
			 definition.datatype = "array";
			 if(data.length > 0){
				 firstObject = data[0];
			 }
		 }else if(typeof data == "object"){
			 definition.datatype = "array";
			 definition.data = [data];
			 firstObject = data;
		 }else {
			 definition.datatype = typeof data;
		 }
		 
		 //---------------------------
		 // Get Visible Fields
		 if(firstObject != null && typeof firstObject == 'object'){
			 
			 //--------------------------
			 // resolve default visible fields
			 if(definition.visiblefields == null){ 
				 definition.visiblefields = [];
				 for(var key in firstObject){
					 definition.visiblefields.push(key);
				 }
			 }
			 
			 //--------------------------
			 // resolve title fields
			 if(definition.titlefields == null){ 
				 if(definition.visiblefields.length > 0){ 
					 definition.titlefields = [definition.visiblefields[0]];
				 }
			 }
		 }
		 
		 //---------------------------
		 // Create Labels
		 for(var key in definition.visiblefields){
			var fieldname = definition.visiblefields[key];
			if(definition.labels[fieldname] == null){
				definition.labels[fieldname] = CFW.format.fieldNameToLabel(fieldname);
			}
		}
		 //---------------------------
		 // Lovercase
		 definition.bulkActionsPos = definition.bulkActionsPos.toLowerCase();
		 
	 }
	 
	 /********************************************
	  * Returns a html string 
	  ********************************************/
	 render(renderDefinition){
		 var definition = Object.assign({}, this.defaultRenderDefinition, renderDefinition);	
		 
		 this.prepareDefinition(definition);
		 
		 return this.renderFunction(definition);
	 }
}

/******************************************************************
 * Class to create form fields
 * 
 ******************************************************************/
class CFWFormField{
	
	 constructor(customOptions){
		 
		 this.defaultOptions = {
			type: "text",
			name: null,
			label: null,
			value: null,
			description: null,
			disabled: false,
			attributes: {},
			// value/label options like { "value": "Label", ... }
			options: {}
		 };
		 
		 this.options = Object.assign({}, this.defaultOptions, customOptions);
		 
		 if(this.options.label == null){
			 this.options.label = CFW.format.fieldNameToLabel(this.options.name);
		 }
		 
		 if(this.options.attributes.placeholder == null){
			 this.options.attributes.placeholder = this.options.label;
		 }
		 
		 if(this.options.disabled == true){
			 this.options.attributes.disabled = "disabled";
		 }
		 
	 }
	
	 /********************************************
	  * Returns a String in the format YYYY-MM-DD
	  ********************************************/
	 createHTML(){
		 var type = this.options.type.trim().toUpperCase();
		 
		 //----------------------------
		 // Start and Label
		 if(type != "HIDDEN"){
			 var htmlString =
			  '<div class="form-group row ml-1">'
				+'<label class="col-sm-3 col-form-label" for="3">'+this.options.label+':</label>'
				+'<div class="col-sm-9">'
				
			 //----------------------------
			 // Description Decorator
			 if(this.options.description != null){	
				 htmlString +=
					'<span class="badge badge-info cfw-decorator" data-toggle="tooltip" data-placement="top" data-delay="500" title=""'
						+'data-original-title="'+this.options.description+'"><i class="fa fa-sm fa-info"></i></span>'
			 }
		 }
		 
		 //----------------------------
		 // Field HTML
		 this.options.attributes.name = this.options.name;
		 
		 switch(type){
		 	case 'TEXT': 		this.options.attributes.value = this.options.value;
		 						htmlString += '<input type="text" class="form-control" '+this.getAttributesString()+'/>';
		 						break;
		 	
		 	case 'TEXTAREA': 	htmlString += this.createTextAreaHTML();
								break;
		 	
		 	case 'BOOLEAN': 	htmlString += this.createBooleanRadios();
								break;
				
		 	case 'SELECT': 		htmlString += this.createSelectHTML();
		 						break;
			
		 	case 'NUMBER':  	this.options.attributes.value = this.options.value;
		 						htmlString += '<input type="number" class="form-control" '+this.getAttributesString()+'/>';
								break;
			
		 	case 'HIDDEN':  	this.options.attributes.value = this.options.value;
								htmlString += '<input type="hidden" '+this.getAttributesString()+'/>';
								break;
			
		 	case 'EMAIL':  		this.options.attributes.value = this.options.value;
		 						htmlString += '<input type="email" class="form-control" '+this.getAttributesString()+'/>';
		 						break;
		 						
		 	case 'PASSWORD':  	this.options.attributes.value = this.options.value;
		 						htmlString += '<input type="password" class="form-control" '+this.getAttributesString()+'/>';
								break;
		 }
		 //----------------------------
		 // End
		if(type != "HIDDEN"){
			htmlString +=
					'</div>'
				+'</div>';
		}

		 return htmlString;
	 }
	 
	/***********************************************************************************
	 * Create a text area
	 ***********************************************************************************/
	createTextAreaHTML() {
		
		if(this.options.attributes.rows == null) {
			this.options.attributes.rows = 5;
		}
		
		var value = "";
		if(this.options.value !== null && this.options.value !== undefined ) {
			value = this.options.value;
		}
		
		return "<textarea class=\"form-control\" "+this.getAttributesString()+">"+value+"</textarea>";
	}
	
	/***********************************************************************************
	 * Create Boolean Radio Buttons
	 ***********************************************************************************/
	createBooleanRadios() {
		
		var falseChecked = "";
		var trueChecked = "";
		
		var value = this.options.value;
		
		if(value != null && value.toString().toLowerCase() == "true") {
			trueChecked = "checked";
		}else {
			falseChecked = "checked";
		}
		
		var disabled = "";
		if(this.options.disabled) {	disabled = "disabled=\"disabled\""; };
		
		var htmlString = '<div class="form-check form-check-inline col-form-labelmt-5">'
			+ '  <input class="form-check-input" type="radio" value="true" name='+this.options.name+' '+disabled+' '+trueChecked+'/>'
			+ '  <label class="form-check-label" for="inlineRadio1">true</label>'
			+ '</div>'
			+ '<div class="form-check form-check-inline col-form-label">'
			+ '  <input class="form-check-input" type="radio" value="false" name='+this.options.name+' '+disabled+' '+falseChecked+'/>'
			+ '  <label class="form-check-label" for="inlineRadio1">false</label>'
			+ '</div>'; 
		
		return htmlString;
	}

	/***********************************************************************************
	 * Create a select
	 ***********************************************************************************/
	createSelectHTML() {
			
		var value = "";
		if(this.options.value !== null && this.options.value !== undefined ) {
			value = this.options.value;
		}
		
		var html = '<select class="form-control" '+this.getAttributesString()+' >';
		
		//-----------------------------------
		// handle options
		var options = this.options.options;
		
		if(options != null) {
			for(var currentVal in options) {
				var label = options[currentVal];
				
				if(currentVal == value) {
					html += '<option value="'+currentVal+'" selected>' + label + '</option>';
				}else {
					html += '<option value="'+currentVal+'">' + label + '</option>';
				}
			}
		}
		
		html += '</select>';
		
		return html;
	}
	 
	/********************************************
	 * 
	 ********************************************/
	 getAttributesString(){
		 var result = '';
			for(var key in this.options.attributes) {
				var value = this.options.attributes[key];
				
				if(value != null && value !== "") {
					result += ' '+key+'="'+value+'" ';
				}
			}
			return result;
	 }
}

/******************************************************************
 * Class to wrap a date for easier formatting.
 * 
 ******************************************************************/
class CFWDate{
	
	 constructor(dateArgument){
		 if(dateArgument != null){
			 this.date = new Date(dateArgument);
		 }else{
			 this.date = new Date();
		 }
	 }
	
	 /********************************************
	  * Returns a String formatted as defined by the
	  * parameter.
	  * 	YYYY: 4-digit year
	  * 	YY: 2-digit year
	  *		MM: 2-digit month (where January is 01 and December is 12)
	  *		DD: 2-digit date (0 to 31)
	  *		HH: 24-digit hour (0 to 23)
	  *		mm: Minutes (0 to 59)
	  *		ss: Seconds (0 to 59)
	  *		SSS: Milliseconds (0 to 999)
	  *
	  ********************************************/
	 getDateFormatted(dateFormatString){
		 
		dateFormatString = 
			dateFormatString
				.replace("YYYY", this.date.getFullYear()) 
				.replace("YY", (this.date.getFullYear()+'').substring(2))
				.replace("MM", this.fillDigits(this.date.getMonth()+1, 2))
				.replace("DD", this.fillDigits(this.date.getDate(), 2))
				.replace("HH", this.fillDigits(this.date.getHours(), 2))
				.replace("mm", this.fillDigits(this.date.getMinutes(), 2))
				.replace("SSS", this.fillDigits(this.date.getMilliseconds(), 3))
				.replace("ss", this.fillDigits(this.date.getSeconds(), 2)); 

		 return dateFormatString;
	 }
	 /********************************************
	  * Returns a String in the format YYYY-MM-DD
	  ********************************************/
	 getDateForInput(){
		 var datestring = this.fillDigits(this.date.getFullYear(), 4)+"-"
		 				+ this.fillDigits(this.date.getMonth()+1, 2)+"-"
		 				+ this.fillDigits(this.date.getDate(), 2);
		 
		 return datestring;
	 }
	 
	 /********************************************
	  * Returns a String in the format HH:MM:ss.SSS
	  ********************************************/
	 getTimeForInput(){
		 var datestring = this.fillDigits(this.date.getHours(), 2)+":"
		 				+ this.fillDigits(this.date.getMinutes(), 2);
		 				/* +":"
		 				+ this.fillDigits(this.date.getSeconds(), 2)+"."
		 				+ this.f illDigits(this.date.getMilliseconds(), 3);*/
		 
		 return datestring;
	 }
	 
	 /********************************************
	  * Fill the digits to the needed amount
	  ********************************************/
	 fillDigits(value, digits){
		
		var stringValue = ''+value;
		var length = stringValue.length;
		
		for(var i = stringValue.length; i < digits;i++){
			stringValue = '0'+stringValue;
		}
		
		return stringValue;
		
	}
}
/******************************************************************
 * Creates a CFWTable.
 * 
 ******************************************************************/
 class CFWTable{
	
	 constructor(customSettings){
		 
		 this.settings = {
			// add a filter field above the table
			filterable: true,
			// make the table responsive
			responsive: true,
			// highlight hovered rows
			hover: true,
			//make the table striped
			striped: true,
			// narrow down the row high
			narrow: false,
			// stick the header on top when scrolling. Doesn't work when responsive is true
			stickyheader: false, 
		 }
		 
		 Object.assign(this.settings, customSettings);
		 
		 this.id = 'cfwtable-'+CFW.utils.randomString(16);
		 this.table = $('<table id="'+this.id +'" class="table">');
		 
		 this.thead = $('<thead>');
		 this.table.append(this.thead);
		 
		 this.tbody = $('<tbody>');
		 this.table.append(this.tbody);
		 
	 }
	
	 
	 /********************************************
	  * Adds a header using a string.
	  ********************************************/
	 addHeader(header){
		 var th = $('<th>');
		 th.append(header);
		 this.thead.append(th);
	 }
	 
	 /********************************************
	  * Adds headers using a string array.
	  ********************************************/
	 addHeaders(stringArray){
		 
		 var htmlString = "";
		 for(var i = 0; i < stringArray.length; i++){
			 htmlString += '<th>'+stringArray[i]+'</th>';
		 }
		 this.thead.append(htmlString);
	 }
	 
	 /********************************************
	  * Adds a row using a html string or a 
	  * jquery object .
	  ********************************************/
	 addRow(htmlOrJQueryObject){
		 this.tbody.append(htmlOrJQueryObject);
	 }
	 
	 /********************************************
	  * Adds rows using a html string or a 
	  * jquery object .
	  ********************************************/
	 addRows(htmlOrJQueryObject){
		 this.tbody.append(htmlOrJQueryObject);
	 }
	 
	 /********************************************
	  * Append the table to the jquery object.
	  * @param parent JQuery object
	  ********************************************/
	 getTable(){
		  
		 if(this.settings.striped){		this.table.addClass('table-striped'); }
		 if(this.settings.hover){		this.table.addClass('table-hover'); }
		 if(this.settings.narrow){		this.table.addClass('table-sm'); }
		 
		 var wrapper = $('<div class="flex-grow-1">');
		 if(this.settings.filterable){
			 var filter = $('<input id="'+this.id+'-filter" type="text" class="form-control form-control-sm tablefilter-marker" onkeyup="cfw_filterTable(this)" placeholder="Filter Table...">');
			 wrapper.append(filter);
			 //jqueryObject.append('<span style="font-size: xx-small;"><strong>Hint:</strong> The filter searches through the innerHTML of the table rows. Use &quot;&gt;&quot; and &quot;&lt;&quot; to search for the beginning and end of a cell content(e.g. &quot;&gt;Test&lt;&quot; )</span>');
			 filter.data("table", this.table);
		 }
		 
		 if(this.settings.stickyheader){
			 this.thead.find("th").addClass("cfw-sticky-th bg-dark text-light");
			 this.isResponsive = false;
			 this.table.css("width", "100%");
		 }
		 
		 if(this.settings.responsive){
			var responsiveDiv = $('<div class="table-responsive">');
			responsiveDiv.append(this.table);
			
			wrapper.append(responsiveDiv);
		 }else{
			 wrapper.append(this.table);
		 }
		 
		 return wrapper;
	 }
	 
	 /********************************************
	  * Append the table to the jquery object.
	  * @param parent JQuery object
	  ********************************************/
	 appendTo(parent){
		 parent.append(this.getTable());
	 }
}

 
/******************************************************************
 * Creates a CFWPanel 
 * 
//	<div class="card">
//	  <div class="card-header">
//	    Featured
//	  </div>
//	  <div class="card-body">
//	    <h5 class="card-title">Special title treatment</h5>
//	    <p class="card-text">With supporting text below as a natural lead-in to additional content.</p>
//	    <a href="#" class="btn btn-primary">Go somewhere</a>
//	  </div>
//	</div>
 * 
 ******************************************************************/
CFW_GLOBAL_PANEL_COUNTER = 0;

class CFWPanel{
	
	 constructor(customSettings){
		 
		 this.settings = {
			// the style to define the cards color
			cardstyle: null,
			// the style used for the text
			textstyle: null,
			// the style used for the header text, if null, textstyle will be used
			textstyleheader: null,
			// the title of the panel
			title: "&nbsp;",
			//the content of the panel
			body: "&nbsp;",
		}
		 
		 Object.assign(this.settings, customSettings);
		 
		//----------------------------
		// resolve classes
		var panelClasses = 'cfwRecordContainer card';
		var panelHeaderClasses = 'card-header';
		
		if(this.settings.cardstyle != null){
			panelClasses += ' border-'+this.settings.cardstyle;
			panelHeaderClasses += 'bg-'+this.settings.cardstyle;
		}
		
		if(this.settings.textstyle != null){
			panelClasses += ' text-'+this.settings.textstyle;
		} 
		
		if(this.settings.textstyleheader != null){
			panelHeaderClasses += ' text-'+this.settings.textstyleheader;
		} 
		//----------------------------
	     // Create Panel
		 this.panel = $(document.createElement("div"));
		 this.panel.addClass(panelClasses);
		 
		 this.counter = CFW_GLOBAL_PANEL_COUNTER++;
		
		//----------------------------
		// Create Header
		this.panelHeader = $(document.createElement("div"));
		this.panelHeader.addClass("card-header text-light bg-"+this.settings.cardstyle);
		this.panelHeader.attr("id", "panelHead"+this.counter);
		this.panelHeader.attr("role", "button");
		this.panelHeader.attr("data-toggle", "collapse");		
		this.panelHeader.attr("data-target", "#collapse"+this.counter);			
	 }
		 
	 /********************************************
	  * Return the JQuery Panel object
	  * @param 
	  ********************************************/
	 getPanel(){
		//----------------------------
		// Populate Header
		this.panelHeader.html("");
		this.panelHeader.append(this.settings.title); 
			
		this.panel.append(this.panelHeader);

		//----------------------------
		// Create Collapse Container
		var collapseContainer = $(document.createElement("div"));
		collapseContainer.addClass("collapse");
		collapseContainer.attr("id", "collapse"+this.counter);
		//collapseContainer.attr("role", "tabpanel");
		collapseContainer.attr("aria-labelledby", "panelHead"+this.counter);
		
		this.panel.append(collapseContainer);
		
		//----------------------------
		// Create Body
		var panelBody = $(document.createElement("div"));
		panelBody.addClass("card-body");
		collapseContainer.append(panelBody);
		panelBody.append(this.settings.body);
		
		return this.panel;
		 
	 }
	 
	 /********************************************
	  * Append the panel to the jquery object.
	  * @param parent JQuery object
	  ********************************************/
	 appendTo(parent){
		 parent.append(this.getPanel()); 
	 }
 }

/******************************************************************
 * Print the list of results found in the database.
 * 
 * @param parent JQuery object
 * @param data object containing the list of results.
 * 
 ******************************************************************/
class CFWToggleButton{
	
	constructor(url, params, isEnabled){
		this.url = url;
		this.params = params;
		this.isLocked = false;
		this.button = $('<button class="btn btn-sm">');
		this.button.data('instance', this);
		this.button.attr('onclick', 'cfw_toggleTheToggleButton(this)');
		this.button.html($('<i class="fa"></i>'));
		
		if(isEnabled){
			this.setEnabled();
		}else{
			this.setDisabled();
		}
	}
	
	/********************************************
	 * Change the display of the button to locked.
	 ********************************************/
	setLocked(){
		this.isLocked = true;
		this.button
		.prop('disabled', this.isLocked)
		.attr('title', "Cannot be changed")
		.find('i')
			.removeClass('fa-check')
			.removeClass('fa-ban')
			.addClass('fa-lock');
	}
	/********************************************
	 * Change the display of the button to enabled.
	 ********************************************/
	setEnabled(){
		this.isEnabled = true;
		this.button.addClass("btn-success")
			.removeClass("btn-danger")
			.attr('title', "Click to Disable")
			.find('i')
				.addClass('fa-check')
				.removeClass('fa-ban');
	}
	
	/********************************************
	 * Change the display of the button to locked.
	 ********************************************/
	setDisabled(){
		this.isEnabled = false;
		this.button.removeClass("btn-success")
			.addClass("btn-danger")
			.attr('title', "Click to Enable")
			.find('i')
				.removeClass('fa-check')
				.addClass('fa-ban');
	}

	/********************************************
	 * toggle the Button
	 ********************************************/
	toggleButton(){
		if(this.isEnabled){
			this.setDisabled();
		}else{
			this.setEnabled();
		}
	}
	
	/********************************************
	 * Send the request and toggle the button if
	 * successful.
	 ********************************************/
	onClick(){
		var button = this.button;

		CFW.http.getJSON(this.url, this.params, 
			function(data){
				if(data.success){
					var instance = $(button).data('instance');
					instance.toggleButton();
					CFW.ui.addToast("Saved!", null, "success", CFW.config.toastDelay);
				}
			}
		);
	}
	
	/********************************************
	 * Toggle the table filter, default is true.
	 ********************************************/
	appendTo(parent){
		parent.append(this.button);
	}
}

function cfw_createToggleButton(url, params, isEnabled){
	return new CFWToggleButton(url, params, isEnabled);
}

function cfw_toggleTheToggleButton(button){
	var CFWToggleButton = $(button).data('instance');
	CFWToggleButton.onClick();
}
