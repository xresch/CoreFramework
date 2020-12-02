
/**************************************************************************************************************
 * This file contains various javascript examples.
 * For your convenience, this code is highly redundant for eassier copy&paste.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 **************************************************************************************************************/

var SORTHIERARCHY_URL = "./sorthierarchy";
var JSEXAMPLES_LAST_OPTIONS = null;
var URL_PARAMS=CFW.http.getURLParams();


/******************************************************************
 * Delete
 ******************************************************************/
//function jsexamples_duplicate(id){
//	
//	params = {action: "duplicate", item: "person", id: id};
//	CFW.http.getJSON(SORTHIERARCHY_URL, params, 
//		function(data) {
//			if(data.success){
//				CFW.cache.clearCache();
//				jsexamples_draw(JSEXAMPLES_LAST_OPTIONS);
//			}
//	});
//}

/******************************************************************
 *
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_sorthierarchy_printSortableHierarchy(data){
	
	var parent = $("#cfw-container");
	parent.html("");
	parent.append("<h1>Sort Hierarchy<h1>");
	
	//--------------------------------
	// Button	
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		//-----------------------------------
		// Render Data
		var rendererSettings = {
				data: data.payload,
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titleformat: '{0}',
			 	visiblefields: ['PK_ID', 'FIRSTNAME', 'LASTNAME', 'LOCATION', "EMAIL", "LIKES_TIRAMISU"],
			 	labels: {
			 		PK_ID: "ID",
			 	},
			 	customizers: {
			 		LIKES_TIRAMISU: function(record, value) { 
			 			var likesTiramisu = value;
			 			if(likesTiramisu){
								return '<span class="badge badge-success m-1">true</span>';
						}else{
							return '<span class="badge badge-danger m-1">false</span>';
						}
			 			 
			 		}
			 	},
				//actions: actionButtons,
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
				
				rendererSettings: {
					hierarchy_sorter: {narrow: false, filterable: true}
				},
			};
				
		var renderResult = CFW.render.getRenderer('hierarchy_sorter').render(rendererSettings);	
		
		parent.append(renderResult);
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no items can be displayed.');
	}
}

/******************************************************************
 * Main function
 ******************************************************************/
function cfw_sorthierarchy_draw(){
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		
		CFW.http.fetchAndCacheData(SORTHIERARCHY_URL, {type: URL_PARAMS.type, action: "fetch", item: "hierarchy", rootid: 111}, "hierarchy", cfw_sorthierarchy_printSortableHierarchy);
			
		CFW.ui.toogleLoader(false);
	}, 50);
}