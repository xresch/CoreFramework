/**************************************************************************************************************
 * CFW.js
 * ======
 * Main library for the core framwork.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/


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

		{	label: 'JSON',
			name: 'json',
			renderdef: {}
		},
		{	label: 'Text Table',
			name: 'texttable',
			renderdef: {}
		},
		{	label: 'XML',
			name: 'xml',
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
		colorToRGBA: cfw_colors_colorToRGBA,
		randomRGB: cfw_colors_randomRGB,
		randomHSL: cfw_colors_randomHSL,
		randomSL: cfw_colors_randomSL,
		getCFWStateStyle: cfw_colors_getCFWStateStyle,
		getThresholdDirection: cfw_colors_getThresholdDirection,
		getThresholdStyle: cfw_colors_getThresholdStyle,
		getThresholdIndicator: cfw_colors_getThresholdIndicator,
		getThresholdIndicatorForStyle: cfw_colors_getThresholdIndicatorForStyle,
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
		, epochToShortDate: 	cfw_format_epochToShortDate
		, fieldNameToLabel: 	cfw_format_fieldNameToLabel
		, millisToClock: 		cfw_format_millisToClock
		, timeToDuration: 		cfw_format_timeToDuration
		, millisToDuration: 	cfw_format_millisToDuration
		, millisToDurationClock: cfw_format_millisToDurationClock
		, cfwSchedule: 			cfw_format_cfwSchedule
		, splitCFWSchedule: 	cfw_format_splitCFWSchedule
		, boxplot: 				cfw_format_boxplot
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
		postFormData: cfw_http_postFormData,
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
		downloadText: cfw_utils_downloadText,
		hash: cfw_utils_hash,
		isNullOrEmpty: cfw_utils_isNullOrEmpty,
		isTrue: cfw_utils_isTrue,
		nullTo: cfw_utils_nullTo,
		setFloatPrecision: cfw_utils_setFloatPrecision,
		urlToLink: cfw_utils_urlToLink,
		replaceAll: cfw_utils_replaceAll,
		randomInt: cfw_utils_randomInt,
		sleep: cfw_utils_sleep,
		clipboardRead: cfw_utils_clipboardRead,
		clipboardWrite: cfw_utils_clipboardWrite,
		
	},
	ui: {
		addAlert: cfw_ui_addAlert,
		addToast: cfw_ui_addToast,
		addToastInfo: function(text){cfw_ui_addToast(text, null, "info", CFW.config.toastDelay);},
		addToastSuccess: function(text){cfw_ui_addToast(text, null, "success", CFW.config.toastDelay);},
		addToastWarning: function(text){cfw_ui_addToast(text, null, "warning", CFW.config.toastDelay);},
		addToastDanger: function(text){cfw_ui_addToast(text, null, "danger", CFW.config.toastErrorDelay);},
		createToggleButton: cfw_ui_createToggleButton,
		createPrintView: cfw_ui_createPrintView,
		createLoaderHTML: cfw_ui_createLoaderHTML,
		confirmExecute: cfw_ui_confirmExecute,
		getWorkspace: cfw_ui_getWorkspace,
		showModalMedium: cfw_ui_showModalMedium,
		showModalSmall: cfw_ui_showModalSmall,
		showModalLarge: cfw_ui_showModalLarge,
		storeJsonDataModal: cfw_ui_storeJsonDataModal,
		toc: cfw_ui_createTOC,
		toggleLoader: cfw_ui_toggleLoader,
		toggleLoaderQuotes: cfw_ui_toggleLoaderQuotes,
		toggleDropdownMenuFixed: cfw_ui_toggleDropdownMenuFixed,
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

