package com.xresch.cfw.features.analytics;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.mail.CFWMailBuilder;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class CFWJobTaskTestMail extends CFWJobTask {
	

	@Override
	public String uniqueName() {
		return "Test: eMail";
	}

	@Override
	public String taskDescription() {
		return "Sends test messages to the selected users eMail address.";
	}

	@Override
	public CFWObject getParameters() {
		return new MailTestSettings();
	}

	@Override
	public int minIntervalSeconds() {
		return 15;
	}

	@Override
	public boolean hasPermission(User user) {
		
		if(user.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS) ) {
			return true;
		}
		
		return false;
	}

	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {
		
		//JobDataMap data = context.getMergedJobDataMap();
		
		MailTestSettings settings = new MailTestSettings();
		settings.mapJobExecutionContext(context);
		
		HashMap<Integer, User> userlist = CFW.DB.Users.convertToUserList(settings.getUsersToMail());
		new CFWMailBuilder(settings.getSubject())
					.fromNoReply()
					.recipientsTo(userlist)
					.addMessage(settings.getMessage())
					.send();
		
	}
	
	
	private class MailTestSettings extends CFWObject {
		
		private CFWField<LinkedHashMap<String, String>> usersToMail = 
				CFWField.newTagsSelector("JSON_USERS")
				.setLabel("Users")
				.setDescription("Select the users that should receive the mail.")
				.setValue(null)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
						return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
					}
			});
		
		private CFWField<String> subject = 
				CFWField.newString(FormFieldType.TEXT, "SUBJECT")
						.setDescription("The subject of the test mail.")
						.addValidator(new NotNullOrEmptyValidator());
		
		private CFWField<String> message = 
				CFWField.newString(FormFieldType.WYSIWYG, "MESSAGE")
						.setDescription("The message of the test mail.")
						.allowHTML(true)
						.addValidator(new NotNullOrEmptyValidator());
		
		public MailTestSettings() {
			this.addFields(usersToMail, subject, message);
		}

		public LinkedHashMap<String, String> getUsersToMail() {
			return usersToMail.getValue();
		}

		@SuppressWarnings("unused")
		public MailTestSettings setUsersToMail(LinkedHashMap<String, String> value) {
			this.usersToMail.setValue(value);
			return this;
		}

		public String getSubject() {
			return subject.getValue();
		}

		@SuppressWarnings("unused")
		public MailTestSettings setSubject(String value) {
			this.subject.setValue(value);
			return this;
		}

		
		public String getMessage() {
			return message.getValue();
		}
		
		@SuppressWarnings("unused")
		public MailTestSettings setMessage(String value) {
			this.message.setValue(value);
			return this;
		}
		
		
		
	}
	
}
