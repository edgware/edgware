/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

/**
 * Interface for classes providing shutdown hook actions for Fabric applications.
 */
public interface IFabricShutdownHookAction {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Shutdown hook entry point.
	 * <p>
	 * The Fabric shutdown hook will invoke this method to perform pre-termination clean-up operations.
	 * </p>
	 */
	public void shutdown();

}
