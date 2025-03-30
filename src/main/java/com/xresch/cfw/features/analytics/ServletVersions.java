package com.xresch.cfw.features.analytics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.HTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletVersions extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static TreeMap<String, String> versions = null;
	
	/******************************************************************
	 *
	 ******************************************************************/
	private TreeMap<String, String> getVersionsMap(){
		
		if(versions == null ) {
			versions = new TreeMap<>();
			
			
			//-----------------------------
			// Load Lib Versions
			if( Files.isDirectory(Paths.get("./lib")) ){
				Set<String> libFiles = CFW.Files.listFilesInFolder("./lib", false);
				for(String file : libFiles) {
					
					file = file.replace(".jar", "");
					int index = file.lastIndexOf("-");
					if(index != -1) {
						String name = file.substring(0, index);
						String version = file.substring(index+1);
						versions.put(name, version);
					}else {
						versions.put(file, "");
					}
				}
			}
			
			//-----------------------------
			// Load Extensions Versions
			if( Files.isDirectory(Paths.get("./extensions")) ){
				Set<String> extensionsFiles = CFW.Files.listFilesInFolder("./extensions", false);
				for(String file : extensionsFiles) {
					file = file.replace(".jar", "");
					int index = file.lastIndexOf("-");
					if(index != -1) {
						String name = file.substring(0, index);
						String version = file.substring(index+1);
						versions.put(name, version);
					}else {
						versions.put(file, "");
					}
				}
			}
		}
		
		return versions;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)) {
			
			HTMLResponse html = new HTMLResponse("Versions");
			StringBuilder content = html.getContent();

			
			//------------------------------
			// Add Content

			content.append("<h1>Versions</h1>");		
			content.append("<div class=\"table-responsive\">");	
			
			content.append("<table class=\"table table-sm table-striped\">");
			content.append("<thead><tr><th>Version</th><th>Component</th></tr></thead>");
			content.append("<tbody>");
				for(Entry<String, String> entry : getVersionsMap().entrySet()) {
					
					content.append("<tr>")
					.append("<td>"+entry.getValue()+"</td>")
						.append("<td>"+entry.getKey()+"</td>")
					.append("</tr>");
				}
			content.append("</tbody></table>");

	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
		
		}else {
			CFW.Messages.accessDenied();
		}
        
    }
	
}