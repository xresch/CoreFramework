package com.pengtoolbox.cfw.tests.web;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.tests._master.WebTestMaster;
import com.pengtoolbox.cfw.tests.assets.servlets.GeneralTestServlet;

public class GeneralWebTests extends WebTestMaster {

	@Test
	public void testLocalization(){
		
		addServlet(GeneralTestServlet.class, "/general");
		String response = CFW.HTTP.sendGETRequest(TEST_URL+"/general").getResponseBody();
		
		
		//Assertions.assertTrue(response.contains("<strong>Localization Test(success):</strong> cfw_lang_test_value<p>"), 
		//		"Check if localization string is found.");
		
		Assertions.assertTrue(response.contains("Localization Test(success if 'lang.does.not.exist'):</strong> {!lang.does.not.exist!}<p>"), 
				"Check if localization string is ignored when undefined.");
		
		
		//System.out.println(response);
	}
	
	@Test
	public void testAlertMessages(){
		
		addServlet(GeneralTestServlet.class, "/general");
		String response = CFW.HTTP.sendGETRequest(TEST_URL+"/general").getResponseBody();
		
		Assertions.assertTrue(response.contains("<div class=\"alert alert-dismissible alert-info\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>this is an info.</div>"),
				"TemplateHTMLDefault.addAlert(AlertType.INFO) creates a info message.");
		
		Assertions.assertTrue(response.contains("<div class=\"alert alert-dismissible alert-warning\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>this is a warning.</div>"),
				"TemplateHTMLDefault.addAlert(AlertType.WARNING) creates a warning message.");
		
		Assertions.assertTrue(response.contains("<div class=\"alert alert-dismissible alert-danger\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>this is an error.</div>"),
				"TemplateHTMLDefault.addAlert(AlertType.ERROR) creates a danger message.");
		
		Assertions.assertTrue(response.contains("<div class=\"alert alert-dismissible alert-success\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>this is a success.</div>"),
				"TemplateHTMLDefault.addAlert(AlertType.SUCCESS) creates a success message.");
		
		Assertions.assertEquals(
				response.indexOf("<div class=\"alert alert-dismissible alert-success\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>Test make same message unique...</div>"),
				response.lastIndexOf("<div class=\"alert alert-dismissible alert-success\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>Test make same message unique...</div>"),
				"TemplateHTMLDefault.addAlert(AlertType.SUCCESS) ignores redundant message.");
		
		
		//System.out.println(response);
	}
	
	@Test
	public void testAssembly(){
		
		addServlet(GeneralTestServlet.class, "/general");
		String response = CFW.HTTP.sendGETRequest(TEST_URL+"/general").getResponseBody();
		
		Assertions.assertTrue(response.contains("<link rel=\"stylesheet\" href=\"/cfw/assembly?name=css_assembly_"),
				"CSS Assembly is added to page.");
		
		Assertions.assertTrue(response.contains("<script src=\"/cfw/assembly?name=js_assembly_cfw_"),
				"Javascript Assembly is added to page.");
		
		//System.out.println(response);
	}
	
	@Test
	public void testAddSingleJS(){
		
		addServlet(GeneralTestServlet.class, "/general");
		String response = CFW.HTTP.sendGETRequest(TEST_URL+"/general").getResponseBody();
		
		Assertions.assertTrue(response.contains("/*Test*/Math.random();"),
				"AbstractHTMLResponse.addJavascriptCode() adds custom javascript.");
		
		Assertions.assertTrue(response.contains("<script src=\"/resources/js/custom.js\"></script>"),
				"html.addJSFileBottomSingle(HandlingType.FILE, ...) adds file resource.");
		
		Assertions.assertTrue(response.contains("<script src=\"/cfw/jarresource?pkg=com.pengtoolbox.cfw.resources.test&file=junit_test.js\">"),
				"html.addJSFileBottomSingle(HandlingType.JAR_RESOURCE, ...) adds package resource.");
		
		System.out.println(response);
	}
	
	
}
