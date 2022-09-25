package com.xresch.cfw.utils;

import java.util.LinkedHashMap;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.cfw.datahandling.CFWObject;

public class CFWConditions {
	
	public enum ThresholdCondition{
		  EXCELLENT(1, 1 << 1)
		, GOOD(2, 1 << 2)
		, WARNING(3, 1 << 3)
		, EMERGENCY(4, 1 << 4)
		, DANGER(5, 1 << 5)
		/* Not evaluated because it is set to disabled */
		, DISABLED(6, 0)
		/* Not evaluated because it is lower than the lowest set threshold value */
		, NOT_EVALUATED(7, 0)
		/* If the value to evaluate is null or no threshold is specified */
		, NONE(8, 0)
		;
		
		public final int id;
		public final int severity;
		
		private ThresholdCondition(int id, int severity) {
			this.id = id;			
			this.severity = severity;			
		}
	}
	
	
	public enum ThresholdDirection{
		HIGH_TO_LOW,
		LOW_TO_HIGH,
	}
	
	public static final String FIELDNAME_THRESHOLD_DANGER = "THRESHOLD_DANGER";
	public static final String FIELDNAME_THRESHOLD_EMERGENCY = "THRESHOLD_EMERGENCY";
	public static final String FIELDNAME_THRESHOLD_WARNING = "THRESHOLD_WARNING";
	public static final String FIELDNAME_THRESHOLD_GOOD = "THRESHOLD_GOOD";
	public static final String FIELDNAME_THRESHOLD_EXCELLENT = "THRESHOLD_EXCELLENT";
	public static final String FIELDNAME_THRESHOLD_DISABLED = "THRESHOLD_DISABLED";
	
	public static final String CONDITION_EXCELLENT 		= ThresholdCondition.EXCELLENT.toString();
	public static final String CONDITION_GOOD 			= ThresholdCondition.GOOD.toString();
	public static final String CONDITION_WARNING 		= ThresholdCondition.WARNING.toString();
	public static final String CONDITION_EMERGENCY 		= ThresholdCondition.EMERGENCY.toString();
	public static final String CONDITION_DANGER 		= ThresholdCondition.DANGER.toString();
	public static final String CONDITION_DISABLED 		= ThresholdCondition.DISABLED.toString();
	
	public static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	
	/************************************************************************************
	 * Returns default threshold fields as a LinkedHashMap.
	 * 
	 * @return
	 ************************************************************************************/
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap<String,CFWField> createThresholdFields(){
		LinkedHashMap<String,CFWField> fieldsMap = new LinkedHashMap<>();
		
		fieldsMap.put(CFWConditions.FIELDNAME_THRESHOLD_EXCELLENT, CFWField.newFloat(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_EXCELLENT)
					.setLabel("{!cfw_core_thresholdexcellent!}")
					.setDescription("{!cfw_core_thresholdexcellent_desc!}")
					.setValue(null)
				);
		
		fieldsMap.put(CFWConditions.FIELDNAME_THRESHOLD_GOOD, CFWField.newFloat(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_GOOD)
					.setLabel("{!cfw_core_thresholdgood!}")
					.setDescription("{!cfw_core_thresholdgood_desc!}")
					.setValue(null)
				);	
		
		fieldsMap.put(CFWConditions.FIELDNAME_THRESHOLD_WARNING, CFWField.newFloat(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_WARNING)
					.setLabel("{!cfw_core_thresholdwarning!}")
					.setDescription("{!cfw_core_thresholdwarning_desc!}")
					.setValue(null)
				);	
		
		fieldsMap.put(CFWConditions.FIELDNAME_THRESHOLD_EMERGENCY, CFWField.newFloat(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_EMERGENCY)
					.setLabel("{!cfw_core_thresholdemergency!}")
					.setDescription("{!cfw_core_thresholdemergency_desc!}")
					.setValue(null)
				);			
		
		fieldsMap.put(CFWConditions.FIELDNAME_THRESHOLD_DANGER, CFWField.newFloat(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_DANGER)
					.setLabel("{!cfw_core_thresholddanger!}")
					.setDescription("{!cfw_core_thresholddanger_desc!}")
					.setValue(null)
				);	
		
		fieldsMap.put(FIELDNAME_THRESHOLD_DISABLED, CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_THRESHOLD_DISABLED)
				.setLabel("{!cfw_core_thresholddisable!}")
				.setDescription("{!cfw_core_thresholddisable_desc!}")
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
			.setOptions(CFW.Conditions.CONDITION_OPTIONS())
			.setValue(CFW.Conditions.CONDITION_EMERGENCY.toString());
		
	}
	
	/***************************************************************************
	 * Returns a list of condition options that can be used for selects etc...  
	 * Always creates a new instance, as it could be extended, for example with
	 * dashboard parameters.
	 * @return list of options
	 ***************************************************************************/
	public static LinkedHashMap<String, String> CONDITION_OPTIONS() {
		LinkedHashMap<String, String> options = new LinkedHashMap<>(); 
		
		options.put(CONDITION_DANGER, "{!cfw_core_condition_danger!}");
		options.put(CONDITION_EMERGENCY, "{!cfw_core_condition_emergency!}");
		options.put(CONDITION_WARNING, "{!cfw_core_condition_warning!}");
		options.put(CONDITION_GOOD, "{!cfw_core_condition_good!}");
		options.put(CONDITION_EXCELLENT, "{!cfw_core_condition_excellent!}");
		
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
	public static ThresholdCondition getConditionForValue(Number valueToEvaluate, CFWObject objectWithThresholdFields) {

		return getConditionForValue(valueToEvaluate
				, (Float)objectWithThresholdFields.getField(CFWConditions.FIELDNAME_THRESHOLD_EXCELLENT).getValue()
				, (Float)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_GOOD).getValue()
				, (Float)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_WARNING).getValue()
				, (Float)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_EMERGENCY).getValue()
				, (Float)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_DANGER).getValue()
				, (Boolean)objectWithThresholdFields.getField(FIELDNAME_THRESHOLD_DISABLED).getValue()
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
	public static ThresholdCondition getConditionForValue(Number valueToEvaluate, Number tExellent, Number tGood, Number tWarning, Number tEmergency, Number tDanger, boolean isDisabled) {

		//---------------------------
		// Initial Checks
		if(isDisabled) { return ThresholdCondition.DISABLED; }
		
		if(valueToEvaluate == null
		|| (  tExellent 	== null
		   && tGood 		== null
		   && tWarning 		== null
		   && tEmergency 	== null
		   && tDanger 		== null
		   )
		){
			return ThresholdCondition.NONE;
		}

		//---------------------------
		// Find Threshold direction
		ThresholdDirection direction = getThresholdDirection(tExellent, tGood, tWarning, tEmergency, tDanger);

		//---------------------------
		// Set Colors for Thresholds

		// default to not evaluated when lower than the lowest threshold
		ThresholdCondition result = ThresholdCondition.NOT_EVALUATED;
		
		float floatValue = valueToEvaluate.floatValue();
		if( direction.equals(ThresholdDirection.HIGH_TO_LOW) ){
			if 		(tExellent 	!= null && floatValue >= tExellent.floatValue()) 	{ result = ThresholdCondition.EXCELLENT; } 
			else if (tGood 		!= null && floatValue >= tGood.floatValue()) 		{ result = ThresholdCondition.GOOD; } 
			else if (tWarning 	!= null && floatValue >= tWarning.floatValue()) 	{ result = ThresholdCondition.WARNING; } 
			else if (tEmergency != null && floatValue >= tEmergency.floatValue()) 	{ result = ThresholdCondition.EMERGENCY; } 
			else if (tDanger 	!= null	&& floatValue >= tDanger.floatValue())  	{ result = ThresholdCondition.DANGER;  } 
			else																	{ result = ThresholdCondition.NOT_EVALUATED; } 
		}else{
			if 		(tDanger 	!= null	&& floatValue >= tDanger.floatValue())  	{ result = ThresholdCondition.DANGER; } 
			else if (tEmergency != null	&& floatValue >= tEmergency.floatValue()) 	{ result = ThresholdCondition.EMERGENCY; } 
			else if (tWarning 	!= null	&& floatValue >= tWarning.floatValue()) 	{ result = ThresholdCondition.WARNING; } 
			else if (tGood		!= null	&& floatValue >= tGood.floatValue()) 		{ result = ThresholdCondition.GOOD; } 
			else if (tExellent 	!= null	&& floatValue >= tExellent.floatValue()) 	{ result = ThresholdCondition.EXCELLENT; } 	
			else																	{ result = ThresholdCondition.NOT_EVALUATED; } 
		}
		
		return result;
	}
	
	/************************************************************************************************
	 *
	 * @param baseCondition the condition against to compare to
	 * @param actualCondition the condition that should be checked if it is equals or more dangerous
	 ************************************************************************************************/
	public static boolean compareIsEqualsOrMoreDangerous(String baseCondition, String actualCondition) {
		
		return compareIsEqualsOrMoreDangerous(ThresholdCondition.valueOf(baseCondition), ThresholdCondition.valueOf(actualCondition));
	}
	
	/************************************************************************************************
	 *
	 * @param baseCondition the condition against to compare to
	 * @param actualCondition the condition that should be checked if it is equals or more dangerous
	 ************************************************************************************************/
	public static boolean compareIsEqualsOrMoreDangerous(ThresholdCondition baseCondition, ThresholdCondition actualCondition) {
		return getConditionSeverity(baseCondition) <= getConditionSeverity(actualCondition);
	}
	
	/************************************************************************************************
	 * Returns a byte shifted number representing the condition severity. 
	 * @param condition of which to get the severity
	 ************************************************************************************************/
	public static int getConditionSeverity(String condition) {
		return getConditionSeverity( ThresholdCondition.valueOf(condition) );
	}
	
	/************************************************************************************************
	 * Returns a byte shifted number representing the condition severity. 
	 * Higher number means higher severity
	 * @param condition of which to get the severity
	 ************************************************************************************************/
	public static int getConditionSeverity(ThresholdCondition condition) {
		
		if(condition == null) {
			return -1;
		}
		return condition.severity;

	}

}
