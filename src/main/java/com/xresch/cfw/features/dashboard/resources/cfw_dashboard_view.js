
//works with public and user-based access
var CFW_DASHBOARDVIEW_URL = CFW.http.getURLPath();

var CFW_DASHBOARD_URLPARAMS = CFW.http.getURLParamsDecoded();
	
var CFW_DASHBOARD_EDIT_MODE = false;
var CFW_DASHBOARD_EDIT_MODE_ADVANCED = true;
var CFW_DASHBOARD_FULLSCREEN_MODE = false;
var CFW_DASHBOARD_REFRESH_INTERVAL_ID = null;
var CFW_DASHBOARD_WIDGET_REGISTRY = {};

// used to avoid collision of multiple refreshes
// drawing at the same time
var CFW_DASHBOARD_FULLREDRAW_COUNTER = 0;
var CFW_DASHBOARD_APPLYING_URLPARAMS = false;

// ignore server-side-caching when true
var CFW_DASHBOARD_FORCE_REFRESH = false;

// saved with guid
var CFW_DASHBOARD_WIDGET_DATA = {};
var CFW_DASHBOARD_WIDGET_GUID = 0;

// -------------------------------------
// Globals needed for UNDO and REDO
// -------------------------------------
// Array of Command Bundles
var CFW_DASHBOARD_COMMAND_HISTORY = [];

// Position in the History
var CFW_DASHBOARD_HISTORY_POSITION = 0;
var CFW_DASHBOARD_HISTORY_BUNDLE_STARTED = false;
var CFW_DASHBOARD_HISTORY_IS_UPDATING = false;

// Bundle of commands to redo/undo
var CFW_DASHBOARD_COMMAND_BUNDLE = null;

// -------------------------------------
// TIMEFRAME
// -------------------------------------
var CFW_DASHBOARD_TIME_FIELD_ID = "timeframePicker";
var CFW_DASHBOARD_TIME_EARLIEST_EPOCH = moment().utc().subtract(30, 'm').utc().valueOf();
var CFW_DASHBOARD_TIME_LATEST_EPOCH = moment().utc().valueOf();


/*******************************************************************************
 * Create default renderer settings for status widgets.
 ******************************************************************************/
function cfw_dashboard_setURLParams(params){
	
	var doPushHistoryState = !CFW_DASHBOARD_APPLYING_URLPARAMS;

	CFW.http.setURLParams(params, doPushHistoryState);
	CFW_DASHBOARD_URLPARAMS = CFW.http.getURLParamsDecoded();
}
		
/******************************************************************
 * Edit Dashboard
 ******************************************************************/
function cfw_dashboard_editDashboard(){
	
	cfw_dashboardcommon_editDashboard(CFW_DASHBOARD_URLPARAMS.id);

}


/*******************************************************************************
 * Create default renderer settings for status widgets.
 ******************************************************************************/
function cfw_dashboard_createStatusWidgetRendererSettings(settings){
	return {
		tiles: {
			sizefactor: settings.sizefactor,
			showlabels: settings.showlabels,
			borderstyle: settings.borderstyle
		},
		statuslist: {
			sizefactor: settings.sizefactor,
			showlabels: settings.showlabels,
			borderstyle: settings.borderstyle
		},
		table: {
			narrow: 	true,
			striped: 	false,
			hover: 		false,
			filterable: false,
		},
		panels: {
			narrow: 	true,
		},
		cards: {
			narrow: 	true,
			maxcolumns: 5,
		}
	};
}


/*******************************************************************************
 * Receives a call from the timeframe picker when selection is updated.
 ******************************************************************************/
function cfw_dashboard_timeframeChangeCallback(fieldID, pickerData){
	
	// -----------------------------------------
	// Update FieldData
	
	CFW_DASHBOARD_TIME_EARLIEST_EPOCH = pickerData.earliest;
	CFW_DASHBOARD_TIME_LATEST_EPOCH = pickerData.latest;
	
	if(pickerData.offset != null){
		window.localStorage.setItem("dashboard-timeframe-preset-"+CFW_DASHBOARD_URLPARAMS.id, pickerData.offset);
		cfw_dashboard_setURLParams({
				"timeframepreset": pickerData.offset
				,"earliest": null
				,"latest": null
			});
	}else{
		cfw_dashboard_setURLParams({
				"timeframepreset": null
				,"earliest": pickerData.earliest
				,"latest": pickerData.latest
			});
	}

	cfw_dashboard_draw(false, false);
	
}
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_timeframe_setOffset(preset){

	cfw_timeframePicker_setOffset("#"+CFW_DASHBOARD_TIME_FIELD_ID, preset);
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_timeframe_setCustom(earliestMillis, latestMillis){
	
	cfw_timeframePicker_setCustom(CFW_DASHBOARD_TIME_FIELD_ID, earliestMillis, latestMillis)	

	$('#timeframeSelectorButton').text(CFWL('cfw_dashboard_customtime', "Custom Time"));
	
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_history_startOperationsBundle(){
	
	if(!CFW_DASHBOARD_HISTORY_IS_UPDATING){
		$.ajaxSetup({async: false});
			CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "undoredo", item: "startbundle", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
				if(data.success){
					CFW_DASHBOARD_HISTORY_BUNDLE_STARTED = true;
				}
			});
		$.ajaxSetup({async: true});
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_history_completeOperationsBundle(){
	
	if(!CFW_DASHBOARD_HISTORY_IS_UPDATING){
		$.ajaxSetup({async: false});
			CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "undoredo", item: "endbundle", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
				if(data.success){
					CFW_DASHBOARD_HISTORY_BUNDLE_STARTED = false;
				}
			});
		$.ajaxSetup({async: true});
	}
	
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_history_triggerUndo(){
	
	if(CFW_DASHBOARD_EDIT_MODE && !CFW_DASHBOARD_HISTORY_IS_UPDATING){
		
		CFW_DASHBOARD_HISTORY_IS_UPDATING = true;
		
		cfw_dashboard_toggleEditMode();
			
		CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "undoredo", item: "triggerundo", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
			
			if(data.success){
				cfw_dashboard_drawEveryWidget(data);
			}
			
			CFW_DASHBOARD_HISTORY_IS_UPDATING = false; 
			cfw_dashboard_toggleEditMode();
			
		});

	}
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_history_triggerRedo(){
	
	if(CFW_DASHBOARD_EDIT_MODE && !CFW_DASHBOARD_HISTORY_IS_UPDATING){
		
		CFW_DASHBOARD_HISTORY_IS_UPDATING = true;
		cfw_dashboard_toggleEditMode();
			
		CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "undoredo", item: "triggerredo", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
			if(data.success){ 
				cfw_dashboard_drawEveryWidget(data);
			}
			 CFW_DASHBOARD_HISTORY_IS_UPDATING = false; 
			 cfw_dashboard_toggleEditMode();
		});
				
	}
	
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_getWidgetDefinition(widgetUniqueType){
	
	return CFW_DASHBOARD_WIDGET_REGISTRY[widgetUniqueType];
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_parameter_fireParamWidgetUpdate(paramButton){
	
	//----------------------------------
	// Initialize
	var FIELDNAME_PROMPT_PW = "cfw-promptpassword";
	var FIELDNAME_AFFECTED_WIDGETS = "cfw-affectedwidgets";
	var paramButton = $(paramButton);
	var widgetID = paramButton.closest('.cfw-parameter-widget-parent').data('widget-id');
	var widgetElement = $('.grid-stack-item[data-id='+widgetID+']');
	var paramForms = $('.cfw-parameter-widget-parent form');
	
	console.log(paramButton)
	console.log(widgetID)
	console.log(widgetElement)
	//-----------------------------------------------
	// Create merged Params of All Parameter Widgets
	var mergedParams = {}; 
	paramForms.each(function(){
		var userParamsForWidget = CFW.format.formToParams($(this), true);
		var preparedParams = {};
		// add to URL
		for(key in userParamsForWidget){

			if(key != CFW.global.formID 
			&& key != FIELDNAME_PROMPT_PW
			&& key != FIELDNAME_AFFECTED_WIDGETS
			){
				preparedParams[key] = userParamsForWidget[key];
			}
		}
		cfw_dashboard_setURLParams(preparedParams);
		Object.assign(mergedParams, userParamsForWidget); 
	});
	
	//-----------------------------------------------
	// Override certain Params from the Originating
	// Param Widget
	originatingForm = widgetElement.find('.cfw-parameter-widget-parent form');
	originatingForm.each(function(){
		var userParamsForWidget = CFW.format.formToParams($(this), true);
		var preparedParams = {};
		// add to URL
		for(key in userParamsForWidget){

			if(key == FIELDNAME_AFFECTED_WIDGETS
			|| key == FIELDNAME_PROMPT_PW
			){
				preparedParams[key] = userParamsForWidget[key];
			}
		}
		cfw_dashboard_setURLParams(preparedParams);
		Object.assign(mergedParams, userParamsForWidget); 
	});
	
	//-----------------------------------------------
	// Create merged Params of All Parameter Widgets
	
	//----------------------------------
	// Get Prompt and Store
	var doPrompt = mergedParams[FIELDNAME_PROMPT_PW];
	var affectedWidgetsString = mergedParams[FIELDNAME_AFFECTED_WIDGETS];
	var affectedWidgetsArray = 
		(affectedWidgetsString == null || affectedWidgetsString == "[]") ? [] : JSON.parse(affectedWidgetsString);
		
	delete mergedParams[CFW.global.formID];
	delete mergedParams[FIELDNAME_PROMPT_PW];
	delete mergedParams[FIELDNAME_AFFECTED_WIDGETS];

	cfw_parameter_storeUserParams(mergedParams);
	
	//----------------------------------
	// Prepare Params and Update Function
	
	//For security reasons, password check will always be sent, regardless if prompt is shown or not
	var passwordCheckParams = { action: 'fetch'
					, item: 'paramwidgetpwcheck'
					, dashboardid: CFW_DASHBOARD_URLPARAMS.id
					, widgetid: widgetID
					, credentialKey: ''
					}; 
	
	var updateFunction = function(affectedWidgetsArray){
		
		if(affectedWidgetsArray.length == 0){ 
			// -------------------------------
			// Redraw every Widget except 
			// parameter widgets
			$(".grid-stack-item").each(function(){
				
				var widgetID = affectedWidgetsArray[i];
				var widgetInstance = $(this);
				var guid = widgetInstance.attr('id');
				var widgetObject = widgetInstance.data("widgetObject");
				
				if(widgetObject.TYPE != "cfw_parameter"){
					cfw_dashboard_widget_rerender(guid, false);
				}
			});
			
		}else{
			// -------------------------------
			// Redraw affected widgets
			for(var i in affectedWidgetsArray){
				var widgetID = affectedWidgetsArray[i];
				var guid = $(".grid-stack-item[data-id="+widgetID+"]").attr('id');
				if(guid != null){
					
					cfw_dashboard_widget_rerender(guid, true);
				}
			}
		}	
	}
	
	//----------------------------------
	// Execute Prompt and Updates
	
	if(!doPrompt){

		CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, passwordCheckParams, function(data){
			if(data.success){
				updateFunction(affectedWidgetsArray);
			}
		});
	}else{
		var modalBody = '<input id="widget-param-password" name="credentialKey" class="w-100" type="password" onkeyup="if(event.keyCode == 13){$(\'#cfw-small-modal-closebutton\').click();}" autocomplete="off">';
		CFW.ui.showModalSmall("Password", modalBody, function(){

			var givenPassword = $('#widget-param-password').val();
			
			passwordCheckParams.credentialKey = givenPassword;
			
			$.ajaxSetup({async: false});
				CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, passwordCheckParams, function(data){
					if(data.success){
						updateFunction(affectedWidgetsArray);
					}
				});
			$.ajaxSetup({async: true});
		});
	}

}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_registerWidget(widgetUniqueType, widgetObject){
	
	var defaultObject = {
			// The category the widget should be added to
			category: CFW.dashboard.global.categoryDefault,
			// The icon of the widget shown in the menu
			menuicon: "fas fa-th-large",
			// the label of the widget
			menulabel: "Unnamed Widget",
			// Description of the widget
			description: "",
			// Override to customize initial settings
			defaultsettings: {},
			// function that creates the widget content and returns them to the
			// framework by calling the callback function
			createWidgetInstance: function (widgetObject2, params, callback) {				
				callback(widgetObject2, params, "Please specify a function on your widgetDefinition.createWidgetInstance.");
			},
			// Must return a html string representing a HTML form. Or null if no
			// settings are needed for this widget.
			getEditForm: function (widgetObject3) {
				return CFW.dashboard.getSettingsForm(widgetObject3);
			},
			
			// Store the values to the widgetObject. Return true if the data
			// should be saved to the server, false otherwise.
			onSave: function (form, widgetObject4) {
				widgetObject4.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
	}
	
	var merged = Object.assign({}, defaultObject, widgetObject);
	if(merged.defaultsettings.TITLE == null){
		merged.defaultsettings.TITLE = merged.menulabel;
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

/*******************************************************************************
 * Register a Category in a hierarchical manner. The category name is either a
 * single name, or a pipe ('|') separated string like "RootCategory |
 * SubCategory | SubSubCategory". The label will be used for the last item.
 * 
 * @param faiconClasses
 * @param the
 *            name of the category, used to reference the category
 * @param the
 *            label of the category, used for localization
 ******************************************************************************/
function cfw_dashboard_registerCategory(faiconClasses, categoryName, categoryLabel){
	
	// ----------------------------
	// Check Category Exists
	var categoryNameTrimmed = categoryName.replace(/[ ]*\|[ ]*/g, '|').trim();
	
	var categorySubmenu = $('ul[data-submenuof="'+categoryNameTrimmed+'"]');
	if(categorySubmenu.length > 0){
		return;
	}
	
	// ----------------------------
	// Get Tokens
	var tokens = categoryNameTrimmed.split('|');
		
	// ----------------------------
	// Create Category with Parents
	var parent = $('#addWidgetDropdown');
	var currentCategoryName = tokens[0];

	for(var i = 0; i < tokens.length; i++ ){
		
		// ----------------------------
		// Category Label
		let currentLabel = tokens[i];
		if(i == tokens.length-1){
			if(categoryLabel != null){
				currentLabel = categoryLabel;
			}
		}
		// ----------------------------
		// Check exists
		var currentSubmenu = $('ul[data-submenuof="'+currentCategoryName+'"]');
		if(currentSubmenu.length == 0){

			// ----------------------------
			// Create
			var categoryHTML = 
				'<li class="dropdown dropdown-submenu">'
					+'<a href="#" class="dropdown-item dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true"><div class="cfw-fa-box"><i class="'+faiconClasses+'"></i></div><span class="cfw-menuitem-label">'+currentLabel+'</span><span class="caret"></span></a>'
					+'<ul class="dropdown-menu dropdown-submenu" data-submenuof="'+currentCategoryName+'">'
					+'</ul>'
				+'</li>';
			parent.append(categoryHTML);
			
		}
		// ----------------------------
		// Update parent and name
		if(i < tokens.length-1){
			parent = $('ul[data-submenuof="'+currentCategoryName+'"]');
			currentCategoryName = currentCategoryName + '|' +tokens[i+1];
		}
		
	}
		
	
}




/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_createGUID(){
	CFW_DASHBOARD_WIDGET_GUID++;
	return "widget-"+CFW_DASHBOARD_WIDGET_GUID ;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_selectByID(widgetID){
	return $('.grid-stack-item[data-id="'+widgetID+'"]');
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_selectByGUID(widgetGUID){
	return $('#'+widgetGUID);
}
	
/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_edit(widgetGUID){
	
	var widgetInstance = $('#'+widgetGUID);
	var widgetObject = widgetInstance.data("widgetObject");
	var widgetDef = CFW.dashboard.getWidgetDefinition(widgetObject.TYPE);
	
	// ##################################################
	// Create Widget Specific Form
	// ##################################################
	var customForm = widgetDef.getEditForm(widgetObject);
	if(customForm != null){
		customForm = $(customForm);
		var buttons = customForm.find('button[type="button"]');
		if(buttons.length > 0){
			buttons.remove();
		}
		var customFormButton = '<button type="button" onclick="cfw_dashboard_widget_save_widgetSettings(this, \''+widgetGUID+'\')" class="form-control btn-primary">'+CFWL('cfw_core_save', 'Save')+'</button>';
		
		customForm.append(customFormButton);
	}else{
		return;
	}
	
	// ##################################################
	// Create Widget Specific Form
	// ##################################################
	var defaultForm = cfw_dashboard_widget_getSettingsFormDefault(widgetObject);
	if(defaultForm != null){
		defaultForm = $(defaultForm);
	}else{
		return;
	}

	// ##################################################
	// Create Description
	// ##################################################
	var compositeDiv = $('<div id="editWidgetComposite">');
	
	//---------------------------
	// Create Panel
	 var panelSettings = {
			cardstyle: null,
			textstyle: null,
			textstyleheader: 'white',
			title: "Manual",
			body: "&nbsp;",
			narrow: true,
			maxheight: '50vh'
	};
	
	var cfwPanel = new CFWPanel(panelSettings);
	cfwPanel.appendTo(compositeDiv);
	
	cfwPanel.onclick(function(event){ 
		var params = {
			action: 'fetch'
			, item: 'manualpage'
			, type: widgetObject.TYPE
		};
		
		CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, params, 
			function(data){
				if(data.payload != null){
					cfwPanel.panelBody.html(data.payload);							
				}
			}
		);
	});
	
	//compositeDiv.append('<p><strong>Widget:</strong>&nbsp;'+widgetDef.menulabel+'</p>');
	//compositeDiv.append('<p><strong>Description:</strong>&nbsp;'+widgetDef.description+'</p>');
	
	//---------------------------
	// Create Panel
	//used for autocomplete parameter substitution
	compositeDiv.append('<div id="edited-widget-type" class="d-none">'+widgetObject.TYPE+'</div>');
	
	// ##################################################
	// Pill Navigation
	// ##################################################

	var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');

	list.append(
		'<li class="nav-item"><a class="nav-link active" id="widgetSettingsTab" data-toggle="pill" href="#widgetSettings" role="tab" ><i class="fas fa-tools mr-2"></i>Widget Settings</a></li>'
		+'<li class="nav-item"><a class="nav-link" id="defaultSettingsTab" data-toggle="pill" href="#defaultSettings" role="tab" ><i class="fas fa-cog mr-2"></i>Standard Settings</a></li>'
	);
	
	if(cfw_hasPermission('Dashboard Tasks')){
		list.append('<li class="nav-item"><a class="nav-link" id="defaultSettingsTab" data-toggle="pill" href="#taskSettings" role="tab" onclick="cfw_dashboard_widget_editCreateTaskParamsForm('+widgetObject.PK_ID+')" ><i class="fas fa-play-circle mr-2"></i>Task Settings</a></li>');
	}
		
	compositeDiv.append(list);
	compositeDiv.append('<div id="settingsTabContent" class="tab-content">'
			  +'<div class="tab-pane fade show active" id="widgetSettings" role="tabpanel" aria-labelledby="widgetSettingsTab"></div>'
			  +'<div class="tab-pane fade" id="defaultSettings" role="tabpanel" aria-labelledby="defaultSettingsTab"></div>'
			  +'<div class="tab-pane fade" id="taskSettings" role="tabpanel" aria-labelledby="taskSettingsTab"></div>'
		+'</div>' );
	
	if(customForm != null){
		compositeDiv.find('#widgetSettings').append(customForm);
	}
	compositeDiv.find('#defaultSettings').append(defaultForm);
	
	// ----------------------------------
	// Show Modal
	CFW.ui.showModalMedium(CFWL('cfw_core_settings', 'Settings'), compositeDiv, "CFW.cache.clearCache();", true);
	
	// -----------------------------------
	// Initialize Forms
	cfw_initializeForm(customForm);
	cfw_initializeForm(defaultForm);
	
	$('#editWidgetComposite [data-toggle="tooltip"]').tooltip();
				
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_editCreateTaskParamsForm(widgetID) {
	
	var formHTML = null;
	
	$.ajaxSetup({async: false});
		CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, {action: 'fetch', item: 'taskparamform', widgetid: widgetID, dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
				
				if(data.payload != null){
					
					let taskSettingsTab = $('#taskSettings');
					let payload = data.payload;
					taskSettingsTab.html('');
					
					//------------------------------------
					// Check supports Tasks
					if(!payload.supportsTask){
						taskSettingsTab.append('<p class="alert alert-info mb-2">This widget does not support tasks.</p>');
						return;
					}
					
					//------------------------------------
					// Add Description
					if(payload.taskDescription != null){
						taskSettingsTab.append('<p><b>Task Description:&nbsp;</b>'+payload.taskDescription+'</p>');
					}
					//------------------------------------
					// Add Disable Button
					if(!payload.hasJob){
						taskSettingsTab.append('<p class="alert alert-info  mb-2">This widget currently does not have a running task. Save the below form and set the task to enable to create a running task.</p>');
					}
					
					taskSettingsTab.append(payload.html);
					
					// -----------------------------------
					// Initialize Forms
					var taskForm = taskSettingsTab.find('form');
					cfw_initializeForm(taskForm);					
				}
			}
		);
	$.ajaxSetup({async: true});
	
}



/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_copyToClipboard(widgetGUID) {
	var widgetInstance = $('#'+widgetGUID);
	var widgetObject = widgetInstance.data("widgetObject");
		

	CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "fetch", item: "widgetcopy", widgetid: widgetObject.PK_ID}, function(data){
		
		if(data.success){
			CFW.utils.clipboardWrite(data.payload);
			CFW.ui.addToastInfo("Task settings are not copied.")
		}
	
	});
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_copyToClipboardAsReplica(widgetGUID) {
	var widgetInstance = $('#'+widgetGUID);
	var widgetObject = widgetInstance.data("widgetObject");
		

	CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "fetch", item: "widgetcopy", widgetid: widgetObject.PK_ID}, function(data){
		
		if(data.success){
			
			//-----------------------------
			// Prepare Replica JSON_SETTINGS
			var widgetInfo = {};
			widgetInfo[widgetObject.PK_ID] = widgetObject.TITLE;
			var dashboardInfo = {};
			var dashboardID = CFW_DASHBOARD_URLPARAMS.id;
			dashboardInfo[dashboardID] = JSDATA.dashboardName;
			
			//-----------------------------
			// Override Settings
			let widgetSettingsReplica = JSON.parse(data.payload);
			widgetSettingsReplica.TYPE = "cfw_replica";
			widgetSettingsReplica.JSON_SETTINGS = {   
		        "JSON_DASHBOARD": dashboardInfo,
		        "JSON_WIDGET": widgetInfo,
		    };
		   
		    //-----------------------------
			// Copy to Clipboard
			CFW.utils.clipboardWrite( JSON.stringify(widgetSettingsReplica) );
		}
	
	});
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_handlePaste() {
	
	if(CFW_DASHBOARD_EDIT_MODE){
		CFW.utils.clipboardRead(
			function(clipboardValue){
				
				//-------------------------------
				// Check not null or empty
				if( CFW.utils.isNullOrEmpty(clipboardValue) ){ return ;}
				
				//-------------------------------
				// Read Data
				widgetObject = null;
				parametersArray = null;
				if( clipboardValue.startsWith('{"widgetObject":') ){
					var clipObject = JSON.parse(clipboardValue);
					widgetObject = clipObject.widgetObject;
					
				}else if(clipboardValue.startsWith('{')
					  && clipboardValue.includes('FK_ID_DASHBOARD')){
					widgetObject = JSON.parse(clipboardValue);
				}
				
				//-------------------------------
				// Paste Widget
				if(widgetObject != null){
					widgetObject.FK_ID_DASHBOARD = CFW_DASHBOARD_URLPARAMS.id;
					widgetObject.guid = cfw_dashboard_widget_createGUID();
					cfw_dashboard_widget_add(widgetObject.TYPE, widgetObject, true, true);
				}
										
			}
		);
	}

}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_save_defaultSettings(widgetID){
	
	//---------------------------
	// Get Variables
	var widget = $('.grid-stack-item[data-id='+widgetID+']')
	var widgetGUID = widget.attr('id');
	var widgetObject = widget.data("widgetObject");
	var settingsForm = $('#formEditDefaultSettings'+widgetID);
	
	//---------------------------
	// Set Data to Object
	var settingsObject = CFW.format.formToObject(settingsForm, false);	
	delete settingsObject[CFW.global.formID];
	
	Object.assign(widgetObject, settingsObject);
	
	
	//---------------------------
	// Rerender
	cfw_dashboard_history_startOperationsBundle();
		
		$.ajaxSetup({async: false});
			// Make sure to save state before rerender
			cfw_dashboard_widget_save_state(widgetObject, true, true);
			
			// TODO: A bit ugly, might trigger another save
			cfw_dashboard_widget_rerender(widgetGUID, false);
		$.ajaxSetup({async: true});
	
	cfw_dashboard_history_completeOperationsBundle();
	
	CFW.ui.addToastInfo('Done');
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_save_widgetSettings(formButton, widgetGUID){
	
	cfw_dashboard_history_startOperationsBundle();
	
	var widget = $('#'+widgetGUID);
	var widgetObject = widget.data("widgetObject");
	
	var widgetDef = CFW.dashboard.getWidgetDefinition(widgetObject.TYPE);
	
	// switch summernote to wysiwyg view
	$('.btn-codeview.active').click();
	
	var success = widgetDef.onSave($(formButton).closest('form'), widgetObject);
	
	if(success){
		
		$.ajaxSetup({async: false});
			
			// Make sure to save state before rerender
			cfw_dashboard_widget_save_state(widgetObject, true, false);

			// TODO: A bit ugly, might trigger another save
			cfw_dashboard_widget_rerender(widgetGUID, false);
		
		$.ajaxSetup({async: true});
	
		cfw_dashboard_history_completeOperationsBundle();
		
		CFW.ui.addToastInfo('Done');
		
	}
	
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_save_state(widgetObject, forceSave, defaultSettingsOnly) {


	if(forceSave || ( JSDATA.canEdit == true && CFW_DASHBOARD_EDIT_MODE) ){
		
		var itemToUpdate = 'widgetfull';
		if(defaultSettingsOnly){
			itemToUpdate = 'widgetdefaultsettings';
		}

		delete widgetObject.widgetBody;
		// ----------------------------------
		// Update Object
		var params = {
			  action: 'update'
			, item: itemToUpdate
			, dashboardid: CFW_DASHBOARD_URLPARAMS.id
			, params: JSON.stringify(cfw_parameter_getFinalPageParams())
			, widget: JSON.stringify(widgetObject)
		}; 
		
		//delete params.widget.JSON_SETTINGS;
		
		// ----------------------------------
		// Update in Database
		CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, params, function(data){});
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_removeConfirmed(widgetGUID){
	CFW.ui.confirmExecute('Do you really want to remove this widget?', 'Remove', "cfw_dashboard_widget_remove('"+widgetGUID+"')" );
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_remove(widgetGUID) {
	var widget = $('#'+widgetGUID);
	var widgetObject = widget.data('widgetObject');
	
	cfw_dashboard_history_startOperationsBundle();
	
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, {action: 'delete', item: 'widget', widgetid: widgetObject.PK_ID, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, function(data){

			if(data.success){
				cfw_dashboard_widget_removeFromGrid(widget);
				
				cfw_dashboard_history_completeOperationsBundle();
			}
		}
	);

};


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_removeFromGrid(widgetElement) {
	var grid = cfw_dashboard_getGrid();
	grid.removeWidget(widgetElement.get(0));
};




/*******************************************************************************
 * 
 * @param type the type of the widget
 * @param optionalWidgetObjectData data for the widgetObject, used for redo
 *        and paste actions 
 * @param doAutoposition define if autoposition should be done or not in case
 *        widget data is provided
 ******************************************************************************/
function cfw_dashboard_widget_add(type, optionalWidgetObjectData, doAutoposition, isPaste) {

	var params =  {
		action: 'create'
		, item: 'widget'
		, type: type
		, dashboardid: CFW_DASHBOARD_URLPARAMS.id 
	}
	
	if(isPaste){
		params.withdata = true;
		params.data = JSON.stringify(optionalWidgetObjectData);
	}
	
	cfw_dashboard_history_startOperationsBundle();
	
	$.ajaxSetup({async: false});
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, params, function(data){
			var widgetObject = data.payload;
			if(widgetObject != null){
				
				// -------------------------
				// Handle Redo/Paste
				if(optionalWidgetObjectData != null){
					optionalWidgetObjectData.PK_ID = widgetObject.PK_ID;
					
					// Make sure to save state before rerender
					$.ajaxSetup({async: false});
						cfw_dashboard_widget_createInstance(optionalWidgetObjectData, doAutoposition, false);
					$.ajaxSetup({async: true});
					
					//TODO: A bit ugly, triggers another save
					cfw_dashboard_widget_rerender(optionalWidgetObjectData.guid, false);
										
					return;
				}
				
				// -------------------------
				// Handle Initial Creation
				var widgetDefinition = CFW.dashboard.getWidgetDefinition(type);
				widgetObject.TYPE   = type;

				if(widgetDefinition.defaultsettings != null){
					widgetObject = Object.assign(widgetObject,widgetDefinition.defaultsettings);
				}

				cfw_dashboard_widget_createInstance(widgetObject, true, false, function(subWidgetObject){
					
				});
				
			}
		}
	);
	$.ajaxSetup({async: true});
	cfw_dashboard_history_completeOperationsBundle();
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_duplicate(widgetGUID) {
	var widgetInstance = $('#'+widgetGUID);
	var widgetObject = widgetInstance.data("widgetObject");
	
	cfw_dashboard_history_startOperationsBundle();
	
	$.ajaxSetup({async: false});
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, {action: 'duplicate', item: 'widget', widgetid: widgetObject.PK_ID, dashboardid: CFW_DASHBOARD_URLPARAMS.id }, 
		function(data){
			var newWidgetObject = data.payload;
			if(newWidgetObject != null){
				// ---------------------------------
				// remove content to avoid circular
				// references on deep copy
				var withoutContent = Object.assign(widgetObject);
				delete withoutContent.widgetBody;
				
				// ---------------------------------
				// Deep copy and create Duplicate
				var deepCopyWidgetObject = _.cloneDeep(withoutContent);
				deepCopyWidgetObject.PK_ID = newWidgetObject.PK_ID;
				delete deepCopyWidgetObject.guid;
				
				if(widgetObject.TYPE == "cfw_parameter"){
					// do not allow the peeps(especially the theusinator) to get the same parameter twice.
					deepCopyWidgetObject.JSON_SETTINGS = {JSON_PARAMETERS: {}};
					CFW.ui.addToastInfo('Parameters removed to avoid duplicated parameters.');
				}

				cfw_dashboard_widget_createInstance(deepCopyWidgetObject, true, false);
			}
		}
	);
	$.ajaxSetup({async: true});
	cfw_dashboard_history_completeOperationsBundle();
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_getSettingsFormWidget(widgetObject) {
	
	var formHTML = null;
	
	var params = Object.assign({action: 'fetch', item: 'settingsformwidget'}, widgetObject); 
	
	delete params.widgetBody;
	delete params.guid;
	delete params.JSON_SETTINGS;
	
	params.JSON_SETTINGS = JSON.stringify(widgetObject.JSON_SETTINGS);
	
	$.ajaxSetup({async: false});
		CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, params, function(data){
				if(data.payload != null){
					formHTML = data.payload.html;
				}
			}
		);
	$.ajaxSetup({async: true});
	
	return formHTML;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_getSettingsFormDefault(widgetObject) {
	
	var formHTML = null;
	
	var params = Object.assign({action: 'fetch', item: 'settingsformdefault'}, widgetObject); 
	
	delete params.widgetBody;
	delete params.guid;
	delete params.JSON_SETTINGS;
	
	$.ajaxSetup({async: false});
		CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, params, function(data){
				if(data.payload != null){
					formHTML = data.payload.html;
				}
			}
		);
	$.ajaxSetup({async: true});
	
	return formHTML;
}


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_fetchData(widgetObject, dashboardParams, callback) {
	
	var formHTML = "";
	
	var urlParams = { action: 'fetch'
					, item: 'widgetdata'
					, dashboardid: CFW_DASHBOARD_URLPARAMS.id
					, timeframepreset: CFW.http.getURLParams()['timeframepreset']
					, widgetid: widgetObject.PK_ID
					, params: JSON.stringify(dashboardParams)
					, forcerefresh: CFW_DASHBOARD_FORCE_REFRESH
					}; 
	
	var definition = CFW.dashboard.getWidgetDefinition(widgetObject.TYPE);
	
	var settings = widgetObject.JSON_SETTINGS;
	
	// ----------------------------
	// Check has Timeframe
	urlParams.timeframe = $("#"+CFW_DASHBOARD_TIME_FIELD_ID).val();
	settings = _.cloneDeep(settings);
		
	//urlParams.timeframe_earliest = CFW_DASHBOARD_TIME_EARLIEST_EPOCH;
	//urlParams.timeframe_latest = CFW_DASHBOARD_TIME_LATEST_EPOCH;

		
	//--------------------------------------
	// Add timeZoneOffset
	var timeZoneOffset = new Date().getTimezoneOffset();
	urlParams.timezoneOffsetMinutes = timeZoneOffset;
	
	//----------------------------
	// Fetch Data
	
	CFW.http.postJSON(CFW_DASHBOARDVIEW_URL, urlParams, function(data){
		callback(data);
	});

	return formHTML;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_rerender(widgetGUID, manualLoad) {
	var widget = $('#'+widgetGUID);
	var widgetObject = widget.data("widgetObject");
	cfw_dashboard_widget_removeFromGrid(widget);
	cfw_dashboard_widget_createInstance(widgetObject, false, manualLoad);
	
}

/*******************************************************************************
 * Creates the HTML element and returns it as a jQuery object.
 ******************************************************************************/
function cfw_dashboard_widget_createHTMLElement(widgetObject){
	
	// ---------------------------------------
	// Merge Data
	var defaultOptions = {
			guid: cfw_dashboard_widget_createGUID(),
			TITLE: "",
			TITLE_LINK: null,
			TITLE_INFO: null,
			TITLE_FONTSIZE: 16,
			TITLE_POSITION: 'top',
			TITLE_ALIGN: 'start',
			CONTENT_FONTSIZE: 16,
			FOOTER: "",
			BGCOLOR: "",
			FGCOLOR: "",
			MANUAL_LOAD: false,
			INVISIBLE: false,
			JSON_SETTINGS: {}
	}
	
	var merged = Object.assign({}, defaultOptions, widgetObject);
	
	// ---------------------------------------
	// Resolve Classes
	var borderClass = '';
	if(merged.FGCOLOR != null && merged.FGCOLOR.trim().length > 0){
		borderClass = ' border-'+merged.FGCOLOR;
	}
		
	var advancedDisplayClass = 'd-none';
	if(widgetObject.WIDTH >= 3 && CFW_DASHBOARD_EDIT_MODE_ADVANCED){
		advancedDisplayClass = '';
	}

	var titleInfoBadge = '';
	if( !CFW.utils.isNullOrEmpty(widgetObject.TITLE_INFO) ){
		titleInfoBadge = `<span class="badge badge-info cfw-decorator" 
		data-toggle="tooltip" 
		data-placement="top"
		data-delay="300" title=""
		data-original-title="${widgetObject.TITLE_INFO}">
		<i class="fa fa-sm fa-info"></i></span>
		`;
	}

	
	var titleposClass = "";
	var titleBorderClass = "border-bottom ";
	if(merged.TITLE_POSITION == "left"){ 
		titleposClass = "flex-row "; 
		titleBorderClass = "";
	}else if(merged.TITLE_POSITION == "bottom"){
		titleposClass = "flex-column-reverse "; 
		titleBorderClass = "border-top ";
	}else if(merged.TITLE_POSITION == "right"){
		titleposClass = "flex-row-reverse "; 
		titleBorderClass = "";
	}
	
	var titleLinkStart = "";
	var titleLinkEnd = "";
	if( !CFW.utils.isNullOrEmpty(merged.TITLE_LINK) ){ 
		titleLinkStart = '<a target="_blank" href="'+merged.TITLE_LINK+'">'; 
		titleLinkEnd = "</a>";
	}
	
	var titleLinkAlignClass = '';
	if(merged.TITLE_POSITION == "top" || merged.TITLE_POSITION == "bottom" ){
		if 		(merged.TITLE_ALIGN == "center"){	titleLinkAlignClass = "text-center"; }
		else if (merged.TITLE_ALIGN == "end"){	titleLinkAlignClass = "text-right"; }
	}else{
		titleLinkAlignClass = ' align-self-'+merged.TITLE_ALIGN;
	}
	
	let styleBody = 'font-size: '+merged.CONTENT_FONTSIZE+'px;';
		
	var htmlString =
		'<div class="grid-stack-item-content card d-flex '+titleposClass+'" data-type="'+widgetObject.TYPE+'">'
		+'	<div role="button" class="cfw-dashboard-widget-actionicons text-cfw-lightgray show-on-edit">'
		+'		<div role="button" class="actionicon-delete '+advancedDisplayClass+'" onclick="cfw_dashboard_widget_remove(\''+merged.guid+'\')"><i class="fas fa-times"></i></div>'
		+'		<div role="button" class="actionicon-duplicate '+advancedDisplayClass+'" onclick="cfw_dashboard_widget_duplicate(\''+merged.guid+'\')"><i class="fas fa-clone"></i></div>'
		+'		<div role="button" class="actionicon-edit '+advancedDisplayClass+'" onclick="cfw_dashboard_widget_edit(\''+merged.guid+'\')"><i class="fas fa-pen"></i></div>'
		+'		<div role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fas fa-cog"></i></div>'
		+'		<div class="dropdown-menu">'
		+'			<a class="dropdown-item" onclick="cfw_dashboard_widget_edit(\''+merged.guid+'\')"><i class="fas fa-pen"></i>&nbsp;'+CFWL('cfw_core_edit', 'Edit')+'</a>'
		+'			<a class="dropdown-item" onclick="cfw_dashboard_widget_duplicate(\''+merged.guid+'\')"><i class="fas fa-clone"></i>&nbsp;'+CFWL('cfw_core_duplicate', 'Duplicate')+'</a>'
		+'			<a class="dropdown-item" onclick="cfw_dashboard_widget_copyToClipboard(\''+merged.guid+'\')"><i class="fas fa-copy"></i>&nbsp;'+CFWL('cfw_core_copy', 'Copy')+'</a>'
		+'			<a class="dropdown-item" onclick="cfw_dashboard_widget_copyToClipboardAsReplica(\''+merged.guid+'\')"><i class="fas fa-copy"></i>&nbsp;'+CFWL('cfw_core_copy', 'Copy')+' Replica</a>'
		// ' <div class="dropdown-divider"></div>'
		+'			<a class="dropdown-item" onclick="cfw_dashboard_widget_removeConfirmed(\''+merged.guid+'\')"><i class="fas fa-trash"></i>&nbsp;'+CFWL('cfw_core_remove', 'Remove')+'</a>'
		+'		</div>'
		+'	</div>'

	if(merged.TITLE != null && merged.TITLE != ''){
		htmlString += 
		 '     	  <div class="cfw-dashboard-widget-title '
								+' '+titleBorderClass
								+' '+borderClass
								+' '+titleLinkAlignClass+'"'
					+' style="font-size: '+merged.TITLE_FONTSIZE+'px;">'
					+ titleInfoBadge
		+'		  	<span>'+titleLinkStart + merged.TITLE + titleLinkEnd+'</span>'
		+'		  </div>'
	}
	
	if(merged.widgetBody != null && merged.widgetBody != ''
	|| merged.FOOTER != null && merged.FOOTER != ''){
		htmlString += 
			'<div class="cfw-dashboard-widget-body" style="'+styleBody+'">';
				if(merged.FOOTER != null && merged.FOOTER != ''){
					htmlString +=
					'		 <div class="cfw-dashboard-widget-footer border-top '+borderClass+'">'
					+			merged.FOOTER
					+'		  </div>'
				}
		htmlString += '</div>';
	}
	htmlString += '</div>';
	
	var widgetItem = $('<div id="'+merged.guid+'" data-id="'+merged.PK_ID+'"  class="grid-stack-item">');
	widgetItem.append(htmlString);
	
	//------------------------------
	// Colorize BG and FG
	let itemToColorize = widgetItem.find('.grid-stack-item-content');
	CFW.colors.colorizeElement(itemToColorize, merged.BGCOLOR, "bg", null);
	CFW.colors.colorizeElement(itemToColorize, merged.FGCOLOR, "text", null);
	widgetItem.data("widgetObject", merged)
	
	if(merged.widgetBody != null && merged.widgetBody != ''){
		widgetItem.find('.cfw-dashboard-widget-body').append(merged.widgetBody);
	}

	return widgetItem;
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_widget_createLoadingPlaceholder(widgetObject, doAutoposition) {
	
// var placeholderWidget = {
// guid: 'placeholder-'+CFW.utils.randomString(24),
// content: CFW.ui.createLoaderHTML()
// };
	
	var placeholderWidget = _.cloneDeep(widgetObject);
	placeholderWidget.widgetBody = CFW.ui.createLoaderHTML();

	var widgetInstance = cfw_dashboard_widget_createHTMLElement(placeholderWidget);

	var grid = cfw_dashboard_getGrid();

    grid.addWidget($(widgetInstance).get(0),
    		{
    			x: widgetObject.X
    			, y: widgetObject.Y
    			, w: widgetObject.WIDTH
    			, h: widgetObject.HEIGHT
    			, minH: 2
    			, minW: 1
    			, autoPosition: doAutoposition
    		}
    	);

	// ----------------------------
    // Check Visibility
	if(widgetObject.INVISIBLE != null && widgetObject.INVISIBLE){
		$('#'+widgetObject.guid).addClass('show-on-edit');
	}
    
}
/*******************************************************************************
 * 
 * @param originalWidgetObject the widget object 
 * @param doAutoposition true if the widget is a new widget and should be auto-positioned
 * @param manualLoad true if the widget is loaded manually
 * @param callback that should be called after the widget was fully added to the grid
 * @param fullRedrawCounter used to avoid collision of refreshes
 ******************************************************************************/
function cfw_dashboard_widget_createInstance(originalWidgetObject, doAutoposition, manualLoad, callback, fullRedrawCounter) {
						
	if(originalWidgetObject.guid == null){
		CFW_DASHBOARD_WIDGET_GUID++;
		originalWidgetObject.guid = "widget-"+CFW_DASHBOARD_WIDGET_GUID ;
	}
	
	var widgetDefinition = CFW.dashboard.getWidgetDefinition(originalWidgetObject.TYPE);	
	
	if(widgetDefinition != null){
		
		// ---------------------------------------
		// Apply Parameters Placeholder
		var finalParams = cfw_parameter_getFinalPageParams();
		let parameterizedSettings = cfw_parameter_applyToFields(originalWidgetObject.JSON_SETTINGS, finalParams, originalWidgetObject.TYPE);
		let widgetCloneParameterized = _.cloneDeep(originalWidgetObject);
		widgetCloneParameterized.JSON_SETTINGS = parameterizedSettings;
		
		//---------------------------------------
		// Create Instance
		try{
			/* 
				#########################################################################
				!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				#########################################################################
				
								         !!!AAAATTENION!!!
				If you change anything in this method, make sure to check the javascript
				of the replica widget. Changes might have to be done there as well.
				Testing replica widget highly recommended.
				
				#########################################################################
				!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				#########################################################################
			
			*/
			// ---------------------------------------
			// Add Placeholder
			// must use original, else there will be issues for manual load(parameters already substituted isntead of substituting newly)
			cfw_dashboard_widget_createLoadingPlaceholder(originalWidgetObject, doAutoposition);
			var placeholderWidget = $('#'+originalWidgetObject.guid);
			
			// ---------------------------------------
			// Replica Position
			// Use the autopositioning from the placeholder
			// Else positioning will not work when using "Widget Menu >> Copy Replica"
			widgetCloneParameterized.X = placeholderWidget.attr("gs-x");
			widgetCloneParameterized.Y = placeholderWidget.attr("gs-y");
			
			// ---------------------------------------
			// Handle Parameter Widget Pause, Load & Manual Load
			if(widgetCloneParameterized.PAUSE == true){
				placeholderWidget.find('.cfw-dashboard-widget-body')
					.html('<span class=" cfw-centered" title="Widget Paused"><i class="fas fa-lg fa-pause-circle" ></i></span>');
				return;
			}
			
			if(!manualLoad && widgetCloneParameterized.PARAM_WIDGET_LOAD == true){
				placeholderWidget.find('.cfw-dashboard-widget-body')
					.html('&nbsp;');
				return;
			}
			
			if(!manualLoad && widgetCloneParameterized.MANUAL_LOAD == true){
				placeholderWidget.find('.cfw-dashboard-widget-body')
					.html('<button class="btn btn-sm btn-primary cfw-centered" onclick="cfw_dashboard_widget_rerender(\''+originalWidgetObject.guid+'\', true)">Click to Load</button>');
				return;
			}
			
			// ---------------------------------------
			// Create Instance by Widget Definition
			widgetDefinition.createWidgetInstance(widgetCloneParameterized, finalParams,
				function(widgetAdjustedByWidgetDef, widgetContent){
				
					// ---------------------------------------
					// Remove Placeholder
					cfw_dashboard_widget_removeFromGrid(placeholderWidget);
					
					// ---------------------------------------
					// Check Collision
					if(fullRedrawCounter != undefined && fullRedrawCounter != CFW_DASHBOARD_FULLREDRAW_COUNTER){
						return;
					}
					
					// ---------------------------------------
					// Add Widget
					widgetAdjustedByWidgetDef.widgetBody = widgetContent;
					var widgetInstance = cfw_dashboard_widget_createHTMLElement(widgetAdjustedByWidgetDef);
	
					var grid = cfw_dashboard_getGrid();
	

				    grid.addWidget($(widgetInstance).get(0),
				    		{
				    			x: widgetAdjustedByWidgetDef.X
				    			, y: widgetAdjustedByWidgetDef.Y
				    			, w: widgetAdjustedByWidgetDef.WIDTH
				    			, h: widgetAdjustedByWidgetDef.HEIGHT
				    			, minH: 2
				    			, minW: 1
				    			, autoPosition: doAutoposition
				    		}
				    		);
				   
				    // ----------------------------
				    // Get Widget with applied default values
				    widgetAdjustedByWidgetDef = $(widgetInstance).data('widgetObject');
				    
				    // ----------------------------
				    // Check Edit Mode
				    if(!CFW_DASHBOARD_EDIT_MODE){
				    	grid.movable('#'+widgetAdjustedByWidgetDef.guid, false);
				    	grid.resizable('#'+widgetAdjustedByWidgetDef.guid, false);
				    }

					// ----------------------------
				    // Check Visibility
					if(originalWidgetObject.INVISIBLE != null && originalWidgetObject.INVISIBLE){
						$('#'+originalWidgetObject.guid).addClass('show-on-edit');
					}

					// ----------------------------
				    // Check TITLEINFO
					if( !CFW.utils.isNullOrEmpty(originalWidgetObject.TITLE_INFO)){
						$('#'+originalWidgetObject.guid)
							.find('[data-toggle="tooltip"]')
							.tooltip();
					}
					
				    // ----------------------------
				    // Update Data of Original&Clone
				    originalWidgetObject.WIDTH	= widgetInstance.attr("gs-w");
				    originalWidgetObject.HEIGHT	= widgetInstance.attr("gs-h");
				    originalWidgetObject.X		= widgetInstance.attr("gs-x");
				    originalWidgetObject.Y		= widgetInstance.attr("gs-y");

				    $(widgetInstance).data('widgetObject', originalWidgetObject);
				    
				    cfw_dashboard_widget_save_state(originalWidgetObject, false, true);
				    
				    if(callback != null){
				    	callback(originalWidgetObject);
				    }
				}
			);
		}catch(err){
			CFW.ui.addToastDanger('An error occured while creating a widget instance: '+err.message);
			console.log(err);
		}
	}
	
}

/*******************************************************************************
 * 
 ******************************************************************************/
CFW.dashboard = {
		global: {
			  categoryDefault: "Default"
			, categoryAdvanced: "Advanced"
			, categoryCustom: "Custom"
		}
		, registerWidget: 		cfw_dashboard_registerWidget
		, getWidgetDefinition: 	cfw_dashboard_getWidgetDefinition
		, registerCategory: 		cfw_dashboard_registerCategory
		, getSettingsForm:		cfw_dashboard_widget_getSettingsFormWidget
		, getSettingsFormDefault:	cfw_dashboard_widget_getSettingsFormDefault
		, fetchWidgetData: 		cfw_dashboard_widget_fetchData
		, createStatusWidgetRendererSettings: cfw_dashboard_createStatusWidgetRendererSettings
};


/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_toggleFullscreenMode(){
	
	if(CFW_DASHBOARD_FULLSCREEN_MODE){
		CFW_DASHBOARD_FULLSCREEN_MODE = false;

		$('.hideOnFullScreen').attr('style', '');
		$('.cfw-content').css('padding-top', '');
		$('.navbar').css('display', '');
		
		$('#cfw-dashboard-control-panel').css('padding', '');
		$('#cfw-dashboard-control-panel').css('margin', '');
		$('#cfw-dashboard-control-panel').css('top', '');
		
		$('#fullscreenButton')
			.removeClass('fullscreened-button')
			.addClass('fullscreenButton');
		
		$('#fullscreenButtonIcon')
			.removeClass('fa-compress')
			.addClass('fa-expand');
		
	}else{
		CFW_DASHBOARD_FULLSCREEN_MODE = true;
		// using attr('style') here as css('display') does not take !important 
		$('.hideOnFullScreen').attr('style', 'display: none !important;');
		$('.cfw-content').css('padding-top', '0px');
		$('.navbar').css('display', 'none');
		
		$('#cfw-dashboard-control-panel').css('padding', '0px');
		$('#cfw-dashboard-control-panel').css('margin', '0px');
		$('#cfw-dashboard-control-panel').css('top', '0px');
		
		$('#fullscreenButton')
			.removeClass('fullscreenButton')
			.addClass('fullscreened-button');
		
		$('#fullscreenButtonIcon')
			.removeClass('fa-expand')
			.addClass('fa-compress');
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_dashboard_toggleEditMode(){

	// -----------------------------------------
	// Toggle Edit Mode
	var grid = cfw_dashboard_getGrid();
	if(CFW_DASHBOARD_EDIT_MODE){
		CFW_DASHBOARD_EDIT_MODE = false;
		$('#cfw-container').removeClass('edit-mode')
		$('.cfw-dashboard-widget-actionicons').addClass('d-none');
		$('#addWidget').addClass('d-none');
		
		if(JSDATA.isOwner 
		|| (JSDATA.canEdit && JSDATA.canEditSettings)
		|| CFW.hasPermission('Dashboard Admin')){
			$('#dashboardSettingsButton').addClass('d-none');
		}
		
		$('#parametersButton').addClass('d-none');
		$('#doneButton').addClass('d-none');
		$('#editButton').removeClass('d-none');
		$('#top-ruler').addClass('d-none');
		$('#side-ruler').addClass('d-none');
		$('#bottom-ruler').addClass('d-none');
		
		
		grid.disable();
		
	}else{
		CFW_DASHBOARD_EDIT_MODE = true;
		$('#cfw-container').addClass('edit-mode')
		$('.cfw-dashboard-widget-actionicons').removeClass('d-none');
		$('#addWidget').removeClass('d-none');
		
		if(JSDATA.isOwner 
		|| (JSDATA.canEdit && JSDATA.canEditSettings)
		|| CFW.hasPermission('Dashboard Admin')){
			$('#dashboardSettingsButton').removeClass('d-none');
		}

		$('#parametersButton').removeClass('d-none');
		$('#doneButton').removeClass('d-none');
		$('#editButton').addClass('d-none');
		$('#top-ruler').removeClass('d-none');
		$('#side-ruler').removeClass('d-none');
		$('#bottom-ruler').removeClass('d-none');
		
		grid.enable();
	}
}

/*******************************************************************************
 * Receives a call from the timeframe picker when selection is updated.
 ******************************************************************************/
function cfw_dashboard_getGrid(){
	return document.querySelector('.grid-stack').gridstack;
}
/*******************************************************************************
 * Change duration between refreshes
 * 
 * @param selector
 *            selected value of #refreshSelector
 * @see storeLocalValue()
 * @see refreshTimer()
 ******************************************************************************/
function cfw_dashboard_setReloadInterval(selector) {
	
	
	var refreshInterval = $(selector).val();
	
	// ------------------------
	// Disable Old Interval
	if(refreshInterval == null 
	|| (refreshInterval == 'stop' && CFW_DASHBOARD_REFRESH_INTERVAL_ID != null) ){
		window.clearInterval(CFW_DASHBOARD_REFRESH_INTERVAL_ID);
		window.localStorage.setItem("dashboard-reload-interval-id"+CFW_DASHBOARD_URLPARAMS.id, 'stop');
		return;
	}
	
	// ------------------------
	// Prevent user set lower interval
// if(refreshInterval < 300000){
// refreshInterval = 300000;
// }

	// ------------------------
	// Disable Old Interval
	if(CFW_DASHBOARD_REFRESH_INTERVAL_ID != null){
		window.clearInterval(CFW_DASHBOARD_REFRESH_INTERVAL_ID);
	}
	
	CFW_DASHBOARD_REFRESH_INTERVAL_ID = window.setInterval(function(){
    	if(!CFW_DASHBOARD_EDIT_MODE){
    		// Use gridstack.removeAll() to prevent widgets from jumping around
			// on reload
    		// $('.grid-stack').html('');
    		
	    	cfw_dashboard_draw(false, false);
	    };
    }, refreshInterval);
	
	window.localStorage.setItem("dashboard-reload-interval-id"+CFW_DASHBOARD_URLPARAMS.id, refreshInterval);
    	
}

/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_dashboard_initialize(gridStackElementSelector){
	
	// -----------------------------
	// Create Title
	var titleSpan = $('<span>'+JSDATA.dashboardName+'</span>');
	
	if(JSDATA.userid != null){
		var params = {action: "update", item: 'favorite', listitemid: CFW_DASHBOARD_URLPARAMS.id};
		var cfwToggleButton = CFW.ui.createToggleButton(CFW_DASHBOARDLIST_URL, params, JSDATA.dashboardIsFaved, "fave");
		var button = cfwToggleButton.getButton();
		button.addClass('mb-1');
		button.find('i').addClass('fa-lg');
		titleSpan.prepend(button  );
	}
	
	$('#dashboardName').append(titleSpan);
	
	// -----------------------------
	// Toggle Edit Button
	if(JSDATA.canEdit){
		$('#editButton').removeClass('d-none');
		
		$('body').keyup(function (e){
			
			// --------------------------------
			// Abort if Modal is open
			if($('.modal.show').length > 0){
				return;
			}
			
			// --------------------------------
			// Ctrl+V - Trigger Paste
			if (e.ctrlKey && e.keyCode == 86) {
				cfw_dashboard_widget_handlePaste();
				return;
			}
			
			// --------------------------------
			// Ctrl+Y - Trigger Redo
			if (e.ctrlKey && e.keyCode == 89) {
				cfw_dashboard_history_triggerRedo()
				return;
			}
			
			// --------------------------------
			// Ctrl+Z - Trigger Undo
			if (e.ctrlKey && e.keyCode == 90) {
				cfw_dashboard_history_triggerUndo();
				return;
			}
			
			// --------------------------------
			// Ctrl+Alt+E - Trigger Edit Mode
			if (e.ctrlKey && event.altKey && e.keyCode == 69) {
				cfw_dashboard_toggleEditMode();
				return;
			}
			
			// --------------------------------
			// Ctrl+Alt+A - Toggle Advanced Edit Mode
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
	
	//--------------------------------------------
	// Enhance Autocomplete Requests
	
	cfw_autocomplete_addParamEnhancer( function(inputField, requestAttributes, originalRequestAttributes){

			//---------------------------
			// Add DashboardID
			requestAttributes['cfw-dashboardid'] = CFW_DASHBOARD_URLPARAMS.id;
			
			//replace custom parameter values in request params
			
			
			//------------------------------------------
			// Parameterize Edit Widget Modal Request
			if($('#editWidgetComposite').is(":visible")){
				let widgetType = $('#edited-widget-type').text();

				let dashboardParams = cfw_parameter_getFinalPageParams();		
				let parameterizedRequestAttributes = cfw_parameter_applyToFields(requestAttributes, dashboardParams, widgetType);
								
				Object.assign(requestAttributes, parameterizedRequestAttributes);
				
				return;
			}
			
			//------------------------------------------
			// Parameterize Parameter Widget Request
			// TODO
			var form = inputField.closest('form');
			
			if(form.attr('id').startsWith('cfwWidgetParameterForm')){
				
				// Applied Param values from dashboard
				dashboardParams = cfw_parameter_getFinalPageParams();
				for(index in dashboardParams){
					let param = dashboardParams[index];
					
					requestAttributes[param.LABEL] = param.VALUE;
					
				}
				
				// Current (unapplied) values from widget
				
				var allFields = form.find('.cfw-widget-parameter-marker');
				var requestSaveValues = cfw_format_formToParams(form);
				
				allFields.each(function(){
					var currentParamField = $(this);
					var name = currentParamField.attr('name');
					var value = requestSaveValues[name];

					var widgettype = inputField.data("widgettype");
					var settingslabel = inputField.data("settingslabel");
					
					if(!CFW.utils.isNullOrEmpty(widgettype)){
						requestAttributes[param.LABEL] = value;
					}
					
				});
				
				
				
			}
			
			
		}
	);
	
	// -----------------------------
	// Setup Gridstack
	var grid = GridStack.init({
		alwaysShowResizeHandle: /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent),
		resizable: {
		    handles: 'e, se, s, sw, w'
		  },
		// IMPORTANT: do not change or you mess up existing dashboards
		column: 64, 
		// IMPORTANT: do not change or you mess up existing dashboards
		cellHeight: '15px',
		// no animation, widgets get "seizures" sometimes
		animate: false,
		float: true,
		marginTop: 6,
		marginBottom: 0,
		marginLeft: 3,
		marginRight: 3,
		// needed to avoid destroying dashboard layout when resizing window in edit mode.
		disableOneColumnMode: true,

	}, gridStackElementSelector);
	
	// -----------------------------
	// Set update on dragstop
	grid.on('change', function(event, items) {
		
		var manageBundle = !CFW_DASHBOARD_HISTORY_BUNDLE_STARTED && !CFW_DASHBOARD_HISTORY_IS_UPDATING;
		
		if(manageBundle){
			cfw_dashboard_history_startOperationsBundle();
		}
		
			for(var key in items){
				
				var currentItem = items[key].el;
			
				var widgetInstance = $(currentItem);
				var widgetObject 	 = widgetInstance.data("widgetObject");

				widgetObject.X		= widgetInstance.attr("gs-x");
				widgetObject.Y		= widgetInstance.attr("gs-y");
				widgetObject.WIDTH	= widgetInstance.attr("gs-w");
				widgetObject.HEIGHT	= widgetInstance.attr("gs-h");
				
				//---------------------------------
				// Show or hide Action Icons
				if(widgetObject.WIDTH >= 3 && CFW_DASHBOARD_EDIT_MODE_ADVANCED){
					//var widgetSelector = '#'+widgetObject.guid+' ';
					widgetInstance.find('.actionicon-delete').removeClass('d-none');
					widgetInstance.find('.actionicon-duplicate').removeClass('d-none');
					widgetInstance.find('.actionicon-edit').removeClass('d-none');
				}else{
					widgetInstance.find('.actionicon-delete').addClass('d-none');
					widgetInstance.find('.actionicon-duplicate').addClass('d-none');
					widgetInstance.find('.actionicon-edit').addClass('d-none');
				}
				
				cfw_dashboard_widget_save_state(widgetObject, false, true);
				
			}
			
		if(manageBundle){
			cfw_dashboard_history_completeOperationsBundle();
		}
	});
	
	
	// -----------------------------
	// Handle back button
	window.onpopstate= function() {
		CFW_DASHBOARD_URLPARAMS = CFW.http.getURLParamsDecoded();
		cfw_dashboard_applyParamsFromURLAndDraw();
	}
	
}

/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_dashboard_storeMergedParams(){
	var storedViewerParams = cfw_parameter_getStoredUserParams();
	var mergedParams = Object.assign(storedViewerParams, CFW_DASHBOARD_URLPARAMS);

	delete mergedParams['title'];
	delete mergedParams['id'];
	
	cfw_parameter_storeUserParams(mergedParams);
}
	
/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_dashboard_applyParamsFromURLAndDraw(){
	
	CFW_DASHBOARD_APPLYING_URLPARAMS = true;
	
	if(	CFW_DASHBOARD_URLPARAMS.earliest != null && CFW_DASHBOARD_URLPARAMS.latest != null){
		// -----------------------------
		// Get Earliest/Latest from URL
		cfw_dashboard_timeframe_setCustom(CFW_DASHBOARD_URLPARAMS.earliest, CFW_DASHBOARD_URLPARAMS.latest);
	}else if(CFW_DASHBOARD_URLPARAMS.timeframepreset != null){
		// -----------------------------
		// Get Preset from URL		
		cfw_dashboard_timeframe_setOffset(CFW_DASHBOARD_URLPARAMS.timeframepreset);
		// above method calls cfw_dashboard_draw(false)
	}else{

		var timeframePreset=window.localStorage.getItem("dashboard-timeframe-preset-"+CFW_DASHBOARD_URLPARAMS.id);
		if(timeframePreset != null && timeframePreset != 'null' && timeframePreset != 'custom' ){
			// ---------------------------------
			// Get last preset from local store
			cfw_dashboard_timeframe_setOffset(timeframePreset);
			// above method calls cfw_dashboard_draw(false)
		}else{
			// ---------------------------------
			// draw with default
			cfw_dashboard_timeframe_setOffset("30-m");
			
		}
	}
			
	// -----------------------------------------------
	// Merge URL Params with Custom Parameter Values
	cfw_dashboard_storeMergedParams();
	
	// ---------------------------------
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
	
	CFW_DASHBOARD_APPLYING_URLPARAMS = false;
}

/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_dashboard_initialDraw(){
	
	cfw_parameter_setScope("dashboard", CFW_DASHBOARD_URLPARAMS.id);
	
	cfw_dashboard_initialize('.grid-stack');
	
	cfw_initializeTimeframePicker(CFW_DASHBOARD_TIME_FIELD_ID, null, cfw_dashboard_timeframeChangeCallback)
	
	cfw_dashboard_applyParamsFromURLAndDraw();
	
	//-----------------------------------------------
	// Set Fullscreen
	if(JSDATA.startFullscreen){
		cfw_dashboard_toggleFullscreenMode();
	}
	
}

/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_dashboard_drawEveryWidget(data, manualLoad){
	CFW_DASHBOARD_FULLREDRAW_COUNTER++;
	
	var widgetArray = data.payload.widgets;
	cfw_parameter_setPageParams(data.payload.params);
	
	var grid = cfw_dashboard_getGrid();
	grid.removeAll();
		
	for(var i = 0;i < widgetArray.length ;i++){
		cfw_dashboard_widget_createInstance(widgetArray[i], false, manualLoad, null, CFW_DASHBOARD_FULLREDRAW_COUNTER);
	}

	// -----------------------------
	// Disable resize & move
	if(!CFW_DASHBOARD_EDIT_MODE){
		cfw_dashboard_getGrid().disable();
	}
}
/*******************************************************************************
 * Main method for building the view.
 * 
 ******************************************************************************/
function cfw_dashboard_draw(manualLoad, forceRefresh){
		

	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		// -----------------------------------
		// Clear existing Widgets
		//var grid = $('.grid-stack').data('gridstack');
		/*var grid = cfw_dashboard_getGrid();
		grid.removeAll();*/
		
		CFW.http.getJSON(CFW_DASHBOARDVIEW_URL, {action: "fetch", item: "widgetsandparams", dashboardid: CFW_DASHBOARD_URLPARAMS.id}, function(data){
			
			 CFW_DASHBOARD_FORCE_REFRESH = forceRefresh;
			 	cfw_dashboard_drawEveryWidget(data, manualLoad);
			 CFW_DASHBOARD_FORCE_REFRESH = false;
		});
		
		CFW.ui.toggleLoader(false);
	}, 100);
}

/*******************************************************************************
 * Initialize Localization has to be done before widgets are registered
 ******************************************************************************/

CFW.dashboard.registerCategory(
					  "fas fa-th-large"
					, CFW.dashboard.global.categoryDefault
					, CFWL('cfw_dashboard_category_default', 'Default')
				);
				
CFW.dashboard.registerCategory(
					  "fas fa-rocket"
					, CFW.dashboard.global.categoryAdvanced
					, CFWL('cfw_dashboard_category_advanced', 'Advanced')
				);

CFW.dashboard.registerCategory(
					  "fas fa-star"
					, CFW.dashboard.global.categoryCustom
					, CFWL('cfw_dashboard_category_custom', 'Custom')
				);
