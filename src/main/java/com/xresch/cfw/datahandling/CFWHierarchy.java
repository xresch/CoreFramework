package com.xresch.cfw.datahandling;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWUtilsArray;

/***************************************************************************************************************************
 * Class to fetch an map hierarchical structures of CFWObjects.
 * The object has to use CFWObject.serHierarchyLevels() to initialize the needed fields.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 ***************************************************************************************************************************/
public class CFWHierarchy<T extends CFWObject> {
	
	private static Logger logger = CFWLog.getLogger(CFWHierarchy.class.getName());
	
	public static final String H_LINEAGE = "H_LINEAGE";
	public static final String H_PARENT = "H_PARENT";
	public static final String H_DEPTH = "H_DEPTH";
	public static final String H_POS = "H_POS";
	
	private CFWSQL partialWhereClauseFilter;
	private T root;

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
	public static void setHierarchyConfig(CFWObject object, CFWHierarchyConfig hierarchyConfig) {
				
		//------------------------------------
		// Add Hierarchy Fields
		object.addField(
				CFWField.newInteger(FormFieldType.NONE, H_DEPTH)
					.setDescription("The depth of this element in the hierarchy.")
					.setValue(0)
			);
		
		object.addField(
				CFWField.newInteger(FormFieldType.NONE, H_POS)
					.setDescription("The position of this element in relation to other elements with the same parent.")
					.setValue(0)
			);
		
		object.addField(
				CFWField.newArrayNumber(FormFieldType.NONE, H_LINEAGE)
					.setDescription("Linage of all the parents of this element.")
					.setValue(new ArrayList<>())
			);
		
		object.addField(
			CFWField.newInteger(FormFieldType.NONE, H_PARENT)
				.setDescription("ID of the parent element.")
		);
				
	}
	/*****************************************************************************
	 * Write an audit log for changes in hierarchy structure
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	private static void writeAuditLog(Integer oldParentID, CFWObject newParent, CFWObject movedObject) {
		CFWHierarchyConfig config = movedObject.getHierarchyConfig();
		Object[] auditFields = config.fieldsForAuditLog();
		
		if(auditFields != null && auditFields.length != 0) {
			
			//---------------------------------
			// Get Old Parent from Database
			CFWObject instance = config.getCFWObjectInstance();
			CFWObject oldParent = null;
			
			if(oldParentID != null ) {
			oldParent = instance.select()
					.where(instance.getPrimaryKeyFieldname(), oldParentID)
					.getFirstAsObject();
			}
			
			//---------------------------------
			// Get Object Details
			JsonObject oldParentDetails = new JsonObject();
			JsonObject newParentDetails = new JsonObject();
			JsonObject childDetails = new JsonObject();
			
			for(Object fieldname : auditFields) {
				String nameString = fieldname.toString();
				
				if(oldParent != null) {
					Object value = oldParent.getField(nameString).getValue();
					if(value != null) { oldParentDetails.addProperty(nameString, value.toString()); }
					else 			  { oldParentDetails.add(nameString, JsonNull.INSTANCE); }
				}
				
				if(newParent != null) {
					Object value = newParent.getField(nameString).getValue();
					if(value != null) { newParentDetails.addProperty(nameString, value.toString()); }
					else 			  { newParentDetails.add(nameString, JsonNull.INSTANCE); }
				}
				
				Object value = movedObject.getField(nameString).getValue();
				if(value != null) { childDetails.addProperty(nameString, value.toString()); }
				else			  { childDetails.add(nameString, JsonNull.INSTANCE); }
				

			}
			
			//---------------------------------
			// Create Message
			StringBuilder message = new StringBuilder();
			
			message.append("Move in Hierarchy - Item:"+CFW.JSON.toJSON(childDetails)+", ");
			
			if(oldParent != null) {	message.append("From:"+CFW.JSON.toJSON(oldParentDetails)+", ");
			}else 				  { message.append("From:Root, "); }
			
			if(newParent != null) {	message.append("To:"+CFW.JSON.toJSON(oldParentDetails));
			}else 				  { message.append("To:Root"); }
			
			new CFWLog(logger).audit(CFWAuditLogAction.MOVE, movedObject.getClass(), message.toString());
		}
	}
		
	/*****************************************************************************
	 * Creates the object in the the hierarchy under the specified parent.
	 * Makes it a root element if parentID is null.
	 * Existing children will be ignored and have to be created separately.
	 * 
	 * @return id of the created element if successful, null otherwise.
	 *****************************************************************************/
	public static Integer create(Integer parentID, CFWObject elementToCreate, Object... fieldnamesToExclude) {
		
		//-----------------------------------
		// Check Parameter
		if(elementToCreate == null) {
			new CFWLog(logger).severe("Element cannot be null.", new IllegalArgumentException());
			return null;
		}
		
		CFW.DB.transactionStart();

		//-----------------------------------
		// Create Child Element
		CFWHierarchyConfig config = elementToCreate.getHierarchyConfig();
		
		Integer createdElementID = null;
		if(fieldnamesToExclude == null) {
			createdElementID = new CFWSQL(elementToCreate).insertGetPrimaryKey();
		}else {
			createdElementID = new CFWSQL(elementToCreate).insertGetPrimaryKeyWithout(fieldnamesToExclude);
		}
		

		if(createdElementID == null) {
			// return null on error
			CFW.DB.transactionRollback();
			return null;
		}

		//-----------------------------------
		// Check ParentID not Null
		if(parentID == null) {
			CFW.DB.transactionCommit();
			return createdElementID;
		}
		
		//-----------------------------------
		// Set Parent if not Null
		boolean isSuccess = CFWHierarchy.updateParent(config, parentID, createdElementID);	
			
		if(isSuccess) {
			//Note: commit  might already have been done by updateParent() >> saveNewParents()
			CFW.DB.transactionCommit();
			return createdElementID;
		}else {
			// return null on error
			CFW.DB.transactionRollback();
			return null;
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
		
		if(!NumberUtils.isDigits(childID)) {
			new CFWLog(logger).severe("childID is not an integer.", new IllegalArgumentException());
			return false;
		}
		
		if(parentID == null) {
			return CFWHierarchy.updateParent(config, null, Integer.parseInt(childID));
		}
		
		if(!NumberUtils.isDigits(parentID)) {
			new CFWLog(logger).severe("parentID is not a integer.", new IllegalArgumentException());
			return false;
		}

		
		return CFWHierarchy.updateParent(config, Integer.parseInt(parentID), Integer.parseInt(childID));		
	}
	
	/*****************************************************************************
	 * Checks if the child can moved to the parent using CFWHierarchyConfig.canSort().
	 * Moves the child if true and returns true if successful.
	 * CFWHierarchyConfig.canSort() is responsible for creating error messages.
	 * Will return false if the parent is still the same.
	 * 
	 * @param config of the hierarchy
	 * @param newParentID id or null if child should turn root.
	 * @param childID id of the child
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean updateParent(CFWHierarchyConfig config, Integer newParentID, int childID) {

		CFWObject instance = config.getCFWObjectInstance();
		String primaryFieldName = instance.getPrimaryKeyFieldname();
		
		//------------------------------------------
		// Resolve Child Object
		CFWObject childObject = instance.select()
				.where(primaryFieldName, childID)
				.getFirstAsObject();	
		
		Integer oldParentID = (Integer)childObject.getField(H_PARENT).getValue();
		
		if(oldParentID == newParentID
		|| (   oldParentID != null
			&& newParentID != null
			&& oldParentID.intValue() == newParentID.intValue()
			)
		) {
			// Do not update Parent if it is still the same
			// avoids corrupting order(H_POS) in DB
			return false;
		}
		
		CFWObject childWithHierarchy = new CFWHierarchy(childObject)
				.fetchAndCreateHierarchy()
				.getSingleRootObject();
		
		//------------------------------------------
		// Resolve Parent Object
		CFWObject parentWithHierarchy = null;
		CFWObject parentObject = null;
		if(newParentID != null) {
			parentObject = instance.select()
					.where(primaryFieldName, newParentID)
					.getFirstAsObject();
			
			parentWithHierarchy = new CFWHierarchy(parentObject)
					.fetchAndCreateHierarchy()
					.getSingleRootObject();
		}
			
		//------------------------------------------
		// Check can be Reordered
		if(!config.canBeReordered(parentObject, childObject)) {
			return false;
		}
		
		//------------------------------------------
		// Set and save Parent
		if(setParentNoSave(parentWithHierarchy, childWithHierarchy)) {
			
			//------------------------------------------
			// Fetch Old Parent Object
			CFWObject oldParentWithHierarchy = null;
			CFWObject oldParentObject = null;
			if(oldParentID != null) {
				oldParentObject = instance.select()
						.where(primaryFieldName, oldParentID)
						.getFirstAsObject();
				
				oldParentWithHierarchy = new CFWHierarchy(oldParentObject)
						.fetchAndCreateHierarchy()
						.getSingleRootObject();
			}
			
			writeAuditLog(oldParentID, parentWithHierarchy, childWithHierarchy);
			return saveNewParentStructure(oldParentWithHierarchy, childWithHierarchy, true);
		}else {
			return false;
		}
	}
		
	/*****************************************************************************
	 * Set the parent object of the child and adds it to the list of children.
	 * The childs db entry has to be updated manually afterwards.
	 * This function is useful to build initial structures in code.
	 * If you want to update the hierarchy in the database, use updateParent()-method
	 * instead.
	 * 
	 * IMPORTANT: The child object must contain all it's children or the hierarchy will
	 * not be updated properly.
	 * 
	 * @param parentWithHierarchy CFWObject or null if child should turn to root
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	private static boolean setParentNoSave(CFWObject parentWithHierarchy, CFWObject childWithHierarchy) {
		
		//-------------------------------
		// Argument check
		if(childWithHierarchy == null) {
			new CFWLog(logger).severe("ChildObject cannot be null.", new IllegalArgumentException());
			return false;
		}
		
		//-------------------------------
		// Check if child should be made root
		if(parentWithHierarchy == null) {
			childWithHierarchy.getField(H_PARENT).setValue(null);
			((CFWField<ArrayList<Number>>)childWithHierarchy.getField(H_LINEAGE)).setValue(new ArrayList<>());
			((CFWField<Integer>)childWithHierarchy.getField(H_DEPTH)).setValue(0);

			int rootItemsCount = CFWHierarchy.getLastChildPosition(childWithHierarchy.getHierarchyConfig(), null);
			((CFWField<Integer>)childWithHierarchy.getField(H_POS)).setValue(rootItemsCount+1);

			boolean isSuccess = true;
			for(Entry<Integer, CFWObject> entry : childWithHierarchy.getChildObjects().entrySet()) {
				isSuccess &= CFWHierarchy.setParentNoSave(childWithHierarchy, entry.getValue());
			}
			
			return isSuccess;
		}
		
		//-------------------------------
		// Argument check
		if(parentWithHierarchy.getPrimaryKeyValue() == null) {
			new CFWLog(logger).severe("Parent primary key is null. Please make sure to store it in the database first.", new IllegalArgumentException());
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
		// Set Parent and Child
		childWithHierarchy.parent = parentWithHierarchy;
		if(parentWithHierarchy.childObjects == null) {
			parentWithHierarchy.childObjects = new LinkedHashMap<Integer, CFWObject>();
		}
		parentWithHierarchy.childObjects.put(childWithHierarchy.getPrimaryKeyValue(), childWithHierarchy);
		
		childWithHierarchy.getField(H_PARENT).setValue(parentWithHierarchy.getPrimaryKeyValue());

		//-------------------------------
		// Propagate values from parentObject to child.
		@SuppressWarnings("rawtypes")
		LinkedHashMap<String, CFWField> parentFields = parentWithHierarchy.getFields();
		
		ArrayList<Number> parentLinage = ((CFWField<ArrayList<Number>>)parentFields.get(H_LINEAGE)).getValue();
		if(parentLinage == null) { parentLinage = new ArrayList<>();}
		
		// do not work directly on parentLinage 
		ArrayList<Number> lineageForChild = new ArrayList<>();
		lineageForChild.addAll(parentLinage);
		lineageForChild.add(parentWithHierarchy.getPrimaryKeyValue());

		((CFWField<ArrayList<Number>>)childWithHierarchy.getField(H_LINEAGE)).setValue(lineageForChild);
		
		((CFWField<Integer>)childWithHierarchy.getField(H_DEPTH)).setValue(lineageForChild.size());

		// read child count from db as reading size from map can be inaccurate
		Integer parentLastChildPos = CFWHierarchy.getLastChildPosition(parentWithHierarchy.getHierarchyConfig(), parentWithHierarchy.getPrimaryKeyValue());
		if(parentLastChildPos == null) {
			new CFWLog(logger).severe("Error while selecting position of last child.", new Exception());
			return false;
		}
		((CFWField<Integer>)childWithHierarchy.getField(H_POS)).setValue(parentLastChildPos+1);
		
		//-----------------------------------------------------
		// Do for all children of the childWithHierarchy
		boolean isSuccess = true;
		for(Entry<Integer, CFWObject> entry : childWithHierarchy.getChildObjects().entrySet()) {
			isSuccess &= CFWHierarchy.setParentNoSave(childWithHierarchy, entry.getValue());
		}
		
		return isSuccess;
	}
	
	
	/*****************************************************************************
	 * Resets the positions of direct children of the given hierarchy.
	 * This function is used to keep proper order of the positions in the database. 
	 * The children in the hierarchy have to be ordered by H_POS in ascending order.
	 * This function should be called during an active DB transaction.
	 * 
	 *****************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean resetChildPositions(CFWObject parentWithHierarchy, Integer removedChildID) {
		boolean isSuccess = true;
		int newPos = 1;
		
		// done by caller
		//CFW.DB.transactionStart();
		for(Entry<Integer, CFWObject> entry : parentWithHierarchy.getChildObjects().entrySet()) {
			CFWObject current = entry.getValue();
			
			// Skip if it the child is not anymore in the hierarchy.
			if(current.getPrimaryKeyValue().intValue() != removedChildID.intValue()) {
				current.getField(H_POS).setValue(newPos);
				isSuccess &= current.update(H_POS);
				newPos++;
			}
		}
		
		return isSuccess;
	}
	/*****************************************************************************
	 * 
	 * @param config of the hierarchy
	 * @param itemID the primary key of the item that should be moved up or down
	 * @param moveUp true to move up, false to move down
	 * @return true if order was updated, false otherwise.
	 *****************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean updatePosition(CFWHierarchyConfig config, Integer itemID, boolean moveUp) {
		boolean isSuccess = true;
		
		CFWObject instance = config.getCFWObjectInstance();
		String primaryFieldName = instance.getPrimaryKeyFieldname();
		
		//------------------------------------------
		// Update if possible
		if(itemID != null) {
	
			//---------------------------------
			// Get Values
			CFWObject itemToMove = instance.select()
					.where(primaryFieldName, itemID)
					.getFirstAsObject();
			
			Integer parentID = (Integer)itemToMove.getField(H_PARENT).getValue();
			int childCount = CFWHierarchy.getChildCount(config, parentID);
			
			Integer originPos = (Integer)itemToMove.getField(H_POS).getValue();
			

			if( moveUp && originPos <= 1) {
				CFW.Messages.addInfoMessage("Item is already at the highest position.");
				return false;
			}
			
			if( !moveUp && originPos >= childCount) {
				CFW.Messages.addInfoMessage("Item is already at the lowest position.");
				return false;
			}
			
			int targetPos = (moveUp) ? originPos - 1 : originPos + 1;
			
			
			CFWObject itemToSwap = instance.select()
					.where(H_PARENT, parentID)
					.and(H_POS, targetPos)
					.getFirstAsObject();
			
			if(itemToSwap != null) {
				itemToSwap.getField(H_POS).setValue(originPos);
				isSuccess &= itemToSwap.update(H_POS);

				itemToMove.getField(H_POS).setValue(targetPos);
				isSuccess &= itemToMove.update(H_POS);
			}else {
				CFW.Messages.addErrorMessage("CFWHierarchy.updatePosition(): Item to swap position with was not found in database.");
				return false;
			}
			
		}else {
			CFW.Messages.addErrorMessage("CFWHierarchy.updatePosition(): ItemID cannot be null.");
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
	private static boolean saveNewParentStructure(CFWObject oldParentWithHierarchy, CFWObject childWithHierarchy, boolean isFirstCall) {
		
		//------------------------------
		// Start Transaction
		boolean isSuccess = true;
		if(!CFW.DB.transactionIsStarted()) { CFW.DB.transactionStart(); };
		
		
		//------------------------------
		// Do Update Old Parent
		if(oldParentWithHierarchy != null) {
			isSuccess &= resetChildPositions(oldParentWithHierarchy, childWithHierarchy.getPrimaryKeyValue());
		}
		//------------------------------
		// Do Updates
		isSuccess &= childWithHierarchy.update(H_DEPTH, H_POS, H_LINEAGE, H_PARENT);
		 
		for(Entry<Integer, CFWObject> entry : childWithHierarchy.childObjects.entrySet()) {
			isSuccess &= saveNewParentStructure(null, entry.getValue(), false);
		}
		
		
		
		//------------------------------
		// Return result
		if(!isFirstCall) { 
			return isSuccess;
		}else {
			//------------------------------
			// Commit or Rollback
			if(isSuccess) {
				CFW.DB.transactionCommit();
				return isSuccess;
			}else {
				CFW.DB.transactionRollback();
				return isSuccess;
			}
		}
	}
	
	/*****************************************************************************
	 * Deletes the element and all its Child elements
	 * 
	 * @return boolean true if successful, false otherwise
	 *****************************************************************************/
	public static boolean deleteWithChildren(CFWHierarchyConfig config, int elementID) {
		
		//-----------------------------------
		// Create Child Element
		CFWObject tempObject = config.getCFWObjectInstance();
		
		return new CFWSQL(tempObject)
			.delete()
			.where(tempObject.getPrimaryKeyFieldname(), elementID)
			.or().arrayContains(H_LINEAGE, elementID)
			.executeDelete()
			;
		

	}
		
	/*****************************************************************************
	 * Returns true if newParent of child would cause a circular reference.
	 * Creates client error messages. 
	 * 
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static boolean checkCausesCircularReference(CFWObject newParent, CFWObject child) {
		
		Integer parentID = newParent.getPrimaryKeyValue();
		Integer childID = child.getPrimaryKeyValue();
		
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
		HashSet<Number> idCheckSet = new HashSet<>();
		
		//--------------------------------
		// Iterate parents hierarchy
	
		ArrayList<Number> lineage = (ArrayList<Number>)newParent.getField(H_LINEAGE).getValue();
		if(lineage != null) {
			for(Number currentID : lineage) {
				int currentSize = idCheckSet.size();
				
				idCheckSet.add(currentID);
				
				//circular reference if the id was already present in the idCheckerSet
				// and size has therefore not increased
				if(idCheckSet.size() == currentSize) {
					new CFWLog(logger)
						.severe("Cannot set the new parent as it would cause a circular reference.(parentID="+parentID+", childID="+childID+", circularReferenceID="+currentID+")", new IllegalStateException());
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
	 * Returns the parent of the hierarchical element.
	 * 
	 *****************************************************************************/
	public static Integer getParentID(CFWObject object) throws IllegalArgumentException {
		
		if(object != null 
		&& object.isHierarchical() 
		&& object.getFields().containsKey(H_PARENT) ) {
			return (Integer)object.getField(H_PARENT).getValue();
		}else {
			throw new IllegalArgumentException("Provided object is either null or not hierarchical.");
		}

	}
	
	/*****************************************************************************
	 * Returns the id of the root element of the provided object.
	 * If it is a root object itself, returns its own id.
	 * 
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static Integer getRootID(CFWObject object) throws IllegalArgumentException {
		
		if(object != null 
		&& object.isHierarchical() 
		&& object.getFields().containsKey(H_LINEAGE) ) {
			
			ArrayList<Number> lineage = (ArrayList<Number>)object.getField(H_LINEAGE).getValue();
			if(lineage != null && lineage.size() > 0) {
				return lineage.get(0).intValue();
			}else {
				return object.getPrimaryKeyValue();
			}
		}else {
			throw new IllegalArgumentException("Provided object is either null or not hierarchical.");
		}

	}
	/*****************************************************************************
	 * Returns true if newParent of child would cause a circular reference.
	 * Creates client error messages. 
	 * 
	 *****************************************************************************/
	@SuppressWarnings("unchecked")
	public static int getChildCount(CFWHierarchyConfig config, Integer parentID) {
		
		CFWObject instance = config.getCFWObjectInstance();
		String primaryFieldName = instance.getPrimaryKeyFieldname();
		
		return instance
			.selectCount()
			.where(H_PARENT, parentID)
			.executeCount();
	}
	
	/*****************************************************************************
	 * Returns the highest number in the Column H_POS for the direct descendants 
	 * in the given hierarchy.
	 * @return highest child position, 0 if there are no children
	 *****************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Integer getLastChildPosition(CFWHierarchyConfig config, Integer parentID) {
		
		CFWObject instance = config.getCFWObjectInstance();

		Integer maxPos = new CFWSQL(instance)
			.custom("SELECT MAX("+H_POS+") AS MAX_POS FROM "+instance.getTableName()+" T ")
			.where(H_PARENT, parentID)
			.getFirstAsInteger();

		return maxPos;
	}
	
	/*****************************************************************************
	 * Returns the highest number in the Column H_POS for the direct descendants 
	 * in the given hierarchy.
	 * @return highest child position, 0 if there are no children
	 *****************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int getLastChildPosition(CFWObject objectWithHierarchy) {
		boolean isSuccess = true;
		int highestPos = 0;
		
		for(Entry<Integer, CFWObject> entry : objectWithHierarchy.getChildObjects().entrySet()) {
			CFWObject current = entry.getValue();
			Integer currentPos = ((CFWField<Integer>)objectWithHierarchy.getField(H_POS)).getValue();
			if(currentPos != null && currentPos.intValue() > highestPos) {
				highestPos = currentPos.intValue();
			}
		}
		
		return highestPos;
	}
	
	/*****************************************************************************
	 * Returns a list of all the parents of a hierarchical object.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	public static LinkedHashMap<Integer, CFWObject> getParentsAsFlatList(CFWHierarchyConfig config, String childID) {
		
		CFWObject instance = config.getCFWObjectInstance();
		
		CFWObject childObject = instance.select()
				.where(instance.getPrimaryKeyFieldname(), childID)
				.getFirstAsObject();	
		
		return getParentsAsFlatList(childObject);
		
	}
	
	/*****************************************************************************
	 * Returns a list of all the parents of a hierarchical object.
	 * 
	 * @return true if successful, false otherwise.
	 *****************************************************************************/
	public static LinkedHashMap<Integer, CFWObject>  getParentsAsFlatList(CFWObject child) {
				
		if(!child.isHierarchical()) {
			return new LinkedHashMap<>();
		}
		
		CFWField<ArrayList<Number>> numberArrayField = (CFWField<ArrayList<Number>>)child.getField(H_LINEAGE);
		
		 LinkedHashMap<Integer, CFWObject> result = new  LinkedHashMap<>();
		for(Number parentID : numberArrayField.getValue()) {
			CFWObject currentParent = new CFWSQL(child)
										.select()
										.where(child.getPrimaryKeyFieldname(), parentID.intValue())
										.getFirstAsObject();
			result.put(parentID.intValue(), currentParent);
		}
		
		return result;
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
	@SuppressWarnings("unchecked")
	public CFWHierarchy<T> fetchAndCreateHierarchy(Object... resultFields) {
		
		if(resultFields == null || resultFields.length == 0) {
			resultFields = root.getFieldnames();
		}
		
		objectListFlat = fetchFlatList(resultFields);

		//-----------------------------------------
		//Iterate over all objects
		Integer rootID = root.getPrimaryKeyValue();
		for(Entry<Integer, T> currentEntry : objectListFlat.entrySet()) {
			
			T currentItem = currentEntry.getValue();
			Integer currentID = currentItem.getPrimaryKeyValue();
			ArrayList<Number> currentLineage = (ArrayList<Number>)currentItem.getField(H_LINEAGE).getValue();
			Integer currentParentID = (Integer)currentItem.getField(H_PARENT).getValue();
			
			//--------------------------------
			// Add currentItem to Flat List
			objectListFlat.put(currentID, currentItem);
			
			//--------------------------------
			// Add root to hierarchy
			if( (rootID == null 
				 && ( currentLineage == null || currentLineage.size() == 0 ) ) 
			 || (rootID != null 
			     && rootID.equals(currentID)) ) {
				//-------------------------
				// is a root of the hierarchy
				objectHierarchy.put(currentID, currentItem);
				continue;
			}
			
			//------------------------------------------------
			// Get Parent of current item and add as child
			if(currentParentID != null) {
				objectListFlat.get(currentParentID).childObjects.put(currentID, currentItem);
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
		
		String parentPrimaryFieldname = root.getPrimaryKeyField().getName();
		Integer parentPrimaryValue = root.getPrimaryKeyValue();
		String[] hierarchyAndPrimaryFieldnames = new String[] {H_DEPTH, H_POS, H_PARENT, H_LINEAGE, parentPrimaryFieldname};
		String[] finalResultFields = CFW.Utils.Array.merge(
				hierarchyAndPrimaryFieldnames, 
				CFWUtilsArray.objectToStringArray(resultFields)
			);
		
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
			
			// sort by lineage and position to get order of items correctly resolved.
			statement
				.orderby(H_LINEAGE, H_POS)
				.nullsFirst();
			return statement;
		}
				
		//---------------------------------
		// Create Select Statement, union
		// of root object and it's children
		CFWSQL statement = root.select(finalResultFields)
				.where(parentPrimaryFieldname, parentPrimaryValue)
				.append(partialWhereClauseFilter)
				.or().arrayContains(H_LINEAGE, parentPrimaryValue+"")
				;
				
		//--------------------------------------------
		// Set ordering
		statement.orderby(hierarchyAndPrimaryFieldnames)
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
			
			Integer rootID = root.getPrimaryKeyValue();
			Integer currentID = entry.getKey();
			if( (rootID == null && currentID == null) 
			 || (rootID != null && rootID.equals(currentID)) ) {
				return entry.getValue();
			}
		}
		
		return null;
	}
	
}
