
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
/******************************************************************
 * Global
 ******************************************************************/
var CFW_MANUAL_COUNTER = 0;
var CFW_MANUAL_GUID_PAGE_MAP = {};

/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_printMenu(data){
	
	console.log(data.payload);
	
	var parent = $('#menu-content');
	var htmlString = '';
	
	var pageArray = data.payload;
	for(var i = 0; i < pageArray.length; i++){
		htmlString += cfw_manual_createMenuItem(pageArray[i]);
	}
	
	parent.append(htmlString);
	
	var pagePath = CFW.http.getURLParamsDecoded()["page"];
	if(pagePath != undefined){
		cfw_manual_loadPage(pagePath);
	}
}

/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_loadPage(pagePath){
	cfw_manual_printContent($('a[data-path="'+pagePath+'"]').get(0));
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
function cfw_manual_printContent(domElement){
	
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
			
			titleTarget.append('<h1>'+pageData.title+'</h1>');
			
			target.html(pageData.content);
			
			//------------------------------
			// Highlight Code Blocks
			target.find('pre code').each(function(index, element){
				console.log("highlight");
				hljs.highlightBlock(element);
			})
			
			//------------------------------
			// Create TOC
			CFW.ui.toc(target, "#manual-toc", 'h2');
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