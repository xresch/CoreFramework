package com.xresch.cfw.tests.pipeline;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.pipeline.StringProcessingPipeline;
import com.xresch.cfw.tests._master.WebTestMaster;

public class TestPipeline {

	@Test
	public void testPipeline() throws InterruptedException {
		
		CFW.Files.addAllowedPackage(WebTestMaster.RESOURCE_PACKAGE);

		//------------------------------
		// Count Lines
		StringProcessingPipeline pipe = new StringProcessingPipeline();
		System.out.println(	"### Line Count Before: "+
			pipe.countLines()
				.data(CFW.Files.readPackageResource(WebTestMaster.RESOURCE_PACKAGE, "cfwjs_test.js").split("\\r\\n|\\n"))
				.execute(-1, true)
				.resultToString()
		);
		
		//------------------------------
		// Remove Blanks
		pipe = new StringProcessingPipeline();
		pipe.removeBlankLines()
			.removeComments()
			.trim()
			//.grep("cfwT", false)
			//.countLines()
			.data(CFW.Files.readPackageResource(WebTestMaster.RESOURCE_PACKAGE, "cfwjs_test.js").split("\\r\\n|\\n"))
			.execute(-1, false);
			
		System.out.println(	
			pipe.waitForComplete(-1)
				.resultToString()
		);
		
		//------------------------------
		// Count Lines
		pipe = new StringProcessingPipeline();
		pipe.removeBlankLines()
			.removeComments()
			.trim()
			//.grep("cfwT", false)
			.countLines()
			.data(CFW.Files.readPackageResource(WebTestMaster.RESOURCE_PACKAGE, "cfwjs_test.js").split("\\r\\n|\\n"))
			.execute(-1, false);
		
		String lineCountAfter = pipe.waitForComplete(-1).resultToString();
		System.out.println(	"### Line Count After: "+lineCountAfter);
		
		Assertions.assertEquals("54", lineCountAfter.trim());
	   
	}
}
