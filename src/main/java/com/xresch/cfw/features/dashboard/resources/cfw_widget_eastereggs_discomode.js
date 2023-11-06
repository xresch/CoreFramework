(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	//CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring");
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_discomode",
		{
			category: "Easter Eggs",
			menuicon: "fas fa-globe fa-spin",
			menulabel: CFWL('cfw_widget_discomode', "Disco!!!"),
			description: CFWL('cfw_widget_discomode_desc', "Toggles discomode."),
			defaultsettings: {
				TITLE: "",
				WIDTH: 3,
				HEIGHT: 4,
			},
			createWidgetInstance: function (widgetObject, params, callback) {		
				var html = 
					 '<button class="btn btn-sm text-white bg-cfw-purple fa fa-globe" onclick="cfw_widget_toggleDisco('+widgetObject.JSON_SETTINGS.discolevel+')" style="height: 100%; width:100%;"></button>'
					;
				callback(widgetObject, html);
				
			},
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
			
		}
	);
})();

var discoSet = window.localStorage.getItem("Disco");
var discoToggle = null;
var discoColor = 0;
function cfw_widget_toggleDisco(level) {
	var speed = (1000 - level) / 100;
	if (discoSet == null && discoToggle == null) {
		$('[id^="widget-"]').addClass("fa-spin");
		discoSet = setInterval(function(){ 
			discoColor += CFW.utils.randomInt(20,40);
			var bodyColor = CFW.colors.randomHSL(discoColor, 40,70,30,70);

			//$('[id^="widget-"]').addClass("fa-spin");
			$('[id^="widget-"]').css('animation', 'fa-spin '+speed+'s infinite linear');
			$('body').css("background-color", bodyColor);
		}, 1000 - level);
		window.localStorage.setItem('Disco', discoSet);
		discoToggle = discoSet;
	}else {
		$('body').css("background-color", "");
		clearInterval(discoSet);
		window.localStorage.removeItem("Disco");
		discoSet = null;
		discoToggle = null;
		//$('[id^="widget-"]').removeClass("fa-spin");
		$('[id^="widget-"]').css('animation', 'discospin '+speed+'s infinite linear');
	}
	
	
}