/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

import fabric.bus.IBusServices;

/**
 * Interface for Fabric service dispatchers.
 */
public interface IBusServiceDispatcher extends IServiceDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Answers the interface to Fabric management services.
	 * 
	 * @return the accessor for Fabric management services.
	 */
	public IBusServices getBusServices();

	/**
	 * Sets the interface to Fabric management services.
	 * 
	 * @param busServices
	 *            the accessor for Fabric management services.
	 */
	public void setBusServices(IBusServices busServices);

}
