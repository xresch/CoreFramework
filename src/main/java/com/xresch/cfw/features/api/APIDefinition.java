package com.xresch.cfw.features.api;

import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWArrayUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class APIDefinition {
	
	private static final Logger logger = CFWLog.getLogger(APIDefinition.class.getName());
	
	@Expose()
	@SerializedName("name")
	private String apiName;
	
	@Expose()
	@SerializedName("action")
	private String actionName;
	
	@Expose()
	@SerializedName("description")
	private String description;

	@Expose()
	@SerializedName("params")
	private String[] inputFieldnames = new String[] {};
	
	@Expose()
	@SerializedName("returnVales")
	private String[] outputFieldnames = new String[] {};
	

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
		this.inputFieldnames = inputFieldnames;
		this.outputFieldnames = outputFieldnames;
		
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
	
	
	public void setInputFieldnames(String[] inputFieldnames) {
		this.inputFieldnames = inputFieldnames;
	}

	public void addInputFieldname(String inputFieldname) {
		inputFieldnames = CFWArrayUtils.add(inputFieldnames, inputFieldname);
	}

	public String[] getInputFieldnames() {
		return inputFieldnames;
	}

	public void setOutputFieldnames(String[] outputFieldnames) {
		this.outputFieldnames = outputFieldnames;
	}
	
	public void addOutputFieldname(String outputFieldname) {
		outputFieldnames = CFWArrayUtils.add(outputFieldnames, outputFieldname);
	}
	
	public String[] getOutputFieldnames() {
		return outputFieldnames;
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
		
		//-----------------------------------
		//resolve parameters
		if(inputFieldnames != null && instance != null) {
			JsonArray params = new JsonArray();
			object.add("params", params);
			
			for(String name : inputFieldnames) {
				CFWField<?> field = instance.getField(name);
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
		if(outputFieldnames != null) {
			JsonArray returnValues = new JsonArray();
			object.add("returnValues", returnValues);
			
			for(String name : outputFieldnames) {
				CFWField<?> field = instance.getField(name);
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
