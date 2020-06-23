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
			defaulttitle: "",
			defaultwidth: 1,
			defaultheight: 1,
			createWidgetInstance: function (widgetObject, callback) {		
				var html = 
					 '<button class="btn btn-sm btn-primary fa fa-globe" onclick="Disco('+widgetObject.JSON_SETTINGS.discolevel+')" style="height: 100%; width:100%;"></button>'
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

function getRandomColor() {
	var letters = '0123456789ABCDEF';
	var color = '#';
	for (var i = 0; i < 6; i++) {
	  color += letters[Math.floor(Math.random() * 16)];
	}
	return color;
}

var discoSet = window.localStorage.getItem("Disco");
var discoToggle = null;

function Disco(level) {
	var speed = (1000 - level) / 100;
	if (discoSet == null && discoToggle == null) {
		$('[id^="widget-"]').addClass("fa-spin");
		discoSet = setInterval(function(){ 
			var bodyColor = getRandomColor();
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