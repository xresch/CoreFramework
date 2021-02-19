package com.xresch.cfw.datahandling;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.response.bootstrap.HierarchicalHTMLItem;


/**************************************************************************************************************
 * Class for creating a form using CFWFields or a CFWObject as a template.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWForm extends HierarchicalHTMLItem {
	
	private static Logger logger = CFWLog.getLogger(CFWForm.class.getName());
	
	public static final String FORM_ID = "cfw-formID";
	protected String formID = "";
	protected String submitLabel = "";
	protected String postURL;
	protected String resultCallback;
	
	private CFWObject origin;
	protected StringBuilder javascript = new StringBuilder();
	
	// Contains the fields with field name as key
	@SuppressWarnings("rawtypes")
	public LinkedHashMap<String, CFWField> fields = new LinkedHashMap<String, CFWField>();
	
	private CFWFormHandler formHandler = null;
	private CFWFormCustomAutocompleteHandler autocompleteHandler = null;
	private boolean isAPIForm = false;
	private boolean isEmptyForm = false;
	private boolean isInlineForm = false;
	/***********************************************************************************
	 * Initialize the form
	 * @param formUniqueName used for caching it in the users session.
	 * @param submitLabel for the button, set to null if no button should be displayed 
	 ***********************************************************************************/
	public CFWForm(String formUniqueName, String submitLabel) {
		
		if(formUniqueName.matches(".*[^A-Za-z0-9]+.*")) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Don't use any other characters for formIDs than A-Z, a-z and 0-9: '"+formUniqueName+"'");
		}
		this.formID = formUniqueName;
		this.submitLabel = submitLabel;
		
		CFWField<String> formIDField = CFWField.newString(FormFieldType.HIDDEN, CFWForm.FORM_ID);
		formIDField.setValueValidated(this.formID);
		this.addChild(formIDField);
		
		// Default post to servlet creating the form
		postURL = CFW.Context.Request.getRequest().getRequestURI();
		
		CFW.Context.Session.addForm(this);
	}
	
	/***********************************************************************************
	 * Initialize the form.
	 * @param formUniqueName used for caching it in the users session.
	 * @param submitLabel for the button, set to null if no button should be displayed 
	 * @param CFWObject to use the fields of to create the form
	 ***********************************************************************************/
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
		String onclick = "cfw_internal_postForm('"+postURL+"', '#"+formID+"', "+resultCallback+")";
		if(this.getAttributes().containsKey("onclick")) {
			onclick = this.getAttributeValue("onclick");
			this.removeAttribute("onclick");
		}
		
		//---------------------------
		// Create HTML
		String formClasses = "form";
		if(isInlineForm) { formClasses += "-inline align-items-start"; }
		formClasses += " "+this.getAttributeValue("class");
		
		html.append("<form id=\""+formID+"\" class=\""+formClasses+"\" method=\"post\" "+getAttributesString()+">");
		
		if(this.hasChildren()) {
				
			for(HierarchicalHTMLItem child : children) {
				if(child instanceof CFWField) {
					((CFWField) child).createHTML_LabeledFormField(this, html);
				}else {
					html.append("\n\t"+child.getHTML());
				}
			}
		}
		
		if(this.hasOneTimeChildren()) {
			
			for(HierarchicalHTMLItem child : oneTimeChildren) {
				html.append("\n\t"+child.getHTML());
			}
		}
		
		//---------------------------
		// Create Submit Button
		if(submitLabel != null) {
			if(!isInlineForm) {
				html.append("<button id=\""+formID+"-submitButton\" type=\"button\" onclick=\""+onclick+"\" class=\"form-control btn-primary mt-2\">"+submitLabel+"</button>");
			}else {
				html.append(
					"<div class=\"d-flex flex-column align-items-start ml-3\">  "
						+"<label class=\"\" for=\"chart_type2\">&nbsp;</label> "
						+"<div class=\"cfw-field-wrapper flex-grow-1 \">"
							+ "<div class=\"btn btn-sm btn-primary\" onclick=\""+onclick+"\">Update</div>"
						+ "</div>"
					+ "</div>");
			}
		}
		//---------------------------
		// Add javascript
		html.append(
				"<script id=\"script-"+formID+"\">\r\n" + 
				"	function intializeForm_"+formID+"(){\r\n"+
				"		$('[data-toggle=\"tooltip\"]').tooltip();\r\n"+		
				"		"+javascript.toString()+
				"	}\r\n" + 
				"	window.addEventListener('DOMContentLoaded', function() {\r\n" + 
				"		intializeForm_"+formID+"();"+
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
			new CFWLog(logger)
				.warn("The field with name '"+field.getName()+"' was already added to the object.", new Throwable());
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
	@SuppressWarnings("rawtypes")
	public LinkedHashMap<String, CFWField> getFields() {
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
	
	public CFWForm setCustomAutocompleteHandler(CFWFormCustomAutocompleteHandler autocompleteHandler) {
		this.autocompleteHandler = autocompleteHandler;
		return this;
	}
	
	public CFWFormCustomAutocompleteHandler getCustomAutocompleteHandler() {
		return autocompleteHandler;
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
	
	public void isInlineForm(boolean isInlineForm) {
		this.isInlineForm = isInlineForm;
	}
	
	public boolean isInlineForm() {
		return isInlineForm ;
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
