package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.validation.NumberRangeValidator;

public class CFWJobsAlertObject extends CFWObject {

	private long lastAlertMillis = -1;
	private AlertType lastAlertType = AlertType.NONE;
	
	private int MAX_OCCURENCE_CHECK = 100;
	private ArrayList<ConditionResult> conditionResults = new ArrayList<>();
	
	private CFWJob associatedJob;

	public enum AlertObjectFields{
		CFW_ALERTCHECKER_OCCURENCES, 
		CFW_ALERTCHECKER_ALERTDELAY,
		JSON_CFW_ALERTCHECKER_USERS_TO_ALERT,
		JSON_CFW_ALERTCHECKER_GROUPS_TO_ALERT,
		JSON_CFW_ALERTCHECKER_ALERT_CHANNEL,
	}
	
	public enum AlertType{
		NONE,
		RAISE,
		LIFT
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
	

	private CFWField<LinkedHashMap<String, Boolean>> alertChannels = 
				CFWField.newCheckboxes(AlertObjectFields.JSON_CFW_ALERTCHECKER_ALERT_CHANNEL)
						.setDescription("Check the channels the alert should be sent through.")
						.setOptions(CFWJobsAlerting.getChannelNamesForUI())
						.setValue(null);
	
	public CFWJobsAlertObject(CFWJob associatedJob) {
		
		this.associatedJob = associatedJob;
		
		this.addFields(occurences, delayMinutes, usersToAlert, groupsToAlert, alertChannels);

	}
	
	
	/**************************************************************************
	 * Add the next condition result and checks if an Alert should be sent or
	 * not.
	 * @return true if alert triggered, false otherwise
	 **************************************************************************/
	public AlertType checkSendAlert(boolean conditionMatched) {
		
		//-------------------------------------
		// Keep Limit
		if(conditionResults.size() > MAX_OCCURENCE_CHECK+1) {
			// remove first half of all entries
			for(int i = 0; i < MAX_OCCURENCE_CHECK/2; i++) {
				conditionResults.remove(0);
			}
		}
		
		//---------------------------------
		// Check Condition
		conditionResults.add(new ConditionResult(conditionMatched));
		
		long currentTimeMillis = System.currentTimeMillis();
		long alertDelayMillis = delayMinutes.getValue() * 1000;
		
		if(lastAlertType.equals(AlertType.NONE)
		|| lastAlertType.equals(AlertType.LIFT)) {
			
			//-----------------------------------------
			// Skip if delay not reached
			if(lastAlertMillis != -1 && (lastAlertMillis + alertDelayMillis) > currentTimeMillis  ) {
				return AlertType.NONE;
			}

			//-----------------------------------------
			// Do alert if all in series are true
			int occurencesInSeries = occurences.getValue();
			
			if(conditionResults.size() >= occurencesInSeries) {
				boolean doAlert = true;
				for(int i = 1; i <= occurencesInSeries; i++) {
					int indexFromLast = conditionResults.size() - i;
					doAlert &= conditionResults.get(indexFromLast).getResult();
				}
				
				if(doAlert) {
					return AlertType.RAISE;
				}else {
					return AlertType.NONE;
				}
			}
		}else {
			
			//-----------------------------------------
			// Do check Lift Alert 
			int occurencesInSeries = occurences.getValue();
			
			if(conditionResults.size() >= occurencesInSeries) {
				
				boolean doLift = true;
				for(int i = 1; i <= occurencesInSeries; i++) {
					int indexFromLast = conditionResults.size() - i;
					doLift &= !conditionResults.get(indexFromLast).getResult();
				}
				
				if(doLift) {
					return AlertType.LIFT;
				}else {
					return AlertType.NONE;
				}
			}
		}
				
		return AlertType.NONE;
		
	}
	
	
	
	/**************************************************************************
	 * Send an alert, either for raising or lifting an alert.
	 * You have to create the content for raising or lifting the content yourself.
	 * 
	 * @param subject the title for your alert
	 * @param content plain text of your alert(mandatory)
	 * @param contentHTML html version of your alert(optional, some alert 
	 * channels might use the HTML version if not null)
	 **************************************************************************/
	public void doSendAlert(String subject, String content, String contentHTML) {
		
		HashMap<Integer, User> uniqueUsers = this.doSendAlert_getMergedListOfUsersToAlert();
		ArrayList<CFWJobsAlertingChannel> channelsToAlert = this.doSendAlert_getListOfAlertChannels();
		
		for(CFWJobsAlertingChannel channel : channelsToAlert) {
			channel.sendAlerts(this, uniqueUsers, subject, content, contentHTML);
		}
		
	}

	
	/**************************************************************************
	 * Returns a HashMap containing a list of unique users that are the mighty
	 * chosen ones who will get the important alerts.
	 **************************************************************************/
	private ArrayList<CFWJobsAlertingChannel> doSendAlert_getListOfAlertChannels(){
		ArrayList<CFWJobsAlertingChannel> channelsToAlert = new ArrayList<>();
		
		LinkedHashMap<String, Boolean> channelSelection = alertChannels.getValue();
		
		if(channelSelection != null && !channelSelection.isEmpty()) {
			for(Entry<String, Boolean> entry : channelSelection.entrySet()) {
				if(entry.getValue() == true) {
					String channelUniqueName = entry.getKey();
					channelsToAlert.add(CFWJobsAlerting.createChannelInstance(channelUniqueName));
				}
			}
		}
		
		return channelsToAlert;
	}
	
	/**************************************************************************
	 * Returns a HashMap containing a list of unique users that are the mighty
	 * chosen ones who will get the important alerts.
	 **************************************************************************/
	private HashMap<Integer, User> doSendAlert_getMergedListOfUsersToAlert(){
		
		HashMap<Integer, User> uniqueUsers = new HashMap<>();
		
		//-------------------------------------------
		// Fetch users to Alert
		
		if(usersToAlert.getValue() != null) {
			uniqueUsers = CFW.DB.Users.convertToUserList(usersToAlert.getValue());
		}
		
		//-------------------------------------------
		// Fetch users to Alert from groups
		if(groupsToAlert.getValue() != null) {
			for(String groupID : groupsToAlert.getValue().keySet()) {
				ArrayList<User> usersFromGroup = CFW.DB.Roles.getUsersForRole(groupID);
				if(usersFromGroup != null) {
					for(User groupMember : usersFromGroup) {
						uniqueUsers.put(groupMember.id(), groupMember);
					}
				}
			}
		}
		
		return uniqueUsers;
	}
	
	
	//========================================================================================
	// GETTERS AND SETTERS
	//========================================================================================

	public CFWField<Integer> getOccurences() {
		return occurences;
	}


	public CFWJobsAlertObject setOccurences(CFWField<Integer> occurences) {
		this.occurences = occurences;
		return this;
	}


	public CFWField<Integer> getDelayMinutes() {
		return delayMinutes;
	}


	public CFWJobsAlertObject setDelayMinutes(CFWField<Integer> delayMinutes) {
		this.delayMinutes = delayMinutes;
		return this;
	}


	public CFWField<LinkedHashMap<String, String>> getUsersToAlert() {
		return usersToAlert;
	}


	public CFWJobsAlertObject setUsersToAlert(CFWField<LinkedHashMap<String, String>> usersToAlert) {
		this.usersToAlert = usersToAlert;
		return this;
	}


	public CFWField<LinkedHashMap<String, String>> getGroupsToAlert() {
		return groupsToAlert;
	}


	public CFWJobsAlertObject setGroupsToAlert(CFWField<LinkedHashMap<String, String>> groupsToAlert) {
		this.groupsToAlert = groupsToAlert;
		return this;
	}


	public CFWField<LinkedHashMap<String, Boolean>> getAlertChannels() {
		return alertChannels;
	}


	public CFWJobsAlertObject setAlertChannels(CFWField<LinkedHashMap<String, Boolean>> alertChannels) {
		this.alertChannels = alertChannels;
		return this;
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
		
		public boolean getResult() {
			return result;
		}
		
		public ConditionResult setResult(boolean result) {
			this.result = result;
			return this;
		}

	}
	

}
