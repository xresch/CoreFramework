package com.xresch.cfw.features.query;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.JSON;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.parameter.FeatureParameter;
import com.xresch.cfw.features.query.FeatureQuery.CFWQueryComponentType;
import com.xresch.cfw.features.query.database.CFWDBQueryHistory;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.PlaintextResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class ServletQuery extends HttpServlet
{

	public static final String AUTOCOMPLETE_FORMID = "cfwQueryAutocompleteForm";

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletQuery.class.getName());
	
	public ServletQuery() {
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		
		doGet(request, response);
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("Query");
		
		if( CFW.Context.Request.hasPermission(FeatureQuery.PERMISSION_QUERY_USER)
		 || CFW.Context.Request.hasPermission(FeatureQuery.PERMISSION_QUERY_ADMIN)) {
			
			//-----------------------------------------------
			// Create query object to handle autocompletion
			// Field will be created on client side
			//createQueryObject(request, null);

			//cfw_autocompleteInitialize('cfwExampleHandlerForm','JSON_TAGS_SELECTOR',1,10);
			String action = request.getParameter("action");
			
			if(action == null) {
				
				// added globally >> html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query.css");
				// added globally >> html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query_rendering.js");
				// added globally >> html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query_editor.js");
			
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureParameter.PACKAGE_RESOURCES, "cfw_parameter.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query_store_list.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureManual.PACKAGE_RESOURCES, "cfw_manual_common.js"); // needed to make links work
				
				html.addJavascriptCode("cfw_query_initialDraw();");
				html.addJavascriptData("formID", AUTOCOMPLETE_FORMID);
				html.addJavascriptData("requestHeaderMaxSize", CFW.Properties.HTTP_MAX_REQUEST_HEADER_SIZE);
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
		}else {
			CFWMessages.accessDenied();
		}
        
    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String ID = request.getParameter("id");
		
		//int	userID = CFW.Context.Request.getUser().id();
			
		JSONResponse jsonResponse = new JSONResponse();		

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "manualpage": 		getManualPage(request, jsonResponse);
	  										break;
	  				
					case "queryhistorylist": jsonResponse.getContent().append(CFWDBQueryHistory.getHistoryForUserAsJSON());
											break;						
	  										
					case "aitraining": 		getAITrainingFile(request, jsonResponse);
											break;
					
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;
				
			case "fetchpartial": 			
				switch(item.toLowerCase()) {
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;		
			
			case "create": 			
				switch(item.toLowerCase()) {

					case "autocompleteform": 	createQueryObject(request, jsonResponse);
												break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				

			
			case "delete": 			
				switch(item.toLowerCase()) {

					case "historyitem": CFWDBQueryHistory.deleteByID( Integer.parseInt(ID) );
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "execute": 			
				switch(item.toLowerCase()) {

					case "query": 	 	executeQuery(request, jsonResponse);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
//			case "getform": 			
//				switch(item.toLowerCase()) {
//					case "editperson": 	createEditForm(jsonResponse, ID);
//										break;
//					
//					default: 			CFW.Messages.itemNotSupported(item);
//										break;
//				}
//				break;
						
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void getManualPage(HttpServletRequest request, JSONResponse jsonResponse) {
		String componentType = request.getParameter("type");
		String componentName = request.getParameter("name");
		
		CFWQuery pseudoQuery = new CFWQuery();
		
		CFWQueryComponentType type = CFWQueryComponentType.valueOf(componentType);
		switch(type) {
			case COMMAND:
				CFWQueryCommand command = CFW.Registry.Query.createCommandInstance(pseudoQuery, componentName);
				CFWQueryManualPageCommand commandPage = new CFWQueryManualPageCommand(componentName, command);
				jsonResponse.setPayload(commandPage.content().readContents());
			break;
				
			case FUNCTION:
				CFWQueryFunction current = CFW.Registry.Query.createFunctionInstance(pseudoQuery.getContext(), componentName);
				CFWQueryManualPageFunction functionPage = new CFWQueryManualPageFunction(componentName, current);
				jsonResponse.setPayload(functionPage.content().readContents());
			break;
				
			case SOURCE:
				CFWQuerySource source = CFW.Registry.Query.createSourceInstance(pseudoQuery, componentName);
				CFWQueryManualPageSource sourcePage = new CFWQueryManualPageSource(componentName, source);
				jsonResponse.setPayload(sourcePage.content().readContents());
			break;
			
			default:
				//do nothing
			break;
		}
		
	}
	/******************************************************************
	 *
	 ******************************************************************/
	private void executeQuery(HttpServletRequest request, JSONResponse jsonResponse) {
		
		String query = request.getParameter("query");
		String timeframeJson = request.getParameter("timeframe");
		CFWTimeframe timeframe = new CFWTimeframe(timeframeJson);
		
		String saveToHistoryString = request.getParameter("saveToHistory");
		boolean saveToHistory = false;
		
		if(saveToHistoryString != null
		&& Boolean.parseBoolean(saveToHistoryString.trim()) ) {
			saveToHistory = true;
		}

		String queryParamsString = request.getParameter("parameters");
		
		JsonObject queryParamsObject = new JsonObject();
		if(!Strings.isNullOrEmpty(queryParamsString)) {
			queryParamsObject = JSON.stringToJsonObject(queryParamsString);
		}

		query = CFW.Time.replaceTimeframePlaceholders(query, timeframe);
		
		CFWQueryResultList resultList = 
				new CFWQueryExecutor()
					.saveToHistory(saveToHistory)
					.parseAndExecuteAll(
							  query
							, timeframe
							, queryParamsObject
						);
		
		if(resultList != null) {
			jsonResponse.setPayload(resultList.toJson());
			jsonResponse.setSuccess(true);
		}else {
			jsonResponse.setSuccess(false);
		}

		//PersonDBMethods.deleteByID(Integer.parseInt(ID));
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicatePerson(JSONResponse jsonResponse, String id) {
		//PersonDBMethods.duplicateByID(id);
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static void createQueryObject(HttpServletRequest request, JSONResponse jsonResponse) {
			
		String fieldname = request.getParameter("fieldname");
		
		if(Strings.isNullOrEmpty(fieldname)) {
			fieldname = "query";
		}
		
		//Create the object
		CFWObject queryObject = new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, fieldname)
						.setAutocompleteHandler(new CFWQueryAutocompleteHandler())
				);
		
		String uniqueFormID = AUTOCOMPLETE_FORMID + "-" +fieldname;
		queryObject.toForm(uniqueFormID, "Form for Autocomplete Handling");
		
		jsonResponse.addCustomAttribute("formid", uniqueFormID);

	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void getAITrainingFile(HttpServletRequest request, JSONResponse jsonResponse) {
		
		JsonObject file = generateAITrainingFile(request, jsonResponse);
		
		PlaintextResponse response = new PlaintextResponse();
		response.append(CFW.JSON.toJSON(file));

		response.setContentType("application/json");
		

	}
	/******************************************************************
	 *
	 ******************************************************************/
	private JsonObject generateAITrainingFile(HttpServletRequest request, JSONResponse jsonResponse) {
				
		CFWQuery pseudoQuery = new CFWQuery();
		
		JsonObject result = new JsonObject();
		
		//-------------------------
		// AI Instructions
		JsonArray instructions = new JsonArray();
		
//		instructions.add("Apply all of the following instructions and do not skip any.");
		instructions.add("The goal of this file is to make you as an AI able to understand a new query language.");
		instructions.add("This file itself is not an example of a query.");
		instructions.add("This file contains information about a query language.");
		instructions.add("This file has nested fields named 'query_examples' which do contain examples of queries.");
		instructions.add("Do only use what you find in this file to generate queries.");
		instructions.add("Mainly use the code examples you find in the nested fields 'examples' to generate queries.");
		instructions.add("CFW stands for 'Core Framework', the framwork that contains the code of query engine.");
		instructions.add("EMP stands for 'Engineered Monitoring Platform', the web application that is using the query language.");
		instructions.add("When parsing, parse the whole file and not just a part of it.");
		instructions.add("When parsing, make sure to parse and understand the examples.");
		instructions.add("When parsing, do not output everything you find in the file. Output the instructions in this list you have properly executed. When done with processing return a single message like 'I am ready to generate queries for you'.");
		instructions.add("Do not include any sources from other languages.");
		instructions.add("Do not include any commands from other languages.");
		instructions.add("Do not include any functions from other languages.");
		// instructions.add("Do not include things you know from other languages.");
		// instructions.add("When generating queries: Do not use syntax from other languages.");
		instructions.add("When generating queries: Always include pipe symbols ('|') to separate commands.");
		instructions.add("Always include pipe symbols ('|') before the first command of the query.");
		instructions.add("If a query is generated without pipe symbols ('|'), automatically detect and add them before each command.");
		instructions.add("The pipe symbol ('|') before each command must be included.");
		instructions.add("Do not invent new sources, only use the sources given in the file.");
		instructions.add("Do not invent new commands, only use the commands given in the file.");
		instructions.add("Do not invent new functions, only use the functions given in the file.");
		instructions.add("Use bash or shell for code formatting of the output.");
		
		result.add("instructions", instructions);
		
		
		//-------------------------
		// All Examples: Intentional Duplicate
		JsonArray query_examples = new JsonArray();
		result.add("query_examples", query_examples);
		
		//-------------------------
		// Language Description
		JsonArray description = new JsonArray();
		description.add("The language is used to to query data from one or multiple sources.");
		description.add("The data is processed as a stream of records.");
		description.add("Records can have one or multiple fields that have values.");
		description.add("The main parts of the language are: sources, commands, and functions.");
		description.add("Commands are preceeded by a pipe-symbol '|', for example '| <command>'. Records are passed from one command to the next through the whole pipeline.");
		description.add("Commands are used to fetch data, manipulate data and define how data should be displayed.");
		description.add("Some of the most important commands are: source, set, filter, sort, keep, remove, move, display, formatfield, formatrecord, chart");
		description.add("Commands can have zero or more parameters.");
		description.add("Commands have tags that indicate their usage.");
		description.add("Different commands can have different structures of parameters. Some take key-value-pairs, while others take strings and others take arrays or combinations of those types.");
		description.add("Functions are used to manipulate data.");
		description.add("Functions can have zero or more parameters.");
		description.add("Functions have tags that indicate their usage.");
		description.add("To get the selected time, there are the two functions 'earliest' and 'latest'.");
		description.add("There is a special command 'source', which should not be confused with the actual sources. The command 'source' takes a name of a source as it's first parameter(e.g. '| source web'). Then it takes all the parameters of the specified source.(e.g. \"| source web as=html url='https://www.acme.com'\").");
		description.add("Available sources are listed in this file by the field 'sources' with details and example queries.");
		description.add("Available commands are listed in this file by the field 'commands' with details and example queries.");
		description.add("Available functions are listed in this file by the field 'functions' with details and example queries.");
		description.add("Comments: Everything after a hashtag '#' until the end of a line is a comment.");
		description.add("Comments: Everything between '/*' and '*/' is a comment. The '*/' is optional, if it is not present, everything from '/*' to the end of the query is considered a comment.");
		description.add("Comments: The command 'off' can be used to turn off a command.");

		result.add("language_description", description);
		
		//-------------------------
		// Language Syntax	

//		result.addProperty("syntax", """
//query ::= (command)* (";" query)?
//command ::= "|" identifier params
//params ::= param+
//param ::= key "=" value | key | value | expression
//key ::= value
//value ::= literal | function-call | expression | group | array | object
//function-call ::= function-name "(" params ")"
//function-name ::= identifier
//expression ::= value (operator expression)?
//group ::= "(" value ")"
//array ::= "[" array-elements "]"
//array-elements ::= value ("," array-elements)?
//object ::= "{" object-members "}"
//object-members ::= key-value-pair ("," object-members)?
//key-value-pair ::= object-key ":" value
//object-key ::= '"' string-content '"'
//operator ::= "==" | "!=" | "~=" | "<=" | ">=" | "<" | ">"
//identifier ::= letter identifier-rest*
//identifier-rest ::= letter | digit | "_"
//literal ::= number | string | boolean | null
//number ::= digit+
//string ::= '"' string-content '"' | "'" string-content "'" | "`" string-content "`"
//string-content ::= character+
//boolean ::= "true" | "false"
//null ::= "null"
//digit ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
//letter ::= "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z" | "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
//text ::= character+
//character ::= letter | digit | symbol
//symbol ::= "." | "," | ";" | ":" | "!" | "?" | "+" | "-" | "*" | "/" | "=" | "<" | ">" | "(" | ")" | "[" | "]" | "{" | "}" | "|" | "&" | "^" | "%" | "$" | "#" | "@" | "~" | "`" | "\\" | '"' | "'"
//member-access ::= access-identifier "[" value "]" | access-identifier "." access-identifier
//access-identifier ::= string | string-content | member-access
//			""");
		
		//-------------------------
		// Language Names
		JsonArray languageNames = new JsonArray();
		languageNames.add("CFW Query");
		languageNames.add("EMP Query");
		
		result.add("query_language_names", languageNames);
				
		//-------------------------
		// Operators
		JsonObject operators = new JsonObject();
		operators.addProperty("==", "Checks if the values are equal.");
		operators.addProperty("!=", "Checks if the values are not equal.");
		operators.addProperty("~=", "Checks if the field matches a regular expression.");
		operators.addProperty("<=", "Checks if the field value is smaller or equals.");
		operators.addProperty(">=", "Checks if the field value is greater or equals.");
		operators.addProperty("<", "Checks if the field value is smaller.");
		operators.addProperty(">", "Checks if the field value is greater.");
		operators.addProperty("AND", "Used to combine two or more conditions. Condition matches only if both sides are true.");
		operators.addProperty("OR", "Used to combine two or more conditions. Condition matches if either side is true.");
		operators.addProperty("NOT", "Used to negate the result of the next condition.");
  
		result.add("operators", operators);
		
		//-------------------------
		// Sources 
		
		JsonObject sources = new JsonObject();
		result.add("sources", sources);
		
		TreeMap<String, Class<? extends CFWQuerySource>> sourcelist = CFW.Registry.Query.getSourceList();
		
		for(String sourceName : sourcelist.keySet()) {
			
			try {
				
				CFWQuerySource current = CFW.Registry.Query.createSourceInstance(pseudoQuery, sourceName);
				JsonObject sourceObject = new JsonObject();
				sourceObject.addProperty("name", sourceName);
				sourceObject.addProperty("description_short", current.descriptionShort());
				//sourceObject.addProperty("description_detailed_html", current.descriptionHTML());
				sourceObject.addProperty("time_handling", current.descriptionTime());
				sourceObject.addProperty("required_permissions", current.descriptionRequiredPermission());
				sourceObject.addProperty("parameters_list_html", current.getParameterListHTML());
				
				
				JsonArray examplesArray = new JsonArray();
				List<String> codeExamples = Jsoup.parse(current.descriptionHTML()).getElementsByTag("code").eachText();
				for(String example : codeExamples) {
					examplesArray.add(example);
				}
				sourceObject.add("query_examples", examplesArray);
				query_examples.addAll(examplesArray);
				
				sources.add(sourceName, sourceObject);
				
			}catch(Exception e) {
				new CFWLog(logger).severe("Error while creating object for source: "+sourceName, e);
			}
		}
		
		//-------------------------
		// Commands 
		
		JsonObject commands = new JsonObject();
		result.add("commands", commands);
		
		TreeMap<String, Class<? extends CFWQueryCommand>> commandlist = CFW.Registry.Query.getCommandList();
		
		for(String commandName : commandlist.keySet()) {
			
			try {
				
				CFWQueryCommand current = CFW.Registry.Query.createCommandInstance(pseudoQuery, commandName);
				JsonObject commandObject = new JsonObject();
				commandObject.addProperty("name", commandName);
				commandObject.addProperty("description", current.descriptionShort());
				//commandObject.addProperty("description_detailed_html", current.descriptionHTML());
				commandObject.addProperty("syntax", current.descriptionSyntax());
				commandObject.addProperty("syntax_details_and_parameters_html", current.descriptionSyntaxDetailsHTML());
				commandObject.add("tags", CFW.JSON.toJSONElement(current.getTags()) );
				
				JsonArray examplesArray = new JsonArray();
				List<String> codeExamples = Jsoup.parse(current.descriptionHTML()).getElementsByTag("code").eachText();
				for(String example : codeExamples) {
					examplesArray.add(example);
				}
				commandObject.add("query_examples", examplesArray);
				query_examples.addAll(examplesArray);
				
				commands.add(commandName, commandObject);
				
			}catch(Exception e) {
				new CFWLog(logger).severe("Error while creating object for command: "+commandName, e);
			}
		}
		
		//-------------------------
		// Functions 
		
		JsonObject functions = new JsonObject();
		result.add("functions", functions);
		
		TreeMap<String, Class<? extends CFWQueryFunction>> functionlist = CFW.Registry.Query.getFunctionList();
		
		for(String functionName : functionlist.keySet()) {
			
			try {
				
				CFWQueryFunction current = CFW.Registry.Query.createFunctionInstance(pseudoQuery.getContext(), functionName);
				JsonObject functionObject = new JsonObject();
				functionObject.addProperty("name", functionName);
				functionObject.addProperty("description", current.descriptionShort());
				//functionObject.addProperty("description_detailed_html", current.descriptionHTML());
				functionObject.addProperty("syntax", current.descriptionSyntax());
				functionObject.addProperty("syntax_details_and_parameters_html", current.descriptionSyntaxDetailsHTML());
				functionObject.add("tags", CFW.JSON.toJSONElement(current.getTags()) );
				
				JsonArray examplesArray = new JsonArray();
				List<String> codeExamples = Jsoup.parse(current.descriptionHTML()).getElementsByTag("code").eachText();
				for(String example : codeExamples) {
					examplesArray.add(example);
				}
				functionObject.add("query_examples", examplesArray);
				query_examples.addAll(examplesArray);
				
				functions.add(functionName, functionObject);
				
			}catch(Exception e) {
				new CFWLog(logger).severe("Error while creating object for function: "+functionName, e);
			}
		}
		
		//-------------------------
		// Return Result 
		return result;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditForm(JSONResponse json, String ID) {

//		Person Person = PersonDBMethods.selectByID(Integer.parseInt(ID));
//		
//		if(Person != null) {
//			
//			CFWForm editPersonForm = Person.toForm("cfwEditPersonForm"+ID, "Update Person");
//			
//			editPersonForm.setFormHandler(new CFWFormHandler() {
//				
//				@Override
//				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
//					
//					if(origin.mapRequestParameters(request)) {
//						
//						if(PersonDBMethods.update((Person)origin)) {
//							CFW.Messages.addSuccessMessage("Updated!");
//						}
//							
//					}
//					
//				}
//			});
//			
//			editPersonForm.appendToPayload(json);
//			json.setSuccess(true);	
//		}

	}
}