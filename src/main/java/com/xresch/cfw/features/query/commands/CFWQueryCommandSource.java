package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.oracle.truffle.js.nodes.unary.TypeOfNode;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFieldnameManager;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.pipeline.PipelineActionListener;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class CFWQueryCommandSource extends CFWQueryCommand {
	
	public static final String MESSAGE_LIMIT_REACHED = "One or more sources have reached their fetch limit.";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandSource.class.getName());
	
	private CFWQueryFieldnameManager fieldnameManager = new CFWQueryFieldnameManager();
	
	private int fetchLimit = 0;
	private int recordCounter = 0;
	
	boolean isSourceFetchingDone = false;
	
	// Cache Source instances 
	private static TreeMap<String, CFWQuerySource> sourceMapCached;

	CFWQuerySource source = null;
	CFWObject paramsForSource = null;
	
	
	/********************************************************************************************************
	 * The fieldnames detected during the query. How fieldnames are detected and managed:
	 * <br><br>
	 * 1. CFWQueryCommandSource creates a local list of fieldnames. <br>
	 * 2. Commands modifying fields(rename, keep, remove ...) are modifying the local list of the query.
	 *    These changes will also be propagated by CFWQueryCommandSource to this class. <br>
	 * 3. CFWQueryCommandSource will wait until the next source or last command is finished and pushes the
	 *    local fields to this class. <br>
	 * 
	 ********************************************************************************************************/
	private HashSet<String> fieldnames = new HashSet<>();
	
	// oldFieldname and newFieldname
	private HashMap<String,String> renameMap = new HashMap<>();
	


	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandSource(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"source", "src"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Choose the source to read the data from.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "source <sourcename> [limit=<integer>] [param1=abc param2=xyz ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 	"<ul>"
					+"<li><b>source:&nbsp;</b> The query source to fetch the data from</li>"
					+"<li><b>limit:&nbsp;</b> The max number of records to fetch from the source. The default and max limit is configured by your administrator.</li>"
					+"<li><b>param1, param2 ...:&nbsp;</b> The parameters that are specific to the source you choose.</li>"
				+"</ul>";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_source.html");
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public int getLimit() {
		return fetchLimit;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private void setSourceFetchingDone() {
		
		isSourceFetchingDone = true;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private boolean isSourceCommandInterrupted() throws InterruptedException {
		
		if(this.isInterrupted()) {
			throw new InterruptedException();
		}
		
		return false;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		
		//------------------------------------------
		// Default Values
		fetchLimit = CFW.DB.Config.getConfigAsInt(FeatureQuery.CONFIG_FETCH_LIMIT_DEFAULT);
		
		//------------------------------------------
		// Get Name
		QueryPart namePart = parts.get(0);
		QueryPartValue nameValue = namePart.determineValue(null);
		
		if( nameValue.isNull() || !nameValue.isString()) {
			parser.throwParseException("source: expected source name.", namePart);
		}
		
		String sourceName = nameValue.getAsString().trim();
		
		//------------------------------------------
		// Get Source
		
		if(!CFW.Registry.Query.sourceExists(sourceName)) {
			parser.throwParseException("source: the source does not exist: '"+sourceName+"'", namePart);
		}
		
		this.source = CFW.Registry.Query.createSourceInstance(this.parent, sourceName);

		//------------------------------------------
		// Get Parameters
		

		EnhancedJsonObject parameters = new EnhancedJsonObject();
		
		for(int i = 1; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				
				String paramName = assignment.getLeftSideAsString(null);
				
				//------------------------------------
				// Handle parameters for this command
				if(paramName != null && paramName.equals("limit")) {
					QueryPartValue limitValue = assignment.getRightSide().determineValue(null);
					if(limitValue.isInteger()) {
						int newLimit = limitValue.getAsInteger();
						int maxLimit = CFW.DB.Config.getConfigAsInt(FeatureQuery.CONFIG_FETCH_LIMIT_MAX);
						if(newLimit <= maxLimit) {
							this.fetchLimit = newLimit;
						}else {
							throw new ParseException("The value chosen for limit exceeds the maximum of "+maxLimit+".", assignment.position());
						}
					}
					continue;
				}
				
				//------------------------------------
				// Other params for the chosen source
				assignment.assignToJsonObject(parameters);				
			}else {
				parser.throwParseException("source: Only source name and parameters(key=value) are allowed)", currentPart);
			}
		}
			
		//------------------------------------------
		// Map to Parameters Object
		this.paramsForSource = source.getParameters();
		if(!paramsForSource.mapJsonFields(parameters.getWrappedObject())) {
			
			for(CFWField field : paramsForSource.getFields().values()) {
				ArrayList<String> invalidMessages = field.getInvalidationMessages();
				if(!invalidMessages.isEmpty()) {
					throw new ParseException(invalidMessages.get(0), -1);
				}
			}
			
			throw new ParseException("Unknown error for source command '"+this.uniqueNameAndAliases()+"'", -1);
		}
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	private static TreeMap<String, CFWQuerySource> getCachedSources() {
		if(sourceMapCached == null) {
			sourceMapCached = CFW.Registry.Query.createSourceInstances(new CFWQuery());
		}
		return sourceMapCached;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		TreeMap<String, CFWQuerySource> sourceMap = getCachedSources();
		
		//-------------------------------
		// List up to 50 sources if only 
		// command name is given.
		if( helper.getTokenCount() == 1 ) {
			
			AutocompleteList list = new AutocompleteList();
			result.addList(list);
			int i = 0;
			for (String currentName : sourceMap.keySet() ) {

				CFWQuerySource source = sourceMap.get(currentName);
				
				list.addItem(
					helper.createAutocompleteItem(
						""
					  , currentName
					  , currentName
					  , source.descriptionShort()
					)
				);
				
				i++;
				
				if((i % 10) == 0) {
					list = new AutocompleteList();
					result.addList(list);
				}
				if(i == 50) { break; }
			}
		}
	}
	
	
	/***********************************************************************************************
	 * FOR INTERNAL USE ONLY! Used by the abstract class CFWQueryCommand.
	 * If you want to modify the fieldnames, use the fieldname*()-methods provided by CFWQueryCommand.
	 * 
	 ***********************************************************************************************/
	public CFWQueryFieldnameManager getFieldManager(){
		return fieldnameManager;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		
		//-------------------------------------------------
		// Add listener either to the next Source, the 
		// last command or to self.
		// Push fieldnames to context when done
		CFWQueryCommand commandToListen = this.getNextSourceCommand();
		
		if(commandToListen == null) {
			commandToListen = this.getLastCommand();
			
			if(commandToListen == null) {
				commandToListen = this;
			}
		}
		
		commandToListen.addListener(new PipelineActionListener() {
			
			@Override
			public void onDone() {
				
				// Apply all field modifications of previous source and
				// add the resulting fields to this fieldmanager.
				// Then use this fieldmanager as the one of the context.
				Set<String> namesFromContext = parent.getContext().getFinalFieldnames();

				fieldnameManager.addSourceFieldnames(namesFromContext);

				parent.getContext().setFieldnames(fieldnameManager);
				
				
			}
		});		
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {

		long earliestMillis = parent.getContext().getEarliestMillis();
		long latestMillis = parent.getContext().getLatestMillis();
		//------------------------------------------
		// Read source asynchronously
		
		LinkedBlockingQueue<EnhancedJsonObject> localQueue = new LinkedBlockingQueue<>();
		new Thread(
			new Runnable() {
				
				@Override
				public void run() {
					try {
						source.execute(paramsForSource, localQueue, earliestMillis, latestMillis, fetchLimit);
					} catch (Exception e) {
						new CFWLog(logger).severe("Error while reading from source '"+source.uniqueName()+"': "+e.getMessage(), e);						
					}
					setSourceFetchingDone();
				}
			}).start();
		
		
		//==================================================
		// Read all Records from Previous Commands
		//==================================================
		while( !this.isPreviousDone() || !inQueue.isEmpty() ) {
			//------------------------------------------
			// Read inQueue and put it to outQueue
			while(!inQueue.isEmpty()) {
				
				EnhancedJsonObject item = inQueue.poll();
				if(!item.has("_source")) {
					item.addProperty("_source", this.source.uniqueName());
				}
				
				//------------------------------------------
				// Throw to next queue
				outQueue.add(item);
				
			}
						
			//---------------------------
			// Wait for more input
			this.waitForInput(100);	
			
		}
				
		//==================================================
		// Read all records for this Source
		//==================================================
		
		outerloop:
		while( !(isSourceFetchingDone && localQueue.isEmpty()) ) {
			
			//------------------------------------------
			// Read inQueue and put it to outQueue
			

			while(!localQueue.isEmpty() && !isSourceCommandInterrupted()) {
				recordCounter++;
				
				//------------------------------------------
				// Check Fetch Limit Reached
				if(recordCounter > fetchLimit) {
					setSourceFetchingDone();
					this.parent.getContext().addMessage(MessageType.INFO, MESSAGE_LIMIT_REACHED);
					break outerloop;
				}
				
				//------------------------------------------
				// Add Source Field
				EnhancedJsonObject item = localQueue.poll();
				if(!item.has("_source")) {
					item.addProperty("_source", this.source.uniqueName());
				}
				
				//------------------------------------------
				//Sample Fieldnames, first 512 and every 256th
				if( recordCounter % 256 == 0 || recordCounter <= 256 ) {

					fieldnameManager.addSourceFieldnames(item.keySet());
				}
				
				//------------------------------------------
				// Throw to queue of next Command
				outQueue.add(item);

			}

			//---------------------------
			// Wait for more input
			this.waitForInput(100);	
		}
		
		
		this.setDone();
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void terminateAction() throws Exception {
		
		//CFWQueryCommandSource nextSource = this.getNextSourceCommand();
		
	}
	

}
