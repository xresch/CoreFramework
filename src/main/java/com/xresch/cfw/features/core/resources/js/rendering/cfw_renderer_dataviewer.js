
const CFW_RENDER_NAME_DATAVIEWER = 'dataviewer';

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer(renderDef) {
	
	//========================================
	// Render Specific settings
	var defaultSettings = {
			// The available renderers which can be chosen with the Display As option
			renderers: [
				{	
					label: 'Table', // Label to display on the UI
					name: 'table',  // the name of the renderer
					// Override the default renderer settings
					renderdef: {
						rendererSettings: {
							table: {filterable: false},
						},
					}
				}
			],
			// The index of the initial renderer. Default is -1, takes first one in the list or last selected by user if storeid is specified. 
			rendererIndex: -1,
			// Defines how the menu should be rendered
			menu: 'default', // either 'default' | 'button' | 'none' | true | false 
			// Toggle if the menu should include a download button for the data
			download: false, 
			// Toggle if the menu should include a store button for the data
			store: false, 
			// Defines how the pagination should be rendered
			pagination: 'both', // either 'both' | 'top' | 'botton' | 'none' | true | false 
			// The initial page to be drawn.
			initialpage: 1,
			// The number of items options for the page size selector. 
			sizes: [10, 25, 50, 100, 200, 500, 1000, 10000],
			// The size selected by default
			defaultsize: 50,
			// if a store ID is provided, the settings will be saved and restored when refreshing the viewer or the page.
			storeid: null,
			// enable sorting. 
			sortable: true,
			// array of the fields available for sorting, if null or empty array, renderDefinition.visiblefields will be used
			sortoptions: null,
			//a function to call after a page has been rendered
			postprocess: null,
			// the interface to fetch the data from
			datainterface: {
				//The url to fetch the data from. If null the data from rendererSettings.data will be used.
				url:  null,
				//The item which should be fetched from the server.
				item:  'default',
				//The param name for the item the action should be applied to
				itemparam:  'item',
				//The param name for the action executed by the dataviewer
				actionparam:  'action',
				//The  param name for the maximum size of the result
				sizeparam:  'pagesize',
				//The param name for the page to fetch
				pageparam: 'pagenumber',
				//The param name for the field used to sort the results
				sortbyparam: 'sortby',
				//The param name for the sort direction (will be either 'asc' or 'desc')
				sortascendingparam: 'isascending',
				//The filter string used for filtering the results
				filterqueryparam: 'filterquery',
				//custom parameters which should be added to the request, e.g. {"paramname": "value", "param2": "anothervalue", ...}
				customparams: {},
				//The name of the field containing the total number of rows
				totalrowsfield: 'TOTAL_RECORDS',
				//a function to pre-process the data: function(data), data will be an array of records
				preprocess: null,
			},
	};
	
	//-----------------------------------
	// Merge settings
	var settings = _.merge({}, defaultSettings, renderDef.rendererSettings.dataviewer);	
	
	// _.merge() merges arrays, we want to override the defaults 
	if( renderDef.rendererSettings.dataviewer != null
	 && Array.isArray(renderDef.rendererSettings.dataviewer.sizes) ){
		settings.sizes = renderDef.rendererSettings.dataviewer.sizes;
	}
	
	//-----------------------------------
	// Create DataviewerDiv
	let dataviewerID = "dataviewer-"+CFW.utils.randomString(12);
	let dataviewerDiv = $('<div class="cfw-dataviewer" id="'+dataviewerID+'">');
	
	dataviewerDiv.data('renderDef', renderDef);
	dataviewerDiv.data('settings', settings);
	
	//-----------------------------------
	// Render without Dataviewer if menu=none
	if(settings.menu == 'none' || settings.menu == false){
		var params = cfw_renderer_dataviewer_createParams(dataviewerDiv, settings.initialpage);
		rendererName = params.rendererName.trim().toLowerCase();
		
		let renderResult = CFW.render.getRenderer(rendererName).render(params.finalRenderDef)
		// following makes too much troubles, don't do it
		// let renderWrapper = $('<div class="cfw-dataviewer-renderresult d-flex flex-grow-1 h-100 w-100">');
		// renderWrapper.append(renderResult);
		
		return renderResult;	
	}
	
	//-----------------------------------
	// Render Dataviewer	
	
	dataviewerDiv.append(cfw_renderer_dataviewer_createMenuHTML(dataviewerID, renderDef, settings, settings.rendererIndex) );
	
	dataviewerDiv.append('<div class="cfw-dataviewer-content  flex-column flex-grow-1">');
	
	cfw_renderer_dataviewer_fireChange(dataviewerDiv, settings.initialpage);
		
	return dataviewerDiv;
	
}

CFW.render.registerRenderer("dataviewer", new CFWRenderer(cfw_renderer_dataviewer) );

/******************************************************************
 * 
 * @param dataviewerIDOrJQuery cssSelector string like '#dataviewer-6a5b39ai' or a JQuery object
 * @param pageToRender page number
 ******************************************************************/
function cfw_renderer_dataviewer_createParams(dataviewerIDOrJQuery, pageToRender) {
	
	//=====================================================
	// Initialize
	var dataviewerDiv = $(dataviewerIDOrJQuery);
	var settingsDiv = dataviewerDiv.find('.cfw-dataviewer-settings');
	var targetDiv = dataviewerDiv.find('.cfw-dataviewer-content');
	var dataviewerID = "#"+dataviewerDiv.attr('id');
	var renderDef = dataviewerDiv.data('renderDef');
	// settings fir dataviewer
	var settings = dataviewerDiv.data('settings');
	
	// create params object to have everything together and can be passed to other functions
	var params = {
		dataviewerDiv: dataviewerDiv
		,settingsDiv: settingsDiv
		,targetDiv: targetDiv
		,dataviewerID: dataviewerID
		,renderDef: renderDef
		,settings: settings
	}
	
	//=====================================================
	// Get finalRenderDef and apply adjustments to dataviewer
	// settings
	cfw_renderer_dataviewer_resolveSelectedRendererDetails(params);
	
	if(params.finalRenderDef.rendererSettings != null
	&& params.finalRenderDef.rendererSettings.dataviewer != null){
		settings = _.merge({}, params.settings, params.finalRenderDef.rendererSettings.dataviewer);
		params.settings = settings;
	}
	
	if(settings.sortable){
		settingsDiv.find('select[name="sortby"]').parent().removeClass("d-none");
	}else{
		settingsDiv.find('select[name="sortby"]').parent().addClass("d-none");
	}
		
	//=====================================================
	// Handle Page to Render
	if(pageToRender == null || pageToRender == undefined){
		pageToRender = dataviewerDiv.data('currentpage');
		if(pageToRender == null || pageToRender == undefined){
			pageToRender = 1;
		}
	}
	params.pageToRender = pageToRender;
	
	dataviewerDiv.data('currentpage', pageToRender);

	//=====================================================
	// Get Settings
	var pageSize = settingsDiv.find('select[name="pagesize"]').val();
	var filterquery = settingsDiv.find('input[name="filterquery"]').val();
	var rendererIndex = settingsDiv.find('select[name="displayas"]').val();
	var sortbyFields = settingsDiv.find('select[name="sortby"]').val();
	var sortbyDirection = settingsDiv.find('select[name="sortby"]').find('option:selected').data('direction');
	
	if(pageSize != null){
		pageSize = parseInt(pageSize);
	}
	var offset = (pageSize > 0 ) ? pageSize * (pageToRender-1): 0;
		
	if(settings.storeid != null){
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][pageSize]', pageSize);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][filterquery]', filterquery);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][rendererIndex]', rendererIndex);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][sortbyFields]', sortbyFields);
		CFW.cache.storeValueForPage('dataviewer['+settings.storeid+'][sortbyDirection]', sortbyDirection);
	}
	
	// Add to params
	params.pageSize = pageSize;
	params.filterquery = filterquery;
	params.rendererIndex = rendererIndex;
	params.sortbyFields = sortbyFields;
	params.sortbyDirection = sortbyDirection;
	params.offset = offset;
	
	return params;
}

/******************************************************************
 * 
 * @param dataviewerIDOrJQuery cssSelector string like '#dataviewer-6a5b39ai' or a JQuery object
 * @param pageToRender page number
 ******************************************************************/
function cfw_renderer_dataviewer_fireChange(dataviewerIDOrJQuery, pageToRender) {
	
	//=====================================================
	// Initialize
	var params = cfw_renderer_dataviewer_createParams(dataviewerIDOrJQuery, pageToRender);
	
	//var dataviewerDiv = params.dataviewerDiv ;
	//var targetDiv = params.targetDiv ;
	//var dataviewerID = params.dataviewerID ;
	var settingsDiv = params.settingsDiv ;
	var renderDef = params.renderDef ;
	var settings = params.settings ;
	var filterquery = params.filterquery ;
	var sortbyFields = params.sortbyFields ;
	var sortbyDirection = params.sortbyDirection ;
	var offset = params.offset ;
	var pageSize = params.pageSize;
	
	//=====================================================
	// Handle Filter Highlight
	if(CFW.utils.isNullOrEmpty(filterquery)){
		settingsDiv.find('input[name="filterquery"]').removeClass('bg-cfw-yellow');
		settingsDiv.find('input[name="filterquery"]')
					.closest('.dropleft')
					.find('.btn-primary')
					.removeClass('bg-cfw-yellow');
	}else{
		settingsDiv.find('input[name="filterquery"]').addClass('bg-cfw-yellow');
		settingsDiv.find('input[name="filterquery"]')
					.closest('.dropleft')
					.find('.btn-primary')
		.addClass('bg-cfw-yellow');
	}
	
	//=====================================================
	// Create sorting function
	
	//default to "asc" if undefined
	let sortDirectionArray = [];
	let sortFunctionArray = [];
	if(sortbyFields != null 
	&& sortbyFields.trim().startsWith("[")
	){
		// handle inputs like [ ['FieldA', 'FieldB'] , ['asc', 'asc'] ]
		try{

			let sortbyFieldsArray = JSON.parse(sortbyFields);
			sortDirectionArray = (sortbyDirection == null) ? ['asc'] : sortbyDirection;
			
			if(sortbyFieldsArray.length > 0){	
				for(let i in sortbyFieldsArray[0]){
					let fieldname = sortbyFieldsArray[i];
					sortFunctionArray.push(
						record => {
							
							if (typeof record[fieldname] === 'string'){
								// make lowercase to have proper string sorting
								return record[fieldname].toLowerCase();
							}
							
							return record[fieldname];
							
						}
					);
				}
			}
			
			
		}catch(e){
			console.log(e);
		}

		
		
	}else{
		sortDirectionArray = (sortbyDirection == null) ? ['asc'] : [sortbyDirection];
		sortFunctionArray.push(
			record => {
				
				if (typeof record[sortbyFields] === 'string'){
					// make lowercase to have proper string sorting
					return record[sortbyFields].toLowerCase();
				}
				
				return record[sortbyFields];
				
			}
		);
	}

	
	//=====================================================
	// Get Render Results
	if(settings.datainterface.url == null){

		//-------------------------------------
		// Static 
		if(CFW.utils.isNullOrEmpty(filterquery)){
			
			//---------------------------------
			// Sort
			let sortedData = renderDef.data;
			if(settings.sortable && sortbyFields != null){
				sortedData = _.orderBy(sortedData, sortFunctionArray, sortDirectionArray);
			}
			
			//---------------------------------
			// Pagination
			let totalRecords = sortedData.length;
			let dataToRender = _.slice(sortedData, offset, offset+pageSize);
			if(pageSize == -1
			|| settings.pagination == 'none'
			|| settings.pagination == false ){
				// reset to unpaginated 
				dataToRender = sortedData;
			}
			params.totalRecords = totalRecords;
			params.dataToRender = dataToRender;
			cfw_renderer_dataviewer_renderPage(params);
		}else{
			
			//---------------------------------
			// Filter
			filterquery = filterquery.toLowerCase();
			
			let regex = null;
			if(filterquery.indexOf("*") > -1){
				regex =  new RegExp(filterquery.replace(/\*/g, '.*'));
			}

			let filteredData = _.filter(renderDef.data, function(o) { 
				let jsonString = JSON.stringify(o).toLowerCase();
				
				return ( 
				  (regex == null && jsonString.indexOf(filterquery) > -1)
				  || (regex != null && regex.test(jsonString) )
			  );

			});
			
			//---------------------------------
			// Sort
			let sortedData = filteredData;
			if(settings.sortable && sortbyFields != null){
				sortedData = _.orderBy(sortedData, sortFunctionArray, sortDirectionArray);
			}
			
			//---------------------------------
			// Pagination
			let totalRecords = sortedData.length;
			let dataToRender = _.slice(sortedData, offset, offset+pageSize);
			if(pageSize == -1
			|| settings.pagination == 'none'
			|| settings.pagination == false ){
				dataToRender = sortedData;
			}	
			params.totalRecords = totalRecords;
			params.dataToRender = dataToRender;
			cfw_renderer_dataviewer_renderPage(params);
		}
	}else{
		
		//-------------------------------------
		// Dynamic
		let httpParams = {};
		httpParams[settings.datainterface.actionparam] = "fetchpartial";
		httpParams[settings.datainterface.sizeparam] = pageSize;
		httpParams[settings.datainterface.pageparam] = pageToRender;
		httpParams[settings.datainterface.filterqueryparam] = filterquery;
		httpParams[settings.datainterface.itemparam] = settings.datainterface.item;
		httpParams[settings.datainterface.sortbyparam] = sortbyFields;
		httpParams[settings.datainterface.sortascendingparam] = (sortbyDirection == 'desc') ? false : true;

		for(key in settings.datainterface.customparams){
			httpParams[key] = settings.datainterface.customparams[key];
		}
		
		CFW.http.getJSON(settings.datainterface.url, httpParams, function(data){
			
			if(data.payload != null){
				let dataToRender = data.payload;
				let totalRecords = (dataToRender.length > 0) ? dataToRender[0][settings.datainterface.totalrowsfield] : 0;
				
				//----------------------------------
				// Call preprocess function
				if(settings.datainterface.preprocess != null){
					settings.datainterface.preprocess(dataToRender);
				}
				params.totalRecords = totalRecords;
				params.dataToRender = dataToRender;
				cfw_renderer_dataviewer_renderPage(params);
				
				
			}
		}
	);
	}
	
	//----------------------------------
	// Callback
	if(settings.postprocess != null){
		settings.postprocess($(dataviewerIDOrJQuery));
	}
			
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_renderPage(params) {
	
	params.dataviewerDiv.data('visibledata', params.dataToRender);
	//-------------------------------------
	// Initialize
	//var dataviewerDiv = $(dataviewerID);
	var settingsDiv = params.settingsDiv;
	var targetDiv = params.targetDiv;
	var dataviewerID = params.dataviewerID;
	var renderDef = params.renderDef;
	var dataviewerSettings = params.settings;
	var pageToRender = params.pageToRender;
	var pageSize = params.pageSize;
	var dataToRender = params.dataToRender;
	var totalRecords = params.totalRecords;
	
	//-------------------------------------
	// Get Settings
	//var totalRecords = dataToRender.length;
	//var pageSize = settingsDiv.find('select[name="pagesize"]').val();
	
	var offset = pageSize * (pageToRender-1);
		
	//-------------------------------------
	// Call Renderer
	params.finalRenderDef.data = dataToRender;
	var renderResult = CFW.render.getRenderer(params.rendererName).render(params.finalRenderDef);
	var renderWrapper = $('<div class="cfw-dataviewer-renderresult d-flex flex-grow-1 w-100">');
	renderWrapper.append(renderResult);
	
	//-------------------------------------
	// Create Paginator
	var pageNavigation = cfw_renderer_dataviewer_createNavigationHTML(dataviewerID, totalRecords, pageSize, pageToRender);
	

	targetDiv.html('');
	
	if(dataviewerSettings.pagination == "both" 
	|| dataviewerSettings.pagination == "top"
	|| dataviewerSettings.pagination == true){
		targetDiv.append(pageNavigation);
	}
	targetDiv.append(renderWrapper);
	
	if(dataviewerSettings.pagination == "both" 
	|| dataviewerSettings.pagination == "bottom"
	|| dataviewerSettings.pagination == true){
		targetDiv.append(pageNavigation);
	}
}

/******************************************************************
 * Evaluates what the selected renderer is and adds the fields
 * "rendererName" and "finalRenderDef"to the params object passed 
 * to this function.
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_resolveSelectedRendererDetails(params) {
	
	let dataviewerSettings = params.settings;
	let settingsDiv = params.settingsDiv;
	let renderDef = params.renderDef;
	
	//-------------------------------------
	// Prepare Renderer
	let renderDefOverrides = dataviewerSettings.renderers[0].renderdef;
	let rendererName = dataviewerSettings.renderers[0].name;
	if(dataviewerSettings.renderers.length > 1){
		var rendererIndex = settingsDiv.find('select[name="displayas"]').val();
		// in case of no menu
		if(rendererIndex == null){ rendererIndex = dataviewerSettings.rendererIndex; }
		renderDefOverrides = dataviewerSettings.renderers[rendererIndex].renderdef;
		rendererName = dataviewerSettings.renderers[rendererIndex].name;
	}

	let finalRenderDef = _.merge({}, renderDef, renderDefOverrides);
	
	params.rendererName =  rendererName;
	params.finalRenderDef =	finalRenderDef;
		
}
	
/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createMenuHTML(dataviewerID, renderDef, dataviewerSettings, initialRendererIndex) {
	
	//--------------------------------------
	// Initialize Variables
	var onchangeAttribute = ' onchange="cfw_renderer_dataviewer_fireChange(\'#'+dataviewerID+'\', 1)" ';
	var html = '<div class="cfw-dataviewer-settings">';
	
	//--------------------------------------
	// Prepare Settings
	var selectedRendererIndex = 0;
	var selectedSize = dataviewerSettings.defaultsize;
	var filterquery = '';
	
	if(dataviewerSettings.storeid != null){
		selectedRendererIndex 	= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][rendererIndex]', selectedRendererIndex);
		selectedSize 			= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][pageSize]', selectedSize);
		filterquery 			= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][filterquery]', filterquery);
	}
	
	if(initialRendererIndex != null && initialRendererIndex > -1){
		selectedRendererIndex = initialRendererIndex;
	}
	
	//--------------------------------------
	// Display As

	if(dataviewerSettings.store){
		
		html += 
			'<div class="float-right ml-2">'
				+'<label for="storeButton">&nbsp;</label>'
				+'<div name="storeButton">'
					+cfw_renderer_dataviewer_createStoreButtonHTML(dataviewerID)
				+'</div>'
			+'</div>'
			;
	}
	//--------------------------------------
	// Display As
	if(dataviewerSettings.download){
		
		html += 
			'<div class="float-right ml-2">'
				+'<label for="downloadButton">&nbsp;</label>'
				+'<div name="downloadButton">'
					+cfw_renderer_dataviewer_createDownloadButtonHTML(dataviewerID)
				+'</div>'
			+'</div>'
			;
	}
	
	//--------------------------------------
	// Display As
	if(dataviewerSettings.renderers.length > 1){
		
		html += '<div class="float-right ml-2">'
			+'	<label for="displayas">Display As:&nbsp;</label>'
			+'	<select name="displayas" class="dataviewer-displayas form-control form-control-sm" '+onchangeAttribute+'>'
		
			for(index in dataviewerSettings.renderers){
				var renderer = dataviewerSettings.renderers[index];
				var selected = (index == selectedRendererIndex) ? 'selected' : '';
				
				if(renderer != null){
					html += '<option value="'+index+'" '+selected+'>'+renderer.label+'</option>';
				}
			}
		
		html += '	</select>'
				+'</div>';
	}
	
	//--------------------------------------
	// Sort By
	if(dataviewerSettings.sortable){
		html += cfw_renderer_dataviewer_createSortSelectHTML(dataviewerSettings, renderDef, onchangeAttribute)
	}
	
	//--------------------------------------
	// Page Size
	if(dataviewerSettings.pagination != "none" 
	&& dataviewerSettings.pagination != false){

		html += '<div class="float-right ml-2">'
			+'	<label for="pagesize">Page Size:&nbsp;</label>'
			+'	<select name="pagesize" class="dataviewer-pagesize form-control form-control-sm" '+onchangeAttribute+'>'
		
			for(key in dataviewerSettings.sizes){
				var size = dataviewerSettings.sizes[key];
				var selected = (size == selectedSize) ? 'selected' : '';
				
				html += '<option value="'+size+'" '+selected+'>'+size+'</option>';
	
			}
		
		html += '	</select>'
				+'</div>';
	}
	//--------------------------------------
	// Filter Query
	let filterHighlightClass = CFW.utils.isNullOrEmpty(filterquery) ? '' : 'bg-cfw-yellow';

	html += '<div class="float-right  ml-2">'
		+'	<label for="filterquery">Filter:&nbsp;</label>'
		+'	<input type="text" name="filterquery" class="dataviewer-filterquery form-control form-control-sm '+filterHighlightClass+'" value="'+filterquery.replaceAll('"','&quot;')+'" placeholder="Filter..." '+onchangeAttribute+'>'
		+'</div>';
	
	html += '</div>';
	
	//--------------------------------------
	// Toogle Button
	
	var topCorrectionCSS = "";
	if(dataviewerSettings.pagination == "bottom" 
	|| dataviewerSettings.pagination == "none"
	|| dataviewerSettings.pagination == false){
		topCorrectionCSS = "top: 0px;";
	}
	
	if(dataviewerSettings.menu == 'button'){
		var dropDownID = 'dropdownMenuButton'+CFW.utils.randomString(12);
		var dropdownHTML = '<div class="dropleft d-inline pl-1 cfw-dataviewer-settings-button">'
			+ '<button  type="button" class="btn btn-xs btn-primary mb-2 '+filterHighlightClass+'"'
					+' id="'+dropDownID+'" '
					+' style="'+topCorrectionCSS+'" '
					+' data-toggle="dropdown" '
					+' aria-haspopup="true" '
					+' aria-expanded="false">'
			+ '  <i class="fas fa-sliders-h"></i> '
			+ '</button>'
			+ '  <div class="dropdown-menu p-2" onclick="event.stopPropagation()"  aria-labelledby="'+dropDownID+'">'
			     + html
			+'   </div> '
			+'</div>';
			
		return dropdownHTML;
		
	}
	
	// default
	return html;
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createPageListItem(dataviewerID, page, label, isActive) {
	return '<li class="page-item '+(isActive ? 'active':'')+'">'
				+'<a class="page-link" onclick="cfw_renderer_dataviewer_fireChange(\''+dataviewerID+'\', '+page+')">'+label+'</a>'
			+'</li>';
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_triggerDownload(dataviewerID, renderer) {
	
	let dataviewerDiv = $("#"+dataviewerID);
	let renderDef = dataviewerDiv.data('renderDef');
	renderDef.visiblefields = null;
	
	let renderedResult = CFW.render.getRenderer(renderer).render(renderDef);
	let formattedData = renderedResult.find('code').text();
	
	
	switch(renderer){
		
		case "csv": 
			CFW.utils.downloadText("data.csv", formattedData); 
		break;
		
		case "json": 
			CFW.utils.downloadText("data.json", formattedData); 
		break;
		
		case "xml": 
			CFW.utils.downloadText("data.xml", formattedData); 
		break;

	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_triggerStoreData(dataviewerID) {
	
	let dataviewerDiv = $("#"+dataviewerID);
	let renderDef = dataviewerDiv.data('renderDef');
	let nameSuggestions = [];
	
	cfw_ui_storeJsonDataModal(renderDef.data, dataviewerDiv, nameSuggestions);

}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createDownloadButtonHTML(dataviewerID) {
	
	var dropDownID = 'dropdownMenuButton'+CFW.utils.randomString(12);
	var dropdownHTML = '<div class="dropdown d-inline">'
		+ '<button  type="button" class="btn btn-sm btn-primary"'
				+' id="'+dropDownID+'" '
				+' data-toggle="dropdown" '
				+' aria-haspopup="true" '
				+' aria-expanded="false">'
		+ '  <i class="fas fa-download"></i>'
		+ '</button>'
		+ '  <div class="dropdown-menu p-2" onclick="event.stopPropagation()"  aria-labelledby="'+dropDownID+'">'
		     	+ '<a class="dropdown-item" onclick="cfw_renderer_dataviewer_triggerDownload(\''+dataviewerID+'\', \'csv\')">CSV</a>'
		     	+ '<a class="dropdown-item" onclick="cfw_renderer_dataviewer_triggerDownload(\''+dataviewerID+'\', \'json\')">JSON</a>'
		     	+ '<a class="dropdown-item" onclick="cfw_renderer_dataviewer_triggerDownload(\''+dataviewerID+'\', \'xml\')">XML</a>'
		+'   </div>'
		+'</div>';
		
	return dropdownHTML;

}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createStoreButtonHTML(dataviewerID) {
	
	var buttonID = 'storedataMenuButton'+CFW.utils.randomString(12);
	
	var dropdownHTML = 
		  '<button  type="button" class="btn btn-sm btn-primary"'
				+' id="'+buttonID+'" '
				+' onclick="cfw_renderer_dataviewer_triggerStoreData(\''+dataviewerID+'\')">'
		+ '  <i class="fas fa-save"></i>'
		+ '</button>'
		;
		
	return dropdownHTML;

}
	
/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createSortSelectHTML(dataviewerSettings, renderDef, onchangeAttribute) {
	
	let html = '<div class="float-right ml-2">'
			+'	<label for="sortby">Sort By:&nbsp;</label>'
			+'	<select name="sortby" class="dataviewer-sortby form-control form-control-sm" '+onchangeAttribute+'>'
			
	
	//--------------------------------------
	// Prepare Settings

	let selectedSortbyFields = null;
	let selectedSortbyDirection = "asc";
	
	if(dataviewerSettings.storeid != null){
		selectedSortbyFields 	= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][sortbyFields]', selectedSortbyFields);
		selectedSortbyDirection	= CFW.cache.retrieveValueForPage('dataviewer['+dataviewerSettings.storeid+'][sortbyDirection]', selectedSortbyDirection);
	}
	
	//-----------------------------
	// Handle Nulls
	let sortoptions = dataviewerSettings.sortoptions;

	if(sortoptions == null 
	|| (Array.isArray(sortoptions) && sortoptions.length == null )
	){
		sortoptions = renderDef.visiblefields;
	}
	
	//-----------------------------
	// Array of Names
	if(Array.isArray(sortoptions)){

			let ascendingHTML = ""; 
			let descendingHTML = ""; 
			for(index in sortoptions){
				let fieldName = sortoptions[index];
				let fielLabel = renderDef.getLabel(fieldName, CFW_RENDER_NAME_DATAVIEWER);
				
				
				let selectedAsc = '';
				let selectedDesc = '';
				if(index == 0 && selectedSortbyFields == null){
					selectedAsc = 'selected';
				}else{
					
					if(fieldName == selectedSortbyFields){
						if(selectedSortbyDirection == 'desc'){
							selectedDesc = 'selected';
						}else{
							//default
							selectedAsc = 'selected';
						}
					}
				}
						
				ascendingHTML += '<option value="'+fieldName+'" data-direction="asc" '+selectedAsc+'>&uarr; '+fielLabel+'</option>';
				descendingHTML += '<option value="'+fieldName+'" data-direction="desc" '+selectedDesc+'>&darr; '+fielLabel+'</option>';
				
			}
		
			html += ascendingHTML + descendingHTML;
	}
	
	//-----------------------------
	// Object of Names
	if( ! Array.isArray(sortoptions) && typeof sortoptions == 'object'){
			let ascendingHTML = ""; 
			let descendingHTML = ""; 
			for(let fielLabel in sortoptions){
				let fieldValue = sortoptions[fielLabel];
				
				let sortbyFields = [];
				let sortbyDirections = [];
				if(fieldValue.length > 0){ sortbyFields = JSON.stringify(fieldValue[0]); }
				if(fieldValue.length > 1){ sortbyDirections = JSON.stringify(fieldValue[1]); }
				
				
				let selectedAsc = '';
				let selectedDesc = '';
				if(index == 0 && selectedSortbyFields == null){
					selectedAsc = 'selected';
				}else{
					if(sortbyFields == selectedSortbyFields){
						if(selectedSortbyDirection == sortbyDirections){
							selectedDesc = 'selected';
						}else{
							//default
							selectedAsc = 'selected';
						}
					}
				}

				sortbyFields = sortbyFields.replaceAll('"', '&quot;');
				sortbyDirections = sortbyDirections.replaceAll('"', '&quot;');
				let sortbyDirectionsDesc = 
							sortbyDirections.toLowerCase()
										.replaceAll('asc', 'temp')
										.replaceAll('desc', 'asc')
										.replaceAll('temp', 'desc')
										;
					
				ascendingHTML += '<option value="'+sortbyFields+'" data-direction="'+sortbyDirections+'" '+selectedAsc+'>&uarr; '+fielLabel+'</option>';
				descendingHTML += '<option value="'+sortbyFields+'" data-direction="'+sortbyDirectionsDesc+'" '+selectedDesc+'>&darr; '+fielLabel+'</option>';
				
			}
		
			html += ascendingHTML + descendingHTML;
	}
	
	//-----------------------------
	// Close HTML
	html +='	</select>'
		 + '</div>';
	
	return html;
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_renderer_dataviewer_createNavigationHTML(dataviewerID, totalRecords, pageSize, pageActive, download) {
	//============================================
	// Variables
	
	var totalPages = (pageSize == -1) ? 1 : Math.ceil(totalRecords / pageSize);
	
	var itemText = "Items";
	if(totalRecords == 1) { itemText = "Item"}
	
	var pagesText = "Pages";
	if(totalPages == 1) { pagesText = "Page"}
	
	
	//============================================
	// HTML
	var html = 
		'<nav aria-label="Page Navigation" class="d-flex justify-content-end align-items-center mb-1">'
			+'<span class="mr-2"><strong>'+totalRecords+'</strong> '+itemText+' on <strong>'+totalPages+'</strong> '+pagesText+'</span>'
			+'<ul class="pagination pagination-sm m-0">'
			+ cfw_renderer_dataviewer_createPageListItem(dataviewerID, 1,'<i class="fas fa-angle-double-left"></i>', false)
			+ cfw_renderer_dataviewer_createPageListItem(dataviewerID, ((pageActive > 1) ? pageActive-1 : 1),'<i class="fas fa-angle-left"></i>', false);
			
	var pageListItems = '';
	

	//============================================
	// Create Pages

	pageListItems += cfw_renderer_dataviewer_createPageListItem(dataviewerID, pageActive, pageActive, true);
	let pagesCreated = 1;

	//-------------------------------
	// Lower Pages
	let currentPage = pageActive-1;
	for(let i = 1; i < 4 && currentPage >= 1; i++ ){
		pageListItems = 
			cfw_renderer_dataviewer_createPageListItem(dataviewerID, currentPage,currentPage, false)
			+ pageListItems ;
		
		pagesCreated++;
		currentPage--;
	}
	if(currentPage > 2){
		var jumpPage = Math.ceil(currentPage / 2);
		pageListItems = cfw_renderer_dataviewer_createPageListItem(dataviewerID, jumpPage,jumpPage, false)
						+ pageListItems;
		pagesCreated++;
	}
	
	//-------------------------------
	// Higher Pages
	currentPage = pageActive+1;
	for(; pagesCreated < 8 && currentPage <= totalPages; ){
		pageListItems += cfw_renderer_dataviewer_createPageListItem(dataviewerID, currentPage,currentPage, false);
		pagesCreated++;
		currentPage++;
	}
	if(currentPage < totalPages-1){
		var jumpPage = currentPage+Math.ceil((totalPages-currentPage) / 2);
		pageListItems += cfw_renderer_dataviewer_createPageListItem(dataviewerID, jumpPage,jumpPage, false);
	}

		
	//-------------------------------------
	// Add Navigate Forward Buttons
	html += pageListItems;
	html +=cfw_renderer_dataviewer_createPageListItem(dataviewerID, ((pageActive < totalPages) ? pageActive+1 : totalPages),'<i class="fas fa-angle-right"></i>', false)
		 + cfw_renderer_dataviewer_createPageListItem(dataviewerID, totalPages,'<i class="fas fa-angle-double-right"></i>', false);
	
			
	html +='</ul></nav>';

	return html;
}
