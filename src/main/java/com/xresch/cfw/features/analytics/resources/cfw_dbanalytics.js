
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/

/******************************************************************
 * Global
 ******************************************************************/



/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_printBottomUp(parent){
	
	parent = $("#cpu-samppling-tabcontent");
	parent.html('');
	
	//------------------------------------------
	// Calculate Percentages for top elements
	var totalBottomCalls = 0;
	for(id in BOTTOM_ELEMENTS){
		totalBottomCalls += BOTTOM_ELEMENTS[id].totalCallsBottomUp;
	}
	
	CFW.array.sortArrayByValueOfObject(BOTTOM_ELEMENTS, 'totalCallsBottomUp', true);
	
	//------------------------------------------
	// Create Hierarchy
	for(id in BOTTOM_ELEMENTS){
		current = BOTTOM_ELEMENTS[id];
		current.percentageBottomUp = (current.totalCallsBottomUp / totalBottomCalls)*100;
		cfw_dbanalytics_printHierarchyDiv(parent, current, id)
	}
	
}



/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_printHierarchyDiv(domTarget, element, parentID){
	
	var percentage;
	if(CURRENT_TAB == "TopDown"){
		percentage = element.percentageTopDown;
	}else{
		percentage = element.percentageBottomUp;
	}
	// domTarget is the child container
	//-------------------------------------
	// Initialize
	GUID += 1;
	var id = 0;
	var title = "";
	var children = [];
	
	if(element.signature == undefined){
		id = element.FK_ID_SIGNATURE;
		title = GLOBAL_SIGNATURES[element.FK_ID_SIGNATURE].signature;
		children = GLOBAL_SIGNATURES[element.FK_ID_SIGNATURE].children;
	}else{
		id = element.id;
		title = element.signature;
		children = element.children;
	}	
	
	//-------------------------------------
	// Check Recursion
	var signatureID = 'signature-'+id;
		
	var isRecursion = false;
	var recursiveLabel = "";
//	console.log("===================");
//	console.log("signatureID = "+signatureID);
//	console.log("domTarget.parent().attr('id') = "+domTarget.parent().attr('id'));
//	console.log("domTarget.closest('#'+signatureID).length = "+domTarget.closest('#'+signatureID).length);
	
	if(domTarget.closest('#'+signatureID).length > 0 ){
		recursiveLabel = '<div class="badge badge-danger ml-2">Recursion</div>';
		isRecursion = true;
		domTarget.closest('#'+signatureID).find('a').first().after(recursiveLabel);
	}
	
	//-------------------------------------
	// Create Div
	var childcontainerID = "children-of-"+GUID;
		
	var htmlString = '<div id="signature-'+id+'">'
						+ '<div class="card-header text-light bg-primary w-100 p-0">'
			 				+ '<div class="cfw-cpusampling-percent-block">'
				 				+ '<div class="cfw-cpusampling-percent bg-success" style="width: '+percentage+'%;">'
				 				+ '</div>'
			 				+ '</div>'
					     + '<a tabindex="0" role="button" class="text-light small"'
					     	+' id="link-of-'+GUID+'"'
					     	+' data-signatureid="'+id+'"'
					     	+' data-parentid="'+parentID+'"'
					     	+' data-guid="'+GUID+'"'
					     	+' data-recursive="'+isRecursion+'"'
					     	+' onclick="cfw_dbanalytics_printChildren(this)"'
					     	+' onkeydown="cfw_dbanalytics_navigateChildren(event, this)" >'
					     		+ Math.round(percentage)+'% - '+title
					     	+'</a>'
					     	+ recursiveLabel
					     + '</div>'
					     + '<div id="'+childcontainerID+'" class="cfw-cpusampling-children w-100" style=" padding-left: 15px;">'
				   + '</div>';
	
	//console.log(domTarget);		   
	domTarget.append(htmlString);
	
	//-------------------------------------
	// Handle children
//	var newLevel = level + 1;
//	for(var i = 0; i < children.length; i++ ){
//		cfw_dbanalytics_printHierarchyDiv(newLevel, domTarget, children[i]);
//	}
}

/******************************************************************
 * Print hierarchy div.
 * 
 ******************************************************************/
function cfw_dbanalytics_printChildren(domElement){
	
	var titleLink = $(domElement);
	if(titleLink.attr('data-recursive') == "true") return;
	var guid = titleLink.attr('data-guid');
	var parentID = titleLink.attr('data-parentid');
	var signatureID = titleLink.attr('data-signatureid');
	
//	console.log("guid-"+guid);
//	console.log("parentID-"+parentID);
//	console.log("signatureID-"+signatureID);
	
	var domTarget = $("#children-of-"+guid);
	var element = GLOBAL_SIGNATURES[signatureID];
	
	//------------------------------
	// Change link to collape
	titleLink.attr('onclick','cfw_dbanalytics_collapseChildren(this)');
	
	//------------------------------
	// Print Children
	domTarget.css('display', 'block');
	
	var children;
	if(CURRENT_TAB == "TopDown"){
		children = element.children;
	}else{
		children = element.parents;
	}
	
	for(var i = 0; i < children.length; i++ ){
		
		cfw_dbanalytics_printHierarchyDiv(domTarget, children[i], signatureID);
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_navigateChildren(e, domElement){
	
	var elementLink = $(domElement);
	var guid = elementLink.attr('data-guid');
	var parentID = elementLink.attr('data-parentid');
	var signatureID = elementLink.attr('data-signatureid');
	
	var elementChildren = $("#children-of-"+guid);
	
	var currentElement = $(elementLink).parent().parent();
	var parentLink = currentElement.parent().parent().find('.card-header a').first();
	var parentChildren = parentLink.parent().next();
	
	//---------------------------
	// Right Arrow
	// Open children
	if (e.keyCode == 39) {
		console.log("RIGHT");
		  if(elementChildren.children().length == 0){
			  cfw_dbanalytics_printChildren(domElement);
		  }else{
			  elementChildren.css('display', 'block');
		  }
		  elementChildren.find("a").first().focus();
		  return;
	}
	
	//---------------------------
	// Down Arrow 
	// Next element
	if (e.keyCode == 40) {
		console.log("DOWN");
		e.preventDefault();
		
		//------------------------
		// Use child if available
		if ( elementChildren.css('display') != "none" 
		  && elementChildren.children().length > 0){
			
			elementChildren.find('a').first().focus();

		}
		
		//------------------------
		// Use next sibling
		else{
			var currentParent = elementLink.parent().parent();
			var nextLink = currentParent.next().find('a').first();
			while(nextLink.length == 0 ){
				currentParent = currentParent.parent().parent();
				nextLink = currentParent.next().find('a').first();
			}
			nextLink.focus();
		}
	}
	
	//---------------------------
	// Left Arrow
	if (e.keyCode == 37) { 
		  console.log("LEFT");
		  parentLink.focus();
		  parentChildren.css('display', 'none');
		  return;
	}
	
	//---------------------------
	// Up Arrow 
	if (e.keyCode == 38) { 
		console.log("UP");
		e.preventDefault();
		
		var prevLink = elementLink.parent().parent().prev().find('a:visible').last().focus();
		if(prevLink.length == 0 ){
			  parentLink.focus();
		}else{
			prevLink.focus();
		}

		return;
	}
	
	//---------------------------
	// Enter
	if (e.keyCode == 13) {
		
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dbanalytics_collapseChildren(domElement){
	
	var titleLink = $(domElement);
	var guid = titleLink.attr('data-guid');
	var parentID = titleLink.attr('data-parentid');
	var signatureID = titleLink.attr('data-signatureid');
	
	var domTarget = $("#children-of-"+guid);

	if(domTarget.css('display') == "none"){
		domTarget.css('display', 'block');
	}else{
		domTarget.css('display', 'none');
	}
	
}

/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function createDatabaseSnapshot(){

	CFW.http.getJSON("./dbanalytics", {action: "dbsnapshot"});
	
}

/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function createDatabaseSnapshot(){

	CFW.http.getJSON("./dbanalytics", {action: "dbsnapshot"});
	
}
/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_dbanalytics_draw(){
	
	CFW.ui.toogleLoader(true);
		
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");
		parent.html('');
		
		//-------------------------------------
		// Create Actions
		
		parent.append('<h1>Database Analytics</h1>');

		parent.append('<div>'
				+ '<a class="btn btn-sm btn-primary" onclick="createDatabaseSnapshot();"><i class="fas fa-camera-retro mr-2"></i>Create Database Snapshot</a>'
			+'</div>');
		
				
		CFW.ui.toogleLoader(false);
	}, 100);
}