/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.traceroute;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.MessagePayload;
import fabric.bus.services.impl.BusService;
import fabric.core.xml.XML;

public class TraceRouteService extends BusService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	public TraceRouteService() {

		super(Logger.getLogger("fabric.samples.services"));
	}

	public TraceRouteService(Logger logger) {

		super(Logger.getLogger("fabric.samples.services"));
	}

	/**
	 * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage, INotificationMessage,
	 *      IClientNotificationMessage[])
	 */
	@Override
	public IServiceMessage handleServiceMessage(IServiceMessage request, INotificationMessage response,
			IClientNotificationMessage[] clientResponses) throws Exception {

		logger.log(Level.FINEST, "TraceRouteService: Entering invokeService");
		logger.log(Level.FINEST, "TraceRouteService: request msg: " + request);

		// Gather required information
		// - Get current host
		Fabric fabric = new Fabric();
		String currentNode = fabric.homeNode();

		// - Get time
		String currentTime;
		Format formatter;
		Date date = new Date();
		formatter = new SimpleDateFormat("S");
		currentTime = formatter.format(date);
		System.out.println(date.getTime() + " : " + currentTime);
		logger.log(Level.FINEST, "TraceRouteService: invokeservice: Host: " + currentNode + " Date: " + currentTime);

		// Get current payload, or new payload
		IMessagePayload responseNotificationPayload = null;
		if (request.getPayload() == null) {
			responseNotificationPayload = new MessagePayload();
		} else {
			responseNotificationPayload = request.getPayload();
		}

		/* Assume the XML has been added as plain text into the Fabric message payload */
		logger.log(Level.FINEST, "TraceRouteService: Payload encoding: "
				+ responseNotificationPayload.getPayloadEncoding());
		// String xmlString = responseNotificationPayload.getPayloadText();

		/* Convert the text into an XML object */
		XML payloadXML = new XML();
		/* The XML object uses an XPath-like notation, this method gets the paths of all of the "node" elements */
		String[] xmlPaths = payloadXML.getPaths("/traceroute\\[.*\\]/node\\[.*\\]");
		/*
		 * Now we know how many there are we can add a new one; this will create a new "node" element with the
		 * attributes "name" and "timestamp"
		 */
		int numberOfNodes = xmlPaths.length;
		payloadXML.set("/traceroute/node[" + numberOfNodes + "]@name", currentNode);
		payloadXML.set("/traceroute/node[" + numberOfNodes + "]@timestamp", String.valueOf(date.getTime()));
		/* That's all, we can now write the XML back into the payload */
		responseNotificationPayload.setPayloadText(payloadXML.toString());
		response.setPayload(responseNotificationPayload);

		logger.log(Level.FINEST, "TraceRouteService: Payload: " + response.getPayload().toString());

		// Is this the target Node for the trace?
		// String targetNode = request.getRouting().endNode();
		// if (currentNode.equalsIgnoreCase(targetNode)){
		// Send a return message
		// }

		return request;

	}

}
