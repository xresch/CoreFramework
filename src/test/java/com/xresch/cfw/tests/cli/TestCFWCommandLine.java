package com.xresch.cfw.tests.cli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.cli.ArgumentsException;

public class TestCFWCommandLine {
	
	@Test
	public void testLoadArguments() throws ArgumentsException {
		
		CFW.CLI.readArguments(new String[] {"cfw.config.folder=./config"});
		CFW.CLI.readArguments(new String[] {"cfw.config.filename=cfw.properties"});
		
		CFW.CLI.printUsage();
		CFW.CLI.printLoadedArguments();
		
		boolean isValid = CFW.CLI.validateArguments();
		String messages = CFW.CLI.getInvalidMessagesAsString();
		System.out.println(messages);
		Assertions.assertTrue(isValid);

		CFW.CLI.readArguments(new String[] {"config.unknownargument=./config/cfw.properties",
											"cfw.config.folder=./unknownfolder",
											"cfw.config.filename=unknownfile."
											}
										);
		
		
		Assertions.assertFalse(CFW.CLI.validateArguments());
		
		messages = CFW.CLI.getInvalidMessagesAsString();
		System.out.println(messages);
		
		Assertions.assertTrue(messages.contains("The argument 'config.unknownargument' is not supported."));
		
		// IMPORTANT! Keep this or it will mess up your other tests
		CFW.CLI.clearLoadedArguments();

	}
	
}
