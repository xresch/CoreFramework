
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/


/******************************************************************
 * Global
 ******************************************************************/
var CFW_MANUAL_HOST_URL = CFW.http.getHostURL();
var CFW_MANUAL_URL = '/app/manual';


/******************************************************************
 * 
 ******************************************************************/
function cfw_manual_loadPage(pagePath, callback){
	
	var path = CFW.http.getURLPath();
	
	if(path.includes(CFW_MANUAL_URL)){
		cfw_manual_printContent($('a[data-path="'+pagePath+'"]').get(0), callback);
	}else{

		var url = CFW_MANUAL_HOST_URL +"/app/manual?page="+encodeURIComponent(pagePath);
		
		window.open(url, "_blank");
	}
	
	
	
}
	



