/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

/**
 * Instantiates a suitable XML tokenizer for the host JVM.
 * <p>
 * If SAX is available then the SAX-based tokenizer is used, otherwise the simple XML tokenizer will be instantiated.
 * </p>
 */
public class XMLTokenizerFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

	/*
	 * Class static fields
	 */

	/** Flag indicating if SAX is available */
	private static boolean isSAXAvailable = false;

	/** Flag indicating if SAX should be used (can only be set if SAX is available) */
	private static boolean tokenizeWithSAX = false;

	/*
	 * Class static initialization
	 */

	static {

		try {

			/* Check if SAX is available */
			Class.forName("org.xml.sax.helpers.DefaultHandler", false, XMLTokenizerFactory.class.getClassLoader());

			/* If we get here then it must be available */
			isSAXAvailable = true;

		} catch (ClassNotFoundException e) {

			/* No SAX, so use the built-in simple tokenizer */
			isSAXAvailable = false;

		}
	}

	/*
	 * Class static methods
	 */

	/**
	 * Don't allow this class to be instantiated.
	 */
	private XMLTokenizerFactory() {

	}

	/**
	 * Get an XML tokenizer.
	 */
	public static final IXMLTokenizer getTokenizer() {

		IXMLTokenizer tokenizer = null;

		if (tokenizeWithSAX) {
			/* Use the SAX-based tokenizer */
			tokenizer = new fabric.core.xml.sax.SAXXMLTokenizer();
		} else {
			/* No SAX, so use the built-in simple tokenizer */
			tokenizer = new fabric.core.xml.nosax.SimpleXMLTokenizer();
		}

		return tokenizer;

	}

	/**
	 * Sets the flag indicating that the factory should return a SAX-based tokenizer (the alternative is the lightweight
	 * built-in tokenizer).
	 * <p>
	 * <strong>Note: </strong>this call will have no effect if a SAX tokenizer is not available.
	 * </p>
	 * 
	 * @param tokenizeWithSAX
	 *            <code>true</code> if the factory should return a SAX-based tokenizer; <code>false</code> otherwise.
	 */
	public static void setSAXTokenizer(boolean tokenizeWithSAX) {

		if (tokenizeWithSAX) {

			if (!isSAXAvailable) {

				tokenizeWithSAX = false;

			}

		}

		XMLTokenizerFactory.tokenizeWithSAX = tokenizeWithSAX;

	}

	/**
	 * Gets the flag indicating if the factory will return a SAX-based tokenizer (the alternative is the lightweight
	 * built-in tokenizer).
	 * 
	 * @return tokenizeWithSAX <code>true</code> if the factory will return a SAX-based tokenizer; <code>false</code>
	 *         otherwise.
	 */
	public static boolean isSAXTokenizer() {

		return tokenizeWithSAX;

	}

}
