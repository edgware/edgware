/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.distributed;

import java.util.logging.Logger;

public class DistributedQueryWaitThread extends Thread 
{
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private final static String CLASS_NAME = DistributedQueryWaitThread.class.getName();
	private final static String PACKAGE_NAME = DistributedQueryWaitThread.class.getPackage().getName();

	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);
	private long timeout;
	private String correlationId;
	private DistributedPersistenceFablet timeOutCallback;
	
	public DistributedQueryWaitThread(long timeout, String correlationId, DistributedPersistenceFablet service)
	{
		this.timeout = timeout;
		this.correlationId = correlationId;
		this.timeOutCallback = service;
	}
	
	public DistributedQueryWaitThread(long timeout, String correlationId)
	{
		this.timeout = timeout;
		this.correlationId = correlationId;
	}
	
	@Override
	public void run() {
		try
		{
			Thread.sleep(timeout);
			logger.fine("Timeout " + timeout + " milliseconds exceeded waiting for result with correlation ID " + correlationId);
			if (timeOutCallback != null)
			{
				timeOutCallback.queryTimedOut(correlationId);
			}
		}
		catch (InterruptedException e)
		{
			logger.finest("Wait Thread interrupted for correlationId " + correlationId);
		}
		catch (Exception e)
		{
			logger.warning("FAILED TO TIMEOUT THE QUERY " + e.getMessage());
		}
		
	}
	
}
