package com.pengtoolbox.cfw.caching;

import com.pengtoolbox.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FileDefinition {

	public enum HandlingType {FILE, JAR_RESOURCE, STRING}
	public static final String CFW_JAR_RESOURCES_PATH = "com.pengtoolbox.cfw.resources";
	
	private HandlingType type; 
	private String path;
	private String filename;
	private String content;
	
	
	/***********************************************************************
	 * Use this constructor with HandlingType FILE and JAR_RESOURCE.
	 * Use the constructor FileDefinition(String) for HandlingType.STRING.
	 * @param type
	 * @param path
	 * @param filename
	 ***********************************************************************/
	public FileDefinition(HandlingType type, String path, String filename) {
		this.type = type;
		this.path = path;
		this.filename = filename;
	}
	
	public FileDefinition(String fileContent) {
		this.type = HandlingType.STRING;
		this.content = fileContent;
		this.path = "";
		this.filename = "";
	}

	public HandlingType getType() {
		return type;
	}

	public void setType(HandlingType type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**************************************************************************
	 * Return the contents set by setContent().
	 * @return
	 **************************************************************************/
	private String getContent() {
		return content;
	}

	/**************************************************************************
	 * Set the contents which should be used when the HandlingType.STRING is used.
	 * @return
	 **************************************************************************/
	public void setContent(String content) {
		this.content = content;
	}
	
	/**************************************************************************
	 * Can be used without the content being read.
	 **************************************************************************/
	public int getUniqueID(){
		if(type == HandlingType.STRING ) {
			return (type + content).hashCode();
		}else {
			return (type + path + filename).hashCode();
		}
	}
	
	
	public int hashCode(){
		return (path + filename + content).hashCode();
	}
	
	/**************************************************************************
	 * Read the contents of the file specified by this File definition and
	 * returns it as a string.
	 * @return
	 **************************************************************************/
	public String readContents(){
		String returnContent = "";
		switch(type) {
			case FILE:			returnContent = CFW.Files.getFileContent(null, path, filename);
								break;
				
			case JAR_RESOURCE: 	returnContent = CFW.Files.readPackageResource(path, filename);
								break;
				
			case STRING: 		returnContent = content;
								break;
				
			default: 			returnContent = "";
							break;
							
		}
		
		return returnContent;
	}
	
	public String getJavascriptTag(){
		
		switch(type) {
			case FILE:			return "<script src=\""+path+"/"+filename+"\"></script>";
				
			case JAR_RESOURCE: 	return "<script src=\"/cfw/jarresource?pkg="+path+"&file="+filename+"\"></script>";
				
			case STRING: 		return "<script>" + content + "</script>";
				
			default: 			return "";
				
		}

	}
	
	

		
}
