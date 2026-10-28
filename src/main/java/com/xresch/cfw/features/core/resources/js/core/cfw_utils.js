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

/************************************************************************************************************
 * This class compares two JSON arrays containing objects.
 *
 * Based on CFWQueryCommandResultCompareMethods.java
 * @license MIT-License
 ************************************************************************************************************/
class CFWCompareObjectArrays {

    constructor() {
        this._identifierFields = null;
        this._toplevelField = null;

        this._labelOld = "_A";
        this._labelYoung = "_B";
        this._labelDiff = "_Diff";
        this._labelDiffPercent = "_%";
        this._thresholdPercentGreen = -15;
        this._thresholdPercentRed = 15;
        this._precision = 1; // number of decimals points to display

        this.detectedFields = new Set();
        this.fieldHasPercentage = new Set();

        this._makeIdentifierUnique = true;

        this._compareNumbersAbsolute = true;
        this._compareNumbersDiffPercent = true;
        this._compareBooleans = true;
        this._compareStrings = true;
        this._compareArrays = true;
        this._compareObjects = true;
        this._doSort = false;
    }

    /***********************************************************************************************
     * Set the fields used for identifying a record.
     ***********************************************************************************************/
    identifierFields(fields) {
        this._identifierFields = fields;
        return this;
    }

    /***********************************************************************************************
     * Set whether duplicate identifiers should be made unique.
     ***********************************************************************************************/
    makeIdentifierUnique(value) {
        this._makeIdentifierUnique = value;
        return this;
    }

    /***********************************************************************************************
     * Set the label used as a postfix for the columns of the older data.
     ***********************************************************************************************/
    labelOld(label) {
        this._labelOld = label;
        return this;
    }

    /***********************************************************************************************
     * Set the label used as a postfix for the columns of the younger data.
     ***********************************************************************************************/
    labelYoung(label) {
        this._labelYoung = label;
        return this;
    }

    /***********************************************************************************************
     * Set the label used as a postfix for the columns of difference.
     ***********************************************************************************************/
    labelDiff(label) {
        this._labelDiff = label;
        return this;
    }

    /***********************************************************************************************
     * Set the label used as a postfix for the columns of percent difference.
     ***********************************************************************************************/
    labelDiffPercent(label) {
        this._labelDiffPercent = label;
        return this;
    }
	
	/***********************************************************************************************
	 * Set the threshhold of change in percent to highlight a percentage diff in green.
	 * Used for customizers only.
	 ***********************************************************************************************/
	thresholdPercentGreen(value) {
	    this._thresholdPercentGreen = value;
	    return this;
	}
	
	/***********************************************************************************************
	 * Set the threshhold of change in percent to highlight a percentage diff in green.
	 * Used for customizers only.
	 ***********************************************************************************************/
	thresholdPercentRed(value) {
	    this._thresholdPercentRed = value;
	    return this;
	}
	
	/***********************************************************************************************
	 * Set the precision of decimal points shown in numbers.
	 * Used for customizers only.
	 ***********************************************************************************************/
	precision(value) {
	    this._precision = value;
	    return this;
	}

    /***********************************************************************************************
     * Set if numeric values should have absolute comparison.
     ***********************************************************************************************/
    doCompareNumbersAbsolute(value) {
        this._compareNumbersAbsolute = value;
        return this;
    }

    /***********************************************************************************************
     * Set if numeric values should have percentage difference comparison.
     ***********************************************************************************************/
    doCompareNumbersDiffPercent(value) {
        this._compareNumbersDiffPercent = value;
        return this;
    }

    /***********************************************************************************************
     * Set if boolean values should be compared.
     ***********************************************************************************************/
    doCompareBooleans(value) {
        this._compareBooleans = value;
        return this;
    }

    /***********************************************************************************************
     * Set if string values should be compared.
     ***********************************************************************************************/
    doCompareStrings(value) {
        this._compareStrings = value;
        return this;
    }

    /***********************************************************************************************
     * Set if array values should be compared.
     ***********************************************************************************************/
    doCompareArrays(value) {
        this._compareArrays = value;
        return this;
    }

    /***********************************************************************************************
     * Set if object values should be compared.
     ***********************************************************************************************/
    doCompareObjects(value) {
        this._compareObjects = value;
        return this;
    }

    /***********************************************************************************************
     * Set the field used for top-level comparison.
     ***********************************************************************************************/
    toplevelFieldField(field) {
        this._toplevelField = field;
        return this;
    }

    /***********************************************************************************************
     * Compare two JSON arrays and return the result in the requested format.
     *
     * @param {Array<Object>} olderArray
     * @param {Array<Object>} youngerArray
     * @returns {Object} Comparison result and field lists
     ***********************************************************************************************/
    compare(olderArray, youngerArray) {

        //=====================================================
        // Initialize Variables
        //=====================================================

        this.detectedFields = new Set();
        this.fieldHasPercentage = new Set();

        olderArray = Array.isArray(olderArray) ? olderArray : [];
        youngerArray = Array.isArray(youngerArray) ? youngerArray : [];

		if(this.identifierFields == null || this.identifierFields == undefined){
        	this.identifierFields = [];
		}
		
        // Find an identifier field if none was provided.
        this.addFirstMemberNameFromArray(
            this.identifierFields,
            youngerArray
        );

        //=====================================================
        // Compare Records
        //=====================================================

        const data = this.doCompareDefault(olderArray, youngerArray);

        //=====================================================
        // Initialize Field Lists
        //=====================================================

		
        const fieldsAll = [];
        const fieldsOlder = [];
        const fieldsYounger = [];
        const fieldsDiff = [];
        const fieldsPercent = [];
		const labels = {};
		const customizers = {};
		
		const precision = this._precision;
		const tGreen = this._thresholdPercentGreen;
		const tRed = this._thresholdPercentRed;
		
/*		const customizerGeneral = function(record, value){
			if(value == null){	return value; }

			if(! isNaN(value) ){
				let fixed = Number(value);
				fixed = CFW.format.numberSeparators(fixed);
				
				return  $('<div class="w-100 text-right pr-2">'+fixed+'</div>');;
			}
			
			return value;
		}*/
		
		const cssClasses = " w-100 text-right font-weight-bold px-2";
		
		const customizerDiff = function(record, value){
			if(value == null){ 						return value; }
			if(value == true  || value == "true"){ 	return `<div class="${cssClasses} text-white bg-cfw-green">true</div>`; }
			if(value == false || value == "false"){ return `<div class="${cssClasses} text-white bg-cfw-red">false</div>`; }
			
			if(! isNaN(value) ){
				let fixed = Number(value).toFixed(precision);
				fixed = CFW.format.numberSeparators(fixed);
				if(value > 0){ fixed = "+"+fixed; }
				
				const span = $('<div class="'+ cssClasses+'">'+fixed+'</div>');
					
				var style = CFW.colors.getSplitThresholdStyle(value, tGreen, tRed, false);
				style = (style == CFW.style.notevaluated) ? "cfw-none" : style;

				CFW.colors.colorizeElement(span, style, "bg", "2px");
				return span;
			}
			
			return value;
		}
		
		const customizerDiffPercent = function(record, value){
			if(value == null){ 						return value; }

			if(! isNaN(value) ){
				let fixed = Number(value).toFixed(precision) + "%";
				fixed = CFW.format.numberSeparators(fixed);
				if(value > 0){ fixed = "+"+fixed; }
				const span = $('<div class="'+ cssClasses+'">'+fixed+'</div>');
					
				var style = CFW.colors.getSplitThresholdStyle(value, tGreen, tRed, false);
				style = (style == CFW.style.notevaluated) ? "cfw-none" : style;

				if(style != "cfw-none"){
					CFW.colors.colorizeElement(span, "cfw-white", "text", "2px");
				}
				CFW.colors.colorizeElement(span, style, "bg", "2px");
				return span;
			}
			
			return value;
		}
        // Keep the field order from the generated result.
        for (const field of this.detectedFields) {

            fieldsAll.push(field);
			labels[field] = field;
			
			if (field.endsWith(this._labelDiff)) {
			    fieldsDiff.push(field);
				customizers[field] = customizerDiff;
				continue;
			}

			if (field.endsWith(this._labelDiffPercent)) {
			    fieldsPercent.push(field);
				customizers[field] = customizerDiffPercent;
				continue;
			}

            if (field.endsWith(this._labelOld)) { fieldsOlder.push(field); }
            if (field.endsWith(this._labelYoung)) { fieldsYounger.push(field); }

			//customizers[field] = customizerGeneral;
        }

        return {
            data,
            fieldsAll,
            fieldsOlder,
            fieldsYounger,
            fieldsDiff,
            fieldsPercent,
			labels,
			customizers
        };
    }

    /***********************************************************************************************
     * If the fieldname array is empty, find the first member name from
     * the first object encountered in the given array.
     ***********************************************************************************************/
    addFirstMemberNameFromArray(fieldnameArray, objectArray) {

        if (fieldnameArray.length === 0 && objectArray.length > 0) {

            // Find a JSON object in the first 100 entries.
            for (let i = 0; i < objectArray.length && i <= 100; i++) {

                const element = objectArray[i];

                if (
                    element !== null &&
                    typeof element === "object" &&
                    !Array.isArray(element)
                ) {
                    const firstField = Object.keys(element)[0];

                    if (firstField !== undefined) {
                        fieldnameArray.push(firstField);
                    }

                    break;
                }
            }
        }
    }

    /***********************************************************************************************
     * Create a record ID from the configured identifier fields.
     ***********************************************************************************************/
    getRecordID(idFieldnames, object) {

        let recordID = "";

        for (const idField of idFieldnames) {

            if (Object.prototype.hasOwnProperty.call(object, idField)) {
                recordID += JSON.stringify(object[idField]) + "_";
            }
        }

        return recordID;
    }

    /***********************************************************************************************
     * Compare the two arrays using the configured identifier fields.
     ***********************************************************************************************/
    doCompareDefault(olderArray, youngerArray) {

        //=====================================================
        // Initialize Variables
        //=====================================================

        const comparableDataMap = new Map();

        //---------------------------------
        // Add All Older Results
        for (const olderObject of olderArray) {

            if (
                olderObject !== null &&
                typeof olderObject === "object" &&
                !Array.isArray(olderObject)
            ) {
                const recordID = this.getRecordID(
                    this._identifierFields,
                    olderObject
                );

                if (!comparableDataMap.has(recordID)) {
                    comparableDataMap.set(recordID, {
                        olderArray: [],
                        youngerArray: []
                    });
                }

                comparableDataMap.get(recordID).olderArray.push(olderObject);
            }
        }

        //---------------------------------
        // Add All Younger Results
        for (const youngerObject of youngerArray) {

            if (
                youngerObject !== null &&
                typeof youngerObject === "object" &&
                !Array.isArray(youngerObject)
            ) {
                const recordID = this.getRecordID(
                    this._identifierFields,
                    youngerObject
                );

                if (!comparableDataMap.has(recordID)) {
                    comparableDataMap.set(recordID, {
                        olderArray: [],
                        youngerArray: []
                    });
                }

                comparableDataMap.get(recordID).youngerArray.push(youngerObject);
            }
        }

        //---------------------------------
        // Sort record IDs like the Java TreeMap.
        // This makes the result independent of the input order of distinct IDs.
        const sortedEntries = [...comparableDataMap.entries()]
            .sort((a, b) => {
                if (a[0] < b[0]) return -1;
                if (a[0] > b[0]) return 1;
                return 0;
            });

        //---------------------------------
        // Compare all records with the same ID.
        const compareResult = [];

        for (const [, comparableData] of sortedEntries) {

            const size = Math.max(
                comparableData.olderArray.length,
                comparableData.youngerArray.length
            );

            for (let i = 0; i < size; i++) {

				const olderObject = ( comparableData.olderArray[i] != null ) ? comparableData.olderArray[i] : null;
				const youngerObject = ( comparableData.youngerArray[i] != null ) ? comparableData.youngerArray[i] : null;

				const iteratorObject = youngerObject != null ? youngerObject : olderObject;

                const resultObject = {};

                //---------------------------------
                // Iterate Object Members
                for (const fieldname of Object.keys(iteratorObject)) {

                    const youngValue = youngerObject !== null
                        ? youngerObject[fieldname]
                        : null;

                    const oldValue = olderObject !== null
                        ? olderObject[fieldname]
                        : null;

                    //---------------------------------
                    // If Identifier, just add without comparison.
                    if (this._identifierFields.includes(fieldname)) {

                        let finalValue = youngValue;

                        if (finalValue == null) {
                            finalValue = oldValue;
                        }

                        // Add an index for duplicate identifiers.
                        if (i === 0 || !this._makeIdentifierUnique) {
                            resultObject[fieldname] = (finalValue != null) ? finalValue : null;
                        } else {
                            const indexedValue = finalValue == null
                                ? "null"
                                : String(finalValue);

                            resultObject[fieldname] =
                                indexedValue + "[" + (i + 1) + "]";
                        }

                        continue;
                    }

                    //---------------------------------
                    // Add Original Values
					resultObject[fieldname + this._labelOld] = oldValue != null ? oldValue : null;
					resultObject[fieldname + this._labelYoung] = youngValue != null ? youngValue : null;

                    //---------------------------------
                    // Ensure Column Order
                    if (this._compareNumbersAbsolute) {
                        resultObject[fieldname + this._labelDiff] = null;
                    }

                    if (this._compareNumbersDiffPercent) {
                        resultObject[fieldname + this._labelDiffPercent] = null;
                    }

                    //---------------------------------
                    // Check Nulls
                    const nullLabel = this.fieldHasPercentage.has(fieldname)
                        ? this._labelDiffPercent
                        : this._labelDiff;

                    if (youngValue == null && oldValue == null) {
                        resultObject[fieldname + nullLabel] = true;
                        continue;
                    }

                    if (youngValue == null || oldValue == null) {
                        resultObject[fieldname + nullLabel] = false;
                        continue;
                    }

                    //---------------------------------
                    // Check Primitives
                    const youngType = typeof youngValue;
                    const oldType = typeof oldValue;

                    const youngPrimitive =
                        youngType === "string" ||
                        youngType === "number" ||
                        youngType === "boolean";

                    const oldPrimitive =
                        oldType === "string" ||
                        oldType === "number" ||
                        oldType === "boolean";

                    if (youngPrimitive && oldPrimitive) {

                        const oldNumber = this.parseNumber(oldValue);
                        const youngNumber = this.parseNumber(youngValue);

                        //--------------------
                        // Numbers
                        if (oldNumber !== null && youngNumber !== null) {

                            if (this._compareNumbersAbsolute) {
                                resultObject[fieldname + this._labelDiff] =
                                    youngNumber - oldNumber;
                            }

                            if (this._compareNumbersDiffPercent) {

                                this.fieldHasPercentage.add(fieldname);

                                if (oldNumber === 0) {

                                    if (youngNumber === 0) {
                                        resultObject[fieldname + this._labelDiffPercent] = 0;
                                    } else if (youngNumber > 0) {
                                        resultObject[fieldname + this._labelDiffPercent] = 9999999;
                                    } else {
                                        resultObject[fieldname + this._labelDiffPercent] = -9999999;
                                    }

                                } else {

                                    // Match Java BigDecimal rounding to 6 decimal places,
                                    // followed by multiplication by 100.
                                    const diff = youngNumber - oldNumber;
                                    const ratio = diff / oldNumber;

                                    const roundedRatio =
                                        Math.sign(ratio) *
                                        (Math.floor(Math.abs(ratio) * 1000000 + 0.5) / 1000000);

                                    resultObject[fieldname + this._labelDiffPercent] =
                                        roundedRatio * 100;
                                }
                            }

                            if (
                                !this._compareNumbersAbsolute &&
                                !this._compareNumbersDiffPercent
                            ) {
                                resultObject[fieldname + this._labelDiff] = null;
                            }

                            continue;
                        }

                        //--------------------
                        // Booleans
                        const oldBoolean = this.parseBoolean(oldValue);
                        const youngBoolean = this.parseBoolean(youngValue);

                        if (oldBoolean !== null && youngBoolean !== null) {

                            if (this._compareBooleans) {
                                resultObject[fieldname + this._labelDiff] =
                                    oldBoolean === youngBoolean;
                            } else {
                                resultObject[fieldname + this._labelDiff] = null;
                            }

                            continue;
                        }

                        //--------------------
                        // Compare As Strings
                        if (typeof oldValue === "string" &&
                            typeof youngValue === "string") {

                            if (this._compareStrings) {
                                resultObject[fieldname + this._labelDiff] =
                                    oldValue === youngValue;
                            } else {
                                resultObject[fieldname + this._labelDiff] = null;
                            }

                            continue;
                        }

                        //--------------------
                        // Anything Else Uncomparable
                        resultObject[fieldname + this._labelDiff] = null;
                        continue;
                    }

                    //---------------------------------
                    // Check Objects
                    if (
                        (typeof youngValue === "object" && !Array.isArray(youngValue)) ||
                        (typeof oldValue === "object" && !Array.isArray(oldValue))
                    ) {

                        if (this._compareObjects) {
                            resultObject[fieldname + this._labelDiff] =
                                this.jsonEquals(youngValue, oldValue);
                        } else {
                            resultObject[fieldname + this._labelDiff] = null;
                        }

                        continue;
                    }

                    //---------------------------------
                    // Check Arrays
                    if (Array.isArray(youngValue) || Array.isArray(oldValue)) {

                        if (this._compareArrays) {
                            resultObject[fieldname + this._labelDiff] =
                                this.jsonEquals(youngValue, oldValue);
                        } else {
                            resultObject[fieldname + this._labelDiff] = null;
                        }

                        continue;
                    }

                    //---------------------------------
                    // Fallback: Anything Else Not Comparable
                    resultObject[fieldname + this._labelDiff] = null;
                }

                compareResult.push(resultObject);

                // Collect all fields in their original insertion order.
                for (const fieldname of Object.keys(resultObject)) {
                    this.detectedFields.add(fieldname);
                }
            }
        }

        return compareResult;
    }

    /***********************************************************************************************
     * Parse a number or a numeric string.
     *
     * Returns null when the value is not numeric.
     ***********************************************************************************************/
    parseNumber(value) {

        if (typeof value === "number") {
            return Number.isFinite(value) ? value : null;
        }

        if (typeof value !== "string" || value.trim() === "") {
            return null;
        }

        // Avoid interpreting boolean strings as numbers.
        if (/^(true|false)$/i.test(value.trim())) {
            return null;
        }

        const numberPattern =
            /^[+-]?(?:(?:\d+(?:\.\d*)?)|(?:\.\d+))(?:[eE][+-]?\d+)?$/;

        if (!numberPattern.test(value.trim())) {
            return null;
        }

        const number = Number(value);

        return Number.isFinite(number) ? number : null;
    }

    /***********************************************************************************************
     * Parse a boolean or a boolean string.
     *
     * Returns null when the value is not boolean.
     ***********************************************************************************************/
    parseBoolean(value) {

        if (typeof value === "boolean") {
            return value;
        }

        if (typeof value === "string") {

            const normalized = value.trim().toLowerCase();

            if (normalized === "true") return true;
            if (normalized === "false") return false;
        }

        return null;
    }

    /***********************************************************************************************
     * Compare two JSON values.
     *
     * Objects and arrays are compared structurally rather than by reference.
     ***********************************************************************************************/
    jsonEquals(valueA, valueB) {

        if (valueA === valueB) {
            return true;
        }

        if (valueA == null || valueB == null) {
            return valueA == null && valueB == null;
        }

        if (typeof valueA !== typeof valueB) {
            return false;
        }

        if (Array.isArray(valueA) || Array.isArray(valueB)) {

            if (!Array.isArray(valueA) || !Array.isArray(valueB)) {
                return false;
            }

            if (valueA.length !== valueB.length) {
                return false;
            }

            return valueA.every((value, index) =>
                this.jsonEquals(value, valueB[index])
            );
        }

        if (typeof valueA === "object") {

            const keysA = Object.keys(valueA).sort();
            const keysB = Object.keys(valueB).sort();

            if (keysA.length !== keysB.length) {
                return false;
            }

            for (let i = 0; i < keysA.length; i++) {

                if (keysA[i] !== keysB[i]) {
                    return false;
                }

                if (!this.jsonEquals(valueA[keysA[i]], valueB[keysB[i]])) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}