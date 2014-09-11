/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

/**
 * Interface for classes providing access to Fabric configuration settings.
 * <p>
 * All configuration settings are name/value pairs, with optional insert values, following the syntax of Java resource
 * bundles.
 * </p>
 */
public interface IConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Interface methods
	 */

	/**
	 * Answers the value of the specified configuration property.
	 * <p>
	 * This method will check local sources only (and not the Fabric Registry).
	 * </p>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @return the configuration property value, or <code>null</code> if it has not been set.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String)
	 */
	public String getLocalProperty(String key);

	/**
	 * Answers the value of the specified configuration property.
	 * <p>
	 * This method will check local sources only (and not the Fabric Registry).
	 * </p>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @param propertyDefault
	 *            the default value for the configuration property.
	 * 
	 * @return the configuration property value, or the default if it has not been set.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String, String)
	 */
	public String getLocalProperty(String key, String propertyDefault);

	/**
	 * Answers the value of the specified configuration property.
	 * <p>
	 * This method will check local sources only (and not the Fabric Registry).
	 * </p>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @param inserts
	 *            configuration property insert values.
	 * 
	 * @return the configuration property value, or <code>null</code> if it has not been set.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String, Object...)
	 */
	public String getLocalProperty(String key, Object... inserts);

	/**
	 * Answers the value of the specified configuration property.
	 * <p>
	 * This method will check local sources only (and not the Fabric Registry).
	 * </p>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @param propertyDefault
	 *            the default value for the configuration property.
	 * 
	 * @param inserts
	 *            configuration property insert values.
	 * 
	 * @return the configuration property value, or the default if it has not been set.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String, String, Object...)
	 */
	public String getLocalProperty(String key, String propertyDefault, Object... inserts);

	/**
	 * Answers the value of the specified configuration property.
	 * <p>
	 * This method will check local sources first, followed by the Fabric Registry if no match has been found.
	 * </p>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @return the configuration property value, or <code>null</code> if it has not been set.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String)
	 */
	public String getProperty(String key);

	/**
	 * Answers the value of the specified configuration property.
	 * <p>
	 * This method will check local sources first, followed by the Fabric Registry if no match has been found.
	 * </p>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @param propertyDefault
	 *            the default value for the configuration property.
	 * 
	 * @return the configuration property value, or the default if it has not been set.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String, String)
	 */
	public String getProperty(String key, String propertyDefault);

	/**
	 * Answers the value of the specified configuration property.
	 * <p>
	 * This method will check local sources first, followed by the Fabric Registry if no match has been found.
	 * </p>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @param inserts
	 *            configuration property insert values.
	 * 
	 * @return the configuration property value, or <code>null</code> if it has not been set.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String, Object...)
	 */
	public String getProperty(String key, Object... inserts);

	/**
	 * Answers the value of the specified configuration property.
	 * <p>
	 * This method will check local sources first, followed by the Fabric Registry if no match has been found.
	 * </p>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @param propertyDefault
	 *            the default value for the configuration property.
	 * 
	 * @param inserts
	 *            configuration property insert values.
	 * 
	 * @return the configuration property value, or the default if it has not been set.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String, String, Object...)
	 */
	public String getProperty(String key, String propertyDefault, Object... inserts);

}