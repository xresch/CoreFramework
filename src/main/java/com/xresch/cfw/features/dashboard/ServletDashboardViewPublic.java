package com.xresch.cfw.features.dashboard;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormCustomAutocompleteHandler;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWMultiForm;
import com.xresch.cfw.datahandling.CFWMultiFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.datahandling.CFWSchedule.EndType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.jobs.CFWDBJob;
import com.xresch.cfw.features.jobs.CFWJob;
import com.xresch.cfw.features.jobs.CFWJob.CFWJobFields;
import com.xresch.cfw.features.parameter.CFWParameter;
import com.xresch.cfw.features.parameter.ParameterDefinition;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterFields;
import com.xresch.cfw.features.parameter.CFWParameter.DashboardParameterMode;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.utils.web.CFWModifiableHTTPRequest;
import com.xresch.cfw.validation.ScheduleValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletDashboardViewPublic extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		ServletDashboardViewMethods.doGet(request, response, true);
    }
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		ServletDashboardViewMethods.doPost(request, response, true);
    }
	
}