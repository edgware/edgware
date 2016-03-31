/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

/**
 * Interface for classes that implement Fabric bus services, i.e. services that execute in a Fabric Manager.
 * <p>
 * Classes implementing this interface are short lived, i.e. they are instantiated to handle a single message.
 * </p>
 */
public interface IBusService extends IService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

}
