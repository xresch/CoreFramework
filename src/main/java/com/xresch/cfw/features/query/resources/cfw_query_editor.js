
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
				manualDiv.css("background", $('body').css('background'))
				
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
		
		//-------------------------------------------
		// Settings
		//-------------------------------------------
		this.settings = {
			// the URL path of the query servlet
			queryURL: CFW_QUERY_URL
			// the div where the results should be sent to
			, resultDiv: null
		};
		
		this.settings = Object.assign({}, this.settings, settings);
		
		//-------------------------------------------
		// Setup
		//-------------------------------------------
		this.textarea.data("queryEditor", this);
		
		this.initializeEditor();

	}
	
	/*******************************************************************************
	 * 
	 ******************************************************************************/
	highlightExecuteButton(enableHighlighting){
		
		if(enableHighlighting){
			$("#executeButton").addClass('btn-warning')
			$("#executeButton").removeClass('btn-btn-primary')
		}else{
			$("#executeButton").removeClass('btn-warning')
			$("#executeButton").addClass('btn-btn-primary')
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
	/*******************************************************************************
	 * Copy indentation from current line.
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
			
				<div class="query-editor">
					<div id="query-button-menu" class="pb-2 pt-2">
						<div class="col-12 d-flex justify-content-start">
							<!-- input id="timeframePicker" name="timeframePicker" type="text" class="form-control" -->
							<!-- a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-save"></i></a>
							<a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-star"></i></a>
							<a type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-history"></i></a -->
							<a id="executeButton" type="button" class="btn btn-sm btn-primary ml-2" onclick="cfw_query_execute(false);"><b>Execute</b></a>
						</div>
					</div>
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
		
		queryEditorWrapper.append(CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV);
		this.createAutocompleteForm();
		
		var fieldname = this.textarea.attr('id');
		
		console.log("CFW_QUERY_EDITOR_AUTOCOMPLETE_FORM_ID:"+CFW_QUERY_EDITOR_AUTOCOMPLETE_FORM_ID)
		console.log("fieldname:"+fieldname)
		console.log(CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV)
		console.log(CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV.get(0))

		cfw_autocompleteInitialize(CFW_QUERY_EDITOR_AUTOCOMPLETE_FORM_ID, fieldname, 0,10, null, true, CFW_QUERY_EDITOR_AUTOCOMPLETE_DIV);
		
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
		this.refreshHighlighting();
		this.resizeToFitQuery();
				
		
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
				cfw_query_execute(false);
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

}