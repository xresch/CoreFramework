
/**************************************************************************************************************
 * This file contains the javscript code for building up the view of the space page.
 * @author Reto Scheiwiller, (c) Copyright 2026
 **************************************************************************************************************/
var URL_CFWSPACES = "/app/spaces";

var CFW_LAST_SELECTED_SPACE = "om-last-selected-space";
var CFW_SPACE_SELECT_ID = 'om-global-space-selector';

/******************************************************************
 * Creates a select field containing the orgs the user can select.
 *
 * @param callbackFunction callback function that will be called with the
 * last selected org id, or null if the user has no org.
 * 
 * @param isForOrganize set to true if the selector is for the org unit 
 * organize view
 ******************************************************************/
function cfw_spaces_createSpaceSelector(callbackFunction){
	
	//-------------------------------
	// Reset if exists
	let existingSelector = $('#'+CFW_SPACE_SELECT_ID);
	if(existingSelector.length > 0){
		existingSelector.parent().remove();
	}
	
	//-------------------------------
	// Create Selector
	let params = {action: "fetch", item: "spacesforuser"};
	CFW.http.getJSON(URL_CFWSPACES, params, 
		function(data) {
			
			let lastSelectedSpace = CFW.cache.retrieveValue(CFW_LAST_SELECTED_SPACE, 1, "session");

			if(data.success 
			&& data.payload != null
			&& data.payload.length > 0){
				
				let inputField = $('<input id="'+CFW_SPACE_SELECT_ID+'" >');
				inputField.data('callbackFunction', callbackFunction)
				
				let lastSelectedSpaceExists = false;
				
				//------------------------------
				// Create Select Options
				let valueLabelOptions = [];
				for(var index in data.payload){
					currentSpace = data.payload[index];
					
					valueLabelOptions.push( { 
						  "value": ""+currentSpace.PK_ID
						, "label": currentSpace.BREADCRUMBS
					});
					//select.append('<option value="'+currentSpace.PK_ID+'">'+ indendation + currentSpace.NAME + '</option>')
					
					if(currentSpace.PK_ID == lastSelectedSpace){
						lastSelectedSpaceExists = true;
						inputField.attr('value', lastSelectedSpace);
					}
				}
				
				valueLabelOptions = _.sortBy(valueLabelOptions, ['label']);
								
				//------------------------------
				// Set Selection
				if(!lastSelectedSpaceExists){
					inputField.attr('value', "");
				}
				
				//------------------------------
				// Create Dropdown Menu
				let navitem = $('<li class="dropdown-item pl-0" style="width: auto">');
				navitem.append(inputField);
				$('#cfw-navbar-right').prepend(navitem);
				
				cfw_initializeSelect(CFW_SPACE_SELECT_ID, valueLabelOptions, true, function(){
					cfw_spaces_onSpaceSelectorChange();
				});
				
				inputField.parent().find("button") // remove classes added by initialize Select
					  .removeClass()
					  .addClass("dropdown-toggle");
				
				CFW.cache.storeValue(CFW_LAST_SELECTED_SPACE, inputField.val());
				
				//------------------------------
				// Create filter button
				let filterInclusive = JSDATA.filterSpaceInclusive;
				let icon = cfw_spaces_getFilterIcon(filterInclusive);
				
				let filterButton = $('<li class="cfw-button-menuitem" title="Toggle if you want to see only items in this space, or all items accessible from this space.">'
						+ '<a class="dropdown-item" id="cfwMenuButtons-filterSpace" onclick="cfw_spaces_toogleFilter(this)">'
						    + '<div class="cfw-fa-box"><i class="fas '+icon+'"></i></div>'
							+ '<span class="cfw-menuitem-label">Space Filter</span>'
						+ '</a>'
					+ '</li>');
						
				$('#cfw-navbar-right').prepend(filterButton);
			}
			
			cfw_spaces_onSpaceSelectorChange();
			
	});
}

/******************************************************************
 *
 ******************************************************************/
function cfw_spaces_onSpaceSelectorChange(){
	let select = $('#'+CFW_SPACE_SELECT_ID);
	
	let callbackFunction = select.data('callbackFunction');
	let selectedSpace = select.val();
	CFW.cache.storeValue(CFW_LAST_SELECTED_SPACE, selectedSpace, "session");
	
	console.log("cfw_spaces_onSpaceSelectorChange")
	CFW.http.getJSON(URL_CFWSPACES, { action: "update", item: "selectedspaceid", spaceid: selectedSpace});
	
	callbackFunction(selectedSpace);
	
}

/******************************************************************
 *
 ******************************************************************/
function cfw_spaces_getSpaceSelector(){
	 return $('#'+CFW_SPACE_SELECT_ID);
}

/******************************************************************
 * Returns the id of the selected org
 ******************************************************************/
function cfw_spaces_getSelectedSpace(){
	 return $('#'+CFW_SPACE_SELECT_ID).val();
}

/******************************************************************
 *
 ******************************************************************/
function cfw_spaces_setSelectedSpace(orgid){
	 $('#'+CFW_SPACE_SELECT_ID).val(orgid);
	 cfw_spaces_onSpaceSelectorChange();
}

/******************************************************************
 *
 ******************************************************************/
function cfw_spaces_getFilterIcon(filterInclusive){

	return (filterInclusive) 
		? 'fa-filter-circle-xmark'
		: 'fa-filter  text-cfw-yellow' 
		;

}
/******************************************************************
 *
 ******************************************************************/
function cfw_spaces_toogleFilter(eventSource){
	
	//---------------------------
	// Reverse Filtering
	let select = $('#'+CFW_SPACE_SELECT_ID);
	let callbackFunction = select.data('callbackFunction');
	let selectedSpace = select.val();
	let filterInclusiveToggled = ! JSDATA.filterSpaceInclusive;
	//---------------------------
	// Reverse Filtering
	let icon = cfw_spaces_getFilterIcon( filterInclusiveToggled );
	$(eventSource).find('i')
			.removeClass()
			.addClass('fas '+icon);
	
	JSDATA.filterSpaceInclusive = filterInclusiveToggled;
	
	CFW.http.getJSON(URL_CFWSPACES, { action: "update", item: "filterSpaceInclusive", filterSpaceInclusive: filterInclusiveToggled});
	
	callbackFunction(selectedSpace);
	
}
