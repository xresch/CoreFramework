package com.xresch.cfw.features.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletConfiguration extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletConfiguration.class.getName());
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
				
		HTMLResponse html = new HTMLResponse("Configuration Management");
		
		StringBuilder content = html.getContent();
		
		if(CFW.Context.Request.hasPermission(FeatureConfig.PERMISSION_CONFIGURATION)) {
			
			//--------------------------
			// Add Form
			content.append("<h1>Configuration Management</h1>");
			content.append("<p>Here you can adjust the application configuration. Changes will affect the overall application behavior and therefore all users(e.g. Look and Feel settings). </p>");
			CFWForm configForm = createConfigForm();
			content.append(configForm.getHTML());
			
			//--------------------------
			// Add Javascript
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureConfig.RESOURCE_PACKAGE, "cfw_config.js");
			html.addJavascriptData("categories", CFW.JSON.toJSON(CFW.DB.Config.getCategories()) );
			html.addJavascriptCode("cfw_config_changeToPanels();");

			
			
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			
		}else {
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	private static CFWForm createConfigForm() {
		
		// id must not start with number, needed to make jquery selectors working properly
		String ID_PREFIX = "id-";
		
		//--------------------------------------
		// Create Group Form
		
		CFWForm configForm = new CFWForm("cfwConfigMgmt", "Save");
		configForm.addAttribute("autocomplete", "off");
		ArrayList<CFWObject> configObjects = CFW.DB.Config.getConfigObjectList();
		for(CFWObject object :  configObjects) {
			Configuration config = (Configuration)object;
			CFWField<String> field = CFWField.newString(FormFieldType.valueOf(config.type()), ID_PREFIX+config.id());
			field.setLabel(config.name());
			field.setValue(config.value());
			field.setDescription(config.description());
			field.setOptions(config.options());		
			field.disableSanitization();
			field.addAttribute("data-category", config.category());
			
			configForm.addField(field);
		}
		
		configForm.setFormHandler(new CFWFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
								
					form.mapRequestParameters(request);
					
					JSONResponse json = new JSONResponse();
			    	
			    	boolean success = true;
			    	
			    	for(CFWField<?> field : form.getFields().values() ) {
			    		String value = (field.getValue() != null) ? field.getValue().toString() : "";
			    		String id = field.getName().replaceFirst(ID_PREFIX, "");
			    		success = success && CFW.DB.Config.updateValue(Integer.parseInt(id), value);
			    	}
			    	CFW.DB.Config.updateCache();
			    	
			    	if(success) {
			    		json.addAlert(MessageType.SUCCESS, "Saved!");
			    	}else {
			    		json.addAlert(MessageType.ERROR, "Something went wrong!");
			    	}
			    	
				}
				
			}
		);
		
		return configForm;
	}
	
}