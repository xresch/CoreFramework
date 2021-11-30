package com.xresch.cfw.features.query.tutorial;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.xresch.cfw.features.query.CFWTokenText;

public class CFWQueryTokenizer {
	
	/*******************************************************
	 * Globals
	 *******************************************************/
	// the original string to be parsed
	String base = null;
	
	// current position in the string
	int cursor = 0;

	//substring(cursor, endOfString) of base string
	String slice = null;
		
	private static final Pattern regexIsDigit = Pattern.compile("\\d");
	private static final Pattern regexIsNumericalChar = Pattern.compile("[\\.\\d]");
	private static final Pattern regexIsWhitespace = Pattern.compile("\\s");
	private static final Pattern regexIsWordChar = Pattern.compile("[a-zA-Z_0-9]");
	
	private Pattern regexIsSplit = null;
	
	/*******************************************************
	 * Enumeration of Token Types
	 *******************************************************/
	public enum CFWQueryTokenType {
		LITERAL_NUMBER
	  , LITERAL_BOOLEAN
	  , LITERAL_STRING
	  
	  , TEXT_SINGLE_QUOTES
	  , TEXT_DOUBLE_QUOTES
	  
	  , SIGN_COMMA
	  , SIGN_BRACE_OPEN
	  , SIGN_BRACE_CLOSE
		
	  , OPERATOR_EQUAL
	  , OPERATOR_PLUS
	  , OPERATOR_MINUS
	  , OPERATOR_MULTIPLY
	  , OPERATOR_DIVIDE
	  , OPERATOR_PIPE
	  , OPERATOR_SPLIT
		
	  , KEYWORD

	}
	
	/*******************************************************
	 * Simple class holding token and token type.
	 *******************************************************/
	public class QueryToken{
		private CFWQueryTokenType type = null;
		private String value = "";
		
		public QueryToken(CFWQueryTokenType type, String value){
			this.type = type;
			this.value = value;
		}
		
		public CFWQueryTokenType type() { return this.type;}
		public String value() { return this.value;}
		public BigDecimal valueAsNumber() { return new BigDecimal(this.value);}
	}
	
		
	/***********************************************************************************************
	 * El Grande Constructore
	 ***********************************************************************************************/
	public CFWQueryTokenizer(String tokenizeThis) {
		this.base = tokenizeThis.trim();		
	}
	
	/***********************************************************************************************
	 * Define the regex used to split the Query.
	 * Parts of the query matching this regex will be converted to a token of type OPERATOR_SPLIT.
	 * The regex has to be written to be applied to a single character.
	 * 
	 ***********************************************************************************************/
	public CFWQueryTokenizer splitBy(String splitRegex) {
		regexIsSplit = Pattern.compile(splitRegex);
		return this;
	}
	
	/***********************************************************************************************
	 * Return true if there are more tokens, false otherwise.
	 * 
	 ***********************************************************************************************/
	public boolean hasMoreTokens() {
		return !isEOF();
	}
	
	/***********************************************************************************************
	 * Parse and return all the tokens as a list.
	 * 
	 ***********************************************************************************************/
	public ArrayList<QueryToken> getAllTokens(){
		ArrayList<QueryToken> tokenList = new ArrayList<>();
		
		while(this.hasMoreTokens()) {
			tokenList.add(getNextToken());
		}
		
		return tokenList;
	}
	
	
	/***********************************************************************************************
	 * Parse and return the next token.
	 * 
	 ***********************************************************************************************/
	public QueryToken getNextToken() {
		//-----------------------------------
		// Skip whitespaces
		while(this.hasMoreTokens() && this.matchesCurrentChar(regexIsWhitespace)) {
			cursor++;
			slice = base.substring(cursor);
		}
		//-----------------------------------
		// Check has more tokens
		if( ! this.hasMoreTokens()) { return null; }
		
		//-----------------------------------
		// Create Slices
		slice = base.substring(cursor);
		int startPos = cursor; 
		
		//-----------------------------------
		// LITERAL_NUMBER
		if(this.matchesCurrentChar(regexIsDigit)) {
			cursor++;
			while( this.matchesCurrentChar(regexIsNumericalChar) ) {
				cursor++;
			}
			return new QueryToken(CFWQueryTokenType.LITERAL_NUMBER, createTokenValue(startPos, cursor));
		}
		
		//-----------------------------------
		// OPERATOR_SPLIT
		if(regexIsSplit != null && this.matchesCurrentChar(regexIsSplit)) {
			cursor++;
			while(this.matchesCurrentChar(regexIsSplit)) {
				cursor++;
			}
			
			return new QueryToken(CFWQueryTokenType.OPERATOR_SPLIT, createTokenValue(startPos, cursor));
		}
		
		//-----------------------------------
		// LITERAL_STRING
		if(this.matchesCurrentChar(regexIsWordChar)) {
			cursor++;
			while(this.matchesCurrentChar(regexIsWordChar)) {
				cursor++;
			}
			
			return new QueryToken(CFWQueryTokenType.LITERAL_STRING, createTokenValue(startPos, cursor));
		}
		
		//-----------------------------------
		// TEXT_DOUBLE_QUOTES
		if( base.charAt(cursor) == '"' ) {
			if(!isCurrentCharEscaped()) {
				this.advancetoQuotedTextEndPosition('"');	
				return new QueryToken(CFWQueryTokenType.TEXT_DOUBLE_QUOTES, base.substring(startPos+1, cursor-1));
			}
		}
		
		//-----------------------------------
		// TEXT_SINGLE_QUOTES
		if( base.charAt(cursor) == '\'' ) {
			if(!isCurrentCharEscaped()) {
				this.advancetoQuotedTextEndPosition('\'');	
				return new QueryToken(CFWQueryTokenType.TEXT_SINGLE_QUOTES, base.substring(startPos+1, cursor-1));
			}
		}
		
		//-----------------------------------
		// Ignore Everything else
		cursor++;
		return null;
		
	}
	
	
	private boolean isEOF() {
		return cursor >= base.length()-1;
	}
		
	private String currentChar(){
		return this.charAt(cursor);
	}
	
	private String charAt(int position){
		int endIndex = position;
		if( endIndex < base.length()-1) { endIndex++; } 
		
		return base.substring(position, endIndex);
	}
	
	
	private boolean matchesCurrentChar(Pattern pattern){
		return pattern.matcher(this.currentChar()).matches();
	}
	

	private String createTokenValue(int startPos, int endPos) {
		if(endPos < base.length()-1) {
			return base.substring(startPos, endPos);
		}else {
			return base.substring(startPos);
		}
		
	}
		
	/***********************************************************************************************
	 * Parses a quoted text.
	 * @param cursor has to be on a position representing quoteChar
	 * @param quoteChar either single or double quote
	 ***********************************************************************************************/
	private void advancetoQuotedTextEndPosition(char quoteChar) {

		while(cursor < base.length()-1) {
			cursor++;
						
			if(base.charAt(cursor) == quoteChar ) {
				if(!isCurrentCharEscaped()) {
					cursor++;
					break;
				}
			}

		}
	}
	
	/***********************************************************************************************
	 * check if the character at the cursors position is escaped with one or multiple backslashes
	 * (\, \\\, \\\\\ etc...).
	 * 
	 * @return true if escaped, false otherwise
	 ***********************************************************************************************/
	private boolean isCurrentCharEscaped() {
		int backslashCount = 0;
		int tempPos = cursor-1; 
		while(base.charAt(tempPos) == '\\') {
			backslashCount++;
			tempPos--;
		}
		
		if(backslashCount % 2 == 0) {
			return false;
		}
		
		return true;
	}
	

}
