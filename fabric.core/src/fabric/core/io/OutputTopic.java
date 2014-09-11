/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

/**
 * Class representing an outbound (output) topic.
 */
public class OutputTopic extends Topic {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class methods
	 */

	/**
	 * Construct a new instance.
	 * 
	 * @param name
	 *            the name of the topic.
	 */
	public OutputTopic(String name) {

		super(name);
	}
}
