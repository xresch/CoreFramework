
/**************************************************************************************************************
 * CFW.js
 * ======
 * Main library for the core framwork.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/


/**************************************************************************************
 * Chains the function after the existing window.onload function.
 *
 *************************************************************************************/
function cfw_utils_chainedOnload(func) {
  var oldonload = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = func;
  } else {
    window.onload = function() {
      if (oldonload) {
        oldonload();
      }
      func();
    }
  }
}


/**************************************************************************************
 * Either executes a function or evaluates a string a s javascript code.
 *
 *************************************************************************************/
function cfw_utils_executeCodeOrFunction(jsCodeOrFunction){
	if(typeof jsCodeOrFunction === "function"){
		jsCodeOrFunction();
	}else{
		eval(jsCodeOrFunction);
	}
	
}

/**************************************************************************************
 * returns a random string
 *
 *************************************************************************************/
function cfw_utils_randomString(maxChars){
	return Math.random().toString(maxChars).substring(2, maxChars /2) + Math.random().toString(maxChars).substring(2, maxChars /2);
}


/************************************************************************************************
 * Check if a string value is null or empty string
 ************************************************************************************************/
function cfw_utils_isNullOrEmpty(value){

	if(value === undefined || value === null ||  value === '' || (typeof value === 'string' && value.trim() == '') ){
		return true;
	}
	
	return false;
}

/************************************************************************************************
 * Replaces all occurences of one string with another.
 ************************************************************************************************/
function cfw_utils_replaceAll(string, search, replace) {
  return string.split(search).join(replace);
}

/************************************************************************************************
 * Replaces all URLs in the string with html links.
 ************************************************************************************************/
function cfw_utils_urlToLink(string) {
  return string.replaceAll(/(.*?)(http[s]?:\/\/[^\s]+)(.*?)/gm, '$1<a target=":blank" href="$2">$2</a>$3' );;
}

/************************************************************************************************
 * Converts null and undefined to the specified nullToThis value.
 * 
 ************************************************************************************************/
function cfw_utils_nullTo(value, nullToThis){

	if(value === undefined || value === null ){
		return nullToThis;
	}
	
	return value;
}

/************************************************************************************************
 * Sets the precision of floating point numbers to a certain amount of decimals.
 * Can alos 
 * 
 ************************************************************************************************/
function cfw_utils_setFloatPrecision(value, numDecimals, convertIntegers, nullToThis){

	if(value == null && nullToThis != undefined){
		return nullToThis;
	}
	
	if(value == null || isNaN(value)) {
		 return value;
	}
	
	if( !convertIntegers && Number.isInteger(value)){
		return value;
	}
	
	return value.toFixed(numDecimals);
}



/************************************************************************************************
 * Writes a string value to the Clipboard.
 * 
 ************************************************************************************************/
function cfw_utils_clipboardWrite(stringValue){
	navigator.permissions.query({name: "clipboard-write"}).then(result => {
	  if (result.state == "granted" || result.state == "prompt") {

		  navigator.clipboard.writeText(stringValue).then(function() {
		    /* clipboard successfully set */
			CFW.ui.addToastInfo('Copied to clipboard.')
		  }, function() {
		    /* clipboard write failed */
			CFW.ui.addToastDanger('Copy to clipboard failed.')
		  });
			
	  }else{
		  CFW.ui.addToastWarning('Access to clipboard seems to be blocked or not supported by your browser.')
	  }
	});
}

/************************************************************************************************
 * Calls the callback function with the retrieved string value from the clipboard.
 * 
 ************************************************************************************************/
function cfw_utils_clipboardRead(callbackFunction){
	navigator.permissions.query({name: "clipboard-read"}).then(result => {
	  if (result.state == "granted" || result.state == "prompt") {

		  var clipboardText = "";
		  var promise =  navigator.clipboard.readText();
		  
		  promise.then( 
				function(value){callbackFunction(value)}
			  , function(error) { clipboardText = ""; CFW.ui.addToastDanger('Reading from clipboard failed: '+error) }
			);
		  
	  }else{
		  CFW.ui.addToastWarning('Access to clipboard seems to be blocked or not supported by your browser.(result.state='+result.state +')')
	  }
	});
}

/************************************************************************************************
 * Returns a Random integer between min(inclusive) and max(exclusive).
 ************************************************************************************************/
function cfw_utils_randomInt(min, max) {
	return Math.floor(Math.random() * (max - min) + min);
}

/**************************************************************************************
 * Converts a string values to the appropriate value.
 * @param fieldname name of the field, if starts with "JSON_", will parse as JSON.
 * @param value to convert
 * @param numbersAsStrings define if number should be converted to strings. Needed to
 *        keep leading zeros.
 *
 *************************************************************************************/
function cfw_utils_convertStringsToType(fieldname, value,  numbersAsStrings){

	var result = value;

	if(typeof value == 'string'){
		//---------------------------
		// Convert String JSON
		if(fieldname.startsWith("JSON_") 
		   && value.startsWith("{")){
			result = JSON.parse(value);
			
		//---------------------------
		// Convert String Numbers
		} else if(!(value === '') && !isNaN(value)){
			if(numbersAsStrings){
				result = value;
			}else{
				result = Number(value);
			}
			
		//---------------------------
		// Convert String true/false to boolean
		}else if(value.toLowerCase() == 'true'){
			result = true;
		}else if(value.toLowerCase() == 'false'){
			result = false;
		}
	}
	
	return result;
}


/*************************************************************
 * Adds coloring to the element based on the records value and
 * the value added in the fields bgstylefield and textstylefield.
 * @param element the DOM or JQuery element to colorize
 * @param color either one of:
 *      - a CSS color name
 *      - an RGB color code starting with '#'
 *      - CFW color class starting with 'cfw-'
 * @param type either 'bg' | 'text' | 'border' | 'table'
 * @param borderSize Optional size definition for the border.
 *************************************************************/
function cfw_colors_colorizeElement(element, color, type, borderSize){
	
	var $element = $(element);
	if(CFW.utils.isNullOrEmpty(color)){ return; }

	if(type == 'bg'){
		
		//--------------------------------
		// Use shorthand-classes for 
		// common colors to reduce dom size
		switch(color){
			case "cfw-green": 	
			case "green":	
				$element.addClass("bgg");  return; break;
				
			case "cfw-limegreen": 				
			case "limegreen": 				
				$element.addClass("bglg"); return; break;
				
			case "cfw-yellow": 					
			case "yellow": 					
				$element.addClass("bgy"); return; break;
				
			case "cfw-orange": 		
			case "orange": 		
				$element.addClass("bgo"); return; break;
				
			case "cfw-red": 		
			case "red": 		
				$element.addClass("bgr"); return; break;
				
			default: 	
				/* keep null */;
		}
			
		if(color.startsWith("cfw-")){
			$element.addClass("bg-"+color)
		}else{
			$element.css("background-color", color);
		}
		return;
		
		
	} else if( type == "text" ){
		
		if(color.startsWith("cfw-")){
			$element.addClass("text-"+color);
		}else{
			$element.css("color", color);
		}
		
		
	} else if (type == 'border'){
		
		let size = (borderSize == null) ? "1px" : borderSize; 
		
		if(color.startsWith("cfw-")){
			 $element.addClass("border-"+color);
			 $element.css("border-width", size);
			 $element.css("border-style", "solid");
		}else{
			
			$element.css("border", size+" solid "+color);
		}
		return;
	}else if(type == 'table'){
		
		if(color.startsWith("cfw-")){
			$element.addClass("table-"+color);
		}else{
			$element.css("background-color", color);
		}
		return;
	}
			 		
}
		
		
	
/************************************************************************************************
 * Creates a random RGB string like "rgba(112, 54, 210, 1.0)" 
 ************************************************************************************************/
function cfw_colors_RGB2HEX(rgb) {
    if(CFW.utils.isNullOrEmpty(rgb) ){
		return rgb;
	}
    
    if (/^#[0-9A-F]{6}$/i.test(rgb)) return rgb;

    rgb = rgb.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+).*$/);
   
    function hex(x) {
        return ("0" + parseInt(x).toString(16)).slice(-2);
    }
    
    return "#" + hex(rgb[1]) + hex(rgb[2]) + hex(rgb[3]);
}
	
/************************************************************************************************
 * Creates a random RGB string like "rgba(112, 54, 210, 1.0)" 
 ************************************************************************************************/
function cfw_colors_randomRGB() {
	var r = Math.floor(Math.random()*255);
	var g = Math.floor(Math.random()*255);
	var b = Math.floor(Math.random()*255);
	
	return 'rgba('+r+','+g+','+b+', 1.0)';
	
}


/************************************************************************************************
 * Creates a random HSL string like "hsla(112, 54, 210, 1.0)" 
 * @param minS The minimum saturation in percent
 * @param maxS The maximum saturation in percent
 * @param minL The minimum Lightness in percent
 * @param maxL The maximum Lightness in percent
 ************************************************************************************************/
function cfw_colors_randomHSL(minS, maxS, minL, maxL) {
	var h = CFW.utils.randomInt(0,256);
	var s = CFW.utils.randomInt(minS, maxS);
	var l = CFW.utils.randomInt(minL, maxL);
	
	return 'hsla('+h+','+s+'%,'+l+'%, 1.0)';
}

/************************************************************************************************
 * Creates a random HSL string like "hsla(112, 54, 210, 1.0)" for the given Hue.
 * @param hue the hue or any integer value (remainder for hue % 360 will be used)
 * @param minS The minimum saturation in percent
 * @param maxS The maximum saturation in percent
 * @param minL The minimum Lightness in percent
 * @param maxL The maximum Lightness in percent
 ************************************************************************************************/
function cfw_colors_randomSL(hue, minS, maxS, minL, maxL) {
	
	var s = CFW.utils.randomInt(minS, maxS);
	var l = CFW.utils.randomInt(minL, maxL);
	
	return 'hsla('+hue % 360+','+s+'%,'+l+'%, 1.0)';
}

/************************************************************************************************
 * Returns the style class string that indicates the status that is worse.
 * If both are null, returns CFW.style.green.
 * @param thresholdClassOne threshold class 
 * @param thresholdClassTwo threshold class 
 ************************************************************************************************/
function cfw_colors_getThresholdWorse(thresholdClassOne, thresholdClassTwo ) {
	

	if(thresholdClassOne == null){
		if(thresholdClassTwo != null){
			return thresholdClassTwo;
		}else{
			return CFW.style.green;
		}
	}
	
	// increase performance
	if(thresholdClassOne == thresholdClassTwo){
		return thresholdClassOne;
	}
	
	if(thresholdClassOne != CFW.style.red){
		switch(thresholdClassTwo){
			
			case CFW.style.red: 
				return CFW.style.red;
			break;
			
			
			case CFW.style.orange: 
				return CFW.style.orange;
			break;
			
			
			case CFW.style.yellow: 
				if(thresholdClassOne != CFW.style.red
				&& thresholdClassOne != CFW.style.orange){
					return CFW.style.yellow;
				}
			break;
			
			
			case CFW.style.limegreen: 
				if(thresholdClassOne != CFW.style.red
				&& thresholdClassOne != CFW.style.orange
				&& thresholdClassOne != CFW.style.yellow
				){
					return CFW.style.limegreen;
				}
			break;
			
			case CFW.style.green: 
				if(thresholdClassOne != CFW.style.red
				&& thresholdClassOne != CFW.style.orange
				&& thresholdClassOne != CFW.style.yellow
				&& thresholdClassOne != CFW.style.limegreen
				){
					return CFW.style.green;
				}
			break;
			
			default:
				// do nothing
			break;
		}
	}

	return thresholdClassOne;
}
/************************************************************************************************
 * Returns a cfw style string for the value based on the defined thresholds e.g CFW.style.green.
 * You can use the string for creating a class like: 
 *   "bg-cfw-green"
 *   "text-cfw-green"
 *   "border-cfw-green"
 *   "table-cfw-green"
 *   
 * If all the thresholds are null/undefined or the value is NaN returns an CFW.style.none.
 * You can define thresholds values increasing from Excellent to Danger, or from Danger to Excellent.
 * You can let thresholds undefined/null to skip the color. Values below the lowest threshold value
 * will result in CFW.style.notevaluated.
 * 
 * If isDisabled is set to "true", returns CFW.style.disabled.
 * 
 * @param value the value that should be thresholded
 * @param tExellent the threshold for excellent
 * @param tGood the threshold for good
 * @param tWarning the threshold for warning
 * @param tEmergency the threshold for emergency
 * @param tDanger the threshold for danger
 * @param isDisabled define if the thresholding is disabled
 * @param evaluateLower if true, values below the lowest threshold are evaluated as the lowest color.
 *        If false, values are not evaluated
 ************************************************************************************************/
function cfw_colors_getThresholdStyle(
		  value
		, tExellent
		, tGood
		, tWarning
		, tEmergency
		, tDanger
		, isDisabled
		, evaluateLower) {

	//---------------------------
	// Initial Checks
	if(isDisabled) { return CFW.style.disabled; }
	
	if(isNaN(value)
	|| (
		CFW.utils.isNullOrEmpty(tExellent)
	   && CFW.utils.isNullOrEmpty(tGood)
	   && CFW.utils.isNullOrEmpty(tWarning)
	   && CFW.utils.isNullOrEmpty(tEmergency)
	   && CFW.utils.isNullOrEmpty(tDanger)
	   )
	){
		return CFW.style.none;
	}

	//---------------------------
	// Find Threshold direction
	var direction = 'HIGH_TO_LOW';
	var thresholds = [tExellent, tGood, tWarning, tEmergency, tDanger];
	var firstDefined = null;

	for(var i = 0; i < thresholds.length; i++){
		var current = thresholds[i];
		if (!CFW.utils.isNullOrEmpty(current)){
			if(firstDefined == null){
				firstDefined = current;
			}else{
				if(current != null && firstDefined < current ){
					direction = 'LOW_TO_HIGH';
				}
				break;
			}
		}
	}

	//---------------------------
	// Set Colors for Thresholds

	var styleString = CFW.style.notevaluated;
	
	if(direction == 'HIGH_TO_LOW'){
		if 		(!CFW.utils.isNullOrEmpty(tExellent) 	&& value >= tExellent) 	{ styleString = CFW.style.green; } 
		else if (!CFW.utils.isNullOrEmpty(tGood) 		&& value >= tGood) 		{ styleString = CFW.style.limegreen; } 
		else if (!CFW.utils.isNullOrEmpty(tWarning) 	&& value >= tWarning) 	{ styleString = CFW.style.yellow; } 
		else if (!CFW.utils.isNullOrEmpty(tEmergency) 	&& value >= tEmergency) { styleString = CFW.style.orange; } 
		else if (!CFW.utils.isNullOrEmpty(tDanger) 		&& value >= tDanger)  	{ styleString = CFW.style.red; } 
		else{ 
			if(evaluateLower){
				styleString = CFW.style.red; 
			}else{
				styleString = CFW.style.notevaluated; 
			}
		} 
	}else{
		if 		(!CFW.utils.isNullOrEmpty(tDanger) 		&& value>= tDanger)  	{ styleString = CFW.style.red; } 
		else if (!CFW.utils.isNullOrEmpty(tEmergency) 	&& value>= tEmergency) 	{ styleString = CFW.style.orange; } 
		else if (!CFW.utils.isNullOrEmpty(tWarning) 	&& value>= tWarning) 	{ styleString = CFW.style.yellow; } 
		else if (!CFW.utils.isNullOrEmpty(tGood) 		&& value>= tGood) 		{ styleString = CFW.style.limegreen; } 
		else if (!CFW.utils.isNullOrEmpty(tExellent) 	&& value>= tExellent) 	{ styleString = CFW.style.green; } 	
		else{ 
			if(evaluateLower){
				styleString = CFW.style.green; 
			}else{
				styleString = CFW.style.notevaluated; 
			}
		}
	}
	
	return styleString;
}

/************************************************************************************************
 * Evaluates two threshholds going in opposite directions, for example:
 *   - tGreen is 10, everything >=10 will be green
 *   - tRed is -10, everything <=-10 will be red
 *   - 
 * 
 * Returns a cfw style string for the value based on the defined thresholds e.g CFW.style.green.
 * You can use the string for creating a class like: 
 *   "bg-cfw-green"
 *   "text-cfw-green"
 *   "border-cfw-green"
 *   "table-cfw-green"
 *   
 * If all the thresholds are null/undefined or the value is NaN returns an CFW.style.none.
 * You can define thresholds values increasing from tGreen to tRed, or from tRed to tGreen.
 * 
 * If isDisabled is set to "true", returns CFW.style.disabled.
 * 
 * @param value the value that should be thresholded
 * @param tGreen the threshold for excellent
 * @param tRed the threshold for danger
 * @param isDisabled define if the thresholding is disabled
 ************************************************************************************************/
function cfw_colors_getSplitThresholdStyle(
		  value
		, tGreen
		, tRed
		, isDisabled
		) {

	//---------------------------
	// Initial Checks
	if(isDisabled) { return CFW.style.disabled; }
	
	if(isNaN(value)
	|| (
		CFW.utils.isNullOrEmpty(tGreen)
	   && CFW.utils.isNullOrEmpty(tRed)
	   )
	){
		return CFW.style.none;
	}

	//---------------------------
	// Find Threshold direction
	var direction = 'HIGH_TO_LOW';
	var thresholds = [tGreen, tRed];
	
	if(tGreen < tRed){
		direction = 'LOW_TO_HIGH';
	}

	//---------------------------
	// Set Colors for Thresholds
	var styleString = CFW.style.notevaluated;
	
	if(direction == 'HIGH_TO_LOW'){
		if 		(!CFW.utils.isNullOrEmpty(tGreen) 	&& value >= tGreen) 	{ styleString = CFW.style.green; } 
		else if (!CFW.utils.isNullOrEmpty(tRed) 		&& value <= tRed)  	{ styleString = CFW.style.red; } 
	}else{
		if 		(!CFW.utils.isNullOrEmpty(tRed) 		&& value >= tRed)  	{ styleString = CFW.style.red; } 
		else if (!CFW.utils.isNullOrEmpty(tGreen) 	&& value <= tGreen) 	{ styleString = CFW.style.green; } 	
	}
	
	return styleString;
}


/**************************************************************************************
 * Filters items in the selected DOM nodes.
 * The items that should be filtered(based on their HTML content) have to be found with
 * the itemSelector.
 * 
 * Example Usage:
 * CFW.utils.filterItems('#menu-content', inputField, '.filterable');
 * 
 *@param context the JQuery selector for the element containing the items which should be filtered.
 *@param searchField the searchField of the field containing the search string.
 *@param itemSelector the JQuery selector for the object which should be filtered.
 *************************************************************************************/
function cfw_utils_filterItems(context, searchField, itemSelector){

	var filterContext = $(context);
	var input = $(searchField);
	
	var filter = input.val().toUpperCase();
	
	filterContext.find(itemSelector).each(function( index ) {
		  var current = $(this);

		  if (current.html().toUpperCase().indexOf(filter) > -1) {
			  
			  current.css("display", "");
			  
			  current.parents()
					 .filter(".collapse")
					 .removeClass('hide')
					 .addClass('show')
					 ;

		  } else {
			  current.css("display", "none")
					 .find(".collapse")
					 .removeClass('show')
					 .addClass('hide');
		  }
	});
}


/**************************************************************************************
 * Filter the rows of a table by the value of the search field.
 * This method is best used by triggering it on the onchange-event on the search field
 * itself.
 * The search field has to have an attached JQuery data object($().data(name, value)), ¨
 * pointing to the table that should be filtered.
 * 
 * @param searchField 
 * @return nothing
 *************************************************************************************/
function cfw_filterTable(searchField){
	
	var table = $(searchField).data("table");
	var input = searchField;

	var filter = input.value.toUpperCase();

	table.find("tbody tr, >tr").each(function( index ) {

		  if ($(this).html().toUpperCase().indexOf(filter) > -1) {
			  $(this).css("display", "");
		  } else {
			  $(this).css("display", "none");
		  }
	});

}


/**************************************************************************************
 * 
 * @param fieldIDOrJQuery either the id of the field e.g. "myField" or a JQueryObject
 *************************************************************************************/
function cfw_internal_initializeBooleanSwitch(fieldIDOrJQuery){
	
	var inputField;
	if (fieldIDOrJQuery instanceof jQuery){
		inputField = fieldIDOrJQuery;
	}else{
		inputField = $('#'+fieldIDOrJQuery);
	}

	labelYes = CFWL("cfw_core_yes", "Yes");
	labelNo = CFWL("cfw_core_no", "cfw_core_no");

	var booleanSwitch = $(`<label class="cfw-switch">
		<div class="cfw-switch-slider" onclick="cfw_internal_toggleBooleanSwitchValue(this);">
			 <div class="cfw-switch-slider-on">${labelYes}</div>
			 <div class="cfw-switch-slider-button">&nbsp;</div>
			 <div class="cfw-switch-slider-off">${labelNo}</div>
		</div>
	</label>`);
		
	inputField.before(booleanSwitch);
	booleanSwitch.prepend(inputField);
	
	cfw_internal_setBooleanSwitchValue(booleanSwitch, inputField.val());
}

/**************************************************************************************
 * 
 * @param switchButton the button that was clicked
 *************************************************************************************/
function cfw_internal_toggleBooleanSwitchValue(switchButton){
	
	var elButton = $(switchButton); 
	var elSwitchWrapper =  elButton.closest('.cfw-switch');
	var hontouNiSelected = elSwitchWrapper.hasClass('switch-on');
		
	cfw_internal_setBooleanSwitchValue(elSwitchWrapper, !hontouNiSelected)


}

/**************************************************************************************
 * 
 * @param elSwitchWrapper the main switch element
 *************************************************************************************/
function cfw_internal_setBooleanSwitchValue(elSwitchWrapper, isSelected){
	
	if(typeof isSelected === "string"){
		if(isSelected.trim().toLowerCase() == "true"){
			isSelected = true;
		}else{
			isSelected = false;
		}
	}
	
	var zeInput =  elSwitchWrapper.find('input');
	
	zeInput.val(isSelected);
	
	if(isSelected){
		elSwitchWrapper.addClass('switch-on');
		elSwitchWrapper.removeClass('switch-off');
	}else{
		elSwitchWrapper.removeClass('switch-on');
		elSwitchWrapper.addClass('switch-off');
	}
	
}

/**************************************************************************************
 * Initialize a form create with the CFWForm Java Class.
 * @param formIDOrObject the ID, element or JQueryObject of the form
 * @param epochMillis the initial date in epoch time or null
 * @return nothing
 *************************************************************************************/
function cfw_initializeForm(formIDOrObject){
	if(formIDOrObject != null){
		let form = $(formIDOrObject)
		var formID = form.attr("id");
		// workaround, force evaluation
		eval(form.find("script").text());
		eval("intializeForm_"+formID+"();");
	}
}

/**************************************************************************************
 * Initialize a Date and/or Timepicker created with the Java object CFWField.
 * @param fieldID the name of the field
 * @param epochMillis the initial date in epoch time or null
 * @return nothing
 *************************************************************************************/
function cfw_initializeSummernote(formID, editorID){
	
	var formSelector = '#'+formID;
	var editorSelector = formSelector+' #'+editorID;

	//--------------------------------------
	// Initialize Editor
	//--------------------------------------
	var editor = $(editorSelector);
	
	if(editor.length == 0){
		CFW.ui.addToastDanger('Error: the editor field is unknown: '+fieldID);
		return;
	}
	
	editor.summernote({
        placeholder: 'Enter your Text',
        tabsize: 2,
        height: 200,
        dialogsInBody: true
      });
	
	//--------------------------------------
	// Blur for saving
	//--------------------------------------
	$('.note-editing-area').on('blur', function() {
		if ($('#summernote').summernote('codeview.isActivated')) {
			$('#summernote').summernote('codeview.deactivate');
		}
	});
	
	//--------------------------------------
	// Get Editor Contents
	//--------------------------------------
	$.get('/cfw/formhandler', {id: formID, summernoteid: editorID})
	  .done(function(response) {
		  $(editor).summernote("code", response.payload.html);
	  })
	  .fail(function(response) {
		  CFW.ui.addToastDanger("Issue Loading Editor Content", "danger", CFW.config.toastErrorDelay);
	  })
	  .always(function(response) {
		  cfw_internal_handleMessages(response);			  
	  });
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeQueryEditor(fieldID){
	var id = '#'+fieldID;

	var textarea = $(id);

	new CFWQueryEditor(textarea, {});

}
/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeExpandableTextareaField(fieldID){
	var id = '#'+fieldID;

	var textarea = $(id);

	var wrapper = $('<div class="cfw-expandable-textarea-wrapper w-100">');
	wrapper.append('<i class="fas fa-expand" onclick="cfw_initializeExpandableTextareaField_toggle(this)" ></i>');
	textarea.before(wrapper);
	wrapper.append(textarea);	

}

/*************************************************************************************
 * Create a filterable select.
 * @param fieldID id of the input field. The value of the field 
 *        will be used as the selected value.
 * @param valueLabelOptions an object of objects containing values and
 *        labels, e.g. [ { value: 1, label: MyLabel}, ... ]
 * @param filterable true if a the select should have a filter, false otherwise
 * 
 *************************************************************************************/
function cfw_initializeSelect(fieldID, valueLabelOptions, filterable){
	
	var id = '#'+fieldID;

	var originalField = $(id);
	var selectedValue = originalField.val();
	originalField.css('display', 'none');
	
	//--------------------------
	// Create Wrapper
	var wrapper = $('<div id="'+fieldID+'-cfw-select" class="cfw-select w-100">');
	wrapper.data('options', valueLabelOptions);
	originalField.before(wrapper);
	wrapper.append(originalField);	
		
	//--------------------------
	// Create Dropdown
	var classes = originalField.attr('class');
	var dropdownHTML = `<div class="dropdown">
			<button id="${id}-dropdownMenuButton" class="form-control mb-2 dropdown-toggle dropdown-toggle-wide ${classes}" style="text-align: start;" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
			   &nbsp;
			</button>
			<div class="dropdown-menu dropdown-scroll w-100" aria-labelledby="${id}-dropdownMenuButton">
		`
		;
	
	//--------------------------
	// Create Options
	if(valueLabelOptions != null){
		for(let i = 0; i < valueLabelOptions.length; i++){
			let currentOption = valueLabelOptions[i];
			
			let label = '&nbsp;';
			if( !CFW.utils.isNullOrEmpty(currentOption.label) ) {
					label = currentOption.label
			}
			
			dropdownHTML += ' <a class="dropdown-item filterable" onclick="cfw_setSelectValue(\''+fieldID+'\', \''+currentOption.value.replaceAll('"', '&quot;')+'\')">'+label+'</a>';
		}
	}
	
	dropdownHTML += '</div> </div>';
	

	//--------------------------------
	// Finishing Touch
	wrapper.append(dropdownHTML);

	cfw_setSelectValue(fieldID, selectedValue);
	
	//--------------------------------
	// Add Filter
	let menu = wrapper.find('.dropdown-menu');
	let filterField = $('<input type="text" class="form-control-sm w-fill ml-1 mr-1"'
								+' placeholder="Filter..."'
								+' onkeyup="cfw_filterSelect(this, event, \''+fieldID+'\')">');
	
	menu.prepend(filterField);
	
	
}


/*************************************************************************************
 * Filter ze select options.
 * @param filterField the field that filters
 * @param event 
 * @param fieldID of the the originalFieldID
 * 
 *************************************************************************************/
function cfw_filterSelect(filterField, event, fieldID){
	
	event.preventDefault();
	event.stopPropagation();
		
	CFW.utils.filterItems('#'+fieldID+'-cfw-select', filterField, '.filterable');
}

/*************************************************************************************
 * Set ze selected value.
 * @param optionElement the originalID of the field
 * @param valueToSelect an object of objects containing values and
 *        labels, e.g. [ { value: 1, label: MyLabel}, ... ]
 * @param filterable true if a the select should have a filter, false otherwise
 * 
 *************************************************************************************/
function cfw_setSelectValue(fieldID, valueToSelect){
	
	var inputField = $('#'+fieldID);
	var wrapper = inputField.closest('.cfw-select');
	var options = wrapper.data('options');
	
	var button = wrapper.find('button');
	
	if(options != null){
		for(var i = 0; i < options.length; i++){
			
			var currentOption = options[i];
			if(currentOption.value == valueToSelect){

				inputField.val(currentOption.value);
				
				if( !CFW.utils.isNullOrEmpty(currentOption.label) ) {
					button.text(currentOption.label);
				}else{
					button.html("&nbsp;");
				}
				
			}
			
		}
	}
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeExpandableTextareaField_toggle(buttonElement){

	var button = $(buttonElement);
	var wrapper = $(buttonElement).closest(".cfw-expandable-textarea-wrapper");

	if(wrapper.hasClass('expanded')){
		wrapper.removeClass('expanded');
		button.removeClass('fa-compress');
		button.addClass('fa-expand');
		
	}else{
		wrapper.addClass('expanded');
		button.addClass('fa-compress');
		button.removeClass('fa-expand');
	}

}


/**************************************************************************************
 * Initialize a TagField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeTagsField(fieldID, maxTags){
	
	var id = '#'+fieldID;

	var tagsfield = $(id);
	
	//$(id).tagsinput();

	
	tagsfield.tagsinput({
		tagClass: 'cfw-tag btn-primary',
		maxTags: maxTags,
		maxChars: 1024,
		trimValue: true,
		allowDuplicates: false,
		addOnBlur: false
//		confirmKeys: [188, 13]
	});
	
	//----------------------------------
	// Add Classes
	var bootstrapTagsinput = $(id+'-tagsinput').closest('.bootstrap-tagsinput');
	if(tagsfield.hasClass('form-control-sm')){
		bootstrapTagsinput.addClass('bootstrap-tagsinput-sm')
	}
	
	if(tagsfield.closest('form').hasClass('form-inline')){
		bootstrapTagsinput.addClass('bootstrap-tagsinput-inline');
	}
	
	//----------------------------------
	// Hack to make it work more stable
	$(id+'-tagsinput').on('keyup', function (e) {
		// Enter and Comma
		if (e.keyCode == 13 || e.keyCode == 188) {
			$(id).tagsinput('add', this.value);
			this.value = '';
		}
	});
	
	
	
}

/**************************************************************************************
 * Initialize a TagField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeTagsSelectorField(fieldID, maxTags, values){
	
	var id = '#'+fieldID;

	var tagsfield = $(id);
	
	// mark with CSS class for selecting the class afterwards
	//tagsfield.addClass("cfw-tags-selector");
	
	tagsfield.tagsinput({
		tagClass: 'cfw-tag btn-primary',
		itemValue: 'value',
		itemText: 'label',
		maxTags: maxTags,
		//maxChars: 30,
		trimValue: true,
		allowDuplicates: false,
//		onTagExists: function(item, $tag) {
//			$tag.fadeIn().fadeIn();
//
//		}
	});
	
	//----------------------------------
	// Add Classes
	var bootstrapTagsinput = $(id+'-tagsinput').closest('.bootstrap-tagsinput');
	if(tagsfield.hasClass('form-control-sm')){
		bootstrapTagsinput.addClass('bootstrap-tagsinput-sm')
	}
	
	if(tagsfield.closest('form').hasClass('form-inline')){
		bootstrapTagsinput.addClass('bootstrap-tagsinput-inline');
	}
	
	//----------------------------------
	// Add Values
	for(var key in values){
		tagsfield.tagsinput('add', { "value": key , "label": values[key] });
	}

}

/**************************************************************************************
 * Initialize a TagField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeCheckboxesField(fieldID, options, values){
	
	var id = '#'+fieldID;

	var checkboxesField = $(id);

	var wrapper = $('<div class="cfw-checkboxes-field-wrapper flex-grow-1 flex-column">');
	checkboxesField.before(wrapper);
	wrapper.append(checkboxesField);
	checkboxesField.val(JSON.stringify(values));
	
	//----------------------------------
	// Add Classes
	var classes = checkboxesField.attr('class');
	checkboxesField.addClass('d-none');

	//----------------------------------
	// Add Values
	for(var key in options){
		var label = options[key];
		var checked = (values != null && values[key] == "true") ? "checked" : "";
		wrapper.append('<label><input type="checkbox" class="cfw-checkbox" id="'+fieldID+'-'+key+'" name="'+key+'" onchange="cfw_internal_updateCheckboxesField(this)" '+checked+' />'+label+'</label>');
	}
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateCheckboxesField(element){
	
	var wrapper = $(element).closest('.cfw-checkboxes-field-wrapper');
	
	var originalField = wrapper.find('> input').first();
	
	var checkboxValues = {}
	
	checkboxesStateJsonString = originalField.val();

	if(!CFW.utils.isNullOrEmpty(checkboxesStateJsonString) && checkboxesStateJsonString != "null"){
		checkboxValues = JSON.parse(checkboxesStateJsonString);
	}
	
	wrapper.find('.cfw-checkbox').each(function(index, element){
		let current = $(element);
		let key = current.attr("name");
		let value = current.prop('checked');
		
		if(!CFW.utils.isNullOrEmpty(key)
		|| !CFW.utils.isNullOrEmpty(value)){
			checkboxValues[key] = value;
		}
	})
	
	originalField.val(JSON.stringify(checkboxValues));

}


/**************************************************************************************
 * Initialize a TagField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeValueLabelField(fieldID, values){
	
	var id = '#'+fieldID;

	var valueLabelField = $(id);

	
	var wrapper = $('<div class="cfw-value-label-field-wrapper flex-grow-1">');
	valueLabelField.before(wrapper);
	wrapper.append(valueLabelField);
	valueLabelField.val(JSON.stringify(values));
	
	//----------------------------------
	// Add Classes
	var classes = valueLabelField.attr('class');
	valueLabelField.addClass('d-none');

	//----------------------------------
	// Add Values
	for(var key in values){
		var fields = cfw_initializeValueLabelField_createField(key, values[key]);
		wrapper.append(fields);
	}
	
	//----------------------------------
	// Add At least one Empty Line
	wrapper.append(cfw_initializeValueLabelField_createField('', ''));
	
	//----------------------------------
	// Add Create Button
	wrapper.append(
			'<div class="d-flex">'
				+'<div class="flex-grow-1">&nbsp;</div>'
				+'<div class="btn btn-sm btn-primary" onclick="cfw_internal_addValueLabelField(this)"><i class="fas fa-plus-circle"></i></div>'
			+'</div>'
		);
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeValueLabelField_createField(key, value){
	return '<div class="cfw-value-label d-flex pb-1">'
			+'<div class="d-flex flex-column">'
				+'<i class="fas fa-sort-up cfw-field-sort-up" onclick="cfw_internal_sortValueLabelField(this, \'up\')" ></i>'
				+'<i class="fas fa-sort-down cfw-field-sort-down" onclick="cfw_internal_sortValueLabelField(this, \'down\')"></i>'
			+'</div>'
			+'<input type="text" class="form-control-sm flex-grow-1" placeholder="Value" onchange="cfw_internal_updateValueLabelField(this)" value="'+key.replaceAll('"', '&quot;')+'">'
			+'<input type="text" class="form-control-sm flex-grow-1" placeholder="Label" onchange="cfw_internal_updateValueLabelField(this)" value="'+value.replaceAll('"', '&quot;')+'">'	
	+'</div>';	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_addValueLabelField(element){
	
	var button = $(element)
	var buttonParent = button.parent();
	
	buttonParent.before(cfw_initializeValueLabelField_createField('', ''))

}
/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateValueLabelField(element){
	
	var wrapper = $(element).closest('.cfw-value-label-field-wrapper');
	
	var originalField = wrapper.find('> input');
	
	var newValueLabels = {};
	wrapper.find('.cfw-value-label').each(function(index, element){
		let current = $(element);
		let value = current.find('input:first').val();
		let label = current.find('input:last').val();
		
		if(!CFW.utils.isNullOrEmpty(value)
		|| !CFW.utils.isNullOrEmpty(label)){
			newValueLabels[value] = label;
		}
	})
	
	originalField.val(JSON.stringify(newValueLabels));

}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_sortValueLabelField(element, upOrDown){
	
	var clickedButton = $(element);
	var currentValueLabel = clickedButton.closest(".cfw-value-label");
	
	
	if(upOrDown.toLowerCase() == "up"){
		var previousValueLabel = currentValueLabel.prev(".cfw-value-label");
		previousValueLabel.before(currentValueLabel);
	}else{
		var nextValueLabel = currentValueLabel.next(".cfw-value-label");
		nextValueLabel.after(currentValueLabel);
	}
	
	 cfw_internal_updateValueLabelField(element);
}

/**************************************************************************************
 * Initialize a Custom List Field created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeCustomListField(fieldID, values){
	
	var id = '#'+fieldID;

	var customListField = $(id);

	
	var wrapper = $('<div class="cfw-custom-list-field-wrapper flex-grow-1">');
	customListField.before(wrapper);
	wrapper.append(customListField);
	customListField.val(JSON.stringify(values));
	
	//----------------------------------
	// Add Classes
	var classes = customListField.attr('class');
	customListField.addClass('d-none');

	//----------------------------------
	// Add Values
	for(var index in values){
		var fields = cfw_initializeCustomListField_createField(values[index]);
		wrapper.append(fields);
	}
	
	//----------------------------------
	// Add At least one Empty Line
	wrapper.append(cfw_initializeCustomListField_createField('', ''));
	
	//----------------------------------
	// Add Create Button
	wrapper.append(
			'<div class="d-flex">'
				+'<div class="flex-grow-1">&nbsp;</div>'
				+'<div class="btn btn-sm btn-primary" onclick="cfw_internal_addCustomListField(this)"><i class="fas fa-plus-circle"></i></div>'
			+'</div>'
		);
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_initializeCustomListField_createField(value){
	return '<div class="cfw-custom-list-item d-flex">'
			+'<input type="text" class="form-control-sm flex-grow-1" placeholder="Value" onchange="cfw_internal_updateCustomListField(this)" value="'+value.replaceAll('"', '&quot;')+'">'	
	+'</div>';	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_addCustomListField(element){
	
	var button = $(element)
	var buttonParent = button.parent();
	
	buttonParent.before(cfw_initializeCustomListField_createField(''))

}
/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateCustomListField(element){
	
	var wrapper = $(element).closest('.cfw-custom-list-field-wrapper');
	
	var originalField = wrapper.find('> input');
	
	var newListValues = [];
	wrapper.find('.cfw-custom-list-item').each(function(index, element){
		let current = $(element);
		let value = current.find('input').val();
		
		if(!CFW.utils.isNullOrEmpty(value)){
			newListValues.push(value);
		}
	})
	
	originalField.val(JSON.stringify(newListValues));

}

/**************************************************************************************
 * Initialize a ChartSettingsField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeChartSettingsField(fieldID, jsonData){
	
	var selector = '#'+fieldID;

	var chartSettingsField = $(selector);

	var wrapper = $('<div class="cfw-chartsettings-field-wrapper flex-grow-1">');
	chartSettingsField.before(wrapper);
	wrapper.append(chartSettingsField);
	
	chartSettingsField.val(JSON.stringify(jsonData));
	
	//----------------------------------
	// Add Classes
	//var classes = chartSettingsField.attr('class');
	//chartSettingsField.addClass('d-none');

	//----------------------------------
	// Create HTML
	
	wrapper.append(`<div class="dropdown dropright">
		<button id="chartSettingsButton" class="btn btn-sm btn-primary dropdown-toggle" type="button" onclick="CFW.ui.toggleDropdownMenuFixed(event, this)" >
			Chart Settings
		</button>
		<div id="${fieldID}-DROPDOWNMENU" class="dropdown-menu-fixed col-sm-12" onclick="event.stopPropagation();">

			<div class="row m-1"><strong>Series Display</strong></div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-CHARTTYPE">Chart Type:</label>   
				<div class="col-sm-9">
					<select id="${fieldID}-CHARTTYPE"  class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')" onkeydown="return event.key != 'Enter';" id="chart_type">
						<option value="area">Area</option>
						<option value="line">Line</option>
						<option value="bar">Bar</option>
						<option value="scatter">Scatter</option>
						<option value="steppedarea">Stepped Area</option>
						<option value="steppedline">Stepped Line</option>
						<option value="sparkline">Spark Line</option>
						<option value="sparkarea">Spark Area</option>
						<option value="sparkbar">Spark Bar</option>
						<option value="pie">Pie</option>
						<option value="doughnut">Doughnut</option>
						<option value="radar">Radar</option>
						<option value="polar">Polar</option>
					</select>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-POINTRADIUS">
					Point Radius:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-POINTRADIUS" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-STACKED">
					Stacked:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-STACKED" name="${fieldID}-STACKED" value="true" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-STACKED" name="${fieldID}-STACKED" value="false" checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-SHOWLEGEND">
					Show Legend:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SHOWLEGEND" name="${fieldID}-SHOWLEGEND" value="true" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SHOWLEGEND" name="${fieldID}-SHOWLEGEND" value="false"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>

			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-SPANGAPS">
					Connect Gaps:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SPANGAPS" name="${fieldID}-SPANGAPS" value="true" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SPANGAPS" name="${fieldID}-SPANGAPS" value="false"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>

			<div class="row m-1"><strong>Axes Display</strong></div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-SHOWAXES">
					Show Axes:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SHOWAXES" name="${fieldID}-SHOWAXES" value="true"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-SHOWAXES" name="${fieldID}-SHOWAXES" value="false" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-XAXIS_TYPE">
					X Axis Type:
				</label>   
				<div class="col-sm-9">
					<select id="${fieldID}-XAXIS_TYPE" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
						<option value="time" selected="">Time</option>
						<option value="linear">Linear</option>
						<option value="logarithmic">Logarithmic</option>
					</select>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-YAXIS_TYPE">
					Y Axis Type:
				</label>   
				<div class="col-sm-9">
					<select id="${fieldID}-YAXIS_TYPE" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
						<option value="linear">Linear</option>
						<option value="logarithmic">Logarithmic</option>
					</select>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-YAXIS_MIN">
					Y Minimum:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-YAXIS_MIN" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-YAXIS_MAX">
					Y Maximum:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-YAXIS_MAX" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1"><strong>Multi Chart Display</strong></div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-MULTICHART">
					Multiple Charts:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-MULTICHART" name="${fieldID}-MULTICHART" value="true"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-MULTICHART" name="${fieldID}-MULTICHART" value="false" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-MULTICHARTTITLE">
					Show Title:
				</label>   
				<div class="col-sm-9">
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-MULTICHARTTITLE" name="${fieldID}-MULTICHARTTITLE" value="true"  checked="checked" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">true</label>
					</div>
					<div class="form-check form-check-inline">
						<input class="form-check-input" type="radio" id="${fieldID}-MULTICHARTTITLE" name="${fieldID}-MULTICHARTTITLE" value="false" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')"> 
						<label class="form-check-label" for="inlineRadio1">false</label>
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-MULTICHARTCOLUMNS">
					Columns:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-MULTICHARTCOLUMNS" type="number" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-HEIGHT">
					Chart Height:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-HEIGHT" type="text" class="form-control-inline form-control-sm col-md-12" onchange="cfw_internal_updateChartSettings(\'${fieldID}\')">
				</div>
			</div>
															
		</div>
	</div>`);
	
	//-----------------------------------------
	// Set Data
	 cfw_internal_applyChartSettings(fieldID, wrapper, jsonData);
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_applyChartSettings(fieldID, wrapper, chartSettings){
	
	var selector = "#"+fieldID; 
	
	if(chartSettings == null || chartSettings.charttype == null){
		return;
	}

	wrapper.find(selector+"-CHARTTYPE").val(chartSettings.charttype );
	wrapper.find(selector+"-POINTRADIUS").val(chartSettings.pointradius );
	wrapper.find(selector+"-XAXIS_TYPE").val(chartSettings.xtype );
	wrapper.find(selector+"-YAXIS_TYPE").val(chartSettings.ytype );
	wrapper.find(selector+"-YAXIS_MIN").val(chartSettings.ymin );
	wrapper.find(selector+"-YAXIS_MAX").val(chartSettings.ymax );
	
	wrapper.find(selector+"-STACKED[value='" + chartSettings.stacked + "']").attr("checked", "checked");  
	wrapper.find(selector+"-SHOWLEGEND[value='" + chartSettings.showlegend + "']").attr("checked", "checked");  
	wrapper.find(selector+"-SHOWAXES[value='" + chartSettings.showaxes + "']").attr("checked", "checked");  
	wrapper.find(selector+"-SPANGAPS[value='" + chartSettings.spangaps + "']").attr("checked", "checked");
	
	wrapper.find(selector+"-MULTICHART[value='" + chartSettings.multichart + "']").attr("checked", "checked");  
	wrapper.find(selector+"-MULTICHARTTITLE[value='" + chartSettings.multicharttitle + "']").attr("checked", "checked");  
	wrapper.find(selector+"-MULTICHARTCOLUMNS").val(chartSettings.multichartcolumns );
	wrapper.find(selector+"-HEIGHT").val(chartSettings.height );
		
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateChartSettings(elementID){
	
	var selector = "#"+elementID;
	var originalField = $(selector);
	var wrapper = originalField.closest('.cfw-chartsettings-field-wrapper');
	
	//--------------------------------------
	// Create Data Structure
	var chartSettings = {};
	
	chartSettings.charttype 		= $(selector+'-CHARTTYPE').val();
	chartSettings.xtype 			= $(selector+'-XAXIS_TYPE').val();
	chartSettings.ytype 			= $(selector+'-YAXIS_TYPE').val();
	chartSettings.ymin 				= $(selector+'-YAXIS_MIN').val();
	chartSettings.ymax				= $(selector+'-YAXIS_MAX').val();
	chartSettings.stacked  			= $(selector+"-STACKED:checked").val();  
	chartSettings.showlegend		= $(selector+"-SHOWLEGEND:checked").val();  
	chartSettings.showaxes			= $(selector+"-SHOWAXES:checked").val();  
	chartSettings.spangaps			= $(selector+"-SPANGAPS:checked").val();  
	chartSettings.pointradius		= $(selector+"-POINTRADIUS").val();  
	
	chartSettings.multichart		= $(selector+"-MULTICHART:checked").val();  
	chartSettings.multicharttitle 	= $(selector+"-MULTICHARTTITLE:checked").val();  
	chartSettings.multichartcolumns	= $(selector+'-MULTICHARTCOLUMNS').val();
	chartSettings.height			= $(selector+'-HEIGHT').val();
	
	//--------------------------------------
	// Convert Numbers and Booleans
	chartSettings.pointradius 		= !isNaN(chartSettings.pointradius) ? parseFloat(chartSettings.pointradius) : 0;
	chartSettings.ymin 				= !isNaN(chartSettings.ymin) ? parseFloat(chartSettings.ymin) : 0;
	chartSettings.ymax 				= !isNaN(chartSettings.ymax) ? parseFloat(chartSettings.ymax) : null;
	chartSettings.multichartcolumns = !isNaN(chartSettings.multichartcolumns) ? parseInt(chartSettings.multichartcolumns) : null;
	
	chartSettings.stacked  			= (chartSettings.stacked.trim().toLowerCase() == "true") ? true : false;  
	chartSettings.showlegend  		= (chartSettings.showlegend.trim().toLowerCase() == "true") ? true : false;  
	chartSettings.showaxes  		= (chartSettings.showaxes.trim().toLowerCase() == "true") ? true : false;  
	chartSettings.multichart  		= (chartSettings.multichart.trim().toLowerCase() == "true") ? true : false;  
	chartSettings.multicharttitle  	= (chartSettings.multicharttitle.trim().toLowerCase() == "true") ? true : false;  
	
	//--------------------------------------
	// Set ChartSettings
	originalField.val(JSON.stringify(chartSettings));
	//originalField.dropdown('toggle');

}

/**************************************************************************************
 * Initialize a ChartSettingsField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeColorPickerField(fieldID, colorOrClass){
	
	var selector = '#'+fieldID;

	var colorpickerField = $(selector);

	var wrapper = $('<div class="cfw-chartsettings-field-wrapper flex-grow-1">');
	colorpickerField.before(wrapper);
	wrapper.append(colorpickerField);
	
	colorpickerField.val(colorOrClass);
	
	//----------------------------------
	// Add Classes
	//var classes = chartSettingsField.attr('class');
	//chartSettingsField.addClass('d-none');

	//----------------------------------
	// Create HTML
	
	wrapper.append(`<div class="cfw-colorpicker dropdown dropright">
		<button id="${fieldID}-BUTTON" class="btn btn-sm" type="button" onclick="CFW.ui.toggleDropdownMenuFixed(event, this)" >
			&nbsp;
		</button>
		<div id="${fieldID}-DROPDOWNMENU" class="dropdown-menu-fixed col-sm-12" onclick="event.stopPropagation();">
			
			<div class="row m-1"><strong>Choose Color</strong></div>
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-CUSTOMCOLOR">
					Custom:
				</label>   
				<div class="col-sm-9">
					<input id="${fieldID}-CUSTOMCOLOR" class="form-control-inline form-control-sm col-md-12" type="color" onchange="cfw_internal_updateColorPickerValueCustom(\'${fieldID}\')" >	
				</div>
			</div>
		
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-CUSTOMCOLOR">
					Default:
				</label>   
				<div class="col-sm-9">
					<div class="cfw-color-box cursor-pointer bg-cfw-none" onclick="cfw_internal_updateColorPickerValue('${fieldID}', null)">&nbsp;</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-CUSTOMCOLOR">
					Transparent:
				</label>   
				<div class="col-sm-9">
					<div class="cfw-color-box cursor-pointer  bg-cfw-transparent" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-transparent')">&nbsp;</div>
				</div>
			</div>
			
			<div class="row m-1"><strong>Default Colors</strong></div>
			<div class="row m-1">  
				<div class="col-sm-12">
					<div class="cfw-color-box cursor-pointer bg-cfw-green" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-green')">&nbsp;</div>
					<div class="cfw-color-box cursor-pointer bg-cfw-limegreen" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-limegreen')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-yellow" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-yellow')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-orange" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-orange')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-red" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-red')">&nbsp;</div>	
				</div>
			</div>	
			
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<div class="cfw-color-box cursor-pointer bg-cfw-blue" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-blue')">&nbsp;</div>
					<div class="cfw-color-box cursor-pointer bg-cfw-indigo" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-indigo')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-purple" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-purple')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-pink" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-pink')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-cyan" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-cyan')">&nbsp;</div>	
				</div>
			</div>	
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<div class="cfw-color-box cursor-pointer bg-cfw-white" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-white')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-lightgray" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-lightgray')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-gray" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-gray')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-darkgray" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-darkgray')">&nbsp;</div>	
					<div class="cfw-color-box cursor-pointer bg-cfw-black" onclick="cfw_internal_updateColorPickerValue('${fieldID}', 'cfw-black')">&nbsp;</div>	
				</div>
			</div>	
												
		</div>
	</div>`);
	
	//-----------------------------------------
	// Set Data
	 cfw_internal_updateColorPickerValue(fieldID, colorOrClass);
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateColorPickerValueCustom(fieldID){

	let selector = "#"+fieldID; 
	
	let colorOrClass = $(selector+"-CUSTOMCOLOR").val();
	

	cfw_internal_updateColorPickerValue(fieldID, colorOrClass);
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_updateColorPickerValue(fieldID, colorOrClass){
	
	let selector = "#"+fieldID; 
	let originalField = $(selector);
	
	//--------------------------
	// Update Selected Value
	originalField.val(colorOrClass);

	//--------------------------
	// Get UI thingies to Update
	let button = $(selector+"-BUTTON");
	let customPicker = $(selector+"-CUSTOMCOLOR");
	
	//--------------------------
	// Update Colors
	button.css("background-color", "");
	button.attr('class', 'btn btn-sm');
	if(!CFW.utils.isNullOrEmpty(colorOrClass)){
		
		CFW.colors.colorizeElement(button, colorOrClass, "bg", null);
		
		let hexColor = colorOrClass;

		if(!hexColor.startsWith("#")){
			let workspace = CFW.ui.getWorkspace();
			let tempDiv = $('<div>');
			workspace.append(tempDiv); // needed or won't get a color when using css-classes
			
			CFW.colors.colorizeElement(tempDiv, colorOrClass, "bg", null);
			let rgbColor = tempDiv.css('background-color');

			hexColor = CFW.colors.RGB2HEX(rgbColor);
		}
		customPicker.val(hexColor);
	}else{
		
		CFW.colors.colorizeElement(button, "cfw-none", "bg", null);
	}
	
}

/**************************************************************************************
 * Initialize a ScheduleField created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_initializeScheduleField(fieldID, jsonData){
	
	var selector = '#'+fieldID;

	var scheduleField = $(selector);

	var wrapper = $('<div class="cfw-schedule-field-wrapper flex-grow-1">');
	scheduleField.before(wrapper);
	wrapper.append(scheduleField);
	
	scheduleField.val(JSON.stringify(jsonData));
	
	//----------------------------------
	// Add Classes
	//var classes = scheduleField.attr('class');
	//scheduleField.addClass('d-none');

	//----------------------------------
	// Create HTML
	
	wrapper.append(`<div class="dropdown">
		<button id="scheduleButton" class="btn btn-sm btn-primary dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
			Select Schedule
		</button>
		<div id="${fieldID}-DROPDOWNMENU" class="dropdown-menu col-sm-12" aria-labelledby="dropdownMenuButton" onclick="event.stopPropagation();">
			
			<div class="row m-1"><strong>Timeframe</strong></div>
			
			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-STARTDATETIME">Start Time:</label>   
				<div class="col-sm-9">
					<div class="custom-control-inline w-100 mr-0">
						<input id="${fieldID}-STARTDATETIME-datepicker" type="date" onchange="cfw_updateTimeField('${fieldID}-STARTDATETIME')" class="col-md-8 form-control form-control-sm">
						<input id="${fieldID}-STARTDATETIME-timepicker" type="time" onchange="cfw_updateTimeField('${fieldID}-STARTDATETIME')" class="col-md-4 form-control form-control-sm">	   
						<input id="${fieldID}-STARTDATETIME" type="hidden" class="form-control" placeholder="Earliest" name="${fieldID}-STARTDATETIME" onkeydown="return event.key != 'Enter';">
					</div>
				</div>
			</div>

			<div class="row m-1">  
				<label class="col-sm-3" for="${fieldID}-ENDDATETIME">
					<input type="radio" id="${fieldID}-RADIO-ENDTYPE" name="${fieldID}-RADIO-ENDTYPE" value="END_DATE_TIME" onkeydown="return event.key != 'Enter';"> End Time:
				</label>   
				<div class="col-sm-9">
					<div class="custom-control-inline w-100 mr-0">
						<input id="${fieldID}-ENDDATETIME-datepicker" type="date" onchange="cfw_updateTimeField('${fieldID}-ENDDATETIME')" class="col-md-8 form-control form-control-sm">
						<input id="${fieldID}-ENDDATETIME-timepicker" type="time" onchange="cfw_updateTimeField('${fieldID}-ENDDATETIME')" class="col-md-4 form-control form-control-sm">	   
						<input id="${fieldID}-ENDDATETIME" type="hidden" class="form-control" placeholder="Earliest" name="${fieldID}-ENDDATETIME" onkeydown="return event.key != 'Enter';">
					</div>
				</div>
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-ENDTYPE" name="${fieldID}-RADIO-ENDTYPE" value="RUN_FOREVER" > Run Forever
				</div>   
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-ENDTYPE" name="${fieldID}-RADIO-ENDTYPE" value="EXECUTION_COUNT" > End after <input id="${fieldID}-EXECUTIONCOUNT" type="number" class="form-control-inline form-control-sm" style="width: 60px;" min="0"> <span>execution(s)</span>
				</div>   
			</div>
			
			<div class="row m-1"><strong>Interval</strong></div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-INTERVAL" name="${fieldID}-RADIO-INTERVAL" value="EVERY_X_MINUTES" > Every <input id="${fieldID}-EVERYXMINUTES" type="number" class="form-control-inline form-control-sm" style="width: 60px;" min="0"> <span>minute(s)</span>
				</div>   
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-INTERVAL" name="${fieldID}-RADIO-INTERVAL" value="EVERY_X_DAYS" > Every <input id="${fieldID}-EVERYXDAYS" type="number" class="form-control-inline form-control-sm" style="width: 60px;" min="0"> <span>day(s)</span>
				</div>   
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-INTERVAL" name="${fieldID}-RADIO-INTERVAL" value="EVERY_WEEK" > Every week on 
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-MON" name="${fieldID}-EVERYWEEK-MON" > <span>Mon</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-TUE" name="${fieldID}-EVERYWEEK-TUE" > <span>Tue</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-WED" name="${fieldID}-EVERYWEEK-WED" > <span>Wed</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-THU" name="${fieldID}-EVERYWEEK-THU" > <span>Thu</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-FRI" name="${fieldID}-EVERYWEEK-FRI" > <span>Fri</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-SAT" name="${fieldID}-EVERYWEEK-SAT" > <span>Sat</span>
					<input class="ml-2" type="checkbox" id="${fieldID}-EVERYWEEK-SUN" name="${fieldID}-EVERYWEEK-SUN" > <span>Sun</span>
				</div>   
			</div>
			
			<div class="row m-1">  
				<div class="col-sm-12">
					<input type="radio" id="${fieldID}-RADIO-INTERVAL" name="${fieldID}-RADIO-INTERVAL" value="CRON_EXPRESSION" > Cron Expression: <input id="${fieldID}-CRON_EXPRESSION" list="cron_samples" class="form-control-inline form-control-sm">
					<datalist id="cron_samples">
						<option value="42 * * ? * *">Every minute at the 42th second</option>
						<option value="0 15,30,45 * ? * *">Every hour at minutes 15, 30 and 45 </option>
						<option value="0 0 22 ? * MON-FRI">Every business day at 22 o'clock </option>
						<option value="0 0,15,30,45 8-18 ? * MON-FRI">Every business day from 08:00 to 18:00 every 15 minutes  </option>
						<option value="0 0 4 L-2 * ?">Every month on the second to last day of the month, at 04:00</option>
						<option value="0 0 12 1L * ?">Every month on the last Sunday, at noon</option>
						<option value="0 0 12 2L * ?">Every month on the last Monday, at noon</option>
			<option value=""></option>
			<option value=""></option>
					</datalist>
				</div>   
			</div>
			
			<div class="row m-1">
				<div class="col-sm-12">
					<button class="btn btn-sm btn-primary" onclick="cfw_internal_confirmSchedule('${fieldID}');" type="button">
						{!cfw_core_confirm!}
					</button>
					<button class="btn btn-sm btn-primary" onclick="$('#${fieldID}').dropdown('toggle');" type="button">
						{!cfw_core_close!}
					</button>
				</div>
			</div>
		</div>
	</div>`);
	
	//-----------------------------------------
	// Set Data
	 cfw_internal_applySchedule(fieldID, wrapper, jsonData);
}


/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_applySchedule(fieldID, wrapper, scheduleData){
	
	var selector = "#"+fieldID; 
	
	if(scheduleData == null || scheduleData.timeframe == null){
		return;
	}
	
	cfw_initializeTimefield(fieldID+'-STARTDATETIME', scheduleData.timeframe.startdatetime);
	cfw_initializeTimefield(fieldID+'-ENDDATETIME', scheduleData.timeframe.enddatetime);
	
	//values: RUN_FOREVER, ENDDATETIME, EXECUTION_COUNT
	wrapper.find(selector+"-RADIO-ENDTYPE[value='" + scheduleData.timeframe.endtype + "']").attr("checked", "checked");  
	wrapper.find(selector+"-EXECUTIONCOUNT").val(scheduleData.timeframe.executioncount );
	
	//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
	wrapper.find(selector+"-RADIO-INTERVAL[value='" + scheduleData.interval.intervaltype + "']").attr("checked", "checked");  
	wrapper.find(selector+"-EVERYXMINUTES").val(scheduleData.interval.everyxminutes	);
	wrapper.find(selector+"-EVERYXDAYS").val(scheduleData.interval.everyxdays );
	
	if(scheduleData.interval.everyweek.MON){ wrapper.find(selector+"-EVERYWEEK-MON").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.TUE){ wrapper.find(selector+"-EVERYWEEK-TUE").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.WED){ wrapper.find(selector+"-EVERYWEEK-WED").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.THU){ wrapper.find(selector+"-EVERYWEEK-THU").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.FRI){ wrapper.find(selector+"-EVERYWEEK-FRI").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.SAT){ wrapper.find(selector+"-EVERYWEEK-SAT").attr("checked", "checked"); }
	if(scheduleData.interval.everyweek.SUN){ wrapper.find(selector+"-EVERYWEEK-SUN").attr("checked", "checked"); }

	wrapper.find(selector+"-CRON_EXPRESSION").val(scheduleData.interval.cronexpression);
	
}
/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_internal_confirmSchedule(elementID){
	
	var selector = "#"+elementID;
	var originalField = $(selector);
	var wrapper = originalField.closest('.cfw-schedule-field-wrapper');
	
	//--------------------------------------
	// Create Data Structure
	var scheduleData = {
			  timezoneOffset: new Date().getTimezoneOffset()
			, timeframe: {}
			, interval: {}
	};
	
	scheduleData.timeframe.startdatetime 	= isNaN($(selector+'-STARTDATETIME').val()) ? null : parseInt($(selector+'-STARTDATETIME').val());
	scheduleData.timeframe.endtype  	= $(selector+"-RADIO-ENDTYPE:checked").val();  //values: RUN_FOREVER, ENDDATETIME, EXECUTION_COUNT
	scheduleData.timeframe.enddatetime  	= isNaN($(selector+'-ENDDATETIME').val()) ? null : parseInt($(selector+'-ENDDATETIME').val());;
	scheduleData.timeframe.executioncount  	= $(selector+"-EXECUTIONCOUNT").val();
	
	//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
	scheduleData.interval.intervaltype  	= $(selector+"-RADIO-INTERVAL:checked").val();
	scheduleData.interval.everyxminutes		= $(selector+"-EVERYXMINUTES").val();
	scheduleData.interval.everyxdays  		= $(selector+"-EVERYXDAYS").val();
	
	scheduleData.interval.everyweek  			= {}
	scheduleData.interval.everyweek.MON  		= $(selector+"-EVERYWEEK-MON").is(":checked");
	scheduleData.interval.everyweek.TUE  		= $(selector+"-EVERYWEEK-TUE").is(":checked");
	scheduleData.interval.everyweek.WED  		= $(selector+"-EVERYWEEK-WED").is(":checked");
	scheduleData.interval.everyweek.THU  		= $(selector+"-EVERYWEEK-THU").is(":checked");
	scheduleData.interval.everyweek.FRI  		= $(selector+"-EVERYWEEK-FRI").is(":checked");
	scheduleData.interval.everyweek.SAT  		= $(selector+"-EVERYWEEK-SAT").is(":checked");
	scheduleData.interval.everyweek.SUN  		= $(selector+"-EVERYWEEK-SUN").is(":checked");

	scheduleData.interval.cronexpression  	= $(selector+"-CRON_EXPRESSION").val();
	
	//--------------------------------------
	// Validate
	var isValid = true;
	
	if( CFW.utils.isNullOrEmpty(scheduleData.timeframe.startdatetime ) ){
		CFW.ui.addToastDanger('Please specify a start time.');
		isValid = false;
	}
	
	if( CFW.utils.isNullOrEmpty(scheduleData.timeframe.endtype) ){
		CFW.ui.addToastDanger('Please select/specify how long the schedule will be valid.');
		isValid = false;
	}

	if(scheduleData.timeframe.endtype === "ENDDATETIME" 
	&& ( CFW.utils.isNullOrEmpty(scheduleData.timeframe.enddatetime)
	   || scheduleData.timeframe.enddatetime < scheduleData.timeframe.startdatetime) 
	){
		CFW.ui.addToastDanger('Please specify an end time that is after the start time.')
		isValid = false;
	}
	
	if(scheduleData.timeframe.endtype === "EXECUTION_COUNT" 
	&& CFW.utils.isNullOrEmpty(scheduleData.timeframe.executioncount) ) {
		CFW.ui.addToastDanger('Please specify the number of executions.')
		isValid = false;
	}

	if( CFW.utils.isNullOrEmpty(scheduleData.interval.intervaltype) ){
		CFW.ui.addToastDanger('Please select and interval.');
		isValid = false;
	}
	
	if(scheduleData.interval.intervaltype === "EVERY_X_MINUTES" 
	&& CFW.utils.isNullOrEmpty(scheduleData.interval.everyxminutes) ) {
		CFW.ui.addToastDanger('Please specify the number of minutes.')
		isValid = false;
	}
	
	if(scheduleData.interval.intervaltype === "EVERY_X_DAYS"){ 
	
		if(CFW.utils.isNullOrEmpty(scheduleData.interval.everyxdays) ) {
			CFW.ui.addToastDanger('Please specify the number of days.')
			isValid = false;
		}
		
		if(scheduleData.timeframe.endtype === "EXECUTION_COUNT"  ) {
			CFW.ui.addToastDanger('Execution count is not supported for interval in days.')
			isValid = false;
		}
		
		
	}
	
	if(scheduleData.interval.intervaltype === "EVERY_WEEK" ) {
		let noDaysSelected = true;
		
		if(     scheduleData.interval.everyweek.MON) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.TUE) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.WED) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.THU) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.FRI) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.SAT) { noDaysSelected = false; }
		else if(scheduleData.interval.everyweek.SUN) { noDaysSelected = false; }
		
		if(noDaysSelected){
			CFW.ui.addToastDanger('Please select at least one week day.')
			isValid = false;
		}
	}
	
	if(scheduleData.interval.intervaltype === "CRON_EXPRESSION"){
		
		if(CFW.utils.isNullOrEmpty(scheduleData.interval.cronexpression) ) {
			CFW.ui.addToastDanger('Please specify a cron expression.')
			isValid = false;
		}
		
		if(scheduleData.timeframe.endtype === "EXECUTION_COUNT"  ) {
			CFW.ui.addToastDanger('Execution count is not supported for CRON expressions.')
			isValid = false;
		}
	}
	
	//--------------------------------------
	// Set Schedule
	if(isValid){
		originalField.val(JSON.stringify(scheduleData));
		originalField.dropdown('toggle');
	}

}

/**************************************************************************************
 * Initialize a Date and/or Timepicker created with the Java object CFWField.
 * @param fieldID the name of the field
 * @param epochMillis the initial date in epoch time or null
 * @return nothing
 *************************************************************************************/
function cfw_initializeTimefield(fieldID, epochMillis){
	
	var id = '#'+fieldID;
	var datepicker = $(id+'-datepicker');
	var timepicker = $(id+'-timepicker');
	
	if(datepicker.length == 0){
		CFW.ui.addToastDanger('Error: the datepicker field is unknown: '+fieldID);
		return;
	}
	
	if(epochMillis != null){
		$(id).val(epochMillis);
		let sureInteger = parseInt(epochMillis, 10);

		var date = moment(sureInteger);
		datepicker.first().val(date.format("YYYY-MM-DD"));

		if(timepicker.length > 0 != null){
			timepicker.first().val(date.format("HH:mm"));
		}
	}
		
}

/**************************************************************************************
 * Update a Date and/or Timepicker created with the Java object CFWField.
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_updateTimeField(fieldID){
	
	var id = '#'+fieldID;
	var datepicker = $(id+'-datepicker');
	var timepicker = $(id+'-timepicker');
	
	if(datepicker.length == 0){
		CFW.ui.addToastDanger('Error: the datepicker field is unknown: '+fieldID)
	}
	
	var dateString = datepicker.first().val();
	
	if(timepicker.length > 0){
		var timeString = timepicker.first().val();
		if(timeString.length > 0 ){
			dateString = dateString +"T"+timeString;
		}
	}
	
	if(dateString.length > 0){
		$(id).val(Date.parse(dateString));
	}else{
		$(id).val('');
	}
	
}

/**************************************************************************************
 * Takes the ID of a text field which will be the target to store the timeframe picker
 * value. 
 * The original field gets hidden and will be replaced by the timeframe picker itself. 
 * 
 * @param fieldID the id of the target field(without '#')
 * @param initialData the json object containing the initial value of the field as epoch time:
 *        {
 *				earliest: 123455678989,
 *              latest: 1234567890
 *        }
 * @param onchangeCallbackFunction function taking parameters func(fieldID, updateType, earliest, latest);
 *           updateType would be one of: 'custom' | 'shift-earlier' | 'shift-later' | a preset
 *************************************************************************************/
function cfw_initializeTimeframePicker(fieldID, initialData, onchangeCallbackFunction){
	
	var selector = '#'+fieldID;

	var timeframeStoreField = $(selector);
	timeframeStoreField.addClass('d-none');
	
	var wrapper = $('<div class="cfw-timeframepicker-wrapper" data-id="'+fieldID+'">');
	timeframeStoreField.before(wrapper);
	wrapper.append(timeframeStoreField);
		
	//----------------------------------
	// Set Intial Value
	var pickerDataString = JSON.stringify(initialData);
	timeframeStoreField.val(pickerDataString);
	
	//----------------------------------
	// Add Classes
	//var classes = scheduleField.attr('class');
	//scheduleField.addClass('d-none');

	//----------------------------------
	// Create HTML
	wrapper.append( `
<div id="${fieldID}-picker" class="btn-group">
	<button class="btn btn-sm btn-primary btn-inline" onclick="cfw_timeframePicker_shift(this, 'earlier');" type="button">
		<i class="fas fa-chevron-left"></i>
	</button>
	
	<div class="dropdown">
		<button id="${fieldID}-timeframeSelectorButton" class="btn btn-sm btn-primary dropdown-toggle" type="button"  data-toggle="collapse" data-target="#${fieldID}-timepickerDropdown" aria-haspopup="true" aria-expanded="false">
			{!cfw_core_last!} 30 {!cfw_core_minutes!}
		</button>
	
	<div id="${fieldID}-timepickerDropdown" class="dropdown-menu" style="width: 400px;" aria-labelledby="dropdownMenuButton">			
		<div class="accordion" id="timePickerAccordion">
			<div class="card">
				<div class="card-header p-1 pl-2" id="timepickerPanel-0">
					<div role="button" data-toggle="collapse" data-target="#collapse0" aria-expanded="true">
						<div class="cfw-fa-box">
							<i class="fas fa-chevron-right mr-2"></i>
							<i class="fas fa-chevron-down mr-2"></i>
						</div>
						<div>
							<span>Presets</span>
						</div>
					</div>
				</div>
				<div class="collapse show" id="collapse0" data-parent="#timePickerAccordion" aria-labelledby="timepickerPanel-0">
					<div class="card-body p-1">
						<div class="row ml-0 mr-0">
							<div class="col-6">
								<a id="time-preset-5-m" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '5-m');">{!cfw_core_last!} 5 {!cfw_core_minutes!}</a>
								<a id="time-preset-15-m" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '15-m');">{!cfw_core_last!} 15 {!cfw_core_minutes!}</a>
								<a id="time-preset-30-m" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '30-m');">{!cfw_core_last!} 30 {!cfw_core_minutes!}</a>
								<a id="time-preset-1-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '1-h');">{!cfw_core_last!} 1 {!cfw_core_hour!}</a>
								<a id="time-preset-2-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '2-h');">{!cfw_core_last!} 2 {!cfw_core_hours!}</a>
								<a id="time-preset-4-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '4-h');">{!cfw_core_last!} 4 {!cfw_core_hours!}</a>
								<a id="time-preset-6-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '6-h');">{!cfw_core_last!} 6 {!cfw_core_hours!}</a>
								<a id="time-preset-12-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '12-h');">{!cfw_core_last!} 12 {!cfw_core_hours!}</a>
								<a id="time-preset-24-h" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '24-h');">{!cfw_core_last!} 24 {!cfw_core_hours!}</a>
							</div>
							<div class="col-6">
								<a id="time-preset-2-d" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '2-d');">{!cfw_core_last!} 2 {!cfw_core_days!}</a>
								<a id="time-preset-7-d" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '7-d');">{!cfw_core_last!} 7 {!cfw_core_days!}</a>
								<a id="time-preset-14-d" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '14-d');">{!cfw_core_last!} 14 {!cfw_core_days!}</a>
								<a id="time-preset-1-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '1-M');">{!cfw_core_last!} 1 {!cfw_core_month!}</a>
								<a id="time-preset-2-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '2-M');">{!cfw_core_last!} 2 {!cfw_core_months!}</a>
								<a id="time-preset-3-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '3-M');">{!cfw_core_last!} 3 {!cfw_core_months!}</a>
								<a id="time-preset-6-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '6-M');">{!cfw_core_last!} 6 {!cfw_core_months!}</a>
								<a id="time-preset-12-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '12-M');">{!cfw_core_last!} 12 {!cfw_core_months!}</a>
								<a id="time-preset-24-M" class="dropdown-item" onclick="cfw_timeframePicker_setOffset(this, '24-M');">{!cfw_core_last!} 24 {!cfw_core_months!}</a>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			
			<div class="card">
				<div class="card-header p-1 pl-2" id="timepickerPanel-1">
					<div role="button" data-toggle="collapse" data-target="#collapse1" aria-expanded="false">
						<div class="cfw-fa-box">
							<i class="fas fa-chevron-right mr-2"></i>
							<i class="fas fa-chevron-down mr-2"></i>
						</div>
						<div>
							<span>Relative Time</span>
						</div>
					</div>
				</div>
				<div class="collapse" id="collapse1" data-parent="#timePickerAccordion" aria-labelledby="timepickerPanel-1">
					<div class="card-body p-1">
						
						
						<label class="col-sm-12 col-form-label col-form-label-sm">Present Time Offset:</label>
						
						<div class="row m-1">  
							<div class="col-sm-12">
								<div class="custom-control-inline w-100 mr-0">
			    					<input id="${fieldID}-RELATIVE_OFFSET-count" type="number" class="col-md-4 form-control form-control-sm" min="0" value="4" >
			    					<select id="${fieldID}-RELATIVE_OFFSET-unit" class="col-md-8 form-control form-control-sm" >	
			    						<option value="m">{!cfw_core_minutes!}</option>
			    						<option value="h">{!cfw_core_hours!}</option>
			    						<option value="d">{!cfw_core_days!}</option>
			    						<option value="M">{!cfw_core_months!}</option>
			    					</select>   	
								</div>
							</div>
						</div>
						
						<div class="row m-1">
							<div class="col-sm-12">
								<button class="btn btn-sm btn-primary" onclick="cfw_timeframePicker_confirmRelativeOffset(this);" type="button">
									{!cfw_core_confirm!}
								</button>
							</div>
						</div>
						
						
						
					</div>
				</div>
			</div>
			
			
			<div class="card">
				<div class="card-header p-1 pl-2" id="timepickerPanel-2">
					<div role="button" data-toggle="collapse" data-target="#collapse2" aria-expanded="false">
						<div class="cfw-fa-box">
							<i class="fas fa-chevron-right mr-2"></i>
							<i class="fas fa-chevron-down mr-2"></i>
						</div>
						<div>
							<span>{!cfw_core_customtime!}</span>
						</div>
					</div>
				</div>
				<div class="collapse" id="collapse2" data-parent="#timePickerAccordion" aria-labelledby="timepickerPanel-2">
					<div class="card-body p-1">
						<div class="row m-1">  
							<label class="col-sm-2 col-form-label col-form-label-sm" for="${fieldID}-CUSTOM_EARLIEST">{!cfw_core_earliest!}:</label>   
							<div class="col-sm-10">
								<div class="custom-control-inline w-100 mr-0">
			    					<input id="${fieldID}-CUSTOM_EARLIEST-datepicker" type="date" onchange="cfw_updateTimeField('${fieldID}-CUSTOM_EARLIEST')" class="col-md-7 form-control form-control-sm">
			    					<input id="${fieldID}-CUSTOM_EARLIEST-timepicker" type="time" onchange="cfw_updateTimeField('${fieldID}-CUSTOM_EARLIEST')" class="col-md-5 form-control form-control-sm">	   
			    					<input id="${fieldID}-CUSTOM_EARLIEST" type="hidden" class="form-control" placeholder="Earliest" name="${fieldID}-CUSTOM_EARLIEST" onkeydown="return event.key != 'Enter';">
								</div>
							</div>
						</div>
						<div class="row m-1">  
							<label class="col-sm-2 col-form-label col-form-label-sm" for="${fieldID}-CUSTOM_LATEST">{!cfw_core_latest!}:</label>   
							<div class="col-sm-10">  
								<div class="custom-control-inline w-100 mr-0">
			    					<input id="${fieldID}-CUSTOM_LATEST-datepicker" type="date" onchange="cfw_updateTimeField('${fieldID}-CUSTOM_LATEST')" class="col-md-7 form-control form-control-sm">
			    					<input id="${fieldID}-CUSTOM_LATEST-timepicker" type="time" onchange="cfw_updateTimeField('${fieldID}-CUSTOM_LATEST')" class="col-md-5 form-control form-control-sm">	   
			    					<input id="${fieldID}-CUSTOM_LATEST" type="hidden" class="form-control" placeholder="Latest" name="${fieldID}-CUSTOM_LATEST" onkeydown="return event.key != 'Enter';">
								</div>
							</div>
						</div>
						<div class="row m-1">
							<div class="col-sm-12">
								<button class="btn btn-sm btn-primary" onclick="cfw_timeframePicker_confirmCustom(this);" type="button">
									{!cfw_core_confirm!}
								</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	</div>
	<button class="btn btn-sm btn-primary btn-inline" onclick="cfw_timeframePicker_shift(this, 'later');" type="button">
		<i class="fas fa-chevron-right"></i>
	</button>
</div>

	`);
	
	//----------------------------------
	// Initialize Value, do not trigger
	// callback
	if(initialData != null){

		if(initialData.offset == null){
			cfw_timeframePicker_setCustom(fieldID, initialData.earliest, initialData.latest);
		}else{
			cfw_timeframePicker_setOffset("#"+fieldID, initialData.offset);
		}
	}
	
	
	//----------------------------------
	// StoreCallback
	if( onchangeCallbackFunction != null){
		CFW.global.timeframePickerOnchangeHandlers[fieldID] = onchangeCallbackFunction;
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_timeframePicker_storeValue(fieldID, offset, earliest, latest){
	
	var selector = '#'+fieldID;
	
	// -----------------------------------------
	// Update FieldData
	var pickerData = {
		offset: 	offset,
		earliest:	parseFloat(earliest),
		latest: 	parseFloat(latest),
		clientTimezoneOffset: 	new Date().getTimezoneOffset()
	}

	$(selector).val(JSON.stringify(pickerData));
	
	// -----------------------------------------
	// Update Custom Time Selector
	cfw_initializeTimefield(fieldID+'-CUSTOM_EARLIEST', earliest);
	cfw_initializeTimefield(fieldID+'-CUSTOM_LATEST', latest);
	
	var callback = CFW.global.timeframePickerOnchangeHandlers[fieldID];
	
	if(callback != null){
		callback(fieldID, pickerData);
	}
	
}
	
/*******************************************************************************
 * 
 * @param origin either JQueryObject or id with leading #
 * @param offset the offset to set.
 ******************************************************************************/
function cfw_timeframePicker_setOffset(origin, offset){
	
	var wrapper = $(origin).closest('.cfw-timeframepicker-wrapper');
	
	var fieldID = wrapper.data('id');
	var selector = '#'+fieldID;
	

	var split = offset.split('-');
	var count = split[0];
	var unit = split[1];
	var earliestMillis = moment().utc().subtract(count, unit).utc().valueOf();
	var latestMillis = moment().utc().valueOf();

	//-----------------------------------------
	// Update Label
	var unitLocale = (unit == "m") ? 'minutes' 
					: (unit == "h") ? 'hours' 
					: (unit == "d") ? 'days' 
					: (unit == "M") ? 'months' 
					: "unknown"
					;
	
	var label = CFWL('cfw_core_last', 'Last')+" "+count+" "+CFWL('cfw_core_'+unitLocale, " ???");

	$(selector+'-timeframeSelectorButton').text(label);
	
	// -----------------------------------------
	// Update Relative Offset Picker
	$(selector+'-RELATIVE_OFFSET-count').val(count);
	$(selector+'-RELATIVE_OFFSET-unit').val(unit);

	if(isNaN(count)){
		CFW.ui.addToastWarning("Count must be at least 1.");
		return;
	}
	// -----------------------------------------
	// Update Original Field
	cfw_timeframePicker_storeValue(fieldID, offset, earliestMillis, latestMillis);
	
	var dropdown = $(origin).closest(selector+'-timepickerDropdown');
	dropdown.collapse('hide');
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_timeframePicker_confirmRelativeOffset(origin){

	var wrapper = $(origin).closest('.cfw-timeframepicker-wrapper');
	var fieldID = wrapper.data('id');
		
	var count = $('#'+fieldID+'-RELATIVE_OFFSET-count').val();
	var unit = $('#'+fieldID+'-RELATIVE_OFFSET-unit').val();

	if(isNaN(count)){
		CFW.ui.addToastWarning("Count must be at least 1.");
		return;
	}
	
	cfw_timeframePicker_setOffset(origin, count+"-"+unit);
	
	//---------------------------
	// Hide Dropdown
	var dropdown = $(origin).closest('#'+fieldID+'-timepickerDropdown');
	dropdown.collapse('hide');
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_timeframePicker_setCustom(fieldID, earliestMillis, latestMillis){
	var selector = '#'+fieldID;
	$(selector+'-timeframeSelectorButton').text(CFWL('cfw_core_customtime', "Custom Time"));
		
	// -----------------------------------------
	// Update Original Field
	cfw_timeframePicker_storeValue(fieldID, null, parseFloat(earliestMillis), parseFloat(latestMillis) );
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_timeframePicker_confirmCustom(origin){

	var wrapper = $(origin).closest('.cfw-timeframepicker-wrapper');
	var fieldID = wrapper.data('id');
		
	var earliestMillis = $('#'+fieldID+'-CUSTOM_EARLIEST').val();
	var latestMillis = $('#'+fieldID+'-CUSTOM_LATEST').val()

	if(earliestMillis >= latestMillis){
		CFW.ui.addToastWarning("Earliest time has to be before latest time.");
		return;
	}
	cfw_timeframePicker_setCustom(fieldID, earliestMillis, latestMillis);
	
	//---------------------------
	// Hide Dropdown
	var dropdown = $(origin).closest('#'+fieldID+'-timepickerDropdown');
	dropdown.collapse('hide');
	
}


/*******************************************************************************
 * 
 * @param direction
 *            'earlier' or 'later'
 ******************************************************************************/
function cfw_timeframePicker_shift(origin, direction){
	
	var wrapper = $(origin).closest('.cfw-timeframepicker-wrapper');
	var fieldID = wrapper.data('id');
	var selector = '#'+fieldID;
	
	var storeField = $(selector);
	var dataString = storeField.val();
	
	var data = JSON.parse(dataString);
	//var label = wrapper.find("#time-preset-"+preset).text();
	
	wrapper.find(selector+'-timeframeSelectorButton').text(CFWL('cfw_core_customtime', "Custom Time"));
	
	var offsetMillis = data.latest - data.earliest;
	
	var offsetEarliest;
	var offsetLatest;
	if(direction == 'earlier'){
		offsetLatest = data.earliest;
		offsetEarliest = data.earliest - offsetMillis;
	}else{
		offsetEarliest = data.latest;
		offsetLatest = data.latest + offsetMillis;
	}
	
	// -----------------------------------------
	// Update Value
	cfw_timeframePicker_storeValue(fieldID, null, offsetEarliest, offsetLatest);
	
}

/**************************************************************************************
 * Will add a function that will be called before sending the autocomplete request
 * to the server.
 * You can add more parameters to the paramObject provided to the callback function
 * 
 * @param functionToEnhanceParams(inputField, paramObject, originalParamObject) 
 * 		callback function, will get an object that can be enhanced with more parameters
 * 			inputField: element on which autocomplete was triggered
 * 			paramObject: parameters for the autocomplete request, change this object.
 * 			originalParamObject: the original parameters without changes
 *************************************************************************************/
function cfw_autocomplete_addParamEnhancer(functionToEnhanceParams){
	
	CFW.global.autcompleteParamEnhancerFunctions.push(functionToEnhanceParams);
}

/**************************************************************************************
 * Initialize an autocomplete added to a CFWField with setAutocompleteHandler().
 * Can be used to make a static autocomplete using the second parameter.
 * 
 * @param formID the id of the form
 * @param fieldName the name of the field
 * @param minChars the minimum amount of chars for triggering the autocomplete
 * @param maxResults the max number of results listed for the autocomplete
 * @param array (optional) an array of strings used for the autocomplete
 * @param triggerWithCtrlSpace (optional)set to true to only trigger autocomplete with Ctrl+Space
 * @param target (optional)set the target of the autocomplete results, either JQuery or selector
 * @return nothing
 *************************************************************************************/
function cfw_autocompleteInitialize(formID, fieldName, minChars, maxResults, array, triggerWithCtrlSpace, target){
	
	//---------------------------------------
	// Initialize	
	CFW.global.autocompleteFocus = -1;
	
	var $input = $("#"+fieldName);	
	if($input.attr('data-role') == "tagsinput"){
		$input = $("#"+fieldName+"-tagsinput");
		//$input.parent().prepend('<i class="fas fa-search pr-2"><i>');
	}
	
	var settings = {
		$input: 				$input,
		formID:					formID,
		fieldName: 				fieldName,
		minChars:				minChars,
		maxResults:				maxResults,
		triggerWithCtrlSpace:	triggerWithCtrlSpace,
	}
	
	//prevent browser default auto fill
	$input.attr('autocomplete', 'off');
	
	settings.inputField = settings.$input.get(0);
	settings.autocompleteID = settings.inputField.id + "-autocomplete";
	
	settings.autocompleteTarget = $input.parent();
	if(target != null){
		settings.autocompleteTarget = $(target);
	}
	
	// For testing
	//var array = ["Afghanistan","Albania","Algeria","Andorra","Angola","Anguilla"];
	
	//--------------------------------------------------------------
	// STATIC ARRAY
	// ============
	// execute a function when someone writes in the text field:
	//--------------------------------------------------------------
	if(array != null){
		$input.on('input', function(e) {
			
			var filteredArray = [];
			var searchString = inputField.value;
		    		    
			//----------------------------
		    // Filter Array
		    for (var i = 0; i < array.length && filteredArray.length < maxResults; i++) {
		      
			   	var currentValue = array[i];
			    
			   	if (currentValue.toUpperCase().indexOf(searchString.toUpperCase()) >= 0) {
			   		filteredArray.push({value: currentValue, label: currentValue});
			   	}
			}
			//----------------------------
		    // Show AutoComplete	
			cfw_autocompleteShow(this, autocompleteTarget, {lists:[filteredArray], description: null});
		});
	}
	
	//--------------------------------------------------------------
	// DYNAMIC SERVER SIDE AUTOCOMPLETE
	//--------------------------------------------------------------
	if(array == null){
		
		$input.on('input', function(e) {
			//cfw_autocompleteEventHandler(e, fieldName, $input, minChars, maxResults, triggerWithCtrlSpace, autocompleteTarget);
			cfw_autocompleteEventHandler(e, settings);
		});
		
		//--------------------------------
		// Do Ctrl+Space separately, as keyup
		// would also trigger on Arrow Keys etc...
		$input.on('keydown', function(e) {
			
			if (triggerWithCtrlSpace && (e.ctrlKey && e.keyCode == 32)) {
				//cfw_autocompleteEventHandler(e, fieldName, $input, minChars, maxResults, triggerWithCtrlSpace, autocompleteTarget);
				cfw_autocompleteEventHandler(e, settings);
			}
			
			// close autocomplete on Esc
			if ( e.keyCode == 27) {
				cfw_autocompleteCloseAll();
			}
			
		});
	}
	
	//--------------------------------------------------------------
	// execute a function presses a key on the keyboard
	//--------------------------------------------------------------
	$input.on('keydown',  function(e) {
		

		var itemList = $("#"+settings.autocompleteID);
		var items;
		
		if (itemList != null && itemList.length > 0){ 
			items = itemList.find(".autocomplete-item")
		};

		//---------------------------
		// Down Arrow
		if (e.keyCode == 40) {
			  CFW.global.autocompleteFocus++;
			  markActiveItem(e, items);
			  return;
		}
		//---------------------------
		// Up Arrow
		if (e.keyCode == 38) { 
			  CFW.global.autocompleteFocus--;
			  markActiveItem(e, items);
			  return;
		}
		
		//---------------------------
		// Enter
		if (e.keyCode == 13) {
			/* If the ENTER key is pressed, prevent the form from being submitted. 
			 * Still do it for tagsinput.js fields*/
			if($input.attr('placeholder') != "Tags" && $input.prop("tagName") != "TEXTAREA"){
				e.preventDefault();
			}
			if (CFW.global.autocompleteFocus > -1) {
				/* and simulate a click on the "active" item. */
				if (itemList != null && items != null){
					items.eq(CFW.global.autocompleteFocus).click();
					e.preventDefault();
				}
			}else{
				// Close if nothing selected
				cfw_autocompleteCloseAll();
			}
		}
	});
	

	
	//--------------------------------------------------------------
	// a function to classify an item as "active"
	//--------------------------------------------------------------
	function markActiveItem(e, items) {
		if (!items) return false;
		/* start by removing the "active" class on all items: */
		removeActiveClass(items);
		if (CFW.global.autocompleteFocus >= items.length) CFW.global.autocompleteFocus = 0;
		if (CFW.global.autocompleteFocus < 0) CFW.global.autocompleteFocus = (items.length - 1);
		/* add class "autocomplete-active": */
		items.eq(CFW.global.autocompleteFocus).addClass("autocomplete-active");
		e.preventDefault();
	}
	
	//--------------------------------------------------------------
	// a function to remove the "active" class from all 
	// autocomplete items.
	//--------------------------------------------------------------
	function removeActiveClass(items) {
		items.removeClass("autocomplete-active");
	}
	

}

/**************************************************************************************
 * Listen on field input or Ctrl+Space and fetch data for autocomplete.

 *************************************************************************************/
function cfw_autocompleteEventHandler(e, settings) {

	
	// --------------------------------
	// Verify only do on  Ctrl+Space
	if (settings.triggerWithCtrlSpace && !(e.ctrlKey && e.keyCode == 32)) {
		return;
	}

	// Only do autocomplete if at least N characters are typed			
	if(settings.$input.val().length >= settings.minChars){
		// use a count and set timeout to wait for the user 
		// finishing his input before sending a request to the
		// server. Reduces overhead.
		var currentCount = ++CFW.global.autocompleteCounter;
		
		//----------------------------
		// Show Loader
		var loader = settings.autocompleteTarget.find('#autocomplete-loader');
		if(loader.length == 0){
			loader = $('<div id="autocomplete-loader"><small><i class="fa fa-cog fa-spin fa-1x"></i><span>&nbsp;Loading...</span></small></div>');
			settings.autocompleteTarget.append(loader);
		}
		
		//----------------------------
		// Load Autocomplete
		setTimeout(
			function(){
				
				//only execute if it is the last triggered autocomplete request
				if(currentCount != CFW.global.autocompleteCounter){
					return;
				}
				
				var params = CFW.format.formToParams(settings.$input.closest('form'));

				params[CFW.global.formID] = settings.formID;
				params.cfwAutocompleteFieldname = settings.fieldName;
				params.cfwAutocompleteSearchstring = settings.inputField.value;
				params.cfwAutocompleteCursorPosition = settings.inputField.selectionStart;
				
				//function to customize the autocomplete
				let origParamsClone = _.cloneDeep(params);
				for(let i in CFW.global.autcompleteParamEnhancerFunctions){
					let currentFunc = CFW.global.autcompleteParamEnhancerFunctions[i];
					currentFunc(settings.$input, params, _.cloneDeep(origParamsClone));
				}
				
				cfw_http_postJSON('/cfw/autocomplete', params, 
					function(data) {
						loader.remove();
						cfw_autocompleteShow(settings.inputField, settings.autocompleteTarget, settings.inputField.selectionStart, data.payload);
					})
			},
			1000);
	}
}	
	
/**************************************************************************************
 * close all autocomplete lists in the document.
 *************************************************************************************/
function cfw_autocompleteCloseAll() {	
	CFW.global.autocompleteFocus = -1;
	$('.autocomplete-wrapper').remove();
}

/**************************************************************************************
 * Show autocomplete
 * @param inputField domElement to show the autocomplete for.
 * @param autocompleteResults JSON object with the structure:
  {
	"lists": [
		[
			{"value": "Value0", "label": "Label0", "description": "some description or null" },
			{"value": "Value1", "label": "Label1", "description": "some description or null" },
			{...}
		],
		[
			{"value": "AnotherValue0", "label": "AnotherLabel0", "description": "some description or null" },
			{"value": "AnotherValue1", "label": "AnotherLabel1", "description": "some description or null" },
			{...}
		],
		[...]
	],
	"description": "<p>This is your HTML Description. Feel free to add some stuff like a list:<p <ol><li>Do this</li><li>Do that</li><li>Do even more...</li></ol>"
}
 *************************************************************************************/
function cfw_autocompleteShow(inputField, autocompleteTarget, cursorPosition, autocompleteResults){
	//----------------------------
    // Initialize and Cleanup
	var searchString = inputField.value;
	var autocompleteID = inputField.id + "-autocomplete";
		
    cfw_autocompleteCloseAll();
    if (!searchString) { return false;}
    if(autocompleteResults == null){ return false;};
    
    //----------------------------
    // Create Multiple Item Lists
    var autocompleteWrapper = $('<div id="'+autocompleteID+'" class="autocomplete-wrapper">');
    
  	//----------------------------
    // Add Description
	if(autocompleteResults.description != null){
		var description = $('<div class="autocomplete-description">');
		description.html(autocompleteResults.description);
		
		//Do not close autocomplete when clicking into description.
		description.on("click", function(e) {
			e.stopPropagation();
		});
		
		$(inputField).parent().append(description);
	}
	autocompleteWrapper.append(description);

  	//----------------------------
    // Add Lists
    var multipleLists = $('<div class="autocomplete-multilist d-flex flex-row">');
    
    // Plus 20% to give some range for flex display
    let maxWidthPercent = (100 / autocompleteResults.lists.length)+20;
    
    for(var key in autocompleteResults.lists){
    	var current = autocompleteResults.lists[key];
    	 multipleLists.append(cfw_autocompleteCreateItemList(inputField, cursorPosition, current, maxWidthPercent));
    }
    
    autocompleteWrapper.append(multipleLists);
	
	autocompleteTarget.append(autocompleteWrapper);
	
}

/**************************************************************************************
 * Creates a list for the autocomplete multilist.
 * @param inputField domElement to show the autocomplete for.
 * @param values a list JSON object containing elements like:
 *    {
 *		"title": "List title or null",
 *		"items": [
 *    		{"value": "Value0", "label": "Label0", "description": "some description or null" }
 *		]
 *	  }
 * 
 *************************************************************************************/
function cfw_autocompleteCreateItemList(targetInputField, cursorPosition, listObject, maxWidthPercent){
	//----------------------------
    // Initialize and Cleanup
	var searchString = targetInputField.value;
	var autocompleteID = targetInputField.id + "-autocomplete-list";
	var isTagsInput = false;
	var isTagsselector = false;

	if(targetInputField.id != null && targetInputField.id.endsWith('-tagsinput')){
		isTagsInput = true;
		isTagsselector = $(targetInputField).parent().siblings('input').hasClass('cfw-tags-selector');
	}
	
	var itemList =  $("<div>");
	
	itemList.attr("id", autocompleteID);
	itemList.css("max-width", maxWidthPercent+"%");
	itemList.addClass("autocomplete-list flex-fill");
			
	var itemClass = "autocomplete-item";
	if($(targetInputField).hasClass("form-control-sm")){
		itemClass += " autocomplete-item-sm";
	}
	
	
	//----------------------------
	// Add List Title
	if( !CFW.utils.isNullOrEmpty(listObject.title)){
		var listTitleElement = $('<div class="autocomplete-list-title"><b>'+listObject.title+'</b><div>');
		//Do not close autocomplete when clicking on title.
		listTitleElement.on("click", function(e) { e.stopPropagation(); });
		
		itemList.append(listTitleElement)
	}
	
	
	//----------------------------
	// Iterate values object
	listItems = listObject.items;
	for (var key in listItems) {
		
	   	var currentValue = listItems[key].value;
	   	var label = ""+listItems[key].label;
		var description = listItems[key].description;	
		// Methods: exchange | append | replacelast | replacebeforecursor
		var method = listItems[key].method;	
		
		//----------------------------
		// Create Item
		var item = $('<div class="'+itemClass+'">');
		
		// make the matching letters bold:
		var index = label.toUpperCase().indexOf(searchString.toUpperCase());
		if(index == 0){
			item.append(
				"<strong>" + label.substr(0, searchString.length) + "</strong>"
				+ label.substr(searchString.length)
				);
		}else if(index > 0){
			var part1 = label.substr(0, index);
			var part2 = label.substr(index, searchString.length);
			var part3 = label.substr(index+searchString.length);
			item.append(part1 + "<strong>" +part2+ "</strong>" +part3);
		}else {
			item.append(label);
		}
		
		if(description != null){
			item.append('<p>'+description+'</p>');
		}
		
		//-----------------------
		// Create Field
		var hiddenInput = $('<input type="hidden" data-label="'+label+'" data-tagsinput="'+isTagsInput+'" data-tagsselector="'+isTagsselector+'">');
		hiddenInput.val(currentValue);
		item.append(hiddenInput);
		
		item.on("click", function(e) { 
			e.stopPropagation();
			e.preventDefault()
			var element = $(this).find('input');
			var itemValue = element.val();
			var itemLabel = element.data('label');
			var itemIsTagsInput = element.data('tagsinput');
			var itemIsTagsSelector = element.data('tagsselector');
			
			if(!itemIsTagsInput){
				if(method == 'exchange'){
					targetInputField.value = itemValue;
					
				}else if(method == 'append'){
					targetInputField.value = targetInputField.value + itemValue;
					
				}else if(method.startsWith('replacelast:')){
					var stringToReplace = method.substring('replacelast:'.length);
					var tempValue = targetInputField.value;
					tempValue = tempValue.substring(0, tempValue.lastIndexOf(stringToReplace));
					targetInputField.value = tempValue + itemValue;
				}else if(method.startsWith('replacebeforecursor:')){
					var stringToReplace = method.substring('replacebeforecursor:'.length);
					var originalValue = targetInputField.value;
					var beforeCursor = originalValue.substring(0, cursorPosition);
					var afterCursor = originalValue.substring(cursorPosition);
					var beforeCursorWithoutReplaceString = beforeCursor.substring(0, beforeCursor.lastIndexOf(stringToReplace));
					targetInputField.value = beforeCursorWithoutReplaceString + itemValue + afterCursor;
					
					var newCursorPos =  (beforeCursorWithoutReplaceString + itemValue).length;
					targetInputField.focus();

					targetInputField.selectionStart = newCursorPos;
					targetInputField.setSelectionRange(newCursorPos, newCursorPos);
				}
				cfw_autocompleteCloseAll();
			}else{

				targetInputField.value = '';
				
				if(itemIsTagsSelector){
					// Do for Selector
					$(targetInputField).parent().siblings('input').tagsinput('add', { "value": itemValue , "label": itemLabel });
				}else{
					// Do For TagsInput
					$(targetInputField).parent().siblings('input').tagsinput('add', itemValue);
					
				}
				cfw_autocompleteCloseAll();
			}
		});
		itemList.append(item);
	    
	}
	
	return itemList;
}


/**************************************************************************************
 * Sort an object array by the values for the given key.
 * @param array the object array to be sorted
 * @param key the name of the field that should be used for sorting
 * @param reverse the order 
 * @return sorted array
 *************************************************************************************/
function cfw_sortArrayByValueOfObject(array, key, reverse){
	array.sort(function(a, b) {
		
			var valueA = a[key];
			var valueB = b[key];
			
			if(valueA == undefined) valueA = 0;
			if(valueB == undefined) valueA = 0;
			
			if(isNaN(valueA)) valueA = Number.MAX_VALUE;
			if(isNaN(valueB)) valueB = Number.MAX_VALUE;
			
			
		if(reverse){
			return valueB - valueA;
		}else{
			return valueA - valueB;
		}
	});
	
	return array;
}

/**************************************************************************************
 * Create a timestamp string
 * @param epoch unix epoch milliseconds since 01.01.1970
 * @return timestamp as string
 *************************************************************************************/
function cfw_format_epochToTimestamp(epoch){

  if(epoch != null){
	  return new  moment(epoch).format('YYYY-MM-DD HH:mm:ss');
  }
  
  return "";
}

/**************************************************************************************
 * Create a clock string in the format HH:mm:ss
 * @param millis milliseconds
 * @return clock string
 *************************************************************************************/
function cfw_format_millisToClock(millis){

  if(millis != null){
	  return moment.utc(millis).format("HH:mm:ss");
  }
  
  return "";
}


/**************************************************************************************
 * Create a duration string in the format # d ##h ##m ##.#s 
 * @param timeValue amount of time
 * @param timeUnit unit of time, one of: ns | us | ms | s | m | h (Default: ms)
 * @return clock string
 *************************************************************************************/
function cfw_format_timeToDuration(timeValue, timeUnit){
	var millisValue = timeValue;
	
	//--------------------------------
	// If null, assume is milliseconds
	if(timeUnit != null ){
		switch(timeUnit){
			case 'ns':	millisValue = timeValue / 1000000; break
			case 'us':	millisValue = timeValue / 1000; break
			//case 'ms':	millisValue = timeValue; break
			case 's':	millisValue = timeValue * 1000; break
			case 'm':	millisValue = timeValue * 1000 * 60; break
			case 'h':	millisValue = timeValue * 1000 * 60 * 60; break
			default: 	millisValue = timeValue; 
		}
	}
	
	
	return cfw_format_millisToDuration(millisValue);
}
/**************************************************************************************
 * Create a duration string in the format # d ##h ##m ##.#s 
 * @param millis milliseconds
 * @return clock string
 *************************************************************************************/
function cfw_format_millisToDuration(millis){
	
	if(millis != null){
		var isNegative = (millis < 0);
		millis = Math.abs(millis);
		
		var milliseconds = parseInt((millis % 1000) / 100);
		var seconds = Math.floor((millis / 1000) % 60);
		var minutes = Math.floor((millis / (1000 * 60)) % 60);
		var hours = Math.floor((millis / (1000 * 60 * 60)) % 24);
		var days = Math.floor( millis / (1000 * 60 * 60 * 24) );
		

		
		hours = (hours < 10) ? "0" + hours : hours;
		minutes = (minutes < 10) ? "0" + minutes : minutes;
		seconds = (seconds < 10) ? "0" + seconds : seconds;
		
		var clockString = seconds + "." + milliseconds + "s";
		
		if(minutes != "00"){
			clockString = minutes + "m "+clockString;
		}
		
		if(hours != "00"){
			
			if(minutes == "00"){
				clockString = minutes + "m "+clockString;
			}
			
			clockString = hours + "h " +clockString;
			
		}
		
		if(days != 0){
			clockString = days + "d " +clockString;
		}
		
		if(isNegative){
			clockString = "-" +clockString;
		}
		return clockString;
	}
	    
  return "";
}


/**************************************************************************************
 * Create a date string
 * @param epoch unix epoch milliseconds since 01.01.1970
 * @return date as string
 *************************************************************************************/
function cfw_format_epochToDate(epoch){
	if(CFW.utils.isNullOrEmpty(epoch)){
		return "";
	}
	
	var a = new Date(epoch);
	var year 		= a.getFullYear();
	var month 	= a.getMonth()+1 < 10 	? "0"+(a.getMonth()+1) : a.getMonth()+1;
	var day 		= a.getDate() < 10 		? "0"+a.getDate() : a.getDate();
	
	var time = year + '-' + month + '-' + day ;
	return time;
}

/**************************************************************************************
 * Create a timestamp string
 * @param epoch unix epoch milliseconds since 01.01.1970
 * @return timestamp as string
 *************************************************************************************/
function cfw_format_cfwSchedule(scheduleData){

	if(scheduleData == null || scheduleData.timeframe == null){
		return "";
	}
	
	var result = "<div>";
	
	if( !CFW.utils.isNullOrEmpty(scheduleData.timeframe.startdatetime) ) { result += '<span><b>Start:&nbsp</b>'+CFW.format.epochToTimestamp(scheduleData.timeframe.startdatetime) +'</span><br/>'; }
	
	if (scheduleData.timeframe.endtype == "RUN_FOREVER"){ result += '<span><b>End:&nbsp</b> Run Forever</span><br/>'; }
	else if (scheduleData.timeframe.endtype == "EXECUTION_COUNT"){ result += '<span><b>End:&nbsp</b>'+scheduleData.timeframe.executioncount+' execution(s)</span><br/>'; }
	else if( !CFW.utils.isNullOrEmpty(scheduleData.timeframe.enddatetime) ) { result += '<span><b>End:&nbsp</b>'+CFW.format.epochToTimestamp(scheduleData.timeframe.enddatetime) +'</span><br/>'; }

	//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
	if (scheduleData.interval.intervaltype == "EVERY_X_MINUTES"){ result += '<span><b>Interval:&nbsp</b> Every '+scheduleData.interval.everyxminutes+' minute(s)</span><br/>'; }
	else if (scheduleData.interval.intervaltype == "EVERY_X_DAYS"){ result += '<span><b>Interval:&nbsp</b> Every '+scheduleData.interval.everyxdays+' day(s)</span><br/>'; }
	else if (scheduleData.interval.intervaltype == "CRON_EXPRESSION"){ result += '<span><b>Interval:&nbsp</b> Cron Expression "'+scheduleData.interval.cronexpression+'"</span><br/>'; }
	else if (scheduleData.interval.intervaltype == "EVERY_WEEK"){ 
		result += '<span><b>Interval:&nbsp</b> Every week on '; 
		let days = "";
		if(scheduleData.interval.everyweek.MON){ days += "MON/"; }
		if(scheduleData.interval.everyweek.TUE){ days += "TUE/"; }
		if(scheduleData.interval.everyweek.WED){ days += "WED/"; }
		if(scheduleData.interval.everyweek.THU){ days += "THU/"; }
		if(scheduleData.interval.everyweek.FRI){ days += "FRI/"; }
		if(scheduleData.interval.everyweek.SAT){ days += "SAT/"; }
		if(scheduleData.interval.everyweek.SUN){ days += "SON/"; }
		
		days = days.substring(0, days.length-1);
		result += days;
	}
	
  return result;
}

/**************************************************************************************
 * Splits up CFWSchedule data and adds the following fields to each record:
 * SCHEDULE_START, SCHEDULE_END, SCHEDULE_INTERVAL
 * The above fields contain a formatted string representing the value defined by the schedule.
 * This method is useful to display schedule definitions better in tables.
 * 
 * @param dataArray containing the records with a schedule field
 * @param fieldname the name of the schedule field
 *************************************************************************************/
function cfw_format_splitCFWSchedule(dataArray, fieldname){
	
	for(let index in dataArray){

		let currentRecord = dataArray[index];
		let scheduleData = currentRecord[fieldname];
		
		if( scheduleData != null && scheduleData.timeframe != null){ 

			if( !CFW.utils.isNullOrEmpty(scheduleData.timeframe.startdatetime) ) { currentRecord.SCHEDULE_START = CFW.format.epochToTimestamp(scheduleData.timeframe.startdatetime); }
			
			if (scheduleData.timeframe.endtype == "RUN_FOREVER"){ currentRecord.SCHEDULE_END = "Run Forever"; }
			else if (scheduleData.timeframe.endtype == "EXECUTION_COUNT"){ currentRecord.SCHEDULE_END = "After "+scheduleData.timeframe.executioncount+" execution(s)"; }
			else if( !CFW.utils.isNullOrEmpty(scheduleData.timeframe.enddatetime) ) { currentRecord.SCHEDULE_END = CFW.format.epochToTimestamp(scheduleData.timeframe.enddatetime); }

			//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
			if (scheduleData.interval.intervaltype == "EVERY_X_MINUTES"){ currentRecord.SCHEDULE_INTERVAL = 'Every '+scheduleData.interval.everyxminutes+' minute(s)'; }
			else if (scheduleData.interval.intervaltype == "EVERY_X_DAYS"){ currentRecord.SCHEDULE_INTERVAL = 'Every '+scheduleData.interval.everyxdays+' day(s)'; }
			else if (scheduleData.interval.intervaltype == "CRON_EXPRESSION"){ currentRecord.SCHEDULE_INTERVAL = 'CRON: "'+scheduleData.interval.cronexpression+'"'; }
			else if (scheduleData.interval.intervaltype == "EVERY_WEEK"){ 
				
				let days = "";
				if(scheduleData.interval.everyweek.MON){ days += "MON/"; }
				if(scheduleData.interval.everyweek.TUE){ days += "TUE/"; }
				if(scheduleData.interval.everyweek.WED){ days += "WED/"; }
				if(scheduleData.interval.everyweek.THU){ days += "THU/"; }
				if(scheduleData.interval.everyweek.FRI){ days += "FRI/"; }
				if(scheduleData.interval.everyweek.SAT){ days += "SAT/"; }
				if(scheduleData.interval.everyweek.SUN){ days += "SON/"; }
				
				days = days.substring(0, days.length-1);
				currentRecord.SCHEDULE_INTERVAL = 'Every week on '+days;
			}
			
		}else{
			currentRecord.SCHEDULE_START = "&nbsp;";
			currentRecord.SCHEDULE_END = "&nbsp;";
			currentRecord.SCHEDULE_INTERVAL = "&nbsp;";
		}
		
	}
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_fieldNameToLabel(fieldname){
	
	if(fieldname == null){
		return "";
	}
 	var regex = /[-_]/;
	var splitted = fieldname.split(regex);
	
	var result = '';
	for(var i = 0; i < splitted.length; i++) {
		result += (CFW.format.capitalize(splitted[i]));
		
		//only do if not last
		if(i+1 < splitted.length) {
			result += " ";
		}
	}
	
	return result;
}
/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_csvToObjectArray(csvString, delimiter){
	
 	var lines = csvString.trim().split(/\r\n|\r|\n/);
 	
 	//------------------------------
 	// Check has at least one record
 	if(lines.length < 2){
 		return [];
 	}
 	 	
 	//------------------------------
 	// Get Headers
 	var headers = lines[0].trim().split(delimiter);
 	
 	//------------------------------
 	// Create Objects
 	var resultArray = [];
 	
 	for(var i = 1; i < lines.length; i++){
 		var line = lines[i];
 		var values = line.split(delimiter);
 		var object = {};
 		
 	 	for(var j = 0; j < headers.length && j < values.length; j++){
 	 		var header = headers[j];
 	 		object[header] = values[j].trim();
 	 	}
 	 	resultArray.push(object);
 	 	
 	}
	
	return resultArray;
}


/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_capitalize(string) {
	 if(string == null) return '';
	 return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
}

/**************************************************************************************
 * Adds separator to numbers
 *************************************************************************************/
function cfw_format_numberSeparators(value, separator, eachDigit) {
	
	if(value == null) return '';
	if(isNaN(value)) return value;
	
	var separator = (separator == null) ?  "'" : separator;
	var eachDigit = (eachDigit == null) ?  3 : eachDigit;
	
	var stringValue = ""+value;
	var startingPos = stringValue.lastIndexOf('.')-1;
	var resultString = stringValue.substring(startingPos+1);
	
	//if no decimal point found, do this
	if(startingPos == -2){	
		startingPos = stringValue.length-1;
		var resultString = "";
	}
	
	var position = 0;
	
	for(var i = startingPos; i >= 0; i--){
		position++;
		
		resultString = stringValue.charAt(i) + resultString;
		if(position % 3 == 0 && i > 0){
			resultString = separator + resultString;
		}
	}
	
	return resultString;
}

/**************************************************************************************
 * Formats numbers as kilos, megas, gigas and terras.
 * @param value the value to format
 * @param decimals number of decimal places
 * @param addBlank if true, adds a blank between number and the K/M/G/T
 * @param isBytes if true, adds "B" to the resulting format
 **************************************************************************************/
function cfw_format_numbersInThousands(value, decimals, addBlank, isBytes) {
	
	blankString = (addBlank) ? "&nbsp;" : "";
	
	bytesString = (isBytes) ? "B" : "";
	
	if(isNaN(value)){
		return value;
	}
	
	if (value > 1000000000000) {
		return (value / 1000000000000).toFixed(decimals)+ blankString + "T" + bytesString;
	} else if (value > 1000000000) {
		return (value / 1000000000).toFixed(decimals)+ blankString + "G" + bytesString;
	} else if (value > 1000000) {
		return (value / 1000000).toFixed(decimals)+ blankString + "M"+bytesString;
	} else if (value > 1000) {
		return (value / 1000).toFixed(decimals)+ blankString + "K"+bytesString;
	} else {
		return value.toFixed(decimals)+blankString+((addBlank)?"&nbsp;":"")+bytesString;
	}
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_badgesFromArray(stringArray, addLineBreaks) {
	
	if(stringArray == null) return '';

	let badgesHTML = '<div>';
	
		for(id in stringArray){
			badgesHTML += '<span class="badge badge-primary m-1">'+stringArray[id]+'</span>';
			if(addLineBreaks) { badgesHTML += '</br>'; }
		}
		
	badgesHTML += '</div>';
	
	return badgesHTML;
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_badgesFromObjectValues(object, addLineBreaks) {
	
	if(object == null) return '';

	let badgesHTML = '<div>';
	
		for(fieldname in object){
			let value = object[fieldname];
			badgesHTML += '<span class="badge badge-primary m-1">'+value+'</span>';
			if(addLineBreaks) { badgesHTML += '</br>'; }
		}
		
	badgesHTML += '</div>';
	
	return badgesHTML;
}




/**************************************************************************************
 * Converts the input fields of a form to an array of parameter objects.
 * Additionally to $().serializeArray(), it changes string representations of 
 * numbers and booleans to the proper types
 * 
 * { 
 * 		fieldname: "fieldvalue",
 * 		fieldname: "fieldvalue",
 * 		fieldname: "fieldvalue",
 * 		...
 * }
 *
 * @param formOrID either ID of form or a JQuery object
 * @param numbersAsStrings define if number should be converted to strings. Needed to
 *        keep leading zeros.
 *
 *************************************************************************************/
function cfw_format_formToParams(formOrID, numbersAsStrings){
	
	var paramsObject = cfw_format_formToObject(formOrID, numbersAsStrings);

	for(var name in paramsObject){
		var value = paramsObject[name];
		
		if(value != null && typeof value === 'object'){
			paramsObject[name] = JSON.stringify(value);
		}
	}
	
	return paramsObject;
}
/**************************************************************************************
 * Converts the input fields of a form to an array of parameter objects.
 * Additionally to $().serializeArray(), it changes string representations of 
 * numbers and booleans to the proper types
 * 
 * { 
 * 		fieldname: "fieldvalue",
 * 		fieldname: "fieldvalue",
 * 		fieldname: "fieldvalue",
 * 		...
 * }
 *
 * @param formOrID either ID of form or a JQuery object
 * @param numbersAsStrings define if number should be converted to strings. Needed to
 *        keep leading zeros.
 *
 *************************************************************************************/
function cfw_format_formToObject(formOrID, numbersAsStrings){
	
	var paramsArray = cfw_format_formToArray(formOrID, numbersAsStrings);
	
	var object = {};
	for(var i in paramsArray){
		var name = paramsArray[i].name;
		var value = paramsArray[i].value;
		object[name] = value;
	}
	
	return object;
}
/**************************************************************************************
 * Converts the input fields of a form to an array of parameter objects.
 * Additionally to $().serializeArray(), it changes string representations of 
 * numbers and booleans to the proper types
 * [
 * {name: "fieldname", value: "fieldvalue"},
 * {name: "fieldname", value: "fieldvalue"},
 * ...
 * ]
 *
 * @param formOrID either ID of form or a JQuery object
 * @param numbersAsStrings define if number should be converted to strings. Needed to
 *        keep leading zeros.
 *
 *************************************************************************************/
function cfw_format_formToArray(formOrID, numbersAsStrings){
	
	var paramsArray = $(formOrID).serializeArray();
	
	//---------------------------
	// Convert String Values
	for(let i in paramsArray){
		let name = paramsArray[i].name;
		let current = paramsArray[i].value;
		
		paramsArray[i].value = cfw_utils_convertStringsToType(name, current, numbersAsStrings);

	}

	//---------------------------
	// Handle Tags Selector
	var tagsselector = $(formOrID).find('.cfw-tags-selector');
	if(tagsselector.length > 0){
		tagsselector.each(function(){
			let current = $(this);
			let name = current.attr('name');
			
			//---------------------------
			// Find in parameters
			for(let i in paramsArray){
				if(paramsArray[i].name == name){
					
					//---------------------------
					// Create object
					let items = current.tagsinput('items');
					var object = {};
					for (var j in items){
						let value = items[j].value;
						let label = items[j].label;
						object[value] = label;
					}
					//---------------------------
					// Change params
					paramsArray[i].value = object;
					break;
				}
			}
		});
	}

	return paramsArray;
}

/**************************************************************************************
 * Creates an HTML ul-list out of an object
 * @param convert a json object to html
 * @return html string
 *************************************************************************************/
function cfw_format_objectToHTMLList(object, style, paddingLeft, doLabelize){
	
	if(style == null){	style = "bullets";	}
	if(paddingLeft == null){	paddingLeft = "20px"; }
	
	
	var htmlString = '<ul style="padding-left: '+paddingLeft+';">';
	if(style == "numbers"){
		htmlString = '<ol style="padding-left: '+paddingLeft+';">';
	}else if(style == "none"){
		htmlString = '<ul style="list-style-type: none; padding-left: '+paddingLeft+';"">';
	}
	
	if(Array.isArray(object)){
		for(var i = 0; i < object.length; i++ ){
			var currentItem = object[i];
			if(typeof currentItem == "object"){
				htmlString += '<li><b>Object:&nbsp;</b>'
					+ cfw_format_objectToHTMLList(currentItem, style, paddingLeft, doLabelize)
				+'</li>';
			}else{
				htmlString += '<li>'+currentItem+'</li>';
			}
			
		}
	}else if(typeof object == "object"){
		
		for(var key in object){
			var currentValue = object[key];
			var key = (!doLabelize) ? key : CFW.format.fieldNameToLabel(key);
			if(typeof currentValue == "object"){
				htmlString += '<li><strong>'+key+':&nbsp;</strong>'
					+ cfw_format_objectToHTMLList(currentValue, style, paddingLeft, doLabelize)
				+'</li>';
			}else{
				htmlString += '<li><strong>'+key+':&nbsp;</strong>'
					+ currentValue
				+'</li>';
			}
			
		}
	}
	htmlString += '</ul>';
	
	return htmlString;
		
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_customizer_booleanFormat(record, value) { 
	if(value){
		return '<span class="cfw-badge bg-success">true</span>';
	}else{
		return '<span class="cfw-badge bg-danger">false</span>';
	} 
};

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_customizer_badgesFromArray(record, value) { 
	return cfw_format_badgesFromArray(value);
};

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_customizer_badgesFromObjectValues(record, value) { 
	return cfw_format_badgesFromObjectValues(value);
};

/**************************************************************************************
 * Create a table of contents for the h-elements on the page.
 * If you do not want a section and it's subsection to appear in the table of contents,
 * add the class 'toc-hidden' to your header element.
 *
 * @param contentAreaSelector the jQuery selector for the element containing all 
 * the headers to show in the table of contents
 * @param targetSelector the jQuery selector for the resulting element
 * @param headerTag the tag used for the Table Header e.g. h1, h2, h3 (default h1)
 * @return nothing
 *************************************************************************************/
function cfw_ui_createTOC(contentAreaSelector, resultSelector, headerTag){
	
	var target = $(resultSelector);
	var headers = $(contentAreaSelector).find("h1:visible, h2:visible, h3:visible, h4:visible, h5:visible, h6:visible");
	
	var h = "h1";
	if(headerTag != null){
		h = headerTag;
	}
	//------------------------------
	//Loop all visible headers
	var currentLevel = 1;
	var resultHTML = '<'+h+' class="cfw-toc-header">Table of Contents</'+h+'><ul>';
	for(var i = 0; i < headers.length ; i++){
		var head = headers[i];
		var headLevel = head.tagName[1];
		
		//------------------------------
		//increase list depth
		while(currentLevel < headLevel){
			resultHTML += "<ul>";
			currentLevel++;
		}
		//------------------------------
		//decrease list depth
		while(currentLevel > headLevel){
			resultHTML += "</ul>";
			currentLevel--;
		}
		
		//------------------------------
		//Add List Item
		var classes = "";
		if($(head).hasClass('toc-hidden')){ classes="d-none"; }
		
		resultHTML += '<li class="'+classes+'"><a href="#toc_anchor_'+i+'">'+head.innerHTML+'</li>';
		$(head).before('<a name="toc_anchor_'+i+'"></a>');
	}
	
	//------------------------------
	// Close remaining levels
	while(currentLevel > 1){
		resultHTML += "</ul>";
		currentLevel--;
	}
	
	target.html(resultHTML);
	
}


/*******************************************************************************
 * Set if the Loading animation is visible or not.
 * 
 * The following example shows how to call this method to create a proper rendering
 * of the loader:
 * 	
 *  CFW.ui.toggleLoader(true);
 *	window.setTimeout( 
 *	  function(){
 *	    // Do your stuff
 *	    CFW.ui.toggleLoader(false);
 *	  }, 100);
 *
 * @param isVisible true or false
 * @param targetID the id of the element where the overlay should be loaded.
 * 				   If undefined, the loader is added to the full body.
 ******************************************************************************/
function cfw_ui_toggleLoader(isVisible, targetID){
	
	var loaderID;
	var target;
	var cssClass;
	
	if(targetID != null){
		loaderID = "cfw-loader-"+targetID;
		target = $("#"+targetID);
		cssClass = "cfw-loader-custom";
	}else{
		loaderID = "cfw-loader-body";
		target = $("body");
		cssClass = "";
	}
	
	loader = $("#"+loaderID);

	if(loader.length == 0){
		loader = $('<div id="'+loaderID+'" class="'+cssClass+'">'
				+'<div>'
					+'<i class="fa fa-cog fa-spin fa-1x fa-fw margin-bottom"></i>'
					+'<span class="m-0">Loading...</span>'
				+'</div>'
			+'</div>');	
		
		var bgColor = $('body').css("background-color");
		loader.css("background-color", bgColor);
		
//		loader.css("position","absolute");
//		loader.css("top","50%");
//		loader.css("left","50%");
//		loader.css("transform","translateX(-50%) translateY(-50%);");
//		loader.css("visibility","hidden");
		
		target.prepend(loader);
	}
	
	if(isVisible){
		var parent = loader.parent();
		
		if(targetID != null){
			loader.width( parent.width() );
			loader.height( parent.height() );
			loader.css("display", "flex");
		}
	}else{
		loader.css("display", "none");
	}
	
}


/*******************************************************************************
 * Returns the html for a Loader with the class cfw-inline-loader.
 ******************************************************************************/
function cfw_ui_createLoaderHTML(){
	
	return '<div class="cfw-inline-loader">'
			+'<div class="d-flex flex-column flex-align-center">'
				+'<i class="fa fa-cog fa-spin fa-2x fa-fw margin-bottom"></i>'
				+'<p class="m-0">Loading</p>'
			+'</div>'
		+'</div>';
	
}

/*******************************************************************************
 * Function to show fixed dropdown menus.
 * Example HTML:
	<div class="dropdown dropright">
		<button id="chartSettingsButton" class="btn btn-sm btn-primary dropdown-toggle" type="button" onclick="CFW.ui.toggleDropdownMenuFixed(event, this)" >
			Chart Settings
		</button>
		<div id="${fieldID}-DROPDOWNMENU" class="dropdown-menu-fixed col-sm-12" onclick="event.stopPropagation();">
			... your dropdown content ...
		</div>
	</div>
 ******************************************************************************/
function cfw_ui_toggleDropdownMenuFixed(event, buttonElement){
	
	event.stopPropagation();
	
	var parent = $(buttonElement).closest('.dropdown');
	var isShown = parent.hasClass('show');
	// close any visible before opening new one
	cfw_ui_closeAllDropdownMenuFixed();
	
	if(!isShown){
		parent.addClass('show');
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_ui_closeAllDropdownMenuFixed(){
	
	$('.dropdown-menu-fixed')
		.closest('.dropdown')
		.removeClass('show');
}


/**************************************************************************************
 * Add an alert message to the message section.
 * Ignores duplicated messages.
 * @param type the type of the alert: INFO, SUCCESS, WARNING, ERROR
 * @param message
 *************************************************************************************/
function cfw_ui_addAlert(type, message){
	
	var clazz = "";
	
	switch(type.toLowerCase()){
		
		case "success": clazz = "alert-success"; break;
		case "info": 	clazz = "alert-info"; break;
		case "warning": clazz = "alert-warning"; break;
		case "error": 	clazz = "alert-danger"; break;
		case "severe": 	clazz = "alert-danger"; break;
		case "danger": 	clazz = "alert-danger"; break;
		default:	 	clazz = "alert-info"; break;
		
	}
	
	var htmlString = '<div class="alert alert-dismissible '+clazz+'" role=alert>'
		+ '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
		+ message
		+"</div>\n";
	
	//----------------------------------------------
	// Add if not already exists
	var messages = $("#cfw-messages");
	
	if (messages.html().indexOf(message) <= 0) {
		messages.append(htmlString);
	}
	
}

/**************************************************************************************
 * Create a new Toast.
 * @param toastTitle the title for the toast
 * @param toastBody the body of the toast (can be null)
 * @param style bootstrap style like 'info', 'success', 'warning', 'danger'
 * @param (optional)delay in milliseconds for autohide
 * @return nothing
 *************************************************************************************/
function cfw_ui_addToast(toastTitle, toastBody, style, delay){
	
	var body = $("body");
	var toastsID = 'cfw-toasts';
	
	//--------------------------------------------
	// Create Toast Wrapper if not exists
	//--------------------------------------------
	var toastDiv = $("#"+toastsID);
	if(toastDiv.length == 0){
	
		var toastWrapper = $(
				'<div id="cfw-toasts-wrapper" aria-live="polite" aria-atomic="true">'
			  + '  <div id="cfw-toasts"></div>'
			  + '</div>');
				
		body.prepend(toastWrapper);
		toastDiv = $("#"+toastsID);
	}

	//--------------------------------------------
	// Prepare arguments
	//--------------------------------------------
	
	if(style == null){
		style = "primary";
	}
	
	var clazz;
	switch(style.toLowerCase()){
	
		case "success": clazz = "success"; break;
		case "info": 	clazz = "info"; break;
		case "warning": clazz = "warning"; break;
		case "error": 	clazz = "danger"; break;
		case "severe": 	clazz = "danger"; break;
		case "danger": 	clazz = "danger"; break;
		default:	 	clazz = style; break;
		
	}
	
	var autohide = 'data-autohide="false"';
	if(delay != null){
		autohide = 'data-autohide="true" data-delay="'+delay+'"';
	}
	//--------------------------------------------
	// Create Toast 
	//--------------------------------------------
		
	var toastHTML = 
		      '<div class="toast bg-'+clazz+' text-light" role="alert" aria-live="assertive" aria-atomic="true" data-animation="true" '+autohide+'>'
			+ '  <div class="toast-header bg-'+clazz+' text-light">'
			  + '	<strong class="mr-auto word-break-word"></strong>'
			  + '	<button type="button" class="ml-2 mb-auto close" data-dismiss="toast" aria-label="Close">'
			  + '	  <span aria-hidden="true">&times;</span>'
			  + '	</button>'
			+ '  </div>' 
			+ '  <div class="toast-body"></div>'
			+ '</div>';
	
	var toast = $(toastHTML);
	
	//-------------------------------------
	// Handle Title
	if(toastTitle != null){
		
		if(toastTitle.length > 500){
			toastTitle = toastTitle.substring(0, 500) + "... (truncated)";
		}
		toast.find('.toast-header strong').append(toastTitle);	
	}else{
		toast.find('.toast-header').remove();
	}
	//-------------------------------------
	// Handle Body
	if(toastBody != null){
		
		if(toastBody.length > 500){
			toastBody = toastBody.substring(0, 500) + "... (truncated)";
		}
		toast.find('.toast-body').text(toastBody);	
	}else{
		toast.find('.toast-body').remove();
	}
	
	toastDiv.append(toast);
	toast.toast('show');
}

/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @param size 'small', 'regular', 'large'
 * @param keepOnOutsideClick (obsolete, now default true)
 * @return nothing
 *************************************************************************************/
function cfw_ui_showModal(modalTitle
		, modalBody
		, jsCodeOrFunction
		, size
		, keepOnOutsideClick){
	
	var modalID = 'cfw-default-modal';
	var modalHeaderClass = '';
	var modalHeaderType = 'h3';
	var modalDialogClass = 'modal-xl';
	var backdrop = true;
	var modalSettings = {};
	
	var style = ''
	if(size == 'small'){
		modalID = 'cfw-small-modal';
		style = 'style="z-index: 1155; display: block; top: 25px;"';
		modalHeaderClass = 'p-2';
		modalDialogClass = 'modal-lg';
		modalHeaderType = 'h4';
		backdrop = false;
	}else if(size == 'large'){
		modalID = 'cfw-large-modal';
		style = 'style="z-index: 1045; display: block; top: -10px;"';
		modalDialogClass = 'modal-xxl-cfw';
	}
	
	//if(keepOnOutsideClick){
	modalSettings.backdrop = 'static';
	modalSettings.keyboard = false;

	
	CFW.global.lastOpenedModal = modalID;
	var modal = $("#"+modalID);
	if(modal.length == 0){
	
		modal = $(
				'<div id="'+modalID+'" class="modal fade" '+style+' tabindex="-1" role="dialog">'
				+ '  <div class="modal-dialog '+modalDialogClass+'" role="document">'
				+ '    <div class="modal-content">'
				+ '      <div class="modal-header '+modalHeaderClass+'">'
				+ '        <'+modalHeaderType+' class="modal-title">Title</'+modalHeaderType+'>'
				+ '        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times</span></button>'
				+ '      </div>'
				+ '      <div class="modal-body" >'
				+ '      </div>'
				+ '      <div class="modal-footer">'
				+ '         <button id="'+modalID+'-closebutton" type="button" class="btn btn-sm btn-primary" data-dismiss="modal">Close</button>'
				+ '      </div>'
				+ '    </div>'
				+ '  </div>'
				+ '</div>');

		modal
		.modal(modalSettings)
		.draggable({
			backdrop: backdrop,	   
		    handle: ".modal-header",
		}); 
		// remove position relative to show content in background
		modal.css('position', '');
		
		// Prevent other modals to close the modal
//		modal.on('hidden.bs.modal', function (e) {
//			  if(e.target.id != modalID) { // ignore events which are raised from child modals
//			    return false;
//			  }
//			  // your code
//			});
		
//		modal.modal({
//		    backdrop: 'static',
//		    keyboard: false
//		});
		$('body').prepend(modal);
		
		//-------------------------------------
		// Reset Scroll Position
		modal.on('hide.bs.modal', function () {
			modal.find('.modal-body').scrollTop(0);
		});	

	}

	//---------------------------------
	// Add Callback
	if(jsCodeOrFunction != null){

		modal.on('hidden.bs.modal', function () {
			cfw_utils_executeCodeOrFunction(jsCodeOrFunction);
			$("#"+modalID).off('hidden.bs.modal');
		});	
	}
	
	//---------------------------------
	// ShowModal
	modal.find(".modal-title").html("").append(modalTitle);
	modal.find('.modal-body').html("").append(modalBody);
	
	modal.modal('show');

}

/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCodeOrFunction to execute on modal close
 * @param keepOnOutsideClick set true to keep the Modal open when clicking on the backdrop
 * @return nothing
 *************************************************************************************/
function cfw_ui_showModalMedium(modalTitle, modalBody, jsCodeOrFunction, keepOnOutsideClick){
	cfw_ui_showModal(modalTitle, modalBody, jsCodeOrFunction, 'regular', keepOnOutsideClick);
}
	
/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @param keepOnOutsideClick set true to keep the Modal open when clicking on the backdrop
 * @return nothing
 *************************************************************************************/
function cfw_ui_showModalSmall(modalTitle, modalBody, jsCodeOrFunction, keepOnOutsideClick){
	cfw_ui_showModal(modalTitle, modalBody, jsCodeOrFunction, 'small', keepOnOutsideClick);
}
	
/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @param keepOnOutsideClick set true to keep the Modal open when clicking on the backdrop
 * @return nothing
 *************************************************************************************/
function cfw_ui_showModalLarge(modalTitle, modalBody, jsCodeOrFunction, keepOnOutsideClick){
	cfw_ui_showModal(modalTitle, modalBody, jsCodeOrFunction, 'large', keepOnOutsideClick);
}	

/**************************************************************************************
 * Create a model with content.
 * @param modalID the ID of the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @return nothing
 *************************************************************************************/
function cfw_ui_reopenModal(modalID){
	
	var modal = $("#"+modalID);
	
	if(modal.length != 0){
		modal.modal('show');
	}
		
}

/**************************************************************************************
 * Show a modal with support Info
 *************************************************************************************/
function cfw_ui_showSupportInfoModal(){
		
	var modalContent = $('<div>');
	
	//=========================================
	// Javascript Data
	//=========================================
	modalContent.append('<h2>Javascript Data</h2>');
	
	var rendererParams = {
			data: [JSDATA],
			customizers: {
				starttime: function(record, value) { return (value != null ) ? CFW.format.epochToTimestamp(value) : "" }
			},
			rendererSettings: {
				table: {
					filterable: false,
					narrow: true,
					verticalize: true
				},
			},
		};
	
	var result = CFW.render.getRenderer('table').render(rendererParams);
	modalContent.append(result);
	
	//=========================================
	// Toast Messages
	//=========================================
	var toastDiv = $("#cfw-toasts");
	if(toastDiv.length != 0){
		modalContent.append('<h2>Toast Messages</h2>');
		var clone = toastDiv.clone();
		clone.find('.close').remove();
		clone.find('.toast')
			.attr('data-autohide', 'false')
			.removeClass('hide')
			.addClass('show');
		modalContent.append(clone.find('.toast').clone());
	}
	
	//=========================================
	// Show Modal
	cfw_ui_showModalMedium("Support Info", modalContent);
}

/**************************************************************************************
 * Create a confirmation modal panel that executes the function passed by the argument
 * @param message the message to show
 * @param confirmLabel the text for the confirm button
 * @param jsCode the javascript to execute when confirmed
 * @return nothing
 *************************************************************************************/
function cfw_ui_confirmExecute(message, confirmLabel, jsCodeOrFunction){
	
	var body = $("body");
	var modalID = 'cfw-confirm-dialog';
	
	
	var modal = $('<div id="'+modalID+'" class="modal fade" tabindex="-1" role="dialog">'
				+ '  <div class="modal-dialog" role="document">'
				+ '    <div class="modal-content">'
				+ '      <div class="modal-header">'
				+ '        '
				+ '        <h3 class="modal-title">Confirm</h3>'
				+ '      </div>'
				+ '      <div class="modal-body">'
				+ '        <p>'+message+'</p>'
				+ '      </div>'
				+ '      <div class="modal-footer">'
				+ '      </div>'
				+ '    </div>'
				+ '  </div>'
				+ '</div>');

	modal.modal();
	
	//set focus on confirm button
	modal.on('shown.bs.modal', function(event) {
		$('#cfw-confirmButton').get(0).focus();
	});
	
	body.prepend(modal);	
	
	var closeButton = $('<button type="button" class="close"><span aria-hidden="true">&times</span></button>');
	closeButton.attr('onclick', 'cfw_ui_confirmExecute_Execute(this, \'cancel\')');
	closeButton.data('modalID', modalID);
	
	var cancelButton = $('<button type="button" class="btn btn-primary">Cancel</button>');
	cancelButton.attr('onclick', 'cfw_ui_confirmExecute_Execute(this, \'cancel\')');
	cancelButton.data('modalID', modalID);
	
	var confirmButton = $('<button id="cfw-confirmButton" type="button" class="btn btn-primary">'+confirmLabel+'</button>');
	confirmButton.attr('onclick', 'cfw_ui_confirmExecute_Execute(this, \'confirm\')');
	confirmButton.data('modalID', modalID);
	confirmButton.data('jsCode', jsCodeOrFunction);
	
	modal.find('.modal-header').append(closeButton);
	modal.find('.modal-footer').append(cancelButton).append(confirmButton);
	
	modal.modal('show');

}


function cfw_ui_confirmExecute_Execute(sourceElement, action){
	
	var $source = $(sourceElement);
	var modalID = $source.data('modalID');
	var jsCode = $source.data('jsCode');
	
	var modal = $('#'+modalID);
		
	//remove modal
	modal.modal('hide');
	modal.remove();
	$('.modal-backdrop').remove();
	$('body').removeClass('modal-open');
	modal.remove();
	
	if(action == 'confirm'){
		CFW.utils.executeCodeOrFunction(jsCode);
	}
}
/******************************************************************
 * Creates a hidden workspace in the DOM tree.
 * This is needed for ChartJS, needs a DOM element to use
 * getComputedStyle.
 * @param 
 * @return object
 ******************************************************************/
function cfw_ui_getWorkspace() {

	var workspace = $('#cfw-hidden-workspace');
	
	if(workspace.length == 0){
		workspace = $('<div id="cfw-hidden-workspace" style="display: none;">');
		$('body').append(workspace);				
	}
	
	return workspace;
}
/******************************************************************
 * Waits for an element to appear.
 * Usage Example:
 	waitForElm('.some-class').then((element) => {
	    console.log('Element is ready');
	    console.log(element.textContent);
	});
 * @param selector a querySelector (JQuery objects not supported)
 * @return object
 ******************************************************************/
function cfw_ui_waitForAppear(selector, timeoutMillis) {
	
	if(timeoutMillis == null){
		timeoutMillis = 5000;
	}
	var starttime = Date.now();
	
    return new Promise((resolve, reject) => {
	
        if (document.querySelector(selector)) {
            return resolve(document.querySelector(selector));
        }

        const observer = new MutationObserver(mutations => {
			
            if (document.querySelector(selector)) {
                resolve(document.querySelector(selector));
                observer.disconnect();
            }

			if((Date.now() - starttime) > timeoutMillis){
				reject(selector);
			}
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    });
}


/*********************************************************************************
* Creates a printView by opening a new window and returns a jQueryDiv where you 
* can put the content inside which you want to print.
* @param title title at the top of the page(optional)
* @param description after the title(optional)
* @return jQueryElement a div you can write the content to print to.
*********************************************************************************/
function cfw_ui_createPrintView(title, description){
	
	//--------------------------
	// Create Window
	var hostURL = CFW.http.getHostURL();
	var printView = window.open();
	
	var printBox = printView.document.createElement("div");
	printBox.id = "cfw-manual-printview-box";
	printView.document.body.appendChild(printBox);
	
	//--------------------------
	// Copy Styles
	var stylesheets = $('link[rel="stylesheet"]');
	for(let i = 0; i < stylesheets.length; i++){
		let href = stylesheets.eq(i).attr('href');
		if(!CFW.utils.isNullOrEmpty(href) && href.startsWith('/cfw')){
			let cssLink = printView.document.createElement("link");
			cssLink.rel = "stylesheet";
			cssLink.media = "screen, print";
			cssLink.href = hostURL+href;
			printView.document.head.appendChild(cssLink);
		}
	}	
	
	//--------------------------
	// Override Bootstrap Style
	let cssLink = printView.document.createElement("link");
	cssLink.rel = "stylesheet";
	cssLink.media = "screen, print";
	cssLink.href = hostURL+"/cfw/jarresource?pkg=com.xresch.cfw.features.core.resources.css&file=bootstrap-theme-bootstrap.css";
	printView.document.head.appendChild(cssLink);
		
	//--------------------------
	// Override Code Style
	let cssCodestyleLink = printView.document.createElement("link");
	cssCodestyleLink.rel = "stylesheet";
	cssCodestyleLink.media = "screen, print";
	cssCodestyleLink.href = hostURL+"/cfw/jarresource?pkg=com.xresch.cfw.features.core.resources.css&file=highlightjs_arduino-light.css";
	printView.document.head.appendChild(cssCodestyleLink);
	
	//--------------------------
	// Copy Scripts
	var javascripts = $('#javascripts script');

	for(let i = 0; i < javascripts.length; i++){

		let source = javascripts.eq(i).attr('src');
		if(!CFW.utils.isNullOrEmpty(source) && source.startsWith('/cfw')){
			let script = printView.document.createElement("script");
			script.src = hostURL+source;
			printView.document.head.appendChild(script);
		}
	}
	
	var parent = $(printBox);
		
	//--------------------------
	//Create CSS
	
	var cssString = '<style  media="print, screen">'
		+'html, body {'
			+'margin: 0px;'
			+'padding: 0px;'
			+'font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";'
		+'}'
		+'table, pre, code, p { page-break-inside:auto }'
		+'tr    { page-break-inside:avoid; page-break-after:auto }'
		+'#paper {'
			+'padding: 20mm;'
			+'width: 100%;'
			+'border-collapse: collapse;'
		+'}'
		+'img{'
			+'padding-bottom: 5mm;' 
		+'}'
		+'h1{'
			+'page-break-before: always;' 
		+'}'
		+'.page-break{'
			+'page-break-before: always;' 
		+'}'
		+'.cfw-toc-header{'
			+'page-break-before: avoid;' 
		+'}'
		+'h1 {font-size: 32px;}' 
		+'h2 {font-size: 30px;}' 
		+'h3 {font-size: 28px;}' 
		+'h4 {font-size: 26px;}'
		+'h5 {font-size: 24px;}'
		+'h6 {font-size: 22px;}'
		+'div, p, span, table {font-size: 20px;}' 
		+'h1, h2, h3, h4, h5, h6{'
			+'padding-top: 5mm;' 
		+'}'
		+'#print-toc > h1, #doc-title, h1 + div > h1,  h1 + div > .page-break, h2 + div > .page-break, h3 + div > .page-break, h4 + div > .page-break{'
			+'page-break-before: avoid;' 
		+'}'
		+'</style>';
	
	parent.append(cssString);
	
	//---------------------------------
	// Add Title
	if(!CFW.utils.isNullOrEmpty(title)){
		var titleElement = printView.document.createElement("title");
		titleElement.innerHTML = title;
		printView.document.head.appendChild(titleElement);
		
		parent.append('<h1 class="text-center pt-4 mt-4">'+title+'</h1>');
	}
	
	//---------------------------------
	// Add Description
	if(!CFW.utils.isNullOrEmpty(description)){		
		parent.append('<p class="text-center pb-4 mb-4">'+description+'</p>');
	}
	return parent;
}

/******************************************************************
 * Get a cookie by ots name
 * @param 
 * @return object
 ******************************************************************/
function cfw_http_readCookie(name) {
    var nameEQ = name + "=";
    var cookieArray = document.cookie.split(';');
    for (var i = 0; i < cookieArray.length; i++) {
        var cookie = cookieArray[i];
        while (cookie.charAt(0) == ' ') {
        	cookie = cookie.substring(1, cookie.length);
        }
        if (cookie.indexOf(nameEQ) == 0){
        	return cookie.substring(nameEQ.length, cookie.length);
        }
    }
    return null;
}

/******************************************************************
 * Reads the parameters from the URL and returns an object containing
 * name/value pairs like {"name": "value", "name2": "value2" ...}.
 * Removes any hashtags at the end of parameter values. 
 * @param 
 * @return object
 ******************************************************************/
function cfw_http_getURLParamsDecoded()
{
    var vars = {};
    
    var keyValuePairs = [];
    if ( window.location.href.indexOf('?') > -1){
    	keyValuePairs = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    }
    
    for(let i = 0; i < keyValuePairs.length; i++)
    {
        var splitted = keyValuePairs[i].split('=');
        var key = cfw_http_secureDecodeURI(splitted[0]);
        let paramValue =  cfw_http_secureDecodeURI(splitted[1]);
        // Remove the annoying hashtag
        if(paramValue.endsWith('#')){
        	paramValue = paramValue.substring(0, paramValue.length-1);
        }
        vars[key] = cfw_utils_convertStringsToType(key, paramValue, true);;
    }
    
    return vars;
}

/******************************************************************
 * Reads the parameters from the URL and returns an object containing
 * name/value pairs like {"name": "value", "name2": "value2" ...}.
 * @param 
 * @return object
 ******************************************************************/
function cfw_http_getURLParams()
{
    var vars = {};
    
    var keyValuePairs = [];
    if ( window.location.href.indexOf('?') > -1){
    	keyValuePairs = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    }
    
    for(let i = 0; i < keyValuePairs.length; i++)
    {
        let splitted = keyValuePairs[i].split('=');
        
        let paramValue =  splitted[1];

        vars[splitted[0]] = paramValue;


    }
    
    return vars;
}

/******************************************************************
 * Sets additional URL params or removes them if the value is 
 * set to null.
 * @param paramsObject containing paramName / Value pairs, like:  
 *               {param: value, param2: value2}
 * @param pushHistoryState define if history state should be updated (default: true)
 * @return nothing
 ******************************************************************/
function cfw_http_setURLParams(paramsObject, pushHistoryState){
	

	if(paramsObject == null){
		return;
	}
	
	var params = cfw_http_getURLParams();
	for(var paramName in paramsObject){

		var value = paramsObject[paramName];
		
		if(value != null){
			params[paramName] = encodeURIComponent(value);
		}else{
			delete params[paramName];
		}
		
	}
	
	cfw_http_changeURLQuery(params,pushHistoryState);

}

/******************************************************************
 * Reads the parameters from the URL and returns an object containing
 * name/value pairs like {"name": "value", "name2": "value2" ...}.
 * @param 
 * @return object
 ******************************************************************/
/*function cfw_http_setURLParam(name, value){

	//------------------------------
	// Set or replace param value
	if(!CFW.utils.isNullOrEmpty(name)){
		var params = cfw_http_getURLParams();
	    params[name] = encodeURIComponent(value);
	    
	    cfw_http_changeURLQuery(params);
	}
}*/

/******************************************************************
 * Reads the parameters from the URL and returns an object containing
 * name/value pairs like {"name": "value", "name2": "value2" ...}.
 * @param 
 * @return object
 ******************************************************************/
function cfw_http_removeURLParam(name, value){

	//------------------------------
	// Remove param Value
	var params = cfw_http_getURLParams();
    delete params[name];
    
    cfw_http_changeURLQuery(params);

}

/******************************************************************
 * 
 * @param pushHistoryState define if history state should be updated (default: true)
 ******************************************************************/
function cfw_http_changeURLQuery(params, pushHistoryState){
	
	if(pushHistoryState === undefined){
		pushHistoryState = true;
	}

	//------------------------------
	// Create Query String
    var queryString = "";
    for(var key in params)
    {
		if(!CFW.utils.isNullOrEmpty(key)){
    		queryString = queryString + key +"="+params[key]+"&";
		}
    }
    //Remove last '&'
    queryString = queryString.substring(0, queryString.length-1);
    
	//------------------------------
	// Recreate URL
    var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?'+queryString;

	if(pushHistoryState && newurl != window.location.href){
		window.history.pushState({}, '', newurl);
		//use {}, do not use for param changes >> window.history.pushState({ path: newurl }, '', newurl);
	}
    

}

/**************************************************************************************
 * Tries to decode a URI and handles errors when they are thrown.
 * If URI cannot be decoded the input string is returned unchanged.
 * 
 * @param uri to decode
 * @return decoded URI or the same URI in case of errors.
 *************************************************************************************/
function cfw_http_secureDecodeURI(uri){
	var decoded;
	try{
		decoded = decodeURIComponent(uri);
	}catch(err){
		decoded = uri;
	}
	
	return decoded;
}

/**************************************************************************************
 * Executes a get request with JQuery and retrieves a standard JSON format of the CFW
 * framework. Handles alert messages if there are any.
 * 
 * The structure of the response has to adhere to the following structure:
 * {
 * 		success: true|false,
 * 		messages: [
 * 			{
 * 				type: info | success | warning | danger
 * 				message: "string",
 * 				stacktrace: null | "stacketrace string"
 * 			},
 * 			{...}
 * 		],
 * 		payload: {...}|[...] object or array
 * }
 * 
 * @param uri to decode
 * @return decoded URI or the same URI in case of errors.
 *************************************************************************************/
function cfw_http_getJSON(url, params, callbackFunc){

	$.get(url, params)
		  .done(function(response, status, xhr) {
		    //alert( "done" );
			  if(callbackFunc != null){
				  callbackFunc(response, status, xhr);
			  }
		  })
		  .fail(function(xhr, status, thrownError) {
			  CFW.ui.addToast("Request failed", "URL: "+url, "danger", CFW.config.toastErrorDelay)
			  var response = JSON.parse(xhr.responseText);
			  cfw_internal_handleMessages(response);
			  //callbackFunc(response);
		  })
		  .always(function(response) {
			  cfw_internal_handleMessages(response);
		  });
}

/**************************************************************************************
 * Executes a post request with JQuery and retrieves a standard JSON format of the CFW
 * framework. Handles alert messages if there are any.
 * 
 * The structure of the response has to adhere to the following structure:
 * {
 * 		success: true|false,
 * 		messages: [
 * 			{
 * 				type: info | success | warning | danger
 * 				message: "string",
 * 				stacktrace: null | "stacketrace string"
 * 			},
 * 			{...}
 * 		],
 * 		payload: {...}|[...] object or array
 * }
 * 
 * @param uri to decode
 * @return decoded URI or the same URI in case of errors.
 *************************************************************************************/
function cfw_http_postJSON(url, params, callbackFunc){

	$.post(url, params)
		  .done(function(response, status, xhr) {
		    //alert( "done" );
			  if(callbackFunc != null) callbackFunc(response, status, xhr);
		  })
		  .fail(function(xhr, status, errorThrown) {
			  CFW.ui.addToast("Request failed", "URL: "+url, "danger", CFW.config.toastErrorDelay);
			  var response = JSON.parse(xhr.responseText);
			  cfw_internal_handleMessages(response);
			  //callbackFunc(response);
		  })
		  .always(function(response) {
			  cfw_internal_handleMessages(response);
		  });
}

/**************************************************************************************
 * Get a form created with the class BTForm on server side using the formid.
 * 
 * The structure of the response has to adhere to the following structure:
 * {
 * 		success: true|false,
 * 		messages: [
 * 			{
 * 				type: info | success | warning | danger
 * 				message: "string",
 * 				stacktrace: null | "stacketrace string"
 * 			},
 * 			{...}
 * 		],
 * 		payload: {html: "the html form"}
 * }
 * 
 * @param formid the id of the form
 * @param targetElement the element in which the form should be placed
 *************************************************************************************/
function cfw_http_getForm(formid, targetElement){

	$.get('/cfw/formhandler', {id: formid})
		  .done(function(response) {
			  if(response.payload != null){
				  $(targetElement).html(response.payload.html);
				  CFW.http.evaluateFormScript(targetElement);
			  }
		  })
		  .fail(function(xhr, status, errorThrown) {
			  console.error("Request failed: "+url);
			  CFW.ui.addToast("Request failed", "URL: "+url, "danger", CFW.config.toastErrorDelay)
			  var response = JSON.parse(xhr.responseText);
			  cfw_internal_handleMessages(response);
		  })
		  .always(function(response) {
			  cfw_internal_handleMessages(response);			  
		  });
}
/**************************************************************************************
 * Finds a CFWForm in the element passed as argument and executes the initialization 
 * script.
 * @param formid the id of the form
 * @param targetElement the element in which the form should be placed
 *************************************************************************************/
function cfw_http_evaluateFormScript(elementContainingForm){
	
	var forms = $(elementContainingForm).find('form');

	forms.each(function() {
		cfw_initializeForm(this);
	});
	    
}


/**************************************************************************************
 * Calls a rest service that creates a form and returns a standard json format,
 * containing the html of the form in the payload.
 * 
 * The structure of the response has to adhere to the following structure:
 * {
 * 		success: true|false,
 * 		messages: [
 * 			{
 * 				type: info | success | warning | danger
 * 				message: "string",
 * 				stacktrace: null | "stacketrace string"
 * 			},
 * 			{...}
 * 		],
 * 		payload: {html: "the html form"}
 * }
 * 
 * @param url to call
 * @param params to pass
 * @param targetElement the element in which the form should be placed
 *************************************************************************************/
function cfw_http_createForm(url, params, targetElement, callback){

	$.get(url, params)
		  .done(function(response, status, xhr) {
			  if(response.payload != null){
			      $(targetElement).append(response.payload.html);
			      var formID = $(targetElement).find('form').attr("id");
	              eval("intializeForm_"+formID+"();");
	              
	              //--------------------------
	              // prevent Submit on enter
	//              $('#'+formID).on('keyup keypress', function(e) {
	//            	  var keyCode = e.keyCode || e.which;
	//            	  if (keyCode === 13) { 
	//            	    e.preventDefault();
	//            	    return false;
	//            	  }
	//            	});
	              //--------------------------
	              // Call callback
	              if(callback != undefined){
	            	  callback(formID, status, xhr);
	              }
			  }
		  })
		  .fail(function(response) {
			  console.error("Request failed: "+url);
			  CFW.ui.addToast("Request failed", "URL: "+url, "danger", CFW.config.toastErrorDelay)

		  })
		  .always(function(response) {
			  cfw_internal_handleMessages(response);			  
		  });
}


/******************************************************************
 * Method to fetch data from the server with CFW.http.getJSON(). 
 * The result is cached in the global variable CFW.cache.data[key].
 *
 * @param url
 * @param params the query params as a json object e.g. {myparam: "value", otherkey: "value2"}
 * @param key under which the data will be stored
 * @param callback method which should be called when the data is available.
 * @return nothing
 *
 ******************************************************************/
function cfw_http_fetchAndCacheData(url, params, key, callback){
	//---------------------------------------
	// Fetch and Return Data
	//---------------------------------------
	if (CFW.cache.data[key] == undefined || CFW.cache.data[key] == null){
		CFW.http.getJSON(url, params, 
			function(data) {
				CFW.cache.data[key] = data;	
				if(callback != undefined && callback != null ){
					callback(data);
				}
		});
	}else{
		if(callback != undefined && callback != null){
			callback(CFW.cache.data[key]);
		}
	}
}

/**************************************************************************************
* Returns the current URL of the page without the query part.
 *************************************************************************************/
function cfw_http_getHostURL() {
	return location.protocol+'//'+location.hostname+(location.port ? ':'+location.port: '');
};

/**************************************************************************************
* Returns the current URL path.
 *************************************************************************************/
function cfw_http_getURLPath() {
	return location.pathname;
};

/**************************************************************************************
 * Calls a rest service that creates a form and returns a standard json format,
 * containing the html of the form in the payload.
 * 
 * @param url to call
 * @param params to pass
 * @param targetElement the element in which the form should be placed
 *************************************************************************************/
function cfw_internal_postForm(url, formID, callback){
	// switch summernote to wysiwyg view, at it will not save in code view
	$('.btn-codeview.active').click();
	
	$(formID+'-submitButton').prepend('<i class="fa fa-cog fa-spin fa-fw mr-1 loaderMarker"></i>')
	
	window.setTimeout( 
		function(){
			cfw_http_postJSON(url, CFW.format.formToParams(formID, true), function (data,status,xhr){
				$(formID+'-submitButton .loaderMarker').remove();
				if(callback != null){
					callback(data,status,xhr);
				}
			})
		},
	50);
	
}
/**************************************************************************************
 * Handle messages of a standard JSON response.
 * 
 * The structure of the response has to contain a array with messages:
 * {
 * 		messages: [
 * 			{
 * 				type: info | success | warning | danger
 * 				message: "string",
 * 				stacktrace: null | "stacketrace string"
 * 			},
 * 			{...}
 * 		],
 * }
 * 
 * @param response the response with messages
 **************************************************************************************/
function cfw_internal_handleMessages(response){
	
	var msgArray = response.messages;
	  
	  if(msgArray != undefined
	  && msgArray != null
	  && msgArray.length > 0){
		  for(var i = 0; i < msgArray.length; i++ ){
			  CFW.ui.addToast(msgArray[i].message, null, msgArray[i].type, CFW.config.toastErrorDelay);
		  }
	  }
}

/**************************************************************************************
* Function to store a value in the local storage.
* @param key 
* @param value
 *************************************************************************************/
function cfw_cache_storeValue(key, value) {
	window.localStorage.setItem("cfw-"+key, value);
};

/**************************************************************************************
* Function to store a value in the local storage for the current page.
* @param key 
* @param value
 *************************************************************************************/
function cfw_cache_storeValueForPage(key, value) {
	window.localStorage.setItem("cfw-["+CFW.http.getURLPath()+"]:"+key, value);
};

/**************************************************************************************
* Function to retrieve a stored value.
* @param key 
* @param defaultValue if there is nothing stored
* @return either the value of the cookie or an empty string
 *************************************************************************************/
function cfw_cache_retrieveValue(key, defaultValue) {
	
	var item = window.localStorage.getItem("cfw-"+key);
    if(item != null){
    	return item;
    }
	return defaultValue;
};

/**************************************************************************************
* Function to retrieve a value that was stored for the page.
* @param key 
* @param defaultValue if there is nothing stored
* @return either the value of the cookie or an empty string
 *************************************************************************************/
function cfw_cache_retrieveValueForPage(key, defaultValue) {
	
	var item = window.localStorage.getItem("cfw-["+CFW.http.getURLPath()+"]:"+key);
    if(item != null){
    	return item;
    }
	return defaultValue;
};
/******************************************************************
 * Method to remove the cached data under the specified key.
 *
 * @param key under which the data is stored
 * @return nothing
 *
 ******************************************************************/
function cfw_cache_removeFromCache(key){
	CFW.cache.data[key] = null;
}

/******************************************************************
 * Method to remove all the data in the cache.
 *
 * @return nothing
 *
 ******************************************************************/
function cfw_cache_clearCache(){
	CFW.cache.data = {};
}

/*************************************************************************************
 * Starts a bundle of steps.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_bundleStart(jqueryObjectOrId){
	
	//---------------------------------------------
	// Add Button to Menubar 
	if($('#cfwMenuButtons-Tutorials').length == 0){
		var tutsButton = $('<li class="cfw-button-menuitem">'
			+'<a class="dropdown-item" onclick="cfw_tutorial_drawStartpage()" id="cfwMenuButtons-Tutorials">'
				+'<div class="cfw-fa-box">'
					+'<i class="fas fa-graduation-cap"></i>'
				+'</div><span class="cfw-menuitem-label">Tutorials</span></a>'
			+'</li>'
			)
		$('.navbar-right').prepend(tutsButton);
	}	
	
	//---------------------------------------------
	// Start Bundle 
	CFW.tutorial.data.currentBundle = {
		  startingObject: jqueryObjectOrId
		, steps: []
	}
}

/*************************************************************************************
 * Starts a bundle of steps.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_bundleEnd(){
	
	CFW.tutorial.data.bundles.push(CFW.tutorial.data.currentBundle);
	CFW.tutorial.data.currentBundle = null;
}

/*************************************************************************************
 * Plays a bundle and starts by showing the first step.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_bundlePlay(event, index){
	
	event.stopPropagation();
	
	CFW.tutorial.data.bundlePlaying = index;
	CFW.tutorial.data.currentStep = 0;
	
	var bundleToPlay = CFW.tutorial.data.bundles[index];
	var steps = bundleToPlay.steps;
	
	if(steps != null && steps.length > 0){
		
		cfw_tutorial_reset();
		$('#cfw-tuts-btn-prev').removeClass('d-none');
		$('#cfw-tuts-btn-next').removeClass('d-none');
		
		cfw_tutorial_drawStep(steps[0]);

	}else{
		CFW.ui.addToastWarning("Seems this tutorial does not contain any steps");
	}

}

/*************************************************************************************
 * Adds a step to the current bundle.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_addStep(stepData){
	
	var defaultValues = {
		  selector: null
		, clickable: false
		// useful to wait for an element to appear, like a modal panel
		, drawDelay: 0
		, text: null
		, beforeDraw: null
	}
	
	var finalStep = Object.assign({}, defaultValues, stepData);

	CFW.tutorial.data.currentBundle.steps.push(finalStep);
}


/*************************************************************************************
 * Adds a step to the current bundle.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_nextStep(){
	
	var bundleIndex = CFW.tutorial.data.bundlePlaying;
	var bundleToPlay = CFW.tutorial.data.bundles[bundleIndex];
	var currentStepIndex = CFW.tutorial.data.currentStep;
	
	if( (currentStepIndex+1) < bundleToPlay.steps.length ){
		
		cfw_tutorial_drawStep(bundleToPlay.steps[currentStepIndex+1]);
		CFW.tutorial.data.currentStep += 1;

	}else if( (currentStepIndex+1) == bundleToPlay.steps.length ) {
		
		cfw_tutorial_drawStep({
			  selector: null
			, clickable: true
			, text: "You have reached the end of the tutorial."
			, drawDelay: 500
			, beforeDraw: null
		});
		CFW.tutorial.data.currentStep += 1;
	}else{
		cfw_tutorial_drawStartpage();
		return;
	}
	
	
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_drawStartpage(){
	
	//Make sure anything previous tutorials are closed 
	cfw_tutorial_close();
	
	var parent = $('body');

	cfw_tutorial_objectAppendOverlays(parent)
	
	//-----------------------------
	// Overlays
	for(var index in CFW.tutorial.data.bundles){
		var bundle = CFW.tutorial.data.bundles[index];
		var object = $(bundle.startingObject);

		if(object.length == 0){
			continue;
		}
		
		cfw_tutorial_objectHighlight(object, true);
		
		var playBundleOverlay = 
			$('<div class="cfw-tuts-playbundle" onclick="cfw_tutorial_bundlePlay(event,'+index+')">');
			
		object.append(playBundleOverlay);
	}
	
	//-----------------------------
	// Controls
	var controls = $('<div class="cfw-tuts-controls">'
						+'<div class="d-flex justify-content-center w-100">'
						+'<a id="cfw-tuts-btn-home" class="btn btn-sm  btn-primary mr-2" onclick="cfw_tutorial_drawStartpage()"><i class="fas fa-home"></i></a>'
						+'<a id="cfw-tuts-btn-close" class="btn btn-sm  btn-primary mr-2" onclick="cfw_tutorial_close()"><i class="fas fa-times"></i></a>'
						+'<a id="cfw-tuts-btn-next" class="btn btn-sm  btn-primary mr-2 d-none" onclick="cfw_tutorial_nextStep()">Next</a>'
					+'</div>'
				);
	
	parent.append(controls);
	
	//-----------------------------
	// Controls	
	if(!CFW.tutorial.data.toastWasShown){
		CFW.ui.addToastInfo("Click one of the highlighted elements to start a tutorial.")
		CFW.tutorial.data.toastWasShown = true;
	}
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_drawStep(stepData){
	
	var selector = stepData.selector;
	
	//----------------------------------
	// Call beforeDraw
	if(stepData.beforeDraw != null){
		stepData.beforeDraw();
	}
	
	if(selector != null){
		//----------------------------------
		// Handle Selector
		CFW.ui.waitForAppear(selector).then(function(element){

			//----------------------------------
			// Handle Modals
			cfw_tutorial_objectAppendOverlays($('.modal-dialog'));
			
			cfw_tutorial_reset();
			
			CFW.ui.toggleLoader(true);	
			
			window.setTimeout(function(){
				
				//----------------------------------
				// Highlight and Popover
				cfw_tutorial_objectHighlight($(selector), stepData.clickable);
				cfw_tutorial_objectPopover($(selector), stepData.text);
				
				CFW.ui.toggleLoader(false);	
			}, stepData.drawDelay);
		})
		return;
	}else{
		cfw_tutorial_reset();
		
		//----------------------------------
		// Show TextBox Textbox
		window.setTimeout(function(){
			var textbox = $('<div class="cfw-tuts-textbox">');
			textbox.append(stepData.text);
			$('body').append(textbox);
		}, stepData.drawDelay);
		
	}
	
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_objectHighlight(objectToHighlight, clickable){
	
	if(objectToHighlight != null){
		
		//only highlight the first
		var object =  $($(objectToHighlight)[0]);
		
		if(object.length > 0){
			
			var clazz = 'cfw-tuts-highlight';
			if(clickable){
				clazz += '-clickable'; 
			}
			object.addClass(clazz)
			object.get(0).scrollIntoView();	  
			window.scrollBy(0, -90);
		}
	}
	
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_objectPopover(objectToAddPopover, content){
	
	// only popover the first
	var object =  $($(objectToAddPopover)[0]);
	
	if(object != null){
		var bounds = object[0].getBoundingClientRect();
		
		object.addClass('tutorial-popover')
		
		//-----------------------------
		// Marking Popover
		object
			.popover({
				  //title: "title"
				  content: content
				, container: "body"
				, sanitize: false
				, html: true
				, placement: 'auto'
				, boundary: 'window'
				, animation: true
			})
			.popover('show')
	}
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_objectAppendOverlays(targetObject){

	if(targetObject != null){
		var object =  $(targetObject);
		
		if(object.find('.cfw-tuts-backdrop').length == 0){
			var backdrop = $('<div class="cfw-tuts-backdrop">');
			object.append(backdrop);
		}
		
		if(object.find('.cfw-tuts-clickblock').length == 0){
			var clickblock = $('<div class="cfw-tuts-clickblock">');
			object.append(clickblock);
		}
		
	}
}


/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_reset(){
			
	$('.tutorial-popover')
		.popover('hide')
		.removeClass('.tutorial-popover');
			
	$('.popover').remove();
	$('.cfw-tuts-highlight').removeClass('cfw-tuts-highlight');
	$('.cfw-tuts-highlight-clickable').removeClass('cfw-tuts-highlight-clickable');
	$('.cfw-tuts-playbundle').remove();
	$('.cfw-tuts-textbox').remove();
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_close(){
	cfw_tutorial_reset();
	
	$('.modal').modal('hide');
		
	$('.cfw-tuts-controls').remove();
	$('.cfw-tuts-clickblock').remove();
	$('.cfw-tuts-backdrop').remove();
	
}

/**************************************************************************************
 * Select all the content of the given element.
 * For example to select everything inside a given DIV element using 
 * <div ondblclick="selectElementContent(this)">.
 * @param el the dom element 
 *************************************************************************************/
function cfw_selectElementContent(el) {
    if (typeof window.getSelection != "undefined" && typeof document.createRange != "undefined") {
        var range = document.createRange();
        range.selectNodeContents(el);
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    } else if (typeof document.selection != "undefined" && typeof document.body.createTextRange != "undefined") {
        var textRange = document.body.createTextRange();
        textRange.moveToElementText(el);
        textRange.select();
    }
}

/**************************************************************************************
 * Checks if the user has the specified permission
 * @param permissionName the name of the Permission
 *************************************************************************************/
function  cfw_hasPermission(permissionName){
	$.ajaxSetup({async: false});
	cfw_http_fetchAndCacheData("/app/usermanagement/permissions", null, "userPermissions")
	$.ajaxSetup({async: true});
	
	if(CFW.cache.data["userPermissions"] != null
	&& CFW.cache.data["userPermissions"].payload.includes(permissionName)){
		return true;
	}
	
	return false;
}


/**************************************************************************************
 * Checks if the user has the specified permission
 * @param permissionName the name of the Permission
 *************************************************************************************/
function  cfw_getUserID(){
	$.ajaxSetup({async: false});
	cfw_http_fetchAndCacheData("./usermanagement/permissions", null, "userPermissions")
	$.ajaxSetup({async: true});
	
	if(CFW.cache.data["userPermissions"] != null
	&& CFW.cache.data["userPermissions"].payload.includes(permissionName)){
		return true;
	}
	
	return false;
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_render_registerRenderer(rendererUniqueName, rendererObject){
	CFW.render.registry[rendererUniqueName] = rendererObject;
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_render_getRenderer(rendererUniqueName){
	return CFW.render.registry[rendererUniqueName];
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_render_createDataviewerDefaults(){
	return [
		{	label: 'Table',
			name: 'table',
			renderdef: {
				rendererSettings: {
					table: {filterable: false},
				},
			}
		},
		{	label: 'Bigger Table',
			name: 'table',
			renderdef: {
				rendererSettings: {
					table: {filterable: false, narrow: false},
				},
			}
		},
		{	label: 'Panels',
			name: 'panels',
			renderdef: {}
		},
		{	label: 'Bigger Panels',
			name: 'panels',
			renderdef: {
				rendererSettings: {
					panels: {narrow: false},
				},
			}
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
	];
}



/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_loadLocalization(){
	//-----------------------------------
	// 
	if(CFW.cache.lang == null){
		$.ajaxSetup({async: false});
			cfw_http_getJSON("/cfw/locale", {id: JSDATA.localeIdentifier}, function(data, status, xhr){
				
				if (xhr.status == 200){
					window.localStorage.setItem("lang-"+JSDATA.localeIdentifier, JSON.stringify(data.payload) );
					CFW.cache.lang = data.payload;
				}else if (xhr.status == 304){
					//CFW.cache.lang = data.payload;
					CFW.cache.lang = JSON.parse(window.localStorage.getItem("lang-"+JSDATA.localeIdentifier));
				}

			});
		$.ajaxSetup({async: true});
		
		//if load not successful, try to fall back to localStorage
		if(CFW.cache.lang == null){
			CFW.cache.lang = JSON.parse(window.localStorage.getItem("lang-"+JSDATA.localeIdentifier));
		}
	}
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_lang(key, defaultValue){

	var value = CFW.cache.lang[key];

	if(value != null){
		return value;
	}else{
		return defaultValue;
	}

}

/********************************************************************
 * CFW FRAMEWORK STRUCTURE
 * -----------------------
 ********************************************************************/
var CFWL = cfw_lang;

var CFW = {
	global: {
		formID: 'cfw-formID',
		autocompleteCounter: 0,
		autocompleteFocus: -1,
		autcompleteParamEnhancerFunctions: [],
		timeframePickerOnchangeHandlers: {},
		isLocaleFetching: null,
		lastServerAccess: moment(),
		lastOpenedModal: null,
	},
	lang: {
		get: cfw_lang,
		loadLocalization: cfw_loadLocalization,
	},
	colors: {
		colorizeElement: cfw_colors_colorizeElement,
		RGB2HEX: cfw_colors_RGB2HEX,
		randomRGB: cfw_colors_randomRGB,
		randomHSL: cfw_colors_randomHSL,
		randomSL: cfw_colors_randomSL,
		getThresholdStyle: cfw_colors_getThresholdStyle,
		getSplitThresholdStyle: cfw_colors_getSplitThresholdStyle,
		getThresholdWorse: cfw_colors_getThresholdWorse,
	},
	config: {
		toastDelay: 	 3000,
		toastErrorDelay: 10000
	},
	cache: { 
		data: {},
		lang: null,
		removeFromCache: cfw_cache_removeFromCache,
		clearCache: cfw_cache_clearCache,
		storeValue: cfw_cache_storeValue,
		storeValueForPage: cfw_cache_storeValueForPage,
		retrieveValue: cfw_cache_retrieveValue,
		retrieveValueForPage: cfw_cache_retrieveValueForPage
	},
	render: {
		registry: {},
		registerRenderer: cfw_render_registerRenderer,
		getRenderer: cfw_render_getRenderer,
		createDataviewerDefaults: cfw_render_createDataviewerDefaults,
	},
	array: {
		sortArrayByValueOfObject: cfw_sortArrayByValueOfObject
	},
	format: {
		  epochToTimestamp: 	cfw_format_epochToTimestamp
		, epochToDate: 			cfw_format_epochToDate
		, fieldNameToLabel: 	cfw_format_fieldNameToLabel
		, millisToClock: 		cfw_format_millisToClock
		, timeToDuration: 		cfw_format_timeToDuration
		, millisToDuration: 	cfw_format_millisToDuration
		, cfwSchedule: 			cfw_format_cfwSchedule
		, splitCFWSchedule: 	cfw_format_splitCFWSchedule
		, objectToHTMLList: 	cfw_format_objectToHTMLList
		, csvToObjectArray: 	cfw_format_csvToObjectArray
		, capitalize: 			cfw_format_capitalize
		, numberSeparators: 	cfw_format_numberSeparators
		, numbersInThousands: 	cfw_format_numbersInThousands
		, badgesFromArray: 		cfw_format_badgesFromArray
		, badgesFromObjectValues: cfw_format_badgesFromObjectValues
		, formToParams: 		cfw_format_formToParams
		, formToArray: 			cfw_format_formToArray
		, formToObject: 		cfw_format_formToObject
	},
	customizer: {
		booleanFormat: 	cfw_customizer_booleanFormat,
		badgesFromArray: 	cfw_customizer_badgesFromArray,
		badgesFromObjectValues: 	cfw_customizer_badgesFromObjectValues,
	},
	http: {
		readCookie: cfw_http_readCookie,
		getURLParams: cfw_http_getURLParams,
		getURLParamsDecoded: cfw_http_getURLParamsDecoded,
		setURLParams: cfw_http_setURLParams,
		removeURLParam: cfw_http_removeURLParam,
		getHostURL: cfw_http_getHostURL,
		getURLPath: cfw_http_getURLPath,
		secureDecodeURI: cfw_http_secureDecodeURI,
		getJSON: cfw_http_getJSON,
		postJSON: cfw_http_postJSON,
		getForm: cfw_http_getForm,
		createForm: cfw_http_createForm,
		evaluateFormScript: cfw_http_evaluateFormScript,
		fetchAndCacheData: cfw_http_fetchAndCacheData
	},
	
	selection: {
		selectElementContent: cfw_selectElementContent
	},
	style: {
		  green: 'cfw-green'
		, limegreen: 'cfw-limegreen'
		, yellow: 'cfw-yellow'
		, orange: 'cfw-orange'
		, red: 'cfw-red'
		, disabled: 'cfw-darkgray'
		, notevaluated: 'cfw-gray'
		, none: 'cfw-none'
	},
	tutorial: {
		data: {
			bundles: []
		}
	},
	utils: {
		executeCodeOrFunction: cfw_utils_executeCodeOrFunction,
		filterItems: cfw_utils_filterItems,
		randomString: cfw_utils_randomString,
		chainedOnload: cfw_utils_chainedOnload,
		isNullOrEmpty: cfw_utils_isNullOrEmpty,
		nullTo: cfw_utils_nullTo,
		setFloatPrecision: cfw_utils_setFloatPrecision,
		urlToLink: cfw_utils_urlToLink,
		replaceAll: cfw_utils_replaceAll,
		randomInt: cfw_utils_randomInt,
		clipboardRead: cfw_utils_clipboardRead,
		clipboardWrite: cfw_utils_clipboardWrite,
	},
	ui: {
		createToggleButton: cfw_ui_createToggleButton,
		toc: cfw_ui_createTOC,
		createPrintView: cfw_ui_createPrintView,
		addToast: cfw_ui_addToast,
		addToastInfo: function(text){cfw_ui_addToast(text, null, "info", CFW.config.toastDelay);},
		addToastSuccess: function(text){cfw_ui_addToast(text, null, "success", CFW.config.toastDelay);},
		addToastWarning: function(text){cfw_ui_addToast(text, null, "warning", CFW.config.toastDelay);},
		addToastDanger: function(text){cfw_ui_addToast(text, null, "danger", CFW.config.toastErrorDelay);},
		showModalMedium: cfw_ui_showModalMedium,
		showModalSmall: cfw_ui_showModalSmall,
		showModalLarge: cfw_ui_showModalLarge,
		confirmExecute: cfw_ui_confirmExecute,
		toggleLoader: cfw_ui_toggleLoader,
		toggleDropdownMenuFixed: cfw_ui_toggleDropdownMenuFixed,
		createLoaderHTML: cfw_ui_createLoaderHTML,
		addAlert: cfw_ui_addAlert,
		getWorkspace: cfw_ui_getWorkspace,
		waitForAppear: cfw_ui_waitForAppear
	},
	hasPermission: cfw_hasPermission,

}


/********************************************************************
 * General initialization
 ********************************************************************/
CFW.lang.loadLocalization();

CFW.utils.chainedOnload(function () {

	
	//-----------------------------------
	// Setup Keyboard Shortcuts
	$('body').keyup(function (e){
		
		//--------------------------------
		// Ctrl+Alt+M - Reopen Modal
		if (e.ctrlKey && event.altKey &&  e.keyCode == 77) {
			if(CFW.global.lastOpenedModal != null)
			cfw_ui_reopenModal(CFW.global.lastOpenedModal);
			return;
		}
		
		//--------------------------------
		// Ctrl+Alt+I - Show Support Info
		if (e.ctrlKey && event.altKey &&  e.keyCode == 73) {
			cfw_ui_showSupportInfoModal();
			return;
		}
		
	});
	
	//-----------------------------------
	// execute a function when someone clicks in the document
	document.addEventListener("click", function (e) {
	    cfw_autocompleteCloseAll();
		cfw_ui_closeAllDropdownMenuFixed();
	});
	
	//--------------------------------
	// Do Ctrl+Space separately, as keyup
	// would also trigger on Arrow Keys etc...
	document.addEventListener('keydown', function(e) {
		
		// close autocomplete on Esc
		if ( e.keyCode == 27) {
			cfw_autocompleteCloseAll();
			// remove any popovers
			$('.popover').remove();
			
			cfw_ui_closeAllDropdownMenuFixed();
		}
		
	});

	
	//-----------------------------------
	// Check Session Timeout
	if(JSDATA.sessionTimeout != null){
		
		$( document ).ajaxComplete(function() {
			  CFW.global.lastServerAccess = moment();
		});
		CFW.global.sessionCheckInterval = window.setInterval(function(){
			
			var lastAccessSeconds = CFW.global.lastServerAccess.unix();
			var currentSeconds = moment().unix();
			var timeoutSeconds = JSDATA.sessionTimeout;
			
			if( (currentSeconds - lastAccessSeconds) > timeoutSeconds ){
				CFW.ui.showModalSmall("Session Timeout", '<p>Your session has timed out, please refresh your browser.</p>');
				window.clearInterval(CFW.global.sessionCheckInterval);
			}
			
		}, 30000);
	}
	
	//-----------------------------------
	// Add scrolling offset for menu bar
	$( window ).on('hashchange', function (e){
		window.scrollBy(0, -60);
	});
	
	//-----------------------------------
	// Initialize tooltip
	$('[data-toggle="tooltip"]').tooltip();
	
	//-----------------------------------
	// Setup Bootstrap hierarchical menu
	$('.dropdown-menu a.dropdown-toggle').on('click', function(e) {
		var clickedMenuItem = $(this);
		var submenuContainer = $(this).next();

		if (!submenuContainer.hasClass('show')) {
			clickedMenuItem.parents('.dropdown-menu').first().find('.show').removeClass("show");
		}

		var $subMenu = $(this).next(".dropdown-menu");
		$subMenu.toggleClass('show');

		$(this).parents('li.nav-item.dropdown.show').on('hidden.bs.dropdown', function(event) {
		  $('.dropdown-submenu .show').removeClass("show");
		});

		return false;
	});
})

