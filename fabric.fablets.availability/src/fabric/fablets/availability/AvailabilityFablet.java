/*
 * (C) Copyright IBM Corp. 2010, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.availability;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.ServiceDescriptor;
import fabric.bus.BusIOChannels;
import fabric.bus.SharedChannel;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.plugins.IFabletConfig;
import fabric.bus.plugins.IFabletPlugin;
import fabric.bus.plugins.IPluginConfig;
import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.logging.FLog;
import fabric.registry.FabricRegistry;
import fabric.registry.Node;
import fabric.registry.NodeFactory;
import fabric.registry.Platform;
import fabric.registry.PlatformFactory;
import fabric.registry.Service;
import fabric.registry.ServiceFactory;
import fabric.registry.System;
import fabric.registry.SystemFactory;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;

/**
 * Fablet class to map data feeds conveying asset availability status to matching Registry updates.
 *
 */
public class AvailabilityFablet extends FabricBus implements IFabletPlugin, ICallback {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2012";

    /*
     * Class constants
     */

    /** The feed descriptor value used to indicate if a platform, system, or feed is an availability feed */
    private static final String AVAILABILITY_FEED_DESCRIPTOR_PART = "availability";

    /*
     * Class fields
     */

    /** The configuration object for this instance */
    private IFabletConfig fabletConfig = null;

    /** Fabric I/O channels */
    private BusIOChannels ioChannels = null;

    /** The topic upon which to listen for all availability status messages sent to the home node */
    private InputTopic availabilityFeedsTopic = null;

    /** The channel used to listen for availability messages */
    private SharedChannel availabilityFeedsChannel = null;

    /** Object used to synchronize with the mapper main thread */
    private final Object threadSync = new Object();

    /** Flag used to indicate when the main thread should terminate */
    private boolean isRunning = true;

    /** Flag used to indicate if availability status changes should be propagated between assets */
    private boolean doPropagate = true;

    /*
     * Class methods
     */

    public AvailabilityFablet() {

        super(Logger.getLogger("fabric.fablets.availability"));
    }

    /**
     * @see fabric.bus.plugins.IPlugin#startPlugin(fabric.bus.plugins.IPluginConfig)
     */
    @Override
    public void startPlugin(IPluginConfig pluginConfig) {

        fabletConfig = (IFabletConfig) pluginConfig;
        ioChannels = fabletConfig.getFabricServices().ioChannels();
        String arguments = pluginConfig.getArguments();

        /* If status changes are not be propagated... */
        if (arguments != null && arguments.equals("NO_PROAGATION")) {

            doPropagate = false;

        }

    }

    /**
     * @see fabric.bus.plugins.IPlugin#stopPlugin()
     */
    @Override
    public void stopPlugin() {

        /* Tell the main thread to stop... */
        isRunning = false;

        /* ...and wake it up */
        synchronized (threadSync) {
            threadSync.notify();
        }

        try {

            /* Cancel the availability feed subscription */
            homeNodeEndPoint().closeChannel(availabilityFeedsChannel, false);

        } catch (Exception e) {

            logger.log(Level.WARNING, "Closure of channel for topic \"{0}\" failed: {1}", new Object[] {
                    availabilityFeedsTopic, FLog.stackTrace(e)});

        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {

            /* Construct the topic upon which to listen for all availability status messages sent to the home node */
            availabilityFeedsTopic = new InputTopic(ioChannels.receiveLocalFeeds + "/+/+/availability");

            try {

                /* Open the channel used to listen for availability messages */
                availabilityFeedsChannel = homeNodeEndPoint().openInputChannel(availabilityFeedsTopic, this);

            } catch (Exception e) {

                logger.log(Level.WARNING,
                        "Failed to open channel \"{0}\" used to monitor availability status messages: {1}",
                        new Object[] {availabilityFeedsTopic, FLog.stackTrace(e)});
            }

            /* While the Fablet is running... */
            while (isRunning) {
                try {
                    synchronized (threadSync) {
                        threadSync.wait();
                    }
                } catch (InterruptedException e) {
                }
            }

        } catch (Exception e1) {

            logger.log(Level.SEVERE, "Plug-in failed: ", e1);

        }
    }

    /**
     * @see fabric.core.io.ICallback#cancelCallback(java.lang.Object)
     */
    @Override
    public void cancelCallback(Object arg1) {

        /* Nothing to do here */

    }

    /**
     * @see fabric.core.io.ICallback#handleMessage(fabric.core.io.Message)
     */
    @Override
    public void handleMessage(Message message) {

        FLog.enter(logger, Level.FINER, this, "handleMessage", message);

        /* Unpack the message */
        String messageTopic = (String) message.topic;
        byte[] messageData = message.data;
        String messageString = new String((messageData != null) ? messageData : new byte[0]);
        String availability = null;

        logger.log(Level.FINER, "Handling availability message [{0}] from topic [{1}]", new Object[] {
                FLog.trim(messageString), message.topic});
        logger.log(Level.FINEST, "Full message:\n{0}", messageString);

        try {

            /* Get the service descriptor out of the message topic */
            int descriptorStart = ioChannels.receiveLocalFeeds.name().length();
            String descriptorString = messageTopic.substring(descriptorStart);
            ServiceDescriptor serviceDescriptor = new ServiceDescriptor(descriptorString);

            /*
             * Convert the availability message payload (0 or 1) to the appropriate Registry (UNAVAILABLE or AVAILABLE)
             * table value
             */

            if (messageString.equals("0")) {

                availability = "UNAVAILABLE";

            } else if (messageString.equals("1")) {

                availability = "AVAILABLE";

            } else {

                logger.log(Level.WARNING, "Invalid availablity message payload \"{0}\"; expecting \"0\" or \"1\"",
                        messageString);
                return;

            }

            /* Update the Registry to reflect the new availability status */

            /* If this is a node availability status change... */
            if (serviceDescriptor.platform().equalsIgnoreCase(AVAILABILITY_FEED_DESCRIPTOR_PART)) {

                setNodeAvailability(availability);

            }
            /* Else if this is a platform availability status change... */
            else if (serviceDescriptor.system().equalsIgnoreCase(AVAILABILITY_FEED_DESCRIPTOR_PART)) {

                setPlatformAvailability(availability, serviceDescriptor);

            }
            /* Else if this is a system availability status change... */
            else if (serviceDescriptor.service().equalsIgnoreCase(AVAILABILITY_FEED_DESCRIPTOR_PART)) {

                setSystemAvailability(availability, serviceDescriptor);

            } else {

                /* Internal error; we're probably listening on the wrong topics */
                logger.log(Level.WARNING, "Invalid feed descriptor \"{0}\"; check subscription (topic = \"{1}\")",
                        new Object[] {serviceDescriptor.toString(), availabilityFeedsTopic});
            }

        } catch (Exception e) {

            logger.log(Level.WARNING, "Cannot update availability received on topic \"{0}\" (value \"{1}\"): {2}",
                    new Object[] {messageTopic, messageString, FLog.stackTrace(e)});

        }

        FLog.exit(logger, Level.FINER, this, "handleMessage", null);
    }

    /**
     * Sets the availability of a node.
     *
     * @param availability
     *            the new availability value.
     *
     * @throws IncompleteObjectException
     * @throws PersistenceException
     */
    private void setNodeAvailability(String availability) throws IncompleteObjectException, PersistenceException {

        /* Update the availability of the node */
        NodeFactory nodeFactory = FabricRegistry.getNodeFactory();
        Node node = nodeFactory.getNodeById(homeNode());
        node.setAvailability(availability);
        nodeFactory.update(node);
        pause();

        if (doPropagate) {

            /* Get the list of platforms attached to this node */
            PlatformFactory platformFactory = FabricRegistry.getPlatformFactory();
            Platform[] platforms = platformFactory.getPlatformsByNode(homeNode());

            /* For each platform... */
            for (int p = 0; p < platforms.length; p++) {

                /* Update the availability of the platform */
                ServiceDescriptor platformFeedDescriptor = new ServiceDescriptor(platforms[p].getId(),
                        AVAILABILITY_FEED_DESCRIPTOR_PART, AVAILABILITY_FEED_DESCRIPTOR_PART);
                setPlatformAvailability(availability, platformFeedDescriptor);

            }

        }

    }

    /**
     * Sets the availability of a platform.
     *
     * @param availability
     *            the new availability value.
     *
     * @param serviceDescriptor
     *            the descriptor identifying the platform.
     *
     * @throws IncompleteObjectException
     * @throws PersistenceException
     */
    private void setPlatformAvailability(String availability, ServiceDescriptor serviceDescriptor)
            throws IncompleteObjectException, PersistenceException {

        /* Update the availability of the platform */
        PlatformFactory platformFactory = FabricRegistry.getPlatformFactory();
        Platform platform = platformFactory.getPlatformById(serviceDescriptor.platform());
        platform.setAvailability(availability);
        platformFactory.update(platform);
        pause();

        if (doPropagate) {

            /* Get the list of systems attached to this platform */
            SystemFactory systemFactory = FabricRegistry.getSystemFactory();
            System[] systems = systemFactory.getSystemsByPlatform(serviceDescriptor.platform());

            /* For each system... */
            for (int s = 0; s < systems.length; s++) {

                /* Update the availability of the system */
                ServiceDescriptor availabilityDescriptor = new ServiceDescriptor(systems[s].getPlatformId(), systems[s]
                        .getId(), AVAILABILITY_FEED_DESCRIPTOR_PART);
                setSystemAvailability(availability, availabilityDescriptor);

            }

        }

    }

    /**
     * Sets the availability of a system.
     *
     * @param availability
     *            the new availability value.
     *
     * @param serviceDescriptor
     *            the descriptor identifying the system.
     *
     * @throws IncompleteObjectException
     * @throws PersistenceException
     */
    private void setSystemAvailability(String availability, ServiceDescriptor serviceDescriptor)
            throws IncompleteObjectException, PersistenceException {

        /* Update the availability of the system */
        SystemFactory systemFactory = FabricRegistry.getSystemFactory();
        System system = systemFactory.getSystemsById(serviceDescriptor.platform(), serviceDescriptor.system());
        system.setAvailability(availability);
        systemFactory.update(system);
        pause();

        /* Get the list of feeds for this system */
        ServiceFactory serviceFactory = FabricRegistry.getServiceFactory();
        Service[] servicesForSystem = serviceFactory.getServicesBySystem(serviceDescriptor.platform(),
                serviceDescriptor.system());

        /* For each service... */
        for (int f = 0; f < servicesForSystem.length; f++) {

            /* Update the availability */
            servicesForSystem[f].setAvailability(availability);
            serviceFactory.update(servicesForSystem[f]);
            pause();

        }
    }

    /**
     * @see fabric.core.io.ICallback#startCallback(java.lang.Object)
     */
    @Override
    public void startCallback(Object arg1) {

        /* Nothing to do here */

    }

    /**
     * @see fabric.bus.plugins.IPlugin#handleControlMessage(fabric.bus.messages.IFabricMessage)
     */
    @Override
    public void handleControlMessage(IFabricMessage message) {

        /* Not supported */

    }

    /**
     * Wait to give the Registry a chance to catch up on any triggers fired as a result of the updates being made.
     */
    private void pause() {

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
        }

    }
}
