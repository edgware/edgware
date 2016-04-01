/*
 * (C) Copyright IBM Corp. 2009, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Notifier;
import fabric.bus.messages.EmbeddedXMLFactory;
import fabric.bus.messages.IEmbeddedXML;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.messages.IReplicate;
import fabric.core.xml.XML;

/**
 * Class representing Fabric message payload.
 */
public class MessagePayload extends Notifier implements IMessagePayload {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

    /*
     * Class constants
     */

    /* Message payload encoding string representations */

    /** Unspecified payload encoding */
    public static final String PAYLOAD_UNKNOWN_STRING = "unknown";

    /** An ASCII payload */
    public static final String PAYLOAD_TEXT_STRING = "text";

    /** A binary (base 64) payload */
    public static final String PAYLOAD_BYTES_STRING = "binary";

    /** An XML payload */
    public static final String PAYLOAD_XML_STRING = "xml";

    /*
     * Class fields
     */

    /** The encoding used for the message payload. */
    private int payloadEncoding = PAYLOAD_UNKNOWN;

    /** The message payload. */
    private Object payload = null;

    /** Cache of the XML form of the message. */
    private XML xmlCache = null;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public MessagePayload() {

        super(Logger.getLogger("fabric.bus.messages"));
        addChangeListener(this);

    }

    /**
     * Constructs a new instance, initialized from the specified instance.
     *
     * @param source
     *            the instance to copy.
     */
    public MessagePayload(MessagePayload source) {

        this();

        payloadEncoding = source.payloadEncoding;

        if (source.payload != null) {

            switch (payloadEncoding) {

                case PAYLOAD_TEXT:

                    payload = source.payload;
                    break;

                case PAYLOAD_XML:

                    IReplicate sourceXML = (IReplicate) payload;
                    payload = sourceXML.replicate();
                    break;

                default:

                    logger.log(Level.WARNING,
                            "Internal error; unrecognized payload encoding ({0}), message will be encode as bytes",
                            payloadEncoding);

                case PAYLOAD_BYTES:

                    byte[] sourceBytes = (byte[]) source.payload;
                    payload = new byte[sourceBytes.length];
                    System.arraycopy(sourceBytes, 0, payload, 0, sourceBytes.length);
                    break;

            }
        }

        xmlCache = null;
    }

    /**
     * @see fabric.bus.messages.IEmbeddedXML#init(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void init(String element, XML messageXML) throws Exception {

        /* Get the payload */
        getPayloadFromMessage(element, messageXML);

        xmlCache = null;

    }

    /**
     * @see fabric.bus.messages.IEmbeddedXML#embed(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void embed(String element, XML messageXML) throws Exception {

        /* Set the payload */
        setPayloadInMessage(element, messageXML);

    }

    /**
     * Gets the payload and payload-type from the message. Several payload types are supported:
     * <ul>
     * <li><strong><code>PAYLOAD_TEXT</code>:</strong> ASCII.</li>
     * <li><strong><code>PAYLOAD_BYTES</code>:</strong> Binary (represented as base 64).</li>
     * <li><strong><code>PAYLOAD_XML</code>:</strong> XML (encoded as a string).</li>
     * </ul>
     *
     * @param element
     *            the element containing the XML.
     *
     * @param messageXML
     *            the document from which this instance will be initialized.
     *
     * @throws Exception
     */
    private void getPayloadFromMessage(String element, XML messageXML) throws Exception {

        /* Get the encoding from the message */
        String encodingString = messageXML.get(element + "/f:payload@enc");

        /* If there is a payload... */
        if (encodingString != null) {

            /* If no encoding or text encoding has been specified... */
            if (encodingString.equals(PAYLOAD_TEXT_STRING)) {

                /* It's a simple ASCII string */
                payloadEncoding = PAYLOAD_TEXT;
                payload = messageXML.get(element + "/f:payload");

            } else if (encodingString.equals(PAYLOAD_BYTES_STRING)) {

                /* It's a base 64 string */
                payloadEncoding = PAYLOAD_BYTES;
                payload = messageXML.getBytes(element + "/f:payload");

            } else {

                /* It's an encoded XML message; the encoding is the Java type name */
                payloadEncoding = PAYLOAD_XML;
                payload = EmbeddedXMLFactory.create(element + "/f:payload", messageXML);

            }
        }
    }

    /**
     * Sets the payload and payload encoding in the message.
     *
     * @param element
     *            the element that will contain the XML.
     *
     * @param messageXML
     *            the XML into which this instance will be serialized.
     *
     * @throws Exception
     */
    private void setPayloadInMessage(String element, XML messageXML) throws Exception {

        if (payload != null) {

            switch (payloadEncoding) {

                case PAYLOAD_TEXT:

                    messageXML.set(element + "/f:payload@enc", PAYLOAD_TEXT_STRING);
                    messageXML.set(element + "/f:payload", (String) payload);
                    break;

                case PAYLOAD_BYTES:

                    messageXML.set(element + "/f:payload@enc", PAYLOAD_BYTES_STRING);
                    messageXML.setBytes(element + "/f:payload", (byte[]) payload);
                    break;

                case PAYLOAD_XML:

                    IEmbeddedXML embeddedXML = (IEmbeddedXML) payload;
                    messageXML.set(element + "/f:payload@enc", payload.getClass().getName());
                    embeddedXML.embed(element + "/f:payload", messageXML);
                    break;

            }
        }
    }

    /**
     * @see fabric.bus.messages.IMessagePayload#getPayload()
     */
    @Override
    public byte[] getPayload() {

        byte[] payload = null;

        switch (payloadEncoding) {

            case PAYLOAD_BYTES:

                payload = getPayloadBytes();
                break;

            case PAYLOAD_TEXT:

                payload = getPayloadText().getBytes();
                break;

            case PAYLOAD_XML:

                XML payloadXML = new XML();
                IEmbeddedXML embeddedXML = getPayloadXML();

                try {
                    embeddedXML.embed("/f:payload", payloadXML);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Cannot convert payload to XML: ", e);
                    throw new IllegalStateException(e);
                }

                payload = payloadXML.toBytes();
                break;

        }

        return payload;

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#setPayload(byte[])
     */
    @Override
    public int setPayload(byte[] payload) {

        /* Record the current payload */
        Object oldPayload = this.payload;

        /* Encode the payload into the Fabric feed message */

        int payloadEncoding = payloadEncoding(payload);

        switch (payloadEncoding) {

            case PAYLOAD_BYTES:

                setPayloadBytes(payload);
                break;

            case PAYLOAD_TEXT:

                setPayloadText(new String(payload));
                break;

            case PAYLOAD_XML:

                try {

                    XML payloadXML = new XML(payload);
                    IEmbeddedXML embeddedXML = EmbeddedXMLFactory.create(payloadXML.getDocumentElementPath(),
                            payloadXML);
                    setPayloadXML(embeddedXML);

                } catch (Exception e1) {

                    logger.log(Level.WARNING, "Error handling XML payload, message will be encoded as bytes: ", e1);
                    setPayloadBytes(payload);

                }
                break;

            default:

                logger.log(Level.WARNING,
                        "Internal error; unrecognized payload encoding ({0}), message will be encode as bytes",
                        payloadEncoding);
                setPayloadBytes(payload);
                break;

        }

        fireChangeNotification("payload", oldPayload, payload);

        return payloadEncoding;

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#getPayloadText()
     */
    @Override
    public String getPayloadText() {

        String payloadText = null;

        if (payloadEncoding == PAYLOAD_TEXT) {

            payloadText = (String) payload;

        } else if (payloadEncoding == PAYLOAD_UNKNOWN) {

            payloadText = null;

        } else {

            throw new IllegalStateException("Wrong payload encoding (" + payloadEncoding + ")");

        }

        return payloadText;

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#setPayloadText(java.lang.String)
     */
    @Override
    public void setPayloadText(String payload) {

        Object oldPayload = this.payload;
        this.payload = payload;
        payloadEncoding = PAYLOAD_TEXT;
        fireChangeNotification("payload", oldPayload, this.payload);

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#getPayloadBytes()
     */
    @Override
    public byte[] getPayloadBytes() {

        byte[] payloadBytes = null;

        if (payloadEncoding == PAYLOAD_BYTES) {

            payloadBytes = (byte[]) payload;

        } else if (payloadEncoding == PAYLOAD_UNKNOWN) {

            payloadBytes = null;

        } else {

            throw new IllegalStateException("Wrong payload encoding (" + payloadEncoding + ")");

        }

        return payloadBytes;

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#setPayloadBytes(byte[])
     */
    @Override
    public void setPayloadBytes(byte[] payload) {

        Object oldPayload = this.payload;
        this.payload = payload;
        payloadEncoding = PAYLOAD_BYTES;
        fireChangeNotification("payload", oldPayload, payload);

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#getPayloadXML()
     */
    @Override
    public IEmbeddedXML getPayloadXML() {

        IEmbeddedXML payloadXML = null;

        if (payloadEncoding == PAYLOAD_XML) {

            payloadXML = (IEmbeddedXML) payload;

        } else if (payloadEncoding == PAYLOAD_UNKNOWN) {

            payloadXML = null;

        } else {

            throw new IllegalStateException("Wrong payload encoding (" + payloadEncoding + ")");

        }

        return payloadXML;

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#setPayloadXML(fabric.bus.messages.IEmbeddedXML)
     */
    @Override
    public void setPayloadXML(IEmbeddedXML payload) {

        Object oldPayload = this.payload;
        this.payload = payload;
        payloadEncoding = PAYLOAD_XML;
        fireChangeNotification("payload", oldPayload, payload);

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#getPayloadEncoding()
     */
    @Override
    public int getPayloadEncoding() {

        return payloadEncoding;

    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String toString = null;

        try {

            if (xmlCache == null) {

                xmlCache = new XML();
                embed("", xmlCache);

            }

            toString = xmlCache.toString();

        } catch (Exception e) {

            e.printStackTrace();
            toString = super.toString();

        }

        return toString;

    }

    /**
     * @see fabric.bus.messages.IMessagePayload#payloadEncoding(byte[])
     */
    @Override
    public int payloadEncoding(byte[] payload) {

        int payloadEncoding = PAYLOAD_UNKNOWN;

        /* Convert the byte array to a string, and trim any leading/trailing whitespace */
        String payloadString = new String(payload).trim();

        /* If the payload is ASCII and the string starts with an opening '<'... */
        if (isASCII(payload) && payloadString.charAt(0) == '<') {

            try {

                /* It could be XML, so try and parse it */
                XML doc = new XML();
                doc.parseString(payloadString);

                /* Success indicates that we have a valid XML document */
                payloadEncoding = PAYLOAD_XML;

            } catch (Exception e) {

                /* Nope, no XML here... */

            }
        }

        /* If we haven't worked out the encoding yet... */
        if (payloadEncoding == PAYLOAD_UNKNOWN) {

            /* If the string only contains printable ASCII characters... */
            if (isASCII(payload)) {

                payloadEncoding = PAYLOAD_TEXT;

            } else {

                payloadEncoding = PAYLOAD_BYTES;

            }

        }

        return payloadEncoding;
    }

    /**
     * Determines if a byte array contains <em>only</em> US ASCII printable characters.
     *
     * @param bytes
     *            the byte array to check.
     *
     * @return <code>true</code> if the byte array contains only US ASCII printable characters, <code>false</code>
     *         otherwise.
     */
    private boolean isASCII(byte[] bytes) {

        boolean isASCII = true;

        /* For each byte... */
        for (int b = 0; isASCII && b < bytes.length; b++) {

            switch (bytes[b]) {

                case '\n':
                case '\r':
                case '\t':

                    /* Valid */
                    break;

                default:

                    /* If the character is not printable... */
                    if (bytes[b] < 32 || bytes[b] > 126) {

                        isASCII = false;

                    }
            }
        }

        return isASCII;

    }

    /**
     * @see fabric.bus.messages.IReplicate#replicate()
     */
    @Override
    public IReplicate replicate() {

        return new MessagePayload(this);

    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {

        super.propertyChange(event);

        /* Something has changed, so invalidate the cached XML form of this instance */
        xmlCache = null;

    }
}
