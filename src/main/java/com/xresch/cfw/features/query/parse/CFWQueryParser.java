package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
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
	
	private ArrayList<CFWQueryToken> tokenlist;
	private CFWQuery currentQuery;
	private CFWQueryContext currentContext;
	
	private int cursor;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryParser(String inputQuery) {
		
		this.query = inputQuery;
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
	 * @param offset positive or negative value to specify offset.
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
	 * 
	 ***********************************************************************************************/
	public ArrayList<CFWQuery> parseQuery() throws ParseException {
		
		tokenlist = new CFWQueryTokenizer(this.query, false)
			.keywords("AND", "OR", "NOT")
			.getAllTokens();
		
		ArrayList<CFWQuery> queryList = new ArrayList<>();
		
		while(this.hasMoreTokens()) {
			
			CFWQuery query = new CFWQuery();
			currentQuery = query;
			currentContext = query.getContext();
			
			while(this.lookahead().type() != CFWQueryTokenType.SIGN_SEMICOLON) {
				query.addCommand(parseQueryCommand(query));
			}
			
			if(this.hasMoreTokens() && this.lookahead().type() == CFWQueryTokenType.SIGN_SEMICOLON) {
				this.consumeToken();
			}
			
			queryList.add(query);

		}
		
		return queryList;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommand parseQueryCommand(CFWQuery parentQuery) throws ParseException {
		
		//------------------------------------
		// Check Has More Tokens
		if(!this.hasMoreTokens()) {
			this.throwParseException("Query cannot end with a pipe symbol", 0);
		}
		
		//------------------------------------
		// Skip '|'
		if(this.lookahead().type() == CFWQueryTokenType.OPERATOR_OR) {
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
			if(CFW.Registry.Query.commandExists(commandName)) {
				
			}else {
				this.throwParseException("Unknown command '"+commandName+"'.", 0);
			}
			
			//------------------------------------
			// ParsePartsUntil End of Command '|'
			ArrayList<QueryPart> parts = parseQueryPartsUntil(CFWQueryTokenType.OPERATOR_OR);
			
			//------------------------------------
			// Create Command instance
			CFWQueryCommand command = CFW.Registry.Query.createCommandInstance(commandName);
			
			command.setAndValidateQueryParts(this, parts);
			
		}else {
			this.throwParseException("Expected command name.", 0);
		}
		
		return null;
		
	}
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public ArrayList<QueryPart> parseQueryPartsUntil(CFWQueryTokenType untilType) throws ParseException {
		
		ArrayList<QueryPart> parts = new ArrayList<>();
		
		while(this.lookahead().type() != untilType) {
			
			CFWQueryToken currentToken = this.consumeToken();
			switch(currentToken.type()) {
			case LITERAL_STRING:	QueryPartValue stringPart = QueryPartValue.newString(currentContext, currentToken.value());
									//TODO check lookahead, determine if AccessMember, Assignment, Method etc...
									break;
				
			case LITERAL_BOOLEAN:	parts.add(QueryPartValue.newBoolean(currentContext, currentToken.valueAsBoolean()));
									break;
			case LITERAL_NUMBER:	parts.add(QueryPartValue.newBoolean(currentContext, currentToken.valueAsBoolean()));
									break;
			case NULL:
				break;
			case KEYWORD:
				break;
				
			case OPERATOR_AND:
				break;
			case OPERATOR_DIVIDE:
				break;
			case OPERATOR_DOT:
				break;
			case OPERATOR_EQUAL:
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
			case OPERATOR_OR:
				break;
			case OPERATOR_PLUS:
				break;
			case SIGN_BRACE_CLOSE:
				break;
			case SIGN_BRACE_OPEN:
				break;
			case SIGN_COMMA:
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
		}
		
		
		return parts;
	}
	
	
	
	
	/***********************************************************************************************
	 * Create parse exception for a token.
	 * 
	 * @param tokenOffset 0 is the next token to consume, -1 is the last consumed token.
	 ***********************************************************************************************/
	public void throwParseException(String message, int tokenOffset) throws ParseException {
		CFWQueryToken token = this.lookat(tokenOffset);
		throw new ParseException(message+"(token: '"+token.value()+"', position: "+token.position()+", type: "+token.type()+" )", token.position());
	}
	
	/***********************************************************************************************
	 * Create parse exception for a token.
	 * 
	 * @param tokenOffset 0 is the next token to consume, -1 is the last consumed token.
	 ***********************************************************************************************/
	public void throwParseException(String message, QueryPart part) throws ParseException {
		throw new ParseException(message+"(position: "+part.position()+", type: "+part.getClass().getSimpleName()+" )", part.position());
	}
	

}