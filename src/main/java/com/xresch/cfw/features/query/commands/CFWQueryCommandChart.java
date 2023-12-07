package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandChart extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "chart";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandChart.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandChart(CFWQuery parent) {
		super(parent);
		this.isManipulativeCommand(false);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {COMMAND_NAME};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Creates a chart from the results.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" [by=<fieldnames>] [type=<typeOptions>] [<otherParamName>=<value> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>type:&nbsp;</b>One of the following(Default:area):</p>"
					+"<ul>"
						+"<li>area</li>"
						+"<li>line</li>"
						+"<li>bar</li>"
						+"<li>scatter</li>"
						+"<li>steppedarea</li>"
						+"<li>steppedline</li>"
						+"<li>sparkline</li>"
						+"<li>sparkarea</li>"
						+"<li>sparkbar</li>"
						+"<li>pie</li>"
						+"<li>doughnut</li>"
						+"<li>radar</li>"
						+"<li>polar</li>"
						+"<li>gantt (x will be start time and y will be end time)</li>"
					+"</ul>"
				+"<p><b>by:&nbsp;</b>Array of fieldnames to group by. This determines the series for the chart.</p>"	
				+"<p><b>aggregation:&nbsp;</b>How the data should be aggregated for category charts(pie|doughnut|radar|polar), either:.</p>"	
					+"<ul>"
						+"<li>sum</li>"
						+"<li>avg</li>"
						+"<li>count</li>"
					+"</ul>"
				+"<p><b>x:&nbsp;</b>Name of the field containing the values for the x-axis.</p>"	
				+"<p><b>y:&nbsp;</b>Name of the field containing the values for the y-axis.</p>"	
				+"<p><b>xtype:&nbsp;</b>Type of the x-axis:</p>"	
					+"<ul>"
						+"<li>time</li>"
						+"<li>linear</li>"
						+"<li>logarithmic</li>"
					+"</ul>"
				+"<p><b>ytype:&nbsp;</b>Type of the y-axis:</p>"	
					+"<ul>"
						+"<li>linear</li>"
						+"<li>logarithmic</li>"
					+"</ul>"
				+"<p><b>stacked:&nbsp;</b>Toogle if the series should be stacked. (Default:false)</p>"	
				+"<p><b>showlegend:&nbsp;</b>Toogle if the legend should be displayed. (Default:false)</p>"	
				+"<p><b>showaxes:&nbsp;</b>Toogle if the axes should be displayed. (Default:true)</p>"	
				+"<p><b>pointradius:&nbsp;</b>Radius of the points drawn in charts. (Default: 1)</p>"	
				+"<p><b>ymin:&nbsp;</b>Suggest a minimum value for the y-axis.</p>"	
				+"<p><b>ymax:&nbsp;</b>Suggest a maximum value for the y-axis.</p>"	
				+"<p><b>spangaps:&nbsp;</b>Set to true to connect lines if there is a gap in the data.</p>"	
				
				+"<p><b>details:&nbsp;</b>If true show display the details of the chart data. (Default: false)</p>"	
				+"<p><b>detailsrenderer:&nbsp;</b>The renderer used for the data. (Default: table, see command 'display' parameter 'as' for list of values)</p>"	
				+"<p><b>detailssize:&nbsp;</b>Size as number in percent of taken up area by the details.(Default: 50)</p>"	
				+"<p><b>detailsposition:&nbsp;</b>Position of the details in relation to the chart, either one of: bottom | right | left (Default: bottom).</p>"	
				+"<p><b>multichart:&nbsp;</b>Set to true to display each series in it's own chart.</p>"	
				+"<p><b>multicharttitle:&nbsp;</b>Set to true to display title for charts.</p>"	
				+"<p><b>multichartcolumns:&nbsp;</b> Number of columns for multi chart display.</p>"	
				+"<p><b>minheight:&nbsp;</b> The minimum height of the chart(e.g. 200px, 20vh etc..).</p>"	
				;
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
		
		//------------------------------------------
		// Get Parameters
	
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				assignmentParts.add((QueryPartAssignment)currentPart);
			}else {
				parser.throwParseException(COMMAND_NAME+": Only parameters(key=value) are allowed.", currentPart);
			}
		}
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// keep default
	}

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		
		//--------------------------------------
		// Do this here to make the command removable for command 'mimic'
		JsonObject displaySettings = this.getParent().getContext().getDisplaySettings();
		displaySettings.addProperty("as", "chart");
		
		for(QueryPartAssignment assignment : assignmentParts) {
				
			String propertyName = assignment.getLeftSideAsString(null);

			QueryPartValue valuePart = assignment.getRightSide().determineValue(null);
			
			if(valuePart.isString()) {
				String value = valuePart.getAsString();
				value = CFW.Security.sanitizeHTML(value);
				displaySettings.addProperty(propertyName, value);
			}else {
				valuePart.addToJsonObject(propertyName, displaySettings);
			}
		}
		
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
