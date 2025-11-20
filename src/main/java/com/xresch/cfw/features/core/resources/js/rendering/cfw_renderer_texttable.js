const CFW_RENDER_NAME_TEXTTABLE = 'texttable';

/******************************************************************
 *  Render as ASCII text table (no map(), only for-loops)
 ******************************************************************/
function cfw_renderer_texttable(renderDef) {
	
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
	// Prepare columns and raw data
	let columns = renderDef.visiblefields;
	let rows = renderDef.data;

	//-----------------------------------
	// Convert data into plain-text first
	let tableData = [];
	
	//-----------------------------------
	// Headers
	let headers = [];
	for (let i = 0; i < columns.length; i++) {
		let col = columns[i];
		headers.push(renderDef.getLabel(col, CFW_RENDER_NAME_TEXTTABLE));
	}
	
	tableData.push(headers);

	//-----------------------------------
	// Data Rows
	for (let i = 0; i < rows.length; i++) {
		let currentRecord = rows[i];
		let rowValues = [];

		for (let k = 0; k < columns.length; k++) {
			let fieldname = columns[k];

			// customizers (same as your CSV renderer)
			var value = currentRecord[fieldname];
			if(settings.csvcustomizers[fieldname] != undefined){
				value = settings.csvcustomizers[fieldname](currentRecord, value, CFW_RENDER_NAME_CSV);
			}

			if (value == null) {
				value = "";
			} else if (typeof value === "object") {
				value = JSON.stringify(value);
			} else {
				value = ("" + value)
					.replaceAll('\n', '\\n')
					.replaceAll('\r', '\\r')
					.replaceAll('<', '&lt;');
			}

			rowValues.push(value);
		}

		tableData.push(rowValues);
	}

	//-----------------------------------
	// Compute column widths using for loops
	let colWidths = [];
	for (let c = 0; c < columns.length; c++) {
		let max = 0;

		for (let r = 0; r < tableData.length; r++) {
			let cell = tableData[r][c];
			if (cell.length > max) {
				max = cell.length;
			}
		}

		colWidths.push(max);
	}

	//-----------------------------------
	// Vertical Line
	let verticalLine = "+";
	for (let i = 0; i < colWidths.length; i++) {
		verticalLine += "-".repeat(colWidths[i] + 2) + "+";
	}
	verticalLine += "\r\n";
	
	//-----------------------------------
	// Build ASCII lines
	function makeRow(values) {
		let result = "|";
		
		for (let c = 0; c < values.length; c++) {
			let currentVal = values[c];
			let padded = (isNaN(currentVal)) ? 
							values[c].padEnd(colWidths[c], " ")
							: values[c].padStart(colWidths[c], " ") ;
			result += " " + padded + " |";
		}
		return result + "\r\n";
	}

	//-----------------------------------
	// Build Text Table
	let output = "";

	output += verticalLine;
	output += makeRow(tableData[0]);    // header
	output += verticalLine.replaceAll("\+","|");

	for (let r = 1; r < tableData.length; r++) {
		output += makeRow(tableData[r]);
	}

	output += verticalLine;

	//-----------------------------------
	code.text(output);

	let wrapperDiv = $('<div class="flex-grow-1">');
	wrapperDiv.append(pre);
	return wrapperDiv;
}

CFW.render.registerRenderer(CFW_RENDER_NAME_TEXTTABLE, new CFWRenderer(cfw_renderer_texttable));
