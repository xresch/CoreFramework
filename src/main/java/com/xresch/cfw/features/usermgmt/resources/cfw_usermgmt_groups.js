
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
				'<li class="nav-item"><a class="nav-link active" id="tab-groups" data-toggle="pill" href="#" role="tab" onclick="cfw_usermgmt_groups_draw({tab: \'groups\'})"><i class="fas fa-users mr-2"></i>Groups</a></li>'
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
									
			case "groups":			CFW.http.fetchAndCacheData(CFW_USERMGMT_URL, {action: "fetch", item: "mygroups"}, "groups", cfw_usermgmt_printGroupList);
			break;
												
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 100);
}