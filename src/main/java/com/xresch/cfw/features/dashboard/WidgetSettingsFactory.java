package com.xresch.cfw.features.dashboard;

import java.util.LinkedHashMap;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.validation.NumberRangeValidator;

public class WidgetSettingsFactory {

	// !!! DO NOT CHANGE VALUES AS IT WILL WASTE SETTINGS CHOSEN BY THE USER
	public static final String FIELDNAME_DISPLAYAS = "renderer";
	public static final String FIELDNAME_DISABLE = "disable";
	public static final String FIELDNAME_SAMPLEDATA = "sampledata";

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
	public static CFWField<?> createDefaultDisplayAsField(){
		
		LinkedHashMap<String, String> rendererOptions = new LinkedHashMap<>();
		rendererOptions.put("tiles", "Tiles");
		rendererOptions.put("table", "Table");
		rendererOptions.put("panels", "Panels");
		rendererOptions.put("cards", "Cards");
		rendererOptions.put("statuslist", "Status List");
		rendererOptions.put("statusbar", "Status Bar");
		rendererOptions.put("statusbarreverse", "Status Bar Reversed");
		
		rendererOptions.put("statusmap", "Status Map(1:1)");
		rendererOptions.put("statusmap_2to1", "Status Map(2:1)");
		rendererOptions.put("statusmap_4to1", "Status Map(4:1)");
		rendererOptions.put("statusmap_8to1", "Status Map(8:1)");
		rendererOptions.put("statusmap_1to2", "Status Map(1:2)");
		rendererOptions.put("statusmap_1to4", "Status Map(1:4)");
		rendererOptions.put("statusmap_1to8", "Status Map(1:8)");
		

		rendererOptions.put("csv", "CSV");
		rendererOptions.put("json", "JSON");
		
		return CFWField.newString(FormFieldType.SELECT, FIELDNAME_DISPLAYAS)
			.setLabel("{!cfw_widget_displayas!}")
			.setDescription("{!cfw_widget_displayas_desc!}")
			.setOptions(rendererOptions)
			.setValue("Tiles");
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
		
		//Needed to clone because dashboard parameters might mess up the hashmap.
		LinkedHashMap<String, String> chartOptions = new LinkedHashMap<>();
		chartOptions.putAll(linearChartTypes);
				
		fieldsMap.put("chart_type", CFWField.newString(FormFieldType.SELECT, "chart_type")
				.setLabel("{!cfw_widget_charttype!}")
				.setDescription("{!cfw_widget_charttype_desc!}")
				.setOptions(chartOptions)
				.setValue("area")
		);
		//----------------------------------
		// X Axis Type
		if(showXAxisOptions) {
			LinkedHashMap<String, String> xaxistypeOptions = new LinkedHashMap<>();
			
			xaxistypeOptions.putAll(allAxisTypes);
	
			fieldsMap.put("x_axis_type", CFWField.newString(FormFieldType.SELECT, "x_axis_type")
					.setLabel("{!cfw_widget_xaxistype!}")
					.setDescription("{!cfw_widget_xaxistype_desc!}")
					.setOptions(xaxistypeOptions)
					.setValue("time")
				);	
		}
		//----------------------------------
		// Y Axis Type
		LinkedHashMap<String, String> yaxistypeOptions = new LinkedHashMap<>();
		
		if(shoxExtendedYAxisOptions) {
			yaxistypeOptions.putAll(allAxisTypes);
		}else {
			yaxistypeOptions.putAll(basicAxisTypes);
		}
		fieldsMap.put("y_axis_type", CFWField.newString(FormFieldType.SELECT, "y_axis_type")
				.setLabel("{!cfw_widget_yaxistype!}")
				.setDescription("{!cfw_widget_yaxistype_desc!}")
				.setOptions(yaxistypeOptions)
				.setValue("linear")
				);
		
		fieldsMap.put("stacked", CFWField.newBoolean(FormFieldType.BOOLEAN, "stacked")
				.setLabel("{!cfw_widget_chartstacked!}")
				.setDescription("{!cfw_widget_chartstacked_desc!}")
				.setValue(false)
		);
		
		fieldsMap.put("show_legend", CFWField.newBoolean(FormFieldType.BOOLEAN, "show_legend")
				.setLabel("{!cfw_widget_chartshowlegend!}")
				.setDescription("{!cfw_widget_chartshowlegend_desc!}")
				.setValue(false)
		);
		
		fieldsMap.put("show_axes", CFWField.newBoolean(FormFieldType.BOOLEAN, "show_axes")
				.setLabel("{!cfw_widget_chartshowaxes!}")
				.setDescription("{!cfw_widget_chartshowaxes_desc!}")
				.setValue(true)
		);
		fieldsMap.put("pointradius", CFWField.newFloat(FormFieldType.NUMBER, "pointradius")
				.setLabel("{!cfw_widget_chartpointradius!}")
				.setDescription("{!cfw_widget_chartpointradius_desc!}")
				.setValue(1.0f)
				.addValidator(new NumberRangeValidator(0, 10))
		);
		fieldsMap.put("ymin", CFWField.newInteger(FormFieldType.NUMBER, "ymin")
				.setLabel("{!cfw_widget_chart_ymin!}")
				.setDescription("{!cfw_widget_chart_ymin_desc!}")
				.setValue(0)
		);
		
		fieldsMap.put("ymax", CFWField.newInteger(FormFieldType.NUMBER, "ymax")
				.setLabel("{!cfw_widget_chart_ymax!}")
				.setDescription("{!cfw_widget_chart_ymax_desc!}")
		);
		return fieldsMap;
	}
}
