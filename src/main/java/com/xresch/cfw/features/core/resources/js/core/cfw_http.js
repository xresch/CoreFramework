
/**************************************************************************************************************
 * Contains the various HTTP functions of CFW.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/

/******************************************************************
 * Get a cookie by ots name
 * @param 
 * @return object
 ******************************************************************/
function cfw_http_readCookie(name) {
    var nameEQ = name + "=";
    var cookieArray = document.cookie.split(';');
    for (let i = 0; i < cookieArray.length; i++) {
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
function cfw_http_postFormData(url, formData, callbackFunc){

	jQuery.ajax({
	    url: url,
	    data: formData,
	    cache: false,
	    contentType: false,
	    processData: false,
	    method: 'POST'
	    //type: 'POST', // For jQuery < 1.9
	    //success: function(data){}
	})
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
 * Uploads JSON data to the server and stores it in the File Manager.
 * @param name the name for the stored file
 * @param jsonArray array of json objects
 * @param loaderTarget the elmenet the loader should be toggles
 *************************************************************************************/
function cfw_http_postStoreJsonData(name, jsonArray) {
	

	CFW.ui.toggleLoader(true);
	
	cfw_utils_sleep(500).then(() => {
		
		//-------------------------------
		// Upload Files
		let uploadedArray = [];
		$.ajaxSetup({async: false});
		
			let CFW_URL_STOREJSONDATA = '/app/stream/storejsondata';
			
			let formData =  JSON.stringify(jsonArray);
	
			CFW.http.postFormData(CFW_URL_STOREJSONDATA+"?name="+ encodeURIComponent(name)
			, formData
			, function(response, status, xhr){
				if(response.payload != null){
					uploadedArray = _.concat(uploadedArray, response.payload);
				}
			});
		
		$.ajaxSetup({async: true});
		
		CFW.ui.toggleLoader(false);
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
		  for(let i = 0; i < msgArray.length; i++ ){
			  CFW.ui.addToast(msgArray[i].message, null, msgArray[i].type, CFW.config.toastErrorDelay);
		  }
	  }
}