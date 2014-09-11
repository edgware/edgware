/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2006, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to manage the instrumentation (profiling information) for the Fabric running on a node.
 */
public class MetricsManager extends Fabric {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2012";

	/*
	 * Class static fields
	 */

	/*
	 * Class fields
	 */

	/** The metrics recorded by this instance */
	private ArrayList<FabricMetric> metrics = new ArrayList<FabricMetric>();

	/** Flag indicating if instrumentation is required */
	private boolean doInstrument = false;

	/** Flag indicating if metrics should be persisted to a file */
	private boolean persistToFile = false;

	/** The name of the file to be created when metrics are persisted (if any) */
	private String fileName = null;

	/** The output stream to which metrics is to be written */
	private FileWriter metricsWriter = null;

	/**
	 * The maximum number of records that should be buffered before being written to the file, written to the registry,
	 * or discarded
	 */
	private int maxBufferedMetrics = 1;

	/*
	 * Inner classes
	 */

	/*
	 * Class methods
	 */

	public MetricsManager() {

		super(Logger.getLogger("fabric"));
	}

	/**
	 * Initialize instrumentation.
	 * 
	 * @param doInstrument
	 *            <code>true</code> if instrumentation is enabled, <code>false</code> otherwise.
	 * 
	 * @param fileName
	 *            the name of the file to which metrics should be persisted, or <code>null</code> if there is none.
	 * 
	 * @param maxBufferedMetrics
	 *            the maximum number of records that should be buffered before being written to the file, written to the
	 *            Registry, or discarded.
	 * 
	 * @throws IOException
	 *             thrown if there is a problem opening the persistence file.
	 */
	public void initManager(boolean doInstrument, String fileName, int maxBufferedMetrics) throws IOException {

		this.doInstrument = doInstrument;
		this.fileName = fileName;
		this.maxBufferedMetrics = maxBufferedMetrics;

		/* If instrumentation is enabled... */
		if (doInstrument) {

			/* If metrics are to be persisted to a file... */
			if (persistToFile) {

				/* Open the file */
				logger.log(Level.FINE, "Opening instrumentation file \"{0}\"", fileName);
				metricsWriter = new FileWriter(fileName);

			}

		}
	}

	/**
	 * Set the start time of a Fabric metric and record.
	 * 
	 * @param metric
	 *            the metric to record.
	 * 
	 * @param event
	 *            the event that has occurred.
	 */
	public void startTiming(FabricMetric metric, String event) {

		if (doInstrument) {

			/* Record the start time */
			metric.setEventTime(System.currentTimeMillis());
			metric.setEvent(event);

			/* Save the record */
			addMetric(metric);

		}

	}

	/**
	 * Set the end time of a Fabric metric and record.
	 * 
	 * @param metric
	 *            the metric to record.
	 * 
	 * @param event
	 *            the event that has occuered.
	 */
	public void endTiming(FabricMetric metric, String event) {

		if (doInstrument) {

			/* Record the end time */
			metric.setEventTime(System.currentTimeMillis());
			metric.setEvent(event);

			/* Save the record */
			addMetric(metric);

		}

	}

	/**
	 * Adds a new metric. If the new metric exceeds the maximum size of the buffer then the results are persisted (if
	 * persistence is enabled) and cleared.
	 * 
	 * @param metric
	 *            the metric to add.
	 */
	public void addMetric(FabricMetric metric) {

		if (doInstrument) {

			synchronized (metrics) {

				/* Record the new metric */
				metrics.add(new FabricMetric(metric));

				/* If we have reached the maximum size of the buffer... */
				if (metrics.size() >= maxBufferedMetrics) {
					flushMetrics();
				}
			}
		}
	}

	/**
	 * Empties the buffer of metrics by writing to a file, and/or discarding.
	 */
	public void flushMetrics() {

		if (doInstrument) {

			logger.log(Level.FINE, "Persisting instrumentation");

			synchronized (metrics) {

				/* To hold the records ready for output to file */
				String csvRecords = "";

				/* While there are more records to process... */
				while (metrics.size() > 0) {

					/* Get the next metric, and remove it from the buffer */
					FabricMetric next = metrics.remove(0);

					/* If metrics are to be persisted to file... */
					if (persistToFile) {

						/* Add the next record as a CSV list */
						String csv = next.toCSV();
						csvRecords += csv + '\n';

					}
				}

				/* If there are metrics to be persisted to file... */
				if (csvRecords.length() > 0) {

					/* Write the records to the file */
					try {
						/* Append the record to the file */
						metricsWriter.write(csvRecords);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Cannot persist metrics to file:", e);
					}

				}
			}
		}
	}

	/**
	 * Stops this instance, flushing the current contents of the buffer and closing files.
	 */
	public void closeManager() {

		if (doInstrument) {

			synchronized (metrics) {

				/* Flush the buffer */
				flushMetrics();

				if (persistToFile) {

					/* We're done with the file */
					try {
						metricsWriter.close();
					} catch (IOException e) {
						/* Not much we can do at this point */
						logger.log(Level.SEVERE, "Closure of instrumentation metrics file failed:", e);
					}

				}
			}
		}
	}
}
