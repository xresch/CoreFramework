package com.xresch.cfw.tests._master;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All Query Tests")
@SelectPackages("com.xresch.cfw.tests.features.query")
@IncludeClassNamePatterns("(Test.*|.+[.$]Test.*|.*Tests?)")
@ExcludeTags("development")
public class SuiteFeatureQuery { 
	
}  
