/*
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.StringTokenizer;

/**
 * Immutable data structure to hold the parts of a Fabric service name associated with a specific task.
 */
public class TaskServiceDescriptor extends ServiceDescriptor {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/*
	 * Class static fields
	 */

	/*
	 * Class fields
	 */

	/** The ID of the task */
	private String task = null;

	/** The string representation of the name of this instance */
	private String toString = null;

	/** The string representation of this instance. */
	private String toStringDescriptor = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance derived from an <code>ServiceDescriptor</code> and a task name.
	 * 
	 * @param task
	 *            the ID of the task.
	 * 
	 * @param service
	 *            the service ID to replicate.
	 */
	public TaskServiceDescriptor(String task, ServiceDescriptor service) {

		super(service);
		this.task = task;
	}

	/**
	 * Constructs a new instance based upon an existing instance.
	 * 
	 * @param source
	 *            the descriptor to replicate.
	 */
	public TaskServiceDescriptor(TaskServiceDescriptor source) {

		this(source.task, source);
	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param task
	 *            the ID of the task.
	 * 
	 * @param platform
	 *            the ID of the platform offering the service.
	 * 
	 * @param system
	 *            the ID of the system offering the service.
	 * 
	 * @param service
	 *            the ID of the service.
	 */
	public TaskServiceDescriptor(String task, String platform, String system, String service) {

		super(platform, system, service);
		this.task = task;

	}

	/**
	 * Constructs a new instance by splitting a Fabric service name of the format:
	 * <p>
	 * <code><em>task-id</em>:<em>platform-id</em>/<em>system-id</em>/<em>service-id</em></code>
	 * </p>
	 * into its component parts.
	 * 
	 * @param descriptor
	 *            the task descriptor to split.
	 */
	public TaskServiceDescriptor(String descriptor) {

		final String usage = "Task service descriptor must be of the form \"<task-id>#<platform-id>/<system-id>/<service-id>\")";

		if (descriptor == null) {
			throw new IllegalArgumentException(usage);
		}

		task = descriptor.substring(0, descriptor.indexOf('#') - 1);
		String serviceDescriptor = descriptor.substring(task.length(), descriptor.length() - 1);

		if (serviceDescriptor == null || serviceDescriptor.equals("")) {
			throw new IllegalArgumentException(usage);
		}

		/* Split the rest of the feed name */
		StringTokenizer serviceTokenizer = new StringTokenizer(serviceDescriptor, "/");
		splitServiceName(serviceTokenizer);

	}

	/**
	 * Gets the ID of the task.
	 * 
	 * @return the task ID.
	 */
	public String task() {

		return task;
	}

	/**
	 * @see fabric.ServiceDescriptor#toString()
	 */
	@Override
	public String toString() {

		if (toString == null) {
			toString = task + '#' + super.toString();
		}

		return toString;

	}

	/**
	 * Generates the string representation of this descriptor.
	 * 
	 * @return the task service descriptor.
	 */
	@Override
	public String toFullDescriptor() {

		if (toStringDescriptor == null) {
			toStringDescriptor = task + '#' + super.toFullDescriptor();
		}

		return toStringDescriptor;
	}

	/**
	 * Answers a new service descriptor instance for this task service descriptor.
	 * 
	 * @return a new service descriptor corresponding to this task service descriptor.
	 */
	public ServiceDescriptor toServiceDescriptor() {

		return new ServiceDescriptor(this);
	}
}
