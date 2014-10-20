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
 * Immutable data structure to hold the parts of a Fabric service name.
 */
public class ServiceDescriptor extends SystemDescriptor {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2014";

	/*
	 * Class fields
	 */

	/** The ID of the service. */
	private String service = null;

	/** The type of the service. */
	private String serviceType = null;

	/** The operation mode of the service. */
	private String mode = null;

	/** The string representation of the name of this instance */
	private String toString = null;

	/** The string representation of this instance. */
	private String toStringDescriptor = null;

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
		serviceType = source.serviceType;
		mode = source.mode;

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

		String exceptionMessage = "Invalid number of tokens in system descriptor (valid format is \"<platform-id>[:<platform-type>]/<system-id>[:<system-type>]/<service-id>[:service-type[:service-mode]]\")";

		try {

			splitSystemName(nameTokenizer);

			String[] serviceParts = nameTokenizer.nextToken().split(":");

			switch (serviceParts.length) {
			case 3:
				mode = serviceParts[2];
			case 2:
				serviceType = serviceParts[1];
			case 1:
				service = serviceParts[0];
				break;
			default:
				throw new IllegalArgumentException(exceptionMessage);
			}

		} catch (NoSuchElementException e) {

			throw new IllegalArgumentException(exceptionMessage);

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
	 * Answers the type of the service.
	 * 
	 * @return the service type, or <code>null</code> if it has not been set in the descriptor.
	 */
	public String serviceType() {

		return serviceType;
	}

	/**
	 * Answers the mode of the service.
	 * 
	 * @return the service mode, or <code>null</code> if it has not been set in the descriptor.
	 */
	public String mode() {

		return mode;
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

		if (toString == null) {
			toString = super.toString() + '/' + service;
		}

		return toString;

	}

	/**
	 * Generates the string representation of this descriptor.
	 * 
	 * @return the service descriptor.
	 */
	@Override
	public String toFullDescriptor() {

		if (toStringDescriptor == null) {
			toStringDescriptor = super.toFullDescriptor() + '/' + service
					+ ((serviceType != null) ? ':' + serviceType : "") + ((mode != null) ? ':' + mode : "");
		}

		return toStringDescriptor;

	}

	/**
	 * Answers a new system descriptor instance for this service descriptor.
	 * 
	 * @return a new system descriptor corresponding to this service descriptor.
	 */
	public SystemDescriptor toSystemDescriptor() {

		return new SystemDescriptor(this);
	}
}
