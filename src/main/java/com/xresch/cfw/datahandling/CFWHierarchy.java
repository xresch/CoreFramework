package com.xresch.cfw.datahandling;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWArrayUtils;

/***************************************************************************************************************************
 * Class to fetch an map hierarchical structures of CFWObjects.
 * The object has to use CFWObject.serHierarchyLevels() to initialize the needed fields.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 ***************************************************************************************************************************/
public class CFWHierarchy<T extends CFWObject> {
	
	private static Logger logger = CFWLog.getLogger(CFWHierarchy.class.getName());
	public static final int MAX_ALLOWED_DEPTH = 32;
	
	private static String[] labels =  new String [] { 
			  "P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9",
			  "P10", "P11", "P12", "P13", "P14", "P15", "P16", "P17", "P18", "P19",
			  "P20", "P21", "P22", "P23", "P24", "P25", "P26", "P27", "P28", "P29",
			  "P30", "P31", "P32"
			  };
	
	private CFWSQL partialWhereClauseFilter;
	private T root;
	private String[] parentAndPrimaryFieldnames;
	// both maps with primary key and object
	private LinkedHashMap<Integer, T> objectListFlat = new LinkedHashMap<Integer, T>();
	private LinkedHashMap<Integer, T> objectHierarchy = new LinkedHashMap<Integer, T>();
	
	
	/*****************************************************************************
	 * Initializes an instance with the root object.
	 * The root object has to set the id in it's primaryField.
	 * If the value is null, all elements will be fetched.
	 * 
	 * @param root object 
	 *****************************************************************************/
	public CFWHierarchy(T root){
		this.root = root;
		parentAndPrimaryFieldnames = getParentAndPrimaryFieldnames(root);
	}
	
	/*****************************************************************************
	 * Set the filter to apply on fetching the database.
	 *
	 * @param CFWSQL that contains only AND/OR clauses
	 *****************************************************************************/
	public CFWHierarchy<T> setFilter(CFWSQL partialWhereClauseFilter) {
		this.partialWhereClauseFilter = partialWhereClauseFilter;
		return this;
	}
	

	/*****************************************************************************
	 * Set the hierarchy levels of the object and adds the needed
	 * parent fields (P0... P1... Pn...) with FormFieldType.NONE.
	 * 
	 * 
	 *****************************************************************************/
	public static void setHierarchyLevels(CFWObject object, CFWHierarchyConfig hierarchyConfig) {
		
		//-------------------------------
		// Argument check
		int maxDepth = hierarchyConfig.getMaxDepth();
		if(maxDepth > MAX_ALLOWED_DEPTH) {
			new CFWLog(logger)
				.severe("Cannot set levels to '"+maxDepth+"'. The maximum allowed levels is: "+MAX_ALLOWED_DEPTH, new IllegalArgumentException());
			
			return;
		}
		
		//------------------------------------
		// Add Parent Fields
		// P0... P1... Pn...
		object.hierarchyConfig = hierarchyConfig;
		for(int i = 0; i < maxDepth; i++) {
			object.addField(
				CFWField.newInteger(FormFieldType.NONE, labels[i])
					.setDescription("ID of parent number "+i+" in the same table.")
			);
		}
	}
	
	/*****************************************************************************
	 * Checks if the child can moved to the parent using CFWHierarchyConfig.canSort().
	 * Moves the child if true and returns true if successful.
	 * CFWHierarchyConfig.canSort() is responsible for creating error messages.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	public static boolean updateParent(CFWHierarchyConfig config, String parentID, String childID) {
		
		if(!NumberUtils.isDigits(parentID)) {
			new CFWLog(logger).severe("parentID is not a integer.", new IllegalArgumentException());
			return false;
		}
		if(!NumberUtils.isDigits(childID)) {
			new CFWLog(logger).severe("childID is not an integer.", new IllegalArgumentException());
			return false;
		}
		
		return CFWHierarchy.updateParent(config, Integer.parseInt(parentID), Integer.parseInt(childID));		
	}
	
	/*****************************************************************************
	 * Checks if the child can moved to the parent using CFWHierarchyConfig.canSort().
	 * Moves the child if true and returns true if successful.
	 * CFWHierarchyConfig.canSort() is repsonsible for creating error messages.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static boolean updateParent(CFWHierarchyConfig config, int parentID, int childID) {

		CFWObject instance = config.getCFWObjectInstance();
		String primaryFieldName = instance.getPrimaryField().getName();
		
		if(!config.canSort(parentID, childID)) {
			return false;
		}
		
		//------------------------------------------
		// Resolve Parent Object
		CFWObject parentObject = instance.select()
				.where(primaryFieldName, parentID)
				.getFirstObject();
		
		if(parentObject == null) {
			new CFWLog(logger).severe("Parent could not be found in the database.", new IllegalArgumentException());
			return false;
		}
		
		//------------------------------------------
		// Resolve Child Object
		CFWObject childObject = instance.select()
				.where(primaryFieldName, childID)
				.getFirstObject();

		if(childObject == null) {
			new CFWLog(logger).severe("Child could not be found in the database.", new IllegalArgumentException());
			return false;
		}
		System.out.println("parent: "+parentObject.toJSON());
		System.out.println("before: "+childObject.toJSON());
		System.out.println("work: "+setParent(parentObject, childObject));
		System.out.println("after: "+childObject.toJSON());
		return childObject.update();
	}
	
	/*****************************************************************************
	 * Set the parent object of the child and adds it to the list of children.
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static boolean setParent(CFWObject parent, CFWObject child) {
		
		//-------------------------------
		// Argument check
		if(parent.getClass() != child.getClass()) {
			new CFWLog(logger).severe("The class of the two provided objects is not the same.", new IllegalArgumentException());
			return false;
		}
		
		//-------------------------------
		// Argument check
		Integer parentID = parent.getPrimaryField().getValue();
		Integer childID = child.getPrimaryField().getValue();
		
		if(parentID.equals(childID) ) {
			new CFWLog(logger)
				.severe("Cannot set an object as it's own parent.", new IllegalArgumentException());
			
			return false;
		}
		
		//-------------------------------
		// Set Parent and Child
		child.parent = parent;
		if(parent.childObjects == null) {
			parent.childObjects = new LinkedHashMap<Integer, CFWObject>();
		}
		parent.childObjects.put(childID, child);
		

		//-------------------------------
		// Check if last parent was already
		// set.
		@SuppressWarnings("rawtypes")
		LinkedHashMap<String, CFWField> parentFields = parent.getFields();
		int maxDepthLevels = parent.getHierarchyConfig().getMaxDepth();
		
		if( parentFields.get(labels[(maxDepthLevels-1)]).getValue() != null) {
			new CFWLog(logger)
				.severe("Cannot set the parent as the maximum hierarchy depth is reached.", new IllegalStateException());
			
			return false;
		}

		//-------------------------------
		// Propagate values from parentObject
		// to child object.
		Integer parentValue = null;
		
		int i = 0;
		for(; i < maxDepthLevels; i++) {
			parentValue = ((CFWField<Integer>)parentFields.get(labels[i])).getValue();
			if(parentValue != null) {
				((CFWField<Integer>)child.getField(labels[i])).setValue(parentValue);
			}else {
				break;
			}
		}
		
		//-----------------------------------------------------
		// set this object as the next parent. Only if the last
		// parent in the hierarchy was not already set.
		if(parentValue == null) {
			// i is at index of the last parent slot that was "null"
			((CFWField<Integer>)child.getField(labels[i])).setValue(parent.primaryField.getValue());
		}else {
			new CFWLog(logger)
				.severe("Cannot set the parent as the maximum hierarchy depth is reached.", new IllegalStateException());
			return false;
		}
		
		return true;
	}
	
	/*****************************************************************************
	 * Returns the number of parents an object currently has in the upper hierarchy.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static int getUsedParentSlotsCount(CFWObject object) {
		
		if(object == null || object.isHierarchical()){
			return 0;
		}
		
		int i;
		int maxObjectDepth = object.getHierarchyConfig().getMaxDepth();
		for(i = 0; i < maxObjectDepth ;i++){
			if(object.getField(labels[i]).getValue() == null) {
				return i;
			}
		}
		return 0;
	}
	
	/*****************************************************************************
	 * Returns the number of available parent slots for an object.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static int getAvailableParentSlotsCount(CFWObject object) {
		
		if(object == null || object.isHierarchical()){
			return 0;
		}
		
		int maxObjectDepth = object.getHierarchyConfig().getMaxDepth();
		int usedSlots = getUsedParentSlotsCount(object);
		return maxObjectDepth - usedSlots;
	}
	
	/*****************************************************************************
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public static String[] getParentAndPrimaryFieldnames(CFWObject object) {
		String[] parentFields = Arrays.copyOfRange(labels, 0, object.hierarchyConfig.getMaxDepth());
		String[] withPrimaryField = CFWArrayUtils.add(parentFields, object.getPrimaryField().getName());
		return withPrimaryField;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public String dumpHierarchy(String... dumpFields) {
		return CFWHierarchy.dumpHierarchy("", (LinkedHashMap<Integer, CFWObject>)objectHierarchy, dumpFields);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static String dumpHierarchy(String currentPrefix, LinkedHashMap<Integer, CFWObject> objectHierarchy, String... dumpFields) {
		
		//-----------------------------------
		//Create Prefix
		StringBuilder builder = new StringBuilder();
		
		int objectCount = objectHierarchy.values().size();
		for(int i = 0; i < objectCount; i++) {
			CFWObject object = (CFWObject)objectHierarchy.values().toArray()[i];
			builder.append(currentPrefix)
				   .append("|--> ")
				   .append(object.dumpFieldsAsPlaintext(dumpFields)).append("\n");
			
			if(objectCount > 1 && (i != objectCount-1)) {
				builder.append(dumpHierarchy(currentPrefix+"|  ", object.childObjects, dumpFields));
			}else{
				builder.append(dumpHierarchy(currentPrefix+"  ", object.childObjects, dumpFields));
			}
		}
		
		return builder.toString();
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public JsonArray toJSONArray() {
		
		//-----------------------------------
		//Create Prefix
		StringBuilder builder = new StringBuilder();
		
		JsonArray array = new JsonArray();
		CFWObject[] topLevelObjects = objectHierarchy.values().toArray(new CFWObject[] {});

		for(int i = 0; i < topLevelObjects.length; i++) {
			CFWObject object = topLevelObjects[i];
			array.add(object.toJSONElement());
		}
		
		return array;
	}
	
	/*****************************************************************************
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @param object used as first parent, primaryField will be used for selection.
	 *        Set to null to retrieve the full hierarchy.
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public CFWHierarchy<T> fetchAndCreateHierarchy(Object... resultFields) {
		
		ArrayList<T>objectArray = fetchFlatList(resultFields);
		objectListFlat.clear();
		//-----------------------------------------
		//Iterate over all objects
		for(T current : objectArray) {
			objectListFlat.put(current.getPrimaryField().getValue(), current);
			
			//-----------------------------------------
			// Iterate over Parent Fields of current object
			int parentCount = parentAndPrimaryFieldnames.length-1;
			for(int i=0; i < parentCount; i++) {
				
				//-----------------------------------------
				//Find last ParentID that is not null in fields P0 ... Pn, ignore Primary Field
				Integer parentValue = (Integer)current.getField(parentAndPrimaryFieldnames[i]).getValue();
				if(parentValue == null) {
					
					if( i == 0 ) {
						//is a root object
						objectHierarchy.put(current.getPrimaryField().getValue(), current);
					}else {
						//is a child object
						Integer lastParentID = (Integer)current.getField(parentAndPrimaryFieldnames[i-1]).getValue();
						objectListFlat.get(lastParentID).childObjects.put(current.getPrimaryKey(), current);
					}
					
					//stop and go to next object
					break;
				}else if(i == parentCount-1) {
					//-----------------------------------------
					// Handle Last Parent Field
					Integer lastParentID = (Integer)current.getField(parentAndPrimaryFieldnames[i]).getValue();
					objectListFlat.get(lastParentID).childObjects.put(current.getPrimaryKey(), current);
					break;
				}
			}
		}
		
		return this;
	}

	/*****************************************************************************
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @param object used as first parent, primaryField will be used for selection.
	 *        Set to null to retrieve the full hierarchy.
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public ArrayList<T> fetchFlatList(Object... resultFields) {
		return (ArrayList<T>)createFetchHierarchyQuery(resultFields)
					.getAsObjectList();
	}
	
	/*****************************************************************************
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @param object used as first parent, primaryField will be used for selection.
	 *        Set to null to retrieve the full hierarchy.
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public ResultSet fetchHierarchyResultSet(String... resultFields) {
		return createFetchHierarchyQuery(resultFields)
					.getResultSet();
	}
	
	/*****************************************************************************
	 * 
	 * @param names of the fields to be fetched additionally to the parent fields.
	 * @param 
	 * @return CFWSQL pre-created statement
	 *****************************************************************************/
	public CFWSQL createFetchHierarchyQuery(Object... resultFields) {
		
		String parentPrimaryFieldname = root.getPrimaryField().getName();
		Integer parentPrimaryValue = root.getPrimaryKey();
		String[] finalResultFields = CFWArrayUtils.merge(parentAndPrimaryFieldnames, CFWArrayUtils.objectToStringArray(resultFields));
		
		//Check if caching makes sense. e.g. String queryCacheID = root.getClass()+parentPrimaryFieldname+parentPrimaryValue+Arrays.deepToString(finalResultFields);
		
		//----------------------------------------------
		// if primaryValue is null fetch All
		if(parentPrimaryValue == null) {
			CFWSQL statement = root.select(finalResultFields);

			
			if(partialWhereClauseFilter != null) {
				statement
					.custom(" WHERE 1 = 1 ")
					.append(partialWhereClauseFilter);
			}
			
			statement
				.orderby(parentAndPrimaryFieldnames)
				.nullsFirst();
			
			return statement;
		}
		
		//---------------------------------
		// get all parent fields with values
		// of the parent element
		CFWObject parent = root.select(parentAndPrimaryFieldnames)
			.where(parentPrimaryFieldname, parentPrimaryValue)
			.getFirstObject();
		
		
		//---------------------------------
		// Create Select Statement, union
		// of root object and it's children
		Integer parentValue = null;
		
		CFWSQL statement = root.select(finalResultFields)
				.where(parentPrimaryFieldname, parentPrimaryValue)
				.append(partialWhereClauseFilter)	
				;
				
				//--------------------------------------------
				// Filter by the parent object, which will always
				// show up in the same P... field.
				int i = 0;
				
				for(; i < parentAndPrimaryFieldnames.length; i++) {
					parentValue = (Integer)parent.getField(parentAndPrimaryFieldnames[i]).getValue();
					
					if(parentValue == null) {
						statement
							.or().custom("(")
								.custom(parentAndPrimaryFieldnames[i]+" = ?", parentPrimaryValue)
								.append(partialWhereClauseFilter)
							.custom(")");
		
						break;
					}
				}
		
		//--------------------------------------------
		// Set ordering
		statement.orderby(parentAndPrimaryFieldnames)
				 .nullsFirst();
		
		return statement;
		
	}
	
}
