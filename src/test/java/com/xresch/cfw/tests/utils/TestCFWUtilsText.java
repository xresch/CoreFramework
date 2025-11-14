package com.xresch.cfw.tests.utils;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.utils.csv.CFWCSV;

public class TestCFWUtilsText {
	
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testIsCharacterEscaped() {
		
		ArrayList<String> splitted;
		int i ;
		boolean isEscaped;
		
		//---------------------------------
		// With Various Quotes
		//---------------------------------
		isEscaped = CFW.Text.isCharacterEscaped("""
				ab\\cd""", 3);
		
		Assertions.assertEquals(true, isEscaped);

		//---------------------------------
		// With Various Quotes
		//---------------------------------
		isEscaped = CFW.Text.isCharacterEscaped("""
				ab\\\\cd""", 4);
		
		Assertions.assertEquals(false, isEscaped);
	}
	
	
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testSplitQuotesAware() {
		
		ArrayList<String> splitted;
		int i ;
		
		//---------------------------------
		// With Various Quotes
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware(" ", """
				a "b c" 'd e' `f g` h""", true, true, true, false);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(5, splitted.size());
		Assertions.assertEquals("a", splitted.get(++i));
		Assertions.assertEquals("b c", splitted.get(++i));
		Assertions.assertEquals("d e", splitted.get(++i));
		Assertions.assertEquals("f g", splitted.get(++i));
		Assertions.assertEquals("h", splitted.get(++i));
		
		
		//---------------------------------
		// Unescape Quotes
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware(" ", "a \"b \\\" c\" d", true, true, true, false);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(3, splitted.size());
		Assertions.assertEquals("a", splitted.get(++i));
		Assertions.assertEquals("b \" c", splitted.get(++i));
		Assertions.assertEquals("d", splitted.get(++i));

		
		//---------------------------------
		// Single Quotes Only
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware(" ", """
				a "b c" 'd e' `f g` h""", false, true, false, false);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(7, splitted.size());
		Assertions.assertEquals("a", splitted.get(++i));
		Assertions.assertEquals("\"b", splitted.get(++i));
		Assertions.assertEquals("c\"", splitted.get(++i));
		Assertions.assertEquals("d e", splitted.get(++i));
		Assertions.assertEquals("`f", splitted.get(++i));
		Assertions.assertEquals("g`", splitted.get(++i));
		Assertions.assertEquals("h", splitted.get(++i));
		
		
		//---------------------------------
		// Command Line Style
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware(" ", """
				java -jar -DmyProps.test="abc def" -DmyProps.foobar="y z" """, true, false, false, false);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(4, splitted.size());
		Assertions.assertEquals("java", splitted.get(++i));
		Assertions.assertEquals("-jar", splitted.get(++i));
		Assertions.assertEquals("-DmyProps.test=\"abc def\"", splitted.get(++i));
		Assertions.assertEquals("-DmyProps.foobar=\"y z\"", splitted.get(++i));

		//---------------------------------
		// Command Line Style all Quotes
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware(" ", """
				java -jar acme.jar -DmyProps.test="abc def" -DmyProps.foobar='y z' -DmyProps.yay=`Woohoo!`""", true, true, true, false);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(6, splitted.size());
		Assertions.assertEquals("java", splitted.get(++i));
		Assertions.assertEquals("-jar", splitted.get(++i));
		Assertions.assertEquals("acme.jar", splitted.get(++i));
		Assertions.assertEquals("-DmyProps.test=\"abc def\"", splitted.get(++i));
		Assertions.assertEquals("-DmyProps.foobar='y z'", splitted.get(++i));
		Assertions.assertEquals("-DmyProps.yay=`Woohoo!`", splitted.get(++i));


		//---------------------------------
		// Command Line Pipes
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware("|", """
				echo "hello world !" | wc -l | sort -u""", true, true, true, false);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(3, splitted.size());
		Assertions.assertEquals("echo \"hello world !\" ", splitted.get(++i));
		Assertions.assertEquals(" wc -l ", splitted.get(++i));
		Assertions.assertEquals(" sort -u", splitted.get(++i));

		//---------------------------------
		// Escaped Separators
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware("\n", """
				curl -H "Cookie: CFWSESSIONID=token" -G \\
				--data-urlencode "QUERY=| source random limit=10" \\
				-X GET "http://localhost:8888/app/api"
				echo "next command" """, true, true, true, true);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(2, splitted.size());
		Assertions.assertEquals("curl -H \"Cookie: CFWSESSIONID=token\" -G \n"
				+ "--data-urlencode \"QUERY=| source random limit=10\" \n"
				+ "-X GET \"http://localhost:8888/app/api\"", splitted.get(++i));
		Assertions.assertEquals("echo \"next command\"", splitted.get(++i));
		
		//---------------------------------
		// CLI Pipeline Escaped
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware("|", """
				cmd /c echo "hello world" > file.txt \\| type file.txt""", true, true, true, true);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(1, splitted.size());
		Assertions.assertEquals("cmd /c echo \"hello world\" > file.txt | type file.txt", splitted.get(++i));
		
		//---------------------------------
		// CLI Multiline pipeline
		//---------------------------------
		splitted = CFW.Text.splitQuotesAware("\n", """
				echo 'test me' \\
				| wc -l \\
				| uniq -c """, true, true, true, true);
		
		System.out.println("splitted: "+CFW.JSON.toJSON(splitted));
		
		i=-1;
		Assertions.assertEquals(1, splitted.size());
		Assertions.assertEquals("echo 'test me' \n| wc -l \n| uniq -c", splitted.get(++i));
		
		
	}
	
}
