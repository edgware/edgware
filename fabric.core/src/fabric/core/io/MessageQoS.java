/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2013
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

/**
 * Enumerated type representing the QoS setting associated with an outbound message from the Fabric Manager.
 * <p>
 * Possible settings are:
 * <ul>
 * <li><strong>RELIABLE:</strong> forward the message to the next node in its route using a reliable protocol.</li>
 * <li><strong>BEST_EFFORT:</strong> forward the message to the next node in its route using a "best effort" protocol.</li>
 * <li><strong>DEFAULT:</strong> the default QoS will be used.</li>
 * <li><strong>UNKNOWN:</strong> the QoS has not been set.</li>
 * </ul>
 * </p>
 */

public enum MessageQoS {
	DEFAULT, RELIABLE, BEST_EFFORT, UNKNOWN
}
