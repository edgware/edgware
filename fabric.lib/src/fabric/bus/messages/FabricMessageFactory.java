/*
 * (C) Copyright IBM Corp. 2009, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import java.net.InetAddress;
import java.net.UnknownHostException;

import fabric.Fabric;
import fabric.bus.messages.impl.FabricMessage;
import fabric.core.xml.XML;

/**
 * Factory class for Fabric messages.
 */
public abstract class FabricMessageFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

    /*
     * Class static fields
     */

    /** The unique ordinal number used for messages created in this VM */
    private static int ordinal = 1;

    /** Object used to synchronize access to the message ordinal */
    private static Object ordinalLock = new Object();

    /** To hold the host identifier part of the UID */
    private static String hostID = null;

    /*
     * Static class initialization
     */

    static {

        /* Get a unique ID for this node */

        StringBuffer hostIDBuffer = new StringBuffer();

        InetAddress address = null;
        byte[] addressBytes = null;

        try {

            /* Try and get the Internet address */
            address = InetAddress.getLocalHost();
            addressBytes = address.getAddress();

        } catch (UnknownHostException e) {

            /* Use a random value instead */
            addressBytes = new byte[4];
            for (int b = 0; b < 4; b++) {
                addressBytes[b] = (byte) Math.abs(Math.round(Math.random() * Byte.MAX_VALUE));
            }

        }

        /* Convert the address to a hex string */
        for (int b = 0; b < addressBytes.length; b++) {
            String nextByte = String.format("%02x", addressBytes[b]);
            hostIDBuffer.append(nextByte);
        }

        /* Save the ID for use */
        hostID = hostIDBuffer.toString();

    }

    /*
     * Class methods
     */

    /**
     * No instantiation of this class.
     */
    private FabricMessageFactory() {

    }

    /**
     * Create an empty Fabric message of the specified type.
     *
     * @param type
     *            the Fabric message type.
     *
     * @return the instance.
     *
     * @throws ClassNotFoundException
     *             thrown if the class cannot be found.
     *
     * @throws InstantiationException
     *             thrown if the class cannot be instantiated.
     *
     * @throws IllegalAccessException
     *             thrown if the class cannot be instantiated (for example, if there is no default constructor).
     */
    public static IFabricMessage create(String type) throws ClassNotFoundException, IllegalAccessException,
        InstantiationException {

        /* To hold the new instance */
        IFabricMessage instance = null;

        /* Create a new instance */
        instance = (FabricMessage) Fabric.instantiate(type);

        return instance;

    }

    /**
     * Create a Fabric message from an existing XML representation.
     *
     * @param topic
     *            the topic associated with the message or <code>null</code> if none.
     *
     * @param messageXML
     *            the Fabric message.
     *
     * @return the instance.
     *
     * @throws ClassNotFoundException
     *             thrown if the class cannot be found.
     *
     * @throws InstantiationException
     *             thrown if the class cannot be instantiated.
     *
     * @throws IllegalAccessException
     *             thrown if the class cannot be instantiated (for example, if there is no default constructor).
     *
     * @throws Exception
     *             thrown if the class cannot be initialized.
     */
    public static IFabricMessage create(String topic, XML messageXML) throws ClassNotFoundException,
        IllegalAccessException, InstantiationException, Exception {

        /* To hold the new instance */
        IFabricMessage instance = null;

        /* Get the message type */
        String compactType = messageXML.get(messageXML.getDocumentElementPath() + "@t");

        /* If no message type has been specified... */
        if (compactType == null) {
            throw new IllegalArgumentException("No type attribute in Fabric message");
        }

        /* Make sure that we have the full class name for the type */
        String type = Fabric.longName(compactType);
        String className = (type != null) ? type : compactType;

        /* Create a new instance */
        instance = (IFabricMessage) Fabric.instantiate(className);

        /* If we have created a new instance... */
        if (instance != null) {

            instance.init(messageXML.getDocumentElementPath(), messageXML);
            instance.metaSetTopic(topic);

        }

        return instance;

    }

    /**
     * Create a Fabric message from an existing wire message.
     *
     * @param topic
     *            the topic associated with the message of <code>null</code> if none.
     *
     * @param wireBytes
     *            the Fabric message, as received.
     *
     * @return the instance.
     *
     * @throws ClassNotFoundException
     *             thrown if the class cannot be found.
     *
     * @throws InstantiationException
     *             thrown if the class cannot be instantiated.
     *
     * @throws IllegalAccessException
     *             thrown if the class cannot be instantiated (for example, if there is no default constructor).
     *
     * @throws Exception
     *             thrown if the class cannot be initialized.
     */
    public static IFabricMessage create(String topic, byte[] wireBytes) throws ClassNotFoundException,
        IllegalAccessException, InstantiationException, Exception {

        /* Currently the wire format is just a byte array representation of an XML document */
        XML messageXML = new XML(wireBytes);
        return create(topic, messageXML);

    }

    /**
     * Answers a unique identifier of the form:
     * <p>
     * <code>ip-address:timestamp:random-id:uid-count</code>
     * </p>
     * where:
     * <ul>
     * <li><strong>ip-address:</strong> the bytes of the IP address of the host system.</li>
     * <li><strong>timestamp:</strong> the time in milliseconds when the UID was created.</li>
     * <li><strong>random-id:</strong> a six digit pseudo random number to minimize the risk of UID clashes with other
     * JVMs on the same system (note that the risk of this is higher on multi-core systems and when the current time is
     * not given to true millisecond resolution).</li>
     * <li><strong>uid-count:</strong> a count incremented for each UID generated by this JVM.</li>
     * </ul>
     * All values are in hex.
     *
     * @return the UID.
     */
    public static String generateUID() {

        /* Generate a unique temporal element */
        long time = System.currentTimeMillis();

        /* Generate a random element */
        long randomModifier = (long) (Math.random() * 10000000);

        /* Generate the message ordinal */
        long thisOrdinal = 0;
        synchronized (ordinalLock) {
            thisOrdinal = ordinal++;
        }

        /* Combine to form the UID */
        String uid = String.format("%s:%x:%06x:%d", hostID, time, randomModifier, thisOrdinal);

        return uid;

    }

}
