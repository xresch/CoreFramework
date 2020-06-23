package com.pengtoolbox.cfw.servlets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class JSONServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		String filename = request.getParameter("file");
		
		JSONResponse json = new JSONResponse();
		StringBuffer content = json.getContent();
		
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        
        List<String> fileContent = Files.readAllLines(Paths.get("./resources/html/"+filename), Charset.forName("UTF-8"));
        
        for(String line : fileContent){
        	content.append(line);
        	content.append("\n");
    	}
    }
}