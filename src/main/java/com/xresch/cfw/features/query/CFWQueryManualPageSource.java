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
		// Parameters Section
		builder.append("<h2 class=\"toc-hidden\" >Parameters</h2>");
		CFWObject object = source.getParameters();
		
		builder.append("<ul>");
			for(CFWField entry : object.getFields().values()) {
				builder.append("<li><b>"+entry.getName()+":&nbsp;</b>")
					   .append(CFW.Security.escapeHTMLEntities(entry.getDescription()))
					   .append("</li>")
					   ;
			}
		builder.append("</ul>");
						
		
		//--------------------------------
		// Usage Section
		builder.append("<h2 class=\"toc-hidden\">Usage</h2>");
		builder.append("<div>"+source.descriptionHTML()+"</div>");
			
		
		this.content(builder.toString());
		
	}

}
