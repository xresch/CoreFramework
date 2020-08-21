package com.xresch.cfw.tests.security;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.xresch.cfw._main.CFW;

public class TestCFWSecurity {
	
	@Test
	public void testSanitizeHTML() throws SQLException {
		
		System.out.println("=========== Sanitize String ===========");
			String value = "Test";
			String sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);

		
		System.out.println("=========== Sanitize Number ===========");
			value = "8888";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);
		
		
		System.out.println("=========== Sanitize URLs ===========");
			value = "www.google.ch";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
			System.out.println(sanitized);
		
			value = "http://www.google.ch";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
			System.out.println(sanitized);
			
			value = "https://www.google.ch";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
			System.out.println(sanitized);
			
			value = "ftp://www.google.ch";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
			System.out.println(sanitized);
			
			value = "ftps://www.google.ch";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
			System.out.println(sanitized);
			
			value = "ldap://www.google.ch";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
			System.out.println(sanitized);
			
			value = "ldaps://www.google.ch";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
			System.out.println(sanitized);
			
			value = "http://www.google.ch:8888";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
			System.out.println(sanitized);
			
			value = "/resources/images/login_background.jpg";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);

		
		System.out.println("=========== Sanitize HTML <a> ===========");
			value = "<a href=\"http://www.google.com/\">Test Link</a>";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);

		
		System.out.println("=========== Sanitize HTML <p> ===========");
			value = "<p>Test Link</p>";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);
		

		System.out.println("=========== Sanitize HTML <table> ===========");
			value = "<table><tr><td>Table Cell</td></tr></table>";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals("<table><tbody><tr><td>Table Cell</td></tr></tbody></table>", sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);

		
		System.out.println("=========== Sanitize HTML <ul> ===========");
			value = "<ul><li>List Element</li></ul>";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);

		
		System.out.println("=========== Sanitize HTML <ol> ===========");
			value = "<ol><li>List Element</li></ol>";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals(value, sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);

		
		System.out.println("=========== Sanitize Remove <script> ===========");
			value = "<div><script>alert('test');</script></div>";
			sanitized = CFW.Security.sanitizeHTML(value);
			Assertions.assertEquals("<div></div>", sanitized, "The string is sanitized as expected.");
		System.out.println(sanitized);
	}

}
