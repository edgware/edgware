/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.traceroute;

import java.util.Date;

public class TraceRecord {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	private String nodeName;
	private Date timeStamp;

	public TraceRecord(String name, Date time) {

		nodeName = name;
		timeStamp = time;
	}

	public String nodeName() {

		return nodeName;
	}

	public Date timeStamp() {

		return timeStamp;
	}
}
