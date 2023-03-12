package com.xresch.cfw.utils;

import java.util.LinkedHashMap;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.cfw.datahandling.CFWObject;

public class CFWState {
	
	public enum CFWStateOption{
		  GREEN(1, 1 << 1)
		, LIMEGREEN(2, 1 << 2)
		, YELLOW(3, 1 << 3)
		, ORANGE(4, 1 << 4)
		, RED(5, 1 << 5)
		/* Not evaluated because it is set to disabled */
		, DISABLED(6, 0)
		/* Not evaluated because it is lower than the lowest set threshold value */
		, NOT_EVALUATED(7, 0)
		/* If the value to evaluate is null or no threshold is specified */
		, NONE(8, 0)
		;
		
		public final int id;
		public final int severity;
		
		private CFWStateOption(int id, int severity) {
			this.id = id;			
			this.severity = severity;			
		}
	}
	
	
	public enum ThresholdDirection{
		HIGH_TO_LOW,
		LOW_TO_HIGH,
	}
	
	public static final String FIELDNAME_THRESHOLD_GREEN = "THRESHOLD_GREEN";
	public static final String FIELDNAME_THRESHOLD_LIMEGREEN = "THRESHOLD_LIMEGREEN";
	public static final String FIELDNAME_THRESHOLD_YELLOW = "THRESHOLD_YELLOW";
	public static final String FIELDNAME_THRESHOLD_ORANGE = "THRESHOLD_ORANGE";
	public static final String FIELDNAME_THRESHOLD_RED = "THRESHOLD_RED";
	public static final String FIELDNAME_THRESHOLD_DISABLE = "THRESHOLD_DISABLED";
	
	public static final String STATE_GREEN 				= CFWStateOption.GREEN.toString();
	public static final String STATE_LIMEGREEN 			= CFWStateOption.LIMEGREEN.toString();
	public static final String STATE_YELLOW 			= CFWStateOption.YELLOW.toString();
	public static final String STATE_ORANGE		 		= CFWStateOption.ORANGE.toString();
	public static final String STATE_RED 				= CFWStateOption.RED.toString();
	public static final String STATE_DISABLED 			= CFWStateOption.DISABLED.toString();
	
	public static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	
	/************************************************************************************
	 * Returns default threshold fields as a LinkedHashMap.
	 * 
	 * @return
	 ************************************************************************************/
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap<String,CFWField> createThresholdFields(){
		LinkedHashMap<String,CFWField> fieldsMap = new LinkedHashMap<>();
		
		fieldsMap.put(CFWState.FIELDNAME_THRESHOLD_GREEN, CFWField.newBigDecimal(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_GREEN)
					.setLabel("{!cfw_core_threshold_green!}")
					.setDescription("{!cfw_core_threshold_green_desc!}")
					.setValue(null)
				);
		
		fieldsMap.put(CFWState.FIELDNAME_THRESHOLD_LIMEGREEN, CFWField.newBigDecimal(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_LIMEGREEN)
					.setLabel("{!cfw_core_threshold_limegreen!}")
					.setDescription("{!cfw_core_threshold_limegreen_desc!}")
					.setValue(null)
				);	
		
		fieldsMap.put(CFWState.FIELDNAME_THRESHOLD_YELLOW, CFWField.newBigDecimal(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_YELLOW)
					.setLabel("{!cfw_core_threshold_yellow!}")
					.setDescription("{!cfw_core_threshold_yellow_desc!}")
					.setValue(null)
				);	
		
		fieldsMap.put(CFWState.FIELDNAME_THRESHOLD_ORANGE, CFWField.newBigDecimal(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_ORANGE)
					.setLabel("{!cfw_core_threshold_orange!}")
					.setDescription("{!cfw_core_threshold_orange_desc!}")
					.setValue(null)
				);			
		
		fieldsMap.put(CFWState.FIELDNAME_THRESHOLD_RED, CFWField.newBigDecimal(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_RED)
					.setLabel("{!cfw_core_threshold_red!}")
					.setDescription("{!cfw_core_threshold_red_desc!}")
					.setValue(null)
				);	
		
		fieldsMap.put(FIELDNAME_THRESHOLD_DISABLE, CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_THRESHOLD_DISABLE)
				.setLabel("{!cfw_core_threshold_disable!}")
				.setDescription("{!cfw_core_threshold_disable_desc!}")
				.setValue(false)
			);
			
		return fieldsMap;
	}
	

	
	/***************************************************************************
	 * Returns a list of condition options that can be used for selects etc...  
	 * Always creates a new instance, as it could be extended, for example with
	 * dashboard parameters.
	 * @return list of options
	 ***************************************************************************/
	public static CFWField<String> createThresholdTriggerSelectorField() {
		
		return CFWField.newString(FormFieldType.SELECT, FIELDNAME_ALERT_THRESHOLD)
			.setDescription("Select the threshhold that should trigger the alert when reached.")
			.addValidator(new NotNullOrEmptyValidator())
			.setOptions(CFW.Conditions.STATE_OPTIONS())
			.setValue(CFW.Conditions.STATE_ORANGE.toString());
		
	}
	
	/***************************************************************************
	 * Returns a list of condition options that can be used for selects etc...  
	 * Always creates a new instance, as it could be extended, for example with
	 * dashboard parameters.
	 * @return list of options
	 ***************************************************************************/
	public static LinkedHashMap<String, String> STATE_OPTIONS() {
		LinkedHashMap<String, String> options = new LinkedHashMap<>(); 
		
		options.put(STATE_RED, "{!cfw_core_state_red!}");
		options.put(STATE_ORANGE, "{!cfw_core_state_orange!}");
		options.put(STATE_YELLOW, "{!cfw_core_state_yellow!}");
		options.put(STATE_LIMEGREEN, "{!cfw_core_state_limegreen!}");
		options.put(STATE_GREEN, "{!cfw_core_state_green!}");
		
		return options;
	}
	
	
		
	
	/***************************************************************************
	 * Returns the direction of the threshhold used for evaluation. 
	 * 
	 * @return ThresholdDirection 
	 ***************************************************************************/
	public static ThresholdDirection getThresholdDirection(Number tExellent, Number tGood, Number tWarning, Number tEmergency, Number tDanger) {
		
		//---------------------------
		// Find Threshold direction
		ThresholdDirection direction = ThresholdDirection.HIGH_TO_LOW;

		Number[] thresholds = new Number[] {tExellent, tGood, tWarning, tEmergency, tDanger};
		Number firstDefined = null;

		for(int i = 0; i < thresholds.length; i++){
			Number current = thresholds[i];
			if (current != null){
				if(firstDefined == null){
					firstDefined = current;
				}else{
					if(current != null && firstDefined.floatValue() < current.floatValue() ){
						direction = ThresholdDirection.LOW_TO_HIGH;
					}
					break;
				}
			}
		}
		
		return direction;
	}
	
	/************************************************************************************************
	 * Returns a ThresholdCondition for the value based on the defined thresholds.
	 * 
	 * If all the thresholds are null/undefined or the value is NaN returns ThresholdCondition.NONE.
	 * You can define thresholds values increasing from Excellent to Danger, or from Danger to Excellent.
	 * Values below the lowest threshold value will result in ThresholdCondition.NOT_EVALUATED.
	 * 
	 * If isDisabled is set to "true", ThresholdCondition.DISABLED.
	 * 
	 * @param valueToEvaluate the value that should be evaluated
	 * @param CFWObject containing the fields created with createThreashholdFields
	 * 
	 ************************************************************************************************/
	public static CFWStateOption getConditionForValue(Number valueToEvaluate, CFWObject objectWithThresholdFields) {

		return getConditionForValue(valueToEvaluate
				, (Float)objectWithThresholdFields.getField(CFWState.FIELDNAME_THRESHOLD_GREEN).getValue()
				, (Float)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_LIMEGREEN).getValue()
				, (Float)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_YELLOW).getValue()
				, (Float)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_ORANGE).getValue()
				, (Float)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_RED).getValue()
				, (Boolean)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_DISABLE).getValue()
				);
	}
	
	/************************************************************************************************
	 * Returns a ThresholdCondition for the value based on the defined thresholds.
	 * 
	 * If all the thresholds are null/undefined or the value is NaN returns ThresholdCondition.NONE.
	 * You can define thresholds values increasing from Excellent to Danger, or from Danger to Excellent.
	 * Values below the lowest threshold value will result in ThresholdCondition.NOT_EVALUATED.
	 * 
	 * If isDisabled is set to "true", ThresholdCondition.DISABLED.
	 * 
	 * @param valueToEvaluate the value that should be evaluated
	 * @param tExellent the threshold for excellent
	 * @param tGood the threshold for good
	 * @param tWarning the threshold for warning
	 * @param tEmergency the threshold for emergency
	 * @param tDanger the threshold for danger
	 * @param isDisabled define if the thresholding is disabled
	 ************************************************************************************************/
	public static CFWStateOption getConditionForValue(Number valueToEvaluate, Number tExellent, Number tGood, Number tWarning, Number tEmergency, Number tDanger, boolean isDisabled) {

		//---------------------------
		// Initial Checks
		if(isDisabled) { return CFWStateOption.DISABLED; }
		
		if(valueToEvaluate == null
		|| (  tExellent 	== null
		   && tGood 		== null
		   && tWarning 		== null
		   && tEmergency 	== null
		   && tDanger 		== null
		   )
		){
			return CFWStateOption.NONE;
		}

		//---------------------------
		// Find Threshold direction
		ThresholdDirection direction = getThresholdDirection(tExellent, tGood, tWarning, tEmergency, tDanger);

		//---------------------------
		// Set Colors for Thresholds

		// default to not evaluated when lower than the lowest threshold
		CFWStateOption result = CFWStateOption.NOT_EVALUATED;
		
		float floatValue = valueToEvaluate.floatValue();
		if( direction.equals(ThresholdDirection.HIGH_TO_LOW) ){
			if 		(tExellent 	!= null && floatValue >= tExellent.floatValue()) 	{ result = CFWStateOption.GREEN; } 
			else if (tGood 		!= null && floatValue >= tGood.floatValue()) 		{ result = CFWStateOption.LIMEGREEN; } 
			else if (tWarning 	!= null && floatValue >= tWarning.floatValue()) 	{ result = CFWStateOption.YELLOW; } 
			else if (tEmergency != null && floatValue >= tEmergency.floatValue()) 	{ result = CFWStateOption.ORANGE; } 
			else if (tDanger 	!= null	&& floatValue >= tDanger.floatValue())  	{ result = CFWStateOption.RED;  } 
			else																	{ result = CFWStateOption.NOT_EVALUATED; } 
		}else{
			if 		(tDanger 	!= null	&& floatValue >= tDanger.floatValue())  	{ result = CFWStateOption.RED; } 
			else if (tEmergency != null	&& floatValue >= tEmergency.floatValue()) 	{ result = CFWStateOption.ORANGE; } 
			else if (tWarning 	!= null	&& floatValue >= tWarning.floatValue()) 	{ result = CFWStateOption.YELLOW; } 
			else if (tGood		!= null	&& floatValue >= tGood.floatValue()) 		{ result = CFWStateOption.LIMEGREEN; } 
			else if (tExellent 	!= null	&& floatValue >= tExellent.floatValue()) 	{ result = CFWStateOption.GREEN; } 	
			else																	{ result = CFWStateOption.NOT_EVALUATED; } 
		}
		
		return result;
	}
	
	/************************************************************************************************
	 *
	 * @param baseCondition the condition against to compare to
	 * @param actualCondition the condition that should be checked if it is equals or more dangerous
	 ************************************************************************************************/
	public static boolean compareIsEqualsOrMoreDangerous(String baseCondition, String actualCondition) {
		
		return compareIsEqualsOrMoreDangerous(CFWStateOption.valueOf(baseCondition), CFWStateOption.valueOf(actualCondition));
	}
	
	/************************************************************************************************
	 *
	 * @param baseCondition the condition against to compare to
	 * @param actualCondition the condition that should be checked if it is equals or more dangerous
	 ************************************************************************************************/
	public static boolean compareIsEqualsOrMoreDangerous(CFWStateOption baseCondition, CFWStateOption actualCondition) {
		return getConditionSeverity(baseCondition) <= getConditionSeverity(actualCondition);
	}
	
	/************************************************************************************************
	 * Returns a byte shifted number representing the condition severity. 
	 * @param condition of which to get the severity
	 ************************************************************************************************/
	public static int getConditionSeverity(String condition) {
		return getConditionSeverity( CFWStateOption.valueOf(condition) );
	}
	
	/************************************************************************************************
	 * Returns a byte shifted number representing the condition severity. 
	 * Higher number means higher severity
	 * @param condition of which to get the severity
	 ************************************************************************************************/
	public static int getConditionSeverity(CFWStateOption condition) {
		
		if(condition == null) {
			return -1;
		}
		return condition.severity;

	}

}
