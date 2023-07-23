package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.TreeMap;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.manual.ManualPage;

public class CFWQueryManualPageRootForFunctions extends ManualPage {

	@SuppressWarnings("rawtypes")
	public CFWQueryManualPageRootForFunctions(ManualPage parent, String pageTitle) {
		//Set title to command name
		super(pageTitle);
		
		parent.addChild(this);
				
		StringBuilder builder = new StringBuilder();
		
		//--------------------------------
		// Short Description Section
		builder.append(
				  "<p>"
				+ "In this section you will find the functions supported of the query language. "
				+ "In the following table you find an overview of functions by their tags. A function can be listed for multiple tags."
				+ "</p>");
				
		
		//--------------------------------
		// Table Header
		builder.append("<table class=\"table table-sm table-striped\">");
		builder.append("<thead><tr>"
				+ "<th>Tag</th>"
				+ "<th>Functions</th>"
				+ "</tr></thead>");
		

		//--------------------------------
		// Get Sorted List of all used Tags
		CFWQuery pseudoQuery = new CFWQuery();
		
		TreeMap<String, ArrayList<CFWQueryFunction>> tagFunctionMap = CFW.Registry.Query.getFunctionsByTags();
		for(String tag : tagFunctionMap.keySet()) {
			
			builder.append(
					  "<tr>"
					+ "<td><span class=\"badge badge-primary\">"+tag+"</td>");
			
			builder.append("<td>");
			ArrayList<CFWQueryFunction> functions = tagFunctionMap.get(tag);
				for(CFWQueryFunction function : functions ) {
					String name = function.uniqueName();

					builder.append("<p>"
								+"<a class=\"monospace\" href=\"#\" onclick=\"cfw_manual_loadPage('"+this.resolvePath(null)+"|"+name+"');\">"
									+function.descriptionSyntax()
								+":</a>&nbsp;"+function.descriptionShort()+"</p>");
				
				}
			builder.append("</td>");
		}

		this.content(builder.toString());

	}

}
