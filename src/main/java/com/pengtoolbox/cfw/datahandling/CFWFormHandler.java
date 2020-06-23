package com.pengtoolbox.cfw.datahandling;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class CFWFormHandler {
	
	public abstract void handleForm( HttpServletRequest request,HttpServletResponse response, CFWForm form, CFWObject origin);

}
