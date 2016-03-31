/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create Actor objects and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getActorFactory().
 */
public interface ActorFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Creates an Actor, specifying only the mandatory attributes.
	 * 
	 * @param id
	 *            - the unique identifier of the actor
	 * @param typeId
	 *            - the type of actor
	 * @return an Actor object with all required fields set; other fields will be set to default values
	 */
	public Actor createActor(String id, String typeId);

	/**
	 * Creates an Actor object with the specified attributes.
	 * 
	 * @param id
	 *            - the unique identifier of the actor
	 * @param typeId
	 *            - the type of actor
	 * @param roles
	 *            - the roles assigned to the actor
	 * @param credentials
	 *            - the credentials the actor
	 * @param affiliation
	 *            - the affiliation of the actor
	 * @param description
	 *            - the description of this actor
	 * @param attributes
	 *            - the actor-specific attributes
	 * @param attributesURI
	 *            - the uri reference for actor-specific attributes
	 * @return an Actor object with all fields set according the values specified
	 */
	public Actor createActor(String id, String typeId, String roles, String credentials, String affiliation,
			String description, String attributes, String attributesURI);

	/**
	 * Gets all the actors that are stored in the Fabric Registry.
	 * 
	 * @return the list of all actors, empty list if none are stored or null in case of error
	 */
	public Actor[] getAllActors();

	/**
	 * Gets a particular actor from the Fabric Registry using the specified id.
	 * 
	 * @param actorId
	 *            - the identifier of the actor to be looked up
	 * @return - the Actor or null if no match was found for the id.
	 */
	public Actor getActorById(String actorId);

	/**
	 * Gets all actors of the specified type.
	 * 
	 * @param typeId
	 * @return
	 */
	public Actor[] getActorsByType(String typeId);

	/**
	 * Gets all actors that match the specified predicates.
	 * 
	 * Predicates form the body of the WHERE clause of a SQL query on the ACTORS table and as such any valid operator
	 * for a WHERE clause is allowed.
	 * 
	 * Example query predicate:
	 * 
	 * ACTOR_ID LIKE 'Commander%'
	 * 
	 * @see <uri of database schema>
	 * 
	 * @param queryPredicates
	 *            the predicates to used for an actor-related query
	 * @return a list of actors, empty list if no matches or null if an error occurred
	 * @throws RegistryQueryException
	 *             if the query predicates are invalid
	 */
	public Actor[] getActors(String queryPredicates) throws RegistryQueryException;

}
