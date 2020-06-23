package com.pengtoolbox.cfw.datahandling;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.utils.CFWArrayUtils;

/***************************************************************************************************************************
 * Class to fetch an map hierarchical structures of CFWObjects.
 * The object has to use CFWObject.serHierarchyLevels() to initialize the needed fields.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 ***************************************************************************************************************************/
public class CFWHierarchy<T extends CFWObject> {
	
	private static Logger logger = CFWLog.getLogger(CFWHierarchy.class.getName());
	public static final int maxLevels = 32;
	
	private static String[] labels =  new String [] { 
			  "P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9",
			  "P10", "P11", "P12", "P13", "P14", "P15", "P16", "P17", "P18", "P19",
			  "P20", "P21", "P22", "P23", "P24", "P25", "P26", "P27", "P28", "P29",
			  "P30", "P31", "P32"
			  };
	
	private CFWSQL partialWhereClauseFilter;
	private T root;
	private String[] parentFieldnames;
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
		parentFieldnames = getParentFieldnames(root);
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
	public static void setHierarchyLevels(CFWObject object, int levels) {
		
		//-------------------------------
		// Argument check
		if(levels > maxLevels) {
			new CFWLog(logger)
				.method("setHierarchyLevels")
				.severe("Cannot set levels to '"+levels+"'. The maximum allowed levels is: "+maxLevels, new IllegalArgumentException());
			
			return;
		}
		
		//------------------------------------
		// Add Parent Fields
		// P0... P1... Pn...
		object.hierarchyLevels = levels;
		for(int i = 0; i < levels; i++) {
			object.addField(
				CFWField.newInteger(FormFieldType.NONE, labels[i])
					.setDescription("ID of parent number "+i+" in the same table.")
			);
		}
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
			new CFWLog(logger)
				.method("setParent")
				.severe("The class of the two provided objects is not the same.", new IllegalArgumentException());
			
			return false;
		}
		
		//-------------------------------
		// Argument check
		Integer parentID = parent.getPrimaryField().getValue();
		Integer childID = child.getPrimaryField().getValue();
		
		if(parentID == childID) {
			new CFWLog(logger)
				.method("setParent")
				.severe("Cannot set an object as it's own parent.", new IllegalArgumentException());
			
			return false;
		}
		
		//-------------------------------
		// Set Parent and Child
		child.parent = parent;
		if(parent.childObjects == null) {
			parent.childObjects = new LinkedHashMap<Integer, CFWObject>();
		}
		parent.childObjects.put(((Integer)child.getPrimaryField().getValue()), child);
		

		//-------------------------------
		// Check if last parent was already
		// set.
		LinkedHashMap<String, CFWField<?>> parentFields = parent.getFields();
		int childLevels = child.getHierarchyLevels();
		
		if( parentFields.get(labels[(childLevels-1)]).getValue() != null) {
			new CFWLog(logger)
				.method("setParent")
				.severe("Cannot set the parent as the maximum hierarchy depth is reached.", new IllegalStateException());
			
			return false;
		}

		//-------------------------------
		// Propagate values from parentObject
		// to child object.
		Integer parentValue = null;
		
		int i = 0;
		for(; i < childLevels; i++) {
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
			((CFWField<Integer>)child.getField(labels[i])).setValue(parent.primaryField.getValue());
		}else {
			new CFWLog(logger)
				.method("setParent")
				.severe("Cannot set the parent as the maximum hierarchy depth is reached.", new IllegalStateException());
			return false;
		}
		
		return true;
	}
	
	
	/*****************************************************************************
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public static String[] getParentFieldnames(CFWObject object) {
		return Arrays.copyOfRange(labels, 0, object.hierarchyLevels);
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
	public CFWHierarchy<T> fetchAndCreateHierarchy(String... resultFields) {
		
		ArrayList<T>objectArray = fetchFlatList(resultFields);
		
		for(T object : objectArray) {
			objectListFlat.put(object.getPrimaryField().getValue(), object);
			
			//Find last ParentID that is not null in fields P0 ... Pn
			for(int i=0; i < parentFieldnames.length; i++) {
				Integer parentValue = (Integer)object.getField(parentFieldnames[i]).getValue();
				
				if(parentValue == null) {
					
					if( i == 0 ) {
						//is a root object
						objectHierarchy.put(object.getPrimaryField().getValue(), object);
					}else {
						//is a child object
						Integer lastParentID = (Integer)object.getField(parentFieldnames[i-1]).getValue();
						objectListFlat.get(lastParentID).childObjects.put(object.getPrimaryKey(), object);
					}
					
					break;
				}
			}
		}
		
		return this;
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
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @param object used as first parent, primaryField will be used for selection.
	 *        Set to null to retrieve the full hierarchy.
	 * @return true if successful, false otherwise.
	 * 
	 *****************************************************************************/
	public ArrayList<T> fetchFlatList(String... resultFields) {
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
	public CFWSQL createFetchHierarchyQuery(String... resultFields) {
		
		String parentPrimaryFieldname = root.getPrimaryField().getName();
		Integer parentPrimaryValue = root.getPrimaryKey();
		String[] finalResultFields = CFWArrayUtils.merge(parentFieldnames, resultFields);
		
		//String queryCacheID = root.getClass()+parentPrimaryFieldname+parentPrimaryValue+Arrays.deepToString(finalResultFields);
		
		//----------------------------------------------
		// if primaryValue is null fetch All
		if(parentPrimaryValue == null) {
			CFWSQL statement = root.select(finalResultFields);
					//.queryCache(CFWHierarchy.class, queryCacheID);
			
			if(partialWhereClauseFilter != null) {
				statement
					.custom(" WHERE 1 = 1 ")
					.append(partialWhereClauseFilter);
			}
			
			statement
				.orderby(parentFieldnames)
				.nullsFirst();
			
			return statement;
		}
		
		//---------------------------------
		// get all parent fields with values
		// of the parent element
		CFWObject parent = root.select(parentFieldnames)
			.where(parentPrimaryFieldname, parentPrimaryValue)
			.getFirstObject();
		
//		System.out.println("=================== Parent ==================");
//		System.out.println(parent.getFieldsAsKeyValueString());
//		
//		System.out.println("*** Parent Labels ***");
//		System.out.println(Arrays.toString(getParentFieldnames(root)));
		
		//---------------------------------
		// Create Select Statement, union
		// of root object and it's children
		Integer parentValue = null;
		
		CFWSQL statement = root.select(finalResultFields)
				//.queryCache(CFWHierarchy.class, queryCacheID)
				.where(parentPrimaryFieldname, parentPrimaryValue)
				.append(partialWhereClauseFilter)	
				.union()
				.select(finalResultFields);
				
		int i = 0;
		
		//--------------------------------------------
		// Filter by the parent object, which will always
		// show up in the same P... field.
		
		for(; i < parentFieldnames.length; i++) {
			parentValue = (Integer)parent.getField(parentFieldnames[i]).getValue();
			
			if(parentValue == null) {
				statement
					.where(parentFieldnames[i], parentPrimaryValue)
					.append(partialWhereClauseFilter)	;

				break;
			}
		}
		
		//--------------------------------------------
		// Set ordering
		statement.orderby(parentFieldnames)
				 .nullsFirst();
		
		return statement;
		
	}
	
}
