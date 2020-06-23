package com.pengtoolbox.cfw.tests.assets.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWContextRequest;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWForm;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;

public class GeneralTestServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(GeneralTestServlet.class.getName());

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		
		CFWLog log = new CFWLog(logger).method("doGet");
		
		HTMLResponse html = new HTMLResponse("Test Page");
		html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH+".test", "cfwjs_test.js"));
		StringBuffer content = html.getContent();
		
		//--------------------------
		// Add single javascripts
		//--------------------------
		html.addJSFileBottomSingle(new FileDefinition(FileDefinition.HandlingType.FILE, "/resources/js", "custom.js"));
		html.addJSFileBottomSingle(new FileDefinition(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH+".test", "junit_test.js"));
		html.addJavascriptCode("/*Test*/Math.random();");
		
		//--------------------------
		//Add messages manually
		//--------------------------
		CFWContextRequest.addAlertMessage(MessageType.INFO, "this is an info.");
		CFWContextRequest.addAlertMessage(MessageType.WARNING, "this is a warning.");
		CFWContextRequest.addAlertMessage(MessageType.ERROR, "this is an error.");
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, "this is a success.");
		
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, "Test make same message unique...");
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, "Test make same message unique...");
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, "Test make same message unique...");
		CFWContextRequest.addAlertMessage(MessageType.SUCCESS, "Test make same message unique...");
		//------------------------------
		//Add messages by log exception
		//------------------------------
		Throwable severe = new ArrayIndexOutOfBoundsException("You went over the bounds.");
		log.severe("Test - Oops!!!Something went severly wrong...", severe);
		
		Throwable warn = new NumberFormatException("The format is Wrong!!!");
		log.warn("Test - Oops!!! some warning...", warn);
		
		//------------------------------
		// Test cannot read file
		//------------------------------
		String cannotReadFile = CFW.Files.getFileContent(request, "./resources/this_file_does_not_exists.txt");

		//------------------------------
		// Test Localization
		//------------------------------
		content.append("<p><strong>Localization Test(success if 'Edit'):</strong> {!cfw_test_edit!}<p>");
		content.append("<p><strong>Localization Test(success if 'lang.does.not.exist'):</strong> {!lang.does.not.exist!}<p>");
		
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        
       
//        List<String> fileContent = Files.readAllLines(Paths.get("./resources/html/"+htmlfile), Charset.forName("UTF-8"));
//        
//        StringBuffer content = html.getContent();
//        
//        for(String line : fileContent){
//        	content.append(line);
//        	content.append("\n");
//    	}
        
    }
}