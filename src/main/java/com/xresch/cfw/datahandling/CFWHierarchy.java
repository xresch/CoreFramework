package com.xresch.cfw.datahandling;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.logging.CFWLog;
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
	
	private static String[] PARENT_LABELS =  new String [] { 
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
				CFWField.newInteger(FormFieldType.NONE, PARENT_LABELS[i])
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
				.getFirstAsObject();
		
		CFWObject parentWithHierarchy = new CFWHierarchy(parentObject)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		//------------------------------------------
		// Resolve Child Object
		CFWObject childObject = instance.select()
				.where(primaryFieldName, childID)
				.getFirstAsObject();	
		
		CFWObject childWithHierarchy = new CFWHierarchy(childObject)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
						
		if(setParent(parentWithHierarchy, childWithHierarchy)) {
			return saveNewParents(childWithHierarchy, true);
		}else {
			return false;
		}
	}
	
	/*****************************************************************************
	 * Set the parent object of the child and adds it to the list of children.
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * IMPORTANT: The child object must contain all it's children or the hierarchy will
	 * not be updated properly.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static boolean setParent(CFWObject parentWithHierarchy, CFWObject childWithHierarchy) {
		
		//-------------------------------
		// Argument check
		if(parentWithHierarchy == null || parentWithHierarchy.getPrimaryKey() == null) {
			new CFWLog(logger).severe("Parent could not be found in the database.", new IllegalArgumentException());
			return false;
		}
		
		// will not work when creating hierarchies programmatically
//		if(childWithHierarchy == null || childWithHierarchy.getPrimaryKey() == null) {
//			new CFWLog(logger).severe("Child could not be found in the database.", new IllegalArgumentException());
//			return false;
//		}
		
		if(parentWithHierarchy.getClass() != childWithHierarchy.getClass()) {
			new CFWLog(logger).severe("The class of the two provided objects is not the same.", new IllegalArgumentException());
			return false;
		}
		
		//-------------------------------
		// Circular Reference Check
		
		if(checkCausesCircularReference(parentWithHierarchy, childWithHierarchy)){
			return false;
		}
		
		//-------------------------------
		// MaxDepth Check
		int availableParentSlots = getAvailableParentSlotsCount(parentWithHierarchy);
		int maxChildDepth = getMaxDepthOfHierarchy(childWithHierarchy, 0);

		if(availableParentSlots < maxChildDepth) {
			new CFWLog(logger).severe("The parent cannot be set as the max hierarchy depth would be reached.(maxChildDepth:"+maxChildDepth+", availableParentSlots:"+availableParentSlots+")", new IllegalArgumentException());
			return false;
		}
		
		//-------------------------------
		// Set Parent and Child
		childWithHierarchy.parent = parentWithHierarchy;
		if(parentWithHierarchy.childObjects == null) {
			parentWithHierarchy.childObjects = new LinkedHashMap<Integer, CFWObject>();
		}
		parentWithHierarchy.childObjects.put(childWithHierarchy.getPrimaryKey(), childWithHierarchy);
		
		//-------------------------------
		// Check if last parent was already set.
		@SuppressWarnings("rawtypes")
		LinkedHashMap<String, CFWField> parentFields = parentWithHierarchy.getFields();
		int maxDepthLevels = parentWithHierarchy.getHierarchyConfig().getMaxDepth();
		
		if( parentFields.get(PARENT_LABELS[(maxDepthLevels-1)]).getValue() != null) {
			new CFWLog(logger)
				.severe("Cannot set the parent as the maximum hierarchy depth is reached.", new IllegalStateException());
			
			return false;
		}

		//-------------------------------
		// Propagate values from parentObject to child.
		Integer parentValue = null;
		
		int i = 0;
		for(; i < maxDepthLevels; i++) {
			parentValue = ((CFWField<Integer>)parentFields.get(PARENT_LABELS[i])).getValue();
			if(parentValue != null) {
				((CFWField<Integer>)childWithHierarchy.getField(PARENT_LABELS[i])).setValue(parentValue);
			}else {
				break;
			}
		}
		
		//-----------------------------------------------------
		// set this object as the next parent. Only if the last
		// parent in the hierarchy was not already set.
		if(parentValue == null) {
			
			// i is at index of the last parent slot that was "null"
			((CFWField<Integer>)childWithHierarchy.getField(PARENT_LABELS[i])).setValue(parentWithHierarchy.primaryField.getValue());
			
			//---------------------------------------------
			// Set the rest to null
			i++;
			for(; i < maxDepthLevels; i++) {
				parentValue = ((CFWField<Integer>)parentFields.get(PARENT_LABELS[i])).getValue();
				((CFWField<Integer>)childWithHierarchy.getField(PARENT_LABELS[i])).setValue(null);
			}
		}else {
			new CFWLog(logger)
				.severe("Cannot set the parent as the maximum hierarchy depth is reached.", new IllegalStateException());
			return false;
		}
		
		//-----------------------------------------------------
		// Do for all children of the childWithHierarchy
		boolean isSuccess = true;
		for(Entry<Integer, CFWObject> entry : childWithHierarchy.getChildObjects().entrySet()) {
			isSuccess &= CFWHierarchy.setParent(childWithHierarchy, entry.getValue());
		}
		
		return isSuccess;
	}
	
	/*****************************************************************************
	 * Saves the new parent hierarchy set with CFWHierarchy.setParent() to the 
	 * database. Only commits the transaction if the full hierarchy could be updated
	 * successfully.
	 * 
	 * @param childWithHierarchy the hierarchy to be saved
	 * @param isFirstCall set to true when the method is called the first time (used for DB transaction management)
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static boolean saveNewParents(CFWObject childWithHierarchy, boolean isFirstCall) {
		// Performance: Do this only once instead of doing it for every item in the hierarchy
		String[] parentFieldnames = getParentFieldnames(childWithHierarchy);
		return saveNewParents(childWithHierarchy, parentFieldnames, isFirstCall);
	}
	
	/*****************************************************************************
	 * Saves the new parent hierarchy set with CFWHierarchy.setParent() to the 
	 * database. Only commits the transaction if the full hierarchy could be updated
	 * successfully.
	 * 
	 * @param childWithHierarchy the hierarchy to be saved
	 * @param isFirstCall set to true when the method is called the first time (used for DB transaction management)
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	private static boolean saveNewParents(CFWObject childWithHierarchy, String[] parentFieldnames, boolean isFirstCall) {
		
		//------------------------------
		// Start Transaction
		if(isFirstCall) { CFW.DB.beginTransaction(); };
		
		//------------------------------
		// Do Updates
		boolean isSuccess = true;
		isSuccess &= childWithHierarchy.update((Object[])parentFieldnames);
		 
		for(Entry<Integer, CFWObject> entry : childWithHierarchy.childObjects.entrySet()) {
			isSuccess &= saveNewParents(entry.getValue(), parentFieldnames, false);
		}
		
		//------------------------------
		// Return result
		if(!isFirstCall) { 
			return isSuccess;
		}else {
			//------------------------------
			// Commit or Rollback
			if(isSuccess) {
				CFW.DB.commitTransaction();
				return isSuccess;
			}else {
				CFW.DB.rollbackTransaction();
				return isSuccess;
			}
		}
	}
		
	/*****************************************************************************
	 * Returns true if newParent of child would cause a circular reference.
	 * Creates client error messages. 
	 * 
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static boolean checkCausesMaxDepthOverflow(CFWObject newParent, CFWObject childwithHierarchy) {
		
		return false;
	}
	/*****************************************************************************
	 * Returns true if newParent of child would cause a circular reference.
	 * Creates client error messages. 
	 * 
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static boolean checkCausesCircularReference(CFWObject newParent, CFWObject child) {
		
		Integer parentID = newParent.getPrimaryKey();
		Integer childID = child.getPrimaryKey();
		
		//--------------------------------
		// Check is it's own parent.
		//--------------------------------
		if(parentID != null
		&& childID != null
		&& parentID.equals(childID)) {
			new CFWLog(logger)
				.severe("Cannot set item to be it's own parent.", new IllegalStateException());
			return true;
		}
		
		//===================================================
		// Check if same ID is twice in the hierarchy
		//===================================================
		HashSet<Integer> idCheckSet = new HashSet<>();
		
		//--------------------------------
		// Iterate parents hierarchy
		int maxObjectDepth = newParent.getHierarchyConfig().getMaxDepth();
		for(int i = 0; i < maxObjectDepth ;i++){
			if(newParent.getField(PARENT_LABELS[i]).getValue() == null) {
				break;
			} else {
				int currentSize = idCheckSet.size();
				Integer currentHierarchyItemID = ((CFWField<Integer>)newParent.getField(PARENT_LABELS[i])).getValue();
				idCheckSet.add(currentHierarchyItemID);
				
				//circular reference if the id was already present in the idCheckerSet
				// and size has therefore not increased
				if(idCheckSet.size() == currentSize) {
					new CFWLog(logger)
						.severe("Cannot set the new parent as it would cause a circular reference.(parentID="+parentID+", childID="+childID+", circularReferenceID="+currentHierarchyItemID+")", new IllegalStateException());
					return true;
				}
			}
		}
		
		//--------------------------------
		// Check Child
		int currentSize = idCheckSet.size();
		idCheckSet.add(childID);
		
		//circular reference if the id was already present in the idCheckerSet
		// and size has therefore not increased
		if(idCheckSet.size() == currentSize) {
			new CFWLog(logger)
				.severe("Cannot set the new parent as it would cause a circular reference.(parentID="+parentID+", childID="+childID+", circularReferenceID="+childID+")", new IllegalStateException());
			return true;
		}
		
		//--------------------------------
		// Iterate children of Child
		maxObjectDepth = child.getHierarchyConfig().getMaxDepth();
		LinkedHashMap<Integer, CFWObject> childFlatMap = getAllChildrenAsFlatList(child, new LinkedHashMap<>());
		
		for(Entry<Integer, CFWObject> entry : childFlatMap.entrySet()){
			Integer currentID = entry.getKey();
			if(currentID == null) {
				break;
			} else {
				int tempSize = idCheckSet.size();
				idCheckSet.add(currentID);
				
				// circular reference if the id was already present in the idCheckerSet
				// and size has therefore not increased
				if(idCheckSet.size() == tempSize) {
					new CFWLog(logger)
						.severe("Cannot set the new parent as it would cause a circular reference.(parentID="+parentID+", childID="+childID+", circularReferenceID="+childID+")", new IllegalStateException());
					return true;
				}
			}
		}
		
		return false;
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<Integer, CFWObject> getAllChildrenAsFlatList(CFWObject parent, LinkedHashMap<Integer, CFWObject> resultMap) {
		
		for(Entry<Integer, CFWObject> entry : parent.getChildObjects().entrySet()) {
			resultMap.put(entry.getKey(), entry.getValue());
			
			getAllChildrenAsFlatList(entry.getValue(), resultMap);
		}
		
		return resultMap;
	}
	/*****************************************************************************
	 * Returns the number of parents an object currently has in the upper hierarchy.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static int getUsedParentSlotsCount(CFWObject object) {
		
		if(object == null || !object.isHierarchical()){
			return 0;
		}
		
		int i;
		int maxObjectDepth = object.getHierarchyConfig().getMaxDepth();
		for(i = 0; i < maxObjectDepth ;i++){
			if(object.getField(PARENT_LABELS[i]).getValue() == null) {
				return i;
			}
		}
		return maxObjectDepth;
	}
	
	/*****************************************************************************
	 * Returns the number of available parent slots for an object.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static int getAvailableParentSlotsCount(CFWObject object) {
		
		if(object == null || !object.isHierarchical()){
			return 0;
		}
		
		int maxObjectDepth = object.getHierarchyConfig().getMaxDepth();
		int usedSlots = getUsedParentSlotsCount(object);
		return maxObjectDepth - usedSlots;
	}
	
	/*****************************************************************************
	 * Returns the maximum depth of the given hierarchy. The root object is excluded 
	 * in the resulting count.
	 * 
	 * @param childWithHierarchy the hierarchy to be counted
	 * @param currentMaxDepth set to 0 when initially called
	 * @return int max depth
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static int getMaxDepthOfHierarchy(CFWObject rootWithHierarchy, int currentMaxDepth) {
		
		//if(currentMaxDepth == 0) { currentMaxDepth = 1; }
		
		int localMaxDepth = currentMaxDepth;
		for(Entry<Integer, CFWObject> entry : rootWithHierarchy.getChildObjects().entrySet()) {
			int depthCount = getMaxDepthOfHierarchy(entry.getValue(), currentMaxDepth+1);
			if(depthCount > localMaxDepth) {
				localMaxDepth = depthCount;
			}
		}
		return localMaxDepth;
	}
	
	/*****************************************************************************
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public static String[] getParentFieldnames(CFWObject object) {
		return Arrays.copyOfRange(PARENT_LABELS, 0, object.hierarchyConfig.getMaxDepth());
	}
	
	/*****************************************************************************
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public static String[] getParentAndPrimaryFieldnames(CFWObject object) {
		String[] parentFields = getParentFieldnames(object);
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
	 * 
	 * @param resultFields names of the fields to be fetched additionally to the parent fields.
	 *        Fetches all fields if null;
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public LinkedHashMap<Integer, T> fetchFlatList(Object... resultFields) {
		return (LinkedHashMap<Integer, T>)createFetchHierarchyQuery(resultFields)
					.getAsKeyObjectMap();
	}
	
	/*****************************************************************************
	 * 
	 * @param resultFields names of the fields to be fetched additionally to the parent fields.
	 *        Fetches all fields if null;
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public ResultSet fetchHierarchyResultSet(String... resultFields) {
		return createFetchHierarchyQuery(resultFields)
					.getResultSet();
	}
	
	/*****************************************************************************
	 * 
	 * @param resultFields names of the fields to be fetched additionally to the parent fields.
	 *        Fetches all fields if null;
	 *        
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public CFWHierarchy<T> fetchAndCreateHierarchy(Object... resultFields) {
		
		if(resultFields == null || resultFields.length == 0) {
			resultFields = root.getFieldnames();
		}
		
		objectListFlat = fetchFlatList(resultFields);

		//-----------------------------------------
		//Iterate over all objects
		Integer rootID = root.getPrimaryKey();
		for(Entry<Integer, T> currentEntry : objectListFlat.entrySet()) {
			
			T currentItem = currentEntry.getValue();
			Integer currentID = currentItem.getPrimaryKey();
			
			if( (rootID == null && currentID == null) 
			 || (rootID != null && rootID.equals(currentID)) ) {
				//-------------------------
				// is a root of the hierarchy
				objectHierarchy.put(currentID, currentItem);
				continue;
			}
			//-----------------------------------------
			// Iterate over Parent Fields of current object
			int parentCount = parentAndPrimaryFieldnames.length-1;
			for(int i=0; i < parentCount; i++) {
				
				//-----------------------------------------
				//Find last ParentID that is not null in fields P0 ... Pn, ignore Primary Field
				Integer parentValue = (Integer)currentItem.getField(parentAndPrimaryFieldnames[i]).getValue();
				if(parentValue == null) {
					
					if( i == 0 ) {
						//-------------------------
						// is a root of the hierarchy
						objectHierarchy.put(currentID, currentItem);
					}else {
						
					
						Integer lastParentID = (Integer)currentItem.getField(parentAndPrimaryFieldnames[i-1]).getValue();
						if(objectListFlat.get(lastParentID) == null) {
							
							//-------------------------
							// is a root of the hierarchy
							new CFWLog(logger).warn("This could should actually never be reached, included to have a fallback and prevent errors.(elementID: "+currentID+")");
							objectHierarchy.put(currentID, currentItem);
						}else {
							//-------------------------
							//is a child 
							objectListFlat.get(lastParentID).childObjects.put(currentID, currentItem);
						}
					}
					
					//stop and go to next object
					break;
				}else if(i == parentCount-1) {
					//-----------------------------------------
					// Handle Last Parent Field
					Integer lastParentID = (Integer)currentItem.getField(parentAndPrimaryFieldnames[i]).getValue();
					objectListFlat.get(lastParentID).childObjects.put(currentID, currentItem);
					break;
				}
			}
		}
		
		return this;
	}

	/*****************************************************************************
	 * 
	 * @param resultFields names of the fields to be fetched additionally to the parent fields.
	 *        Fetches all fields if null;
	 * @param 
	 * @return CFWSQL pre-created statement
	 *****************************************************************************/
	public CFWSQL createFetchHierarchyQuery(Object... resultFields) {
		
		if(resultFields == null || resultFields.length == 0) {
			resultFields = root.getFieldnames();
		}
		
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
			.getFirstAsObject();
		
		
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
	
	/*****************************************************************************
	 * Returns a hashmap with all the root elements of the hierarchy.
	 * 
	 *****************************************************************************/
	public LinkedHashMap<Integer, T> getAllRootElements() {
		
		return objectHierarchy;

	}
	/*****************************************************************************
	 * Returns the root element with all it's child elements or null if the Hierarchy
	 * was not fetched.
	 * 
	 *****************************************************************************/
	public T getSingleRootObject() {
		
		for(Entry<Integer, T> entry : objectHierarchy.entrySet()) {
			
			Integer rootID = root.getPrimaryKey();
			Integer currentID = entry.getKey();
			if( (rootID == null && currentID == null) 
			 || (rootID != null && rootID.equals(currentID)) ) {
				return entry.getValue();
			}
		}
		
		return null;
	}
	
}
