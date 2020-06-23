package com.pengtoolbox.cfw.response.bootstrap;

import com.pengtoolbox.cfw._main.SessionData;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class UserMenuItem extends MenuItem {

	public UserMenuItem(SessionData data) {
		super(data.getUser().username());
		this.alignDropdownRight(true);
		this.faicon("fas fa-user");
	}

}
