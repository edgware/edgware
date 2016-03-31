/*
 * (C) Copyright IBM Corp. 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

/**
 * Interface for classes that introduce new functionality into the Fabric Manager. Each instance is launched by the
 * Fabric manager, and runs on a separate thread in parallel with the Fabric Manager.
 */
public interface IFabletPlugin extends Runnable, IPlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008";

}
