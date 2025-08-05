package com.xresch.cfw.tests.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TestCFWRandom {
	
	/*****************************************************
	 * This is a manual check, no assertions are done.
	 *****************************************************/
	@Test
	public void testCFWRandom() {
		
		//------------------------------------
		// Test Data
		//------------------------------------
		Set<String> stringSet = new HashSet<>();
		stringSet.add("Filament");
		stringSet.add("Fiber");
		stringSet.add("Floss");
		stringSet.add("Thread");
		stringSet.add("Yarn");
		stringSet.add("Twine");
		stringSet.add("String");
		stringSet.add("Cord");
		stringSet.add("Rope");
		stringSet.add("Towline");
		
		HashMap<String, String> map = new HashMap<>();
		map.put("hello", "world");
		map.put("world", "hello");
		map.put("foo", "bar");
		map.put("bar", "efoot");
		map.put("italian", "desserts");
		
		long latest = CFWTimeUnit.d.offset(null, 0);
		long earliest = CFWTimeUnit.d.offset(null, -1);
		
		//------------------------------------
		// Generate Data
		//------------------------------------
		Integer[] nullPercentArray = new Integer[] {100, 50, 0};
		
		for(Integer nullPercent : nullPercentArray) {
			
			System.out.println("================== Nulls: "+nullPercent+"% ==================");
			System.out.println("from(): " + CFW.Random.from("One", true, "Three", null) );  // well it works 😅 
			System.out.println("fromStrings(): " + CFW.Random.fromStrings("Tiramisu", "Panna Cotta", "Amaretti") );
			System.out.println("fromChars(): " + CFW.Random.fromChars('A', 'B', 'C') );
			System.out.println("fromInts(): " + CFW.Random.fromInts(1, 2, 3) );
			System.out.println("fromLongs(): " + CFW.Random.fromLongs(11L, 22L, 33L) );
			System.out.println("fromFloats(): " + CFW.Random.fromFloats(11.1f, 22.2f, 33.3f) );
			System.out.println("fromDoubles(): " + CFW.Random.fromDoubles(11.11d, 22.22d, 33.33d) );
			System.out.println("fromBigDecimals(): " + CFW.Random.fromBigDecimals(new BigDecimal("111.111"), new BigDecimal("123.456"), new BigDecimal("987.654")) );
			System.out.println("fromDates(): " 
									+ CFW.Random.fromDates(
											  new Date(Instant.now().toEpochMilli())
											, new Date(Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli())
											, new Date(Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli())
										) 
									);
			
			System.out.println("fromTimestamps(): " 
									+ CFW.Random.fromTimestamps(
											  new Timestamp(Instant.now().toEpochMilli())
											, new Timestamp(Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli())
											, new Timestamp(Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli())
										) 
									);
			
			System.out.println("fromInstants(): " 
									+ CFW.Random.fromInstants(
												  Instant.now()
												, Instant.now().minus(7, ChronoUnit.DAYS)
												, Instant.now().minus(14, ChronoUnit.DAYS)
											) 
									);
			
			
			System.out.println(" ");
			System.out.println("fromArray(String[]): " + CFW.Random.fromArray(new String[]{"StringA", "StringB", "StringC"}) );
			System.out.println("fromArray(Integer[]): " + CFW.Random.fromArray(new Integer[]{11, 22, 33, 44, 55}) );
			System.out.println("fromArray(nullPercent, Integer[]): " + CFW.Random.fromArray(nullPercent, new Integer[]{11, 22, 33, 44, 55}) );
			System.out.println("fromSet(nullPercent, stringSet): " + CFW.Random.fromSet(nullPercent, stringSet) );
			System.out.println("fromMap(nullPercent, map): " + CFW.Random.fromMap(nullPercent, map) );
			
			System.out.println(" ");
			System.out.println("string(16): " + CFW.Random.string(16) );
			System.out.println("stringAlphaNum(32): " + CFW.Random.stringAlphaNum(32) );
			System.out.println("stringAlphaNumSpecial(64): " + CFW.Random.stringAlphaNumSpecial(64) );
			
			System.out.println(" ");
			System.out.println("bool(nullPercent): " + CFW.Random.bool(nullPercent) );
			System.out.println("fromZeroToInteger(100): " + CFW.Random.fromZeroToInteger(100) );
			System.out.println("integer(42, 88): " + CFW.Random.integer(42, 88) );
			System.out.println("integer(42, 88, nullPercent): " + CFW.Random.integer(42, 88, nullPercent) );
			System.out.println("doubleInRange(1000, 1000_000): " + CFW.Random.doubleInRange(1000, 1000_000) );
			System.out.println("floatInRange(1.0f, 1000.0f): " + CFW.Random.floatInRange(1.0f, 1000.0f) );
			System.out.println("longInRange(1000_000L, 1000_000_000L): " + CFW.Random.longInRange(1000_000L, 1000_000_000L) );
			System.out.println("bigDecimal(1000_000L, 1000_000_000L): " + CFW.Random.bigDecimal(1000_000L, 1000_000_000L) );
			
			System.out.println(" ");
			System.out.println("epoch(earliest, latest): " + CFW.Random.epoch(earliest, latest) );
			System.out.println("date(earliest, latest): " + CFW.Random.date(earliest, latest).toString() );
			System.out.println("dateString(earliest, latest): " + CFW.Random.dateString(earliest, latest, "YYYY-MM-dd"));
			System.out.println("timestamp(earliest, latest): " + CFW.Random.timestamp(earliest, latest).toString() );
			System.out.println("instant(earliest, latest): " + CFW.Random.instant(earliest, latest).toString() );
			System.out.println("zonedDateTime(earliest, latest): " + CFW.Random.zonedDateTime(earliest, latest, ZoneOffset.UTC).toString() );

			System.out.println(" ");
			System.out.println("firstnameOfGod(nullPercent): " + CFW.Random.firstnameOfGod(nullPercent) );
			System.out.println("lastnameSweden(nullPercent): " + CFW.Random.lastnameSweden(nullPercent) );
			System.out.println("companyTitle(nullPercent): " + CFW.Random.companyTitle(nullPercent) );
			System.out.println("jobTitle(nullPercent): " + CFW.Random.jobTitle(nullPercent) );
			System.out.println("street(nullPercent): " + CFW.Random.street(nullPercent) );
			System.out.println("capital(nullPercent): " + CFW.Random.capitalCity(nullPercent) );
			System.out.println("country(nullPercent): " + CFW.Random.country(nullPercent) );
			System.out.println("countryCode(nullPercent): " + CFW.Random.countryCode(nullPercent) );
			System.out.println("continent(nullPercent): " + CFW.Random.continent(nullPercent) );
			System.out.println("countryData(nullPercent): " + CFW.JSON.toJSON(CFW.Random.countryData()) );
			System.out.println("phoneNumber(nullPercent): " + CFW.Random.phoneNumber(nullPercent) );
			
			System.out.println(" ");
			System.out.println("mythicalLocation(nullPercent): " + CFW.Random.mythicalLocation(nullPercent) );
			System.out.println("colorName(nullPercent): " + CFW.Random.colorName(nullPercent) );
			System.out.println("colorSL(nullPercent): " + CFW.Random.colorSL(200, 20, 80, 20, 80) );
			System.out.println("exaggaratingAdjective(nullPercent): " + CFW.Random.exaggaratingAdjective(nullPercent) );
			System.out.println("fruitName(nullPercent): " + CFW.Random.fruitName(nullPercent) );
			System.out.println("issueResolvedMessage(nullPercent): " + CFW.Random.issueResolvedMessage(nullPercent) );
			System.out.println("italianDessert(nullPercent): " + CFW.Random.italianDessert(nullPercent) );
			System.out.println("messageOfObedience(nullPercent): " + CFW.Random.messageOfObedience(nullPercent) );
			System.out.println("methodName(nullPercent): " + CFW.Random.methodName(nullPercent) );
			System.out.println("statisticsTitle(nullPercent): " + CFW.Random.statisticsTitle(nullPercent) );
			System.out.println("ultimateServiceName(nullPercent): " + CFW.Random.ultimateServiceName(nullPercent) );
			
			System.out.println(" ");
			System.out.println("loremIpsum(128): '" + CFW.Random.loremIpsum(128)+"'" );
			System.out.println("loremIpsum(256): '" + CFW.Random.loremIpsum(256)+"'" );
			System.out.println("loremIpsum(512): '" + CFW.Random.loremIpsum(512)+"'" );
			System.out.println("loremIpsum(1024): '" + CFW.Random.loremIpsum(1024)+"'" );
			System.out.println("loremIpsum(2049): '" + CFW.Random.loremIpsum(2049)+"'" );
		}
		
	}

	
	
}
