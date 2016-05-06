/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

public interface Type extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";
	
	public static final int TYPE_FEED = 0;
	public static final int TYPE_NODE = 1;
	public static final int TYPE_PLATFORM = 2;
	public static final int TYPE_SERVICE = 3;
	public static final int TYPE_ACTOR = 4;

	/**
	 * @deprecated
	 */
	public static final int TYPE_SENSOR = 3;

	/**
	 * Get the classifier integer for this type which will be one of TYPE_SERVICE, TYPE_NODE, TYPE_PLATFORM, TYPE_SYSTEM or TYPE_ACTOR.
	 * @return
	 */
	public int getClassifier();
	
	/**
	 * 
	 * @param classifier
	 */
	public void setClassifier(int classifier);
	
	/**
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * 
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * 
	 * @return
	 */
	public String getDescription();
	
	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description);
	
	/**
	 * 
	 * @return
	 */
	public String getAttributes();
	
	/**
	 * 
	 * @param attributes
	 */
	public void setAttributes(String attributes);
	
	/**
	 * 
	 * @return
	 */
	public String getAttributesUri();
	
	/**
	 * 
	 * @param uri
	 */
	public void setAttributesUri(String uri);
}
