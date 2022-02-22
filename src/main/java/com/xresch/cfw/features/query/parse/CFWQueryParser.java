package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;
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
	ArrayList<QueryPart> currentQueryParts = new ArrayList<>(); //initialized here so test cases don't run into nullpointer
	
	// cached instance
	private static CFWQueryCommandSource sourceCommand = new CFWQueryCommandSource(new CFWQuery());
	private static String[] sourceCommandNames = sourceCommand.uniqueNameAndAliases();
	
	private int cursor;
	
	public static final String KEYWORD_AND = "AND";
	public static final String KEYWORD_OR = "OR";
	public static final String KEYWORD_NOT = "NOT";
	
	private boolean enableTracing = false;
	private JsonArray traceArray;
	
	//Used for GIB!-Easteregg
	String obedienceMessage = CFW.Random.randomMessageOfObedience();
	
	//Manage open arrays and groups.
	private Stack<CFWQueryParserContext> contextStack = new Stack<>();
	
	public enum CFWQueryParserContext{
		DEFAULT, BINARY, ARRAY, GROUP
	}
	
	// Stub Class to indicate end of arrays and groups during parsing
	private class QueryPartEnd extends QueryPart {
		public QueryPartEnd(CFWQueryContext context) { super(context); }
		@Override public QueryPartValue determineValue(EnhancedJsonObject object) { return null; }
		@Override public JsonObject createDebugObject(EnhancedJsonObject object) { return null; }
	};
	
	
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
					throw new ParseException("Could not verify permissions for source: "+commandName, commandNameToken.position());
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
				QueryPart part = parseQueryPart(CFWQueryParserContext.DEFAULT, null);
				if(part != null && !(part instanceof QueryPartEnd)) {
					System.out.println("========= add Part ==========");
					System.out.println(CFW.JSON.toJSONPrettyDebugOnly(part.createDebugObject(null)));
					System.out.println("========= add Part End ==========");
					currentQueryParts.add(part);
					addTrace("Parse", "Query Part", part);
				}
			
		}
		
		
		return currentQueryParts;
	}
	
	/***********************************************************************************************
	 * Parses a query part.
	 * @param context TODO
	 * @param previousPart TODO
	 ***********************************************************************************************/
	private QueryPart parseQueryPart(CFWQueryParserContext context, QueryPart previousPart) throws ParseException {
		
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
		QueryPart resultPart = null;
		
		System.out.println("firstToken:"+firstToken.value()+" "+firstToken.type());
		
		switch(firstToken.type()) {								
		
			//=======================================================
			// LITERALS
			//=======================================================	
			case TEXT_SINGLE_QUOTES:
			case TEXT_DOUBLE_QUOTES:	
			case LITERAL_STRING:		addTrace("Create Value Part", "String", firstToken.value());
										resultPart = QueryPartValue.newString(currentContext, firstToken.value());
										//TODO check lookahead, determine if AccessMember, Assignment, Method etc...
										break;
				
			case LITERAL_BOOLEAN:		addTrace("Create Value Part", "Boolean", firstToken.value());
										resultPart = QueryPartValue.newBoolean(currentContext, firstToken.valueAsBoolean());
										break;
									
			case LITERAL_NUMBER:		addTrace("Create Value Part", "Number", firstToken.value());
										resultPart = QueryPartValue.newNumber(currentContext, firstToken.valueAsNumber());
										break;
								
			case NULL: 					addTrace("Create Value Part", "NULL", firstToken.value());
										resultPart = QueryPartValue.newNull(currentContext);
										break;
			
			//=======================================================
			// Array Part Open
			//=======================================================	
			case SIGN_BRACE_SQUARE_OPEN: 
										
				contextStack.add(CFWQueryParserContext.ARRAY);
				addTrace("Start Part", "Array", firstToken.value());
				resultPart = new QueryPartArray(currentContext)
									.isEmbracedArray(true);
				
				secondPart = this.parseQueryPart(CFWQueryParserContext.ARRAY, previousPart);
				
				//Handle empty arrays and end of array
				if(secondPart != null && !(secondPart instanceof QueryPartEnd)) {
					((QueryPartArray)resultPart).add(secondPart);
				}
				
			break;	
			
			//=======================================================
			// Array Contents
			//=======================================================						
			case SIGN_COMMA:
				if(context != CFWQueryParserContext.BINARY) {
					addTrace("Proceed with Array", "Comma encountered", "");
					
					QueryPart firstArrayElement = previousPart;

					QueryPart secondArrayElement = this.parseQueryPart(context, null);
					firstArrayElement = new QueryPartArray(currentContext, firstArrayElement, secondArrayElement);
					
					
					resultPart = firstArrayElement;
				}
			break;	
			
			//=======================================================
			// Close Array END
			//=======================================================	
			case SIGN_BRACE_SQUARE_CLOSE:
			
				addTrace("End Part", "Array", firstToken.value());
				CFWQueryParserContext shouldBeArrayContext = contextStack.pop();	
				if(shouldBeArrayContext != CFWQueryParserContext.ARRAY) {
					this.throwParseException("Expected end of '"+shouldBeArrayContext+"' but found ']'", firstToken.position());
				}
				
			return new QueryPartEnd(null);
										
			//=======================================================
			// Parse Group Part
			//=======================================================							
			case SIGN_BRACE_ROUND_OPEN: 

				contextStack.add(CFWQueryParserContext.GROUP);
				addTrace("Start Part", "Group", firstToken.value());

				QueryPartGroup groupPart = new QueryPartGroup(currentContext);
				
				QueryPart groupMember = this.parseQueryPart(CFWQueryParserContext.GROUP, previousPart);
				
				while(!(groupMember instanceof QueryPartEnd) ) {
					
					//handle AND & OR
//					CFWQueryToken nextToken = this.lookahead();
//					if(nextToken != null 
//					&& nextToken.type() == CFWQueryTokenType.KEYWORD 	
//					&& (
//							nextToken.value().toUpperCase().equals(KEYWORD_AND) 
//						|| nextToken.value().toUpperCase().equals(KEYWORD_OR) 
//						)
//					
//					) {
//						 groupMember = this.parseQueryPart(CFWQueryParserContext.GROUP, groupMember); 
//					}
										
					groupPart.add(groupMember);			
					if(this.hasMoreTokens()){
						groupMember = this.parseQueryPart(CFWQueryParserContext.GROUP, groupMember);
					}else {
						break;
					}
				}
				
				resultPart = groupPart;
				
			break;	
			
			//=======================================================
			// End of Group Part
			//=======================================================
			case SIGN_BRACE_ROUND_CLOSE:								
				addTrace("End Part", "Group", firstToken.value());
				CFWQueryParserContext shouldBeGroupContext = contextStack.pop();	
				if(shouldBeGroupContext != CFWQueryParserContext.GROUP) {
					this.throwParseException("Expected end of '"+shouldBeGroupContext+"' but found ')'", firstToken.position());
				}
			return new QueryPartEnd(null);
			
			//=======================================================
			// Keywords
			//=======================================================						
			case KEYWORD:
				String keyword = firstToken.value().toUpperCase();
								
				switch(keyword) {
				
					case KEYWORD_AND: 	return createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_AND, CFWQueryParserContext.DEFAULT ); 
					case KEYWORD_OR: 	return createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_OR, CFWQueryParserContext.DEFAULT ); 
					
					case KEYWORD_NOT: 	return createBinaryExpressionPart(null, CFWQueryTokenType.OPERATOR_NOT, CFWQueryParserContext.DEFAULT );
					default:			this.throwParseException("Unknown keyword:"+keyword, firstToken.position());
				}
			break;

			//=======================================================
			// Assignment Part
			//=======================================================	
			case OPERATOR_EQUAL:
				addTrace("Start Part", "Assignment", "");
				QueryPart rightside = this.parseQueryPart(context, null);
				resultPart = new QueryPartAssignment(currentContext, previousPart, rightside);
				addTrace("End Part", "Assignment", "");
			break;

			default:		
				//this.throwParseException("Unexpected token.", firstToken.position());
			break;
			
			//=======================================================
			// Binary Expressions
			//=======================================================	

			//case OPERATOR_OR: not supported as pipe '|' is used as command separator, see keyword 'OR'
			case OPERATOR_AND:				resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_AND, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_EQUAL_EQUAL:		resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_EQUAL_EQUAL, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_EQUAL_NOT:		resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_EQUAL_NOT, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_EQUAL_OR_GREATER:	resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_EQUAL_OR_LOWER:	resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_GREATERTHEN:		resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_GREATERTHEN, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_LOWERTHEN:		resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_LOWERTHEN, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_PLUS:				resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_PLUS, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_MINUS:			resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_MINUS, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_MULTIPLY:			resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_MULTIPLY, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_DIVIDE:			resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_DIVIDE, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_POWER:			resultPart = createBinaryExpressionPart(previousPart, CFWQueryTokenType.OPERATOR_POWER, CFWQueryParserContext.BINARY ); break;	
			case OPERATOR_NOT:
				break;

		}
		
		
		//##################################################################
		// Check if next token should be fetched or 
		//##################################################################
		if(!this.hasMoreTokens()) { return resultPart; }
		if(context == CFWQueryParserContext.BINARY) { return resultPart; }
		
		
		//##################################################################
		// Check if Next Token would create a Composite Expression
		//##################################################################
		//handle Operators, Keywords AND & OR
		

		
		CFWQueryToken nextToken = this.lookahead();

		//------------------------------
		// Keep parsing array elements
		if(nextToken.type() == CFWQueryTokenType.SIGN_COMMA) {
			resultPart = parseQueryPart(CFWQueryParserContext.DEFAULT, resultPart);
		}
		
		//------------------------------
		// Create Binary Expressions 
		if(   
			   nextToken.isOperator() 
		    && nextToken.type() != CFWQueryTokenType.OPERATOR_OR
		    && nextToken.type() != CFWQueryTokenType.OPERATOR_NOT
		    ) {
			resultPart = parseQueryPart(CFWQueryParserContext.DEFAULT, resultPart);
		}
		
		//------------------------------
		// Create Keyword Expressions
		if(nextToken.isKeyword()
			&&( 
				 nextToken.value().toUpperCase().equals(KEYWORD_AND) 
			  || nextToken.value().toUpperCase().equals(KEYWORD_OR) 
			)
		){
			resultPart = parseQueryPart(CFWQueryParserContext.DEFAULT, resultPart);
		}
			
		
		return resultPart;
	}
	
	/***********************************************************************************************
	 * Creates a Binary Expression by parsing the Next Part of the expression.
	 * @param contextType TODO
	 ***********************************************************************************************/
	private QueryPart createBinaryExpressionPart(QueryPart leftside, CFWQueryTokenType operatorType, CFWQueryParserContext contextType ) throws ParseException {
		addTrace("Start Part", "Binary Expression", operatorType);
			QueryPart secondPart = this.parseQueryPart(contextType, null);
		addTrace("End Part", "Binary Expression", "");
		
		return new QueryPartBinaryExpression(currentContext, leftside, operatorType, secondPart);
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
