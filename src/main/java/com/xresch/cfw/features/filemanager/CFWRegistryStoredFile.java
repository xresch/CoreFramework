package com.xresch.cfw.features.filemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT-License
 **************************************************************************************************************/
public class CFWRegistryStoredFile {
	
	private static final Logger logger = CFWLog.getLogger(CFWRegistryStoredFile.class.getName());
	
	private static ArrayList<CFWStoredFileListener> listenerArray = new ArrayList<CFWStoredFileListener>();
	

	/***********************************************************************
	 * Adds a CFWStoredFileListener class to the registry.
	 * @param listener
	 ***********************************************************************/
	public static void registerListener(CFWStoredFileListener listener)  {

		if(listener == null) { return; }

		listenerArray.add(listener);
		
	}
	
	/***********************************************************************
	 * Adds a CFWStoredFileListener class to the registry.
	 * @param listener
	 ***********************************************************************/
	public static void addAll(ArrayList<CFWStoredFileListener> listeners)  {
		if(listeners != null) {
			for(CFWStoredFileListener listener : listeners) {
				CFWRegistryStoredFile.registerListener(listener);
			}
		}
	}
	
	/***********************************************************************
	 * Removes a CFWStoredFileListener class from the registry.
	 * @param listener
	 ***********************************************************************/
	public static void remove(CFWStoredFileListener listener)  {
		listenerArray.remove(listener);
	}
	
	/***********************************************************************
	 * Check if the file is deletable.
	 * @param listener
	 ***********************************************************************/
	public static boolean canDelete(String id)  {
		if(id == null) { return true; }
		return canDelete( Integer.parseInt(id) );
	}
	
	/***********************************************************************
	 * Check if the file is deletable.
	 * @param listener
	 ***********************************************************************/
	public static boolean canDelete(Integer id)  {
		
		if(id == null) { return true; }
		
		boolean canDelete = true;
		for(CFWStoredFileListener listener : listenerArray) {
			canDelete &= listener.canDelete( id );
		}
		
		return canDelete;
	}
	
	/***********************************************************************
	 * Execute code of listeners after creation.
	 * @param listener
	 ***********************************************************************/
	public static void afterCreate(Integer id)  {
		
		if(id == null) { return; }
		
		for(CFWStoredFileListener listener : listenerArray) {
			listener.afterCreate(id);
		}
		
	}
	
	/***********************************************************************
	 * Execute code of listeners after file content update.
	 * @param listener
	 ***********************************************************************/
	public static void afterFileContentUpdate(Integer id)  {
		
		if(id == null) { return; }
		
		for(CFWStoredFileListener listener : listenerArray) {
			listener.afterFileContentUpdate(id);
		}
		
	}
	
	/***********************************************************************
	 * Returns the list of FWStoredFileListeners.
	 * @param listener
	 ***********************************************************************/
	public static List<CFWStoredFileListener> getCFWStoredFileListeners()  {
		return Collections.unmodifiableList(listenerArray);
	}
	

	
	 
}
