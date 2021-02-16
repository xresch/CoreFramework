
var CFW_DASHBOARD_URLPARAMS = CFW.http.getURLParamsDecoded();
var CFW_DASHBOARD_PARAMS = null;
	
var CFW_DASHBOARD_EDIT_MODE = false;
var CFW_DASHBOARD_EDIT_MODE_ADVANCED = true;
var CFW_DASHBOARD_FULLSCREEN_MODE = false;
var CFW_DASHBOARD_REFRESH_INTERVAL_ID = null;
var CFW_DASHBOARD_WIDGET_REGISTRY = {};

//saved with guid
var CFW_DASHBOARD_WIDGET_DATA = {};
var CFW_DASHBOARD_WIDGET_GUID = 0;

var CFW_DASHBOARDVIEW_URL = "/app/dashboard/view";

//-------------------------------------
// Globals needed for UNDO and REDO
//-------------------------------------
// Array of Command Bundles 
var CFW_DASHBOARD_COMMAND_HISTORY = [];
// Position in the History
var CFW_DASHBOARD_HISTORY_POSITION = 0;
// Bundle of commands to redo/undo
var CFW_DASHBOARD_COMMAND_BUNDLE = null;

var CFW_DASHBOARD_TIME_ENABLED = false;
var CFW_DASHBOARD_TIME_PRESET = "30m";
var CFW_DASHBOARD_TIME_EARLIEST_EPOCH = moment().utc().subtract(30, 'm').utc().valueOf();
var CFW_DASHBOARD_TIME_LATEST_EPOCH = moment().utc().valueOf();
/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_timeframe_setPreset(preset){

	
	window.localStorage.setItem("dashboard-timeframe-preset-"+CFW_DASHBOARD_URLPARAMS.id, preset);

	var label = $("#time-preset-"+preset).text();
	$('#timeframeSelectorButton').text(label);

	var split = preset.split('-');
	CFW_DASHBOARD_TIME_EARLIEST_EPOCH = moment().utc().subtract(split[0], split[1]).utc().valueOf();
	CFW_DASHBOARD_TIME_LATEST_EPOCH = moment().utc().valueOf();
	
	//-----------------------------------------
	// Update Custom Time Selector
	cfw_initializeTimefield('CUSTOM_EARLIEST', CFW_DASHBOARD_TIME_EARLIEST_EPOCH);
	cfw_initializeTimefield('CUSTOM_LATEST', CFW_DASHBOARD_TIME_LATEST_EPOCH);
	
	CFW.http.removeURLParam('earliest');
	CFW.http.removeURLParam('latest');
	CFW.http.setURLParam('timeframepreset', preset);
	cfw_dashboard_draw();
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_timeframe_setCustom(earliestMillis, latestMillis){
		
	$('#timeframeSelectorButton').text(CFWL('cfw_dashboard_customtime', "Custom Time"));
	
	CFW_DASHBOARD_TIME_EARLIEST_EPOCH = earliestMillis;
	CFW_DASHBOARD_TIME_LATEST_EPOCH = latestMillis;
	
	cfw_initializeTimefield('CUSTOM_EARLIEST', earliestMillis);
	cfw_initializeTimefield('CUSTOM_LATEST', latestMillis);

	CFW.http.setURLParam('earliest', earliestMillis);
	CFW.http.setURLParam('latest', latestMillis);
	CFW.http.removeURLParam('timeframepreset');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_timeframe_confirmCustom(){

	var earliestMillis = $('#CUSTOM_EARLIEST').val();
	var latestMillis = $('#CUSTOM_LATEST').val()

	if(earliestMillis > latestMillis){
		CFW.ui.addToastWarning("Earliest time has to be before latest time.");
		return;
	}
	cfw_dashboard_timeframe_setCustom(earliestMillis, latestMillis);
	
	//-----------------------------------------
	// Disable Refresh
	var refreshSelector = $("#refreshSelector");
	if(refreshSelector.val() != 'stop'){
		$("#refreshSelector").val('stop');
		cfw_dashboard_setReloadInterval("#refreshSelector");
	}
	
	cfw_dashboard_draw();
}

/******************************************************************
 * 
 * @param direction 'earlier' or 'later'
 ******************************************************************/
function cfw_dashboard_timeframe_shift(direction){
	
	$('#timeframeSelectorButton').text(CFWL('cfw_dashboard_customtime', "Custom Time"));
	
	var offsetMillis = CFW_DASHBOARD_TIME_LATEST_EPOCH - CFW_DASHBOARD_TIME_EARLIEST_EPOCH;
	
	if(direction == 'earlier'){
		CFW_DASHBOARD_TIME_LATEST_EPOCH = CFW_DASHBOARD_TIME_EARLIEST_EPOCH;
		CFW_DASHBOARD_TIME_EARLIEST_EPOCH = CFW_DASHBOARD_TIME_EARLIEST_EPOCH - offsetMillis;
	}else{
		CFW_DASHBOARD_TIME_EARLIEST_EPOCH = CFW_DASHBOARD_TIME_LATEST_EPOCH;
		CFW_DASHBOARD_TIME_LATEST_EPOCH = CFW_DASHBOARD_TIME_LATEST_EPOCH + offsetMillis;
	}
	
	//-----------------------------------------
	// Update Custom Time Selector
	cfw_dashboard_timeframe_setCustom(CFW_DASHBOARD_TIME_EARLIEST_EPOCH, CFW_DASHBOARD_TIME_LATEST_EPOCH);
	//cfw_initializeTimefield('CUSTOM_EARLIEST', CFW_DASHBOARD_TIME_EARLIEST_EPOCH);
	//cfw_initializeTimefield('CUSTOM_LATEST', CFW_DASHBOARD_TIME_LATEST_EPOCH);
	
	//-----------------------------------------
	// Disable Refresh
	var refreshSelector = $("#refreshSelector");
	if(refreshSelector.val() != 'stop'){
		$("#refreshSelector").val('stop');
		cfw_dashboard_setReloadInterval("#refreshSelector");
	}
	
	cfw_dashboard_draw();
}
/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_startOperationsBundle(){
	if(CFW_DASHBOARD_EDIT_MODE){
		//sole.log("------ Command Bundle Start ------ ");
		CFW_DASHBOARD_COMMAND_BUNDLE = [];
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_completeOperationsBundle(){
	if(CFW_DASHBOARD_EDIT_MODE){
		//------------------------------------------
		// Check Position and clear Redo states
		if(CFW_DASHBOARD_COMMAND_HISTORY.length > 0 
		&& CFW_DASHBOARD_COMMAND_HISTORY.length > CFW_DASHBOARD_HISTORY_POSITION){
			CFW_DASHBOARD_COMMAND_HISTORY = CFW_DASHBOARD_COMMAND_HISTORY.splice(0, CFW_DASHBOARD_HISTORY_POSITION);
		}
		
		CFW_DASHBOARD_COMMAND_HISTORY.push(CFW_DASHBOARD_COMMAND_BUNDLE);
		CFW_DASHBOARD_HISTORY_POSITION++;
		CFW_DASHBOARD_COMMAND_BUNDLE = null;
		
		//sole.log("------ Command Bundle Complete ------ ");
		//sole.log("Undo Command History:");
		//sole.log(CFW_DASHBOARD_COMMAND_HISTORY);
	}
	
}
/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_addUndoableOperation(widgetObjectOld, widgetObjectNew, undoFunction, redoFunction){
	
	if(CFW_DASHBOARD_EDIT_MODE && CFW_DASHBOARD_COMMAND_BUNDLE != null){
		//------------------------------------------
		// Add State to History
		var command = {
				undo: undoFunction,
				undoData: _.cloneDeep(widgetObjectOld),
				redo: redoFunction,
				redoData: _.cloneDeep(widgetObjectNew),
		}
		//var deepClone = _.cloneDeep(widgetObjectArray);
		CFW_DASHBOARD_COMMAND_BUNDLE.push(command);
	}

	//sole.log("  >> Add Command");
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_triggerUndo(){

	if(CFW_DASHBOARD_EDIT_MODE){
		
		//----------------------------------
		// Check Position
		if(CFW_DASHBOARD_HISTORY_POSITION > 0){
			
			CFW_DASHBOARD_HISTORY_POSITION--;
			
			CFW.ui.toogleLoader(true);
			window.setTimeout( 
				function(){
					var commandBundle = CFW_DASHBOARD_COMMAND_HISTORY[CFW_DASHBOARD_HISTORY_POSITION];
					$.ajaxSetup({async: false});
						cfw_dashboard_toggleEditMode();
							//----------------------------------
							// Iterate all Changes in Bundle
							for(var i = 0;i < commandBundle.length ;i++){
								var current = commandBundle[i];
								current.undo(current.undoData);
								var widgetObject = $("#"+current.undoData.guid).data('widgetObject');
								if(widgetObject != null){
									cfw_dashboard_widget_save_state(widgetObject, true);
								}
							}
						cfw_dashboard_toggleEditMode();
					$.ajaxSetup({async: true});
					
					CFW.ui.toogleLoader(false);
				}, 50);
		}
		
		//sole.log(CFW_DASHBOARD_HISTORY_POSITION);
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_triggerRedo(){
	if(CFW_DASHBOARD_EDIT_MODE){
		var commandBundle = [];
		
		//----------------------------------
		// Check Position
		if(CFW_DASHBOARD_COMMAND_HISTORY.length != CFW_DASHBOARD_HISTORY_POSITION){
			CFW_DASHBOARD_HISTORY_POSITION++;
			commandBundle = CFW_DASHBOARD_COMMAND_HISTORY[CFW_DASHBOARD_HISTORY_POSITION-1];
			
			CFW.ui.toogleLoader(true);
			window.setTimeout( 
				function(){
					$.ajaxSetup({async: false});
						cfw_dashboard_toggleEditMode();
						
							//----------------------------------
							// Clear All Widgets
							for(var i = 0;i < commandBundle.length ;i++){
								var current = commandBundle[i];
								current.redo(current.redoData);
								var widgetObject = $("#"+current.redoData.guid).data('widgetObject');
								if(widgetObject != null){
									cfw_dashboard_widget_save_state(widgetObject, true);
								}
							}
						cfw_dashboard_toggleEditMode();
					$.ajaxSetup({async: true});
					CFW.ui.toogleLoader(false);
				}, 50);
		}
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_redoUpdateAction(redoData){
	
	var widgetObject = redoData;
	var widget = $('#'+widgetObject.guid);
	
	cfw_dashboard_widget_removeFromGrid(widget);
	cfw_dashboard_widget_createInstance(widgetObject, false);
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_undoUpdateAction(undoData){
	
	var widgetObject = undoData;
	var widget = $('#'+widgetObject.guid);
	
	cfw_dashboard_widget_removeFromGrid(widget);
	cfw_dashboard_widget_createInstance(widgetObject, false);
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_redoCreateAction(redoData){
	
	var widgetObject = redoData;
	cfw_dashboard_widget_add(widgetObject.TYPE, widgetObject);
	 
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_history_undoCreateAction(undoData){
	
	var widgetObject = undoData;
	cfw_dashboard_widget_remove(widgetObject.guid);
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_getWidgetDefinition(widgetUniqueType){
	
	return CFW_DASHBOARD_WIDGET_REGISTRY[widgetUniqueType];
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_registerWidget(widgetUniqueType, widgetObject){
	
	var defaultObject = {
			// The category the widget should be added to
			category: "Static Widgets",
			// The icon of the widget shown in the menu
			menuicon: "fas fa-th-large",
			// the label of the widget
			menulabel: "Unnamed Widget",
			// Description of the widget
			description: "",
			// Override to customize initial widget title. If null, the menu label will be used as default.
			defaulttitle: null,
			// Override to customize initial widget height. If null, default value defined in DashboardWidget.java is used.
			defaultheight: null,
			// Override to customize initial widget defaultwidth. If null, default value defined in DashboardWidget.java is used.
			defaultwidth: null,
			//Set to true if this widget uses the time from the global timeframe picker. Timeframe will be added to the settings with the fields timeframe_earliest/timeframe_latest.
			usetimeframe: false,
			// function that creates the widget content and returns them to the framework by calling the callback function
			createWidgetInstance: function (widgetObject2, callback) {				
				callback(widgetObject2, "Please specify a function on your widgetDefinition.createWidgetInstance.");
			},
			// Must return a html string representing a HTML form. Or null if no settings are needed for this widget.
			getEditForm: function (widgetObject3) {
				return CFW.dashboard.getSettingsForm(widgetObject3);
			},
			
			// Store the values to the widgetObject. Return true if the data should be saved to the server, false otherwise.
			onSave: function (form, widgetObject4) {
				widgetObject4.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
	}
	
	var merged = Object.assign({}, defaultObject, widgetObject);
	if(merged.defaulttitle == null){
		merged.defaulttitle = merged.menulabel;
	}
	
	CFW_DASHBOARD_WIDGET_REGISTRY[widgetUniqueType] = merged;
	
	var category =  widgetObject.category.replace(/[ ]*\|[ ]*/g, '|').trim();;
	var menulabel =  widgetObject.menulabel;
	var menuicon = widgetObject.menuicon;
	
	var categorySubmenu = $('ul[data-submenuof="'+category+'"]');
	
	var menuitemHTML = 
		'<li><a class="dropdown-item" onclick="cfw_dashboard_widget_add(\''+widgetUniqueType+'\')" >'
			+'<div class="cfw-fa-box"><i class="'+menuicon+'"></i></div>'
			+'<span class="cfw-menuitem-label">'+menulabel+'</span>'
		+'</a></li>';
	
	categorySubmenu.append(menuitemHTML);
	
}

/************************************************************************************************
 * Register a Category in a hierarchical manner. The category name is either a single name, or 
 * a pipe ('|') separated string like "RootCategory | SubCategory | SubSubCategory".
 * The label will be used for the last item.
 * @param faiconClasses
 * @param the name of the category, used to reference the category
 * @param the label of the category, used for localization
 ************************************************************************************************/
function cfw_dashboard_registerCategory(faiconClasses, categoryName, categoryLabel){
	
	//----------------------------
	// Check Category Exists
	var categoryNameTrimmed = categoryName.replace(/[ ]*\|[ ]*/g, '|').trim();
	
	var categorySubmenu = $('ul[data-submenuof="'+categoryNameTrimmed+'"]');
	if(categorySubmenu.length > 0){
		return;
	}
	
	//----------------------------
	// Get Tokens
	var tokens = categoryNameTrimmed.split('|');
		
	//----------------------------
	// Create Category with Parents
	var parent = $('#addWidgetDropdown');
	var currentCategoryName = tokens[0];

	for(var i = 0; i < tokens.length; i++ ){
		
		//----------------------------
		// Category Label
		categoryLabel = tokens[i];
		if(i == tokens.length-1 && categoryLabel != null){
			categoryLabel = tokens[tokens.length-1];
		}
		//----------------------------
		// Check exists
		var currentSubmenu = $('ul[data-submenuof="'+currentCategoryName+'"]');
		if(currentSubmenu.length == 0){

			//----------------------------
			// Create
			var categoryHTML = 
				'<li class="dropdown dropdown-submenu">'
					+'<a href="#" class="dropdown-item dropdown-toggle" id="cfwMenuDropdown" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true"><div class="cfw-fa-box"><i class="'+faiconClasses+'"></i></div><span class="cfw-menuitem-label">'+categoryLabel+'</span><span class="caret"></span></a>'
					+'<ul class="dropdown-menu dropdown-submenu" aria-labelledby="cfwMenuDropdown" data-submenuof="'+currentCategoryName+'">'
					+'</ul>'
				+'</li>';
			parent.append(categoryHTML);
			
		}
		//----------------------------
		// Update parent and name
		if(i < tokens.length-1){
			parent = $('ul[data-submenuof="'+currentCategoryName+'"]');
			currentCategoryName = currentCategoryName + '|' +tokens[i+1];
		}
		
	}
		
	
}
/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_parameters_edit(){
	
	//----------------------------
	// Create Content Div
	let contentDiv = $('<div>');
	contentDiv.append('<p>Parameters will substitute values in the widgets on the dashboard.</p>');

	//----------------------------
	// Create Add Params Button
	let addParametersButton = 
		$('<button class="btn btn-sm btn-success mb-3"><i class="fas fa-plus-circle"></i>&nbsp;Add Parameters</button>')
				.click(cfw_dashboard_parameters_showAddParametersModal);
	
	contentDiv.append(addParametersButton);		
	
	//----------------------------
	// Create Param List Div
	let paramListDiv = $('<div id="param-list">');
	contentDiv.append(paramListDiv);
	
	CFW.ui.showLargeModal('Parameters', contentDiv);
	
    cfw_dashboard_parameters_loadParameterForm();
}
/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_parameters_save(){
	var paramListDiv = $('#param-list');
	var form = paramListDiv.find('form');
	var formID = form.attr('id');
	
	//paramListDiv.find('button').click();
	cfw_internal_postForm('/cfw/formhandler', '#'+formID, function(data){
		if(data.success){
			cfw_dashboard_draw();
		}
	});
	
}
/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_parameters_loadParameterForm(){
	
	var paramListDiv = $('#param-list');
	var form = paramListDiv.find('form');
	var formID = form.attr('id');
	//--------------------------------
	// Trigger Save
	if(form.length == 0){
		loadForm();
	}else{
		//paramListDiv.find('button').click();
		cfw_internal_postForm('/cfw/formhandler', '#'+formID, function(data){
			
			//-------------------------------
			// Update if save was successfull
			if(data.success){
				loadForm();
			}
		});
	
	}
		
	function loadForm(){
		var paramListDiv = $('#param-list');
		paramListDiv.html('');
		
		CFW.http.createForm(CFW_DASHBOARDVIEW_URL, 
				{action: "fetch", item: "paramform", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, 
				paramListDiv, 
				function (formID){
					
					//----------------------------
					// Replace Save Action
					paramListDiv.find('form > button').attr('onclick', 'cfw_dashboard_parameters_save()');
					
					//----------------------------
					// Add Header
					paramListDiv.find('form thead tr').append('<th>&nbsp</th>');
					
					//----------------------------
					// Add Delete Buttons
					formRows = paramListDiv.find('form tbody tr');
					
					formRows.each(function (){
						row = $(this);
						
						row.append('<td><div class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
							+ 'onclick="cfw_dashboard_parameters_removeConfirmed('+row.data('id')+');">'
							+ '<i class="fa fa-trash"></i>'
							+ '</div></td>')
					})
				}
			);
	}
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_parameters_add(widgetType, widgetSetting, label){
	
	CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: 'create', item: 'param', widgetType: widgetType, widgetSetting: widgetSetting, label: label, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, function(data){
		if(data.success){
			// Reload Form
			cfw_dashboard_parameters_loadParameterForm();
		}
	});
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_parameters_removeConfirmed(parameterID){
	CFW.ui.confirmExecute('Do you really want to delete this parameter? (Cannot be undone)', 'Remove', "cfw_dashboard_parameters_remove('"+parameterID+"')" );
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_parameters_remove(parameterID) {
	var formID = $('#param-list form').attr('id');
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, {action: 'delete', item: 'param', paramid: parameterID, formid: formID, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, function(data){

			if(data.success){
				// Remove from Form 
				$('#param-list tr[data-id="'+parameterID+'"]').remove();
				
			}
		}
	);

};

/************************************************************************************************
 * 
 * @return cloned widgetObject
 ************************************************************************************************/
function cfw_dashboard_parameters_applyToWidgetSettings(widgetObject) {

//	FK_ID_DASHBOARD: 2081
//	IS_MODE_CHANGE_ALLOWED: true
//	MODE: "MODE_SUBSTITUTE"
//	NAME: "param_name_VMUkHT"
//	PARAM_TYPE: "SELECT"
//	PK_ID: 164
//	VALUE: "737"
//	WIDGET_SETTING: "environment"
//	WIDGET_TYPE: "emp_prometheus_range_chart"
	
	widgetType = widgetObject.TYPE;
	widgetJsonSettings = _.cloneDeep(widgetObject.JSON_SETTINGS);
	
	for(var index in CFW_DASHBOARD_PARAMS){
		var currentParam = CFW_DASHBOARD_PARAMS[index];
		var currentSettingName = currentParam.WIDGET_SETTING;
		var paramValue = currentParam.VALUE;
		
		console.log('=========================================');
		console.log('widgetJsonSettings[currentSettingName]: '+widgetJsonSettings[currentSettingName]);
		console.log('currentSettingName: '+currentSettingName);
		console.log('settingsValue: '+widgetJsonSettings[currentSettingName]);
		console.log('paramValue: '+paramValue);
		
		//----------------------------------------
		// Handle General Params
		if(CFW.utils.isNullOrEmpty(currentParam.WIDGET_TYPE)
				&& CFW.utils.isNullOrEmpty(currentSettingName)){
					//-------------------------------------
					// Replace Regular Parameters
					for(var key in widgetJsonSettings){
						console.log('>> settingName: '+key);
						console.log('>> oldsettingValue: '+widgetJsonSettings[key]);
						var oldSettingsValue = widgetJsonSettings[key];

						if (typeof oldSettingsValue == "string"){
							widgetJsonSettings[key] = 
								oldSettingsValue.replaceAll('$'+currentParam.NAME+'$', paramValue);
							console.log('C: '+widgetJsonSettings[key]);
						}else if (currentParam.PARAM_TYPE == "BOOLEAN" 
							&& typeof oldSettingsValue == "boolean"
							&& oldSettingsValue == ('$'+currentParam.NAME+'$') ){
							widgetJsonSettings[key] = paramValue;
							console.log('D: '+widgetJsonSettings[key]);
						}else{
							console.log('>>>>>> missed settingName: '+key);
							console.log('>>>>>> missed oldsettingValue: '+widgetJsonSettings[key]);
						}	
					}
		}else {
			
			//-------------------------------------
			// Replace Widget Settings Parameters
			var oldSettingsValue = widgetJsonSettings[currentSettingName];
			var mode = currentParam.MODE;
			switch(currentParam.PARAM_TYPE){
				case 'BOOLEAN':  
					paramValue = paramValue.toLowerCase().trim();
					switch(paramValue){
			        	case "true": case "yes": case "1": widgetJsonSettings[currentSettingName] = true; break;
			        	case "false": case "no": case "0": widgetJsonSettings[currentSettingName] = false; break;
			        	default: widgetJsonSettings[currentSettingName] = Boolean(paramValue); break;
					}
					break;
				case 'NUMBER':
					// objects, numbers etc...
					widgetJsonSettings[currentSettingName] = paramValue;
					console.log('NUMBER: '+widgetJsonSettings[currentSettingName]);
				
				// TEXT, TEXTAREA, PASSWORD, EMAIL, SELECT, LIST 
				default:
					if(mode == "MODE_SUBSTITUTE" && typeof oldSettingsValue == "string"){
							widgetJsonSettings[currentSettingName] = oldSettingsValue.replaceAll('$'+currentParam.NAME+'$',paramValue);
							console.log('DEFAULT SUB: '+widgetJsonSettings[currentSettingName]);
					}else{
						// objects, numbers etc...
						widgetJsonSettings[currentSettingName] = paramValue;
						console.log('DEFAULT OTHER: '+widgetJsonSettings[currentSettingName]);
					}
					break;
			}
			
//			TEXT, 
//			TEXTAREA, 
//			PASSWORD, 
//			NUMBER, 
//			EMAIL, 
//			HIDDEN, 
//			BOOLEAN, 
//			SELECT, 
//			LIST, 
//			WYSIWYG, 
//			DATEPICKER, 
//			DATETIMEPICKER, 
//			TAGS, 
//			TAGS_SELECTOR, 
//			UNMODIFIABLE_TEXT, 
//			NONE
			
//			if(currentParam.MODE == "MODE_SUBSTITUTE"){
//				if (typeof oldSettingsValue == "string"){
//					widgetJsonSettings[currentSettingName] = 
//						oldSettingsValue.replaceAll('$'+currentParam.NAME+'$',paramValue);
//					console.log('A: '+widgetJsonSettings[currentSettingName]);
//				}else if(typeof oldSettingsValue == "object"){
//					// objects, numbers etc...
//					widgetJsonSettings[currentSettingName] = paramValue;
//					console.log('B2: '+widgetJsonSettings[currentSettingName]);
//				}
//			}else if(currentParam.MODE == "MODE_GLOBAL_OVERRIDE"){
//				
//				if (typeof oldSettingsValue == "boolean"){
//					
//					paramValue = paramValue.toLowerCase().trim();
//					switch(paramValue){
//			        	case "true": case "yes": case "1": widgetJsonSettings[currentSettingName] = true;
//			        	case "false": case "no": case "0": widgetJsonSettings[currentSettingName] = false;
//			        	default: widgetJsonSettings[currentSettingName] = Boolean(paramValue);
//					}
//				}else{
//					// objects, numbers etc...
//					widgetJsonSettings[currentSettingName] = paramValue;
//					console.log('B2: '+widgetJsonSettings[currentSettingName]);
//				}
//			    
//					console.log('B: '+widgetJsonSettings[currentSettingName]);
//				widgetJsonSettings[currentSettingName] = paramValue;
//			}
		}
	}
	
	var clone = _.cloneDeep(widgetObject);
	clone.JSON_SETTINGS = widgetJsonSettings;
	
	return clone;
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_parameters_showAddParametersModal(){
	
	//----------------------------
	// Create Content Div
	let contentDiv = $('<div>');
	
	//--------------------------------------
	// General
	
	CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "fetch", item: "availableparams", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
		
		let paramsArray = data.payload;
		
		//-----------------------------------
		// Table Renderer
		var rendererSettings = {
				data: paramsArray,
			 	idfield: null,
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['label'],
			 	titleformat: '{0}',
			 	visiblefields: ['widgetType', 'label'],
			 	labels: {
			 		widgetType: "Widget",
			 		label: "Setting",
			 	},
			 	customizers: {
			 		widgetType: 
			 			function(record, value) { 
				 			if(record.widgetType == null){
				 					return "All";
				 			}else{
				 				return cfw_dashboard_getWidgetDefinition(record.widgetType).menulabel;
				 			} 
				 		},
			 	},
				actions: [
					function (record, id){
						let widgetType = (record.widgetType != null) ? "'"+record.widgetType+"'" : null;
						let widgetSetting = (record.widgetSetting != null) ? "'"+record.widgetSetting+"'" : null;
						let label = (record.label != null) ? "'"+record.label+"'" : null;
						return '<button class="btn btn-success btn-sm" alt="Delete" title="Add Param" onclick="cfw_dashboard_parameters_add('+widgetType+', '+widgetSetting+', '+label+')">'
								+ '<i class="fa fa-plus-circle"></i>'
								+ '</button>';

					},
				],
				
				rendererSettings: {
					table: {narrow: true, filterable: true}
				},
			};
				
		var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
		
		contentDiv.append(renderResult);
		
		CFW.ui.showSmallModal('Add Parameters', contentDiv, "CFW.cache.clearCache();");
	});

}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_edit(widgetGUID){
	var widgetInstance = $('#'+widgetGUID);
	var widgetObject = widgetInstance.data("widgetObject");
	var widgetDef = CFW.dashboard.getWidgetDefinition(widgetObject.TYPE);
	
	//##################################################
	// Create Widget Specific Form
	//##################################################
	var customForm = widgetDef.getEditForm(widgetObject);
	if(customForm != null){
		customForm = $(customForm);
		var buttons = customForm.find('button[type="button"]');
		if(buttons.length > 0){
			buttons.remove();
		}
		var customFormButton = '<button type="button" onclick="cfw_dashboard_widget_save_widgetSettings(this, \''+widgetGUID+'\')" class="form-control btn-primary">'+CFWL('cfw_core_save', 'Save')+'</button>';
		
		customForm.append(customFormButton);
	}
	//##################################################
	// Show Form for Default values
	//##################################################
	var defaultForm = '<form id="form-edit-'+widgetGUID+'">';
	
	//------------------------------
	// Title
	defaultForm += new CFWFormField({ 
			type: "text", 
			name: "title", 
			label: CFWL('cfw_core_title', 'Title'), 
			value: widgetObject.TITLE, 
			description: CFWL('cfw_widget_title_desc', 'The title of the widget.')
		}
	).createHTML();
	
	//------------------------------
	// Title Fontsize
	defaultForm += new CFWFormField({ 
			type: "number", 
			name: "titlefontsize", 
			label: CFWL('cfw_core_fontsize', 'Font Size') + ' ' + CFWL('cfw_core_title', 'Title'), 
			value: widgetObject.TITLE_FONTSIZE, 
			description: CFWL('cfw_widget_titlefontsize_desc', 'The font size of the title in pixel.') 
		}
	).createHTML();
	
	//------------------------------
	// Content Fontsize
	defaultForm += new CFWFormField({ 
			type: "number", 
			name: "contentfontsize", 
			label: CFWL('cfw_core_fontsize', 'Font Size') + ' ' + CFWL('cfw_core_content', 'Content'), 
			value: widgetObject.CONTENT_FONTSIZE, 
			description: CFWL('cfw_widget_contentfontsize_desc', 'The font size used for the widget content. Some widgets might ignore or override this value.') 
		}
	).createHTML();
	//------------------------------
	// Footer
	defaultForm += new CFWFormField({ 
			type: "textarea", 
			name: "footer", 
			label: CFWL('cfw_core_footer', 'Footer'), 
			value: widgetObject.FOOTER, 
			description: CFWL('cfw_widget_footer_desc', 'The contents of the footer of the widget.') 
		}
	).createHTML();
	

	//------------------------------
	// Color Selectors
	var selectOptions = {
			'': 			'Default', 
			'cfw-blue': 	'Blue', 
			'cfw-indigo': 	'Indigo',
			'cfw-purple': 	'Purple', 
			'cfw-pink': 	'Pink', 
			'cfw-red': 		'Red', 
			'cfw-orange': 	'Orange', 
			'cfw-yellow': 	'Yellow', 
			'cfw-green': 	'Green',
			'cfw-teal': 	'Teal',
			'cfw-cyan': 	'Cyan',
			'cfw-white': 	'White',
			'cfw-lightgray': 'Light Gray',
			'cfw-gray': 	'Gray',
			'cfw-darkgray': 'Dark Gray',
			'cfw-black': 	'Black',
	};
	
	defaultForm += new CFWFormField({ 
		type: "select", 
		name: "BGCOLOR", 
		label: CFWL('cfw_core_bgcolor', 'Background Color'), 
		value: widgetObject.BGCOLOR, 
		options: selectOptions,
		description: CFWL('cfw_core_bgcolor_desc', 'Define the color used for the background.') 
	}).createHTML();
	
	defaultForm += new CFWFormField({ 
		type: "select", 
		name: "FGCOLOR", 
		label: CFWL('cfw_core_fgcolor', 'Foreground Color'), 
		value: widgetObject.FGCOLOR, 
		options: selectOptions,
		description: CFWL('cfw_core_fgcolor_desc', 'Define the color used for the foreground, like text and borders.')
	}).createHTML();
	
	//------------------------------
	// Save Button
	defaultForm += '<button type="button" onclick="cfw_dashboard_widget_save_defaultSettings(\''+widgetGUID+'\')" class="form-control btn-primary">'+CFWL('cfw_core_save', 'Save')+'</button>';
	


	//##################################################
	// Create and show Modal
	//##################################################
	var compositeDiv = $('<div id="editWidgetComposite">');
	compositeDiv.append('<p><strong>Widget:</strong>&nbsp;'+widgetDef.menulabel+'</p>');
	compositeDiv.append('<p><strong>Description:</strong>&nbsp;'+widgetDef.description+'</p>');
		
	//----------------------------------
	// Create Pill Navigation
	var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');

	list.append(
		'<li class="nav-item"><a class="nav-link active" id="widgetSettingsTab" data-toggle="pill" href="#widgetSettings" role="tab" ><i class="fas fa-tools mr-2"></i>Widget Settings</a></li>'
		+'<li class="nav-item"><a class="nav-link" id="defaultSettingsTab" data-toggle="pill" href="#defaultSettings" role="tab" ><i class="fas fa-cog mr-2"></i>Standard Settings</a></li>'
	);
		
	compositeDiv.append(list);
	compositeDiv.append('<div id="settingsTabContent" class="tab-content">'
			  +'<div class="tab-pane fade show active" id="widgetSettings" role="tabpanel" aria-labelledby="widgetSettingsTab"></div>'
			  +'<div class="tab-pane fade" id="defaultSettings" role="tabpanel" aria-labelledby="defaultSettingsTab"></div>'
		+'</div>' );
	

	if(customForm != null){
		compositeDiv.find('#widgetSettings').append(customForm);
	}
	compositeDiv.find('#defaultSettings').append(defaultForm);
	//----------------------------------
	// Show Modal
	CFW.ui.showModal(CFWL('cfw_core_settings', 'Settings'), compositeDiv, "CFW.cache.clearCache();");
	
	//-----------------------------------
	// Initialize Forms
	if(customForm != null){
		var formID = $(customForm).attr("id");
		// workaround, force evaluation
		eval($(customForm).find("script").text());
		eval("intializeForm_"+formID+"();");
	}
	$('#editWidgetComposite [data-toggle="tooltip"]').tooltip();
				
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_duplicate(widgetGUID) {
	var widgetInstance = $('#'+widgetGUID);
	var widgetObject = widgetInstance.data("widgetObject");
	
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, {action: 'create', item: 'widget', type: widgetObject.TYPE, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, function(data){
			var newWidgetObject = data.payload;
			if(newWidgetObject != null){
				//---------------------------------
				// remove content to avoid circular 
				// references on deep copy
				var withoutContent = Object.assign(widgetObject);
				delete withoutContent.content;
				
				//---------------------------------
				// Deep copy and create Duplicate
				var deepCopyWidgetObject = _.cloneDeep(withoutContent);
				deepCopyWidgetObject.PK_ID = newWidgetObject.PK_ID;
				delete deepCopyWidgetObject.guid;

				cfw_dashboard_widget_createInstance(deepCopyWidgetObject, true, function(widgetObject2){
					
				    //----------------------------------
					// Add Undoable Operation
					cfw_dashboard_history_startOperationsBundle();
						cfw_dashboard_history_addUndoableOperation(
								widgetObject2, 
								widgetObject2, 
								cfw_dashboard_history_undoCreateAction, 
								cfw_dashboard_history_redoCreateAction
						);
					cfw_dashboard_history_completeOperationsBundle();
					
				});
			}
		}
	);
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_save_defaultSettings(widgetGUID){
	var widget = $('#'+widgetGUID);
	var widgetObject = widget.data("widgetObject");
	var settingsForm = $('#form-edit-'+widgetGUID);
			
	var undoState = _.cloneDeep(widgetObject);
	widgetObject.TITLE = settingsForm.find('input[name="title"]').val();
	widgetObject.TITLE_FONTSIZE = settingsForm.find('input[name="titlefontsize"]').val();
	widgetObject.CONTENT_FONTSIZE = settingsForm.find('input[name="contentfontsize"]').val();
	widgetObject.FOOTER = settingsForm.find('textarea[name="footer"]').val();
	widgetObject.BGCOLOR = settingsForm.find('select[name="BGCOLOR"]').val();
	widgetObject.FGCOLOR = settingsForm.find('select[name="FGCOLOR"]').val();
	
	cfw_dashboard_widget_rerender(widgetGUID);
	
    //----------------------------------
	// Add Undoable Operation
	cfw_dashboard_history_startOperationsBundle();
		cfw_dashboard_history_addUndoableOperation(
				undoState, 
				widgetObject, 
				cfw_dashboard_history_undoUpdateAction,
				cfw_dashboard_history_redoUpdateAction
				
		);
	cfw_dashboard_history_completeOperationsBundle();
	
	CFW.ui.addToastInfo('Done');
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_save_widgetSettings(formButton, widgetGUID){
	var widget = $('#'+widgetGUID);
	var widgetObject = widget.data("widgetObject");
	var undoState = _.cloneDeep(widgetObject);
	
	var widgetDef = CFW.dashboard.getWidgetDefinition(widgetObject.TYPE);
	// switch summernote to wysiwyg view
	$('.btn-codeview.active').click();
	var success = widgetDef.onSave($(formButton).closest('form'), widgetObject);

	if(success){
		cfw_dashboard_widget_rerender(widgetGUID);
		
	    //----------------------------------
		// Add Undoable Operation
		cfw_dashboard_history_startOperationsBundle();
			cfw_dashboard_history_addUndoableOperation(
					undoState, 
					widgetObject, 
					cfw_dashboard_history_undoUpdateAction,
					cfw_dashboard_history_redoUpdateAction
					
			);
		cfw_dashboard_history_completeOperationsBundle();
		
		CFW.ui.addToastInfo('Done');
	}
}
/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_removeConfirmed(widgetGUID){
	CFW.ui.confirmExecute('Do you really want to remove this widget?', 'Remove', "cfw_dashboard_widget_remove('"+widgetGUID+"')" );
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_remove(widgetGUID) {
	var widget = $('#'+widgetGUID);
	var widgetObject = widget.data('widgetObject');
	
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, {action: 'delete', item: 'widget', widgetid: widgetObject.PK_ID, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, function(data){

			if(data.success){
				cfw_dashboard_widget_removeFromGrid(widget);
				
			    //----------------------------------
				// Add Undoable Operation
				cfw_dashboard_history_startOperationsBundle();
					cfw_dashboard_history_addUndoableOperation(
							widgetObject, 
							widgetObject, 
							cfw_dashboard_history_redoCreateAction,
							cfw_dashboard_history_undoCreateAction
							
					);
				cfw_dashboard_history_completeOperationsBundle();
			}
		}
	);

};


/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_removeFromGrid(widgetElement) {
	var grid = $('.grid-stack').data('gridstack');
	grid.removeWidget(widgetElement);
};




/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_add(type, optionalRedoWidgetObject) {

	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, {action: 'create', item: 'widget', type: type, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, function(data){
			var widgetObject = data.payload;
			if(widgetObject != null){
				
				//-------------------------
				// Handle Redo
				if(optionalRedoWidgetObject != null){
					optionalRedoWidgetObject.PK_ID = widgetObject.PK_ID;
					cfw_dashboard_widget_createInstance(optionalRedoWidgetObject, false);
					return;
				}
				
				//-------------------------
				// Handle Initial Creation
				var widgetDefinition = CFW.dashboard.getWidgetDefinition(type);
				widgetObject.TYPE   = type;
				widgetObject.TITLE  = widgetDefinition.defaulttitle;
				
				if(widgetDefinition.defaultheight != null){
					widgetObject.HEIGHT = widgetDefinition.defaultheight;
				}
				
				if(widgetDefinition.defaultwidth != null){
					widgetObject.WIDTH  = widgetDefinition.defaultwidth;
				}

				cfw_dashboard_widget_createInstance(widgetObject, true, function(subWidgetObject){
					
				    //----------------------------------
					// Add Undoable Operation
					
					cfw_dashboard_history_startOperationsBundle();
					
						cfw_dashboard_history_addUndoableOperation(
								subWidgetObject, 
								subWidgetObject, 
								cfw_dashboard_history_undoCreateAction, 
								cfw_dashboard_history_redoCreateAction
						);
						
					cfw_dashboard_history_completeOperationsBundle();
					
				});

			}
		}
	);
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_getSettingsForm(widgetObject) {
	
	var formHTML = "";
	
	var params = Object.assign({action: 'fetch', item: 'settingsform'}, widgetObject); 
	
	delete params.content;
	delete params.guid;
	delete params.JSON_SETTINGS;
	
	params.JSON_SETTINGS = JSON.stringify(widgetObject.JSON_SETTINGS);
	
	$.ajaxSetup({async: false});
		CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, params, function(data){
				formHTML = data.payload.html;
			}
		);
	$.ajaxSetup({async: true});
	
	return formHTML;
}


/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_fetchData(widgetObject, callback) {
	
	var formHTML = "";
	
	var params = Object.assign({action: 'fetch', item: 'widgetdata', dashboardid: CFW_DASHBOARD_URLPARAMS.id}, widgetObject); 
	
	delete params.content;
	delete params.guid;
	delete params.JSON_SETTINGS;
	
	var definition = CFW.dashboard.getWidgetDefinition(widgetObject.TYPE);
	
	var settings = widgetObject.JSON_SETTINGS;
	
	//----------------------------
	// Check has Timeframe
	if(definition.usetimeframe){
		if(!CFW_DASHBOARD_TIME_ENABLED){
			$('#timeframePicker').removeClass('d-none');
			CFW_DASHBOARD_TIME_ENABLED = true;
		}
		settings = _.cloneDeep(settings);
		settings.timeframe_earliest = CFW_DASHBOARD_TIME_EARLIEST_EPOCH;
		settings.timeframe_latest = CFW_DASHBOARD_TIME_LATEST_EPOCH;
	}
	
	//----------------------------
	// Fetch Data
	params.JSON_SETTINGS = JSON.stringify(settings);
	
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, params, function(data){
		callback(data);
	});

	
	return formHTML;
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_save_state(widgetObject, forceSave) {
	
	if(forceSave || ( JSDATA.canEdit == true && CFW_DASHBOARD_EDIT_MODE) ){
		//----------------------------------
		// Update Object
		var params = Object.assign({action: 'update', item: 'widget'}, widgetObject); 
		
		delete params.content;
		delete params.guid;
		delete params.JSON_SETTINGS;
		
		params.JSON_SETTINGS = JSON.stringify(widgetObject.JSON_SETTINGS);
				
		//----------------------------------
		// Update in Database
		CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, params, function(data){});
	}
}
/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_rerender(widgetGUID) {
	var widget = $('#'+widgetGUID);
	var widgetObject = widget.data("widgetObject");
	
	cfw_dashboard_widget_removeFromGrid(widget);
	cfw_dashboard_widget_createInstance(widgetObject, false);
	
}

/************************************************************************************************
 * Creates the HTML element and returns it as a jQuery object.
 ************************************************************************************************/
function cfw_dashboard_widget_createHTMLElement(widgetObject){
	
	//---------------------------------------
	// Merge Data
	CFW_DASHBOARD_WIDGET_GUID++;
	var defaultOptions = {
			guid: 'widget-'+CFW_DASHBOARD_WIDGET_GUID,
			TITLE: "",
			TITLE_FONTSIZE: 16,
			CONTENT_FONTSIZE: 16,
			FOOTER: "",
			BGCOLOR: "",
			FGCOLOR: "",
			JSON_SETTINGS: {}
	}
	
	var merged = Object.assign({}, defaultOptions, widgetObject);
	
	//---------------------------------------
	// Resolve Classes
	var FGCOLORClass = '';
	var borderClass = '';
	if(merged.FGCOLOR != null && merged.FGCOLOR.trim().length > 0){
		FGCOLORClass = 'text-'+merged.FGCOLOR;
		borderClass = 'border-'+merged.FGCOLOR;
	}
	
	var BGCOLORClass = '';
	if(merged.BGCOLOR != null && merged.BGCOLOR.trim().length > 0){
		BGCOLORClass = 'bg-'+merged.BGCOLOR;
	}
	
	var settingsDisplayClass = 'd-none';
	if(CFW_DASHBOARD_EDIT_MODE){
		settingsDisplayClass = '';
	}
	
	var advancedDisplayClass = 'd-none';
	if(CFW_DASHBOARD_EDIT_MODE_ADVANCED){
		advancedDisplayClass = '';
	}
	
	var htmlString =
		'<div class="grid-stack-item-content card d-flex '+BGCOLORClass+' '+FGCOLORClass+'">'
		+'	<div role="button" class="cfw-dashboard-widget-actionicons '+settingsDisplayClass+' text-cfw-lightgray">'
		+'		<div role="button" class="actionicon-delete '+advancedDisplayClass+'" onclick="cfw_dashboard_widget_remove(\''+merged.guid+'\')"><i class="fas fa-times"></i></div>'
		+'		<div role="button" class="actionicon-duplicate '+advancedDisplayClass+'" onclick="cfw_dashboard_widget_duplicate(\''+merged.guid+'\')"><i class="fas fa-clone"></i></div>'
		+'		<div role="button" class="actionicon-edit '+advancedDisplayClass+'" onclick="cfw_dashboard_widget_edit(\''+merged.guid+'\')"><i class="fas fa-pen"></i></div>'
		+'		<div role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fas fa-cog"></i></div>'
		+'		<div class="dropdown-menu">'
		+'			<a class="dropdown-item" onclick="cfw_dashboard_widget_edit(\''+merged.guid+'\')"><i class="fas fa-pen"></i>&nbsp;'+CFWL('cfw_core_edit', 'Edit')+'</a>'
		+'			<a class="dropdown-item" onclick="cfw_dashboard_widget_duplicate(\''+merged.guid+'\')"><i class="fas fa-clone"></i>&nbsp;'+CFWL('cfw_core_duplicate', 'Duplicate')+'</a>'
		//'			<div class="dropdown-divider"></div>'
		+'			<a class="dropdown-item" onclick="cfw_dashboard_widget_removeConfirmed(\''+merged.guid+'\')"><i class="fas fa-trash"></i>&nbsp;'+CFWL('cfw_core_remove', 'Remove')+'</a>'
		+'		</div>'
		+'	</div>'

	if(merged.TITLE != null && merged.TITLE != ''){
		htmlString += 
		 '     	  <div class="cfw-dashboard-widget-title border-bottom '+borderClass+'" style="font-size: '+merged.TITLE_FONTSIZE+'px;">'
		+'		  	<span>'+merged.TITLE+'</span>'
		+'		  </div>'
	}
	
	if(merged.content != null && merged.content != ''
	|| merged.FOOTER != null && merged.FOOTER != ''){
		htmlString += 
			'<div class="cfw-dashboard-widget-body" style="font-size: '+merged.CONTENT_FONTSIZE+'px;">';
				if(merged.FOOTER != null && merged.FOOTER != ''){
					htmlString +=
					'		 <div class="cfw-dashboard-widget-footer border-top '+borderClass+'">'
					+			merged.FOOTER
					+'		  </div>'
				}
		htmlString += '</div>';
	}
	htmlString += '</div>';
	
	var widgetItem = $('<div id="'+merged.guid+'" data-id="'+merged.widgetID+'"  class="grid-stack-item">');
	widgetItem.append(htmlString);
	widgetItem.data("widgetObject", merged)
	
	if(merged.content != null && merged.content != ''){
		widgetItem.find('.cfw-dashboard-widget-body').append(merged.content);
	}

	return widgetItem;
}

/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_createLoadingPlaceholder(widgetObject, doAutoposition) {
	
	var placeholderWidget = { 
			guid: 'placeholder-'+CFW.utils.randomString(24), 
			content: CFW.ui.createLoaderHTML()
			};
	
	widgetObject.placeholderGUID = placeholderWidget.guid;
	
	var widgetInstance = cfw_dashboard_widget_createHTMLElement(placeholderWidget);

	var grid = $('.grid-stack').data('gridstack');

    grid.addWidget($(widgetInstance),
    		widgetObject.X, 
    		widgetObject.Y, 
    		widgetObject.WIDTH, 
    		widgetObject.HEIGHT, 
    		doAutoposition);
    
}
/************************************************************************************************
 * 
 ************************************************************************************************/
function cfw_dashboard_widget_createInstance(originalwidgetObject, doAutoposition, callback) {
	
	var widgetDefinition = CFW.dashboard.getWidgetDefinition(originalwidgetObject.TYPE);	
	
	if(widgetDefinition != null){
		
		widgetObjectClone = cfw_dashboard_parameters_applyToWidgetSettings(originalwidgetObject);
		
		try{
		//---------------------------------------
		// Add Placeholder	
		cfw_dashboard_widget_createLoadingPlaceholder(widgetObjectClone, doAutoposition);
		
		//---------------------------------------
		// Create Instance by Widget Definition
		widgetDefinition.createWidgetInstance(widgetObjectClone, 
			function(subWidgetObject, widgetContent){
				
				//---------------------------------------
				// Remove Placeholder
				var placeholderWidget = $('#'+subWidgetObject.placeholderGUID);
				cfw_dashboard_widget_removeFromGrid(placeholderWidget);
				
				//---------------------------------------
				// Add Widget
				subWidgetObject.content = widgetContent;
				var widgetInstance = cfw_dashboard_widget_createHTMLElement(subWidgetObject);

				var grid = $('.grid-stack').data('gridstack');

			    grid.addWidget($(widgetInstance),
			    		subWidgetObject.X, 
			    		subWidgetObject.Y, 
			    		subWidgetObject.WIDTH, 
			    		subWidgetObject.HEIGHT, 
			    		doAutoposition);
			   
			    //----------------------------
			    // Get Widget with applied default values
			    subWidgetObject = $(widgetInstance).data('widgetObject');
			    
			    //----------------------------
			    // Check Edit Mode
			    if(!CFW_DASHBOARD_EDIT_MODE){
			    	grid.movable('#'+subWidgetObject.guid, false);
			    	grid.resizable('#'+subWidgetObject.guid, false);
			    }
			    //----------------------------
			    // Update Data
			    subWidgetObject.WIDTH	= widgetInstance.attr("data-gs-width");
			    subWidgetObject.HEIGHT	= widgetInstance.attr("data-gs-height");
			    subWidgetObject.X		= widgetInstance.attr("data-gs-x");
			    subWidgetObject.Y		= widgetInstance.attr("data-gs-y");
			    $(widgetInstance).data('widgetObject', originalwidgetObject);
			    
			    cfw_dashboard_widget_save_state(originalwidgetObject);
			    
			    if(callback != null){
			    	callback(subWidgetObject);
			    }
			}
		);
		}catch(err){
			CFW.ui.addToastDanger('An error occured while creating a widget instance: '+err.message);
			console.log(err);
		}
	}
	
}

/******************************************************************
 * 
 ******************************************************************/
CFW.dashboard = {
		registerWidget: 		cfw_dashboard_registerWidget,
		getWidgetDefinition: 	cfw_dashboard_getWidgetDefinition,
		registerCategory: 		cfw_dashboard_registerCategory,
		getSettingsForm:		cfw_dashboard_widget_getSettingsForm,
		fetchWidgetData: 		cfw_dashboard_widget_fetchData,
};


/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_toggleFullscreenMode(){
	
	if(CFW_DASHBOARD_FULLSCREEN_MODE){
		CFW_DASHBOARD_FULLSCREEN_MODE = false;

		$('.hideOnFullScreen').css('display', '');
		$('.navbar').css('display', '');
		$('#cfw-dashboard-control-panel').css('padding', '');
		$('#cfw-dashboard-control-panel').css('margin', '');
		$('#fullscreenButton')
			.removeClass('fullscreened-button')
			.addClass('fullscreenButton');
		
		$('#fullscreenButtonIcon')
			.removeClass('fa-compress')
			.addClass('fa-expand');
		
	}else{
		CFW_DASHBOARD_FULLSCREEN_MODE = true;
		$('.hideOnFullScreen').css('display', 'none');
		$('.navbar').css('display', 'none');
		$('#cfw-dashboard-control-panel').css('padding', '0px');
		$('#cfw-dashboard-control-panel').css('margin', '0px');
		$('#fullscreenButton')
			.removeClass('fullscreenButton')
			.addClass('fullscreened-button');
		
		$('#fullscreenButtonIcon')
			.removeClass('fa-expand')
			.addClass('fa-compress');
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_dashboard_toggleEditMode(){

	//-----------------------------------------
	// Toggle Edit Mode
	var grid = $('.grid-stack').data('gridstack');
	if(CFW_DASHBOARD_EDIT_MODE){
		CFW_DASHBOARD_EDIT_MODE = false;
		$('.cfw-dashboard-widget-actionicons').addClass('d-none');
		$('#addWidget').addClass('d-none');
		$('#doneButton').addClass('d-none');
		$('#top-ruler').addClass('d-none');
		$('#editButton').removeClass('d-none');
		
		
		grid.disable();
		
	}else{
		CFW_DASHBOARD_EDIT_MODE = true;
		$('.cfw-dashboard-widget-actionicons').removeClass('d-none');
		$('#addWidget').removeClass('d-none');
		$('#parametersButton').removeClass('d-none');
		$('#doneButton').removeClass('d-none');
		$('#top-ruler').removeClass('d-none');
		$('#editButton').addClass('d-none');
		
		grid.enable();
	}
}

/**************************************************************************************
 * Change duration between refreshes
 * @param selector selected value of #refreshSelector
 * @see storeLocalValue()
 * @see refreshTimer()
 *************************************************************************************/
function cfw_dashboard_setReloadInterval(selector) {
	
	
	var refreshInterval = $(selector).val();
	
	//------------------------
	// Disable Old Interval
	if(refreshInterval == null 
	|| (refreshInterval == 'stop' && CFW_DASHBOARD_REFRESH_INTERVAL_ID != null) ){
		clearInterval(CFW_DASHBOARD_REFRESH_INTERVAL_ID);
		window.localStorage.setItem("dashboard-reload-interval-id"+CFW_DASHBOARD_URLPARAMS.id, 'stop');
		return;
	}
	
	//------------------------
	// Prevent user set lower interval
//	if(refreshInterval < 300000){
//		refreshInterval = 300000;
//	}

	//------------------------
	// Disable Old Interval
	if(CFW_DASHBOARD_REFRESH_INTERVAL_ID != null){
		clearInterval(CFW_DASHBOARD_REFRESH_INTERVAL_ID);
	}
	
	CFW_DASHBOARD_REFRESH_INTERVAL_ID = setInterval(function(){
    	if(!CFW_DASHBOARD_EDIT_MODE){
    		// Use gridstack.removeAll() to prevent widgets from jumping around on reload
    		//$('.grid-stack').html('');
    		
	    	cfw_dashboard_draw();
	    };
    }, refreshInterval);
	
	window.localStorage.setItem("dashboard-reload-interval-id"+CFW_DASHBOARD_URLPARAMS.id, refreshInterval);
    	
}

/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_dashboard_initialize(gridStackElementSelector){
	
	
	$('#dashboardName').text(JSDATA.dashboardName);
	
	//-----------------------------
	// Toggle Edit Button
	if(JSDATA.canEdit){
		$('#editButton').removeClass('d-none');
		
		$('body').keyup(function (e){
			
			//--------------------------------
			// Abort if Modal is open
			if($('.modal.show').length > 0){
				return;
			}
			
			//--------------------------------
			// Ctrl+Y - Trigger Redo
			if (e.ctrlKey && e.keyCode == 89) {
				cfw_dashboard_history_triggerRedo()
				return;
			}
			
			//--------------------------------
			// Ctrl+Z - Trigger Undo
			if (e.ctrlKey && e.keyCode == 90) {
				cfw_dashboard_history_triggerUndo();
				return;
			}
			
			//--------------------------------
			// Ctrl+Alt+E - Trigger Edit Mode
			if (e.ctrlKey && event.altKey && e.keyCode == 69) {
				cfw_dashboard_toggleEditMode();
				return;
			}
			
			//--------------------------------
			// Ctrl+Alt+A -  Toggle Advanced Edit Mode
			if ( e.ctrlKey && event.altKey && e.keyCode == 65) {
				
				var actionButtons = $('.actionicon-delete, .actionicon-duplicate, .actionicon-edit');
				
				if(!CFW_DASHBOARD_EDIT_MODE || actionButtons.first().hasClass('d-none')){
					CFW_DASHBOARD_EDIT_MODE_ADVANCED = true;
					actionButtons.removeClass('d-none');
				}else{
					CFW_DASHBOARD_EDIT_MODE_ADVANCED = false;
					actionButtons.addClass('d-none');
				}
				
				if(!CFW_DASHBOARD_EDIT_MODE){
					cfw_dashboard_toggleEditMode();
				}
				
						
				return;
			}
			
			
		})
	}
	
	
	//-----------------------------
	// Setup Gridstack
	$(gridStackElementSelector).gridstack({
		alwaysShowResizeHandle: /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent),
		resizable: {
		    handles: 'e, se, s, sw, w'
		  },
		column: 64, 
		cellHeight: 5,
		animate: true,
		float: true,
		verticalMargin: 10
	});
	
	//-----------------------------
	// Set update on dragstop 
	$(gridStackElementSelector).on('change', function(event, items) {
		 
		cfw_dashboard_history_startOperationsBundle();
		
			for(var key in items){
				
				var currentItem = items[key].el;
			
				var widgetInstance = $(currentItem);
				var widgetObject 	 = widgetInstance.data("widgetObject");
				var undoData = _.cloneDeep(widgetObject);
				
				widgetObject.X			= widgetInstance.attr("data-gs-x");
				widgetObject.Y		 	= widgetInstance.attr("data-gs-y");
				widgetObject.WIDTH	= widgetInstance.attr("data-gs-width");
				widgetObject.HEIGHT	= widgetInstance.attr("data-gs-height");
				
				var redoData = _.cloneDeep(widgetObject);
				cfw_dashboard_widget_save_state(widgetObject);
				
				//----------------------------------
				// Add Undoable Operation
				cfw_dashboard_history_addUndoableOperation(
						undoData, 
						redoData, 
						cfw_dashboard_history_undoUpdateAction, 
						cfw_dashboard_history_redoUpdateAction
				);
			}
		cfw_dashboard_history_completeOperationsBundle();
	});
	
}

/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_dashboard_initialDraw(){
		
	cfw_dashboard_initialize('.grid-stack');
	
	if(	CFW_DASHBOARD_URLPARAMS.earliest != null && CFW_DASHBOARD_URLPARAMS.latest != null){
		//-----------------------------
		// Get Earliest/Latest from URL
		cfw_dashboard_timeframe_setCustom(CFW_DASHBOARD_URLPARAMS.earliest, CFW_DASHBOARD_URLPARAMS.latest);
		cfw_dashboard_draw();
	}else if(CFW_DASHBOARD_URLPARAMS.timeframepreset != null){
		//-----------------------------
		// Get Preset from URL
		cfw_dashboard_timeframe_setPreset(CFW_DASHBOARD_URLPARAMS.timeframepreset);
		// above method calls cfw_dashboard_draw()
	}else{

		var timeframePreset =window.localStorage.getItem("dashboard-timeframe-preset-"+CFW_DASHBOARD_URLPARAMS.id);
		if(timeframePreset != null && timeframePreset != 'null' && timeframePreset != 'custom' ){
			//---------------------------------
			// Get last preset from local store
			cfw_dashboard_timeframe_setPreset(timeframePreset);
			// above method calls cfw_dashboard_draw()
		}else{
			//---------------------------------
			// Just draw with default
			cfw_dashboard_draw();
		}
	}
	
	//---------------------------------
	// Load Refresh interval from URL or Local store
	var refreshParam = CFW_DASHBOARD_URLPARAMS.refreshinterval;
	if(refreshParam != null){
		$("#refreshSelector").val(refreshParam);
		cfw_dashboard_setReloadInterval("#refreshSelector");
	}else{
		var refreshInterval = window.localStorage.getItem("dashboard-reload-interval-id"+CFW_DASHBOARD_URLPARAMS.id);
		if(refreshInterval != null && refreshInterval != 'null' && refreshInterval != 'stop' ){
			$("#refreshSelector").val(refreshInterval);
			cfw_dashboard_setReloadInterval("#refreshSelector");
		}
	}
	
	

	
	
}
/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_dashboard_draw(){
		
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		//-----------------------------------
		// Clear existing Widgets
		var grid = $('.grid-stack').data('gridstack');
		grid.removeAll();
		
		CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "fetch", item: "widgetsandparams", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
			
			var widgetArray = data.payload.widgets;
			CFW_DASHBOARD_PARAMS =  data.payload.params;
			
			for(var i = 0;i < widgetArray.length ;i++){
				cfw_dashboard_widget_createInstance(widgetArray[i], false);
			}

			//-----------------------------
			// Disable resize & move
			$('.grid-stack').data('gridstack').disable();
		});
		
		CFW.ui.toogleLoader(false);
	}, 100);
}

/******************************************************************
 * Initialize Localization
 * has to be done before widgets are registered
 ******************************************************************/
CFW.dashboard.registerCategory("fas fa-th-large", "Static Widgets", CFWL('cfw_dashboard_category_static'));
