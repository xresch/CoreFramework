package com.pengtoolbox.cfw.tests.localization;

import java.util.Locale;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;

public class LocalizationTests {
	
	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.cfw.tests.localization";
	
	@Test
	public void testLocaleMerging() {
		
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "testlang_en.properties"));
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "testlangoverride_en.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, "", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "testlang_de.properties"));
		
		Properties result = CFW.Localization.getLanguagePack(new Locale[] {Locale.ENGLISH, Locale.GERMAN}, "");
		
		String resultString = result.toString();
		System.out.println(resultString);
		
		Assertions.assertTrue(resultString.contains("cfw_test_stayenglish=stayEnglish"), "English is loaded." );
		Assertions.assertTrue(resultString.contains("cfw_test_override=isOverridden"), "Later english files override previous files." );

		Assertions.assertTrue(resultString.contains("cfw_test_german=Deutsch"), "German is loaded." );
		Assertions.assertTrue(resultString.contains("cfw_test_valueA=Wert A"), "Later locale GERMAN overrides previous locale ENGLISH." );
		Assertions.assertTrue(resultString.contains("cfw_test_edit=Bearbeiten"), "Later locale GERMAN overrides previous locale ENGLISH." );
		
		System.out.println(CFW.JSON.toJSON(result));
		System.out.flush();
	}
	
	@Test
	public void testLocaleCFWL() {
		
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "testlang_en.properties"));
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "testlangoverride_en.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, "", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "testlang_de.properties"));
		
		Assertions.assertTrue(CFW.L("cfw_test_stayenglish", "Default value").contentEquals("stayEnglish"), "Value is returned correctly." );
		Assertions.assertTrue(CFW.L("cfw_test_doesntexists", "Default Value").contentEquals("Default Value"), "Default Value is used." );

	}

	
}
