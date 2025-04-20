
/**************************************************************************************************************
 * CFW.js
 * ======
 * Component library for the core framwork.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
	

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
	 addHeader(header, clazz){
		 var th = $('<th>');
		 var div = $('<div>');
		 
		 if(clazz != null){
			 th.addClass(clazz);
		 }
		 
		 th.append(div);
		 div.append(header);
		 this.thead.append(th);
	 }
	 
	 /********************************************
	  * Adds headers using a string array.
	  ********************************************/
	 addHeaders(headerStringArray, classesArray){
		 
		 var htmlString = "";
		 
		 for(var i = 0; i < headerStringArray.length; i++){
			 
			 if(classesArray == null || classesArray.length == 0){
				 htmlString += '<th><span>'+headerStringArray[i]+'</span></th>';
		 	 }else{
		 		 if(i <= classesArray.length-1 ){
		 			 htmlString += '<th class="'+classesArray[i]+'"><span>'+headerStringArray[i]+'</span></th>';
		 		 }
		 	 }
			 
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
		 
		 var wrapper = $('<div class="flex-grow-1 w-100">');
		 if(this.settings.filterable){
			 var filter = $('<input id="'+this.id+'-filter" type="text" class="form-control form-control-sm tablefilter-marker" onkeyup="cfw_filterTable(this)" placeholder="Filter Table...">');
			 wrapper.append(filter);
			 //jqueryObject.append('<span style="font-size: xx-small;"><strong>Hint:</strong> The filter searches through the innerHTML of the table rows. Use &quot;&gt;&quot; and &quot;&lt;&quot; to search for the beginning and end of a cell content(e.g. &quot;&gt;Test&lt;&quot; )</span>');
			 filter.data("table", this.table);
		 }
		 
		 if(this.settings.stickyheader){
			 this.thead.find("th").addClass("cfw-sticky-th bg-dark text-light");
			 this.settings.responsive = false;
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
var CFW_GLOBAL_PANEL_COUNTER = 0;

class CFWPanel{
	
	 constructor(customSettings){
		 
		 this.settings = {
			// the style to define the cards color
			cardstyle: null,
			// the style used for the text
			textstyle: null,
			// the style used for the header text, if null, textstyle will be used
			textstyleheader: "white",
			// the title of the panel
			title: "&nbsp;",
			//additional content on the right side of the title
			titleright: "&nbsp;",
			//the content of the panel
			body: "&nbsp;",
			//set to true to make the header smaller
			narrow: false,
			// set a max height for the content before scrolling occurs
			maxheight: null
		}
		 
		 Object.assign(this.settings, customSettings);
		 
		//----------------------------
		// resolve classes
		var panelClasses = 'cfwRecordContainer card';
		var panelHeaderClasses = 'card-header d-flex';
		
		if(this.settings.narrow){
			panelHeaderClasses += ' p-1 pl-2';
		} 
		
		//----------------------------
	     // Create Panel
		 this.panel = $(document.createElement("div"));
		 this.panel.addClass(panelClasses);
	
		 this.counter = CFW_GLOBAL_PANEL_COUNTER++;
		
		//----------------------------
		// Create Header
		this.panelHeader = $(document.createElement("div"));
		this.panelHeader.addClass(panelHeaderClasses);
		this.panelHeader.attr("id", "panelHead"+this.counter);	
				
		//----------------------------
		// Create Body
		this.panelBody = $(document.createElement("div"));
		
		//----------------------------
		// Add Styles
		if(this.settings.cardstyle != null){
			CFW.colors.colorizeElement(this.panel, this.settings.cardstyle, "border");
			CFW.colors.colorizeElement(this.panelHeader, this.settings.cardstyle, "bg");			
		}
		
		if(this.settings.textstyle != null){
			CFW.colors.colorizeElement(this.panel, this.settings.textstyle, "text");
		} 
		
		if(this.settings.textstyleheader != null){
			CFW.colors.colorizeElement(this.panelHeader, this.settings.textstyleheader, "text");
		} 
				
	 }
		 
	 /********************************************
	  * Return the JQuery Panel object
	  * @param 
	  ********************************************/
	 getPanel(){
		//----------------------------
		// Populate Header
		let panelTitle = $('<div class="flex-grow-1">');
		panelTitle.append(
			'<div class="cfw-fa-box"></i>'
			+ '<i class="fas fa-chevron-right mr-2"></i>'
			+ '<i class="fas fa-chevron-down mr-2"></i>'
		+ '</div>');
		panelTitle.append(this.settings.title);
		panelTitle.addClass('cursor-pointer');
		panelTitle.attr("role", "button");
		panelTitle.attr("data-toggle", "collapse");		
		panelTitle.attr("data-target", "#collapse"+this.counter);		
		panelTitle.attr("aria-expanded", false);	
		
		let headerRight = $('<div style="top: 10px; right: 10px;">');
		
		this.panelHeader.html(""); 
		this.panelHeader.append(panelTitle);
		headerRight.append(this.settings.titleright); 
		this.panelHeader.append(headerRight);
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
		
		this.panelBody.addClass("card-body");
		if(this.settings.maxheight != null){
			this.panelBody.css("max-height", this.settings.maxheight);
			this.panelBody.css("overflow-y", "scroll");
		}
		collapseContainer.append(this.panelBody);
		this.panelBody.append(this.settings.body);
		
		return this.panel;
		 
	 }

	 /********************************************
	  * Return the JQuery PanelBody object
	  * @param 
	  ********************************************/
	 onclick(callbackFunction){
		
		if(callbackFunction != null){
			this.panelHeader.on("click", callbackFunction);
		}	
		
	 }
	 /********************************************
	  * Return the JQuery PanelBody object
	  * @param 
	  ********************************************/
	 getPanelBody(){
		return this.panelBody;
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
 * Creates a CFWCard
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
var CFW_GLOBAL_CARD_COUNTER = 0;

class CFWCard{
	
	 constructor(customSettings){
		 
		 this.settings = {
			// the style to define the cards color
			cardstyle: null,
			// the style used for the text
			textstyle: null,
			// the style used for the header text, if null, textstyle will be used
			textstyleheader: null,
			// the title of the card
			title: "&nbsp;",
			//additional content on the right side of the title
			titleright: "&nbsp;",
			//the content of the card
			body: "&nbsp;",
			//set to true to make the header smaller
			narrow: false,
		}
		 
		 Object.assign(this.settings, customSettings);
		 
		//----------------------------
		// resolve classes
		var cardClasses = 'cfwRecordContainer card';
		var cardHeaderClasses = 'card-header d-flex';
				
		if(this.settings.narrow){
			cardClasses += ' pr-0 mr-0';
			cardHeaderClasses += ' p-1 pl-2';
		} 
		
		
		//----------------------------
	    // Create Card
		this.card = $(document.createElement("div"));
		this.card.addClass(cardClasses);
		 
		 this.counter = CFW_GLOBAL_CARD_COUNTER++;
		
		//----------------------------
		// Create Header
		this.cardHeader = $(document.createElement("div"));
		this.cardHeader.addClass(cardHeaderClasses);
		this.cardHeader.attr("id", "cardHead"+this.counter);	
		
		//----------------------------
		// Add Styles
		if(this.settings.cardstyle != null){
			CFW.colors.colorizeElement(this.card, this.settings.cardstyle, "border");
			CFW.colors.colorizeElement(this.cardHeader, this.settings.cardstyle, "bg");			
		}
		
		if(this.settings.textstyle != null){
			CFW.colors.colorizeElement(this.card, this.settings.textstyle, "text");
		} 
		
		if(this.settings.textstyleheader != null){
			CFW.colors.colorizeElement(this.cardHeader, this.settings.textstyleheader, "text");
		} 
	 }
		 
	 /********************************************
	  * Return the JQuery Card object
	  * @param 
	  ********************************************/
	 getCard(){
		//----------------------------
		// Populate Header
		let cardTitle = $('<div class="flex-grow-1">');
		cardTitle.append(this.settings.title);		
		let headerRight = $('<div style="top: 10px; right: 10px;">');
		
		this.cardHeader.html(""); 
		this.cardHeader.append(cardTitle);
		headerRight.append(this.settings.titleright); 
		this.cardHeader.append(headerRight);
		this.card.append(this.cardHeader);

		//----------------------------
		// Create Body
		var cardBody = $(document.createElement("div"));
		cardBody.addClass("card-body");
		this.card.append(cardBody);
		cardBody.append(this.settings.body);
		
		return this.card;
		 
	 }
	 
	 /********************************************
	  * Append the card to the jquery object.
	  * @param parent JQuery object
	  ********************************************/
	 appendTo(parent){
		 parent.append(this.getCard()); 
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
	
	constructor(url, params, isEnabled, style){
		
		this.options = {
			url: url,
			params: params,
			isEnabled: isEnabled,
			isLocked: false,
			button: $('<button class="btn btn-sm">'),
			//following are all css classes
			bgColorTrue: 'btn-success',
			bgColorFalse: 'btn-danger',
			textColorTrue: '',
			textColorFalse: '',
			trueIcon: 'fa-check',
			falseIcon: 'fa-ban',
			lockedIcon: 'fa-lock',
		};
		
		//this.options = Object.assign({}, this.defaultOptions, customOptions);
		
		if(style == "fave"){
			this.styleFaveButton();
		}
		
		this.options.button
			.data('instance', this)
			.attr('onclick', 'cfw_toggleTheToggleButton(this)')
			.html($('<i class="fa"></i>'))
			;
					
		if(isEnabled){
			this.setEnabled();
		}else{
			this.setDisabled();
		}
	}
	
	/********************************************
	 * Change the display of the button to a fave button.
	 ********************************************/
	styleFaveButton(){
		this.options.button = $('<a class="btn btn-sm">');
		
		this.options.bgColorTrue = '';
		this.options.bgColorFalse = '';
		
		this.options.textColorTrue = 'text-cfw-yellow';
		this.options.textColorFalse = 'text-cfw-gray';
		
		this.options.trueIcon = 'fa-star';
		this.options.falseIcon = 'fa-star';
		this.options.lockedIcon = 'fa-lock';
		
	}
	
	/********************************************
	 * Change the display of the button to locked.
	 ********************************************/
	setLocked(){
		this.options.isLocked = true;
		this.options.button
			.prop('disabled', this.options.isLocked)
			.attr('title', "Cannot be changed")
			.find('i')
				.removeClass(this.options.trueIcon)
				.removeClass(this.options.falseIcon)
				.addClass(this.options.lockedIcon)
				;
	}
	
	/********************************************
	 * Change the display of the button to enabled.
	 ********************************************/
	setEnabled(){
		this.options.isEnabled = true;
		this.options.button
			.removeClass(this.options.bgColorFalse)
			.addClass(this.options.bgColorTrue)
			.attr('title', "Click to Disable")
			.find('i')
				.removeClass(this.options.falseIcon)
				.removeClass(this.options.textColorFalse)
				.addClass(this.options.trueIcon)
				.addClass(this.options.textColorTrue)
				;
				
	}
	
	/********************************************
	 * Change the display of the button to locked.
	 ********************************************/
	setDisabled(){
		this.options.isEnabled = false;
		this.options.button
			.removeClass(this.options.bgColorTrue)
			.addClass(this.options.bgColorFalse)
			.attr('title', "Click to Enable")
			.find('i')
				.removeClass(this.options.trueIcon)
				.removeClass(this.options.textColorTrue)
				.addClass(this.options.falseIcon)
				.addClass(this.options.textColorFalse)
				;
	}

	/********************************************
	 * toggle the Button
	 ********************************************/
	toggleButton(){
		if(this.options.isEnabled){
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
		var button = this.options.button;

		CFW.http.getJSON(this.options.url, this.options.params, 
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
	 * Returns the button
	 ********************************************/
	getButton(){
		return this.options.button;
	}
	
	/********************************************
	 * Toggle the table filter, default is true.
	 ********************************************/
	appendTo(parent){
		parent.append(this.options.button);
	}
}

function cfw_ui_createToggleButton(url, params, isEnabled, style){
	return new CFWToggleButton(url, params, isEnabled, style);
}

function cfw_toggleTheToggleButton(button){
	let toggleButton = $(button).data('instance');
	toggleButton.onClick();
}
