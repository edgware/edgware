/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.proxypublisher;

import java.util.logging.Level;

import fabric.bus.IBusServices;
import fabric.bus.SharedChannel;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.MessageProperties;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IBusServiceConfig;
import fabric.bus.services.impl.BusService;
import fabric.core.io.InputTopic;
import fabric.core.io.OutputTopic;

/**
 * Service to publish Fabric messages locally on behalf of a remote requester.
 */
public class ProxyPublisherService extends BusService {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /*
     * Class static fields
     */

    /*
     * Class fields
     */

    /** A local copy of the interface to Fabric management functions. */
    private IBusServices busServices = null;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public ProxyPublisherService() {

        super();

    }

    /**
     * @see fabric.bus.services.impl.BusService#initService(fabric.bus.plugins.IPluginConfig)
     */
    @Override
    public void initService(IPluginConfig config) {

        super.initService(config);

        /* Make a local copy of the accessor for Fabric management services */
        busServices = ((IBusServiceConfig) config).getFabricServices();

    }

    /**
     * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage,INotificationMessage,
     *      IClientNotificationMessage[])
     */
    @Override
    public IServiceMessage handleServiceMessage(IServiceMessage serviceMessage, INotificationMessage responseMessage,
            IClientNotificationMessage[] clientResponses) throws Exception {

        /* Get the payload to publish */
        IMessagePayload payloadToPublish = serviceMessage.getPayload();

        /* Build the feed message */
        IFeedMessage feedMessage = busServices.busIO().wrapRawMessage(new byte[1], false);

        /* Add the payload */
        feedMessage.setPayload((IMessagePayload) payloadToPublish.replicate());

        /* Add the correlation ID (if any) */
        String correlationID = serviceMessage.getCorrelationID();
        if (correlationID != null) {
            feedMessage.setCorrelationID(correlationID);
        }

        /* Add the properties */
        MessageProperties properties = (MessageProperties) serviceMessage.getProperties().replicate();
        feedMessage.setProperties(properties);

        /* Get the bus channel and base topic (if any) */
        SharedChannel busChannel = busServices.ioChannels().receiveBusChannel;
        InputTopic busBaseTopic = busServices.ioChannels().receiveBus;

        /* Build the bus topic to which the payload is to be published */
        OutputTopic busTopic = new OutputTopic(busBaseTopic.name() + '/'
                + serviceMessage.getProperty(IServiceMessage.PROPERTY_DELIVER_TO_SERVICE));

        /* Publish the message */
        logger.log(Level.FINEST, "Publishing message to bus topic [{0}]:\n{1}", new Object[] {busTopic,
                feedMessage.toString()});
        busChannel.write(feedMessage.toWireBytes(), busTopic);

        return serviceMessage;

    }

    /**
     * @see fabric.bus.services.IPersistentService#stopService()
     */
    public void stopService() {

        logger.log(Level.FINE, "Service [{0}] stopped", getClass().getName());
    }
}
