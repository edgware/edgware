/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.floodmessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.services.IFloodMessageService;
import fabric.bus.services.IPersistentService;
import fabric.bus.services.impl.BusService;

/**
 *
 */
public class FloodMessageService extends BusService implements IPersistentService {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /* Singleton instance of the service */
    private static FloodMessageService INSTANCE = null;

    /**
     * Gets the singleton instance of the service.
     * 
     * @return the instance
     */
    public static FloodMessageService getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new FloodMessageService();
        }
        return INSTANCE;
    }

    /** The cache of messages that have been handled. */
    private final HashMap<String, MessageCacheEntry> handledMessageCache;

    /** Watchdog for expiring messages from the cache. */
    private MessageCacheWatchdog watchdog = null;

    /**
     * Create an instance of the service
     */
    public FloodMessageService() {

        super(Logger.getLogger("fabric.bus.services"));

        if (INSTANCE == null) {
            INSTANCE = this;
        } else {
            // Only allow a single instance of the service
            throw new UnsupportedOperationException();
        }

        handledMessageCache = new HashMap<String, MessageCacheEntry>();
        watchdog = new MessageCacheWatchdog();
        watchdog.start();
    }

    /**
     * Returns whether this message should be handled by this node.
     * 
     * @param message
     * @return false if this message has already been handled, true otherwise.
     */
    public boolean isDuplicate(String uid) {

        return handledMessageCache.containsKey(uid);
    }

    /**
     * @see IFloodMessageService#addMessage(IFabricMessage, long, boolean)
     */
    public void addMessage(IFabricMessage message, long ttl, boolean retained) {

        // A ttl of 0 means never expire
        long expiryTime = (ttl > 0) ? System.currentTimeMillis() + ttl : 0;
        MessageCacheEntry entry = new MessageCacheEntry(retained, expiryTime, message);
        synchronized (handledMessageCache) {
            handledMessageCache.put(message.getUID(), entry);
        }
    }

    /**
     * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage, INotificationMessage,
     *      IClientNotificationMessage[])
     */
    @Override
    public IServiceMessage handleServiceMessage(IServiceMessage request, INotificationMessage response,
            IClientNotificationMessage[] clientResponses) throws Exception {

        /*
         * // Some useful debug code when this service is identified as the handler // for a message. IRouting routing =
         * message.getRouting(); int age = Integer.parseInt(message.getProperty("age")); if (routing instanceof
         * FloodRouting) { FloodRouting fr = (FloodRouting)routing;
         * System.out.println("@@@ Flood Message ["+message.getUID()+"] from ["+fr.previousNode()+"]  age="+age);
         * String[] nn = fr.nextNodes(); for (int i=0;i<nn.length;i++) { System.out.println("@@@    "+i+":"+nn[i]); } }
         * if (age < 5) { message.setProperty("age", Integer.toString(age+1)); } else { message = null; }
         */

        return request;

    }

    /**
     * @see IPersistentService#stopService()
     */
    @Override
    public void stopService() {

        watchdog.shutdown();
        logger.log(Level.FINE, "Service [{0}] stopped", getClass().getName());
    }

    /**
     * Class representing a message entry in the cache
     */
    class MessageCacheEntry {

        IFabricMessage message;
        long expiryTime;
        boolean retained;

        public MessageCacheEntry(boolean retained, long expiryTime, IFabricMessage message) {

            this.message = message;
            this.expiryTime = expiryTime;
            this.retained = retained;

        }
    }

    /**
     * The Watchdog thread used to expire messages from the cache.
     */
    class MessageCacheWatchdog extends Thread {

        /**
         * Default interval for how often the watchdog runs.
         */
        public static final int WATCHDOG_INTERVAL = 60000;

        /** flag to control running state of the watchdog. */
        boolean running = true;

        /** Object to synchronize lifecycle operations against. */
        Object lifecycle = new Object();

        /** Stops the watchdog. Blocks until the watchdog is stopped. */
        public void shutdown() {

            synchronized (lifecycle) {
                if (running) {
                    running = false;
                    lifecycle.notifyAll();
                    try {
                        lifecycle.wait(WATCHDOG_INTERVAL);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        @Override
        public void run() {

            while (running) {
                synchronized (lifecycle) {
                    try {
                        lifecycle.wait(WATCHDOG_INTERVAL);
                    } catch (InterruptedException e) {
                    }
                    // Check we should still be running
                    if (!running) {
                        break;
                    }
                }

                // Get the current time
                long now = System.currentTimeMillis();

                // Build up a list of the messages that have expired.
                ArrayList<String> toExpire = new ArrayList<String>();

                // Prevent concurrent modification errors
                synchronized (handledMessageCache) {
                    Iterator<MessageCacheEntry> it = handledMessageCache.values().iterator();
                    while (it.hasNext()) {
                        MessageCacheEntry entry = it.next();
                        if (entry.expiryTime > 0 && entry.expiryTime < now) {
                            toExpire.add(entry.message.getUID());
                        }
                    }

                    Iterator<String> uidIt = toExpire.iterator();
                    while (uidIt.hasNext()) {
                        String uid = uidIt.next();
                        handledMessageCache.remove(uid);
                    }
                }

            }
            synchronized (lifecycle) {
                lifecycle.notifyAll();
            }
        }
    }
}
