/*
 * (C) Copyright IBM Corp. 2007, 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import fabric.ServiceDescriptor;
import fabric.bus.messages.IFeedMessage;
import fabric.core.xml.XML;

/**
 * Class representing a Fabric feed message.
 */
public class FeedMessage extends FabricMessage implements IFeedMessage {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

    /*
     * Class constants
     */

    /** The name of the ordinal property */
    public static final String PROPERTY_ORDINAL = "ordl";

    /** The name of the replay flag property */
    public static final String PROPERTY_REPLAY = "rply";

    /*
     * Class fields
     */

    /** The ID of the Fabric service */
    private ServiceDescriptor serviceDescriptor = null;

    /** The task and client IDs from a message */
    private TaskSubscriptions subscriptions = null;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public FeedMessage() {

        super();

        setSubscriptions(new TaskSubscriptions());

        /* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
        metaResetModified();

    }

    /**
     * @see fabric.bus.messages.impl.FabricMessage#init(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void init(String element, XML messageXML) throws Exception {

        super.init(element, messageXML);

        /* Get the list of subscriptions in this message */
        subscriptions.init(element, messageXML);

        /* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
        metaResetModified();

    }

    /**
     * @see fabric.bus.messages.impl.FabricMessage#embed(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void embed(String element, XML messageXML) throws Exception {

        super.embed(element, messageXML);

        /* Set the list of subscriptions in this message */
        subscriptions.embed(element, messageXML);

    }

    /**
     * Makes a deep copy of a table of task and client IDs.
     * <p>
     * This information is recorded in a table where each key is a task ID, and each value is an <code>ArrayList</code>
     * of client IDs.
     * </p>
     *
     * @return the copy.
     */
    private HashMap<String, ArrayList<String>> deepCopySubscriptions(HashMap<String, ArrayList<String>> subscriptions) {

        /* Return a deep copy of the subscriptions table -- we don't want to allow uncontrolled modification */

        HashMap<String, ArrayList<String>> subscriptionsCopy = new HashMap<String, ArrayList<String>>();

        /* For each task... */
        for (Iterator<String> s = subscriptions.keySet().iterator(); s.hasNext();) {

            /* Get the task name */
            String task = s.next();

            /* Get the list of client IDs */
            ArrayList<String> clientIDs = subscriptions.get(task);

            /* Make a copy */

            ArrayList<String> clientIDsCopy = new ArrayList<String>();

            /* For each client ID... */
            for (Iterator<String> c = clientIDs.iterator(); c.hasNext();) {

                clientIDsCopy.add(c.next());

            }

            /* Save it all away */
            subscriptionsCopy.put(task, clientIDsCopy);

        }

        return subscriptionsCopy;

    }

    /**
     * @see fabric.bus.messages.impl.FabricMessage#metaSetTopic(java.lang.String)
     */
    @Override
    public void metaSetTopic(String topic) {

        super.metaSetTopic(topic);

        /* Create the feed ID from the topic */
        String serviceDescriptor = ServiceDescriptor.extract(topic);
        metaSetFeedDescriptor(new ServiceDescriptor(serviceDescriptor));

    }

    /**
     * @see fabric.bus.messages.IFeedMessage#metaGetFeedDescriptor()
     */
    @Override
    public ServiceDescriptor metaGetFeedDescriptor() {

        return serviceDescriptor;

    }

    /**
     * @see fabric.bus.messages.IFeedMessage#metaSetFeedDescriptor(fabric.ServiceDescriptor)
     */
    @Override
    public void metaSetFeedDescriptor(ServiceDescriptor serviceDescriptor) {

        this.serviceDescriptor = serviceDescriptor;

    }

    /**
     * @see fabric.bus.messages.IFeedMessage#getOrdinal()
     */
    @Override
    public long getOrdinal() {

        long ordinal;

        try {

            ordinal = Long.parseLong(getProperty(FeedMessage.PROPERTY_ORDINAL));

        } catch (Exception e) {

            ordinal = -1;

        }

        return ordinal;

    }

    /**
     * @see fabric.bus.messages.IFeedMessage#setOrdinal(long)
     */
    @Override
    public void setOrdinal(long ordinal) {

        setProperty(FeedMessage.PROPERTY_ORDINAL, Long.toString(ordinal));

    }

    /**
     * @see fabric.bus.messages.IFeedMessage#isReplay()
     */
    @Override
    public boolean isReplay() {

        String isReplay = getProperty(FeedMessage.PROPERTY_REPLAY);

        if (isReplay == null) {
            isReplay = "false";
        }

        return Boolean.parseBoolean(isReplay);

    }

    /**
     * @see fabric.bus.messages.IFeedMessage#setReplay(boolean)
     */
    @Override
    public void setReplay(boolean isReplay) {

        setProperty(FeedMessage.PROPERTY_REPLAY, Boolean.toString(isReplay));

    }

    /**
     * @see fabric.bus.messages.IFeedMessage#getSubscriptions()
     */
    @Override
    public TaskSubscriptions getSubscriptions() {

        /* Return a deep copy of the subscriptions table -- we don't want to allow uncontrolled modification */
        return subscriptions;

    }

    /**
     * @see fabric.bus.messages.IFeedMessage#setSubscriptions(fabric.bus.messages.impl.TaskSubscriptions)
     */
    @Override
    public void setSubscriptions(TaskSubscriptions subscriptions) {

        /* Make a note of the old subscriptions */
        TaskSubscriptions oldSubscriptions = this.subscriptions;

        /* If there is currently a subscriptions object... */
        if (oldSubscriptions != null) {

            /* Stop listening for changes to the old subscriptions object */
            oldSubscriptions.removeChangeListener(this);

        }

        /* Record the new subscriptions */
        this.subscriptions = subscriptions;

        /* If there is currently a subscriptions object... */
        if (subscriptions != null) {

            /* Start listening for changes to it */
            subscriptions.addChangeListener(this);

        }

        /* Notify listeners that something has changed */
        fireChangeNotification("subscriptions", oldSubscriptions, subscriptions);

    }
}
