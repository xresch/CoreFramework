
/**************************************************************************************************************
 * This file contains various javascript examples.
 * For your convenience, this code is highly redundant for eassier copy&paste.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 **************************************************************************************************************/

var CFW_FEATUREMGMT_URL = "./featuremanagement";

/******************************************************************
 * 
 ******************************************************************/
function cfw_feature_mgmt_createFeatureToggle(){
	var parent = $("#cfw-container");
	
	parent.append(
			'<h1>Feature Management</h1>'
			+'<p>On this page you can enable and disable the features which are managable. Application restart is required for the changes to take effect.</p>'
			+'<p class="bg-cfw-danger text-cfw-white p-2">IMPORTANT:</b> Disabling Features that where already used can lead to errors in the application.</p>'
		);
	
	CFW.http.getJSON(CFW_FEATUREMGMT_URL, {action: "fetch", item: "featurelist"}, 
		function(data) {
			if(data.payload != null){
				
				var htmlString = "";
				var cfwTable = new CFWTable({narrow: true});
				
				cfwTable.addHeaders(['&nbsp;',
									"Name",
									"Description"]);
				
				var resultCount = data.payload.length;
				
				if(resultCount == 0){
					CFW.ui.addAlert("info", "Hmm... seems there aren't any features that can be managed.");
				}

				for(var i = 0; i < resultCount; i++){
					var current = data.payload[i];
					var row = $('<tr>');
					
					//Toggle Button
					var params = {action: "update", item: "feature", name: current.NAME};
					var cfwToggleButton = CFW.ui.createToggleButton(CFW_FEATUREMGMT_URL, params, current.VALUE);
					
					var buttonCell = $("<td>");
					cfwToggleButton.appendTo(buttonCell);
					row.append(buttonCell);
					
					row.append('<td>'+current.NAME+'</td>'
							  +'<td>'+CFW.utils.nullTo(current.DESCRIPTION, '')+'</td>');
					
					cfwTable.addRow(row);
				}
				
				
				cfwTable.appendTo(parent);
				
			}else{
				CFW.ui.addAlert('error', '<span>Error loading the list of features.</span>');
			}	
		}
	);
}

