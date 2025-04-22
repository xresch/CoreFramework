
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/

CFW_CERTDETAILS_URL = "./certdetails";


/******************************************************************
 * 
 ******************************************************************/
function cfw_certdetails_forceACMERenewal(){
	CFW.http.getJSON(CFW_CERTDETAILS_URL, {action: "forcerenewal"});
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_certdetails_importScript(){
	
	var importHTML = 
		'<p class="bg-cfw-red"><b>IMPORTANT:</b>This import is intended for migration purposes with empty databases.' 
		+' Make sure to create a backup copy of your database files before executing an import.</p>'
		+'<p>Select a previously exported SQL script file.</p>'
		+'<div class="form-group">'
			+'<label for="importFile">Path to SQL File:</label>'
			+'<input type="text" class="form-control" name="scriptFilePath" id="scriptFilePath" />'
		+'</div>'
		+'<button class="form-control btn btn-primary" onclick="cfw_certdetails_importScriptExecute()">'+CFWL('cfw_core_import', 'Import')+'</button>';
	
	CFW.ui.showModalMedium(
			"Import Script", 
			importHTML);
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_certdetails_importScriptExecute(){
	
	var filepath = $('#scriptFilePath').val();

	CFW.http.getJSON(CFW_CERTDETAILS_URL, {action: "importscript", filepath: filepath});


}


/******************************************************************
 * 
 ******************************************************************/
function cfw_certdetails_fetchDetailsAndDisplay(){

	let parent = $("#cfw-container");

	CFW.http.getJSON(CFW_CERTDETAILS_URL, {action: "fetch", item: "certdetails"}, function(data){
		
		if(data.payload != null){
			//-----------------------------------
			// Render Data
			var timestampFormatter = function(record, value){ return CFW.format.epochToTimestamp(value); };
			
			var rendererSettings = {
					data: data.payload,
				 	idfield: null,
				 	bgstylefield: null,
				 	textstylefield: null,
				 	titlefields: ['NAME'],
				 	titleformat: '{0}',
				 	//visiblefields: [],
				 	labels: {},
				 	customizers: {
				 		"Certificate Valid From": timestampFormatter,
				 		"Certificate Valid Until": timestampFormatter,
				 	},
					rendererSettings: {
						table: {
							  narrow: false
							, filterable: true
							, verticalize: true
							}
					},
				};
			
			//---------------------------
			// Each Certificate
			for(key in data.payload){	
				
				let currentDetails = data.payload[key];
				rendererSettings.data = currentDetails;
				let renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
			
				parent.append('<h2>'+key+'</h2>');
				parent.append(renderResult);
				
			}
		}
	});
	
}




/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_certdetails_draw(){
	
	CFW.ui.toggleLoader(true);
		
	window.setTimeout( 
	function(){

		parent = $("#cfw-container");
		parent.html('');
		
		//-------------------------------------
		// Create Actions
		
		parent.append('<h1>Certificate Details</h1>');
		parent.append('<h2>Actions</h2>');
		
		var confirmMessage = "'Do you really want to force a certificate renewal?'";
		parent.append(
			'<p>'
				+ '<a href="#" class="btn btn-sm btn-primary mr-1" onclick="cfw_ui_confirmExecute('+confirmMessage+', \'May the renewal be with you!\', cfw_certdetails_forceACMERenewal);"><i class="fas fa-sync mr-2"></i>Force ACME Renewal</a>'
			+'</p>'
		);
		
		cfw_certdetails_fetchDetailsAndDisplay();
			
		CFW.ui.toggleLoader(false);
	}, 100);
}