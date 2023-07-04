package com.xresch.cfw.features.query;

import java.util.TreeMap;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.query.commands.CFWQueryCommandChart;
import com.xresch.cfw.features.query.commands.CFWQueryCommandComment;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDisplay;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDistinct;
import com.xresch.cfw.features.query.commands.CFWQueryCommandExecute;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFilter;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatField;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatRecord;
import com.xresch.cfw.features.query.commands.CFWQueryCommandGlobals;
import com.xresch.cfw.features.query.commands.CFWQueryCommandKeep;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMetadata;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMimic;
import com.xresch.cfw.features.query.commands.CFWQueryCommandNullTo;
import com.xresch.cfw.features.query.commands.CFWQueryCommandRemove;
import com.xresch.cfw.features.query.commands.CFWQueryCommandRename;
import com.xresch.cfw.features.query.commands.CFWQueryCommandResultCompare;
import com.xresch.cfw.features.query.commands.CFWQueryCommandResultConcat;
import com.xresch.cfw.features.query.commands.CFWQueryCommandResultCopy;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSet;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSort;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.commands.CFWQueryCommandStats;
import com.xresch.cfw.features.query.commands.CFWQueryCommandTail;
import com.xresch.cfw.features.query.commands.CFWQueryCommandTop;
import com.xresch.cfw.features.query.commands.CFWQueryCommandUnbox;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionAvg;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionClone;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionContains;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionCount;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionCountNulls;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionDecode;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionEarliest;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionEarliestSet;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionEncode;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionExtract;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionFirst;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionGlobals;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionIf;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionIndexOf;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionLast;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionLastIndexOf;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionLatest;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionLatestSet;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionLength;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionMax;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionMedian;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionMeta;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionMin;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionNow;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionNullTo;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionPerc;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionRandom;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionRandomFloat;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionRandomFrom;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionReplace;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionRound;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionSubquery;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionSubstring;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionSum;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionTimeOffset;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionTimeParse;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionTimeframeOffset;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionToJSON;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionTrim;
import com.xresch.cfw.features.query.functions.CFWQueryFunctionType;
import com.xresch.cfw.features.query.sources.CFWQuerySourceApplog;
import com.xresch.cfw.features.query.sources.CFWQuerySourceAuditlog;
import com.xresch.cfw.features.query.sources.CFWQuerySourceEmpty;
import com.xresch.cfw.features.query.sources.CFWQuerySourceJson;
import com.xresch.cfw.features.query.sources.CFWQuerySourceRandom;
import com.xresch.cfw.features.query.sources.CFWQuerySourceText;
import com.xresch.cfw.features.query.sources.CFWQuerySourceThreaddump;
import com.xresch.cfw.features.query.sources.CFWQuerySourceWeb;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureQuery extends CFWAppFeature {
	
	private static final String URI_QUERY = "/app/query";
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.query.resources";
	public static final String PACKAGE_MANUAL =    "com.xresch.cfw.features.query.manual";
	public static final String PERMISSION_QUERY_USER = "Query: User";
	public static final String PERMISSION_QUERY_ADMIN = "Query: Admin";
	
	public static final String CONFIG_FETCH_LIMIT_DEFAULT = "Fetch Limit Default";
	public static final String CONFIG_FETCH_LIMIT_MAX = "Fetch Limit Max";
	public static final String CONFIG_QUERY_RECORD_LIMIT = "Query Record Limit";
	public static final String CONFIG_QUERY_COMMAND_LIMIT = "Query Command Limit";
	public static final String CONFIG_QUERY_EXEC_LIMIT = "Query Time Limit";
	
	public static final ManualPage ROOT_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, 
			new ManualPage("Query")
				.faicon("fas fa-terminal")
				.addPermission(PERMISSION_QUERY_USER)
				.addPermission(PERMISSION_QUERY_ADMIN)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "000_query.html"))
			;
	
	public enum CFWQueryComponentType{
		  SOURCE
		, COMMAND
		, FUNCTION
	}
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
		//----------------------------------
		// Register Objects
		//CFW.Registry.Objects.addCFWObject(CFWJob.class);
		
		//----------------------------------
		// Register Global Javascript
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES , "hightlight_cfwquery.js");
		
		//----------------------------------
		// Register Commands
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSource(null));
		
		CFW.Registry.Query.registerCommand(new CFWQueryCommandChart(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandComment(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDisplay(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDistinct(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandExecute(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFilter(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatField(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatRecord(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandGlobals(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandKeep(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandResultCompare(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandResultConcat(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandResultCopy(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMetadata(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMimic(null));
		//CFW.Registry.Query.registerCommand(new CFWQueryCommandNoMessage(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandNullTo(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandRemove(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandRename(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSet(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSort(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandStats(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandTail(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandTop(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandUnbox(null));
		
		//----------------------------------
		// Register Functions
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionAvg(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionClone(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionContains(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCount(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCountNulls(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionDecode(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionEncode(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionEarliest(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionEarliestSet(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionExtract(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionFirst(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionGlobals(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIndexOf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLast(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLastIndexOf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLatest(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLatestSet(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLength(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMax(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMedian(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMeta(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMin(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionNow(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionNullTo(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionPerc(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRandom(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRandomFloat(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRandomFrom(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionReplace(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRound(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSubstring(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSubquery(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSum(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeframeOffset(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeOffset(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeParse(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionToJSON(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTrim(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionType(null));
		 
		//----------------------------------
		// Register Sources
		CFW.Registry.Query.registerSource(new CFWQuerySourceApplog(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceAuditlog(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceEmpty(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceJson(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceRandom(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceText(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceThreaddump(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceWeb(null));
		
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetQueryResults());
		
		//----------------------------------
		// Register Dashboard Parameters
		CFW.Registry.Parameters.add(new DashboardParameterQueryResult());
		
		//----------------------------------
		// Register Job Tasks
		//CFW.Registry.Jobs.registerTask(new CFWJobTaskSendMail());

		
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addToolsMenuItem(
				(MenuItem)new MenuItem("Query")
					.faicon("fas fa-terminal")
					.addPermission(PERMISSION_QUERY_USER)
					.addPermission(PERMISSION_QUERY_ADMIN)
					.href("/app/query")
					.addAttribute("id", "cfwMenuTools-Query")
				, null);
		
		//----------------------------------
    	// Register APIs
		CFW.Registry.API.add( new APIQueryExecute("Query", "execute") );
	}

	@Override
	public void initializeDB() {
		
		//----------------------------------
    	// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_QUERY_USER, FeatureUserManagement.CATEGORY_USER)
					.description("User can view and edit his own queries."),
					true,
					false
			);
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_QUERY_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("User can view and edit all queries in the system."),
					true,
					false
			);
		
		//============================================================
		// CONFIGURATION
		//============================================================
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Query", CONFIG_FETCH_LIMIT_DEFAULT)
				.description("The default fetch limit for number of records that are allowed per source.")
				.type(FormFieldType.NUMBER)
				.value("50000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Query", CONFIG_FETCH_LIMIT_MAX)
				.description("The maximum fetch limit for number of records that are allowed per source. Helps to limit load on your sources.")
				.type(FormFieldType.NUMBER)
				.value("250000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Query", CONFIG_QUERY_RECORD_LIMIT)
				.description("The maximum number of records that are allowed per query(sum of all source limits). Helps to reduce performance impact on this application.")
				.type(FormFieldType.NUMBER)
				.value("500000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration("Query", CONFIG_QUERY_COMMAND_LIMIT)
				.description("The maximum number of commands that are allowed per query. Limits the number of threads started per query.")
				.type(FormFieldType.NUMBER)
				.value("30")
				);
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration("Query", CONFIG_QUERY_EXEC_LIMIT)
				.description("The maximum execution time in seconds before a query gets aborted.")
				.type(FormFieldType.NUMBER)
				.value("180")
				);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		app.addAppServlet(ServletQuery.class,  URI_QUERY);
		
		//-----------------------------------------------
    	// Register Manual: Done here after all Sources, 
		// Commands etc... are registered.
		registerManual();
	}

	@Override
	public void startTasks() {
		//do nothing
	}

	@Override
	public void stopFeature() {
		// do nothing
	}
	
	public static String getQueryURI() {
		return URI_QUERY;
	}
	
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	public void registerManual() {
		
		CFWQuery pseudoQuery = new CFWQuery();
		
		//----------------------------------
		// Cheat Sheet
		ROOT_MANUAL_PAGE.addChild(new ManualPage("Cheat Sheet")
				.faicon("fas fa-star")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "100_query_cheatsheet.html")
			);
		
		//----------------------------------
		// Working with Time
		ROOT_MANUAL_PAGE.addChild(new ManualPage("Using Objects")
				.faicon("fas fa-grip-horizontal")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "200_query_objects.html")
			);
		
		//----------------------------------
		// Working with Time
		ROOT_MANUAL_PAGE.addChild(new ManualPage("Working with Time")
				.faicon("fas fa-clock")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "300_query_time.html")
			);
		
		//----------------------------------
		// Query API
		ROOT_MANUAL_PAGE.addChild(new ManualPage("Query API")
				.faicon("fas fa-code")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "900_query_api.html")
			);
		
		
		//----------------------------------
		// Source Main Page
		ManualPage sourcePage = new ManualPage("Sources")
				.faicon("fas fa-star-of-life")
				.content("&nbsp;");
		
		ROOT_MANUAL_PAGE.addChild(sourcePage);
		
		
		//----------------------------------
		// Pages for each Source
		TreeMap<String, Class<? extends CFWQuerySource>> sourcelist = CFW.Registry.Query.getSourceList();
		
		
		for(String sourceName : sourcelist.keySet()) {
			
			CFWQuerySource current = CFW.Registry.Query.createSourceInstance(pseudoQuery, sourceName);
			
			CFWQueryManualPageSource page = new CFWQueryManualPageSource(sourceName, current);
			
			sourcePage.addChild(page);
			
		}
		
		//----------------------------------
		// Commands Main Page
		ManualPage commandsPage = new ManualPage("Commands")
				.faicon("fas fa-cogs")
				.content("&nbsp;");
		
		ROOT_MANUAL_PAGE.addChild(commandsPage);
		
		//----------------------------------
		// Pages for each Command
		TreeMap<String, Class<? extends CFWQueryCommand>> commandlist = CFW.Registry.Query.getCommandList();
		
		
		for(String commandName : commandlist.keySet()) {
			
			CFWQueryCommand current = CFW.Registry.Query.createCommandInstance(pseudoQuery, commandName);
			
			new CFWQueryManualPageCommand(commandsPage, commandName, current);
						
		}
		
		//----------------------------------
		// Functions Main Page
		ManualPage functionsMainPage = new ManualPage("Functions")
				.faicon("fas fa-cog")
				.content("&nbsp;")
				;
		
		ROOT_MANUAL_PAGE.addChild(functionsMainPage);
		
		//----------------------------------
		// Pages for each Function
		TreeMap<String, Class<? extends CFWQueryFunction>> functionlist = CFW.Registry.Query.getFunctionList();
		
		for(String functionName : functionlist.keySet()) {
			
			CFWQueryFunction current = CFW.Registry.Query.createFunctionInstance(pseudoQuery.getContext(), functionName);
			
			CFWQueryManualPageFunction currentPage = new CFWQueryManualPageFunction(functionName, current);
			functionsMainPage.addChild(currentPage);
		}
		


				
	}

}
