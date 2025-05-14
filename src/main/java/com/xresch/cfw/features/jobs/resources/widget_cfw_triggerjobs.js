
let CFW_TRIGGERJOBS_URL = "/app/jobs";
/******************************************************************
 * Execute
 ******************************************************************/
function cfw_widget_triggerjobs_rerender(){
	
	$('div[data-type="cfw_triggerjobs"]').each(function(){
		
		console.log($(this).parent());
		var widgetInstance = $(this).parent();
		var guid = widgetInstance.attr('id');

		cfw_dashboard_widget_rerender(guid, false);

	});

}

// additional rerendering to make sure times are updated
 window.setInterval(cfw_widget_triggerjobs_rerender, 30000);
	
/******************************************************************
 * Execute
 ******************************************************************/
function cfw_widget_triggerjobs_execute(id){
	
	let obfuscatedID = (id + 7) * 761;
	let dashboardParams = cfw_parameter_getFinalParams(CFW_DASHBOARD_PARAMS);

	params = {action: "execute", item: "widgettriggerjob", id: obfuscatedID, dashboardParams: JSON.stringify(dashboardParams)};
	CFW.http.getJSON(CFW_TRIGGERJOBS_URL, params, 
		function(data) {
			if(data.success){ 
				cfw_widget_triggerjobs_rerender();
			}
	});
}


/******************************************************************
 * Execute
 ******************************************************************/
function cfw_widget_triggerjobs_stop(id){
	
	let obfuscatedID = (id + 7) * 761;
	params = {action: "stop", item: "widgettriggerjob", id: obfuscatedID};
	CFW.http.getJSON(CFW_TRIGGERJOBS_URL, params, 
		function(data) {
			if(data.success){
				cfw_widget_triggerjobs_rerender();
			}
	});
}

/******************************************************************
 * update Clocks
 ******************************************************************/
 window.setInterval(function(){
	$('.cfw-triggerjobs-clock').each(function(){
		let clockDiv = $(this);
		let startMillis = clockDiv.data("EXECUTION_START");
		let millis = Date.now() - startMillis;
		clockDiv.text('[' + CFW.format.millisToDurationClock(millis)+']');
	})

}, 1000);
	
(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_triggerjobs",
		{
			category: CFW.dashboard.global.categoryAdvanced,
			menuicon: "fas fa-magic",
			menulabel: "Trigger Jobs",
			description: "Allows a user to start and stop jobs.", 
			createWidgetInstance: function (widgetObject, params, callback) {
					
				
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					//var settings = widgetObject.JSON_SETTINGS;				
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Check is Public Dashboard
					if(CFW.http.getURLPath().includes("/public/dashboard/")){
						callback(widgetObject, 'Cannot trigger jobs from public dashboards.');
						return;
					}
					
					
					//---------------------------------
					// Render Data
					var targetDiv = $('<div class="flex-column align-items-start h-100 w-100">');
					
					var payloadissimo = data.payload; // doesn't make it shorter but more interesting ;-)
					for(let key in payloadissimo){
						let job = payloadissimo[key];
						let jobDiv = $('<div class="flex-row w-100 mb-1">');
						let isEnabled = job.IS_ENABLED;
						let isRunning = !CFW.utils.isNullOrEmpty(job.EXECUTION_START);

						//----------------------------
						// Execute Button
						jobDiv.append(
							'<button class="btn btn-xs btn-success"'
							+' alt="Execute" title="Execute" '
							+' '+ (isRunning || !isEnabled ? 'disabled' : '') +' '
							+' onclick="CFW.ui.confirmExecute(\'Do you really want to execute the job <strong>\\\''+job.JOB_NAME.replace(/\"/g,'&quot;')+'\\\'</strong> now?\', \'Let\\\'s Go!\', \'cfw_widget_triggerjobs_execute('+job.PK_ID+');\')">'
								+ '<i class="fa fa-play"></i>'
							+ '</button>'
							);
							
						//----------------------------
						// Stop Button
						jobDiv.append(
							'<button class="btn btn-xs btn-danger ml-1"'
							+' alt="Stop" title="Stop" '
							+' '+ (!isRunning || !isEnabled ? 'disabled' : '') +' '
							+' onclick="CFW.ui.confirmExecute(\'Do you really want to stop the job <strong>\\\''+job.JOB_NAME.replace(/\"/g,'&quot;')+'\\\'</strong> now?\', \'Stop it now!\', \'cfw_widget_triggerjobs_stop('+job.PK_ID+');\')">'
								+ '<i class="fa fa-ban"></i>'
							+ '</button>'
							);
					
						//----------------------------
						// Jobname
						var classMuted = ( isEnabled ? "" : "text-muted");
						var disabledText = ( isEnabled ? "" : "(Disabled)");
						jobDiv.append('<div class="'+classMuted+' pl-1">'
										+job.JOB_NAME
										+disabledText
									 +'</div>');
						
						//----------------------------
						// Job Duration
						if(isRunning){  
							let millis = Date.now() - job.EXECUTION_START;
							let clockDiv = $('<div class="cfw-triggerjobs-clock pl-1" >[' + CFW.format.millisToDurationClock(millis)+']<div>');
							clockDiv.data("EXECUTION_START", job.EXECUTION_START);
							jobDiv.append(clockDiv);
						}
						
						targetDiv.append(jobDiv);
					}
					
					callback(widgetObject, targetDiv);
				});
			},
			
		}
	);	
	
})();
