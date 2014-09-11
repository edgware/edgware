/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

import java.io.IOException;
import java.io.Reader;

/**
 * A common interface to a variety of XML tokenizers.
 */
public interface IXMLTokenizer {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

	/*
	 * Interface methods
	 */

	/**
	 * Parses the XML on the specified input stream.
	 * 
	 * @param xmlStream
	 *            the input stream.
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public void tokenize(Reader xmlStream) throws IOException, Exception;

	/**
	 * Sets the handler for this tokenizer.
	 * 
	 * @param handler
	 *            the new content handler.
	 */
	public void setHandler(IXMLTokenHandler handler);

	/**
	 * Gets the content handler for this tokenizer.
	 * 
	 * @return the content handler.
	 */
	public IXMLTokenHandler getHandler();
}