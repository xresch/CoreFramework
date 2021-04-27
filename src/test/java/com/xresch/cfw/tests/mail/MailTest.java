package com.xresch.cfw.tests.mail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;

@Tag("development")
public class MailTest {
	
	@Test
	public void testMail() throws IOException {
		CFW.Properties.loadProperties(CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER));
	    CFW.Mail.sendFromNoReply("test@pengtoolbox.io", "Testing Subject", "<!DOCTYPE HTML><html>SimpleEmail Testing Body. <strong>STRONG</strong></html>");
	}
	
	@Test
	public void testMailWithAttachment() throws IOException {
		CFW.Properties.loadProperties(CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER));
		
		File attachment = new File(CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER));
	    CFW.Mail.sendFromNoReplyWithAttachement("test@pengtoolbox.io", "AttachmentTest", "See <strong>attachment</strong>", attachment);
	}

}
