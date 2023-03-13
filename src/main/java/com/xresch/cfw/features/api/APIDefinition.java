package com.xresch.cfw.features.api;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class APIDefinition {
	
	private static final Logger logger = CFWLog.getLogger(APIDefinition.class.getName());
	
	private String apiName;
	private String actionName;
	private String description;
	private String bodyParamName;

	// Fieldname and Field
	private LinkedHashMap<String, CFWField> inputFields = new LinkedHashMap<>();
	private LinkedHashMap<String, CFWField> outputFields = new LinkedHashMap<>();
	
	private Class<? extends CFWObject> clazz;
	
	private CFWObject instance;
	
	private APIRequestHandler requestHandler;

	public APIDefinition(			  
			  Class<? extends CFWObject> clazz,
			  String apiName, 
			  String actionName, 
			  String[] inputFieldnames,
			  String[] outputFieldnames) {
		
		this.apiName = apiName;
		this.actionName = actionName;
		this.clazz = clazz;

		try {
			instance = createObjectInstance();
			
			//----------------------------------------------
			// Add input fields
			if(inputFieldnames != null) {
				for(String name : inputFieldnames) {
					CFWField field = instance.getField(name);
					if(field != null) {
						inputFields.put(field.getName(), field);
					}
				}
			}
			//----------------------------------------------
			// Add output fields
			if(outputFieldnames != null) {
				for(String name : outputFieldnames) {
					CFWField field = instance.getField(name);
					if(field != null) {
						outputFields.put(field.getName(), field);
					}
				}
			}
		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Could not create instance for '"+clazz.getSimpleName()+"'. Check if you have a constructor without parameters.", e);
			return;
		}
		
	}
	
	public APIDefinition(			  
			  Class<? extends CFWObject> clazz,
			  String apiName, 
			  String actionName, 
			  LinkedHashMap<String, CFWField> inputFields,
			  LinkedHashMap<String, CFWField> outputFields) {
		
		this.apiName = apiName;
		this.actionName = actionName;
		this.clazz = clazz;
		this.inputFields = inputFields;
		this.outputFields = outputFields;
		
		try {
			instance = createObjectInstance();

		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Could not create instance for '"+clazz.getSimpleName()+"'. Check if you have a constructor without parameters.", e);
			return;
		}
	}
	
	public APIDefinition(			  
			  Class<? extends CFWObject> clazz,
			  String apiName, 
			  String actionName, 
			  CFWObject inputFieldsObject,
			  CFWObject outputFieldsObject) {
		
		this.apiName = apiName;
		this.actionName = actionName;
		this.clazz = clazz;
		this.inputFields = inputFieldsObject.getFields();
		this.outputFields = outputFieldsObject.getFields();
		
		try {
			instance = createObjectInstance();

		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Could not create instance for '"+clazz.getSimpleName()+"'. Check if you have a constructor without parameters.", e);
			return;
		}
	}
	
	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	
	public String getFullyQualifiedName() {
		return apiName+"-"+actionName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getPostBodyParamName() {
		return bodyParamName;
	}

	/********************************************************************************
	 * Set the bodyParam name to support Post requests with body contents.
	 * If the param is provided as a request parameter, the contents of the parameter
	 * are read and provided to the method {@link com.xresch.cfw.features.api.APIRequestHandler#handleRequest()}
	 * as the argument "body".
	 * If the value is not given as a request parameter, the request body is read. 
	 ********************************************************************************/
	public void setPostBodyParamName(String bodyParamName) {
		this.bodyParamName = bodyParamName;
	}

	public Class<? extends CFWObject> getObjectClass() {
		return clazz;
	}
	
	/********************************************************************************
	 * Return the object providing the input fields for the API.
	 * @return CFWObject or null
	 ********************************************************************************/
	public CFWObject createObjectInstance() {
		CFWObject object = null;
		try {
			if(this.clazz != null) {
				object = this.clazz.newInstance();
			}
		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Could not create instance for '"+getObjectClass().getSimpleName()+"'. Check if you have a constructor without parameters.", e);
		}
		return object;
	}
	


	public void setObject(Class<? extends CFWObject> clazz) {
		this.clazz = clazz;
	}
	
	@SuppressWarnings("rawtypes")
	public void addInputFields(CFWField... fields) {
		for(CFWField field : fields) {
			inputFields.put(field.getName(), field);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void addOutputFields(CFWField... fields) {
		for(CFWField field : fields) {
			outputFields.put(field.getName(), field);
		}
	}
	
	public String[] getInputFieldnames() {
		return inputFields.keySet().toArray(new String[]{});
	}


	public String[] getOutputFieldnames() {
		return outputFields.keySet().toArray(new String[]{});
	}

	public APIRequestHandler getRequestHandler() {
		return requestHandler;
	}

	public void setRequestHandler(APIRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	public JsonObject getJSON() {
			
		JsonObject object = new JsonObject();
		object.addProperty("name", apiName);
		object.addProperty("action", actionName);
		object.addProperty("description", description);
		object.addProperty("bodyParamName", bodyParamName);
		
		//-----------------------------------
		//resolve parameters
		if(instance != null) {
			JsonArray params = new JsonArray();
			object.add("params", params);
			
			for(CFWField<?> field : inputFields.values().toArray(new CFWField[] {})) {
				//ignore unavailable fields that where added to the base CFWObject
				if(field != null) {
					JsonObject paramObject = new JsonObject();
					paramObject.addProperty("name", field.getName());
					paramObject.addProperty("type", field.getValueClass().getSimpleName());
					paramObject.addProperty("description", field.getDescription());
					params.add(paramObject);
				}
			}
		}
		
		//-----------------------------------
		// Resolve Return Values
		if(instance != null) {
			JsonArray returnValues = new JsonArray();
			object.add("returnValues", returnValues);
			
			for(CFWField<?> field : outputFields.values().toArray(new CFWField[] {})) {
				//ignore unavailable fields that where added to the base CFWObject
				if(field != null) {
					JsonObject returnValueObject = new JsonObject();
					returnValueObject.addProperty("name", field.getName());
					returnValueObject.addProperty("type", field.getValueClass().getSimpleName());
					returnValueObject.addProperty("description", field.getDescription());
					returnValues.add(returnValueObject);
				}
			}
		}

		return object;
	}
	
	
	
	

}
