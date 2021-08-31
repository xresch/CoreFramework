package com.xresch.cfw.tests.mail;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.mail.CFWMailer;
import com.xresch.cfw.tests._master.WebTestMaster;


@Tag("development")
public class MailTest extends WebTestMaster  {
	
	public static final String PACKAGE = "com.xresch.cfw.tests.mail.recources";
	@Test
	public void testMail() throws IOException {

		LinkedHashMap<String, String> recipientsTo = new LinkedHashMap<>();
		recipientsTo.put("testika@pengtoolbox.io", "Testika");
		recipientsTo.put("testonia@pengtoolbox.io", "Testonia");
		
		LinkedHashMap<String, String> recipientsCC = new LinkedHashMap<>();
		recipientsCC.put("testikaCC@pengtoolbox.io", "Testika CC");
		recipientsCC.put("testoniaCC@pengtoolbox.io", "Testonia CC");
		
		LinkedHashMap<String, String> recipientsBCC = new LinkedHashMap<>();
		recipientsBCC.put("testikaBCC@pengtoolbox.io", "Testika BCC");
		recipientsBCC.put("testoniaBCC@pengtoolbox.io", "Testonia BCC");
		
		String messageBody = "<!DOCTYPE HTML><html>SimpleEmail Testing Body. <strong>STRONG</strong></html>";
		
		new CFWMailer("Test Mail Builder", messageBody)
			.fromNoReply()
			.recipientsTo(recipientsTo)
			.recipientsCC(recipientsCC)
			.recipientsBCC(recipientsBCC)
			.send();

	}
	
	@Test
	public void testMailWithTextAttachment() throws IOException {

		CFW.Files.addAllowedPackage(PACKAGE);
		String attachmentContent = CFW.Files.readPackageResource(PACKAGE, "attachment_small.txt");

	    CFW.Mail.sendFromNoReplyWithAttachement(CFW.Mail.initializeSession(), 
	    		"test@pengtoolbox.io", 
	    		"AttachmentTest", 
	    		"See <strong>attachment</strong>", 
	    		attachmentContent, 
	    		"attachment_small.txt"
	    	);
	    
	}

}
