package com.xresch.cfw.tests.cli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.cli.ArgumentsException;

public class TestCFWCommandLine {
	
	@Test
	public void testLoadArguments() throws ArgumentsException {
		
		CFW.CLI.readArguments(new String[] {"-config.file=./config/cfw.properties"});
		
		CFW.CLI.printUsage();
		CFW.CLI.printLoadedArguments();
		Assertions.assertTrue(CFW.CLI.validateArguments());
		
		CFW.CLI.readArguments(new String[] {"-config.unknownargument=./config/cfw.properties",
											"-config.file=./xxxxx/unknownpath.properties"});
		
		
		Assertions.assertFalse(CFW.CLI.validateArguments());
		
		String messages = CFW.CLI.getInvalidMessagesAsString();
		System.out.println(messages);
		
		Assertions.assertTrue(messages.contains("File cannot be read: './xxxxx/unknownpath.properties'"));
		Assertions.assertTrue(messages.contains("The argument '-config.unknownargument' is not supported."));
		
		// IMPORTANT! Keep this or it will mess up your other tests
		CFW.CLI.clearLoadedArguments();

	}
	
}
