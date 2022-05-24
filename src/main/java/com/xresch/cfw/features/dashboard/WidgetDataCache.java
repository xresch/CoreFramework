package com.xresch.cfw.features.dashboard;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.config.ConfigChangeListener;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetDataCache {
	
	public enum WidgetDataCachePolicy {
		  /* Ignore timeframepreset and cache always */
		  ALWAYS
		  /* Cache based on timeframepreset value */
		, TIMEPRESET_BASED
		  /* Do not cache and always load new data */
		, OFF
	}
	
	/** Cache widget data responses to reduce number of API calls. */
	public static final Cache<String, JSONResponse> CACHE = 
		CFW.Caching.addCache(
			"Widget Data Cache[1min]", 
			CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(2000)
				.expireAfterWrite(1, TimeUnit.MINUTES)
		);

	/*************************************************************************
	 * 
	 *************************************************************************/
	public static void initialize() {
		
		//-------------------------------
		// Create Change Listener listening
		// to all Config changes		
		ContextSettingsChangeListener listener = new ContextSettingsChangeListener() {
			
			@Override
			public void onChange(AbstractContextSettings changedSetting, boolean isNew) {
				CACHE.invalidateAll();
			}

			@Override
			public void onDelete(AbstractContextSettings typeSettings) {
				CACHE.invalidateAll();
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
	}
}
