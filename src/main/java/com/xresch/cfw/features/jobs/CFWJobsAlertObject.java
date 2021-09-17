package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.validation.NumberRangeValidator;

public class CFWJobsAlertObject extends CFWObject {

	private int MAX_OCCURENCE_CHECK = 24;
	
	private ArrayList<AlertState> alertStateArray;
	
	// Stores Condition Results based on JobIDs
	private static HashMap<String, ArrayList<AlertState>> alertStateStore = new HashMap<>();

	private String jobID;
	private String taskName;

	public enum AlertObjectFields{
		CFW_ALERTING_OCCURENCES_TO_RAISE, 
		CFW_ALERTING_OCCURENCES_TO_RESOLVE, 
		CFW_ALERTING_ALERTDELAY,
		JSON_CFW_ALERTING_USERS_TO_ALERT,
		JSON_CFW_ALERTING_GROUPS_TO_ALERT,
		JSON_CFW_ALERTING_ALERT_CHANNEL,
	}
	
	public enum AlertType{
		NONE,
		RAISE,
		RESOLVE
	}
	
	private CFWField<Integer> occurencesToRaise = 
			CFWField.newInteger(FormFieldType.NUMBER, AlertObjectFields.CFW_ALERTING_OCCURENCES_TO_RAISE)
			.setLabel("Occurences to Raise")
			.setDescription("Number of occurences(matched condition) in series needed to raise an alert.")
			.setValue(2)
			.addValidator(new NumberRangeValidator(1, MAX_OCCURENCE_CHECK));
	
	private CFWField<Integer> occurencesToResolve = 
			CFWField.newInteger(FormFieldType.NUMBER, AlertObjectFields.CFW_ALERTING_OCCURENCES_TO_RESOLVE)
			.setLabel("Occurences to Resolve")
			.setDescription("Number of occurences(not matched condition) in series needed to resolve an alert.")
			.setValue(2)
			.addValidator(new NumberRangeValidator(1, MAX_OCCURENCE_CHECK));
	
	private CFWField<Integer> delayMinutes = 
			CFWField.newInteger(FormFieldType.NUMBER, AlertObjectFields.CFW_ALERTING_ALERTDELAY)
			.setLabel("Alert Delay Minutes")
			.setDescription("The delay in minutes before another alert is triggered, in case the condition matches again.")
			.setValue(60)
			.addValidator(new NumberRangeValidator(1, 60*24*7));
	
	private CFWField<LinkedHashMap<String,String>> usersToAlert = CFWField.newTagsSelector(AlertObjectFields.JSON_CFW_ALERTING_USERS_TO_ALERT)
			.setLabel("Alert Users")
			.setDescription("Select the users that should be alerted.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
				}
			});
	
	private CFWField<LinkedHashMap<String,String>> groupsToAlert = CFWField.newTagsSelector(AlertObjectFields.JSON_CFW_ALERTING_GROUPS_TO_ALERT)
			.setLabel("Alert Groups")
			.setDescription("Select the groups that should be alerted.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());					
				}
			});		
	

	private CFWField<LinkedHashMap<String, String>> alertChannels = 
				CFWField.newCheckboxes(AlertObjectFields.JSON_CFW_ALERTING_ALERT_CHANNEL)
						.setLabel("Alert Channels")
						.setDescription("Choose the channels the alert should be sent through.")
						.setOptions(CFWJobsAlerting.getChannelNamesForUI())
						.setValue(null);
	
	
	/**************************************************************************
	 * Use this constructor only for getting the fields(for forms etc...)
	 **************************************************************************/	
	public CFWJobsAlertObject() {
		 initialize();
	}
	
	/**************************************************************************
	 * Use this constructor to do de actual alerting.
	 * Condition result are associated with the given Job ID.
	 **************************************************************************/	
	public CFWJobsAlertObject(JobExecutionContext context, CFWJobTask task) {

		this.jobID = context.getJobDetail().getKey().getName();
		this.taskName = task.uniqueName();
		
		//-------------------------
		// Get Condition Results
		System.out.println("jobID:"+jobID);
		if(!alertStateStore.containsKey(jobID)) {
			System.out.println("create array");
			alertStateArray = new ArrayList<>();
			alertStateStore.put(jobID, alertStateArray);
		}
		alertStateArray = alertStateStore.get(jobID);
		
		initialize();

	}
	
	private void initialize() {
		this.addFields(occurencesToRaise, occurencesToResolve, delayMinutes, usersToAlert, groupsToAlert, alertChannels);
	}
	
	
	/**************************************************************************
	 * Add the next condition result and checks if an Alert should be sent or
	 * not.
	 * @return true if alert triggered, false otherwise
	 **************************************************************************/
	public AlertType checkSendAlert(boolean conditionMatched) {
		
		synchronized (alertStateArray) {
			
			//-------------------------------------
			// Keep Limit
			if(alertStateArray.size() > MAX_OCCURENCE_CHECK+1) {
				// remove first half of all entries
				for(int i = 0; i < MAX_OCCURENCE_CHECK/2; i++) {
					alertStateArray.remove(0);
				}
			}
			
			//---------------------------------
			// Update State
			AlertType lastAlertType = AlertType.NONE;
			long lastAlertMillis = -1;
	
			if(!alertStateArray.isEmpty()) {
				AlertState lastState = alertStateArray.get(alertStateArray.size()-1);
				lastAlertType = lastState.getAlertType();
				lastAlertMillis = lastState.getLastAlertMillis();
				System.out.println("Last State: "+CFW.JSON.toJSON(lastState));
			}
			
			AlertState currentState = new AlertState(conditionMatched, lastAlertMillis);
			currentState.setAlertType(lastAlertType);
			alertStateArray.add(currentState);
			System.out.println("currentState: "+CFW.JSON.toJSON(currentState));
			
			//---------------------------------
			// Check Condition
			long currentTimeMillis = System.currentTimeMillis();
			long alertDelayMillis = delayMinutes.getValue() * 1000 * 60;
			
			if(lastAlertType.equals(AlertType.NONE)
			|| lastAlertType.equals(AlertType.RESOLVE)) {
				
				//-----------------------------------------
				// Skip if delay not reached
				if(lastAlertMillis != -1 && (lastAlertMillis + alertDelayMillis) > currentTimeMillis ) {
					return AlertType.NONE;
				}
	
				//-----------------------------------------
				// Do alert if all in series are true
				int occurencesInSeries = occurencesToRaise.getValue();
				
				if(alertStateArray.size() >= occurencesInSeries) {
					boolean doAlert = true;
					for(int i = 1; i <= occurencesInSeries; i++) {
						int indexFromLast = alertStateArray.size() - i;
						doAlert &= alertStateArray.get(indexFromLast).getConditionResult();
						System.out.println("in loop:"+CFW.JSON.toJSON(alertStateArray.get(indexFromLast)) );
					}
					System.out.println(doAlert);
					if(doAlert) {
						currentState.setAlertType(AlertType.RAISE);
						currentState.setLastAlertMillis(currentTimeMillis);
						return AlertType.RAISE;
					}else {
						return AlertType.NONE;
					}
				}
			}else {
				
				//-----------------------------------------
				// Do check Lift Alert 
				int occurencesInSeries = occurencesToResolve.getValue();
				
				if(alertStateArray.size() >= occurencesInSeries) {
					
					boolean doLift = true;
					for(int i = 1; i <= occurencesInSeries; i++) {
						int indexFromLast = alertStateArray.size() - i;
						doLift &= !alertStateArray.get(indexFromLast).getConditionResult();
					}
					
					if(doLift) {
						currentState.setAlertType(AlertType.RESOLVE);
						return AlertType.RESOLVE;
					}else {
						return AlertType.NONE;
					}
				}
			}
				
			return AlertType.NONE;
		}
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
		
		LinkedHashMap<String, String> channelSelection = alertChannels.getValue();
		
		if(channelSelection != null && !channelSelection.isEmpty()) {
			for(Entry<String, String> entry : channelSelection.entrySet()) {
				if(entry.getValue().toLowerCase().equals("true")) {
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

	public Integer getOccurencesToRaise() {
		return occurencesToRaise.getValue();
	}

	public CFWJobsAlertObject setOccurencesToRaise(Integer value) {
		this.occurencesToRaise.setValue(value);
		return this;
	}
	
	public CFWJobsAlertObject occurencesToResolve(Integer value) {
		this.occurencesToResolve.setValue(value);
		return this;
	}

	public Integer occurencesToResolve() {
		return occurencesToResolve.getValue();
	}
	

	public Integer getDelayMinutes() {
		return delayMinutes.getValue();
	}


	public CFWJobsAlertObject setDelayMinutes(Integer value) {
		this.delayMinutes.setValue(value);
		return this;
	}


	public LinkedHashMap<String, String> getUsersToAlert() {
		return usersToAlert.getValue();
	}


	public CFWJobsAlertObject setUsersToAlert(LinkedHashMap<String, String> value) {
		this.usersToAlert.setValue(value);
		return this;
	}


	public LinkedHashMap<String, String> getGroupsToAlert() {
		return groupsToAlert.getValue();
	}


	public CFWJobsAlertObject setGroupsToAlert(LinkedHashMap<String, String> value) {
		this.groupsToAlert.setValue(value);
		return this;
	}


	public LinkedHashMap<String, String> getAlertChannels() {
		return alertChannels.getValue();
	}


	public CFWJobsAlertObject setAlertChannels(LinkedHashMap<String, String> value) {
		this.alertChannels.setValue(value);
		return this;
	}

	

	/**************************************************************************
	 * Inner class to store results in an array
	 * 
	 **************************************************************************/
	public class AlertState {
		
		private AlertType alertType = AlertType.NONE;
		private long timeMillis;
		private long lastAlertMillis;
		private boolean result;
		
		public AlertState(boolean result, long lastAlertMillis) {
			this.result = result;
			this.lastAlertMillis = lastAlertMillis;
			
			this.timeMillis = System.currentTimeMillis();
		}
		
		public long getTimeMillis() {
			return timeMillis;
		}
		
		public AlertState setTimeMillis(long timeMillis) {
			this.timeMillis = timeMillis;
			return this;
		}
		
		public long getLastAlertMillis() {
			return lastAlertMillis;
		}
		
		public AlertState setLastAlertMillis(long lastAlertMillis) {
			this.lastAlertMillis = lastAlertMillis;
			return this;
		}
		
		public boolean getConditionResult() {
			return result;
		}
		
		public AlertState setResult(boolean result) {
			this.result = result;
			return this;
		}
		
		public AlertType getAlertType() {
			return alertType;
		}
		
		public AlertState setAlertType(AlertType alertType) {
			this.alertType = alertType;
			return this;
		}
		
		

	}
	

}
