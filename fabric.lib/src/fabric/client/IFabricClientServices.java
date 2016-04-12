/*
 * (C) Copyright IBM Corp. 2010
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client;

import fabric.client.services.IClientNotification;
import fabric.client.services.IClientNotificationServices;

/**
 * Interface for classes supporting Fabric client operations.
 */
public interface IFabricClientServices extends IClientNotificationServices, IClientNotification {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

    /*
     * Interface methods
     */

    /**
     * Registers a callback to receive Fabric notifications.
     * 
     * @param callback
     *            the callback.
     */
    public IClientNotification registerClientNotificationCallback(IClientNotification callback);

}
