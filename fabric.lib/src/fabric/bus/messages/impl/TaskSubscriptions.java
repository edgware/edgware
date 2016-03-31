/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import fabric.Notifier;
import fabric.bus.messages.IReplicate;
import fabric.bus.messages.ITaskSubscriptions;
import fabric.core.xml.XML;

/**
 * Class representing the list of client subscriptions associated with a task.
 */
public class TaskSubscriptions extends Notifier implements ITaskSubscriptions {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class fields
	 */

	/**
	 * The task and client IDs from a message, recorded in this table where each key is a task ID, and each value is an
	 * <code>ArrayList</code> of client IDs.
	 */
	private HashMap<String, ArrayList<String>> taskSubscriptions = new HashMap<String, ArrayList<String>>();

	/** Cache of the XML form of the message. */
	private XML xmlCache = null;

	/**
	 * Constructs a new instance.
	 */
	public TaskSubscriptions() {
		super(Logger.getLogger("fabric.bus.messages"));
		addChangeListener(this);

	}

	/**
	 * Constructs a new instance, initialized from the specified instance.
	 * 
	 * @param source
	 *            the instance to copy.
	 */
	public TaskSubscriptions(TaskSubscriptions source) {

		this();

		/* For each set of subscriptions in the source... */
		for (Iterator<String> s = source.taskSubscriptions.keySet().iterator(); s.hasNext();) {

			/* Get the next list of client IDs */
			String nextKey = s.next();
			ArrayList<String> nextClientList = source.taskSubscriptions.get(nextKey);

			/* Clone and save */
			nextClientList = (nextClientList != null) ? (ArrayList<String>) nextClientList.clone() : null;
			taskSubscriptions.put(nextKey, nextClientList);

		}

		xmlCache = null;
	}

	/**
	 * @see fabric.bus.messages.IEmbeddedXML#init(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void init(String element, XML messageXML) throws Exception {

		taskSubscriptions.clear();

		/* Get the XML paths for the subscription elements */
		String elementPath = XML.expandPath(element);
		elementPath = XML.regexpEscape(elementPath);
		String[] subscriptionPaths = messageXML.getPaths(elementPath
				+ "/f:subscriptions\\[.*\\]/f:subscription\\[.*\\]");

		/* For each subscription... */
		for (int s = 0; s < subscriptionPaths.length; s++) {

			/* Get the task ID from the next subscription element */
			String task = messageXML.get(subscriptionPaths[s] + "@task");

			/* Get the list of clients subscribed to this task */
			ArrayList<String> clients = lookupSublist(task, taskSubscriptions);

			/* Get and record the client ID */
			String client = messageXML.get(subscriptionPaths[s] + "@client");
			clients.add(client);

		}

		xmlCache = null;

	}

	/**
	 * @see fabric.bus.messages.IEmbeddedXML#embed(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void embed(String element, XML messageXML) throws Exception {

		/* For each task... */
		for (Iterator<String> s = taskSubscriptions.keySet().iterator(); s.hasNext();) {

			/* Get the task name */
			String task = s.next();

			/* Get the list of client IDs */
			ArrayList<String> clientIDs = taskSubscriptions.get(task);

			/* For each client ID... */
			for (int c = 0; c < clientIDs.size(); c++) {

				/* Serialize the subscription to the XML */
				messageXML.set(element + "/f:subscriptions/f:subscription[%d]@task", task, c);
				messageXML.set(element + "/f:subscriptions/f:subscription[%d]@client", clientIDs.get(c), c);

			}
		}
	}

	/**
	 * @see fabric.bus.messages.ITaskSubscriptions#addActor(java.lang.String, java.lang.String)
	 */
	@Override
	public void addActor(String task, String actorID) {

		/* Get the list of client IDs */
		ArrayList<String> actorList = taskSubscriptions.get(task);
		ArrayList<String> oldActorList = (actorList != null) ? (ArrayList<String>) actorList.clone() : null;

		/* If the list is empty... */
		if (actorList == null) {

			/* Create it */
			actorList = new ArrayList<String>();
			taskSubscriptions.put(task, actorList);

		}

		/* If the client is not already in the list... */
		if (!actorList.contains(actorID)) {

			/* Add it */
			actorList.add(actorID);

		}

		fireChangeNotification("actorList", oldActorList, actorList);

	}

	/**
	 * @see fabric.bus.messages.ITaskSubscriptions#removeActor(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeActor(String task, String actorID) {

		/* Get the list of actor IDs */
		ArrayList<String> actorList = taskSubscriptions.get(task);

		/* If the list isn't empty... */
		if (actorList != null) {

			ArrayList<String> oldActorList = (ArrayList<String>) actorList.clone();

			/* Remove the actor ID */
			actorList.remove(actorID);

			fireChangeNotification("actorList", oldActorList, actorList);

		}
	}

	/**
	 * @see fabric.bus.messages.ITaskSubscriptions#getActors(java.lang.String)
	 */
	@Override
	public List<String> getActors(String task) {

		/* To hold the result */
		List<String> getActors = null;

		/* Get the list of client IDs */
		ArrayList<String> actorList = taskSubscriptions.get(task);

		/* If the list isn't empty... */
		if (actorList != null) {

			/* Copy the list */
			getActors = (List<String>) actorList.clone();

		} else {

			getActors = new ArrayList<String>();

		}

		return getActors;
	}

	/**
	 * @see fabric.bus.messages.ITaskSubscriptions#setActors(java.lang.String, java.util.List)
	 */
	@Override
	public void setActors(String task, List<String> actors) {

		/* Get the list of actor IDs */
		ArrayList<String> actorList = lookupSublist(task, taskSubscriptions);
		ArrayList<String> oldActorList = (ArrayList<String>) actorList.clone();

		/* Remove the current list contents */
		actorList.clear();

		/* If there is a new list... */
		if (actors != null) {

			actorList.addAll(actors);

		}

		fireChangeNotification("actorList", oldActorList, actorList);
	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#toString()
	 */
	@Override
	public String toString() {

		String toString = null;

		try {

			if (xmlCache == null) {

				xmlCache = new XML();
				embed("", xmlCache);

			}

			toString = xmlCache.toString();

		} catch (Exception e) {

			e.printStackTrace();
			toString = super.toString();

		}

		return toString;

	}

	/**
	 * @see fabric.bus.messages.IReplicate#replicate()
	 */
	@Override
	public IReplicate replicate() {

		return new TaskSubscriptions(this);

	}

	/**
	 * @see fabric.bus.messages.ITaskSubscriptions#isEmpty()
	 */
	@Override
	public boolean isEmpty() {

		return taskSubscriptions.size() == 0;

	}

	/**
	 * @see fabric.bus.messages.ITaskSubscriptions#taskIterator()
	 */
	@Override
	public Iterator<String> taskIterator() {

		return taskSubscriptions.keySet().iterator();

	}

	/**
	 * @see fabric.Notifier#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		super.propertyChange(event);

		/* Something has changed, so invalidate the cached XML form of this instance */
		xmlCache = null;

	}
}
