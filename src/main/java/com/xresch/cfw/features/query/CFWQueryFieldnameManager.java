package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.JsonArray;
/**************************************************************************************************************
 * This class is used to manage fieldnames during query execution.
 * 
 * Here is how the fieldnames are managed during the query execution:
 * <br><br>
 * 1. CFWQueryCommandSource has a local instance of this class, adding all the fieldnames it detects. <br>
 * 2. Commands modifying fields(rename, keep, remove ...) are modifying the local list of the query.
 *    These changes will also be propagated by CFWQueryCommandSource to this class. <br>
 * 3. CFWQueryCommandSource will wait until the next source or last command is finished and pushes the
 *    local fields to this class. <br>
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 * 
 **************************************************************************************************************/
public class CFWQueryFieldnameManager {
	
	// fieldnames as detected by the query
	protected LinkedHashSet<String> sourceFieldnames = new LinkedHashSet<>();
	
	ArrayList<FieldModification> modifications = new ArrayList<>();
	
	protected enum ModificationType { ADD, ADDALL, RENAME, REMOVE, CLEAR};
	
	/***********************************************************************************************
	 * Apply all modifications and return resulting list
	 ***********************************************************************************************/
	public LinkedHashSet<String> getFinalFieldList() {
		
		LinkedHashSet<String> finalNames = new LinkedHashSet<>();
		
		finalNames.addAll(sourceFieldnames);
		
		for(FieldModification currentMod : modifications) {
			
			switch(currentMod.type) {
			
				case ADD:		finalNames.add(currentMod.fieldname);
								break;
								
				case ADDALL:	for(String name : currentMod.fieldnames) {
									if(!name.startsWith("_")) {
										finalNames.add(name);
									}
								}
								break;
								
				case CLEAR: 	finalNames.clear();
								break;
								
				case RENAME:	finalNames.remove(currentMod.fieldname);
								finalNames.add(currentMod.newName);
								break;
								
				case REMOVE:	finalNames.remove(currentMod.fieldname);
					
								break;
				default:
								break;
			
			}
		}
		
		return finalNames;
	}
	
	/***********************************************************************************************
	 * Gets the final List as a Json Array
	 ***********************************************************************************************/
	public JsonArray getFinalFieldListAsJsonArray() {
		JsonArray jsonArray = new JsonArray();
		
		for(String fieldname : this.getFinalFieldList()) {
			jsonArray.add(fieldname);
		}
		
		return jsonArray;
	}
	/***********************************************************************************************
	 * INTERNAL USE ONLY!
	 * Method is called by CFWQueryCommandSource to add names.
	 * 
	 ***********************************************************************************************/
	public CFWQueryFieldnameManager addSourceFieldnames(Set<String> names) {
		for(String name : names) {
			sourceFieldnames.add(name);
		}
		return this;
	}
	
	/***********************************************************************************************
	 * Removes all fields and adds the selected ones
	 ***********************************************************************************************/
	public CFWQueryFieldnameManager keep(String... fieldnames) {
		
		this.clear();
		
		for(String current : fieldnames) {
			this.add(current);
		}
		
		return this;
	}
	
	/***********************************************************************************************
	 * add the fieldname
	 ***********************************************************************************************/
	public CFWQueryFieldnameManager add(String fieldname) {
		
		modifications.add(new FieldModification(ModificationType.ADD, fieldname, null) );
		return this;
	}
	
	/***********************************************************************************************
	 * Add all fieldnames, except for fieldnames that start with an underscore.
	 ***********************************************************************************************/
	public CFWQueryFieldnameManager addall(Set<String> names) {
		
		if(modifications.size()>0) {
			FieldModification previous = modifications.get(modifications.size()-1);
			
			if(previous.type == ModificationType.ADDALL && previous.fieldnames.equals(names)) {
				// save memory by doing nothing
				return this;
			}
			
		}
		
		modifications.add(new FieldModification(ModificationType.ADDALL, names) );
		
		return this;
	}
	
	/***********************************************************************************************
	 * remove the fieldname
	 ***********************************************************************************************/
	public CFWQueryFieldnameManager remove(String fieldname) {
		
		modifications.add( new FieldModification(ModificationType.REMOVE, fieldname, null) );
		return this;
	}
	
	/***********************************************************************************************
	 * Rename the field
	 ***********************************************************************************************/
	public CFWQueryFieldnameManager rename(String fieldname, String newName) {
		
		modifications.add( new FieldModification(ModificationType.RENAME, fieldname, newName) );
		return this;
	}
	
	/***********************************************************************************************
	 * Remove all fieldnames
	 ***********************************************************************************************/
	public CFWQueryFieldnameManager clear() {
		
		modifications.add( new FieldModification(ModificationType.CLEAR, null, null) );
		return this;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private class FieldModification {
		
		protected ModificationType type;
		protected String fieldname;
		protected Set<String> fieldnames;
		protected String newName;
		
		public FieldModification(ModificationType modification, String fieldname, String newName) {
			this.type 	= modification;
			this.fieldname		= fieldname;
			this.newName 		= newName;
		}
		
		public FieldModification(ModificationType modification, Set<String> fieldnames) {
			this.type 		= modification;
			this.fieldnames	= fieldnames;
		}
	}
	

}
