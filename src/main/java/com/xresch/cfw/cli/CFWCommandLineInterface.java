package com.xresch.cfw.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import com.xresch.cfw.validation.FileCanReadValidator;
import com.xresch.cfw.validation.ValidationEngine;


/**************************************************************************************************************
 * Provides the basic functionalities for the handling of 
 * command line arguments.
 * It provides default arguments for a configuration file and handling of log levels.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWCommandLineInterface {
	
	public static final String VM_CONFIG_LOGFOLDER = "-Dcfw.log.folder";
	
	public static final String VM_CONFIG_FOLDER_DEFAULT = "cfw.config.defaultfolder";
	public static final String VM_CONFIG_FOLDER = "cfw.config.folder";
	public static final String VM_CONFIG_FILENAME = "cfw.config.filename";
	public static final String STOP = "-stop";
	
	protected static LinkedHashMap<String,String> loadedArguments = new LinkedHashMap<>();
	protected static LinkedHashMap<String,ArgumentDefinition> supportedArgumentsMap = new LinkedHashMap<>();

	protected static ArrayList<String> invalidMessages;
	
	protected static ValidationEngine valengine = new ValidationEngine();
	
	static {
		
		//*********************************************
		// Config Folder
		ArgumentDefinition logpath = 
				new ArgumentDefinition(	VM_CONFIG_LOGFOLDER, 
										VM_CONFIG_LOGFOLDER+"={folderpath}",
										"./log",
										"(VM Argument)The path to the folder were the logs should be written.",
										true);
		
		addSupportedArgument(logpath.getLabel(), logpath);
		
		//*********************************************
		// Config Folder
		ArgumentDefinition configFolderDefault = 
				new ArgumentDefinition(	VM_CONFIG_FOLDER_DEFAULT, 
						VM_CONFIG_FOLDER_DEFAULT+"={folderpath}",
										"./config",
										"(VM Argument)The path to the folder containing the default config files. from here the configuration will be copied to the location defined by -config.folder in case the files do not exist in the target.",
										true);
		
		valengine.addValidator(new FileCanReadValidator(configFolderDefault));
		addSupportedArgument(configFolderDefault.getLabel(), configFolderDefault);
		
		//*********************************************
		// Config Folder
		ArgumentDefinition configFolder = 
				new ArgumentDefinition(	VM_CONFIG_FOLDER, 
										VM_CONFIG_FOLDER+"={folderpath}",
										"./config",
										"(VM Argument)The path to the folder containing the config files like cfw.properties.",
										true);
		
		addSupportedArgument(configFolder.getLabel(), configFolder);
		
		//*********************************************
		// Config Filename
		ArgumentDefinition configFilename = 
				new ArgumentDefinition(	VM_CONFIG_FILENAME, 
										VM_CONFIG_FILENAME+"={filename}",
										"cfw.properties",
										"(VM Argument)The filename of the cfw.properties file.",
										true);
		
		addSupportedArgument(configFilename.getLabel(), configFilename);
		
		// doesn't work without foldername
//		valengine.addValidator(new FileCanReadValidator(configFilename));

		
		//*********************************************
		// Stop
		ArgumentDefinition stop = 
				new ArgumentDefinition(	STOP, 
										STOP,
										"",
										"Stop command for shutting down the running server.",
										false);
		
		addSupportedArgument(stop.getLabel(), stop);
	}

	
	/***********************************************************
	 * Resolves the command line arguments and stores them in
	 * the internal argument list.
	 * 
	 * @param argArray the arguments to resolve with the format
	 * "-{key}={value}"
	 * @throws ArgumentsException 
	 * 
	 ***********************************************************/
	protected static void resolveArguments(String[] argArray) throws ArgumentsException {
				
		//-------------------------------------------
		// Read the Arguments
		for(String argument : argArray){
			
			String argKey = "";
			String argValue = "";
			
			String[] splitted = argument.split("=");
			if(splitted.length == 2){
				argKey = splitted[0];
				argValue = splitted[1];
				
				loadedArguments.put(argKey, argValue);
				
			}else if(splitted.length == 1){
				loadedArguments.put(splitted[0], "null");
			}else {
				ArgumentsException exception = new ArgumentsException("Argument could not be loaded: "+argument);
				exception.setArgument(argument);
				throw exception;
			}
			
		}
	}
	
	/***********************************************************
	 * Resolves the command line arguments and stores them in
	 * the internal argument list.
	 * 
	 * @param argArrayList the arguments to resolve with the format
	 * "-{key}={value}"
	 * @throws ArgumentsException 
	 * 
	 ***********************************************************/
	protected static void resolveArguments(ArrayList<String> argArrayList) throws ArgumentsException {
		resolveArguments(argArrayList.toArray(new String[0]));
	}

	/***********************************************************
	 * Parses the command line arguments. 
	 * 
	 * @param args the arguments to parse.
	 * @throws ArgumentsException 
	 * 
	 ***********************************************************/
	public static void readArguments(String[] args) throws ArgumentsException{
		
		resolveArguments(args);
	}
	
	/***********************************************************
	 * Print a usage listing with supported arguments.
	 * 
	 ***********************************************************/
	public static void printUsage(){
		for(ArgumentDefinition currentArgument : supportedArgumentsMap.values()){
			System.out.println("");
			System.out.print(currentArgument.getLabel());
			System.out.println("\n\t\tSyntax: "+currentArgument.getSyntax());
			
			if(currentArgument.getDefaultValue() != null && !currentArgument.getDefaultValue().trim().isEmpty()){
				System.out.println("\t\tDefault: "+currentArgument.getDefaultValue());
			}
			
			System.out.println("\t\tDescription: "+currentArgument.getDescription());
		
		}
	}
	
	/***********************************************************
	 * Returns true if all arguments were correct, false otherwise.
	 ***********************************************************/
	public static boolean validateArguments(){

		boolean isValid = true;
		
		invalidMessages = new ArrayList<>();
		
		for(String argumentKey : loadedArguments.keySet()){
			String argumentValue = loadedArguments.get(argumentKey);
			
			ArgumentDefinition supportedArgument = supportedArgumentsMap.get(argumentKey);
			
			if(supportedArgument != null){
				if(!supportedArgument.validateValue(argumentValue)){
					invalidMessages.addAll(supportedArgument.getInvalidMessages());
					isValid=false;
				}
			}else{
				invalidMessages.add("The argument '"+argumentKey+"' is not supported.");
				isValid=false;
			}
		}
		
		return isValid;
	}
	
	/***********************************************************
	 * Print a list of readed arguments to standard output.
	 * Will be executed if debug is enabled.
	 * 
	 ***********************************************************/
	public static void printLoadedArguments(){
		Set<String> keySet = loadedArguments.keySet();
		for(String key : keySet){
			System.out.println("Key: "+key+", Value:"+loadedArguments.get(key));
		}
	}

	/***********************************************************
	 * Add a supported Argument.
	 ***********************************************************/
	public static ArgumentDefinition addSupportedArgument(String key, ArgumentDefinition value) {
		return supportedArgumentsMap.put(key, value);
	}

	
	/***********************************************************
	 * Check if the argument is supported.
	 * 
	 ***********************************************************/
	public static boolean isArgumentSupported(String argumentKey){
		Set<String> keySet = supportedArgumentsMap.keySet();
		for(String key : keySet){
			if(argumentKey.equals(key))
				return true;
		}
		
		return false;
	}

	public static boolean hasArguments() {
		return loadedArguments.size() > 0 ? true : false;
	}
	
	//####################################################################################
	// GETTERS & SETTERS
	//####################################################################################
	
	public static void addArgument(String key, String value){
		loadedArguments.put(key, value);
	}
	
	public static void addAllArgument(LinkedHashMap<String,String> arguments){
		loadedArguments.putAll(arguments);
	}
	
	public static LinkedHashMap<String,String> getLoadedArguments() {
		return loadedArguments;
	}

	public static ArrayList<String> getInvalidMessages() {
		return invalidMessages;
	}
	
	public static String getInvalidMessagesAsString() {
		StringBuilder builder = new StringBuilder();
		
		for(String message : invalidMessages) {
			builder.append(message).append("\n");
		}
		
		return builder.toString();
	}
	
	public static boolean isArgumentLoaded(String argumentKey) {
		if(loadedArguments.get(argumentKey) != null) {
			return true;
		}
		return false;
	}

	public static String getValue(String argumentKey) {
		
		//------------------------------------------
		// Handle VM Argument
		if(supportedArgumentsMap.get(argumentKey).isVMArgument()) {
			String value = System.getProperty(argumentKey);
			if(value == null) {
				// return default
				value = supportedArgumentsMap.get(argumentKey).getValue();
			}
			
			return value;
		}
		
		//------------------------------------------
		// Handle Application Argument
		if(loadedArguments.get(argumentKey) != null) {
			return loadedArguments.get(argumentKey);
		}else {
			return supportedArgumentsMap.get(argumentKey).getValue();
		}
	}
		
	public static void clearLoadedArguments() {
		loadedArguments = new LinkedHashMap<>();
	}
	
}
