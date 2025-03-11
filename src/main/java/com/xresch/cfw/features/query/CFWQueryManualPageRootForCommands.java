package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.manual.ManualPage;

public class CFWQueryManualPageRootForCommands extends ManualPage {

	@SuppressWarnings("rawtypes")
	public CFWQueryManualPageRootForCommands(ManualPage parent, String pageTitle) {
		//Set title to command name
		super(pageTitle);
		
		parent.addChild(this);
				
		StringBuilder builder = new StringBuilder();
		
		//--------------------------------
		// Short Description Section
		builder.append(
				  "<p>"
				+ "In this section you will find the commands supported of the query language. "
				+ "In the following table you find an overview of commands ordered by their tags. A command can be listed for multiple tags."
				+ "</p>");
				
		
		//--------------------------------
		// Table Header
		builder.append("<table class=\"table table-sm table-striped\">");
		builder.append("<thead><tr>"
				+ "<th>Tag</th>"
				+ "<th>Command</th>"
				+ "</tr></thead>");
		

		//--------------------------------
		// Get Sorted List of all used Tags
		CFWQuery pseudoQuery = new CFWQuery();
		
		TreeMap<String, ArrayList<CFWQueryCommand>> tagCommandMap = CFW.Registry.Query.getCommandsByTags();
		for(String tag : tagCommandMap.keySet()) {
			
			
			builder.append(
					  "<tr>"
					+ "<td><span class=\"badge badge-primary\">"+tag+"</td>");
			
			builder.append("<td>");
				ArrayList<CFWQueryCommand> commands = tagCommandMap.get(tag);

				for(CFWQueryCommand command : commands ) {
					String name = command.getUniqueName();

					builder.append("<p>"
								+"<a class=\"monospace\" href=\"#\" onclick=\"cfw_manual_loadPage('"+this.resolvePath(null)+"|"+name+"');\">"
									+name
								+":</a>&nbsp;"+command.descriptionShort()+"</p>");
				
				}
			builder.append("</td>");
		}

		this.content(builder.toString());

	}

}
