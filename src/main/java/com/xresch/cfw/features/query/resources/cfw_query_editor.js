
CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV = null;
CFW_QUERY_URL = "/app/query";

/******************************************************************
 * Initialize a query editor with a result area.
 * 
 * @param parent the parent element for the editor.
 * @param query initial query for the editor.
 * @param useURLParams define if user
 ******************************************************************/
function cfw_query_editor_initializeEditor(parent, query, useURLParams){

	if(useURLParams == null){ useURLParams = true; }
	
	let queryAreaID = "query-"+CFW.utils.randomString(16);
	let resultID = "cfw-query-results-"+CFW.utils.randomString(16);
	
	
	parent.append(`
		<div>
			<div class="row">
				<div class="col-12">
					<textarea id="${queryAreaID}" name="${queryAreaID}" class="query-original query-text-format" spellcheck="false" placeholder="Write your query. \r\n Ctrl+Space for content assist. \r\n Ctrl+Enter to execute."></textarea>
				</div>
			</div>
		</div>
		<div id="${resultID}" class="monospace">
		</div>
	`);
	
	let $QUERYAREA = $('#'+queryAreaID);
	
	let queryEditor = new CFWQueryEditor($QUERYAREA, {
			// the div where the results should be sent to
			 resultDiv: $("#"+resultID)
			// the id of the timeframe picker, if null new one will be created (Default: null)
			, timeframePickerID: null
			// toggle is the query data should use URL params
			, useURLParams: useURLParams
			
		});
	
	//-----------------------------
	// Handle back button
	if(query == null){
		window.onpopstate = function() {
			queryEditor.loadQueryFromURLAndExecute();
		}
	}else{
		queryEditor.setQuery(query);
	}
	
	return queryEditor;
}

/*******************************************************************************
 * 
 *******************************************************************************/
(function (){
	
	//-------------------------------------
	// Add ParamEnhancer Function
	// needed to adjust cursor position on
	// parameter changes
	cfw_autocomplete_addParamEnhancer( function(inputField, requestAttributes, originalRequestAttributes){

			//------------------------------------------
			// Parameterize Edit Widget Modal Request
			if(inputField.hasClass("query-original")){

				let originalQuery = originalRequestAttributes.query;
				
				if(originalQuery != null && originalQuery.includes("$")){

					let cursorPos = requestAttributes.cfwAutocompleteCursorPosition;
					let beforeCursor = originalQuery.substring(0, cursorPos);
					
					//TODO Find a more stable way to access params for currentPage
					let params = cfw_parameter_getFinalParams(CFW_DASHBOARD_PARAMS);	
					
					let beforeCursorNew = cfw_parameter_substituteInString(beforeCursor, params);
					let newCursorPos = cursorPos + (beforeCursorNew.length - beforeCursor.length);
					
					requestAttributes.cfwAutocompleteCursorPosition = newCursorPos;

				
				}
				
				return;
			}

			
		}
	);
}) ()

/*******************************************************************************
 * 
 *******************************************************************************/
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
	
	if(!queryEditor.isExecuting){
		queryEditor.executeQuery(false);
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_query_editor_handleButtonFullscreen(buttonElement){
	
	var textarea = $(buttonElement).closest('.cfw-query-content-wrapper').find('textarea');	
	var queryEditor = textarea.data('queryEditor');
	
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
		
		// the id of the autocomplete form for this editor
		this.autocompleteFormID = null

		
		// the div containing the button menu
		this.editorButtonMenu = null 
		
		// the execute button, as JQuery
		this.executeButton = null;
		
		// used to avoid multiple parallel executions
		this.isExecuting = false;	
		
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
			// toggle is the query data should make use of URL params (store and retrieve from params)
			, useURLParams: false
			
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
	
	/********************************************
	* 
	*********************************************/
	setQuery(query) {
		this.textarea.val(query);
		this.query_hljs.text(query);
		hljs.highlightElement(this.query_hljs.get(0));
		this.resizeToFitQuery();
	}
	
	/*******************************************************************************
	 * 
	 ******************************************************************************/
	toggleLoading(isLoading) {
		
		this.isExecuting = isLoading;
		
		this.editorButtonMenu.find('button').prop('disabled', isLoading);
		this.executeButton.prop('disabled', isLoading);
		
		if(this.settings.resultDiv != null){
			CFW.ui.toggleLoader(isLoading, this.settings.resultDiv.attr('id'));	
		}
			
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
	copySelectedLines(direction){
		
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
		var selectedLines = value.substring(indexLineStart, indexLineEnd);	
		
		// set textarea value to: text before caret + tab + text after caret
		value = value.substring(0, indexLineStart) 
						+selectedLines
						+breakAtStart
						+selectedLines
		    		+ value.substring(indexLineEnd);
		
		domElement.value = value;
		
		let newCursorPos = (direction == "down") ? selectionStart + selectedLines.length : selectionStart;
		
		domElement.selectionStart = newCursorPos;
		domElement.selectionEnd = newCursorPos;
	
	}
	
	/*******************************************************************************
	 * 
	 * @param direction 'up' or 'down' 
	 ******************************************************************************/
	moveSelectedLines(direction){
		
		let domElement = this.textarea.get(0);
		let selectionStart = domElement.selectionStart;
		let selectionEnd = domElement.selectionEnd;
		let value = domElement.value;
		
		//--------------------------------------------
		// Find Line Start
		let indexLineStart = selectionStart;
		if(value.charAt(indexLineStart) == "\n"){ indexLineStart-- };
		
		for(; indexLineStart > 0 ;indexLineStart-- ){
			if(value.charAt(indexLineStart) == "\n"){ break; }
		}
				
		if(indexLineStart == 0 && direction == "up"){
			return; // start of text, cannot move up
		}
		
		//--------------------------------------------
		// Find Line End
		let indexLineEnd = selectionEnd;
		let newlineFound = false;
		for(; indexLineEnd < value.length-1 ;indexLineEnd++ ){
			if(value.charAt(indexLineEnd) == "\n"){ newlineFound = true; break; }
		}
		
		if(!newlineFound){
			indexLineEnd = value.length;
		}
		
		if(indexLineEnd == value.length && direction == "down"){
			return; // end of text, cannot move down
		}
		
		//--------------------------------------------
		// Find insert Position
		let insertPosition = 0;
		
		if(direction == "down"){
			insertPosition = indexLineEnd+1;
			for(; insertPosition < value.length-1 ;insertPosition++ ){
				if(value.charAt(insertPosition) == "\n"){ newlineFound = true; break; }
			}
			if(!newlineFound){ insertPosition = value.length; }
		}else{
			insertPosition = indexLineStart-1;
			for(; insertPosition > 0 ;insertPosition-- ){
				if(value.charAt(insertPosition) == "\n"){ break; 
			}
			if(insertPosition < 0 ){ insertPosition = 0; }
		}
		}
		
		//--------------------------------------------
		// Move Lines
		let selectedLines = value.substring(indexLineStart, indexLineEnd);	
		let textBefore = value.substring(0, indexLineStart);
		let textAfter = value.substring(indexLineEnd);
		
		let selectedRemoved = textBefore + textAfter;

		if(direction == "down"){ 
			insertPosition -= selectedLines.length; 
			if(insertPosition < 0){ insertPosition = 0; }
		}	
		
		let insertMode = 
				(insertPosition == 0) 
					? "start"
					: (insertPosition == selectedRemoved.length-1)
					 	? "end"
					 	: "somewhere"
					 	;
					 	
		let newText;	
		let newCursorStart = 0;			 	
		let newCursorEnd = 0;			 	
		switch(insertMode){
			
			case 'start':
				
				if(selectedLines.startsWith("\n")) { selectedLines = selectedLines.substring(1); };
				
				selectedLines = selectedLines+"\n";
				newText = selectedRemoved.substring(0, insertPosition) 
						+ selectedLines
		    		+ selectedRemoved.substring(insertPosition);
		    	newCursorStart = 0;
		    	newCursorEnd = selectedLines.length-1;
		
			break;
			
			case 'end':
				selectedLines = selectedLines;
				newText = selectedRemoved + selectedLines;
				newCursorEnd = newText.length;
				newCursorStart = newCursorEnd - selectedLines.length+1;
			break;	
			
			default:

				if(indexLineStart == 0 
				&& selectedRemoved.startsWith("\n")) { 
					selectedRemoved = selectedRemoved.substring(1);
					selectedLines = selectedLines+"\n";
					newCursorStart = -1;
					newCursorEnd = -1;
				};
				
				newText = selectedRemoved.substring(0, insertPosition) 
						+ selectedLines
		    		+ selectedRemoved.substring(insertPosition);
		    	newCursorStart += insertPosition+1;
		    	newCursorEnd += insertPosition + selectedLines.length;
			break;	
		}
		
		domElement.value = newText;
		domElement.selectionStart = newCursorStart;
		domElement.selectionEnd = newCursorEnd;
	
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
		
		//-----------------------------------
		// Load Timeframe from URL or set default
		var urlParams = CFW.http.getURLParamsDecoded();
		
		var callbackFunction = function(){
					queryEditor.executeQuery(false);
				};
		
		var picker = $('#'+this.settings.timeframePickerID);
		if(picker.length == 0){
			var executeButton = $('#executeButton-'+this.guid);
			var timeframePicker = $(`<input id="${this.settings.timeframePickerID}" name="timeframePicker" type="text" class="form-control">`);
			executeButton.before(timeframePicker);
			
			var queryEditor  = this;
			
			if(!CFW.utils.isNullOrEmpty(urlParams.offset)){
				cfw_initializeTimeframePicker(this.settings.timeframePickerID
								, {offset: urlParams.offset}
								, callbackFunction );
			}else{
				if(!CFW.utils.isNullOrEmpty(urlParams.earliest)
				&& !CFW.utils.isNullOrEmpty(urlParams.latest) ){
					cfw_initializeTimeframePicker(this.settings.timeframePickerID, {earliest: urlParams.earliest, latest: urlParams.latest}, callbackFunction);
				}else{
					cfw_initializeTimeframePicker(this.settings.timeframePickerID, {offset: '1-h'}, callbackFunction);
				}
			}
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
		this.textarea.attr('placeholder', 'Write your query. '
					+'\r\n Ctrl + Space for content assist. '
					+'\r\n Ctrl + Enter to execute.'
					+'\r\n Ctrl + Alt + Up/Down duplicate selected lines.'
					+'\r\n Alt + Up/Down to move selected lines.'
					);
		
		//--------------------------------
		// Create Editor
		var queryEditorWrapper = $(`
			<div class="cfw-query-content-wrapper">
				
				<div id="query-editor-btn-menu-${this.guid}" class="pb-2 pt-2">
					<div class="col-12 d-flex justify-content-start">
						<!-- input id="timeframePicker" name="timeframePicker" type="text" class="form-control" -->
						<!-- a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-save"></i></a>
						<a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-star"></i></a>
						<a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-history"></i></a -->
						<button id="executeButton-${this.guid}" type="button" class="btn btn-sm btn-primary ml-2" onclick="cfw_query_editor_handleButtonExecute(this);"><b>Execute</b></button>
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
		this.editorButtonMenu = queryEditorWrapper.find('#query-editor-btn-menu-'+this.guid);
		this.executeButton = queryEditorWrapper.find('#executeButton-'+this.guid);
		
		this.editorfield = queryEditorWrapper.find('#query-editor-field-'+this.guid);
		this.editorfield.prepend(this.textarea);
		this.query_hljs = queryEditorWrapper.find('code');
		
		//--------------------------------
		// Create Autocomplete
		if(CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV == null){
			CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV = $('<div id="query-autocomplete-results" class="block-modal">');
			// add to body as it is shared by all editors
			$('body').append(CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV);
		}
		
		this.createAutocompleteForm();
		
		var fieldname = this.textarea.attr('name');

		cfw_autocompleteInitialize(this.autocompleteFormID, fieldname, 0,10, null, true, CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV);
			
		//--------------------------------
		// Timeframe Picker
		this.createTimeframePickerField();
	}	
	
	/******************************************************************************
	 * Initialize the editor field by adding the event listeners
	 * 
	 ******************************************************************************/
	createAutocompleteForm(){
		
		if(this.autocompleteFormID == null){
			
			var fieldname = this.textarea.attr('name');
			if(fieldname == null){
				fieldname = this.textarea.attr('id');
			}
			
			var queryEditor = this;
			$.ajaxSetup({async: false});
				
				CFW.http.getJSON(this.settings.queryURL, {action: "create", item: "autocompleteform", fieldname: fieldname}, function(data){
					if(data.success){
						queryEditor.autocompleteFormID = data.formid;
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
		
		if(this.settings.useURLParams == true){
			this.loadQueryFromURLAndExecute();
		}
		
		// instance of  class CFWQueryEditor
		var queryEditor = this;
		

		//-----------------------------------
		// Query Field Event Handler
		this.textarea.on("keydown", function(e){
			
			queryEditor.highlightExecuteButton(true);
			
			//---------------------------
			// Ctrl + Alt + Up
			if (e.ctrlKey && e.altKey && e.keyCode == 38) {
				queryEditor.copySelectedLines('up');
				return;
			}
			
			//---------------------------
			// Ctrl + Alt + Down
			if (e.ctrlKey && e.altKey && e.keyCode == 40) {
				queryEditor.copySelectedLines('down');
				return;
			}
			//---------------------------
			// Alt + Up
			if ( e.altKey && e.keyCode == 38) {
				queryEditor.moveSelectedLines('up');
				return;
			}
			
			//---------------------------
			// Alt + Down
			if (e.altKey && e.keyCode == 40) {
				queryEditor.moveSelectedLines('down');
				return;
			}
			
			//---------------------------
			// Alt + Left or Right
			// Is mapped to browser go back and forth in tab history
			// Prevent it to not lose changes in query. 
			if (e.altKey 
			   && (  
				     e.keyCode == 37 
			      || e.keyCode == 39
			      ) 
			   ){
				e.preventDefault();
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
			queryEditor.resizeToFitQuery();
			queryEditor.refreshHighlighting();
		});
		
		// needed for autocomplete, select with click
		this.textarea.on("focus", function(e){
			queryEditor.resizeToFitQuery();
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
	 * 
	 ******************************************************************************/
	loadQueryFromURLAndExecute(){
		
		var urlParams = CFW.http.getURLParamsDecoded();
		
		//-----------------------------------
		// Load Query from URL
		if( !CFW.utils.isNullOrEmpty(urlParams.query) ){
	
			this.textarea.val(urlParams.query);
	
			this.resizeToFitQuery();
			this.refreshHighlighting();
			this.executeQuery(true);
	
		}else{
			//cfw_query_editor_resizeToFitQuery();
			this.refreshHighlighting();
		}
	}
	
	/*******************************************************************************
	 * Execute the query and fetch data from the server.
	 * 
	 * @param isPageLoad if the execution is caused by a page load 
	 ******************************************************************************/
	executeQueryRequest(query, saveToHistory, timeframe, parameters){
		
		var params = {action: "execute"
			, item: "query"
			, query: query
			, saveToHistory: saveToHistory
			, timeframe: JSON.stringify(timeframe)
			, parameters: JSON.stringify(parameters)
		};
		
		//-----------------------------------
		// Do Execution
		
		//cfw_query_toggleLoading(true);
		var queryEditor = this;
		
		this.toggleLoading(true);
		
		CFW.http.postJSON(CFW_QUERY_URL, params, 
			function(data) {
				
				if(data.success){
					
					if(queryEditor.settings.resultDiv != null){
						cfw_query_renderAllQueryResults(queryEditor.settings.resultDiv, data.payload);
					}else{
						
						// use autocomplete wrapper to get it closed when clicking outside of div
						var resultWrapper = $('<div class="autocomplete-wrapper p-2 monospace">');
						resultWrapper.attr('onclick', 'event.stopPropagation();');
						
						CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV.append(resultWrapper);					
						cfw_query_renderAllQueryResults(resultWrapper, data.payload);
					}
					
				}
				queryEditor.toggleLoading(false);
				
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
		if(this.isExecuting == true){
			return;
		}

		var timeframe = JSON.parse($('#'+this.settings.timeframePickerID).val());
	
		var originalQuery =  this.textarea.val();
	
		if(CFW.utils.isNullOrEmpty(originalQuery)){
			return;
		}
		this.isExecuting = true;
						
		//-----------------------------------
		// Update Params in URL
		
		var queryLength = encodeURIComponent(originalQuery).length;
		var finalLength = queryLength + CFW.http.getHostURL().length + CFW.http.getURLPath().length ;
		
		if(finalLength+300 > JSDATA.requestHeaderMaxSize){
			CFW.ui.addToastInfo("The query is quite long and the URL might not work. Make sure to save a copy of your query.");
		}
		
		if(this.settings.useURLParams){
			
			var doPushHistoryState = !isPageLoad;
			CFW.http.setURLParams({
					  "query": originalQuery
					, "offset": timeframe.offset
					, "earliest": timeframe.earliest
					, "latest": timeframe.latest
				}, doPushHistoryState);
		}
		
		//-----------------------------------			
		// hide existing messages to not confuse user
		$('.toast.show').removeClass('show').addClass('hide');
		
		//-----------------------------------
		// Revert Button Highlighting
		this.highlightExecuteButton(false);
		
		//-----------------------------------
		// Prepare Parameters
		var pageParams;
		var finalQuery = originalQuery;
		if (typeof cfw_parameter_getFinalParams !== "undefined") { 
 			pageParams = cfw_parameter_getFinalParams(CFW_DASHBOARD_PARAMS);
			//finalQuery = cfw_parameter_substituteInString(originalQuery, pageParams);
		} 
		
		var queryParams = {};
		if(pageParams != null){
			for(var index in pageParams){
				var current = pageParams[index];
				queryParams[current.NAME] = current.VALUE;
			}
		}
			
		//-----------------------------------
		// Do Execution
		
		var queryEditor = this;
		var saveToHistory = true;
		
		this.executeQueryRequest(finalQuery, saveToHistory, timeframe, queryParams);

	}

}