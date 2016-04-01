/*
 * (C) Copyright IBM Corp. 2010
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

import fabric.bus.messages.IServiceMessage;

/**
 * Interface for classes handling Fabric topology change information.
 */
public interface ITopologyChange {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

    /*
     * Interface constants
     */

    /*
     * Interface methods
     */

    /**
     * Handles a message indicating a change in the connectivity status of the client's home node.
     *
     * @param message
     *            the message.
     */
    public void homeNodeUpdate(IServiceMessage message);

    /**
     * Handles a message indicating a change in Fabric topology.
     *
     * @param message
     *            the message.
     */
    public void topologyUpdate(IServiceMessage message);

}
