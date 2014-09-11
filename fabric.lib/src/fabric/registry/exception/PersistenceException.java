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
 * Generic exception representing an error which occurred while accessing the Fabric Registry.
 */

public class PersistenceException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private static final long serialVersionUID = 1L;

	private String errorMessage = null;
	private int errorCode = 0;
	private String sqlState = null;

	/**
	 * 
	 */
	public PersistenceException() {
	}

	/**
	 * 
	 * @param msg
	 */
	public PersistenceException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param detailMessage
	 * @param throwable
	 */
	public PersistenceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	/**
	 * 
	 * @param throwable
	 */
	public PersistenceException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * 
	 * @param message
	 * @param errorMessage
	 * @param errorCode
	 * @param sqlState
	 */
	public PersistenceException(String message, String errorMessage, int errorCode, String sqlState) {
		super(message);

		this.errorMessage = errorMessage;
		this.errorCode = errorCode;
		this.sqlState = sqlState;
	}

	/**
	 * 
	 * @return
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * 
	 * @param errorCode
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * 
	 * @return
	 */
	public String getSqlState() {
		return sqlState;
	}

	/**
	 * 
	 * @param sqlState
	 */
	public void setSqlState(String sqlState) {
		this.sqlState = sqlState;
	}
}
