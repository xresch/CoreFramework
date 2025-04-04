package com.xresch.cfw.response.bootstrap;

import com.xresch.cfw.features.usermgmt.CFWSessionData;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHTMLItemMenuItemUser extends CFWHTMLItemMenuItem {

	public CFWHTMLItemMenuItemUser(CFWSessionData data) {
		super(data.getUser().username());
		this.alignDropdownRight(true);
		this.faicon("fas fa-user");
		this.addAttribute("id", "cfwMenuUser");
	}

}
