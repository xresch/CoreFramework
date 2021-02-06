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
import com.xresch.cfw.response.bootstrap.HierarchicalHTMLItem;


/**************************************************************************************************************
 * Class for creating a form using CFWFields or a CFWObject as a template.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWFormMulti extends CFWForm {
	
	private static Logger logger = CFWLog.getLogger(CFWFormMulti.class.getName());
	
	public static final String FORM_ID = "cfw-formID";
	private String postURL;
	private String resultCallback;

	public StringBuilder javascript = new StringBuilder();
	
	// Contains all the CFWObjects fields with primaryKey as key
	@SuppressWarnings("rawtypes")
	public LinkedHashMap<Integer, CFWObject> originsMap;
	
	private CFWObject firstObject;
	
	private CFWFormHandler formHandler = null;
	
	public CFWFormMulti(String formID, String submitLabel, ArrayList<CFWObject> origins) {
		
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
		html.append("<table class=\"table table-sm table-striped\">");
		
		//---------------------------
		// Create Table Header
		html.append("<thead><tr>");
			for(Entry<String, CFWField> entry : firstObject.getFields().entrySet()) {
				CFWField currentField = entry.getValue();
				if(currentField.fieldType() != FormFieldType.NONE && currentField.fieldType() != FormFieldType.HIDDEN ) {
					html.append("<th>"+entry.getValue().getLabel()+"</th>");
				}
			}
		html.append("</tr></thead>");
		
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
						for(Entry<String, CFWField> fieldEntry : currentObject.getFields().entrySet()) {
							CFWField field = fieldEntry.getValue();
							// Make every field name unique by prepending id
							field.setName(objectID+"-"+field.getName());
							field.addCssClass("form-control-sm");

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
	 * Returns a hashmap with fields. The keys are the names of the fields.
	 ***********************************************************************************/	
	
	public CFWFormMulti setFormHandler(CFWFormHandler formHandler) {
		fireChange();
		postURL = "/cfw/formhandler";
		this.formHandler = formHandler;
		return this;
	}
	
	public CFWFormHandler getFormHandler() {
		return formHandler;
	}
		
	public LinkedHashMap<Integer, CFWObject> getOrigins() {
		return originsMap;
	}

	public void setOrigins(LinkedHashMap<Integer, CFWObject> originsMap) {
		this.originsMap = originsMap;
	}
	
	
	public boolean mapRequestParameters(HttpServletRequest request) {
		// TODO for multi
		return CFWField.mapAndValidateParamsToFields(request, fields);
	}
	
	public void appendToPayload(JSONResponse json) {
    	JsonObject payload = new JsonObject();
    	payload.addProperty("html", this.getHTML());
    	
    	json.getContent().append(payload.toString());
	}
	
	
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
