package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineAction;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandFormatField extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandFormatField.class.getName());
	
	CFWQuerySource source = null;
	ArrayList<String> fieldnames = new ArrayList<>();
		
	int recordCounter = 0;
	
	// Key: FormatterName Value: FormatterDefinition
	private static TreeMap<String, FormatterDefinition> formatterArray = new TreeMap<>();
	
	/***********************************************************************************************
	 * Static initialize of Formatters
	 ***********************************************************************************************/
	static {
		CFWQueryCommandFormatField instance = new CFWQueryCommandFormatField(null);
		
		formatterArray.put("boolean",
			instance.new FormatterDefinition(
				"boolean", 
				"Formats the value as a badge and adds two different colors for true/false.",
				new Object[][] {
					 new Object[] {"trueColor", "green", "The background color used for values that are true."}
					,new Object[] {"falseColor", "red", "The background color used for values that are false."}
					,new Object[] {"trueTextColor", "white", "The text color used for values that are true."}
					 ,new Object[] {"falseTextColor", "white", "The text color used for values that are false."}
				}
			).example(
				 "#Use default colors green and red."
				+"\r\n| source random | formatfield LIKES_TIRAMISU=boolean"
				+"\r\n# Use custom CSS colors for background."
				+"\r\n| source random | formatfield LIKES_TIRAMISU=[\"boolean\", \"steelblue\", \"orange\"]"
				+"\r\n# Use custom CSS colors for background and text."
				+"\r\n| source random | formatfield LIKES_TIRAMISU=[\"boolean\", \"yellow\", \"purple\", \"black\", \"#fff\"]"
			)
			
		);
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFormatField(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"formatfield", "fieldformat"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Used to set the format of a field.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "display <fieldname>=<stringOrArray> [<fieldname>=<stringOrArray> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 	"<p><b>fieldname:&nbsp;</b>Name of the field to apply the format.</p>"
				+"<p><b>stringOrArray:&nbsp;</b>Either a name of a format or an array with the name and parameters.</p>"
				;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		StringBuilder builder = new StringBuilder();
		builder.append(CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_display.html"));
		builder.append("<h2>Available Formatters</h3>");
		
		for(FormatterDefinition formatterDefinition : formatterArray.values()) {
			builder.append(formatterDefinition.getHTMLDocumentation());
		}
		return builder.toString();
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// Get Parameters
		
		JsonObject displaySettings = this.getParent().getContext().getDisplaySettings();
		
		JsonElement fieldFormatsElement = displaySettings.get("fieldFormats");
		if(fieldFormatsElement == null || fieldFormatsElement.isJsonNull()) {
			fieldFormatsElement = new JsonObject();
			displaySettings.add("fieldFormats", fieldFormatsElement);
		}
		
		JsonObject fieldFormats = fieldFormatsElement.getAsJsonObject();
		
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				
				String fieldname = assignment.getLeftSideAsString(null);

				QueryPartValue valuePart = assignment.getRightSide().determineValue(null);
				if(valuePart.isString()) {
					//--------------------------------------
					// Add Formatter By Name
					String formatterName = valuePart.getAsString().trim().toLowerCase();
					
					FormatterDefinition definition = formatterArray.get(formatterName);
					if(definition != null) {
						
						definition.manifestTheMightyFormatterArray(fieldFormats, fieldname);
					}else {
						parser.throwParseException("formatfield: Unknown formatter '"+formatterName+"'.", currentPart);
					}
				}if(valuePart.isJsonArray()) {
					//--------------------------------------
					// Add Formatter By Array
					JsonArray array = valuePart.getAsJsonArray();
					if(array.isEmpty()) {
						parser.throwParseException("formatfield: The array was empty, please provide at least a name for the formatter.", currentPart);
					}
					
					FormatterDefinition definition = formatterArray.get(array.get(0).getAsString());
					if(definition != null) {
						definition.manifestTheMightyFormatterArray(fieldFormats, fieldname, array);
					}else {
						parser.throwParseException("formatfield: Unknown formatter '"+array.get(0).getAsString()+"'.", currentPart);
					}
					
				}
			
			}else {
				parser.throwParseException("formatfield: Only parameters(key=value) are allowed.", currentPart);
			}
		}
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		result.setHTMLDescription(
				"<b>Hint:&nbsp;</b>Specify how a field should be formatted.<br>"
				+"<b>Syntax:&nbsp;</b>"+CFW.Security.escapeHTMLEntities(this.descriptionSyntax())
			);

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
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private class FormatterDefinition {
		
		private String formatName;
		private String description;
		private String example;
		
		// Array of formatter parameters, each param definition must have 3 entries in the array
		// [
		//		[<paramName>, <defaultValue>, <description>]
		// ]
		private Object[][] formatterParameters;
		
		public FormatterDefinition(String formatName, String description, Object[][] formatterParameters) {
			this.formatName = formatName;
			this.description = description;
			this.formatterParameters = formatterParameters;
		}
		
		public FormatterDefinition example(String example) {
			this.example = example;
			return this;
		}
		
		public String getHTMLDocumentation() {
			
			StringBuilder builder = new StringBuilder();
			
			builder.append("<h3>"+formatName+"</h3>");
			
			//---------------------------
			// Add Syntax
			builder.append("<p><b>Syntax:&nbsp;</b>['"+formatName+"'");
			for(Object[] paramDefinition : formatterParameters) {
				builder.append(", "+paramDefinition[0]);
			}
			builder.append("]");
			
			//---------------------------
			// Add Description
			builder.append("<p><b>Description:&nbsp;</b>"+description+"</p>");
			
			//---------------------------
			// Add Description
			builder.append("<p><b>Parameters:&nbsp;</b></p>")
				   .append("<ul>");
			
			for(Object[] paramDefinition : formatterParameters) {
				builder.append("<li><b>"+paramDefinition[0]+":&nbsp;</b>"+paramDefinition[2]+" (Default: '"+paramDefinition[1]+"')</li>");
			}
			
			builder.append("</ul>");
			
			if(this.example != null) {
				builder.append("<p><b>Example:&nbsp;</b></p>");
				builder.append("<pre><code class=\"language-bash\">"+CFW.Security.escapeHTMLEntities(example)+"</code></pre>");
			}
			
			return builder.toString();
		}
		
		
		public void manifestTheMightyFormatterArray(JsonObject fieldFormats, String fieldname) throws ParseException {
			
			manifestTheMightyFormatterArray(fieldFormats, fieldname, null);
		}

		public void manifestTheMightyFormatterArray(JsonObject fieldFormats, String fieldname, JsonArray array) throws ParseException {
			
			//-----------------------------------------
			// Prepare Array of Arrays
			JsonElement formatterElement = fieldFormats.get(fieldname);
			
			if(formatterElement == null) {
				formatterElement = new JsonArray();
				fieldFormats.add(fieldname, formatterElement);
			}
			
			JsonArray arrayOfFormatterDefinitions = formatterElement.getAsJsonArray();
			
			//-----------------------------------------
			// Prepare Array
			if(array == null) {  	array = new JsonArray(); }
			
			if(array.isEmpty()) {	array.add(this.formatName); }
			
			JsonElement firstElement = array.get(0);
			
			if(!firstElement.isJsonPrimitive()
			|| !firstElement.getAsJsonPrimitive().isString()
			|| !firstElement.getAsString().equals(this.formatName)
			) {
				throw new ParseException("Unknown value for formatter: "+firstElement.getAsString(), -1);
			}
			//-----------------------------------------
			//Add default values for missing parameters
			for(int i = array.size(); i <= formatterParameters.length;i++) {
				Object defaultValue = formatterParameters[i-1][1];
				
				if(defaultValue instanceof String) {
					array.add((String)defaultValue);
				}else if(defaultValue instanceof Number) {
					array.add((Number)defaultValue);
				}else if(defaultValue instanceof Boolean) {
					array.add((Boolean)defaultValue);
				}else {
					throw new ParseException("Dear Developer, the type is not supported for formatter parameters default value", -1);
				}
			}
			
			arrayOfFormatterDefinitions.add(array);

		}
		
	}
	

}
