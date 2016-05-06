/*
 * (C) Copyright IBM Corp. 2010, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.heartbeat;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.bus.BusIOChannels;
import fabric.bus.SharedChannel;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.plugins.IFabletConfig;
import fabric.bus.plugins.IFabletPlugin;
import fabric.bus.plugins.IPluginConfig;
import fabric.core.io.ICallback;
import fabric.core.io.Message;
import fabric.core.io.OutputTopic;

/**
 * Simple Fablet class that publishes a heartbeat message to a topic periodically.
 */
public class HeartbeatFablet extends FabricBus implements IFabletPlugin, ICallback {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2012";

    /*
     * Class constants
     */

    /** message payload for the heartbeat */
    private static String hebtMessage = "1";

    /*
     * Class fields
     */

    /** The configuration object for this instance */
    private IFabletConfig fabletConfig = null;

    /** Fabric I/O channels */
    private BusIOChannels ioChannels = null;

    /** Topic that heartbeat messages are sent to */
    private OutputTopic heartbeatTopic = null;

    /** Fabric channel used to publish heartbeat */
    private SharedChannel hebtChannel = null;

    /** Interval between heartbeats - 0 means inactive */
    private long sleepInterval = 60000;

    /** Object used to synchronize with the mapper main thread */
    private final Object threadSync = new Object();

    /** Flag used to indicate when the main thread should terminate */
    private boolean isRunning = false;

    public HeartbeatFablet() {

        super(Logger.getLogger("fabric.fabricmanager.fablets"));
    }

    /*
     * Class methods
     */

    /**
     * @see fabric.bus.plugins.IPlugin#startPlugin(fabric.bus.plugins.IPluginConfig)
     */
    @Override
    public void startPlugin(IPluginConfig pluginConfig) {

        fabletConfig = (IFabletConfig) pluginConfig;
        ioChannels = fabletConfig.getFabricServices().ioChannels();
    }

    /**
     * @see fabric.bus.plugins.IPlugin#stopPlugin()
     */
    @Override
    public void stopPlugin() {

        /* Tell the main thread to stop */
        isRunning = false;

        synchronized (threadSync) {
            threadSync.notifyAll();
        }

        /* flatline */
        try {
            hebtChannel.write("0".getBytes());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to send flatline signal: ", e);
        }

        /* Close it */
        try {
            homeNodeEndPoint().closeChannel(hebtChannel, false);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Closure of channel for [{0}] failed: {1}", new Object[] {heartbeatTopic,
                    e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {

            isRunning = true;

            /* read config values */
            String topic = config("heartbeat.topic", "/SYS/1/FABHEBT");
            String interval = config("heartbeat.interval", "60000");

            try {
                sleepInterval = Long.parseLong(interval);
            } catch (Exception e1) {
                /* not a number - default instead */
                sleepInterval = 60000;
                logger.log(Level.WARNING, "Unable to parse sleep interval [{1}]; using default ({0}) instead: {2}",
                        new Object[] {interval, sleepInterval, e1.getMessage()});
                logger.log(Level.FINEST, "Full exception: ", e1);
            }

            if (sleepInterval != 0) {
                /* get the topic to publish on */
                heartbeatTopic = new OutputTopic(ioChannels.receiveLocalFeeds + topic);

                /* Open the channel to start listening for inbound messages */
                hebtChannel = homeNodeEndPoint().openOutputChannel(heartbeatTopic);
            }

            while (isRunning) {

                /* if sleep interval is not 0 at start up, we're beating */
                if (sleepInterval != 0) {

                    /* beat once */
                    hebtChannel.write(hebtMessage.getBytes());

                    /* go to sleep */
                    synchronized (threadSync) {
                        try {
                            threadSync.wait(sleepInterval);
                        } catch (InterruptedException e) {
                        }
                    }

                } else { /* we're dormant */
                    logger.log(Level.INFO, "No heartbeat interval set - going to sleep.");
                    try {
                        synchronized (threadSync) {
                            threadSync.wait();
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }

        } catch (Exception e1) {

            logger.log(Level.WARNING, "Plug-in [{0}] failed with exception: {1}", new Object[] {
                    this.getClass().getName(), e1.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e1);

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

        /* Nothing to do here */
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
}
