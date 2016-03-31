/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

/**
 * Base interface for classes representing a Fabric message payload.
 */
public interface IMessagePayload extends IEmbeddedXML {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Class constants
	 */

	/* Message payload encoding */

	/** Unspecified payload encoding */
	public static final int PAYLOAD_UNKNOWN = 0;

	/** An ASCII payload */
	public static final int PAYLOAD_TEXT = 1;

	/** A binary (base 64) payload */
	public static final int PAYLOAD_BYTES = 2;

	/** An XML payload */
	public static final int PAYLOAD_XML = 3;

	/*
	 * Interface methods
	 */

	/**
	 * Gets the payload as a byte array, whatever encoding has been used.
	 * 
	 * @return the payload.
	 */
	public byte[] getPayload();

	/**
	 * Sets the payload, automatically determining the type via the <code>payloadEncoding()</code> method.
	 * 
	 * @param payload
	 *            the payload byte array.
	 * 
	 * @return the payload encoding.
	 */
	public int setPayload(byte[] payload);

	/**
	 * Gets a text payload.
	 * 
	 * @return the message payload.
	 * 
	 * @throws IllegalStateException
	 *             thrown if the payload type is not <code>PAYLOAD_TEXT</code>.
	 */
	public String getPayloadText();

	/**
	 * Sets a text payload.
	 * 
	 * @param payload
	 *            the new payload.
	 */
	public void setPayloadText(String payload);

	/**
	 * Gets a binary message payload.
	 * 
	 * @return the message payload.
	 * 
	 * @throws IllegalStateException
	 *             thrown if the payload type is not <code>PAYLOAD_BYTES</code>.
	 */
	public byte[] getPayloadBytes();

	/**
	 * Sets a binary message payload.
	 * 
	 * @param payload
	 *            the new payload.
	 */
	public void setPayloadBytes(byte[] payload);

	/**
	 * Gets an XML message payload.
	 * 
	 * @return the message payload.
	 * 
	 * @throws IllegalStateException
	 *             thrown if the payload type is not <code>PAYLOAD_XML</code>, or is not a valid serialized
	 *             <code>IEmbeddedXML</code> instance.
	 */
	public IEmbeddedXML getPayloadXML();

	/**
	 * Sets an XML message payload.
	 * 
	 * @param payload
	 *            the new payload.
	 */
	public void setPayloadXML(IEmbeddedXML payload);

	/**
	 * Gets the current encoding of the message payload, one of the constants:
	 * <ul>
	 * <li>PAYLOAD_TEXT</li>
	 * <li>PAYLOAD_BYTES</li>
	 * <li>PAYLOAD_XML</li>
	 * </ul>
	 * 
	 * @return the current encoding of the message payload.</code>.
	 */
	public int getPayloadEncoding();

	/**
	 * Attempts to determine the correct encoding of a byte array payload:
	 * <ol>
	 * <li>If the byte array contains a valid XML document then <code>PAYLOAD_XML</code> is returned.</li>
	 * <li>If the byte arrays contains only US ASCII printable characters then <code>PAYLOAD_TEXT</code> is returned.</li>
	 * <li>Otherwise <code>PAYLOAD_BYTES</code> is returned.</li>
	 * </ol>
	 * 
	 * @param payload
	 *            the payload byte array.
	 * 
	 * @return the payload encoding.
	 */
	public int payloadEncoding(byte[] payload);

}