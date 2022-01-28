package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
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
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandSource extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandSource.class.getName());
	
	private int fetchLimit = 0;
	private int recordCounter = 0;
	
	boolean isSourceFetchingDone = false;
	
	// Cache Source instances
	private static TreeMap<String, CFWQuerySource> sourceMapCached;

	/********************************************************
	 * 
	 ********************************************************/
	private static TreeMap<String, CFWQuerySource> getCachedSources() {
		if(sourceMapCached == null) {
			sourceMapCached = CFW.Registry.Query.createSourceInstances(new CFWQuery());
		}
		return sourceMapCached;
	}
	CFWQuerySource source = null;
	CFWObject paramsForSource = null;
	
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
		return "source <sourcename> [param1=abc param2=xyz ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p>Just a test how this will look like</p>";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		
		return "<p>This still has to be documented, any volunteers?</p>";
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
		//System.out.println("CFWQueryCommand.setAndValidateQueryParts()-source:"+source);
		
		//------------------------------------------
		// Get Parameters
		

		EnhancedJsonObject parameters = new EnhancedJsonObject();
		
		for(int i = 1; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
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
			
			System.out.println("test");
			AutocompleteList list = new AutocompleteList();
			result.addList(list);
			int i = 0;
			for (String currentName : sourceMap.keySet() ) {
				System.out.println("test:"+currentName);
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
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		// CFWQuerySource does not have own parameter definition, will be propagated from this command class
		// context.getProperty("parameters");
		
		//------------------------------------------
		// Read source asynchronously
		
		LinkedBlockingQueue<EnhancedJsonObject> localQueue = new LinkedBlockingQueue<>();
		new Thread(
			new Runnable() {
				
				@Override
				public void run() {
					try {
						source.execute(paramsForSource, localQueue, fetchLimit);
					} catch (Exception e) {
						new CFWLog(logger).severe("Error while reading from source '"+source.uniqueName()+"': "+e.getMessage(), e);						
					}
					setSourceFetchingDone();
				}
			}).start();
		
		
		//==================================================
		// Read all Records from Previous Commands
		//==================================================
		while( !( this.isPreviousDone() && inQueue.isEmpty() ) ) {
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
		
		System.out.println(fetchLimit);
		//==================================================
		// Read all records for this Source
		//==================================================
		
		outerloop:
		while( !(isSourceFetchingDone && localQueue.isEmpty()) ) {
			
			//------------------------------------------
			// Read inQueue and put it to outQueue
			

			while(!localQueue.isEmpty()) {
				recordCounter++;
				System.out.println(recordCounter);
				EnhancedJsonObject item = localQueue.poll();
				if(!item.has("_source")) {
					item.addProperty("_source", this.source.uniqueName());
				}
				
				//------------------------------------------
				//Sample Fieldnames, first 10 and every 512
				if( recordCounter % 512 == 0 || recordCounter <= 10 ) {

					parent.addFieldnames(item.keySet());
				}
				
				//------------------------------------------
				// Throw to queue of next Command
				outQueue.add(item);
				
				//------------------------------------------
				// Check Fetch Limit Reached
				if(recordCounter >= fetchLimit) {
					System.out.println("A");
					setSourceFetchingDone();
					CFW.Messages.addInfoMessage("Source '"+source.uniqueName()+"' reached fetch limit of "+fetchLimit);
					break outerloop;
				}
				
			}

			//---------------------------
			// Wait for more input
			this.waitForInput(100);	
		}
		
		
		this.setDone(true);

		
		
	}

}
