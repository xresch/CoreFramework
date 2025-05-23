package com.xresch.cfw.tests.features.query;

import java.io.IOException;
import java.util.Calendar;

import org.joda.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.tests._master.DBTestMaster;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TestCFWQueryGeneral extends DBTestMaster{
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static long earliest = new Instant().minus(1000*60*30).getMillis();
	private static long latest = new Instant().getMillis();
	@BeforeAll
	public static void setup() {
		
		FeatureQuery feature = new FeatureQuery();
		feature.register();
		
		context.setEarliest(earliest);
		context.setLatest(latest);
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testParameterPlaceholderSubstitution() throws IOException {
		
		//---------------------------------
		String queryString = """
| source random records=1000
| defaultparams
	test = "| filter FIRSTNAME == Hera"
	tiramisu = '| filter LIKES_TIRAMISU == true'
| defaultparams
	number = `| filter VALUE >= 20`
	array = [1, 2, 3]
| formatrecord
	[(VALUE == null), "cfw-gray"] 
	[(VALUE < 20), "cfw-red"] 
	[(VALUE < 40), "cfw-orange"]  
	[(VALUE < 60), "cfw-yellow"] 
	[(VALUE < 80), "cfw-limegreen"] 
	[true, "cfw-green"] 
| filter
	length($array$) > 1
$test$
$tiramisu$
$number$
			""";

		CFWQueryResultList resultArray = new CFWQueryExecutor()
				.parseAndExecuteAll(queryString, earliest, latest, 0);
		
		//  query results
		Assertions.assertEquals(1, resultArray.size());
		
		//------------------------------
		// Check First Result
		CFWQueryResult queryResults = resultArray.get(0);
		
		// Check Aurora Records
		for(int i = 0; i < queryResults.getRecordCount(); i++) {
			JsonObject record = queryResults.getRecordAsObject(i);
					
			Assertions.assertEquals("Hera", record.get("FIRSTNAME").getAsString());
			Assertions.assertEquals(true, record.get("LIKES_TIRAMISU").getAsBoolean());
			Assertions.assertEquals(true, 20 < record.get("VALUE").getAsInt());

		}
						
	}
	
}
