/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.CachedMessage;
import fabric.registry.exception.IncompleteObjectException;

public class CachedMessageImpl extends AbstractRegistryObject implements
		CachedMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";
	
	private long timestamp = 0;
	
	private String source = null;
	private String destination = null;
	private String message = null;
	
	
	protected CachedMessageImpl() {
		
	}
	
	protected CachedMessageImpl(long timestamp, String source, String destination, String message) {
		this.timestamp = timestamp;
		this.source = source;
		this.destination = destination;
		this.message = message;
	}
	@Override
	public void validate() throws IncompleteObjectException {
		if (this.source == null || this.destination == null) {
			throw new IncompleteObjectException("Source or Destination not specified");
		}
	}

	@Override
	public String key() {
		return this.timestamp+":"+this.source+":"+this.destination;
	}

	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj instanceof CachedMessageImpl) {
			CachedMessageImpl m = (CachedMessageImpl)obj;
			if (m.getTimestamp() == getTimestamp() &&
					m.getSource() == null ? getSource() == null : m.getSource().equals(getSource()) &&
					m.getDestination() == null ? getDestination() == null : m.getDestination().equals(getDestination()) &&
					m.getMessage() == null ? getMessage() == null : m.getMessage().equals(getMessage())) {
				equal = true;
			}
		}
		return equal;
	}
	@Override
	public String getSource() {
		return this.source;
	}

	@Override
	public String getDestination() {
		return this.destination;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public void setSource(String src) {
		this.source = src;
	}

	@Override
	public void setDestination(String dst) {
		this.destination = dst;
	}

	@Override
	public void setMessage(String msg) {
		this.message = msg;
	}

	@Override
	public void setTimestamp(long ts) {
		this.timestamp = ts;
		
	}
}
