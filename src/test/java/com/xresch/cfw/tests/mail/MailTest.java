package com.xresch.cfw.tests.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.mail.CFWMailBuilder;
import com.xresch.cfw.tests._master.WebTestMaster;


@Tag("development")
public class MailTest extends WebTestMaster  {
	
	public static final String PACKAGE = "com.xresch.cfw.tests.mail.recources";
	@Test
	public void testMail() throws IOException {

		LinkedHashMap<String, String> recipientsTo = new LinkedHashMap<>();
		recipientsTo.put("testika@xresch.com", "Testika");
		recipientsTo.put("testonia@xresch.com", "Testonia");
		
		LinkedHashMap<String, String> recipientsCC = new LinkedHashMap<>();
		recipientsCC.put("testikaCC@xresch.com", "Testika CC");
		recipientsCC.put("testoniaCC@xresch.com", "Testonia CC");
		
		LinkedHashMap<String, String> recipientsBCC = new LinkedHashMap<>();
		recipientsBCC.put("testikaBCC@xresch.com", "Testika BCC");
		recipientsBCC.put("testoniaBCC@xresch.com", "Testonia BCC");
		
		String messageBody = "<!DOCTYPE HTML><html>SimpleEmail Testing Body. <strong>STRONG</strong></html>";
		
		CFW.Files.addAllowedPackage(PACKAGE);
		String attachmentContent = CFW.Files.readPackageResource(PACKAGE, "attachment_small.txt");
		
		FileInputStream pngInputStream = new FileInputStream("./testdata/image.png");
		FileInputStream pdfInputStream = new FileInputStream("./testdata/document.pdf");
		byte[] pdfBytes = CFW.Files.readBytesFromInputStream(pdfInputStream);
		
		new CFWMailBuilder("Test CFWMailer", messageBody)
			.addMessage("<i>Second Message</i>", true)
			.addMessage("<p>Third Message</p>", true)
			.fromNoReply()
			.replyTo("replyToMeC@xresch.com", "REPLY TO ME")
			.recipientsTo(recipientsTo)
			.recipientsCC(recipientsCC)
			.recipientsBCC(recipientsBCC)
			.addAttachment("attachment_small.txt", attachmentContent)
			.addAttachment("custom.txt", "somefile")
			.addAttachment(new File("./testdata/test.css"))
			.addAttachment("image.png", pngInputStream, "image/png")
			.addAttachment("document.pdf", pdfBytes, "application/pdf")
			.send();

	}
	

}
