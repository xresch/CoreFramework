package com.xresch.cfw.tests.assets.mockups;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.NumberRangeValidator;

public class CFWObjectMockup extends CFWObject{
	
	//------------------------------------------------------------------------------------------------
	// A regular text field. The fieldname "FIRSTNAME" will be used to create the label "Firstname"
	private CFWField<String> firstname = 
				CFWField.newString(FormFieldType.TEXT, "FIRSTNAME");
	
	//------------------------------------------------------------------------------------------------
	// A text field with custom label and a length validator and a description
	private CFWField<String> lastname = 
				CFWField.newString(FormFieldType.TEXT, "LASTNAME")
						.setLabel("Lastname with custom Label")
						.setDescription("Provide a lastname with 2 to 24 characters.")
						.addValidator(new LengthValidator(2, 24));
	
	//------------------------------------------------------------------------------------------------
	// A text field with default value
	private CFWField<String> withDefaultValue = 
				CFWField.newString(FormFieldType.TEXT, "WITH_DEFAULT_VALUE")
						.setDescription("This field has a default value.")
						.setValue("Default Value");
	
	//------------------------------------------------------------------------------------------------
	// A regular textarea
	private CFWField<String> description = 
				CFWField.newString(FormFieldType.TEXTAREA, "A_LONG_DESCRIPTION")
						.setDescription("Put a description in this field.")
						.addValidator(new LengthValidator(5, 10));
	
	//------------------------------------------------------------------------------------------------
	// A textarea with 10 rows instead of the default 5
	private CFWField<String> textarea =
				CFWField.newString(FormFieldType.TEXTAREA, "10ROW_TEXTAREA")
						.setLabel("10 Row Textarea")
						.setDescription("Now thats what I call a big text area.")
						.addAttribute("rows", "10");
	
	//------------------------------------------------------------------------------------------------
	// A regular number field with the custom Label "Enter a Number"
	private CFWField<Integer> number = 
				CFWField.newInteger(FormFieldType.NUMBER, "Number_Fieldxyz")
						.setLabel("Enter a Number");
	
	//------------------------------------------------------------------------------------------------
	// A number field with RangeValidator
	private CFWField<Integer> numberRange = 
				CFWField.newInteger(FormFieldType.NUMBER, "Number_Range")
						.addValidator(new NumberRangeValidator(8, 8008))
						.setValue(7);
	
	//------------------------------------------------------------------------------------------------
	// A datepicker field with an initial value
	private CFWField<Date> date = 
				CFWField.newDate(FormFieldType.DATEPICKER, "DATE")
						.setValue(new Date(1580053600000L));
	
	//------------------------------------------------------------------------------------------------
	// A date and time picker field without a default value
	private CFWField<Timestamp> timestamp = 
				CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, "TIMESTAMP");
	
	//------------------------------------------------------------------------------------------------
	// A select with options
	private CFWField<String> select = 
				CFWField.newString(FormFieldType.SELECT, "SELECT")
						.setOptions(new String[] {"Option A","Option B","Option C","Option D"});
	
	//------------------------------------------------------------------------------------------------
	// A select with key and value options
	private CFWField<Integer> keyValSelect = 
				CFWField.newInteger(FormFieldType.SELECT, "KEY_VAL_SELECT")
						.setValue(2);
	
	//------------------------------------------------------------------------------------------------
	// A WYSIWYG Editor with default value
	private CFWField<String> editor = 
			CFWField.newString(FormFieldType.WYSIWYG, "EDITOR")
					.setValue("<strong>Intial Value:</strong> successful!!!");
	
	//------------------------------------------------------------------------------------------------
	// A tags field which will create a comma separated string
	private CFWField<String> tags = 
			CFWField.newString(FormFieldType.TAGS, "TAGS")
					.setValue("foo,test,bar,bla")
					.addAttribute("maxTags", "20")
					.setDescription("Type at least 3 characters to get suggestions.")
					.setAutocompleteHandler(new CFWAutocompleteHandler(5) {
						
						public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue) {
							AutocompleteList list = new AutocompleteList();
							
							for(int i = 0; i < this.getMaxResults(); i++ ) {
								String tag = "Tag_"+inputValue+"_"+i;
								list.addItem(tag);
							}
							return new AutocompleteResult(list);
						}
					});
	
	//------------------------------------------------------------------------------------------------
	// A tags selector which will be used to select keys with an associated label.
	// new CFWAutocompleteHandler(10,1) will cause the autocomplete to display max 10 results and start autocomplete from 1 character
	private CFWField<LinkedHashMap<String,String>> tagsselector = 
			CFWField.newTagsSelector("JSON_TAGS_SELECTOR")
					.setDescription("Start typing to get suggestions.")
					.setAutocompleteHandler(new CFWAutocompleteHandler(10,1) {
						
						public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue) {
							AutocompleteList list = new AutocompleteList();
							for(int i = 0; i < this.getMaxResults(); i++ ) {
								String tag = inputValue+"_"+i;
								list.addItem("key_"+tag, "Label_"+tag);
							}
							return new AutocompleteResult(list);
						}
					});
	
	//------------------------------------------------------------------------------------------------
	// A text field with autocomplete
	// new CFWAutocompleteHandler(10,1) will cause the autocomplete to display max 10 results and start autocomplete from 1 character
	private CFWField<String> autocomplete = CFWField.newString(FormFieldType.TEXT, "AUTOCOMPLETE")
			.setAutocompleteHandler(new CFWAutocompleteHandler(10,1) {
				
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue) {
					AutocompleteList list = new AutocompleteList();
					for(int i = 0; i < 7; i++ ) {
						String tag = "Test_"+inputValue+"_"+i;
						list.addItem(tag);
					}
					
					AutocompleteList list2 = new AutocompleteList();
					for(int i = 0; i < 10; i++ ) {
						String tag = "Foobar_"+inputValue+"_"+i;
						list2.addItem(tag, tag, "Some example description that should be added.");
					}
					
					return new AutocompleteResult(list)
							.addList(list2)
							.setHTMLDescription("<p>This is your HTML Description. Feel free to add some stuff like a list:<p <ol><li>Do this</li><li>Do that</li><li>Do even more...</li></ol>");
				}
			});
	
	//------------------------------------------------------------------------------------------------
	// A text field showing the two additional autocomplete method "replace" and "append"
	// new CFWAutocompleteHandler(5,1) will cause the autocomplete to display max 5 results and start autocomplete from 1 character
	private CFWField<String> autocompleteMethods = CFWField.newString(FormFieldType.TEXT, "AUTOCOMPLETE_METHODS")
		.setAutocompleteHandler(new CFWAutocompleteHandler(5,1) {
			
			public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue) {
				
				if (Strings.isNullOrEmpty(inputValue)) return null;
				
				//-----------------------------------------------
				// Create Replace Examples
				AutocompleteList list = new AutocompleteList();
				
				for(int i = 0; i < 5; i++ ) {
					String[] splitted = inputValue.split(" ");
					String lastWord = splitted[splitted.length-1];
					
					list.addItem(
						new AutocompleteItem(
							lastWord.toUpperCase()+i, 
							"Replace with "+lastWord.toUpperCase()+i, 
							"Replace last word with uppercase.")
								.setMethodReplace(lastWord)
					);
				}
				
				//-----------------------------------------------
				// Create Append Examples
				AutocompleteList list2 = new AutocompleteList();
				for(int i = 0; i < 5; i++ ) {
					String[] splitted = inputValue.split(" ");
					String lastWord = splitted[splitted.length-1];
					
					list2.addItem(
							new AutocompleteItem(
								lastWord.toUpperCase()+i, 
								"Append "+lastWord.toUpperCase()+i, 
								"Append last word as uppercase.")
									.setMethodAppend()
						);
				}
				
				return new AutocompleteResult(list)
						.addList(list2)
						.setHTMLDescription("<p>Example of autocomplete methods replace and append.</ol>");
			}
		});
		
	
	//======================================================================
	// The default constructor
	public CFWObjectMockup() {
		
		initialize();
	}
		
	//======================================================================
	// Initialize Fields and add the fields to the object
	public void initialize() {
		withDefaultValue.setValueValidated("This is the Value");
		LinkedHashMap<Integer, String> options = new LinkedHashMap<Integer, String>();
		options.put(1, "Apple");
		options.put(2, "Banana");
		options.put(3, "Plumb");
		options.put(4, "Strawwberry");
		keyValSelect.setOptions(options);
		
		this.addFields(
				firstname
				, lastname
				, withDefaultValue
				, description
				, textarea
				, number
				, numberRange
				, date
				, timestamp
				, select
				, keyValSelect
				, editor
				, tags
				, tagsselector
				, autocomplete
				, autocompleteMethods);
	}

}
