package com.xresch.cfw.tests._master;


import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@IncludeClassNamePatterns("(Test.*|.+[.$]Test.*|.*Tests?)")
@SuiteDisplayName("Database Tests")
@SelectPackages("com.xresch.cfw.tests.db")
@ExcludeTags("development")
public class SuiteDatabase { 
	
}  
