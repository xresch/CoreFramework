package com.xresch.cfw.features.query;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.manual.ManualPage;

public class CFWQueryManualPageFunction extends ManualPage {

	@SuppressWarnings("rawtypes")
	public CFWQueryManualPageFunction(String functionName, CFWQueryFunction function) {
		//Set title to command name
		super(functionName);
				
		StringBuilder builder = new StringBuilder();
		

		//--------------------------------
		// Short Description Section
		builder.append("<p>"+function.descriptionShort()+"</p>");
		
		builder.append("<h2 class=\"toc-hidden\">Syntax</h2>");
		builder.append("<pre><code class=\"language-cfwquery\">"
					+ CFW.Security.escapeHTMLEntities(function.descriptionSyntax())
					+"</code></pre>");
		
		String syntaxDetails = function.descriptionSyntaxDetailsHTML();
		if( !Strings.isNullOrEmpty(syntaxDetails) ) {
			builder.append("<div>"+syntaxDetails+"</div>");
			
		}
		
		//--------------------------------
		// Usage Section
		builder.append("<h2 class=\"toc-hidden\">Usage</h2>");
		builder.append("<div>"+function.descriptionHTML()+"</div>");
			
		
		this.content(builder.toString());
		
	}

}
