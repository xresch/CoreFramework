package com.xresch.cfw.features.query;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.manual.ManualPage;

public class CFWQueryManualPageCommand extends ManualPage {

	public CFWQueryManualPageCommand(String commandName, CFWQueryCommand command) {
		this(null, commandName, command);
	}
	
	public CFWQueryManualPageCommand(ManualPage parent, String commandName, CFWQueryCommand command) {
		//Set title to command name
		super(commandName);
		
		if(parent != null) {
			parent.addChild(this);
		}
		
		String mainName = command.uniqueNameAndAliases()[0];
		
		StringBuilder builder = new StringBuilder();
		
		if(commandName.equals(mainName)) {
		
			//--------------------------------
			// Add docs under main name

			builder.append("<p>"+command.descriptionShort()+"</p>");
			
			builder.append("<h2 class=\"toc-hidden\">Syntax</h2>");
			builder.append("<pre><code class=\"language-cfwquery\">"
						+ CFW.Security.escapeHTMLEntities(command.descriptionSyntax())
						+"</code></pre>");
			
			String syntaxDetails = command.descriptionSyntaxDetailsHTML();
			if( !Strings.isNullOrEmpty(syntaxDetails) ) {
				builder.append("<div>"+syntaxDetails+"</div>");
				
			}
			
			builder.append("<h2 class=\"toc-hidden\">Usage</h2>");
			builder.append("<div>"+command.descriptionHTML()+"</div>");
			
			
		}else {
			//--------------------------------
			// Add docs under main name
			if(this.getParent() != null) {
				builder.append("<p>This is an alias for the command "
							+"<a href=\"#\" onclick=\"cfw_manual_loadPage('"+parent.resolvePath(null)+"|"+mainName+"');\">"
								+mainName+
						"</a>.</p>");
			}else {
				builder.append("<p>This is an alias for the command "
							+mainName
					+"</p>");
			}
			
		}
		
		this.content(builder.toString());
		
	}

}
