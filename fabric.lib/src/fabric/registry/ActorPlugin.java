/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents an ActorPlugin in the Fabric.
 * 
 * Instances of this class can be created by calling PluginFactory.createActorPlugin().
 * 
 * They can then be saved or deleted by calling either FabricRegistry.save(), FabricRegistry.delete(),
 * FabricRegistry.getPluginFactory().save() or FabricRegistry.getPluginFactory().delete().
 * 
 * Queries for actor plugins stored in the Fabric Registry are performed using the get methods on PluginFactory.
 */
public interface ActorPlugin extends TaskPlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Get the identifier of the actor with which this plugin is associated.
	 * 
	 * @return the actor id
	 */
	public String getActorId();

	/**
	 * Set the identifier of the actor with which this plugin is associated.
	 * 
	 * @param actorId
	 *            the id of the actor
	 */
	public void setActorId(String actorId);

}
