package com.pengtoolbox.cfw.datahandling;

import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.cfw.response.bootstrap.HierarchicalHTMLItem;


/**************************************************************************************************************
 * Class for creating a form using CFWFields or a CFWObject as a template.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWForm extends HierarchicalHTMLItem {
	
	public static final String FORM_ID = "cfw-formID";
	private String formID = "";
	private String submitLabel = "";
	private String postURL;
	private String resultCallback;
	
	private CFWObject origin;
	public StringBuilder javascript = new StringBuilder();
	
	// Contains the fields with field name as key
	public LinkedHashMap<String, CFWField<?>> fields = new LinkedHashMap<String, CFWField<?>>();
	
	private CFWFormHandler formHandler = null;
	private boolean isAPIForm = false;
	private boolean isEmptyForm = false;
	
	public CFWForm(String formID, String submitLabel) {
		
		if(formID.matches(".*[^A-Za-z0-9]+.*")) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Don't use any other characters for formIDs than A-Z, a-z and 0-9: '"+formID+"'");
		}
		this.formID = formID;
		this.submitLabel = submitLabel;
		
		CFWField<String> formIDField = CFWField.newString(FormFieldType.HIDDEN, CFWForm.FORM_ID);
		formIDField.setValueValidated(this.formID);
		this.addChild(formIDField);
		
		// Default post to servlet creating the form
		postURL = CFW.Context.Request.getRequest().getRequestURI();
		
		CFW.Context.Session.addForm(this);
	}
	
	public CFWForm(String formID, String submitLabel, CFWObject origin) {
		this(formID, submitLabel);
		this.addFields(origin.getFields().values().toArray(new CFWField[]{}));
		this.origin = origin;
	}
	
	/***********************************************************************************
	 * Create the HTML representation of this item.
	 * @return String html for this item. 
	 ***********************************************************************************/
	protected void createHTML(StringBuilder html) {
		
		//---------------------------
		// Resolve onClick action
		String onclick = "cfw_postForm('"+postURL+"', '#"+formID+"', "+resultCallback+")";
		if(this.getAttributes().containsKey("onclick")) {
			onclick = this.getAttributeValue("onclick");
			this.removeAttribute("onclick");
		}
		
		//---------------------------
		// Create HTML
		html.append("<form id=\""+formID+"\" class=\"form\" method=\"post\" "+getAttributesString()+">");
		
		if(this.hasChildren()) {
				
			for(HierarchicalHTMLItem child : children) {
				html.append("\n\t"+child.getHTML());
			}
		}
		
		if(this.hasOneTimeChildren()) {
			
			for(HierarchicalHTMLItem child : oneTimeChildren) {
				html.append("\n\t"+child.getHTML());
			}
		}
		
		//---------------------------
		// Create Submit Button
		//html.append("<input id=\""+formID+"-submitButton\" type=\"button\" onclick=\""+onclick+"\" class=\"form-control btn-primary mt-2\" value=\""+submitLabel+"\">");
		html.append("<button id=\""+formID+"-submitButton\" type=\"button\" onclick=\""+onclick+"\" class=\"form-control btn-primary mt-2\">"+submitLabel+"</button>");
		
		//---------------------------
		// Add javascript
		html.append(
				"<script id=\"script-"+formID+"\">\r\n" + 
				"	function intializeForm_"+formID+"(){\r\n"+
				"		$('[data-toggle=\"tooltip\"]').tooltip();\r\n"+		
						javascript.toString()+
				"	}\r\n" + 
				"	window.addEventListener('DOMContentLoaded', function() {\r\n" + 
				"       intializeForm_"+formID+"();"+
				"});\r\n"+
				"</script>"
				);
		
		html.append("</form>");
	}	

	public String getLabel() {
		return formID;
	}

	
	public void addField(CFWField<?> field) {
		
		if(!fields.containsKey(field.getName())) {
			fields.put(field.getName(), field);
		}else {
			System.out.println("CFWField.addField()");
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The field with name '"+field.getName()+"' was already added to the object.");
		}
		
		this.addChild(field);
	}
	
	public void addFields(CFWField<?>[] fields) {
		for(CFWField<?> field : fields) {
			this.addField(field);
		}
	}
	
	/***********************************************************************************
	 * Returns a hashmap with fields. The keys are the names of the fields.
	 ***********************************************************************************/
	public LinkedHashMap<String, CFWField<?>> getFields() {
		return fields;
	}
	public String getFormID() {
		return formID;
	}

	public CFWForm setLabel(String label) {
		fireChange();
		this.formID = label;
		return this;
	}
	
	public CFWForm setFormHandler(CFWFormHandler formHandler) {
		fireChange();
		postURL = "/cfw/formhandler";
		this.formHandler = formHandler;
		return this;
	}
	
	public CFWFormHandler getFormHandler() {
		return formHandler;
	}
	
	public CFWField<?> getField(String name) {
		return fields.get(name);
	}
	
	public CFWObject getOrigin() {
		return origin;
	}

	public void setOrigin(CFWObject origin) {
		this.origin = origin;
	}
	
	
	public void setResultCallback(String resultCallback) {
		this.resultCallback = resultCallback;
	}

	public void isAPIForm(boolean isAPIForm) {
		this.isAPIForm = isAPIForm;
		this.isEmptyForm = true;
	}
	
	public boolean isAPIForm() {
		return isAPIForm;
	}
	
	public void isEmptyForm(boolean isEmptyForm) {
		this.isEmptyForm = isEmptyForm;
	}
	
	public boolean isEmptyForm() {
		return isEmptyForm ;
	}

	public boolean mapRequestParameters(HttpServletRequest request) {
		return CFWField.mapAndValidateParamsToFields(request, fields);
	}
	
	public void appendToPayload(JSONResponse json) {
    	JsonObject payload = new JsonObject();
    	payload.addProperty("html", this.getHTML());
    	
    	json.getContent().append(payload.toString());
	}
	
	public String getFieldsAsKeyValueString() {
		
		StringBuilder builder = new StringBuilder();
		
		for(CFWField<?> field : fields.values()) {
			builder.append("\n")
			.append(field.getName())
			.append(": ");
			if(!(field.getValue() instanceof Object[])) {
				builder.append(field.getValue());
			}else {
				builder.append(Arrays.toString((Object[])field.getValue()));
			}
			
		}

		return builder.toString();
	}
	
	public String getFieldsAsKeyValueHTML() {
		
		StringBuilder builder = new StringBuilder();
		
		for(CFWField<?> field : fields.values()) {
			builder.append("<br/>")
			.append(field.getName())
			.append(": ")
			.append(field.getValue());
		}

		return builder.toString();
	}

}
