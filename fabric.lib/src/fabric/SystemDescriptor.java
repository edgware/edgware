/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2008, 2014
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
public class SystemDescriptor extends PlatformDescriptor {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2014";

	/*
	 * Class fields
	 */

	/** The ID of the system. */
	private String system = null;

	/** The type of the system. */
	private String systemType = null;

	/** The string representation of this instance. */
	private String toString = null;

	/** The string representation of the name of this instance. */
	private String toName = null;

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

		super(source);
		system = source.system;
		systemType = source.systemType;

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

		super(platform);
		this.system = system;

	}

	/**
	 * Constructs a new instance by splitting a system descriptor the format:
	 * <p>
	 * <code>platform-id[:platform-type]/system-id[:system-type]</code>
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

		String exceptionMessage = "Invalid number of tokens in system descriptor (valid format is \"<platform-id>[:<platform-type>]/<system-id>[:<system-type>]\")";

		try {

			splitPlatformName(nameTokenizer);

			String[] systemParts = nameTokenizer.nextToken().split(":");

			switch (systemParts.length) {
			case 2:
				systemType = systemParts[1];
			case 1:
				system = systemParts[0];
				break;
			default:
				throw new IllegalArgumentException(exceptionMessage);
			}

		} catch (NoSuchElementException e) {

			throw new IllegalArgumentException(exceptionMessage);

		}
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
	 * Answers the type of the system.
	 * 
	 * @return the system type, or <code>null</code> if it has not been set in the descriptor.
	 */
	public String systemType() {

		return systemType;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		/* If we need to generate the string form of this instance... */
		if (toString == null) {
			toString = super.toString() + '/' + system + ((systemType != null) ? ':' + systemType : "");
		}

		return toString;

	}

	/**
	 * Generates the name of this instance.
	 * 
	 * @return the name of the system.
	 */
	@Override
	public String toName() {

		return super.toName() + '/' + system;

	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return toString().hashCode();

	}
}
