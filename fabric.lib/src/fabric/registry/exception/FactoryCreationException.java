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
 * Exception which is thrown if an error occurs trying to instantiate a factory.
 */
public class FactoryCreationException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * 
	 */
	public FactoryCreationException() {
	}

	/**
	 * 
	 * @param detailMessage
	 */
	public FactoryCreationException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * 
	 * @param throwable
	 */
	public FactoryCreationException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * 
	 * @param detailMessage
	 * @param throwable
	 */
	public FactoryCreationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
