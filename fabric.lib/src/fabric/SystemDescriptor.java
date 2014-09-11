/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2008, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Immutable data structure to hold the parts of a Fabric system name.
 */
public class SystemDescriptor {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2012";

	/*
	 * Class fields
	 */

	/** The ID of the platform to which the system is connected. */
	private String platform = null;

	/** The ID of the system. */
	private String system = null;

	/** The string representation of this instance. */
	private String toString = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	protected SystemDescriptor() {

	}

	/**
	 * Constructs a new instance based upon an existing instance.
	 * 
	 * @param source
	 *            the system descriptor to replicate.
	 */
	public SystemDescriptor(SystemDescriptor source) {

		platform = source.platform;
		system = source.system;

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param platform
	 *            the ID of the platform.
	 * 
	 * @param system
	 *            the ID of the system.
	 */
	public SystemDescriptor(String platform, String system) {

		this.platform = platform;
		this.system = system;

	}

	/**
	 * Constructs a new instance by splitting a system descriptor the format:
	 * <p>
	 * <code>platform-id/system-id</code>
	 * </p>
	 * into its component parts.
	 * 
	 * @param name
	 *            the system descriptor to split.
	 */
	public SystemDescriptor(String name) {

		StringTokenizer systemDescriptorTokenizer = new StringTokenizer(name, "/");

		splitSystemName(systemDescriptorTokenizer);

	}

	/**
	 * Splits a system descriptor into its component parts and records them.
	 * 
	 * @param nameTokenizer
	 *            the tokenizer used to split the name.
	 */
	protected void splitSystemName(StringTokenizer nameTokenizer) {

		try {

			platform = nameTokenizer.nextToken();
			system = nameTokenizer.nextToken();

		} catch (NoSuchElementException e) {

			throw new IllegalArgumentException(
					"Invalid number of tokens in system descriptor (must be 2 i.e. '<platform-id>/<system-id>')");

		}
	}

	/**
	 * Answers the ID of the platform to which the system is connected.
	 * 
	 * @return the platform ID.
	 */
	public String platform() {

		return platform;
	}

	/**
	 * Answers the ID of the system.
	 * 
	 * @return the system ID.
	 */
	public String system() {

		return system;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		/* If we need to generate the string form of this instance... */
		if (toString == null) {
			toString = platform + '/' + system;
		}

		return toString;

	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		/* To hold the result */
		boolean isEqual = false;

		if (obj != null) {

			isEqual = toString().equals(obj.toString());

		}

		return isEqual;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return toString().hashCode();

	}
}
