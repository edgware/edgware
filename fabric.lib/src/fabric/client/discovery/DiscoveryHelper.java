/*
 * (C) Copyright IBM Corp. 2006, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.discovery;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import fabric.LocalConfig;
import fabric.session.NodeDescriptor;
import fabric.session.RegistryDescriptor;

/**
 */
public class DiscoveryHelper {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2012";

    private int port;
    private String multiCastGroup;

    private long responseWait = 5000;
    private int readTimeout = 500;

    private Logger logger;
    private LocalConfig config;

    public DiscoveryHelper(LocalConfig localConfig) {
        logger = Logger.getLogger("fabric.client");
        config = localConfig;
        port = Integer.parseInt(config.getLocalProperty("autodiscovery.port", "61883"));
        multiCastGroup = config.getLocalProperty("autodiscovery.group", "225.0.18.83");

        responseWait = Long.parseLong(config.getLocalProperty("autodiscovery.wait", "5000"));

        logger.log(INFO, "autodiscovery port %d, group %s", new Object[] {port, multiCastGroup});
    }

    /**
     * Returns details of the closet (by response time) fabric manager. Broadcast requests are first sent out on the
     * loopback adapter, then on all other adapters if on response is received.
     * 
     * @param clientName
     * @return the NodeDescriptor for the node
     */
    public NodeDescriptor findFabricManager(String clientName) {

        logger.log(INFO, "findFabricManager called with client name %s", clientName);

        NetworkInterface loopback = null;

        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces
                    .hasMoreElements();) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback()) {
                    loopback = iface;
                    logger.log(FINE, "Loopback interface is %s", iface.getDisplayName());
                    break;
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        List<DiscoveryNodeDescriptor> nodes = null;
        if (loopback != null) {
            nodes = sendFabricManagerBroadcast(clientName, loopback);
        } else {
            logger.log(INFO, "No loopback adapter found");
        }

        if (nodes == null || nodes.isEmpty()) {

            try {
                ArrayList<FabricManagerSeaker> threads = new ArrayList<FabricManagerSeaker>();
                for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces
                        .hasMoreElements();) {
                    NetworkInterface iface = interfaces.nextElement();
                    if (!iface.isLoopback()) {
                        FabricManagerSeaker t = new FabricManagerSeaker(clientName, iface);
                        t.start();
                        threads.add(t);
                    }

                }

                for (Iterator<FabricManagerSeaker> iterator = threads.iterator(); iterator.hasNext();) {
                    FabricManagerSeaker thread = iterator.next();
                    try {
                        thread.join();
                        List<DiscoveryNodeDescriptor> results = thread.getResults();
                        nodes.addAll(results);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Collections.sort(nodes, new DiscoveryNodeDescriptor.ResponseTimeComparitor());

            } catch (SocketException e) {
                logger.log(WARNING, "");
            }
        }

        if (nodes != null && !nodes.isEmpty()) {
            return nodes.iterator().next();
        }

        return null;

    }

    /**
     * Returns a list of all the Fabric Managers that replied to a broadcast
     * 
     * 
     * @return List of Fabric Managers
     */
    public List<DiscoveryNodeDescriptor> findAllFabricManagers(String clientName) {
        List<DiscoveryNodeDescriptor> nodes = new ArrayList<DiscoveryNodeDescriptor>();

        try {
            ArrayList<FabricManagerSeaker> threads = new ArrayList<FabricManagerSeaker>();
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces
                    .hasMoreElements();) {
                NetworkInterface iface = interfaces.nextElement();
                FabricManagerSeaker t = new FabricManagerSeaker(clientName, iface);
                t.start();
                threads.add(t);

            }

            for (Iterator<FabricManagerSeaker> iterator = threads.iterator(); iterator.hasNext();) {
                FabricManagerSeaker thread = iterator.next();
                try {
                    thread.join();
                    List<DiscoveryNodeDescriptor> results = thread.getResults();
                    nodes.addAll(results);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (SocketException e) {
            logger.log(WARNING, "");
        }

        return nodes;
    }

    private List<DiscoveryNodeDescriptor> sendFabricManagerBroadcast(String clientName, NetworkInterface netInterface) {
        List<DiscoveryNodeDescriptor> nodes = new ArrayList<DiscoveryNodeDescriptor>();
        try {
            MulticastSocket udpSendRequestSocket = null;
            InetAddress groupSendRequestAddr = InetAddress.getByName(multiCastGroup);

            if (groupSendRequestAddr.isMulticastAddress()) {
                udpSendRequestSocket = new MulticastSocket();
                udpSendRequestSocket.setNetworkInterface(netInterface);
                udpSendRequestSocket.joinGroup(groupSendRequestAddr);
            } else {
                logger.log(INFO, multiCastGroup + " is not a MulticastAddress");
            }

            StringBuilder reqestMessage = new StringBuilder("C!:");
            reqestMessage.append(clientName);
            byte requestBuffer[] = reqestMessage.toString().getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length,
                    groupSendRequestAddr, port);

            // System.out.println("Request - " + reqestMessage);
            long startTime = System.currentTimeMillis();
            udpSendRequestSocket.send(requestPacket);

            byte responseBuffer[] = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, 1024);

            udpSendRequestSocket.setSoTimeout(readTimeout);
            do {
                try {
                    udpSendRequestSocket.receive(responsePacket);
                    long responseTime = System.currentTimeMillis() - startTime;
                    String response = new String(responseBuffer, 0, responsePacket.getLength());
                    String responseParts[] = response.split(":");
                    int respPort = Integer.parseInt(responseParts[4]);
                    DiscoveryNodeDescriptor node = new DiscoveryNodeDescriptor(responseParts[2], responseParts[3],
                            responsePacket.getAddress().getHostAddress(), respPort, responseTime, netInterface);
                    nodes.add(node);
                    // shortcut out of loop if answered on loopback
                    if (netInterface.isLoopback()) {
                        break;
                    }

                } catch (SocketTimeoutException e) {
                    // do nothing here deliberately
                }
            } while (System.currentTimeMillis() - startTime < responseWait);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nodes;
    }

    public RegistryDescriptor findRegister() {
        List<DiscoveryRegistryDescriptor> registries = new ArrayList<DiscoveryRegistryDescriptor>();

        logger.log(INFO, "findRegister called");

        NetworkInterface loopback = null;

        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces
                    .hasMoreElements();) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback()) {
                    loopback = iface;
                    logger.log(FINE, "Loopback interface is %s", iface.getDisplayName());
                    break;
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (loopback != null) {
            registries = sendRegistryBroadcast(loopback);
        } else {
            logger.log(INFO, "No loopback adapter found");
        }

        if (registries == null || registries.isEmpty()) {

            try {
                ArrayList<RegistrySeaker> threads = new ArrayList<RegistrySeaker>();
                for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces
                        .hasMoreElements();) {
                    NetworkInterface iface = interfaces.nextElement();
                    if (!iface.isLoopback()) {
                        RegistrySeaker t = new RegistrySeaker(iface);
                        t.start();
                        threads.add(t);
                    }
                }

                for (Iterator<RegistrySeaker> iterator = threads.iterator(); iterator.hasNext();) {
                    RegistrySeaker thread = iterator.next();
                    try {
                        thread.join();
                        registries.addAll(thread.getResults());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Collections.sort(registries, new DiscoveryRegistryDescriptor.ResponseTimeComparitor());

            } catch (SocketException e) {
                logger.log(WARNING, "");
            }
        }

        if (registries != null && !registries.isEmpty()) {
            return registries.iterator().next();
        }

        return null;
    }

    public List<DiscoveryRegistryDescriptor> findAllRegstiries() {

        List<DiscoveryRegistryDescriptor> registries = new ArrayList<DiscoveryRegistryDescriptor>();

        try {
            ArrayList<RegistrySeaker> threads = new ArrayList<RegistrySeaker>();
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces
                    .hasMoreElements();) {
                NetworkInterface iface = interfaces.nextElement();
                RegistrySeaker t = new RegistrySeaker(iface);
                t.start();
                threads.add(t);
            }

            for (Iterator<RegistrySeaker> iterator = threads.iterator(); iterator.hasNext();) {
                RegistrySeaker thread = iterator.next();
                try {
                    thread.join();
                    registries.addAll(thread.getResults());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            logger.log(WARNING, "");
        }
        return registries;
    }

    private List<DiscoveryRegistryDescriptor> sendRegistryBroadcast(NetworkInterface netInterface) {
        List<DiscoveryRegistryDescriptor> registries = new ArrayList<DiscoveryRegistryDescriptor>();
        try {
            MulticastSocket udpSendRequestSocket = null;
            InetAddress groupSendRequestAddr = InetAddress.getByName(multiCastGroup);

            if (groupSendRequestAddr.isMulticastAddress()) {
                udpSendRequestSocket = new MulticastSocket();
                udpSendRequestSocket.setNetworkInterface(netInterface);
                udpSendRequestSocket.joinGroup(groupSendRequestAddr);
            } else {

            }

            StringBuilder reqestMessage = new StringBuilder("R!:");
            byte requestBuffer[] = reqestMessage.toString().getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length,
                    groupSendRequestAddr, port);

            // System.out.println("Request - " + reqestMessage);
            long startTime = System.currentTimeMillis();
            udpSendRequestSocket.send(requestPacket);

            byte responseBuffer[] = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, 1024);

            udpSendRequestSocket.setSoTimeout(readTimeout);
            do {
                try {
                    udpSendRequestSocket.receive(responsePacket);
                    long responseTime = System.currentTimeMillis() - startTime;
                    String response = new String(responseBuffer, 0, responsePacket.getLength());
                    String responseParts[] = response.split(":");
                    String uri = URLDecoder.decode(responseParts[2], "UTF-8");
                    boolean reconnect = Boolean.parseBoolean(responseParts[3]);
                    DiscoveryRegistryDescriptor registry = new DiscoveryRegistryDescriptor(responseParts[1],
                            responseParts[2], uri, reconnect, responseTime, netInterface);
                    registries.add(registry);
                    // shortcut out of loop if answered on loopback
                    if (netInterface.isLoopback()) {
                        break;
                    }

                } catch (SocketTimeoutException e) {
                    // do nothing here deliberately
                }
            } while (System.currentTimeMillis() - startTime < responseWait);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return registries;
    }

    /**
     * Helper background class to do Fabric Manager looks on a separate thread
     */
    private class FabricManagerSeaker extends Thread {

        private String clientName;
        private NetworkInterface seakInterface = null;
        List<DiscoveryNodeDescriptor> results = null;

        public FabricManagerSeaker(String clientName, NetworkInterface netInterface) {
            super("Autodisovery Seaker - " + netInterface.getDisplayName());
            this.clientName = clientName;
            this.seakInterface = netInterface;
        }

        @Override
        public void run() {
            logger.log(FINE, "Starting farbic manager seaker on %s", seakInterface.getDisplayName());
            results = sendFabricManagerBroadcast(clientName, seakInterface);
            logger.log(FINE, "Fabric Manager Seaker on %s had %d responses", new Object[] {
                    seakInterface.getDisplayName(), new Integer(results.size())});
        }

        public List<DiscoveryNodeDescriptor> getResults() {
            return results;
        }
    }

    /**
     * Helper background class to do Registry looks on a separate thread
     */
    private class RegistrySeaker extends Thread {

        private NetworkInterface seakInterface = null;
        List<DiscoveryRegistryDescriptor> results = null;

        public RegistrySeaker(NetworkInterface netInterface) {
            this.seakInterface = netInterface;
            setName("Registry-Seeker");
        }

        @Override
        public void run() {
            logger.log(FINE, "Starting registry seaker on %s", seakInterface.getDisplayName());
            results = sendRegistryBroadcast(seakInterface);
            logger.log(FINE, "Registry Seaker on %s had %d responses", new Object[] {seakInterface.getDisplayName(),
                    new Integer(results.size())});
        }

        public List<DiscoveryRegistryDescriptor> getResults() {
            return results;
        }
    }
}
