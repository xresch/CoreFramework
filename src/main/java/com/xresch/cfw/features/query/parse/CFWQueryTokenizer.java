package com.xresch.cfw.features.query.parse;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
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
	public ArrayList<CFWQueryToken> getAllTokens(){
		ArrayList<CFWQueryToken> tokenList = new ArrayList<>();
		
		while(this.hasMoreTokens()) {
			tokenList.add(getNextToken());
		}
		
		return tokenList;
	}
	
	
	/***********************************************************************************************
	 * Parse and return the next token.
	 * 
	 ***********************************************************************************************/
	public CFWQueryToken getNextToken() {
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
			return createToken(CFWQueryToken.CFWQueryTokenType.LITERAL_NUMBER, startPos, cursor);
		}
		
		//-----------------------------------
		// SPLIT
		if(regexIsSplit != null && this.matchesCurrentChar(regexIsSplit)) {
			cursor++;
			while(this.matchesCurrentChar(regexIsSplit)) {
				cursor++;
			}
			
			return createToken(CFWQueryToken.CFWQueryTokenType.SPLIT, startPos, cursor);
		}
		
		//-----------------------------------
		// SIGNS AND OPERATORS
		
		if(slice.startsWith("==")) { 	  cursor+=2; return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_EQUAL_EQUAL, startPos, cursor); }
		if(slice.startsWith("!=")) { 	  cursor+=2; return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_EQUAL_NOT, startPos, cursor); }
		else if(slice.startsWith("<=")) { cursor+=2; return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_EQUAL_OR_LOWER, startPos, cursor); } 
		else if(slice.startsWith(">=")) { cursor+=2; return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_EQUAL_OR_GREATER, startPos, cursor); } 
		
		switch(base.charAt(cursor)) {
			case '=':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_EQUAL, startPos, cursor);
			case ',':	return createToken(CFWQueryToken.CFWQueryTokenType.SIGN_COMMA, startPos, cursor); 
			case ';':	return createToken(CFWQueryToken.CFWQueryTokenType.SIGN_SEMICOLON, startPos, cursor); 
			case '(':	return createToken(CFWQueryToken.CFWQueryTokenType.SIGN_BRACE_OPEN, startPos, cursor);
			case ')':	return createToken(CFWQueryToken.CFWQueryTokenType.SIGN_BRACE_CLOSE, startPos, cursor);
			case '+':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_PLUS, startPos, cursor);
			case '-':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_MINUS, startPos, cursor);
			case '*':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_MULTIPLY, startPos, cursor);
			case '/':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_DIVIDE, startPos, cursor);
			case '!':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_NOT, startPos, cursor); 
			case '.':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_DOT, startPos, cursor); 
			case '&':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_AND, startPos, cursor);
			case '|':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_OR, startPos, cursor);
			case '>':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_GREATERTHEN, startPos, cursor);
			case '<':	return createToken(CFWQueryToken.CFWQueryTokenType.OPERATOR_LOWERTHEN, startPos, cursor);
			
		}
		
		//-----------------------------------
		// NULL
		if(slice.startsWith("null")) {
			cursor+=4;
			return createToken(CFWQueryToken.CFWQueryTokenType.NULL, startPos, cursor);
		}
		
		//-----------------------------------
		// KEYWORDS
		if( !keywordList.isEmpty() ) {
			
			String keywordSlice = slice; 
			if(!keywordsCaseSensitive) { keywordSlice = keywordSlice.toLowerCase(); }
			
			for(String keyword : keywordList) {
				if(keywordSlice.startsWith(keyword)) {
					cursor += keyword.length();
					return createToken(CFWQueryToken.CFWQueryTokenType.KEYWORD, startPos, cursor);
				}
			}	
		}
		
		//-----------------------------------
		// LITERAL_BOOLEAN
		if(slice.startsWith("true")) {
			cursor+=4;
			return createToken(CFWQueryToken.CFWQueryTokenType.LITERAL_BOOLEAN, startPos, cursor);
		}
		
		if(slice.startsWith("false")) {
			cursor+=5;
			return createToken(CFWQueryToken.CFWQueryTokenType.LITERAL_BOOLEAN, startPos, cursor);
		}
		
		
		//-----------------------------------
		// LITERAL_STRING
		if(this.matchesCurrentChar(regexIsWordChar)) {
			cursor++;
			while(this.matchesCurrentChar(regexIsWordChar)) {
				cursor++;
			}
			
			if( !this.currentChar().equals("(") ) {
				return createToken(CFWQueryToken.CFWQueryTokenType.FUNCTION_NAME, startPos, cursor);
			}
		}
		
		//-----------------------------------
		// TEXT_DOUBLE_QUOTES
		if( base.charAt(cursor) == '"' ) {
			if(!isCurrentCharEscaped()) {
				this.advancetoQuotedTextEndPosition('"');	
				
				// do nut use createToken(), will not work if quoted text is at the end of the string
				String textValue = base.substring(startPos+1, cursor-1);
				return new CFWQueryToken(CFWQueryToken.CFWQueryTokenType.TEXT_DOUBLE_QUOTES, textValue, startPos);
			}
		}
		
		//-----------------------------------
		// TEXT_SINGLE_QUOTES
		if( base.charAt(cursor) == '\'' ) {
			if(!isCurrentCharEscaped()) {
				this.advancetoQuotedTextEndPosition('\'');	
				
				// do nut use createToken(), will not work if quoted text is at the end of the string
				String textValue = base.substring(startPos+1, cursor-1);
				return new CFWQueryToken(CFWQueryToken.CFWQueryTokenType.TEXT_SINGLE_QUOTES, textValue, startPos);
			}
		}
		
		//-----------------------------------
		// UNKNOWN
		cursor++;
		return createToken(CFWQueryToken.CFWQueryTokenType.UNKNOWN, startPos+1, cursor-1);
		
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
	private CFWQueryToken createToken(CFWQueryToken.CFWQueryTokenType type, int startPos, int endPos) {
		
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
		
		return new CFWQueryToken(type, tokenValue, startPos);
		
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