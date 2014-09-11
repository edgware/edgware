/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fabricmanager.osgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class AdminListener extends Thread {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	ServerSocket ssocket;
	String node;
	boolean running;
	String key = null;
	File pidFile = null;

	BundleContext context;

	public AdminListener(BundleContext context, String node) {

		this.context = context;
		this.node = node;
		key = UUID.randomUUID().toString();
		try {
			ssocket = new ServerSocket(0);
			ssocket.setReuseAddress(true);
			pidFile = new File(System.getenv("FABRIC_HOME") + "/pid/.fm." + this.node);
			FileOutputStream fos = new FileOutputStream(pidFile);
			PrintStream ps = new PrintStream(fos);
			ps.print(this.node + ":" + key + ":" + ssocket.getLocalPort());
			ps.flush();
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {

		this.running = false;
		try {
			ssocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			pidFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		running = true;
		while (running) {
			Socket sock = null;
			try {
				sock = ssocket.accept();
			} catch (Exception e) {
				this.shutdown();
				break;
			}
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				boolean authenticated = false;
				while (true) {
					String l = null;
					try {
						l = in.readLine();
					} catch (IOException ioe) {
						break;
					}
					if (l == null) {
						break;
					}
					if (!authenticated) {
						if (l.equals(key)) {
							authenticated = true;
						} else {
							break;
						}
					} else {
						if (l.equals("shutdown")) {
							Bundle bs = context.getBundle(0);
							try {
								bs.stop();
								System.exit(0);
							} catch (BundleException e) {
							}
						}
					}
				}
			} catch (IOException ioe) {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e2) {
					}
					try {
						sock.close();
					} catch (Exception e2) {
					}
				}
			}
		}

		try {
			ssocket.close();
		} catch (Exception e) {
		}
	}

}