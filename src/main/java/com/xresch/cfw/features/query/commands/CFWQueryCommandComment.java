package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandComment extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "comment";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandComment.class.getName());
	
	CFWQuerySource source = null;
	ArrayList<String> fieldnames = new ArrayList<>();
		
	int recordCounter = 0;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandComment(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {COMMAND_NAME, "off"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Used to add a comment to the script or turn off another command.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <your comment>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>your comment:&nbsp;</b>Whatever you have to say or a command you want to turn off.</p>";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_"+COMMAND_NAME+".html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		// every part is ignored
			
	}
	
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// keep default

	}

	
	/****************************************************************************
	 * Override to make the inQueue the outQueue
	 ****************************************************************************/
	@Override
	public PipelineAction<EnhancedJsonObject, EnhancedJsonObject> setOutQueue(LinkedBlockingQueue<EnhancedJsonObject> out) {

		this.inQueue = out;
		
		if(previousAction != null) {
			previousAction.setOutQueue(out);
		}
		
		return this;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		// Do nothing, inQueue is the same as outQueue
		this.setDoneIfPreviousDone();
	
	}

}
