package com.xresch.cfw.features.core;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.AbstractResponse;
import com.xresch.cfw.utils.LinkedProperties;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWLocalization {
	
	private static final Logger logger = CFWLog.getLogger(CFWLocalization.class.getName());
	
	public static final String LOCALE_LB  = "{!";
	public static final String LOCALE_RB = "!}";
	
	public static final int LOCALE_LB_SIZE  = LOCALE_LB.length();
	public static final int LOCALE_RB_SIZE = LOCALE_RB.length();
	
	private static int localeFilesID = 0;
	
	// key consists of {language}+{contextPath}+{localeFilesID}
	private static Cache<String, Properties> languageCache = CFW.Caching.addCache("CFW Locales", 
		CacheBuilder.newBuilder()
			.initialCapacity(100)
			.maximumSize(2000)
			.expireAfterAccess(24, TimeUnit.HOURS)
	);
	
	private static LinkedHashMap<String, FileDefinition> localeFiles = new LinkedHashMap<String, FileDefinition>();
		
	/******************************************************************************************
	 * Return a localized value or the default value
	 ******************************************************************************************/
	public static String getLocalized(String key, String defaultValue) {
		
		String value = getLanguagePack(getLocalesForRequest(), null).getProperty(key);
		
		if(value != null) {
			return value;
		}else {
			return defaultValue;
		}
	}
	
	/******************************************************************************************
	 * Return a localized value or the default value.
	 ******************************************************************************************/
	public static String getLocalized(String key, String defaultValue, Object placeholders) {
		
		String value = getLanguagePack(getLocalesForRequest(), null).getProperty(key);
		
		if(value != null) {
			return MessageFormat.format(value, placeholders);
		}else {
			return MessageFormat.format(defaultValue, placeholders);
		}
	}
	
	/******************************************************************************************
	 * 
	 ******************************************************************************************/
	public static Properties getAllProperties() {
		return getLanguagePack(getLocalesForRequest(), null);
	}
	
	/******************************************************************************************
	 * 
	 * @param locale 
	 * @param contextPath the absolute path of the context the language pack should be loaded. 
	 *        e.g. "/app/yourservlet"
	 * @throws IOException
	 ******************************************************************************************/
	public static void registerLocaleFile(Locale locale, String contextPath, FileDefinition propertiesFileDefinition) {
		String id = locale.getLanguage()+contextPath+"-"+localeFilesID;
		localeFiles.put(id.toLowerCase(), propertiesFileDefinition);
		localeFilesID++;
		
	}
	
	/******************************************************************************************
	 * 
	 ******************************************************************************************/
	public static Locale[] getLocalesForRequest() {
		
		ArrayList<Locale> localeArray = new ArrayList<Locale>();
		
		// fall back to english
		localeArray.add(Locale.ENGLISH);
		
		String configLanguage = CFW.DB.Config.getConfigAsString(FeatureConfiguration.CONFIG_LANGUAGE);
		
		if(configLanguage != null) {
			Locale defaultLanguage = Locale.forLanguageTag(configLanguage.toLowerCase());
			if(defaultLanguage != null) {
				localeArray.add(defaultLanguage);
			}
		}

		HttpServletRequest request = CFW.Context.Request.getRequest();
		if(request != null) {
			Locale browserLanguage = request.getLocale();
			if(browserLanguage != null) {
				localeArray.add(browserLanguage);
			}
		}
		
		return localeArray.toArray(new Locale[localeArray.size()]);
	}
	
	/******************************************************************************************
	 * 
	 ******************************************************************************************/
	public static String getLocaleIdentifierForRequest() {
		return getLocaleIdentifier(getLocalesForRequest());
	}
	/******************************************************************************************
	 * 
	 ******************************************************************************************/
	public static String getLocaleIdentifier(Locale[] locales) {
		StringBuilder builder = new StringBuilder();
		
		Locale lastlocale = null;
		for(Locale locale : locales) {
			
			//----------------------------
			// Skip reoccuring language
			if(lastlocale != null && locale.getLanguage().equals(lastlocale.getLanguage()) ) {
				lastlocale = locale;
				continue;
			}else {
				lastlocale = locale;
			}
			
			builder.append(locale.getLanguage()).append("_"); 
		}
		
		HttpServletRequest request = CFW.Context.Request.getRequest();
		if(request != null) {
			builder.append(request.getRequestURI());
		}else {
			builder.deleteCharAt(builder.length()-1);
		}
		
		return builder.toString().toLowerCase();
	}
	
	/******************************************************************************************
	 * 
	 ******************************************************************************************/
	public static Properties getLanguagePackByIdentifier(String localeIdentifier) {
		return languageCache.getIfPresent(localeIdentifier);
	}
	/******************************************************************************************
	 * 
	 * @param request
	 * @param response
	 * @return 
	 * @throws IOException
	 ******************************************************************************************/
	public static Properties getLanguagePackForRequest() {
		String requestURI = "";
		if(CFW.Context.Request.getRequest() != null) {
			requestURI = CFW.Context.Request.getRequest().getRequestURI();
		}
		return getLanguagePack(getLocalesForRequest(), requestURI);
	}
	
	/******************************************************************************************
	 * 
	 * @param locales, later will override earliers
	 * @param requestURI of the request, provide to get everything for the selected locales
	 * @return 
	 * @throws IOException
	 ******************************************************************************************/
	public static Properties getLanguagePack(Locale[] locales, String requestURI) {
		
		//------------------------------
		// Initialize
		String cacheID = CFW.Localization.getLocaleIdentifier(locales);
		Properties languagePack = new Properties();
		//------------------------------
		// Check is Cached
		if (!CFW.DB.Config.getConfigAsBoolean(FeatureConfiguration.CONFIG_FILE_CACHING)) {
			languagePack =  loadLanguagePack(locales, requestURI);
			languageCache.put(cacheID, languagePack);
		}else {
			
			try {
				languagePack = languageCache.get(cacheID, new Callable<Properties>() {
					@Override
					public Properties call() throws Exception {
						return CFWLocalization.loadLanguagePack(locales, requestURI);
					}
					
				});
			} catch (ExecutionException e) {
				new CFWLog(logger).severe("Error loading language pack from Cache.", e);
			}
		}
		
		return languagePack;
	}
	
	/******************************************************************************************
	 * 
	 * @param locales, later will override earliers
	 * @param requestURI of the request, provide to get everything for the selected locales
	 * @return 
	 * @throws IOException
	 ******************************************************************************************/
	private static Properties loadLanguagePack(Locale[] locales, String requestURI) {
		
		LinkedProperties mergedPorperties = new LinkedProperties();
		
		Locale lastlocale = null;
		for(Locale locale : locales) {
			
			//----------------------------
			// Skip reoccuring language
			if(lastlocale != null && locale.getLanguage().equals(lastlocale.getLanguage()) ) {
				lastlocale = locale;
				continue;
			}else {
				lastlocale = locale;
			}
			
			String language = locale.getLanguage().toLowerCase(); 
			for(Entry<String, FileDefinition> entry : localeFiles.entrySet()) {
				String entryID = entry.getKey();

				if( (language+requestURI).startsWith(entryID.substring(0, entryID.lastIndexOf('-'))) 
				|| (requestURI == null && entryID.startsWith(language))
				) {
					
					FileDefinition def = entry.getValue();
					Properties currentProps = new Properties();
					String propertiesString = def.readContents();
					
					if(propertiesString != null) {
						try (StringReader reader = new StringReader(propertiesString);) {
							
							currentProps.load( reader );
							mergedPorperties.putAll(currentProps);
							
						} catch (IOException e) {
							new CFWLog(logger)
								.severe("Error while reading language pack.", e);
						}
					}
				}
			}
		}

		return mergedPorperties;
	}
	
	/******************************************************************************************
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 ******************************************************************************************/
	public static String localizeString(HttpServletRequest request, String toLocalize) throws IOException{
		return localizeString(request, new StringBuilder(toLocalize));
	}
	/******************************************************************************************
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 ******************************************************************************************/
	public static String localizeString(HttpServletRequest request, StringBuilder sb) throws IOException{
		
		AbstractResponse template = CFW.Context.Request.getResponse();
		
		Properties langMap;
		if(template.useGlobaleLocale() == false) {
			langMap = getLanguagePackForRequest();
		}else {
			langMap = getAllProperties();
		}
				
		int fromIndex = 0;
		int leftIndex = 0;
		int rightIndex = 0;
		int length = sb.length();
		
		while(fromIndex < length && leftIndex < length){
		
			leftIndex = sb.indexOf(CFWLocalization.LOCALE_LB, fromIndex);
			
			if(leftIndex != -1){
				rightIndex = sb.indexOf(CFWLocalization.LOCALE_RB, leftIndex);
				
				if(rightIndex != -1 && (leftIndex+CFWLocalization.LOCALE_LB_SIZE) < rightIndex){

					String propertyName = sb.substring(leftIndex+CFWLocalization.LOCALE_LB_SIZE, rightIndex);
					if(langMap != null && langMap.containsKey(propertyName)){
						sb.replace(leftIndex, rightIndex+CFWLocalization.LOCALE_RB_SIZE, langMap.getProperty(propertyName));
					}
					//start again from leftIndex
					fromIndex = leftIndex+1;
					
				}else{
					//TODO: Localize message
					new CFWLog(logger)
						.finest("Localization Parameter was missing the right bound");
				
					break;
				}
				
			}else{
				//no more stuff found to replace
				break;
			}
		}
		
		return sb.toString();
	}
		
	
	/******************************************************************************************
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 ******************************************************************************************/
	public static void writeLocalized(HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		AbstractResponse template = CFW.Context.Request.getResponse();
		
		if(template != null){
			
			String localized = localizeString(request, template.buildResponse());
			response.getWriter().write(localized);
		}
	}
}
