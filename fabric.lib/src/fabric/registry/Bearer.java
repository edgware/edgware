/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents information about a bearer.
 */
public interface Bearer extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Get the identifier for the bearer.
	 * 
	 * @return the id of the bearer.
	 */
	public String getId();

	/**
	 * Set the identifier for the bearer - this attribute must always be specified. Without it, the object cannot be
	 * saved to the Fabric Registry and will generate an IncompleteObjectException.
	 * 
	 * @param id
	 *            the bearer id.
	 */
	public void setId(String id);

	/**
	 * Get the availability status of the bearer.
	 * 
	 * @return the availability status, one of "true" or "false".
	 */
	public String getAvailable();

	/**
	 * Set the availability status of the bearer - this attribute must always be specified. Without it, the object
	 * cannot be saved to the Fabric Registry and will generate an IncompleteObjectException.
	 * 
	 * @param available
	 *            the availability status, one of "true" or "false". Cannot be <code>null</code>.
	 */
	public void setAvailable(String available);

	/**
	 * Get the custom attributes for the bearer.
	 * 
	 * @return the attributes or <code>null</code> if none are specified.
	 */
	public String getAttributes();

	/**
	 * Set the custom attributes for the bearer.
	 * 
	 * @param attributes
	 *            the attributes or <code>null</code>.
	 */
	public void setAttributes(String attributes);

	/**
	 * Get the URI of the attributes.
	 * 
	 * @return the URI or <code>null</code> if not specified.
	 */
	public String getAttributesURI();

	/**
	 * Set the URI of the attributes.
	 * 
	 * @param attributesURI
	 *            a URI or <code>null</code>.
	 */
	public void setAttributesURI(String attributesURI);

	/**
	 * Get the description of the bearer.
	 * 
	 * @return the description or <code>null</code> if none was specified.
	 */
	public String getDescription();

	/**
	 * Set the description of the bearer.
	 * 
	 * @param description
	 *            the description or <code>null</code>.
	 */
	public void setDescription(String description);
}
