package com.xresch.cfw.tests.features.contextsettings;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class TestMockupContextSettings extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "TestMockupContextSettings Type";
	private String apiURL = null;
	
	public enum TestMockupContextSettingsFields{
		HOST,
		PORT,
		USE_HTTPS
	}
			
	private CFWField<String> host = CFWField.newString(FormFieldType.TEXT, TestMockupContextSettingsFields.HOST)
			.setDescription("The hostname of the this example instance.");
	
	private CFWField<Integer> port = CFWField.newInteger(FormFieldType.NUMBER, TestMockupContextSettingsFields.PORT)
			.setDescription("The port for this example environment.");
	
	private CFWField<Boolean> useHttps = CFWField.newBoolean(FormFieldType.BOOLEAN, TestMockupContextSettingsFields.USE_HTTPS)
			.setDescription("Use HTTPS for this example environment.");
	
	public TestMockupContextSettings() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(host, port, useHttps);
	}
		
		
	@Override
	public boolean isDeletable(int settingsID) {
		// add your check here if required
		return true;
	}
	
	public boolean isDefined() {
		if(host.getValue() != null
		&& port.getValue() != null) {
			return true;
		}
		
		return false;
	}
	
	
	public String getExampleUrl() {
		
		if(apiURL == null) {
			StringBuilder builder = new StringBuilder();
			
			if(useHttps.getValue()) {
				builder.append("https://");
			}else {
				builder.append("http://");
			}
			builder.append(host.getValue())
				.append(":")
				.append(port.getValue())
				.append("/api/v1");
			
			apiURL = builder.toString();
		}
		
		return apiURL;
	}
	
	public String host() {
		return host.getValue();
	}
	
	public TestMockupContextSettings host(String value) {
		this.host.setValue(value);
		return this;
	}
		
	public int port() {
		return port.getValue();
	}
	
	public TestMockupContextSettings port(int value) {
		this.port.setValue(value);
		return this;
	}
	
	public boolean useHttps() {
		return useHttps.getValue();
	}
	
	public TestMockupContextSettings useHttps(boolean value) {
		this.useHttps.setValue(value);
		return this;
	}
		
}
