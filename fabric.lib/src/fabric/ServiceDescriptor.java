/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2008, 2013
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Immutable data structure to hold the parts of a Fabric service name.
 */
public class ServiceDescriptor extends SystemDescriptor {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2013";

	/*
	 * Class fields
	 */

	/** The ID of the service. */
	private String service = null;

	/** The string representation of this instance. */
	private String toString = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	protected ServiceDescriptor() {

	}

	/**
	 * Constructs a new instance based upon an existing instance.
	 * 
	 * @param source
	 *            the descriptor to replicate.
	 */
	public ServiceDescriptor(ServiceDescriptor source) {

		super(source);
		service = source.service;

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param platform
	 *            the ID of the platform hosting the service.
	 * 
	 * @param system
	 *            the ID of the system offering the service.
	 * 
	 * @param service
	 *            the ID of the service.
	 */
	public ServiceDescriptor(String platform, String system, String service) {

		super(platform, system);
		this.service = service;

	}

	/**
	 * Constructs a new instance by splitting a Fabric service descriptor of the format:
	 * <p>
	 * <code>platform-id/system-id/service-id</code>
	 * </p>
	 * into its component parts.
	 * 
	 * @param name
	 *            the service name to split.
	 */
	public ServiceDescriptor(String name) {

		StringTokenizer serviceDescriptorTokenizer = new StringTokenizer(name, "/");

		splitServiceName(serviceDescriptorTokenizer);

	}

	/**
	 * Splits a Fabric service descriptor into its component parts and records them.
	 * 
	 * @param nameTokenizer
	 *            the tokenizer used to split the name.
	 */
	protected void splitServiceName(StringTokenizer nameTokenizer) {

		splitSystemName(nameTokenizer);

		try {

			service = nameTokenizer.nextToken();

		} catch (NoSuchElementException e) {

			throw new IllegalArgumentException(
					"Invalid number of tokens in service descriptor (must be 3 i.e. '<platform-id>/<system-id>/<service-id>')");

		}
	}

	/**
	 * Answers the ID of this service.
	 * 
	 * @return the ID of the service.
	 */
	public String service() {

		return service;
	}

	/**
	 * Extracts the service descriptor from a broker topic name of the form:
	 * <p>
	 * <code>topic-prefix/platform-id/system-id/service-id</code> <br>
	 * </p>
	 * The descriptor is considered to be the final three segments of the topic name, following the
	 * <em>topic-prefix</em> where "<code>/</code>" is the segment separator.
	 * <p>
	 * This method will have no effect if the topic name is <code>null</code> or an empty string.
	 * </p>
	 * 
	 * @param topic
	 *            the topic name.
	 * 
	 * @return the descriptor (if any).
	 * 
	 */
	public static String extract(String topic) {

		/* To hold the result */
		String serviceDescriptor = null;

		if (topic != null) {

			int expectedSegments = 3;

			/* Find the start of the descriptor */

			char[] topicChars = topic.toCharArray();
			int serviceDescriptorStart = topicChars.length - 1;
			int segmentCount = 0;

			/* While we haven't found the start of the descriptor... */
			for (; segmentCount < expectedSegments && serviceDescriptorStart >= 0; serviceDescriptorStart--) {

				if (topicChars[serviceDescriptorStart] == '/') {

					segmentCount++;

				}
			}

			/* If we found the start of the descriptor... */
			if (segmentCount == expectedSegments) {

				/* Extract the descriptor name */
				serviceDescriptor = topic.substring(serviceDescriptorStart + 2);

			}

		}

		return serviceDescriptor;

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		/* If we need to generate the string form of this instance... */
		if (toString == null) {
			toString = super.toString() + '/' + service;
		}

		return toString;

	}
}