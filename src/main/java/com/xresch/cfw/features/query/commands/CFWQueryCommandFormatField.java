package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandFormatField extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "formatfield";

	private static final String FORMATTER_NAME_EA_STERE_GGS = "EA"+"STERE"+"GGS";

	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();
	 
	public enum FieldFormatterName {
		  ALIGN
		, BOOLEAN
		, CSS
		, CASE
		, DATE
		, DECIMALS
		, DURATION
		, LINK
		, LIST
		, LOWERCASE
		, NONE
		, PERCENT
		, POSTFIX
		, PREFIX
		, SEPARATORS
		, SHOWNULLS
		, THOUSANDS
		, THRESHOLD
		, TIMESTAMP
		, UPPERCASE
		
	}
	// Key: FormatterName Value: FormatterDefinition
	private static TreeMap<String, FormatterDefinition> formatterDefinitionArray = new TreeMap<>();
	
	/***********************************************************************************************
	 * Static initialize of Formatters
	 ***********************************************************************************************/
	static {
		CFWQueryCommandFormatField instance = new CFWQueryCommandFormatField(null);
		
		//------------------------------------------------
		// Ea-ster E-ggs
		//------------------------------------------------
		formatterDefinitionArray.put(FORMATTER_NAME_EA_STERE_GGS,
			instance.new FormatterDefinition(
				FORMATTER_NAME_EA_STERE_GGS, 
				"Adds ea-ster e-ggs to the values.",
				new Object[][] {
				}
			).example(
				 "#add those ea-ster e-ggs."
				+"\r\n| source random | formatfield FIRSTNAME=ea"+"stere"+"ggs"
			)
		);
		
		//------------------------------------------------
		// Align 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.ALIGN.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.ALIGN.toString(), 
				"Choose how the text is aligned.",
				new Object[][] {
					 new Object[] {"position", "center", "The alignment of the text, either left, right or center."}
				}
			).example(
				 "#Aligns the INDEX values to the right."
				+"\r\n| source random | formatfield INDEX=[align,right]"
			)
		);
		
		//------------------------------------------------
		// Boolean 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.BOOLEAN.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.BOOLEAN.toString(), 
				"Formats the value as a badge and adds two different colors for true/false.",
				new Object[][] {
					 new Object[] {"trueColor", "cfw-green", "The background color used for values that are true."}
					,new Object[] {"falseColor", "cfw-red", "The background color used for values that are false."}
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
		
		//------------------------------------------------
		// css 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.CSS.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.CSS.toString(), 
				"Adds a custom CSS property to the formatting of the value. Adds font-weight bold by default.",
				new Object[][] {
					 new Object[] {"propertyName", "font-weight", "The name of the css property."}
					,new Object[] {"propertyValue", "bold", "The value of the css property."}
				}
			).example(
				 "#Adds three css properties to LASTNAME"
				+"\r\n| source random | formatfield"
				+ "\r\n    LASTNAME=[css,\"border\",\"3px dashed white\"]"
				+ "\r\n    LASTNAME=[css,\"width\",\"100%\"]"
				+ "\r\n    LASTNAME=[css,\"display\",\"block\"]"
			)
		);
		
		//------------------------------------------------
		// Case
		//------------------------------------------------
		
		formatterDefinitionArray.put(FieldFormatterName.CASE.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.CASE.toString(), 
				"Formats based on one or multiple cases. See manual of formatfield command for detailed instructions.",
				new Object[][] {
					 new Object[] {"conditions", "<100", "Either a single condition or an array of conditions."}
				   , new Object[] {"format", "green", "Either a color or an array of formatter definitions."}
				}
			).example(
				 "#Make values lower than 1000 green, everything else red."
				+"\r\n| source random type=numbers | formatfield THOUSANDS=['case', \"<1000\", \"green\", [\">=1000\"],  \"darkred\" ]"
			)
		);
		
		//------------------------------------------------
		// Date
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.DATE.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.DATE.toString(),
				"Formats epoch milliseconds as date.",
				new Object[][] {
					 new Object[] {"format", "yyyy-MM-DD", "The format of the date, google moment.js for details."}
				}
			).example(
				 "#Formats the LAST_LOGIN epoch milliseconds as a date."
				+"\r\n| source random | formatfield LAST_LOGIN=date"
			)
		);
		
		//------------------------------------------------
		// Decimals
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.DECIMALS.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.DECIMALS.toString(),
				"Sets the decimal precision to a fixed number",
				new Object[][] {
					 new Object[] {"precision", 2, "The number of decimal places."}
				}
			).example(
				 "#Reduces decimal precision to 2 and adds separators, order matters."
				+"\r\n| source random type=numbers | formatfield BIG_DECIMAL=['decimals', 2] BIG_DECIMAL=['separators'] "
			)
		);
		
		//------------------------------------------------
		// Duration
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.DURATION.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.DURATION.toString(),
				"Formats a duration as seconds, minutes, hours and days.",
				new Object[][] {
					 new Object[] {"durationUnit", "ms", "The unit of the duration, either of 'ns', 'us', 'ms', 's', 'm', 'h'."}
				}
			).example(
				 "#Formats the LAST_LOGIN epoch milliseconds as a date."
				+"\r\n| source random | formatfield LAST_LOGIN=duration"
			)
		);
		
		//------------------------------------------------
		// Link 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.LINK.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.LINK.toString(),
				"Used to format URLs as links, either as text or as button.",
				new Object[][] {
					 new Object[] {"linkText", "Open Link", "The text for displaying the link."}
					,new Object[] {"displayAs", "button", "Either 'link' or 'button'."}
					,new Object[] {"icon", "fa-external-link-square-alt", "A fontawesome icon to prepend to the link text."}
					 ,new Object[] {"target", "blank", "How to open the link."}
				}
			).example(
				 "#Displays the link with the text 'Open' and as a button."
				+"\r\n| source random | formatfield URL=link,\"Open\",button"
			)
		);
		
		//------------------------------------------------
		// List
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.LIST.toString(),
				instance.new FormatterDefinition(
						FieldFormatterName.LIST.toString(),
						"Used to format an object as a list.",
						new Object[][] {
							new Object[] {"type", "bullets", "Type of the bullet points either: bullets|numbers|none."}
							,new Object[] {"paddingLeft", "10px", "The indendation of the list."}
							,new Object[] {"doLabelize", false, "Define if the keys should be made into labels."}
						}
						).example(
								"#Displays the list without bullets."
										+"\r\n| source random | formatfield OBJECTS=[list,\"none\"]"
								)
				);
		
		//------------------------------------------------
		// Lowercase 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.LOWERCASE.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.LOWERCASE.toString(), 
				"Displays the value as lowercase.",
				new Object[][] {
				}
			).example(
				 "#Makes the firstname lowercase."
				+"\r\n| source random | formatfield FIRSTNAME=lowercase"
			)
		);
				
		//------------------------------------------------
		// None 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.NONE.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.NONE.toString(), 
				"Disables any formatting and displays the plain value.",
				new Object[][] {
				}
			).example(
				 "#Disable the default boolean formatter."
				+"\r\n| source random | formatfield LIKES_TIRAMISU=none"
			)
		);

		//------------------------------------------------
		// Percent
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.PERCENT.toString(),
				instance.new FormatterDefinition(
						FieldFormatterName.PERCENT.toString(), 
						"Formats values as percent and formats it green/blue/red depending on thresholds.",
						new Object[][] {
							  new Object[] {"greenThreshold", "0", "Values reaching the threshold will be marked green."}
							, new Object[] {"redThreshold", "0", "Values reaching the threashold will be marked red."}
							, new Object[] {"type", "bg", "Either 'bg' or 'text'."}
							, new Object[] {"neutralColor", "", "Values between the thresholds will be marked with the specified color."}
						}
						).example(
								"#Formats value as percent, all values above 10 / below -10 are colored."
										+"\r\n| source random | formatfield VALUE=[percent, -10, 10]"
								)
				);
		
		//------------------------------------------------
		// Postfix 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.POSTFIX.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.POSTFIX.toString(), 
				"Appends a postfix to the value.",
				new Object[][] {
					 new Object[] {"postfix", "", "The postfix that should be added to the value."}
				}
			).example(
				 "#Add Swiss Francs(CHF) to the value."
				+"\r\n| source random | formatfield VALUE=[postfix,\" CHF\"]"
			)
		);
		
		//------------------------------------------------
		// Prefix 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.PREFIX.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.PREFIX.toString(), 
				"Prepends a prefix to the value.",
				new Object[][] {
					 new Object[] {"prefix", "", "The prefix that should be added to the value."}
				}
			).example(
				 "#Add a dollar sign to the value."
				+"\r\n| source random | formatfield VALUE=[prefix,\"$ \"]"
			)
		);
		
		//------------------------------------------------
		// Separators
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.SEPARATORS.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.SEPARATORS.toString(), 
				"Adds thousand separators to numbers.",
				new Object[][] {
					   new Object[] {"separator", "'", "The separator to add to the number."}
					 , new Object[] {"eachDigit", "3", "Number of digits between the separators."}
				}
			).example(
				 "#Add separators to the fields FLOAT and THOUSANDS."
				+"\r\n| source random type=numbers"
				+ "\r\n| formatfield FLOAT=separators THOUSANDS=separators"
			)
		);
		
		
		//------------------------------------------------
		// Shownulls 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.SHOWNULLS.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.SHOWNULLS.toString(), 
				"Show or hide null values.",
				new Object[][] {
					 new Object[] {"isVisible", true, "If true, shows nulls, if false make them blank."}
				}
			).example(
				 "#Adds formatting for null values after default null handling got overridden"
				+"\r\n| source random | formatfield LIKES_TIRAMISU=boolean LIKES_TIRAMISU=['shownulls']"
				+"\r\n#Adds formatting for null values after default null handling got overridden"
				+"\r\n| source random | formatfield LIKES_TIRAMISU=['shownulls', false]"
			)
		);
			
		
		//------------------------------------------------
		// Thousands
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.THOUSANDS.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.THOUSANDS.toString(), 
				"Displays numbers in kilos, megas, gigas and terras.",
				new Object[][] {
					  new Object[] {"isBytes", false, "If true, appends a 'B' to the formatted string."}
					 , new Object[] {"decimals", "1", "Number of decimal places to display."}
					 , new Object[] {"addBlank", true, "If true, adds a blank between number and the K/M/G/T."}
				}
			).example(
				 "#Displays the values of field FLOAT as kilos, megas..."
				+"\r\n| source random type=numbers | formatfield FLOAT=thousands"
				+"\r\n#Displays the values of field THOUSANDS as bytes, kilobytes..."
				+"\r\n| source random type=numbers | formatfield THOUSANDS=thousands,true"
			)
		);
				
		//------------------------------------------------
		// Threshhold 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.THRESHOLD.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.THRESHOLD.toString(), 
				"Colors the value based on a threshold.",
				new Object[][] {
					 new Object[] {"excellent", 0, "The threshold for status excellent."}
					 ,new Object[] {"good", 20, "The threshold for status good."}
					 ,new Object[] {"warning", 40, "The threshold for status warning."}
					 ,new Object[] {"emergency", 60, "The threshold for status emergency."}
					 ,new Object[] {"danger", 80, "The threshold for status emergency."}
					 ,new Object[] {"type", "bg", "Either 'bg' or 'text'."}
				}
			).example(
				 "#Add default threshold(0,20,40,60,80) to the VALUE field."
				+"\r\n| source random | formatfield VALUE=threshold"
				+"\r\n#Custom threshold and colorize text instead of adding a background"
				+"\r\n| source random | formatfield VALUE=threshold,0,10,20,30,40,text"
				+"\r\n#Reverse threshold and use border"
				+"\r\n| source random | formatfield VALUE=threshold,80,60,40,20,0,border"
				+"\r\n#Use null to skip a color"
				+"\r\n| source random | formatfield VALUE=threshold,80,null,40,null,0"
			)
		);
		
		//------------------------------------------------
		// Timestamp
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.TIMESTAMP.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.TIMESTAMP.toString(), 
				"Formats epoch milliseconds as a timestamp.",
				new Object[][] {
					 new Object[] {"format", "yyyy-MM-DD HH:mm:ss", "The format of the timestamp, google moment.js for details."}
				}
			).example(
				 "#Formats the LAST_LOGIN epoch milliseconds as a timestamp."
				+"\r\n| source random | formatfield LAST_LOGIN=timestamp"
			)
		);
		
		//------------------------------------------------
		// Uppercase 
		//------------------------------------------------
		formatterDefinitionArray.put(FieldFormatterName.UPPERCASE.toString(),
			instance.new FormatterDefinition(
				FieldFormatterName.UPPERCASE.toString(), 
				"Displays the value as uppercase.",
				new Object[][] {
				}
			).example(
				 "#makes the lastname uppercase."
				+"\r\n| source random | formatfield LASTNAME=uppercase"
			)
		);
		
		
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFormatField(CFWQuery parent) {
		super(parent);
		this.isManipulativeCommand(false);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {COMMAND_NAME, "fieldformat"};
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
		return COMMAND_NAME+" <fieldname>=<stringOrArray> [<fieldname>=<stringOrArray> ...]";
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
		builder.append(CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_"+COMMAND_NAME+".html"));
		builder.append("<h2 class=\"toc-hidden\">Available Formatters</h3>");
		
		for(FormatterDefinition definition : formatterDefinitionArray.values()) {
			
			if(!definition.formatName.equals(FORMATTER_NAME_EA_STERE_GGS)) {
				builder.append(definition.getHTMLDocumentation());
			}
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
	public static void addFormatter(
			CFWQueryContext context,
			String fieldname, 
			QueryPartValue valuePart) throws ParseException {
		
		ArrayList<String> fieldnameArray = new ArrayList();
		fieldnameArray.add(fieldname);
		addFormatter(context, fieldnameArray, valuePart);
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static void addFormatter(CFWQueryContext context,
			ArrayList<String> fieldnames, QueryPartValue valuePart) throws ParseException {
		
		if(valuePart.isString()) {
			addFormatterByName(context, fieldnames, valuePart.getAsString().toUpperCase());
		}else if(valuePart.isJsonArray()) {
			//--------------------------------------
			// Add Formatter By Array
			JsonArray formatterArray = valuePart.getAsJsonArray();
			if(formatterArray.isEmpty()) {
				throw new ParseException(COMMAND_NAME+": The array was empty, please provide at least a name for the formatter.", -1);
			}
			
			//--------------------------------------
			// Convert Single Formatter Array to Array
			JsonArray arrayOfFormatterArrays = formatterArray;
			if(formatterArray.get(0).isJsonPrimitive()) {
				arrayOfFormatterArrays = new JsonArray();
				arrayOfFormatterArrays.add(formatterArray);
			}
			
			
			//--------------------------------------
			// iterate arrayOfFormatterArrays

			for(JsonElement currentElement : arrayOfFormatterArrays) {
				
				// Add by Name if not JsonArray
				if(!currentElement.isJsonArray()) {
					addFormatterByName(context, fieldnames, currentElement.getAsString());
					continue;
				}
				
				// Add As JsonArray
				JsonArray currentFormatterArray = currentElement.getAsJsonArray();
				String formatterName = currentFormatterArray.get(0).getAsString().toUpperCase();
				FormatterDefinition definition = formatterDefinitionArray.get(formatterName);
				if(definition != null) {
					for(String fieldname : fieldnames) {
						definition.manifestTheMightyFormatterArray(context.getFieldFormats(), fieldname, currentFormatterArray);
					}
				}else {
					throw new ParseException(COMMAND_NAME+": Unknown formatter '"+currentFormatterArray.get(0).getAsString()+"'.", -1);
				}
			}
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static void addFormatterByName(CFWQueryContext context, ArrayList<String> fieldnames, String formatterName) throws ParseException {
		//--------------------------------------
		// Add Formatter By Name
		formatterName = formatterName.trim();
		
		FormatterDefinition definition = formatterDefinitionArray.get(formatterName.toUpperCase());
		if(definition != null) {
			for(String fieldname : fieldnames) {
				definition.manifestTheMightyFormatterArray(context.getFieldFormats(), fieldname);
			}
		}else {
			throw new ParseException(COMMAND_NAME+": Unknown formatter '"+formatterName+"'.", -1);
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		AutocompleteList list = new AutocompleteList();
		result.addList(list);
		int i = 0;
		for (String currentName : formatterDefinitionArray.keySet() ) {

			if(currentName.equals(FORMATTER_NAME_EA_STERE_GGS)) { continue; };
			
			FormatterDefinition formatter = formatterDefinitionArray.get(currentName);
			
			list.addItem(
				helper.createAutocompleteItem(
					""
				  , formatter.getAutocompleteDefaultValues()
				  , currentName.toLowerCase()
				  , formatter.description+"<br><n>Syntax:&nbsp;</b>"+formatter.getSyntax()
				)
			);
			
			i++;
			
			if((i % 5) == 0) {
				list = new AutocompleteList();
				result.addList(list);
			}
			if(i == 25) { break; }
		}
		
		

	}

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		//do nothing
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//--------------------------------------
		// Do this here to allow the functions
		// like fields() being used in the command 
		if(!this.isPreviousDone()) {
			return;
		}
		
		//--------------------------------------
		// Do this here to allow the functions
		// like fields() being used in the command 
		for(QueryPartAssignment assignment : assignmentParts) {
			
			ArrayList<String> fieldnames = new ArrayList<>();
			QueryPart leftside = assignment.getLeftSide();
			QueryPartValue leftValue = leftside.determineValue(null);
			
			fieldnames.addAll(leftValue.getAsStringArray());

			QueryPartValue valuePart = assignment.getRightSide().determineValue(null);
			addFormatter(this.parent.getContext(), fieldnames, valuePart);
			
		}

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
		
		/****************************************
		 * 
		 ****************************************/
		public FormatterDefinition(String formatName, String description, Object[][] formatterParameters) {
			this.formatName = formatName.toLowerCase();
			this.description = description;
			this.formatterParameters = formatterParameters;
		}
		
		/****************************************
		 * 
		 ****************************************/
		public FormatterDefinition example(String example) {
			this.example = example;
			return this;
		}
		
		/****************************************
		 * 
		 ****************************************/
		public String getSyntax() {
			StringBuilder builder = new StringBuilder();
			builder.append("['"+formatName+"'");
			for(Object[] paramDefinition : formatterParameters) {
				builder.append(", "+paramDefinition[0]);
			}
			builder.append("]");
			
			return builder.toString();		
		}
		
		/****************************************
		 * 
		 ****************************************/
		public String getAutocompleteDefaultValues() {
			StringBuilder builder = new StringBuilder();
			builder.append("['"+formatName+"'");
			for(Object[] paramDefinition : formatterParameters) {
				Object defaultValue = paramDefinition[1];
				if(defaultValue instanceof Boolean || defaultValue instanceof Number ) {
					builder.append(", "+paramDefinition[1]);
				}else {
					builder.append(", \""+paramDefinition[1]+"\"");
				}
			}
			builder.append("]");
			
			return builder.toString();		
		}
		
		/****************************************
		 * 
		 ****************************************/
		public String getHTMLDocumentation() {
			
			StringBuilder builder = new StringBuilder();
			
			builder.append("<h3 class=\"toc-hidden\">"+formatName+"</h3>");
			
			//---------------------------
			// Add Syntax
			builder.append("<p><b>Syntax:&nbsp;</b>"+getSyntax() );
			
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
		
		/****************************************
		 * 
		 ****************************************/
		public void manifestTheMightyFormatterArray(JsonObject fieldFormats, String fieldname) throws ParseException {
			
			manifestTheMightyFormatterArray(fieldFormats, fieldname, null);
		}
		
		/****************************************
		 * 
		 ****************************************/
		public void manifestTheMightyFormatterArray(JsonObject fieldFormats, String fieldname, JsonArray formatterArray) throws ParseException {
			
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
			if(formatterArray == null) {  	formatterArray = new JsonArray(); }
			
			if(formatterArray.isEmpty()) {	formatterArray.add(this.formatName); }
			
			JsonElement firstElement = formatterArray.get(0);
			
			if(!firstElement.isJsonPrimitive()
			|| !firstElement.getAsJsonPrimitive().isString()
			|| !firstElement.getAsString().toLowerCase().equals(this.formatName)
			) {
				throw new ParseException("Unknown value for formatter: "+firstElement.getAsString(), -1);
			}
			//-----------------------------------------
			//Add default values for missing parameters
			for(int i = formatterArray.size(); i <= formatterParameters.length;i++) {
				Object defaultValue = formatterParameters[i-1][1];
				
				if(defaultValue == null) {
					formatterArray.add(JsonNull.INSTANCE);
				}else if(defaultValue instanceof String) {
					formatterArray.add((String)defaultValue);
				}else if(defaultValue instanceof Number) {
					formatterArray.add((Number)defaultValue);
				}else if(defaultValue instanceof Boolean) {
					formatterArray.add((Boolean)defaultValue);
				}else {
					throw new ParseException("Dear Developer, the type is not supported for formatter parameters default value.", -1);
				}
			}
			
			arrayOfFormatterDefinitions.add(formatterArray);

		}
		
	}
	

}
