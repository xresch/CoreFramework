package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartGroup;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWQueryCommandFilter extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandFilter.class.getName());
	
	private QueryPartGroup evaluationGroup;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFilter(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"filter", "grep"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Filters the record based on field values.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "filter <fieldname><operator><value> [<fieldname><operator><value> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>fieldname:&nbsp;</b>The name of the field to evaluate the value against.</p>"
			  +"<p><b>value:&nbsp;</b>The value to check.</p>"
			  +"<p><b>operator:&nbsp;</b>The operator, any of:</p>"
			  +"<ul>"
			  + "	<li><b>'=':&nbsp;</b> Checks if the field contains the value.</li>"
			  + "	<li><b>'==':&nbsp;</b> Checks if the values are equal.</li>"
			  + "	<li><b>'!=':&nbsp;</b> Checks if the values are not equal.</li>"
			  + "	<li><b>'&lt;=':&nbsp;</b> Checks if the field value is smaller or equals.</li>"
			  + "	<li><b>'&gt;=':&nbsp;</b> Checks if the field value is greater or equals.</li>"
			  + "	<li><b>'&lt;':&nbsp;</b>  Checks if the field value is smaller.</li>"
			  + "	<li><b>'&gt;':&nbsp;</b>  Checks if the field value is greater.</li>"
			  + "	<li><b>AND:&nbsp;</b> Used to combine two or more conditions. Condition matches only if both sides are true.</li>"
			  + "	<li><b>OR:&nbsp;</b>  Used to combine two or more conditions. Condition matches if either side is true.</li>"
			  + "</ul>"
				;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_filter.html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		if(evaluationGroup == null) {
			evaluationGroup = new QueryPartGroup(parent.getContext());
		}
		//------------------------------------------
		// Get Parameters
		
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			// BinaryExpressions, Groups and Booleans
			if(QueryPartGroup.partEvaluatesToBoolean(currentPart)) {
				evaluationGroup.add(currentPart);
								
			}else if(currentPart instanceof QueryPartArray) {
				setAndValidateQueryParts(parser, ((QueryPartArray)currentPart).getAsParts(done));
			}else {
				parser.throwParseException("filter: Only binary expressions allowed.", currentPart);
			}
		}
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		result.setHTMLDescription(
				"<b>Hint:&nbsp;</b>Filter by fields by using binary operators(== != >= <= > <).<br>"
				+"<b>Syntax:&nbsp;</b>"+CFW.Security.escapeHTMLEntities(this.descriptionSyntax())
			);
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			
			if(evaluationGroup == null || evaluationGroup.size() == 0) {
				outQueue.add(record);
			}else {
				QueryPartValue evalResult = evaluationGroup.determineValue(record);
				if(evalResult.isBoolOrBoolString()) {
					if(evalResult.getAsBoolean()) {
						outQueue.add(record);
					}
				}else {
					System.out.println("===========================");
					System.out.println("evalResultString="+evalResult.getAsString());
					System.out.println("evalResultBool="+evalResult.getAsBoolean());
					System.out.println("Record="+CFW.JSON.toJSON(record.getWrappedObject()));
					
					this.getParent().getContext().addMessage(MessageType.WARNING, "filter: Something has not evaluated to a boolean value. You might want to check your expression.");
				}
			}
		
			
			
		}
		
		this.setDoneIfPreviousDone();
	
	}

}
