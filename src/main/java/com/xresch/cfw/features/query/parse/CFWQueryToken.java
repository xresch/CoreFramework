package com.xresch.cfw.features.query.parse;

import java.math.BigDecimal;

import com.google.gson.JsonObject;

/**************************************************************************************************************
 * Simple class holding token, token type and position of the token in the string.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryToken{
	
	private CFWQueryToken.CFWQueryTokenType type = null;
	private String value = "";
	private int position = 0;
	
	public CFWQueryToken(CFWQueryToken.CFWQueryTokenType type, String value, int position){
		this.type = type;
		this.value = value;
		this.position = position;
	}
	
	/*******************************************************
	 * Enumeration of Token Types
	 *******************************************************/
	public enum CFWQueryTokenType {
		/** Type for all numbers, integer or float  */
		LITERAL_NUMBER
		
		/** Type for boolean literals (true, false)  */
	  , LITERAL_BOOLEAN
	  
	    /** Type for unquoted strings that are not matched by other types like LITERAL_BOOLEAN, TEXT_*, KEYWORD  */
	  , LITERAL_STRING
	
	  , /** Type for single quoted text, quotes will be removed  */
	    TEXT_SINGLE_QUOTES
	    
	  , /** Type for double quoted text, quotes will be removed */
	  	TEXT_DOUBLE_QUOTES
	
	  , /** Type for backticks quoted text, backticks will be removed */
		TEXT_BACKTICKS
		
	  , /** Type for matched split expressions defined by calling method CFWQueryTokenizer.splitBy()  */
	    SPLIT
	    
	  , /** Type for string literals that are immediately followed by SIGN_BRACE_OPEN '('  */
		FUNCTION_NAME
	
	  , /** The character '=' */ OPERATOR_EQUAL
	  , /** The characters ">=" */ OPERATOR_EQUAL_OR_GREATER
	  , /** The characters "<=" */ OPERATOR_EQUAL_OR_LOWER
	  , /** The characters "==" */ OPERATOR_EQUAL_EQUAL
	  , /** The characters "!=" */ OPERATOR_EQUAL_NOT
	  , /** The characters "~=" */ OPERATOR_REGEX
	  , /** The character '>' */ OPERATOR_GREATERTHEN
	  , /** The character '<' */ OPERATOR_LOWERTHEN
		  		
	  , /** The character '+' */ OPERATOR_PLUS
	  , /** The character '-' */ OPERATOR_MINUS
	  , /** The character '*' */ OPERATOR_MULTIPLY
	  , /** The character '/' */ OPERATOR_DIVIDE
	  , /** The character '^' */ OPERATOR_POWER
	  , /** The character '%' */ OPERATOR_MODULO
	  , /** The character '&' */ OPERATOR_AND
	  , /** The character '|' */ OPERATOR_OR
	  , /** The character '!' */ OPERATOR_NOT
	  , /** The character '.' */ OPERATOR_DOT
	  
	  , /** The character ',' */ SIGN_COMMA
	  , /** The character ':' */ SIGN_COLON
	  , /** The character ';' */ SIGN_SEMICOLON
	  , /** The character '(' */ SIGN_BRACE_ROUND_OPEN
	  , /** The character ')' */ SIGN_BRACE_ROUND_CLOSE
	  , /** The character '[' */ SIGN_BRACE_SQUARE_OPEN
	  , /** The character ']' */ SIGN_BRACE_SQUARE_CLOSE
	  , /** The character '{' */ SIGN_BRACE_CURLY_OPEN
	  , /** The character '}' */ SIGN_BRACE_CURLY_CLOSE
	  , /** The character '#' */ SIGN_HASH

	
	  , /** Applied to any keyword defined with the method CFWQueryTokenizer.keywords() */
	    KEYWORD
	    
	  , /** The literal string sequence "null" */
		NULL
		
	  , /** Applied to any unexpected character */
	    UNKNOWN
	
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryToken.CFWQueryTokenType type() { 
		return this.type;
	}
	

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public int position() { 
		return this.position;
	}
	
	/***********************************************************************************************
	 * Returns the value, strings without any quotation.
	 ***********************************************************************************************/
	public String value() { 
		return this.value;
	}
	
	/***********************************************************************************************
	 * Returns the value, strings with original quotation.
	 ***********************************************************************************************/
	public String valueQuotedStrings() { 
		
		switch(type) {
			case TEXT_DOUBLE_QUOTES:	return "\""+value+"\"";
			case TEXT_SINGLE_QUOTES:	return "'"+value+"'";
			case TEXT_BACKTICKS:		return "`"+value+"`";
			default:					return value;
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public BigDecimal valueAsNumber() { 
		return new BigDecimal(this.value);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public Boolean valueAsBoolean() { 
		return Boolean.parseBoolean(this.value);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isLiteral() { 
		return type == CFWQueryToken.CFWQueryTokenType.LITERAL_STRING
			|| type == CFWQueryToken.CFWQueryTokenType.LITERAL_NUMBER
			|| type == CFWQueryToken.CFWQueryTokenType.LITERAL_BOOLEAN
			; 
	};
	
	/***********************************************************************************************
	 * Check if the token is a literal string (unquoted text that is not a keyword).
	 * 
	 ***********************************************************************************************/
	public boolean isString() { 
		return type == CFWQueryToken.CFWQueryTokenType.LITERAL_STRING; 
	};
	
	/***********************************************************************************************
	 * Check if the token is single and/or double quoted text.
	 * Either one of the parameters has to be true, else the return value will always be null.
	 * 
	 * @param singleQuoted check if is single quoted text
	 * @param doubleQuoted check if is double quoted text
	 * @param doubleQuoted check if is backticked text
	 * @param backTicked TODO
	 * 
	 ***********************************************************************************************/
	public boolean isText(boolean singleQuoted, boolean doubleQuoted, boolean backTicked) { 
		
		return (doubleQuoted && type == CFWQueryToken.CFWQueryTokenType.TEXT_DOUBLE_QUOTES)
			|| (singleQuoted && type == CFWQueryToken.CFWQueryTokenType.TEXT_SINGLE_QUOTES)
			|| (backTicked && type == CFWQueryToken.CFWQueryTokenType.TEXT_BACKTICKS)
				; 
	};
	
	/***********************************************************************************************
	 * Check if the token is either a string literal or a single, double quoted or backticked text.
	 * Either one of the parameters has to be true, else the return value will always be null.
	 * 
	 * @param backTicked TODO
	 * 
	 ***********************************************************************************************/
	public boolean isStringOrText() { 
		return type == CFWQueryToken.CFWQueryTokenType.LITERAL_STRING
			 || isText(true, true, true); 
	};
	
	/***********************************************************************************************
	 * Check if the token is either a string literal or a single and/or double quoted and/or backticked text.
	 * Either one of the parameters has to be true, else the return value will always be null.
	 * 
	 * @param singleQuoted check if is single quoted text
	 * @param doubleQuoted check if is double quoted text
	 * @param doubleQuoted check if is backticked text
	 * @param backTicked TODO
	 * 
	 ***********************************************************************************************/
	public boolean isStringOrText(boolean singleQuoted, boolean doubleQuoted, boolean backTicked) { 
		return type == CFWQueryToken.CFWQueryTokenType.LITERAL_STRING
			 || isText(singleQuoted, doubleQuoted, backTicked); 
	};
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isNumber() { 
		return type == CFWQueryToken.CFWQueryTokenType.LITERAL_NUMBER; 
	};
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isBoolean() { 
		return type == CFWQueryToken.CFWQueryTokenType.LITERAL_BOOLEAN; 
	};
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isKeyword() { 
		return type == CFWQueryToken.CFWQueryTokenType.KEYWORD; 
	};
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isOperator() { 
		return this.type.toString().startsWith("OPERATOR_");
	}

	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public JsonObject toJson() { 
		JsonObject object = new JsonObject();
		object.addProperty("type", this.type.name());
		object.addProperty("value", this.value);
		object.addProperty("position", this.position);
		
		return object;
	};
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String toString() { 
		return this.value; 
	};
	
}