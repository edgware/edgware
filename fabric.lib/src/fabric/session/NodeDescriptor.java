/*
 * (C) Copyright IBM Corp. 2012, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.session;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.registry.NodeIpMapping;

/**
 * Class describing the attributes of a connection to a Fabric node.
 */
public class NodeDescriptor {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012, 2014";

    /*
     * Class fields
     */

    /** The node's name. */
    private String name = null;

    /** The network interface via which to connect to the node. */
    private String networkInterface = null;

    /** The node's IP address. */
    private String address = null;

    /** The node's IP port. */
    private int port = 0;

    /** The string representation of this instance. */
    private String toString = null;

    private Logger logger = null;

    /*
     * Static class initialization
     */

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public NodeDescriptor() {
        logger = logger();
    }

    /**
     * Constructs a new instance.
     * 
     * @param name
     *            the node's name.
     * 
     * @param port
     *            the node's IP port.
     */
    public NodeDescriptor(String name, int port) {

        this();

        try {
            this.name = name;
            this.port = port;
            this.address = "127.0.0.1";
            InetAddress ia = InetAddress.getByName(this.address);
            this.networkInterface = NetworkInterface.getByInetAddress(ia).getName();
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, "Inet address lookup failed: ", e);
        } catch (SocketException e) {
            logger.log(Level.WARNING, "Network interface lookup failed: ", e);
        }
    }

    /**
     * Constructs a new instance.
     * 
     * @param nipm
     *            the network information.
     */
    public NodeDescriptor(NodeIpMapping nipm) {

        this.name = nipm.getNodeId();
        this.networkInterface = nipm.getNodeInterface();
        this.address = nipm.getIpAddress();
        this.port = nipm.getPort();

    }

    /**
     * Constructs a new instance.
     * 
     * @param name
     *            the node's name.
     * 
     * @param networkInterface
     *            the name of the network interface associated with the IP address.
     * 
     * @param address
     *            the node's IP address.
     * 
     * @param port
     *            the node's IP port.
     */
    public NodeDescriptor(String name, String nodeInterface, String address, int port) {

        this();

        this.name = name;
        this.networkInterface = nodeInterface;
        this.address = address;
        this.port = port;

    }

    /**
     * Answers the name of the node.
     * 
     * @return the name.
     */
    public String name() {

        return name;

    }

    /**
     * Answers the interface of the node.
     * 
     * @return the interface name.
     */
    public String networkInterface() {

        return networkInterface;

    }

    /**
     * Answers the node's IP address.
     * 
     * @return the IP address.
     */
    public String address() {

        return address;

    }

    /**
     * Answers the node's IP port.
     * 
     * @return the port.
     */
    public int port() {

        return port;

    }

    /**
     * Answers the name of the loopback interface.
     * 
     * @return the name.
     */
    public static String loopbackInterface() {

        String loopbackInterface = null;

        try {
            InetAddress ia = InetAddress.getByName("127.0.0.1");
            loopbackInterface = NetworkInterface.getByInetAddress(ia).getName();
        } catch (UnknownHostException e) {
            logger().log(Level.WARNING, "Inet address lookup failed: ", e);
        } catch (SocketException e) {
            logger().log(Level.WARNING, "Network interface lookup failed: ", e);
        }

        return loopbackInterface;
    }

    private static Logger logger() {
        return Logger.getLogger("fabric.session");
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        /* If we need to generate the string form of this instance... */
        if (toString == null) {
            toString = name + ':' + networkInterface + ':' + address + ':' + port;
        }

        return toString;

    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        /* To hold the result */
        boolean isEqual = false;

        if (obj != null) {
            isEqual = toString().equals(obj.toString());
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
