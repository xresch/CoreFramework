package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;

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
	
	// cached instance
	private static CFWQueryCommandSource sourceCommand = new CFWQueryCommandSource(new CFWQuery());
	private static String[] sourceCommandNames = sourceCommand.uniqueNameAndAliases();
	
	private int cursor;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryParser(String inputQuery, boolean checkSourcePermissions) {
		
		this.query = inputQuery;
		this.checkSourcePermissions = checkSourcePermissions;
		this.cursor = 0;
		
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
		
		tokenlist = new CFWQueryTokenizer(this.query, false)
			.keywords("AND", "OR", "NOT")
			.getAllTokens();
		
		ArrayList<CFWQuery> queryList = new ArrayList<>();
		//-----------------------------------
		// Skip preceding semicolons
		if(this.lookahead().type() == CFWQueryTokenType.SIGN_SEMICOLON) {
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
	 * 
	 ***********************************************************************************************/
	private CFWQuery parseQuery() throws ParseException {
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
				
		return currentQuery;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private CFWQueryCommand parseQueryCommand(CFWQuery parentQuery) throws ParseException {
		
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
			
			return command;
		}else {
			this.throwParseException("Expected command name.", 0);
		}
		
		return null;
		
	}
		
	/***********************************************************************************************
	 * Parse Parts Until End of Command ('|') or query (';')
	 ***********************************************************************************************/
	private ArrayList<QueryPart> parseQueryParts() throws ParseException {
		
		ArrayList<QueryPart> parts = new ArrayList<>();
		
		
		while(this.hasMoreTokens() 
		   && this.lookahead().type() != CFWQueryTokenType.OPERATOR_OR
		   && this.lookahead().type() != CFWQueryTokenType.SIGN_SEMICOLON) {
			
			parts.add(parseQueryPart());
		}
		
		
		return parts;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private QueryPart parseQueryPart() throws ParseException {
		
		QueryPart tempPart = null;

		if(!this.hasMoreTokens()) {
			throw new ParseException("Unexpected end of query.", cursor);
		}
		//------------------------------------------
		// FIRST TOKEN EXPECT: 
		//   LITERAL, 
		//   QUOTED_TEXT,
		//   NULL,
		//   KEYWORD,
		//   OPERATOR_NOT,
		//   SIGN_BRACE_ROUND_OPEN,
		//   FUNCTION_NAME
		CFWQueryToken firstToken = this.consumeToken();
		QueryPart firstPart = null;
		
		//System.out.println("firstToken:"+firstToken.value()+" "+firstToken.type());
		
		switch(firstToken.type()) {								
		
			case TEXT_SINGLE_QUOTES:
			case TEXT_DOUBLE_QUOTES:	
			case LITERAL_STRING:		firstPart = QueryPartValue.newString(currentContext, firstToken.value());
										//TODO check lookahead, determine if AccessMember, Assignment, Method etc...
										break;
				
			case LITERAL_BOOLEAN:		firstPart = QueryPartValue.newBoolean(currentContext, firstToken.valueAsBoolean());
										break;
									
			case LITERAL_NUMBER:		firstPart = QueryPartValue.newNumber(currentContext, firstToken.valueAsNumber());
										break;
								
			case NULL: 					firstPart = QueryPartValue.newNull(currentContext);
										break;
	
			case SIGN_BRACE_SQUARE_OPEN: 
										//------------------------------
										// QueryPartArray
										tempPart = this.parseQueryPart();
										firstPart = new QueryPartArray(currentContext).add(tempPart);

										break;	
										
//			case KEYWORD:				firstPart = QueryPartValue.newNull(currentContext);
//										break;
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
		// NEXT TOKEN can basically be anything
		QueryPart resultPart = firstPart;

		
		if(!this.hasMoreTokens()) { return resultPart; }
		
		switch(this.lookahead().type()) {
		case OPERATOR_OR:		
		case LITERAL_BOOLEAN:
		case LITERAL_NUMBER:
		case LITERAL_STRING:
		case NULL:
								return resultPart;
								
		case SIGN_BRACE_SQUARE_CLOSE:
		case SIGN_BRACE_ROUND_CLOSE:
								//------------------------------
								//End of Query Part
								this.consumeToken();
								return resultPart;
		
		case OPERATOR_EQUAL:	//------------------------------
								// QueryPartAssignment
								this.consumeToken();
								QueryPart rightside = this.parseQueryPart();
								resultPart = new QueryPartAssignment(currentContext, firstPart, rightside);
								break;
		
		case SIGN_COMMA:		//------------------------------
								// QueryPartArray
								this.consumeToken();
								tempPart = this.parseQueryPart();
								resultPart = new QueryPartArray(currentContext, firstPart, tempPart);
								break;			
		
			
		case FUNCTION_NAME:
			break;
		case KEYWORD:
			break;

		case OPERATOR_AND:
			break;
		case OPERATOR_DIVIDE:
			break;
		case OPERATOR_DOT:
			break;

		case OPERATOR_EQUAL_EQUAL:
			break;
		case OPERATOR_EQUAL_NOT:
			break;
		case OPERATOR_EQUAL_OR_GREATER:
			break;
		case OPERATOR_EQUAL_OR_LOWER:
			break;
		case OPERATOR_GREATERTHEN:
			break;
		case OPERATOR_LOWERTHEN:
			break;
		case OPERATOR_MINUS:
			break;
		case OPERATOR_MULTIPLY:
			break;
		case OPERATOR_NOT:
			break;

		case OPERATOR_PLUS:
			break;

		case SIGN_BRACE_ROUND_OPEN:
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
