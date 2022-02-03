package com.xresch.cfw.features.query;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWQuerySource{

	protected CFWQuery parent;
	
	public CFWQuerySource(CFWQuery parent) {
		this.parent = parent;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract String uniqueName();
	
	/***********************************************************************************************
	 * Return a short description that can be shown in content assist and will be used as intro text
	 * in the manual. Do not use newlines in this description.
	 ***********************************************************************************************/
	public abstract String descriptionShort();
	
	/***********************************************************************************************
	 * Return a short description How time is applied for this source. This will be shown in content
	 * assist and will be used as text in the manual. Do not use newlines in this description.
	 ***********************************************************************************************/
	public abstract String descriptionTime();
	
	/***********************************************************************************************
	 * Return a CFWObject with the parameters you will support.
	 * The source command will map the parameters to the object and execute the validation. If
	 * all parameter values are valid, it will be passed to the execute() method.
	 * Make sure to add a description for every parameter and a default value.
	 ***********************************************************************************************/
	public abstract CFWObject getParameters();
	
	/***********************************************************************************************
	 * Return the description for the manual page.
	 * This description will be shown on the manual under the header " <h2>Usage</h2>".
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 ***********************************************************************************************/
	public abstract String descriptionHTML();
	
	/*************************************************************************
	 * Return the required permissions as a String.
	 *************************************************************************/
	public abstract String descriptionRequiredPermission();
	
	/*************************************************************************
	 * Return if the user is able to fetch from this source.
	 *************************************************************************/
	public abstract boolean hasPermission(User user);
	
	/***********************************************************************************************
	 * Implement the fetching of the data from your source.
	 * Create EnhancedJsonObjects containing your data and add them to the outQueue.
	 * This method is responsible for filter by the given time range if the processed data
	 * is time based. (see also {@link com.xresch.cfw.utils.json.JsonTimerangeChecker#isInTimeRange() JsonTimerangeChecker} )
	 * If applicable, it is recommended to add a field "_epoch" that contains the
	 * time in milliseconds in case it was parsed from a date string.
	 * For performance reasons it is recommended to interrupt the processing of data when the limit
	 * of records is reached. However, this limit is also enforced by the source command itself.
	 * Use the isLimitReached()-method of this class, this will also add an info message in case 
	 * the limit was reached, example code: 
	 * <pre><code>if( this.isLimitReached(recordCounter, limit)) { break; }</code></pre>
	 * 
	 ***********************************************************************************************/
	public abstract void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit ) throws Exception;
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQuery getParent() {
		return parent;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public boolean isLimitReached(int limit, int recordCount) {
		
		if(recordCount > limit) {
			this.parent.getContext().addMessage(MessageType.INFO, CFWQueryCommandSource.MESSAGE_LIMIT_REACHED);
			return true;
		}
		
		return false;
	}
	
}
