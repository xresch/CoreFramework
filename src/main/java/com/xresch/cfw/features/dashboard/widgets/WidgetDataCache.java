package com.xresch.cfw.features.dashboard.widgets;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.xrutils.data.ByteSize;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetDataCache {
	
	//============================================================
	// Enum Cache Policy
	//============================================================
	public enum WidgetDataCachePolicy {
		  /* Ignore timeframepreset and cache always */
		  ALWAYS
		  /* Cache based on timepreset value */
		, TIME_BASED
		  /* Do not cache and always load new data */
		, OFF
	}
	
	//============================================================
	// Enum Widget Caches
	//============================================================
	public enum WidgetDataCaches {
		  ONE_MIN(1, 500,  "1 Minute")
		, FIVE_MIN(5, 100, "5 Minutes")
		, FIFTEEN_MIN(15, 100, "15 Minutes")
		, SIXTY_MIN(60, 100, "60 Minutes")
		, TWELVE_HOURS(12*60, 100, "12 hours")
		;
		
		private int minutes = 0;
		private String label;
		private final Cache<String, JSONResponse> cache;
		
		private static LinkedHashMap<Integer, String> options = new LinkedHashMap<>();
		private static LinkedHashMap<Integer, WidgetDataCaches> minutesCacheMap = new LinkedHashMap<>();

		static {
			for(WidgetDataCaches type : WidgetDataCaches.values()) { 
				minutesCacheMap.put(type.minutes, type); 
				options.put(type.minutes, type.label); 
			}
		}

		WidgetDataCaches(int minutes, int maxWeightMB, String label){
			this.minutes = minutes;
			this.label = label;
			
			cache = CFW.Caching.addCache(
					"CFW Widget Data Cache["+minutes+"min]", 
					CacheBuilder.newBuilder()
						.initialCapacity(10)
						.maximumWeight(maxWeightMB * 1000)
						.weigher(new Weigher<String, JSONResponse>() {

							@Override
							public int weigh(String key, JSONResponse value) {
								
								if(value == null) { return 0; }
								
								int sizeKB = (int)ByteSize.KB.convertBytes( 1L * value.getEstimatedSizeChars());
								return sizeKB;
							}
						})
						.expireAfterWrite( (minutes * 60)-1, TimeUnit.SECONDS)
				);
		}
		
		public Cache<String, JSONResponse> getCache() {
			return cache;
		}
		
		
		public void invalidateAll() {
			cache.invalidateAll();
		}
		
		public void invalidate(String name) {
			cache.invalidate(name);
		}
		
		public static void invalidateAllEvery() {
			for(WidgetDataCaches type : WidgetDataCaches.values()) { 
				type.invalidateAll();
			}
		}
		
		public static Cache<String, JSONResponse> getCache(Integer minutes) {
			
			if(minutes != null) {
				return minutesCacheMap.get(minutes).getCache();
			}
			
			return ONE_MIN.getCache();
		}
		
		
		public static HashMap<Integer, String> getSelectOptions(){
			LinkedHashMap<Integer, String> optionsClone = new LinkedHashMap<>();
			
			optionsClone.putAll(options);
			return optionsClone;
		}
		
		
	}

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
				WidgetDataCaches.invalidateAllEvery();
			}

			@Override
			public void onDeleteOrDeactivate(AbstractContextSettings typeSettings) {
				WidgetDataCaches.invalidateAllEvery();
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
	}
}
