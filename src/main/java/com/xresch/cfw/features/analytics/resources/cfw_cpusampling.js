
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/

/******************************************************************
 * Global
 ******************************************************************/
var GLOBAL_SIGNATURES = {};
var TOP_ELEMENTS = [];
var BOTTOM_ELEMENTS = [];

var GUID = 0;

// TopDown | BottomUp
var CURRENT_TAB = "TopDown";

/******************************************************************
 * Print the overview of the apis.
 * 
 ******************************************************************/
function cfw_cpusampling_prepareData(data){
		
	//------------------------------------------
	// Initialize
	var signatures = data.payload.signatures;
	GLOBAL_SIGNATURES = {}
	TOP_ELEMENTS = [];
	BOTTOM_ELEMENTS = [];

	
	//------------------------------------------
	// Convert Signatures
	for(let i = 0; i < signatures.length; i++){
		let current = signatures[i];
		GLOBAL_SIGNATURES[current.PK_ID] = 
			{ 
				id:			current.PK_ID, 
				signature: 	current.SIGNATURE, 
				parents: 	[],
				children: 	[],
				totalCallsTopDown: 0,
				totalCallsBottomUp: 0
			};
	}
	
	//------------------------------------------
	// Create Parent to Child relations and find
	// top elements
	var timeseries = data.payload.timeseries;

	for(let i = 0; i < timeseries.length; i++){
		let current = timeseries[i];
		let signatureID = current.FK_ID_SIGNATURE;
		let parentID = current.FK_ID_PARENT;
		if(current.FK_ID_PARENT == null){
			let signature = GLOBAL_SIGNATURES[current.FK_ID_SIGNATURE]
			if(!TOP_ELEMENTS.includes(signature)){
				TOP_ELEMENTS.push(signature);
			}
		}else{
			//------------------------
			// Push Children
			GLOBAL_SIGNATURES[parentID].children.push(current);
			
			//------------------------
			// Push Parent
			for(let j = i-1; j >= 0; j --){
				let potentialParentTimes = timeseries[j];
				if(potentialParentTimes.FK_ID_SIGNATURE == parentID){
					GLOBAL_SIGNATURES[signatureID].parents.push(potentialParentTimes);
					break;
				}
			}
			
		}
	}
		
	//------------------------------------------
	// Sort, calculate Percentages and find bottoms
	for(let id in GLOBAL_SIGNATURES){

		let current = GLOBAL_SIGNATURES[id];
		let children = current.children;
		let parents = current.parents;

		CFW.array.sortArrayByValueOfObject(children, 'COUNT', true);
		
		//------------------------------------
		// Ignore if never used
		if(children.length == 0 && parents.length == 0){
			continue;
		}
		//------------------------------------
		// Check Bottom call
		if(children.length == 0){
			BOTTOM_ELEMENTS.push(current);
			
		}
		
		//------------------------------------
		// Get Total Count Top Down
		current.totalCallsTopDown = 0;
		for(let i = 0; i < children.length; i++){
			current.totalCallsTopDown += children[i].COUNT;
		}
		
		//------------------------------------
		// Get Total Count Bottom Up
		current.totalCallsBottomUp = 0;
		for(let i = 0; i < parents.length; i++){
			current.totalCallsBottomUp += parents[i].COUNT;
		}
		
		//------------------------------------
		// Calculate Percentage Top Down
		for(let i = 0; i < children.length; i++){
			children[i].percentageTopDown = (children[i].COUNT / current.totalCallsTopDown) * 100;
		}
		
		//------------------------------------
		// Calculate Percentage Bottom Up
		for(let i = 0; i < parents.length; i++){
			parents[i].percentageBottomUp = (parents[i].COUNT / current.totalCallsBottomUp) * 100;
		}
	}
	
	
	cfw_cpusampling_printTabContent();
}

/******************************************************************
 * Print the overview of the apis .
 * 
 ******************************************************************/
function cfw_cpusampling_printTabContent(){
		
	switch(CURRENT_TAB){
		case 'TopDown': 	cfw_cpusampling_printTopDown();
							break;
						
		case 'BottomUp': 	cfw_cpusampling_printBottomUp();
							break;
	}
	
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_cpusampling_printTopDown(){
	
	parent = $("#cpu-samppling-tabcontent");
	parent.html('');
	
	//------------------------------------------
	// Calculate Percentages for top elements
	var totalTopCalls = 0;
	for(id in TOP_ELEMENTS){
		totalTopCalls += TOP_ELEMENTS[id].totalCallsTopDown;
	}
	
	CFW.array.sortArrayByValueOfObject(TOP_ELEMENTS, 'totalCallsTopDown', true);
	
	//------------------------------------------
	// Create Hierarchy
	for(var id in TOP_ELEMENTS){
		var current = TOP_ELEMENTS[id];
		current.percentageTopDown = (current.totalCallsTopDown / totalTopCalls)*100;
		cfw_cpusampling_printHierarchyDiv(parent, current, id)
	}
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_cpusampling_printBottomUp(){
	
	parent = $("#cpu-samppling-tabcontent");
	parent.html('');
	
	//------------------------------------------
	// Calculate Percentages for top elements
	var totalBottomCalls = 0;
	for(var id in BOTTOM_ELEMENTS){
		totalBottomCalls += BOTTOM_ELEMENTS[id].totalCallsBottomUp;
	}
	
	CFW.array.sortArrayByValueOfObject(BOTTOM_ELEMENTS, 'totalCallsBottomUp', true);
	
	//------------------------------------------
	// Create Hierarchy
	for(var id in BOTTOM_ELEMENTS){
		var current = BOTTOM_ELEMENTS[id];
		current.percentageBottomUp = (current.totalCallsBottomUp / totalBottomCalls)*100;
		cfw_cpusampling_printHierarchyDiv(parent, current, id)
	}
	
}



/******************************************************************
 * 
 ******************************************************************/
function cfw_cpusampling_printHierarchyDiv(domTarget, element, parentID){
	
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
	
	if(element.signature == undefined){
		id = element.FK_ID_SIGNATURE;
		title = GLOBAL_SIGNATURES[element.FK_ID_SIGNATURE].signature;
	}else{
		id = element.id;
		title = element.signature;
	}	
	
	//-------------------------------------
	// Check Recursion
	var signatureID = 'signature-'+id;
		
	var isRecursion = false;
	var recursiveLabel = "";

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
					     	+' onclick="cfw_cpusampling_printChildren(this)"'
					     	+' onkeydown="cfw_cpusampling_navigateChildren(event, this)" >'
					     		+ Math.round(percentage)+'% - '+title
					     	+'</a>'
					     	+ recursiveLabel
					     + '</div>'
					     + '<div id="'+childcontainerID+'" class="cfw-cpusampling-children w-100" style=" padding-left: 15px;">'
				   + '</div>';
	
	   
	domTarget.append(htmlString);
	
}

/******************************************************************
 * Print hierarchy div.
 * 
 ******************************************************************/
function cfw_cpusampling_printChildren(domElement){
	
	var titleLink = $(domElement);
	if(titleLink.attr('data-recursive') == "true") return;
	var guid = titleLink.attr('data-guid');
	var signatureID = titleLink.attr('data-signatureid');
	
	
	var domTarget = $("#children-of-"+guid);
	var element = GLOBAL_SIGNATURES[signatureID];
	
	//------------------------------
	// Change link to collape
	titleLink.attr('onclick','cfw_cpusampling_collapseChildren(this)');
	
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
		
		cfw_cpusampling_printHierarchyDiv(domTarget, children[i], signatureID);
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_cpusampling_navigateChildren(e, domElement){
	
	var elementLink = $(domElement);
	var guid = elementLink.attr('data-guid');
	
	var elementChildren = $("#children-of-"+guid);
	
	var currentElement = $(elementLink).parent().parent();
	var parentLink = currentElement.parent().parent().find('.card-header a').first();
	var parentChildren = parentLink.parent().next();
	
	//---------------------------
	// Right Arrow
	// Open children
	if (e.keyCode == 39) {
		
		  if(elementChildren.children().length == 0){
			  cfw_cpusampling_printChildren(domElement);
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
		  
		  parentLink.focus();
		  parentChildren.css('display', 'none');
		  return;
	}
	
	//---------------------------
	// Up Arrow 
	if (e.keyCode == 38) { 
		
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
function cfw_cpusampling_collapseChildren(domElement){
	
	var titleLink = $(domElement);
	var guid = titleLink.attr('data-guid');

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
function fetchAndRenderForSelectedTimeframe(){
	var earliestMillis = $('#EARLIEST').val();
	var latestMillis = $('#LATEST').val();
	CFW.http.getJSON("./cpusampling", {action: "fetch", item: "cpusampling", EARLIEST: earliestMillis, LATEST: latestMillis }, cfw_cpusampling_prepareData);
	
}
/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_cpusampling_draw(){
	
	CFW.ui.toggleLoader(true);
		
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");

		//-------------------------------------
		// Create Pills
		var pills = $("#cpu-sampling-pills");
		if(pills.length == 0){


			parent.append('<ul class="nav nav-pills pt-3 pb-3" id="cpu-sampling-pills" role="tablist">'
				+ '<li class="nav-item"><a class="nav-link active" data-toggle="pill" href="#" role="tab" onclick="CURRENT_TAB =  \'TopDown\'; cfw_cpusampling_printTopDown();"><i class="fas fa-sort-amount-down mr-2"></i>Top Down</a></li>'
				+'<li class="nav-item"><a class="nav-link" data-toggle="pill" href="#" role="tab" onclick="CURRENT_TAB =  \'BottomUp\'; cfw_cpusampling_printBottomUp();"><i class="fas fa-sort-amount-up mr-2"></i>Bottom Up</a></li>'
			+'<ul>');
		
			parent.append(pills);
		}
		
		//-------------------------------------
		// Create Tab Content Div
		var tree = $("#cpu-samppling-tabcontent");
		if(tree.length == 0){
			parent.append("<div id=\"cpu-samppling-tabcontent\"></div>");
		}
		
		//-------------------------------------
		// Fetch Data and Display
		fetchAndRenderForSelectedTimeframe();
		
		CFW.ui.toggleLoader(false);
	}, 100);
}