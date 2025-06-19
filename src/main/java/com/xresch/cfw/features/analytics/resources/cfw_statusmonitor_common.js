/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT
 **************************************************************************************************************/
const CFW_CACHE_STATISTICS_URL='/app/statusmonitor';


/******************************************************************
 * 
 ******************************************************************/
function cfw_statusmonitor_applyColorOnLoad(){
	
	let worstStatusItem = $("#cfw-worst-status");
	
	if(worstStatusItem.length > 0){
		let worstStatus = worstStatusItem.attr('data-worst-status');
		
		let menuButton = $("#cfwMenuButtons-StatusMonitor");
		let faicon = menuButton.find(".fas");
		
		let style = CFW.colors.getCFWStateStyle(worstStatus);

		CFW.colors.colorizeElement(faicon, style, "text");
		
		
		
	}
}

cfw_statusmonitor_applyColorOnLoad();

/******************************************************************
 * 
 ******************************************************************/
function cfw_statusmonitor_loadInMenu(){
	cfw_statusmonitor_fetchStatusMonitorsAndDisplay(true);
}


	
/******************************************************************
 * 
 ******************************************************************/
function cfw_statusmonitor_fetchStatusMonitorsAndDisplay(isMenu){

	CFW.http.getJSON(CFW_CACHE_STATISTICS_URL, {action: "fetch", item: "statusmonitor"}, function(data){
		
		if(data.payload != null){
			
			let payload = data.payload;
			
			//-----------------------------------
			// Find and clear target
			let rendererName = 'statustiles';
			let target = $("#cfwMenuButtons-StatusMonitor")
							.parent()
							.find(".dropdown-submenu")
							;
				target.addClass("p-1")
				target.css("width", "250px")
			if(!isMenu){
				rendererName = 'statuslist';
				target = $("#statusmonitor");
			}
			
			target.html('');
			
			//-----------------------------------
			// Add Status time
			if(isMenu){
				target.append('<li><b>Status Time:&nbsp;</b></li>'
							+ '<li>'+CFW.format.epochToTimestamp(payload.time)+'</li>'
						);
			}else{
				target.append('<p><b>Status Time:&nbsp;</b>'
							+ CFW.format.epochToTimestamp(payload.time) 
						  +'</p>');
			}
			
			
			//-----------------------------------
			// Render Settings
			
			var rendererSettings = {
					data: null,
				 	idfield: null,
				 	bgstylefield: "bgstyle",
				 	textstylefield: null,
				 	titlefields: ['name'],
				 	visiblefields: ['status', 'name'],
				 	titleformat: '{0} {1}',
				 	labels: {},
				 	customizers: {},
					rendererSettings: {
						tiles: {
							sizefactor: 0.3
							//, borderstyle: "ellipsis"
						}
					}
				};
			
			for(let category in payload.categories)	{	
				let categoryArray = payload.categories[category];
				rendererSettings.data = categoryArray;
				
				// Do not display empty categories
				if(categoryArray == null || categoryArray.length <= 0 ){
					continue;
				}
				
				for(let index in categoryArray){
					let current = categoryArray[index];
					current.bgstyle = CFW.colors.getCFWStateStyle(current.status);
				}
				
				let renderResult = CFW.render.getRenderer(rendererName).render(rendererSettings);
				
				
				if(isMenu){								
					target.append('<li class="mt-2"><b>'+category+':&nbsp;</b></li>');
					target.append(renderResult);
				}else{
					target.append('<h3>'+category+'</h3>');
					target.append(renderResult);
				}
				
			}	
			
			
		}
	});
	
}
