/*
 * (C) Copyright IBM Corp. 2012.
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fabricmanager.osgi;

import java.util.HashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

import fabric.bus.services.IPluginRegistry;

public class PluginTracker extends BundleTracker implements IPluginRegistry {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	HashMap<String, Bundle> registeredPlugins;
	BundleContext context;

	public PluginTracker(BundleContext context) {

		super(context, Bundle.ACTIVE, null);

		this.context = context;
		this.registeredPlugins = new HashMap<String, Bundle>();

		Bundle[] bundles = context.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			loadPlugins(bundles[i]);
		}
	}

	public void loadPlugins(Bundle bundle) {

		String cnHeader = (String) bundle.getHeaders().get("Fabric-Plugin");
		if (cnHeader != null) {
			String[] classNames = cnHeader.split(",");
			for (int i = 0; i < classNames.length; i++) {
				String className = classNames[i].trim();
				System.out.printf("Registering plugin %s from bundle %s", className, bundle.getSymbolicName());
				if (!this.registeredPlugins.containsKey(className)) {
					this.registeredPlugins.put(className, bundle);
				}
			}
		}
	}

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {

		loadPlugins(bundle);
		return bundle;
	}

	@Override
	public Object getPluginInstance(String classname) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		Object instance = null;

		if (registeredPlugins.containsKey(classname)) {
			Class<?> clazz;
			Bundle bundle = registeredPlugins.get(classname);
			clazz = bundle.loadClass(classname);
			instance = clazz.newInstance();
		}

		return instance;
	}

}
