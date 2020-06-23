package com.pengtoolbox.cfw.tests.validation;

import java.sql.SQLException;

import org.junit.Test;

import com.pengtoolbox.cfw._main.CFW;

public class TestCFWSecurity {
	
	@Test
	public void testSanitizeHTML() throws SQLException {
		
		System.out.println("=========== Sanitize String ===========");
		System.out.println(CFW.Security.sanitizeHTML("Test"));
		
		System.out.println("=========== Sanitize Number ===========");
		System.out.println(CFW.Security.sanitizeHTML("8888"));
		
		System.out.println("=========== Sanitize URLs ===========");
		System.out.println(CFW.Security.sanitizeHTML("www.google.ch"));
		System.out.println(CFW.Security.sanitizeHTML("http://www.google.ch"));
		System.out.println(CFW.Security.sanitizeHTML("https://www.google.ch"));
		System.out.println(CFW.Security.sanitizeHTML("ftp://www.google.ch"));
		System.out.println(CFW.Security.sanitizeHTML("ftps://www.google.ch"));
		System.out.println(CFW.Security.sanitizeHTML("ldap://www.google.ch"));
		System.out.println(CFW.Security.sanitizeHTML("ldaps://www.google.ch"));
		System.out.println(CFW.Security.sanitizeHTML("http://www.google.ch:8888"));
		System.out.println(CFW.Security.sanitizeHTML("/resources/images/login_background.jpg"));
		
		System.out.println("=========== Sanitize HTML <a> ===========");
		System.out.println(CFW.Security.sanitizeHTML("<a href=\"http://www.google.com/\">Test Link</a>"));

		System.out.println("=========== Sanitize HTML <p> ===========");
		System.out.println(CFW.Security.sanitizeHTML("<p>Test Link</p>"));
		
		System.out.println("=========== Sanitize HTML <table> ===========");
		System.out.println(CFW.Security.sanitizeHTML("<table><tr><td>Table Cell</td></tr></table>"));
		
		System.out.println("=========== Sanitize HTML <ul> ===========");
		System.out.println(CFW.Security.sanitizeHTML("<ul><li>List Element</li></ul>"));
		
		System.out.println("=========== Sanitize HTML <ol> ===========");
		System.out.println(CFW.Security.sanitizeHTML("<ol><li>List Element</li></ol>"));
		//Assertions.assertNotNull(result, "Result was found.");
		
	}

}
