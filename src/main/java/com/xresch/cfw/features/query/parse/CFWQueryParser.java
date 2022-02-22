package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;

/**************************************************************************************************************
 * 
 * <CFWQuery> ::= <CFWQueryCommand>+ 
 * 
 * Commands are splitted by '|'
 * <QueryPartCommand>			::= <commandName> (<QueryPart>)*
 * 
 * 		<commandName> 			::= <LITERAL_STRING>
 * 		
 * 		<QueryPart> 			::= (<QueryPartValue>|<QueryPartList>|<QueryPartFunction>|<QueryPartAssignment>)+
 * 			<QueryPartValue>	::= (<LITERAL>|<GROUP_EXPRESSION>)
 * 			<QueryPartList>		::= <QueryPart> <SIGN_COMMA> <QueryPart> <SIGN_COMMA> ... <QueryPart>
 * 
 * 		<KEYWORD> ::= <customized LITERAL_STRING defined by CFWQueryTokenizer.keywords() >
 * 
 * 		<EXPRESSION> ::= <EXPRESSION_GROUP> | <EXPRESSION_ASSIGNMENT> | <EXPRESSION_ACCESSMEMBER> |<EXPRESSION_BINARY> | <EXPRESSION_FUNCTION> 
 * 
 * 			<EXPRESSION_GROUP> ::= <ANY>*
 * 			
 * 			<EXPRESSION_ASSIGNMENT> ::= (<LITERAL>|<TEXT>) <OPERATOR_EQUAL> (<EXPRESSION>|<LITERAL>)
 * 
 * 			<EXPRESSION_ACCESSMEMBER> ::= (<LITERAL>|<TEXT>) <OPERATOR_DOT>  (<LITERAL>|<TEXT>) 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryParser {
	
	private String query = null;
	private boolean checkSourcePermissions = true;
	
	private ArrayList<CFWQueryToken> tokenlist;
	CFWQuery currentQuery;
	private CFWQueryContext currentContext;
	ArrayList<QueryPart> currentQueryParts;
	
	// cached instance
	private static CFWQueryCommandSource sourceCommand = new CFWQueryCommandSource(new CFWQuery());
	private static String[] sourceCommandNames = sourceCommand.uniqueNameAndAliases();
	
	private int cursor;
	
	public static final String KEYWORD_AND = "AND";
	public static final String KEYWORD_OR = "OR";
	public static final String KEYWORD_NOT = "NOT";
	
	private boolean enableTracing = false;
	private JsonArray traceArray;
	
	//Used for GIB-Easteregg
	String obedienceMessage = CFW.Random.randomMessageOfObedience();
	
	public enum ParsingContext{
		DEFAULT, BINARY, ARRAY
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryParser(String inputQuery, boolean checkSourcePermissions) {
		
		this.query = inputQuery;
		this.checkSourcePermissions = checkSourcePermissions;
		this.cursor = 0;
		
		tokenlist = new CFWQueryTokenizer(this.query, false, true)
				.keywords(KEYWORD_AND
						, KEYWORD_OR
						, KEYWORD_NOT)
				.getAllTokens();
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryParser enableTracing() {
		enableTracing = true;
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
	 * Add a trace object to the trace array.
	 * @param key a key for your trace
	 * @param value the value related to your key (use for smaller values)
	 * @param message for your trace (use for longer values)
	 ***********************************************************************************************/
	public void addTrace(Object key, Object value, Object message) {
		if(enableTracing) {
			JsonObject object = new JsonObject();
			object.addProperty("Key", 		(key != null) ? key.toString() : null);
			object.addProperty("Value", 	(value != null) ? value.toString() : null);
			object.addProperty("Message", 	
					(message != null) ? 
						(
							(!(message instanceof QueryPart)) ? message.toString() : ((QueryPart)message).createDebugObject(null).toString()
						)
						: null
			);
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
	public JsonArray getTraceResults() {
		return traceArray;
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
			
			if( query != null && query.getCommandList().size() > 0) {
				
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
		
		currentQuery = new CFWQuery();
		currentContext = currentQuery.getContext();
		
		while(this.hasMoreTokens() && this.lookahead().type() != CFWQueryTokenType.SIGN_SEMICOLON) {
			
			CFWQueryCommand command = parseQueryCommand(currentQuery);
			if(command != null) {
				currentQuery.addCommand(command);
			}
		}

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
			// GIB-Easteregg for Vincent Theus
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
				QueryPart part = parseQueryPart(ParsingContext.DEFAULT);
				currentQueryParts.add(part);
			addTrace("Parse", "Query Part", part);
		}
		
		
		return currentQueryParts;
	}
	
	/***********************************************************************************************
	 * Parses a query part.
	 * @param context TODO
	 ***********************************************************************************************/
	public QueryPart parseQueryPart(ParsingContext context) throws ParseException {
		

		QueryPart secondPart = null;

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
		CFWQueryToken firstToken = this.consumeToken();
		QueryPart firstPart = null;
		
		//System.out.println("firstToken:"+firstToken.value()+" "+firstToken.type());
		
		switch(firstToken.type()) {								
		
			case TEXT_SINGLE_QUOTES:
			case TEXT_DOUBLE_QUOTES:	
			case LITERAL_STRING:		addTrace("Create Value Part", "String", firstToken.value());
										firstPart = QueryPartValue.newString(currentContext, firstToken.value());
										//TODO check lookahead, determine if AccessMember, Assignment, Method etc...
										break;
				
			case LITERAL_BOOLEAN:		addTrace("Create Value Part", "Boolean", firstToken.value());
										firstPart = QueryPartValue.newBoolean(currentContext, firstToken.valueAsBoolean());
										break;
									
			case LITERAL_NUMBER:		addTrace("Create Value Part", "Number", firstToken.value());
										firstPart = QueryPartValue.newNumber(currentContext, firstToken.valueAsNumber());
										break;
								
			case NULL: 					addTrace("Create Value Part", "NULL", firstToken.value());
										firstPart = QueryPartValue.newNull(currentContext);
										break;
	
			case SIGN_BRACE_SQUARE_OPEN: 
										//------------------------------
										// QueryPartArray
										addTrace("Start Part", "Open Array", firstToken.value());
										firstPart = new QueryPartArray(currentContext)
															.isEmbracedArray(true);
										
										secondPart = this.parseQueryPart(context);
										//Handle empty arrays
										if(secondPart != null) {
											((QueryPartArray)firstPart).add(secondPart);
										}
											
										break;	
			
			case SIGN_BRACE_SQUARE_CLOSE:
			case SIGN_BRACE_ROUND_CLOSE:
										//------------------------------
										//End of Query Part
										addTrace("End Part", "Close Array or Group", firstToken.value());
										return null;
									
			case KEYWORD:
				String keyword = firstToken.value().toUpperCase();
				
				QueryPart lastPart = null;
				if( !keyword.equals(KEYWORD_NOT) && currentQueryParts.size() > 0) { 
					lastPart = popPreviousPart();
				}else {
					if(!keyword.equals(KEYWORD_NOT)){
						this.throwParseException("Keyword cannot be at the beginning of the command: "+keyword, firstToken.position()); 
					}
				}
				
				
				switch(keyword) {
				
					case KEYWORD_AND: 	return createBinaryExpressionPart(lastPart, CFWQueryTokenType.OPERATOR_AND, false ); 
					case KEYWORD_OR: 	return createBinaryExpressionPart(lastPart, CFWQueryTokenType.OPERATOR_OR, false ); 
					
					case KEYWORD_NOT: 	return createBinaryExpressionPart(null, CFWQueryTokenType.OPERATOR_NOT, false );
					default:			this.throwParseException("Unknown keyword:"+keyword, firstToken.position());
				}
			break;
//									
//			case OPERATOR_NOT:
//				break;
//
//			case FUNCTION_NAME:
//				break;
			default:					//this.throwParseException("Unexpected token.", firstToken.position());
										break;

		}
		
		
		//------------------------------------------
		// NEXT TOKEN can be anything that would
		// create a binary expression or complete an expression
		QueryPart resultPart = firstPart;

		if(!this.hasMoreTokens()) { return resultPart; }
		
		switch(this.lookahead().type()) {
		case OPERATOR_OR:		
		case LITERAL_BOOLEAN:
		case LITERAL_NUMBER:
		case LITERAL_STRING:
		case NULL:
								return resultPart;
		
		//------------------------------
		//End of Query Part
		case SIGN_BRACE_SQUARE_CLOSE:
		case SIGN_BRACE_ROUND_CLOSE:	
			addTrace("End Part", "Close Array or Group", "");
			this.consumeToken();
			return resultPart;

		
		//------------------------------
		// QueryPartAssignment
		case OPERATOR_EQUAL:
			this.consumeToken();
			addTrace("Start Part", "Assignment", "");
			QueryPart rightside = this.parseQueryPart(context);
			resultPart = new QueryPartAssignment(currentContext, firstPart, rightside);
			addTrace("End Part", "Assignment", "");
		break;
		
		//------------------------------
		// QueryPartArray						
		case SIGN_COMMA:
			if(context != ParsingContext.BINARY) {
				addTrace("Proceed with Array", "Comma encountered", "");
				
				QueryPart firstArrayElement = firstPart;
				QueryPart secondArrayElement = firstPart;
				while(this.lookahead() != null && this.lookahead().type() == CFWQueryTokenType.SIGN_COMMA) {
					this.consumeToken();
					secondArrayElement = this.parseQueryPart(context);
					firstArrayElement = new QueryPartArray(currentContext, firstArrayElement, secondArrayElement);
				}
				
				resultPart = firstArrayElement;
			}
		break;			
		
						
		//------------------------------
		// Binary Operations
		//case OPERATOR_OR: not supported as pipe '|' is used as command separator, see keyword 'OR'
		case OPERATOR_AND:				resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_AND, true ); break;	
		case OPERATOR_EQUAL_EQUAL:		resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_EQUAL_EQUAL, true ); break;	
		case OPERATOR_EQUAL_NOT:		resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_EQUAL_NOT, true ); break;	
		case OPERATOR_EQUAL_OR_GREATER:	resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER, true ); break;	
		case OPERATOR_EQUAL_OR_LOWER:	resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER, true ); break;	
		case OPERATOR_GREATERTHEN:		resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_GREATERTHEN, true ); break;	
		case OPERATOR_LOWERTHEN:		resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_LOWERTHEN, true ); break;	
		case OPERATOR_PLUS:				resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_PLUS, true ); break;	
		case OPERATOR_MINUS:			resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_MINUS, true ); break;	
		case OPERATOR_MULTIPLY:			resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_MULTIPLY, true ); break;	
		case OPERATOR_DIVIDE:			resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_DIVIDE, true ); break;	
		case OPERATOR_POWER:			resultPart = createBinaryExpressionPart(firstPart, CFWQueryTokenType.OPERATOR_POWER, true ); break;	
		case OPERATOR_NOT:
			break;


		case SIGN_BRACE_ROUND_OPEN:
			break;

		case FUNCTION_NAME:
			break;
			

			
		case OPERATOR_DOT:
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
	 * Creates a Binary Expression by parsing the Next Part of the expression.
	 * @param consumeToken TODO
	 ***********************************************************************************************/
	private QueryPart createBinaryExpressionPart(QueryPart firstPart, CFWQueryTokenType operatorType, boolean consumeToken ) throws ParseException {
		addTrace("Start Part", "Binary Expression", operatorType);
			if(consumeToken) { this.consumeToken(); }
			QueryPart secondPart = this.parseQueryPart(ParsingContext.BINARY);
		addTrace("End Part", "Binary Expression", "");
		return new QueryPartBinaryExpression(currentContext, firstPart, operatorType, secondPart);
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
	

}
