/*
 * (C) Copyright IBM Corp. 2007, 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus;

import fabric.core.io.InputTopic;
import fabric.core.io.OutputTopic;

/**
 * Data structure holding topic names and associated channels for Fabric I/O.
 */
public class BusIOChannels {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2008";

	/*
	 * Class fields
	 */

	/*
	 * Connection messages (last will and testament)
	 */

	/** The topic upon which to listen for broker connection messages. */
	public InputTopic connectionComands = null;

	/** The channel upon which to listen for broker connection messages. */
	public SharedChannel connectionCommandsChannel = null;

	/** The topic upon which to send broker connection messages. */
	public OutputTopic sendConnectionMessages = null;

	/** The channel upon which to send broker connection messages. */
	public SharedChannel sendConnectionMessagesChannel = null;

	/*
	 * Command (service) messages
	 */

	/** The topic upon which to listen for commands. */
	public InputTopic receiveCommands = null;

	/** The channel upon which to listen for commands. */
	public SharedChannel receiveCommandsChannel = null;

	/** The topic upon which to listen for platform commands. */
	public InputTopic receivePlatformCommands = null;

	/** The channel upon which to listen for platform commands. */
	public SharedChannel receivePlatformCommandsChannel = null;

	/** The topic upon which to listen for service commands. */
	public InputTopic receiveServiceCommands = null;

	/** The channel upon which to listen for service commands. */
	public SharedChannel receiveServiceCommandsChannel = null;

	/** The topic via which commands are sent to the local Fabric Manager. */
	public OutputTopic sendCommands = null;

	/** The channel via which commands are sent to the local Fabric Manager. */
	public SharedChannel sendCommandsChannel = null;

	/**
	 * The channel to which Fabric client commands are to be sent (note that the client-specific topic must be provided
	 * when this channel is used)
	 */
	public SharedChannel sendClientCommandsChannel = null;

	/** The channel to which Fabric platform notifications are to be sent. */
	public SharedChannel sendPlatformCommandsChannel = null;

	/** The channel to which Fabric service notifications are to be sent. */
	public SharedChannel sendServiceCommandsChannel = null;

	/** The root topic upon which to listen for messages from local replay feeds. */
	public InputTopic receiveLocalReplayFeeds = null;

	/** The root channel upon which to listen for messages from local replay feeds. */
	public SharedChannel receiveLocalReplayFeedsChannel = null;

	/** The root topic upon which to listen for messages from local feeds. */
	public InputTopic receiveLocalFeeds = null;

	/** The root channel upon which to listen for messages from local feeds. */
	public SharedChannel receiveLocalFeedsChannel = null;

	/** The root topic to which local feed messages are to be sent. */
	public OutputTopic sendLocalFeeds = null;

	/** The root channel to which local feed messages are to be sent. */
	public SharedChannel sendLocalFeedsChannel = null;

	/** The root topic upon which to listen for Fabric bus messages. */
	public InputTopic receiveBus = null;

	/** The root channel upon which to listen for Fabric bus messages. */
	public SharedChannel receiveBusChannel = null;

	/** The root topic upon which to listen for messages for local consumption. */
	public InputTopic receiveLocalSubscription = null;

	/** The root channel upon which to listen for messages for local consumption. */
	public SharedChannel receiveLocalSubscriptionChannel = null;

	/** The root topic to which messages for local consumption are to be sent. */
	public OutputTopic sendLocalSubscription = null;

	/** The root channel to which messages for local consumption are to be sent. */
	public SharedChannel sendLocalSubscriptionChannel = null;

}
