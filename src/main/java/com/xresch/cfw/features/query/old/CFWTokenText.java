package com.xresch.cfw.features.query.old;

public class CFWTokenText extends CFWToken {

	private boolean wasQuoted = false;
	
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
