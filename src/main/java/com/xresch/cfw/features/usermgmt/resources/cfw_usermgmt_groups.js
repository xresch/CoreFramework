
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/

var CFW_GROUPS_LAST_OPTIONS = null;

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_usermgmt_groups_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		//--------------------------------
		// Groups
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-groups" data-toggle="pill" href="#" role="tab" onclick="cfw_usermgmt_groups_draw({tab: \'groups\'})"><i class="fas fa-users mr-2"></i>My Groups</a></li>'
				+'<li class="nav-item"><a class="nav-link ml-2" id="tab-allgroups" data-toggle="pill" href="#" role="tab" onclick="cfw_usermgmt_groups_draw({tab: \'allgroups\'})"><i class="fas fa-users mr-2"></i>All Groups</a></li>'
			);

		
		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}

/******************************************************************
 *
 ******************************************************************/
function cfw_usermgmt_groups_sanitizeCurrentOptions(options){
	
	//-----------------------
	// Options is Set
	if(options != null){ return options; }
	
	//-----------------------
	// Last Options Available
	if(CFW_GROUPS_LAST_OPTIONS != null ){
		return CFW_GROUPS_LAST_OPTIONS;
	}
	
	//-----------------------
	// Last Options From Cache
	// or Default
	let tabToDisplay = CFW.cache.retrieveValueForPage("groups-lasttab", "groups");
	

	$('#tab-'+tabToDisplay).addClass('active');
	
	return {tab: tabToDisplay};
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'users|roles|permissions', 
 *  }
 * @return 
 ******************************************************************/
function cfw_usermgmt_groups_initialDraw(){
	
	$('#cfw-container').css('max-width', '80%');
	
	cfw_usermgmt_setScopeGroups();
	cfw_usermgmt_groups_createTabs();
	
	//-------------------------------------------
	// Create Selector and Draw
	cfw_spaces_createSpaceSelector(function(spaceid){
			cfw_usermgmt_groups_draw(null);
		}, true);

}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'users|roles|permissions', 
 *  }
 * @return 
 ******************************************************************/
function cfw_usermgmt_groups_draw(options){
	
	//-------------------------
	// Options
	options = cfw_usermgmt_groups_sanitizeCurrentOptions(options); 
	CFW_GROUPS_LAST_OPTIONS = options;
	CFW.cache.storeValueForPage("groups-lasttab", options.tab);
	
	//-------------------------
	// Clear Tab
	$("#tab-content").html("");
	
	//-------------------------
	// Draw
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){

		switch(options.tab){
									
			case "groups":			CFW.http.getJSON(CFW_USERMGMT_URL, {action: "fetch", item: "mygroups"}, cfw_usermgmt_printGroupListCanEdit);
									break;
									
			case "allgroups":		CFW.http.getJSON(CFW_USERMGMT_URL, {action: "fetch", item: "allgroups"}, cfw_usermgmt_printGroupListOverview);
									break;		
																
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 100);
}