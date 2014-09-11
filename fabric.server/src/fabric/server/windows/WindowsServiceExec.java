/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.server.windows;

import java.io.File;
import java.util.HashMap;

/**
 * <p>
 * The WindowsServiceExec Class provides an object populated and used by the {@link WindowsService} class for exchanging
 * information required to start Fabric Services
 * </p>
 */
public class WindowsServiceExec {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private String command;
	private File workingDir = new File(".");
	private HashMap<String, String> env = new HashMap<String, String>();

	/**
	 * @param command
	 *            the command to set
	 * @param workingDir
	 *            the workingDir to set
	 * @param env
	 *            the env to set
	 */
	public WindowsServiceExec(String command, File workingDir, HashMap<String, String> env) {

		super();
		this.command = command;
		this.workingDir = workingDir;
		this.env = env;
	}

	/**
	 * @param command
	 *            the command to set
	 * @param workingDir
	 *            the workingDir to set
	 */
	public WindowsServiceExec(String command, File workingDir) {

		super();
		this.command = command;
		this.workingDir = workingDir;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public WindowsServiceExec(String command) {

		super();
		this.command = command;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {

		return command;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public void setCommand(String command) {

		this.command = command;
	}

	/**
	 * @return the workingDir
	 */
	public File getWorkingDir() {

		return workingDir;
	}

	/**
	 * @param workingDir
	 *            the workingDir to set
	 */
	public void setWorkingDir(File workingDir) {

		this.workingDir = workingDir;
	}

	/**
	 * @return the env
	 */
	public HashMap<String, String> getEnv() {

		return env;
	}

	/**
	 * @param env
	 *            the env to set
	 */
	public void setEnv(HashMap<String, String> env) {

		this.env = env;
	}
}
