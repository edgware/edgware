/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

/**
 * Interface for classes that implement persistent (i.e. long lived) Fabric services.
 * <p>
 * Classes implementing this interface are long lived, i.e. they are instantiated and available to handle multiple
 * messages until explicitly stopped.
 * </p>
 */
public interface IPersistentService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Stops this service.
	 */
	public void stopService();

}
