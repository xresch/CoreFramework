
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
	return Math.random().toString(maxChars).substring(2, maxChars /2) + Math.random().toString(36).substring(2, maxChars /2);
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
 * Returns a Random integer between min(inclusive) and max(exclusive).
 ************************************************************************************************/
function cfw_utils_randomInt(min, max) {
	return Math.floor(Math.random() * (max - min) + min);
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
function cfw_colors_randomHSL(hue, minS, maxS, minL, maxL) {
	
	var s = CFW.utils.randomInt(minS, maxS);
	var l = CFW.utils.randomInt(minL, maxL);
	
	return 'hsla('+hue % 360+','+s+'%,'+l+'%, 1.0)';
}

/************************************************************************************************
 * Returns a cfw style string for the value based on the defined thresholds e.g "cfw-excellent".
 * You can use the string for creating a class like: 
 *   "bg-cfw-excellent"
 *   "text-cfw-excellent"
 *   "border-cfw-excellent"
 *   "table-cfw-excellent"
 *   
 * If all the thresholds are null/undefined or the value is NaN returns an "cfw-none".
 * You can define thresholds values increasing from Excellent to Danger, or from Danger to Excellent.
 * You can let thresholds undefined/null to skip the color. Values below the lowest threshold value
 * will result in "cfw-gray".
 * 
 * If isDisabled is set to "true", returns "cfw-darkgray".
 * 
 * @param value the value that should be thresholded
 * @param tExellent the threshold for excellent
 * @param tGood the threshold for good
 * @param tWarning the threshold for warning
 * @param tEmergency the threshold for emergency
 * @param tDanger the threshold for danger
 * @param isDisabled define if the thresholding is disabled
 ************************************************************************************************/
function cfw_colors_getThresholdStyle(value, tExellent, tGood, tWarning, tEmergency, tDanger, isDisabled) {

	//---------------------------
	// Initial Checks
	if(isDisabled) { return "cfw-darkgray"; }
	
	if(isNaN(value)
	|| (
		CFW.utils.isNullOrEmpty(tExellent)
	   && CFW.utils.isNullOrEmpty(tGood)
	   && CFW.utils.isNullOrEmpty(tWarning)
	   && CFW.utils.isNullOrEmpty(tEmergency)
	   && CFW.utils.isNullOrEmpty(tDanger)
	   )
	){
		return "cfw-none";
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

	var styleString = "cfw-gray";
	
	if(direction == 'HIGH_TO_LOW'){
		if 		(!CFW.utils.isNullOrEmpty(tExellent) 	&& value >= tExellent) 	{ styleString = "cfw-excellent"; } 
		else if (!CFW.utils.isNullOrEmpty(tGood) 		&& value >= tGood) 		{ styleString = "cfw-good"; } 
		else if (!CFW.utils.isNullOrEmpty(tWarning) 	&& value >= tWarning) 	{ styleString = "cfw-warning"; } 
		else if (!CFW.utils.isNullOrEmpty(tEmergency) 	&& value >= tEmergency) { styleString = "cfw-emergency"; } 
		else if (!CFW.utils.isNullOrEmpty(tDanger) 		&& value >= tDanger)  	{ styleString = "cfw-danger"; } 
		else																	{ styleString = "cfw-gray"; } 
	}else{
		if 		(!CFW.utils.isNullOrEmpty(tDanger) 		&& value>= tDanger)  	{ styleString = "cfw-danger"; } 
		else if (!CFW.utils.isNullOrEmpty(tEmergency) 	&& value>= tEmergency) 	{ styleString = "cfw-emergency"; } 
		else if (!CFW.utils.isNullOrEmpty(tWarning) 	&& value>= tWarning) 	{ styleString = "cfw-warning"; } 
		else if (!CFW.utils.isNullOrEmpty(tGood) 		&& value>= tGood) 		{ styleString = "cfw-good"; } 
		else if (!CFW.utils.isNullOrEmpty(tExellent) 	&& value>= tExellent) 	{ styleString = "cfw-excellent"; } 	
		else																	{ styleString = "cfw-gray"; } 
	}
	
	return styleString;
}




/**************************************************************************************
 * Filters items in the selected DOM nodes.
 * The items that should be filtered(based on their HTML content) have to be found with
 * the itemSelector.
 * 
 *@param context the JQuery selector for the element containing the items which should be filtered.
 *@param searchField the searchField of the field containing the search string.
 *@param itemSelector the JQuery selector for the object which should be filtered.
 *************************************************************************************/
function cfw_filterItems(context, searchField, itemSelector){

	var filterContext = $(context);
	var input = $(searchField);
	
	var filter = input.val().toUpperCase();
	
	filterContext.find(itemSelector).each(function( index ) {
		  
		  if ($(this).html().toUpperCase().indexOf(filter) > -1) {
			  $(this).css("display", "");
		  } else {
			  $(this).css("display", "none");
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
		  cfw_handleMessages(response);			  
	  });
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
		tagClass: 'btn btn-sm btn-primary mb-1',
		maxTags: 255,
		maxChars: 1024,
		trimValue: true,
		allowDuplicates: false,
		addOnBlur: false
//		confirmKeys: [188, 13]
	});
	
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
	tagsfield.addClass("cfw-tags-selector");
	
	tagsfield.tagsinput({
		tagClass: 'btn btn-sm btn-primary mb-1',
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
	
	for(var key in values){
		tagsfield.tagsinput('add', { "value": key , "label": values[key] });
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
		var date = new CFWDate(epochMillis);
		datepicker.first().val(date.getDateForInput());
		
		if(timepicker.length > 0 != null){
			timepicker.first().val(date.getTimeForInput());
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
 * Initialize an autocomplete added to a CFWField with setAutocompleteHandler().
 * Can be used to make a static autocomplete using the second parameter.
 * 
 * @param fieldID the name of the field
 * @return nothing
 *************************************************************************************/
function cfw_autocompleteInitialize(formID, fieldName, maxResults, array){
		
	CFW.global.autocompleteFocus = -1;
	var $input = $("#"+fieldName);
	
	if($input.attr('data-role') == "tagsinput"){
		$input = $("#"+fieldName+"-tagsinput")
	}
	
	//prevent browser default auto fill
	$input.attr('autocomplete', 'off');
	
	var inputField = $input.get(0);
	var autocompleteID = inputField.id + "-autocomplete";
	
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
			cfw_autocompleteShow(this, {lists:[filteredArray], description: null});
		});
	}
	
	//--------------------------------------------------------------
	// DYNAMIC SERVER SIDE AUTOCOMPLETE
	//--------------------------------------------------------------
	if(array == null){
		$input.on('input', function(e) {
			
			// use a count and set timeout to wait for the user 
			// finishing his input before sending a request to the
			// server. Reduces overhead.
			var currentCount = ++CFW.global.autocompleteCounter;
			
			//----------------------------
			// Show Loader
			var loader = $input.parent().find('#autocomplete-loader');
			if(loader.length == 0){
				loader = $('<div id="autocomplete-loader"><small><i class="fa fa-cog fa-spin fa-1x"></i><span>&nbsp;Loading...</span></small></div>');
				$input.after(loader);
			}
			
			//----------------------------
			// Load Autocomlete
			setTimeout(
				function(){
					
					if(currentCount != CFW.global.autocompleteCounter){
						return;
					}
					
					var params = CFW.format.formToParams($input.closest('form'));
					params.cfwAutocompleteFieldname = fieldName;
					params.cfwAutocompleteSearchstring = inputField.value;
					
					cfw_postJSON('/cfw/autocomplete', params, 
						function(data) {
							loader.remove();
							cfw_autocompleteShow(inputField, data.payload);
						})
				},
				500);
				
		});
	}
	
	//--------------------------------------------------------------
	// execute a function presses a key on the keyboard
	//--------------------------------------------------------------
	$input.on('keydown',  function(e) {
		

		var itemList = $("#"+autocompleteID);
		var items;
		
		if (itemList != null && itemList.length > 0){ 
			items = itemList.find(".autocomplete-item")
		};

		//---------------------------
		// Down Arrow
		if (e.keyCode == 40) {
			  CFW.global.autocompleteFocus++;
			  markActiveItem(items);
			  return;
		}
		//---------------------------
		// Up Arrow
		if (e.keyCode == 38) { 
			  CFW.global.autocompleteFocus--;
			  markActiveItem(items);
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
				if (itemList != null && items != null) items.eq(CFW.global.autocompleteFocus).click();
			}else{
				// Close if nothing selected
				cfw_autocompleteCloseAll();
			}
		}
	});
	

	
	//--------------------------------------------------------------
	// a function to classify an item as "active"
	//--------------------------------------------------------------
	function markActiveItem(items) {
		if (!items) return false;
		/* start by removing the "active" class on all items: */
		removeActiveClass(items);
		if (CFW.global.autocompleteFocus >= items.length) CFW.global.autocompleteFocus = 0;
		if (CFW.global.autocompleteFocus < 0) CFW.global.autocompleteFocus = (items.length - 1);
		/* add class "autocomplete-active": */
		items.eq(CFW.global.autocompleteFocus).addClass("autocomplete-active");
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
 * close all autocomplete lists in the document.
 *************************************************************************************/
function cfw_autocompleteCloseAll() {	
	CFW.global.autocompleteFocus = -1;
	$('.autocomplete-wrapper').remove();
}

/* execute a function when someone clicks in the document: */
document.addEventListener("click", function (e) {
    cfw_autocompleteCloseAll();
});

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
function cfw_autocompleteShow(inputField, autocompleteResults){
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
    	
    var multipleLists = $('<div class="autocomplete-multilist d-flex flex-row">');
    
    for(var key in autocompleteResults.lists){
    	var current = autocompleteResults.lists[key];
    	 multipleLists.append(cfw_autocompleteCreateItemList(inputField, current));
    }
    
    autocompleteWrapper.append(multipleLists);
	
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
	 
	 
	$(inputField).parent().append(autocompleteWrapper);
	
}

/**************************************************************************************
 * Creates a list for the autocomplete multilist.
 * @param inputField domElement to show the autocomplete for.
 * @param values an array containing elements like:
 *    {"value": "Value0", "label": "Label0", "description": "some description or null" }
 * 
 *************************************************************************************/
function cfw_autocompleteCreateItemList(targetInputField, values){
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
	itemList.addClass("autocomplete-list flex-fill");
			
	//----------------------------
	// Iterate values object
	for (var key in values) {
		
	   	var currentValue = values[key].value;
	   	var label = ""+values[key].label;
		var description = values[key].description;	
		// Methods: exchange | replace
		var method = values[key].method;	
		//----------------------------
		// Create Item
		var item = $('<div class="autocomplete-item">');
		
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
					
				}else if(method.startsWith('replace:')){
					var stringToReplace = method.substring('replace:'.length);
					var tempValue = targetInputField.value;
					tempValue = tempValue.substring(0, tempValue.lastIndexOf(stringToReplace));
					targetInputField.value = tempValue + itemValue;
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
			
			if(isNaN(valueA)) valueA = 9999999;
			if(isNaN(valueB)) valueB = 9999999;
			
			
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
	  return new CFWDate(epoch).getDateFormatted('YYYY-MM-DD HH:mm:ss');
  }
  
  return "";
}

/**************************************************************************************
 * Create a date string
 * @param epoch unix epoch milliseconds since 01.01.1970
 * @return date as string
 *************************************************************************************/
function cfw_format_epochToDate(epoch){
  var a = new Date(epoch);
  var year 		= a.getFullYear();
  var month 	= a.getMonth()+1 < 10 	? "0"+(a.getMonth()+1) : a.getMonth()+1;
  var day 		= a.getDate() < 10 		? "0"+a.getDate() : a.getDate();

  var time = year + '-' + month + '-' + day ;
  return time;
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_fieldNameToLabel(fieldName){
	
 	var regex = /[-_]/;
	var splitted = fieldName.split(regex);
	
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
 *************************************************************************************/
function cfw_format_formToParams(formOrID){
	
	var paramsObject = cfw_format_formToObject(formOrID);

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
 *************************************************************************************/
function cfw_format_formToObject(formOrID){
	
	var paramsArray = cfw_format_formToArray(formOrID);
	
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
 *************************************************************************************/
function cfw_format_formToArray(formOrID){
	
	var paramsArray = $(formOrID).serializeArray();
	
	//---------------------------
	// Convert String true/false to boolean
	for(let i in paramsArray){
		let current = paramsArray[i].value;
		if(typeof current == 'string'){
			if(!(current === '') && !isNaN(current)){
				paramsArray[i].value = Number(current);
			}else if(current.toLowerCase() === 'true'){
				paramsArray[i].value = true;
			}else if(current.toLowerCase() === 'false'){
				paramsArray[i].value = false;
			}
		}
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
function cfw_objectToHTMLList(object){
	
	var htmlString = '<ul>';
	
	if(Array.isArray(object)){
		for(var i = 0; i < object.length; i++ ){
			var currentItem = object[i];
			if(typeof currentItem == "object"){
				htmlString += '<li><strong>Object:&nbsp;</strong>'
					+ cfw_objectToHTMLList(currentItem)
				+'</li>';
			}else{
				htmlString += '<li>'+currentItem+'</li>';
			}
			
		}
	}else if(typeof object == "object"){
		
		for(var key in object){
			var currentValue = object[key];
			if(typeof currentValue == "object"){
				htmlString += '<li><strong>'+CFW.format.fieldNameToLabel(key)+':&nbsp;</strong>'
					+ cfw_objectToHTMLList(currentValue)
				+'</li>';
				
			}else{
				htmlString += '<li><strong>'+CFW.format.fieldNameToLabel(key)+':&nbsp;</strong>'
					+ currentValue
				+'</li>';
			}
			
		}
	}
	htmlString += '</ul>';
	
	return htmlString;
		
}


/**************************************************************************************
 * Create a table of contents for the h-elements on the page.
 * @param contentAreaSelector the jQuery selector for the element containing all 
 * the headers to show in the table of contents
 * @param targetSelector the jQuery selector for the resulting element
 * @param headerTag the tag used for the Table Header e.g. h1, h2, h3 (default h1)
 * @return nothing
 *************************************************************************************/
function cfw_table_toc(contentAreaSelector, resultSelector, headerTag){
	
	var target = $(resultSelector);
	var headers = $(contentAreaSelector).find("h1:visible, h2:visible, h3:visible, h4:visible, h5:visible, h6:visible");
	
	var h = "h1";
	if(headerTag != null){
		h = headerTag;
	}
	//------------------------------
	//Loop all visible headers
	var currentLevel = 1;
	var resultHTML = '<'+h+'>Table of Contents</'+h+'><ul>';
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
		resultHTML += '<li><a href="#toc_anchor_'+i+'">'+head.innerHTML+'</li>';
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
 *  CFW.ui.toogleLoader(true);
 *	window.setTimeout( 
 *	  function(){
 *	    // Do your stuff
 *	    CFW.ui.toogleLoader(false);
 *	  }, 100);
 *
 * @param isVisible true or false
 ******************************************************************************/
function cfw_toogleLoader(isVisible){
	
	var loader = $("#cfw-loader");
	
	if(loader.length == 0){
		loader = $('<div id="cfw-loader">'
				+'<div>'
					+'<i class="fa fa-cog fa-spin fa-3x fa-fw margin-bottom"></i>'
					+'<p class="m-0">Loading...</p>'
				+'</div>'
			+'</div>');	
		
//		loader.css("position","absolute");
//		loader.css("top","50%");
//		loader.css("left","50%");
//		loader.css("transform","translateX(-50%) translateY(-50%);");
//		loader.css("visibility","hidden");
		
		$("body").append(loader);
	}
	if(isVisible){
		loader.css("display", "flex");
	}else{
		loader.css("display", "none");
	}
	
}

/**************************************************************************************
 * Add an alert message to the message section.
 * Ignores duplicated messages.
 * @param type the type of the alert: INFO, SUCCESS, WARNING, ERROR
 * @param message
 *************************************************************************************/
function cfw_addAlertMessage(type, message){
	
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
function cfw_addToast(toastTitle, toastBody, style, delay){
	
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
		
	var toastHTML = '<div class="toast bg-'+clazz+' text-light" role="alert" aria-live="assertive" aria-atomic="true" data-animation="true" '+autohide+'>'
			+ '  <div class="toast-header bg-'+clazz+' text-light">'
			//+ '	<img class="rounded mr-2" alt="...">'
			+ '	<strong class="mr-auto">'+toastTitle+'</strong>'
			//+ '	<small class="text-muted">just now</small>'
			+ '	<button type="button" class="ml-2 mb-auto close" data-dismiss="toast" aria-label="Close">'
			+ '	  <span aria-hidden="true">&times;</span>'
			+ '	</button>'
			+ '  </div>';
	
	if(toastBody != null){
		toastHTML += '  <div class="toast-body">'+ toastBody+'</div>';	
	}
	toastHTML += '</div>';
	
	var toast = $(toastHTML);
	
	toastDiv.append(toast);
	toast.toast('show');
}

/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @return nothing
 *************************************************************************************/
function cfw_showModal(modalTitle, modalBody, jsCode){
	
	var modalID = 'cfw-default-modal';
	
	var defaultModal = $("#"+modalID);
	if(defaultModal.length == 0){
	
		defaultModal = $(
				'<div id="'+modalID+'" class="modal fade"  tabindex="-1" role="dialog">'
				+ '  <div class="modal-dialog modal-lg" role="document">'
				+ '    <div class="modal-content">'
				+ '      <div class="modal-header">'
				+ '        <h3 class="modal-title">Title</h3>'
				+ '        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times</span></button>'
				+ '      </div>'
				+ '      <div class="modal-body" >'
				+ '      </div>'
				+ '      <div class="modal-footer">'
				+ '         <button type="button" class="btn btn-sm btn-primary" data-dismiss="modal">Close</button>'
				+ '      </div>'
				+ '    </div>'
				+ '  </div>'
				+ '</div>');
		
		defaultModal
		.modal()
		.draggable({
		    handle: ".modal-header"
		}); 
		
		// remove position relative to show content in background
		defaultModal.css('position', '');
		
		// Prevent other modals to close the modal
//		defaultModal.on('hidden.bs.modal', function (e) {
//			  console.log('e.target.id'+e.target.id);
//			  console.log('modalID'+modalID);
//			  console.log(e.target);
//			  if(e.target.id != modalID) { // ignore events which are raised from child modals
//			    return false;
//			  }
//			  // your code
//			});
		
//		defaultModal.modal({
//		    backdrop: 'static',
//		    keyboard: false
//		});
		$('body').prepend(defaultModal);
		
		//-------------------------------------
		// Reset Scroll Position
		defaultModal.on('hide.bs.modal', function () {
			defaultModal.find('.modal-body').scrollTop(0);
		});	

	}

	//---------------------------------
	// Add Callback
	if(jsCode != null){
		defaultModal.on('hidden.bs.modal', function () {
			cfw_utils_executeCodeOrFunction(jsCode);
			$("#"+modalID).off('hidden.bs.modal');
		});	
	}
	
	//---------------------------------
	// ShowModal
	defaultModal.find(".modal-title").html("").append(modalTitle);
	defaultModal.find('.modal-body').html("").append(modalBody);
	
	defaultModal.modal('show');

}

/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @return nothing
 *************************************************************************************/
function cfw_showSmallModal(modalTitle, modalBody, jsCode){
	
	var modalID = 'cfw-small-modal';
	
	var smallModal = $("#"+modalID);
	if(smallModal.length == 0){
	
		smallModal = $(
				'<div id="'+modalID+'" class="modal fade"  tabindex="-1" role="dialog">'
				+ '  <div class="modal-dialog" role="document">'
				+ '    <div class="modal-content">'
				+ '      <div class="modal-header p-2">'
				+ '        <h4 class="modal-title">Title</h4>'
				+ '        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times</span></button>'
				+ '      </div>'
				+ '      <div class="modal-body">'
				+ '      </div>'
				+ '      <div class="modal-footer  p-2">'
				+ '         <button type="button" class="btn btn-primary btn-sm" data-dismiss="modal">Close</button>'
				+ '      </div>'
				+ '    </div>'
				+ '  </div>'
				+ '</div>');

		smallModal.modal();
		
		smallModal.draggable({
			backdrop: false,
		    handle: ".modal-header"
		}); 
		
		// remove position relative to show content in background
		smallModal.css('position', '');
		
//		smallModal.modal({
//		    backdrop: 'static',
//		    keyboard: false
//		});
		$('body').prepend(smallModal);
		
	}

	//---------------------------------
	// Add Callback
	if(jsCode != null){

		smallModal.on('hidden.bs.modal', function () {
			cfw_utils_executeCodeOrFunction(jsCode);
			$("#"+modalID).off('hidden.bs.modal');
		});	
	}
	
	//---------------------------------
	// Show Modal
	smallModal.find(".modal-title").html("").append(modalTitle);
	smallModal.find('.modal-body').html("").append(modalBody);
	
	smallModal.modal('show');
}

/**************************************************************************************
 * Create a model with content.
 * @param modalID the ID of the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @return nothing
 *************************************************************************************/
function cfw_reopenModal(modalID){
	
	var modal = $("#"+modalID);
	
	if(modal.length != 0){
		modal.modal('show');
	}
		
}

/**************************************************************************************
 * Show a modal with support Info
 *************************************************************************************/
function cfw_showSupportInfoModal(){
		
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
	cfw_showModal("Support Info", modalContent);
}

/**************************************************************************************
 * Create a confirmation modal panel that executes the function passed by the argument
 * @param message the message to show
 * @param confirmLabel the text for the confirm button
 * @param jsCode the javascript to execute when confirmed
 * @return nothing
 *************************************************************************************/
function cfw_confirmExecution(message, confirmLabel, jsCodeOrFunction){
	
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
	closeButton.attr('onclick', 'cfw_confirmExecution_Execute(this, \'cancel\')');
	closeButton.data('modalID', modalID);
	
	var cancelButton = $('<button type="button" class="btn btn-primary">Cancel</button>');
	cancelButton.attr('onclick', 'cfw_confirmExecution_Execute(this, \'cancel\')');
	cancelButton.data('modalID', modalID);
	
	var confirmButton = $('<button id="cfw-confirmButton" type="button" class="btn btn-primary">'+confirmLabel+'</button>');
	confirmButton.attr('onclick', 'cfw_confirmExecution_Execute(this, \'confirm\')');
	confirmButton.data('modalID', modalID);
	confirmButton.data('jsCode', jsCodeOrFunction);
	
	modal.find('.modal-header').append(closeButton);
	modal.find('.modal-footer').append(cancelButton).append(confirmButton);
	
	modal.modal('show');

}


function cfw_confirmExecution_Execute(sourceElement, action){
	
	var $source = $(sourceElement);
	var modalID = $source.data('modalID');
	var jsCode = $source.data('jsCode');
	
	var modal = $('#'+modalID);
	
	if(action == 'confirm'){
		CFW.utils.executeCodeOrFunction(jsCode);
	}
	
	//remove modal
	modal.modal('hide');
	modal.remove();
	$('.modal-backdrop').remove();
	$('body').removeClass('modal-open');
	modal.remove();
}

/******************************************************************
 * Get a cookie by ots name
 * @param 
 * @return object
 ******************************************************************/
function cfw_readCookie(name) {
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
 * @param 
 * @return object
 ******************************************************************/
function cfw_getURLParamsDecoded()
{
    var vars = {};
    
    var keyValuePairs = [];
    if ( window.location.href.indexOf('?') > -1){
    	keyValuePairs = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    }
    
    for(let i = 0; i < keyValuePairs.length; i++)
    {
        var splitted = keyValuePairs[i].split('=');
        var key = cfw_secureDecodeURI(splitted[0])
        vars[key] = cfw_secureDecodeURI(splitted[1]);
    }
    
    return vars;
}

/******************************************************************
 * Reads the parameters from the URL and returns an object containing
 * name/value pairs like {"name": "value", "name2": "value2" ...}.
 * @param 
 * @return object
 ******************************************************************/
function cfw_getURLParams()
{
    var vars = {};
    
    var keyValuePairs = [];
    if ( window.location.href.indexOf('?') > -1){
    	keyValuePairs = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    }
    
    for(let i = 0; i < keyValuePairs.length; i++)
    {
        var splitted = keyValuePairs[i].split('=');
        vars[splitted[0]] = splitted[1];
    }
    
    return vars;
}

/******************************************************************
 * Reads the parameters from the URL and returns an object containing
 * name/value pairs like {"name": "value", "name2": "value2" ...}.
 * @param 
 * @return object
 ******************************************************************/
function cfw_setURLParam(name, value){

	//------------------------------
	// Set or replace param value
	var params = cfw_getURLParams();
    params[name] = encodeURIComponent(value);
    
	//------------------------------
	// Create Query String
    var queryString = "";
    for(var key in params)
    {
    	queryString = queryString + key +"="+params[key]+"&";
    }
    //Remove last '&'
    queryString = queryString.substring(0, queryString.length-1);
    
	//------------------------------
	// Recreate URL
    var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?'+queryString;
    window.history.pushState({ path: newurl }, '', newurl);

    
}

/**************************************************************************************
 * Tries to decode a URI and handles errors when they are thrown.
 * If URI cannot be decoded the input string is returned unchanged.
 * 
 * @param uri to decode
 * @return decoded URI or the same URI in case of errors.
 *************************************************************************************/
function cfw_secureDecodeURI(uri){
	var decoded;
	try{
		decoded = decodeURIComponent(uri);
	}catch(err){
		decoded = uri;
	}
	
	return decoded;
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
function cfw_handleMessages(response){
	
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
function cfw_getJSON(url, params, callbackFunc){

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
			  cfw_handleMessages(response);
			  //callbackFunc(response);
		  })
		  .always(function(response) {
			  cfw_handleMessages(response);
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
function cfw_postJSON(url, params, callbackFunc){

	$.post(url, params)
		  .done(function(response, status, xhr) {
		    //alert( "done" );
			  if(callbackFunc != null) callbackFunc(response, status, xhr);
		  })
		  .fail(function(xhr, status, errorThrown) {
			  CFW.ui.addToast("Request failed", "URL: "+url, "danger", CFW.config.toastErrorDelay);
			  var response = JSON.parse(xhr.responseText);
			  cfw_handleMessages(response);
			  //callbackFunc(response);
		  })
		  .always(function(response) {
			  cfw_handleMessages(response);
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
function cfw_getForm(formid, targetElement){

	$.get('/cfw/formhandler', {id: formid})
		  .done(function(response) {
			  if(response.payload != null){
				  $(targetElement).html(response.payload.html);
				  var form = $(targetElement).find('form')
			      var formID = $(targetElement).find('form').attr("id");
			      // workaround, force evaluation
			      eval($(form).find("script").text());
	              eval("intializeForm_"+formID+"();");
			  }
		  })
		  .fail(function(xhr, status, errorThrown) {
			  console.error("Request failed: "+url);
			  CFW.ui.addToast("Request failed", "URL: "+url, "danger", CFW.config.toastErrorDelay)
			  var response = JSON.parse(xhr.responseText);
			  cfw_handleMessages(response);
		  })
		  .always(function(response) {
			  cfw_handleMessages(response);			  
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
function cfw_createForm(url, params, targetElement, callback){

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
			  cfw_handleMessages(response);			  
		  });
}

/**************************************************************************************
 * Calls a rest service that creates a form and returns a standard json format,
 * containing the html of the form in the payload.
 * 
 * @param url to call
 * @param params to pass
 * @param targetElement the element in which the form should be placed
 *************************************************************************************/
function cfw_postForm(url, formID, callback){
	// switch summernote to wysiwyg view, at it will not save in code view
	$('.btn-codeview.active').click();
	
	$(formID+'-submitButton').prepend('<i class="fa fa-cog fa-spin fa-fw mr-1 loaderMarker"></i>')
	
	window.setTimeout( 
		function(){
			cfw_postJSON(url, CFW.format.formToParams(formID), function (data,status,xhr){
				$(formID+'-submitButton .loaderMarker').remove();
				if(callback != null){
					callback(data,status,xhr);
				}
			})
		},
	50);
	
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
function cfw_fetchAndCacheData(url, params, key, callback){
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
function cfw_getHostURL() {
	return location.protocol+'//'+location.hostname+(location.port ? ':'+location.port: '');
};


/**************************************************************************************
* Function to store a value in the local storage.
* @param key name of the cookie
* @param value value of the cookie
* @param exdays number of days after which the cookie gets deleted
 *************************************************************************************/
function cfw_storeValue(key, value) {
	window.localStorage.setItem("cfw-"+key, value);
};

/**************************************************************************************
* Function to get a cookie
* @param key name of the cookie to get
* @return either the value of the cookie or an empty string
 *************************************************************************************/
function cfw_retrieveValue(key, defaultValue) {
	
	var item = window.localStorage.getItem("cfw-"+key);
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
function cfw_removeFromCache(key){
	CFW.cache.data[key] = null;
}

/******************************************************************
 * Method to remove all the data in the cache.
 *
 * @return nothing
 *
 ******************************************************************/
function cfw_clearCache(){
	CFW.cache.data = {};
}

/**************************************************************************************
 * Select all the content of the given element.
 * For example to select everything inside a given DIV element using 
 * <div onclick="selectElementContent(this)">.
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
	cfw_fetchAndCacheData("/app/usermanagement/permissions", null, "userPermissions")
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
	cfw_fetchAndCacheData("./usermanagement/permissions", null, "userPermissions")
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
function cfw_registerRenderer(rendererUniqueName, rendererObject){
	CFW.render.registry[rendererUniqueName] = rendererObject;
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_getRenderer(rendererUniqueName){
	return CFW.render.registry[rendererUniqueName];
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_loadLocalization(){
	//-----------------------------------
	// 
	if(CFW.cache.lang == null){
		$.ajaxSetup({async: false});
			cfw_getJSON("/cfw/locale", {id: JSDATA.localeIdentifier}, function(data, status, xhr){
				
				if (xhr.status == 200){
					window.localStorage.setItem("lang-"+JSDATA.localeIdentifier, JSON.stringify(data.payload) );
					CFW.cache.lang = data.payload;
				}else if (xhr.status == 304){
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
		autocompleteCounter: 0,
		autocompleteFocus: -1,
		isLocaleFetching: null,
		lastServerAccess: moment(),
	},
	lang: {
		get: cfw_lang,
		loadLocalization: cfw_loadLocalization,
	},
	colors: {
		randomRGB: cfw_colors_randomRGB,
		randomHSL: cfw_colors_randomHSL,
		getThresholdStyle: cfw_colors_getThresholdStyle,
	},
	config: {
		toastDelay: 	 3000,
		toastErrorDelay: 10000
	},
	cache: { 
		data: {},
		lang: null,
		removeFromCache: cfw_removeFromCache,
		clearCache: cfw_clearCache,
		storeValue: cfw_storeValue,
		retrieveValue: cfw_retrieveValue
	},
	render: {
		registry: {},
		registerRenderer: cfw_registerRenderer,
		getRenderer: cfw_getRenderer,
	},
	array: {
		sortArrayByValueOfObject: cfw_sortArrayByValueOfObject
	},
	format: {
		epochToTimestamp: 	cfw_format_epochToTimestamp,
		epochToDate: 		cfw_format_epochToDate,
		objectToHTMLList: 	cfw_objectToHTMLList,
		csvToObjectArray: 	cfw_format_csvToObjectArray,
		fieldNameToLabel: 	cfw_format_fieldNameToLabel,
		capitalize: 		cfw_format_capitalize,
		formToParams: 		cfw_format_formToParams,
		formToArray: 		cfw_format_formToArray,
		formToObject: 		cfw_format_formToObject
	},
	
	http: {
		readCookie: cfw_readCookie,
		getURLParams: cfw_getURLParams,
		getURLParamsDecoded: cfw_getURLParamsDecoded,
		setURLParam: cfw_setURLParam,
		getHostURL: cfw_getHostURL,
		secureDecodeURI: cfw_secureDecodeURI,
		getJSON: cfw_getJSON,
		postJSON: cfw_postJSON,
		getForm: cfw_getForm,
		createForm: cfw_createForm,
		fetchAndCacheData: cfw_fetchAndCacheData
	},
	
	selection: {
		selectElementContent: cfw_selectElementContent
	},
	utils: {
		executeCodeOrFunction: cfw_utils_executeCodeOrFunction,
		randomString: cfw_utils_randomString,
		chainedOnload: cfw_utils_chainedOnload,
		isNullOrEmpty: cfw_utils_isNullOrEmpty,
		nullTo: cfw_utils_nullTo,
		replaceAll: cfw_utils_replaceAll,
		randomInt: cfw_utils_randomInt,
	},
	ui: {
		createToggleButton: cfw_createToggleButton,
		toc: cfw_table_toc,
		addToast: cfw_addToast,
		addToastInfo: function(text){cfw_addToast(text, null, "info", CFW.config.toastDelay);},
		addToastSuccess: function(text){cfw_addToast(text, null, "success", CFW.config.toastDelay);},
		addToastWarning: function(text){cfw_addToast(text, null, "warning", CFW.config.toastDelay);},
		addToastDanger: function(text){cfw_addToast(text, null, "danger", CFW.config.toastErrorDelay);},
		showModal: cfw_showModal,
		showSmallModal: cfw_showSmallModal,
		confirmExecute: cfw_confirmExecution,
		toogleLoader: cfw_toogleLoader,
		addAlert: cfw_addAlertMessage
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
			cfw_reopenModal('cfw-default-modal');
			return;
		}
		
		//--------------------------------
		// Ctrl+Alt+I - Show Support Info
		if (e.ctrlKey && event.altKey &&  e.keyCode == 73) {
			cfw_showSupportInfoModal();
			return;
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
				CFW.ui.showSmallModal("Session Timeout", '<p>Your session has timed out, please refresh your browser.</p>');
				window.clearInterval(CFW.global.sessionCheckInterval);
			}
			
		}, 30000);
		
	}
	//-----------------------------------
	// Add scrolling offset for menu bar
	$( window ).on('hashchange', function (e){
		console.log('change');
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
	  
	//-----------------------------------
	// Highlight Code Blocks
	$(document).ready(function() {
		$('pre code').each(function(i, block) {
		    hljs.highlightBlock(block);
		});
	});
})