/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import fabric.FabricBus;

/**
 * The status returned from a runtime operation performed on a system.
 */
public class RuntimeStatus extends FabricBus {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class constants
	 */

	public static final String MESSAGE_OK = "OK";

	public static final RuntimeStatus STATUS_OK = new RuntimeStatus(Status.OK, MESSAGE_OK);

	/*
	 * Class enumerated types
	 */

	/** System status codes. */
	public enum Status {
		NONE, OK, ALREADY_RUNNING, NOT_RUNNING, SEND_REQUEST_FAILED, SEND_NOTIFICATION_FAILED, NOT_FOUND, START_FAILED, ALREADY_SUBSCRIBED, SUBSCRIBE_FAILED, PUBLISH_FAILED, UNSUBSCRIBE_FAILED
	};

	/*
	 * Class fields
	 */

	/** The status code. */
	private Status status = Status.NONE;

	/** The status message. */
	private String message = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public RuntimeStatus(Status status, String message) {

		this.status = status;
		this.message = message;

	}

	/**
	 * Answers <code>true</code> if this is a non-error (i.e. OK) status.
	 * <p>
	 * The detail message is not considered in this method.
	 * </p>
	 * 
	 * @return <code>true</code> if this is an OK state, <code>false</code> otherwise.
	 */
	public boolean isOK() {

		return (status == Status.OK);
	}

	/**
	 * Answers the status code.
	 * 
	 * @return the status.
	 */
	public Status getStatus() {

		return status;
	}

	/**
	 * Sets the status code.
	 * 
	 * @param status
	 *            the status to set.
	 */
	public void setStatus(Status status) {

		this.status = status;
	}

	/**
	 * Answers the status message.
	 * 
	 * @return the message.
	 */
	public String getMessage() {

		return message;
	}

	/**
	 * Sets the status message.
	 * 
	 * @param message
	 *            the message to set.
	 */
	public void setMessage(String message) {

		this.message = message;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		boolean isEqual = false;

		if (obj != null && obj instanceof RuntimeStatus) {

			RuntimeStatus target = (RuntimeStatus) obj;

			/* If it's not the same instance... */
			if (this != target) {

				/* If the fields are equal... */
				if (this.status == target.status && (this.message != null && this.message.equals(target.message))) {
					isEqual = true;
				}
			} else {
				isEqual = true;
			}
		}

		return isEqual;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return toString().hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder toString = new StringBuilder(status.toString());
		toString.append(':');
		toString.append(message);
		return toString.toString();
	}
}
