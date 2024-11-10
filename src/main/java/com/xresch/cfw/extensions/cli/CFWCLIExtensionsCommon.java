package com.xresch.cfw.extensions.cli;

import java.util.LinkedHashMap;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.cfw.validation.NumberRangeValidator;

public class CFWCLIExtensionsCommon {
	
	public static final String PARAM_DIR 		= "WORKING_DIRECTORY";
	public static final String PARAM_COMMANDS 	= "COMMANDS";
	public static final String PARAM_ENV		= "JSON_ENV_VARIABLES";
	public static final String PARAM_HEAD		= "HEAD";
	public static final String PARAM_TAIL		= "TAIL";
	public static final String PARAM_COUNT_SKIPPED	= "COUNT_SKIPPED";
	public static final String PARAM_TIMEOUT 	= "TIMEOUT";	
	
	public static CFWField<String> createSettingsFieldWorkingDir() {
		return createSettingsFieldWorkingDir(PARAM_DIR);
	}
	
	/*************************************************************************
	 * 
	 *************************************************************************/
	public static CFWField<String> createSettingsFieldWorkingDir(String paramName) {
		return  CFWField.newString(FormFieldType.TEXT, paramName)
						.setDescription("(Optional)The working directory where the commands should be executed. (Default: \""+FeatureCLIExtensions.getDefaultFolderDescription()+"\")")
						.disableSanitization()
						;
	}
	
	/*************************************************************************
	 * 
	 *************************************************************************/
	public static CFWField<String> createSettingsFieldCommands() {
		CFWField<String> field =
				CFWField.newString(FormFieldType.TEXTAREA, PARAM_COMMANDS)
						.setDescription("The commands to be executed on the command line.")
						.disableSanitization()
					  	;
		
		field.addCssClass("monospace");
		return field;
	}
	
	/*************************************************************************
	 * 
	 *************************************************************************/
	public static CFWField<LinkedHashMap<String, String>> createSettingsFieldEnvVariables() {
		return CFWField.newValueLabel(PARAM_ENV)
				.setLabel("Environment Variables")
				.setDescription("(Optional)Additional environment variables which should be passed to the command line.")
				.addValidator(new NotNullOrEmptyValidator())
				.disableSanitization()
				;
	}
	
	/*************************************************************************
	 * 
	 *************************************************************************/
	public static CFWField<Integer> createSettingsFieldHead() {
		return 	CFWField.newInteger(FormFieldType.NUMBER, PARAM_HEAD)
						.setDescription("(Optional)Number of lines that should be read from the head(start) of the output.")
						.disableSanitization()
						.addValidator(new NumberRangeValidator(0, 10000))
						.setValue(100)
						;
	}
		
	/*************************************************************************
	 * 
	 *************************************************************************/
	public static CFWField<Integer> createSettingsFieldTail() {
		return 	CFWField.newInteger(FormFieldType.NUMBER, PARAM_TAIL)
				.setDescription("Number of lines that should be read from the tail(end) of the output.")
				.disableSanitization()
				.addValidator(new NumberRangeValidator(0, 10000))
				.setValue(100)
				;
	}
	
	/*************************************************************************
	 * 
	 *************************************************************************/
	public static CFWField<Boolean> createSettingsFieldCountSkipped() {
		return 	CFWField.newBoolean(FormFieldType.BOOLEAN, PARAM_COUNT_SKIPPED)
						.setDescription("If parameter head or tail is set, this parameter decides if skipped line count should be added in the output.(Default:true)")
						.disableSanitization()
						.setValue(true)
						;
	}
	
	/*************************************************************************
	 * 
	 *************************************************************************/
	public static CFWField<Integer> createSettingsFieldTimeout() {
		return 	CFWField.newInteger(FormFieldType.NUMBER, PARAM_TIMEOUT)
				.setDescription("The timeout in seconds.")
				.setValue(120)
				;
	}

}
