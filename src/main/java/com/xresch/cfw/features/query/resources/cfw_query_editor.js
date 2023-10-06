
// global as you can have multiple editors at once
CFW_QUERY_EDITOR_AUTOCOMPLETE_FORM_ID = null;
CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV = null;
CFW_QUERY_URL = "/app/query";



/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_editor_getManualPage(type, componentName){
	
	//-----------------------------------
	// Do Execution
	
	params = {action: "fetch"
		, item: "manualpage"
		, type: type
		, name: componentName
	};
	
	CFW.http.postJSON(CFW_QUERY_URL, params, 
		function(data) {
		
			if(data.success){	
				// this will make the manual close then clicked outside the manual
				var autocompleteWrapper = CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV.find(".autocomplete-wrapper");										
				autocompleteWrapper.html('');
				
				var manualDiv = $('<div>');

				manualDiv.append('<h1 style="margin-top: 0px;">Manual of '+componentName+'</h1>');
				manualDiv.attr('onclick', 'event.stopPropagation();');
				
				manualDiv.append(data.payload);
				
				autocompleteWrapper.append(manualDiv);
				
				autocompleteWrapper.find('pre code').each(function(index, element){
					hljs.highlightElement(element);
				})
			}
			
	});
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_editor_handleButtonExecute(buttonElement){
	
	var textarea = $(buttonElement).closest('.cfw-query-content-wrapper').find('textarea');	
	var queryEditor = textarea.data('queryEditor');

	queryEditor.executeQuery(false);
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_editor_handleButtonFullscreen(buttonElement){
	
	var textarea = $(buttonElement).closest('.cfw-query-content-wrapper').find('textarea');	
	var queryEditor = textarea.data('queryEditor');
	
	console.log(textarea)
	console.log(queryEditor)
	
	queryEditor.toggleFullscreen(buttonElement);
}


/******************************************************************
 * Makes a Query Editor out of a textarea.
 * 
 * @param parent JQuery object
 * @param data object containing the list of results.
 * 
 ******************************************************************/
class CFWQueryEditor{
	
	constructor(textarea, settings){
		
		//-------------------------------------------
		// Class Fields 
		//-------------------------------------------
		
		// the GUI ID of this element
		this.guid = CFW.utils.randomString(12);
		
		// the original textarea element as JQuery
		this.formID = $(textarea).closest('form').find('#'+CFW.global.formID).val();
		
		// the original textarea element as JQuery
		this.textarea = $(textarea);

		// contains the original textarea and the code highlighting
		this.editorfield = null;
		
		// the div that will contain the highlighted code, as JQuery
		this.query_hljs = null;
		
		// the div that will contain the autocomplete results
		this.autocompleteDiv = null
		
		// the execute button, as JQuery
		this.executeButton = null;
		
		// used to avoid multiple parallel executions
		this.isQueryExecuting = false;	
		
		//-------------------------------------------
		// Settings
		//-------------------------------------------
		this.settings = {
			// the URL path of the query servlet
			queryURL: CFW_QUERY_URL
			// the div where the results should be sent to
			, resultDiv: null
			// the id of the timeframe picker, if null new one will be created (Default: null)
			, timeframePickerID: null
			// toggle is the query data should be added to the URL
			, setURLParams: false
			
		};
		
		this.settings = Object.assign({}, this.settings, settings);
		
		//-------------------------------------------
		// Setup
		//-------------------------------------------
		var queryEditor = this;
		this.textarea.data("queryEditor", queryEditor);
		this.initializeEditor();

	}
	
	/*******************************************************************************
	 * 
	 ******************************************************************************/
	highlightExecuteButton(enableHighlighting){
		var button = $("#executeButton-"+this.guid);
		
		if(enableHighlighting){
			button.addClass('btn-warning')
			button.removeClass('btn-btn-primary')
		}else{
			button.removeClass('btn-warning')
			button.addClass('btn-btn-primary')
		}
		
	}
	
	/********************************************
	* 
	*********************************************/
	refreshHighlighting() {
		var query = this.textarea.val();
		this.query_hljs.text(query);
		hljs.highlightElement(this.query_hljs.get(0));
	}
	
	/**************************************************************************************
	 * 
	 *************************************************************************************/
	toggleFullscreen(buttonElement){
	
		var button = $(buttonElement);
		var wrapper = $(buttonElement).closest(".query-editor");
	
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
	
	/*******************************************************************************
	 * 
	 * 
	 ******************************************************************************/
	handleEnter(){
		
		var domElement = this.textarea.get(0);
		//==========================================================
		// Copy Indendation
		//==========================================================
		var selectionStart = domElement.selectionStart;
		var selectionEnd = domElement.selectionEnd;
		var value = domElement.value;
		
		//--------------------------------------------
		// Find Line Start
		var indexLineStart = selectionStart;
		if(value.charAt(indexLineStart) == "\n"){ indexLineStart-- };
		
		for(; indexLineStart > 0 ;indexLineStart-- ){
			if(value.charAt(indexLineStart) == "\n"){ break; }
		}
		
		//--------------------------------------------
		// Find Line End
		var indexLineEnd = selectionEnd;
		for(; indexLineEnd < value.length-1 ;indexLineEnd++ ){
			if(value.charAt(indexLineEnd) == "\n"){ break; }
		}
		
		//--------------------------------------------
		// Insert newline with indentation
		var line = value.substring(indexLineStart, indexLineEnd);	
		var indexFirstNonWhitespace = line.search(/[^\s]/);
		var indentation = "";
		if(indexFirstNonWhitespace != 0){
			if(indexFirstNonWhitespace == -1){ indexFirstNonWhitespace = line.length; }
			var indentation = line.substring(1, indexFirstNonWhitespace);
		}
		
		// set textarea value to: text before caret + tab + text after caret
		value = value.substring(0, selectionStart) 
						+"\n"
						+indentation
		    		+ value.substring(selectionEnd);
		
		domElement.value = value;
		
		let newCursorPos = selectionStart + indentation.length + 1;
		
		domElement.selectionStart = newCursorPos;
		domElement.selectionEnd = newCursorPos;
			
	}
	/*******************************************************************************
	 * Resize the text Area to fit query.
	 * 
	 ******************************************************************************/
	resizeToFitQuery(){
		
		var value =  this.textarea.val();

		if( !CFW.utils.isNullOrEmpty(value) ){
			
			// Number of lines multiplied with 16px line high
			var queryLineCount = (value.match(/\n/g) || []).length;
			
			// minimum height 6 lines
			if(queryLineCount < 5) {	queryLineCount = 6; }
			
			// Line heigth is 16px by css class query-text-format
			// add 0.2 to make sure scrolling is prevented
			// add +2 lines by default
			var queryHeight = Math.floor((queryLineCount+2) * 16.2);
			
			var queryWidth = this.textarea[0].scrollWidth;

			this.textarea.css("height", queryHeight+'px'); 
			this.textarea.css("width", queryWidth+'px'); 
			
			this.query_hljs.css("height", queryHeight+"px");
			this.query_hljs.css("width", queryWidth+"px");
			
			var editorHeight = queryHeight+7;
			if(editorHeight > 500){ editorHeight = 500; };
			
			this.editorfield.css('height',editorHeight+"px")
			
			
		}
		
	}
	
	/*******************************************************************************
	 * 
	 * @param direction 'up' or 'down' 
	 ******************************************************************************/
	copyCurrentLine(direction){
		
		var domElement = this.textarea.get(0);
		var selectionStart = domElement.selectionStart;
		var selectionEnd = domElement.selectionEnd;
		var value = domElement.value;
		
		//--------------------------------------------
		// Find Line Start
		var indexLineStart = selectionStart;
		if(value.charAt(indexLineStart) == "\n"){ indexLineStart-- };
		
		for(; indexLineStart > 0 ;indexLineStart-- ){
			if(value.charAt(indexLineStart) == "\n"){ break; }
		}
		
		// no clue why this is needed and why it works, but hey I take it! XD 
		// Might look into it in the future when I find more time. ;-)
		var breakAtStart = '';
		if(indexLineStart == 0){
			breakAtStart = '\n';
		}
		
		//--------------------------------------------
		// Find Line End
		var indexLineEnd = selectionEnd;
		var newlineFound = false;
		for(; indexLineEnd < value.length-1 ;indexLineEnd++ ){
			if(value.charAt(indexLineEnd) == "\n"){ newlineFound = true; break; }
		}
		
		if(!newlineFound){
			indexLineEnd = value.length;
		}
		
		//--------------------------------------------
		// Insert Line
		var line = value.substring(indexLineStart, indexLineEnd);	
		
		// set textarea value to: text before caret + tab + text after caret
		value = value.substring(0, indexLineStart) 
						+line+breakAtStart
						+line
		    		+ value.substring(indexLineEnd);
		
		domElement.value = value;
		
		let newCursorPos = (direction == "down") ? selectionStart + line.length : selectionStart;
		
		domElement.selectionStart = newCursorPos;
		domElement.selectionEnd = newCursorPos;
	
	}
	
	/*******************************************************************************
	 * Main method for building the view.
	 * 
	 * @param direction of the indentation in case of multiline 'increase' or 'decrease' 
	 ******************************************************************************/
	handleTab(direction){
		
		var domElement = this.textarea.get(0);
		
		var start = domElement.selectionStart;
	    var end = domElement.selectionEnd;
		var currentText = domElement.value;
	
		var selectedText = null; 
		if(start < end){
			selectedText = currentText.substring(start, end);
		}
		
		if(direction == "increase" 
		&& (selectedText == null || !selectedText.includes("\n") )
		){
			
			//-------------------------------
			// Insert Tab
		    domElement.value = currentText.substring(0, start) +
		      "\t" + currentText.substring(end);
		
		    domElement.selectionStart =
		      domElement.selectionEnd = start + 1;
	
		}else{
			
			//--------------------------------------------
			// Find Line Start
			var indexLineStart = start;
			if(currentText.charAt(indexLineStart) == "\n"){ indexLineStart-- };
			
			for(; indexLineStart > 0 ;indexLineStart-- ){
				if(currentText.charAt(indexLineStart) == "\n"){ break; }
			}
			
			//--------------------------------------------
			// Create Replacement
			var adjustTabsOnThis = currentText.substring(indexLineStart, end); 
			var changeCount;
			
			if(direction == "increase"){
				changeCount = (adjustTabsOnThis.match(/\n/g) || []).length;
				adjustTabsOnThis = adjustTabsOnThis.replaceAll("\n", '\n\t');
				
			}else{
				changeCount = -1 * (adjustTabsOnThis.match(/\n\t/g) || []).length;
				adjustTabsOnThis = adjustTabsOnThis.replaceAll("\n\t", '\n');
			}
			
			//-------------------------------
			// Replace NewLine With Newline+Tab
		    domElement.value = currentText.substring(0, indexLineStart) +
		      adjustTabsOnThis + currentText.substring(end);
	
			domElement.selectionStart = indexLineStart+1;
		    domElement.selectionEnd = end + changeCount;
		}
		
	}
		
	
				
	/*******************************************************************************
	 * if not exists create timeframe picker
	 * 
	 ******************************************************************************/
	createTimeframePickerField(){
		
		//------------------------------
		// Check if picker is null
		if(this.settings.timeframePickerID == null){
			this.settings.timeframePickerID = "timeframePicker-"+this.guid;
		}
		
		//------------------------------
		// Create Picker if not exists
		if($("#"+this.settings.timeframePickerID).length == 0){
			var executeButton = $('#executeButton-'+this.guid);
			var timeframePicker = $(`<input id="${this.settings.timeframePickerID}" name="timeframePicker" type="text" class="form-control">`);
			
			executeButton.before(timeframePicker);
			
			var queryEditor  = this;
			cfw_initializeTimeframePicker(this.settings.timeframePickerID, {offset: '30-m'}, function(){
				queryEditor.executeQuery(false);
			});
			
		}
		
	}
				
	/*******************************************************************************
	 * Initialize the editor field by adding the event listeners
	 * 
	 ******************************************************************************/
	createEditorField(){
		var parent = this.textarea.parent();
		
		//--------------------------------
		// Modify Original Textarea
		this.textarea.addClass('query-original query-text-format');
		this.textarea.removeClass('form-control');
		this.textarea.attr('spellcheck', false);
		this.textarea.attr('placeholder', 'Write your query. \r\n Ctrl+Space for content assist. \r\n Ctrl+Enter to execute.');
		
		//--------------------------------
		// Create Editor
		var queryEditorWrapper = $(`
			<div class="cfw-query-content-wrapper">
				
				<div id="query-button-menu-${this.guid}" class="pb-2 pt-2">
					<div class="col-12 d-flex justify-content-start">
						<!-- input id="timeframePicker" name="timeframePicker" type="text" class="form-control" -->
						<!-- a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-save"></i></a>
						<a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-star"></i></a>
						<a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-history"></i></a -->
						<a id="executeButton-${this.guid}" type="button" class="btn btn-sm btn-primary ml-2" onclick="cfw_query_editor_handleButtonExecute(this);"><b>Execute</b></a>
					</div>
				</div>
				<div class="query-editor">
					<i class="fas fa-expand" onclick="cfw_query_editor_handleButtonFullscreen(this)" ></i>
					<div id="query-editor-field-${this.guid}" class="scroll-fix" style="position: relative; height: auto; ">
						<pre id="query-pre-element"><code id="query-highlighting" class="preview language-cfwquery query-text-format"></code></pre>
					</div>
				</div>
				
			</div>
		`);
		
		parent.append(queryEditorWrapper);
		this.editorfield = queryEditorWrapper.find('#query-editor-field-'+this.guid);
		this.editorfield.prepend(this.textarea);
		this.query_hljs = queryEditorWrapper.find('code');
		
		//--------------------------------
		// Create Autocomplete
		CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV = $('<div id="query-autocomplete-results">');
		CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV.css("background", $('body').css('background'));
		
		queryEditorWrapper.append(CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV);
		this.createAutocompleteForm();
		
		var fieldname = this.textarea.attr('id');

		cfw_autocompleteInitialize(CFW_QUERY_EDITOR_AUTOCOMPLETE_FORM_ID, fieldname, 0,10, null, true, CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV);
		
		//--------------------------------
		// Timeframe Picker
		this.createTimeframePickerField();
	}	
	
	/******************************************************************************
	 * Initialize the editor field by adding the event listeners
	 * 
	 ******************************************************************************/
	createAutocompleteForm(){
		
		if(CFW_QUERY_EDITOR_AUTOCOMPLETE_FORM_ID == null){
			$.ajaxSetup({async: false});
				
				CFW.http.getJSON(this.settings.queryURL, {action: "create", item: "autocompleteform"}, function(data){
					if(data.success){
						CFW_QUERY_EDITOR_AUTOCOMPLETE_FORM_ID = data.formid;
					}
				});
			
			$.ajaxSetup({async: true});
		}
	}
	
	/*******************************************************************************
	 * Initialize the editor field by adding the event listeners
	 * 
	 ******************************************************************************/
	initializeEditor(){
		
		this.createAutocompleteForm();
		this.createEditorField();
		this.resizeToFitQuery();	
		this.refreshHighlighting();
		
		// instance of  class CFWQueryEditor
		var queryEditor = this;
		

		//-----------------------------------
		// Query Field Event Handler
		this.textarea.on("keydown", function(e){
			
			
			queryEditor.highlightExecuteButton(true);
			
			//---------------------------
			// Ctrl + Alt + Up
			if (e.ctrlKey && e.altKey && e.keyCode == 38) {
				queryEditor.copyCurrentLine('up');
				return;
			}
			
			//---------------------------
			// Ctrl + Alt + Down
			if (e.ctrlKey && e.altKey && e.keyCode == 40) {
				queryEditor.copyCurrentLine('down');
				return;
			}
			
			//---------------------------
			// Shift+Tab: Decrease Indentation
			if (e.shiftKey && e.key == 'Tab') {
			    e.preventDefault();
				queryEditor.handleTab("decrease");
				return;
			}
				
			//---------------------------
			// Allow Tab for Indentation
			if (e.key == 'Tab') {
			    e.preventDefault();
				queryEditor.handleTab("increase");
			}
			
	
			
			//---------------------------
			// Ctrl + Enter
			if (e.ctrlKey && e.keyCode == 13) {
				queryEditor.executeQuery(false);
				return;
			}
			
			//---------------------------
			// Enter
			if (e.keyCode == 13) {
				e.preventDefault();
				queryEditor.handleEnter();
				queryEditor.resizeToFitQuery();
				
				return;
			}
			
		});
		
		//-----------------------------------
		// Refresh highlight and resize
		this.textarea.on("keyup", function(e){
			queryEditor.refreshHighlighting();
			queryEditor.resizeToFitQuery();
		});
		
		//-----------------------------------
		// Refresh highlight and resize on keyup/paste
		this.textarea.on("paste", function(e){
			window.setTimeout(function(){
				queryEditor.resizeToFitQuery();
				queryEditor.refreshHighlighting();
			}, 100);
		});
		
		// needed for autocomplete, select with enter
		this.textarea.on("change", function(e){
			queryEditor.refreshHighlighting();
		});
		
		// needed for autocomplete, select with click
		this.textarea.on("focus", function(e){
			queryEditor.refreshHighlighting();
		});
		
		//-----------------------------------
		// Monitor Window Resize
		$(function() {
		    var $window = $(window);
		    var width = $window.width();
		    var height = $window.height();
		
		    setInterval(function () {
		        if ((width != $window.width()) || (height != $window.height())) {
		            width = $window.width();
		            height = $window.height();
		
					queryEditor.resizeToFitQuery();
		        }
		    }, 1000);
		});
	}
	
	/*******************************************************************************
	 * Execute the query and fetch data from the server.
	 * 
	 * @param isPageLoad if the execution is caused by a page load 
	 ******************************************************************************/
	executeQuery(isPageLoad){
		
		//-----------------------------------
		// Check is already Executing
		if(this.isQueryExecuting){
			return;
		}
		this.isQueryExecuting = true;
		

		var targetDiv = CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV;
		var timeframe = JSON.parse($('#'+this.settings.timeframePickerID).val());
		var query =  this.textarea.val();
	
	 	var timeZoneOffset = new Date().getTimezoneOffset();
					
		//-----------------------------------
		// Update Params in URL
		
		var queryLength = encodeURIComponent(query).length;
		var finalLength = queryLength + CFW.http.getHostURL().length + CFW.http.getURLPath().length ;
		
		if(finalLength+300 > JSDATA.requestHeaderMaxSize){
			CFW.ui.addToastInfo("The query is quite long and the URL might not work. Make sure to save a copy of your query.");
		}
		
		if(this.settings.setURLParams){
			
			var doPushHistoryState = !isPageLoad;
			CFW.http.setURLParams({
					  "query": query
					, "offset": timeframe.offset
					, "earliest": timeframe.earliest
					, "latest": timeframe.latest
				}, doPushHistoryState);
		}
				
		//CFW_QUERY_URLPARAMS = CFW.http.getURLParamsDecoded();
		
		//-----------------------------------			
		// hide existing messages to not confuse user
		$('.toast.show').removeClass('show').addClass('hide');
		
	
		//-----------------------------------
		// Revert Button Highlighting
		this.highlightExecuteButton(false);
		
		//-----------------------------------
		// Prepare Parameters
		var pageParams;
		if (typeof cfw_parameter_getFinalParams !== "undefined") { 
 			pageParams = cfw_parameter_getFinalParams(CFW_DASHBOARD_PARAMS);
		} 
		
		var queryParams = {};
		if(pageParams != null){
			for(var index in pageParams){
				var current = pageParams[index];
				queryParams[current.NAME] = current.VALUE;
			}
		}
		
		params = {action: "execute"
				, item: "query"
				, query: query
				, timezoneOffsetMinutes: timeZoneOffset
				, offset: timeframe.offset
				, earliest: timeframe.earliest
				, latest: timeframe.latest
				, parameters: JSON.stringify(queryParams)
			};
			
		//-----------------------------------
		// Do Execution
		
		//cfw_query_toggleLoading(true);
		var queryEditor = this;
		
		CFW.http.postJSON(CFW_QUERY_URL, params, 
			function(data) {
				//cfw_query_toggleLoading(false);
				
				if(data.success){
					queryEditor.isQueryExecuting = false;	
					
					// use autocomplete wrapper to get it closed when clicking outside of div
					var resultWrapper = $('<div class="autocomplete-wrapper p-2">');
					resultWrapper.attr('onclick', 'event.stopPropagation();');
					
					targetDiv.append(resultWrapper);					
					cfw_query_renderAllQueryResults(resultWrapper, data.payload);
					
				}
				
		});
	}

}