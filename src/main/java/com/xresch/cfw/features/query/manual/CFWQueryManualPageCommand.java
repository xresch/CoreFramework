package com.xresch.cfw.features.query.manual;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.query.CFWQueryCommand;

public class CFWQueryManualPageCommand extends ManualPage {

	public CFWQueryManualPageCommand(String commandName, CFWQueryCommand command) {
		//Set title to command name
		super(commandName);
		
		String mainName = command.uniqueNameAndAliases()[0];
		
		StringBuilder builder = new StringBuilder();
		
		if(commandName.equals(mainName)) {
		
			//--------------------------------
			// Add docs under main name

			builder.append("<p>"+command.descriptionShort()+"</p>");
			
			builder.append("<h2>Syntax</h2>");
			builder.append("<pre><code>"
						+ CFW.Security.escapeHTMLEntities(command.descriptionSyntax())
						+"</code></pre>");
			
			String syntaxDetails = command.descriptionSyntaxDetailsHTML();
			if( !Strings.isNullOrEmpty(syntaxDetails) ) {
				builder.append("<div>"+syntaxDetails+"</div>");
				
			}
			
			builder.append("<h2>Usage</h2>");
			builder.append("<div>"+command.descriptionHTML()+"</div>");
			
			
		}else {
			//--------------------------------
			// Add docs under main name
			builder.append("<p>This command is an alias for "+mainName+".</p>");
			
		}
		
		this.content(builder.toString());
		
	}

}
