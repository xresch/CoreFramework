package com.xresch.cfw.caching;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

import io.prometheus.client.guava.cache.CacheMetricsCollector;

@SuppressWarnings("rawtypes")
public class CFWCacheManagement {
	
	private static final Logger logger = CFWLog.getLogger(CFWCacheManagement.class.getName());
	
    private static final CacheMetricsCollector cacheMetricsCollector = new CacheMetricsCollector().register();
	private static final SortedMap<String, Cache> cacheMap = Collections.synchronizedSortedMap(new TreeMap<>());
	
	/************************************************************************
	 * 
	 ************************************************************************/
	@SuppressWarnings("unchecked")
	public static <K1 extends Object, V1 extends Object> Cache<K1, V1> addCache(
			String cacheName,  CacheBuilder cacheBuilder) {
		
		if(cacheMap.containsKey(cacheName)) {
			new CFWLog(logger)
				.severe("Failed to add cache. The cache name '"+cacheName+"' is already used.", new IllegalArgumentException());
			return null;
		}
		
		Cache<K1, V1> cache = cacheBuilder
				.recordStats()
				.build();
		
		cacheMap.put(cacheName, cache);
		cacheMetricsCollector.addCache(cacheName, cache);
		
		return cache;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	@SuppressWarnings("unchecked")
	public static <K1 extends Object, V1 extends Object> Cache<K1, V1> removeCache(
			String cacheName) {
		
		return cacheMap.remove(cacheName);

	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static JsonArray getCacheStatisticsAsJSON() {
		
		JsonArray array = new JsonArray();
		
		for(Entry<String, Cache> entry : cacheMap.entrySet()) {
			
			String name = entry.getKey();
			Cache cache = entry.getValue();
			CacheStats stats = cache.stats();
			JsonObject object = new JsonObject();

			long hitCount = stats.hitCount();
			double loadTimeAvgMillis = stats.averageLoadPenalty() / 1000000;
			
			object.addProperty("name", name);
			object.addProperty("estimated_entries", cache.size());
			object.addProperty("requests", stats.requestCount());
			object.addProperty("hit_count", hitCount);
			object.addProperty("hit_rate", stats.hitRate());
			object.addProperty("miss_count", stats.missCount());
			object.addProperty("miss_rate", stats.missRate());
			object.addProperty("eviction_count", stats.evictionCount());
			object.addProperty("load_time_avg", loadTimeAvgMillis);
			object.addProperty("load_time_sum", stats.totalLoadTime() / 1000000);
			object.addProperty("load_time_saved", loadTimeAvgMillis * hitCount);
			
			array.add(object);
		}
		
		return array;
		
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static String getCacheDetailsAsJSON(String cacheName) {
		
		Cache<Object, Object> cache = cacheMap.get(cacheName);

		Set<Entry<Object, Object>> entries = cache.asMap().entrySet();
		
		JsonObject cacheDetails = new JsonObject();
		cacheDetails.addProperty("name", cacheName);
		cacheDetails.addProperty("entry_count", entries.size());

		if(entries.size() > 0) {
			cacheDetails.addProperty("clazz", ((Entry<Object, Object>)entries.toArray()[0]).getValue().getClass().getName());
		}
		
		JsonArray array = new JsonArray();
		int i = 0;
		for(Entry<Object, Object> entry : entries ) {
			
			JsonObject entryDetails = new JsonObject();
			entryDetails.addProperty("key", entry.getKey().toString());
			Object value = entry.getValue();
			String finalValue = "";
			if(String.class.isAssignableFrom(value.getClass())) {
				finalValue = value.toString();
			}else {
				finalValue = CFW.JSON.toJSON(value);
			}
			if(finalValue.length() > 500) {
				finalValue = finalValue.substring(0, 500)+"\n ... [truncated]";
			}
			
			entryDetails.addProperty("value", finalValue);

			array.add(entryDetails);
			i++;
			if(i >= 100) {
				break;
			}
			
		}

		cacheDetails.add("entries", array);

		return cacheDetails.toString();
		
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static JsonArray clearAllCaches() {
		
		JsonArray array = new JsonArray();
		
		for(Entry<String, Cache> entry : cacheMap.entrySet()) {
			Cache cache = entry.getValue();
			cache.invalidateAll();
			cache.cleanUp();
		}
		
		return array;
		
	}
	

}
