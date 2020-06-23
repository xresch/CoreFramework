package com.pengtoolbox.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;

public class ContextualTokenizer {
	
	private String textToParse;

	private char[] charArray;
	private int lastCutPosition = 0;
	
	private boolean handleQuotes = true;
	
	public ContextualTokenizer(String textToParse) {
		this.textToParse = textToParse.trim();
		charArray = this.textToParse.toCharArray(); 

	}
	
	/***********************************************************************************************
	 * 
	 * @param splitterChars
	 * @return
	 * @throws ParseException
	 ***********************************************************************************************/
	public ArrayList<String> getTokenStringsbyDelimiters(ArrayList<Character> splitterChars) throws ParseException {
		
		CFWToken token = this.getNextToken(splitterChars);

		ArrayList<String> tokensArray = new ArrayList<String>();
		
		while(token != null) {
			tokensArray.add(token.getText());
			token = this.getNextToken(splitterChars);
		}
		
		return tokensArray;
	}
	
	/***********************************************************************************************
	 * 
	 * @param splitterChars
	 * @return
	 * @throws ParseException
	 ***********************************************************************************************/
	public ArrayList<CFWToken> getTokensbyDelimiters(ArrayList<Character> splitterChars) throws ParseException {
		
		CFWToken token = this.getNextToken(splitterChars);

		ArrayList<CFWToken> tokensArray = new ArrayList<CFWToken>();
		
		while(token != null) {
			tokensArray.add(token);
			token = this.getNextToken(splitterChars);
		}
		
		return tokensArray;
	}
		
	/***********************************************************************************************
	 * 
	 * @param delimiterChars
	 * @return
	 * @throws ParseException
	 ***********************************************************************************************/
	private CFWToken getNextToken(ArrayList<Character> delimiterChars) throws ParseException {
		int currentPos = lastCutPosition;
		for( ;currentPos < charArray.length; currentPos++) {
			
			switch(charArray[currentPos]) {
				case '"':  		if(handleQuotes) { currentPos = skipQuotedText('"', currentPos); }
								break;
				case '\'': 		if(handleQuotes) { currentPos = skipQuotedText('\'', currentPos); }
								break;	
				default: 
					//---------------------------
					// handle if delimiter, else
					// move currentPos
					if(delimiterChars.contains(charArray[currentPos]) ) {
						CFWToken result;
						
						result = new CFWToken(textToParse.substring(lastCutPosition, currentPos).trim());
						
						//System.out.println("lastCutPos: "+lastCutPosition+", pos: "+currentPos+", char:"+charArray[currentPos]+", token: '"+result.getText()+"'");
						lastCutPosition = currentPos;
						
						//-------------------------
						//Skip subsequent delimiters
						while(delimiterChars.contains(charArray[lastCutPosition]) ) {
							lastCutPosition++;
						}
						return result;
					}
					break;
			}
			
		}
		
		//------------------------------
		// End of Text
		if(lastCutPosition != currentPos && lastCutPosition < charArray.length) {
			CFWToken result = new CFWToken(textToParse.substring(lastCutPosition).trim());
			lastCutPosition = currentPos+1;
			return result;
		}
		//------------------------------
		// Return null if no more data
		return null;
	}


	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private int skipQuotedText(char quoteChar, int currentPos) throws ParseException {
		String quotedText = getQuotedText(quoteChar, currentPos).getText();
		return currentPos + quotedText.length()+1;
	}
	
	/***********************************************************************************************
	 * Parses a quoted text.
	 * @param currentPos has to be on a position representing quoteChar
	 * @param quoteChar either single or double quote
	 ***********************************************************************************************/
	private CFWTokenText getQuotedText(char quoteChar, int currentPos) throws ParseException {
		boolean isQuoteOpen = true;
		int startPos = currentPos;
		while(isQuoteOpen && currentPos < charArray.length-1) {
			currentPos++;
			
			if(currentPos >= charArray.length) {
				throw new ParseException("Unbalanced Quotes!", currentPos);							
			}
			
			if(charArray[currentPos] == quoteChar) {
				int backslashCount = 0;
				int tempPos = currentPos-1; 
				while(charArray[tempPos] == '\\') {
					backslashCount++;
					tempPos--;
				}
				
				if(backslashCount % 2 == 0) {
					isQuoteOpen = false;
				}
			}

		}
		
		return new CFWTokenText(textToParse.substring(startPos,currentPos-1)).wasQuoted(true);
	}
}
