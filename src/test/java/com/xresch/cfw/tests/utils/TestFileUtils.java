package com.xresch.cfw.tests.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.tests._master.WebTestMaster;
import com.xresch.cfw.utils.files.CFWFiles;

public class TestFileUtils {
		
	@Test
	public void testFileAssembly() {
		
		FileAssembly assembler = new FileAssembly("common", "js");
		
		CFWFiles.addAllowedPackage(WebTestMaster.RESOURCE_PACKAGE);
		
		String assemblyName = assembler.addFile(FileDefinition.HandlingType.FILE, "./testdata", "test.css")
				.addFile(FileDefinition.HandlingType.JAR_RESOURCE, WebTestMaster.RESOURCE_PACKAGE, "junit_test.js")
				.addFileContent("/* just some comment */")
				.assembleAndCache()
				.getAssemblyName();
		
		FileAssembly cachedAssembly = FileAssembly.getAssembly(assemblyName);
		Assertions.assertNotNull(cachedAssembly, "Assembly is not null");
		
		System.out.println(cachedAssembly.getAssemblyContent());
		
		Assertions.assertTrue( cachedAssembly.getAssemblyContent().contains(".test{display: block;}"),
				"Contains the CSS string, FILE successfully loaded.");
		
		Assertions.assertTrue( cachedAssembly.getAssemblyContent().contains("function test(){alert('JUnit');}"),
				"Contains the javascript string, JAR_RESOURCE successfully loaded.");
		
		Assertions.assertTrue( cachedAssembly.getAssemblyContent().contains("/* just some comment */"),
				"Contains the manual string, STRING successfully loaded.");
		
	}
	
}
