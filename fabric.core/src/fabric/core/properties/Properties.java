/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.logging.Logger;

/**
 * Provides access to a local properties (resource bundle) file.
 */
public class Properties {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Class fields
	 */

	/** The name of the resource bundle */
	private String bundleName = null;

	/** The resource bundle containing the properties set */
	protected PropertyResourceBundle bundle = null;

	protected Logger logger;

	/*
	 * Class methods
	 */

	/**
	 * Creates a new instance.
	 * 
	 * @param bundleName
	 *            the name of the properties file.
	 */
	public Properties(String bundleName) {

		this(bundleName, Logger.getLogger("fabric.core.properties"));
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param bundleName
	 *            the name of the properties file.
	 * 
	 * @param logger
	 *            the logger.
	 */
	public Properties(String bundleName, Logger logger) {

		this.bundleName = bundleName;
		this.logger = logger;

		try {

			this.bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(bundleName);

		} catch (MissingResourceException mre) {

			/* Not found on the classpath: try again as a filename */

			File f = new File(bundleName);

			/* If the file exists... */
			if (f.exists()) {

				try {

					/* Load it */
					InputStream is = new FileInputStream(f);
					this.bundle = new PropertyResourceBundle(is);
					is.close();

				} catch (Exception e) {

					throw mre;

				}

			} else {

				throw mre;

			}
		}
	}

	/**
	 * Creates a new instance from an existing instance.
	 * 
	 * @param source
	 *            the source properties.
	 * 
	 * @param logger
	 *            the logger.
	 */
	public Properties(Properties source, Logger logger) {

		this.bundleName = source.bundleName;
		this.bundle = source.bundle;
		this.logger = logger;

	}

	/**
	 * Answers the name of this resource bundle.
	 * 
	 * @return the properties file name.
	 */
	public String getBundleName() {

		return bundleName;

	}

	/**
	 * Answers the value of the specified property as follows:
	 * <ol>
	 * <li>The system properties are searched.</li>
	 * <li>The user defined resource bundle is searched.</li>
	 * <li>The default value is returned.</li>
	 * </ol>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @return the property value.
	 */
	public String getProperty(String key) {

		return lookupProperty(key, null, (Object[]) null);

	}

	/**
	 * Answers the value of the specified property as follows:
	 * <ol>
	 * <li>The system properties are searched.</li>
	 * <li>The user defined resource bundle is searched.</li>
	 * <li>The default value is returned.</li>
	 * </ol>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @param propertyDefault
	 *            the default value for this property.
	 * 
	 * @return the property value.
	 */
	public String getProperty(String key, String propertyDefault) {

		return lookupProperty(key, propertyDefault, (Object[]) null);

	}

	/**
	 * Answers the value of the specified property as follows:
	 * <ol>
	 * <li>The system properties are searched.</li>
	 * <li>The user defined resource bundle is searched.</li>
	 * <li>The default value is returned.</li>
	 * </ol>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @param inserts
	 *            property insert values.
	 * 
	 * @return the property value.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String)
	 */
	public String getProperty(String key, Object... inserts) {

		return lookupProperty(key, null, inserts);

	}

	/**
	 * Answers the value of the specified property as follows:
	 * <ol>
	 * <li>The system properties are searched.</li>
	 * <li>The user defined resource bundle is searched.</li>
	 * <li>The default value is returned.</li>
	 * </ol>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @param propertyDefault
	 *            the default value for this property.
	 * 
	 * @param inserts
	 *            property insert values.
	 * 
	 * @return the property value.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String)
	 */
	public String getProperty(String key, String propertyDefault, Object... inserts) {

		return lookupProperty(key, propertyDefault, inserts);

	}

	/**
	 * Answers the value of the specified property as follows:
	 * <ol>
	 * <li>The system properties are searched.</li>
	 * <li>The user defined resource bundle is searched.</li>
	 * <li>The default value is returned.</li>
	 * </ol>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @param propertyDefault
	 *            the default value for this property.
	 * 
	 * @param inserts
	 *            property insert values.
	 * 
	 * @return the property value.
	 * 
	 * @see fabric.core.properties.Properties#getProperty(String)
	 */
	public String lookupProperty(String key, String propertyDefault, Object... inserts) {

		String property = null;

		/* Try and get the property from the system properties first */
		property = System.getProperty(key);

		/* If we didn't find it... */
		if (property == null) {

			try {

				/* Get the property from the properties file */
				property = bundle.getString(key);

			} catch (MissingResourceException mre) {

				/* We still didn't find it, so use the default */
				property = propertyDefault;

			}
		}

		/* If we've got a property value and a set of inserts... */
		if (property != null && inserts != null && inserts.length > 0) {

			/* Merge the inserts */
			property = mergeInserts(property, inserts);

		}

		return property;

	}

	/**
	 * Answers the specified property value merged with its inserts.
	 * 
	 * @param property
	 *            the property value.
	 * 
	 * @param inserts
	 *            the insert values.
	 * 
	 * @return the fully expanded property.
	 * 
	 * @see java.text.MessageFormat
	 */
	public static String mergeInserts(String property, Object... inserts) {

		return MessageFormat.format(property, inserts);

	}
}
