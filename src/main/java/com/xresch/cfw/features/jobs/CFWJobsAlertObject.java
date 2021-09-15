package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.validation.NumberRangeValidator;

public class CFWJobsAlertObject extends CFWObject {

	private Date lastAlert = null;
	
	private int MAX_OCCURENCE_CHECK = 100;
	private ArrayList<ConditionResult> conditionResults = new ArrayList<>();
	

	public enum AlertObjectFields{
		CFW_ALERTCHECKER_OCCURENCES, 
		CFW_ALERTCHECKER_ALERTDELAY,
		JSON_CFW_ALERTCHECKER_USERS_TO_ALERT,
		JSON_CFW_ALERTCHECKER_GROUPS_TO_ALERT,
		JSON_CFW_ALERTCHECKER_ALERT_CHANNEL,
	}
	
	private CFWField<Integer> occurences = 
			CFWField.newInteger(FormFieldType.NUMBER, AlertObjectFields.CFW_ALERTCHECKER_OCCURENCES)
			.setLabel("Occurences")
			.setDescription("Number of occurences(matched condition) in series needed to trigger an alert.")
			.setValue(2)
			.addValidator(new NumberRangeValidator(1, MAX_OCCURENCE_CHECK));
	
	private CFWField<Integer> delayMinutes = 
			CFWField.newInteger(FormFieldType.NUMBER, AlertObjectFields.CFW_ALERTCHECKER_ALERTDELAY)
			.setLabel("Alert Delay Minutes")
			.setDescription("The delay in minutes before another alert is triggered, in case the condition matches again.")
			.setValue(60)
			.addValidator(new NumberRangeValidator(1, 60*24*7));
	
	private CFWField<LinkedHashMap<String,String>> usersToAlert = CFWField.newTagsSelector(AlertObjectFields.JSON_CFW_ALERTCHECKER_USERS_TO_ALERT)
			.setLabel("Alert Users")
			.setDescription("Select the users that should be alerted.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
				}
			});
	
	private CFWField<LinkedHashMap<String,String>> groupsToAlert = CFWField.newTagsSelector(AlertObjectFields.JSON_CFW_ALERTCHECKER_GROUPS_TO_ALERT)
			.setLabel("Alert Groups")
			.setDescription("Select the groups that should be alerted.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());					
				}
			});		
	

	private CFWField<LinkedHashMap<String, Boolean>> alertChannel = 
				CFWField.newCheckboxes(AlertObjectFields.JSON_CFW_ALERTCHECKER_ALERT_CHANNEL)
						.setDescription("Check the channels the alert should be sent through.")
						.setOptions(CFWJobsAlerting.getChannelNamesForUI())
						.setValue(null);
	
	public CFWJobsAlertObject() {

		this.addFields(occurences, delayMinutes, usersToAlert, groupsToAlert);

	}
	
	
	/**************************************************************************
	 * 
	 * @return true if alert triggered, false otherwise
	 **************************************************************************/
	public boolean addConditionResult(boolean conditionMatched) {
		
		if(conditionResults.size() > MAX_OCCURENCE_CHECK+1) {
			// remove first 3 entries
			conditionResults.remove(0);
			conditionResults.remove(0);
			conditionResults.remove(0);
		}
		
		conditionResults.add(new ConditionResult(conditionMatched));
		
		return checkConditionTriggerAlert();
		
	}
	
	/**************************************************************************
	 * 
	 * @return true if alert triggered, false otherwise
	 **************************************************************************/
	public boolean checkConditionTriggerAlert() {
		
		//settings
		
		return true;
	}
	
	
	/**************************************************************************
	 * Inner class to store results in an array
	 * 
	 **************************************************************************/
	public class ConditionResult {
		
		private long timeMillis;
		private boolean result;
		
		public ConditionResult(boolean result) {
			this.result = result;
			this.timeMillis = System.currentTimeMillis();
		}
		public long getTimeMillis() {
			return timeMillis;
		}
		
		public ConditionResult setTimeMillis(long timeMillis) {
			this.timeMillis = timeMillis;
			return this;
		}
		
		public boolean isResult() {
			return result;
		}
		
		public ConditionResult setResult(boolean result) {
			this.result = result;
			return this;
		}

	}
		

}
