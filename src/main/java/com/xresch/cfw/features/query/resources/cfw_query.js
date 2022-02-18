
var CFW_QUERY_URLPARAMS = CFW.http.getURLParamsDecoded();
var CFW_QUERY_URL="/app/query";

//The query textarea containing the unformatted text
var $QUERYAREA;
//The code element that contains the highlighted syntax
var $QUERYCODE;

/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_query_execute(){
	
	var targetDiv = $('#cfw-query-results');
	var timeframe = JSON.parse($('#timeframePicker').val());
	var query =  $QUERYAREA.val();
	
	params = {action: "execute"
			, item: "query"
			, query: query
			, offset: timeframe.offset
			, earliest: timeframe.earliest
			, latest: timeframe.latest
			};
	
	cfw_query_highlightExecuteButton(false);
	cfw_ui_toogleLoader(true);	
	
	CFW.http.getJSON(CFW_QUERY_URL, params, 
		function(data) {
						
			if(data.success){
				
				//-----------------------------------
				// Add Params to URL
				CFW.http.removeURLParam('query');
				
				if(timeframe.offset != null){
					CFW.http.removeURLParam('earliest');
					CFW.http.removeURLParam('latest');
					CFW.http.setURLParam('offset', timeframe.offset);
				}else{
					CFW.http.setURLParam('earliest', timeframe.earliest);
					CFW.http.setURLParam('latest', timeframe.latest);
					CFW.http.removeURLParam('offset');
				}
				CFW.http.setURLParam('query', query);
						
				//-----------------------------------
				// Iterate all Query results
				for(var i = 0; i < data.payload.length; i++){
					var currentResults = data.payload[i];
						
					cfw_query_renderQueryResult(targetDiv, currentResults);
				}
				
			}
			
		cfw_ui_toogleLoader(false);	
	});
}

/*******************************************************************************
 * To Be Done
 * 
 ******************************************************************************/

function cfw_query_editor_refreshHighlighting() {
	
	$QUERYCODE.html($QUERYAREA.val());
	hljs.highlightElement($QUERYCODE.get(0));
	
}

/*******************************************************************************
 * Copy indentation from current line.
 * 
 ******************************************************************************/
function cfw_query_editor_handleEnter(domElement){
		
	console.log("cfw_query_editor_handleEnter");
	
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
	
	console.log("line:"+line);
	console.log("indexFirstNonWhitespace:"+indexFirstNonWhitespace);
	console.log("indentation:"+indentation);
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
function cfw_query_editor_resizeToFitQuery(){
	
	var value =  $QUERYAREA.val();
	if( !CFW.utils.isNullOrEmpty(value) ){
		
		var queryHeight = $QUERYAREA[0].scrollHeight;
		var queryWidth = $QUERYAREA[0].scrollWidth-10;
		
		 $QUERYAREA.css("height", queryHeight+'px'); 
		 $QUERYAREA.css("width", queryWidth+'px'); 
		
		$QUERYCODE.css("height", queryHeight+"px");
		$QUERYCODE.css("width", queryWidth+"px");
		
		var editorHeight = queryHeight+10;
		if(editorHeight > 500){ editorHeight = 500; };
		$('.query-editor').css('height',editorHeight+"px")
		
	}
	
}

/*******************************************************************************
  * 
 * @param direction 'up' or 'down' 
 ******************************************************************************/
function cfw_query_editor_copyCurrentLine(direction, domElement){
	

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
	// Insert Line
	var line = value.substring(indexLineStart, indexLineEnd);	
	
	// set textarea value to: text before caret + tab + text after caret
	value = value.substring(0, indexLineStart) 
					+line
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
function cfw_query_editor_handleTab(domElement, direction){
	
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
 * 
 ******************************************************************************/
function cfw_query_highlightExecuteButton(enableHighlighting){
	
	if(enableHighlighting){
		$("#executeButton").addClass('btn-warning')
		$("#executeButton").removeClass('btn-btn-primary')
	}else{
		$("#executeButton").removeClass('btn-warning')
		$("#executeButton").addClass('btn-btn-primary')
	}
	
}
		
/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_query_initialDraw(){
	
	//-----------------------------------
	// Prepare HTML Base
	var parent = $('#cfw-container');
	var formID = JSDATA.formID;
	parent.css('max-width', '100%');
	
	parent.append(`
		<div id="cfw-query-content-wrapper">
			<div class="row mb-2">
				<div class="col-12 d-flex justify-content-end">
					<input id="timeframePicker" name="timeframePicker" type="text" class="form-control">
					<button type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-save"></i></button>
					<button type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-star"></i></button>
					<button type="button" class="btn btn-sm btn-primary ml-2" onclick="alert('save!')"><i class="fas fa-history"></i></button>
					<button id="executeButton" type="button" class="btn btn-sm btn-primary ml-2" onclick="cfw_query_execute();"><b>Execute</b></button>
				</div>
				
			</div>
			<div class="row">
				<div class="col-12">
					<div class="query-editor">
						<div class="scroll-fix" style="position: relative; height: auto; ">
							<form id="${formID}">
								<input id="cfw-formID" name="cfw-formID" type="hidden" value="${formID}">
								<textarea id="query" name="query" class="form-control query-text-format" rows="3" placeholder="Write your query. \r\n Ctrl+Space for content assist. \r\n Ctrl+Enter to execute."></textarea>
								<pre id="query-pre-element"><code id="query-highlighting" class="preview language-cfwquery query-text-format"> </code></pre>
							</form>
						</div>
					</div>
					<div id="query-autocomplete-results"></div>
				</div>
			</div>
			
		</div>
		
		<div id="cfw-query-results" class="monospace">
		</div>
	`);
	
	$QUERYAREA = $('#query');
	$QUERYCODE = $('#query-highlighting');
	//-------------------------------------------------
	// Initialize Autocomplete, trigger with Ctrl+Space
	cfw_autocompleteInitialize(formID,'query',0,10, null, true, $('#query-autocomplete-results'));
	
	//-----------------------------------
	// Load Timeframe from URL or set default
	if(!CFW.utils.isNullOrEmpty(CFW_QUERY_URLPARAMS.offset)){
		cfw_initializeTimeframePicker('timeframePicker'
						, {offset: CFW_QUERY_URLPARAMS.offset}
						, function(){ cfw_query_highlightExecuteButton(true); } );
	}else{
		if(!CFW.utils.isNullOrEmpty(CFW_QUERY_URLPARAMS.earliest)
		&& !CFW.utils.isNullOrEmpty(CFW_QUERY_URLPARAMS.latest) ){
			cfw_initializeTimeframePicker('timeframePicker', {earliest: CFW_QUERY_URLPARAMS.earliest, latest: CFW_QUERY_URLPARAMS.latest}, null);
		}else{
			cfw_initializeTimeframePicker('timeframePicker', {offset: '1-h'}, null);
		}
	}
	
	//-----------------------------------
	// Load Query from URL
	if( !CFW.utils.isNullOrEmpty(CFW_QUERY_URLPARAMS.query) ){

		$QUERYAREA.val(CFW_QUERY_URLPARAMS.query);

		cfw_query_editor_resizeToFitQuery();
		cfw_query_editor_refreshHighlighting();
		cfw_query_execute();

	}else{
		//cfw_query_editor_resizeToFitQuery();
		cfw_query_editor_refreshHighlighting();
	}
	
	//-----------------------------------
	// Query Field Event Handler
	$QUERYAREA.on("keydown", function(e){
		
		cfw_query_highlightExecuteButton(true);
		
		//---------------------------
		// Ctrl + Alt + Up
		if (e.ctrlKey && e.altKey && e.keyCode == 38) {
			cfw_query_editor_copyCurrentLine('up', this);
			return;
		}
		
		//---------------------------
		// Ctrl + Alt + Down
		if (e.ctrlKey && e.altKey && e.keyCode == 40) {
			cfw_query_editor_copyCurrentLine('down', this);
			return;
		}
		
		//---------------------------
		// Shift+Tab: Decrease Indentation
		if (e.shiftKey && e.key == 'Tab') {
		    e.preventDefault();
			cfw_query_editor_handleTab(this, "decrease");
			return;
		}
			
		//---------------------------
		// Allow Tab for Indentation
		if (e.key == 'Tab') {
		    e.preventDefault();
			cfw_query_editor_handleTab(this, "increase");
		}
		

		
		//---------------------------
		// Ctrl + Enter
		if (e.ctrlKey && e.keyCode == 13) {
			cfw_query_execute();
			return;
		}
		
		//---------------------------
		// Enter
		if (e.keyCode == 13) {
			e.preventDefault();
			cfw_query_editor_handleEnter(this);
			cfw_query_editor_resizeToFitQuery();
			
			return;
		}
		
	});
	
	//-----------------------------------
	// Refresh highlight on keyup/paste
	$QUERYAREA.on("keyup", function(e){
		cfw_query_editor_refreshHighlighting();
	});
	
	$QUERYAREA.on("paste", function(e){
		window.setTimeout(function(){
			cfw_query_editor_resizeToFitQuery();
			cfw_query_editor_refreshHighlighting();
		}, 100);
	});
	
	// needed for autocomplete, select with enter
	$QUERYAREA.on("change", function(e){
		cfw_query_editor_refreshHighlighting();
	});
	
	// needed for autocomplete, select with click
	$QUERYAREA.on("focus", function(e){
		cfw_query_editor_refreshHighlighting();
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
	
				cfw_query_editor_resizeToFitQuery();
	        }
	    }, 1000);
	});
	
		
}