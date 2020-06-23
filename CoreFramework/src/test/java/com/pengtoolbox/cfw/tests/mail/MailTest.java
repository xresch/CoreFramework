package com.pengtoolbox.cfw.tests.mail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.pengtoolbox.cfw._main.CFW;

public class MailTest {
	
	@Test
	public void testMail() throws IOException {
		CFW.Properties.loadProperties(CFW.CLI.getValue(CFW.CLI.CONFIG_FILE));
	    CFW.Mail.sendFromNoReply("test@pengtoolbox.io", "Testing Subject", "<!DOCTYPE HTML><html>SimpleEmail Testing Body. <strong>STRONG</strong></html>");
	}
	
	@Test
	public void testMailWithAttachment() throws IOException {
		CFW.Properties.loadProperties(CFW.CLI.getValue(CFW.CLI.CONFIG_FILE));
		
		File attachment = new File(CFW.CLI.getValue(CFW.CLI.CONFIG_FILE));
	    CFW.Mail.sendFromNoReplyWithAttachement("test@pengtoolbox.io", "AttachmentTest", "See <strong>attachment</strong>", attachment);
	}

}
