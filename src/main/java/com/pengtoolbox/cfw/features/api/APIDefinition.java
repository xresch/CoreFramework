package com.pengtoolbox.cfw.features.api;

import java.util.logging.Logger;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.utils.CFWArrayUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class APIDefinition {
	
	public static Logger logger = CFWLog.getLogger(APIDefinition.class.getName());
	
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
				.method("APIDefinition.<init>")
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
				.method("handleRequest")
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

	public String getJSON() {
			
		StringBuilder json = new StringBuilder("{");
		json.append("\"name\"").append(": \"").append(apiName)
			.append("\", \"action\"").append(": \"").append(actionName)
			.append("\", \"description\"").append(": \"").append(description).append("\"");
		
		//-----------------------------------
		//resolve parameters
		if(inputFieldnames != null && instance != null) {
			json.append(", \"params\"").append(": [");
			
			int counter = 0;
			for(String name : inputFieldnames) {
				CFWField<?> field = instance.getField(name);
				//ignore unavailable fields that where added to the base CFWOBject
				if(field != null) {
					counter++;
					json.append("{\"name\"").append(": \"").append(field.getName())
					.append("\", \"type\"").append(": \"").append(field.getValueClass().getSimpleName())
					.append("\", \"description\"").append(": \"").append(field.getDescription()).append("\"},");
				}
			}
			
			if(counter > 0) {
				json.deleteCharAt(json.length()-1); //remove last comma
			}
			json.append("]");
		}
		
		//-----------------------------------
		//resolve parameters
		if(outputFieldnames != null) {
			json.append(", \"returnValues\"").append(": [");
			
			int counter = 0;
			for(String name : outputFieldnames) {
				CFWField<?> field = instance.getField(name);
				if(field != null) {
					counter++;
					json.append("{\"name\"").append(": \"").append(field.getName())
					.append("\", \"type\"").append(": \"").append(field.getValueClass().getSimpleName())
					.append("\", \"description\"").append(": \"").append(field.getDescription()).append("\"},");
				}
			}
			
			if(counter > 0) {
				json.deleteCharAt(json.length()-1); //remove last comma
			}
			
			json.append("]");
			
		}
		
		json.append("}");
		return json.toString();
	}
	
	
	
	

}
