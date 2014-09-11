/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds.impl;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import fabric.Notifier;
import fabric.TaskServiceDescriptor;
import fabric.bus.messages.IEmbeddedXML;
import fabric.bus.messages.IReplicate;
import fabric.core.xml.XML;

/**
 * Class representing the list of Fabric data feeds embedded in a subscription message.
 * 
 */
public class FeedList extends Notifier implements IEmbeddedXML {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class constants
	 */

	/** The feed list. */
	private ArrayList<TaskServiceDescriptor> feedList = new ArrayList<TaskServiceDescriptor>();

	/** Cache of the XML form of the message. */
	private XML xmlCache = null;

	/**
	 * Constructs a new instance.
	 */
	public FeedList() {

		super(Logger.getLogger("fabric.bus.feeds"));
		addChangeListener(this);

	}

	/**
	 * Constructs a new instance, initialized from the specified instance.
	 * 
	 * @param source
	 *            the instance to copy.
	 */
	public FeedList(FeedList source) {

		this();
		feedList = (ArrayList<TaskServiceDescriptor>) source.feedList.clone();
		xmlCache = null;

	}

	/**
	 * @see fabric.bus.messages.IEmbeddedXML#init(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void init(String element, XML messageXML) throws Exception {

		feedList.clear();

		/* Get the XML paths for the feeds */
		String elementPath = XML.expandPath(element);
		elementPath = XML.regexpEscape(elementPath);
		String[] propertyPaths = messageXML.getPaths(elementPath + "/f:feeds\\[.*\\]/f:feed\\[.*\\]");

		/* For each feed... */
		for (int p = 0; p < propertyPaths.length; p++) {

			/* Get and record the next feed */

			String task = messageXML.get(propertyPaths[p] + "@task");
			String platform = messageXML.get(propertyPaths[p] + "@platform");
			String system = messageXML.get(propertyPaths[p] + "@system");
			String feed = messageXML.get(propertyPaths[p] + "@feed");

			TaskServiceDescriptor nextFeed = new TaskServiceDescriptor(task, platform, system, feed);

			feedList.add(nextFeed);

		}

		xmlCache = null;

	}

	/**
	 * @see fabric.bus.messages.IEmbeddedXML#embed(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void embed(String element, XML messageXML) throws Exception {

		/* For each feed... */
		for (int f = 0; f < feedList.size(); f++) {

			/* Serialize the feed to the XML */

			TaskServiceDescriptor nextFeed = feedList.get(f);

			messageXML.set(element + "/f:feeds/f:feed[%d]@task", nextFeed.task(), f);
			messageXML.set(element + "/f:feeds/f:feed[%d]@platform", nextFeed.platform(), f);
			messageXML.set(element + "/f:feeds/f:feed[%d]@system", nextFeed.system(), f);
			messageXML.set(element + "/f:feeds/f:feed[%d]@feed", nextFeed.service(), f);

		}

	}

	/**
	 * Answers the list of feeds.
	 * 
	 * @return the feed list.
	 */
	public TaskServiceDescriptor[] getFeeds() {

		TaskServiceDescriptor[] feeds = new TaskServiceDescriptor[feedList.size()];
		feeds = feedList.toArray(feeds);
		return feeds;

	}

	/**
	 * Sets the list of feeds.
	 * 
	 * @param feeds
	 *            the feed list.
	 */
	public void setFeeds(TaskServiceDescriptor[] feeds) {

		ArrayList<TaskServiceDescriptor> oldFeedList = (ArrayList<TaskServiceDescriptor>) feedList.clone();

		feedList.clear();
		feedList.addAll(Arrays.asList(feeds));

		fireChangeNotification("feedList", oldFeedList, feedList);

	}

	public void addFeed(TaskServiceDescriptor feed) {

		ArrayList<TaskServiceDescriptor> oldFeedList = (ArrayList<TaskServiceDescriptor>) feedList.clone();
		feedList.add(feed);
		fireChangeNotification("feedList", oldFeedList, feedList);
	}

	/**
	 * Answers the number of elements in the feed list.
	 * 
	 * @return the number of feeds in the list.
	 */
	public int size() {

		return feedList.size();

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

		return new FeedList(this);

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
