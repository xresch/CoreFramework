package com.xresch.cfw.features.filemanager;

/**************************************************************************************************************
 * Interface to listen to changes of files and check certain conditions
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT-License
 **************************************************************************************************************/
public interface CFWStoredFileListener {

	/***************************************************************************
	 * Hook method that lets you run code after a file was created.
	 * Useful if you want to scan or modify the file and store the results of that 
	 * action.
	 * @param ID of the new file.
	 ***************************************************************************/
	public void afterCreate(int id);
	
	/***************************************************************************
	 * Hook method that lets you run code after a user uploaded new file content.
	 * Useful if you want to scan or modify the file and store the results of that 
	 * action.
	 * @param ID of the updated file.
	 ***************************************************************************/
	public void afterFileContentUpdate(int id);
	
	/***************************************************************************
	 * Check if the file is still in use and return true if the file can be deleted.
	 * Return false if the file is still in use and cannot be be deleted from the 
	 * file manager.
	 * You can also output the names or IDs of objects that still reference the file
	 * as messages to make it easier for the user to understand what blocks him from
	 * deleting the file.
	 * 
	 * @param ID of the file.
	 * @return true if the file with the id can be deleted, false otherwise
	 ***************************************************************************/
	public boolean canDelete(int id);
}
