package com.xresch.cfw.tests.various;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.utils.CFWFiles;

public class TestFileUtils {
	
	public static final String INTERNAL_RESOURCES_PATH = "com/pengtoolbox/cfw/resources";
	
	@Test
	public void testFileAssembly() {
		
		FileAssembly assembler = new FileAssembly("common", "js");
		
		CFWFiles.addAllowedPackage(FeatureCore.RESOURCE_PACKAGE);
		
		String assemblyName = assembler.addFile(FileDefinition.HandlingType.FILE, "./testdata", "test.css")
				.addFile(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE +".test", "junit_test.js")
				.addFileContent("/* just some comment */")
				.assemble()
				.cache()
				.getAssemblyName();
		
		FileAssembly cachedAssembly = FileAssembly.getAssemblyFromCache(assemblyName);
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
