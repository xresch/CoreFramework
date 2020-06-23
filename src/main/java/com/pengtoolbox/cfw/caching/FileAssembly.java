package com.pengtoolbox.cfw.caching;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.config.Configuration;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FileAssembly {
	
	/** Static field to store the assembled results by their file names. */
	private static final LinkedHashMap<String,FileAssembly> assemblyCache = new LinkedHashMap<String, FileAssembly>();
	
	// only add a file once
	private LinkedHashMap<Integer, FileDefinition> fileArray = new LinkedHashMap<Integer, FileDefinition>();
	
	private String inputName = "";
	private String assemblyName = "";
	private String assemblyServletPath = "";
	private String filetype = "";
	private String contentType = "";
	private String assemblyContent = "";
	private int etag = 0;
	
	/***********************************************************************
	 * Constructor for the FileAssembler.
	 * @param name used for building the assembled file name
	 * @param filetype the file type e.g. "js", "css"
	 ***********************************************************************/
	public FileAssembly(String name, String filetype) {
		this.inputName = name;
		this.filetype = filetype;
		this.determineContentType();
	}
	
	/***********************************************************************
	 * Add a file to the Assembly.
	 * @param type the handling type of the file.
	 * @param filetype the file type e.g. "js", "css"
	 ***********************************************************************/
	public FileAssembly addFile(FileDefinition.HandlingType type, String path, String filename) {
		FileDefinition fileDef = new FileDefinition(type, path, filename);
		
		fileArray.put(fileDef.getUniqueID(), fileDef);
		return this;
	}
	
	/***********************************************************************
	 * Add a file definition to the Assembly.
	 * @param type the handling type of the file.
	 * @param filetype the file type e.g. "js", "css"
	 ***********************************************************************/
	public FileAssembly addFile(FileDefinition definition) {
		fileArray.put(definition.getUniqueID(), definition);
		return this;
	}
	/***********************************************************************
	 * Add a file definition to the Assembly.
	 * @param type the handling type of the file.
	 * @param filetype the file type e.g. "js", "css"
	 ***********************************************************************/
	public FileAssembly addAll(ArrayList<FileDefinition> definitionArray) {
		if(definitionArray != null) {
			for (FileDefinition definition : definitionArray) {
				fileArray.put(definition.getUniqueID(), definition);
			}
		}
		return this;
	}
	public FileAssembly addFileContent(String content) {
		FileDefinition fileDef = new FileDefinition(content);
		fileArray.put(fileDef.getUniqueID(), fileDef);
		return this;
	}
	/***********************************************************************
	 * Check if the assembly has any files added.
	 * @param data.type the 
	 * @param filetype the file type e.g. "js", "css"
	 ***********************************************************************/
	public boolean hasFiles() {
		return (fileArray.size() > 0);
	}
	
	
	/***********************************************************************
	 * Assembles and stores the file in the permanent cache of 
	 * {@link com.pengtoolbox.cfw.utils.CFWFiles FileUtils}.
	 * 
	 * @return the filename that can be used for retrieving the file content.
	 ***********************************************************************/
	public FileAssembly assemble() {
		

		//--------------------------------
		// Initialize
		if(!FileAssembly.hasAssembly((assemblyName)) || !CFW.DB.Config.getConfigAsBoolean(Configuration.FILE_CACHING)) {
			
			StringBuffer concatenatedFile = new StringBuffer();
			for(FileDefinition fileDef : fileArray.values()) {
				
				
				String content = fileDef.readContents();
				
				if(content != null  && !content.isEmpty()) {
					concatenatedFile.append(content).append("\n");
				}
			}
			
			assemblyContent = concatenatedFile.toString();
			etag = assemblyContent.hashCode();
						
			assemblyName = inputName + "_" + etag + "." + filetype;
			assemblyServletPath = "/cfw/assembly?name="+URLEncoder.encode(assemblyName);
		}
		
		return this;
	}
	
	/***********************************************************************
	 * Store this instance in the cache.
	 * @return the name of the assembly
	 ***********************************************************************/
	public FileAssembly cache() {
		if(!FileAssembly.hasAssembly((assemblyName)) || !CFW.DB.Config.getConfigAsBoolean(Configuration.FILE_CACHING)) {
			assemblyCache.put(assemblyName, this);
		}
		return this;
	}
	
	/***********************************************************************
	 * Check if the cache contains the specific assembly.
	 * @return true or false
	 ***********************************************************************/
	public static boolean hasAssembly(String assemblyName) {
		return assemblyCache.containsKey(assemblyName);
	}
	
	/***********************************************************************
	 * Check if the cache contains the specific assembly.
	 * @return FileAssembler instance or null
	 ***********************************************************************/
	public static FileAssembly getAssemblyFromCache(String assemblyName) {
		return assemblyCache.get(assemblyName);
	}
	
	/***********************************************************************
	 * Tries to determine the content type of the assembly and returns it 
	 * as a string.
	 * @return string determined content type
	 ***********************************************************************/
	public void determineContentType() {
		
		switch(filetype) {
			case "js":		contentType = "text/javascript";
							break;
			
			case "css":		contentType = "text/css";
							break;
			
			case "html":	contentType = "text/html";
							break;
			
			case "json":	contentType = "application/json";
							break;
			
			case "xml":		contentType = "application/xml";
							break;
			
			default:		contentType = "text/"+filetype;
							break;
		}
	}

	public String getAssemblyName() {
		return assemblyName;
	}

	public String getAssemblyServletPath() {
		return assemblyServletPath;
	}
	
	public String getFiletype() {
		return filetype;
	}
	
	public String getContentType() {
		return contentType;
	}

	public String getAssemblyContent() {
		return assemblyContent;
	}	
	
	public int getEtag() {
		return etag;
	}	
	
}
