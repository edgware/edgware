/*
 * (C) Copyright IBM Corp. 2007, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds.impl;

import java.util.ArrayList;

import fabric.TaskServiceDescriptor;
import fabric.bus.plugins.IFeedPluginDispatcher;
import fabric.bus.plugins.impl.FeedPluginDispatcher;
import fabric.bus.routing.IRouting;
import fabric.core.io.MessageQoS;

/**
 * Data structure holding the details of a Fabric subscription.
 */
public class SubscriptionRecord {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

    /*
     * Class fields
     */

    /** The ID of the actor associated with this subscription. */
    private String actor = null;

    /** The ID of the actor's platform, i.e. the application or service via which the actor is connected to the Fabric */
    private String actorPlatform = null;

    /** The service descriptor for this subscription. */
    private TaskServiceDescriptor service = null;

    /** The routing information for this subscription. */
    private IRouting routing = null;

    /** The subscription QoS (QoS) setting. */
    private MessageQoS messageQoS = MessageQoS.DEFAULT;

    /** The in-bound actor plug-in dispatcher. */
    private IFeedPluginDispatcher inboundActorDispatcher = null;

    /** The out-bound actor plug-in dispatcher. */
    private IFeedPluginDispatcher outboundActorDispatcher = null;

    /**
     * The list of handles of clean-up messages registered for this subscription with the local connection manager
     * service.
     */
    private ArrayList<String> cleanupMessageHandles = null;

    /**
     * The handle for the downstream the clean-up message registered for this subscription with the connection manager
     * service
     */
    private String downstreamCleanupMessageHandle = null;

    /*
     * Inner classes
     */

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     * 
     * @param actor
     *            the ID of the actor (user) associated with this subscription.
     * 
     * @param actorPlatform
     *            the platform via which the actor is connected to the Fabric (e.g. the application or service).
     * 
     * @param service
     *            the service descriptor.
     * 
     * @param routing
     *            the routing information for this subscription.
     * 
     * @param messageQoS
     *            the subscription QoS (QoS) setting.
     * 
     * @param inboundActorDispatcher
     *            the in-bound actor plug-in dispatcher.
     * 
     * @param outboundActorDispatcher
     *            the out-bound actor plug-in dispatcher.
     * 
     * @param cleanupMessageHandles
     *            the list of handles of clean-up messages registered for this subscription with the local connection
     *            manager service.
     */
    public SubscriptionRecord(String actor, String actorPlatform, TaskServiceDescriptor service, IRouting routing,
            MessageQoS messageQoS, FeedPluginDispatcher inboundActorDispatcher,
            FeedPluginDispatcher outboundActorDispatcher, ArrayList<String> cleanupMessageHandles) {

        this.actor = actor;
        this.actorPlatform = actorPlatform;
        this.service = service;
        this.routing = routing;
        this.messageQoS = messageQoS;
        this.inboundActorDispatcher = inboundActorDispatcher;
        this.outboundActorDispatcher = outboundActorDispatcher;
        this.cleanupMessageHandles = cleanupMessageHandles;

    }

    /**
     * Answers the ID of the actor associated with this subscription.
     * 
     * @return the actor.
     */
    public String actor() {

        return actor;
    }

    /**
     * Answers the ID of the platform via which the actor has made this subscription.
     * 
     * @return the actor's platform.
     */
    public String actorPlatform() {

        return actorPlatform;
    }

    /**
     * Answers the ID of the actor's home node.
     * 
     * @return the node.
     */
    public String actorNode() {

        return routing.endNode();
    }

    /**
     * Answers the service descriptor.
     * 
     * @return the descriptor.
     */
    public TaskServiceDescriptor service() {

        return service;
    }

    /**
     * Answers the in-bound plug-in dispatcher for this actor/subscription.
     * 
     * @return the dispatcher.
     */
    public IFeedPluginDispatcher inboundActorDispatcher() {

        return inboundActorDispatcher;
    }

    /**
     * Answers the out-bound plug-in dispatcher for this actor/subscription.
     * 
     * @return the dispatcher.
     */
    public IFeedPluginDispatcher outboundActorDispatcher() {

        return outboundActorDispatcher;
    }

    /**
     * Answers the routing information for this subscription.
     * 
     * @return the subscription routing.
     */
    public IRouting routing() {

        return routing;
    }

    /**
     * Answers the subscription QoS (QoS) setting.
     * 
     * @return the QoS setting.
     */
    public MessageQoS messageQoS() {

        return messageQoS;
    }

    /**
     * Answers the the list of handles of clean-up messages registered for this subscription with the local connection
     * manager service.
     * 
     * @return the handle.
     */
    public ArrayList<String> cleanupMessageHandles() {

        return cleanupMessageHandles;
    }

    /**
     * Answers the handle for the downstream the clean-up message registered for this subscription with the connection
     * manager service.
     * 
     * @return the handle.
     */
    public String downstreamCleanupMessageHandle() {

        return downstreamCleanupMessageHandle;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer toString = new StringBuffer();

        toString.append(actor);

        toString.append(':');
        toString.append(actorNode());

        toString.append(':');
        toString.append(actorPlatform);

        toString.append(':');
        toString.append(service.toString());

        return toString.toString();

    }
}
