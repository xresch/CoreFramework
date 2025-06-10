package com.xresch.cfw.features.query;

import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.query.commands.CFWQueryCommandAggregate;
import com.xresch.cfw.features.query.commands.CFWQueryCommandAwait;
import com.xresch.cfw.features.query.commands.CFWQueryCommandBollBands;
import com.xresch.cfw.features.query.commands.CFWQueryCommandChangepoint;
import com.xresch.cfw.features.query.commands.CFWQueryCommandChart;
import com.xresch.cfw.features.query.commands.CFWQueryCommandChartFields;
import com.xresch.cfw.features.query.commands.CFWQueryCommandComment;
import com.xresch.cfw.features.query.commands.CFWQueryCommandCrates;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDecompose;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDisplay;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDisplayFields;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDistinct;
import com.xresch.cfw.features.query.commands.CFWQueryCommandEach;
import com.xresch.cfw.features.query.commands.CFWQueryCommandElse;
import com.xresch.cfw.features.query.commands.CFWQueryCommandEnd;
import com.xresch.cfw.features.query.commands.CFWQueryCommandExecute;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFilter;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatBoxplot;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatCSS;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatField;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatLink;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatQuery;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatRecord;
import com.xresch.cfw.features.query.commands.CFWQueryCommandForwardFill;
import com.xresch.cfw.features.query.commands.CFWQueryCommandGlobals;
import com.xresch.cfw.features.query.commands.CFWQueryCommandHide;
import com.xresch.cfw.features.query.commands.CFWQueryCommandIf;
import com.xresch.cfw.features.query.commands.CFWQueryCommandIndex;
import com.xresch.cfw.features.query.commands.CFWQueryCommandKeep;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMapArrays;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMessage;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMetadata;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMimic;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMovAvg;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMovDiff;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMovOutlier;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMovStdev;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMove;
import com.xresch.cfw.features.query.commands.CFWQueryCommandNullTo;
import com.xresch.cfw.features.query.commands.CFWQueryCommandOutlier;
import com.xresch.cfw.features.query.commands.CFWQueryCommandPSAR;
import com.xresch.cfw.features.query.commands.CFWQueryCommandParamDefaults;
import com.xresch.cfw.features.query.commands.CFWQueryCommandParseCSV;
import com.xresch.cfw.features.query.commands.CFWQueryCommandPercentiles;
import com.xresch.cfw.features.query.commands.CFWQueryCommandRSI;
import com.xresch.cfw.features.query.commands.CFWQueryCommandRecord;
import com.xresch.cfw.features.query.commands.CFWQueryCommandRemove;
import com.xresch.cfw.features.query.commands.CFWQueryCommandRename;
import com.xresch.cfw.features.query.commands.CFWQueryCommandResultCompare;
import com.xresch.cfw.features.query.commands.CFWQueryCommandResultConcat;
import com.xresch.cfw.features.query.commands.CFWQueryCommandResultCopy;
import com.xresch.cfw.features.query.commands.CFWQueryCommandResultJoin;
import com.xresch.cfw.features.query.commands.CFWQueryCommandResultRemove;
import com.xresch.cfw.features.query.commands.CFWQueryCommandScore;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSet;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSkip;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSort;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.commands.CFWQueryCommandStats;
import com.xresch.cfw.features.query.commands.CFWQueryCommandStatsMatrix;
import com.xresch.cfw.features.query.commands.CFWQueryCommandStored;
import com.xresch.cfw.features.query.commands.CFWQueryCommandTail;
import com.xresch.cfw.features.query.commands.CFWQueryCommandTop;
import com.xresch.cfw.features.query.commands.CFWQueryCommandUnbox;
import com.xresch.cfw.features.query.commands.CFWQueryCommandUndefinedTo;
import com.xresch.cfw.features.query.commands.CFWQueryCommandUnhide;
import com.xresch.cfw.features.query.database.CFWQueryHistory;
import com.xresch.cfw.features.query.database.TaskQueryHistoryLimitEntries;
import com.xresch.cfw.features.query.functions.*;
import com.xresch.cfw.features.query.sources.CFWQuerySourceAppDB;
import com.xresch.cfw.features.query.sources.CFWQuerySourceApplog;
import com.xresch.cfw.features.query.sources.CFWQuerySourceAuditlog;
import com.xresch.cfw.features.query.sources.CFWQuerySourceCSV;
import com.xresch.cfw.features.query.sources.CFWQuerySourceEmpty;
import com.xresch.cfw.features.query.sources.CFWQuerySourceJson;
import com.xresch.cfw.features.query.sources.CFWQuerySourceRandom;
import com.xresch.cfw.features.query.sources.CFWQuerySourceStored;
import com.xresch.cfw.features.query.sources.CFWQuerySourceText;
import com.xresch.cfw.features.query.sources.CFWQuerySourceThreaddump;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class FeatureQuery extends CFWAppFeature {
	
	private static final Logger logger = CFWLog.getLogger(FeatureQuery.class.getName());
	
	private static final String URI_QUERY = "/app/query";
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.query.resources";
	public static final String PACKAGE_MANUAL =    "com.xresch.cfw.features.query.manual";
	public static final String PERMISSION_QUERY_USER = "Query: User";
	public static final String PERMISSION_QUERY_ADMIN = "Query: Admin";
	public static final String PERMISSION_QUERY_SOURCE_APPDB = "Query: Source appdb";
	
	public static final String CONFIG_CATEGORY = "Query";
	public static final String CONFIG_FETCH_LIMIT_DEFAULT = "Fetch Limit Default";
	public static final String CONFIG_FETCH_LIMIT_MAX = "Fetch Limit Max";
	public static final String CONFIG_QUERY_RECORD_LIMIT = "Query Record Limit";
	public static final String CONFIG_QUERY_COMMAND_LIMIT = "Query Command Limit";
	public static final String CONFIG_QUERY_EXEC_LIMIT = "Query Time Limit";
	public static final String CONFIG_QUERY_HISTORY_LIMIT = "Query History Limit";
	
	private static ScheduledFuture<?> taskQueryHistoryLimit;
	
	public static ManualPage ROOT_MANUAL_PAGE;
	
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
		CFW.Registry.Objects.addCFWObject(CFWQueryHistory.class);

		//----------------------------------
		// Register Global Javascript
		CFW.Registry.Components.addGlobalCSSFile(FileDefinition.HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES , "cfw_query.css");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES , "hightlight_cfwquery.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES , "cfw_query_rendering.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES , "cfw_query_editor.js");
				
		//----------------------------------
		// Register Commands
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSource(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandAggregate(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandAwait(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandBollBands(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandChangepoint(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandChart(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandChartFields(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandComment(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandCrates(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDecompose(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDisplay(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDisplayFields(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDistinct(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandEach(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandElse(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandEnd(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandExecute(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFilter(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatBoxplot(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatCSS(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatField(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatLink(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatQuery(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatRecord(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandForwardFill(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandGlobals(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandHide(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandIf(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandIndex(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandKeep(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMapArrays(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMessage(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMetadata(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMimic(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMovAvg(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMovDiff(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMove(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMovOutlier(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMovStdev(null));
		//CFW.Registry.Query.registerCommand(new CFWQueryCommandNoMessage(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandNullTo(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandOutlier(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandParamDefaults(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandParseCSV(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandPercentiles(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandPSAR(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandRecord(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandRemove(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandRename(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandResultCompare(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandResultConcat(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandResultCopy(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandResultJoin(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandResultRemove(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandRSI(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandScore(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSet(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSkip(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSort(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandStats(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandStatsMatrix(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandStored(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandTail(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandTop(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandUnbox(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandUndefinedTo(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandUnhide(null));
		
		//----------------------------------
		// Register Functions
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionAbs(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionArray(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionArrayAdd(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionArrayConcat(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionArrayReverse(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionArraySplit(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionArrayStringify(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionAvg(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCase(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCeil(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionClone(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCoalesce(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionContains(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCorr(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCos(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCount(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCountIf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCountNulls(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionCredentials(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionDecode(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionDecodeBase64(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionDistinct(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionEncode(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionEncodeBase64(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionEndsWith(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionEarliest(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionEarliestSet(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionExtract(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionExtractAll(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionExtractBounds(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionFields(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionFirst(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionFloor(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionG(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionGlobals(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIndexOf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionInterval(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIntervalPoints(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIntervalUnit(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIsArray(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIsBoolean(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIsNull(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIsNullOrEmpty(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIsNumber(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIsObject(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIsString(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionIsUndef(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLast(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLastIndexOf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLatest(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLatestSet(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLength(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLiteral(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionLowercase(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionM(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMatches(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMax(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMedian(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMessage(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMeta(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionMin(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionNow(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionNullIf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionNullTo(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionObject(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionP(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionParam(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionPerc(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionPrev(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRandom(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRandomColorGroup(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRandomColorHSL(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRandomFloat(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRandomFrom(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRecord(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRecords(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionReplace(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionReplaceFirst(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionReplaceRegex(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionRound(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSin(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSourceEach(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSourcepage(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSplit(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionStartsWith(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionStdev(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSubquery(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSubstring(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSum(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionSumIf(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTan(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeFormat(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeframeDuration(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeframeMax(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeframeOffset(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeOffset(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeParse(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeRound(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTimeTruncate(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionToJSON(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionTrim(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionType(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionUppercase(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionUserdata(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionValue(null));
		 
		//----------------------------------
		// Register Sources
		CFW.Registry.Query.registerSource(new CFWQuerySourceAppDB(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceApplog(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceAuditlog(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceCSV(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceEmpty(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceJson(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceRandom(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceStored(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceText(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceThreaddump(null));
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetQueryResults());
		

		//----------------------------------
		// Register Job Tasks
		CFW.Registry.Jobs.registerTask(new CFWJobTaskAlertingCFWQLQuery());
		CFW.Registry.Jobs.registerTask(new CFWJobTaskReportingCFWQLQuery());

		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addToolsMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Query")
					.faicon("fas fa-terminal")
					.addPermission(PERMISSION_QUERY_USER)
					.addPermission(PERMISSION_QUERY_ADMIN)
					.href("/app/query")
					.addAttribute("id", "cfwMenuTools-Query")
				, null);
		
		//----------------------------------
		// Register Root Manual Page
		// Don't put this into the registerManual() function
		// to keep order of the manual
		ROOT_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, 
				new ManualPage("Query")
					.faicon("fas fa-terminal")
					.addPermission(PERMISSION_QUERY_USER)
					.addPermission(PERMISSION_QUERY_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "000_query.html"))
				;
		
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
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_QUERY_SOURCE_APPDB, FeatureUserManagement.CATEGORY_USER)
					.description("User can use the query source appdb to access this application's database."),
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
			new Configuration(CONFIG_CATEGORY, CONFIG_FETCH_LIMIT_DEFAULT)
				.description("The default fetch limit for number of records that are allowed per source.")
				.type(FormFieldType.NUMBER)
				.value("50000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_FETCH_LIMIT_MAX)
				.description("The maximum fetch limit for number of records that are allowed per source. Helps to limit load on your sources.")
				.type(FormFieldType.NUMBER)
				.value("250000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_QUERY_RECORD_LIMIT)
				.description("The maximum number of records that are allowed per query(sum of all source limits). Helps to reduce performance impact on this application.")
				.type(FormFieldType.NUMBER)
				.value("500000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY, CONFIG_QUERY_COMMAND_LIMIT)
				.description("The maximum number of commands that are allowed per query. Limits the number of threads started per query.")
				.type(FormFieldType.NUMBER)
				.value("30")
				);
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY, CONFIG_QUERY_EXEC_LIMIT)
				.description("The maximum execution time in seconds before a query gets aborted.")
				.type(FormFieldType.NUMBER)
				.value("180")
				);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY, CONFIG_QUERY_HISTORY_LIMIT)
				.description("The maximum amount of queries kept in the history per user. Exceeding amount will be deleted periodically.")
				.type(FormFieldType.NUMBER)
				.value("1000")
				);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		//----------------------------------
    	// Register Servlets
		app.addAppServlet(ServletQuery.class,  URI_QUERY);
		
		//----------------------------------
    	// Register APIs
		CFW.Registry.API.add( new APIQueryExecute("Query", "execute") );
		
		//-----------------------------------------------
    	// Register Manual: Done here after all Sources, 
		// Commands etc... are registered.
		registerManual();
	}

	@Override
	public void startTasks() {

		//----------------------------------------
		// Task: Store to Database
		if(taskQueryHistoryLimit != null) {
			taskQueryHistoryLimit.cancel(false);
		}
		
		// use an odd number to reduce number of clashes
		int millis = (int)CFWTimeUnit.m.toMillis(144);
		taskQueryHistoryLimit = CFW.Schedule.runPeriodicallyMillis(millis, millis, new TaskQueryHistoryLimitEntries());
		
	}

	@Override
	public void stopFeature() {
		taskQueryHistoryLimit.cancel(false);
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
		ROOT_MANUAL_PAGE.addChild(new ManualPage("Using Objects and Arrays")
				.faicon("fas fa-grip-horizontal")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "200_query_objects_arrays.html")
			);
		
		//----------------------------------
		// Working with Time
		ROOT_MANUAL_PAGE.addChild(new ManualPage("Working with Time")
				.faicon("fas fa-clock")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "300_query_time.html")
			);
		
		//----------------------------------
		// Query API
		ROOT_MANUAL_PAGE.addChild(new ManualPage("Special Cases")
				.faicon("fas fa-star")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "800_query_specialcases.html")
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

			try {
				
				CFWQuerySource current = CFW.Registry.Query.createSourceInstance(pseudoQuery, sourceName);
				CFWQueryManualPageSource page = new CFWQueryManualPageSource(sourceName, current);
				sourcePage.addChild(page);
				
			}catch(Exception e) {
				new CFWLog(logger).severe("Error while creating page for source: "+sourceName, e);
			}
			
		}
		
		//----------------------------------
		// Commands Main Page
		ManualPage commandsPage = new CFWQueryManualPageRootForCommands(ROOT_MANUAL_PAGE, "Commands");
		
		//----------------------------------
		// Pages for each Command
		TreeMap<String, Class<? extends CFWQueryCommand>> commandlist = CFW.Registry.Query.getCommandList();
		
		
		for(String commandName : commandlist.keySet()) {
			
			try {
				
				CFWQueryCommand current = CFW.Registry.Query.createCommandInstance(pseudoQuery, commandName);
				new CFWQueryManualPageCommand(commandsPage, commandName, current);
				
			}catch(Exception e) {
				new CFWLog(logger).severe("Error while creating page for command: "+commandName, e);
			}			
		}
		
		//----------------------------------
		// Functions Main Page
		
		ManualPage functionsMainPage = new CFWQueryManualPageRootForFunctions(ROOT_MANUAL_PAGE, "Functions")
				.faicon("fas fa-cog")
			;
		
		//----------------------------------
		// Pages for each Function
		TreeMap<String, Class<? extends CFWQueryFunction>> functionlist = CFW.Registry.Query.getFunctionList();
		
		for(String functionName : functionlist.keySet()) {
			
			try {
				
				CFWQueryFunction current = CFW.Registry.Query.createFunctionInstance(pseudoQuery.getContext(), functionName);
				CFWQueryManualPageFunction currentPage = new CFWQueryManualPageFunction(current.uniqueName(), current);
				functionsMainPage.addChild(currentPage);
				
			}catch(Exception e) {
				new CFWLog(logger).severe("Error while creating page for function: "+functionName, e);
			}
		}
		


				
	}

}
