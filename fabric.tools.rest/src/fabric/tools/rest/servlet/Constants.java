/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.rest.servlet;

/**
 * A single file that contains all constants relating to the websockets and Fabric
 */
public final class Constants {

	// Copyright notice
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	// Global socket settings
	public static final int timeout = 60 * 10 * 1000;

	// Error messages
	public static final String JSONParse = "There was an error parsing the inputted JSON.";
	public static final String JSONMapping = "The inputted JSON was unable to be correctly mapped to an object.";
	public static final String IOException = "An I/O exception has occurred";
	public static final String ConnectException = "Unable to connect to the Fabric Registry.";
	public static final String SocketTimeout = "The socket timeout window has elasped, please refresh.";
	public static final String MQTTConnectionLost = "The MQTT connection has been lost, please refresh.";
}
