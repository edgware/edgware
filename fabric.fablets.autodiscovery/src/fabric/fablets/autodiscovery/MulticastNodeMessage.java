/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.autodiscovery;

import java.net.DatagramPacket;
import java.util.logging.Logger;

import fabric.session.NodeDescriptor;

/**
 * This class represents the AutoDiscovery message for a node
 *
 * The unpacking of the message is delayed until needed to allow the thread listening to the Multicast Socket to work
 * more efficiently.
 */
public class MulticastNodeMessage extends MulticastMessage {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    @SuppressWarnings("unused")
    private final static String CLASS_NAME = MulticastNodeMessage.class.getName();
    private final static String PACKAGE_NAME = MulticastNodeMessage.class.getPackage().getName();
    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

    public static final int AVAILABLE = 1;
    public static final int UNAVAILABLE = 0;

    private NodeDescriptor nodeDescriptor = null;
    private String nodeType = "";
    private String nodeAffiliation = "";
    private int messageSequenceNumber = -1;
    private long messageTimeStamp = 0;

    public MulticastNodeMessage(DatagramPacket p, String nodeInterface) {
        super(p, nodeInterface);
    }

    @Override
    public boolean unpack() {

        boolean response = false;

        String messageData = new String(getPacket().getData(), 0, getPacket().getLength());
        // Each request is a delimited string, with fields separated by a colon.
        String[] messageElements = messageData.split(":");

        if (messageElements.length == 8 && messageElements[0].equals("N!")) {
            setNodeType(messageElements[1]);
            setNodeAffiliation(messageElements[5]);
            setMessageSequenceNumber(Integer.parseInt(messageElements[6]));
            setMessageTimeStamp(Long.parseLong(messageElements[7]));
            setNodeDescriptor(new NodeDescriptor(messageElements[2], messageElements[3], getPacket().getAddress()
                    .getHostAddress(), Integer.parseInt(messageElements[4])));
            response = true;
        }
        return response;
    }

    public NodeDescriptor getNodeDescriptor() {
        return nodeDescriptor;
    }

    public void setNodeDescriptor(NodeDescriptor nodeDescriptor) {
        this.nodeDescriptor = nodeDescriptor;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeAffiliation() {
        return nodeAffiliation;
    }

    public void setNodeAffiliation(String nodeAffiliation) {
        this.nodeAffiliation = nodeAffiliation;
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public void setMessageSequenceNumber(int messageSequenceNumber) {
        this.messageSequenceNumber = messageSequenceNumber;
    }

    public long getMessageTimeStamp() {
        return messageTimeStamp;
    }

    public void setMessageTimeStamp(long messageTimeStamp) {
        this.messageTimeStamp = messageTimeStamp;
    }

    public String getDiscoveryMessage(int availability) {
        String discoveryMessage = "";
        discoveryMessage = "nt=" + getNodeType() + ",n=" + getNodeDescriptor().name() + ",nf="
                + getNodeDescriptor().networkInterface() + ",ni=" + getNodeDescriptor().address() + ",np="
                + getNodeDescriptor().port() + ",li=" + getLocalInterface() + ",ns=";

        discoveryMessage = discoveryMessage + availability + ",na=" + getNodeAffiliation();
        return discoveryMessage;

    }

}
