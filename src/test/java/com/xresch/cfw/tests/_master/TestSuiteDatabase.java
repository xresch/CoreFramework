package com.xresch.cfw.tests._master;


import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)

@SuiteDisplayName("Database Tests")
@SelectPackages("com.xresch.cfw.tests.db")
@ExcludeTags("development")
public class TestSuiteDatabase { 
	
}  
