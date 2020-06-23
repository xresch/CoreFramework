package com.pengtoolbox.cfw.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import com.pengtoolbox.cfw.validation.FileCanReadValidator;
import com.pengtoolbox.cfw.validation.ValidationEngine;


/**************************************************************************************************************
 * Provides the basic functionalities for the handling of 
 * command line arguments.
 * It provides default arguments for a configuration file and handling of log levels.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class CFWCommandLineInterface {
	
	public static final String CONFIG_FILE = "-config.file";
	public static final String STOP = "-stop";
	
	protected static LinkedHashMap<String,String> loadedArguments = new LinkedHashMap<String,String>();
	protected static LinkedHashMap<String,ArgumentDefinition> supportedArgumentsMap = new LinkedHashMap<String,ArgumentDefinition>();

	protected static ArrayList<String> invalidMessages;
	
	protected static ValidationEngine valengine = new ValidationEngine();
	
	static {
		//*********************************************
		// Config File
		ArgumentDefinition configFile = 
				new ArgumentDefinition(	CONFIG_FILE, 
										CONFIG_FILE+"={filepath}",
										"./config/cfw.properties",
										"The path to a config-file. The config-file can include all the arguments defined in this list delimited by newline. Also lines starting with �#� are considered as comments, as well blank lines are allowed.");
		
		valengine.addValidator(new FileCanReadValidator(configFile));
		addSupportedArgument(configFile.getName(), configFile);
		
		//*********************************************
		// Stop
		ArgumentDefinition stop = 
				new ArgumentDefinition(	STOP, 
										STOP,
										"",
										"Stop command for shutting down the running server.");
		
		addSupportedArgument(stop.getName(), stop);
	}

	
	/***********************************************************
	 * Before resolving all the arguments, first set the log 
	 * levels if specified.
	 * 
	 ***********************************************************/
//	protected void loadLogLevels(String[] argArray) {
//		
//		// Load default values and overwrite if specified
//		String consoleLevel = this.getValue(CONFIG_LOGLEVEL_CONSOLE).toUpperCase();
//		String fileLevel = this.getValue(CONFIG_LOGLEVEL_FILE).toUpperCase();
//		
//		for(String argument : argArray){
//			if(argument.startsWith(CONFIG_LOGLEVEL_CONSOLE)){
//				consoleLevel = argument.split("=")[1].toUpperCase();
//			}
//			if(argument.startsWith(CONFIG_LOGLEVEL_FILE)){
//				fileLevel = argument.split("=")[1].toUpperCase();
//			}
//		}
//		
//		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//		Configuration config = ctx.getConfiguration();
//		
//		ConsoleAppender consoleAppender = (ConsoleAppender)config.getAppender("CONSOLE");
//		ThresholdFilter consoleFilter = ThresholdFilter.createFilter(Level.getLevel(consoleLevel), Result.ACCEPT, Result.DENY);
//		consoleAppender.addFilter(consoleFilter);
//		
//		RollingFileAppender fileAppender = (RollingFileAppender)config.getAppender("ROLLING_FILE");
//		ThresholdFilter fileFilter = ThresholdFilter.createFilter(Level.getLevel(fileLevel), Result.ACCEPT, Result.DENY);
//		fileAppender.addFilter(fileFilter);
//	}
	
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
	
//	/***********************************************************
//	 * Resolves the arguments from a file and stores them in
//	 * the internal argument list.
//	 * 
//	 * @param configFilePath the path of the config file
//	 * @throws ArgumentsException 
//	 * 
//	 ***********************************************************/
//	private static void readArgumentsFromFile(String configFilePath) throws ArgumentsException {
//		ArrayList<String> argArrayList = new ArrayList<String>();
//		
//		//-------------------------------------------
//		// Read config File
//		BufferedReader bf = null;
//		try {
//			bf = new BufferedReader(new FileReader(configFilePath));
//			
//			boolean hasMoreLines = true;
//			while(hasMoreLines){
//			 
//				String line = bf.readLine();
//				
//				if(line != null && !line.trim().isEmpty() && !line.startsWith("#")){
//					argArrayList.add(line);
//				}else{
//					if(line == null) hasMoreLines = false;
//				}
//			}
//			
//			//-------------------------------------------
//			// overwrite CommandLine-Arguments
//			resolveArguments(argArrayList);
//			
//		} catch (FileNotFoundException e) {
//			new CFWLog(logger).severe("specified config file not found:"+e.getMessage());
//		} catch (IOException e) {
//			new CFWLog(logger).severe("error while reading config file:"+e.getMessage());
//		} finally{
//			if(bf != null){
//				try {
//					bf.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//	}

	/***********************************************************
	 * Print a usage listing with supported arguments.
	 * 
	 ***********************************************************/
	public static void printUsage(){
		for(ArgumentDefinition currentArgument : supportedArgumentsMap.values()){
			System.out.println("");
			System.out.print(currentArgument.getName());
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
		
		invalidMessages = new ArrayList<String>();
		
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
		if(loadedArguments.get(argumentKey) != null) {
			return loadedArguments.get(argumentKey);
		}else {
			return supportedArgumentsMap.get(argumentKey).getValue();
		}
	}
	
	public static void clearLoadedArguments() {
		loadedArguments = new LinkedHashMap<String,String>();
	}
	
}
