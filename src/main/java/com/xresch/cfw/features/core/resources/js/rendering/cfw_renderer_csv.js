
const CFW_RENDER_NAME_CSV = 'csv';

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_csv(renderDef) {
		
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// the delimiter for the csv
		delimiter: ',',
		// customizers used for customizing CSV values. Do only return text.
		csvcustomizers: {}
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.csv);
	
	//-----------------------------------
	// Target Element
	let pre = $('<pre ondblclick="CFW.selection.selectElementContent(this)">');
	let code = $('<code>');
	pre.append(code);
	
	//-----------------------------------
	// Headers
	let headers = '';
	for(var key in renderDef.visiblefields){
		var fieldname = renderDef.visiblefields[key];
		
		headers += '"' +renderDef.getLabel(fieldname, CFW_RENDER_NAME_CSV).replace('"', '""') + '"' + settings.delimiter;
	}
	// remove last semicolon
	headers = headers.substring(0, headers.length-1);
	code.append(headers+"\r\n");
	
	//-----------------------------------
	// Print Records
	for(let i = 0; i < renderDef.data.length; i++ ){
		let currentRecord = renderDef.data[i];
		let recordCSV = "";
		for(var key in renderDef.visiblefields){
			var fieldname = renderDef.visiblefields[key];
			
			// do not use normal customized values as it might return html
			var value = currentRecord[fieldname];
			if(settings.csvcustomizers[fieldname] != undefined){
				value = settings.csvcustomizers[fieldname](currentRecord, value, CFW_RENDER_NAME_CSV);
			}
			
			if(value == null){
				value = "";
			}else{
				if(typeof value === "object" ){
				value = JSON.stringify(value);
				}else{

					value = (""+value).replaceAll('\n', '\\n')
									 .replaceAll('\r', '\\r')
									 .replaceAll('<', '&lt;')
									 ;
				}
			}
			
			recordCSV += '"' + value.replaceAll('"', '""') + '"' + settings.delimiter;
		}
		// remove last semicolon
		recordCSV = recordCSV.substring(0, recordCSV.length-1);
		
		//=====================================
		// Create Colored span
		//let span = $('<span>');
		//span.html(recordCSV+'</span>\r\n')
		
		//=====================================
		// Add Styles
		//renderDef.colorizeElement(currentRecord, span, "bg");
		//renderDef.colorizeElement(currentRecord, span, "text");
		
		code.append(recordCSV+'\r\n');
	}

	var wrapperDiv = $('<div class="flex-grow-1">');
	wrapperDiv.append(pre);
	return wrapperDiv;
}

CFW.render.registerRenderer(CFW_RENDER_NAME_CSV,  new CFWRenderer(cfw_renderer_csv));
