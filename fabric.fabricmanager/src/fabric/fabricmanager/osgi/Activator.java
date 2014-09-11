/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2006, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fabricmanager.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fabric.Fabric;
import fabric.fabricmanager.FabricManager;

public class Activator implements BundleActivator {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2014";

	/*
	 * Class fields
	 */

	BundleContext context;

	FabricManager fabricManager = null;

	PluginTracker pluginTracker;

	AdminListener adminListener;

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

			if (System.getenv("FABRIC_HOME") == null) {
				throw new Exception("FABRIC_HOME not set");
			}

			this.context = context;
			pluginTracker = new PluginTracker(context);
			pluginTracker.open();

			fabricManager = new FabricManager();
			Fabric.setPluginRegistry(pluginTracker);
			fabricManager.init();
			System.out.println("Fabric Manager: started");

			adminListener = new AdminListener(context, fabricManager.nodeName());
			adminListener.start();

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

		adminListener.shutdown();
		System.out.println("Fabric Manager stopping...");
		fabricManager.shutdown();
		System.out.println("Fabric Manager stopped.");
	}

}