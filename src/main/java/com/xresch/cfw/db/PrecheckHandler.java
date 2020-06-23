package com.xresch.cfw.db;

import com.xresch.cfw.datahandling.CFWObject;

public abstract class PrecheckHandler {
	public abstract boolean doCheck(CFWObject object);
}
