package com.xresch.cfw.utils.json;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.EnumSet;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWChartSettings;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.parameter.CFWParameter;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.xrutils.json.SerializerBigDecimal;
import com.xresch.xrutils.json.TypeAdapterBigDecimal;
import com.xresch.xrutils.utils.XRJson;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWJson extends XRJson {
	
	public static final Logger logger = CFWLog.getLogger(CFWJson.class.getName());
	
	private static Gson gsonInstanceEncrypted;
	
	static{
		//Type cfwobjectListType = new TypeToken<LinkedHashMap<CFWObject>>() {}.getType();

		gsonInstance = createGsonBuilderBase()
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(false))
				.serializeNulls()
				.setStrictness(Strictness.LENIENT)
				.create();
		
		gsonInstancePretty = createGsonBuilderBase()
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(false))
				.serializeNulls()
				.setStrictness(Strictness.LENIENT)
				.setPrettyPrinting()
				.create();
		
		gsonInstanceEncrypted = createGsonBuilderBase()
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(true))
				.serializeNulls()
				.setStrictness(Strictness.LENIENT)
				.create();
		
		
		exposedOnlyInstance = createGsonBuilderBase()
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(true))
				.excludeFieldsWithoutExposeAnnotation()
				.serializeNulls()
				.setStrictness(Strictness.LENIENT)
				.create();
	}
			

	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	protected static GsonBuilder createGsonBuilderBase() {
		return new GsonBuilder()
				.registerTypeAdapter(BigDecimal.class, new TypeAdapterBigDecimal())
				//.registerTypeHierarchyAdapter(BigDecimal.class, new SerializerBigDecimal())
				.registerTypeHierarchyAdapter(CFWChartSettings.class, new SerializerCFWChartSettings())
				.registerTypeHierarchyAdapter(CFWParameter.class, new SerializerCFWParameter())
				.registerTypeHierarchyAdapter(CFWSchedule.class, new SerializerCFWSchedule())
				.registerTypeHierarchyAdapter(CFWTimeframe.class, new SerializerCFWTimeframe())
				.registerTypeHierarchyAdapter(JSONResponse.class, new SerializerJSONResponse())
				.registerTypeHierarchyAdapter(EnhancedJsonObject.class, new SerializerEnhancedJsonObject())
				.registerTypeHierarchyAdapter(ResultSet.class, new SerializerResultSet())
			;
	}

	
	/****************************************************************
	 * Return a JSON element containing values of the fields of this 
	 * object, flagged or not flagged with the specified flags.
	 * 
	 * @param enableEncryption if true, encrypt values that have 
	 *        encryption enabled, false otherwise
	 * @param flags the flags for the filter
	 * @param includeFlagged if true, only includes the fields 
	 *        with the specified flag, if false exclude flagged and
	 *        keep the non-flagged
	 ****************************************************************/
	public static String toJSON(Object object, boolean enableEncryption, EnumSet<CFWFieldFlag> flags, boolean includeFlagged) {
		return toJSONElement(object, enableEncryption, flags, includeFlagged).toString();
	}
	
	/****************************************************************
	 * Return a JSON element containing values of the fields of this 
	 * object, flagged or not flagged with the specified flags.
	 * 
	 * @param enableEncryption if true, encrypt values that have 
	 *        encryption enabled, false otherwise
	 * @param flags the flags for the filter
	 * @param includeFlagged if true, only includes the fields 
	 *        with the specified flag, if false exclude flagged and
	 *        keep the non-flagged
	 ****************************************************************/
	public static JsonElement toJSONElement(Object object, boolean enableEncryption, EnumSet<CFWFieldFlag> flags, boolean includeFlagged) {	
		Gson gsonInstanceCustom = createGsonBuilderBase()
			.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(false, flags, includeFlagged) )
			.serializeNulls()
			.create();
		
		return gsonInstanceCustom.toJsonTree(object);
	}
	
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static String toJSONEncrypted(CFWObject object) {
		return gsonInstanceEncrypted.toJson(object);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonElement toJSONElement(Object object) {
		return gsonInstance.toJsonTree(object);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonElement toJSONElementEncrypted(CFWObject object) {
		return gsonInstanceEncrypted.toJsonTree(object);
	}

	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static void addFieldAsProperty(JsonObject target, CFWField field, boolean encryptValues) {
		
		String name = field.getName();
		Object value = (!encryptValues) ? field.getValue() : field.getValueEncrypted();
		
		if(name.toUpperCase().startsWith("JSON")) {
			if(value == null) {
				value = "";
			}
			if(value instanceof String) {
				JsonElement asElement = CFW.JSON.stringToJsonElement(value.toString());
				target.add(name, asElement);
			}else {
				
				target.add(name, gsonInstance.toJsonTree(value));
			}
			
		}else {
			CFW.JSON.addObject(target, name, value);
		}
		
	}

	

}
