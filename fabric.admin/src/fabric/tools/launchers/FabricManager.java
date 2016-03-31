/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.launchers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class FabricManager {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	protected static void stopNode(String node) throws Exception {

		sendNodeCommand(node, "shutdown");
	}

	private static void sendNodeCommand(String node, String command) throws IOException {

		File pidFile = new File(FabricLauncher.PID_DIR + "/.fm." + node);
		if (pidFile.exists()) {
			PrintWriter bw = null;
			InputStream sockIn = null;
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(pidFile));
				String procInfo = br.readLine();
				br.close();
				br = null;
				String[] parts = procInfo.split(":");
				if (parts.length == 3 && parts[0].equals(node)) {
					String key = parts[1];
					int port = Integer.parseInt(parts[2]);
					Socket so = new Socket("localhost", port);
					sockIn = so.getInputStream();
					bw = new PrintWriter(so.getOutputStream());
					bw.println(key);
					bw.println(command);
					bw.flush();
					try {
						sockIn.read();
					} catch (Exception e) {
					}
					bw.close();
				}
			} catch (ConnectException ce) {
				// Cannot connect - must be a stale pid file
				pidFile.delete();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (Exception e) {
					}
				}
				if (bw != null) {
					try {
						bw.close();
					} catch (Exception e) {
					}
				}
				if (sockIn != null) {
					try {
						sockIn.close();
					} catch (Exception e) {
					}
				}
			}
		} else {
			throw new FileNotFoundException("Cannot find pid file for node " + node);
		}
	}

}
