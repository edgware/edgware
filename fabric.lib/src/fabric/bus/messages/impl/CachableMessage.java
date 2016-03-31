/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import fabric.Fabric;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.routing.impl.StaticRouting;
import fabric.bus.services.impl.MessageCacheService;


/**
 * A Fabric Service Message that can be stored in the message cache.
 */
public class CachableMessage extends ServiceMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	// Property names
	private static final String SOURCE = "f:src";
	private static final String DESTINATION = "f:dst";
	
	
	/**
	 * Create a default message.
	 */
	public CachableMessage() {
		super();
		
		setServiceName(MessageCacheService.class.getName());
		setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);
		setActionEnRoute(false);
	}
	
	/**
	 * Create a default message, with routing to the specified node preset.
	 * @param node The node to send this message to
	 */
	public CachableMessage(String node) {
		this();
		
		StaticRouting messageRouting = new StaticRouting(new String[] { node });
		this.setRouting(messageRouting);

	}
	
	/**
	 * Sets the source property of the message
	 * @param src
	 */
	public void setSource(String src) {
		setProperty(CachableMessage.SOURCE,src);
	}
	
	/**
	 * Sets the destination property of the message
	 * @param dst
	 */
	public void setDestination(String dst) {
		setProperty(CachableMessage.DESTINATION,dst);
	}
	
	/**
	 * Sets the message body
	 * @param msg
	 */
	public void setMessage(String msg) {
		MessagePayload payload = new MessagePayload();
		payload.setPayloadText(msg);
		setPayload(payload);
	}
	
	/**
	 * @return the source of the message
	 */
	public String getSource() {
		return getProperty(CachableMessage.SOURCE);
	}
	
	/**
	 * @return the destination of the message
	 */
	public String getDestination() {
		return getProperty(CachableMessage.DESTINATION);
	}
	
	/**
	 * @return the body of the message
	 */
	public String getMessage() {
		IMessagePayload payload = this.getPayload();
		if (payload != null) {
			return payload.getPayloadText();
		} else {
			return null;
		}
	}
}
