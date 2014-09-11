/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2008, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.Date;

/**
 * Class to manage a single Fabric metrics n-tuple.
 * <p>
 * This class is used to record all of the information relevant to an event in the Fabric.
 * </p>
 */
public class FabricMetric {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2009";

	/*
	 * Class constants
	 */

	/** Constant representing no event */
	public static final String EVENT_NONE = "none";

	/** Constant representing the start of node processing for a feed message */
	public static final String EVENT_NODE_PROCESSING_START = "node_processing_start";

	/** Constant representing the start of task processing for a feed message */
	public static final String EVENT_TASK_PROCESSING_START = "task_processing_start";

	/** Constant representing the start of actor processing for a feed message */
	public static final String EVENT_ACTOR_PROCESSING_START = "actor_processing_start";

	/** Constant representing the end of actor processing for a feed message */
	public static final String EVENT_ACTOR_PROCESSING_STOP = "actor_processing_stop";

	/** Constant representing the end of task processing for a feed message */
	public static final String EVENT_TASK_PROCESSING_STOP = "task_processing_stop";

	/** Constant representing the end of node processing for a feed message */
	public static final String EVENT_NODE_PROCESSING_STOP = "node_processing_stop";

	/** Constant representing the start of plug-in processing for a feed message */
	public static final String EVENT_PLUGIN_PROCESSING_START = "plugin_processing_start";

	/** Constant representing the end of plug-in processing for a feed message */
	public static final String EVENT_PLUGIN_PROCESSING_STOP = "plugin_processing_stop";

	/*
	 * Class fields
	 */

	/** The ID of the node upon which the event occurred. */
	private String nodeID = null;

	/** The ID of the task associated with this event (if any). */
	private String taskID = null;

	/** The ID of the actor associated with this event (if any). */
	private String actorID = null;

	/** The feed descriptor associated with this event (if any). */
	private ServiceDescriptor serviceDescriptor = null;

	/** The UUID of the Fabric message associated with the event (if any). */
	private String messageID = null;

	/** The ordinal number of the message associated with this event (if any). */
	private long ordinal = -1;

	/** The message associated with this event (if any). */
	private byte[] message = null;

	/** The name of the plug-in associated with this event (if any). */
	private String pluginName = null;

	/** The event that generated this record. */
	private String event = EVENT_NONE;

	/** The unique ID for this record (generated). */
	private long recordID = -1;

	/** The event time stamp */
	private long eventTime = -1;

	/** Unique ID for this VM invocation */
	private static final String runID = (new Date()).toString();

	/** Lock protecting access to the metric record count for this VM */
	private static Object vmRecordCount = new Object();

	/** The record metric record count for this VM */
	private static long vmRecordID = 0;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance (for internal use only).
	 */
	private FabricMetric() {
	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param nodeID
	 *            the ID of the node upon which the event occurred.
	 * 
	 * @param taskID
	 *            the ID of the task associated with this event (if any).
	 * 
	 * @param actorID
	 *            the ID of the actor associated with this event (if any).
	 * 
	 * @param serviceDescriptor
	 *            the feed descriptor associated with this event (if any).
	 * 
	 * @param messageID
	 *            the UUID of the Fabric message associated with the event (if any).
	 * 
	 * @param ordinal
	 *            the ordinal number of the message associated with this event (if any).
	 * 
	 * @param message
	 *            the message associated with this event (if any).
	 * 
	 * @param pluginName
	 *            the name of the plug-in associated with this event (if any).
	 */
	public FabricMetric(String nodeID, String taskID, String actorID, ServiceDescriptor serviceDescriptor,
			String messageID, long ordinal, byte[] message, String pluginName) {

		/* Increment the record count */
		synchronized (vmRecordCount) {

			this.nodeID = nodeID;
			this.messageID = messageID;
			this.recordID = ++vmRecordID;
			this.taskID = taskID;
			this.actorID = actorID;
			this.serviceDescriptor = serviceDescriptor;
			this.pluginName = pluginName;
			this.ordinal = ordinal;
			this.message = message;

		}
	}

	/**
	 * Constructs a new instance by making a deep copy of an existing instance.
	 * 
	 * @param metric
	 *            the instance to copy.
	 */
	public FabricMetric(FabricMetric metric) {

		/* Increment the record count */
		synchronized (vmRecordCount) {

			nodeID = metric.nodeID;
			messageID = metric.messageID;
			recordID = ++vmRecordID;
			taskID = metric.taskID;
			actorID = metric.actorID;
			serviceDescriptor = metric.serviceDescriptor;
			pluginName = metric.pluginName;
			ordinal = metric.ordinal;
			message = metric.message;
			event = metric.event;
			eventTime = metric.eventTime;

		}
	}

	/**
	 * Converts this instance into a comma separated value string.
	 * 
	 * @return the CSV string.
	 */
	public String toCSV() {

		String csv = "";

		csv += toCSVValue(runID) + ',';
		csv += toCSVValue(nodeID) + ',';
		csv += recordID + ",";
		csv += toCSVValue(event) + ',';
		csv += toCSVValue(messageID) + ',';
		csv += ordinal + ",";
		csv += toCSVValue(taskID) + ',';
		csv += toCSVValue(actorID) + ',';
		csv += toCSVValue(serviceDescriptor.toString()) + ',';
		csv += toCSVValue(pluginName) + ',';
		csv += eventTime + ',';
		csv += toCSVValue(new String(message));

		return csv;

	}

	/**
	 * Escapes reserved characters in strings to be used in a comma separated value list.
	 * 
	 * @param value
	 *            the value to test and escape if required.
	 * 
	 * @return the value in a suitable form for a CSV list.
	 */
	private String toCSVValue(String value) {

		String csvValue = value;

		if (value == null) {

			csvValue = "";

		}
		/* Else if the value contains a reserved character... */
		else if (value.indexOf(',') >= 0 || value.indexOf('\n') >= 0) {

			/* Escape the value */
			csvValue = '"' + value + '"';

		}

		return csvValue;

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String toString = "";
		toString += "runID=[" + runID + "],";
		toString += "nodeID=[" + nodeID + "],";
		toString += "recordID=[" + recordID + "],";
		toString += "event=[" + event + "],";
		toString += "messageID=[" + messageID + "],";
		toString += "ordinal=[" + ordinal + "],";
		toString += "missionID=[" + taskID + "],";
		toString += "clientID=[" + actorID + "],";
		toString += "serviceDescriptor=[" + serviceDescriptor + "],";
		toString += "pluginName=[" + pluginName + "],";
		toString += "eventTime=[" + eventTime + "]";
		toString += "message=[" + new String(message) + "]";

		return toString;

	}

	/**
	 * Answers the ID of the node upon which the event occurred.
	 * 
	 * @return the node ID.
	 */
	public String getNodeID() {
		return nodeID;
	}

	/**
	 * Answers the ID of the task associated with this event (if any).
	 * 
	 * @return the ID.
	 */
	public String getTaskID() {
		return taskID;
	}

	/**
	 * Answers the ID of the actor associated with this event (if any).
	 * 
	 * @return the ID.
	 */
	public String getActorID() {
		return actorID;
	}

	/**
	 * Answers the UUID of the Fabric message associated with the event (if any).
	 * 
	 * @return the ID.
	 */
	public String getMessageID() {
		return messageID;
	}

	/**
	 * Answers the ordinal number of the message associated with this event (if any).
	 * 
	 * @return the ordinal.
	 */
	public long getOrdinal() {
		return ordinal;
	}

	/**
	 * Answers the message associated with this event (if any).
	 * 
	 * @return the message.
	 */
	public byte[] getMessage() {
		return message;
	}

	/**
	 * Answers the name of the plug-in associated with this event (if any).
	 * 
	 * @return the name.
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * Answers the event that generated this record.
	 * 
	 * @return the event.
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * Sets the event that generated this record.
	 * 
	 * @param event
	 *            the event.
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	/**
	 * Answers the unique ID for this record (generated).
	 * 
	 * @return the record ID.
	 */
	public long getRecordID() {
		return recordID;
	}

	/**
	 * Answers the time of the event.
	 * 
	 * @return the event time stamp.
	 */
	public long getEventTime() {
		return eventTime;
	}

	/**
	 * Sets the time of the event.
	 * 
	 * @param eventTime
	 *            the event time stamp.
	 */
	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}
}
