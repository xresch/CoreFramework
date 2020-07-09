
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
/******************************************************************
 * Global
 ******************************************************************/
var CFW_MANUAL_COUNTER = 0;
var CFW_MANUAL_COUNTER_PRINT_GUID = 0;
var CFW_MANUAL_COUNTER_PRINT_IN_PROGRESS = 0;
var CFW_MANUAL_GUID_PAGE_MAP = {};

var CFW_MANUAL_HOST_URL = CFW.http.getHostURL();

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
	for(var i = 0; i < stylesheets.length; i++){
		var href = stylesheets.eq(i).attr('href');
		if(!CFW.utils.isNullOrEmpty(href) && href.startsWith('/cfw')){
			var cssLink = printView.document.createElement("link");
			cssLink.rel = "stylesheet";
			cssLink.media = "screen, print";
			cssLink.href = CFW_MANUAL_HOST_URL+href;
			printView.document.head.appendChild(cssLink);
		}
	}
	
	var cssLink = printView.document.createElement("link");
	cssLink.rel = "stylesheet";
	cssLink.media = "screen, print";
	cssLink.href = CFW_MANUAL_HOST_URL+"/cfw/jarresource?pkg=com.xresch.cfw.features.core.resources.css&file=bootstrap.css";
	printView.document.head.appendChild(cssLink);
		
	//--------------------------
	// Copy Scripts
	var javascripts = $('#javascripts script');
	console.log(javascripts);
	for(var i = 0; i < javascripts.length; i++){
		console.log("A");
		var source = javascripts.eq(i).attr('src');
		if(!CFW.utils.isNullOrEmpty(source) && source.startsWith('/cfw')){
			var script = printView.document.createElement("script");
			script.src = CFW_MANUAL_HOST_URL+source;
			printView.document.head.appendChild(script);
		}
	}
	
	var parent = $(printBox);
	
	//--------------------------
	//create window title	
	var title = printView.document.createElement("title");
	title.innerHTML = "Quotes Print View";
	printView.document.head.appendChild(title);
	
	//--------------------------
	//Create CSS
	
	cssString = '<style  media="print, screen">'
		+'html, body {'
			+'margin: 0px;'
			+'padding: 0px;'
			+'font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";'
		+'}'
		+'table, pre, code, p { page-break-inside:auto }'
		+'tr    { page-break-inside:avoid; page-break-after:auto }'
		+'#paper {'
			+'padding: 7mm;'
			+'width: 80%;'
			+'border-collapse: collapse;'
		+'}'
		+'img{'
			+'padding-bottom: 5mm;' 
		+'}'
		+'h1, h2, h3{'
			+'padding-top: 5mm;' 
		+'}'
		'</style>';
	
	parent.append(cssString);
		


	//--------------------------
	// Print as Text
	var parentPage = CFW_MANUAL_GUID_PAGE_MAP[pageGUID];
	if(parentPage != null){
		title.innerHTML = 'Manual - '+parentPage.title;
		var paper = $('<div id="paper">');
		var printTOC = $('<div id="print-toc">');
		
		paper.append('<h1>Manual - '+parentPage.title+'</h1>');
		paper.append(printTOC);
		parent.append(paper);
		
		cfw_manual_addPageToPrintView(paper, parentPage);
		
		var progressInterval = window.setInterval(function(){
			if(CFW_MANUAL_COUNTER_PRINT_IN_PROGRESS == 0){
				CFW.ui.toc(paper, printTOC, 'h1');
				window.clearInterval(progressInterval);
			}

		}, 500);
	}
	

}

/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_addPageToPrintView(parentContainer, page){
		
	var guid = "page-"+CFW.utils.randomString(16);
	var pageDiv = $('<div id="'+guid+'">');
	pageDiv.append('<h1>'+page.title+'</h1>');
	parentContainer.append(pageDiv);
	
	if(page.hasContent){
		CFW_MANUAL_COUNTER_PRINT_IN_PROGRESS++;
		CFW.http.getJSON("./manual", {action: "fetch", item: "page", path: page.path}, function (data){
			if(data.payload != undefined){
				var pageData = data.payload;
				var enhancedContent = pageData.content.replace(/"\/cfw/g, '"'+CFW_MANUAL_HOST_URL+'/cfw')
	
				pageDiv.find('h1').first(1).after(enhancedContent);
				pageDiv.find('pre code').each(function(index, element){
					hljs.highlightBlock(element);
				})
			}
			CFW_MANUAL_COUNTER_PRINT_IN_PROGRESS--;
		});
	}
	
	if(!CFW.utils.isNullOrEmpty(page.children)){
		for(var i = 0; i < page.children.length; i++){
			cfw_manual_addPageToPrintView(pageDiv, page.children[i]);
		}
	}
	
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
		dataToggle = ' data-toggle="collapse" data-target="#'+collapseID+'" '
		arrow += '<i class="arrow" '+dataToggle+'></i>';
	}
	arrow += '</div>';
	
	//-------------------------
	// faicon
	var faicon = "";
	if(pageData.faiconClasses != null){
		faicon = '<i class="'+pageData.faiconClasses+'"></i>';
	}
	
	//-------------------------
	// Title
	var onclick = '';
	if(pageData.hasContent){
		onclick = 'onclick="cfw_manual_printContent(this)"';
	}
	
	//-------------------------
	// Put everything together
	var htmlString = '<li>';
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
	
	CFW.http.fetchAndCacheData("./manual", {action: "fetch", item: "page", path: page.path}, "page"+page.path, function (data){
		if(data.payload != undefined){
			var pageData = data.payload;
			
			CFW.http.setURLParam("page", pageData.path);
			
			titleTarget.append('<h1>'+pageData.title+'<a class="btn btn-primary btn-sm ml-2" onclick="cfw_manual_createPrintView('+id+')"><i class="fas fa-print"></i></a></h1>');
			
			target.html(pageData.content);
			
			//------------------------------
			// Highlight Code Blocks
			target.find('pre code').each(function(index, element){
				hljs.highlightBlock(element);
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
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){

		CFW.http.fetchAndCacheData("./manual", {action: "fetch", item: "menuitems"}, "menuitems", cfw_manual_printMenu);
		
		CFW.ui.toogleLoader(false);
	}, 100);
}