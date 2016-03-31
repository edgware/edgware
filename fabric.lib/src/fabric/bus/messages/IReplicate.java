/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

/**
 * Interface for classes that can create a deep copy (replica) of themselves.
 * <p>
 * Classes implementing this interface should ensure that the replica copy is created as efficiently as possible.
 * </p>
 */
public interface IReplicate {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Answers a deep copy of this instance.
	 * 
	 * @return a replica of this instance.
	 */
	public IReplicate replicate();

}