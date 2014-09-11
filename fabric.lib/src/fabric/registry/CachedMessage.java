/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

public interface CachedMessage extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";
	
	public String getSource();
	public String getDestination();
	public long getTimestamp();
	public String getMessage();
	
	public void setSource(String src);
	public void setDestination(String dst);
	public void setMessage(String msg);
	public void setTimestamp(long ts);
	

	
}
