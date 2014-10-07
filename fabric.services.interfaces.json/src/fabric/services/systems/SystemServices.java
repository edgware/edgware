/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.FabricBus;
import fabric.ServiceDescriptor;
import fabric.TaskServiceDescriptor;
import fabric.bus.SharedChannel;
import fabric.bus.feeds.ISubscription;
import fabric.bus.feeds.impl.Subscription;
import fabric.core.io.OutputTopic;
import fabric.registry.FabricRegistry;
import fabric.registry.Service;
import fabric.registry.ServiceFactory;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;

/**
 * Class providing access to a system's services.
 * <p>
 * The services associated with a system are derived from the system definition that is recorded in the Registry.
 * </p>
 * <p>
 * Subscriptions are set up to any request/response services to which this service instance responds, notification
 * listener services, and input stream services.
 * <p>
 * Two sets of output channels are opened:
 * <ul>
 * <li><strong>On-ramp</strong> channels to send raw payload data onto the Fabric for a new data feed created by this
 * service.</li>
 * <li><strong>Bus</strong> channels to send Fabric messages onto the Fabric when this service is delivering in-flight
 * message processing.</li>
 * </ul>
 * </p>
 */
public class SystemServices extends FabricBus {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012, 2014";

	/*
	 * Class fields
	 */

	/** The container managing this system instance. */
	private SystemRuntime systemRuntime = null;

	/** The table of input feed subscriptions; the key is the remote feed descriptor to which the input is mapped. */
	private final HashMap<ServiceDescriptor, ISubscription> wiredInputFeeds = new HashMap<ServiceDescriptor, ISubscription>();

	/**
	 * Table mapping upstream output feeds to their local input feeds (i.e. the feed wiring); the key is the remote feed
	 * descriptor.
	 */
	private final HashMap<ServiceDescriptor, ServiceDescriptor> wiredInputFeedMappings = new HashMap<ServiceDescriptor, ServiceDescriptor>();

	/** The table of request/response subscriptions; the key is feed descriptor. */
	private final HashMap<ServiceDescriptor, ISubscription> requestResponseFeeds = new HashMap<ServiceDescriptor, ISubscription>();

	/** The table of one way subscriptions; the key is feed descriptor. */
	private final HashMap<ServiceDescriptor, ISubscription> oneWayFeeds = new HashMap<ServiceDescriptor, ISubscription>();

	/** The table of solicit response subscriptions; the key is the local feed descriptor. */
	private final HashMap<ServiceDescriptor, ISubscription> solicitResponseFeeds = new HashMap<ServiceDescriptor, ISubscription>();

	/**
	 * Table mapping solicit response feeds to their remote remote request/response counterparts; the key is the local
	 * solicit response feed descriptor.
	 */
	private final HashMap<ServiceDescriptor, ArrayList<ServiceDescriptor>> wiredSolicitResponseFeedMappings = new HashMap<ServiceDescriptor, ArrayList<ServiceDescriptor>>();

	/**
	 * Table mapping notification feeds to their remote remote one way counterparts; the key is the local notification
	 * feed descriptor.
	 */
	private final HashMap<ServiceDescriptor, ArrayList<ServiceDescriptor>> wiredNotificationFeedMappings = new HashMap<ServiceDescriptor, ArrayList<ServiceDescriptor>>();

	/**
	 * The table of Fabric channels to which this service instance will write <em>on-ramp</em> messages, i.e. raw
	 * payloads (the key is the ID of the output feed).
	 */
	private final HashMap<ServiceDescriptor, SharedChannel> onrampOutputFeeds = new HashMap<ServiceDescriptor, SharedChannel>();

	/**
	 * The table of Fabric channels to which this service instance will write <em>bus</em> messages (the key is the ID
	 * of the output feed).
	 */
	private final HashMap<ServiceDescriptor, SharedChannel> busOutputFeeds = new HashMap<ServiceDescriptor, SharedChannel>();

	/*
	 * Feed lists (recorded for the convenience of the calling code)
	 */

	/** List of this service's INPUT feeds. */
	private final ArrayList<String> inputFeedList = new ArrayList<String>();

	/** List of this service's OUTPUT feeds. */
	private final ArrayList<String> outputFeedList = new ArrayList<String>();

	/** List of this service's NOTIFY feeds. */
	private final ArrayList<String> notifyFeedList = new ArrayList<String>();

	/** List of this service's ONE_WAY feeds. */
	private final ArrayList<String> oneWayFeedList = new ArrayList<String>();

	/** List of this service's SOLICIT_RESPONSE feeds. */
	private final ArrayList<String> solicitResponseFeedList = new ArrayList<String>();

	/** List of this service's REQUEST_RESPONSE feeds. */
	private final ArrayList<String> requestResponseFeedList = new ArrayList<String>();

	/** Flag indicating if Registry queries should be local or distributed. */
	private boolean doQueryLocal = false;

	/*
	 * Registry queries
	 */

	/** Query for input feeds wired to the output feeds of remote services. */
	private String WIRING_QUERY = null;

	/*
	 * Static code
	 */

	{

		/* Query for input feeds; parameters (%s) are for the platform ID and the service ID respectively */
		WIRING_QUERY = "select ";
		WIRING_QUERY += "sw.to_service_platform_id, "; // Column 0
		WIRING_QUERY += "sw.to_service_id, "; // Column 1
		WIRING_QUERY += "sw.to_interface_id, "; // Column 2
		WIRING_QUERY += "sw.from_interface_id, "; // Column 3
		WIRING_QUERY += "df.direction "; // Column 4
		WIRING_QUERY += "from ";
		WIRING_QUERY += "service_wiring as sw, ";
		WIRING_QUERY += "data_feeds as df ";
		WIRING_QUERY += "where ";
		WIRING_QUERY += "sw.from_service_platform_id='%s' and sw.from_service_id='%s' and ";
		WIRING_QUERY += "sw.from_service_platform_id=df.platform_id and ";
		WIRING_QUERY += "sw.from_service_id=df.service_id and ";
		WIRING_QUERY += "sw.from_interface_id=df.id and ";
		WIRING_QUERY += "(df.direction='" + Service.MODE_INPUT_FEED + "' or df.direction='"
				+ Service.MODE_SOLICIT_RESPONSE + "' or df.direction='" + Service.MODE_NOTIFICATION + "')";

	}

	/*
	 * Class methods
	 */

	public SystemServices() {

		this(Logger.getLogger("fabric.services.systems"));
	}

	public SystemServices(Logger logger) {

		this.logger = logger;
	}

	/**
	 * Constructor.
	 * 
	 * @param systemRuntime
	 *            the container for this service instance.
	 */
	public SystemServices(SystemRuntime componentContainer) {

		this.systemRuntime = componentContainer;

		/* Determine if Registry queries should be local or distributed */
		doQueryLocal = Boolean.parseBoolean(config("fabric.composition.queryLocal", "false"));

	}

	/**
	 * Initializes all of the feeds (input and output) associated with this service instance, including wiring to remote
	 * services as indicated in the Fabric Registry.
	 * 
	 * @throws Exception
	 */
	public void initFeeds() throws Exception {

		initWiredFeeds();
		initUnwiredFeeds();
		enumerateFeeds();

	}

	/**
	 * Enumerate all of the services, by type, for this system.
	 */
	private void enumerateFeeds() {

		/* Get the list of feeds for the service */
		ServiceFactory serviceFactory = FabricRegistry.getServiceFactory(doQueryLocal);
		Service[] services = serviceFactory.getServicesBySystem(systemRuntime.systemDescriptor().platform(),
				systemRuntime.systemDescriptor().system());

		/* For each service... */
		for (int f = 0; f < services.length; f++) {

			/* Sort the services into their types and save */

			String serviceMode = services[f].getMode();
			String serviceID = services[f].getId();

			if (serviceMode.equals(Service.MODE_INPUT_FEED)) {

				inputFeedList.add(serviceID);

			} else if (serviceMode.equals(Service.MODE_OUTPUT_FEED)) {

				outputFeedList.add(serviceID);

			} else if (serviceMode.equals(Service.MODE_NOTIFICATION)) {

				notifyFeedList.add(serviceID);

			} else if (serviceMode.equals(Service.MODE_LISTENER)) {

				oneWayFeedList.add(serviceID);

			} else if (serviceMode.equals(Service.MODE_SOLICIT_RESPONSE)) {

				solicitResponseFeedList.add(serviceID);

			} else if (serviceMode.equals(Service.MODE_REQUEST_RESPONSE)) {

				requestResponseFeedList.add(serviceID);

			}
		}
	}

	/**
	 * Lookup and iniitalize wiring information for this system instance:
	 * <ol>
	 * <li>Subscriptions will be made for <em>input</em> feed wirings.</li>
	 * <li>Mappings between local <em>solicit request</em> feeds and remote <em>request response</em> services will be
	 * recorded.</li>
	 * </ol>
	 * 
	 * @throws Exception
	 */
	private void initWiredFeeds() throws Exception {

		/* Build the query for the wiring (sources) of the input feeds */
		String wiringQuery = Fabric.format(WIRING_QUERY, systemRuntime.systemDescriptor().platform(), systemRuntime
				.systemDescriptor().system());

		/* Run the query */

		Object[] resultTable = null;

		try {

			resultTable = FabricRegistry.runQuery(wiringQuery, false);

		} catch (PersistenceException e) {

			String message = Fabric.format("Cannot get wiring for service %s", systemRuntime.systemDescriptor());
			logger.log(Level.SEVERE, message);
			throw new Exception(message, e);

		}

		/* For each wiring entry... */
		for (int w = 0; resultTable != null && w < resultTable.length; w++) {

			/* Extract the columns of the next row */
			Object[] nextRow = (Object[]) resultTable[w];
			String toPlatformID = (String) nextRow[0];
			String toServiceID = (String) nextRow[1];
			String toFeedID = (String) nextRow[2];
			String fromFeedID = (String) nextRow[3];
			String direction = (String) nextRow[4];

			/* If this wires the output of another service to the input of this one... */
			if (direction.equals(Service.MODE_INPUT_FEED)) {

				ServiceDescriptor outputFeed = new ServiceDescriptor(toPlatformID, toServiceID, toFeedID);
				wireInputFeed(outputFeed, fromFeedID);

			}
			/* Else if this is a request/response wiring... */
			else if (direction.equals(Service.MODE_SOLICIT_RESPONSE)) {

				/* Build the descriptor for the remote request/response endpoint */
				ServiceDescriptor requestResponseDescriptor = new ServiceDescriptor(toPlatformID, toServiceID, toFeedID);

				/* Build the descriptor for the local solicit response endpoint */
				ServiceDescriptor solicitResponseDescriptor = new ServiceDescriptor(systemRuntime.systemDescriptor()
						.platform(), systemRuntime.systemDescriptor().system(), fromFeedID);

				/* Build the task feed descriptor */
				TaskServiceDescriptor solicitResponseTaskDescriptor = new TaskServiceDescriptor("DEFAULT",
						solicitResponseDescriptor);

				try {

					/* Subscribe to the request feed */
					ISubscription solicitResponseSubscription = new Subscription(systemRuntime.fabricClient());
					solicitResponseSubscription.subscribe(solicitResponseTaskDescriptor, systemRuntime);

					/* Record the subscription */
					solicitResponseFeeds.put(solicitResponseDescriptor, solicitResponseSubscription);

				} catch (Exception e) {

					String message = Fabric.format("Cannot subscribe to request feed '%s' for service instance '%s':",
							e, solicitResponseTaskDescriptor.toString(), systemRuntime.toString());
					logger.log(Level.SEVERE, message);
					throw new Exception(message, e);

				}

				/* Get the list of mappings for the local name */
				ArrayList<ServiceDescriptor> descriptorList = wiredSolicitResponseFeedMappings
						.get(solicitResponseDescriptor);

				/* If the list has not been created... */
				if (descriptorList == null) {

					/* Create it */
					descriptorList = new ArrayList<ServiceDescriptor>();
					wiredSolicitResponseFeedMappings.put(solicitResponseDescriptor, descriptorList);

				}

				/* Record the mapping */
				descriptorList.add(requestResponseDescriptor);

			}
			/* Else if this is a notification wiring... */
			else if (direction.equals(Service.MODE_NOTIFICATION)) {

				/* Build the descriptor for the remote one way endpoint */
				ServiceDescriptor oneWayDescriptor = new ServiceDescriptor(toPlatformID, toServiceID, toFeedID);

				/* Build the descriptor for the local notification endpoint */
				ServiceDescriptor notificationDescriptor = new ServiceDescriptor(systemRuntime.systemDescriptor()
						.platform(), systemRuntime.systemDescriptor().system(), fromFeedID);

				/* Get the list of mappings for the local name */
				ArrayList<ServiceDescriptor> descriptorList = wiredNotificationFeedMappings.get(notificationDescriptor);

				/* If the list has not been created... */
				if (descriptorList == null) {

					/* Create it */
					descriptorList = new ArrayList<ServiceDescriptor>();
					wiredNotificationFeedMappings.put(notificationDescriptor, descriptorList);

				}

				/* Record the mapping */
				descriptorList.add(oneWayDescriptor);

			} else {

				String error = Fabric.format("Unsupported feed direction '%s' for service '%s'", direction,
						systemRuntime.systemDescriptor());
				logger.log(Level.SEVERE, error);
				throw new UnsupportedOperationException(error);

			}
		}
	}

	/**
	 * Subscribes to an output feed and wires it to a local input feed.
	 * 
	 * @param outputFeed
	 *            the feed to which a subscription is being made.
	 * 
	 * @param fromFeedID
	 *            the local feed to which the subscription will be wired.
	 * 
	 * @throws Exception
	 */
	public void unwireInputFeed(ServiceDescriptor outputFeed) throws Exception {

		try {

			ISubscription subscription = wiredInputFeeds.remove(outputFeed);

			if (subscription != null) {

				subscription.unsubscribe();
				wiredInputFeedMappings.remove(outputFeed);

			}

		} catch (Exception e) {

			String message = Fabric.format("Cannot unsubscribe from output feed '%s' for service instance '%s':", e,
					outputFeed, systemRuntime.systemDescriptor());
			logger.log(Level.SEVERE, message);
			throw new Exception(message, e);

		}
	}

	/**
	 * Subscribes to an output feed.
	 * 
	 * @param outputFeed
	 *            the feed from which a subscription is being made.
	 * 
	 * @param fromFeedID
	 *            the local feed to which the subscription will be wired.
	 * 
	 * @throws Exception
	 */
	public void wireInputFeed(ServiceDescriptor outputFeed, String fromFeedID) throws Exception {

		/* Build the remote (from) task feed descriptor */
		TaskServiceDescriptor remoteOutputTaskDescriptor = new TaskServiceDescriptor("DEFAULT", outputFeed);
		ServiceDescriptor remoteOutputDescriptor = new ServiceDescriptor(remoteOutputTaskDescriptor);

		/* Build the local (to) feed descriptor */
		ServiceDescriptor localInputDescriptor = new ServiceDescriptor(systemRuntime.systemDescriptor().platform(),
				systemRuntime.systemDescriptor().system(), fromFeedID);

		try {

			/* If we are already subscribed to this feed... */
			if (wiredInputFeeds.containsKey(remoteOutputDescriptor)) {

				logger.log(
						Level.FINE,
						"Repeat subscription to remote feed \"%s\" (wired to local feed \"%s\") for service instance \"%s\":",
						new Object[] {remoteOutputTaskDescriptor, localInputDescriptor,
								systemRuntime.systemDescriptor()});

			}

			/* Subscribe to the remote feed */
			ISubscription inputSubscription = new Subscription(systemRuntime.fabricClient());
			inputSubscription.subscribe(remoteOutputTaskDescriptor, systemRuntime);

			/* Record the subscription */
			wiredInputFeeds.put(remoteOutputDescriptor, inputSubscription);

			/* Record the mapping given by the wiring */
			wiredInputFeedMappings.put(remoteOutputDescriptor, localInputDescriptor);

		} catch (Exception e) {

			String message = Fabric.format(
					"Cannot subscribe to remote feed '%s' (wired to local feed %s) for service instance '%s':", e,
					remoteOutputTaskDescriptor, localInputDescriptor, systemRuntime.systemDescriptor());
			logger.log(Level.SEVERE, message);
			throw new Exception(message, e);

		}
	}

	/**
	 * This method subscribes to the feeds used by this service instance to:
	 * <p>
	 * <ol>
	 * <li>Receive <em>requests</em> for request/response actions (i.e. incoming requests for services offered).</li>
	 * <li>Receive <em>responses</em> to this service's request/response requests (i.e. incoming replies to service
	 * invocations).</li>
	 * </ol>
	 * The method also opens channels to the feeds to which the specified service instance will publish its output.
	 * <p>
	 * Two sets of channels are opened:
	 * <ul>
	 * <li><strong>On-ramp</strong> channels to send raw payload data onto the Fabric for a new data feed created by
	 * this service.</li>
	 * <li><strong>Bus</strong> channels to send Fabric messages onto the Fabric when this service is delivering
	 * in-flight message processing.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note that this method is not concerned with the wiring of these feeds, just with setting up the appropriate
	 * subscriptions and output channels on the bus.
	 * 
	 * @throws Exception
	 * 
	 * @see initWiredFeeds
	 */
	private void initUnwiredFeeds() throws Exception {

		/* Get the request feeds */

		ServiceFactory serviceFactory = FabricRegistry.getServiceFactory(doQueryLocal);

		String predicate = String
				.format("platform_id = '%s' and service_id = '%s' and (direction='%s' or direction='%s' or direction='%s' or direction='%s')",
						systemRuntime.systemDescriptor().platform(), systemRuntime.systemDescriptor().system(),
						Service.MODE_REQUEST_RESPONSE, Service.MODE_LISTENER, Service.MODE_OUTPUT_FEED,
						Service.MODE_SOLICIT_RESPONSE); // Added solicit-response
		Service[] services = null;

		try {

			services = serviceFactory.getServices(predicate);

		} catch (RegistryQueryException e) {

			String message = Fabric.format("Cannot get wiring for service %s", systemRuntime.systemDescriptor());
			logger.log(Level.SEVERE, message);
			throw new Exception(message, e);

		}

		/* For each feed... */
		for (int f = 0; f < services.length; f++) {

			/* If this is an output feed... */
			if (services[f].getMode().equals(Service.MODE_OUTPUT_FEED)) {

				/*
				 * Open channels ready for the service to write messages from this feed onto the bus.
				 */

				/* Build the on-ramp topic */
				String onrampSubtopic = Fabric.format("/%s/%s/%s", services[f].getPlatformId(), services[f]
						.getSystemId(), services[f].getId());
				OutputTopic onrampTopic = new OutputTopic(config("fabric.feeds.onramp", null, homeNode())
						+ onrampSubtopic);

				try {

					/* Open and record the channel */
					SharedChannel onrampChannel = homeNodeEndPoint().openOutputChannel(onrampTopic);
					ServiceDescriptor onrampDescriptor = new ServiceDescriptor(services[f].getPlatformId(), services[f]
							.getSystemId(), services[f].getId());
					onrampOutputFeeds.put(onrampDescriptor, onrampChannel);

				} catch (Exception e) {

					String message = Fabric.format("Cannot open on-ramp feed '%s' for service instance '%s':", e,
							onrampTopic, systemRuntime.toString());
					logger.log(Level.SEVERE, message);
					throw new Exception(message, e);

				}

				/* Build the bus topic */
				String busSubtopic = Fabric.format("/%s/%s/%s", services[f].getPlatformId(), services[f].getSystemId(),
						services[f].getId());
				OutputTopic busTopic = new OutputTopic(config("fabric.feeds.bus", null, homeNode()) + busSubtopic);

				try {

					/* Open and record the channel */
					SharedChannel busChannel = homeNodeEndPoint().openOutputChannel(busTopic);
					ServiceDescriptor busDescriptor = new ServiceDescriptor(services[f].getPlatformId(), services[f]
							.getSystemId(), services[f].getId());
					busOutputFeeds.put(busDescriptor, busChannel);

				} catch (Exception e) {

					String message = Fabric.format("Cannot open output feed '%s' for service instance '%s':", e,
							onrampTopic, systemRuntime.toString());
					logger.log(Level.SEVERE, message);
					throw new Exception(message, e);

				}

			}
			/*
			 * Else if this is a request/response feed (i.e. for the receipt of requests in a request/response
			 * operation)...
			 */
			else if (services[f].getMode().equals(Service.MODE_REQUEST_RESPONSE)) {

				/* Build the descriptor for the local service feed */
				TaskServiceDescriptor requestResponseTaskDescriptor = new TaskServiceDescriptor("DEFAULT", services[f]
						.getPlatformId().toString(), services[f].getSystemId().toString(), services[f].getId()
						.toString());

				try {

					/* Subscribe to the feed */
					ISubscription responseSubscription = new Subscription(systemRuntime.fabricClient());
					responseSubscription.subscribe(requestResponseTaskDescriptor, systemRuntime);

					/* Record the subscription */
					ServiceDescriptor requestResponseDescriptor = new ServiceDescriptor(requestResponseTaskDescriptor);
					requestResponseFeeds.put(requestResponseDescriptor, responseSubscription);

				} catch (Exception e) {

					String message = Fabric.format("Cannot subscribe to input feed '%s' for service instance '%s':", e,
							requestResponseTaskDescriptor.toString(), systemRuntime.toString());
					logger.log(Level.SEVERE, message);
					throw new Exception(message, e);

				}

			}
			/*
			 * Else if this is a solicit response feed (i.e. for the receipt of responses in a request/response
			 * operation)...
			 */
			else if (services[f].getMode().equals(Service.MODE_SOLICIT_RESPONSE)) {

				/* Build the descriptor for the local service feed */
				TaskServiceDescriptor solicitResponseTaskDescriptor = new TaskServiceDescriptor("DEFAULT", services[f]
						.getPlatformId().toString(), services[f].getSystemId().toString(), services[f].getId()
						.toString());

				try {

					/* Subscribe to the feed */
					ISubscription solicitResponseSubscription = new Subscription(systemRuntime.fabricClient());
					solicitResponseSubscription.subscribe(solicitResponseTaskDescriptor, systemRuntime);

					/* Record the subscription */
					ServiceDescriptor solicitResponseDescriptor = new ServiceDescriptor(solicitResponseTaskDescriptor);
					solicitResponseFeeds.put(solicitResponseDescriptor, solicitResponseSubscription);

				} catch (Exception e) {

					String message = Fabric.format("Cannot subscribe to input feed '%s' for service instance '%s':", e,
							solicitResponseTaskDescriptor.toString(), systemRuntime.toString());
					logger.log(Level.SEVERE, message);
					throw new Exception(message, e);

				}

			}
			/*
			 * Else if this is a one way feed (i.e. for the receipt of notification messages that do not receive a
			 * response)...
			 */
			else if (services[f].getMode().equals(Service.MODE_LISTENER)) {

				/* Build the descriptor for the local service feed */
				TaskServiceDescriptor oneWayTaskDescriptor = new TaskServiceDescriptor("DEFAULT", services[f]
						.getPlatformId().toString(), services[f].getSystemId().toString(), services[f].getId()
						.toString());

				try {

					/* Subscribe to the feed */
					ISubscription responseSubscription = new Subscription(systemRuntime.fabricClient());
					responseSubscription.subscribe(oneWayTaskDescriptor, systemRuntime);

					/* Record the subscription */
					ServiceDescriptor oneWayDescriptor = new ServiceDescriptor(oneWayTaskDescriptor);
					oneWayFeeds.put(oneWayDescriptor, responseSubscription);

				} catch (Exception e) {

					String message = Fabric.format("Cannot subscribe to input feed '%s' for service instance '%s':", e,
							oneWayTaskDescriptor.toString(), systemRuntime.toString());
					logger.log(Level.SEVERE, message);
					throw new Exception(message, e);

				}

			} else {

				String error = Fabric.format("Unsupported feed direction '%s' for service '%s'", services[f].getMode(),
						systemRuntime.systemDescriptor());
				logger.log(Level.SEVERE, error);
				throw new UnsupportedOperationException(error);

			}
		}
	}

	/**
	 * Closes all of the feeds associated with this service instance including unsubscribing from input feeds, and
	 * closing the channels associated with the output feeds.
	 * 
	 * @throws Exception
	 */
	public void closeFeeds() throws Exception {

		unsubscribe(wiredInputFeeds);
		wiredInputFeedMappings.clear();

		unsubscribe(requestResponseFeeds);

		unsubscribe(oneWayFeeds);

		wiredNotificationFeedMappings.clear();

		unsubscribe(solicitResponseFeeds);
		wiredSolicitResponseFeedMappings.clear();

		closeChannels(busOutputFeeds);
		closeChannels(onrampOutputFeeds);

	}

	/**
	 * Unsubscribes from the specified set of feeds.
	 * 
	 * @param feeds
	 *            the table of feeds.
	 * 
	 * @throws Exception
	 */
	private void unsubscribe(HashMap<ServiceDescriptor, ISubscription> feeds) throws Exception {

		HashMap<ServiceDescriptor, ISubscription> feedsCopy = (HashMap<ServiceDescriptor, ISubscription>) feeds.clone();

		/* For each feed... */
		for (Iterator<ServiceDescriptor> i = feedsCopy.keySet().iterator(); i.hasNext();) {

			ServiceDescriptor serviceDescriptor = i.next();
			ISubscription feedSubscription = feedsCopy.get(serviceDescriptor);

			try {

				/* Unsubscribe */
				feedSubscription.unsubscribe();

			} catch (Exception e) {

				String message = Fabric.format("Cannot unsubscribe from input feed '%s' for service instance '%s':", e,
						serviceDescriptor, systemRuntime.toString());
				logger.log(Level.SEVERE, message);
				throw new Exception(message, e);

			}

			feeds.remove(serviceDescriptor);

		}
	}

	/**
	 * Closes the specified set of channels.
	 * 
	 * @param channels
	 *            the table of channels.
	 * 
	 * @throws Exception
	 */
	private void closeChannels(HashMap<ServiceDescriptor, SharedChannel> channels) throws Exception {

		HashMap<ServiceDescriptor, SharedChannel> channelsCopy = (HashMap<ServiceDescriptor, SharedChannel>) channels
				.clone();

		/* For each channel... */
		for (ServiceDescriptor serviceDescriptor : channelsCopy.keySet()) {

			SharedChannel channel = channels.get(serviceDescriptor);

			try {

				/* Close the channel */
				homeNodeEndPoint().closeChannel(channel, false);
				// channel.instance().close(null);

			} catch (Exception e) {

				String message = Fabric.format("Cannot close output channel '%s' for service instance '%s':", e,
						serviceDescriptor, systemRuntime.toString());
				logger.log(Level.SEVERE, message);
				throw new Exception(message, e);

			}

			channels.remove(serviceDescriptor);

		}
	}

	/**
	 * Answers the table of input feed subscriptions; the key is the remote feed descriptor to which the input is
	 * mapped.
	 * 
	 * @return the input feeds.
	 */
	public HashMap<ServiceDescriptor, ISubscription> wiredInputFeeds() {

		return wiredInputFeeds;

	}

	/**
	 * Answers the table mapping upstream output feeds to their local input feeds (i.e. the feed wiring); the key is the
	 * remote feed descriptor.
	 * 
	 * @return the table.
	 */
	public HashMap<ServiceDescriptor, ServiceDescriptor> wiredInputFeedMappings() {

		return wiredInputFeedMappings;

	}

	/**
	 * Answers the table of request/response subscriptions; the key is feed descriptor.
	 * 
	 * @return the response feeds.
	 */
	public HashMap<ServiceDescriptor, ISubscription> requestResponseFeeds() {

		return requestResponseFeeds;

	}

	/**
	 * Answers the table of one way subscriptions; the key is feed descriptor.
	 * 
	 * @return the response feeds.
	 */
	public HashMap<ServiceDescriptor, ISubscription> oneWayFeeds() {

		return oneWayFeeds;

	}

	/**
	 * Answers the table of solicit response subscriptions; the key is the local feed descriptor.
	 * 
	 * @return the response feeds.
	 */
	public HashMap<ServiceDescriptor, ISubscription> solicitResponseFeeds() {

		return solicitResponseFeeds;

	}

	/**
	 * Answers the table mapping solicit response feeds to their remote remote request/response counterparts; the key is
	 * the local solicit response feed descriptor.
	 * 
	 * @return the table.
	 */
	public HashMap<ServiceDescriptor, ArrayList<ServiceDescriptor>> wiredSolicitResponseFeedMappings() {

		return wiredSolicitResponseFeedMappings;

	}

	/**
	 * Answers the table mapping notification feeds to their remote remote one way counterparts; the key is the local
	 * notification feed descriptor.
	 * 
	 * @return the table.
	 */
	public HashMap<ServiceDescriptor, ArrayList<ServiceDescriptor>> wiredNotificationFeedMappings() {

		return wiredNotificationFeedMappings;

	}

	/**
	 * Answers the table of Fabric channels to which this service instance will write <em>on-ramp</em> messages, i.e.
	 * raw payloads (the key is the ID of the output feed).
	 * 
	 * @return the output feeds.
	 */
	public HashMap<ServiceDescriptor, SharedChannel> onrampOutputFeeds() {

		return onrampOutputFeeds;

	}

	/**
	 * Answers the table of Fabric channels to which this service instance will write <em>bus</em> messages (the key is
	 * the ID of the output feed).
	 * 
	 * @return the output feeds.
	 */
	public HashMap<ServiceDescriptor, SharedChannel> busOutputFeeds() {

		return busOutputFeeds;

	}

	/**
	 * Answers the list of <em>input</em> feed names.
	 * 
	 * @return the input feed names.
	 */
	public List<String> inputFeedIDs() {

		return inputFeedList;

	}

	/**
	 * Answers the list of <em>output</em> feed names.
	 * 
	 * @return the output feed names.
	 */
	public List<String> outputFeedIDs() {

		return outputFeedList;

	}

	/**
	 * Answers the list of <em>notify</em> feed names.
	 * 
	 * @return the notify feed names.
	 */
	public List<String> notifyFeedIDs() {

		return notifyFeedList;

	}

	/**
	 * Answers the list of <em>one way</em> feed names.
	 * 
	 * @return the one way feed names.
	 */
	public List<String> oneWayFeedIDs() {

		return oneWayFeedList;

	}

	/**
	 * Answers the list of <em>solicit response</em> feed names.
	 * 
	 * @return the solicit response feed names.
	 */
	public List<String> solicitResponseFeedIDs() {

		return solicitResponseFeedList;

	}

	/**
	 * Answers the list of <em>request response</em> feed names.
	 * 
	 * @return the request response feed names.
	 */
	public List<String> requestResponseFeedIDs() {

		return requestResponseFeedList;

	}
}
