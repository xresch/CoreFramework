package com.xresch.cfw.features.dashboard.widgets;

import java.util.LinkedHashMap;

import com.xresch.cfw.datahandling.CFWChartSettings;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class WidgetSettingsFactory {


	// !!! DO NOT CHANGE VALUES AS IT WILL WASTE SETTINGS CHOSEN BY THE USER
	public static final String FIELDNAME_DISPLAYAS = "renderer";
	public static final String FIELDNAME_DISABLE = "disable";
	public static final String FIELDNAME_SAMPLEDATA = "sampledata";
	public static final String FIELDNAME_SUFFIX = "suffix";
	public static final String FIELDNAME_CHARTSETTINGS = "JSON_CHART_SETTINGS";
	
	//new String[]{"Area", "Line", "Bar", "Scatter"}
	private static LinkedHashMap<String,String> linearChartTypes = new LinkedHashMap<String,String>();
	private static LinkedHashMap<String,String> basicAxisTypes = new LinkedHashMap<String,String>();
	private static LinkedHashMap<String,String> allAxisTypes = new LinkedHashMap<String,String>();
	
	static {
		linearChartTypes.put("area", "Area");
		linearChartTypes.put("line", "Line");
		linearChartTypes.put("bar", "Bar");
		linearChartTypes.put("scatter", "Scatter");
		linearChartTypes.put("steppedline", "Stepped Line");
		linearChartTypes.put("steppedarea", "Stepped Area");
		
		allAxisTypes.put("linear", "Linear");
		allAxisTypes.put("logarithmic", "Logarithmic");
		//allAxisTypes.put("category", "Category");
		allAxisTypes.put("time", "Time");
		
		//linear|logarithmic|category|time
		basicAxisTypes.put("linear", "Linear");
		basicAxisTypes.put("logarithmic", "Logarithmic");
	}
	
	private static  LinkedHashMap<String, String> displayAsOptions = new LinkedHashMap<>();
	static {
		displayAsOptions.put("table", "Table");
		displayAsOptions.put("panels", "Panels");
		displayAsOptions.put("properties", "Properties");
		displayAsOptions.put("cards", "Cards");
		displayAsOptions.put("tiles", "Tiles");
		
		displayAsOptions.put("statustiles", "Status Tiles");
		displayAsOptions.put("tileandbar", "Tile and Status Bar");
		displayAsOptions.put("statuslist", "Status List");
		displayAsOptions.put("statusbar", "Status Bar");
		displayAsOptions.put("statusbarreverse", "Status Bar Reversed");
		displayAsOptions.put("statusmap", "Status Map");
		
		displayAsOptions.put("csv", "CSV");
		displayAsOptions.put("json", "JSON");
		displayAsOptions.put("xml", "XML");
	}
		
	/************************************************************************************
	 *  
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDisplayAsSelect(String[] rendererNames, String defaultValue ){
		return 	CFWField.newString(FormFieldType.SELECT, FIELDNAME_DISPLAYAS)
					.setLabel("{!cfw_widget_displayas!}")
					.setDescription("{!cfw_widget_displayas_desc!}")
					.setOptions(rendererNames)
					.setValue(defaultValue);
		
	}
	
	/************************************************************************************
	 *  
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDisableBoolean(){
		return 	CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_DISABLE)
					.setLabel("{!cfw_widget_disable!}")
					.setDescription("{!cfw_widget_disable_desc!}")
					.setValue(false);
	}
	
	/************************************************************************************
	 * Returns the default example data boolean field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createSampleDataField(){
		return CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_SAMPLEDATA)
		.setLabel("{!cfw_widget_sampledata!}")
		.setDescription("{!cfw_widget_sampledata_desc!}")
		.setValue(false);
	}
	
	/************************************************************************************
	 * Create Display As field to Choose the Renderer.(fieldname=renderer)
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDefaultSuffixField(){
		return CFWField.newString(FormFieldType.TEXT, FIELDNAME_SUFFIX)
			.setLabel("{!cfw_widget_suffix!}")
			.setDescription("{!cfw_widget_suffix_desc!}");
	}
	
	
	/************************************************************************************
	 * Create Display As field to Choose the Renderer.(fieldname=renderer)
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDefaultDisplayAsField(){
		
		
		return CFWField.newString(FormFieldType.SELECT, FIELDNAME_DISPLAYAS)
			.setLabel("{!cfw_widget_displayas!}")
			.setDescription("{!cfw_widget_displayas_desc!}")
			.setOptions(getDisplayAsOptions())
			.setValue("Tiles");
	}
	
	/************************************************************************************
	 * Create Display As field to Choose the Renderer.(fieldname=renderer)
	 * 
	 * @return
	 ************************************************************************************/
	public static LinkedHashMap<String, String> getDisplayAsOptions(){
		LinkedHashMap<String, String> clone = new LinkedHashMap<>();
		clone.putAll(displayAsOptions);
		return clone;
	}
	
	/************************************************************************************
	 * Create settings used for tiles: sizefactor, borderstyle, showlabels.
	 * 
	 * @return
	 ************************************************************************************/
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap<String,CFWField> createTilesSettingsFields(){
		LinkedHashMap<String,CFWField> fieldsMap = new LinkedHashMap<>();
		
		// Hack: When form gets converted, whole numbers will lose it's decimal points.
		// To have proper display and value handling, value and label for whole numbers are different.
		LinkedHashMap<String,String> sizefactorOptions = new LinkedHashMap<>();
		sizefactorOptions.put("0.1","0.1");
		sizefactorOptions.put("0.2","0.2");
		sizefactorOptions.put("0.3","0.3");
		sizefactorOptions.put("0.4","0.4");
		sizefactorOptions.put("0.5","0.5");
		sizefactorOptions.put("0.6","0.6");
		sizefactorOptions.put("0.7","0.7");
		sizefactorOptions.put("0.8","0.8");
		sizefactorOptions.put("0.9","0.9");
		sizefactorOptions.put("1"  ,"1.0");
		sizefactorOptions.put("1.2","1.2");
		sizefactorOptions.put("1.4","1.4");
		sizefactorOptions.put("1.6","1.6");
		sizefactorOptions.put("1.8","1.8");
		sizefactorOptions.put("2"  ,"2.0");
		sizefactorOptions.put("2.5","2.5");
		sizefactorOptions.put("3"  ,"3.0");
		sizefactorOptions.put("3.5","3.5");
		sizefactorOptions.put("4"  ,"4.0");
		sizefactorOptions.put("5"  ,"5.0");
		sizefactorOptions.put("7"  ,"7.0");
		sizefactorOptions.put("10" ,"10.0");


		fieldsMap.put("sizefactor", CFWField.newString(FormFieldType.SELECT, "sizefactor")
				.setLabel("{!cfw_widget_tilessizefactor!}")
				.setDescription("{!cfw_widget_tilessizefactor_desc!}")
				.setOptions(sizefactorOptions)
				.setValue("1"));
		
		fieldsMap.put("borderstyle", CFWField.newString(FormFieldType.SELECT, "borderstyle")
				.setLabel("{!cfw_widget_tilesborderstyle!}")
				.setDescription("{!cfw_widget_tilesborderstyle_desc!}")
				.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
				.setValue("None")
				);		
		
		fieldsMap.put("showlabels", CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
				.setLabel("{!cfw_widget_tilesshowlabels!}")
				.setDescription("{!cfw_widget_tilesshowlabels_desc!}")
				.setValue(true)
				);	
			
		return fieldsMap;
	}
	
	/************************************************************************************
	 * Returns default chart fields as a LinkedHashMap.
	 * @param showXAxisOptions TODO
	 * @param shoxExtendedYAxisOptions TODO
	 * 
	 * @return
	 ************************************************************************************/
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap<String,CFWField> createDefaultChartFields(boolean showXAxisOptions, boolean shoxExtendedYAxisOptions){
		
		
		LinkedHashMap<String,CFWField> fieldsMap = new LinkedHashMap<>();
		
		fieldsMap.put(FIELDNAME_CHARTSETTINGS, 
			CFWField.newChartSettings(FIELDNAME_CHARTSETTINGS)
				.setLabel("Chart Settings")
				.setValue(new CFWChartSettings())
			);
		
		return fieldsMap;
	
	}
}
