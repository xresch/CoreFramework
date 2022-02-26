package com.xresch.cfw.features.query;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.manual.ManualPage;

public class CFWQueryManualPageSource extends ManualPage {

	@SuppressWarnings("rawtypes")
	public CFWQueryManualPageSource(String sourceName, CFWQuerySource source) {
		//Set title to command name
		super(sourceName);
				
		StringBuilder builder = new StringBuilder();
		

		//--------------------------------
		// Short Description Section
		builder.append("<p>"+source.descriptionShort()+"</p>");
		
		//--------------------------------
		// Permissions Section
		builder.append("<h2 class=\"toc-hidden\" >Required Permissions</h2>");
		builder.append("<p>"+CFW.Security.escapeHTMLEntities(source.descriptionRequiredPermission())+"</p>" );
		
		//--------------------------------
		// Parameters Section
		builder.append("<h2 class=\"toc-hidden\" >Parameters</h2>");
		
		builder.append(source.getParameterListHTML());
				
		
		//--------------------------------
		// Time Handling Section
		builder.append("<h2 class=\"toc-hidden\" >Time Handling</h2>");
		builder.append("<p>"+source.descriptionTime()+"</p>");
		
		//--------------------------------
		// Usage Section
		builder.append("<h2 class=\"toc-hidden\">Usage</h2>");
		builder.append("<div>"+source.descriptionHTML()+"</div>");
			
		
		this.content(builder.toString());
		
	}

}
