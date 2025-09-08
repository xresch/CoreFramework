/**************************************************************************************************************
 * Contains the various util functions of CFW.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
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
 * Sleep by promise, use it like:
   CFW.utils.sleep(2000).then(() => {
	  //do something
   });
 *
 *************************************************************************************/
function cfw_utils_sleep(time) {
  return new Promise((resolve) => setTimeout(resolve, time));
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
 * Check if a value is true or a true string.
 ************************************************************************************************/
function cfw_utils_isTrue(value){

	if(value == true 
	|| (
		  typeof value === "string" 
		&& value.trim().toLowerCase() == "true" 
	   ) 
	){
		return true;
	}
	
	return false;
}

/************************************************************************************************
 * Creates a hash for a string.
 ************************************************************************************************/
function cfw_utils_hash(string) {

    let hash = 0;

    if (string.length == 0) return hash;

    for (i = 0; i < string.length; i++) {
        char = string.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash;
    }

    return hash;
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

/************************************************************************************************
 * Triggers a download for the given text and the specified filename.
 ************************************************************************************************/
function cfw_utils_downloadText(filename, text) {
 
  var element = document.createElement('a');
  
  element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
  element.setAttribute('download', filename);

  element.style.display = 'none';
  document.body.appendChild(element);

  element.click();

  document.body.removeChild(element);
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

	let filterContext = $(context);
	let input = $(searchField);
	
	let filter = input.val().toUpperCase();
	
	//--------------------------
	// To regex, or not to regex, that is the question
	let regex = null;
	if(filter.indexOf("*") > -1){
		regex =  new RegExp(filter.replace(/\*/g, '.*'));
	}
	
	filterContext.find(itemSelector).each(function( index ) {
		  var current = $(this);

		  if ( 
			  (regex == null && current.html().toUpperCase().indexOf(filter) > -1)
			  || (regex != null && regex.test(current.html().toUpperCase()) )
		  ){
			  
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
