
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/


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
				'<li class="nav-item"><a class="nav-link active" id="tab-mygroups" data-toggle="pill" href="#" role="tab" onclick="cfw_usermgmt_groups_draw({tab: \'mygroups\'})"><i class="fas fa-users mr-2"></i>My Groups</a></li>'
				+'<li class="nav-item"><a class="nav-link ml-2" id="tab-allgroups" data-toggle="pill" href="#" role="tab" onclick="cfw_usermgmt_groups_draw({tab: \'allgroups\'})"><i class="fas fa-users mr-2"></i>All Groups</a></li>'
			);

		
		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

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
function cfw_usermgmt_groups_initialDraw(options){
	
	$('#cfw-container').css('max-width', '80%');
	
	cfw_usermgmt_setScopeGroups();
	cfw_usermgmt_groups_createTabs();
	
	cfw_usermgmt_groups_draw(options);
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
	
	$("#tab-content").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){

		switch(options.tab){
									
			case "mygroups":		CFW.http.fetchAndCacheData(CFW_USERMGMT_URL, {action: "fetch", item: "mygroups"}, "mygroups", cfw_usermgmt_printGroupListCanEdit);
									break;
									
			case "allgroups":		CFW.http.fetchAndCacheData(CFW_USERMGMT_URL, {action: "fetch", item: "groups"}, "allgroups", cfw_usermgmt_printGroupListOverview);
									break;		
																
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 100);
}