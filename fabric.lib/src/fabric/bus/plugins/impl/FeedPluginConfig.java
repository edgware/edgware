/*
 * (C) Copyright IBM Corp. 2007, 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import fabric.ServiceDescriptor;
import fabric.bus.plugins.IFeedPluginConfig;

/**
 * Class representing configuration information for a Fabric Manager message plug-in.
 */
public class FeedPluginConfig extends FabletConfig implements IFeedPluginConfig {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

    /*
     * Class fields
     */

    /** The ID of the task (if any). */
    protected String task = null;

    /** The ID of the actor (if any). */
    protected String actor = null;

    /** The ID of the service. */
    protected ServiceDescriptor service = null;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public FeedPluginConfig() {
    }

    /**
     * @see fabric.bus.plugins.IFeedPluginConfig#getActor()
     */
    @Override
    public String getActor() {
        return actor;
    }

    /**
     * @see fabric.bus.plugins.IFeedPluginConfig#setActor(java.lang.String)
     */
    @Override
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * @see fabric.bus.plugins.IFeedPluginConfig#getTask()
     */
    @Override
    public String getTask() {
        return task;
    }

    /**
     * @see fabric.bus.plugins.IFeedPluginConfig#setTask(java.lang.String)
     */
    @Override
    public void setTask(String task) {
        this.task = task;
    }

    /**
     * @see fabric.bus.plugins.IFeedPluginConfig#getService()
     */
    @Override
    public ServiceDescriptor getService() {
        return service;
    }

    /**
     * @see fabric.bus.plugins.IFeedPluginConfig#setService(fabric.ServiceDescriptor)
     */
    @Override
    public void setService(ServiceDescriptor service) {
        this.service = service;
    }

}
