/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.utilities;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

import fabric.services.json.JSON;

/**
 * Class representing adapter status's.
 * 
 * This class is substantiated to create an object that contains the status codes generated through the JSON adapter,
 * and can provide a JSON message containing the error code an any additional error messages.
 */
public class AdapterStatus extends AdapterConstants {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class constants
	 */

	private static final String STATUS_JSON = "{" + //
			"\"" + FIELD_OPERATION + "\":\"" + FIELD_STATUS + "\"," + // Operation
			"\"" + FIELD_CORRELATION_ID + "\":\"%s\"," + // Correlation ID
			"\"" + FIELD_STATUS + "\":\"%s\"," + // Status code
			"\"" + FIELD_MESSAGE + "\":\"%s\"" + // Status message
			"}";

	/*
	 * Class fields
	 */

	// public static final AdapterConstants STATUS_OK = new AdapterStatus();

	/**
	 * The code indicating the type of error that has occurred (one of the <code>ERROR_</code> constants in this class).
	 */
	private int errorCode;

	/** The code indicating the operation being performed (one of the <code>OP_CODE_</code> constants in this class). */
	private int opCode;

	/** The code indicating the article being referenced (one of the <code>ARTICLE_</code> constants in this class). */
	private int articleCode;

	/** Message containing additional error information. */
	private String message;

	/** The correlation ID associated with this instance. */
	private String correlationID;

	/** The string form of the object. */
	private String toString = null;

	/** The JSON form of the object. */
	private JSON toJson = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructor method for the AdapterConstants object. It sets the four variables such that the status is OK.
	 */
	private AdapterStatus() {

		this.errorCode = OK;
		this.opCode = OK;
		this.articleCode = OK;
		this.message = STATUS_MSG_OK;
		this.correlationID = null;
	}

	/**
	 * Constructor method for the AdapterConstants object.
	 * 
	 * @param statusCode
	 *            Code indicating the type of error that has occurred (one of the <code>STATUS_</code> constants in this
	 *            class).
	 * 
	 * @param opCode
	 *            Code indicating the operation being performed (one of the <code>OP_CODE_</code> constants in this
	 *            class).
	 * 
	 * @param articleCode
	 *            Code indicating the article being referenced (one of the <code>ARTICLE_</code> constants in this
	 *            class).
	 * 
	 * @param message
	 *            The message to be set.
	 */
	public AdapterStatus(int statusCode, int opCode, int articleCode, String message) {

		this.errorCode = statusCode;
		this.opCode = opCode;
		this.articleCode = articleCode;
		this.message = message;
		this.correlationID = null;
	}

	/**
	 * Constructor method for the AdapterConstants object.
	 * 
	 * @param statusCode
	 *            Code indicating the type of error that has occurred (one of the <code>STATUS_</code> constants in this
	 *            class).
	 * 
	 * @param opCode
	 *            Code indicating the operation being performed (one of the <code>OP_CODE_</code> constants in this
	 *            class).
	 * 
	 * @param articleCode
	 *            Code indicating the article being referenced (one of the <code>ARTICLE_</code> constants in this
	 *            class).
	 * 
	 * @param message
	 *            The message to be set.
	 * 
	 * @param correlationID
	 *            the message correlation ID.
	 */
	public AdapterStatus(int statusCode, int opCode, int articleCode, String message, String correlationID) {

		this.errorCode = statusCode;
		this.opCode = opCode;
		this.articleCode = articleCode;
		this.message = message;
		this.correlationID = correlationID;
	}

	/**
	 * Constructor method for the AdapterConstants object.
	 * 
	 * @param status
	 *            Source status.
	 * 
	 * @param correlationID
	 *            the message correlation ID (overwrites the correlation ID in the source <code>AdapterConstants</code>
	 *            object).
	 */
	public AdapterStatus(String correlationID) {

		this();
		this.correlationID = correlationID;

	}

	/**
	 * Answers <code>true</code> if this is a non-error (i.e. OK) status.
	 * <p>
	 * The detail message and the correlation ID are not considered in this method.
	 * </p>
	 * 
	 * @return <code>true</code> if this is an OK state, <code>false</code> otherwise.
	 */
	public boolean isOK() {

		return (errorCode == OK) && (opCode == OK) && (articleCode == OK);
	}

	/**
	 * Constructs a JSON object representing this instance.
	 * 
	 * @return the JSON object.
	 */
	public JSON toJsonObject() {

		if (toJson == null) {
			try {
				toJson = new JSON(toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return toJson;
	}

	/**
	 * Formats the error code to be human readable.
	 * <p>
	 * The code is 5 characters long and consists of three parts (in hexadecimal):
	 * <ol>
	 * <li>First digit: error code.</li>
	 * <li>Second and third digits: operation code.</li>
	 * <li>Fourth and fifth digits: article code.</li>
	 * </ol>
	 * 
	 * @return The formatted error code.
	 */
	private String formatStatus() {

		String s = String.format("%01x%02x%02x", errorCode, opCode, articleCode);
		return s;
	}

	/**
	 * Getter method for the status code.
	 * 
	 * @return status code
	 */
	public int getErrorCode() {

		return errorCode;
	}

	/**
	 * Getter method for the operation code.
	 * 
	 * @return operation code
	 */
	public int getOpCode() {

		return opCode;
	}

	/**
	 * Getter method for the article code.
	 * 
	 * @return article code
	 */
	public int getArticleCode() {

		return articleCode;
	}

	/**
	 * Getter method for the correlation ID.
	 * 
	 * @return correlation ID.
	 */
	public String getCorrelationID() {

		return correlationID;
	}

	/**
	 * Getter method for the message.
	 * 
	 * @return the message.
	 */
	public String getMessage() {

		return message;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		if (toString == null) {

			String statusCode = formatStatus();

			/* Make sure that the message is properly escaped */
			JsonStringEncoder jse = JsonStringEncoder.getInstance();
			String escapedMessage = new String(jse.quoteAsString(message));

			toString = String.format(STATUS_JSON, correlationID, statusCode, escapedMessage);

		}

		return toString;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		boolean isEqual = false;

		if (obj != null && obj instanceof AdapterStatus) {

			AdapterStatus target = (AdapterStatus) obj;

			/* If it's not the same instance... */
			if (this != target) {

				/* If the fields are equal... */
				if (errorCode == target.errorCode
						&& opCode == target.opCode
						&& articleCode == target.articleCode
						&& ((message == null && target.message == null) || (message != null && message
								.equals(target.message)))
						&& ((correlationID == null && target.correlationID == null) || (correlationID != null && correlationID
								.equals(target.correlationID)))) {

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
}
