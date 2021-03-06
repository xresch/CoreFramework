
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
/******************************************************************
 * Global
 ******************************************************************/
var MODAL_CURRENT_NAME = "";
var MODAL_CURRENT_ACTION = "";

/******************************************************************
 * 
 ******************************************************************/
function cfw_apioverview_formResult(data, status, xhr){
	
	//-------------------------------
	// URL
	var form = $('#cfw-apioverview-samplemodal form');
	var serialized = form.serialize();
	
	//-------------------------------
	// Regex hack remove empty params
	serialized = serialized.replace(/cfw-formID.*?&/g, "&");
	serialized = serialized.replace(/&[^=]+=&/g, "&");
	serialized = serialized.replace(/&[^=]+=&/g, "&");
	serialized = serialized.replace(/&[^=]+=&/g, "&");
	serialized = serialized.replace(/&[^=]+=$/g, "&");
	
	var sampleURL = $('#cfw-apioverview-sampleurl');
	
	var url = window.location.href 
			+ "?apiName="+MODAL_CURRENT_NAME
			+ "&actionName="+MODAL_CURRENT_ACTION
			+ serialized;
	sampleURL.html('<a target="_blank" href="'+url+'">'+url+'</a>');

	hljs.highlightBlock(sampleURL.get(0));
	
	//-------------------------------
	// Sample CURL
	var curl = $('#cfw-apioverview-samplecurl');
	var cookie = JSDATA.id;
	var curlString = 'curl -H "Cookie: CFWSESSIONID='+cookie+'" -X GET "'+url+'"';
	curl.text(curlString);
	hljs.highlightBlock(curl.get(0));
	//-------------------------------
	// Sample Response
	var responseElement = $('#cfw-apioverview-response');
	responseElement.html('');
    
	var contentType = xhr.getResponseHeader("content-type") || "";
    if (contentType.indexOf('json') > -1) {
    	responseElement.text(JSON.stringify(data, null, 2));
    }else{
    	//responseElement.text(data.replace(/\r\n/g, "<br/>"));
    	responseElement.text(data);
    }
	
	
	responseElement.text();
	
	//hljs.highlightBlock(responseElement.get(0));
}

/******************************************************************
 * Edit user
 ******************************************************************/
function cfw_apioverview_createExample(apiName, actionName){
	
	MODAL_CURRENT_NAME = apiName;
	MODAL_CURRENT_ACTION = actionName;
	var allDiv = $('<div id="cfw-apioverview-samplemodal">');	

	//-----------------------------------
	// User Details
	//-----------------------------------
	var formDiv = $('<div id="cfw-apioverview-sampleform">');
	formDiv.append('<h2>Sample Form</h2>');
	allDiv.append(formDiv);
	
	//-----------------------------------
	// Placeholders
	//-----------------------------------
	allDiv.append('<h4>URL:</h4>');
	allDiv.append('<pre class="m-3" style="height: 50px;" ><code id="cfw-apioverview-sampleurl"></code></pre>');
	
	allDiv.append('<h4>CURL:</h4>');
	allDiv.append('<pre class="m-3" style="height: 50px;" ><code id="cfw-apioverview-samplecurl"></code></pre>');
	
	allDiv.append('<h4>Response:</h4>');
	allDiv.append('<pre class="m-3" style="max-height: 400px; display:block; white-space:pre-wrap" ><code id="cfw-apioverview-response"></code></pre>');
	//-----------------------------------
	//Show Modal and Load Form
	//-----------------------------------
	CFW.ui.showModalMedium("Example for "+apiName+": "+actionName, allDiv);
	
	CFW.http.createForm("./api", {formName: apiName, actionName: actionName, callbackMethod: "cfw_apioverview_formResult"}, formDiv);
	
}

/******************************************************************
 * Print the overview of the apis .
 * 
 ******************************************************************/
function cfw_apioverview_printLoginPanel(parent){
	
	//---------------------------
	// Create Panel Content
	var url = location.protocol+'//'+location.hostname+(location.port ? ':'+location.port: '');
	var apiURL = url +"/cfw/apilogin";
	var html = 
		 '<h2>Token-Based Access</h2>'
		+'<p>The API can be accessed by tokens. This is the recommended way to grant access to the API.</p>'
		+ '<ul>'
		+ '  <li><strong>Permissions: </strong>Tokens get access to the APIs it needs, while users have access to the whole API.</li>'
		+ '  <li><strong>Session Timeout: </strong>Sessions created with tokens time out faster than user sessions.</li>'
		+ '  <li><strong>No Login: </strong>No login is needed to access the API.</li>'
		+ '</ul>';
	
	html += 
		  '<p>To use the API with tokens,  either provide the token with the header \'API-Token\' or with the parameter \'apitoken\'. For example:</p>'
		+ '<pre class="cfwApiOverviewCode"><code>'
		+ 'curl -H "API-Token: exampletoken-IYQIf" -X GET "http://localhost:8888/app/api?apiName=User&actionName=fetchData"\n'
		+ 'curl -X GET "http://localhost:8888/app/api?apitoken=exampletoken-IYQIf&apiName=User&actionName=fetchData"</code></pre>';
	
	html += 
		  '<p>To get a list of all available APIs and actions for a token, call the api URL without providing actionName or apiName:</p>'
		+ '<pre class="cfwApiOverviewCode"><code>'
		+ 'curl -H "API-Token: exampletoken-IYQIf" -X GET "http://localhost:8888/app/api"\n'
		+ 'curl -X GET "http://localhost:8888/app/api?apitoken=exampletoken-IYQIf"</code></pre>';
	
	html +=	
		'<h2>User-Based Access</h2>'
		+'<p>Users can get access to this API user interface with the corresponding API permission. This can be useful for manual data extractions or for development purposes.</p>'
		+'<p>To login with a REST request, send a post request to <a target="_blank" href="'+apiURL+'">'+apiURL+'</a> with the following parameters in the post body: </p>'
		+ '<ul>'
		+ '  <li><strong>username: </strong>The username for accessing the api.</li>'
		+ '  <li><strong>password: </strong>The password of the user.</li>'
		+ '</ul>'
		+ '<p>To use the APIs, add the cookie you have received to the HTTP Header "Cookie" of the requests. For example:</p>'
		+ '<pre class="cfwApiOverviewCode"><code>Cookie: CFWSESSIONID=node01ab2c3d4e5f61xhc7f6puqsab1</code></pre>'
	
	html += '<p>Here is an example login request using curl that will return the SessionID string:</p>'
			+'<pre class="cfwApiOverviewCode"><code>curl -X POST --data \'username=apiUser&password=apiUserPW\' \'http://localhost:8888/cfw/apilogin\'</code></pre>';
	
	var cookie = JSDATA.id;
	html += '<p>Afterwards you can use the SessionID to access the API, here is an example. You can create examples for each API using the :</p>';
	html += '<pre class="cfwApiOverviewCode"><code>curl -H "Cookie: CFWSESSIONID='+cookie+'" -X GET \''+window.location.href+'?apiName=User&actionName=fetchData\'</code></pre>';
	

	
	//---------------------------
	// Create Panel
	
	 var panelSettings = {
			cardstyle: 'cfw-blue',
			textstyle: null,
			textstyleheader: 'white',
			title: "Login and Usage",
			body: html,
	};
	
	var cfwPanel = new CFWPanel(panelSettings);
	cfwPanel.appendTo(parent);
	
	//---------------------------
	// Highlight Code Blocks
	var codeblocks = $(".cfwApiOverviewCode");
	hljs.highlightBlock(codeblocks.get(0));
	hljs.highlightBlock(codeblocks.get(1));
	hljs.highlightBlock(codeblocks.get(2));
}
/******************************************************************
 * Print the overview of the apis .
 * 
 ******************************************************************/
function cfw_apioverview_printOverview(data){
	
	parent = $("#cfw-container");
	
	parent.append("<h1>API Overview</h1>");
	
	if(CFW.hasPermission('API Token Managment')){
		var managementButton = $('<a class="btn btn-sm btn-primary mb-2" role="button" href="./api/tokenmanagement" >'
				+ '<i class="fas fa-ticket-alt mr-1"></i>Manage Tokens</button>');
	
		parent.append(managementButton);
	}
	
	
	if(data.payload != undefined){
		cfw_apioverview_printLoginPanel(parent);
		//--------------------------------
		// Initialization
		var panels = {}
		var count = data.payload.length;
		if(count == 0){
			CFW.ui.addAlert("info", "Hmm... seems there aren't any APIs in the list.");
		}

		//--------------------------------
		// Create Data Structure
		for(var i = 0; i < count; i++){
			var current = data.payload[i];
			var name = current.name;
			var action = current.action;
			if(panels[name] == undefined){
				panels[name] = {}
			}
			
			if(panels[name][action] == undefined){
				panels[name][action] = {
						description: current.description,
						params: current.params,
						returnValues: current.returnValues
				}
			}else{
				CFW.ui.addToastDanger("The action '"+action+"' seems to be defined multiple times for the same API name: '"+name+"'");
			}
		}
		
		//--------------------------------
		// Create Panels
		
		for(name in panels){
			 var current = panels[name];
			
			 var panelSettings = {
						cardstyle: 'cfw-blue',
						textstyle: null,
						textstyleheader: 'white',
						title: name,
						body: $('<div>'),
				};

			for(action in current){
				var sub = current[action];
				
				//----------------------------------------
				// Create Panel Content
				//----------------------------------------
				var content = $('<div>');
				content.append('<p>'+sub.description+'</p>');
				
				
				//----------------------------
				// Create Parameter Table
				
				if(sub.params != undefined && sub.params.length > 0){
					content.append('<h3>Parameters:</h3>');
					
					var cfwTable = new CFWTable({filterable: false, narrow: true});

					cfwTable.addHeaders(['Name','Type','Description']);
					
					var htmlRows = '';
					for(let j = 0; j < sub.params.length; j++){
						//{"name": "pk_id", "type": "Integer", "description": "null"}
						var paramDef = sub.params[j];
						htmlRows += '<tr>'
						htmlRows += '<td>'+paramDef.name+'</td>';
						htmlRows += '<td>'+paramDef.type+'</td>';
						htmlRows += '<td>'+((paramDef.description != "null") ? paramDef.description : '') +'</td>';
						htmlRows += '</tr>'
						
					}
	
					cfwTable.addRows(htmlRows);
					cfwTable.appendTo(content);
				}
				//----------------------------
				// Create Return Value
				if(sub.returnValues != undefined && sub.returnValues.length > 0){
					content.append('<h3>Return Values:</h3>');
					
					var returnTable = new CFWTable({filterable: false, narrow: true});
					
					returnTable.addHeaders(['Name','Type','Description']);
					
					htmlRows = '';
					for(let j = 0; j < sub.returnValues.length; j++){
						//{"name": "pk_id", "type": "Integer", "description": "null"}
						var returnValue = sub.returnValues[j];
						htmlRows += '<tr>'
						htmlRows += '<td>'+returnValue.name+'</td>';
						htmlRows += '<td>'+returnValue.type+'</td>';
						htmlRows += '<td>'+((returnValue.description != "null") ? returnValue.description : '') +'</td>';
						htmlRows += '</tr>'
						
					}
	
					returnTable.addRows(htmlRows);
					returnTable.appendTo(content);
				}

				//----------------------------
				// Create Example Button
				content.append('<button class="btn btn-primary" onclick="cfw_apioverview_createExample(\''+name+'\', \''+action+'\')">Example</button>');
				
				//----------------------------------------
				// Create Panel
				//----------------------------------------
				 var subpanelSettings = {
							cardstyle: 'cfw-green',
							textstyle: null,
							textstyleheader: null,
							title: action,
							body: content,
					};
				var subPanel = new CFWPanel(subpanelSettings);
				subPanel.appendTo(panelSettings.body);

			}
			
			new CFWPanel(panelSettings).appendTo(parent);
		}


	}else{
		CFW.ui.addToastDanger('Something went wrong and no APIs can be displayed.');
		
	}
}

/******************************************************************
 * Main method for building the view.
 * 
 ******************************************************************/
function cfw_apioverview_draw(){
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){

		CFW.http.fetchAndCacheData("./api", {overviewdata: "fetch"}, "api_definitions", cfw_apioverview_printOverview);
		
		CFW.ui.toogleLoader(false);
	}, 100);
}