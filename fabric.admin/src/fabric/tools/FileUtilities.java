/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * File management utilities:
 * <ul>
 * <li>Copy a file</li>
 * <li>Delete the contents of a directory</li>
 * <li>Delete a directory</li>
 * </ul>
 */
public class FileUtilities {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012, 2014";

	/*
	 * Class methods
	 */

	/**
	 * Copies a file.
	 * 
	 * @param src
	 *            the source file.
	 * 
	 * @param dest
	 *            the destination file.
	 * 
	 * @return <code>true</code> if the number of bytes copied matches the number of bytes in the source file,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws Exception
	 */
	public static boolean copyFile(File src, File dest) throws Exception {

		boolean result = false;

		if (!dest.exists()) {
			dest.createNewFile();
		}

		FileInputStream sis = null;
		FileChannel sc = null;
		FileOutputStream dos = null;
		FileChannel dc = null;

		try {

			sis = new FileInputStream(src);
			sc = sis.getChannel();
			dos = new FileOutputStream(dest);
			dc = dos.getChannel();
			long bytesCopied = sc.transferTo(0, sc.size(), dc);

			if (bytesCopied == sc.size()) {
				result = true;
			}

		} finally {

			if (sc != null) {
				sc.close();
				sis.close();
			}

			if (dc != null) {
				dc.close();
				dos.close();
			}
		}

		return result;
	}

	/**
	 * Delete contents of this directory but not the directory itself.
	 * 
	 * @param dir
	 *            the directory.
	 * 
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	public static boolean deleteDirectoryContents(File dir) {

		boolean result = false;

		try {

			if (dir.exists()) {

				if (dir.isDirectory()) {

					File[] files = dir.listFiles();
					if (files.length == 0) {
						return true;
					}

					for (int i = 0; i < files.length; i++) {
						result = deleteDirectory(files[i]);
					}
				}
			} else {
				result = false;
			}
		} catch (Exception e) {

			System.out.println("Failed deleting directory/files : " + e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Delete a directory and its contents.
	 * 
	 * @param dir
	 *            the directory.
	 * 
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	public static boolean deleteDirectory(File dir) {

		boolean result = false;

		try {

			if (dir.exists()) {

				if (dir.isDirectory()) {

					File[] files = dir.listFiles();

					for (int i = 0; i < files.length; i++) {
						result = deleteDirectory(files[i]);
					}
				}

				result = dir.delete();

			} else {

				result = false;

			}

		} catch (Exception e) {

			System.out.println("Failed deleting directory/files : " + e.getMessage());
			e.printStackTrace();

		}

		return result;
	}
}
