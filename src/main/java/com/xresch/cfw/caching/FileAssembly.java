package com.xresch.cfw.caching;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FileAssembly {
	
	private static final Logger logger = CFWLog.getLogger(FileAssembly.class.getName());
	
	/** Static field to store the assembled results by their file names. */
	private static final Cache<String, FileAssembly> ASSEMBLY_CACHE = CFW.Caching.addCache("CFW Assembly", 
			CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(1000)
				.expireAfterAccess(24, TimeUnit.HOURS)
		);
	
	// only add a file once
	private LinkedHashMap<Integer, FileDefinition> fileMap = new LinkedHashMap<>();
	
	private String inputName = "";
	private String assemblyName = "";
	private String assemblyServletPath = "";
	private String filetype = "";
	private String contentType = "";
	private String assemblyContent = "";
	
	private int lastEtag = -1;
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
		
		fileMap.put(fileDef.getUniqueID(), fileDef);
		return this;
	}
	
	/***********************************************************************
	 * Add a file definition to the Assembly.
	 * @param type the handling type of the file.
	 * @param filetype the file type e.g. "js", "css"
	 ***********************************************************************/
	public FileAssembly addFile(FileDefinition definition) {
		fileMap.put(definition.getUniqueID(), definition);
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
				fileMap.put(definition.getUniqueID(), definition);
			}
		}
		return this;
	}
	public FileAssembly addFileContent(String content) {
		FileDefinition fileDef = new FileDefinition(content);
		fileMap.put(fileDef.getUniqueID(), fileDef);
		return this;
	}
	/***********************************************************************
	 * Check if the assembly has any files added.
	 * @param data.type the 
	 * @param filetype the file type e.g. "js", "css"
	 ***********************************************************************/
	public boolean hasFiles() {
		return (fileMap.size() > 0);
	}
	
	
	/***********************************************************************
	 * Assembles and stores the file in the permanent cache of 
	 * {@link com.xresch.cfw.utils.CFWFiles FileUtils}.
	 * 
	 * @return the filename that can be used for retrieving the file content.
	 ***********************************************************************/
	public FileAssembly assembleAndCache() {
		
		//--------------------------------
		// Initialize
		createEtagNameAndPath();
		
		if(CFW.DB.Config.getConfigAsBoolean(FeatureConfiguration.CONFIG_FILE_CACHING)) {
			try {
				// Do this to get proper cache statistics.
				FileAssembly assembly = this;
				ASSEMBLY_CACHE.get(assemblyName, new Callable<FileAssembly>() {

					@Override
					public FileAssembly call() throws Exception {
						assembly.assembleContent();
						return assembly;
					}
					
				});
			} catch (ExecutionException e) {
				new CFWLog(logger).severe("Error occured while handling FileAssembly caching.", e);
			}

		}else {
			// always assemble and overwrite cache if caching is disabled
			this.assembleContent();
			ASSEMBLY_CACHE.put(assemblyName, this);
		}
		
		return this;
	}
	
	/***********************************************************************
	 * Assembles this file assembly. 
	 * 
	 * @return the filename that can be used for retrieving the file content.
	 ***********************************************************************/
	private FileAssembly assembleContent() {
		
		//----------------------------------
		// Reload if Etag has changed.
		if(lastEtag != etag) {
			StringBuilder concatenatedFile = new StringBuilder();
			for(FileDefinition fileDef : fileMap.values()) {
	
				String content = fileDef.readContents();
				
				if(content != null  && !content.isEmpty()) {
					concatenatedFile.append(content).append("\n");
				}
			}
			
			assemblyContent = concatenatedFile.toString();
		}
		
		return this;
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private void createEtagNameAndPath() {

		lastEtag = etag;
		etag = 0;
		for(FileDefinition fileDef : fileMap.values()) {
			etag += CFW.Random.STARTUP_RANDOM_INT + fileDef.getHash();
		}
				
		assemblyName = inputName + "_" + etag + "." + filetype;
		assemblyServletPath = "/cfw/assembly?name="+CFW.HTTP.encode(assemblyName);

	}
	
	
	/***********************************************************************
	 * Check if the cache contains the specific assembly.
	 * @return true or false
	 ***********************************************************************/
	public static boolean isAssemblyCached(String assemblyName) {
		return ASSEMBLY_CACHE.asMap().containsKey(assemblyName);
	}
	
	/***********************************************************************
	 * Check if the cache contains the specific assembly.
	 * @return FileAssembler instance or null
	 ***********************************************************************/
	public static FileAssembly getAssembly(String assemblyName) {
		return ASSEMBLY_CACHE.getIfPresent(assemblyName);
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
