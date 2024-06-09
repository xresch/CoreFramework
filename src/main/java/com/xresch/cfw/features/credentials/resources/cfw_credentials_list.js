
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/

var URI_DASHBOARD_VIEW_PUBLIC = "/public/credentials/view";
var CFW_DASHBOARDLIST_LAST_OPTIONS = null;

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_credentialslist_tutorialsRegister(){
	cfw_credentialslist_tutorialsMyCredentialss();
	cfw_credentialslist_tutorialsFavedCredentialss();
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_credentialslist_tutorialsMyCredentialss(){
		
	//===================================================
	// Check has Tab
	if($("#tab-mycredentialss").length == 0){
		return;
	}
	
	//===================================================
	// Tutorial: My Credentialss
	var DASHBOARD_NAME = 'A Nice Tutorial Credentials';
	
	cfw_tutorial_bundleStart($("#tab-mycredentialss"));
	
		//----------------------------------
		// Tab MyCredentials
		cfw_tutorial_addStep({
			  selector: "#tab-mycredentialss"
			, clickable: false
			, text: "In this tab you can create, view and manage your own credentialss."
			, drawDelay: 500
			, beforeDraw: null
		});
		
		//----------------------------------
		// Create Button
		cfw_tutorial_addStep({
			  selector: "#button-add-credentials"
			, clickable: false
			, text: "Using this button allows you to add a new credentials."
			, drawDelay: 500
			, beforeDraw: function(){
				$("#tab-mycredentialss").click();
			}
		});	

		//----------------------------------
		// Enter Name
		cfw_tutorial_addStep({
			  selector: 'input#NAME'
			, clickable: false
			, text: "We have to give the credentials a name."
			, drawDelay: 1000
			, beforeDraw: function(){
				$("#button-add-credentials").click();
				CFW.ui.waitForAppear('input#NAME').then(function(){
					
					$('#cfw-default-modal').addClass('cfw-tuts-highlight').css('z-index', "1055");
					$('input#NAME').val(DASHBOARD_NAME);
				})
			}
		});	
		
		//----------------------------------
		// Enter Description
		cfw_tutorial_addStep({
			  selector: "textarea#DESCRIPTION"
			, clickable: false
			, text: "We can enter a description for the credentials."
			, drawDelay: 500
			, beforeDraw: function(){
				$('textarea#DESCRIPTION').val("Credentials showing cute cats blocking someones keyboard.");
			}
		});	
		
		//----------------------------------
		// Enter TAGS
		cfw_tutorial_addStep({
			  selector: ".tutorial0-tagsparent"
			, clickable: false
			, text: "Lets add some tags too...."
			, drawDelay: 500
			, beforeDraw: function(){
				$('input#TAGS-tagsinput').parent().addClass('tutorial0-tagsparent');
				
				$('input#TAGS').tagsinput('add', 'cute');
				$('input#TAGS').tagsinput('add', 'cats');
				$('input#TAGS').tagsinput('add', 'keyboard');
				$('input#TAGS').tagsinput('add', 'siege');
				
			}
		});	
		
		//----------------------------------
		// Click Submit Button
		cfw_tutorial_addStep({
			  selector: "#cfwCreateCredentialsForm-submitButton"
			, clickable: false
			, text: "With a click on create the credentials will be added to your list."
			, drawDelay: 500
			, beforeDraw: null
		});	
		
		//----------------------------------
		// Close Button
		cfw_tutorial_addStep({
			  selector: "#cfw-default-modal-closebutton"
			, clickable: false
			, text: "After we created the credentials we can close the modal."
			, drawDelay: 500
			, beforeDraw: function(){
				$("#cfwCreateCredentialsForm-submitButton").click();
				
			}
		});		
		
		//----------------------------------
		// Set Display as
		cfw_tutorial_addStep({
			  selector: "select.dataviewer-displayas"
			, clickable: false
			, text: "Here we can choose how to display the records. Let me make sure it is set to table so the tutorial will work properly."
			, drawDelay: 1000
			, beforeDraw: function(){
				$("#cfw-default-modal-closebutton").click();
				$("select.dataviewer-displayas").val(0);
				
			}
		});	
		
		//----------------------------------
		// Set Filter
		cfw_tutorial_addStep({
			  selector: "input.dataviewer-filterquery"
			, clickable: false
			, text: "Now let this automated tutorial set a filter to find that credentials we just created."
			, drawDelay: 1000
			, beforeDraw: function(){
				
				window.setTimeout(function(){
					$("input.dataviewer-filterquery").val(DASHBOARD_NAME);
				}, 1000);
			}
		});	
		
		//----------------------------------
		// First Row
		cfw_tutorial_addStep({
			  selector: ".cfwRecordContainer"
			, clickable: false
			, text: "Now we have the list filtered for our credentials."
			, drawDelay: 500
			, beforeDraw: function(){
				$("input.dataviewer-filterquery").trigger('change');
				
			}
		});	
		
		//----------------------------------
		// Explain Favorite
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-0"
			, clickable: false
			, text: "Clicking the star will add the credentials to your favorites."
			, drawDelay: 500
			, beforeDraw: function(){
				
				//add IDs to cells
				$(".cfwRecordContainer:first td").each(function(index){
					$(this).addClass('tutorial0-cell-'+index);
				});
				
			}
		});	
		
		//----------------------------------
		// Explain Name
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-1"
			, clickable: false
			, text: "Clicking the title will open your credentials."
			, drawDelay: 500
			, beforeDraw: function(){					
			}
		});		
		
		//----------------------------------
		// Explain Description
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-2"
			, clickable: false
			, text: "Here is the description, as precise as ever."
			, drawDelay: 500
			, beforeDraw: function(){					
			}
		});		
		
		//----------------------------------
		// Explain Tags
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-3"
			, clickable: false
			, text: "Tags can be useful to filter and find the credentials you search for faster."
			, drawDelay: 500
			, beforeDraw: function(){					
			}
		});	
		
		//----------------------------------
		// Explain Shared
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-4"
			, clickable: false
			, text: "Here you can see if the credentials is shared with others."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});		
		
		//----------------------------------
		// Explain View Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-6"
			, clickable: false
			, text: "Clicking this button opens the credentials, same as clicking on the name of the credentials."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});		
		
		//----------------------------------
		// Explain Edit Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-7"
			, clickable: false
			, text: "The edit button opens a modal to edit the settings of the credentials."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});		
				
		//----------------------------------
		// Explain Duplicate Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-8"
			, clickable: false
			, text: "This will create a copy of the credentials."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});		
		
		//----------------------------------
		// Explain Export Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-9"
			, clickable: false
			, text: "Here you can export your credentials to store it to a file. Can be useful to create backups."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});	
				
		//----------------------------------
		// Explain Delete Button
		cfw_tutorial_addStep({
			  selector: ".tutorial0-cell-10"
			, clickable: false
			, text: "To delete a credentials use this button."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});	
		
		//----------------------------------
		// Explain Import Button
		cfw_tutorial_addStep({
			  selector: "#button-import"
			, clickable: false
			, text: "Oh... not to forget what to do with your exports. Here you can import them when needed."
			, drawDelay: 500
			, beforeDraw: function(){				
			}
		});	
			
	cfw_tutorial_bundleEnd();
	
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_credentialslist_tutorialsFavedCredentialss(){
	
	//===================================================
	// Check has Tab
	if($("#tab-favedcredentialss").length == 0){
		return;
	}
	
	//===================================================
	// Tutorial: Faved Credentialss
	cfw_tutorial_bundleStart($("#tab-favedcredentialss"));
	
		//----------------------------------
		// Favorite Tab	
		cfw_tutorial_addStep({
				  selector: "#tab-favedcredentialss"
				, clickable: false
				, text: "Here you can find all the credentialss you have marked as favorite."
				, drawDelay: 500
				, beforeDraw: function(){				
					}
			});
		
		//----------------------------------
		// Explain favorites
		
		cfw_tutorial_addStep({
			  selector: null
			, clickable: false
			, text: 'The favorites tab lists all the credentialss you have marked as favorite.'
					+'<br/>Else the functioniality is similar to the My Credentialss tab.'
					+''
			, drawDelay: 500
			, beforeDraw: function(){
				$("#tab-favedcredentialss").click();
				
			}
		});
		
					
	cfw_tutorial_bundleEnd();
}
/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_credentialslist_createTabs(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		
		var list = $('<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">');
		
		//--------------------------------
		// My Credentials Tab
		if(CFW.hasPermission('Credentials Creator') 
		|| CFW.hasPermission('Credentials Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-mycredentialss" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'mycredentialss\'})"><i class="fas fa-user-circle mr-2"></i>My Credentialss</a></li>'
			);
		}
		
		//--------------------------------
		// Shared Credentials Tab	
		list.append('<li class="nav-item"><a class="nav-link" id="tab-sharedcredentialss" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'sharedcredentialss\'})"><i class="fas fa-share-alt mr-2"></i>Shared</a></li>');
		
		
		//--------------------------------
		// Faved Credentials Tab	
		list.append('<li class="nav-item"><a class="nav-link" id="tab-favedcredentialss" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'favedcredentialss\'})"><i class="fas fa-star mr-2"></i>Favorites</a></li>');
		
		//--------------------------------
		// Archived Credentials Tab	
		if( CFW.hasPermission('Credentials Creator') ){
			list.append('<li class="nav-item"><a class="nav-link" id="tab-myarchived" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'myarchived\'})"><i class="fas fa-folder-open mr-2"></i>Archive</a></li>');
		}
		
		//--------------------------------
		// Admin Credentials Tab	
		if(CFW.hasPermission('Credentials Admin')){
			list.append(
				'<li class="nav-item"><a class="nav-link" id="tab-adminarchived" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'adminarchived\'})"><i class="fas fa-folder-open mr-2"></i>Admin Archive</a></li>'
				+'<li class="nav-item"><a class="nav-link" id="tab-admincredentialss" data-toggle="pill" href="#" role="tab" onclick="cfw_credentialslist_draw({tab: \'admincredentialss\'})"><i class="fas fa-tools mr-2"></i>Admin</a></li>'
				);
		}
		
		var parent = $("#cfw-container");
		parent.append(list);
		parent.append('<div id="tab-content"></div>');
	}

}

/******************************************************************
 * Edit
 ******************************************************************/
function cfw_credentialslist_editCredentials(id){
	
	cfw_credentialscommon_editCredentials(id, "cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
	 
}

/******************************************************************
 * Create
 ******************************************************************/
function cfw_credentialslist_createCredentials(){
	
	var html = $('<div id="cfw-credentials-createCredentials">');	

	CFW.http.getForm('cfwCreateCredentialsForm', html);
	
	CFW.ui.showModalMedium(CFWL('cfw_credentialslist_createCredentials', 
			CFWL("cfw_credentialslist_createCredentials", "Create Credentials")), 
			html, "cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_importCredentials(){
	
	var uploadHTML = 
		'<p>Select a previously exported credentials file. Share settings will be imported as well. If you exported the credentials from another application or application instance, the widgets might not be able to load correctly.</p>'
		+'<div class="form-group">'
			+'<label for="importFile">Select File to Import:</label>'
			+'<input type="file" class="form-control" name="importFile" id="importFile" />'
		+'</div>'
		+'<button class="form-control btn btn-primary" onclick="cfw_credentialslist_importCredentialsExecute()">'+CFWL('cfw_core_import', 'Import')+'</button>';
	
	CFW.ui.showModalMedium(
			"Import Credentials", 
			uploadHTML,
			"cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)");
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_importCredentialsExecute(){
	
	var file = document.getElementById('importFile').files[0];
	var reader = new FileReader();

	  // Read file into memory as UTF-8
	  reader.readAsText(file, "UTF-8");
	  
	  reader.onload = function loaded(evt) {
		  // Obtain the read file data
		  var fileString = evt.target.result;
		  
			var params = {action: "import", item: "credentialss", jsonString: fileString};
			CFW.http.postJSON(CFW_DASHBOARDLIST_URL, params, 
				function(data) {
					//do nothing
				}
			);

		}
}


/******************************************************************
 * Show Public Link
 ******************************************************************/
function cfw_credentialslist_showPublicLink(id){
	var link = CFW.http.getHostURL() + URI_DASHBOARD_VIEW_PUBLIC + "?id=" + id;
	var linkDiv = $('<div id="cfw-credentials-publiclink">');
	
	linkDiv.append('<p>Public Link: <a target="blank" href="'+link+'">'+link+'</a></p>');
	
	CFW.ui.showModalMedium("Public Link", linkDiv);
}

/******************************************************************
 * Edit Credentials
 ******************************************************************/
function cfw_credentialslist_changeCredentialsOwner(id){
	
	//-----------------------------------
	// Role Details
	//-----------------------------------
	var formDiv = $('<div id="cfw-credentials-details">');

	
	CFW.ui.showModalMedium(
			CFWL("cfw_credentialscommon_editCredentials","Edit Credentials"), 
			formDiv, 
			"cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS)"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_DASHBOARDLIST_URL, {action: "getform", item: "changeowner", id: id}, formDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_credentialslist_delete(id){
	
	var params = {action: "delete", item: "credentials", id: id};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected credentials could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_credentialslist_archive(id, isarchived){
	
	var params = {action: "update", item: "isarchived", id: id, isarchived: isarchived};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}else{
				CFW.ui.showModalSmall("Error!", '<span>The selected credentials could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_credentialslist_duplicate(id){
	
	var params = {action: "duplicate", item: "credentials", id: id};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				cfw_credentialslist_draw(CFW_DASHBOARDLIST_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printMyCredentialss(data){
	cfw_credentialslist_printCredentialss(data, 'mycredentialss');
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printFavedCredentialss(data){
	cfw_credentialslist_printCredentialss(data, 'favedcredentialss');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printSharedCredentialss(data){
	cfw_credentialslist_printCredentialss(data, 'sharedcredentialss');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printMyArchived(data){
	cfw_credentialslist_printCredentialss(data, 'myarchived');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printAdminArchived(data){
	cfw_credentialslist_printCredentialss(data, 'adminarchived');
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_credentialslist_printAdminCredentialss(data){
	cfw_credentialslist_printCredentialss(data, 'admincredentialss');
}


/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_credentialslist_printCredentialss(data, type){
	
	var parent = $("#tab-content");
	
	//--------------------------------
	// Tab Desciption

	switch(type){
		case "mycredentialss":		parent.append('<p>This tab shows all credentialss where you are the owner.</p>')
									break;	
									
		case "myarchived":			parent.append('<p>This tab shows all archived credentialss where you are the owner.</p>')
									break;	
									
		case "sharedcredentialss":	parent.append('<p>This list contains all the credentials that are shared by others and by you.</p>')
									break;
									
		case "favedcredentialss":		parent.append('<p>Here you can find all the credentialss you have faved. If you unfave a credentials here it will vanish from the list the next time the tab or page gets refreshed.</p>')
									break;	
									
		case "adminarchived":		parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin archive. The list contains all archived credentialss of all users.</b></p>')
									break;	
		case "admincredentialss":		parent.append('<p class="bg-cfw-orange p-1 text-white"><b><i class="fas fa-exclamation-triangle pl-1 pr-2"></i>This is the admin area. The list contains all credentialss of all users.</b></p>')
									break;	
														
		default:					break;
	}
	
	//--------------------------------
	//  Create Button
	if(type == 'mycredentialss'){
		var createButton = $('<button id="button-add-credentials" class="btn btn-sm btn-success m-1" onclick="cfw_credentialslist_createCredentials()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_credentialslist_createCredentials')
					   + '</button>');
	
		parent.append(createButton);
		
		var importButton = $('<button id="button-import" class="btn btn-sm btn-success m-1" onclick="cfw_credentialslist_importCredentials()">'
				+ '<i class="fas fa-upload"></i> '+ CFWL('cfw_core_import', 'Import')
		   + '</button>');

		parent.append(importButton);
		
	}
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addToastInfo("Hmm... seems there aren't any credentialss in the list.");
		}
		
		//-----------------------------------
		// Prepare Columns
		var showFields = [];
		if(type == 'mycredentialss' 
		|| type == 'myarchived'){
			showFields = ['IS_FAVED', 'NAME', 'DESCRIPTION', 'TAGS', 'IS_SHARED', 'TIME_CREATED'];
		}else if ( type == 'sharedcredentialss'
				|| type == 'favedcredentialss'){
			showFields = ['IS_FAVED', 'OWNER', 'NAME', 'DESCRIPTION', 'TAGS'];
		}else if (type == 'admincredentialss'
				||type == 'adminarchived' ){
			showFields = ['IS_FAVED', 'PK_ID', 'OWNER', 'NAME', 'DESCRIPTION', 'TAGS','IS_SHARED', 'TIME_CREATED'];
		}
		
		
		//======================================
		// Prepare actions
		
		var actionButtons = [ ];
		//-------------------------
		// Public Link Button
		actionButtons.push(
			function (record, id){ 
				var htmlString = '';
				if(record.IS_PUBLIC){
					htmlString += '<button class="btn btn-primary btn-sm" title="Show Public Link"'
						+'onclick="cfw_credentialslist_showPublicLink('+id+');">'
						+ '<i class="fa fa-link"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
		
		
		//-------------------------
		// View Button
		actionButtons.push(
			function (record, id){ 
				return '<a class="btn btn-success btn-sm" role="button" href="/app/credentials/view?id='+id+'&title='+encodeURIComponent(record.NAME)+'" alt="View" title="View" >'
				+ '<i class="fa fa-eye"></i>'
				+ '</a>';
			}
		);

		//-------------------------
		// Edit Button
		actionButtons.push(
			function (record, id){ 
				var htmlString = '';
				if(JSDATA.userid == record.FK_ID_USER 
				|| type == 'admincredentialss'
				|| (record.IS_EDITOR && record.ALLOW_EDIT_SETTINGS) ){
					htmlString += '<button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
						+'onclick="cfw_credentialslist_editCredentials('+id+')");">'
						+ '<i class="fa fa-pen"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
		
		
		//-------------------------
		// Change Owner Button
		if(type == 'mycredentialss'
		|| type == 'admincredentialss'){

					actionButtons.push(
						function (record, id){
							var htmlString = '<button class="btn btn-primary btn-sm" alt="Change Owner" title="Change Owner" '
									+'onclick="cfw_credentialslist_changeCredentialsOwner('+id+');">'
									+ '<i class="fas fa-user-edit"></i>'
									+ '</button>';
							
							return htmlString;
						});
				}
		
		//-------------------------
		// Duplicate Button
		if( (type != 'myarchived' && type != 'adminarchived' )
			&& (
			   CFW.hasPermission('Credentials Creator')
			|| CFW.hasPermission('Credentials Admin')
			)
		){
			actionButtons.push(
				function (record, id){
					
					// IMPORTANT: Do only allow duplicate if the user can edit the credentials,
					// else this would create a security issue.
					var htmlString = '';
					if(JSDATA.userid == record.FK_ID_USER 
					|| CFW.hasPermission('Credentials Admin')
					|| (record.IS_EDITOR && record.ALLOW_EDIT_SETTINGS) ){
						htmlString = '<button class="btn btn-warning btn-sm text-white" alt="Duplicate" title="Duplicate" '
							+'onclick="CFW.ui.confirmExecute(\'This will create a duplicate of <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong> and add it to your credentialss.\', \'Do it!\', \'cfw_credentialslist_duplicate('+id+');\')">'
							+ '<i class="fas fa-clone"></i>'
							+ '</button>';
					}else{
						htmlString += '&nbsp;';
					}
					
					return htmlString;
				});
		}
		
		//-------------------------
		// Export Button
		if(type == 'mycredentialss'
		|| type == 'admincredentialss'
		|| type == 'favedcredentialss'){

			actionButtons.push(
				function (record, id){
					if(JSDATA.userid == record.FK_ID_USER 
					|| type == 'admincredentialss'){
						return '<a class="btn btn-warning btn-sm text-white" target="_blank" alt="Export" title="Export" '
							+' href="'+CFW_DASHBOARDLIST_URL+'?action=fetch&item=export&id='+id+'" download="'+record.NAME.replaceAll(' ', '_')+'_export.json">'
							+'<i class="fa fa-download"></i>'
							+ '</a>';
					}else{
						return '&nbsp;';
					}
					
				});
		}

		//-------------------------
		// Archive / Restore Button
		actionButtons.push(
			function (record, id){
				var htmlString = '';
				if(JSDATA.userid == record.FK_ID_USER 
				|| type == 'adminarchived'
				|| type == 'admincredentialss'
				){
					let isArchived = record.IS_ARCHIVED;
					let confirmMessage = "Do you want to archive the credentials";
					let icon = "fa-folder-open";
					let color =  "btn-danger";
					
					if(isArchived){
						confirmMessage = "Do you want to restore the credentials";
						icon = "fa-trash-restore" ;
						color = "btn-success";
					}
					
					htmlString += '<button class="btn '+color+' btn-sm" alt="Archive" title="Archive" '
						+'onclick="CFW.ui.confirmExecute(\''+confirmMessage+' <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Do it!\', \'cfw_credentialslist_archive('+id+', '+!isArchived+');\')">'
						+ '<i class="fa '+icon+'"></i>'
						+ '</button>';
				}else{
					htmlString += '&nbsp;';
				}
				return htmlString;
			});
	

		//-------------------------
		// Delete Button
		if(type == 'myarchived'
		|| type == 'adminarchived'){
			actionButtons.push(
				function (record, id){
					return '<button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
							+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the credentials <strong>\\\''+record.NAME.replace(/\"/g,'&quot;')+'\\\'</strong>?\', \'Delete\', \'cfw_credentialslist_delete('+id+');\')">'
							+ '<i class="fa fa-trash"></i>'
							+ '</button>';

				});
		}

		
		//-------------------------
		// Sharing Details View
		sharingDetailsView = null;
		
		if(type == 'mycredentialss'
		|| type == 'admincredentialss'){
			sharingDetailsView = 
				{ 
					label: 'Sharing Details',
					name: 'table',
					renderdef: {
						visiblefields: ["IS_FAVED", "NAME", "IS_SHARED", "JSON_SHARE_WITH_USERS", "JSON_SHARE_WITH_GROUPS", "JSON_EDITORS", "JSON_EDITOR_GROUPS"],
						labels: {
					 		PK_ID: "ID",
					 		IS_SHARED: 'Shared',
					 		JSON_SHARE_WITH_USERS: 'Shared User', 
						 	JSON_SHARE_WITH_GROUPS: 'Shared Groups', 
						 	JSON_EDITORS: 'Editors', 
						 	JSON_EDITOR_GROUPS: 'Editor Groups'
					 	},
						rendererSettings: {
							table: {filterable: false},
						},
					}
				};
			
			if(type == 'admincredentialss'){
				sharingDetailsView.renderdef.visiblefields.unshift("OWNER");
			}
		}

		//-----------------------------------
		// Render Data
		
		var badgeCustomizerFunction = function(record, value) { 
 			var badgesHTML = '<div class="maxvw-25">';
 			
 			for(id in value){
 				badgesHTML += '<span class="badge badge-primary m-1">'+value[id]+'</span>';
 			}
 			badgesHTML += '</div>';
 			
 			return badgesHTML;
 			 
 		};
 		
		var storeID = 'credentialss-'+type;
		
		var rendererSettings = {
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titleformat: null,
			 	visiblefields: showFields,
			 	labels: {
			 		IS_FAVED: "Favorite",
			 		PK_ID: "ID",
			 		IS_SHARED: 'Shared'
			 	},
			 	customizers: {
					IS_FAVED: function(record, value) { 
						
						var isFaved = value;
						//Toggle Button
						var params = {action: "update", item: 'favorite', listitemid: record.PK_ID};
						var cfwToggleButton = CFW.ui.createToggleButton(CFW_DASHBOARDLIST_URL, params, isFaved, "fave");
						
						return cfwToggleButton.getButton();
			 		},
						
			 		NAME: function(record, value, rendererName) { 
			 			
			 			if(rendererName == 'table'){
								return '<a href="/app/credentials/view?id='+record.PK_ID+'" style="color: inherit;">'+record.NAME+'</a>';
						}else{
							return value;
						} 
			 		},
					IS_SHARED: function(record, value) { 
			 			var isShared = value;
			 			if(isShared){
								return '<span class="badge badge-success m-1">true</span>';
						}else{
							return '<span class="badge badge-danger m-1">false</span>';
						} 
			 		},
			 		TAGS: badgeCustomizerFunction,
			 		TIME_CREATED: function(record, value) { 
			 			if(value == null) return "&nbsp;";
			 			if(isNaN(value)){
			 				return value; 
			 			}else{
			 				return CFW.format.epochToTimestamp(parseInt(value)); 
			 			}
			 			
			 		},
			 		JSON_SHARE_WITH_USERS: badgeCustomizerFunction, 
			 		JSON_SHARE_WITH_GROUPS: badgeCustomizerFunction, 
			 		JSON_EDITORS: badgeCustomizerFunction, 
			 		JSON_EDITOR_GROUPS: badgeCustomizerFunction
			 	},
				actions: actionButtons,
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
				data: data.payload,
				rendererSettings: {
					dataviewer:{
						storeid: 'credentialss-'+type,
						renderers: [
							{	label: 'Table',
								name: 'table',
								renderdef: {
									labels: {
		 								IS_FAVED: "&nbsp;",
										PK_ID: "ID",
			 							IS_SHARED: 'Shared'
									},
									rendererSettings: {
										table: {filterable: false, narrow: true},
										
									},
								}
							},
							{	label: 'Bigger Table',
								name: 'table',
								renderdef: {
									labels: {
		 								IS_FAVED: "&nbsp;",
										PK_ID: "ID",
			 							IS_SHARED: 'Shared'
									},
									rendererSettings: {
										table: {filterable: false},
									},
								}
							},
							sharingDetailsView,
							{	label: 'Panels',
								name: 'panels',
								renderdef: {}
							},
							{	label: 'Cards',
								name: 'cards',
								renderdef: {}
							},
							{	label: 'Tiles',
								name: 'tiles',
								renderdef: {
									visiblefields: showFields,
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
								renderdef: {
									visiblefields: null
								}
							},
							{	label: 'XML',
								name: 'xml',
								renderdef: {
									visiblefields: null
								}
							},
							{	label: 'JSON',
								name: 'json',
								renderdef: {}
							}
						],
					},
					table: {filterable: false}
				},
			};
				
		
		var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
		
		parent.append(renderResult);
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no credentialss can be displayed.');
	}
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'mycredentialss|sharedcredentialss|admincredentialss', 
 *  }
 * @return 
 ******************************************************************/

function cfw_credentialslist_initialDraw(){
	
	//-------------------------------------------
	// Increase Width
	$('#cfw-container').css('max-width', '100%');
	
	//-------------------------------------------
	// Create Tabs
	cfw_credentialslist_createTabs();
	
	//-------------------------------------------
	// Register Tutorials
	cfw_credentialslist_tutorialsRegister()
	

	
	var tabToDisplay = CFW.cache.retrieveValueForPage("credentialslist-lasttab", "mycredentialss");
	
	if(CFW.hasPermission('Credentials Viewer') 
	&& !CFW.hasPermission('Credentials Creator') 
	&& !CFW.hasPermission('Credentials Admin')){
		tabToDisplay = "sharedcredentialss";
	}
	
	$('#tab-'+tabToDisplay).addClass('active');
	
	//-------------------------------------------
	// Draw Tab
	cfw_credentialslist_draw({tab: tabToDisplay});
	
}

function cfw_credentialslist_draw(options){
	CFW_DASHBOARDLIST_LAST_OPTIONS = options;
	
	CFW.cache.storeValueForPage("credentialslist-lasttab", options.tab);
	$("#tab-content").html("");
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "mycredentialss":		CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "mycredentialss"}, cfw_credentialslist_printMyCredentialss);
										break;	
			case "favedcredentialss":		CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "favedcredentialss"}, cfw_credentialslist_printFavedCredentialss);
										break;	
			case "sharedcredentialss":	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "sharedcredentialss"}, cfw_credentialslist_printSharedCredentialss);
										break;
			case "myarchived":			CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "myarchived"}, cfw_credentialslist_printMyArchived);
										break;	
			case "adminarchived":		CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "adminarchived"}, cfw_credentialslist_printAdminArchived);
										break;	
			case "admincredentialss":		CFW.http.getJSON(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "admincredentialss"}, cfw_credentialslist_printAdminCredentialss);
										break;						
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toggleLoader(false);
	}, 50);
}
