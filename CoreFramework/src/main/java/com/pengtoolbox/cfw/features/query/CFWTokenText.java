package com.pengtoolbox.cfw.features.query;

public class CFWTokenText extends CFWToken {

	public boolean wasQuoted = false;
	
	public CFWTokenText(String text) {
		super(text);
	}

	public boolean wasQuoted() {
		return wasQuoted;
	}

	public CFWTokenText wasQuoted(boolean wasQuoted) {
		this.wasQuoted = wasQuoted;
		return this;
	}
	
	
}
