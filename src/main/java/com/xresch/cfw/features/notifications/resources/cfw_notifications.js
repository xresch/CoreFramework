

/**************************************************************************************************************
 * This file contains various javascript examples.
 * For your convenience, this code is highly redundant for eassier copy&paste.
 * @author Reto Scheiwiller, (c) Copyright 2021
 **************************************************************************************************************/

var CFW_NOTIFICATIONS_URL = "/app/notifications";
var JSEXAMPLES_LAST_OPTIONS = null;

var CFW_NOTIFICATIONS_PARENT = $('<div id="cfw-notifications-parent">');
	
/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_notifications_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		list.append('<li class="nav-item"><a class="nav-link" id="tab-unread" data-toggle="pill" href="#" role="tab" onclick="cfw_notifications_printList(\'unread\')"><i class="fas fa-share-alt mr-2"></i>Unread</a></li>');
		list.append('<li class="nav-item"><a class="nav-link" id="tab-read" data-toggle="pill" href="#" role="tab" onclick="cfw_notifications_printList(\'read\')"><i class="fas fa-copy mr-2"></i>Read</a></li>');

		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}


/******************************************************************
 * 
 ******************************************************************/
function cfw_notifications_addStyleFields(data){
	
	if(data != null){
		for(index in data){
			current = data[index];
			
			current.TEXTSTYLE = "cfw-white"; 
			
			switch(current.MESSAGE_TYPE){
				case 'INFO': 		current.BGSTYLE = "cfw-blue"; 
									break;
				case 'SUCCESS':		current.BGSTYLE = "cfw-green"; 
									break;
				case 'WARNING':		current.BGSTYLE = "cfw-orange"; 
									break;
				case 'ERROR':		current.BGSTYLE = "cfw-red"; 
									break;
				default:			current.BGSTYLE = "cfw-none"; 
									current.TEXTSTYLE = "cfw-none";
			}
		}
	}
}
/******************************************************************
 * 
 ******************************************************************/
function cfw_notifications_showModal(){
		
	CFW.ui.showModalLarge(
		CFWL("cfw_core_notifications", "Notifications"), 
		CFW_NOTIFICATIONS_PARENT
	);
	
	cfw_notifications_markAsRead();
	cfw_notifications_removeBadge();
	cfw_notifications_printList();
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_notifications_removeBadge(){
	badge = $("#cfw-notification-badge");
	if(badge.length > 0){
		badge.remove();
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_notifications_markAsRead(){
	
	params = {action: "update", item: "markasread"};
	CFW.http.getJSON(CFW_NOTIFICATIONS_URL, params, 
		function(data) {
			/* do nothing, no spaming of the holy user. */
	});
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_notifications_updateUnreadCountAndSeverity(){
	
	params = {action: "fetch", item: "unreadcountseverity"};
	CFW.http.getJSON(CFW_NOTIFICATIONS_URL, params, 
		function(data) {
			if(data.success){
				

				//-----------------------------------
				// Clean Up
				cfw_notifications_removeBadge();
				
				let info = data.payload[0];
				if(info != null && info.COUNT > 0){
					
					let menuItem = $('.cfw-menuitem-notification > .dropdown-item') 
					
					let badge = $('<span id="cfw-notification-badge" class="badge ml-1">');
					
					if(info.COUNT < 100){
						badge.text(info.COUNT);
					}else{
						badge.text("99+");
					}

					switch(info.SEVERITY){
						case 1:		badge.addClass('bg-cfw-blue'); break;
						case 2: 	badge.addClass('bg-cfw-green'); break;
						case 3: 	badge.addClass('bg-cfw-orange'); break;
						case 4:		badge.addClass('bg-cfw-red'); break;
						default:			break;
					}
					
					menuItem.append(badge);
				}
			}else{
				/* do nothing, no spaming of the holy user. */
			}
	});
}

cfw_notifications_updateUnreadCountAndSeverity();
CFW.global.notificationPollInterval = window.setInterval(cfw_notifications_updateUnreadCountAndSeverity, 2*60*1000);

/******************************************************************
 * 
 ******************************************************************/
function cfw_notifications_delete(id){
	
	params = {action: "delete", item: "single", id: id};
	CFW.http.getJSON(CFW_NOTIFICATIONS_URL, params, 
		function(data) {
			if(data.success){
				cfw_notifications_printList();
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected notification could <b style="color: red">NOT</strong> be deleted.</span>');
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_notifications_deleteMultiple(elements, records, values){
	
	params = {action: "delete", item: "multiple", ids: values.join()};
	CFW.http.getJSON(CFW_NOTIFICATIONS_URL, params, 
		function(data) {
			if(data.success){
				cfw_notifications_printList();
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected notification(s) could <b style="color: red">NOT</strong> be deleted.</span>');
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_notifications_deleteAll(elements, records, values){
	
	params = {action: "delete", item: "all"};
	CFW.http.getJSON(CFW_NOTIFICATIONS_URL, params, 
		function(data) {
			if(data.success){
				cfw_notifications_printList();
			}else{
				CFW.ui.showModalSmall("Error!", '<span>An error occured when deleting the notifications.</span>');
			}
	});
}


/******************************************************************
 * Full example using the dataviewer renderer.
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function cfw_notifications_printList(){
	
	var parent = CFW_NOTIFICATIONS_PARENT;
	parent.html("");
	
	//======================================
	// Prepare actions
	var actionButtons = [];

	//-------------------------
	// Delete Button
	actionButtons.push(
		function (record, id){
			return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
					+'onclick="CFW.ui.confirmExecute(\'Do you want to delete <strong>\\\''+record.TITLE.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfw_notifications_delete('+id+');\')">'
					+ '<i class="fa fa-trash"></i>'
					+ '</button>';

		});
	
	//-----------------------------------
	// Render Data
	var rendererSettings = {
			data: null,
		 	idfield: 'PK_ID',
		 	bgstylefield: 'BGSTYLE',
		 	textstylefield: 'TEXTSTYLE',
		 	titlefields: ['TIMESTAMP', 'TITLE'],
		 	titleformat: '{0} - {1}',
		 	visiblefields: ['TIMESTAMP', 'MESSAGE_TYPE', 'TITLE', 'MESSAGE'],
		 	labels: {
		 		TIMESTAMP: "Time",
		 	},
		 	customizers: {
		 		TIMESTAMP: function(record, value) { 
		 			if( !CFW.utils.isNullOrEmpty(value) ){
							return CFW.format.epochToTimestamp(value);
					}else{
						return '&nbsp;';
					}
		 		}
		 	},
			actions: actionButtons,
				bulkActions: {
					"Select All Visible": function(elements, records, values){
						$('#cfw-notifications-parent .cfw-dataviewer input[type=checkbox]').prop( "checked", true );
					},
					"Delete Selected": function(elements, records, values){
						cfw_ui_confirmExecute("Are you sure you want to delete the selected notification(s)?", "Delete", function(){
							cfw_notifications_deleteMultiple(elements, records, values);
						});
					},
					"Delete All": function(elements, records, values){
						cfw_ui_confirmExecute("Are you sure you want to clear the whole list and delete all notifications?", "Delete", function(){
							cfw_notifications_deleteAll();
						});
					}
					
				},
				bulkActionsPos: "top",
			
			rendererSettings: {
				dataviewer: {
					storeid: 'cfw_notifications',
					sortable: false,
					datainterface: {
						url: CFW_NOTIFICATIONS_URL,
						item: 'notifications',
						preprocess: function(data){
							cfw_notifications_addStyleFields(data);
						}
					},
					renderers: [
						{	label: 'Panels',
							name: 'panels',
							renderdef: {
								rendererSettings: {
									panels: {narrow: true},
								},
							}
						},
						{	label: 'Table',
							name: 'table',
							renderdef: {
								rendererSettings: {
									table: {filterable: false, hover: false},
								},
							}
						},
						{	label: 'Smaller Table',
							name: 'table',
							renderdef: {
								rendererSettings: {
									table: {filterable: false, hover: false, narrow: true},
								},
							}
						},
						
						{	label: 'Cards',
							name: 'cards',
							renderdef: {}
						},
						{	label: 'Tiles',
							name: 'tiles',
							renderdef: {
								visiblefields: ['PK_ID', 'LOCATION', "EMAIL", "LIKES_TIRAMISU"],
								rendererSettings: {
									tiles: {
										popover: false,
										border: '2px solid black'
									},
								},
								
							}
						},
						{	label: 'CSV',
							name: 'csv',
							renderdef: {}
						},
						{	label: 'JSON',
							name: 'json',
							renderdef: {}
						}
					],
				},
			},
		};
	
	var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
	
	parent.append(renderResult);
	
}
