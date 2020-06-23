
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/


/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_config_changeToPanels(){
	
	var categoriesArray = JSON.parse(JSDATA.categories);
	for(key in categoriesArray){
		var category = categoriesArray[key];
		
		 var panelSettings = {
					cardstyle: 'success',
					textstyle: null,
					textstyleheader: 'white',
					title: category,
					body: $('<div>'),
			};
		 
		//-------------------------
		// Move inputs to panel
		$('form > .form-group *[data-category="'+category+'"]')
			.each(function(index, element){
				var parentGroup = $(element).closest('.form-group');
				panelSettings.body.append(parentGroup);
			});
		
		//-------------------------
		// Add Panel to Form
		var cfwPanel = new CFWPanel(panelSettings);
		$('form').prepend(cfwPanel.getPanel());
	}
}

