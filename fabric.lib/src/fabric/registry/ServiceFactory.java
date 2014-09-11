/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2011, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create services and save/delete/query them in the Fabric Registry.
 * 
 * @see fabric.registry.FabricRegistry#getServiceFactory()
 */
public interface ServiceFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011, 2014";

	/**
	 * Creates an <em>output</em> Service, specifying only the mandatory attributes.
	 * 
	 * @param platformId
	 *            - the name of the platform which the service is associated with.
	 * @param systemId
	 *            - the name of the system.
	 * @param feedId
	 *            - the name of the feed.
	 * @param typeID
	 *            - the type of the feed.
	 * @return a Service object with only the required fields set; all other fields will be set to default values.
	 */
	public Service createOutputFeed(String platformId, String systemId, String feedId, String typeID);

	/**
	 * Creates an <em>input</em> Service, specifying only the mandatory attributes.
	 * 
	 * @param platformId
	 *            - the id of the platform which the service is associated with.
	 * @param systemId
	 *            - the id of the system.
	 * @param feedId
	 *            - the name of the feed.
	 * @param typeID
	 *            - the type of the feed.
	 * @return a Service object with only the required fields set; all other fields will be set to default values.
	 */
	public Service createInputFeed(String platformId, String systemId, String feedId, String typeId);

	/**
	 * Creates a <em>solicit request</em> Service, specifying only the mandatory attributes.
	 * 
	 * @param platformId
	 *            - the name of the platform which the service is associated with.
	 * @param systemId
	 *            - the name of the system.
	 * @param serviceId
	 *            - the name of the service.
	 * @param typeID
	 *            - the type of the service.
	 * @return a Service object with only the required fields set; all other fields will be set to default values.
	 */
	public Service createSolicitRequest(String platformId, String systemId, String serviceId, String typeId);

	/**
	 * Creates a <em>request response</em> Service, specifying only the mandatory attributes.
	 * 
	 * @param platformId
	 *            - the name of the platform which the service is associated with.
	 * @param systemId
	 *            - the name of the system.
	 * @param serviceId
	 *            - the name of the service.
	 * @param typeID
	 *            - the type of the service.
	 * @return a Service object with only the required fields set; all other fields will be set to default values.
	 */
	public Service createRequestResponse(String platformId, String systemId, String serviceId, String typeId);

	/**
	 * Creates a <em>notification</em> Service, specifying only the mandatory attributes.
	 * 
	 * @param platformId
	 *            - the name of the platform which the service is associated with.
	 * @param systemId
	 *            - the name of the system.
	 * @param serviceId
	 *            - the name of the service.
	 * @param typeID
	 *            - the type of the service.
	 * @return a Service object with only the required fields set; all other fields will be set to default values.
	 */
	public Service createNotification(String platformId, String systemId, String serviceId, String typeId);

	/**
	 * Creates a <em>one way</em> Service, specifying only the mandatory attributes.
	 * 
	 * @param platformId
	 *            - the name of the platform which the service is associated with.
	 * @param systemId
	 *            - the name of the system.
	 * @param service
	 *            - the name of the service.
	 * @param typeID
	 *            - the type of the feed.
	 * @return a Service object with only the required fields set; all other fields will be set to default values.
	 */
	public Service createListener(String platformId, String systemId, String service, String typeId);

	/**
	 * Creates a Service using the specified information.
	 * 
	 * @param platformId
	 *            the id of the platform which the feed's system is attached to.
	 * @param systemId
	 *            the name of the system on the platform.
	 * @param serviceId
	 *            the name of the service.
	 * @param typeID
	 *            the type of the service.
	 * @param mode
	 *            the mode of the interface.
	 * @param credentials
	 *            the security classification of the feed.
	 * @param availability
	 *            the availability status.
	 * @param description
	 *            the description.
	 * @param attributes
	 *            the custom attributes string.
	 * @param attributesURI
	 *            the custom attributes uri.
	 * 
	 * @return a populated Service object.
	 */
	public Service createService(String platformId, String systemId, String serviceId, String typeID, String mode,
			String credentials, String availability, String description, String attributes, String attributesURI);

	/**
	 * Get a list of services using a custom WHERE-clause predicate.
	 * 
	 * @param queryPredicates
	 *            - the predicates to use in the WHERE clause.
	 * @return a list of services that match or <code>null</code> otherwise.
	 * @throws RegistryQueryException
	 *             if the predicates are malformed.
	 */
	public Service[] getServices(String queryPredicates) throws RegistryQueryException;

	/**
	 * Get a list of services of a particular type.
	 * 
	 * @param type
	 *            - the type of service to find.
	 * @return a list of services of that type or <code>null</code> if none exist.
	 */
	public Service[] getServicesByType(String type);

	/**
	 * Get the list of all services defined in the Registry.
	 * 
	 * @return the complete list of all services.
	 */
	public Service[] getAllServices();

	/**
	 * Get the list of all input feeds.
	 * 
	 * @return the list of matching feeds.
	 */
	public Service[] getAllInputFeeds();

	/**
	 * Get the list of all output feeds.
	 * 
	 * @return the list of matching feeds.
	 */
	public Service[] getAllOutputFeeds();

	/**
	 * Get the list of all solicit response services.
	 * 
	 * @return the list of matching services.
	 */
	public Service[] getAllSolicitResponses();

	/**
	 * Get the list of all request/response services.
	 * 
	 * @return the list of matching services.
	 */
	public Service[] getAllRequestResponses();

	/**
	 * Get the list of all notification services.
	 * 
	 * @return the list of matching services.
	 */
	public Service[] getAllNotifications();

	/**
	 * Get the list of all listener services.
	 * 
	 * @return the list of matching services.
	 */
	public Service[] getAllListeners();

	/**
	 * Get the list of services associated with a particular system.
	 * 
	 * @param platformId
	 *            - the name of the platform..
	 * @param systemId
	 *            - the name of the system.
	 * 
	 * @return the list of services or <code>null</code> otherwise.
	 */
	public Service[] getServicesBySystem(String platformId, String systemId);

	/**
	 * Get the metadata for a specific Service.
	 * 
	 * @param platformId
	 *            - the name of the platform.
	 * @param systemId
	 *            - the name of the system.
	 * @param serviceId
	 *            - the name of the service.
	 * 
	 * @return the list of services or <code>null</code> otherwise.
	 */
	public Service getServiceById(String platformId, String systemId, String serviceId);
}