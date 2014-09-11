/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import java.util.Iterator;
import java.util.List;

/**
 * Interface representing the list of actor subscriptions associated with a task.
 */
public interface ITaskSubscriptions extends IEmbeddedXML {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Adds an actor to the list for the specified task.
	 * 
	 * @param task
	 *            the task ID.
	 * 
	 * @param actorID
	 *            the actor ID.
	 */
	public void addActor(String task, String actorID);

	/**
	 * Removes an actor from the list for the specified task.
	 * 
	 * @param task
	 *            the task ID.
	 * 
	 * @param actorID
	 *            the actor ID.
	 */
	public void removeActor(String task, String actorID);

	/**
	 * Answers a copy of the list of actor IDs for the specified task.
	 * 
	 * @param task
	 *            the task ID.
	 * 
	 *            return the list of actor IDs.
	 * 
	 * @return the list of actor IDs.
	 */
	public List<String> getActors(String task);

	/**
	 * Sets the list of actor IDs for the specified task.
	 * 
	 * @param task
	 *            the task ID.
	 * 
	 * @param actors
	 *            the list of actor IDs.
	 */
	public void setActors(String task, List<String> actors);

	/**
	 * Answers the status of the task subscriptions table.
	 * 
	 * @return <code>true</code> if the table of task subscriptions is empty, <code>false</code> otherwise.
	 */
	public boolean isEmpty();

	/**
	 * Answers an iterator to traverse the set of task names listed in this instance.
	 * 
	 * @return the iterator.
	 */
	public Iterator<String> taskIterator();
}
