
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/


/******************************************************************
 * Global
 ******************************************************************/
var CFW_MANUAL_URL = "./manual";
var CFW_MANUAL_COUNTER = 0;
var CFW_MANUAL_COUNTER_PRINT_GUID = 0;
var CFW_MANUAL_COUNTER_PRINT_IN_PROGRESS = 0;
var CFW_MANUAL_GUID_PAGE_MAP = {};


var CFW_MANUAL_PRINTVIEW_PAGEPATH_ANCHOR_MAP = {};

/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_manual_searchManual(event){
	
	//-----------------------
	// Only Execute on Enter
	if (event.keyCode != 13) {
        return;
    }
    
    //-----------------------
	// Get Input Field
    let inputField = event.target || event.srcElement;
	let query = $(inputField).val();
	
	//-----------------------
	// Send Request
	CFW.http.getJSON(CFW_MANUAL_URL, {action: "search", item: "page", query: query}, function (data){
		
		if(data.payload != undefined){
			var results = data.payload;
			let titleTarget = $('#cfw-manual-page-title');
			let target = $('#cfw-manual-page-content');
			titleTarget.html('<h1>Search: '+results.length+' Results</h1>');
			target.html('');
			
			for(let i in results){
				let current = results[i];
				
				let resultDiv = $('<div class="card p-2" >');
				resultDiv.append('<div class="h3 m-0 cfw-link">'
								    +'<b><a onclick="cfw_manual_loadPage(\''+current.path+'\')">'+current.title+'</a><b>'
								+'</div>');
				resultDiv.append('<div class="font-sm mb-2 text-secondary"><b>Path:&nbsp;</b>'+current.path.replaceAll('\|', ' &gt;&gt; ') +'</div>');
				resultDiv.append('<div>'+current.snippet.replaceAll('\n', '<br>') +'</div>');
		
				target.append(resultDiv);
				
			}
			
		}
		
	});


}

/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_manual_filterMenu(inputField){
	
	CFW.utils.filterItems('#menu-content', inputField, '.filterable');
	

	//---------------------------------------------
	// Open all parent elements and highlight
	if( CFW.utils.isNullOrEmpty($(inputField).val()) ){

		$('#menu-content')
			.find('.collapse')
			.removeClass('show')
			.addClass('hide')
			;
		$('.cfw-manual-selected').parents().filter('.collapse').collapse('show');
	}


}
	

/*********************************************************************************
* Creates a printView by opening a new window and returns a divElement where you 
* can put the content inside which you want to print.
* 
* @param "cards or "text"
* @return domElement a div you can write the content to print to.
*********************************************************************************/
function cfw_manual_createPrintView(pageGUID){
	
	//--------------------------
	// Create Window
	var printView = window.open();
	
	var printBox = printView.document.createElement("div");
	printBox.id = "cfw-manual-printview-box";
	printView.document.body.appendChild(printBox);
	
	//--------------------------
	// Copy Styles
	var stylesheets = $('link[rel="stylesheet"]');
	for(let i = 0; i < stylesheets.length; i++){
		let href = stylesheets.eq(i).attr('href');
		if(!CFW.utils.isNullOrEmpty(href) && href.startsWith('/cfw')){
			let cssLink = printView.document.createElement("link");
			cssLink.rel = "stylesheet";
			cssLink.media = "screen, print";
			cssLink.href = CFW_MANUAL_HOST_URL+href;
			printView.document.head.appendChild(cssLink);
		}
	}	
	
	//--------------------------
	// Override Bootstrap Style
	let cssLink = printView.document.createElement("link");
	cssLink.rel = "stylesheet";
	cssLink.media = "screen, print";
	cssLink.href = CFW_MANUAL_HOST_URL+"/cfw/jarresource?pkg=com.xresch.cfw.features.core.resources.css&file=bootstrap-theme-bootstrap.css";
	printView.document.head.appendChild(cssLink);
		
	//--------------------------
	// Override Code Style
	let cssCodestyleLink = printView.document.createElement("link");
	cssCodestyleLink.rel = "stylesheet";
	cssCodestyleLink.media = "screen, print";
	cssCodestyleLink.href = CFW_MANUAL_HOST_URL+"/cfw/jarresource?pkg=com.xresch.cfw.features.core.resources.css&file=highlightjs_arduino-light.css";
	printView.document.head.appendChild(cssCodestyleLink);
	
	//--------------------------
	// Copy Scripts
	var javascripts = $('#javascripts script');

	for(let i = 0; i < javascripts.length; i++){

		let source = javascripts.eq(i).attr('src');
		if(!CFW.utils.isNullOrEmpty(source) && source.startsWith('/cfw')){
			let script = printView.document.createElement("script");
			script.src = CFW_MANUAL_HOST_URL+source;
			printView.document.head.appendChild(script);
		}
	}
	
	var parent = $(printBox);
	
	//--------------------------
	//create window title	
	var title = printView.document.createElement("title");
	title.innerHTML = "Manual";
	printView.document.head.appendChild(title);
	
	//--------------------------
	//Create CSS
	
	var cssString = '<style  media="print, screen">'
		+'html, body {'
			+'margin: 0px;'
			+'padding: 0px;'
			+'font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";'
		+'}'
		+'table, pre, code, p { page-break-inside:auto }'
		+'tr    { page-break-inside:avoid; page-break-after:auto }'
		+'#paper {'
			+'padding: 20mm;'
			+'width: 100%;'
			+'border-collapse: collapse;'
		+'}'
		+'img{'
			+'padding-bottom: 5mm;' 
		+'}'
		+'h1{'
			+'page-break-before: always;' 
		+'}'
		+'h1 {font-size: 32px;}' 
		+'h2 {font-size: 30px;}' 
		+'h3 {font-size: 28px;}' 
		+'h4 {font-size: 26px;}'
		+'h5 {font-size: 24px;}'
		+'h6 {font-size: 22px;}'
		+'div, p, span, table {font-size: 20px;}' 
		+'h1, h2, h3, h4, h5, h6{'
			+'padding-top: 5mm;' 
		+'}'
		+'#print-toc > h1, #doc-title, h1 + div > h1,  h1 + div > .page-break, h2 + div > .page-break, h3 + div > .page-break, h4 + div > .page-break{'
			+'page-break-before: avoid;' 
		+'}'
		+'</style>';
	
	parent.append(cssString);
		
	//--------------------------
	// Print as Text
	var parentPages = [];
	var titleString = "Application Manual";
	if(pageGUID != null){
		//--------------------------
		// Print Current Page
		parentPages = [CFW_MANUAL_GUID_PAGE_MAP[pageGUID]];
		titleString = 'Manual - '+parentPages[0].title;
	}else{
		//--------------------------
		// Print Full manual
		$('#menu-content > li > a').each(function(){
			var id = $(this).attr('id');
			parentPages.push(CFW_MANUAL_GUID_PAGE_MAP[id]);
		});
		
	}
	
	if(parentPages.length != 0){
		title.innerHTML = titleString;
		var paper = $('<div id="paper">');
		var printTOC = $('<div id="print-toc">');
		
		paper.append('<h1 id="doc-title" class="toc-hidden">'+titleString+'</h1>');
		paper.append(printTOC);
		parent.append(paper);
		
		for(let i = 0; i < parentPages.length; i++){
			cfw_manual_addPageToPrintView(paper, parentPages[i], 0);
		}
		
		//--------------------------
		// Post Process
		var progressInterval = window.setInterval(function(){
			if(CFW_MANUAL_COUNTER_PRINT_IN_PROGRESS == 0){
				CFW.ui.toc(paper, printTOC, 'h1');
				window.clearInterval(progressInterval);

				cfw_manual_printview_postProcessPageLinks(paper);				
			}

		}, 500);
	}
	

	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_addPageToPrintView(parentContainer, page, headerOffset){
	
	var head = headerOffset+1;
	
	var guid = "page-"+CFW.utils.randomString(16);
	CFW_MANUAL_PRINTVIEW_PAGEPATH_ANCHOR_MAP[page.path] = guid;
	
	var anchorForLinks = $('<a name="'+guid+'">');
	var pageDiv = $('<div id="'+guid+'">');

	pageDiv.append('<h'+head+'>'+page.title+'</h'+head+'>');
	pageDiv.append(anchorForLinks);
	
	parentContainer.append(pageDiv);
		
	if(page.hasContent){
		CFW_MANUAL_COUNTER_PRINT_IN_PROGRESS++;
		CFW.http.getJSON(CFW_MANUAL_URL, {action: "fetch", item: "page", path: page.path}, function (data){
			if(data.payload != undefined){
				var pageData = data.payload;
				var enhancedContent = cfw_manual_preparePageForPrint(pageData.content, head);				
				pageDiv.find('h'+head).first(1).after(enhancedContent);
				pageDiv.find('pre code').each(function(index, element){
					hljs.highlightElement(element);
				})
			}
			CFW_MANUAL_COUNTER_PRINT_IN_PROGRESS--;
		});
	}
	
	if(!CFW.utils.isNullOrEmpty(page.children)){
		for(var i = 0; i < page.children.length; i++){
			cfw_manual_addPageToPrintView(pageDiv, page.children[i], headerOffset+1);
		}
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_preparePageForPrint(pageContent, headerOffset){


	

	if( CFW.utils.isNullOrEmpty(pageContent) ){
		return "";
	}else{
		
		//------------------------------
		// Replace relative paths
		pageContent = CFW.utils.replaceAll(pageContent, '"/cfw', '"'+CFW_MANUAL_HOST_URL+'/cfw');
		pageContent = CFW.utils.replaceAll(pageContent, '"/app', '"'+CFW_MANUAL_HOST_URL+'/app');
		
		//------------------------------
		// Find first Header Level
		var level = 0;
		for(level = 1; level <= 7; level++){
	
			if(level == 7){ level = -1; break;}
			if(pageContent.indexOf('<h'+level) > -1){
				break;
			}
		}
		
		//------------------------------
		// Replace Headers
		if(level != -1){
	
			var offset = headerOffset - level + 1;
			for(var i = 5; i > 0; i-- ){
	
				var newLevel = (i+offset <= 6) ? i+offset : 6;
				pageContent = pageContent.replaceAll('</h'+i, '</h'+newLevel)
										.replaceAll('<h'+i, '<h'+newLevel);
			}
		}
		
		return pageContent;
	}

		

	
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_printview_postProcessPageLinks(paper){
	
	var pageLinks = paper.find('a[onclick^="cfw_manual_loadPage"]');
	
	pageLinks.each(function(){
		var link = $(this);
		var onclick = link.attr('onclick');
		var pagePath = onclick.replace("cfw_manual_loadPage('", "")
							  .replace("');", "");

		var anchorName = CFW_MANUAL_PRINTVIEW_PAGEPATH_ANCHOR_MAP[pagePath.trim()];
		
		//-----------------------------
		// Adjust Link
		link.attr('onclick', '');
		link.attr('href', '#'+anchorName);
	});
	
}


/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_printMenu(data){
	
	var parent = $('#menu-content');
	var htmlString = '';
	
	var pageArray = data.payload;
	for(var i = 0; i < pageArray.length; i++){
		htmlString += cfw_manual_createMenuItem(pageArray[i]);
	}
	
	parent.append(htmlString);
	
	var pagePath = CFW.http.getURLParamsDecoded()["page"];
	if(pagePath != undefined){
		var index = pagePath.indexOf('#');
		if(index == -1){
			cfw_manual_loadPage(pagePath);
		}else{
			cfw_manual_loadPage(pagePath.substring(0,index), function(){
				location.hash = pagePath.substring(index);
			});
			
		}
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_loadPage(pagePath, callback){
	cfw_manual_printContent($('a[data-path="'+pagePath+'"]').get(0), callback);
	
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_createMenuItem(pageData){
	CFW_MANUAL_COUNTER++;
	CFW_MANUAL_GUID_PAGE_MAP[CFW_MANUAL_COUNTER] = pageData;
	
	var collapseID = 'collapse-'+CFW_MANUAL_COUNTER;	
	//-------------------------
	// arrow
	var arrow = '<div class="cfw-manual-fa-box">';
	var dataToggle = '';
	if(pageData.children != null && pageData.children.length > 0){
		dataToggle = ' data-toggle="collapse" data-target="#'+collapseID+'" aria-expanded=false'
		arrow += '<i class="arrow" '+dataToggle+'></i>';
	}else{
		arrow += "&nbsp;";
	}
	arrow += '</div>';
	
	//-------------------------
	// faicon
	var faicon = "";
	/*if(pageData.faiconClasses != null){
		faicon = '<i class="'+pageData.faiconClasses+'"></i>';
	}*/
	
	//-------------------------
	// Title
	var onclick = '';
	if(pageData.hasContent){
		onclick = 'onclick="cfw_manual_printContent(this)"';
	}
	
	//-------------------------
	// Put everything together
	var htmlString = '<li class="filterable">';
	htmlString += arrow+'<a id="'+CFW_MANUAL_COUNTER+'" data-path="'+pageData.path+'" '+onclick+' '+dataToggle+'>'+faicon+' <span>'+pageData.title+'</span> </a>';
	htmlString += '</li>';
	
	//-------------------------
	// Title
	if(pageData.children != null && pageData.children.length > 0){
		htmlString += '<ul class="sub-menu collapse" id="'+collapseID+'">';
		for(var i = 0; i < pageData.children.length; i++){
			htmlString += cfw_manual_createMenuItem(pageData.children[i]);
		}
		htmlString += '</ul>';
	}
	
	return htmlString;
}

/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_manual_printContent(domElement, callback){
	
	//---------------------------------------------
	// Open all parent elements and highlight
	$('.cfw-manual-selected').removeClass('cfw-manual-selected');
	$(domElement).closest('li').addClass('cfw-manual-selected');
	$(domElement).parents('ul').collapse('show');
	
	//---------------------------------------------
	// Open all parent elements
	var id = $(domElement).attr('id');
	var page = CFW_MANUAL_GUID_PAGE_MAP[id];
	
	var titleTarget = $('#cfw-manual-page-title');
	var target = $('#cfw-manual-page-content');
	titleTarget.html('');
	target.html('');
	
	window.scrollTo(0, 0);
	
	CFW.http.fetchAndCacheData("./manual", {action: "fetch", item: "page", path: page.path}, "page"+page.path, function (data){
		if(data.payload != undefined){
			var pageData = data.payload;
			
			CFW.http.setURLParams({"page": pageData.path});
			
			titleTarget.append('<h1>'+pageData.title+'<a class="btn btn-primary btn-sm ml-2" onclick="cfw_manual_createPrintView('+id+')"><i class="fas fa-print"></i></a></h1>');
			
			target.html(pageData.content);
			
			//------------------------------
			// Highlight Code Blocks
			target.find('pre code').each(function(index, element){
				hljs.highlightElement(element);
			})
			
			//------------------------------
			// Create TOC
			CFW.ui.toc(target, "#manual-toc", 'h2');
			
			if(callback != null){
				callback();
			}
		}
		
	})
	
	
	
}
/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_manual_draw(){
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){

		CFW.http.fetchAndCacheData("./manual", {action: "fetch", item: "menuitems"}, "menuitems", cfw_manual_printMenu);
		
		CFW.ui.toggleLoader(false);
	}, 100);
}
