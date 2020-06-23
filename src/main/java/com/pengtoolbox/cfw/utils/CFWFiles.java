package com.pengtoolbox.cfw.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWFiles {


	private static final HashMap<String,String> permanentStringFileCache = new HashMap<String,String>();
	private static final HashMap<String,byte[]> permanentByteFileCache = new HashMap<String,byte[]>();
	
	/** Only packages in this list can be accessed with readPackageResource*().*/
	private static final ArrayList<String> allowedPackages = new ArrayList<String>();
	
	public static Logger logger = CFWLog.getLogger(CFWFiles.class.getName());
	
	static String[] cachedFiles = new String[15];
	static int fileCounter = 0;
	
	/***********************************************************************
	 * Returns the file content of the given file path as a string.
	 * If it fails to read the file it will handle the exception and
	 * will add an alert to the given request.
	 * A file once loaded will 
	 * 
	 * @param request the request that is currently handled
	 * @param path the path 
	 * @param filename the name of the file 
	 * 
	 * @return String content of the file or null if an exception occurred.
	 * 
	 ***********************************************************************/
	public static String getFileContent(HttpServletRequest request, String path, String filename){
		return getFileContent(request, path + "/" + filename);
	}
	/***********************************************************************
	 * Returns the file content of the given file path as a string.
	 * If it fails to read the file it will handle the exception and
	 * will add an alert to the given request.
	 * A file once loaded will 
	 * 
	 * @param request the request that is currently handled
	 * @param path the path 
	 * 
	 * @return String content of the file or null if an exception occurred.
	 * 
	 ***********************************************************************/
	public static String getFileContent(HttpServletRequest request, String path){
		CFWLog omlogger = new CFWLog(logger).method("getFileContent");
		
		if( CFW.DB.Config.getConfigAsBoolean(Configuration.FILE_CACHING) && CFWFiles.permanentStringFileCache.containsKey(path)){
			omlogger.finest("Read file content from cache");
			return CFWFiles.permanentStringFileCache.get(path);
		}else{
			omlogger.finest("Read from disk into cache");
			
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path),"utf-8"));
				
				StringBuffer contentBuffer = new StringBuffer();
				String line;
				
				while( (line = reader.readLine()) != null) {
					contentBuffer.append(line);
					contentBuffer.append("\n");
				}

				String content = contentBuffer.toString();
				CFWFiles.permanentStringFileCache.put(path, content);
				
				// remove UTF-8 byte order mark if present
				content = content.replace("\uFEFF", "");
				
				return content;
				
			} catch (IOException e) {
				//TODO: Localize message
				new CFWLog(logger)
					.method("getFileContent")
					.severe("Could not read file: "+path, e);
				
				return null;
			}
			
		}
	}

	
	/***********************************************************************
	 * Write a string to a file.
	 * 
	 * 
	 * @param request the request that is currently handled
	 * @param path the path 
	 * @param content to be written
	 *   
	 * @return String content of the file or null if an exception occurred.
	 * 
	 ***********************************************************************/
	public static void writeFileContent(HttpServletRequest request, String path, String content){
		
		try{
			Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			
			
		} catch (IOException e) {
			//TODO: Localize message
			new CFWLog(logger)
				.method("writeFileContent")
				.severe("Could not write file: "+path, e);
		}
			
	}
	
	/*************************************************************
	 * Add a package to the allowed packages that can be accessed
	 * by the readPackageResource*() methods.
	 * 
	 * @param packageName the name of the package e.g. "com.example.resources"
	 *************************************************************/
	public static void addAllowedPackage(String packageName) {
		allowedPackages.add(packageName);
	}
	
	/*************************************************************
	 * Remove a package from the allowed packages that can be accessed
	 * by the readPackageResource*() methods.
	 * 
	 * @param packageName the name of the package e.g. "com.example.resources"
	 *************************************************************/
	public static void removeAllowedPackage(String packageName) {
		allowedPackages.remove(packageName);
	}
	
	/*************************************************************
	 * Check if the package resource is allowed to be accessed.
	 * 
	 * @param packagePath the name of the package e.g. "com.example.resources"
	 *************************************************************/
	public static boolean isAllowedRecource(String packagePath) {
		
		boolean isAllowed = false;
		for(String allowed : allowedPackages) {
			if (packagePath.startsWith(allowed)) {
				isAllowed = true;
				break;
			}
		}	
		return isAllowed;
	}
	
	/*************************************************************
	 * Read a resource from the package.
	 * @param packageName e.g. "com.pengtoolbox.cfw.resources.js.bootstrap.js"
	 * @return content as string or null if not found or not accessible.
	 *************************************************************/
	public static String readPackageResource(String packageName, String filename) {
		String fileContent = null;
		
		if(isAllowedRecource(packageName)) {
			
			packageName = packageName.replaceAll("\\.", "/");
			String resourcePath = packageName + "/" + filename;
			
			if( CFW.DB.Config.getConfigAsBoolean(Configuration.FILE_CACHING) && CFWFiles.permanentStringFileCache.containsKey(resourcePath)){
				new CFWLog(logger).finest("Read package resource content from cache");
				return CFWFiles.permanentStringFileCache.get(resourcePath);
			}else{
				
				try(InputStream in = CFWFiles.class.getClassLoader().getResourceAsStream(resourcePath)){
					fileContent = readContentsFromInputStream(in);
					CFWFiles.permanentStringFileCache.put(resourcePath, fileContent);
				} catch (IOException e) {
					new CFWLog(logger)
					.method("readPackageResource")
					.severe("Error while reading resource from package: "+resourcePath, e);
				}

			}
		}else {
			new CFWLog(logger).severe("Not allowed to read resource from package: "+packageName);
		}
		return fileContent;

	}
	
	/*************************************************************
	 * Read a resource from the package.
	 * @param path
	 * @return content as string or null if not found or not accessible.
	 *************************************************************/
	public static byte[] readPackageResourceAsBytes(String packageName, String filename) {
		
		byte[] fileContent = null;
		
		if(isAllowedRecource(packageName)) {
			
			packageName = packageName.replaceAll("\\.", "/");
			String resourcePath = packageName + "/" + filename;
			
			if( CFW.DB.Config.getConfigAsBoolean(Configuration.FILE_CACHING) && CFWFiles.permanentByteFileCache.containsKey(resourcePath)){
				new CFWLog(logger).finest("Read package resource content from cache");
				return CFWFiles.permanentByteFileCache.get(resourcePath);
			}else{
				
				InputStream in = CFWFiles.class.getClassLoader().getResourceAsStream(resourcePath);
				fileContent = readBytesFromInputStream(in);
				CFWFiles.permanentByteFileCache.put(resourcePath, fileContent);
			}
		}else {
			new CFWLog(logger).severe("Not allowed to read resource from package: "+packageName);
		}
		
		return fileContent;

	}

	/*************************************************************
	 * 
	 * @param path
	 * @return content as string or null if not found.
	 *************************************************************/
	public static String readContentsFromInputStream(InputStream inputStream) {
		
		if(inputStream == null) {
			return null;
		}
		
		BufferedReader reader = null;
		String line = "";
		StringBuffer buffer = new StringBuffer();
		
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
	
			while( (line = reader.readLine()) != null) {
				buffer.append(line).append("\n");
				//line = reader.readLine();
			}
			 
		} catch (IOException e) {
			new CFWLog(logger).log(Level.SEVERE, "IOException: ", e);
			e.printStackTrace();
		}finally {
			try {
				if(reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				new CFWLog(logger).log(Level.SEVERE, "IOException", e);
				e.printStackTrace();
			}
		}
		
		String result = buffer.toString();
	
		// remove UTF-8 byte order mark if present
		result = result.replace("\uFEFF", "");
		
		return result;
	}

	/*************************************************************
	 * 
	 * @param path
	 * @return content as string or null if not found.
	 *************************************************************/
	public static byte[] readBytesFromInputStream(InputStream inputStream) {
		
		if(inputStream == null) {
			return null;
		}
		
		InputStreamReader reader = null;
		StringBuffer stringBuffer = new StringBuffer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try {
			byte[] buffer = new byte[1];
			//DataInputStream dis = new DataInputStream(inputStream);
			
			for (int nChunk = inputStream.read(buffer); nChunk!=-1; nChunk = inputStream.read(buffer))
			{
				stringBuffer.append(buffer);
				os.write(buffer);
			} 
			 
		} catch (IOException e) {
			new CFWLog(logger).log(Level.SEVERE, "IOException: ", e);
			e.printStackTrace();
		}finally {
			try {
				if(reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				new CFWLog(logger).log(Level.SEVERE, "IOException", e);
				e.printStackTrace();
			}
		}
			
		return os.toByteArray();
	}


	
	/***********************************************************************
	 * Caches a file permanently in memory.
	 * @param filename the name of the file
	 * @param fileContent a String representation of the file.
	 * @return nothing
	 ***********************************************************************/
	public static void permanentlyCacheFile(String filename, String fileContent) {
		CFWFiles.permanentStringFileCache.put(filename, fileContent);
	}
	
	/***********************************************************************
	 * Check if the files is in the cache.
	 * @param filename the name of the file
	 * @return true or false
	 ***********************************************************************/
	public static boolean isFilePermanentlyCached(String filename) {
		return CFWFiles.permanentStringFileCache.containsKey(filename);
	}
	
	/***********************************************************************
	 * Retrieve a cached file by it's index.
	 * @param index the index of the file to be retrieved
	 ***********************************************************************/
	public static String getPermanentlyCachedFile(String filename) {
		return CFWFiles.permanentStringFileCache.get(filename);
	}
	
	/***********************************************************************
	 * Check if the files exists.
	 * @param filename the name of the file
	 * @return true or false
	 ***********************************************************************/
	public static boolean isFile(String path, String filename) {
		
		return isFile(path+"/"+filename);
	}
	/***********************************************************************
	 * Check if the files exists.
	 * @param filename the name of the file
	 * @return true or false
	 ***********************************************************************/
	public static boolean isFile(String filePath) {
		File file = new File(filePath);
		
		return file.isFile();
	}
	
}
