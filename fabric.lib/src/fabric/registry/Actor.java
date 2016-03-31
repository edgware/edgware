/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents an actor that is connected to the Fabric.
 * 
 * Instances of this class can be created by calling ActorFactory.createActor().
 * 
 * They can then be saved or deleted by calling either FabricRegistry.save(), FabricRegistry.delete(),
 * FabricRegistry.getActorFactory().save() or FabricRegistry.getActorFactory().delete().
 * 
 * Queries for actors stored in the Fabric Registry are performed using the get methods on ActorFactory.
 * 
 * @see fabric.registry.ActorFactory
 */
public interface Actor extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Returns the unique id of this actor.
	 * 
	 * @return the actor id
	 */
	public String getId();

	/**
	 * Set the id of this actor.
	 * 
	 * @param actorId
	 *            the unique identifier for this actor.
	 */
	public void setId(String actorId);

	/**
	 * Returns the type identifier for this actor.
	 * 
	 * @return
	 */
	public String getTypeId();

	/**
	 * Sets the type of this actor.
	 * 
	 * @param actorTypeId
	 *            - the identifier for the actor type
	 */
	public void setTypeId(String actorTypeId);

	/**
	 * Get the roles assigned to this actor.
	 * 
	 * @return A string representing a list of roles or null if no roles are associated.
	 */
	public String getRoles();

	/**
	 * Set the roles that are to be associated with this actor.
	 * 
	 * @param roles
	 *            the roles, represented as a string
	 * 
	 */
	public void setRoles(String roles);

	/**
	 * Get the credentials attributed to this actor.
	 * 
	 * @return the credentials or null if none are set.
	 */
	public String getCredentials();

	/**
	 * Set the credentials for this actor.
	 * 
	 * @param credentials
	 *            the credentials or null if none are required
	 */
	public void setCredentials(String credentials);

	/**
	 * Get the affiliation of this actor.
	 * 
	 * @return the affiliation or null if it has not been set.
	 */
	public String getAffiliation();

	/**
	 * Set the affiliation of this actor
	 * 
	 * @param affiliation
	 *            the affiliation of the actor
	 */
	public void setAffiliation(String affiliation);

	/**
	 * Get the description of this actor
	 * 
	 * @return the description
	 */
	public String getDescription();

	/**
	 * Set the description of this actor
	 * 
	 * @param description
	 *            the description of this actor
	 */
	public void setDescription(String description);

	/**
	 * Get the custom attributes of this actor
	 * 
	 * @return the attributes
	 */
	public String getAttributes();

	/**
	 * Set the custom attributes of this actor
	 * 
	 * @param attributes
	 *            the attributes, represented as a string
	 */
	public void setAttributes(String attributes);

	/**
	 * Get the uri of the custom attributes
	 * 
	 * @return the uri reference for the attributes
	 */
	public String getAttributesUri();

	/**
	 * Set the custom attributes uri
	 * 
	 * @param uri
	 *            the uri rerference for the attributes
	 */
	public void setAttributesUri(String uri);

}
