package com.xresch.cfw.tests.web.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;


public class SeleniumTest {


    //@Test
	public void seleniumTest() {

	    WebDriverManager.chromedriver().setup();
	    
	    ChromeOptions options = new ChromeOptions();
	    options.addArguments("start-maximized");
	    ChromeDriver driver = new ChromeDriver(options);
	    
	    driver.get("http://localhost:8888/");

	    System.out.println(driver.getTitle());
	}
	
}