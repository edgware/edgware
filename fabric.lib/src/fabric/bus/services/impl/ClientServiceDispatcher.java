/*
 * (C) Copyright IBM Corp. 2009, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services.impl;

import java.util.logging.Level;

import fabric.bus.BusIOChannels;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IConnectionMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.services.IClientService;
import fabric.bus.services.IClientServiceDispatcher;
import fabric.bus.services.IService;
import fabric.client.FabricClient;
import fabric.core.logging.FLog;

/**
 * Base class for Fabric plug-in dispatchers.
 */
public class ClientServiceDispatcher extends ServiceDispatcher implements IClientServiceDispatcher {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

    /*
     * Class fields
     */

    /** The Fabric bus I/O channels available to this service instance */
    private BusIOChannels ioChannels = null;

    /** The Fabric client associated with this service dispatcher */
    private FabricClient client = null;

    /*
     * Class methods
     */

    /**
     * @see fabric.bus.services.IClientServiceDispatcher#setFabricClient(fabric.client.FabricClient)
     */
    @Override
    public void setFabricClient(FabricClient client) {

        this.client = client;

    }

    /**
     * @see fabric.bus.services.IClientServiceDispatcher#setIOChannels(fabric.bus.BusIOChannels)
     */
    @Override
    public void setIOChannels(BusIOChannels ioChannels) {

        this.ioChannels = ioChannels;

    }

    /**
     * @see fabric.bus.services.impl.ServiceDispatcher#serviceInstance(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public IService serviceInstance(String name, String arguments, String familyName, String description) {

        IClientService cep = null;

        /* If this is a client service... */
        if (isClientService(name)) {

            /* Load the service */
            cep = (IClientService) super.serviceInstance(name, arguments, familyName, description);

            /* If the load was successful... */
            if (cep != null) {

                /* Configure the service */
                cep.setFabricClient(client);
                cep.setIOChannels(ioChannels);

            }
        }

        return cep;
    }

    /**
     * Checks that the type of the service is a client service.
     *
     * @param serviceName
     *            the name of the service.
     *
     * @return <code>true</code> if the service is a client service, <code>false</code> otherwise.
     */
    boolean isClientService(String serviceName) {

        boolean isClientService = false;

        try {

            Class iClientServiceClass = Class.forName("fabric.bus.services.IClientService");
            Class serviceClass = Class.forName(serviceName);

            isClientService = iClientServiceClass.isAssignableFrom(serviceClass);

        } catch (Throwable e) {

            logger.log(Level.WARNING, "Cannot check type of service class \"{0}\": {1}", new Object[] {serviceName,
                    FLog.stackTrace(e)});

        }

        return isClientService;

    }

    /**
     * @see fabric.bus.services.impl.ServiceDispatcher#dispatch(fabric.bus.messages.IServiceMessage,
     *      fabric.bus.messages.INotificationMessage, fabric.bus.messages.IClientNotificationMessage[])
     */
    @Override
    public IServiceMessage dispatch(IServiceMessage requestIn, INotificationMessage response,
            IClientNotificationMessage[] clientResponses) throws Exception {

        /* Carry out mainstream processing */
        IServiceMessage handledMessage = super.dispatch(requestIn, response, clientResponses);

        if (handledMessage != null) {

            /* If this is a connection message... */
            if (handledMessage instanceof IConnectionMessage) {

                /* Determine what happened */
                int event = requestIn.getEvent();

                /* Determine to whom it happened */
                String node = requestIn.getProperty(IServiceMessage.PROPERTY_NODE);

                /* If this is a change in the Fabric topology... */
                if ((event == IServiceMessage.EVENT_DISCONNECTED || event == IServiceMessage.EVENT_CONNECTED)) {
                    if (homeNode().equals(node)) {
                        if (client != null) {
                            client.homeNodeNotification(requestIn);
                        }
                    } else {
                        if (client != null) {
                            client.topologyNotification(requestIn);
                        }
                    }
                } else {
                    /* Pass all others through to the client */
                    client.fabricNotification(requestIn);
                }

            } else {

                /* Pass all others through to the client */
                client.fabricNotification(requestIn);

            }
        }

        return handledMessage;
    }
}
