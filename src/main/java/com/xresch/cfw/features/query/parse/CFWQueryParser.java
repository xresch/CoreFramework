package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * QUERY SYNTAX DOCUMENTATION
 * ==========================
 * Given in more or less Backus–Naur form with regular expressions:
 *   - "*" stands for 0 to any number of repetitions
 *   - "+" stands for 1 to any number of repetitions
 *   - "?" stands for optional, or sometimes not neccessary(e.g. semicolon between repetitions when there is no repetition)
 * 
 * EBNF (more or less)
 * --------------------
	query ::= (command)* (";" query)?
	command ::= "|" identifier params
	params ::= param+
	param ::= key "=" value | key | value | expression
	key ::= value
	value ::= literal | function-call | expression | group | array | object
	function-call ::= function-name "(" params ")"
	function-name ::= identifier
	expression ::= value (operator expression)?
	group ::= "(" value ")"
	array ::= "[" array-elements "]"
	array-elements ::= value ("," array-elements)?
	object ::= "{" object-members "}"
	object-members ::= key-value-pair ("," object-members)?
	key-value-pair ::= object-key ":" value
	object-key ::= '"' string-content '"'
	operator ::= "==" | "!=" | "~=" | "<=" | ">=" | "<" | ">"
	identifier ::= letter identifier-rest*
	identifier-rest ::= letter | digit | "_"
	literal ::= number | string | boolean | null
	number ::= digit+
	string ::= '"' string-content '"' | "'" string-content "'" | "`" string-content "`"
	string-content ::= character+
	boolean ::= "true" | "false"
	null ::= "null"
	digit ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
	letter ::= "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z" | "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
	text ::= character+
	character ::= letter | digit | symbol
	symbol ::= "." | "," | ";" | ":" | "!" | "?" | "+" | "-" | "*" | "/" | "=" | "<" | ">" | "(" | ")" | "[" | "]" | "{" | "}" | "|" | "&" | "^" | "%" | "$" | "#" | "@" | "~" | "`" | "\\" | '"' | "'"
	member-access ::= access-identifier "[" value "]" | access-identifier "." access-identifier
	access-identifier ::= string | string-content | member-access
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022 
 * @license MIT-License
 **************************************************************************************************************/


public class CFWQueryParser {
	
	private static Logger logger = CFWLog.getLogger(CFWQueryParser.class.getName());
		
	// contains the original query string
	private String query = null;
	
	// used to extract the queryString of the query under parsing
	private int lastSemicolonIndex = 0;
	
	// set to true to check if user has permissions to access the specified sources
	private boolean checkSourcePermissions = true;
	
	// the initial context to be copied to each instance of CFWQuery
	private CFWQueryContext initialContext;
	private boolean doCloneContext;
	
	// the list of tokens after tokenizing the query string
	private ArrayList<CFWQueryToken> tokenlist;
	
	// the current Query instance under parsing
	CFWQuery currentQuery;
	
	// the context of currentQuery
	private CFWQueryContext currentContext;
	
	// the parts of all queries, used for debugging, will contain CFWQueryCommand and CFQQueryPart
	ArrayList<Object> allqueryParts = new ArrayList<>(); 
	
	// the parts of the currentQuery
	ArrayList<QueryPart> currentQueryParts = new ArrayList<>(); //initialized here so test cases don't run into nullpointer
	
	// cached instance
	private static CFWQueryCommandSource sourceCommand = new CFWQueryCommandSource(new CFWQuery());
	private static String[] sourceCommandNames = sourceCommand.uniqueNameAndAliases();
	
	// cursors
	private int cursor;
	private int lastCursor;
	
	// counts how often the same cursor was detected, used to prevent endless looping
	private int endlessLoopPreventionCounter = 0;
	
	
	public static final String KEYWORD_AND = "AND";
	public static final String KEYWORD_OR = "OR";
	public static final String KEYWORD_NOT = "NOT";
	
	private boolean enableTracing = false;
	private boolean enableTrackParts = false;
	private boolean enableTrackTokens = false;
	
	private JsonArray traceArray;
	
	//Used for GIB!-Ea-stere-gg
	String obedienceMessage = CFW.Random.messageOfObedience();
	
	//Manage open arrays and groups.
	private Stack<CFWQueryParserContext> contextStack = new Stack<>();
	
	public enum CFWQueryParserContext{
		DEFAULT, BINARY, ARRAY, GROUP, FUNCTION, JSON_MEMBER_ACCESS
	}
	
	// Stub Class to indicate end of arrays and groups during parsing
	private class QueryPartEnd extends QueryPart {
		public QueryPartEnd(CFWQueryContext context) { super(); }
		@Override public QueryPartValue determineValue(EnhancedJsonObject object) { return null; }
		@Override public JsonObject createDebugObject(EnhancedJsonObject object) { return null; }
		@Override public QueryPart clone() { return null; }
		@Override public void setParentCommand(CFWQueryCommand parent) { /*do nothing */}
	};
	
	
	/***********************************************************************************************
	 * 
	 * @param initialContext with earliest, latest, timezoneOffset and checkPermission values.
	 * @param doCloneContext TODO
	 ***********************************************************************************************/
	public CFWQueryParser(
			String inputQuery
			, boolean checkSourcePermissions
			, CFWQueryContext initialContext
			, boolean doCloneContext) {
		
		//--------------------------------
		// Set Context
		if(initialContext == null) {
			initialContext = new CFWQueryContext();
		}
		
		initialContext.setFullQueryString(inputQuery);
		
		//--------------------------------
		// Set Parameters
		this.query = inputQuery;
		this.initialContext = initialContext;
		this.doCloneContext = doCloneContext;
		this.checkSourcePermissions = initialContext.checkPermissions();
		this.cursor = 0;
		
		//--------------------------------
		// Get Tokens
		this.tokenlist = new CFWQueryTokenizer(this.query, false, true)
				.keywords(KEYWORD_AND
						, KEYWORD_OR
						, KEYWORD_NOT)
				.getAllTokens();
		
		//--------------------------------
		// Check Tracing Enabled
		if( ! Strings.isNullOrEmpty(inputQuery) ) {
			
			String firstLine = inputQuery.split("\n")[0].toUpperCase();
			if(firstLine.contains("#TRACE")) {
				this.enableTracing(true);
			};
			
			if(firstLine.contains("#PARTS")) {
				this.enableTrackParts = true;
			};
			
			if(firstLine.contains("#TOKENS")) {
				this.enableTrackTokens = true;
			};
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryParser enableTracing(boolean enableTracing) {
		this.enableTracing = enableTracing;
		
		if(!enableTracing) {
			return this;
		}

		traceArray = new JsonArray();
		
		if(tokenlist != null) {
			
			addTrace("TokenCount", ""+tokenlist.size(), "");
			
			StringBuilder tokenOverview = new StringBuilder();
			for(CFWQueryToken token : tokenlist) {
				
				tokenOverview.append(token.type().toString()+"("+token.value()+"), ");
			}
			
			addTrace("TokenOverview", "", tokenOverview.toString());
		}
		return this;
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isTracingEnabled() {
		return enableTracing;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isTrackPartsEnabled() {
		return enableTrackParts;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isTrackTokensEnabled() {
		return enableTrackTokens;
	}
	
	/***********************************************************************************************
	 * Add a trace object to the trace array.
	 * @param key a key for your trace
	 * @param value the value related to your key (use for smaller values)
	 * @param message for your trace (use for longer values)
	 ***********************************************************************************************/
	public void addTrace(Object key, Object value, Object message) {

		if(enableTracing) {

			JsonObject object = new JsonObject();
			
			//-------------------------------
			// Create Message
			String finalMessage = "";
			if(message != null) { 
				
				if(!(message instanceof QueryPart)) {
					finalMessage = message.toString();
				}else{
					JsonObject debugObject = ((QueryPart)message).createDebugObject(null);
					if(debugObject != null) {
						finalMessage = debugObject.toString();
					}
				}
			}
			
			//-------------------------------
			// Create Trace object
			object.addProperty("Key", 		(key != null) ? key.toString() : null);
			object.addProperty("Value", 	(value != null) ? value.toString() : null);
			object.addProperty("Message", 	finalMessage);
			object.addProperty("TokenPosition", cursor);
			
			if(cursor < tokenlist.size()) {
				CFWQueryToken token = tokenlist.get(cursor);
				object.addProperty("nextTokenType", (token != null) ? token.type().toString()  : null);
				object.addProperty("nextTokenValue",(token != null) ? tokenlist.get(cursor).value()  : null);
			}
			
			traceArray.add(object);
			
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public JsonArray getTraceResults() {
		return traceArray;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryResult getTraceAsQueryResult() {
		
		CFWQueryResult traceResult = new CFWQueryResult(new CFWQueryContext());
		ArrayList<EnhancedJsonObject> traceRecords = new ArrayList<>();
		
		//-----------------------------
		// Set Metadata
		traceResult.getMetadata().addProperty("name", "[#TRACE] Parsing");
		traceResult.getMetadata().addProperty("title", true);
		
		//-----------------------------
		// Create Trace Records
		if(traceArray != null) {
			for(JsonElement element : traceArray) {
				traceRecords.add(new EnhancedJsonObject(element.getAsJsonObject()));
			}
		}
		
		//-----------------------------
		// Set Detected Fieldnames
		if( ! traceRecords.isEmpty() ) {
			traceResult.setDetectedFields(traceRecords.get(0).keySet());
		}
		
		
		//-----------------------------
		// Return Result
		traceResult.setRecords(traceRecords);
		return traceResult;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryResult getPartsAsQueryResult() {
		CFWQueryResult partsResult = new CFWQueryResult(new CFWQueryContext());
		ArrayList<EnhancedJsonObject> partRecords = new ArrayList<>();
		
		//-----------------------------
		// Set Metadata
		partsResult.getMetadata().addProperty("name", "[#PARTS] List of Final Query Parts");
		partsResult.getMetadata().addProperty("title", true);

		//-----------------------------
		// Create Records
		for(Object part : allqueryParts) {
			
			EnhancedJsonObject partObject = new EnhancedJsonObject();
			partRecords.add(partObject);
			
			if(part instanceof QueryPart) {
				JsonObject debugObject = ((QueryPart)part).createDebugObject(null);
				
				partObject.add(
						QueryPart.FIELD_PARTTYPE
					  , debugObject.remove(QueryPart.FIELD_PARTTYPE)
				);
				partObject.add("value", debugObject);
				
			}else if(part instanceof CFWQueryCommand) { 
				CFWQueryCommand command = (CFWQueryCommand)part;
				partObject.addProperty(QueryPart.FIELD_PARTTYPE, "command");
				partObject.addProperty("value", command.getUniqueName());
			}
		}
		
		//-----------------------------
		// Set Detected Fieldnames
		if( ! partRecords.isEmpty() ) {
			partsResult.setDetectedFields(partRecords.get(0).keySet());
		}
					
		//-----------------------------
		// Return Result
		partsResult.setRecords(partRecords);
		return partsResult;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryResult getTokensAsQueryResult() {
		CFWQueryResult result = new CFWQueryResult(new CFWQueryContext());
		ArrayList<EnhancedJsonObject> partRecords = new ArrayList<>();
		
		//-----------------------------
		// Set Metadata
		result.getMetadata().addProperty("name", "[#TOKENS] List of Tokens");
		result.getMetadata().addProperty("title", true);

		//-----------------------------
		// Create Records
		for(CFWQueryToken token : tokenlist) {
			
			EnhancedJsonObject partObject = new EnhancedJsonObject(token.toJson());
			partRecords.add(partObject);

		}
		
		//-----------------------------
		// Set Detected Fieldnames
		if( ! partRecords.isEmpty() ) {
			result.setDetectedFields(partRecords.get(0).keySet());
		}
					
		//-----------------------------
		// Return Result
		result.setRecords(partRecords);
		return result;
	}
	
	
//	/***********************************************************************************************
//	 * Get the previous part from the array of parts.
//	 * Returns null of there is no previous part.
//	 ***********************************************************************************************/
//	private QueryPart getPreviousPart() {
//
//		QueryPart previousPart = null;
//		if( currentQueryParts.size() > 0) { 
//			previousPart = currentQueryParts.remove(currentQueryParts.size()-1);
//		}
//		
//		return previousPart;
//	}
	
	/***********************************************************************************************
	 * Pops the previous part from the array of parts.
	 * Returns null of there is no previous part.
	 ***********************************************************************************************/
	private QueryPart popPreviousPart() {

		QueryPart previousPart = null;
		if( currentQueryParts.size() > 0) { 
			previousPart = currentQueryParts.remove(currentQueryParts.size()-1);
		}
		
		return previousPart;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean hasMoreTokens() {
		return cursor < tokenlist.size();
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryToken consumeToken() {
		cursor++;
		return tokenlist.get(cursor-1);
		
	}
	
	/***********************************************************************************************
	 * Returns the token which will be consumed next by consumeToken().
	 ***********************************************************************************************/
	public CFWQueryToken lookahead() {
		return this.lookat(0);
	}
	/***********************************************************************************************
	 * Returns the token before the current token.
	 ***********************************************************************************************/
	public CFWQueryToken lookbehind() {
		return this.lookat(-2);
	}
	
	/***********************************************************************************************
	 * Returns the token with a specific offset to the token that will be consumed next by consumeToken().
	 * Returns null if index is out of bounds.
	 * 
	 * @param offset positive or negative value to specify offset. 0 is next, 1 is the one one after etc...
	 ***********************************************************************************************/
	public CFWQueryToken lookat(int offset) {
		int pos = cursor+offset;
		if(pos < 0 || pos >= tokenlist.size()) {
			return null;
		}
		return tokenlist.get(pos);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public int cursor() {
		return cursor;
		
	}
	
	/***********************************************************************************************
	 * Entry method parsing the whole query String and returns an ArrayList of queries.
	 * Multiple Queries are separated by semicolon.
	 * 
	 ***********************************************************************************************/
	public ArrayList<CFWQuery> parse() throws ParseException {

		new CFWLog(logger).finer("Parsing Query: "+this.query);
		
		ArrayList<CFWQuery> queryList = new ArrayList<>();
		//-----------------------------------
		// Skip preceding semicolons
		if(this.lookahead().type() == CFWQueryTokenType.SIGN_SEMICOLON) {
			addTrace("Skip Preceeding Semicolon", "", "");
			this.consumeToken();
		}
		
		//-----------------------------------
		// Parse all queries separated by 
		// SEMICOLON
		while(this.hasMoreTokens()) {
			
			CFWQuery query = parseQuery();
			
			if( query != null && query.getCopyOfCommandList().size() > 0) {
				
				queryList.add(query);
			}

		}
		
		return queryList;
	}
	
	/***********************************************************************************************
	 * Method that parses a single query.
	 * It is recommended to use the parse()-method to parse a query that can contain multiple methods.
	 ***********************************************************************************************/
	public CFWQuery parseQuery() throws ParseException {
		
		addTrace("Parse", "Query", "[START]");
		
		currentQuery = new CFWQuery(initialContext, doCloneContext);
		currentContext = currentQuery.getContext();
		
		//-----------------------------------
		// Extract Current Query String
		if(this.hasMoreTokens()) {
			int startIndex = (lastSemicolonIndex == 0 ) ? 0 : lastSemicolonIndex+1;
			lastSemicolonIndex = query.indexOf(";", startIndex);
			lastSemicolonIndex = (lastSemicolonIndex != -1) ? lastSemicolonIndex : query.length();
			String currentQueryString = query.substring(startIndex, lastSemicolonIndex);
			//System.out.println("======= currentQueryString ========\n"+currentQueryString);
			currentContext.setOriginalQueryString(currentQueryString);
		}
		
		//-----------------------------------
		// Parse All Commands until Semicolon
		while(this.hasMoreTokens() && this.lookahead().type() != CFWQueryTokenType.SIGN_SEMICOLON) {
			
			CFWQueryCommand command = parseQueryCommand(currentQuery);
			if(command != null) {
				currentQuery.addCommand(command);
			}
		}
		
		//-----------------------------------
		//Skip successive Semicolons
		while(this.hasMoreTokens() && this.lookahead().type() == CFWQueryTokenType.SIGN_SEMICOLON) {
			this.consumeToken();
		}
		
		addTrace("Parse", "Query", "[END]");	
		return currentQuery;
	}
	
	/***********************************************************************************************
	 * Parses a single command
	 ***********************************************************************************************/
	public CFWQueryCommand parseQueryCommand(CFWQuery parentQuery) throws ParseException {
				
		//------------------------------------
		// Check Has More Tokens
		if(!this.hasMoreTokens()) {
			this.throwParseException("Query cannot end with a pipe symbol", 0);
		}
		
		//------------------------------------
		// Skip preceding command separators '|'
		while(this.lookahead().type() == CFWQueryTokenType.OPERATOR_OR) {
			this.consumeToken();
		}
		
		//------------------------------------
		// Expect LITERAL_STRING
		// Command Name

		if(this.lookahead().isString()) {
			
			//------------------------------------
			// Get Command
			CFWQueryToken commandNameToken = this.consumeToken();
			String commandName = commandNameToken.value();
			addTrace("Parse", "Command", "[START] "+commandName);
			//------------------------------------
			// GIB-Ea-stere-gg for Vincent Theus
			while(commandName.toLowerCase().equals("gib")
				||(commandName.toLowerCase().equals("gimme"))) {
				
				addTrace(commandName, commandName, commandName);
				
				if(!this.hasMoreTokens()) {
					this.throwParseException("Query cannot end with the super ultimate GIB as this would also end the universe.", 0);
				}else {
					CFW.Messages.addSuccessMessage(obedienceMessage);
					commandNameToken = this.consumeToken();
					commandName = commandNameToken.value();
					
					while(commandName.equals("!")) {
						if(!this.hasMoreTokens()) {
							this.throwParseException("Query cannot end with the super ultimate GIB as this would also end the universe.", 0);
						}else {
							commandNameToken = this.consumeToken();
							commandName = commandNameToken.value();
						}
					}
				}
			}
			
			//------------------------------------
			// Registry Check exists
			if(!CFW.Registry.Query.commandExists(commandName)) {
				this.throwParseException("Unknown command '"+commandName+"'.", 0);
			}
			
			//------------------------------------
			// Check if user has permission for
			// the source
			
			if (CFW.Utils.Array.contains(sourceCommandNames, commandName) 
			&& checkSourcePermissions) {
				
				CFWQueryToken token = this.lookahead();	
				
				if(token != null){
					boolean hasPermission = CFW.Registry.Query.checkSourcePermission(
							token.value(), 
							CFW.Context.Request.getUser()
						);
					
					if(!hasPermission) {
						throw new ParseException("User does not have permission to use the source: "+token.value(), token.position());
					}
				}else {
					throw new ParseException("Could not verify permissions for source: "+token.value(), token.position());
				}
				
			}
							
			
			//------------------------------------
			// Parse Parts Until End of Command ('|') or query (';')
			ArrayList<QueryPart> parts = parseQueryParts();
			
			//------------------------------------
			// Create Command instance
			CFWQueryCommand command = CFW.Registry.Query.createCommandInstance(parentQuery, commandName);
			
			if(this.enableTrackParts) {
				allqueryParts.add(command);
				allqueryParts.addAll(parts);
			}
			
			for(QueryPart part : parts) {
				part.setParentCommand(command);
			}
			
			command.setAndValidateQueryParts(this, parts);
			
			addTrace("Parse", "Command", "[END]");
			
			return command;
		}else {
			this.throwParseException("Expected command name.", 0);
		}
		
		addTrace("Parse", "Command", "[END] return null");
		return null;
		
	}
	
		
	/***********************************************************************************************
	 * Parse Parts Until End of Command ('|') or query (';')
	 ***********************************************************************************************/
	public ArrayList<QueryPart> parseQueryParts() throws ParseException {
		
		currentQueryParts = new ArrayList<>();
		
		while(this.hasMoreTokens() 
		   && this.lookahead().type() != CFWQueryTokenType.OPERATOR_OR
		   && this.lookahead().type() != CFWQueryTokenType.SIGN_SEMICOLON) {
				QueryPart part = parseQueryPart(CFWQueryParserContext.DEFAULT);
				currentQueryParts.add(part);
			addTrace("Parse", "Query Part", part);
		}
		
		return currentQueryParts;
	}
	
	/***********************************************************************************************
	 * Parses a query part.
	 * @param context TODO
	 ***********************************************************************************************/
	public QueryPart parseQueryPart(CFWQueryParserContext context) throws ParseException {
		
		//------------------------------------------
		// Prevent endless loops and OutOfMemoryErrors
		if(cursor != lastCursor) {
			endlessLoopPreventionCounter = 0;
			lastCursor = cursor;
		}else {
			endlessLoopPreventionCounter++;
			
			if(endlessLoopPreventionCounter >= 10 ) {
				throw new ParseException("Query Parser got stuck at a single cursor position and looped endlessly.", cursor);
			}
		}
		
		if(currentQueryParts.size() > 100000) {
			// should not be reached, just in case it ever does.
			throw new ParseException("Query Parser reached max number of allowed query parts(100'000). It's likely you started looping endlessly because of syntax errors.", cursor);
		}
		
		//------------------------------------------
		// Check Unexpected end of Query
		if(!this.hasMoreTokens()) {
			throw new ParseException("Unexpected end of query.", cursor);
		}
		
		//------------------------------------------
		// FIRST TOKEN: Expect anything that can
		// be part of an expression.
		// Do not expect anything that would create
		// a binary expression
		// No not consume any tokens in the switch
		// statement.
		CFWQueryToken firstToken;;
		QueryPart firstPart = null;
		
		switch(this.lookahead().type()) {								
		
			case TEXT_SINGLE_QUOTES:
			case TEXT_DOUBLE_QUOTES:	
			case TEXT_BACKTICKS:	
			case LITERAL_STRING:		firstToken = this.consumeToken();
										addTrace("Create Value Part", "String", firstToken.value());
										firstPart = QueryPartValue.newString(firstToken.value());
										break;
				
			case LITERAL_BOOLEAN:		firstToken = this.consumeToken();
										addTrace("Create Value Part", "Boolean", firstToken.value());
										firstPart = QueryPartValue.newBoolean(firstToken.valueAsBoolean());
										break;
			
			case LITERAL_NUMBER:		firstToken = this.consumeToken();
										addTrace("Create Value Part", "Number", firstToken.value());
										firstPart = QueryPartValue.newNumber(firstToken.valueAsNumber());
										break;
								
			case NULL: 					firstToken = this.consumeToken();
										addTrace("Create Value Part", "NULL", firstToken.value());
										firstPart = QueryPartValue.newNull();
										break;
	
			//=======================================================
			// Negative Numbers
			//=======================================================
			case OPERATOR_MINUS:	
				firstToken = this.consumeToken();
				if( this.hasMoreTokens()
				&& this.lookahead().type() == CFWQueryTokenType.LITERAL_NUMBER ){
					CFWQueryToken token = this.consumeToken();
					firstPart = QueryPartValue.newNumber(token.valueAsNumber().negate());
				}else {
					this.throwParseException("Unexpected '-'", firstToken.position());
				}
				
			break;
			
			//=======================================================
			// Create Function Part
			//=======================================================	
			case FUNCTION_NAME: 	
				firstToken = this.consumeToken();
				String functionName = firstToken.value();
				addTrace("Create Function Part", "Function", firstToken.value());
				
				QueryPart paramGroup = this.parseQueryPart(CFWQueryParserContext.FUNCTION);

				if(paramGroup instanceof QueryPartGroup) {
					firstPart = new QueryPartFunction(currentContext, functionName, (QueryPartGroup)paramGroup);

					if(this.lookahead() != null 
					&& this.lookahead().type() == CFWQueryTokenType.SIGN_BRACE_SQUARE_OPEN) {
						firstPart = this.parseJsonMemberAccess(context, firstPart);						
					}
				}else {
					this.throwParseException("expected parameters after function: "+functionName, firstToken.position());
				}
				
			break;
			
			
			//=======================================================
			// Create JSON Part
			//=======================================================	
			case SIGN_BRACE_CURLY_OPEN:		
				
				firstToken = this.consumeToken();
				
				int openCount = 1;
				
				StringBuilder jsonBuilder = new StringBuilder("{");
				while(openCount > 0 && this.hasMoreTokens()) {
					CFWQueryToken token = this.consumeToken();
					
					//-------------------------------------------
					// handle string values
					if(token.type() == CFWQueryTokenType.TEXT_SINGLE_QUOTES
					|| token.type() == CFWQueryTokenType.TEXT_DOUBLE_QUOTES
					|| token.type() == CFWQueryTokenType.LITERAL_STRING) {
						jsonBuilder.append("\""+CFW.JSON.escapeString(token.value())+"\"");
						continue;
					}
					
					//-------------------------------------------
					// Handle others
					jsonBuilder.append(token.value());
					
					if(token.type() == CFWQueryTokenType.SIGN_BRACE_CURLY_OPEN) {
						openCount++;
					}else if(token.type() == CFWQueryTokenType.SIGN_BRACE_CURLY_CLOSE) {
						openCount--;
					}
				}
				
				firstPart = QueryPartValue.newJson(CFW.JSON.fromJson(jsonBuilder.toString()) );
				
			break;
				
				
			//=======================================================
			// Start Array Part
			//=======================================================	
			case SIGN_BRACE_SQUARE_OPEN: 
				
				//--------------------------------------
				// Check is Previous Arrayable
				boolean isPreviousArrayable = false;
				CFWQueryToken previousToken = this.lookat(-1);
				if(previousToken != null) {
					isPreviousArrayable =  previousToken.isStringOrText(true, true, true)
										 || previousToken.type() == CFWQueryTokenType.SIGN_BRACE_SQUARE_CLOSE
										 ;
				}
				
				//--------------------------------------
				// Start Array
				firstToken = this.consumeToken();
				
				contextStack.add(CFWQueryParserContext.ARRAY);
				addTrace("Start Part", "Open Array", firstToken.value());
				QueryPartArray arrayPart = new QueryPartArray(currentContext).isEmbracedArray(true);
				firstPart = arrayPart;
				
				QueryPart tempPart = this.parseQueryPart(CFWQueryParserContext.ARRAY);
				
				//--------------------------------------
				//Handle empty arrays and end of array
				if(tempPart != null && !(tempPart instanceof QueryPartEnd)) {
					arrayPart.add(tempPart);
				}
				
				//--------------------------------------
				// Close Array
				if(this.lookahead() != null 
				&& this.lookahead().type() == CFWQueryTokenType.SIGN_BRACE_SQUARE_CLOSE) {
					//TODO check if this is needed >> contextStack.pop(CFWQueryParserContext.ARRAY);
					this.consumeToken();
				}
				
				//--------------------------------------
				// Check if the array is part of a JsonMemberAccess
				if(this.lookahead() != null 
				&& this.lookahead().type() == CFWQueryTokenType.OPERATOR_DOT
				){
					this.consumeToken();
					firstPart = parseJsonMemberAccess(context, arrayPart);
				}
				
				//------------------------------
				// Handle Functions
				if(context == CFWQueryParserContext.FUNCTION) {
					return firstPart;
				}
				
				//--------------------------------------
				//Create member access if previous is string/function and current is array
				if(isPreviousArrayable && arrayPart.isIndex()) { 
					
					QueryPart previousPart = popPreviousPart();
					firstPart = QueryPartJsonMemberAccess.createMemberAccess(currentContext, previousPart, firstPart);
					
//					QueryPart previousPart = popPreviousPart();
//					if(previousPart instanceof QueryPartAssignment) {
//						QueryPartAssignment assignmentPart = (QueryPartAssignment)previousPart;
//						QueryPart leftside = assignmentPart.getLeftSide();
//						QueryPart rightside = assignmentPart.getRightSide();
//						QueryPartJsonMemberAccess memberAccessPart = QueryPartJsonMemberAccess(currentContext, rightside, firstPart);
//						firstPart = new QueryPartAssignment(currentContext, leftside, memberAccessPart);
//					}else if(previousPart instanceof QueryPartBinaryExpression) { 
//						QueryPartBinaryExpression expression = (QueryPartBinaryExpression)previousPart;
//						QueryPartJsonMemberAccess memberAccessPart = new QueryPartJsonMemberAccess(currentContext, expression.getRightSide(), firstPart);
//						firstPart = new QueryPartBinaryExpression(currentContext, expression.getLeftSide(), expression.getOperatorType(), memberAccessPart);
//					}else {
//						QueryPartJsonMemberAccess memberAccessPart = new QueryPartJsonMemberAccess(currentContext, previousPart, firstPart);
//						firstPart = memberAccessPart;
//					}
				}
				
				
			break;	
			
			//=======================================================
			// End Array Part
			//=======================================================	
			case SIGN_BRACE_SQUARE_CLOSE:
				firstToken = this.consumeToken();
				addTrace("End Part", "Close Array", firstToken.value());
				CFWQueryParserContext shouldBeArrayContext = contextStack.pop();	
				if(shouldBeArrayContext != CFWQueryParserContext.ARRAY) {
					this.throwParseException("Expected end of '"+shouldBeArrayContext+"' but found ']'", firstToken.position());
				}
				
			return new QueryPartEnd(null);
										
			//=======================================================
			// Parse Group Part
			//=======================================================							
			case SIGN_BRACE_ROUND_OPEN: 
				firstToken = this.consumeToken();
				
				contextStack.add(CFWQueryParserContext.GROUP);
				addTrace("Start Part", "Group", firstToken.value());

				QueryPart groupMember = this.parseQueryPart(CFWQueryParserContext.GROUP);
				
				int startIndex = currentQueryParts.size();
				
				while( groupMember != null && !(groupMember instanceof QueryPartEnd) ) {

					currentQueryParts.add(groupMember);
					
					CFWQueryToken nextToken = this.lookahead();
					if(nextToken != null) {
						//------------------------------
						// Break if next token is closing group
						if(nextToken.type() == CFWQueryTokenType.SIGN_BRACE_ROUND_CLOSE) {
							this.consumeToken();
							break;
						}
						
						//------------------------------
						// Parse Next Part
						groupMember = this.parseQueryPart(CFWQueryParserContext.GROUP);
					}else {
						this.throwParseException("A opening '(' is missing a matching ')', or a syntax error is preventing it. ", firstToken.position());
					}
				}
				
				//------------------------------
				// Steal all the parts from currentQueryParts
				QueryPartGroup groupPart = new QueryPartGroup(currentContext);
				firstPart = groupPart;
				
				for(; startIndex < currentQueryParts.size(); ) {
					groupPart.add(currentQueryParts.remove(startIndex));
				}
				
				//------------------------------
				// Handle Functions
				if(context == CFWQueryParserContext.FUNCTION) {
					return firstPart;
				}
								
			break;	
			
			//=======================================================
			// End of Group Part
			//=======================================================
			case SIGN_BRACE_ROUND_CLOSE:		
				firstToken = this.consumeToken();
				addTrace("End Part", "Group", firstToken.value());
				CFWQueryParserContext shouldBeGroupContext = contextStack.pop();	
				if(shouldBeGroupContext != CFWQueryParserContext.GROUP) {
					this.throwParseException("Expected end of '"+shouldBeGroupContext+"' but found ')'", firstToken.position());
				}
			return new QueryPartEnd(null);
			
									
			case KEYWORD:
				firstToken = this.consumeToken();
				String keyword = firstToken.value().toUpperCase();
				
				QueryPart lastPart = null;
				if( !keyword.equals(KEYWORD_NOT) && currentQueryParts.size() > 0) { 
					lastPart = popPreviousPart();
				}
				
				switch(keyword) {
				
					case KEYWORD_AND: 	return parseBinaryExpressionPart(context, lastPart, CFWQueryTokenType.OPERATOR_AND, false ); 
					case KEYWORD_OR: 	return parseBinaryExpressionPart(context, lastPart, CFWQueryTokenType.OPERATOR_OR, false ); 
					
					case KEYWORD_NOT: 	return parseBinaryExpressionPart(context, null, CFWQueryTokenType.OPERATOR_NOT, false );
					default:			this.throwParseException("Unknown keyword:"+keyword, firstToken.position());
				}
			break;
									
//			case OPERATOR_NOT:
//
//				break;


			default:					//this.throwParseException("Unexpected token.", firstToken.position());
										break;

		}
		
		
		//########################################################################
		// NEXT TOKEN can be anything that would
		// create a binary expression or complete an expression
		//########################################################################
		QueryPart resultPart = firstPart;

		if(!this.hasMoreTokens()) { return resultPart; }
		//if(context == CFWQueryParserContext.BINARY) { return resultPart; } 
		
		switch(this.lookahead().type()) {
		
			case OPERATOR_OR:		
			case LITERAL_BOOLEAN:
			case LITERAL_NUMBER:
			case LITERAL_STRING:
			case NULL:
									return resultPart;
						
			//------------------------------
			//End of Array Part
			case SIGN_BRACE_SQUARE_CLOSE:
				addTrace("End Part", "Close Array", firstPart);
				
				
				contextStack.add(CFWQueryParserContext.ARRAY);
				
			return resultPart;
			
			//------------------------------
			//End of Group
			case SIGN_BRACE_ROUND_CLOSE:	
				addTrace("End Part", "Close Group", ")");
			return resultPart;
			
	
			
			//------------------------------
			// QueryPartAssignment
			case OPERATOR_EQUAL:
				
				this.consumeToken();

				addTrace("Start Part", "Assignment", "");
				QueryPart rightside = this.parseQueryPart(context);
				if(firstPart == null) {
					firstPart = this.popPreviousPart();
				}
				resultPart = new QueryPartAssignment(currentContext, firstPart, rightside);
				addTrace("End Part", "Assignment", "");
			break;
			
			//------------------------------
			// QueryPartArray						
			case SIGN_COMMA:
				if(context != CFWQueryParserContext.BINARY) {
					if(firstPart == null) {
						firstPart = this.popPreviousPart();
					}
					resultPart = parseArrayPart(context, firstPart);
				}
			break;			
			
			//------------------------------
			// QueryPartJsonMemberAccess		
			case OPERATOR_DOT:
				this.consumeToken();
				resultPart = parseJsonMemberAccess(context, firstPart);
			break;	
			
				
			//------------------------------
			// Binary Operations
			//case OPERATOR_OR: not supported as pipe '|' is used as command separator, see keyword 'OR'
			case OPERATOR_AND:				resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_AND, true ); break;	
			case OPERATOR_EQUAL_EQUAL:		resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_EQUAL_EQUAL, true ); break;	
			case OPERATOR_EQUAL_NOT:		resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_EQUAL_NOT, true ); break;	
			case OPERATOR_EQUAL_OR_GREATER:	resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER, true ); break;	
			case OPERATOR_EQUAL_OR_LOWER:	resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER, true ); break;	
			case OPERATOR_REGEX:			resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_REGEX, true ); break;	
			case OPERATOR_GREATERTHEN:		resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_GREATERTHEN, true ); break;	
			case OPERATOR_LOWERTHEN:		resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_LOWERTHEN, true ); break;	
			case OPERATOR_PLUS:				resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_PLUS, true ); break;	
			case OPERATOR_MINUS:			resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_MINUS, true ); break;	
			case OPERATOR_MULTIPLY:			resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_MULTIPLY, true ); break;	
			case OPERATOR_DIVIDE:			resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_DIVIDE, true ); break;	
			case OPERATOR_POWER:			resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_POWER, true ); break;	
			case OPERATOR_MODULO:			resultPart = parseBinaryExpressionPart(context, firstPart, CFWQueryTokenType.OPERATOR_MODULO, true ); break;	
			case OPERATOR_NOT:
				break;
	
			case FUNCTION_NAME:
				break;
							

			case SIGN_SEMICOLON:
				break;
			case SPLIT:
				break;
			case TEXT_DOUBLE_QUOTES:
				break;
			case TEXT_SINGLE_QUOTES:
				break;
			case UNKNOWN:
				break;
			default:
				break;
			
		}
		
		return resultPart;
	}

	/***********************************************************************************************
	 * Parses a JsonMemberAccess.
	 * @param consumeToken TODO
	 ***********************************************************************************************/
	private QueryPart parseJsonMemberAccess(CFWQueryParserContext context, QueryPart firstPart)
			throws ParseException {
		
		addTrace("Start Part", "JsonMemberAccess", "");

		QueryPart memberAccessPart = null;
		
		while(this.hasMoreTokens()) {
			CFWQueryToken lookahead = this.lookahead();
			if(lookahead.isStringOrText()
			|| lookahead.type() == CFWQueryTokenType.FUNCTION_NAME
			|| lookahead.type() == CFWQueryTokenType.SIGN_BRACE_SQUARE_OPEN) {
				
				//-------------------------------
				// Get Access Value
				QueryPart memberAccessValue = null;
				if(lookahead.isStringOrText()) {
					// Handle String Value
					CFWQueryToken nextToken = this.consumeToken();
					memberAccessValue = QueryPartValue.newString(nextToken.value());
				}else {
					// Handle Array Part Value
					memberAccessValue = this.parseQueryPart(CFWQueryParserContext.JSON_MEMBER_ACCESS);
				}
				
				//-------------------------------
				// Create MemberAccessPart
				if(memberAccessPart != null) {
					// nested parts during while-loop
					memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(currentContext, memberAccessPart, memberAccessValue);
				}else {
					memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(currentContext, firstPart, memberAccessValue);
				}
								
				//-------------------------------
				// Check is there another Dot
				if(this.hasMoreTokens() && this.lookahead().type() == CFWQueryTokenType.OPERATOR_DOT) {
					this.consumeToken();
				}else {
					break;
				}
				
			}else {
				this.throwParseException("Expected string, function or '[fieldname]' after dot operator '.'. ", this.cursor()-1);
			}
			
		}
		
		addTrace("End Part", "JsonMemberAccess", "");
		
		//------------------------------------
		// Return 
		return memberAccessPart;

		
	}

	/***********************************************************************************************
	 * Parses an Array Part.
	 * @param consumeToken TODO
	 ***********************************************************************************************/
	private QueryPart parseArrayPart(CFWQueryParserContext context, QueryPart firstPart) throws ParseException {

		addTrace("Proceed with Array", "Comma encountered", "");
		
		QueryPart firstArrayElement = firstPart;
		QueryPart secondArrayElement = null;
		while(this.lookahead() != null && this.lookahead().type() == CFWQueryTokenType.SIGN_COMMA) {
			this.consumeToken();
			secondArrayElement = this.parseQueryPart(context);
			firstArrayElement = new QueryPartArray(currentContext, firstArrayElement, secondArrayElement);
		}

		return firstArrayElement;
	}
	
	/***********************************************************************************************
	 * Creates a Binary Expression by parsing the Next Part of the expression.
	 * @param context TODO
	 * @param consumeToken TODO
	 ***********************************************************************************************/
	private QueryPart parseBinaryExpressionPart(CFWQueryParserContext context, QueryPart expressionLeftPart, CFWQueryTokenType operatorType, boolean consumeToken ) throws ParseException {
		//-------------------------------------
		// Do Nothing when in JsonMember Context
		if(context == CFWQueryParserContext.JSON_MEMBER_ACCESS) {
			return expressionLeftPart;
			
		}
		
		
		addTrace("Start Part", "Binary Expression", operatorType);
			
			//-------------------------------------
			// Check firstPart
			if(expressionLeftPart == null) {
				expressionLeftPart = this.popPreviousPart();
			}
			
			//-------------------------------------
			// Consume Token
			if(consumeToken) { this.consumeToken(); }
			
			//-------------------------------------
			// Parse Part
			QueryPart expressionRightPart = this.parseQueryPart(CFWQueryParserContext.BINARY);
			
			//-------------------------------------
			// Parse All Subsequent Binary Operations
			while(this.hasMoreTokens() 
			  && this.lookahead().type() != CFWQueryTokenType.OPERATOR_EQUAL // used for assignments
			  && this.lookahead().type() != CFWQueryTokenType.OPERATOR_DOT  // used for JsonMemberAccess
			  && this.lookahead().type() != CFWQueryTokenType.OPERATOR_OR  // used for piping commands
			  && this.lookahead().type().name().startsWith("OPERATOR_")   // any other is used for binary expressions
			  ){
				currentQueryParts.add(expressionRightPart);
				expressionRightPart = this.parseQueryPart(CFWQueryParserContext.BINARY);
				//CFWQueryToken binaryOperator = this.consumeToken();
				//expressionRightPart = parseBinaryExpressionPart(context, expressionRightPart, binaryOperator.type(), false );
			}
			
			
			//-------------------------------------
			// Check Assignment Part
			QueryPart resultPart;
			
			if( ! (expressionLeftPart instanceof QueryPartAssignment) ) {
				resultPart = new QueryPartBinaryExpression(currentContext, expressionLeftPart, operatorType, expressionRightPart);
			}else {
				QueryPartAssignment assignment = (QueryPartAssignment)expressionLeftPart;
				QueryPartBinaryExpression expression = new QueryPartBinaryExpression(currentContext, assignment.getRightSide(), operatorType, expressionRightPart);
				resultPart = new QueryPartAssignment(currentContext, assignment.getLeftSide(), expression);
				
			}
			
		addTrace("End Part", "Binary Expression", "");
		
		return resultPart;
		
		// following is outdated, kept here in case any more problems arise.
		//----------------------------------
		// If next sign is Comma parse Array
//		if(this.hasMoreTokens() 
//		&& this.lookahead().type() != CFWQueryTokenType.SIGN_COMMA) {
//			return expression;
//		}else {
//			return this.parseArrayPart(context, expression);
//		}

	}
	
	/***********************************************************************************************
	 * Create parse exception for a token.
	 * 
	 * @param tokenOffset 0 is the next token to consume, -1 is the last consumed token.
	 ***********************************************************************************************/
	public void throwParseException(String message, int tokenOffset) throws ParseException {
		
		CFWQueryToken token = this.lookat(tokenOffset);
		if(token == null) { throw new ParseException(message, -1); }
		
		throw new ParseException(message+"(token: '"+token.value()+"', position: "+token.position()+", type: "+token.type()+" )", token.position());
	}
	
	/***********************************************************************************************
	 * Create parse exception for a token.
	 * 
	 * @param tokenOffset 0 is the next token to consume, -1 is the last consumed token.
	 ***********************************************************************************************/
	public void throwParseException(String message, QueryPart part) throws ParseException {
		
		if(part == null) { throw new ParseException(message, -1); }
		
		throw new ParseException(message+"(position: "+part.position()+", type: "+part.getClass().getSimpleName()+" )", part.position());
	}
	
	/***********************************************************************************************
	 * Returns a JsonArray with the current state of the Parser. 
	 * Used for debugging
	 ***********************************************************************************************/
	public JsonArray getParserState() {
		
		JsonArray resultArray = new JsonArray();
		
		CFWQueryToken lastToken = this.lookat(-2);
		CFWQueryToken currentToken = this.lookat(-1);
		CFWQueryToken nextToken = this.lookahead();
		
		//-------------------------
		JsonObject object = new JsonObject();
		object.addProperty("KEY", "Description");
		object.addProperty("VALUE", "This result contains debug information to analyze the error which occured during the parsing of the query.");
		resultArray.add(object);
					
		//-------------------------
		object = new JsonObject();
		object.addProperty("KEY", "Query Before Issue");
		object.addProperty("VALUE", this.query.substring(0,lastToken.position()) );
		resultArray.add(object);
		
		//-------------------------
		object = new JsonObject();
		object.addProperty("KEY", "Query After Issue");
		object.addProperty("VALUE", this.query.substring(currentToken.position()) );
		resultArray.add(object);
		
		//-------------------------
		object = new JsonObject();
		object.addProperty("KEY", "Last Token");
		object.add("VALUE", (lastToken == null) ? JsonNull.INSTANCE : lastToken.toJson());
		resultArray.add(object);
		
		//-------------------------
		object = new JsonObject();
		object.addProperty("KEY", "Next Token");
		object.add("VALUE", (nextToken == null) ? JsonNull.INSTANCE : nextToken.toJson());
		resultArray.add(object);
		
		//-------------------------
		object = new JsonObject();
		object.addProperty("KEY", "Current Token");
		object.add("VALUE", (currentToken == null) ? JsonNull.INSTANCE : currentToken.toJson());
		resultArray.add(object);
		
		//-------------------------
		object = new JsonObject();
		object.addProperty("KEY", "Current Token Index");
		object.addProperty("VALUE", cursor-1);
		resultArray.add(object);
		
		//-------------------------
		object = new JsonObject();
		object.addProperty("KEY", "Query Part Count");
		object.addProperty("VALUE", currentQueryParts.size());
		resultArray.add(object);
		
		//-------------------------
		object = new JsonObject();
		object.addProperty("KEY", "Full Query");
		object.addProperty("VALUE", this.query);
		resultArray.add(object);
		
		return resultArray;
	}
	

}
