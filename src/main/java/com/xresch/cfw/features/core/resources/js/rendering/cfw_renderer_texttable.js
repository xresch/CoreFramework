const CFW_RENDER_NAME_TEXTTABLE = 'texttable';

/******************************************************************
 *  Render as ASCII text table (no map(), only for-loops)
 ******************************************************************/
function cfw_renderer_texttable(renderDef) {
	
	//-----------------------------------
	// Render Specific settings
	var defaultSettings = {
		// Max column width in number of characters
		maxwidth: 150,
		// customizers used for customizing CSV values. Do only return text.
		texttablecustomizers: {}
	};
	
	var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.texttable);
	
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
			if(settings.texttablecustomizers[fieldname] != undefined){
				value = settings.texttablecustomizers[fieldname](currentRecord, value, CFW_RENDER_NAME_CSV);
			}

			if (value == null) {
				value = "";
			} else if (typeof value === "object") {
				value = JSON.stringify(value);
			} else {
				value = ("" + value)
					.replaceAll('\n', '\\n')
					.replaceAll('\r', '\\r')
					.replaceAll('\t', '\\t')
					.replaceAll('<', '&lt;');
			}

			rowValues.push(value);
		}

		tableData.push(rowValues);
	}

	//-----------------------------------
	// Compute column widths 
	let colWidths = [];
	for (let c = 0; c < columns.length; c++) {
		let max = 0;

		for (let r = 0; r < tableData.length; r++) {
			let cell = tableData[r][c];
			if (cell.length > max) {
				max = Math.min(cell.length, settings.maxwidth);
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
			currentVal = (currentVal.length <= settings.maxwidth) 
								? currentVal
								: currentVal.substr(0, settings.maxwidth - 5) + "[...]"
								;
								
			let padded = (isNaN(currentVal)) 
							? currentVal.padEnd(colWidths[c], " ")
							: currentVal.padStart(colWidths[c], " ") 
							;
							
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
