/*
 * (C) Copyright IBM Corp. 2013
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
package fabric.services.messageforwarding;

/**
 * Enumerated type representing the action to be performed on an outbound message from the Fabric Manager.
 * <p>
 * Possible actions are:
 * <ul>
 * <li><strong>FORWARD:</strong> forward the message to the next node in its route.</li>
 * <li><strong>DELIVER:</strong> deliver the message to an actor connected to the current node.</li>
 * </ul>
 * </p>
 */

public enum Action {
	FORWARD, DELIVER, UNKNOWN
}
