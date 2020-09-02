package com.xresch.cfw.caching;

import com.google.common.cache.CacheBuilder;

public class CFWCacheBuilder<K, V>  {

	private CacheBuilder guavaBuilder = CacheBuilder.newBuilder();
	
	public void CFWCacheBuilder() {
		guavaBuilder.build();
//		guavaBuilder.build(CacheLoader loader)
//		guavaBuilder.concurrencyLevel(int concurrencyLevel)
//		guavaBuilder.expireAfterAccess(Duration duration)
//		guavaBuilder.expireAfterAccess(long duration, TimeUnit unit)
//		guavaBuilder.expireAfterWrite(Duration duration)
//		guavaBuilder.expireAfterWrite(long duration, TimeUnit unit)
//		guavaBuilder.initialCapacity(int initialCapacity)
//		guavaBuilder.maximumSize(long maximumSize)
//		guavaBuilder.maximumWeight(maximumWeight)
//		guavaBuilder.
//		guavaBuilder.
//		guavaBuilder.
	}
	
}
