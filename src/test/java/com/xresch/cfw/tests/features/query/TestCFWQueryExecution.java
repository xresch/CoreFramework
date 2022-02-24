package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.CFWQueryParser.CFWQueryParserContext;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartBinaryExpression;
import com.xresch.cfw.features.query.parse.QueryPartGroup;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.tests._master.DBTestMaster;

public class TestCFWQueryExecution extends DBTestMaster{
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static String jsonTestData;
	private static String sourceString ;
	private static final String PACKAGE = "com.xresch.cfw.tests.features.query.testdata";
	
	@BeforeAll
	public static void setup() {
		
		FeatureQuery feature = new FeatureQuery();
		feature.register();
		
		CFW.Files.addAllowedPackage(PACKAGE);
		jsonTestData =	CFW.Files.readPackageResource(PACKAGE, "SourceJsonTestdata.json");
		jsonTestData = jsonTestData.replace("'", "\'");
		sourceString = "source json data='"+jsonTestData+"'";

		context.setEarliest(new Instant().minus(1000*60*30).getMillis());
		context.setLatest(new Instant().getMillis());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testSpecialCaseNewLineAfterNegativeNumber() throws IOException {
		
		String queryString = 
				"| source random type=numbers records=100 \r\n"
				+ "| filter \r\n"
				+ "		(null - 10)	== -10 	\r\n"
				+ "	AND (null + 10)	== 10";
		
		JsonArray resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, System.currentTimeMillis()-(1000^2), System.currentTimeMillis());
		
		Assertions.assertEquals(1, resultArray.size());
		
		// No filtering occurs as both are true
		JsonObject queryResults = resultArray.get(0).getAsJsonObject();
		Assertions.assertEquals(100, queryResults.get("results").getAsJsonArray().size());

	}
	
	
}
