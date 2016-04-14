/*
 * (C) Copyright IBM Corp. 2010, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.bus.BusIOChannels;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.ClientNotificationMessage;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IClientService;
import fabric.bus.services.IPersistentService;
import fabric.client.FabricClient;

/**
 * Handles notification messages sent from the Fabric to a fabricClient.
 */
public class ClientNotificationService extends Fabric implements IClientService, IPersistentService,
IClientNotificationServices {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

    /*
     * Class fields
     */

    /** The service configuration. */
    private IPluginConfig config = null;

    /** The Fabric client associated with this service. */
    private FabricClient fabricClient = null;

    /** Channels used for Fabric I/O */
    private BusIOChannels ioChannels = null;

    /** The client notification handlers registered for each correlation ID */
    private final HashMap<String, IClientNotificationHandler> notificationHandlers = new HashMap<String, IClientNotificationHandler>();

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public ClientNotificationService() {

        super(Logger.getLogger("fabric.client.services"));

    }

    /**
     * Constructs a new instance.
     */
    public ClientNotificationService(Logger logger) {

        super(logger);

    }

    /**
     * @see fabric.bus.services.IService#serviceConfig()
     */
    @Override
    public IPluginConfig serviceConfig() {

        return config;

    }

    /**
     * @see fabric.bus.services.IClientService#setFabricClient(fabric.client.FabricClient)
     */
    @Override
    public void setFabricClient(FabricClient fabricClient) {

        this.fabricClient = fabricClient;

    }

    /**
     * @see fabric.bus.services.IClientService#setIOChannels(fabric.bus.BusIOChannels)
     */
    @Override
    public void setIOChannels(BusIOChannels ioChannels) {

        this.ioChannels = ioChannels;

    }

    /**
     * @see fabric.bus.services.IService#initService(fabric.bus.plugins.IPluginConfig)
     */
    @Override
    public void initService(IPluginConfig config) {

        this.config = config;

    }

    /**
     * @see fabric.client.services.IClientNotificationServices#registerNotificationHandler(java.lang.String,
     *      fabric.client.services.IClientNotificationHandler)
     */
    @Override
    public IClientNotificationHandler registerNotificationHandler(String correlationID,
            IClientNotificationHandler handler) {

        logger.log(Level.FINEST, "Registering notification handler for correlation ID [{0}]: {1}", new Object[] {
                correlationID, handler});

        IClientNotificationHandler oldHandler = notificationHandlers.put(correlationID, handler);
        return oldHandler;

    }

    /**
     * @see fabric.client.services.IClientNotificationServices#deregisterNotificationHandler(java.lang.String)
     */
    @Override
    public IClientNotificationHandler deregisterNotificationHandler(String correlationID) {

        logger.log(Level.FINEST, "De-registering notification handler for correlation ID [{0}]", correlationID);

        IClientNotificationHandler oldHandler = notificationHandlers.remove(correlationID);
        return oldHandler;

    }

    /**
     * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage, INotificationMessage,
     *      IClientNotificationMessage[])
     */
    @Override
    public IServiceMessage handleServiceMessage(IServiceMessage message, INotificationMessage response,
            IClientNotificationMessage[] clientResponses) throws Exception {

        ClientNotificationMessage notificationMessage = (ClientNotificationMessage) message;

        /* Get the client notification handler registered for this correlation ID */
        IClientNotificationHandler notificationHandler = notificationHandlers.get(message.getCorrelationID());

        /* If there is one... */
        if (notificationHandler != null) {

            /* Invoke it */
            notificationHandler.handleNotification(notificationMessage);

        } else {

            logger.log(Level.FINEST, "No client handler registered for message UID [{0}] (correlation ID [{1}])",
                    new Object[] {message.getUID(), message.getCorrelationID()});

        }

        return message;

    }

    /**
     * @see fabric.bus.services.IPersistentService#stopService()
     */
    @Override
    public void stopService() {

        logger.log(Level.FINE, "Service [{0}] stopped", getClass().getName());
    }
}
