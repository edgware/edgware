/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import fabric.Fabric;
import fabric.bus.messages.IPlatformNotificationMessage;
import fabric.client.services.PlatformNotificationService;
import fabric.core.xml.XML;

/**
 * A Fabric platform notification message, used by the Fabric to notify platforms of Fabric events.
 */
public class PlatformNotificationMessage extends ServiceMessage implements IPlatformNotificationMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Class fields
	 */

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public PlatformNotificationMessage() {

		super();

		construct();

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * Initializes an instance.
	 */
	private void construct() {

		/* Set the service name: i.e. indicate that this is a message for the platform message handler */
		setServiceName(PlatformNotificationService.class.getName());

		/* Indicate that this is a built-in Fabric plug-in */
		setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);

		/* Indicate that this message should be not actioned along the route to the platform */
		setActionEnRoute(false);

	}

	/**
	 * @see fabric.bus.messages.impl.FabricMessage#init(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void init(String element, XML messageXML) throws Exception {

		super.init(element, messageXML);

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * @see fabric.bus.messages.impl.FabricMessage#embed(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void embed(String element, XML messageXML) throws Exception {

		super.embed(element, messageXML);

	}

	/**
	 * @see fabric.bus.messages.IPlatformNotificationMessage#getPlatform()
	 */
	@Override
	public String getPlatform() {

		return getProperty(PROPERTY_PLATFORM);

	}

	/**
	 * @see fabric.bus.messages.IPlatformNotificationMessage#setPlatform(java.lang.String)
	 */
	@Override
	public void setPlatform(String platform) {

		setProperty(PROPERTY_PLATFORM, platform);

	}
}
