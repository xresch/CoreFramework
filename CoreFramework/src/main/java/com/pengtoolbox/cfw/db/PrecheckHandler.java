package com.pengtoolbox.cfw.db;

import com.pengtoolbox.cfw.datahandling.CFWObject;

public abstract class PrecheckHandler {
	public abstract boolean doCheck(CFWObject object);
}
