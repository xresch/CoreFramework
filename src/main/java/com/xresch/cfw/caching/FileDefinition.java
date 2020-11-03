package com.xresch.cfw.caching;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.config.FeatureConfiguration;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FileDefinition {

	public enum HandlingType {FILE, JAR_RESOURCE, STRING}
	
	private HandlingType type; 
	private String path;
	private String filename;
	private String content;
	
	private Integer hashCode = null;
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
	 * Set the contents which should be used when the HandlingType.STRING is used.
	 * @return
	 **************************************************************************/
	public void setContent(String content) {
		this.content = content;
		hashCode = null;
	}
	
	/**************************************************************************
	 * Can be used without the content being read.
	 **************************************************************************/
	public int getUniqueID(){
		if(type == HandlingType.STRING ) {
			return (type != null ? type.hashCode() : 0)
				 + (content != null ? content.hashCode() : 0);
		}else {
			return (type != null ? type.hashCode() : 0)
				 + (path != null ? path.hashCode() : 0)
				 + (filename != null ? filename.hashCode(): 0);
		}
	}
	
	public int getHash(){
		if(hashCode == null || !CFW.DB.Config.getConfigAsBoolean(FeatureConfiguration.CONFIG_FILE_CACHING)) {
			// Hard Refresh
			readContents();
			hashCode = (path != null ? path.hashCode() : 0)
					 + (filename != null ? filename.hashCode(): 0)
					 + (content != null ? content.hashCode() : 0);
		}
		return hashCode;
	}
	
	/**************************************************************************
	 * Read the contents of the file specified by this File definition and
	 * returns it as a string.
	 * @return
	 **************************************************************************/
	public String readContents(){

		switch(type) {
			case FILE:			content = CFW.Files.getFileContent(null, path, filename);
								break;
				
			case JAR_RESOURCE: 	content = CFW.Files.readPackageResource(path, filename);
								break;
				
			case STRING: 		
								break;
				
			default: 			content = "";
							break;
							
		}

		return content;
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
