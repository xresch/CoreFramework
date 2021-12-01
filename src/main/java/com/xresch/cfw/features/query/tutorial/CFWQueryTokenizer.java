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
		
	private static final Pattern regexStartsWithDigit = Pattern.compile("^[\\-]?\\d.*");
	private static final Pattern regexIsNumericalChar = Pattern.compile("[\\.\\d]");
	private static final Pattern regexIsWhitespace = Pattern.compile("\\s");
	private static final Pattern regexIsWordChar = Pattern.compile("[a-zA-Z_0-9]");
	
	private Pattern regexIsSplit = null;
	
	private boolean keywordsCaseSensitive = true;
	private ArrayList<String> keywordList = new ArrayList<>();
	
	/*******************************************************
	 * Enumeration of Token Types
	 *******************************************************/
	public enum CFWQueryTokenType {
		LITERAL_NUMBER
	  , LITERAL_BOOLEAN
	  , LITERAL_STRING
	  
	  , /** Type for single quoted text  */
	    TEXT_SINGLE_QUOTES
	  , /** Type for double quoted text  */
	  	TEXT_DOUBLE_QUOTES
	  
	  , /** Type for matched split expressions defined by calling method CFWQueryTokenizer.splitBy()  */
	    SPLIT
	  
	  , /** The character '=' */ OPERATOR_EQUAL
	  , /** The characters ">=" */ OPERATOR_EQUAL_OR_GREATER
	  , /** The characters "<=" */ OPERATOR_EQUAL_OR_LOWER
	  , /** The characters "!=" */ OPERATOR_EQUAL_NOT
	  , /** The character '>' */ OPERATOR_GREATERTHEN
	  , /** The character '<' */ OPERATOR_LOWERTHEN
		  
	  , /** The character ',' */ SIGN_COMMA
	  , /** The character '(' */ SIGN_BRACE_OPEN
	  , /** The character ')' */ SIGN_BRACE_CLOSE
		
	  , /** The character '+' */ OPERATOR_PLUS
	  , /** The character '-' */ OPERATOR_MINUS
	  , /** The character '*' */ OPERATOR_MULTIPLY
	  , /** The character '/' */ OPERATOR_DIVIDE
	  , /** The character '&' */ OPERATOR_AND
	  , /** The character '|' */ OPERATOR_OR
	  , /** The character '!' */ OPERATOR_NOT
	  
	  , /** Applied to any keyword defined with the method CFWQueryTokenizer.keywords() */
	    KEYWORD
	  , /** Applied to any unexpected character */
	    UNKNOWN
	  
	}
	
	/*******************************************************
	 * Simple class holding token and token type.
	 *******************************************************/
	public class QueryToken{
		private CFWQueryTokenType type = null;
		private String value = "";
		private int position = 0;
		
		public QueryToken(CFWQueryTokenType type, String value, int position){
			this.type = type;
			this.value = value;
			this.position = position;
		}
		
		public CFWQueryTokenType type() { return this.type;}
		public int position() { return this.position;}
		public String value() { return this.value;}
		public BigDecimal valueAsNumber() { return new BigDecimal(this.value);}
	}
	
		
	/***********************************************************************************************
	 * El Grande Constructore
	 * @param keywordsCaseSensitive TODO
	 ***********************************************************************************************/
	public CFWQueryTokenizer(String tokenizeThis, boolean keywordsCaseSensitive) {
		this.base = tokenizeThis.trim();		
		this.keywordsCaseSensitive = keywordsCaseSensitive;		
	}
	
	/***********************************************************************************************
	 * Define the regex used to split the Query.
	 * Parts of the query matching this regex will be converted to a token of type SPLIT.
	 * The regex has to be written to be applied to a single character.
	 * 
	 ***********************************************************************************************/
	public CFWQueryTokenizer splitBy(String splitRegex) {
		regexIsSplit = Pattern.compile(splitRegex);
		return this;
	}
	
	/***********************************************************************************************
	 * Define the words that should be considered as Keywords. (e.g. AND / OR).
	 * 
	 * 
	 ***********************************************************************************************/
	public CFWQueryTokenizer keywords(String... keywords) {
		
		if(keywordsCaseSensitive) {
			for(String keyword : keywords) {
				keywordList.add(keyword);
			}
		}else {
			for(String keyword : keywords) {
				keywordList.add(keyword.toLowerCase());
			}
		}
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
		// Skip Whitespaces
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
		if(this.matches(regexStartsWithDigit, slice)) {
			cursor++;
			while( this.matchesCurrentChar(regexIsNumericalChar) ) {
				cursor++;
			}
			return createToken(CFWQueryTokenType.LITERAL_NUMBER, startPos, cursor);
		}
		
		//-----------------------------------
		// SPLIT
		if(regexIsSplit != null && this.matchesCurrentChar(regexIsSplit)) {
			cursor++;
			while(this.matchesCurrentChar(regexIsSplit)) {
				cursor++;
			}
			
			return createToken(CFWQueryTokenType.SPLIT, startPos, cursor);
		}
		
		//-----------------------------------
		// SIGNS AND OPERATORS
		
		if(slice.startsWith("!=")) { 	  cursor+=2; return createToken(CFWQueryTokenType.OPERATOR_EQUAL_NOT, startPos, cursor); }
		else if(slice.startsWith("<=")) { cursor+=2; return createToken(CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER, startPos, cursor); } 
		else if(slice.startsWith(">=")) { cursor+=2; return createToken(CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER, startPos, cursor); } 
		
		switch(base.charAt(cursor)) {
			case '=':	return createToken(CFWQueryTokenType.OPERATOR_EQUAL, startPos, cursor);
			case ',':	return createToken(CFWQueryTokenType.SIGN_COMMA, startPos, cursor); 
			case '(':	return createToken(CFWQueryTokenType.SIGN_BRACE_OPEN, startPos, cursor);
			case ')':	return createToken(CFWQueryTokenType.SIGN_BRACE_CLOSE, startPos, cursor);
			case '+':	return createToken(CFWQueryTokenType.OPERATOR_PLUS, startPos, cursor);
			case '-':	return createToken(CFWQueryTokenType.OPERATOR_MINUS, startPos, cursor);
			case '*':	return createToken(CFWQueryTokenType.OPERATOR_MULTIPLY, startPos, cursor);
			case '/':	return createToken(CFWQueryTokenType.OPERATOR_DIVIDE, startPos, cursor);
			case '!':	return createToken(CFWQueryTokenType.OPERATOR_NOT, startPos, cursor); 
			case '&':	return createToken(CFWQueryTokenType.OPERATOR_AND, startPos, cursor);
			case '|':	return createToken(CFWQueryTokenType.OPERATOR_OR, startPos, cursor);
			case '>':	
				System.out.println("startPos:"+startPos);
				System.out.println("cursor:"+(cursor+1));
				System.out.println("length:"+base.length());
						return createToken(CFWQueryTokenType.OPERATOR_GREATERTHEN, startPos, cursor);
			case '<':	return createToken(CFWQueryTokenType.OPERATOR_LOWERTHEN, startPos, cursor);
			
		}
		
		//-----------------------------------
		// KEYWORDS
		if( !keywordList.isEmpty() ) {
			
			String keywordSlice = slice; 
			if(!keywordsCaseSensitive) { keywordSlice = keywordSlice.toLowerCase(); }
			
			for(String keyword : keywordList) {
				if(keywordSlice.startsWith(keyword)) {
					cursor += keyword.length();
					return createToken(CFWQueryTokenType.KEYWORD, startPos, cursor);
				}
			}	
		}
		
		//-----------------------------------
		// LITERAL_BOOLEAN
		if(slice.startsWith("true")) {
			cursor+=4;
			return createToken(CFWQueryTokenType.LITERAL_BOOLEAN, startPos, cursor);
		}
		
		if(slice.startsWith("false")) {
			cursor+=5;
			return createToken(CFWQueryTokenType.LITERAL_BOOLEAN, startPos, cursor);
		}
		
		
		//-----------------------------------
		// LITERAL_STRING
		if(this.matchesCurrentChar(regexIsWordChar)) {
			cursor++;
			while(this.matchesCurrentChar(regexIsWordChar)) {
				cursor++;
			}
			return createToken(CFWQueryTokenType.LITERAL_STRING, startPos, cursor);
		}
		
		//-----------------------------------
		// TEXT_DOUBLE_QUOTES
		if( base.charAt(cursor) == '"' ) {
			if(!isCurrentCharEscaped()) {
				this.advancetoQuotedTextEndPosition('"');	
				
				// do nut use createToken(), will not work if quoted text is at the end of the string
				String textValue = base.substring(startPos+1, cursor-1);
				return new QueryToken(CFWQueryTokenType.TEXT_DOUBLE_QUOTES, textValue, startPos);
			}
		}
		
		//-----------------------------------
		// TEXT_SINGLE_QUOTES
		if( base.charAt(cursor) == '\'' ) {
			if(!isCurrentCharEscaped()) {
				this.advancetoQuotedTextEndPosition('\'');	
				
				// do nut use createToken(), will not work if quoted text is at the end of the string
				String textValue = base.substring(startPos+1, cursor-1);
				return new QueryToken(CFWQueryTokenType.TEXT_SINGLE_QUOTES, textValue, startPos);
			}
		}
		
		//-----------------------------------
		// UNKNOWN
		cursor++;
		return createToken(CFWQueryTokenType.UNKNOWN, startPos+1, cursor-1);
		
	}
	
	
	private boolean isEOF() {
		return cursor >= base.length();
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
		return matches(pattern, this.currentChar());
	}
	
	private boolean matches(Pattern pattern, String string){
		return pattern.matcher(string).matches();
	}
	

	/***********************************************************************************************
	 * Creates the token for the currently parsing positions.
	 ***********************************************************************************************/
	private QueryToken createToken(CFWQueryTokenType type, int startPos, int endPos) {
		
		String tokenValue;
		if(startPos == endPos){
			//----------------------------------------
			// Extract Single Char and advance Cursor
			tokenValue = ""+base.charAt(startPos);
			cursor++;
		}else if(endPos < base.length()-1) {
			//----------------------------------------
			// Extract Multiple Chars, endPos not at EOF
			tokenValue = base.substring(startPos, endPos);
		}else {
			//----------------------------------------
			// Extract Multiple Chars, endPos at EOF
			tokenValue = base.substring(startPos);
		}
		
		return new QueryToken(type, tokenValue, startPos);
		
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
		while( tempPos >= 0 && base.charAt(tempPos) == '\\') {
			backslashCount++;
			tempPos--;
		}
		
		if(backslashCount % 2 == 0) {
			return false;
		}
		
		return true;
	}
	

}
