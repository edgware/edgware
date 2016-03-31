/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.interfaces.json.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fabric.services.jsonclient.MQTTAdapter;

/**
 * Activator class for the JSON Fabric interface bundle.
 */
public class Activator implements BundleActivator {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class fields
	 */

	private MQTTAdapter mqttAdapter = null;

	/*
	 * Class methods
	 */

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {

		try {

			mqttAdapter = new MQTTAdapter();
			mqttAdapter.init();
			System.out.println("JSON interface: started");

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {

		System.out.println("JSON interface: stopping");
		mqttAdapter.stop();

	}

}