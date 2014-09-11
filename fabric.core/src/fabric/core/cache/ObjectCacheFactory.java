/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.cache;

import java.util.concurrent.ConcurrentHashMap;

import fabric.core.cache.impl.ObjectCache;

/**
 *
 */

public class ObjectCacheFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	private static ConcurrentHashMap<Object, Object> objectCacheInstances = new ConcurrentHashMap();

	public static IObjectCache getInstance(Object cacheName) {

		IObjectCache cache = null;

		if (objectCacheInstances.get(cacheName) == null) {
			objectCacheInstances.putIfAbsent(cacheName, new ObjectCache());
		}

		cache = (IObjectCache) objectCacheInstances.get(cacheName);

		return cache;
	}
}
