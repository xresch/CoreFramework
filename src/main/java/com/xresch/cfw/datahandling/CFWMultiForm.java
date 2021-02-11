package com.xresch.cfw.datahandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;


/**************************************************************************************************************
 * Class for creating a form for multiple CFWObjects.
 * CAUTION:
 * <ul>
 *   <li>Adjusts the fieldnames of the CFWObjects by prepending '{ID}-'.</li>
 *   <li>Do not save adjusted CFWObjects(e.g by calling {@link CFWObject#update()}) to the database, it will fail.</li>
 *   <li>Fieldnames are reverted by using {@link #revertFieldNames()}.</li>
 *   <li>In case you want to be able to save the same form multiple times, make the fieldnames unique again by using {@link #makeFieldNamesUnique()}.</li>
 * </ul>  
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 * 
 **************************************************************************************************************/
public class CFWMultiForm extends CFWForm {
	
	private static Logger logger = CFWLog.getLogger(CFWMultiForm.class.getName());
	
	public static final String FORM_ID = "cfw-formID";

	// Contains all the CFWObjects fields with primaryKey as key
	@SuppressWarnings("rawtypes")
	public LinkedHashMap<Integer, CFWObject> originsMap;
	
	private CFWObject firstObject;
	
	private CFWMultiFormHandler formHandler = null;
	
	public CFWMultiForm(String formID, String submitLabel, ArrayList<CFWObject> origins) {
		
		//---------------------------------------
		// Initialize
		super(formID, submitLabel);
		
		if(origins.size() == 0) {
			new CFWLog(logger).severe("Origins cannot be an empty list.", new IllegalArgumentException());
			return;
		}
		
		//---------------------------------------
		// Create map
		originsMap = new LinkedHashMap<>();
		for(CFWObject object : origins) {
			originsMap.put(object.getPrimaryKey(), object);
		}
		
		//---------------------------------------
		// Get Details from first object
		CFWObject firstOrigin = origins.get(0);
		firstObject = firstOrigin;
		//super.addFields(firstOrigin.getFields().values().toArray(new CFWField[]{}));
		
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
		
		//########################################################
		// Create HTML
		//########################################################
		html.append("<form id=\""+formID+"\" class=\"form\" method=\"post\" "+getAttributesString()+">");
		CFWField<String> formIDField = CFWField.newString(FormFieldType.HIDDEN, CFWForm.FORM_ID);
		formIDField.setValueValidated(this.formID);
		formIDField.createHTML(html);
		
		html.append("<table class=\"table table-sm table-striped\">");
		
		//---------------------------
		// Create Table Header
		html.append("<thead><tr>");
			for(Entry<String, CFWField> entry : firstObject.getFields().entrySet()) {
				CFWField currentField = entry.getValue();
								
				if(currentField.fieldType() != FormFieldType.NONE && currentField.fieldType() != FormFieldType.HIDDEN ) {
					html.append("<th><div class=\"d-flex\">");
					currentField.createDecoratorArea(html, currentField.fieldType());
					html.append("<span>"+entry.getValue().getLabel()+"<span>")
						.append("</div></th>");
				}
			}
		html.append("</tr></thead>");
		
		//---------------------------
		// Make Fieldnames Unique
		makeFieldNamesUnique();
		
		//---------------------------
		// Create Table Body	
		html.append("<tbody>");
			for(Entry<Integer, CFWObject> entry : originsMap.entrySet()) {
				
				//-----------------------------------
				// Table Row for every object
				Integer objectID = entry.getKey();
				CFWObject currentObject = entry.getValue();
				if(firstObject.getClass() == currentObject.getClass() && objectID != null) {
					
					//-----------------------------------
					// Table Cell for each visible field
					html.append("<tr>");
						// Prevent concurrent modification exception
						CFWField[] fields =  currentObject.getFields().values().toArray(new CFWField[]{});
						for(CFWField field : fields) {
							
							//add as child so it will recognize this form as parent and add the javascript for initialization
							super.addField(field);
							
							//make the fields smaller to get a compact table
							field.addCssClass("form-control-sm");
							field.isDecoratorDisplayed(false);

							if(field.fieldType() != FormFieldType.NONE) {
								if(field.fieldType() == FormFieldType.HIDDEN ) {
									field.createHTML(html);
								}else {
									html.append("<td>");
									field.createHTML(html);
									html.append("</td>");
								}
							}
							
						}
					html.append("</tr>");
				}
			}
		html.append("</tbody>");
		html.append("</table>");
				
		//---------------------------
		// Create Submit Button
		html.append("<button id=\""+formID+"-submitButton\" type=\"button\" onclick=\""+onclick+"\" class=\"form-control btn-primary mt-2\">"+submitLabel+"</button>");
		
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
		new CFWLog(logger).severe("Development Bug: This method is not supported by CFWFormMulti", new IllegalAccessException());
	}
	
	public void addFields(CFWField<?>[] fields) {
		new CFWLog(logger).severe("Development Bug: This method is not supported by CFWFormMulti", new IllegalAccessException());
	}
	
	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public CFWMultiForm setMultiFormHandler(CFWMultiFormHandler formHandler) {
		fireChange();
		postURL = "/cfw/formhandler";
		this.formHandler = formHandler;
		return this;
	}
	
	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public CFWMultiFormHandler getMultiFormHandler() {
		
		return formHandler;
	}
	
	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public CFWMultiForm setFormHandler(CFWFormHandler formHandler) {
		new CFWLog(logger).severe("Development Bug: This method is not supported by CFWMultiForm, please use setMultiFormHandler(). ", new IllegalAccessException());
		return this;
	}
	
	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public CFWFormHandler getFormHandler() {
		new CFWLog(logger).severe("Development Bug: This method is not supported by CFWMultiForm, please use getMultiFormHandler(). ", new IllegalAccessException());
		return null;
	}
		
	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public LinkedHashMap<Integer, CFWObject> getOrigins() {
		return originsMap;
	}

	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public void setOrigins(LinkedHashMap<Integer, CFWObject> originsMap) {
		this.originsMap = originsMap;
	}
	
	/***********************************************************************************
	 * Maps all the form field to the adjusted origins CFWObjects.
	 * Reverts the prepdended ID from the fieldnames.
	 * @return true if all mapped successful, returns false if one or more validation
	 * failed or an error occured.
	 * 
	 ***********************************************************************************/	
	public boolean mapRequestParameters(HttpServletRequest request) {
		// TODO for multi
		boolean success = true;

		for (CFWObject currentObject : originsMap.values()) {
			System.out.println("before:"+currentObject.toJSON());
			success &= currentObject.mapRequestParameters(request);
			System.out.println("after:"+currentObject.toJSON());
		}
		return success;
	}
	
	/***********************************************************************************
	 * Reverts the fieldnames back by removing the prepended IDs.
	 ***********************************************************************************/	
	public void revertFieldNames() {
		for (CFWObject object : originsMap.values()) {
			// Prevent concurrent modification exception
			CFWField[] fields = object.getFields().values().toArray(new CFWField[]{});
			for(CFWField field : fields) {
				String nameWithID = field.getName();
				int index = nameWithID.indexOf("-");
				String nameWithoutID = nameWithID.substring(index+1);
				field.setName(nameWithoutID);
			}
		}
	}
	
	/***********************************************************************************
	 * Make fieldnames unique by prepending them with the ID of the related object.
	 ***********************************************************************************/	
	public void makeFieldNamesUnique() {
		
		for (CFWObject object : originsMap.values()) {
			// Prevent concurrent modification exception
			CFWField[] fields = object.getFields().values().toArray(new CFWField[]{});
			for(CFWField field : fields) {			
				field.setName(object.getPrimaryKey()+"-"+field.getName());
			}
		}
	}
	
	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public void appendToPayload(JSONResponse json) {
    	JsonObject payload = new JsonObject();
    	payload.addProperty("html", this.getHTML());
    	
    	json.getContent().append(payload.toString());
	}
	
	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public String getFieldsAsKeyValueString() {
		// TODO for multi
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
	
	/***********************************************************************************
	 * 
	 ***********************************************************************************/	
	public String getFieldsAsKeyValueHTML() {
		// TODO for multi
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
