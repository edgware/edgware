/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.cache.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fabric.core.cache.IObjectCache;

/**
 *
 */

public class ObjectCache implements IObjectCache{

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	
	private ConcurrentHashMap<Object, Object[]> objectCache = null;
	
	/** Default constructor */
	public ObjectCache() {
		objectCache = new ConcurrentHashMap<Object, Object[]>();
	}
	
	/**
	 * Constructor which sets the cache to an initial size.
	 * 
	 * @param cacheSize - the initial size of the cache.
	 */
	public ObjectCache(int cacheSize) {
		objectCache = new ConcurrentHashMap<Object, Object[]>(cacheSize);
	}
	
    public void put(Object key, Object val) {
        this.put(key, val, null);
    }
    
    @Override
    public void put(Object key, Object value, Long time_to_live) {
   	if(key == null) throw new RuntimeException("Invaild Key");
    	time_to_live = time_to_live != null ? (time_to_live + System.currentTimeMillis()) : -1;

     	objectCache.put(key, new Object[]{time_to_live, value});

    }

	@Override
	public void clear() {
		objectCache.clear();
		
	}

	@Override
	public Object get(Object key) {
		Object[] cacheEntry = null;
		
		if ( (cacheEntry = objectCache.get(key)) != null ) {
			if ( (Long) cacheEntry[0] >=0 && ((Long) cacheEntry[0] - System.currentTimeMillis()) <= 0 ){
				remove(key);
				cacheEntry[1] = null;
			}
		}
		return (cacheEntry != null ? cacheEntry[1]:null);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getAll() {
		return this.getAll(objectCache.keySet());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map getAll(Collection keyCollection) {
       Map allCacheEntriest = new HashMap();
        for (Object key : keyCollection) {
			allCacheEntriest.put(key, this.get(key));
        }
        return allCacheEntriest;
	}

	@Override
	public Object getAndRemove(Object key) {
		Object value;
		if ((value = this.get(key)) != null){
			objectCache.remove(key);
		}
		return value;
	}

	@Override
	public boolean remove(Object key) {
		return (objectCache.remove(key) != null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map remove(Collection keyCollection) {
		Map allCacheEntriest = new HashMap();
        for (Object key : keyCollection) {
        	allCacheEntriest.put(key, (objectCache.remove(key) != null ? true : false) );
        }
		return allCacheEntriest;
	}

	@Override
	public int size() {
		return objectCache.size();
	}



}
