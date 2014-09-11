/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.cache;

import java.util.Collection;
import java.util.Map;

/**
 *
 */

public interface IObjectCache {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	/**
	 * Puts a Key,Value pair which will not expire in the cache
	 */
	public void put(Object key, Object value);

	/**
	 * Puts a Key,Value pair with a time-to-live in the cache
	 */
	public void put(Object key, Object value, Long TTL);

	/**
	 * Get an entry from the cache
	 */
	public Object get(Object key);

	/**
	 * Get the entire cache as a Map
	 */
	public Map getAll();

	/**
	 * Get the collection of Keys as a Map
	 */
	public Map getAll(Collection keyCollection);

	/**
	 * Get and remove an entry from the cache
	 */
	public Object getAndRemove(Object key);

	/**
	 * Remove an entry from the cache if it exists
	 */
	public boolean remove(Object key);

	/**
	 * Remove the collection of key/values from the cache if it exists
	 */
	public Map remove(Collection keyCollection);

	/**
	 * Returns the cache size
	 */
	public int size();

	/**
	 * Clear the cache
	 */
	public void clear();

}
