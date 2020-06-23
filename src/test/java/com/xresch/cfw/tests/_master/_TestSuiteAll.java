package com.xresch.cfw.tests._master;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.xresch.cfw.tests.db.TestCFWDBUserManagement;
import com.xresch.cfw.tests.localization.LocalizationTests;
import com.xresch.cfw.tests.validation.CFWValidationTests;
import com.xresch.cfw.tests.various.TestCFWCommandLine;
import com.xresch.cfw.tests.various.TestFileUtils;
import com.xresch.cfw.tests.web.GeneralWebTests;
import com.xresch.cfw.tests.web.MenuTests;
@RunWith(Suite.class)

@Suite.SuiteClasses({
   CFWValidationTests.class,
   MenuTests.class,
   TestCFWCommandLine.class,
   TestCFWDBUserManagement.class,
   TestFileUtils.class,
   LocalizationTests.class,
   GeneralWebTests.class
})

public class _TestSuiteAll { 
	
}  
