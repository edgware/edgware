/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.exception;

/**
 * Exception which is thrown when an error occurs running multiple updates to the Fabric Registry.
 */
public class BatchRegistryUpdateException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private Integer[] rejectObjects = null;

	/**
	 * 
	 */
	public BatchRegistryUpdateException() {
	}

	/**
	 * 
	 * @param message
	 */
	public BatchRegistryUpdateException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param cause
	 */
	public BatchRegistryUpdateException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public BatchRegistryUpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @return
	 */
	public Integer[] getRejectObjects() {
		return rejectObjects;
	}

	/**
	 * 
	 * @param rejectObjects
	 */
	public void setRejectObjects(Integer[] rejectObjects) {
		this.rejectObjects = rejectObjects;
	}

}
