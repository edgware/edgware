/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

/**
 * Class representing an I/O topic.
 */
public abstract class Topic {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class fields
	 */

	/** The name of the topic. */
	private String name = null;

	/*
	 * Class methods
	 */

	/**
	 * Construct a new instance.
	 * 
	 * @param name
	 *            the name of the topic.
	 */
	public Topic(String name) {

		this.name = name;
	}

	/**
	 * Answers the name of the topic.
	 * 
	 * @return the name.
	 */
	public String name() {

		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return name;
	}
}
